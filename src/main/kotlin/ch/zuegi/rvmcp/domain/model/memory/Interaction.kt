package ch.zuegi.rvmcp.domain.model.memory

import ch.zuegi.rvmcp.domain.model.status.InteractionType
import java.time.Instant

data class Interaction(
    val timestamp: Instant,
    val type: InteractionType,
    val context: String,
    val userResponse: String? = null
) {
    init {
        require(context.isNotBlank()) { "Context must not be blank" }
    }
}
