package ch.zuegi.rvmcp.adapter.output.workflow.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Root model for YAML workflow templates.
 */
data class WorkflowTemplate(
    val name: String,
    val description: String,
    val version: String,
    @JsonProperty("context_variables")
    val contextVariables: List<String> = emptyList(),
    val nodes: List<WorkflowNode>,
    val graph: WorkflowGraph,
    val outputs: WorkflowOutputs? = null,
    @JsonProperty("vibe_checks")
    val vibeChecks: List<VibeCheckDefinition> = emptyList(),
)

/**
 * A single node in the workflow (e.g., LLM call, conditional, human interaction).
 */
data class WorkflowNode(
    val id: String,
    val type: NodeType,
    val description: String,
    val prompt: String? = null,
    val output: String? = null,
    val condition: String? = null,
    @JsonProperty("if_true")
    val ifTrue: String? = null,
    @JsonProperty("if_false")
    val ifFalse: String? = null,
    val inputs: List<String>? = null,
    val command: String? = null,
    @JsonProperty("expected_output")
    val expectedOutput: String? = null,
    @JsonProperty("on_failure")
    val onFailure: String? = null,
    val next: String? = null,
    @JsonProperty("max_iterations")
    val maxIterations: Int? = null,
    val required: Boolean? = null,
    // Für Katalog-basierte Nodes
    val questionId: String? = null, // Referenz zu Frage im Katalog
    val validationRules: List<String>? = null, // Optional: Override Katalog-Rules
    val retryOnInvalid: Boolean = true,
    val maxRetries: Int = 3,
    val skipIfAnswered: Boolean = false,
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
    @JsonProperty("get_queestion")
    GET_QUESTION,

    /** Katalog-Frage stellen */
    @JsonProperty("ask_cataloq_question")
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
