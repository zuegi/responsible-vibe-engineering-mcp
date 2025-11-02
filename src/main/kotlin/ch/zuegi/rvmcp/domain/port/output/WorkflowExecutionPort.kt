package ch.zuegi.rvmcp.domain.port.output

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary

/**
 * Port for executing Kotlin Koog workflows.
 *
 * This port abstracts the execution of YAML-based workflow templates
 * and provides a bridge between Koog's Intelligent History Compression
 * and our Long-Term Memory (ExecutionContext).
 */
interface WorkflowExecutionPort {
    /**
     * Executes a workflow template with the given execution context.
     *
     * @param template The workflow template identifier (e.g., "requirements-analysis.yml")
     * @param context The current execution context containing project and phase information
     * @return The result of the workflow execution including decisions and vibe check results
     */
    fun executeWorkflow(
        template: String,
        context: ExecutionContext,
    ): WorkflowExecutionResult

    /**
     * Retrieves a compressed summary of the workflow execution.
     *
     * This method leverages Koog's Intelligent History Compression to provide
     * a condensed representation of the workflow execution for Long-Term Memory storage.
     *
     * @return A compressed summary containing key decisions and insights
     */
    fun getSummary(): WorkflowSummary
}
