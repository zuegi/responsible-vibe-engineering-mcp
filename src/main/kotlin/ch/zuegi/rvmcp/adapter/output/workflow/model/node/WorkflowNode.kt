package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Base class for all workflow nodes.
 * Uses Jackson polymorphism for type-safe YAML deserialization.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = LLMNode::class, name = "llm"),
    JsonSubTypes.Type(value = GetQuestionNode::class, name = "get_question"),
    JsonSubTypes.Type(value = AskCatalogQuestionNode::class, name = "ask_catalog_question"),
    JsonSubTypes.Type(value = ValidateAnswerNode::class, name = "validate_answer"),
    JsonSubTypes.Type(value = ConditionalNode::class, name = "conditional"),
    JsonSubTypes.Type(value = HumanInteractionNode::class, name = "human_interaction"),
    JsonSubTypes.Type(value = AggregationNode::class, name = "aggregation"),
    JsonSubTypes.Type(value = SystemCommandNode::class, name = "system_command"),
)
sealed class WorkflowNode {
    abstract val id: String
    abstract val description: String?

    /**
     * Returns the node type for logging/debugging
     */
    abstract fun getNodeType(): NodeType
}
