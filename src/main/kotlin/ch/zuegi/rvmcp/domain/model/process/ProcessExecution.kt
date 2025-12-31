package ch.zuegi.rvmcp.domain.model.process

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import ch.zuegi.rvmcp.domain.model.interaction.InteractionResponse
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.status.ExecutionState
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import java.time.Instant

data class ProcessExecution(
    val id: ExecutionId,
    val process: EngineeringProcess,
    val status: ExecutionStatus,
    val state: ExecutionState = ExecutionState.RUNNING,
    val currentPhaseIndex: Int = 0,
    val pendingInteraction: InteractionRequest? = null,
    val interactionHistory: List<InteractionResponse> = emptyList(),
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

    fun pauseForInteraction(interactionRequest: InteractionRequest): ProcessExecution =
        copy(
            state = ExecutionState.AWAITING_INPUT,
            pendingInteraction = interactionRequest,
        )

    fun resumeWithAnswer(answer: String): ProcessExecution {
        require(state == ExecutionState.AWAITING_INPUT) {
            "Can only resume execution when in AWAITING_INPUT state"
        }
        require(pendingInteraction != null) {
            "Cannot resume without pending interaction"
        }

        val response =
            InteractionResponse(
                request = pendingInteraction,
                answer = answer,
            )

        return copy(
            state = ExecutionState.RUNNING,
            pendingInteraction = null,
            interactionHistory = interactionHistory + response,
        )
    }

    fun isAwaitingInput(): Boolean = state == ExecutionState.AWAITING_INPUT
}
