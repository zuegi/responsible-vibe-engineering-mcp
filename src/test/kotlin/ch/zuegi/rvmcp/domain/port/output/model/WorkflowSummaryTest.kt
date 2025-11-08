package ch.zuegi.rvmcp.domain.port.output.model

import ch.zuegi.rvmcp.domain.model.memory.Decision
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class WorkflowSummaryTest {
    @Test
    fun `should create workflow summary with valid data`() {
        val summary =
            WorkflowSummary(
                compressed = "User wants feature X with constraints Y and Z",
                decisions = emptyList(),
                keyInsights = listOf("Use REST API", "Implement caching"),
            )

        assertThat(summary.compressed).isEqualTo("User wants feature X with constraints Y and Z")
        assertThat(summary.keyInsights).hasSize(2)
    }

    @Test
    fun `should fail when compressed summary is blank`() {
        assertThatThrownBy {
            WorkflowSummary(
                compressed = "",
                decisions = emptyList(),
                keyInsights = emptyList(),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Compressed summary must not be blank")
    }

    @Test
    fun `should contain decisions`() {
        val decision =
            Decision(
                phase = "Architecture Design",
                decision = "Use microservices",
                reasoning = "Better scalability",
                date = LocalDate.now(),
            )

        val summary =
            WorkflowSummary(
                compressed = "Architecture defined",
                decisions = listOf(decision),
                keyInsights = emptyList(),
            )

        assertThat(summary.decisions).hasSize(1)
        assertThat(summary.decisions.first()).isEqualTo(decision)
    }
}
