package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List

// =============================
// Test Data Builder functions
// =============================

fun createExecutionContext(
    executionId: ExecutionId = ExecutionId.generate(),
    projectPath: String = "./tmp/project",
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

fun createProcessPhase(
    name: String,
    order: Int,
    vibeChecks: List<VibeCheck> =
        listOf(
            VibeCheck(
                question = "Is everything ok?",
                type = VibeCheckType.QUALITY,
                required = true,
            ),
        ),
): ProcessPhase =
    ProcessPhase(
        name = name,
        description = "Test phase $name",
        vibeChecks = vibeChecks,
        koogWorkflowTemplate = "test-workflow.yml",
        order = order,
    )

fun createFakeWorkflowExecutionResult(
    success: Boolean = true,
    summary: String = "Das ist eine generierte Test-Summary",
    decisions: List<Decision> = emptyList(),
    vibeCheckResults: List<VibeCheckResult> = emptyList(),
    awaitingInput: Boolean = false,
    interactionRequest: InteractionRequest? = null,
    startedAt: Instant = Instant.now(),
    completedAt: Instant = Instant.now().plusMillis(250),
): WorkflowExecutionResult =
    WorkflowExecutionResult(
        success = success,
        summary = summary,
        decisions = decisions,
        vibeCheckResults = vibeCheckResults,
        awaitingInput = awaitingInput,
        interactionRequest = interactionRequest,
        startedAt = startedAt,
        completedAt = completedAt,
    )

fun createFakeWorkflowSummary(
    compressed: String = "ThisIsACompressedString",
    decisions: List<Decision> = emptyList(),
    keyInsights: List<String> = emptyList(),
): WorkflowSummary =
    WorkflowSummary(
        compressed = compressed,
        decisions = decisions,
        keyInsights = keyInsights,
    )
