package ch.zuegi.rvmcp.domain.model.process

import ch.zuegi.rvmcp.createProcessPhase
import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EngineeringProcessTest {
    @Test
    fun `should create valid engineering process`() {
        val phase1 = createProcessPhase("Phase 1", 0)
        val phase2 = createProcessPhase("Phase 2", 1)

        val process =
            EngineeringProcess(
                id = EngineeringProcessId.generate(),
                name = "Feature Development",
                description = "Standard feature development process",
                phases = listOf(phase1, phase2),
            )

        assertThat(process.name).isEqualTo("Feature Development")
        assertThat(process.phases).hasSize(2)
        assertThat(process.totalPhases()).isEqualTo(2)
    }

    @Test
    fun `should fail when name is blank`() {
        assertThatThrownBy {
            EngineeringProcess(
                id = EngineeringProcessId.generate(),
                name = "",
                description = "Test",
                phases = listOf(createProcessPhase("Phase 1", 0)),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Name must not be blank")
    }

    @Test
    fun `should fail when description is blank`() {
        assertThatThrownBy {
            EngineeringProcess(
                id = EngineeringProcessId.generate(),
                name = "Test",
                description = "",
                phases = listOf(createProcessPhase("Phase 1", 0)),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Description must not be blank")
    }

    @Test
    fun `should fail when phases list is empty`() {
        assertThatThrownBy {
            EngineeringProcess(
                id = EngineeringProcessId.generate(),
                name = "Test",
                description = "Test description",
                phases = emptyList(),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Process must have at least one phase")
    }

    @Test
    fun `should fail when phase order is not sequential`() {
        val phase1 = createProcessPhase("Phase 1", 0)
        val phase2 = createProcessPhase("Phase 2", 2) // Wrong order

        assertThatThrownBy {
            EngineeringProcess(
                id = EngineeringProcessId.generate(),
                name = "Test",
                description = "Test description",
                phases = listOf(phase1, phase2),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Phase order must be sequential starting from 0")
    }

    @Test
    fun `should get phase by index`() {
        val phase1 = createProcessPhase("Phase 1", 0)
        val phase2 = createProcessPhase("Phase 2", 1)

        val process =
            EngineeringProcess(
                id = EngineeringProcessId.generate(),
                name = "Test",
                description = "Test description",
                phases = listOf(phase1, phase2),
            )

        assertThat(process.getPhase(0)).isEqualTo(phase1)
        assertThat(process.getPhase(1)).isEqualTo(phase2)
        assertThat(process.getPhase(2)).isNull()
    }

    @Test
    fun `should check if next phase exists`() {
        val phase1 = createProcessPhase("Phase 1", 0)
        val phase2 = createProcessPhase("Phase 2", 1)
        val phase3 = createProcessPhase("Phase 3", 2)

        val process =
            EngineeringProcess(
                id = EngineeringProcessId.generate(),
                name = "Test",
                description = "Test description",
                phases = listOf(phase1, phase2, phase3),
            )

        assertThat(process.hasNextPhase(0)).isTrue()
        assertThat(process.hasNextPhase(1)).isTrue()
        assertThat(process.hasNextPhase(2)).isFalse()
    }

    @Test
    fun `should return total number of phases`() {
        val phases =
            listOf(
                createProcessPhase("Phase 1", 0),
                createProcessPhase("Phase 2", 1),
                createProcessPhase("Phase 3", 2),
            )

        val process =
            EngineeringProcess(
                id = EngineeringProcessId.generate(),
                name = "Test",
                description = "Test description",
                phases = phases,
            )

        assertThat(process.totalPhases()).isEqualTo(3)
    }
}
