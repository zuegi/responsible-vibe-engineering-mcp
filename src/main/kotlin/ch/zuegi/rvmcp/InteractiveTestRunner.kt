package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.memory.InMemoryMemoryRepository
import ch.zuegi.rvmcp.adapter.output.process.InMemoryProcessRepository
import ch.zuegi.rvmcp.adapter.output.vibe.ConsoleVibeCheckEvaluator
import ch.zuegi.rvmcp.adapter.output.workflow.KoogWorkflowExecutor
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService
import ch.zuegi.rvmcp.domain.service.StartProcessExecutionService
import ch.zuegi.rvmcp.infrastructure.config.LlmSetup
import kotlinx.coroutines.runBlocking

/**
 * Interactive test runner for testing the business logic WITH real LLM.
 *
 * KEY DIFFERENCE to ManualTestRunner:
 * - Uses KoogWorkflowExecutor (REAL LLM) instead of ManualWorkflowExecutor (dummy)
 * - Executes ACTUAL YAML workflows (requirements-analysis.yml, etc.)
 * - User experiences REAL LLM conversation (not just "Enter drücken")
 * - Tests EXACTLY the production logic
 *
 * This program:
 * 1. Sets up adapters with REAL KoogWorkflowExecutor
 * 2. Creates a sample engineering process (Feature Development)
 * 3. Executes the process phase by phase with REAL LLM interaction
 * 4. Tests the complete flow: Start → Execute Phases (with LLM) → Complete
 *
 * Usage:
 * - LLM Configuration wird aus application-local.yml geladen
 * - Run: mvn exec:java -Dexec.mainClass="ch.zuegi.rvmcp.InteractiveTestRunnerKt"
 * - Or: kotlin -classpath target/classes ch.zuegi.rvmcp.InteractiveTestRunnerKt
 *
 * Optional: Override via Environment Variables
 * - export AZURE_OPENAI_ENDPOINT="https://..." (überschreibt application-local.yml)
 * - export AZURE_OPENAI_API_KEY="your-key" (überschreibt application-local.yml)
 */
fun main() {
    println("╔════════════════════════════════════════════════════════╗")
    println("║   Responsible Vibe MCP - Interactive Test Runner     ║")
    println("║   Testing Business Logic MIT ECHTEM LLM              ║")
    println("╚════════════════════════════════════════════════════════╝")

    // 1. Setup and verify LLM configuration
    val llmProperties = LlmSetup.setupAndVerify() ?: return

    println("\n✓ Initializing KoogWorkflowExecutor (with REAL LLM)...")
    val workflowExecutor = KoogWorkflowExecutor(llmProperties)

    val vibeCheckEvaluator = ConsoleVibeCheckEvaluator()

    // 2. Setup: Adapters initialisieren
    val processRepository = InMemoryProcessRepository()
    val memoryRepository = InMemoryMemoryRepository()

    // 3. Services initialisieren (unverändert - wie in Produktion)
    val startService = StartProcessExecutionService(processRepository, memoryRepository)
    val executePhaseService =
        ExecuteProcessPhaseService(
            workflowExecutor = workflowExecutor, // Echter Executor!
            vibeCheckEvaluator = vibeCheckEvaluator,
        )
    val completePhaseService = CompletePhaseService(memoryRepository)

    // 4. Sample Process erstellen: "Feature Development"
    val featureDevelopmentProcess = createFeatureDevelopmentProcess()
    processRepository.save(featureDevelopmentProcess)

    println("\n✓ Setup abgeschlossen")
    println("  Process: ${featureDevelopmentProcess.name}")
    println("  Phasen: ${featureDevelopmentProcess.totalPhases()}")

    // 5. Process starten
    println("\n" + "=".repeat(60))
    println("\n🚀 Bereit zum Starten des Feature Development Prozesses")
    println("\n💡 HINWEIS: Der LLM wird dich jetzt interviewen!")
    println("   - Beantworte die Fragen des LLMs")
    println("   - Am Ende jeder Phase: Vibe Checks bestätigen")
    print("\nEnter drücken zum Starten...")
    System.out.flush() // Stelle sicher, dass Output geschrieben wird

    try {
        val input = readlnOrNull()
        println("Input empfangen: ${if (input.isNullOrEmpty()) "<Enter>" else input}")
    } catch (e: Exception) {
        println("\n⚠️ Fehler beim Lesen von stdin: ${e.message}")
        println("   Fahre trotzdem fort...")
    }
    // TODO Ask for Project and Git branch
    val project = "/Users/groot/WS/responsible-vibe-test-project"
    val gitBranch = "feature/new-feature"
    println("Starte mit dem Projekt")
    println("   Projekt: $project")
    println("   Branch: $gitBranch")

    var processExecution =
        runBlocking {
            startService.execute(
                processId = featureDevelopmentProcess.id,
                projectPath = project,
                gitBranch = gitBranch,
            )
        }

    // 6. Context laden
    var context =
        memoryRepository.load(
            projectPath = project,
            gitBranch = gitBranch,
        ) ?: throw IllegalStateException("Context not found")

    // 7. Phasen durchlaufen
    while (processExecution.status == ExecutionStatus.IN_PROGRESS ||
        processExecution.status == ExecutionStatus.PHASE_COMPLETED
    ) {
        println("\n" + "=".repeat(60))
        println("📍 Current Phase: ${processExecution.currentPhase().name}")
        println("   Phase ${processExecution.currentPhaseIndex + 1}/${processExecution.process.totalPhases()}")
        println("   Template: ${processExecution.currentPhase().koogWorkflowTemplate}")

        println("\n💡 ACHTUNG: Jetzt startet die LLM-Konversation!")
        println("   Die LLM wird dir Fragen stellen (aus dem YAML-Workflow).")
        println("   Beantworte sie so, als würdest du ein echtes Feature planen.")
        print("\nPhase starten? (Enter drücken): ")
        System.out.flush()

        try {
            // Der Code wartet nur darauf, dass der User Enter drückt, um fortzufahren
            readlnOrNull()
        } catch (e: Exception) {
            println("\n⚠️ stdin nicht verfügbar, fahre automatisch fort...")
        }

        // 🔑 HIER passiert die echte LLM-Interaktion!
        // Der KoogWorkflowExecutor führt requirements-analysis.yml aus
        // Der LLM stellt Fragen, der User antwortet
        val phaseResult =
            runBlocking {
                executePhaseService.execute(
                    phase = processExecution.currentPhase(),
                    context = context,
                )
            }

        // Update context with phase result
        context = context.addPhaseResult(phaseResult)

        // Wenn Phase fehlgeschlagen, frage ob wiederholen oder abbrechen
        if (phaseResult.status == ExecutionStatus.FAILED) {
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
            runBlocking {
                completePhaseService.execute(
                    execution = processExecution,
                    context = context,
                    phaseResult = phaseResult,
                )
            }
    }

    // 8. Zusammenfassung
    println("\n" + "=".repeat(60))
    println("📊 Zusammenfassung")
    println("=".repeat(60))
    println("Status: ${processExecution.status}")
    println("\nAbgeschlossene Phasen:")
    context.phaseHistory.forEach { phase ->
        println("  ✓ ${phase.phaseName}")
        println("    Summary: ${phase.summary.take(100)}...")
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
        artifact.content?.let { println("    ${it.take(100)}...") }
    }

    println("\n🎉 Interactive Test abgeschlossen!")
    println("\n✅ Du hast gerade die ECHTE Business Logic getestet:")
    println("   - KoogWorkflowExecutor mit echtem LLM")
    println("   - YAML-Workflows (requirements-analysis.yml, etc.)")
    println("   - Domain Services (Start, Execute, Complete)")
    println("   - Vibe Checks (Quality Gates)")
    println("   - Memory Persistence (ExecutionContext)")
}

/**
 * Erstellt einen Sample "Feature Development" Process.
 * WICHTIG: Nutzt die ECHTEN YAML-Workflows aus src/main/resources/workflows/
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
//            koogWorkflowTemplate = "requirements-analysis.yml", // Echte YAML-Datei!
            koogWorkflowTemplate = "instrument-collection-workflow.yml", // Echte YAML-Datei!
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
            koogWorkflowTemplate = "architecture-design.yml", // Echte YAML-Datei!
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
            koogWorkflowTemplate = "implementation.yml", // Echte YAML-Datei!
            order = 2,
        )

    return EngineeringProcess(
        id = ProcessId("feature-development"),
        name = "Feature Development",
        description = "Strukturierter Prozess für neue Features",
        phases = listOf(requirementsPhase, architecturePhase, implementationPhase),
    )
}
