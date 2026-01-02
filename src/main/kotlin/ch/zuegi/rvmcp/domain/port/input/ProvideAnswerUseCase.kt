package ch.zuegi.rvmcp.domain.port.input

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution

/**
 * Use Case: Provide Answer to Resume Paused Workflow
 *
 * When a workflow is paused (awaiting user input), this use case:
 * 1. Validates the execution is in AWAITING_INPUT state
 * 2. Records the user's answer
 * 3. Resumes the workflow execution
 *
 * This is called by the MCP `provide_answer` tool.
 */
interface ProvideAnswerUseCase {
    /**
     * Provides a user answer to resume a paused workflow execution.
     *
     * @param executionId ID of the paused execution
     * @param answer User's answer to the pending interaction request
     * @return Updated ProcessExecution (now RUNNING again)
     * @throws IllegalStateException if execution is not in AWAITING_INPUT state
     * @throws NoSuchElementException if execution not found
     */
    suspend fun execute(
        executionId: ExecutionId,
        answer: String,
    ): ProcessExecution
}
