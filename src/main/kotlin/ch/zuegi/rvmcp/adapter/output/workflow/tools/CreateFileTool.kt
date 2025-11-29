package ch.zuegi.rvmcp.adapter.output.workflow.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import java.io.File

class CreateFileTool(
    private val basePathProvider: () -> String,
) : SimpleTool<CreateFileTool.Args>() {
    @Serializable
    data class Args(
        val path: String,
        val content: String,
        val mimeType: String = "text/plain",
    )

    override val name: String = "create_file"

    override val description: String =
        """
        Creates or overwrites a file relative to the project root.
        """.trimIndent()

    override val argsSerializer = serializer<Args>()

    override val descriptor: ToolDescriptor =
        ToolDescriptor(
            name = name,
            description = description,
            requiredParameters =
                listOf(
                    ToolParameterDescriptor("path", "Path relative to project root", ToolParameterType.String),
                    ToolParameterDescriptor("content", "Content of the file", ToolParameterType.String),
                    ToolParameterDescriptor("mimeType", "Optional MIME type", ToolParameterType.String),
                ),
        )

    override suspend fun doExecute(args: Args): String =
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
