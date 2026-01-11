package ch.zuegi.rvmcp.domain.service

import ch.zuegi.rvmcp.adapter.output.memory.InMemoryPersistencePort
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.document.DocumentType
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.id.ProcessId
import ch.zuegi.rvmcp.domain.model.memory.Decision
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import ch.zuegi.rvmcp.domain.model.status.ExecutionStatus
import ch.zuegi.rvmcp.domain.port.output.DocumentPersistencePort
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class DocumentGenerationServiceTest {
    private lateinit var service: DocumentGenerationService
    private lateinit var documentPersistencePort: DocumentPersistencePort

    @BeforeEach
    fun setup() {
        documentPersistencePort = InMemoryPersistencePort()
        service = DocumentGenerationService(documentPersistencePort)
    }

    @Test
    fun `should generate requirements document from phase result`(): Unit =
        runBlocking {
            // Given
            val phaseResult =
                PhaseResult(
                    phaseName = "Requirements Analysis",
                    status = ExecutionStatus.PHASE_COMPLETED,
                    summary = "Gathered all functional requirements for authentication",
                    vibeCheckResults = emptyList(),
                    decisions =
                        listOf(
                            Decision(
                                phase = "Requirements Analysis",
                                decision = "Use JWT tokens",
                                reasoning = "Stateless and scalable",
                                date = LocalDate.now(),
                            ),
                        ),
                    startedAt = Instant.now().minusSeconds(3600),
                    completedAt = Instant.now(),
                )

            val context =
                ExecutionContext(
                    executionId = ExecutionId.generate(),
                    projectPath = "/Users/test/my-app",
                    gitBranch = "feature/auth",
                    processId = ProcessId("feature-development"),
                    architecturalDecisions =
                        listOf(
                            Decision("Architecture", "Hexagonal Architecture", "Clean separation", LocalDate.now()),
                        ),
                )

            // When
            val document = service.generateAndPersistRequirementsDoc(phaseResult, context)

            // Then
            assertThat(document).isNotNull
            assertThat(document.filename).isEqualTo("docs/requirements.md")
            assertThat(document.type).isEqualTo(DocumentType.REQUIREMENTS)
            assertThat(document.content).isNotBlank()
            assertThat(document.content).contains("# Requirements: my-app")
            assertThat(document.content).contains("Gathered all functional requirements")
            assertThat(document.metadata.phaseName).isEqualTo("Requirements Analysis")
            assertThat(document.metadata.version).isEqualTo("1.0")
        }

    @Test
    fun `should include all context decisions in generated document`(): Unit =
        runBlocking {
            // Given
            val context =
                ExecutionContext(
                    executionId = ExecutionId.generate(),
                    projectPath = "/test/project",
                    gitBranch = "main",
                    architecturalDecisions =
                        listOf(
                            Decision("Phase1", "Decision 1", "Reason 1", LocalDate.now()),
                            Decision("Phase2", "Decision 2", "Reason 2", LocalDate.now()),
                        ),
                )

            val phaseResult =
                PhaseResult(
                    phaseName = "Test Phase",
                    status = ExecutionStatus.PHASE_COMPLETED,
                    summary = "Test summary",
                    vibeCheckResults = emptyList(),
                    startedAt = Instant.now(),
                )

            // When
            val document = service.generateAndPersistRequirementsDoc(phaseResult, context)

            // Then
            assertThat(document.content).contains("Decision 1")
            assertThat(document.content).contains("Decision 2")
        }
}
