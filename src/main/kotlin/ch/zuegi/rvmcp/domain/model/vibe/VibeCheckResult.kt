package ch.zuegi.rvmcp.domain.model.vibe

import java.time.Instant

data class VibeCheckResult(
    val check: VibeCheck,
    val passed: Boolean,
    val findings: String,
    val timestamp: Instant = Instant.now()
)
