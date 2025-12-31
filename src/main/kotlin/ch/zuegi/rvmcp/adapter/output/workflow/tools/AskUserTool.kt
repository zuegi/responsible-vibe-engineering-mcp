package ch.zuegi.rvmcp.adapter.output.workflow.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ch.zuegi.rvmcp.domain.port.output.UserInteractionPort
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Koog Tool für User-Interaktion in Workflows.
 *
 * Erlaubt dem LLM Agent, Fragen an den User zu stellen und auf Antworten zu warten.
 * Dies ist der Schlüssel für interaktive Requirements-Gathering Workflows.
 *
 * Uses UserInteractionPort abstraction:
 * - MCP Mode: Throws InteractionRequiredException (workflow pauses)
 * - CLI Mode: Uses stdin/stdout (direct interaction)
 * - Test Mode: Returns predefined answers
 *
 * Usage in Koog Agent:
 * ```kotlin
 * val toolRegistry = ToolRegistry {
 *     tool(AskUserTool(userInteractionPort))
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
) : SimpleTool<AskUserTool.Args>() {
    @Serializable
    data class Args(
        val question: String,
    )

    override val description: String =
        "Ask the user a question and wait for their response. Use this when you need information from the user to proceed."

    override val name: String = "ask_user"

    override val argsSerializer = serializer<Args>()

    override val descriptor =
        ToolDescriptor(
            name = "ask_user",
            description =
                """
                Ask the user a question and wait for their response.
                Use this when you need information from the user to proceed.
                
                Examples:
                - "What should this feature do?"
                - "Which authentication providers should be supported?"
                - "What are the performance requirements?"
                """.trimIndent(),
            requiredParameters =
                listOf(
                    ToolParameterDescriptor(
                        name = "question",
                        description = "The question to ask the user. Be specific and clear.",
                        type = ToolParameterType.String,
                    ),
                ),
        )

    override suspend fun doExecute(args: Args): String {
        // Delegate to UserInteractionPort
        // In MCP mode: throws InteractionRequiredException (will be caught by executor)
        // In CLI mode: uses stdin/stdout
        return userInteractionPort.askUser(
            question = args.question,
            context = emptyMap(),
        )
    }
}
