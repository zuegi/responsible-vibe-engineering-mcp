package ch.zuegi.rvmcp.domain.port.output.model

import ch.zuegi.rvmcp.domain.model.memory.Decision

data class WorkflowSummary(
    val compressed: String,
    val decisions: List<Decision>,
    val keyInsights: List<String>,
) {
    init {
        require(compressed.isNotBlank()) { "Compressed summary must not be blank" }
    }
}
