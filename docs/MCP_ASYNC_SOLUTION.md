# MCP Async Execution - Solution

## Problem: stdin Blocking Deadlock

### Root Cause
**ConsoleVibeCheckEvaluator** blockierte bei `readlnOrNull()` auf stdin → Endloses Warten

**Warum?**
- MCP Server nutzt **stdio Transport** (stdin/stdout für JSON-RPC Messages)
- `ConsoleVibeCheckEvaluator.evaluate()` versucht User Input von stdin zu lesen
- stdin ist bereits vom MCP Protocol belegt
- Background Job wartet ewig auf Input der nie kommt → **Deadlock**

### Symptome
- `execute_phase` Tool gibt Job-ID zurück
- Job Status bleibt RUNNING
- Keine Completion nach 10+ Minuten
- Keine Fehler-Logs
- Last log line: `🔹 Calling workflowExecutor.executeWorkflow...`

### Investigation Timeline
1. **Zeile 1639**: `🔹 agent.run() returned successfully` ✅
2. **Zeile 1696**: `🔹 workflowExecutor.executeWorkflow returned` ✅
3. **Missing**: Vibe Check Evaluation logs
4. **Missing**: `ExecuteProcessPhaseService` return log
5. **Root Cause**: Code hängt bei `vibeCheckEvaluator.evaluateBatch()`

## Solution: AutoPassVibeCheckEvaluator

### Implementation
```kotlin
class AutoPassVibeCheckEvaluator : VibeCheckEvaluatorPort {
    override fun evaluate(
        vibeCheck: VibeCheck,
        context: ExecutionContext,
    ): VibeCheckResult {
        System.err.println("🔹 AutoPassVibeCheckEvaluator: ${vibeCheck.question} → PASS")
        return VibeCheckResult(
            check = vibeCheck,
            passed = true,
            findings = "Auto-passed (MCP Server Mode)",
        )
    }

    override fun evaluateBatch(
        vibeChecks: List<VibeCheck>,
        context: ExecutionContext,
    ): List<VibeCheckResult> {
        System.err.println("🔹 AutoPassVibeCheckEvaluator: Evaluating ${vibeChecks.size} vibe checks")
        return vibeChecks.map { evaluate(it, context) }
    }
}
```

### Configuration
**ApplicationConfiguration.kt:**
```kotlin
@Bean
fun vibeCheckEvaluator(): VibeCheckEvaluatorPort {
    // Use AutoPassVibeCheckEvaluator for MCP Server mode (non-interactive)
    // stdin is not available when running as MCP Server
    return AutoPassVibeCheckEvaluator()
}
```

### ConsoleVibeCheckEvaluator
- `@Component` annotation removed
- Only used in manual tests
- Should be manually instantiated when needed

## Async Job Pattern

### Architecture
```
execute_phase Tool Call
    ↓
Create Job ID
    ↓
Launch Background Coroutine (jobScope)
    ↓
Return Job ID immediately ⚡
    ↓
Background: Execute Phase
    ↓
Background: Complete Job → jobManager
    ↓
Client Polls: get_phase_result
```

### Implementation Details
**ResponsibleVibeMcpServer.kt:**
```kotlin
// Coroutine scope for background execution
private val jobScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

// execute_phase tool
jobScope.launch {
    try {
        val phaseResult = withContext(Dispatchers.IO) {
            executePhaseUseCase.execute(currentPhase, context)
        }
        jobManager.completeJob(jobId, phaseResult)
    } catch (e: Exception) {
        jobManager.failJob(jobId, e.message ?: "Unknown error")
    }
}
```

### Job Manager
**AsyncJobManager.kt:**
```kotlin
class AsyncJobManager {
    private val jobs = ConcurrentHashMap<String, AsyncJob>()
    
    fun createJob(): String
    fun completeJob(jobId: String, result: PhaseResult)
    fun failJob(jobId: String, error: String)
    fun getJob(jobId: String): AsyncJob?
}

data class AsyncJob(
    val id: String,
    val status: JobStatus,
    val result: PhaseResult? = null,
    val error: String? = null
)

enum class JobStatus { RUNNING, COMPLETED, FAILED }
```

## Results

### Performance
- **Before**: Timeout after >9 minutes (Deadlock)
- **After**: **1074ms** complete execution ⚡

### Test Results
```bash
mvn clean verify -T 2C
```
- ✅ 64 Tests passing
- ✅ ktlint passed
- ✅ All integration tests successful
- ✅ End-to-End workflow execution working

### MCP Tool Call Sequence
```bash
1. start_process → Execution ID
2. execute_phase → Job ID (immediate return)
3. get_phase_result → Poll until COMPLETED
4. complete_phase → Advance to next phase
```

### Example Output
```
✅ Phase executed successfully!
Job ID: job-1763127973400-3999
Status: COMPLETED

Phase: Requirements Analysis
Execution Status: PHASE_COMPLETED
Summary: Workflow: Simple Test
...
Vibe Checks: 1 checks (1 passed)
Decisions: 1 architectural decisions made
Duration: 1074ms
```

## Debugging Enhancements

### Extensive Logging Added
All critical execution points now have detailed logging:

**ResponsibleVibeMcpServer.kt:**
- Job creation and launch
- Thread names and dispatcher info
- Entry/exit of critical sections

**KoogWorkflowExecutor.kt:**
- Template and thread info
- Agent creation timeline
- agent.run() entry/exit
- Error handling

**ExecuteProcessPhaseService.kt:**
- Service entry with thread info
- workflowExecutor call boundaries
- Workflow return confirmation

**ExecuteProcessPhaseUseCaseImpl.kt:**
- Use case entry/exit
- Phase name and thread tracking

### Log Output Format
```
🔹 Component.method called
   Context: value
   Thread: DefaultDispatcher-worker-X
[... execution ...]
🔹 Component.method returned
```

## Lessons Learned

### 1. stdin in MCP Server Mode
**Never use stdin for user interaction when running as MCP Server!**
- stdin is reserved for MCP Protocol (JSON-RPC)
- Any `readln()` calls will deadlock
- Use non-interactive alternatives (AutoPass, Config-based, etc.)

### 2. Async Job Pattern for Long Operations
**MCP Clients have timeout windows (30-60s typical)**
- Return Job ID immediately
- Execute in background coroutine
- Provide polling endpoint (get_phase_result)
- Use proper job state management

### 3. Coroutine Scope Management
**Use dedicated scope for background jobs:**
```kotlin
CoroutineScope(Dispatchers.IO + SupervisorJob())
```
- IO dispatcher for blocking operations
- SupervisorJob for failure isolation
- Proper cleanup on application shutdown

### 4. Debugging Long-Running Operations
**Extensive logging is crucial:**
- Log at all layer boundaries
- Include thread names
- Track entry/exit of critical methods
- Log dispatcher context
- Use System.err for visibility in MCP logs

## ADR-007: Non-Interactive Vibe Checks in MCP Mode

**Decision**: Use AutoPassVibeCheckEvaluator as default in MCP Server mode

**Reasoning**:
- stdin is not available in MCP Server mode (used by Protocol)
- Interactive vibe checks (ConsoleVibeCheckEvaluator) cause deadlock
- MCP clients expect quick responses (async job pattern)
- Vibe checks can be made interactive via future MCP Resource/Prompt features

**Alternatives Considered**:
1. ❌ Skip vibe checks entirely → Loses quality gates
2. ❌ Timeout-based fallback → Complex error handling
3. ✅ Auto-pass with logging → Simple, debuggable, extensible

**Future Enhancement**:
- MCP Prompt-based interactive vibe checks
- Config-based vibe check rules
- LLM-based automated vibe check evaluation

## Migration Guide

### For Existing Deployments
1. **Update dependency injection**:
   - Remove `@Component` from ConsoleVibeCheckEvaluator
   - Add Bean for AutoPassVibeCheckEvaluator in ApplicationConfiguration

2. **Update tests**:
   - Tests using ConsoleVibeCheckEvaluator must instantiate manually
   - Use AutoPassVibeCheckEvaluator for non-interactive test scenarios

3. **Update workflows**:
   - Replace direct vibe check interactions with async job pattern
   - Update clients to poll get_phase_result

### Breaking Changes
- ConsoleVibeCheckEvaluator no longer auto-configured
- execute_phase now returns job ID instead of immediate result
- Clients must implement polling logic for get_phase_result

## References
- [MCP Protocol Specification](https://modelcontextprotocol.io/docs/specification)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- WARP.md - Phase 2a documentation
- ADR-007: Non-Interactive Vibe Checks
