package ch.zuegi.rvmcp.infrastructure.config

import ch.zuegi.rvmcp.adapter.output.process.YamlProcessLoader
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
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
    private val log by rvmcpLogger()

    // FIXME Will ich den wirklich schon zu Beginn gestartet haben??
    @PostConstruct
    fun initializeProcesses() {
        log.info("🔧 Initializing Engineering Processes from YAML workflows...")

        val featureDevelopment = yamlProcessLoader.loadFeatureDevelopmentProcess()
        processRepository.save(featureDevelopment)

        log.info("   ✅ Loaded: ${featureDevelopment.name}")
        log.debug("      Phases: ${featureDevelopment.totalPhases()}")
        featureDevelopment.phases.forEach { phase ->
            log.debug("        - ${phase.name} (${phase.vibeChecks.size} vibe checks)")
        }
    }
}
