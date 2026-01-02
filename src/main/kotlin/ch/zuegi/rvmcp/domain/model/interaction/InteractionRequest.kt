package ch.zuegi.rvmcp.domain.model.interaction

import java.time.Instant

/**
 * Represents a request for user interaction during workflow execution.
 *
 * When a workflow needs user input, it creates an InteractionRequest that describes:
 * - What type of interaction is needed (ASK_USER, ASK_CATALOG_QUESTION, APPROVAL)
 * - What question to ask
 * - Optional metadata (e.g., catalog question ID, validation rules)
 *
 * This is a Value Object - immutable and self-contained.
 */
data class InteractionRequest(
    val type: InteractionType,
    val question: String,
    val questionId: String? = null,
    val context: Map<String, String> = emptyMap(),
    val requestedAt: Instant = Instant.now(),
) {
    init {
        require(question.isNotBlank()) { "Question cannot be blank" }
        require(type != InteractionType.ASK_CATALOG_QUESTION || questionId != null) {
            "questionId is required for ASK_CATALOG_QUESTION type"
        }
    }

    companion object {
        fun askUser(
            question: String,
            context: Map<String, String> = emptyMap(),
        ): InteractionRequest =
            InteractionRequest(
                type = InteractionType.ASK_USER,
                question = question,
                context = context,
            )

        fun askCatalogQuestion(
            questionId: String,
            question: String,
            context: Map<String, String> = emptyMap(),
        ): InteractionRequest =
            InteractionRequest(
                type = InteractionType.ASK_CATALOG_QUESTION,
                question = question,
                questionId = questionId,
                context = context,
            )

        fun requestApproval(
            question: String,
            context: Map<String, String> = emptyMap(),
        ): InteractionRequest =
            InteractionRequest(
                type = InteractionType.APPROVAL,
                question = question,
                context = context,
            )
    }
}
