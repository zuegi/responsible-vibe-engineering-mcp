package ch.zuegi.rvmcp.infrastructure.config

import ch.zuegi.rvmcp.adapter.output.vibe.AutoPassVibeCheckEvaluator
import ch.zuegi.rvmcp.application.usecase.CompletePhaseUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.ExecuteProcessPhaseUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.StartProcessExecutionUseCaseImpl
import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.StartProcessExecutionUseCase
import ch.zuegi.rvmcp.domain.port.output.DocumentPersistencePort
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import ch.zuegi.rvmcp.domain.service.DocumentGenerationService
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
    // ========== Output Adapters ==========

    @Bean
    fun vibeCheckEvaluator(): VibeCheckEvaluatorPort {
        // Use AutoPassVibeCheckEvaluator for MCP Server mode (non-interactive)
        // stdin is not available when running as MCP Server
        return AutoPassVibeCheckEvaluator()
    }

    // ========== Domain Services (Pure Business Logic) ==========

    @Bean
    fun startProcessExecutionService(
        processRepository: ProcessRepositoryPort,
        memoryRepository: MemoryRepositoryPort,
    ): StartProcessExecutionService =
        StartProcessExecutionService(
            processRepository = processRepository,
            memoryRepository = memoryRepository,
        )

    @Bean
    fun documentGenerationService(documentPersistence: DocumentPersistencePort): DocumentGenerationService =
        DocumentGenerationService(
            documentPersistence = documentPersistence,
        )

    @Bean
    fun executeProcessPhaseService(
        workflowExecutionPort: WorkflowExecutionPort,
        vibeCheckEvaluator: VibeCheckEvaluatorPort,
        documentGenerationService: DocumentGenerationService,
    ): ExecuteProcessPhaseService =
        ExecuteProcessPhaseService(
            workflowExecutor = workflowExecutionPort,
            vibeCheckEvaluator = vibeCheckEvaluator,
            documentGenerationService = documentGenerationService,
        )

    @Bean
    fun completePhaseService(memoryRepository: MemoryRepositoryPort): CompletePhaseService =
        CompletePhaseService(
            memoryRepository = memoryRepository,
        )

    // ========== Application Services (Use Case Implementations) ==========

    @Bean
    fun startProcessExecutionUseCase(domainService: StartProcessExecutionService): StartProcessExecutionUseCase =
        StartProcessExecutionUseCaseImpl(domainService)

    @Bean
    fun executeProcessPhaseUseCase(
        domainService: ExecuteProcessPhaseService,
        memoryRepository: MemoryRepositoryPort,
    ): ExecuteProcessPhaseUseCase = ExecuteProcessPhaseUseCaseImpl(domainService)

    @Bean
    fun completePhaseUseCase(
        domainService: CompletePhaseService,
        memoryRepository: MemoryRepositoryPort,
        processRepository: ProcessRepositoryPort,
    ): CompletePhaseUseCase =
        CompletePhaseUseCaseImpl(
            domainService = domainService,
            memoryRepository = memoryRepository,
            processRepository = processRepository,
        )
}
