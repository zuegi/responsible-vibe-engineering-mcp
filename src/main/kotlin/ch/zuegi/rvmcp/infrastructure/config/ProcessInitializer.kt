package ch.zuegi.rvmcp.infrastructure.config

import ch.zuegi.rvmcp.adapter.output.process.YamlProcessLoader
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

/**
 * Initializes default Engineering Processes on application startup.
 *
 * Loads processes from YAML workflow definitions.
 */
@Component
class ProcessInitializer(
    private val processRepository: ProcessRepositoryPort,
    private val yamlProcessLoader: YamlProcessLoader,
) {
    @PostConstruct
    fun initializeProcesses() {
        System.err.println("🔧 Initializing Engineering Processes from YAML workflows...")

        val featureDevelopment = yamlProcessLoader.loadFeatureDevelopmentProcess()
        processRepository.save(featureDevelopment)

        System.err.println("   ✅ Loaded: ${featureDevelopment.name}")
        System.err.println("      Phases: ${featureDevelopment.totalPhases()}")
        featureDevelopment.phases.forEach { phase ->
            System.err.println("        - ${phase.name} (${phase.vibeChecks.size} vibe checks)")
        }
        System.err.println()
    }
}
