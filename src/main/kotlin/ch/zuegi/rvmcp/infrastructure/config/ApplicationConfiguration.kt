package ch.zuegi.rvmcp.infrastructure.config

import ch.zuegi.rvmcp.application.usecase.CompletePhaseUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.ExecuteProcessPhaseUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.StartProcessExecutionUseCaseImpl
import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.StartProcessExecutionUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService
import ch.zuegi.rvmcp.domain.service.StartProcessExecutionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring Configuration for Application Layer.
 *
 * Hexagonal Architecture - Clean Separation:
 * 1. Output Adapters (Repositories, Executors) are @Component annotated
 * 2. Domain Services contain pure business logic (created here)
 * 3. Application Services (Use Cases) orchestrate domain services (created here)
 *
 * This follows Option B: Application Layer pattern for maximum maintainability.
 */
@Configuration
class ApplicationConfiguration {
    // ========== Domain Services (Pure Business Logic) ==========

    @Bean
    fun startProcessExecutionService(
        processRepository: ProcessRepositoryPort,
        memoryRepository: MemoryRepositoryPort,
    ): StartProcessExecutionService {
        return StartProcessExecutionService(
            processRepository = processRepository,
            memoryRepository = memoryRepository,
        )
    }

    @Bean
    fun executeProcessPhaseService(
        workflowExecutionPort: WorkflowExecutionPort,
        vibeCheckEvaluator: VibeCheckEvaluatorPort,
    ): ExecuteProcessPhaseService {
        return ExecuteProcessPhaseService(
            workflowExecutor = workflowExecutionPort,
            vibeCheckEvaluator = vibeCheckEvaluator,
        )
    }

    @Bean
    fun completePhaseService(memoryRepository: MemoryRepositoryPort): CompletePhaseService {
        return CompletePhaseService(
            memoryRepository = memoryRepository,
        )
    }

    // ========== Application Services (Use Case Implementations) ==========

    @Bean
    fun startProcessExecutionUseCase(domainService: StartProcessExecutionService): StartProcessExecutionUseCase {
        return StartProcessExecutionUseCaseImpl(domainService)
    }

    @Bean
    fun executeProcessPhaseUseCase(domainService: ExecuteProcessPhaseService): ExecuteProcessPhaseUseCase {
        return ExecuteProcessPhaseUseCaseImpl(domainService)
    }

    @Bean
    fun completePhaseUseCase(domainService: CompletePhaseService): CompletePhaseUseCase {
        return CompletePhaseUseCaseImpl(domainService)
    }
}
