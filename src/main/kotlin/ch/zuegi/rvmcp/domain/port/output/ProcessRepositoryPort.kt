package ch.zuegi.rvmcp.domain.port.output

import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess

/**
 * Port for managing engineering process definitions.
 *
 * This port provides access to predefined engineering workflows
 * (e.g., Feature Development, Bug Fix, Refactoring) that guide
 * the AI through structured development phases.
 */
interface ProcessRepositoryPort {
    /**
     * Finds an engineering process by its ID.
     *
     * @param processId The process identifier
     * @return The engineering process if found, null otherwise
     */
    fun findById(processId: ProcessId): EngineeringProcess?

    /**
     * Retrieves all available engineering processes.
     *
     * @return List of all engineering processes
     */
    fun findAll(): List<EngineeringProcess>

    /**
     * Saves or updates an engineering process.
     *
     * @param process The engineering process to save
     */
    fun save(process: EngineeringProcess)

    /**
     * Deletes an engineering process by its ID.
     *
     * @param processId The process identifier to delete
     */
    fun delete(processId: ProcessId)
}
