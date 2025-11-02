package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.memory.Artifact
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary

/**
 * Manual workflow executor for testing without AI.
 * Prompts user via console to simulate workflow execution.
 */
class ManualWorkflowExecutor : WorkflowExecutionPort {

    private var lastExecution: WorkflowExecutionResult? = null

    override fun executeWorkflow(
        template: String,
        context: ExecutionContext,
    ): WorkflowExecutionResult {
        println("\n=== Manual Workflow Execution ===")
        println("Template: $template")
        println("Project: ${context.projectPath}")
        println("Branch: ${context.gitBranch}")
        println("\nBitte führe die folgenden Schritte manuell durch:")

        // Simulate workflow steps based on template
        val steps = getWorkflowSteps(template)
        steps.forEach { step ->
            println("\n→ $step")
            print("  Fertig? (Enter drücken)")
            readlnOrNull()
        }

        // Collect results
        val decisions = collectDecisions()
        val artifacts = collectArtifacts()
        val output = collectOutput()

        lastExecution = WorkflowExecutionResult(
            success = true,
            summary = output,
            decisions = decisions,
            vibeCheckResults = emptyList(),
            startedAt = java.time.Instant.now().minusSeconds(300),
            completedAt = java.time.Instant.now(),
        )

        return lastExecution!!
    }

    override fun getSummary(): WorkflowSummary {
        return WorkflowSummary(
            compressed = lastExecution?.summary ?: "",
            decisions = lastExecution?.decisions ?: emptyList(),
            keyInsights = lastExecution?.decisions?.map { it.decision } ?: emptyList(),
        )
    }

    private fun getWorkflowSteps(template: String): List<String> {
        return when {
            template.contains("requirements") -> listOf(
                "Sammle Anforderungen vom User",
                "Identifiziere Edge Cases",
                "Dokumentiere Requirements in requirements.md",
            )
            template.contains("architecture") -> listOf(
                "Analysiere bestehende Architektur",
                "Entwerfe Komponenten",
                "Dokumentiere Architektur-Entscheidungen",
            )
            template.contains("implementation") -> listOf(
                "Implementiere Features",
                "Schreibe Unit Tests",
                "Code Review",
            )
            else -> listOf("Führe Workflow-Schritte aus")
        }
    }

    private fun collectDecisions(): List<Decision> {
        println("\n=== Entscheidungen dokumentieren ===")
        val decisions = mutableListOf<Decision>()

        while (true) {
            print("\nWeitere Entscheidung? (j/n): ")
            if (readlnOrNull()?.lowercase() != "j") break

            print("Phase: ")
            val phase = readlnOrNull() ?: continue

            print("Entscheidung: ")
            val decision = readlnOrNull() ?: continue

            print("Begründung: ")
            val reasoning = readlnOrNull() ?: ""

            decisions.add(
                Decision(
                    phase = phase,
                    decision = decision,
                    reasoning = reasoning,
                ),
            )
        }

        return decisions
    }

    private fun collectArtifacts(): List<Artifact> {
        println("\n=== Artifacts dokumentieren ===")
        val artifacts = mutableListOf<Artifact>()

        while (true) {
            print("\nWeiteres Artifact? (j/n): ")
            if (readlnOrNull()?.lowercase() != "j") break

            print("Pfad (z.B. docs/requirements.md): ")
            val path = readlnOrNull() ?: continue

            print("Beschreibung: ")
            val description = readlnOrNull() ?: ""

            artifacts.add(
                Artifact(
                    name = path.substringAfterLast('/', path),
                    type = ch.zuegi.rvmcp.domain.model.status.ArtifactType.DOCUMENTATION,
                    path = path,
                    content = description,
                ),
            )
        }

        return artifacts
    }

    private fun collectOutput(): String {
        println("\n=== Zusammenfassung ===")
        print("Kurze Zusammenfassung der Phase: ")
        return readlnOrNull() ?: "Phase abgeschlossen"
    }
}
