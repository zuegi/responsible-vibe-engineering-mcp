package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import kotlinx.coroutines.ThreadContextElement
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext element for storing pending interaction requests.
 *
 * This is a coroutine-safe mechanism for tools to signal workflow interruptions
 * without relying on ThreadLocal, which doesn't work reliably with Kotlin Coroutines
 * (especially when switching dispatchers).
 *
 * Uses AtomicReference for thread-safe mutations across coroutine contexts.
 *
 * Usage:
 * ```
 * val interactionContext = InteractionContextElement()
 * withContext(interactionContext) {
 *     // Workflow execution
 *     // Tools can set: coroutineContext[InteractionContextElement]?.setRequest(request)
 * }
 * // After withContext, check: interactionContext.hasRequest()
 * ```
 */
class InteractionContextElement(
    initialRequest: InteractionRequest? = null,
    var executionId: String? = null,
) : ThreadContextElement<InteractionRequest?>,
    CoroutineContext.Element {
    // Use AtomicReference for thread-safe access
    private val pendingRequest = AtomicReference<InteractionRequest?>(initialRequest)

    companion object Key : CoroutineContext.Key<InteractionContextElement>

    override val key: CoroutineContext.Key<*>
        get() = Key

    /**
     * Signal that workflow should pause for user interaction.
     */
    fun setRequest(request: InteractionRequest) {
        pendingRequest.set(request)
    }

    /**
     * Check if there's a pending interaction request.
     */
    fun hasRequest(): Boolean = pendingRequest.get() != null

    /**
     * Get and clear the pending interaction request.
     */
    fun consumeRequest(): InteractionRequest? = pendingRequest.getAndSet(null)

    /**
     * Clear any pending interaction.
     */
    fun clear() {
        pendingRequest.set(null)
    }

    // ThreadContextElement implementation
    override fun updateThreadContext(context: CoroutineContext): InteractionRequest? {
        // Save current value when entering this thread
        return pendingRequest.get()
    }

    override fun restoreThreadContext(
        context: CoroutineContext,
        oldState: InteractionRequest?,
    ) {
        // Restore old value when leaving this thread
        // Note: We don't restore here because we want to keep changes made during execution
        // The AtomicReference ensures thread-safety
    }
}

/**
 * Helper object for backward compatibility and simplified access.
 * This provides a similar API to the old ThreadLocal-based approach.
 */
@Deprecated(
    "Use coroutineContext[InteractionContextElement] directly",
    ReplaceWith("coroutineContext[InteractionContextElement]"),
)
object WorkflowInterruptionSignal {
    /**
     * @deprecated Use coroutineContext[InteractionContextElement] instead
     */
    @Deprecated("Use CoroutineContext approach", level = DeprecationLevel.WARNING)
    @Suppress("UNUSED_PARAMETER")
    fun requestInteraction(request: InteractionRequest): Unit =
        throw UnsupportedOperationException(
            "ThreadLocal approach deprecated. Use coroutineContext[InteractionContextElement]?.setRequest()",
        )

    @Deprecated("Use CoroutineContext approach", level = DeprecationLevel.WARNING)
    fun hasPendingInteraction(): Nothing =
        throw UnsupportedOperationException(
            "ThreadLocal approach deprecated. Use coroutineContext[InteractionContextElement]?.hasRequest()",
        )

    @Deprecated("Use CoroutineContext approach", level = DeprecationLevel.WARNING)
    fun consumePendingInteraction(): Nothing =
        throw UnsupportedOperationException(
            "ThreadLocal approach deprecated. Use coroutineContext[InteractionContextElement]?.consumeRequest()",
        )

    @Deprecated("Use CoroutineContext approach", level = DeprecationLevel.WARNING)
    fun clear(): Nothing =
        throw UnsupportedOperationException(
            "ThreadLocal approach deprecated. Use coroutineContext[InteractionContextElement]?.clear()",
        )
}
