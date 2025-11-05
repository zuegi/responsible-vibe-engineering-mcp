package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.memory.InMemoryMemoryRepository
import ch.zuegi.rvmcp.adapter.output.process.InMemoryProcessRepository
import ch.zuegi.rvmcp.adapter.output.vibe.ConsoleVibeCheckEvaluator
import ch.zuegi.rvmcp.adapter.output.workflow.ManualWorkflowExecutor
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService
import ch.zuegi.rvmcp.domain.service.StartProcessExecutionService

/**
 * Manual test runner for testing the business logic without Spring Boot.
 *
 * This program:
 * 1. Sets up dummy adapters
 * 2. Creates a sample engineering process (Feature Development)
 * 3. Executes the process phase by phase manually
 * 4. Tests the complete flow: Start → Execute Phases → Complete
 */
fun main() {
    println("╔════════════════════════════════════════════════════════╗")
    println("║   Responsible Vibe MCP - Manual Test Runner          ║")
    println("║   Testing Business Logic ohne KI/Koog                ║")
    println("╚════════════════════════════════════════════════════════╝")

    // 1. Setup: Dummy-Adapter initialisieren
    val processRepository = InMemoryProcessRepository()
    val memoryRepository = InMemoryMemoryRepository()
    val workflowExecutor = ManualWorkflowExecutor()
    val vibeCheckEvaluator = ConsoleVibeCheckEvaluator()

    // 2. Services initialisieren
    val startService = StartProcessExecutionService(processRepository, memoryRepository)
    val executePhaseService =
        ExecuteProcessPhaseService(
            workflowExecutor,
            vibeCheckEvaluator,
            memoryRepository,
        )
    val completePhaseService = CompletePhaseService(memoryRepository)

    // 3. Sample Process erstellen: "Feature Development"
    val featureDevelopmentProcess = createFeatureDevelopmentProcess()
    processRepository.save(featureDevelopmentProcess)

    println("\n✓ Setup abgeschlossen")
    println("  Process: ${featureDevelopmentProcess.name}")
    println("  Phasen: ${featureDevelopmentProcess.totalPhases()}")

    // 4. Process starten
    println("\n" + "=".repeat(60))
    print("\nBereit zum Starten? (Enter drücken)")
    readlnOrNull()

    var processExecution =
        startService.execute(
            processId = featureDevelopmentProcess.id,
            projectPath = "/Users/groot/test-project",
            gitBranch = "feature/new-feature",
        )

    // 5. Context laden
    var context =
        memoryRepository.load(
            projectPath = "/Users/groot/test-project",
            gitBranch = "feature/new-feature",
        ) ?: throw IllegalStateException("Context not found")

    // 6. Phasen durchlaufen
    while (processExecution.status == ch.zuegi.rvmcp.domain.model.status.ExecutionStatus.IN_PROGRESS ||
        processExecution.status == ch.zuegi.rvmcp.domain.model.status.ExecutionStatus.PHASE_COMPLETED
    ) {
        println("\n" + "=".repeat(60))
        println("📍 Current Phase: ${processExecution.currentPhase().name}")
        println("   Phase ${processExecution.currentPhaseIndex + 1}/${processExecution.process.totalPhases()}")

        print("\nPhase ausführen? (Enter drücken)")
        readlnOrNull()

        // Phase ausführen
        context =
            executePhaseService.execute(
                phase = processExecution.currentPhase(),
                context = context,
            )

        // Phase Result prüfen
        val phaseResult = context.phaseHistory.last()

        // Wenn Phase fehlgeschlagen, frage ob wiederholen oder abbrechen
        if (phaseResult.status == ch.zuegi.rvmcp.domain.model.status.ExecutionStatus.FAILED) {
            println("\n⚠️  Phase ist fehlgeschlagen!")
            print("Wiederholen? (j/n): ")
            val retry = readlnOrNull()?.lowercase()

            if (retry == "j" || retry == "y") {
                println("↺ Phase wird wiederholt...")
                continue // Gleiche Phase nochmal
            } else {
                println("⛔ Prozess abgebrochen")
                processExecution = completePhaseService.fail(processExecution, context)
                break
            }
        }

        // Phase abschließen und zur nächsten
        processExecution =
            completePhaseService.execute(
                execution = processExecution,
                context = context,
                phaseResult = phaseResult,
            )
    }

    // 7. Zusammenfassung
    println("\n" + "=".repeat(60))
    println("📊 Zusammenfassung")
    println("=".repeat(60))
    println("Status: ${processExecution.status}")
    println("\nAbgeschlossene Phasen:")
    context.phaseHistory.forEach { phase ->
        println("  ✓ ${phase.phaseName}")
        println("    Summary: ${phase.summary}")
        println("    Vibe Checks: ${phase.vibeCheckResults.count { it.passed }}/${phase.vibeCheckResults.size} bestanden")
    }

    println("\nArchitektur-Entscheidungen: ${context.architecturalDecisions.size}")
    context.architecturalDecisions.forEach { decision ->
        println("  • [${decision.phase}] ${decision.decision}")
        println("    → ${decision.reasoning}")
    }

    println("\nArtifacts: ${context.artifacts.size}")
    context.artifacts.forEach { artifact ->
        println("  • ${artifact.path} (${artifact.type})")
        artifact.content?.let { println("    $it") }
    }

    println("\n🎉 Test abgeschlossen!")
}

/**
 * Erstellt einen Sample "Feature Development" Process.
 */
private fun createFeatureDevelopmentProcess(): EngineeringProcess {
    val requirementsPhase =
        ProcessPhase(
            name = "Requirements Analysis",
            description = "Sammle und dokumentiere Anforderungen",
            vibeChecks =
                listOf(
                    VibeCheck(
                        question = "Sind die Requirements klar und vollständig?",
                        type = VibeCheckType.REQUIREMENTS,
                        required = true,
                    ),
                    VibeCheck(
                        question = "Wurden Edge Cases identifiziert?",
                        type = VibeCheckType.REQUIREMENTS,
                        required = false,
                    ),
                ),
            koogWorkflowTemplate = "requirements-analysis.yml",
            order = 0,
        )

    val architecturePhase =
        ProcessPhase(
            name = "Architecture Design",
            description = "Entwerfe die Architektur und Komponenten",
            vibeChecks =
                listOf(
                    VibeCheck(
                        question = "Passt das Design in die bestehende Architektur?",
                        type = VibeCheckType.ARCHITECTURE,
                        required = true,
                    ),
                    VibeCheck(
                        question = "Ist das Design testbar?",
                        type = VibeCheckType.ARCHITECTURE,
                        required = true,
                    ),
                ),
            koogWorkflowTemplate = "architecture-design.yml",
            order = 1,
        )

    val implementationPhase =
        ProcessPhase(
            name = "Implementation",
            description = "Implementiere das Feature mit Tests",
            vibeChecks =
                listOf(
                    VibeCheck(
                        question = "Entspricht der Code den Qualitätsstandards?",
                        type = VibeCheckType.QUALITY,
                        required = true,
                    ),
                    VibeCheck(
                        question = "Sind Tests vorhanden und aussagekräftig?",
                        type = VibeCheckType.TESTING,
                        required = true,
                    ),
                ),
            koogWorkflowTemplate = "implementation.yml",
            order = 2,
        )

    return EngineeringProcess(
        id = ProcessId("feature-development"),
        name = "Feature Development",
        description = "Strukturierter Prozess für neue Features",
        phases = listOf(requirementsPhase, architecturePhase, implementationPhase),
    )
}
