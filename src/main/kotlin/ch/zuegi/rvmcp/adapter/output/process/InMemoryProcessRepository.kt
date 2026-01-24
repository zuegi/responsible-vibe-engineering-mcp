package ch.zuegi.rvmcp.adapter.output.process

import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of ProcessRepositoryPort.
 * Stores process definitions in memory (lost on restart).
 */
@Component
class InMemoryProcessRepository : ProcessRepositoryPort {
    private val log by rvmcpLogger()

    private val storage = ConcurrentHashMap<EngineeringProcessId, EngineeringProcess>()

    override fun findById(engineeringProcessId: EngineeringProcessId): EngineeringProcess? = storage[engineeringProcessId]

    override fun findAll(): List<EngineeringProcess> = storage.values.toList()

    override fun save(process: EngineeringProcess) {
        storage[process.id] = process
        log.trace("Process gespeichert: ${process.name}")
    }

    override fun delete(engineeringProcessId: EngineeringProcessId) {
        storage.remove(engineeringProcessId)
    }
}
