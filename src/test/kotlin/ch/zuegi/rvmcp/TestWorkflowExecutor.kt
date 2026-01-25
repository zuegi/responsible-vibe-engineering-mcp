package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary

class TestWorkflowExecutor(
    private val result: WorkflowExecutionResult,
    private val summary: WorkflowSummary,
) : WorkflowExecutionPort {
    val calls = mutableListOf<Pair<String, ExecutionContext>>()

    override suspend fun executeWorkflow(
        template: String,
        context: ExecutionContext,
    ): WorkflowExecutionResult {
        calls += template to context
        return result
    }

    override suspend fun getSummary(): WorkflowSummary = summary
}
