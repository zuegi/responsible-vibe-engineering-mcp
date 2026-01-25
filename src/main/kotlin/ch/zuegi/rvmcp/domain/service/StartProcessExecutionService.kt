package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import java.time.Instant
import java.util.UUID

/**
 * Domain service for starting a new process execution.
 *
 * This service:
 * 1. Loads the engineering process definition
 * 2. Creates or loads execution context for the project/branch
 * 3. Initializes process execution
 * 4. Persists initial state
 */
class StartProcessExecutionService(
    private val processRepository: ProcessRepositoryPort,
    private val memoryRepository: MemoryRepositoryPort,
) {
    private val logger by rvmcpLogger()

    /**
     * Starts a new process execution.
     *
     * @param engineeringProcessId The ID of the engineering process to execute
     * @param projectPath The absolute path to the project directory
     * @param gitBranch The git branch name for branch-aware context
     * @return The initialized process execution
     * @throws IllegalArgumentException if the process is not found
     */
    suspend fun execute(
        engineeringProcessId: EngineeringProcessId,
        projectPath: String,
        gitBranch: String,
    ): ProcessExecution {
        logger.info(
            "Starting process execution - Process ID: {}, Project: {}, Branch: {}",
            engineeringProcessId.value,
            projectPath,
            gitBranch,
        )

        // 1. Load process definition
        val process =
            processRepository.findById(engineeringProcessId)
                ?: throw IllegalArgumentException("Process not found: ${engineeringProcessId.value}")

        logger.info("Process: {}, Phases: {}", process.name, process.totalPhases())

        // 2. Create or load execution context
        val executionContext =
            memoryRepository.load(projectPath, gitBranch)
                ?: createNewExecutionContext(projectPath, gitBranch)

        // 3. Create process execution
        val processExecution =
            ProcessExecution(
                id = executionContext.executionId,
                process = process,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 0,
                startedAt = Instant.now(),
            )

        // 4. Update context with process info, execution, and persist
        val updatedContext =
            executionContext.copy(
                engineeringProcessId = engineeringProcessId,
                currentPhaseIndex = 0,
                currentExecution = processExecution,
            )
        memoryRepository.save(updatedContext)

        logger.info(
            "Process execution initialized - Execution ID: {}, Starting phase: {}",
            processExecution.id.value,
            processExecution.currentPhase().name,
        )

        return processExecution
    }

    private fun createNewExecutionContext(
        projectPath: String,
        gitBranch: String,
    ): ExecutionContext =
        ExecutionContext(
            executionId = ExecutionId(UUID.randomUUID().toString()),
            projectPath = projectPath,
            gitBranch = gitBranch,
        )
}
