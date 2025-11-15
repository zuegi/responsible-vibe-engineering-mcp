package ch.zuegi.rvmcp

import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test

/**
 * Explorer to understand MCP SDK API
 */
class McpSdkApiExplorer {
    @Test
    fun `explore Server creation`() {
        // Try to create a server and see what API is available
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
                                // What parameters are needed here?
                            ),
                    ),
            ) {
                "Test description"
            }

        // What methods are available on server?
        // server.addTool(...)
        // server.addResource(...)
        // server.connect(...)
    }

    @Test
    fun `explore Tool creation`() {
        // Try to create a tool
        val tool =
            Tool(
                name = "test_tool",
                description = "A test tool",
                title = "Test Tool",
                inputSchema = Tool.Input(properties = JsonObject(emptyMap()), required = emptyList()),
                outputSchema = null,
                annotations = null,
            )
    }
}
