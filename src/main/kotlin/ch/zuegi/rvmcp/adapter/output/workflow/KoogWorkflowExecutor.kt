package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Kotlin Koog-based workflow executor.
 *
 * Executes YAML workflow templates using Kotlin Koog's LLM orchestration.
 *
 * Architecture:
 * 1. Loads YAML template
 * 2. Parses to WorkflowTemplate
 * 3. Converts to Koog workflow graph
 * 4. Executes with LLM calls
 * 5. Returns WorkflowExecutionResult
 *
 * TODO: Currently a simplified implementation.
 * Full Koog integration will be added in next phase.
 */
@Component
class KoogWorkflowExecutor(
    private val templateParser: WorkflowTemplateParser,
) : WorkflowExecutionPort {
    private var lastExecution: WorkflowExecutionResult? = null
    private val executionState = mutableMapOf<String, Any>()

    override fun executeWorkflow(
        template: String,
        context: ExecutionContext,
    ): WorkflowExecutionResult {
        val startTime = Instant.now()

        println("\n▶ Executing Koog Workflow: $template")

        // 1. Load and parse template
        val workflowTemplate = templateParser.parseTemplate(template)
        templateParser.validateTemplate(workflowTemplate)

        println("  Template: ${workflowTemplate.name}")
        println("  Description: ${workflowTemplate.description}")
        println("  Nodes: ${workflowTemplate.nodes.size}")

        // 2. Execute workflow nodes
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

            println("\n  → Executing node: ${node.id} (${node.type})")
            println("     ${node.description}")

            // Execute node based on type
            when (node.type) {
                NodeType.LLM -> currentNodeId = executeLLMNode(node, context, decisions, workflowTemplate)
                NodeType.CONDITIONAL -> currentNodeId = executeConditionalNode(node, workflowTemplate)
                NodeType.HUMAN_INTERACTION -> currentNodeId = executeHumanInteractionNode(node, workflowTemplate)
                NodeType.AGGREGATION -> currentNodeId = executeAggregationNode(node, workflowTemplate)
                NodeType.SYSTEM_COMMAND -> currentNodeId = executeSystemCommandNode(node, workflowTemplate)
            }
        }

        // 3. Process final node (end)
        val endNode = workflowTemplate.nodes.find { it.id == workflowTemplate.graph.end }
        if (endNode != null) {
            println("\n  → Finishing workflow: ${endNode.id}")
        }

        // 4. Generate summary from outputs
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
        return result
    }

    override fun getSummary(): WorkflowSummary {
        return WorkflowSummary(
            compressed = lastExecution?.summary ?: "",
            decisions = lastExecution?.decisions ?: emptyList(),
            keyInsights = lastExecution?.decisions?.map { it.decision } ?: emptyList(),
        )
    }

    // ========== Node Execution Methods ==========

    private fun executeLLMNode(
        node: WorkflowNode,
        context: ExecutionContext,
        decisions: MutableList<Decision>,
        template: WorkflowTemplate,
    ): String {
        // TODO: Implement actual Koog LLM call
        // For now: simulate with console interaction

        println("\n     [LLM Prompt]:")
        val prompt = interpolateVariables(node.prompt!!, context)
        println("     ${prompt.take(200)}...")

        // Simulate LLM response
        print("\n     Enter simulated LLM response (or press Enter for default): ")
        val response =
            readlnOrNull()?.takeIf { it.isNotBlank() }
                ?: "Simulated response for ${node.id}"

        // Store output in execution state
        node.output?.let { outputKey ->
            executionState[outputKey] = response
        }

        // Record decision if this node generates one
        decisions.add(
            Decision(
                phase = template.name,
                decision = "Completed ${node.id}",
                reasoning = "LLM analysis",
            ),
        )

        // Find next node
        return findNextNode(node.id, template)
    }

    private fun executeConditionalNode(
        node: WorkflowNode,
        template: WorkflowTemplate,
    ): String {
        // TODO: Implement condition evaluation
        // For now: ask user

        println("\n     Condition: ${node.condition}")
        print("     Evaluate condition as true? (y/n): ")
        val result = readlnOrNull()?.lowercase() == "y"

        return if (result) {
            node.ifTrue ?: findNextNode(node.id, template)
        } else {
            node.ifFalse ?: findNextNode(node.id, template)
        }
    }

    private fun executeHumanInteractionNode(
        node: WorkflowNode,
        template: WorkflowTemplate,
    ): String {
        println("\n     [Human Input Required]")
        println("     ${node.prompt}")

        print("\n     Your input: ")
        val input = readlnOrNull() ?: ""

        node.output?.let { outputKey ->
            executionState[outputKey] = input
        }

        return findNextNode(node.id, template)
    }

    private fun executeAggregationNode(
        node: WorkflowNode,
        template: WorkflowTemplate,
    ): String {
        println("\n     Aggregating inputs: ${node.inputs}")

        // Collect inputs from execution state
        val aggregatedData =
            node.inputs?.mapNotNull { inputKey ->
                executionState[inputKey]
            } ?: emptyList()

        node.output?.let { outputKey ->
            executionState[outputKey] = aggregatedData
        }

        return findNextNode(node.id, template)
    }

    private fun executeSystemCommandNode(
        node: WorkflowNode,
        template: WorkflowTemplate,
    ): String {
        println("\n     [System Command]: ${node.command}")
        println("     Expected: ${node.expectedOutput}")

        // TODO: Actually execute command
        print("     Simulate success? (y/n): ")
        val success = readlnOrNull()?.lowercase() == "y"

        return if (success) {
            findNextNode(node.id, template)
        } else {
            node.onFailure ?: findNextNode(node.id, template)
        }
    }

    // ========== Helper Methods ==========

    private fun findNextNode(
        currentNodeId: String,
        template: WorkflowTemplate,
    ): String {
        // Find edge from current node
        val edge =
            template.graph.edges.find { it.from == currentNodeId }
                ?: throw IllegalStateException("No outgoing edge from node: $currentNodeId")

        return edge.to
    }

    private fun interpolateVariables(
        text: String,
        context: ExecutionContext,
    ): String {
        var result = text

        // Replace context variables
        result = result.replace("{{context.project_path}}", context.projectPath)
        result = result.replace("{{context.git_branch}}", context.gitBranch ?: "main")

        // Replace execution state variables
        executionState.forEach { (key, value) ->
            result = result.replace("{{$key}}", value.toString())
        }

        return result
    }

    private fun generateSummary(
        template: WorkflowTemplate,
        context: ExecutionContext,
    ): String {
        return """
            Workflow: ${template.name}
            Description: ${template.description}
            Project: ${context.projectPath}
            Branch: ${context.gitBranch}
            
            Executed ${executionState.size} nodes successfully.
            
            Results available in execution state.
            """.trimIndent()
    }
}
