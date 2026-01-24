package ch.zuegi.rvmcp.application.usecase

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService
import ch.zuegi.rvmcp.shared.rvmcpLogger

/**
 * Application Layer implementation of ExecuteProcessPhaseUseCase.
 *
 * Orchestrates phase execution through domain service and handles
 * persistence of paused execution state.
 */
class ExecuteProcessPhaseUseCaseImpl(
    private val domainService: ExecuteProcessPhaseService,
) : ExecuteProcessPhaseUseCase {
    private val logger by rvmcpLogger()

    override suspend fun execute(
        phase: ProcessPhase,
        context: ExecutionContext,
    ): PhaseResult {
        val phaseResult = domainService.execute(phase, context)

        // If workflow paused for user input, update and persist execution state
        if (phaseResult.awaitingInput && phaseResult.interactionRequest != null) {
            logger.info("Phase paused for user interaction, persisting state")
            val execution =
                context.currentExecution
                    ?: throw IllegalStateException("No active execution in context")

            val pausedExecution = execution.pauseForInteraction(phaseResult.interactionRequest!!)
            val updatedContext = context.copy(currentExecution = pausedExecution)

            logger.info("Paused execution state saved (executionId=${execution.id.value})")
        }

        return phaseResult
    }
}
