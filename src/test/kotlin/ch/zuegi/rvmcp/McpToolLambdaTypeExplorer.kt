package ch.zuegi.rvmcp

import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test

/**
 * Exploration of MCP Tool lambda parameter type.
 *
 * The addTool lambda receives a CallToolRequest parameter.
 * Let's explore its structure.
 */
class McpToolLambdaTypeExplorer {
    @Test
    fun `explore CallToolRequest structure`() {
        val server =
            Server(
                serverInfo =
                    Implementation(
                        name = "test-server",
                        version = "1.0",
                    ),
                options =
                    ServerOptions(
                        capabilities =
                            ServerCapabilities(
                                tools = ServerCapabilities.Tools(listChanged = true),
                            ),
                    ),
            )

        // The lambda parameter type is CallToolRequest
        server.addTool(
            name = "test_tool",
            description = "Test tool",
        ) { request: CallToolRequest ->

            // CallToolRequest should have:
            // - name: String (tool name)
            // - arguments: Map<String, JsonElement>? (tool arguments)

            println("Tool name: ${request.name}")
            println("Arguments: ${request.arguments}")

            // Try to access arguments
            val arguments = request.arguments
            if (arguments != null) {
                println("Arguments is not null")
                arguments.forEach { (key, value) ->
                    println("  $key: $value")

                    // Try to extract as primitive
                    if (value is JsonElement) {
                        try {
                            val stringValue = value.jsonPrimitive.content
                            println("    -> String value: $stringValue")
                        } catch (e: Exception) {
                            println("    -> Not a primitive: ${e.message}")
                        }
                    }
                }
            } else {
                println("Arguments is null")
            }

            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text = "Explored request structure",
                        ),
                    ),
            )
        }

        println("✅ Tool registered with explicit CallToolRequest type")
    }
}
