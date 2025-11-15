package ch.zuegi.rvmcp

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties

/**
 * Explorer to understand MCP Tool Request parameter structure.
 *
 * This test explores what properties and methods are available on the
 * `request` parameter passed to tool handlers.
 */
class McpToolRequestExplorer {
    @Test
    fun `explore tool request parameter type and properties`() {
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

        server.addTool(
            name = "test_tool",
            description = "Test tool to explore request structure",
        ) { request ->
            // Explore the request object
            println("=== MCP Tool Request Explorer ===")
            println("Request class: ${request::class.qualifiedName}")
            println("Request simple name: ${request::class.simpleName}")
            println()

            // Try to access Kotlin reflection to see properties
            try {
                val properties = request::class.memberProperties
                println("Available properties:")
                properties.forEach { prop ->
                    println("  - ${prop.name}: ${prop.returnType}")
                    try {
                        val value = prop.call(request)
                        println("    Value: $value")
                    } catch (e: Exception) {
                        println("    (Could not access: ${e.message})")
                    }
                }
            } catch (e: Exception) {
                println("Could not access properties via reflection: ${e.message}")
            }

            println()
            println("Request toString: $request")

            // Return a result
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text = "Request type: ${request::class.simpleName}\nCheck logs for details",
                        ),
                    ),
            )
        }

        println("✅ Tool registered. Handler signature understood.")
        println("   Note: Actual request structure will be visible when tool is invoked by MCP client")
    }
}
