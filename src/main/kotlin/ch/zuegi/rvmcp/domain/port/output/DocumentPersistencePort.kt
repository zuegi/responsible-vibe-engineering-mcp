package ch.zuegi.rvmcp.domain.port.output

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.document.DocumentMetadata
import ch.zuegi.rvmcp.domain.model.document.GeneratedDocument

interface DocumentPersistencePort {
    suspend fun saveDocument(
        doc: GeneratedDocument,
        context: ExecutionContext,
    ): Result<Unit>

    suspend fun getDocument(
        filename: String,
        context: ExecutionContext,
    ): GeneratedDocument?

    suspend fun listDocuments(context: ExecutionContext): List<DocumentMetadata>
}
