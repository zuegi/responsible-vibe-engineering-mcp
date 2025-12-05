package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.memory.InMemoryMemoryRepository
import ch.zuegi.rvmcp.adapter.output.process.InMemoryProcessRepository
import ch.zuegi.rvmcp.adapter.output.vibe.ConsoleVibeCheckEvaluator
import ch.zuegi.rvmcp.adapter.output.workflow.KoogWorkflowExecutor
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService
import ch.zuegi.rvmcp.domain.service.StartProcessExecutionService
import ch.zuegi.rvmcp.infrastructure.config.LlmHealthCheck
import ch.zuegi.rvmcp.infrastructure.config.LlmProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.runBlocking
import org.springframework.core.io.ClassPathResource

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

    // 1. Setup: Load LLM configuration from application-local.yml
    val llmProperties = loadLlmPropertiesFromYaml()
    println("\n✓ LLM Configuration loaded (from application-local.yml)")
    println("  Provider: ${llmProperties.provider}")
    println("  Base URL: ${llmProperties.baseUrl}")
    println("  API Version: ${llmProperties.apiVersion}")

    // 2. Setup: Adapters initialisieren
    val processRepository = InMemoryProcessRepository()
    val memoryRepository = InMemoryMemoryRepository()

    // 🔑 KEY CHANGE: KoogWorkflowExecutor statt ManualWorkflowExecutor
    println("\n✓ Initializing KoogWorkflowExecutor (with REAL LLM)...")
    val workflowExecutor = KoogWorkflowExecutor(llmProperties)

    // Test LLM connection early (before user input)
    println("\n🔍 Testing LLM connection (this may take ~10 seconds)...")
    val healthCheck = LlmHealthCheck(llmProperties)
    try {
        healthCheck.checkLlmConnection()
    } catch (e: Exception) {
        println("\n❌ LLM Health Check failed!")
        println("   Error: ${e.message}")
        println("\n⚠️  Workflows will likely fail. Do you want to continue anyway?")
        print("   Continue? (j/n): ")
        System.out.flush()

        val input =
            try {
                readlnOrNull()
            } catch (ex: Exception) {
                "n"
            }
        if (input?.lowercase() != "j" && input?.lowercase() != "y") {
            println("\n⛔ Aborted by user")
            return
        }
        println("\n⚠️  Continuing despite health check failure...")
    }

    val vibeCheckEvaluator = ConsoleVibeCheckEvaluator()

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
    while (processExecution.status == ch.zuegi.rvmcp.domain.model.status.ExecutionStatus.IN_PROGRESS ||
        processExecution.status == ch.zuegi.rvmcp.domain.model.status.ExecutionStatus.PHASE_COMPLETED
    ) {
        println("\n" + "=".repeat(60))
        println("📍 Current Phase: ${processExecution.currentPhase().name}")
        println("   Phase ${processExecution.currentPhaseIndex + 1}/${processExecution.process.totalPhases()}")
        println("   Template: ${processExecution.currentPhase().koogWorkflowTemplate}")

        println("\n💡 ACHTUNG: Jetzt startet die LLM-Konversation!")
        println("   Der LLM wird dir Fragen stellen (aus dem YAML-Workflow).")
        println("   Beantworte sie so, als würdest du ein echtes Feature planen.")
        print("\nPhase starten? (Enter drücken): ")
        System.out.flush()

        try {
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
 * Loads LLM properties from application-local.yml.
 *
 * Falls back to application.yml if application-local.yml not found.
 * Environment variables can override YAML values.
 *
 * Config structure in YAML:
 * ```yaml
 * llm:
 *   provider: azure-openai
 *   base-url: https://...
 *   api-version: 2024-05-01-preview
 *   api-token: dummy
 * ```
 */
private fun loadLlmPropertiesFromYaml(): LlmProperties {
    val yamlMapper =
        ObjectMapper(YAMLFactory()).apply {
            registerModule(KotlinModule.Builder().build())
        }

    // Try application-local.yml first, then application.yml
    val configFiles = listOf("application-local.yml", "application.yml")
    var config: Map<String, Any>? = null

    for (configFile in configFiles) {
        try {
            val resource = ClassPathResource(configFile)
            if (resource.exists()) {
                config =
                    resource.inputStream.use { inputStream ->
                        yamlMapper.readValue(inputStream, Map::class.java) as Map<String, Any>
                    }
                println("  Loaded from: $configFile")
                break
            }
        } catch (e: Exception) {
            // Continue to next file
        }
    }

    if (config == null) {
        throw IllegalStateException(
            """
            Could not find application-local.yml or application.yml!
            
            Please create src/main/resources/application-local.yml with:
            llm:
              provider: azure-openai
              base-url: https://your-gateway.example.com/openai/deployments/gpt-4o/
              api-version: 2024-05-01-preview
              api-token: dummy
            """.trimIndent(),
        )
    }

    // Extract llm config
    @Suppress("UNCHECKED_CAST")
    val llmConfig =
        config["llm"] as? Map<String, Any>
            ?: throw IllegalStateException("No 'llm' section found in config!")

    // Extract values with optional environment variable overrides
    val baseUrl =
        System.getenv("AZURE_OPENAI_ENDPOINT")
            ?: llmConfig["base-url"] as? String
            ?: llmConfig["baseUrl"] as? String
            ?: throw IllegalStateException("base-url not configured!")

    val apiToken =
        System.getenv("AZURE_OPENAI_API_KEY")
            ?: llmConfig["api-token"] as? String
            ?: llmConfig["apiToken"] as? String
            ?: "dummy"

    val apiVersion =
        System.getenv("AZURE_OPENAI_API_VERSION")
            ?: llmConfig["api-version"] as? String
            ?: llmConfig["apiVersion"] as? String
            ?: "2024-05-01-preview"

    val provider =
        System.getenv("LLM_PROVIDER")
            ?: llmConfig["provider"] as? String
            ?: "azure-openai"

    return LlmProperties(
        baseUrl = baseUrl,
        apiVersion = apiVersion,
        apiToken = apiToken,
        provider = provider,
    )
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
            koogWorkflowTemplate = "requirement-question-catalog.yml", // Echte YAML-Datei!
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
