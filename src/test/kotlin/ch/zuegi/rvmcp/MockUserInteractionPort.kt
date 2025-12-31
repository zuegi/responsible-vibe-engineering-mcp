package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import ch.zuegi.rvmcp.domain.model.interaction.InteractionType
import ch.zuegi.rvmcp.domain.port.output.UserInteractionPort

/**
 * Mock UserInteractionPort for automated tests.
 *
 * Returns predefined answers instead of waiting for user input.
 * Prevents tests from blocking on stdin.
 */
class MockUserInteractionPort(
    private val autoAnswer: String = "Yes, proceed with the workflow execution.",
) : UserInteractionPort {
    override suspend fun askUser(
        question: String,
        context: Map<String, String>,
    ): String {
        println("Mock: LLM asked: $question")
        println("Mock: Auto-answering: $autoAnswer")
        return autoAnswer
    }

    override suspend fun askCatalogQuestion(
        questionId: String,
        question: String,
        context: Map<String, String>,
    ): String {
        println("Mock: Catalog question [$questionId]: $question")
        println("Mock: Auto-answering: $autoAnswer")
        return autoAnswer
    }

    override suspend fun requestApproval(
        question: String,
        context: Map<String, String>,
    ): String {
        println("Mock: Approval request: $question")
        println("Mock: Auto-answering: $autoAnswer")
        return autoAnswer
    }

    override fun createInteractionRequest(
        question: String,
        questionId: String?,
        context: Map<String, String>,
    ): InteractionRequest =
        InteractionRequest(
            type = if (questionId != null) InteractionType.ASK_CATALOG_QUESTION else InteractionType.ASK_USER,
            question = question,
            questionId = questionId,
            context = context,
        )
}
