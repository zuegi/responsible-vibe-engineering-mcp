package ch.zuegi.rvmcp.adapter.output.vibe

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort
import ch.zuegi.rvmcp.shared.rvmcpLogger

/**
 * Console-based Vibe Check Evaluator for manual testing.
 * Prompts the user via console to evaluate vibe checks.
 *
 * Note: Not annotated with @Component - should be manually created when needed.
 * For MCP Server mode, use AutoPassVibeCheckEvaluator instead (non-interactive).
 */
class ConsoleVibeCheckEvaluator : VibeCheckEvaluatorPort {
    private val log by rvmcpLogger()

    override fun evaluate(
        vibeCheck: VibeCheck,
        context: ExecutionContext,
    ): VibeCheckResult {
        log.info("\n=== Vibe Check ===")
        log.info("Frage: ${vibeCheck.question}")
        log.info("Typ: ${vibeCheck.type}")
        log.info("Obligatorisch: ${vibeCheck.required}")

        // Show context
        if (context.phaseHistory.isNotEmpty()) {
            log.info("\nBisherige Phasen:")
            context.phaseHistory.forEach { phase ->
                log.info("  - ${phase.phaseName}: ${phase.summary}")
            }
        }

        print("\nBesteht der Check? (j/n oder y/n): ")
        val input = readlnOrNull()?.lowercase()
        val passed = input == "j" || input == "y" || input == "yes" || input == "ja"

        val feedback =
            if (!passed) {
                print("Feedback/Begründung: ")
                readlnOrNull() ?: ""
            } else {
                ""
            }

        return VibeCheckResult(
            check = vibeCheck,
            passed = passed,
            findings = feedback.takeIf { it.isNotBlank() } ?: "Keine Anmerkungen",
        )
    }

    override fun evaluateBatch(
        vibeChecks: List<VibeCheck>,
        context: ExecutionContext,
    ): List<VibeCheckResult> = vibeChecks.map { evaluate(it, context) }
}
