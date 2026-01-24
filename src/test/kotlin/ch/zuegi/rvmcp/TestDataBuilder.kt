package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck

fun createExecutionExtension(
    executionId: ExecutionId = ExecutionId.generate(),
    projectPath: String,
    gitBranch: String = "main",
) = ExecutionContext(
    executionId = executionId,
    projectPath = projectPath,
    gitBranch = gitBranch,
)

fun testEngineeringProcess(engineeringProcessId: EngineeringProcessId = EngineeringProcessId.generate()): EngineeringProcess =
    EngineeringProcess(
        id = engineeringProcessId,
        name = "EngineeringProcess",
        description = "EngineeringDescription",
        phases =
            listOf(
                ProcessPhase(
                    name = "Requirements Analysis",
                    description = "Analyze and document requirements",
                    koogWorkflowTemplate = "simple-test.yml",
                    order = 0,
                    vibeChecks =
                        listOf(
                            VibeCheck(
                                question = "Are requirements clear and complete?",
                                type = VibeCheckType.REQUIREMENTS,
                                required = true,
                            ),
                            VibeCheck(
                                question = "Are edge cases considered?",
                                type = VibeCheckType.REQUIREMENTS,
                                required = false,
                            ),
                        ),
                ),
            ),
    )
