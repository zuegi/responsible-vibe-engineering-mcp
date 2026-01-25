package ch.zuegi.rvmcp.application.usecase

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import java.time.Instant

/**
 * Application Layer implementation of CompletePhaseUseCase.
 *
 * Reconstructs ProcessExecution from ExecutionContext and delegates to domain service.
 */
class CompletePhaseUseCaseImpl(
    private val domainService: CompletePhaseService,
    private val memoryRepository: MemoryRepositoryPort,
    private val processRepository: ProcessRepositoryPort,
) : CompletePhaseUseCase {
    override suspend fun execute(
        executionId: ExecutionId,
        phaseResult: PhaseResult,
    ): ProcessExecution {
        // Find context by executionId
        val context =
            memoryRepository.findByExecutionId(executionId)
                ?: throw IllegalArgumentException("Execution context not found for execution ID: ${executionId.value}")

        val processId =
            context.engineeringProcessId
                ?: throw IllegalStateException("No process ID found in execution context")

        // Reconstruct ProcessExecution from context
        val process =
            processRepository.findById(processId)
                ?: throw IllegalArgumentException("Process not found: ${processId.value}")

        val processExecution =
            ProcessExecution(
                id = context.executionId,
                process = process,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = context.currentPhaseIndex,
                startedAt = Instant.now().minusSeconds(3600), // Approximation - real startedAt not stored
            )

        return domainService.execute(processExecution, context, phaseResult)
    }
}
