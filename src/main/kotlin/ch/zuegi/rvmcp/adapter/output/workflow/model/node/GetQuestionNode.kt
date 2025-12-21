package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Get Question node - loads question from catalog
 */
@JsonTypeName("GET_QUESTION")
data class GetQuestionNode(
    override val id: String,
    override val description: String? = null,
    val questionId: String,
    val skipIfAnswered: Boolean = false,
) : WorkflowNode() {
    override fun getNodeType() = NodeType.GET_QUESTION
}
