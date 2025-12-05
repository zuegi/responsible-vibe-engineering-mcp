package ch.zuegi.rvmcp.adapter.output.workflow.model

import ch.zuegi.rvmcp.adapter.output.workflow.model.node.AskCatalogQuestionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.ConditionalNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.GetQuestionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.HumanInteractionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.LLMNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.ValidateAnswerNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.WorkflowNode
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Root model for YAML workflow templates.
 */
data class WorkflowTemplate(
    val name: String,
    val description: String,
    val nodes: List<WorkflowNode>, // Jetzt polymorphe Liste
    val graph: WorkflowGraph,
    @JsonProperty("vibe_checks")
    val vibeChecks: List<VibeCheckDefinition> = emptyList(),
)

/**
 * Node types supported in workflows.
 */
enum class NodeType {
    @JsonProperty("llm")
    LLM,

    @JsonProperty("conditional")
    CONDITIONAL,

    @JsonProperty("human_interaction")
    HUMAN_INTERACTION,

    @JsonProperty("aggregation")
    AGGREGATION,

    @JsonProperty("system_command")
    SYSTEM_COMMAND,

    /** Frage aus Katalog laden */
    @JsonProperty("get_question")
    GET_QUESTION,

    /** Katalog-Frage stellen */
    @JsonProperty("ask_catalog_question")
    ASK_CATALOG_QUESTION,

    /** Antwort validieren */
    @JsonProperty("validate_answer")
    VALIDATE_ANSWER,
}

/**
 * Defines the workflow execution graph.
 */
data class WorkflowGraph(
    val start: String,
    val edges: List<WorkflowEdge>,
    val end: String,
)

/**
 * An edge in the workflow graph.
 */
data class WorkflowEdge(
    val from: String,
    val to: String,
    val condition: String? = null,
)

/**
 * Output definitions for the workflow.
 */
data class WorkflowOutputs(
    val summary: String,
    val artifacts: List<ArtifactDefinition>? = null,
    val decisions: List<DecisionDefinition>? = null,
    @JsonProperty("code_files")
    val codeFiles: List<Map<String, String>>? = null,
)

/**
 * Artifact to be created by the workflow.
 */
data class ArtifactDefinition(
    val name: String,
    val type: String,
    val content: String,
)

/**
 * Decision to be recorded by the workflow.
 */
data class DecisionDefinition(
    val phase: String,
    val decision: String,
    val reasoning: String,
)

/**
 * Vibe check definition in the workflow.
 */
data class VibeCheckDefinition(
    val question: String,
    val type: String,
)

// ============================================================================
// HELPER EXTENSIONS
// ============================================================================

/**
 * Extension functions to safely cast nodes to specific types
 */
fun WorkflowNode.asLLMNode(): LLMNode? = this as? LLMNode

fun WorkflowNode.asGetQuestionNode(): GetQuestionNode? = this as? GetQuestionNode

fun WorkflowNode.asAskCatalogQuestionNode(): AskCatalogQuestionNode? = this as? AskCatalogQuestionNode

fun WorkflowNode.asValidateAnswerNode(): ValidateAnswerNode? = this as? ValidateAnswerNode

fun WorkflowNode.asConditionalNode(): ConditionalNode? = this as? ConditionalNode

fun WorkflowNode.asHumanInteractionNode(): HumanInteractionNode? = this as? HumanInteractionNode

/**
 * Get all LLM nodes from workflow
 */
fun WorkflowTemplate.getLLMNodes(): List<LLMNode> = nodes.filterIsInstance<LLMNode>()

/**
 * Get all catalog nodes from workflow
 */
fun WorkflowTemplate.getCatalogNodes(): List<WorkflowNode> =
    nodes.filter {
        it.getNodeType() in
            listOf(
                NodeType.GET_QUESTION,
                NodeType.ASK_CATALOG_QUESTION,
                NodeType.VALIDATE_ANSWER,
            )
    }

/**
 * Check if workflow uses question catalog
 */
fun WorkflowTemplate.usesCatalog(): Boolean = getCatalogNodes().isNotEmpty()

/**
 * Get node by ID
 */
fun WorkflowTemplate.getNodeById(id: String): WorkflowNode? = nodes.find { it.id == id }

/**
 * Get all node IDs
 */
fun WorkflowTemplate.getNodeIds(): Set<String> = nodes.map { it.id }.toSet()
