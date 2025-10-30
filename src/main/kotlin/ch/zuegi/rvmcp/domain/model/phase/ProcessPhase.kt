package ch.zuegi.rvmcp.domain.model.phase

import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck

data class ProcessPhase(
    val name: String,
    val description: String,
    val vibeChecks: List<VibeCheck>,
    val koogWorkflowTemplate: String,
    val order: Int,
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(description.isNotBlank()) { "Description must not be blank" }
        require(koogWorkflowTemplate.isNotBlank()) { "Koog workflow template must not be blank" }
        require(order >= 0) { "Order must be non-negative" }
    }
}
