package ch.zuegi.rvmcp.application.usecase

import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution
import ch.zuegi.rvmcp.domain.port.input.StartProcessExecutionUseCase
import ch.zuegi.rvmcp.domain.service.StartProcessExecutionService

/**
 * Application Layer implementation of StartProcessExecutionUseCase.
 *
 * Delegates to Domain Service for business logic execution.
 * This layer is responsible for orchestrating domain services and handling cross-cutting concerns.
 */
class StartProcessExecutionUseCaseImpl(
    private val domainService: StartProcessExecutionService,
) : StartProcessExecutionUseCase {
    override suspend fun execute(
        engineeringProcessId: EngineeringProcessId,
        projectPath: String,
        gitBranch: String,
    ): ProcessExecution = domainService.execute(engineeringProcessId, projectPath, gitBranch)
}
