package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
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
    private val memoryRepository: MemoryRepositoryPort,
) {

    /**
     * Executes a single process phase with the given context.
     *
     * @param phase The process phase to execute
     * @param context The current execution context
     * @return The updated execution context after phase completion
     */
    fun execute(
        phase: ProcessPhase,
        context: ExecutionContext,
    ): ExecutionContext {
        val startTime = Instant.now()
        println("\n▶ Starting phase: ${phase.name}")
        println("  Description: ${phase.description}")

        // 1. Execute workflow
        val workflowResult = workflowExecutor.executeWorkflow(
            template = phase.koogWorkflowTemplate,
            context = context,
        )

        // 2. Add workflow decisions to context
        var updatedContext = workflowResult.decisions.fold(context) { ctx, decision ->
            ctx.addDecision(decision)
        }

        // 4. Evaluate vibe checks
        val vibeCheckResults = vibeCheckEvaluator.evaluateBatch(
            vibeChecks = phase.vibeChecks,
            context = updatedContext,
        )

        val allVibeChecksPassed = vibeCheckResults.all { it.passed }

        // 5. Human-in-the-loop if vibe checks failed
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
                    context = updatedContext,
                    vibeCheckResults = vibeCheckResults,
                    startTime = startTime,
                    workflowSummary = workflowResult.summary,
                )
            }
        }

        // 6. Create phase result
        val phaseResult = PhaseResult(
            phaseName = phase.name,
            status = if (allVibeChecksPassed) ExecutionStatus.PHASE_COMPLETED else ExecutionStatus.FAILED,
            summary = workflowResult.summary,
            vibeCheckResults = vibeCheckResults,
            startedAt = startTime,
            completedAt = Instant.now(),
        )

        // 7. Update context with phase result
        updatedContext = updatedContext.addPhaseResult(phaseResult)

        // 8. Persist context
        memoryRepository.save(updatedContext)

        println("\n✓ Phase completed: ${phase.name}")
        return updatedContext
    }

    private fun createFailedPhaseResult(
        phase: ProcessPhase,
        context: ExecutionContext,
        vibeCheckResults: List<ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult>,
        startTime: Instant,
        workflowSummary: String,
    ): ExecutionContext {
        val phaseResult = PhaseResult(
            phaseName = phase.name,
            status = ExecutionStatus.FAILED,
            summary = workflowSummary,
            vibeCheckResults = vibeCheckResults,
            startedAt = startTime,
            completedAt = Instant.now(),
        )

        val updatedContext = context.addPhaseResult(phaseResult)
        memoryRepository.save(updatedContext)

        return updatedContext
    }
}
