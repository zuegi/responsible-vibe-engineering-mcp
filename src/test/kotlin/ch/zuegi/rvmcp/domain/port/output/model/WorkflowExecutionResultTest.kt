package ch.zuegi.rvmcp.domain.port.output.model

import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class WorkflowExecutionResultTest {
    @Test
    fun `should create workflow execution result with valid data`() {
        val startedAt = Instant.now()
        val completedAt = startedAt.plusSeconds(300)

        val result = WorkflowExecutionResult(
            success = true,
            summary = "Requirements gathered successfully",
            decisions = emptyList(),
            vibeCheckResults = emptyList(),
            startedAt = startedAt,
            completedAt = completedAt,
        )

        assertThat(result.success).isTrue()
        assertThat(result.summary).isEqualTo("Requirements gathered successfully")
        assertThat(result.startedAt).isEqualTo(startedAt)
        assertThat(result.completedAt).isEqualTo(completedAt)
    }

    @Test
    fun `should fail when summary is blank`() {
        val startedAt = Instant.now()

        assertThatThrownBy {
            WorkflowExecutionResult(
                success = true,
                summary = "",
                decisions = emptyList(),
                vibeCheckResults = emptyList(),
                startedAt = startedAt,
                completedAt = startedAt,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Summary must not be blank")
    }

    @Test
    fun `should fail when completedAt is before startedAt`() {
        val completedAt = Instant.now()
        val startedAt = completedAt.plusSeconds(100)

        assertThatThrownBy {
            WorkflowExecutionResult(
                success = true,
                summary = "Test",
                decisions = emptyList(),
                vibeCheckResults = emptyList(),
                startedAt = startedAt,
                completedAt = completedAt,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Completed at must be after or equal to started at")
    }

    @Test
    fun `should contain decisions and vibe check results`() {
        val startedAt = Instant.now()
        val decision = Decision(
            phase = "Requirements Analysis",
            decision = "Use hexagonal architecture",
            reasoning = "Better testability",
            date = LocalDate.now(),
        )

        val vibeCheck = VibeCheck(
            question = "Are requirements clear?",
            type = VibeCheckType.REQUIREMENTS,
            required = true,
        )

        val vibeCheckResult = VibeCheckResult(
            check = vibeCheck,
            passed = true,
            findings = "All requirements are well-defined",
            timestamp = startedAt,
        )

        val result = WorkflowExecutionResult(
            success = true,
            summary = "Phase completed",
            decisions = listOf(decision),
            vibeCheckResults = listOf(vibeCheckResult),
            startedAt = startedAt,
            completedAt = startedAt.plusSeconds(100),
        )

        assertThat(result.decisions).hasSize(1)
        assertThat(result.vibeCheckResults).hasSize(1)
        assertThat(result.decisions.first()).isEqualTo(decision)
        assertThat(result.vibeCheckResults.first()).isEqualTo(vibeCheckResult)
    }
}
