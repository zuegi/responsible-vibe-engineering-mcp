package ch.zuegi.rvmcp.adapter.output.memory

import ch.zuegi.rvmcp.createExecutionExtension
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.nio.file.Path

@SpringBootTest
@ActiveProfiles("test")
// FIXME in einem Prototypen vielleicht noch akzeptabel
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InMemoryPersistencePortIntegrationTest {
    @Autowired
    private lateinit var repository: MemoryRepositoryPort

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should save an execution context and load`(): Unit =
        runBlocking {
            // Given
            val context = createExecutionExtension(projectPath = tempDir.toString())
            // When
            repository.save(context)

            // Then
            val loadedContext = repository.load(context.projectPath, context.gitBranch)
            assertThat(loadedContext).isNotNull.isEqualTo(context)
        }

    @Test
    fun `should delete an execution context and find by executionId`(): Unit =
        runBlocking {
            // Given
            val context = createExecutionExtension(projectPath = tempDir.toString())
            // When
            repository.save(context)

            // Then
            val loadedContext = repository.findByExecutionId(context.executionId)
            assertThat(loadedContext).isNotNull.isEqualTo(context)
        }

    @Test
    fun `should save multiple execution contexts and delete one`(): Unit =
        runBlocking {
            // Given
            val context = createExecutionExtension(projectPath = tempDir.toString(), gitBranch = "main")
            val context1 = createExecutionExtension(projectPath = tempDir.toString(), gitBranch = "main1")
            val context2 = createExecutionExtension(projectPath = tempDir.toString(), gitBranch = "main2")
            // When
            repository.save(context)
            repository.save(context1)
            repository.save(context2)

            assertThat(repository.load(context.projectPath, context.gitBranch)).isEqualTo(context)
            assertThat(repository.load(context1.projectPath, context1.gitBranch)).isEqualTo(context1)
            assertThat(repository.load(context2.projectPath, context2.gitBranch)).isEqualTo(context2)

            // then
            repository.delete(context1.executionId)

            assertThat(repository.load(context1.projectPath, context1.gitBranch)).isNull()

            assertThat(repository.load(context2.projectPath, context2.gitBranch)).isNotNull
            assertThat(repository.load(context.projectPath, context.gitBranch)).isNotNull
        }

    @Test
    fun `should throw exception when MAX_CONTEXTS limit of 100 is exceeded`(): Unit =
        runBlocking {
            // Given: Fill repository with 100 contexts
            repeat(100) { index ->
                val context =
                    createExecutionExtension(
                        projectPath = "$tempDir/project-$index",
                        gitBranch = "main",
                    )
                repository.save(context)
            }

            // When/Then: Adding 101st context should throw IllegalStateException
            val context101 =
                createExecutionExtension(
                    projectPath = "$tempDir/project-101",
                    gitBranch = "main",
                )

            val exception =
                org.junit.jupiter.api.assertThrows<IllegalStateException> {
                    runBlocking {
                        repository.save(context101)
                    }
                }

            // Verify exception message
            assertThat(exception.message)
                .contains("In-Memory storage limit of 100 contexts exceeded")
                .contains("Configure a persistent backend")
        }
}
