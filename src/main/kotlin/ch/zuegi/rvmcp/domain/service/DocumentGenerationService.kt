package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.document.DocumentMetadata
import ch.zuegi.rvmcp.domain.model.document.DocumentType
import ch.zuegi.rvmcp.domain.model.document.GeneratedDocument
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.requirement.Requirement
import ch.zuegi.rvmcp.domain.model.requirement.RequirementPriority
import ch.zuegi.rvmcp.domain.model.requirement.RequirementType
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
     * Extracts requirements from phase result decisions.
     *
     * Strategy: Convert Decision objects to Requirements by:
     * 1. Filtering decisions that represent requirements (based on keywords)
     * 2. Mapping decision fields to requirement fields
     * 3. Inferring type and priority from decision text
     */
    private fun extractRequirements(phaseResult: PhaseResult): List<Requirement> {
        logger.debug("Extracting requirements from ${phaseResult.decisions.size} decisions")

        val requirements =
            phaseResult.decisions
                .filter { isRequirementDecision(it) }
                .mapIndexed { index, decision ->
                    Requirement(
                        id = "REQ-${index + 1}",
                        title = extractTitle(decision),
                        description = decision.reasoning,
                        type = inferRequirementType(decision),
                        priority = inferPriority(decision),
                        acceptanceCriteria = extractAcceptanceCriteria(decision),
                    )
                }

        logger.info("Extracted ${requirements.size} requirements from ${phaseResult.decisions.size} decisions")
        return requirements
    }

    /**
     * Determines if a decision represents a requirement.
     *
     * Keywords: requirement, must, should, feature, user, system
     */
    private fun isRequirementDecision(decision: Decision): Boolean {
        val keywords = listOf("requirement", "must", "should", "feature", "user", "system")
        val text = "${decision.decision} ${decision.reasoning}".lowercase()
        return keywords.any { keyword -> text.contains(keyword) }
    }

    /**
     * Extracts requirement title from decision.
     *
     * Uses the decision text, truncated to reasonable length.
     */
    private fun extractTitle(decision: Decision): String {
        // Take first sentence or up to 100 chars
        val title =
            decision.decision
                .split(".", "\n")
                .firstOrNull()
                ?.trim()
                ?: decision.decision

        return if (title.length > 100) {
            title.take(97) + "..."
        } else {
            title
        }
    }

    /**
     * Infers requirement type from decision text.
     *
     * Heuristics:
     * - "performance", "scalability" → NON_FUNCTIONAL
     * - "technical", "architecture" → TECHNICAL
     * - "business", "revenue" → BUSINESS
     * - Default: FUNCTIONAL
     */
    private fun inferRequirementType(decision: Decision): RequirementType {
        val text = "${decision.decision} ${decision.reasoning}".lowercase()

        return when {
            text.contains("performance") || text.contains("scalability") -> RequirementType.NON_FUNCTIONAL
            text.contains("technical") || text.contains("architecture") -> RequirementType.TECHNICAL
            text.contains("business") || text.contains("revenue") -> RequirementType.BUSINESS
            else -> RequirementType.FUNCTIONAL
        }
    }

    /**
     * Infers requirement priority from decision text.
     *
     * Heuristics:
     * - "must", "critical", "required" → MUST_HAVE
     * - "should", "important" → SHOULD_HAVE
     * - "could", "nice" → COULD_HAVE
     * - Default: SHOULD_HAVE
     */
    private fun inferPriority(decision: Decision): RequirementPriority {
        val text = "${decision.decision} ${decision.reasoning}".lowercase()

        return when {
            text.contains("must") || text.contains("critical") || text.contains("required") -> RequirementPriority.MUST_HAVE
            text.contains("could") || text.contains("nice") -> RequirementPriority.COULD_HAVE
            text.contains("wont") || text.contains("won't") -> RequirementPriority.WONT_HAVE
            else -> RequirementPriority.SHOULD_HAVE
        }
    }

    /**
     * Extracts acceptance criteria from decision reasoning.
     *
     * Looks for bullet points or numbered lists in reasoning text.
     */
    private fun extractAcceptanceCriteria(decision: Decision): List<String> {
        val criteria = mutableListOf<String>()

        // Pattern: Lines starting with "- ", "* ", or "1. "
        val bulletPattern = """^[\\s]*[-*]\\s+(.+)$""".toRegex(RegexOption.MULTILINE)
        val numberedPattern = """^[\\s]*\\d+\\.\\s+(.+)$""".toRegex(RegexOption.MULTILINE)

        bulletPattern.findAll(decision.reasoning).forEach { match ->
            criteria.add(match.groupValues[1].trim())
        }

        numberedPattern.findAll(decision.reasoning).forEach { match ->
            criteria.add(match.groupValues[1].trim())
        }

        return criteria
    }

    /**
     * Extracts stakeholders from phase result decisions.
     *
     * Strategy: Look for decisions mentioning stakeholder roles/names.
     * Uses keyword detection for common roles.
     */
    private fun extractStakeholders(phaseResult: PhaseResult): List<Stakeholder> {
        logger.debug("Extracting stakeholders from phase result")

        val stakeholderDecisions =
            phaseResult.decisions
                .filter { isStakeholderDecision(it) }

        val stakeholders =
            stakeholderDecisions
                .mapNotNull { decision ->
                    extractStakeholderFromDecision(decision)
                }.distinctBy { it.name }

        logger.info("Extracted ${stakeholders.size} stakeholders")
        return stakeholders
    }

    /**
     * Determines if a decision mentions stakeholders.
     */
    private fun isStakeholderDecision(decision: Decision): Boolean {
        val keywords = listOf("stakeholder", "user", "customer", "team", "owner", "manager")
        val text = "${decision.decision} ${decision.reasoning}".lowercase()
        return keywords.any { text.contains(it) }
    }

    /**
     * Extracts stakeholder information from decision text.
     *
     * Attempts to parse role and name from decision/reasoning.
     */
    private fun extractStakeholderFromDecision(decision: Decision): Stakeholder? {
        // Common role patterns
        val rolePatterns =
            mapOf(
                "product owner" to "Product Owner",
                "developer" to "Developer",
                "customer" to "Customer",
                "end user" to "End User",
                "business analyst" to "Business Analyst",
                "project manager" to "Project Manager",
            )

        val text = "${decision.decision} ${decision.reasoning}".lowercase()

        // Find matching role
        val role =
            rolePatterns.entries
                .firstOrNull { (pattern, _) -> text.contains(pattern) }
                ?.value
                ?: return null

        // Use role as name if no explicit name found
        return Stakeholder(
            name = role,
            role = role,
            responsibilities = listOf(decision.decision),
        )
    }
}
