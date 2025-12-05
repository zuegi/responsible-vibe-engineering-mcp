package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Conditional node - branching logic
 */
@JsonTypeName("CONDITIONAL")
data class ConditionalNode(
    override val id: String,
    override val description: String? = null,
    val condition: String,
    val if_true: String,
    val if_false: String,
) : WorkflowNode() {
    override fun getNodeType() = NodeType.CONDITIONAL
}
