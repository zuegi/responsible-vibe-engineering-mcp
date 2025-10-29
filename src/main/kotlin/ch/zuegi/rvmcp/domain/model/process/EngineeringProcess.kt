package ch.zuegi.rvmcp.domain.model.process

import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase

data class EngineeringProcess(
    val id: ProcessId,
    val name: String,
    val description: String,
    val phases: List<ProcessPhase>
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(description.isNotBlank()) { "Description must not be blank" }
        require(phases.isNotEmpty()) { "Process must have at least one phase" }
        require(phases.mapIndexed { index, phase -> phase.order == index }.all { it }) {
            "Phase order must be sequential starting from 0"
        }
    }

    fun getPhase(index: Int): ProcessPhase? = phases.getOrNull(index)

    fun hasNextPhase(currentIndex: Int): Boolean = currentIndex < phases.size - 1

    fun totalPhases(): Int = phases.size
}
