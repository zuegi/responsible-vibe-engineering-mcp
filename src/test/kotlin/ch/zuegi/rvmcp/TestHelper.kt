package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.ExecutionId

fun createExecutionExtension(
    executionId: ExecutionId = ExecutionId.generate(),
    projectPath: String,
    gitBranch: String = "main",
) = ExecutionContext(
    executionId = executionId,
    projectPath = projectPath,
    gitBranch = gitBranch,
)
