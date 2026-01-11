package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.document.DocumentMetadata
import ch.zuegi.rvmcp.domain.model.document.DocumentType
import ch.zuegi.rvmcp.domain.model.document.GeneratedDocument
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.requirement.Requirement
import ch.zuegi.rvmcp.domain.model.requirement.Stakeholder
import ch.zuegi.rvmcp.domain.port.output.DocumentPersistencePort
import ch.zuegi.rvmcp.domain.template.RequirementsTemplate
import ch.zuegi.rvmcp.shared.rvmcpLogger
import java.time.Instant

/**
 * Service for generating engineering documents from phase results.
 *
 * Documents are generated using Kotlin String Templates (ADR-005)
 * and persisted via DocumentPersistencePort.
 */
class DocumentGenerationService(
    private val documentPersistence: DocumentPersistencePort,
) {
    private val logger by rvmcpLogger()

    suspend fun generateAndPersistRequirementsDoc(
        phaseResult: PhaseResult,
        context: ExecutionContext,
    ): GeneratedDocument {
        val doc = generateRequirementsDoc(phaseResult, context)

        // Persist
        documentPersistence
            .saveDocument(doc, context)
            .onFailure { error ->
                logger.error("Failed to persist document: ${error.message}")
            }

        return doc
    }

    private fun generateRequirementsDoc(
        phaseResult: PhaseResult,
        context: ExecutionContext,
    ): GeneratedDocument {
        logger.info("Generating requirements document for phase: {}", phaseResult.phaseName)

        // Extract requirements from phase result
        val requirements = extractRequirements(phaseResult)
        val stakeholders = extractStakeholders(phaseResult)

        // Generate markdown content
        val content =
            RequirementsTemplate.generate(
                projectName = context.projectPath.substringAfterLast("/"),
                summary = phaseResult.summary,
                requirements = requirements,
                stakeholders = stakeholders,
                decisions = context.architecturalDecisions,
            )

        // Create document
        return GeneratedDocument(
            filename = "docs/requirements.md",
            content = content,
            type = DocumentType.REQUIREMENTS,
            metadata =
                DocumentMetadata(
                    generatedAt = Instant.now(),
                    phaseName = phaseResult.phaseName,
                    version = "1.0",
                ),
        )
    }

    /**
     * Extracts requirements from phase result.
     *
     * TODO: Implement proper extraction logic based on workflow output
     * Currently returns mock data for demonstration.
     */
    private fun extractRequirements(phaseResult: PhaseResult): List<Requirement> {
        logger.debug("Extracting requirements from phase result")

        // TODO: Parse phaseResult.summary or use structured workflow output
        // For now: return empty or mock data
        return emptyList()

        // Future implementation:
        // - Parse LLM output from phase result
        // - Extract structured requirement data
        // - Use QuestionCatalog responses as input
    }

    /**
     * Extracts stakeholders from phase result.
     */
    private fun extractStakeholders(phaseResult: PhaseResult): List<Stakeholder> {
        logger.debug("Extracting stakeholders from phase result")

        // TODO: Parse stakeholder information from workflow
        return emptyList()
    }
}
