package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class WorkflowTemplateParserTest {
    private val parser = WorkflowTemplateParser()

    @Test
    fun `should parse requirements-analysis template`() {
        // When
        val template = parser.parseTemplate("requirements-analysis.yml")

        // Then
        assertThat(template.name).isEqualTo("Requirements Analysis")
        assertThat(template.description).contains("Sammelt und analysiert Anforderungen")
        assertThat(template.version).isEqualTo("1.0")
        assertThat(template.contextVariables).contains("project_path", "git_branch")
        assertThat(template.nodes).isNotEmpty()
        assertThat(template.graph.start).isEqualTo("gather_requirements")
        assertThat(template.graph.end).isEqualTo("prepare_vibe_checks")
    }

    @Test
    fun `should parse architecture-design template`() {
        // When
        val template = parser.parseTemplate("architecture-design.yml")

        // Then
        assertThat(template.name).isEqualTo("Architecture Design")
        assertThat(template.description).contains("Entwirft die technische Architektur")
        assertThat(template.nodes).hasSizeGreaterThan(5)
        assertThat(template.vibeChecks).isNotEmpty()
    }

    @Test
    fun `should parse implementation template`() {
        // When
        val template = parser.parseTemplate("implementation.yml")

        // Then
        assertThat(template.name).isEqualTo("Implementation")
        assertThat(template.nodes).hasSizeGreaterThan(5)
        assertThat(template.graph.start).isEqualTo("load_implementation_plan")
    }

    @Test
    fun `should validate template successfully`() {
        // Given
        val template = parser.parseTemplate("requirements-analysis.yml")

        // When/Then - should not throw
        parser.validateTemplate(template)
    }

    @Test
    fun `should throw exception for non-existent template`() {
        // When/Then
        assertThatThrownBy {
            parser.parseTemplate("non-existent.yml")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Failed to parse workflow template")
    }

    @Test
    fun `should parse LLM nodes correctly`() {
        // Given
        val template = parser.parseTemplate("requirements-analysis.yml")

        // When
        val llmNodes = template.nodes.filter { it.type == NodeType.LLM }

        // Then
        assertThat(llmNodes).isNotEmpty()
        llmNodes.forEach { node ->
            assertThat(node.prompt).isNotNull()
            assertThat(node.output).isNotNull()
        }
    }

    @Test
    fun `should parse conditional nodes correctly`() {
        // Given
        val template = parser.parseTemplate("requirements-analysis.yml")

        // When
        val conditionalNode = template.nodes.find { it.id == "check_ambiguities" }

        // Then
        assertThat(conditionalNode).isNotNull
        assertThat(conditionalNode?.type).isEqualTo(NodeType.CONDITIONAL)
        assertThat(conditionalNode?.condition).isNotNull()
        assertThat(conditionalNode?.ifTrue).isEqualTo("request_clarification")
        assertThat(conditionalNode?.ifFalse).isEqualTo("analyze_existing_architecture")
    }

    @Test
    fun `should parse human interaction nodes correctly`() {
        // Given
        val template = parser.parseTemplate("requirements-analysis.yml")

        // When
        val humanNode = template.nodes.find { it.id == "request_clarification" }

        // Then
        assertThat(humanNode).isNotNull
        assertThat(humanNode?.type).isEqualTo(NodeType.HUMAN_INTERACTION)
        assertThat(humanNode?.prompt).isNotNull()
        assertThat(humanNode?.required).isTrue()
    }

    @Test
    fun `should parse workflow graph correctly`() {
        // Given
        val template = parser.parseTemplate("requirements-analysis.yml")

        // Then
        assertThat(template.graph.edges).isNotEmpty()
        template.graph.edges.forEach { edge ->
            assertThat(edge.from).isNotBlank()
            assertThat(edge.to).isNotBlank()
        }
    }

    @Test
    fun `should parse vibe checks correctly`() {
        // Given
        val template = parser.parseTemplate("requirements-analysis.yml")

        // Then
        assertThat(template.vibeChecks).hasSize(4)
        template.vibeChecks.forEach { vibeCheck ->
            assertThat(vibeCheck.question).isNotBlank()
            assertThat(vibeCheck.type).isNotBlank()
        }
    }

    @Test
    fun `should parse outputs section correctly`() {
        // Given
        val template = parser.parseTemplate("requirements-analysis.yml")

        // Then
        assertThat(template.outputs).isNotNull
        assertThat(template.outputs?.summary).isNotBlank()
        assertThat(template.outputs?.artifacts).isNotEmpty()
        assertThat(template.outputs?.decisions).isNotEmpty()
    }
}
