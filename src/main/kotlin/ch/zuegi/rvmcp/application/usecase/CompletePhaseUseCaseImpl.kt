package ch.zuegi.rvmcp.application.usecase

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution
import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.service.CompletePhaseService

/**
 * Application Layer implementation of CompletePhaseUseCase.
 *
 * NOTE: Current implementation needs execution loading logic.
 * For now, the domain service uses a different signature (execution, context, phaseResult).
 *
 * TODO: Add ExecutionRepository to load execution by ID.
 */
class CompletePhaseUseCaseImpl(
    private val domainService: CompletePhaseService,
) : CompletePhaseUseCase {
    override fun execute(
        executionId: ExecutionId,
        phaseResult: PhaseResult,
    ): ProcessExecution {
        // TODO: Load execution and context by executionId
        throw UnsupportedOperationException(
            "CompletePhaseUseCase requires execution loading - not yet implemented. " +
                "Use domain service directly with execute(execution, context, phaseResult)",
        )
    }
}
