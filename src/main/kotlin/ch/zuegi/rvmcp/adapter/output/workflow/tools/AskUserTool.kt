package ch.zuegi.rvmcp.adapter.output.workflow.tools

import ai.koog.agents.core.tools.SimpleTool
import ch.zuegi.rvmcp.adapter.output.workflow.InteractionContextElement
import ch.zuegi.rvmcp.domain.port.output.UserInteractionPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Koog Tool für User-Interaktion in Workflows.
 *
 * Erlaubt dem LLM Agent, Fragen an den User zu stellen und auf Antworten zu warten.
 * Dies ist der Schlüssel für interaktive Requirements-Gathering Workflows.
 *
 * Uses UserInteractionPort abstraction:
 * - MCP Mode: Sets InteractionRequest in InteractionContextElement (workflow pauses)
 * - CLI Mode: Uses stdin/stdout (direct interaction)
 * - Test Mode: Returns predefined answers
 *
 * Usage in Koog Agent:
 * ```kotlin
 * val interactionContext = InteractionContextElement()
 * val toolRegistry = ToolRegistry {
 *     tool(AskUserTool(userInteractionPort, interactionContext))
 * }
 * ```
 *
 * Der LLM Agent kann dann:
 * ```
 * LLM: I need to ask the user: "What should the feature do?"
 * Tool Call: ask_user(question="What should the feature do?")
 * [User wird gefragt, gibt Antwort]
 * Tool Result: "An OAuth2 login system"
 * LLM: [verarbeitet Antwort weiter]
 * ```
 */
class AskUserTool(
    private val userInteractionPort: UserInteractionPort,
    private val interactionContext: InteractionContextElement? = null,
) : SimpleTool<AskUserTool.Args>(
        argsSerializer = serializer<Args>(),
        name = "ask_user",
        description = "Ask the user a question and wait for their response. Use this when you need information from the user to proceed.",
    ) {
    private val logger by rvmcpLogger()

    @Serializable
    data class Args(
        val question: String,
    )

    override suspend fun execute(args: Args): String {
        logger.info("ask_user tool called with question: ${args.question}")

        // Delegate to UserInteractionPort
        val answer =
            userInteractionPort.askUser(
                question = args.question,
                context = emptyMap(),
            )

        // If InteractionContextElement was provided and request was set, log it
        if (interactionContext?.hasRequest() == true) {
            logger.info("InteractionRequest captured in context for question: ${args.question}")
        }

        return answer
    }
}
