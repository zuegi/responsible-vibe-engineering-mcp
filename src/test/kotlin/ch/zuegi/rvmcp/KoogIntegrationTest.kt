package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.workflow.KoogWorkflowExecutor
import ch.zuegi.rvmcp.adapter.output.workflow.WorkflowTemplateParser
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.ExecutionId

fun main() {
    val parser = WorkflowTemplateParser()
    val executor = KoogWorkflowExecutor(parser, "YOUR_OPENAI_API_KEY")

    val templateFileName = "requirements-analysis.yml"
    val template = parser.parseTemplate(templateFileName)

    val context =
        ExecutionContext(
            projectPath = ".",
            gitBranch = "feature/koog-integration",
            executionId = ExecutionId("test-execution"),
        )

    println("🚀 Starting workflow execution with Azure OpenAI...")
    println("Template: ${template.name}")
    println("Nodes: ${template.nodes.size}")
    println()

    val result =
        executor.executeWorkflow(
            template = templateFileName,
            context = context,
        )

    println("\n✅ Workflow completed!")
    println("Summary: ${result.summary}")
    println("State: ${result.success}")
}
