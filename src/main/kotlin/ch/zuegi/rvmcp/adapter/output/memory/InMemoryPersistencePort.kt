package ch.zuegi.rvmcp.adapter.output.memory

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.document.DocumentMetadata
import ch.zuegi.rvmcp.domain.model.document.GeneratedDocument
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.port.output.DocumentPersistencePort
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@ConditionalOnProperty("persistence.backend", havingValue = "inmemory", matchIfMissing = true)
class InMemoryPersistencePort :
    MemoryRepositoryPort,
    DocumentPersistencePort {
    private val logger by rvmcpLogger()

    private val contexts: MutableMap<String, ExecutionContext> = ConcurrentHashMap()
    private val documents: MutableMap<String, GeneratedDocument> = ConcurrentHashMap()

    companion object {
        private const val MAX_CONTEXTS = 100
        private const val MAX_DOCUMENTS = 500
    }

    override suspend fun save(context: ExecutionContext) {
        val key = "${context.projectPath}#${context.gitBranch}"

        // Simple eviction (LRU would be better)
        if (contexts.size >= MAX_CONTEXTS) {
            val oldest = contexts.keys.first()
            contexts.remove(oldest)
            logger.warn("Context evicted (limit: $MAX_CONTEXTS): $oldest")
        }

        contexts[key] = context
        logger.info("Context saved: $key")
    }

    override suspend fun load(
        projectPath: String,
        branch: String,
    ): ExecutionContext? {
        val key = "$projectPath#$branch"
        return contexts[key].also {
            if (it != null) {
                logger.info("Context loaded: $key")
            } else {
                logger.info("Context not found: $key")
            }
        }
    }

    override suspend fun findByExecutionId(executionId: ExecutionId): ExecutionContext? =
        contexts.values.firstOrNull { it.executionId == executionId }

    override suspend fun delete(executionId: ExecutionId) {
        TODO("Not yet implemented")
    }

    override suspend fun exists(
        projectPath: String,
        gitBranch: String,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun saveDocument(
        doc: GeneratedDocument,
        context: ExecutionContext,
    ): Result<Unit> {
        val key = makeKey(doc.filename, context)
        documents[key] = doc
        logger.info("Document saved: ${doc.filename}")
        logger.info("   Type: ${doc.type}, Phase: ${doc.metadata.phaseName}")
        logger.info("   In-Memory Storage Active (ephemeral)")
        logger.info("      To enable persistence, set application.yml:")
        logger.info("        persistence.backend: git|file|confluence")
        logger.warn("    Data will be lost on server restart!")
        return Result.success(Unit)
    }

    override suspend fun getDocument(
        filename: String,
        context: ExecutionContext,
    ): GeneratedDocument? {
        TODO("Not yet implemented")
    }

    override suspend fun listDocuments(context: ExecutionContext): List<DocumentMetadata> {
        TODO("Not yet implemented")
    }

    private fun makeKey(
        filename: String,
        context: ExecutionContext,
    ): String = "${context.projectPath}#${context.gitBranch}#$filename"
}
