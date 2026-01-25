package ch.zuegi.rvmcp.domain.template

import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.model.requirement.Requirement
import ch.zuegi.rvmcp.domain.model.requirement.RequirementPriority
import ch.zuegi.rvmcp.domain.model.requirement.RequirementType
import ch.zuegi.rvmcp.domain.model.requirement.Stakeholder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RequirementsTemplateTest {
    @Test
    fun `should generate valid markdown with all sections`() {
        // Given
        val requirements =
            listOf(
                Requirement(
                    id = "REQ-001",
                    title = "User Authentication",
                    description = "System must authenticate users via OAuth2",
                    type = RequirementType.FUNCTIONAL,
                    priority = RequirementPriority.MUST_HAVE,
                    acceptanceCriteria =
                        listOf(
                            "User can login with email",
                            "Session expires after 24h",
                        ),
                ),
            )

        val stakeholders =
            listOf(
                Stakeholder(
                    name = "John Doe",
                    role = "Product Owner",
                    email = "john@example.com",
                    responsibilities = listOf("Define requirements", "Approve features"),
                ),
            )

        val decisions =
            listOf(
                Decision(
                    phase = "Requirements Analysis",
                    decision = "Use OAuth2 for authentication",
                    reasoning = "Industry standard, better security",
                    date = LocalDate.now(),
                ),
            )

        // When
        val markdown =
            RequirementsTemplate.generate(
                projectName = "MyApp",
                summary = "Build a secure authentication system",
                requirements = requirements,
                stakeholders = stakeholders,
                decisions = decisions,
            )

        // Then
        assertThat(markdown).isNotBlank()
        assertThat(markdown).contains("# Requirements: MyApp")
        assertThat(markdown).contains("## Executive Summary")
        assertThat(markdown).contains("Build a secure authentication system")
        assertThat(markdown).contains("## Stakeholders")
        assertThat(markdown).contains("Product Owner: John Doe")
        assertThat(markdown).contains("REQ-001: User Authentication")
        assertThat(markdown).contains("User can login with email")
        assertThat(markdown).contains("Use OAuth2 for authentication")
    }

    @Test
    fun `should handle empty requirements gracefully`() {
        // When
        val markdown =
            RequirementsTemplate.generate(
                projectName = "EmptyProject",
                summary = "Project without requirements yet",
                requirements = emptyList(),
                stakeholders = emptyList(),
                decisions = emptyList(),
            )

        // Then
        assertThat(markdown).contains("*No stakeholders defined yet*")
        assertThat(markdown).contains("*No must have requirements*")
    }

    @Test
    fun `should group requirements by priority`() {
        // Given
        val requirements =
            listOf(
                Requirement(
                    "REQ-001",
                    "Feature A",
                    "Description A",
                    RequirementType.FUNCTIONAL,
                    RequirementPriority.MUST_HAVE,
                ),
                Requirement(
                    "REQ-002",
                    "Feature B",
                    "Description B",
                    RequirementType.FUNCTIONAL,
                    RequirementPriority.SHOULD_HAVE,
                ),
                Requirement(
                    "REQ-003",
                    "Feature C",
                    "Description C",
                    RequirementType.FUNCTIONAL,
                    RequirementPriority.COULD_HAVE,
                ),
            )

        // When
        val markdown =
            RequirementsTemplate.generate(
                projectName = "Test",
                summary = "Test",
                requirements = requirements,
                stakeholders = emptyList(),
                decisions = emptyList(),
            )

        // Then
        assertThat(markdown).contains("### Must-Have Requirements")
        assertThat(markdown).contains("REQ-001: Feature A")
        assertThat(markdown).contains("### Should-Have Requirements")
        assertThat(markdown).contains("REQ-002: Feature B")
        assertThat(markdown).contains("### Could-Have Requirements")
        assertThat(markdown).contains("REQ-003: Feature C")
    }
}
