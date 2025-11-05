package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
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
    /**
     * Starts a new process execution.
     *
     * @param processId The ID of the engineering process to execute
     * @param projectPath The absolute path to the project directory
     * @param gitBranch The git branch name for branch-aware context
     * @return The initialized process execution
     * @throws IllegalArgumentException if the process is not found
     */
    fun execute(
        processId: ProcessId,
        projectPath: String,
        gitBranch: String,
    ): ProcessExecution {
        println("\n🚀 Starting process execution")
        println("   Process ID: ${processId.value}")
        println("   Project: $projectPath")
        println("   Branch: $gitBranch")

        // 1. Load process definition
        val process =
            processRepository.findById(processId)
                ?: throw IllegalArgumentException("Process not found: ${processId.value}")

        println("   Process: ${process.name}")
        println("   Phases: ${process.totalPhases()}")

        // 2. Create or load execution context
        val executionContext =
            memoryRepository.load(projectPath, gitBranch)
                ?: createNewExecutionContext(projectPath, gitBranch)

        // 3. Create process execution
        val processExecution =
            ProcessExecution(
                id = ExecutionId(UUID.randomUUID().toString()),
                process = process,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 0,
                startedAt = Instant.now(),
            )

        // 4. Persist initial state
        memoryRepository.save(executionContext)

        println("\n✓ Process execution initialized")
        println("   Execution ID: ${processExecution.id.value}")
        println("   Starting with phase: ${processExecution.currentPhase().name}")

        return processExecution
    }

    private fun createNewExecutionContext(
        projectPath: String,
        gitBranch: String,
    ): ExecutionContext {
        return ExecutionContext(
            executionId = ExecutionId(UUID.randomUUID().toString()),
            projectPath = projectPath,
            gitBranch = gitBranch,
        )
    }
}
