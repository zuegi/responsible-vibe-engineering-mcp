package ch.zuegi.rvmcp.domain.port.output

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult

/**
 * Port for evaluating vibe checks using AI-assisted validation.
 *
 * This port provides quality gates for each phase by evaluating
 * whether the work meets architectural, quality, and engineering standards.
 */
interface VibeCheckEvaluatorPort {
    /**
     * Evaluates a vibe check against the current execution context.
     *
     * The evaluator uses the execution context (phase results, decisions, artifacts)
     * to determine if the vibe check criteria are met. This may involve AI-assisted
     * analysis for complex architectural questions.
     *
     * @param vibeCheck The vibe check to evaluate
     * @param context The current execution context providing evaluation input
     * @return The result of the vibe check evaluation
     */
    fun evaluate(
        vibeCheck: VibeCheck,
        context: ExecutionContext,
    ): VibeCheckResult

    /**
     * Evaluates multiple vibe checks in batch.
     *
     * @param vibeChecks List of vibe checks to evaluate
     * @param context The current execution context providing evaluation input
     * @return List of vibe check results in the same order as input
     */
    fun evaluateBatch(
        vibeChecks: List<VibeCheck>,
        context: ExecutionContext,
    ): List<VibeCheckResult>
}
