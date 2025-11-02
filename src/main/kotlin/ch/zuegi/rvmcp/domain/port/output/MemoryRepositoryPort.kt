package ch.zuegi.rvmcp.domain.port.output

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.ExecutionId

/**
 * Port for persisting and retrieving execution contexts (Long-Term Memory).
 *
 * This port is responsible for storing project-level context across sessions,
 * including architectural decisions, phase results, and artifacts.
 * It supports branch-aware storage for parallel feature development.
 */
interface MemoryRepositoryPort {
    /**
     * Saves the execution context to persistent storage.
     *
     * @param context The execution context to persist
     */
    fun save(context: ExecutionContext)

    /**
     * Loads an execution context for a specific project and git branch.
     *
     * @param projectPath The absolute path to the project directory
     * @param gitBranch The git branch name
     * @return The execution context if found, null otherwise
     */
    fun load(
        projectPath: String,
        gitBranch: String,
    ): ExecutionContext?

    /**
     * Deletes an execution context by its ID.
     *
     * @param executionId The execution ID to delete
     */
    fun delete(executionId: ExecutionId)

    /**
     * Checks if an execution context exists for the given project and branch.
     *
     * @param projectPath The absolute path to the project directory
     * @param gitBranch The git branch name
     * @return true if a context exists, false otherwise
     */
    fun exists(
        projectPath: String,
        gitBranch: String,
    ): Boolean
}
