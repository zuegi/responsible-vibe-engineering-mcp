package ch.zuegi.rvmcp.adapter.output.vibe

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort

/**
 * Console-based vibe check evaluator for manual testing.
 * Prompts user to answer quality gate questions.
 */
class ConsoleVibeCheckEvaluator : VibeCheckEvaluatorPort {
    override fun evaluate(
        vibeCheck: VibeCheck,
        context: ExecutionContext,
    ): VibeCheckResult {
        println("\n=== Vibe Check ===")
        println("Frage: ${vibeCheck.question}")
        println("Typ: ${vibeCheck.type}")
        println("Obligatorisch: ${vibeCheck.required}")

        // Show context
        if (context.phaseHistory.isNotEmpty()) {
            println("\nBisherige Phasen:")
            context.phaseHistory.forEach { phase ->
                println("  - ${phase.phaseName}: ${phase.summary}")
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
    ): List<VibeCheckResult> {
        return vibeChecks.map { evaluate(it, context) }
    }
}
