# ADR 0001: CoroutineContext-based Workflow Interruption

**Status:** Accepted  
**Date:** 2026-01-01  
**Decision Makers:** Development Team  
**Related Issues:** Phase 2c - Human Interaction over MCP

## Context

The application needs a mechanism for tools to signal workflow interruptions (e.g., for user interaction) in an async, coroutine-based environment. The Koog framework doesn't propagate exceptions from tools, requiring an alternative signaling mechanism.

### Initial Approach (ThreadLocal)

The first implementation used `ThreadLocal<InteractionRequest?>` to store pending interaction requests:

```kotlin
object WorkflowInterruptionSignal {
    private val pendingInteraction = ThreadLocal<InteractionRequest?>()
    // ...
}
```

### Problems with ThreadLocal

1. **Thread-Context Mismatch:** Kotlin Coroutines can switch threads (especially with `Dispatchers.IO`), causing `ThreadLocal` values to be lost
2. **Dispatcher Switching:** When a coroutine suspends and resumes on a different thread, `ThreadLocal` state doesn't follow
3. **Structured Concurrency:** `ThreadLocal` doesn't integrate with Kotlin's structured concurrency model
4. **Testing Complexity:** ThreadLocal state can leak between tests

## Decision

We will use **Kotlin CoroutineContext with a custom CoroutineContext.Element** instead of ThreadLocal.

### Implementation

```kotlin
class InteractionContextElement(
    private var pendingRequest: InteractionRequest? = null
) : ThreadContextElement<InteractionRequest?>, CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<InteractionContextElement>
    
    override val key: CoroutineContext.Key<*> get() = Key
    
    fun setRequest(request: InteractionRequest) {
        pendingRequest = request
    }
    
    fun consumeRequest(): InteractionRequest? {
        val request = pendingRequest
        pendingRequest = null
        return request
    }
}
```

### Usage Pattern

**In KoogWorkflowExecutor:**
```kotlin
withContext(InteractionContextElement()) {
    agent.run(initialPrompt)
}

val interactionElement = currentCoroutineContext()[InteractionContextElement]
if (interactionElement?.hasRequest() == true) {
    // Handle interaction
}
```

**In McpAwareInteractionAdapter:**
```kotlin
override suspend fun askUser(question: String, context: Map<String, String>): String {
    val request = createInteractionRequest(question, null, context)
    currentCoroutineContext()[InteractionContextElement]?.setRequest(request)
    return "[Awaiting user input: $question]"
}
```

## Consequences

### Positive

✅ **Coroutine-Safe:** Context follows coroutine across thread switches  
✅ **Structured Concurrency:** Integrates with Kotlin's coroutine model  
✅ **Explicit Scope:** `withContext()` provides clear boundaries  
✅ **Better Testing:** Context is isolated per coroutine scope  
✅ **Thread-Safety:** `ThreadContextElement` provides thread-local backup  
✅ **Cleanup Guarantee:** `finally` block ensures cleanup even on exceptions

### Negative

⚠️ **More Verbose:** Requires `withContext()` wrapper  
⚠️ **Breaking Change:** Existing code using `WorkflowInterruptionSignal` needs migration  
⚠️ **Learning Curve:** Developers need to understand CoroutineContext

### Migration Notes

The old `WorkflowInterruptionSignal` object is now deprecated with clear error messages:

```kotlin
@Deprecated("Use CoroutineContext approach", level = DeprecationLevel.WARNING)
object WorkflowInterruptionSignal {
    fun requestInteraction(request: InteractionRequest) {
        throw UnsupportedOperationException(
            "ThreadLocal approach deprecated. Use coroutineContext[InteractionContextElement]?.setRequest()"
        )
    }
}
```

## Alternatives Considered

### 1. Exception-based Flow
**Rejected:** Koog framework doesn't propagate tool exceptions correctly

### 2. Channels/Flow
**Rejected:** Would require significant refactoring of Koog's agent architecture

### 3. Shared Mutable State with Mutex
**Rejected:** More complex than CoroutineContext, doesn't solve dispatcher switching

### 4. Continuation Interceptor
**Rejected:** Too low-level, harder to understand and maintain

## References

- [Kotlin Coroutines: Context and Dispatchers](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html)
- [ThreadContextElement Documentation](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-thread-context-element/)
- [Structured Concurrency](https://kotlinlang.org/docs/coroutines-basics.html#structured-concurrency)

## Related Changes

- `WorkflowInterruptionSignal.kt` - Replaced with `InteractionContextElement`
- `KoogWorkflowExecutor.kt` - Added `withContext(InteractionContextElement())`
- `McpAwareInteractionAdapter.kt` - Uses `currentCoroutineContext()[InteractionContextElement]`
- Tests updated to use new context-based approach

## Validation

- [x] Unit tests pass with new CoroutineContext approach
- [x] No ThreadLocal-related issues in multi-threaded environments
- [x] Context preserved across dispatcher switches
- [x] Cleanup works correctly in exception scenarios

