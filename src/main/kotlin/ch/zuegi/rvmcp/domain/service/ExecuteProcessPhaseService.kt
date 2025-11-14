package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import java.time.Instant

/**
 * Domain service for executing a process phase.
 *
 * This service orchestrates:
 * 1. Workflow execution (via WorkflowExecutionPort)
 * 2. Vibe check evaluation (quality gates)
 * 3. Human-in-the-loop if vibe checks fail
 * 4. Context updates and persistence
 */
class ExecuteProcessPhaseService(
    private val workflowExecutor: WorkflowExecutionPort,
    private val vibeCheckEvaluator: VibeCheckEvaluatorPort,
) {
    /**
     * Executes a single process phase with the given context.
     *
     * @param phase The process phase to execute
     * @param context The current execution context
     * @return PhaseResult
     */
    suspend fun execute(
        phase: ProcessPhase,
        context: ExecutionContext,
    ): PhaseResult {
        System.err.println("🔹 ExecuteProcessPhaseService.execute called")
        System.err.println("   Thread: ${Thread.currentThread().name}")
        val startTime = Instant.now()
        println("\n▶ Starting phase: ${phase.name}")
        println("  Description: ${phase.description}")

        // 1. Execute workflow
        System.err.println("🔹 Calling workflowExecutor.executeWorkflow...")
        val workflowResult =
            workflowExecutor.executeWorkflow(
                template = phase.koogWorkflowTemplate,
                context = context,
            )
        System.err.println("🔹 workflowExecutor.executeWorkflow returned")

        // 2. Evaluate vibe checks (with original context - decisions will be added later via PhaseResult)
        val vibeCheckResults =
            vibeCheckEvaluator.evaluateBatch(
                vibeChecks = phase.vibeChecks,
                context = context,
            )

        val allVibeChecksPassed = vibeCheckResults.all { it.passed }

        // 3. Human-in-the-loop if vibe checks failed
        if (!allVibeChecksPassed) {
            println("\n⚠ Einige Vibe Checks sind fehlgeschlagen:")
            vibeCheckResults.filter { !it.passed }.forEach { result ->
                println("  ✗ ${result.check.question}")
                println("    → ${result.findings}")
            }

            if (phase.vibeChecks.any { it.required }) {
                println("\n⛔ Obligatorische Vibe Checks nicht bestanden.")
                println("   Phase muss wiederholt werden.")

                return createFailedPhaseResult(
                    phase = phase,
                    vibeCheckResults = vibeCheckResults,
                    startTime = startTime,
                    workflowSummary = workflowResult.summary,
                    decisions = workflowResult.decisions,
                )
            }
        }

        // 4. Create phase result (including workflow decisions)
        val phaseResult =
            PhaseResult(
                phaseName = phase.name,
                status = if (allVibeChecksPassed) ExecutionStatus.PHASE_COMPLETED else ExecutionStatus.FAILED,
                summary = workflowResult.summary,
                vibeCheckResults = vibeCheckResults,
                decisions = workflowResult.decisions,
                startedAt = startTime,
                completedAt = Instant.now(),
            )

        // 5. Persistence handled externally

        println("\n✓ Phase completed: ${phase.name}")
        return phaseResult
    }

    private fun createFailedPhaseResult(
        phase: ProcessPhase,
        vibeCheckResults: List<ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult>,
        startTime: Instant,
        workflowSummary: String,
        decisions: List<ch.zuegi.rvmcp.domain.model.memory.Decision>,
    ): PhaseResult {
        val phaseResult =
            PhaseResult(
                phaseName = phase.name,
                status = ExecutionStatus.FAILED,
                summary = workflowSummary,
                vibeCheckResults = vibeCheckResults,
                decisions = decisions,
                startedAt = startTime,
                completedAt = Instant.now(),
            )

        return phaseResult
    }
}
