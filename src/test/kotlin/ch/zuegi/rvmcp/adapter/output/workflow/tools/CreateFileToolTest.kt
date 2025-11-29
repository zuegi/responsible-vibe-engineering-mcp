package ch.zuegi.rvmcp.adapter.output.workflow.tools

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class CreateFileToolTest {
    @Test
    fun `should create file with given content`() {
        runBlocking {
            val tool = CreateFileTool()
            val testPath = "target/testfile.txt"
            val testContent = "Hello Test!"
            val args =
                CreateFileTool.Args(
                    parameters =
                        CreateFileTool.FileParameters(
                            path = testPath,
                            content = testContent,
                            mimeType = "text/plain",
                        ),
                )

            val result = tool.doExecute(args)
            assertTrue(File(testPath).exists(), "File should exist")
            assertTrue(File(testPath).readText() == testContent, "File content should match")
            println(result)
            // Clean up
            File(testPath).delete()
        }
    }
}
