package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.interaction.McpAwareInteractionAdapter
import ch.zuegi.rvmcp.adapter.output.interaction.PendingInteractionManager
import ch.zuegi.rvmcp.adapter.output.memory.InMemoryPersistenceRepository
import ch.zuegi.rvmcp.adapter.output.process.InMemoryProcessRepository
import ch.zuegi.rvmcp.adapter.output.workflow.KoogWorkflowExecutor
import ch.zuegi.rvmcp.application.usecase.CompletePhaseUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.ExecuteProcessPhaseUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.ProvideAnswerUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.StartProcessExecutionUseCaseImpl
import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.ExecutionState
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.port.output.DocumentPersistencePort
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import ch.zuegi.rvmcp.domain.service.DocumentGenerationService
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService
import ch.zuegi.rvmcp.domain.service.StartProcessExecutionService
import ch.zuegi.rvmcp.infrastructure.config.LlmProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * End-to-End MCP Client Test with User Interaction Flow.
 *
 * This test simulates a complete MCP client interaction including:
 * 1. Start Process (via start_process tool)
 * 2. Execute Phase (via execute_phase tool)
 * 3. Workflow Pause (AWAITING_INPUT state)
 * 4. Provide Answer (via provide_answer tool)
 * 5. Resume Execution (automatic)
 * 6. Complete Phase (via complete_phase tool)
 *
 * Architecture:
 * - Simulates MCP client behavior through use cases
 * - Tests the new CoroutineContext-based interaction flow
 * - Validates AWAITING_INPUT → RUNNING state transitions
 * - Ensures interaction history is correctly maintained
 */
@Disabled("To be fixed when domain is REALLY read!!!")
@SpringBootTest
@ActiveProfiles("test")
class McpClientE2EWithInteractionTest {
    @Autowired
    private lateinit var llmProperties: LlmProperties

    private lateinit var processRepository: InMemoryProcessRepository
    private lateinit var memoryRepository: MemoryRepositoryPort
    private lateinit var workflowExecutor: KoogWorkflowExecutor
    private lateinit var vibeCheckEvaluator: AutoPassVibeCheckEvaluator

    private lateinit var startProcessUseCase: StartProcessExecutionUseCaseImpl
    private lateinit var executePhaseUseCase: ExecuteProcessPhaseUseCaseImpl
    private lateinit var provideAnswerUseCase: ProvideAnswerUseCaseImpl
    private lateinit var completePhaseUseCase: CompletePhaseUseCaseImpl

    @BeforeEach
    fun setup() {
        println("\n" + "=".repeat(80))
        println("🧪 MCP CLIENT E2E TEST WITH USER INTERACTION - Setup")
        println("=".repeat(80))

        // Initialize repositories
        processRepository = InMemoryProcessRepository()
        memoryRepository = InMemoryPersistenceRepository()
        val documentPersistence: DocumentPersistencePort = InMemoryPersistenceRepository()

        // Initialize workflow executor with Test UserInteractionPort
        // This port returns immediate test answers without suspending
        workflowExecutor =
            KoogWorkflowExecutor(
                llmProperties = llmProperties,
//                userInteractionPort = TestUserInteractionPort(),
                userInteractionPort = McpAwareInteractionAdapter(), // real implementation
            )

        // Use automatic vibe check evaluator
        vibeCheckEvaluator = AutoPassVibeCheckEvaluator()

        // Initialize domain services
        val startProcessService =
            StartProcessExecutionService(
                processRepository,
                memoryRepository,
            )

        val documentGenerationService =
            DocumentGenerationService(
                documentPersistence = documentPersistence,
            )

        val executePhaseService =
            ExecuteProcessPhaseService(
                workflowExecutor = workflowExecutor,
                vibeCheckEvaluator = vibeCheckEvaluator,
                documentGenerationService = documentGenerationService,
            )

        val completePhaseService =
            CompletePhaseService(
                memoryRepository,
            )

        // Initialize use cases
        startProcessUseCase = StartProcessExecutionUseCaseImpl(startProcessService)
        executePhaseUseCase = ExecuteProcessPhaseUseCaseImpl(executePhaseService)
        provideAnswerUseCase = ProvideAnswerUseCaseImpl(memoryRepository)
        completePhaseUseCase =
            CompletePhaseUseCaseImpl(
                completePhaseService,
                memoryRepository,
                processRepository,
            )

        // Setup test process with interaction
        setupProcessWithInteraction()

        println("✅ Setup complete")
    }

    @AfterEach
    fun cleanup() {
        PendingInteractionManager.resetForTest()
    }

    @Test
    fun testCompleteWorkflowWithUserInteractionPauseAndResume() =
        runBlocking<Unit> {
            // Step 1: MCP Client calls start_process
            val engineeringProcessId = EngineeringProcessId("interactive-feature-dev")
            val projectPath = "./tmp/e2e-interactive-test"
            val gitBranch = "feature/e2e-interaction"

            val execution =
                startProcessUseCase.execute(
                    engineeringProcessId = engineeringProcessId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )

            assertThat(execution).isNotNull
            assertThat(execution.state).isEqualTo(ExecutionState.RUNNING)
            assertThat(execution.status).isEqualTo(ExecutionStatus.IN_PROGRESS)

            // Step 2: MCP Client calls execute_phase
            val context = memoryRepository.load(projectPath, gitBranch)!!
            val phase = execution.currentPhase()

            val phaseResultJob =
                launch {
                    executePhaseUseCase.execute(
                        phase = phase,
                        context = context,
                    )
                }

            // Step 3: Warten bis Interaction aktiv ist
            awaitInteraction(execution.id.value)

            // ==========================================
            // Step 4: MCP Client polls get_phase_result
            // ==========================================
            println("\n📍 Step 4: MCP Client → get_phase_result (polling)")
            println("   ℹ️  Client detects AWAITING_INPUT status")
            println("   ℹ️  Client prompts user for answer")

            // Simulate user providing answer via MCP client
            val userAnswer =
                "The application should provide AI-driven software development assistance " +
                    "with requirements gathering, architecture design, and coding support."

            // ==========================================
            // Step 5: MCP Client calls provide_answer
            // ==========================================
            println("\n📍 Step 5: MCP Client → provide_answer")
            println("   User Answer: ${userAnswer.take(80)}...")

            val resumedExecution =
                provideAnswerUseCase.execute(
                    executionId = execution.id,
                    answer = userAnswer,
                )

            assertThat(resumedExecution).isNotNull
            assertThat(resumedExecution.state).isEqualTo(ExecutionState.RUNNING)
            assertThat(resumedExecution.pendingInteraction).isNull()
            assertThat(resumedExecution.interactionHistory).hasSize(1)
            assertThat(resumedExecution.interactionHistory[0].answer).isEqualTo(userAnswer)

            println("   ✅ Workflow Resumed")
            println("      State: ${resumedExecution.state}")
            println("      Interaction History: ${resumedExecution.interactionHistory.size} entries")
            println("      Last Answer: ${resumedExecution.interactionHistory[0].answer.take(80)}...")

            // ==========================================
            // Step 6: MCP Client calls execute_phase again to continue
            // ==========================================
            phaseResultJob.join()
            println("\n📍 Step 6: MCP Client → execute_phase (continue)")

            val updatedContext = memoryRepository.load(projectPath, gitBranch)!!
            val continuedPhaseResult =
                executePhaseUseCase.execute(
                    phase = phase,
                    context = updatedContext,
                )

            // This time it should complete without interruption
            // (assuming the workflow doesn't have more user interactions)
            println("   ✅ Phase Continued")
            println("      Status: ${continuedPhaseResult.status}")
            println("      Awaiting Input: ${continuedPhaseResult.awaitingInput}")
            println("      Summary: ${continuedPhaseResult.summary.take(100)}...")

            // ==========================================
            // Step 7: MCP Client calls complete_phase
            // ==========================================
            println("\n📍 Step 7: MCP Client → complete_phase")

            val completedResult =
                completePhaseUseCase.execute(
                    executionId = execution.id,
                    phaseResult = continuedPhaseResult,
                )

            assertThat(completedResult).isNotNull
            assertThat(completedResult.status).isEqualTo(ExecutionStatus.PHASE_COMPLETED)

            println("   ✅ Phase Completed")
            println("      Status: ${completedResult.status}")
            println("      Next Phase: ${completedResult.currentPhase().name}")

            // ==========================================
            // Verification: Complete Flow
            // ==========================================
            println("\n" + "=".repeat(80))
            println("✅ E2E TEST PASSED - Complete Flow Verified")
            println("=".repeat(80))
            println("Flow Summary:")
            println("  1. ✅ Process started")
            println("  2. ✅ Phase execution initiated")
            println("  3. ✅ Workflow paused (AWAITING_INPUT)")
            println("  4. ✅ Client detected pause state")
            println("  5. ✅ Answer provided via provide_answer")
            println("  6. ✅ Workflow resumed (RUNNING)")
            println("  7. ✅ Phase completed")
            println("  8. ✅ Interaction history maintained")
            println("=".repeat(80))
        }

    @Test
    fun testMultipleInteractionsInSingleWorkflow() =
        runBlocking {
            println("\n" + "=".repeat(80))
            println("🚀 E2E TEST: Multiple User Interactions")
            println("=".repeat(80))

            // Start process
            val engineeringProcessId = EngineeringProcessId("interactive-feature-dev")
            val projectPath = "./tmp/e2e-multi-interaction"
            val gitBranch = "feature/multi-interaction"

            val execution =
                startProcessUseCase.execute(
                    engineeringProcessId = engineeringProcessId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )

            var context = memoryRepository.load(projectPath, gitBranch)!!
            val phase = execution.currentPhase()

            // First interaction cycle
            println("\n📍 Interaction Cycle 1")
            var phaseResult =
                executePhaseUseCase.execute(
                    phase = phase,
                    context = context,
                )

            if (phaseResult.awaitingInput) {
                println("   ℹ️  Paused for input: ${phaseResult.interactionRequest?.question?.take(50)}...")

                provideAnswerUseCase.execute(
                    executionId = execution.id,
                    answer = "Answer 1: Initial requirements",
                )

                context = memoryRepository.load(projectPath, gitBranch)!!
                println("   ✅ Answer 1 provided")
            }

            // Continue execution - might trigger second interaction
            println("\n📍 Interaction Cycle 2")
            phaseResult =
                executePhaseUseCase.execute(
                    phase = phase,
                    context = context,
                )

            if (phaseResult.awaitingInput) {
                println("   ℹ️  Paused for input: ${phaseResult.interactionRequest?.question?.take(50)}...")

                val resumedExecution =
                    provideAnswerUseCase.execute(
                        executionId = execution.id,
                        answer = "Answer 2: Additional details",
                    )

                assertThat(resumedExecution.interactionHistory).hasSizeGreaterThanOrEqualTo(1)
                println("   ✅ Answer 2 provided")
                println("   ℹ️  Total interactions: ${resumedExecution.interactionHistory.size}")
            }

            println("\n✅ Multiple interactions handled successfully")
        }

    @Test
    fun testErrorHandlingWhenProvidingAnswerToNonPausedWorkflow(): Unit =
        runBlocking<Unit> {
            println("\n" + "=".repeat(80))
            println("🚀 E2E TEST: Error Handling - Invalid State")
            println("=".repeat(80))

            // Start process
            val engineeringProcessId = EngineeringProcessId("interactive-feature-dev")
            val projectPath = "./tmp/e2e-error-test"
            val gitBranch = "feature/error-test"

            val execution =
                startProcessUseCase.execute(
                    engineeringProcessId = engineeringProcessId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )

            // Try to provide answer when NOT in AWAITING_INPUT state
            println("\n📍 Attempting to provide answer without pause")

            val result =
                kotlin.runCatching {
                    provideAnswerUseCase.execute(
                        executionId = execution.id,
                        answer = "This should fail",
                    )
                }

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            assertThat(result.exceptionOrNull()?.message).containsAnyOf("not awaiting input", "No active execution")

            println("   ✅ Error correctly thrown")
            println("      Error: ${result.exceptionOrNull()?.message}")
            println("\n✅ Error handling works correctly")
        }

    private fun setupProcessWithInteraction() {
        val process =
            EngineeringProcess(
                id = EngineeringProcessId("interactive-feature-dev"),
                name = "Interactive Feature Development",
                description = "Feature development with user interactions",
                phases =
                    listOf(
                        ProcessPhase(
                            name = "Interactive Requirements",
                            description = "Requirements gathering with user input",
                            koogWorkflowTemplate = "interactive-test.yml",
                            order = 0,
                            vibeChecks =
                                listOf(
                                    VibeCheck(
                                        question = "Are requirements complete?",
                                        type = VibeCheckType.REQUIREMENTS,
                                        required = true,
                                    ),
                                ),
                        ),
                        ProcessPhase(
                            name = "Design Phase",
                            description = "Architecture design",
                            koogWorkflowTemplate = "interactive-test.yml",
                            order = 1,
                            vibeChecks = emptyList(),
                        ),
                    ),
            )

        processRepository.save(process)
        println("✅ Interactive Feature Development Process setup complete")
    }

    private suspend fun awaitInteraction(executionId: String) {
        withTimeout(5_000) {
            while (!PendingInteractionManager.hasPendingInteraction(executionId)) {
                delay(50)
            }
        }
    }
}
