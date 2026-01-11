package ch.zuegi.rvmcp.domain.model.requirement

/**
 * Represents a functional or non-functional requirement.
 */
data class Requirement(
    val id: String,
    val title: String,
    val description: String,
    val type: RequirementType,
    val priority: RequirementPriority,
    val acceptanceCriteria: List<String> = emptyList(),
    val relatedStakeholders: List<String> = emptyList(),
) {
    init {
        require(id.isNotBlank()) { "Id must not be blank" }
        require(title.isNotBlank()) { "Title must not be blank" }
        require(description.isNotBlank()) { "Description must not be blank" }
    }
}

enum class RequirementType {
    FUNCTIONAL,
    NON_FUNCTIONAL,
    TECHNICAL,
    BUSINESS,
}

enum class RequirementPriority {
    MUST_HAVE,
    SHOULD_HAVE,
    COULD_HAVE,
    WONT_HAVE,
}
