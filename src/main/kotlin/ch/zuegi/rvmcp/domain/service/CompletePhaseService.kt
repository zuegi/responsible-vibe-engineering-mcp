package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort

/**
 * Domain service for completing a process phase.
 *
 * This service:
 * 1. Validates phase completion
 * 2. Updates execution context with phase result
 * 3. Transitions to next phase or completes execution
 * 4. Persists updated state
 */
class CompletePhaseService(
    private val memoryRepository: MemoryRepositoryPort,
) {
    /**
     * Completes the current phase and transitions to the next phase if available.
     *
     * @param execution The current process execution
     * @param context The updated execution context after phase completion
     * @param phaseResult The result of the completed phase
     * @return The updated process execution (either moved to next phase or completed)
     */
    fun execute(
        execution: ProcessExecution,
        context: ExecutionContext,
        phaseResult: PhaseResult,
    ): ProcessExecution {
        println("\n📋 Completing phase: ${phaseResult.phaseName}")

        // 1. Update context with phase result (if not already added)
        val updatedContext =
            if (!context.hasCompletedPhase(phaseResult.phaseName)) {
                context.addPhaseResult(phaseResult)
            } else {
                context
            }

        // 2. Persist context
        memoryRepository.save(updatedContext)

        // 3. Check if there are more phases
        return if (execution.process.hasNextPhase(execution.currentPhaseIndex)) {
            val nextExecution = execution.nextPhase()
            println("   ➡ Moving to next phase: ${nextExecution.currentPhase().name}")
            nextExecution
        } else {
            val completedExecution = execution.complete()
            println("   ✓ All phases completed!")
            println("   🎉 Process execution finished")
            completedExecution
        }
    }

    /**
     * Marks a process execution as failed.
     *
     * @param execution The current process execution
     * @param context The execution context
     * @return The failed process execution
     */
    fun fail(
        execution: ProcessExecution,
        context: ExecutionContext,
    ): ProcessExecution {
        println("\n❌ Process execution failed")

        val failedExecution = execution.fail()
        memoryRepository.save(context)

        return failedExecution
    }
}
