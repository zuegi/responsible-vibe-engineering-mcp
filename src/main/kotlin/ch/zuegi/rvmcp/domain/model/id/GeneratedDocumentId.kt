package ch.zuegi.rvmcp.domain.model.id

import java.util.UUID

class GeneratedDocumentId(
    val id: UUID,
) {
    companion object {
        fun generate(): GeneratedDocumentId = GeneratedDocumentId(UUID.randomUUID())

        fun of(value: String): GeneratedDocumentId = GeneratedDocumentId(UUID.fromString(value))
    }
}
