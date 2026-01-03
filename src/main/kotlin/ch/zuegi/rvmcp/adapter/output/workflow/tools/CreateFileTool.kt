package ch.zuegi.rvmcp.adapter.output.workflow.tools

import ai.koog.agents.core.tools.SimpleTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import java.io.File

class CreateFileTool(
    private val basePathProvider: () -> String,
) : SimpleTool<CreateFileTool.Args>(
        argsSerializer = serializer<Args>(),
        name = "create_file",
        description = "Creates or overwrites a file relative to the project root.",
    ) {
    @Serializable
    data class Args(
        val path: String,
        val content: String,
        val mimeType: String = "text/plain",
    )

    override suspend fun execute(args: Args): String =
        try {
            val basePath = basePathProvider()
            val file = File(basePath, args.path)

            file.parentFile?.mkdirs()
            file.writeText(args.content)

            "File '${file.absolutePath}' created successfully."
        } catch (e: Exception) {
            "Error creating file '${args.path}': ${e.message}"
        }
}
