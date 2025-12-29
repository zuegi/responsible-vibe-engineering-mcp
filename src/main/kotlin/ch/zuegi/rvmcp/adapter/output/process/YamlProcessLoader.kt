package ch.zuegi.rvmcp.adapter.output.process

import ch.zuegi.rvmcp.adapter.output.workflow.WorkflowTemplateParser
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import org.springframework.stereotype.Component

/**
 * Loads EngineeringProcess definitions from YAML workflow templates.
 *
 * Converts workflow YAMLs into ProcessPhases and combines them into
 * a full EngineeringProcess.
 */
@Component
class YamlProcessLoader(
    private val yamlParser: WorkflowTemplateParser = WorkflowTemplateParser(),
) {
    /**
     * Loads the "Feature Development" process from YAML workflows.
     *
     */
    fun loadFeatureDevelopmentProcess(): EngineeringProcess {
        val phases =
            listOf(
                ProcessPhase(
                    name = "Requirements Analysis",
                    description = "Sammle und dokumentiere Anforderungen (Fast Test)",
                    vibeChecks =
                        listOf(
                            VibeCheck(
                                question = "Sind die Requirements klar?",
                                type = VibeCheckType.REQUIREMENTS,
                                required = true,
                            ),
                        ),
                    koogWorkflowTemplate = "requirements-analysis.yml",
                    order = 0,
                ),
                ProcessPhase(
                    name = "Architecture Design",
                    description = "Entwerfe die Architektur (Fast Test)",
                    vibeChecks =
                        listOf(
                            VibeCheck(
                                question = "Passt das Design in die Architektur?",
                                type = VibeCheckType.ARCHITECTURE,
                                required = true,
                            ),
                        ),
                    koogWorkflowTemplate = "architecture-design.yml",
                    order = 1,
                ),
                ProcessPhase(
                    name = "Implementation",
                    description = "Implementiere das Feature (Fast Test)",
                    vibeChecks =
                        listOf(
                            VibeCheck(
                                question = "Sind Tests vorhanden?",
                                type = VibeCheckType.QUALITY,
                                required = true,
                            ),
                        ),
                    koogWorkflowTemplate = "implementation.yml",
                    order = 2,
                ),
            )

        return EngineeringProcess(
            id = ProcessId("feature-development"),
            name = "Feature Development",
            description = "Strukturierter Prozess für neue Features (Fast Test Mode)",
            phases = phases,
        )
    }

    private fun createPhaseFromYaml(
        yamlPath: String,
        order: Int,
    ): ProcessPhase {
        val templateName = yamlPath.substringAfterLast("/").removeSuffix(".yml")
        val workflowTemplate = yamlParser.parseTemplate(templateName)

        // Convert YAML vibe_checks to domain VibeChecks
        val vibeChecks =
            workflowTemplate.vibeChecks.map { yamlCheck ->
                VibeCheck(
                    question = yamlCheck.question,
                    type = mapVibeCheckType(yamlCheck.type),
                    required = true, // Default: alle required
                )
            }

        return ProcessPhase(
            name = workflowTemplate.name,
            description = workflowTemplate.description,
            vibeChecks = vibeChecks,
            koogWorkflowTemplate = yamlPath.substringAfterLast("/"), // Extract filename
            order = order,
        )
    }

    private fun mapVibeCheckType(yamlType: String): VibeCheckType =
        when (yamlType.uppercase()) {
            "REQUIREMENTS" -> VibeCheckType.REQUIREMENTS
            "ARCHITECTURE" -> VibeCheckType.ARCHITECTURE
            "DESIGN" -> VibeCheckType.ARCHITECTURE
            "QUALITY" -> VibeCheckType.QUALITY
            "TESTING" -> VibeCheckType.TESTING
            "COMPLETENESS" -> VibeCheckType.QUALITY
            else -> VibeCheckType.QUALITY
        }
}
