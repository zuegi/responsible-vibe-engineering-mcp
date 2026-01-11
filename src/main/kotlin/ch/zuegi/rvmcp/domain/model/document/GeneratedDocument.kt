package ch.zuegi.rvmcp.domain.model.document

import java.time.Instant

/**
 * Represents a generated engineering document.
 *
 * Documents are generated from phase results and persisted
 * via [DocumentPersistencePort] to various backends (Git, File, Confluence).
 */
data class GeneratedDocument(
    /**
     * Relative file path within project (e.g., "docs/requirements.md")
     */
    val filename: String,
    /**
     * Document content (typically Markdown)
     */
    val content: String,
    /**
     * Document type
     */
    val type: DocumentType,
    /**
     * Metadata about the document
     */
    val metadata: DocumentMetadata,
) {
    init {
        require(filename.isNotBlank()) { "Filename must not be blank" }
        require(content.isNotBlank()) { "Content must not be blank" }
    }
}

/**
 * Type of engineering document.
 */
enum class DocumentType {
    REQUIREMENTS,
    ARCHITECTURE,
    FEATURE_SPEC,
    USER_STORY,
    TEST_PLAN,
    DECISION_LOG,
}

/**
 * Metadata for a generated document.
 */
data class DocumentMetadata(
    /**
     * Timestamp when document was generated
     */
    val generatedAt: Instant,
    /**
     * Phase that generated this document
     */
    val phaseName: String,
    /**
     * Document version
     */
    val version: String = "1.0",
    /**
     * Optional tags for organization
     */
    val tags: Set<String> = emptySet(),
)
