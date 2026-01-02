package ch.zuegi.rvmcp.domain.model.interaction

/**
 * Type of user interaction required by the workflow.
 *
 * Determines how the interaction should be handled and what validation rules apply.
 */
enum class InteractionType {
    /**
     * LLM-driven question - flexible, open-ended.
     * Used in requirements analysis and exploratory workflows.
     */
    ASK_USER,

    /**
     * Catalog-based question - structured, with validation rules.
     * Used for compliance-driven data collection (e.g., instrument data).
     */
    ASK_CATALOG_QUESTION,

    /**
     * Approval request - requires explicit confirmation from user.
     * Used for breaking changes, architectural decisions, etc.
     */
    APPROVAL,
}
