package ch.zuegi.rvmcp.domain.model.context

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.memory.Artifact
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.model.memory.Interaction
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution

data class ExecutionContext(
    val executionId: ExecutionId,
    val projectPath: String,
    val gitBranch: String,
    val processId: ProcessId? = null,
    val currentPhaseIndex: Int = 0,
    val currentExecution: ProcessExecution? = null,
    val phaseResults: Map<String, PhaseResult> = emptyMap(),
    val architecturalDecisions: List<Decision> = emptyList(),
    val interactions: List<Interaction> = emptyList(),
    val artifacts: List<Artifact> = emptyList(),
) {
    val phaseHistory: List<PhaseResult>
        get() = phaseResults.values.toList()

    init {
        require(projectPath.isNotBlank()) { "Project path must not be blank" }
        require(gitBranch.isNotBlank()) { "Git branch must not be blank" }
    }

    fun addPhaseResult(result: PhaseResult): ExecutionContext =
        copy(
            phaseResults = phaseResults + (result.phaseName to result),
            architecturalDecisions = architecturalDecisions + result.decisions,
        )

    fun addDecision(decision: Decision): ExecutionContext =
        copy(
            architecturalDecisions = architecturalDecisions + decision,
        )

    fun addInteraction(interaction: Interaction): ExecutionContext =
        copy(
            interactions = interactions + interaction,
        )

    fun addArtifact(artifact: Artifact): ExecutionContext =
        copy(
            artifacts = artifacts + artifact,
        )

    fun getPhaseResult(phaseName: String): PhaseResult? = phaseResults[phaseName]

    fun hasCompletedPhase(phaseName: String): Boolean = phaseResults.containsKey(phaseName)

    fun getDecisionsByPhase(phaseName: String): List<Decision> = architecturalDecisions.filter { it.phase == phaseName }

    fun advanceToNextPhase(): ExecutionContext = copy(currentPhaseIndex = currentPhaseIndex + 1)
}
