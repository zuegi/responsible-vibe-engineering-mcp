package ch.zuegi.rvmcp.adapter.output.interaction

import ch.zuegi.rvmcp.adapter.output.workflow.InteractionContextElement
import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import ch.zuegi.rvmcp.domain.model.interaction.InteractionType
import ch.zuegi.rvmcp.domain.port.output.UserInteractionPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import kotlinx.coroutines.currentCoroutineContext

/**
 * MCP implementation of UserInteractionPort.
 *
 * Suspends workflow execution when user interaction is needed via PendingInteractionManager.
 * The workflow remains suspended until provide_answer is called through the MCP server.
 *
 * This adapter is the bridge between the domain (which wants to ask questions)
 * and the MCP protocol (which requires pause/resume via coroutine suspension).
 */
class McpAwareInteractionAdapter : UserInteractionPort {
    private val logger by rvmcpLogger()

    override suspend fun askUser(
        question: String,
        context: Map<String, String>,
    ): String {
        logger.info("askUser called: $question")

        val request = createInteractionRequest(question, null, context)
        val contextElement = currentCoroutineContext()[InteractionContextElement]
        val executionId =
            contextElement?.executionId
                ?: throw IllegalStateException("executionId must be set in InteractionContextElement")

        logger.info("Suspending workflow for user interaction (executionId=$executionId)")

        // Suspend coroutine until provideAnswer() is called via MCP
        return PendingInteractionManager.awaitAnswer(executionId, request)
    }

    override suspend fun askCatalogQuestion(
        questionId: String,
        question: String,
        context: Map<String, String>,
    ): String {
        logger.info("askCatalogQuestion called: $questionId")

        val request = createInteractionRequest(question, questionId, context)
        val contextElement = currentCoroutineContext()[InteractionContextElement]
        val executionId =
            contextElement?.executionId
                ?: throw IllegalStateException("executionId must be set in InteractionContextElement")

        logger.info("Suspending workflow for catalog question: $questionId (executionId=$executionId)")

        // Suspend coroutine until provideAnswer() is called via MCP
        return PendingInteractionManager.awaitAnswer(executionId, request)
    }

    override suspend fun requestApproval(
        question: String,
        context: Map<String, String>,
    ): String {
        logger.info("requestApproval called: $question")

        val request =
            InteractionRequest(
                type = InteractionType.APPROVAL,
                question = question,
                context = context,
            )
        val contextElement = currentCoroutineContext()[InteractionContextElement]
        val executionId =
            contextElement?.executionId
                ?: throw IllegalStateException("executionId must be set in InteractionContextElement")

        logger.info("Suspending workflow for approval (executionId=$executionId)")

        // Suspend coroutine until provideAnswer() is called via MCP
        return PendingInteractionManager.awaitAnswer(executionId, request)
    }

    override fun createInteractionRequest(
        question: String,
        questionId: String?,
        context: Map<String, String>,
    ): InteractionRequest =
        when {
            questionId != null -> {
                InteractionRequest(
                    type = InteractionType.ASK_CATALOG_QUESTION,
                    question = question,
                    questionId = questionId,
                    context = context,
                )
            }

            question.contains("approve", ignoreCase = true) ||
                question.contains("confirm", ignoreCase = true) -> {
                InteractionRequest(
                    type = InteractionType.APPROVAL,
                    question = question,
                    context = context,
                )
            }

            else -> {
                InteractionRequest(
                    type = InteractionType.ASK_USER,
                    question = question,
                    context = context,
                )
            }
        }
}
