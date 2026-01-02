package ch.zuegi.rvmcp.adapter.output.interaction

import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages pending user interactions with suspend/resume capability.
 *
 * When a tool calls askUser():
 * 1. Creates a CompletableDeferred for the answer
 * 2. Stores the InteractionRequest
 * 3. Suspends until answer is provided via provideAnswer()
 *
 * This allows the agent to truly "pause" and wait for user input.
 */
object PendingInteractionManager {
    private val pendingInteractions = ConcurrentHashMap<String, PendingInteraction>()

    data class PendingInteraction(
        val request: InteractionRequest,
        val answerDeferred: CompletableDeferred<String>,
    )

    /**
     * Suspend until user provides an answer for this interaction.
     * Returns the answer when available.
     */
    suspend fun awaitAnswer(
        executionId: String,
        request: InteractionRequest,
    ): String {
        val deferred = CompletableDeferred<String>()
        pendingInteractions[executionId] = PendingInteraction(request, deferred)
        return deferred.await()
    }

    /**
     * Provide an answer for a pending interaction, resuming the suspended coroutine.
     */
    fun provideAnswer(
        executionId: String,
        answer: String,
    ): Boolean {
        val pending = pendingInteractions.remove(executionId) ?: return false
        pending.answerDeferred.complete(answer)
        return true
    }

    /**
     * Get pending interaction request without providing answer.
     */
    fun getPendingInteraction(executionId: String): InteractionRequest? {
        return pendingInteractions[executionId]?.request
    }

    /**
     * Check if there's a pending interaction for this execution.
     */
    fun hasPendingInteraction(executionId: String): Boolean {
        return pendingInteractions.containsKey(executionId)
    }

    /**
     * Cancel a pending interaction (e.g. on workflow failure).
     */
    fun cancel(executionId: String) {
        pendingInteractions.remove(executionId)?.answerDeferred?.cancel()
    }
}
