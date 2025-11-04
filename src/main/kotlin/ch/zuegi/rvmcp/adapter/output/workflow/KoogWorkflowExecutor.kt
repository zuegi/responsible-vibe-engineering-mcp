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
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate
import ch.zuegi.rvmcp.adapter.output.workflow.strategy.YamlWorkflowStrategy
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary
import ch.zuegi.rvmcp.infrastructure.logging.logger
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class KoogWorkflowExecutor(
    private val templateParser: WorkflowTemplateParser,
    @Value("\${OPENAI_API_KEY:#{null}}") private val openAiApiKey: String?,
) : WorkflowExecutionPort {
    private val logger by logger()
    private var lastExecution: WorkflowExecutionResult? = null
    private val executionState = mutableMapOf<String, Any>()

    override fun executeWorkflow(
        template: String,
        context: ExecutionContext,
    ): WorkflowExecutionResult =
        runBlocking {
            val startTime = Instant.now()

            logger.info("▶ Executing Koog Workflow: $template")

            val workflowTemplate = templateParser.parseTemplate(template)
            templateParser.validateTemplate(workflowTemplate)

            logger.info("Template: ${workflowTemplate.name}")
            logger.info("Nodes: ${workflowTemplate.nodes.size}")

            val agent = createKoogAgent(workflowTemplate, context)

            val decisions = mutableListOf<Decision>()
            var currentNodeId = workflowTemplate.graph.start
            val visitedNodes = mutableSetOf<String>()

            while (currentNodeId != workflowTemplate.graph.end) {
                if (currentNodeId in visitedNodes && visitedNodes.size > 100) {
                    throw IllegalStateException("Workflow appears to be in infinite loop")
                }
                visitedNodes.add(currentNodeId)

                val node =
                    workflowTemplate.nodes.find { it.id == currentNodeId }
                        ?: throw IllegalStateException("Node not found: $currentNodeId")

                logger.info("→ Executing node: ${node.id} (${node.type})")

                currentNodeId =
                    when (node.type) {
                        NodeType.LLM -> executeLLMNode(node, context, decisions, workflowTemplate, agent)
                        NodeType.CONDITIONAL -> executeConditionalNode(node, workflowTemplate)
                        NodeType.HUMAN_INTERACTION -> executeHumanInteractionNode(node, workflowTemplate)
                        NodeType.AGGREGATION -> executeAggregationNode(node, workflowTemplate)
                        NodeType.SYSTEM_COMMAND -> executeSystemCommandNode(node, workflowTemplate)
                    }
            }

            val summary = generateSummary(workflowTemplate, context)

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
            result
        }

    override fun getSummary(): WorkflowSummary =
        WorkflowSummary(
            compressed = lastExecution?.summary ?: "",
            decisions = lastExecution?.decisions ?: emptyList(),
            keyInsights = lastExecution?.decisions?.map { it.decision } ?: emptyList(),
        )

    private suspend fun createKoogAgent(
        template: WorkflowTemplate,
        context: ExecutionContext,
    ): AIAgent<String, String> {
        val executor =
            simpleAzureOpenAIExecutor(
                baseUrl = "https://aivs-llm-gateway-aivisorium-dev.apps.cp.rch.cloud/openai/shared-chn/openai/deployments/gpt-4o/",
                version = AzureOpenAIServiceVersion.fromString("2024-05-01-preview"),
                apiToken = "dummy",
            )
        val strategy = YamlWorkflowStrategy.simpleSingleShotStrategy()

        val config =
            AIAgentConfig(
                prompt =
                    prompt("workflow_${template.name}") {
                        system(
                            """
                            You are an AI assistant executing: ${template.name}
                            Project: ${context.projectPath}
                            """.trimIndent(),
                        )
                    },
                model = OpenAIModels.Chat.GPT4o,
                maxAgentIterations = 5,
            )

        return AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = config,
            toolRegistry = ToolRegistry { },
            installFeatures = { install(Tracing) },
        )
    }

    private suspend fun executeLLMNode(
        node: WorkflowNode,
        context: ExecutionContext,
        decisions: MutableList<Decision>,
        template: WorkflowTemplate,
        agent: AIAgent<String, String>,
    ): String {
        val prompt = interpolateVariables(node.prompt!!, context)
        val response: String =
            try {
                agent.run(prompt)
            } catch (e: Exception) {
                logger.error("LLM call failed", e)
                "Error: ${e.message}"
            }

        node.output?.let { executionState[it] = response }
        decisions.add(Decision(template.name, "Completed ${node.id}", "LLM analysis"))

        return findNextNode(node.id, template)
    }

    private fun executeConditionalNode(
        node: WorkflowNode,
        template: WorkflowTemplate,
    ): String = if (evaluateCondition(node.condition!!)) node.ifTrue!! else node.ifFalse!!

    private fun executeHumanInteractionNode(
        node: WorkflowNode,
        template: WorkflowTemplate,
    ): String {
        println("${node.prompt}")
        val input = readlnOrNull() ?: ""
        node.output?.let { executionState[it] = input }
        return findNextNode(node.id, template)
    }

    private fun executeAggregationNode(
        node: WorkflowNode,
        template: WorkflowTemplate,
    ): String {
        val data = node.inputs?.mapNotNull { executionState[it] } ?: emptyList()
        node.output?.let { executionState[it] = data }
        return findNextNode(node.id, template)
    }

    private fun executeSystemCommandNode(
        node: WorkflowNode,
        template: WorkflowTemplate,
    ): String = findNextNode(node.id, template)

    private fun findNextNode(
        currentNodeId: String,
        template: WorkflowTemplate,
    ): String =
        template.graph.edges.find { it.from == currentNodeId }?.to
            ?: throw IllegalStateException("No edge from: $currentNodeId")

    private fun interpolateVariables(
        text: String,
        context: ExecutionContext,
    ): String {
        var result = text
        result = result.replace("{{context.project_path}}", context.projectPath)
        result = result.replace("{{context.git_branch}}", context.gitBranch ?: "main")
        executionState.forEach { (key, value) ->
            result = result.replace("{{$key}}", value.toString())
        }
        return result
    }

    private fun evaluateCondition(condition: String): Boolean {
        val pattern = """contains\((\w+), "([^"]+)"\)""".toRegex()
        val match = pattern.find(condition)
        return if (match != null) {
            val (varName, searchTerm) = match.destructured
            executionState[varName]?.toString()?.contains(searchTerm, true) ?: false
        } else {
            false
        }
    }

    private fun generateSummary(
        template: WorkflowTemplate,
        context: ExecutionContext,
    ): String =
        """
        Workflow: ${template.name}
        Project: ${context.projectPath}
        Executed ${executionState.size} nodes with Koog AIAgent.
        """.trimIndent()
}
