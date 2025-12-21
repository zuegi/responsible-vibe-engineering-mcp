package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Validate Answer node - validates user's answer
 */
@JsonTypeName("VALIDATE_ANSWER")
data class ValidateAnswerNode(
    override val id: String,
    override val description: String? = null,
    val questionId: String,
    val validationRules: List<String>? = null,
    val on_failure: String? = null, // What to do on validation failure
) : WorkflowNode() {
    override fun getNodeType() = NodeType.VALIDATE_ANSWER
}
