package ch.zuegi.rvmcp.domain.port.input

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase

/**
 * Use case for executing a single process phase.
 *
 * This is the main orchestration point for phase execution:
 * 1. Execute Koog workflow for the phase
 * 2. Perform vibe checks
 * 3. Handle human-in-the-loop interactions
 * 4. Generate phase result
 * 5. Update execution context
 */
interface ExecuteProcessPhaseUseCase {
    /**
     * Executes a process phase with the given execution context.
     *
     * @param phase The process phase to execute
     * @param context The current execution context
     * @return The phase result including vibe check results and summary
     */
    suspend fun execute(
        phase: ProcessPhase,
        context: ExecutionContext,
    ): PhaseResult
}
