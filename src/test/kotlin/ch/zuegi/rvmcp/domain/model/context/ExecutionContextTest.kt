package ch.zuegi.rvmcp.domain.model.context

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.memory.Artifact
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.model.memory.Interaction
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.status.ArtifactType
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.model.status.InteractionType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class ExecutionContextTest {
    @Test
    fun `should create valid execution context`() {
        val context =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "feature/new-feature",
            )

        assertThat(context.projectPath).isEqualTo("/path/to/project")
        assertThat(context.gitBranch).isEqualTo("feature/new-feature")
        assertThat(context.phaseResults).isEmpty()
        assertThat(context.architecturalDecisions).isEmpty()
        assertThat(context.interactions).isEmpty()
        assertThat(context.artifacts).isEmpty()
    }

    @Test
    fun `should fail when project path is blank`() {
        assertThatThrownBy {
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "",
                gitBranch = "main",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Project path must not be blank")
    }

    @Test
    fun `should fail when git branch is blank`() {
        assertThatThrownBy {
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Git branch must not be blank")
    }

    @Test
    fun `should add phase result`() {
        val context =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "main",
            )

        val phaseResult =
            PhaseResult(
                phaseName = "Requirements Analysis",
                status = ExecutionStatus.COMPLETED,
                summary = "Requirements gathered successfully",
                vibeCheckResults = emptyList(),
                startedAt = Instant.now(),
                completedAt = Instant.now(),
            )

        val updatedContext = context.addPhaseResult(phaseResult)

        assertThat(updatedContext.phaseResults).hasSize(1)
        assertThat(updatedContext.phaseResults["Requirements Analysis"]).isEqualTo(phaseResult)
    }

    @Test
    fun `should add decision`() {
        val context =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "main",
            )

        val decision =
            Decision(
                phase = "Architecture Design",
                decision = "Use Hexagonal Architecture",
                reasoning = "Better testability and maintainability",
                date = LocalDate.now(),
            )

        val updatedContext = context.addDecision(decision)

        assertThat(updatedContext.architecturalDecisions).hasSize(1)
        assertThat(updatedContext.architecturalDecisions).contains(decision)
    }

    @Test
    fun `should add interaction`() {
        val context =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "main",
            )

        val interaction =
            Interaction(
                timestamp = Instant.now(),
                type = InteractionType.PHASE_CONFIRMATION,
                context = "User confirmed requirements phase",
                userResponse = "Approved",
            )

        val updatedContext = context.addInteraction(interaction)

        assertThat(updatedContext.interactions).hasSize(1)
        assertThat(updatedContext.interactions).contains(interaction)
    }

    @Test
    fun `should add artifact`() {
        val context =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "main",
            )

        val artifact =
            Artifact(
                name = "UserService.kt",
                type = ArtifactType.CODE,
                path = "src/main/kotlin/UserService.kt",
                content = "class UserService { }",
            )

        val updatedContext = context.addArtifact(artifact)

        assertThat(updatedContext.artifacts).hasSize(1)
        assertThat(updatedContext.artifacts).contains(artifact)
    }

    @Test
    fun `should get phase result by name`() {
        val phaseResult =
            PhaseResult(
                phaseName = "Requirements Analysis",
                status = ExecutionStatus.COMPLETED,
                summary = "Requirements gathered",
                vibeCheckResults = emptyList(),
                startedAt = Instant.now(),
                completedAt = Instant.now(),
            )

        val context =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "main",
            ).addPhaseResult(phaseResult)

        val retrieved = context.getPhaseResult("Requirements Analysis")

        assertThat(retrieved).isEqualTo(phaseResult)
        assertThat(context.getPhaseResult("Non Existent")).isNull()
    }

    @Test
    fun `should check if phase is completed`() {
        val phaseResult =
            PhaseResult(
                phaseName = "Requirements Analysis",
                status = ExecutionStatus.COMPLETED,
                summary = "Requirements gathered",
                vibeCheckResults = emptyList(),
                startedAt = Instant.now(),
                completedAt = Instant.now(),
            )

        val context =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "main",
            ).addPhaseResult(phaseResult)

        assertThat(context.hasCompletedPhase("Requirements Analysis")).isTrue()
        assertThat(context.hasCompletedPhase("Architecture Design")).isFalse()
    }

    @Test
    fun `should get decisions by phase`() {
        val decision1 =
            Decision(
                phase = "Architecture Design",
                decision = "Use Hexagonal Architecture",
                reasoning = "Better testability",
            )

        val decision2 =
            Decision(
                phase = "Architecture Design",
                decision = "Use Kotlin Koog for AI",
                reasoning = "Framework support",
            )

        val decision3 =
            Decision(
                phase = "Implementation",
                decision = "Use Spring Boot",
                reasoning = "Enterprise ready",
            )

        val context =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "main",
            )
                .addDecision(decision1)
                .addDecision(decision2)
                .addDecision(decision3)

        val architectureDecisions = context.getDecisionsByPhase("Architecture Design")

        assertThat(architectureDecisions).hasSize(2)
        assertThat(architectureDecisions).contains(decision1, decision2)
        assertThat(architectureDecisions).doesNotContain(decision3)
    }

    @Test
    fun `should maintain immutability when adding elements`() {
        val originalContext =
            ExecutionContext(
                executionId = ExecutionId.generate(),
                projectPath = "/path/to/project",
                gitBranch = "main",
            )

        val decision =
            Decision(
                phase = "Test",
                decision = "Test decision",
                reasoning = "Test reasoning",
            )

        val updatedContext = originalContext.addDecision(decision)

        assertThat(originalContext.architecturalDecisions).isEmpty()
        assertThat(updatedContext.architecturalDecisions).hasSize(1)
    }
}
