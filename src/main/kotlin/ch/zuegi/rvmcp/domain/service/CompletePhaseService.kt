package ch.zuegi.rvmcp.domain.service

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
     * Completes a phase and advances execution.
     */
    suspend fun execute(
        execution: ProcessExecution,
        context: ch.zuegi.rvmcp.domain.model.context.ExecutionContext,
        phaseResult: PhaseResult,
    ): ProcessExecution {
        println("\n📋 Completing phase: ${phaseResult.phaseName}")

        // 1. Update context with phase result (if not already added)
        val contextWithResult =
            if (!context.hasCompletedPhase(phaseResult.phaseName)) {
                context.addPhaseResult(phaseResult)
            } else {
                context
            }

        // 2. Check if there are more phases
        val updatedContext =
            if (execution.process.hasNextPhase(execution.currentPhaseIndex)) {
                println("   ➡ Moving to next phase")
                contextWithResult.advanceToNextPhase()
            } else {
                println("   ✓ All phases completed!")
                println("   🎉 Process execution finished")
                contextWithResult
            }

        // 3. Persist updated context
        memoryRepository.save(updatedContext)

        // 4. Return updated execution
        return if (execution.process.hasNextPhase(execution.currentPhaseIndex)) {
            val nextExecution = execution.nextPhase()
            println("   Next phase: ${nextExecution.currentPhase().name}")
            nextExecution
        } else {
            execution.complete()
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
        context: ch.zuegi.rvmcp.domain.model.context.ExecutionContext,
    ): ProcessExecution {
        println("\n❌ Process execution failed")

        val failedExecution = execution.fail()
        memoryRepository.save(context)

        return failedExecution
    }
}
