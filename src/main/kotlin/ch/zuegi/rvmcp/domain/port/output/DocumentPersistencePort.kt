package ch.zuegi.rvmcp.domain.port.output

import ch.zuegi.rvmcp.domain.model.document.GeneratedDocument
import ch.zuegi.rvmcp.domain.model.id.GeneratedDocumentId

interface DocumentPersistencePort {
    suspend fun saveDocument(doc: GeneratedDocument): Result<GeneratedDocument?>

    suspend fun findById(generatedDocumentId: GeneratedDocumentId): GeneratedDocument?
}
