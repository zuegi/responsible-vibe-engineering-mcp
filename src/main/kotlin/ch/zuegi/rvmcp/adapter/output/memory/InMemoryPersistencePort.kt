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

    private fun makeContextKey(
        projectPath: String,
        gitBranch: String,
    ) = "$projectPath#$gitBranch"

    override suspend fun save(context: ExecutionContext) {
        val key = makeContextKey(context.projectPath, context.gitBranch)

        // Check limit only for new contexts (not updates)
        if (!contexts.containsKey(key) && contexts.size >= MAX_CONTEXTS) {
            logger.error("In-Memory storage limit reached: $MAX_CONTEXTS contexts")
            logger.error("   To store more contexts, configure a persistent backend:")
            logger.error("     persistence.backend: git|file|confluence")
            throw IllegalStateException(
                "In-Memory storage limit of $MAX_CONTEXTS contexts exceeded. " +
                    "Configure a persistent backend (git/file/confluence) in application.yml",
            )
        }

        contexts[key] = context
        logger.info("Context saved: $key (${contexts.size}/$MAX_CONTEXTS)")
    }

    override suspend fun load(
        projectPath: String,
        branch: String,
    ): ExecutionContext? {
        val key = makeContextKey(projectPath, branch)
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
        contexts.values
            .firstOrNull { it.executionId == executionId }
            ?.let { contexts.remove(makeContextKey(it.projectPath, it.gitBranch)) }
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
