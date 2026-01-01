package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext element for storing pending interaction requests.
 *
 * This is a coroutine-safe mechanism for tools to signal workflow interruptions
 * without relying on ThreadLocal, which doesn't work reliably with Kotlin Coroutines
 * (especially when switching dispatchers).
 *
 * Usage:
 * ```
 * withContext(InteractionContextElement()) {
 *     // Workflow execution
 *     // Tools can set: coroutineContext[InteractionContextElement]?.setRequest(request)
 *     // Executor can check: coroutineContext[InteractionContextElement]?.consumeRequest()
 * }
 * ```
 */
class InteractionContextElement(
    private var pendingRequest: InteractionRequest? = null,
) : ThreadContextElement<InteractionRequest?>,
    CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<InteractionContextElement>

    override val key: CoroutineContext.Key<*>
        get() = Key

    /**
     * Signal that workflow should pause for user interaction.
     */
    fun setRequest(request: InteractionRequest) {
        pendingRequest = request
    }

    /**
     * Check if there's a pending interaction request.
     */
    fun hasRequest(): Boolean = pendingRequest != null

    /**
     * Get and clear the pending interaction request.
     */
    fun consumeRequest(): InteractionRequest? {
        val request = pendingRequest
        pendingRequest = null
        return request
    }

    /**
     * Clear any pending interaction.
     */
    fun clear() {
        pendingRequest = null
    }

    // ThreadContextElement implementation (for thread-local backup)
    override fun updateThreadContext(context: CoroutineContext): InteractionRequest? {
        val oldState = pendingRequest
        return oldState
    }

    override fun restoreThreadContext(
        context: CoroutineContext,
        oldState: InteractionRequest?,
    ) {
        pendingRequest = oldState
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
    fun hasPendingInteraction(): Boolean =
        throw UnsupportedOperationException(
            "ThreadLocal approach deprecated. Use coroutineContext[InteractionContextElement]?.hasRequest()",
        )

    @Deprecated("Use CoroutineContext approach", level = DeprecationLevel.WARNING)
    fun consumePendingInteraction(): InteractionRequest? =
        throw UnsupportedOperationException(
            "ThreadLocal approach deprecated. Use coroutineContext[InteractionContextElement]?.consumeRequest()",
        )

    @Deprecated("Use CoroutineContext approach", level = DeprecationLevel.WARNING)
    fun clear(): Unit =
        throw UnsupportedOperationException(
            "ThreadLocal approach deprecated. Use coroutineContext[InteractionContextElement]?.clear()",
        )
}
