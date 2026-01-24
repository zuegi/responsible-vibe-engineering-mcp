package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.adapter.output.memory.InMemoryPersistenceRepository
import ch.zuegi.rvmcp.adapter.output.process.InMemoryProcessRepository
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.EngineeringProcessId
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.testEngineeringProcess
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.temporal.ChronoUnit

class StartProcessExecutionServiceTest {
    private lateinit var processRepository: ProcessRepositoryPort
    private lateinit var memoryRepository: MemoryRepositoryPort
    private lateinit var service: StartProcessExecutionService

    @BeforeEach
    fun setUp() {
        processRepository = InMemoryProcessRepository()
        memoryRepository = InMemoryPersistenceRepository()
        service = StartProcessExecutionService(processRepository, memoryRepository)
    }

    @Test
    fun `execute throws when process not found`(): Unit =
        runBlocking {
            // given
            val missingProcessId = EngineeringProcessId.generate()

            // when / then
            val exception =
                assertThrows<IllegalArgumentException> {
                    service.execute(missingProcessId, "./tmp/project", "main")
                }

            assertThat(exception.message).contains("Process not found")
        }

    @Test
    fun `should create new execution context when none exists`(): Unit =
        runBlocking {
            // given
            val process = testEngineeringProcess()
            processRepository.save(process)

            val projectPath = "./tmp/start-process-execution-context"
            val gitBranch = "main"

            // when
            val execution = service.execute(process.id, projectPath, gitBranch)

            // then
            assertThat(execution.status).isEqualTo(ExecutionStatus.IN_PROGRESS)
            assertThat(execution.process).isEqualTo(process)
            assertThat(execution.currentPhaseIndex).isEqualTo(0)
            assertThat(execution.startedAt)
                .isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS))
        }

    @Test
    fun `execute reuses existing context executionId`(): Unit =
        runBlocking {
            // given
            val process = testEngineeringProcess()
            processRepository.save(process)

            val existingExecutionId = ExecutionId.generate()
            val projectPath = "./tmp/repo"
            val gitBranch = "develop"

            val existingContext =
                ExecutionContext(
                    executionId = existingExecutionId,
                    projectPath = projectPath,
                    gitBranch = gitBranch,
                )
            memoryRepository.save(existingContext)

            // when
            val execution = service.execute(process.id, projectPath, gitBranch)

            // then
            assertThat(execution.id).isEqualTo(existingExecutionId)
            assertThat(execution.process).isSameAs(process)

            val persistedContext = memoryRepository.load(projectPath, gitBranch)!!
            assertThat(persistedContext.executionId).isEqualTo(existingExecutionId)
            assertThat(persistedContext.currentExecution).isNotNull
            assertThat(persistedContext.currentExecution!!.id).isEqualTo(existingExecutionId)
        }
}
