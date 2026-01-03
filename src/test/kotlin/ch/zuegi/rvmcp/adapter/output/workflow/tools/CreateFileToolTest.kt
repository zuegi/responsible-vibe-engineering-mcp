package ch.zuegi.rvmcp.adapter.output.workflow.tools

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class CreateFileToolTest {
    @Test
    fun `should create file with given content`() {
        runBlocking {
            val basePath = "target/test-project"
            val testRelativePath = "files/testfile.txt"
            val testContent = "Hello Test!"

            // Clean up old file
            val expectedFile = File(basePath, testRelativePath)
            if (expectedFile.exists()) expectedFile.delete()

            val tool = CreateFileTool { basePath }

            val args =
                CreateFileTool.Args(
                    path = testRelativePath,
                    content = testContent,
                    mimeType = "text/plain",
                )

            val result = tool.execute(args)

            // Assertions
            assertTrue(expectedFile.exists(), "Expected file to be created")
            assertEquals(testContent, expectedFile.readText(), "File content should match")
            assertTrue(result.contains("created successfully"), "Result should indicate success")
            println(result)
            // Cleanup
            expectedFile.delete()
        }
    }
}
