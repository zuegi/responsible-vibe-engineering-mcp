package ch.zuegi.rvmcp.adapter.output.workflow.tools

// Tool Definition
data class CreateFileTool(
    override val name: String = "create_file",
    override val description: String = "Creates a file in the project directory",
    override val parameters: Map<String, Any> =
        mapOf(
            "type" to "object",
            "properties" to
                mapOf(
                    "path" to
                        mapOf(
                            "type" to "string",
                            "description" to "Relative path from project root (e.g., docs/feature.md)",
                        ),
                    "content" to
                        mapOf(
                            "type" to "string",
                            "description" to "Complete file content",
                        ),
                    "mimeType" to
                        mapOf(
                            "type" to "string",
                            "description" to "MIME type (e.g., text/markdown, application/json)",
                            "default" to "text/plain",
                        ),
                ),
            "required" to listOf("path", "content"),
        ),
) : Tool
