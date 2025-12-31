package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest

/**
 * Thread-safe signal mechanism for tools to communicate workflow interruptions.
 *
 * Since Koog's tool framework doesn't propagate exceptions from tools,
 * we need an alternative mechanism for tools to signal that the workflow
 * should pause (e.g. for user interaction).
 *
 * This uses ThreadLocal to ensure thread-safety in async contexts.
 */
object WorkflowInterruptionSignal {
    private val pendingInteraction = ThreadLocal<InteractionRequest?>()

    /**
     * Signal that workflow should pause for user interaction.
     */
    fun requestInteraction(request: InteractionRequest) {
        pendingInteraction.set(request)
    }

    /**
     * Check if there's a pending interaction request.
     */
    fun hasPendingInteraction(): Boolean = pendingInteraction.get() != null

    /**
     * Get and clear the pending interaction request.
     */
    fun consumePendingInteraction(): InteractionRequest? {
        val request = pendingInteraction.get()
        pendingInteraction.remove()
        return request
    }

    /**
     * Clear any pending interaction (e.g. after workflow completes).
     */
    fun clear() {
        pendingInteraction.remove()
    }
}
