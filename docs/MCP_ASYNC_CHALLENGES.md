# MCP Server Async Execution Challenges

## Problem Statement

Long-running operations (e.g., LLM workflow execution taking >30 seconds) cause **timeout issues** in MCP client-server communication, even when using Kotlin Coroutines and suspend functions.

## Timeline of Investigation

### Initial Problem
- **Symptom**: `execute_phase` tool call never returns response to client (Warp Agent)
- **Duration**: LLM workflow takes 9-30 seconds, client times out after ~30-60 seconds
- **Impact**: Server appears hung, client cancels request

### Attempted Solutions

#### 1. `runBlocking` with `Dispatchers.IO` ❌
```kotlin
val phaseResult = runBlocking(Dispatchers.IO) {
    executePhaseUseCase.execute(currentPhase, context)
}
```
**Result**: Blocked MCP server thread, caused deadlock

#### 2. `GlobalScope.launch` with Async Job Pattern ⚠️
```kotlin
val jobId = jobManager.createJob()
GlobalScope.launch(Dispatchers.IO) {
    val phaseResult = executePhaseUseCase.execute(currentPhase, context)
    jobManager.completeJob(jobId, phaseResult)
}
// Return job ID immediately
```
**Result**: Server returned job ID immediately, but subsequent `get_phase_result` calls also timed out

#### 3. Direct suspend function calls ❌
```kotlin
// MCP SDK has first-class coroutine support
val processExecution = startProcessUseCase.execute(...)
```
**Result**: Compiled successfully, but still blocked on long operations

#### 4. `withContext(Dispatchers.IO)` ❌
```kotlin
val phaseResult = withContext(Dispatchers.IO) {
    executePhaseUseCase.execute(currentPhase, context)
}
```
**Result**: Still blocked, no response to client

#### 5. `server.connect()` instead of `createSession()` ❌
```kotlin
suspend fun start() {
    val transport = StdioServerTransport(...)
    server.connect(transport) // Correct suspend function API
}
```
**Result**: Fixed server initialization, but long operations still timeout

## Root Cause Analysis

### MCP SDK Architecture
- ✅ **MCP Kotlin SDK 0.7.6** has **full coroutine support**
- ✅ All API methods are **suspend functions**
- ✅ Tool handlers can call suspend functions directly
- ✅ `server.connect()` starts async event loop

### The Real Problem: Request/Response Protocol
**MCP Protocol is request/response based:**
```
Client sends request → Server processes → Server sends response
```

**Constraints:**
1. Client expects response within timeout window (~30-60 seconds)
2. Long-running operations exceed this window
3. Even with coroutines, the **response cannot be sent until operation completes**
4. Client timeout occurs before LLM workflow finishes

### Why Coroutines Don't Solve This
Coroutines make operations **non-blocking on the server**, but:
- ❌ The **MCP protocol** still requires a synchronous response
- ❌ The **client** waits for the response and times out
- ❌ No built-in streaming or progress updates in basic tool calls

## Evidence from Logs

### Server Logs (Successful Execution Start)
```
2025-11-14 13:59:06 | MCP: Sending request: execute_phase
⏳ Executing phase: Requirements Analysis...
   This may take a while (LLM workflow execution)
```

### Client Behavior
- Request sent successfully
- Server starts processing
- **No response received within timeout**
- Client cancels request

### LLM Performance
- `SimpleLLMConnectionTest`: **323ms** ✅
- `simple-test.yml` workflow: **381ms** ✅
- `requirements-analysis.yml` workflow: **8973ms** ⚠️

**Conclusion**: 9-second workflows exceed client timeout threshold.

## The Solution: Async Job Pattern

### Architecture
```
execute_phase Request
    ↓
Server creates Job ID
    ↓
Server launches background coroutine
    ↓ (returns immediately)
Response: { jobId: "job-123", status: "RUNNING" }
    
Background coroutine:
    ↓
executePhaseUseCase.execute() (9 seconds)
    ↓
Store result in JobManager
    
Client polls:
    ↓
get_phase_result(jobId) → { status: "RUNNING" }
get_phase_result(jobId) → { status: "COMPLETED", result: ... }
```

### Why This Works
1. ✅ `execute_phase` returns **immediately** (no timeout)
2. ✅ Background coroutine runs independently
3. ✅ Client can poll without blocking
4. ✅ No protocol violation (all requests/responses are fast)

### Implementation Requirements
1. **JobManager**: Thread-safe storage for job state
2. **Background Execution**: `launch(Dispatchers.IO)` for LLM calls
3. **New Tool**: `get_phase_result(jobId)` for polling
4. **Job Lifecycle**: Running → Completed/Failed

## Lessons Learned

### ✅ What Works
- MCP SDK coroutine support is excellent
- `withContext(Dispatchers.IO)` for CPU/IO-bound operations
- `server.connect()` for proper async initialization
- Suspend functions throughout the call chain

### ❌ What Doesn't Work for Long Operations
- Direct suspend function calls in tool handlers (timeout)
- `runBlocking` (blocks server thread)
- `GlobalScope.launch` without proper job tracking (client still times out on next call)

### 🎯 Best Practice
**For operations > 5 seconds:**
- Use Async Job Pattern
- Return immediately with job ID
- Provide polling endpoint
- Store results in memory/persistent storage

**For operations < 5 seconds:**
- Direct suspend function calls work fine
- MCP SDK handles async properly

## Related Issues

### MCP Protocol Limitations
- No built-in support for long-running operations
- No streaming progress updates in tool calls
- No server-initiated notifications (only responses)

### Future Improvements
1. MCP Protocol could add:
   - Streaming tool results
   - Progress notifications
   - Server-sent events
2. Warp Client could:
   - Increase timeout for known long operations
   - Support progress indicators

## References
- MCP Kotlin SDK: https://modelcontextprotocol.github.io/kotlin-sdk/
- Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
- Issue Thread: [Internal investigation 2025-11-14]

## Status
**Current State**: Async Job Pattern implementation in progress  
**Next Steps**: Implement JobManager + background execution pattern  
**Target**: Sub-second response times for all MCP tool calls
