package ch.zuegi.rvmcp.domain.port.output.model

import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult
import java.time.Instant

data class WorkflowExecutionResult(
    val success: Boolean,
    val summary: String,
    val decisions: List<Decision>,
    val vibeCheckResults: List<VibeCheckResult>,
    val startedAt: Instant,
    val completedAt: Instant,
) {
    init {
        require(summary.isNotBlank()) { "Summary must not be blank" }
        require(!completedAt.isBefore(startedAt)) { "Completed at must be after or equal to started at" }
    }
}
