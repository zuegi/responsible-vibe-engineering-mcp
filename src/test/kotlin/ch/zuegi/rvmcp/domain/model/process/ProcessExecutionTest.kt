package ch.zuegi.rvmcp.domain.model.process

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ProcessExecutionTest {
    private lateinit var testProcess: EngineeringProcess

    @BeforeEach
    fun setup() {
        val phase1 =
            ProcessPhase(
                name = "Requirements Analysis",
                description = "Gather and analyze requirements",
                vibeChecks =
                    listOf(
                        VibeCheck(
                            question = "Are requirements clear?",
                            type = VibeCheckType.REQUIREMENTS,
                        ),
                    ),
                koogWorkflowTemplate = "requirements-analysis.yml",
                order = 0,
            )

        val phase2 =
            ProcessPhase(
                name = "Architecture Design",
                description = "Design the architecture",
                vibeChecks =
                    listOf(
                        VibeCheck(
                            question = "Does architecture fit?",
                            type = VibeCheckType.ARCHITECTURE,
                        ),
                    ),
                koogWorkflowTemplate = "architecture-design.yml",
                order = 1,
            )

        testProcess =
            EngineeringProcess(
                id = ProcessId.generate(),
                name = "Feature Development",
                description = "Standard feature development",
                phases = listOf(phase1, phase2),
            )
    }

    @Test
    fun `should create valid process execution`() {
        val execution =
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 0,
                startedAt = Instant.now(),
            )

        assertThat(execution.status).isEqualTo(ExecutionStatus.IN_PROGRESS)
        assertThat(execution.currentPhaseIndex).isEqualTo(0)
        assertThat(execution.completedAt).isNull()
    }

    @Test
    fun `should fail when current phase index is negative`() {
        assertThatThrownBy {
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = -1,
                startedAt = Instant.now(),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Current phase index must be non-negative")
    }

    @Test
    fun `should fail when current phase index is out of bounds`() {
        assertThatThrownBy {
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 10,
                startedAt = Instant.now(),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Current phase index must be within process phases")
    }

    @Test
    fun `should fail when completed without timestamp`() {
        assertThatThrownBy {
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.COMPLETED,
                currentPhaseIndex = 0,
                startedAt = Instant.now(),
                completedAt = null,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Completed execution must have completion timestamp")
    }

    @Test
    fun `should get current phase`() {
        val execution =
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 0,
                startedAt = Instant.now(),
            )

        val currentPhase = execution.currentPhase()

        assertThat(currentPhase.name).isEqualTo("Requirements Analysis")
        assertThat(currentPhase.order).isEqualTo(0)
    }

    @Test
    fun `should move to next phase`() {
        val execution =
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.PHASE_COMPLETED,
                currentPhaseIndex = 0,
                startedAt = Instant.now(),
            )

        val nextExecution = execution.nextPhase()

        assertThat(nextExecution.currentPhaseIndex).isEqualTo(1)
        assertThat(nextExecution.status).isEqualTo(ExecutionStatus.IN_PROGRESS)
        assertThat(nextExecution.currentPhase().name).isEqualTo("Architecture Design")
    }

    @Test
    fun `should fail when moving to next phase beyond last phase`() {
        val execution =
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 1,
                startedAt = Instant.now(),
            )

        assertThatThrownBy {
            execution.nextPhase()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No next phase available")
    }

    @Test
    fun `should complete execution`() {
        val execution =
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 1,
                startedAt = Instant.now(),
            )

        val completed = execution.complete()

        assertThat(completed.status).isEqualTo(ExecutionStatus.COMPLETED)
        assertThat(completed.completedAt).isNotNull()
    }

    @Test
    fun `should fail execution`() {
        val execution =
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 0,
                startedAt = Instant.now(),
            )

        val failed = execution.fail()

        assertThat(failed.status).isEqualTo(ExecutionStatus.FAILED)
        assertThat(failed.completedAt).isNotNull()
    }

    @Test
    fun `should complete phase`() {
        val execution =
            ProcessExecution(
                id = ExecutionId.generate(),
                process = testProcess,
                status = ExecutionStatus.IN_PROGRESS,
                currentPhaseIndex = 0,
                startedAt = Instant.now(),
            )

        val phaseCompleted = execution.completePhase()

        assertThat(phaseCompleted.status).isEqualTo(ExecutionStatus.PHASE_COMPLETED)
        assertThat(phaseCompleted.currentPhaseIndex).isEqualTo(0)
    }
}
