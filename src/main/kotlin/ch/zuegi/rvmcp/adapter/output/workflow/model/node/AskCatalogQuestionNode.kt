package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Ask Catalog Question node - asks user a question from catalog
 */
@JsonTypeName("ASK_CATALOG_QUESTION")
data class AskCatalogQuestionNode(
    override val id: String,
    override val description: String? = null,
    val questionId: String,
    val retryOnInvalid: Boolean = true,
    val maxRetries: Int = 3,
    val skipIfAnswered: Boolean = false,
    val required: Boolean = true,
) : WorkflowNode() {
    override fun getNodeType() = NodeType.ASK_CATALOG_QUESTION
}
