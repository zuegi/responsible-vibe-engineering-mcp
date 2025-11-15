package ch.zuegi.rvmcp

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import org.junit.jupiter.api.Test

/**
 * Explorer to understand MCP Request structure and parameter extraction
 */
class McpRequestStructureExplorer {
    @Test
    fun `explore addTool request structure`() {
        val server =
            Server(
                serverInfo =
                    Implementation(
                        name = "test",
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

        // Explore what the request parameter looks like
        server.addTool(
            name = "test_tool",
            description = "Test tool to explore request structure",
        ) { request ->
            // What properties does 'request' have?
            println("Request type: ${request::class.qualifiedName}")
            println("Request toString: $request")

            // Since we don't know the structure yet, just return success
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text = "Request type: ${request::class.simpleName}",
                        ),
                    ),
            )
        }

        println("Tool registered. Request handler signature understood.")
    }
}
