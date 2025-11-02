package ch.zuegi.rvmcp.adapter.output.process

import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory storage for engineering process definitions.
 */
class InMemoryProcessRepository : ProcessRepositoryPort {

    private val storage = ConcurrentHashMap<ProcessId, EngineeringProcess>()

    override fun findById(processId: ProcessId): EngineeringProcess? {
        return storage[processId]
    }

    override fun findAll(): List<EngineeringProcess> {
        return storage.values.toList()
    }

    override fun save(process: EngineeringProcess) {
        storage[process.id] = process
        println("✓ Process gespeichert: ${process.name}")
    }

    override fun delete(processId: ProcessId) {
        storage.remove(processId)
    }
}
