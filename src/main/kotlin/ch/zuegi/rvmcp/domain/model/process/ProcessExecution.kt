package ch.zuegi.rvmcp.domain.model.process

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import java.time.Instant

data class ProcessExecution(
    val id: ExecutionId,
    val process: EngineeringProcess,
    val status: ExecutionStatus,
    val currentPhaseIndex: Int = 0,
    val startedAt: Instant,
    val completedAt: Instant? = null,
) {
    init {
        require(currentPhaseIndex >= 0) { "Current phase index must be non-negative" }
        require(currentPhaseIndex < process.totalPhases()) {
            "Current phase index must be within process phases"
        }
        if (status == ExecutionStatus.COMPLETED) {
            require(completedAt != null) { "Completed execution must have completion timestamp" }
        }
    }

    fun currentPhase(): ProcessPhase =
        process.getPhase(currentPhaseIndex)
            ?: throw IllegalStateException("Current phase not found")

    fun nextPhase(): ProcessExecution {
        require(process.hasNextPhase(currentPhaseIndex)) { "No next phase available" }
        return copy(
            currentPhaseIndex = currentPhaseIndex + 1,
            status = ExecutionStatus.IN_PROGRESS,
        )
    }

    fun complete(): ProcessExecution =
        copy(
            status = ExecutionStatus.COMPLETED,
            completedAt = Instant.now(),
        )

    fun fail(): ProcessExecution =
        copy(
            status = ExecutionStatus.FAILED,
            completedAt = Instant.now(),
        )

    fun completePhase(): ProcessExecution =
        copy(
            status = ExecutionStatus.PHASE_COMPLETED,
        )
}
