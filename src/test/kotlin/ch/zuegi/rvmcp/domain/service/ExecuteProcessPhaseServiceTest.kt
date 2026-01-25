package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.AutoPassVibeCheckEvaluator
import ch.zuegi.rvmcp.FailingVibeCheckEvaluator
import ch.zuegi.rvmcp.TestWorkflowExecutor
import ch.zuegi.rvmcp.adapter.output.memory.InMemoryPersistenceRepository
import ch.zuegi.rvmcp.createExecutionContext
import ch.zuegi.rvmcp.createFakeWorkflowExecutionResult
import ch.zuegi.rvmcp.createFakeWorkflowSummary
import ch.zuegi.rvmcp.createProcessPhase
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.output.DocumentPersistencePort
import ch.zuegi.rvmcp.domain.port.output.VibeCheckEvaluatorPort
import ch.zuegi.rvmcp.domain.port.output.WorkflowExecutionPort
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowExecutionResult
import ch.zuegi.rvmcp.domain.port.output.model.WorkflowSummary
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExecuteProcessPhaseServiceTest {
    private lateinit var workflowExecutor: WorkflowExecutionPort
    private lateinit var vibeCheckEvaluator: VibeCheckEvaluatorPort
    private lateinit var documentGenerationService: DocumentGenerationService
    private lateinit var inMemoryProcessRepository: DocumentPersistencePort
    private lateinit var executeProcessPhaseService: ExecuteProcessPhaseService

    @BeforeEach
    fun setUp() {
        val fakeResult: WorkflowExecutionResult = createFakeWorkflowExecutionResult()
        val fakeSummary: WorkflowSummary = createFakeWorkflowSummary()
        workflowExecutor = TestWorkflowExecutor(fakeResult, fakeSummary)
        vibeCheckEvaluator = AutoPassVibeCheckEvaluator()
        inMemoryProcessRepository = InMemoryPersistenceRepository()
        documentGenerationService = DocumentGenerationService(inMemoryProcessRepository)
        executeProcessPhaseService = ExecuteProcessPhaseService(workflowExecutor, vibeCheckEvaluator, documentGenerationService)
    }

    @Test
    fun `should execute workflow and create phase result on success`(): Unit =
        runBlocking {
            // given
            val phase1 = createProcessPhase("Phase 1", 1)
            val executionContext = createExecutionContext()

            // when
            val phaseResult = executeProcessPhaseService.execute(phase1, executionContext)

            // then
            assertThat(phaseResult).isNotNull
            assertThat(phaseResult.status).isEqualTo(ExecutionStatus.PHASE_COMPLETED)
            assertThat(phaseResult.vibeCheckResults).hasSize(1)
        }

    @Test
    fun `should evaluate vibe checks after workflow execution`(): Unit =
        runBlocking {
            // given
            val phase = createProcessPhase("Phase A", 1)
            val ctx = createExecutionContext()

            // when
            val result = executeProcessPhaseService.execute(phase, ctx)

            // then
            // TestWorkflowExecutor zeichnet Aufrufe auf -> prüfen, dass Workflow ausgeführt wurde
            val exec = workflowExecutor as TestWorkflowExecutor
            assertThat(exec.calls).hasSize(1)
            val (template, calledCtx) = exec.calls.first()
            assertThat(calledCtx.executionId).isEqualTo(ctx.executionId)
            assertThat(template).isEqualTo(phase.koogWorkflowTemplate)

            // und die Vibe-Checks wurden ausgewertet (AutoPass gibt eine Antwort zurück)
            assertThat(result.vibeCheckResults).isNotEmpty
        }

    @Test
    fun `should return failed phase result when required vibe checks fail`(): Unit =
        runBlocking {
            // given: ein Evaluator, der alle Checks schlägt (failed) zurückgibt
            val failingEvaluator = FailingVibeCheckEvaluator()

            // neuer Service mit dem failenden Evaluator
            val fakeResult: WorkflowExecutionResult = createFakeWorkflowExecutionResult()
            val fakeSummary: WorkflowSummary = createFakeWorkflowSummary()
            val failingExecutor = TestWorkflowExecutor(fakeResult, fakeSummary)
            val svc = ExecuteProcessPhaseService(failingExecutor, failingEvaluator, documentGenerationService)

            val phaseWithRequired = createProcessPhase("Phase Required", 1) // helper erzeugt normalerweise required checks
            val ctx = createExecutionContext()

            // when
            val phaseResult = svc.execute(phaseWithRequired, ctx)

            // then
            assertThat(phaseResult.status).isEqualTo(ExecutionStatus.FAILED)
            assertThat(phaseResult.vibeCheckResults).allMatch { !it.passed }
        }

    @Test
    fun `should return paused phase result when workflow awaits user input`(): Unit =
        runBlocking {
            // given
            // Erzeuge ein WorkflowExecutionResult, das awaitingInput = true liefert.
            // createFakeWorkflowExecutionResult unterstützt erwartungsgemäß Konfiguration in den Test-Utilities.
            val awaitingResult: WorkflowExecutionResult = createFakeWorkflowExecutionResult(awaitingInput = true)
            val fakeSummary: WorkflowSummary = createFakeWorkflowSummary()
            val awaitingExecutor = TestWorkflowExecutor(awaitingResult, fakeSummary)

            val executeProcessPhaseService = ExecuteProcessPhaseService(awaitingExecutor, vibeCheckEvaluator, documentGenerationService)

            val phase = createProcessPhase("Phase Pause", 1)
            val ctx = createExecutionContext()

            // when
            val phaseResult = executeProcessPhaseService.execute(phase, ctx)

            // then
            assertThat(phaseResult.awaitingInput).isTrue
            assertThat(phaseResult.status).isEqualTo(ExecutionStatus.IN_PROGRESS)
            assertThat(phaseResult.completedAt).isNull()
        }

    @Test
    fun `should generate documentation after successful phase completion`(): Unit =
        runBlocking {
            // given
            val fakeResult: WorkflowExecutionResult = createFakeWorkflowExecutionResult()
            val fakeSummary: WorkflowSummary = createFakeWorkflowSummary()
            val exec = TestWorkflowExecutor(fakeResult, fakeSummary)

            val phase = createProcessPhase("Phase Doc", 1)
            val ctx = createExecutionContext()

            // when
            val phaseResult = executeProcessPhaseService.execute(phase, ctx)

            // find the generatedDocument
            val generatedDocument = documentGenerationService.findById(phaseResult.generatedDocumentIds.first()!!)

            // then
            assertThat(phaseResult.status).isEqualTo(ExecutionStatus.PHASE_COMPLETED)
            assertThat(generatedDocument).isNotNull
            assertThat(generatedDocument!!.content).contains(fakeResult.summary)
        }

    @Test
    fun `should handle workflow execution failures`(): Unit =
        runBlocking {
            // given: Executor, der eine Exception wirft
            val throwingExecutor =
                object : WorkflowExecutionPort {
                    override suspend fun executeWorkflow(
                        template: String,
                        context: ExecutionContext,
                    ): WorkflowExecutionResult = throw RuntimeException("simulated-execution-failure")

                    override suspend fun getSummary(): WorkflowSummary = createFakeWorkflowSummary()
                }

            val svc = ExecuteProcessPhaseService(throwingExecutor, vibeCheckEvaluator, documentGenerationService)
            val phase = createProcessPhase("Phase Error", 1)
            val ctx = createExecutionContext()

            // Act / Assert: Exception wird weitergereicht
            assertThrows<RuntimeException> {
                svc.execute(phase, ctx)
            }
        }
}
