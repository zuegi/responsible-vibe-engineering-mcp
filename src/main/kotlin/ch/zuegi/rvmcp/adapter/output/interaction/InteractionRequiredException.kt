package ch.zuegi.rvmcp.adapter.output.interaction

import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest

/**
 * Exception thrown when workflow requires user interaction in MCP mode.
 *
 * This signals that the workflow should pause and wait for the MCP client
 * to provide an answer via the `provide_answer` tool.
 *
 * This exception is caught by KoogWorkflowExecutor and converted into a
 * WorkflowExecutionResult with awaitingInput=true.
 */
class InteractionRequiredException(
    val interactionRequest: InteractionRequest,
    message: String = "Workflow requires user interaction: ${interactionRequest.question}",
) : RuntimeException(message) {
    companion object {
        fun askUser(
            question: String,
            context: Map<String, String> = emptyMap(),
        ): InteractionRequiredException =
            InteractionRequiredException(
                InteractionRequest.askUser(question, context),
            )

        fun askCatalogQuestion(
            questionId: String,
            question: String,
            context: Map<String, String> = emptyMap(),
        ): InteractionRequiredException =
            InteractionRequiredException(
                InteractionRequest.askCatalogQuestion(questionId, question, context),
            )

        fun requestApproval(
            question: String,
            context: Map<String, String> = emptyMap(),
        ): InteractionRequiredException =
            InteractionRequiredException(
                InteractionRequest.requestApproval(question, context),
            )
    }
}
