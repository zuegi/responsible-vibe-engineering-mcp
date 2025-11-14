package ch.zuegi.rvmcp.adapter.output.vibe

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort

/**
 * Auto-pass Vibe Check Evaluator for non-interactive environments.
 *
 * Used in MCP Server mode where stdin is not available for user interaction.
 * All vibe checks automatically pass.
 */
class AutoPassVibeCheckEvaluator : VibeCheckEvaluatorPort {
    override fun evaluate(
        vibeCheck: VibeCheck,
        context: ExecutionContext,
    ): VibeCheckResult {
        return VibeCheckResult(
            check = vibeCheck,
            passed = true,
            findings = "Auto-passed (MCP Server Mode)",
        )
    }

    override fun evaluateBatch(
        vibeChecks: List<VibeCheck>,
        context: ExecutionContext,
    ): List<VibeCheckResult> {
        return vibeChecks.map { evaluate(it, context) }
    }
}
