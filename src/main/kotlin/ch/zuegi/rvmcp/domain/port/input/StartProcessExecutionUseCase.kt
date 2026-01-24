package ch.zuegi.rvmcp.domain.port.input

import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution

/**
 * Use case for starting a new process execution.
 *
 * This use case:
 * 1. Loads the engineering process definition
 * 2. Creates or loads execution context for the project/branch
 * 3. Initializes process execution
 * 4. Persists initial state
 */
interface StartProcessExecutionUseCase {
    /**
     * Starts a new process execution.
     *
     * @param engineeringProcessId The ID of the engineering process to execute
     * @param projectPath The absolute path to the project directory
     * @param gitBranch The git branch name for branch-aware context
     * @return The initialized process execution
     */
    suspend fun execute(
        engineeringProcessId: EngineeringProcessId,
        projectPath: String,
        gitBranch: String,
    ): ProcessExecution
}
