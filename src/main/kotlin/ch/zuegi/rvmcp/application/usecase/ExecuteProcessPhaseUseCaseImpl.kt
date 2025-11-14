package ch.zuegi.rvmcp.application.usecase

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService

/**
 * Application Layer implementation of ExecuteProcessPhaseUseCase.
 *
 * Orchestrates phase execution through domain service.
 */
class ExecuteProcessPhaseUseCaseImpl(
    private val domainService: ExecuteProcessPhaseService,
) : ExecuteProcessPhaseUseCase {
    override suspend fun execute(
        phase: ProcessPhase,
        context: ExecutionContext,
    ): PhaseResult {
        System.err.println("\uD83D\uDD39 ExecuteProcessPhaseUseCaseImpl.execute called")
        System.err.println("   Phase: ${phase.name}")
        System.err.println("   Thread: ${Thread.currentThread().name}")
        val result = domainService.execute(phase, context)
        System.err.println("\uD83D\uDD39 ExecuteProcessPhaseUseCaseImpl.execute returned")
        return result
    }
}
