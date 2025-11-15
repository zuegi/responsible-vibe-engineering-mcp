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
        return domainService.execute(phase, context)
    }
}
