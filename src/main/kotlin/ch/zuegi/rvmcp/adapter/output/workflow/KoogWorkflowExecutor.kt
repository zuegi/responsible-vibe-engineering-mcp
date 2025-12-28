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
import ch.zuegi.rvmcp.adapter.output.workflow.tools.AskUserTool
import ch.zuegi.rvmcp.adapter.output.workflow.tools.CreateFileTool
import ch.zuegi.rvmcp.adapter.output.workflow.tools.QuestionCatalogTool
import ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog.QuestionCatalog
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary
import ch.zuegi.rvmcp.infrastructure.config.LlmProperties
import ch.zuegi.rvmcp.shared.rvmcpLogger
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

/**
 * Koog Workflow Executor that uses a single agent for entire workflow execution.
 *
 * Key features:
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
@Component
class KoogWorkflowExecutor(
    private val llmProperties: LlmProperties,
    private val askUserTool: ai.koog.agents.core.tools.SimpleTool<*>? = null,
) : WorkflowExecutionPort {
    private val logger by rvmcpLogger()
    private var lastExecution: WorkflowExecutionResult? = null

    // Helper classes (internal to this adapter)
    private val templateParser = WorkflowTemplateParser()
    private val strategyTranslator = YamlToKoogStrategyTranslator()
    private val promptBuilder = WorkflowPromptBuilder()

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

    override suspend fun executeWorkflow(
        template: String,
        context: ExecutionContext,
    ): WorkflowExecutionResult {
        val startTime = Instant.now()

        logger.info("▶ Executing Koog Workflow: $template")

        // 1. Parse and validate YAML workflow
        val workflowTemplate = templateParser.parseTemplate(template)
        templateParser.validateTemplate(workflowTemplate)

        val llmNodeCount = workflowTemplate.nodes.count { it.getNodeType() == NodeType.LLM }
        logger.info("Template: ${workflowTemplate.name}")
        logger.info("Total nodes: ${workflowTemplate.nodes.size} ($llmNodeCount LLM nodes)")

        // 2. Translate YAML to Koog Strategy
        logger.info("Translating YAML to Koog Strategy...")
        val strategy = strategyTranslator.translate(workflowTemplate)
        logger.info("Strategy created with $llmNodeCount LLM nodes")

        // 3. Build comprehensive system prompt
        logger.info("Building workflow system prompt...")
        val systemPrompt = promptBuilder.buildNodeSpecificInstructions(workflowTemplate.nodes)

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
                        // FIXME Warum OpenAIModels.Chat.GBT40
                        model = OpenAIModels.Chat.GPT4o,
                        // Generous iterations to allow multiple tool calls per node
                        // Formula: base (10) + nodes (llmNodeCount * 20) to allow ~5-10 tool calls per node
                        maxAgentIterations = 10 + (llmNodeCount * 20),
                    ),
                toolRegistry =
                    ToolRegistry {
                        // Register ask_user tool (configurable for tests)
                        val userTool = askUserTool ?: AskUserTool()
                        tool(userTool)
                        logger.info("Registered ask_user tool (${userTool.javaClass.simpleName})")
                        tool(CreateFileTool { context.projectPath })
                        logger.info("Registered create_file tool for user interaction")
                        //  TODO korrekter Pfad angeben, die Daten sind aktuell noch hard codiert im QuestionCatalogk
                        tool(QuestionCatalogTool(QuestionCatalog.fromFile("src/main/resources/users.json")))
                        logger.info("Registered get_question tool for query a catalog")
                    },
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

        // Log full agent response
        logger.info("Workflow result:\n{}", agentResponse)

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

        logger.info("Workflow '${workflowTemplate.name}' completed successfully")
        logger.info("   Agent creation: ${agentCreationTime}ms")
        logger.info("   Workflow execution: ${workflowDuration}ms")
        logger.info("   Total duration: ${System.currentTimeMillis() - startTime.toEpochMilli()}ms")

        return result
    }

    override suspend fun getSummary(): WorkflowSummary =
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
        val llmNodes = workflow.nodes.filter { it.getNodeType() == NodeType.LLM }

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
        val llmNodeCount = workflow.nodes.count { it.getNodeType() == NodeType.LLM }

        return """
Workflow: ${workflow.name}
Project: ${context.projectPath}
Branch: ${context.gitBranch ?: "main"}

Executed $llmNodeCount LLM steps using Koog AIAgent with context preservation.

---

$agentResponse
            """.trimIndent()
    }
}
