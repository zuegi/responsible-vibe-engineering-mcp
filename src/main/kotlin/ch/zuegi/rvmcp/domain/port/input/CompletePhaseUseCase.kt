package ch.zuegi.rvmcp.domain.port.input

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution

/**
 * Use case for completing a process phase.
 *
 * This use case:
 * 1. Validates phase result
 * 2. Updates execution context with phase result
 * 3. Persists context to long-term memory
 * 4. Advances process execution to next phase
 * 5. Returns updated process execution
 */
interface CompletePhaseUseCase {
    /**
     * Completes a phase and updates the process execution.
     *
     * @param executionId The execution ID
     * @param phaseResult The result of the completed phase
     * @return The updated process execution, advanced to next phase if available
     */
    suspend fun execute(
        executionId: ExecutionId,
        phaseResult: PhaseResult,
    ): ProcessExecution
}
