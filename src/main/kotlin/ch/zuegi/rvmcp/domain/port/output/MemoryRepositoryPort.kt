package ch.zuegi.rvmcp.domain.port.output

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.ExecutionId

interface MemoryRepositoryPort {
    suspend fun save(context: ExecutionContext)

    suspend fun load(
        projectPath: String,
        branch: String,
    ): ExecutionContext?

    /**
     * Finds an execution context by its execution ID.
     *
     * @param executionId The execution ID to find
     * @return The execution context if found, null otherwise
     */
    suspend fun findByExecutionId(executionId: ExecutionId): ExecutionContext?

    /**
     * Deletes an execution context by its ID.
     *
     * @param executionId The execution ID to delete
     */
    suspend fun delete(executionId: ExecutionId)

    /**
     * Checks if an execution context exists for the given project and branch.
     *
     * @param projectPath The absolute path to the project directory
     * @param gitBranch The git branch name
     * @return true if a context exists, false otherwise
     */
    suspend fun exists(
        projectPath: String,
        gitBranch: String,
    ): Boolean
}
