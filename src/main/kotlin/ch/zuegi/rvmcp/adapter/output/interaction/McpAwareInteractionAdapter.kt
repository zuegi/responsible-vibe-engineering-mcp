package ch.zuegi.rvmcp.adapter.output.interaction

import ch.zuegi.rvmcp.adapter.output.workflow.InteractionContextElement
import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import ch.zuegi.rvmcp.domain.model.interaction.InteractionType
import ch.zuegi.rvmcp.domain.port.output.UserInteractionPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import kotlinx.coroutines.currentCoroutineContext

/**
 * MCP-aware implementation of UserInteractionPort.
 *
 * Two modes of operation:
 * 1. MCP Mode (mcpMode=true): Sets InteractionRequest in CoroutineContext to pause workflow
 * 2. CLI Mode (mcpMode=false): Uses stdin/stdout for direct interaction
 *
 * This adapter is the bridge between the domain (which just wants to ask questions)
 * and the MCP protocol (which requires pause/resume).
 *
 * Note: Uses CoroutineContext instead of ThreadLocal for proper coroutine support.
 */
class McpAwareInteractionAdapter(
    private val mcpMode: Boolean = true,
) : UserInteractionPort {
    private val logger by rvmcpLogger()

    override suspend fun askUser(
        question: String,
        context: Map<String, String>,
    ): String {
        logger.info("askUser called: $question (mcpMode=$mcpMode)")

        return if (mcpMode) {
            // MCP Mode: Set interaction request in CoroutineContext and return placeholder
            val request = createInteractionRequest(question, null, context)
            currentCoroutineContext()[InteractionContextElement]?.setRequest(request)
            logger.info("Workflow interruption signaled for question: $question")
            "[Awaiting user input: $question]"
        } else {
            // CLI Mode: Direct stdin interaction
            println("\n${"=".repeat(60)}")
            println("🤖 Question:")
            println(question)
            println("=".repeat(60))
            print("\n👤 Your answer: ")
            System.out.flush()

            readlnOrNull()?.trim() ?: ""
        }
    }

    override suspend fun askCatalogQuestion(
        questionId: String,
        question: String,
        context: Map<String, String>,
    ): String {
        logger.info("askCatalogQuestion called: $questionId (mcpMode=$mcpMode)")

        return if (mcpMode) {
            // MCP Mode: Set interaction request with catalog metadata in CoroutineContext
            val request = createInteractionRequest(question, questionId, context)
            currentCoroutineContext()[InteractionContextElement]?.setRequest(request)
            logger.info("Workflow interruption signaled for catalog question: $questionId")
            "[Awaiting catalog answer for: $questionId]"
        } else {
            // CLI Mode: Show catalog question
            println("\n${"=".repeat(60)}")
            println("📋 Catalog Question [$questionId]:")
            println(question)
            println("=".repeat(60))
            print("\n👤 Your answer: ")
            System.out.flush()

            readlnOrNull()?.trim() ?: ""
        }
    }

    override suspend fun requestApproval(
        question: String,
        context: Map<String, String>,
    ): String {
        logger.info("requestApproval called: $question (mcpMode=$mcpMode)")

        return if (mcpMode) {
            // MCP Mode: Set interaction request for approval in CoroutineContext
            val request =
                InteractionRequest(
                    type = InteractionType.APPROVAL,
                    question = question,
                    context = context,
                )
            currentCoroutineContext()[InteractionContextElement]?.setRequest(request)
            logger.info("Workflow interruption signaled for approval: $question")
            "[Awaiting approval: $question]"
        } else {
            // CLI Mode: Show approval request
            println("\n${"=".repeat(60)}")
            println("⚠️  Approval Required:")
            println(question)
            println("=".repeat(60))
            print("\n👤 Approve? (yes/no): ")
            System.out.flush()

            readlnOrNull()?.trim()?.lowercase() ?: "no"
        }
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
