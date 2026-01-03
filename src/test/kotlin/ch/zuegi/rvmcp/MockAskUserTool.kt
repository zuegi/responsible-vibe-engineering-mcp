package ch.zuegi.rvmcp

import ai.koog.agents.core.tools.SimpleTool
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
) : SimpleTool<MockAskUserTool.Args>(
        argsSerializer = serializer<Args>(),
        name = "ask_user",
        description = "Ask the user a question and wait for their response. Use this when you need information from the user to proceed.",
    ) {
    @Serializable
    data class Args(
        val question: String,
    )

    override suspend fun execute(args: Args): String {
        println("🤖 Mock: LLM asked: ${args.question}")
        println("🤖 Mock: Auto-answering: $autoAnswer")
        return autoAnswer
    }
}
