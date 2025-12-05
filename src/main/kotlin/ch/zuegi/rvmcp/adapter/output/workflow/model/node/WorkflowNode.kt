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
    JsonSubTypes.Type(value = LLMNode::class, name = "LLM"),
    JsonSubTypes.Type(value = GetQuestionNode::class, name = "GET_QUESTION"),
    JsonSubTypes.Type(value = AskCatalogQuestionNode::class, name = "ASK_CATALOG_QUESTION"),
    JsonSubTypes.Type(value = ValidateAnswerNode::class, name = "VALIDATE_ANSWER"),
    JsonSubTypes.Type(value = ConditionalNode::class, name = "CONDITIONAL"),
    JsonSubTypes.Type(value = HumanInteractionNode::class, name = "HUMAN_INTERACTION"),
)
sealed class WorkflowNode {
    abstract val id: String
    abstract val description: String?

    /**
     * Returns the node type for logging/debugging
     */
    abstract fun getNodeType(): NodeType
}
