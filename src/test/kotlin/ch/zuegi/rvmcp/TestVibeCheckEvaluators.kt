package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort

/**
 * Auto-passing Vibe Check Evaluator for testing.
 * All vibe checks pass automatically.
 */
class AutoPassVibeCheckEvaluator : VibeCheckEvaluatorPort {
    override fun evaluate(
        vibeCheck: VibeCheck,
        context: ExecutionContext,
    ): VibeCheckResult {
        println("    Vibe Check (auto-pass): ${vibeCheck.question}")
        return VibeCheckResult(
            check = vibeCheck,
            passed = true,
            findings = "Automatically passed for testing",
        )
    }

    override fun evaluateBatch(
        vibeChecks: List<VibeCheck>,
        context: ExecutionContext,
    ): List<VibeCheckResult> = vibeChecks.map { evaluate(it, context) }
}

/**
 * Failing Vibe Check Evaluator for error handling tests.
 * All required vibe checks fail automatically.
 */
class FailingVibeCheckEvaluator : VibeCheckEvaluatorPort {
    override fun evaluate(
        vibeCheck: VibeCheck,
        context: ExecutionContext,
    ): VibeCheckResult {
        val passed = !vibeCheck.required // Only required checks fail
        val status = if (passed) "✓" else "✗"
        println("   $status Vibe Check (${if (passed) "pass" else "FAIL"}): ${vibeCheck.question}")
        return VibeCheckResult(
            check = vibeCheck,
            passed = passed,
            findings = if (passed) "Non-required check passed" else "Required check failed for testing",
        )
    }

    override fun evaluateBatch(
        vibeChecks: List<VibeCheck>,
        context: ExecutionContext,
    ): List<VibeCheckResult> = vibeChecks.map { evaluate(it, context) }
}
