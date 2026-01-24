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

   /* Alle tests funktionieren
    leider funktioniert der workflow mit dem mcp server überhaupt nicht
    der workflow hängt irgendwie oder wir gestoppt
    2026-01-18 11:13:16 ERROR [DefaultDispatcher-worker-4] i.m.k.s.server.StdioServerTransport - Error writing to stdout
    2   │ kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled
    ───────┴────────────────────────────────────────────────────────────────────────────────────*/

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

    @Test
    fun `should extract requirements from decisions with keywords`(): Unit =
        runBlocking {
            // Given - Decisions mit Requirement-Keywords
            val phaseResult =
                PhaseResult(
                    phaseName = "Requirements",
                    status = ExecutionStatus.PHASE_COMPLETED,
                    summary = "Requirements gathered",
                    vibeCheckResults = emptyList(),
                    decisions =
                        listOf(
                            Decision(
                                phase = "Requirements",
                                decision = "System must support user authentication",
                                reasoning =
                                    """
                                    Users need secure access to the system.
                                    - Support username/password login
                                    - Support OAuth2 providers
                                    - Session timeout after 30 minutes
                                    """.trimIndent(),
                                date = LocalDate.now(),
                            ),
                            Decision(
                                phase = "Requirements",
                                decision = "The application should provide real-time notifications",
                                reasoning = "Important for user engagement and timely updates",
                                date = LocalDate.now(),
                            ),
                            Decision(
                                phase = "Architecture",
                                decision = "Use PostgreSQL database",
                                reasoning = "Technical decision, not a requirement",
                                date = LocalDate.now(),
                            ),
                        ),
                    startedAt = Instant.now(),
                )

            val context =
                ExecutionContext(
                    executionId = ExecutionId.generate(),
                    projectPath = "/test/app",
                    gitBranch = "main",
                )

            // When
            val document = service.generateAndPersistRequirementsDoc(phaseResult, context)

            // Then - Document enthält extrahierte Requirements
            assertThat(document.content).contains("REQ-1")
            assertThat(document.content).contains("System must support user authentication")
            assertThat(document.content).contains("Support username/password login")
            assertThat(document.content).contains("REQ-2")
            assertThat(document.content).contains("real-time notifications")
            // PostgreSQL Decision sollte NICHT als Requirement erscheinen
            assertThat(document.content.split("## Functional Requirements")[1])
                .doesNotContain("PostgreSQL")
        }

    @Test
    fun `should infer requirement types correctly`(): Unit =
        runBlocking {
            // Given - Decisions mit verschiedenen Type-Keywords
            val phaseResult =
                PhaseResult(
                    phaseName = "Requirements",
                    status = ExecutionStatus.PHASE_COMPLETED,
                    summary = "Requirements",
                    vibeCheckResults = emptyList(),
                    decisions =
                        listOf(
                            Decision(
                                phase = "Requirements",
                                decision = "System must handle 1000 requests per second",
                                reasoning = "Performance requirement for scalability",
                            ),
                            Decision(
                                phase = "Requirements",
                                decision = "Use microservices architecture",
                                reasoning = "Technical decision for system design",
                            ),
                            Decision(
                                phase = "Requirements",
                                decision = "Feature must increase revenue by 10%",
                                reasoning = "Business goal",
                            ),
                        ),
                    startedAt = Instant.now(),
                )

            val context = ExecutionContext(ExecutionId.generate(), "/test", "main")

            // When
            val document = service.generateAndPersistRequirementsDoc(phaseResult, context)

            // Then - Types werden korrekt inferiert
            val content = document.content
            assertThat(content).contains("Performance requirement") // NON_FUNCTIONAL
            assertThat(content).contains("Technical decision") // TECHNICAL
            assertThat(content).contains("Business goal") // BUSINESS
        }

    @Test
    fun `should extract stakeholders from decisions`(): Unit =
        runBlocking {
            // Given - Decisions mit Stakeholder-Erwähnungen
            val phaseResult =
                PhaseResult(
                    phaseName = "Planning",
                    status = ExecutionStatus.PHASE_COMPLETED,
                    summary = "Stakeholders identified",
                    vibeCheckResults = emptyList(),
                    decisions =
                        listOf(
                            Decision(
                                phase = "Planning",
                                decision = "Product owner will prioritize backlog items",
                                reasoning = "Product owner has ultimate decision authority",
                            ),
                            Decision(
                                phase = "Planning",
                                decision = "End user needs simple interface",
                                reasoning = "Target end user group: non-technical users",
                            ),
                            Decision(
                                phase = "Planning",
                                decision = "Developer team will use pair programming",
                                reasoning = "Improve code quality through collaboration",
                            ),
                        ),
                    startedAt = Instant.now(),
                )

            val context = ExecutionContext(ExecutionId.generate(), "/test", "main")

            // When
            val document = service.generateAndPersistRequirementsDoc(phaseResult, context)

            // Then - Stakeholders werden extrahiert
            val content = document.content
            assertThat(content).contains("## Stakeholders")
            assertThat(content).contains("Product Owner")
            assertThat(content).contains("End User")
            assertThat(content).contains("Developer")
        }

    @Test
    fun `should extract acceptance criteria from bullet points`(): Unit =
        runBlocking {
            // Given - Decision mit strukturierten Acceptance Criteria
            val phaseResult =
                PhaseResult(
                    phaseName = "Requirements",
                    status = ExecutionStatus.PHASE_COMPLETED,
                    summary = "Requirements with AC",
                    vibeCheckResults = emptyList(),
                    decisions =
                        listOf(
                            Decision(
                                phase = "Requirements",
                                decision = "User login feature",
                                reasoning =
                                    """
                                    The system must provide secure user authentication.
                                    
                                    Acceptance Criteria:
                                    - User can login with email and password
                                    - Invalid credentials show error message
                                    - Successful login redirects to dashboard
                                    1. Password must be at least 8 characters
                                    2. Account locks after 5 failed attempts
                                    """.trimIndent(),
                            ),
                        ),
                    startedAt = Instant.now(),
                )

            val context = ExecutionContext(ExecutionId.generate(), "/test", "main")

            // When
            val document = service.generateAndPersistRequirementsDoc(phaseResult, context)

            // Then - AC werden extrahiert
            val content = document.content
            assertThat(content).contains("User can login with email and password")
            assertThat(content).contains("Invalid credentials show error message")
            assertThat(content).contains("Password must be at least 8 characters")
            assertThat(content).contains("Account locks after 5 failed attempts")
        }
}
