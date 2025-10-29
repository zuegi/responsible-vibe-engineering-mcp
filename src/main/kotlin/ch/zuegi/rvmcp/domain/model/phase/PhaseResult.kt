package ch.zuegi.rvmcp.domain.model.phase

import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult
import java.time.Instant

data class PhaseResult(
    val phaseName: String,
    val status: ExecutionStatus,
    val summary: String,
    val vibeCheckResults: List<VibeCheckResult>,
    val startedAt: Instant,
    val completedAt: Instant? = null
) {
    init {
        require(phaseName.isNotBlank()) { "Phase name must not be blank" }
    }
}
