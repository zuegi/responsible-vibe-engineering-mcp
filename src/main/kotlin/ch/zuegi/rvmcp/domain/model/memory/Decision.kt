package ch.zuegi.rvmcp.domain.model.memory

import java.time.LocalDate

data class Decision(
    val phase: String,
    val decision: String,
    val reasoning: String,
    val date: LocalDate = LocalDate.now()
) {
    init {
        require(phase.isNotBlank()) { "Phase must not be blank" }
        require(decision.isNotBlank()) { "Decision must not be blank" }
        require(reasoning.isNotBlank()) { "Reasoning must not be blank" }
    }
}
