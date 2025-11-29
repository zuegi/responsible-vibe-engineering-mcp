package ch.zuegi.rvmcp.adapter.output.workflow.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Tool for creating files in the project directory.
 *
 * This tool receives a path, content, and MIME type, and creates a file accordingly.
 *
 * Usage:
 * Use this tool to automatically generate or update files in the project directory.
 *
 * Example:
 * val args = CreateFileTool.Args(
 *     parameters = CreateFileTool.FileParameters(
 *         path = "example.txt",
 *         content = "Hello, World!",
 *         mimeType = "text/plain"
 *     )
 * )
 * createFileTool.doExecute(args)
 */
class CreateFileTool : SimpleTool<CreateFileTool.Args>() {
    /**
     * Parameters for file creation.
     *
     * @property path The file path where the new file will be created.
     * @property content The content to write into the file.
     * @property mimeType The MIME type of the file (default: text/plain).
     */
    @Serializable
    data class FileParameters(
        val path: String,
        val content: String,
        val mimeType: String = "text/plain",
    )

    /**
     * Arguments for the tool.
     *
     * @property name Name of the tool (default: create_file)
     * @property description Description of the function
     * @property parameters Parameters for file creation
     */
    @Serializable
    data class Args(
        val name: String = "create_file",
        val description: String = "Creates a file in the project directory",
        val parameters: FileParameters,
    )

    override val name: String = "create_file"
    override val argsSerializer = serializer<Args>()
    override val description: String =
        """
        Creates a file in the project directory.
        Provide the file parameters as a nested object under the 'parameters' key, including 'path', 'content', and optionally 'mimeType'.
        Example input:
        {
          "parameters": {
            "path": "docs/requirements.md",
            "content": "Your file content here",
            "mimeType": "text/markdown"
          }
        }
        This tool will write the specified content to the given path and set the MIME type if provided.
        """.trimIndent()

    override val descriptor =
        ToolDescriptor(
            name = name,
            description = description,
            requiredParameters =
                listOf(
                    ToolParameterDescriptor(
                        name = "path",
                        description = "The file path where the new file will be created.",
                        type = ToolParameterType.String,
                    ),
                    ToolParameterDescriptor(
                        name = "content",
                        description = "The content to write into the file.",
                        type = ToolParameterType.String,
                    ),
                    ToolParameterDescriptor(
                        name = "mimeType",
                        description = "The MIME type of the file. Default is 'text/plain'.",
                        type = ToolParameterType.String,
                    ),
                ),
        )

    override suspend fun doExecute(args: Args): String {
        val params = args.parameters
        return try {
            val file = java.io.File(params.path)
            file.writeText(params.content)
            "File '${params.path}' created successfully with MIME type '${params.mimeType}'."
        } catch (e: Exception) {
            "Error creating file '${params.path}': ${e.message}"
        }
    }
}
