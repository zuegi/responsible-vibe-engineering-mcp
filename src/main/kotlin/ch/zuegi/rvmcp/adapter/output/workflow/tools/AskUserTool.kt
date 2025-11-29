package ch.zuegi.rvmcp.adapter.output.workflow.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Koog Tool für User-Interaktion in Workflows.
 *
 * Erlaubt dem LLM Agent, Fragen an den User zu stellen und auf Antworten zu warten.
 * Dies ist der Schlüssel für interaktive Requirements-Gathering Workflows.
 *
 * Usage in Koog Agent:
 * ```kotlin
 * val toolRegistry = ToolRegistry {
 *     tool(AskUserTool())
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
class AskUserTool : SimpleTool<AskUserTool.Args>() {
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
        // Print question to console
        println("\n" + "=".repeat(60))
        println("🤖 LLM Question:")
        println(args.question)
        println("=".repeat(60))
        print("\n👤 Your answer: ")
        System.out.flush()

        // Wait for user input
        val lines = mutableListOf<String>()
        while (true) {
            val line = readlnOrNull() ?: break
            if (line.isBlank()) break
            if (line.endsWith("\\")) {
                lines.add(line.removeSuffix("\\"))
            } else {
                lines.add(line)
                break // oder weiter einlesen, je nach gewünschtem Verhalten
            }
        }
        val answer = lines.joinToString(" ")

        println("\n✓ Answer recorded: $answer\n")

        return answer
    }
}
