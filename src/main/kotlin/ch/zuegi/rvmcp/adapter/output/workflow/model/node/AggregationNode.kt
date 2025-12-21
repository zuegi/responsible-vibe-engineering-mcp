package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType

/**
 * Node that aggregates multiple inputs into a single output.
 * Used for combining results from previous nodes.
 */
data class AggregationNode(
    override val id: String,
    override val description: String?,
    val inputs: List<String>,
    val output: String,
) : WorkflowNode() {
    override fun getNodeType(): NodeType = NodeType.AGGREGATION
}
