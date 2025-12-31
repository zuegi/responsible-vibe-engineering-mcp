package ch.zuegi.rvmcp.domain.model.interaction

import java.time.Instant

/**
 * Represents a user's response to an InteractionRequest.
 *
 * Contains:
 * - The answer provided by the user
 * - Reference to the original request
 * - Timestamp of when the response was received
 *
 * This is a Value Object - immutable and self-contained.
 */
data class InteractionResponse(
    val request: InteractionRequest,
    val answer: String,
    val respondedAt: Instant = Instant.now(),
) {
    init {
        require(answer.isNotBlank()) { "Answer cannot be blank" }
    }

    /**
     * Time elapsed between request and response.
     */
    fun responseTime(): java.time.Duration = java.time.Duration.between(request.requestedAt, respondedAt)
}
