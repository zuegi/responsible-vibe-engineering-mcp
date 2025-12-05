package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Human Interaction node - generic user interaction
 */
@JsonTypeName("HUMAN_INTERACTION")
data class HumanInteractionNode(
    override val id: String,
    override val description: String? = null,
    val prompt: String,
    val inputs: List<String>? = null,
    val command: String? = null,
) : WorkflowNode() {
    override fun getNodeType() = NodeType.HUMAN_INTERACTION
}
