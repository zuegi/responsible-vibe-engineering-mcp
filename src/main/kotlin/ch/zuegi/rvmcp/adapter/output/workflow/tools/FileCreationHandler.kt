package ch.zuegi.rvmcp.adapter.output.workflow.tools

import io.github.oshai.kotlinlogging.KotlinLogging.logger

class FileCreationHandler(
    private val projectPath: Path,
) {
    fun handleCreateFile(args: Map<String, Any>): ToolResult {
        val path =
            args["path"] as? String
                ?: return ToolResult.error("Missing 'path' parameter")

        val content =
            args["content"] as? String
                ?: return ToolResult.error("Missing 'content' parameter")

        val mimeType = args["mimeType"] as? String ?: "text/plain"

        try {
            val targetFile = projectPath.resolve(path)

            // Sicherheit: Verhindern von Path Traversal
            if (!targetFile.normalize().startsWith(projectPath.normalize())) {
                return ToolResult.error("Path traversal detected: $path")
            }

            // Parent Directory erstellen
            targetFile.parent?.createDirectories()

            // File schreiben
            targetFile.writeText(content)

            logger.info("✅ File created: $path (${content.length} bytes)")

            return ToolResult.success(
                result =
                    mapOf(
                        "path" to path,
                        "absolutePath" to targetFile.absolutePathString(),
                        "size" to content.length,
                        "mimeType" to mimeType,
                    ),
                message = "File created successfully at: $path",
            )
        } catch (e: Exception) {
            logger.error("❌ Failed to create file: $path", e)
            return ToolResult.error("Failed to create file: ${e.message}")
        }
    }
}
