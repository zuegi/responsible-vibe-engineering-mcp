package ch.zuegi.rvmcp

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Mock AskUserTool für automatisierte Tests.
 *
 * Gibt automatisch vordefinierte Antworten zurück statt auf User-Input zu warten.
 * Verhindert, dass Tests auf stdin blockieren.
 */
class MockAskUserTool(
    private val autoAnswer: String = "Yes, proceed with the workflow execution.",
) : SimpleTool<MockAskUserTool.Args>() {
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
        println("🤖 Mock: LLM asked: ${args.question}")
        println("🤖 Mock: Auto-answering: $autoAnswer")
        return autoAnswer
    }
}
