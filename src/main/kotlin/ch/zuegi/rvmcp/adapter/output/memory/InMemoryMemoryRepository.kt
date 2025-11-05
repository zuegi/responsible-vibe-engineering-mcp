package ch.zuegi.rvmcp.adapter.output.memory

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory storage for execution contexts.
 * Data is lost on application restart.
 */
class InMemoryMemoryRepository : MemoryRepositoryPort {
    private val storage = ConcurrentHashMap<String, ExecutionContext>()

    override fun save(context: ExecutionContext) {
        val key = buildKey(context.projectPath, context.gitBranch)
        storage[key] = context
        println("✓ Context gespeichert: ${context.projectPath} (${context.gitBranch})")
    }

    override fun load(
        projectPath: String,
        gitBranch: String,
    ): ExecutionContext? {
        val key = buildKey(projectPath, gitBranch)
        return storage[key]
    }

    override fun delete(executionId: ExecutionId) {
        storage.values.removeIf { it.executionId == executionId }
    }

    override fun exists(
        projectPath: String,
        gitBranch: String,
    ): Boolean {
        val key = buildKey(projectPath, gitBranch)
        return storage.containsKey(key)
    }

    private fun buildKey(
        projectPath: String,
        gitBranch: String,
    ): String {
        return "$projectPath::$gitBranch"
    }
}
