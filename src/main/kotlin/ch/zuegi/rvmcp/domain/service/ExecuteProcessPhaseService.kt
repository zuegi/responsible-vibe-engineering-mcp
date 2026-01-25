package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import java.time.Instant

/**
 * Domain service for executing a process phase.
 *
 * This service orchestrates:
 * 1. Workflow execution (via WorkflowExecutionPort)
 * 2. Vibe check evaluation (quality gates)
 * 3. Human-in-the-loop if vibe checks fail
 * 4. Context updates and persistence
 */
class ExecuteProcessPhaseService(
    private val workflowExecutor: WorkflowExecutionPort,
    private val vibeCheckEvaluator: VibeCheckEvaluatorPort,
    private val documentGenerationService: DocumentGenerationService,
) {
    private val logger by rvmcpLogger()

    /**
     * Executes a single process phase with the given context.
     *
     * @param phase The process phase to execute
     * @param context The current execution context
     * @return PhaseResult
     */
    suspend fun execute(
        phase: ProcessPhase,
        context: ExecutionContext,
    ): PhaseResult {
        val startTime = Instant.now()
        logger.info("Starting phase: {} - {}", phase.name, phase.description)

        // 1. Execute workflow
        val workflowResult =
            workflowExecutor.executeWorkflow(
                template = phase.koogWorkflowTemplate,
                context = context,
            )

        // 2. Evaluate vibe checks (with original context - decisions will be added later via PhaseResult)
        val vibeCheckResults =
            vibeCheckEvaluator.evaluateBatch(
                vibeChecks = phase.vibeChecks,
                context = context,
            )

        val allVibeChecksPassed = vibeCheckResults.all { it.passed }

        // 3. Human-in-the-loop if vibe checks failed
        if (!allVibeChecksPassed) {
            logger.warn("Some vibe checks failed for phase: {}", phase.name)
            vibeCheckResults.filter { !it.passed }.forEach { result ->
                logger.warn("Failed vibe check: {} - {}", result.check.question, result.findings)
            }

            if (phase.vibeChecks.any { it.required }) {
                logger.error("Required vibe checks not passed. Phase must be repeated.")

                return createFailedPhaseResult(
                    phase = phase,
                    vibeCheckResults = vibeCheckResults,
                    startTime = startTime,
                    workflowSummary = workflowResult.summary,
                    decisions = workflowResult.decisions,
                )
            }
        }

        // 4. Check if workflow is awaiting user input
        if (workflowResult.awaitingInput) {
            logger.info("Workflow is awaiting user input, returning paused phase result")
            return PhaseResult(
                phaseName = phase.name,
                status = ExecutionStatus.IN_PROGRESS,
                summary = workflowResult.summary,
                vibeCheckResults = emptyList(), // Don't evaluate vibe checks yet
                decisions = workflowResult.decisions,
                awaitingInput = true,
                interactionRequest = workflowResult.interactionRequest,
                startedAt = startTime,
                completedAt = null, // Not completed yet
            )
        }

        // 5. Create phase result (including workflow decisions)
        val phaseResult =
            PhaseResult(
                phaseName = phase.name,
                status = if (allVibeChecksPassed) ExecutionStatus.PHASE_COMPLETED else ExecutionStatus.FAILED,
                summary = workflowResult.summary,
                vibeCheckResults = vibeCheckResults,
                decisions = workflowResult.decisions,
                startedAt = startTime,
                completedAt = Instant.now(),
            )

        // 6. Generate documentation - and add GeneratedDocumentID to phaseResult
        documentGenerationService.generateAndPersistRequirementsDoc(phaseResult, context)

        logger.info("Phase completed: {}", phase.name)
        return phaseResult
    }

    private fun createFailedPhaseResult(
        phase: ProcessPhase,
        vibeCheckResults: List<ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult>,
        startTime: Instant,
        workflowSummary: String,
        decisions: List<ch.zuegi.rvmcp.domain.model.memory.Decision>,
    ): PhaseResult {
        val phaseResult =
            PhaseResult(
                phaseName = phase.name,
                status = ExecutionStatus.FAILED,
                summary = workflowSummary,
                vibeCheckResults = vibeCheckResults,
                decisions = decisions,
                startedAt = startTime,
                completedAt = Instant.now(),
            )

        return phaseResult
    }
}
