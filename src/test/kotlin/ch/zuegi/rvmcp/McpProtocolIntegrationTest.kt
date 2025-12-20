package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.memory.InMemoryMemoryRepository
import ch.zuegi.rvmcp.adapter.output.process.InMemoryProcessRepository
import ch.zuegi.rvmcp.adapter.output.workflow.KoogWorkflowExecutor
import ch.zuegi.rvmcp.application.usecase.CompletePhaseUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.ExecuteProcessPhaseUseCaseImpl
import ch.zuegi.rvmcp.application.usecase.StartProcessExecutionUseCaseImpl
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.phase.ProcessPhase
import ch.zuegi.rvmcp.domain.model.process.EngineeringProcess
import ch.zuegi.rvmcp.domain.model.status.VibeCheckType
import ch.zuegi.rvmcp.domain.model.vibe.VibeCheck
import ch.zuegi.rvmcp.domain.service.CompletePhaseService
import ch.zuegi.rvmcp.domain.service.ExecuteProcessPhaseService
import ch.zuegi.rvmcp.domain.service.StartProcessExecutionService
import ch.zuegi.rvmcp.infrastructure.config.LlmProperties
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * MCP Protocol Integration Tests.
 *
 * Tests MCP tool functionality via use cases:
 * 1. list_processes - Repository integration
 * 2. start_process - Process execution
 * 3. get_context - Memory retrieval
 * 4. execute_phase - Workflow execution
 * 5. complete_phase - Phase completion
 * 6. Error handling
 *
 * Architecture:
 * - Tests use case layer (MCP tools call these)
 * - Real Domain Services
 * - In-Memory persistence (no external dependencies)
 * - No actual MCP protocol (tested separately)
 */
@SpringBootTest
@ActiveProfiles("local")
class McpProtocolIntegrationTest {
    @Autowired
    private lateinit var llmProperties: LlmProperties

    private lateinit var processRepository: InMemoryProcessRepository
    private lateinit var memoryRepository: InMemoryMemoryRepository
    private lateinit var workflowExecutor: KoogWorkflowExecutor
    private lateinit var vibeCheckEvaluator: AutoPassVibeCheckEvaluator

    private lateinit var startProcessUseCase: StartProcessExecutionUseCaseImpl
    private lateinit var executePhaseUseCase: ExecuteProcessPhaseUseCaseImpl
    private lateinit var completePhaseUseCase: CompletePhaseUseCaseImpl

    @BeforeEach
    fun setup() {
        println("\n" + "=".repeat(80))
        println("🧪 MCP PROTOCOL INTEGRATION TEST - Setup")
        println("=".repeat(80))

        // Initialize repositories
        processRepository = InMemoryProcessRepository()
        memoryRepository = InMemoryMemoryRepository()

        // Initialize workflow executor with mock ask_user tool
        workflowExecutor =
            KoogWorkflowExecutor(
                llmProperties = llmProperties,
                askUserTool = MockAskUserTool(),
            )

        // Use automatic vibe check evaluator for testing
        vibeCheckEvaluator = AutoPassVibeCheckEvaluator()

        // Initialize domain services
        val startProcessService =
            StartProcessExecutionService(
                processRepository,
                memoryRepository,
            )

        val executePhaseService =
            ExecuteProcessPhaseService(
                workflowExecutor = workflowExecutor,
                vibeCheckEvaluator = vibeCheckEvaluator,
            )

        val completePhaseService =
            CompletePhaseService(
                memoryRepository,
            )

        // Initialize use cases
        startProcessUseCase = StartProcessExecutionUseCaseImpl(startProcessService)
        executePhaseUseCase = ExecuteProcessPhaseUseCaseImpl(executePhaseService)
        completePhaseUseCase =
            CompletePhaseUseCaseImpl(
                completePhaseService,
                memoryRepository,
                processRepository,
            )

        // Setup test process
        setupFeatureDevelopmentProcess()

        println("✅ Setup complete")
    }

    @Test
    fun `should call list_processes tool and get response`() =
        runBlocking {
            println("\n🔧 TEST: Call list_processes Tool")

            // Setup MCP session (simplified - direct tool call simulation)
            val processes = processRepository.findAll()

            assertThat(processes).isNotEmpty
            assertThat(processes).anyMatch { it.id.value == "feature-development" }

            println("   Found ${processes.size} processes:")
            processes.forEach { process ->
                println("   - ${process.id.value}: ${process.name}")
            }

            println("✅ list_processes tool works correctly")
        }

    @Test
    fun `should call start_process tool and create execution`() =
        runBlocking {
            println("\n🚀 TEST: Call start_process Tool")

            // Simulate MCP tool call via use case
            val processId = ProcessId("feature-development")
            val projectPath = "/tmp/mcp-test-project"
            val gitBranch = "feature/mcp-integration"

            val execution =
                startProcessUseCase.execute(
                    processId = processId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )

            assertThat(execution).isNotNull
            assertThat(execution.id.value).isNotEmpty()
            assertThat(execution.process.id).isEqualTo(processId)

            println("   Execution ID: ${execution.id.value}")
            println("   Process: ${execution.process.name}")
            println("   Current Phase: ${execution.currentPhase().name}")

            println("✅ start_process tool works correctly")
        }

    @Test
    fun `should call get_context tool and retrieve execution context`() =
        runBlocking {
            println("\n📂 TEST: Call get_context Tool")

            // First start a process to create context
            val processId = ProcessId("feature-development")
            val projectPath = "/tmp/mcp-context-test"
            val gitBranch = "feature/test-context"

            startProcessUseCase.execute(
                processId = processId,
                projectPath = projectPath,
                gitBranch = gitBranch,
            )

            // Simulate get_context tool call
            val context = memoryRepository.load(projectPath, gitBranch)

            assertThat(context).isNotNull
            assertThat(context!!.projectPath).isEqualTo(projectPath)
            assertThat(context.gitBranch).isEqualTo(gitBranch)

            println("   Project: ${context.projectPath}")
            println("   Branch: ${context.gitBranch}")
            println("   Execution ID: ${context.executionId.value}")

            println("✅ get_context tool works correctly")
        }

    @Test
    fun `should call execute_phase tool and run workflow`() =
        runBlocking {
            println("\n⚙️  TEST: Call execute_phase Tool")

            // Setup: Start process
            val processId = ProcessId("feature-development")
            val projectPath = "/tmp/mcp-phase-test"
            val gitBranch = "feature/test-phase"

            val execution =
                startProcessUseCase.execute(
                    processId = processId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )

            val context = memoryRepository.load(projectPath, gitBranch)!!
            val phase = execution.currentPhase()

            // Simulate execute_phase tool call via use case
            val phaseResult =
                executePhaseUseCase.execute(
                    phase = phase,
                    context = context,
                )

            assertThat(phaseResult).isNotNull
            assertThat(phaseResult.phaseName).isEqualTo("Requirements Analysis")
            assertThat(phaseResult.summary).isNotEmpty

            println("   Phase: ${phaseResult.phaseName}")
            println("   Status: ${phaseResult.status}")
            val durationMs =
                if (phaseResult.completedAt != null) {
                    phaseResult.completedAt.toEpochMilli() - phaseResult.startedAt.toEpochMilli()
                } else {
                    0
                }
            println("   Duration: ${durationMs}ms")
            println("   Summary: ${phaseResult.summary.take(100)}...")

            println("✅ execute_phase tool works correctly")
        }

    @Test
    fun `should call complete_phase tool and advance to next phase`() =
        runBlocking {
            println("\n📝 TEST: Call complete_phase Tool")

            // Setup: Start process and execute first phase
            val processId = ProcessId("feature-development")
            val projectPath = "/tmp/mcp-complete-test"
            val gitBranch = "feature/test-complete"

            val execution =
                startProcessUseCase.execute(
                    processId = processId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )

            val context = memoryRepository.load(projectPath, gitBranch)!!
            val phase = execution.currentPhase()

            val phaseResult =
                executePhaseUseCase.execute(
                    phase = phase,
                    context = context,
                )

            // Simulate complete_phase tool call via use case
            val result =
                completePhaseUseCase.execute(
                    executionId = execution.id,
                    phaseResult = phaseResult,
                )

            assertThat(result).isNotNull
            assertThat(result.status).isNotNull
            assertThat(result.currentPhaseIndex).isEqualTo(1)

            println("   Process Status: ${result.status}")
            println("   Next Phase: ${result.currentPhase().name}")
            println("   Phase Index: ${result.currentPhaseIndex}")

            println("✅ complete_phase tool works correctly")
        }

    @Test
    fun `should handle error when process not found`() =
        runBlocking {
            println("\n❌ TEST: Error Handling - Process Not Found")

            // Try to start non-existent process
            val result =
                kotlin.runCatching {
                    startProcessUseCase.execute(
                        processId = ProcessId("non-existent-process"),
                        projectPath = "/tmp/error-test",
                        gitBranch = "feature/error",
                    )
                }

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
            assertThat(result.exceptionOrNull()?.message).contains("non-existent-process")

            println("   Error: ${result.exceptionOrNull()?.message}")
            println("✅ Error handling works correctly")
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
                            koogWorkflowTemplate = "simple-test.yml",
                            order = 0,
                            vibeChecks =
                                listOf(
                                    VibeCheck(
                                        question = "Are requirements clear?",
                                        type = VibeCheckType.REQUIREMENTS,
                                        required = true,
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
                                        question = "Is architecture sound?",
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
                                        question = "Is code quality good?",
                                        type = VibeCheckType.QUALITY,
                                        required = true,
                                    ),
                                ),
                        ),
                    ),
            )

        processRepository.save(process)
        println("✅ Feature Development Process setup complete")
    }
}
