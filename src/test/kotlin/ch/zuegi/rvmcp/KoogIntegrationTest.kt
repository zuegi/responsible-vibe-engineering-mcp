package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.workflow.RefactoredKoogWorkflowExecutor
import ch.zuegi.rvmcp.adapter.output.workflow.WorkflowPromptBuilder
import ch.zuegi.rvmcp.adapter.output.workflow.WorkflowTemplateParser
import ch.zuegi.rvmcp.adapter.output.workflow.YamlToKoogStrategyTranslator
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Integration test for Koog workflow execution with Azure OpenAI.
 *
 * This test verifies:
 * - YAML workflow parsing and validation
 * - Koog AIAgent integration with Azure OpenAI Gateway
 * - Workflow execution with real LLM calls
 * - Result generation and summary
 *
 * Note: Requires Azure OpenAI configuration.
 * See src/test/resources/application-test.yml.example for setup.
 */
class KoogIntegrationTest {
    private val parser = WorkflowTemplateParser()
    private val strategyTranslator = YamlToKoogStrategyTranslator()
    private val promptBuilder = WorkflowPromptBuilder()

    // Load config from environment or fallback
    private val baseUrl =
        System.getenv("AZURE_OPENAI_BASE_URL")
            ?: System.getProperty("azure.openai.base-url")
            ?: "https://api.openai.com/v1/"
    private val apiVersion = System.getenv("AZURE_OPENAI_API_VERSION") ?: "2024-05-01-preview"
    private val apiToken = System.getenv("AZURE_OPENAI_API_TOKEN") ?: "dummy"

    private val executor =
        RefactoredKoogWorkflowExecutor(
            parser,
            strategyTranslator,
            promptBuilder,
            baseUrl,
            apiVersion,
            apiToken,
        )

    @Test
    fun `should execute simple test workflow with single LLM node`() {
        // Given
        val templateFileName = "simple-test.yml"
        val context =
            ExecutionContext(
                projectPath = ".",
                gitBranch = "feature/yaml-workflow-templates",
                executionId = ExecutionId("simple-test-${System.currentTimeMillis()}"),
            )

        println("\n🚀 Starting SIMPLE workflow execution...")
        println("   Template: $templateFileName")

        // When
        val startTime = System.currentTimeMillis()
        val result =
            executor.executeWorkflow(
                template = templateFileName,
                context = context,
            )
        val duration = System.currentTimeMillis() - startTime

        // Then
        assertThat(result.success).isTrue()
        assertThat(result.summary).isNotBlank()

        println("\n✅ Simple workflow completed!")
        println("   Duration: ${duration}ms")
        println("   Summary: ${result.summary}")
    }

    @Test
    fun `should execute multi-node workflow with context preservation`() {
        // Given
        val templateFileName = "multi-node-test.yml"
        val context =
            ExecutionContext(
                projectPath = ".",
                gitBranch = "feature/yaml-workflow-templates",
                executionId = ExecutionId("multi-node-test-${System.currentTimeMillis()}"),
            )

        println("\n🔗 Testing MULTI-NODE workflow with context preservation...")
        println("   Template: $templateFileName")
        println("   This test verifies that the agent remembers information from previous nodes.")
        println()

        // When
        val startTime = System.currentTimeMillis()
        val result =
            executor.executeWorkflow(
                template = templateFileName,
                context = context,
            )
        val duration = System.currentTimeMillis() - startTime

        // Then
        assertThat(result.success).isTrue()
        assertThat(result.summary).isNotBlank()
        assertThat(result.decisions).hasSize(2) // 2 LLM nodes

        println("\n✅ Multi-node workflow completed!")
        println("   Duration: ${duration}ms")
        println("   Nodes executed: ${result.decisions.size}")
        println("   Summary:")
        println(result.summary.prependIndent("   "))
        println()
        println("📊 Context Preservation Check:")
        println("   Look for the agent recalling the secret code from step 1 in step 2.")
        println("   If the codes match, context was successfully preserved!")
    }

    @Test
    fun `should execute three-node workflow with full context chain`() {
        // Given
        val templateFileName = "three-node-test.yml"
        val context =
            ExecutionContext(
                projectPath = ".",
                gitBranch = "feature/yaml-workflow-templates",
                executionId = ExecutionId("three-node-test-${System.currentTimeMillis()}"),
            )

        println("\n🔗🔗🔗 Testing THREE-NODE workflow (max supported)...")
        println("   Template: $templateFileName")
        println("   This tests the maximum supported chain length.")
        println()

        // When
        val startTime = System.currentTimeMillis()
        val result =
            executor.executeWorkflow(
                template = templateFileName,
                context = context,
            )
        val duration = System.currentTimeMillis() - startTime

        // Then
        assertThat(result.success).isTrue()
        assertThat(result.summary).isNotBlank()
        assertThat(result.decisions).hasSize(3) // 3 LLM nodes

        println("\n✅ Three-node workflow completed!")
        println("   Duration: ${duration}ms (avg ${duration / 3}ms per node)")
        println("   Nodes executed: ${result.decisions.size}")
        println("   Summary:")
        println(result.summary.prependIndent("   "))
        println()
        println("🏆 Context Chain Check:")
        println("   The agent should correctly chain: City → Landmark → Summary")
        println("   All three pieces of information should be present in the final summary!")
    }

    @Test
    fun `should parse and validate requirements-analysis workflow template`() {
        // Given
        val templateFileName = "requirements-analysis.yml"

        // When
        val template = parser.parseTemplate(templateFileName)

        // Then
        assertThat(template.name).isEqualTo("Requirements Analysis")
        assertThat(template.nodes).isNotEmpty()
        assertThat(template.graph.start).isNotBlank()
        assertThat(template.graph.end).isNotBlank()

        println("✅ Template parsed successfully")
        println("   Name: ${template.name}")
        println("   Nodes: ${template.nodes.size}")
        println("   Start: ${template.graph.start}")
        println("   End: ${template.graph.end}")
    }

    @Test
    fun `should execute requirements-analysis workflow with Azure OpenAI`() {
        // Given
        val templateFileName = "requirements-analysis.yml"
        val context =
            ExecutionContext(
                projectPath = ".",
                gitBranch = "feature/yaml-workflow-templates",
                executionId = ExecutionId("test-execution-${System.currentTimeMillis()}"),
            )

        println("\n🚀 Starting workflow execution with Azure OpenAI...")
        println("   Template: $templateFileName")
        println("   Project: ${context.projectPath}")
        println("   Branch: ${context.gitBranch}")
        println("   Execution ID: ${context.executionId.value}")
        println()

        // When
        val startTime = System.currentTimeMillis()
        val result =
            executor.executeWorkflow(
                template = templateFileName,
                context = context,
            )
        val duration = System.currentTimeMillis() - startTime

        // Then
        assertThat(result.success).isTrue()
        assertThat(result.summary).isNotBlank()
        assertThat(result.startedAt).isNotNull()
        assertThat(result.completedAt).isNotNull()
        assertThat(result.completedAt).isAfterOrEqualTo(result.startedAt)

        println("\n✅ Workflow completed successfully!")
        println("   Duration: ${duration}ms")
        println("   Success: ${result.success}")
        println("   Decisions: ${result.decisions.size}")
        println("   Summary:")
        println("   ${result.summary.prependIndent("   ")}")

        // Verify summary generation
        val summary = executor.getSummary()
        assertThat(summary.compressed).isNotBlank()
        assertThat(summary.decisions).isEqualTo(result.decisions)

        println("\n📊 Workflow Summary:")
        println("   Compressed: ${summary.compressed}")
        println("   Key Insights: ${summary.keyInsights.size}")
    }

    @Test
    fun `should execute architecture-design workflow with Azure OpenAI`() {
        // Given
        val templateFileName = "architecture-design.yml"
        val context =
            ExecutionContext(
                projectPath = ".",
                gitBranch = "feature/yaml-workflow-templates",
                executionId = ExecutionId("test-arch-${System.currentTimeMillis()}"),
            )

        println("\n🚀 Starting architecture workflow execution...")
        println("   Template: $templateFileName")

        // When
        val result =
            executor.executeWorkflow(
                template = templateFileName,
                context = context,
            )

        // Then
        assertThat(result.success).isTrue()
        assertThat(result.summary).contains("Architecture Design")

        println("✅ Architecture workflow completed!")
        println("   Summary: ${result.summary}")
    }
}
