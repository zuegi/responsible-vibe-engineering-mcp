package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.memory.InMemoryMemoryRepository
import ch.zuegi.rvmcp.adapter.output.process.InMemoryProcessRepository
import ch.zuegi.rvmcp.adapter.output.workflow.KoogWorkflowExecutor
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService
import ch.zuegi.rvmcp.domain.service.StartProcessExecutionService
import ch.zuegi.rvmcp.infrastructure.config.LlmProperties
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Simple End-to-End Test for the complete Responsible Vibe Engineering workflow.
 *
 * This test demonstrates the complete flow:
 * 1. User Request → Process Selection
 * 2. Process Execution starts
 * 3. Phase Execution (with Koog Workflow)
 * 4. Vibe Checks (automatic pass for testing)
 * 5. Results stored in memory
 * 6. Next phase or completion
 *
 * Architecture validation:
 * - Domain Services orchestrate the flow
 * - Ports & Adapters pattern works end-to-end
 * - Koog Integration executes real LLM workflows
 * - In-Memory persistence (no external dependencies)
 */
@Disabled
@SpringBootTest
@ActiveProfiles("test")
class SimpleEndToEndTest {
    @Autowired
    private lateinit var llmProperties: LlmProperties

    private lateinit var processRepository: InMemoryProcessRepository
    private lateinit var memoryRepository: InMemoryMemoryRepository
    private lateinit var workflowExecutor: KoogWorkflowExecutor
    private lateinit var vibeCheckEvaluator: AutoPassVibeCheckEvaluator

    private lateinit var startProcessService: StartProcessExecutionService
    private lateinit var executePhaseService: ExecuteProcessPhaseService
    private lateinit var completePhaseService: CompletePhaseService

    @BeforeEach
    fun setup() {
        // Initialize repositories
        processRepository = InMemoryProcessRepository()
        memoryRepository = InMemoryMemoryRepository()

        // Initialize workflow executor with Koog
        workflowExecutor =
            KoogWorkflowExecutor(
                llmProperties = llmProperties,
            )

        // Use automatic vibe check evaluator for testing
        vibeCheckEvaluator = AutoPassVibeCheckEvaluator()

        // Initialize domain services
        startProcessService =
            StartProcessExecutionService(
                processRepository,
                memoryRepository,
            )

        executePhaseService =
            ExecuteProcessPhaseService(
                workflowExecutor = workflowExecutor,
                vibeCheckEvaluator = vibeCheckEvaluator,
            )

        completePhaseService =
            CompletePhaseService(
                memoryRepository,
            )

        // Setup test data: Feature Development Process
        setupFeatureDevelopmentProcess()
    }

    @Test
    fun `should execute complete Feature Development workflow end-to-end`() {
        println("\n" + "=".repeat(80))
        println("🚀 SIMPLE END-TO-END TEST: Feature Development Process")
        println("=".repeat(80))

        // ===== STEP 1: Start Process Execution =====
        println("\n📋 STEP 1: Start Process Execution")
        val processId = ProcessId("feature-development")
        val projectPath = "/tmp/test-project"
        val gitBranch = "feature/new-feature"

        val processExecution =
            runBlocking {
                startProcessService.execute(
                    processId = processId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )
            }

        assertThat(processExecution).isNotNull
        assertThat(processExecution.status).isEqualTo(ExecutionStatus.IN_PROGRESS)
        assertThat(processExecution.currentPhaseIndex).isEqualTo(0)
        assertThat(processExecution.process.name).isEqualTo("Feature Development")

        println("✅ Process started successfully")
        println("   Execution ID: ${processExecution.id.value}")
        println("   Process: ${processExecution.process.name}")
        println("   Current Phase: ${processExecution.currentPhase().name}")

        // ===== STEP 2: Load Execution Context =====
        println("\n📂 STEP 2: Load Execution Context")
        var executionContext = memoryRepository.load(projectPath, gitBranch)
        assertThat(executionContext).isNotNull
        executionContext!!

        println("✅ Context loaded")
        println("   Project: ${executionContext.projectPath}")
        println("   Branch: ${executionContext.gitBranch}")

        // ===== STEP 3: Execute Phase "Requirements Analysis" =====
        println("\n⚙️  STEP 3: Execute Phase 'Requirements Analysis'")
        val phase = processExecution.currentPhase()

        val phaseResult =
            runBlocking {
                executePhaseService.execute(phase, executionContext)
            }
        executionContext = executionContext.addPhaseResult(phaseResult)

        assertThat(executionContext.phaseHistory).hasSize(1)
        assertThat(executionContext.phaseHistory.first().phaseName).isEqualTo("Requirements Analysis")
        assertThat(executionContext.phaseHistory.first().status).isEqualTo(ExecutionStatus.PHASE_COMPLETED)

        println("✅ Phase executed successfully")
        println("   Phase: ${executionContext.phaseHistory.first().phaseName}")
        println("   Status: ${executionContext.phaseHistory.first().status}")
        println("   Summary: ${executionContext.phaseHistory.first().summary.take(100)}...")

        // ===== STEP 4: Complete Phase and Move to Next =====
        println("\n📝 STEP 4: Complete Phase")
        val updatedExecution =
            runBlocking {
                completePhaseService.execute(processExecution, executionContext, phaseResult)
            }

        assertThat(updatedExecution.currentPhaseIndex).isEqualTo(1)
        assertThat(updatedExecution.status).isEqualTo(ExecutionStatus.IN_PROGRESS)

        println("✅ Phase completed, moved to next phase")
        println("   Next Phase: ${updatedExecution.currentPhase().name}")

        // ===== STEP 5: Verify Results =====
        println("\n🔍 STEP 5: Verify Results")
        val savedContext = memoryRepository.load(projectPath, gitBranch)
        assertThat(savedContext).isNotNull
        assertThat(savedContext!!.phaseHistory).hasSize(1)
        assertThat(savedContext.architecturalDecisions).isNotEmpty

        println("✅ Results persisted in memory")
        println("   Phases completed: ${savedContext.phaseHistory.size}")
        println("   Architectural decisions: ${savedContext.architecturalDecisions.size}")
        println("   Interactions: ${savedContext.interactions.size}")

        // ===== Summary =====
        println("\n" + "=".repeat(80))
        println("🎉 END-TO-END TEST SUCCESSFUL!")
        println("=".repeat(80))
        println("✅ Architecture validated:")
        println("   - Domain Services orchestrate flow")
        println("   - Ports & Adapters pattern works")
        println("   - Koog Integration executes LLM workflows")
        println("   - In-Memory persistence functional")
        println("   - Vibe Checks automated")
        println("=".repeat(80) + "\n")
    }

    private fun setupFeatureDevelopmentProcess() {
        val process =
            EngineeringProcess(
                id = ProcessId("feature-development"),
                name = "Feature Development",
                description = "Complete feature development workflow",
                phases =
                    listOf(
                        ProcessPhase(
                            name = "Requirements Analysis",
                            description = "Analyze and document requirements",
                            koogWorkflowTemplate = "simple-test.yml", // Use simple test workflow
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
                        ProcessPhase(
                            name = "Architecture Design",
                            description = "Design system architecture",
                            koogWorkflowTemplate = "simple-test.yml",
                            order = 1,
                            vibeChecks =
                                listOf(
                                    VibeCheck(
                                        question = "Does the architecture fit existing systems?",
                                        type = VibeCheckType.ARCHITECTURE,
                                        required = true,
                                    ),
                                ),
                        ),
                        ProcessPhase(
                            name = "Implementation",
                            description = "Implement the feature",
                            koogWorkflowTemplate = "simple-test.yml",
                            order = 2,
                            vibeChecks =
                                listOf(
                                    VibeCheck(
                                        question = "Is code quality acceptable?",
                                        type = VibeCheckType.QUALITY,
                                        required = true,
                                    ),
                                ),
                        ),
                    ),
            )

        processRepository.save(process)
        println("✓ Feature Development Process configured with ${process.totalPhases()} phases")
    }

    @Test
    fun `should execute all three phases of Feature Development workflow`() {
        println("\n" + "=".repeat(80))
        println("🔄 MULTI-PHASE TEST: Complete Feature Development (3 Phases)")
        println("=".repeat(80))

        val processId = ProcessId("feature-development")
        val projectPath = "/tmp/multi-phase-test"
        val gitBranch = "feature/complete-flow"

        // Start process
        var processExecution =
            runBlocking {
                startProcessService.execute(
                    processId = processId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )
            }

        assertThat(processExecution.status).isEqualTo(ExecutionStatus.IN_PROGRESS)
        println("✅ Process started with ${processExecution.process.totalPhases()} phases")

        // Execute all 3 phases
        for (phaseIndex in 0 until processExecution.process.totalPhases()) {
            println(
                "\n➡️  Executing Phase ${phaseIndex + 1}/" +
                    "${processExecution.process.totalPhases()}: ${processExecution.currentPhase().name}",
            )

            // Load context
            var executionContext = memoryRepository.load(projectPath, gitBranch)!!

            // Execute phase
            val phase = processExecution.currentPhase()
            val phaseResult =
                runBlocking {
                    executePhaseService.execute(phase, executionContext)
                }
            executionContext = executionContext.addPhaseResult(phaseResult)

            // Complete phase (phaseResult already added to context)
            processExecution =
                runBlocking {
                    completePhaseService.execute(processExecution, executionContext, phaseResult)
                }

            println("✅ Phase ${phaseIndex + 1} completed: ${phaseResult.phaseName}")
        }

        // Verify all phases completed
        assertThat(processExecution.status).isEqualTo(ExecutionStatus.COMPLETED)

        val savedContext = memoryRepository.load(projectPath, gitBranch)!!
        assertThat(savedContext.phaseHistory).hasSize(3)
        assertThat(savedContext.phaseHistory.map { it.phaseName }).containsExactly(
            "Requirements Analysis",
            "Architecture Design",
            "Implementation",
        )

        println("\n" + "=".repeat(80))
        println("🎉 MULTI-PHASE TEST SUCCESSFUL!")
        println("✅ All 3 phases completed")
        println("✅ Process status: ${processExecution.status}")
        println("✅ Total decisions: ${savedContext.architecturalDecisions.size}")
        println("=".repeat(80) + "\n")
    }

    @Test
    fun `should handle failed required vibe check correctly`() {
        println("\n" + "=".repeat(80))
        println("❌ ERROR HANDLING TEST: Failed Required Vibe Check")
        println("=".repeat(80))

        val processId = ProcessId("feature-development")
        val projectPath = "/tmp/failed-vibe-check-test"
        val gitBranch = "feature/vibe-check-fail"

        // Start process
        val processExecution =
            runBlocking {
                startProcessService.execute(
                    processId = processId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )
            }

        // Replace vibe check evaluator with one that fails required checks
        val failingEvaluator = FailingVibeCheckEvaluator()
        val executePhaseServiceWithFailure =
            ExecuteProcessPhaseService(
                workflowExecutor = workflowExecutor,
                vibeCheckEvaluator = failingEvaluator,
            )

        println("✅ Process started")
        println("⚠️  Executing phase with failing required vibe check...")

        // Execute phase (should fail vibe check)
        var executionContext = memoryRepository.load(projectPath, gitBranch)!!
        val phase = processExecution.currentPhase()
        val phaseResult =
            runBlocking {
                executePhaseServiceWithFailure.execute(phase, executionContext)
            }
        executionContext = executionContext.addPhaseResult(phaseResult)

        // Verify phase failed
        assertThat(phaseResult.status).isEqualTo(ExecutionStatus.FAILED)
        assertThat(phaseResult.vibeCheckResults).isNotEmpty
        assertThat(phaseResult.vibeCheckResults.any { !it.passed }).isTrue()

        println("\n✅ Phase correctly marked as FAILED")
        println("✅ Vibe check results captured: ${phaseResult.vibeCheckResults.size}")
        println("✅ Failed check: ${phaseResult.vibeCheckResults.first { !it.passed }.check.question}")

        println("\n" + "=".repeat(80))
        println("🎉 ERROR HANDLING TEST SUCCESSFUL!")
        println("✅ Failed vibe checks are properly handled")
        println("=".repeat(80) + "\n")
    }

    @Test
    fun `should throw exception when process not found`() {
        println("\n" + "=".repeat(80))
        println("❌ ERROR HANDLING TEST: Process Not Found")
        println("=".repeat(80))

        val nonExistentProcessId = ProcessId("non-existent-process")
        val projectPath = "/tmp/error-test"
        val gitBranch = "feature/error-test"

        println("⚠️  Attempting to start non-existent process...")

        // Should throw IllegalArgumentException
        val exception =
            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                runBlocking {
                    startProcessService.execute(
                        processId = nonExistentProcessId,
                        projectPath = projectPath,
                        gitBranch = gitBranch,
                    )
                }
            }

        assertThat(exception.message).contains("Process not found")
        assertThat(exception.message).contains(nonExistentProcessId.value)

        println("✅ Exception thrown as expected")
        println("✅ Error message: ${exception.message}")

        println("\n" + "=".repeat(80))
        println("🎉 ERROR HANDLING TEST SUCCESSFUL!")
        println("✅ Non-existent process properly rejected")
        println("=".repeat(80) + "\n")
    }
}

/**
 * Auto-passing Vibe Check Evaluator for testing.
 * All vibe checks pass automatically.
 */
class AutoPassVibeCheckEvaluator : ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort {
    override fun evaluate(
        vibeCheck: ch.zuegi.rvmcp.domain.model.vibe.VibeCheck,
        context: ch.zuegi.rvmcp.domain.model.context.ExecutionContext,
    ): ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult {
        println("   ✓ Vibe Check (auto-pass): ${vibeCheck.question}")
        return ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult(
            check = vibeCheck,
            passed = true,
            findings = "Automatically passed for testing",
        )
    }

    override fun evaluateBatch(
        vibeChecks: List<ch.zuegi.rvmcp.domain.model.vibe.VibeCheck>,
        context: ch.zuegi.rvmcp.domain.model.context.ExecutionContext,
    ): List<ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult> = vibeChecks.map { evaluate(it, context) }
}

/**
 * Failing Vibe Check Evaluator for error handling tests.
 * All required vibe checks fail automatically.
 */
class FailingVibeCheckEvaluator : ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort {
    override fun evaluate(
        vibeCheck: ch.zuegi.rvmcp.domain.model.vibe.VibeCheck,
        context: ch.zuegi.rvmcp.domain.model.context.ExecutionContext,
    ): ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult {
        val passed = !vibeCheck.required // Only required checks fail
        val status = if (passed) "✓" else "✗"
        println("   $status Vibe Check (${if (passed) "pass" else "FAIL"}): ${vibeCheck.question}")
        return ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult(
            check = vibeCheck,
            passed = passed,
            findings = if (passed) "Non-required check passed" else "Required check failed for testing",
        )
    }

    override fun evaluateBatch(
        vibeChecks: List<ch.zuegi.rvmcp.domain.model.vibe.VibeCheck>,
        context: ch.zuegi.rvmcp.domain.model.context.ExecutionContext,
    ): List<ch.zuegi.rvmcp.domain.model.vibe.VibeCheckResult> = vibeChecks.map { evaluate(it, context) }
}
