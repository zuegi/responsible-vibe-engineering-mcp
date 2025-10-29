package ch.zuegi.rvmcp.domain.model.vibe

import ch.zuegi.rvmcp.domain.model.status.VibeCheckType

data class VibeCheck(
    val question: String,
    val type: VibeCheckType,
    val required: Boolean = true,
    val validationCriteria: List<String> = emptyList()
) {
    init {
        require(question.isNotBlank()) { "Question must not be blank" }
    }
}
