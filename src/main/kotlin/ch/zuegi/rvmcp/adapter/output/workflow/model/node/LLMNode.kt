package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("LLM")
class LLMNode(
    override val id: String,
    override val description: String? = null,
    val prompt: String, // Required for LLM nodes
    val tools: List<String>? = null,
    val output: String? = null,
    val expected_output: String? = null,
    val max_iterations: Int? = null,
) : WorkflowNode() {
    override fun getNodeType() = NodeType.LLM
}
