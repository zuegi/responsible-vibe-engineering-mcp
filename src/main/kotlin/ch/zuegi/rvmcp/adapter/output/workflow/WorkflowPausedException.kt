package ch.zuegi.rvmcp.adapter.output.workflow

import ai.koog.agents.core.agent.AIAgent
import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest

/**
 * Exception thrown to pause workflow execution and wait for user input.
 *
 * This exception carries the agent instance and interaction request,
 * allowing the workflow to resume later with the user's answer.
 */
class WorkflowPausedException(
    val agent: AIAgent<String, String>,
    val interactionRequest: InteractionRequest,
    message: String = "Workflow paused for user interaction: ${interactionRequest.question}",
) : Exception(message)
