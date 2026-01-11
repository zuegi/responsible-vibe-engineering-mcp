package ch.zuegi.rvmcp.adapter.output.memory

import ch.zuegi.rvmcp.domain.model.context.ExecutionContext
import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@Disabled
@SpringBootTest
class InMemoryPersistencePortIntegrationTest {
    @Autowired
    private lateinit var repository: MemoryRepositoryPort

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should work`(): Unit =
        runBlocking {
            // Given
            val context =
                ExecutionContext(
                    executionId = ExecutionId.generate(),
                    projectPath = tempDir.toString(),
                    gitBranch = "main",
                )

            // When
            repository.save(context)
            // Then

            diesen test weiterführen, bleibt aber noch hängen....
        }
}
