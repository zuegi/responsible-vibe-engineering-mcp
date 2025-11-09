package ch.zuegi.rvmcp.adapter.output.workflow

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.tracing.feature.Tracing
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openai.azure.AzureOpenAIServiceVersion
import ai.koog.prompt.executor.llms.all.simpleAzureOpenAIExecutor
import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary
import ch.zuegi.rvmcp.infrastructure.config.LlmProperties
import ch.zuegi.rvmcp.infrastructure.logging.logger
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

/**
 * Refactored Koog Workflow Executor that uses a single agent for entire workflow execution.
 *
 * Key improvements over old executor:
 * - Single agent run preserves context across all workflow nodes
 * - YAML workflow is translated to Koog Strategy Graph
 * - Dramatically improved performance (no agent creation per node)
 * - LLM can see full conversation history
 *
 * Architecture:
 * 1. Parse YAML workflow template
 * 2. Translate to Koog Strategy using YamlToKoogStrategyTranslator
 * 3. Build comprehensive system prompt with WorkflowPromptBuilder
 * 4. Create single AIAgent with translated strategy
 * 5. Execute agent.run() once for entire workflow
 * 6. Extract results from agent conversation history
 */
@Component("refactoredKoogWorkflowExecutor")
class RefactoredKoogWorkflowExecutor(
    private val templateParser: WorkflowTemplateParser,
    private val strategyTranslator: YamlToKoogStrategyTranslator,
    private val promptBuilder: WorkflowPromptBuilder,
    private val llmProperties: LlmProperties,
) : WorkflowExecutionPort {
    private val logger by logger()
    private var lastExecution: WorkflowExecutionResult? = null

    // Create LLM executor lazily and reuse (expensive to create)
    private val llmExecutor by lazy {
        logger.info("Initializing LLM executor...")
        logger.info("Provider: ${llmProperties.provider}")
        logger.debug("Base URL: ${llmProperties.baseUrl}")
        simpleAzureOpenAIExecutor(
            baseUrl = llmProperties.baseUrl,
            version = AzureOpenAIServiceVersion.fromString(llmProperties.apiVersion),
            apiToken = llmProperties.apiToken,
        ).also {
            logger.info("LLM executor initialized successfully")
        }
    }

    override fun executeWorkflow(
        template: String,
        context: ExecutionContext,
    ): WorkflowExecutionResult =
        runBlocking {
            val startTime = Instant.now()

            logger.info("▶ Executing Koog Workflow (REFACTORED): $template")

            // 1. Parse and validate YAML workflow
            val workflowTemplate = templateParser.parseTemplate(template)
            templateParser.validateTemplate(workflowTemplate)

            val llmNodeCount = workflowTemplate.nodes.count { it.type == NodeType.LLM }
            logger.info("Template: ${workflowTemplate.name}")
            logger.info("Total nodes: ${workflowTemplate.nodes.size} ($llmNodeCount LLM nodes)")

            // 2. Translate YAML to Koog Strategy
            logger.info("Translating YAML to Koog Strategy...")
            val strategy = strategyTranslator.translate(workflowTemplate)
            logger.info("Strategy created with $llmNodeCount LLM nodes")

            // 3. Build comprehensive system prompt
            logger.info("Building workflow system prompt...")
            val systemPrompt = promptBuilder.buildWorkflowSystemPrompt(workflowTemplate, context)

            // 4. Create single agent for entire workflow
            logger.info("Creating Koog agent for entire workflow...")
            val agentStartTime = System.currentTimeMillis()

            val agent =
                AIAgent<String, String>(
                    promptExecutor = llmExecutor,
                    strategy = strategy,
                    agentConfig =
                        AIAgentConfig(
                            prompt =
                                prompt("workflow_${workflowTemplate.name}") {
                                    system(systemPrompt)
                                },
                            model = OpenAIModels.Chat.GPT4o,
                            // Generous iterations: start + (LLM nodes * 2) + finish
                            maxAgentIterations = 1 + (llmNodeCount * 2) + 1,
                        ),
                    toolRegistry = ToolRegistry { },
                    installFeatures = { install(Tracing) },
                )

            val agentCreationTime = System.currentTimeMillis() - agentStartTime
            logger.info("Agent created in ${agentCreationTime}ms")

            // 5. Execute entire workflow in single agent run
            logger.info("Starting workflow execution...")
            val workflowStartTime = System.currentTimeMillis()

            val initialPrompt = promptBuilder.buildInitialPrompt(workflowTemplate, context)
            val agentResponse =
                try {
                    agent.run(initialPrompt)
                } catch (e: Exception) {
                    logger.error("Workflow execution failed", e)
                    throw IllegalStateException("Workflow execution failed: ${e.message}", e)
                }

            val workflowDuration = System.currentTimeMillis() - workflowStartTime
            logger.info("Workflow completed in ${workflowDuration}ms")

            // 6. Extract results and create decisions
            val decisions = extractDecisions(workflowTemplate, agentResponse)

            val summary = generateSummary(workflowTemplate, context, agentResponse)

            val result =
                WorkflowExecutionResult(
                    success = true,
                    summary = summary,
                    decisions = decisions,
                    vibeCheckResults = emptyList(),
                    startedAt = startTime,
                    completedAt = Instant.now(),
                )

            lastExecution = result

            logger.info("✅ Workflow '${workflowTemplate.name}' completed successfully")
            logger.info("   Agent creation: ${agentCreationTime}ms")
            logger.info("   Workflow execution: ${workflowDuration}ms")
            logger.info("   Total duration: ${System.currentTimeMillis() - startTime.toEpochMilli()}ms")

            result
        }

    override fun getSummary(): WorkflowSummary =
        WorkflowSummary(
            compressed = lastExecution?.summary ?: "",
            decisions = lastExecution?.decisions ?: emptyList(),
            keyInsights = lastExecution?.decisions?.map { it.decision } ?: emptyList(),
        )

    /**
     * Extracts decisions from agent response.
     * For now, creates a decision per LLM node that was executed.
     */
    private fun extractDecisions(
        workflow: ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate,
        agentResponse: String,
    ): List<Decision> {
        val llmNodes = workflow.nodes.filter { it.type == NodeType.LLM }

        return llmNodes.map { node ->
            Decision(
                phase = workflow.name,
                decision = "Completed step: ${node.id}",
                reasoning = node.description ?: "No description",
                date = LocalDate.now(),
            )
        }
    }

    /**
     * Generates workflow summary from template and agent response.
     */
    private fun generateSummary(
        workflow: ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate,
        context: ExecutionContext,
        agentResponse: String,
    ): String {
        val llmNodeCount = workflow.nodes.count { it.type == NodeType.LLM }

        return """
Workflow: ${workflow.name}
Project: ${context.projectPath}
Branch: ${context.gitBranch ?: "main"}

Executed $llmNodeCount LLM steps using Koog AIAgent with context preservation.

Agent Response Summary:
${agentResponse.take(500)}${if (agentResponse.length > 500) "..." else ""}
            """.trimIndent()
    }
}
