package ch.zuegi.rvmcp

import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Test to explore if MCP SDK addTool supports suspend functions.
 */
class McpAsyncSupportExplorer {
    @Test
    fun `explore if addTool accepts suspend lambda`() {
        val server =
            Server(
                serverInfo =
                    Implementation(
                        name = "async-test-server",
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

        // Try to use suspend lambda
        server.addTool(
            name = "async_test_tool",
            description = "Test if suspend functions work",
        ) { request: CallToolRequest ->
            // Try to use suspend function
            runBlocking {
                delay(100) // This would be an async LLM call
                println("Async operation completed")
            }

            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text = "Async test successful",
                        ),
                    ),
            )
        }

        println("✅ If this compiles, MCP SDK addTool lambda can use suspend functions internally")
    }

    @Test
    fun `explore if addTool lambda itself can be suspend`() {
        val server =
            Server(
                serverInfo =
                    Implementation(
                        name = "suspend-lambda-test",
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

        // Try suspend lambda directly
        try {
            // This will fail at compile-time if not supported
            server.addTool(
                name = "suspend_lambda_tool",
                description = "Test suspend lambda",
            ) { request: CallToolRequest ->
                // Can we call suspend function here?
                // If this compiles, suspend is implicitly supported
                runBlocking {
                    delay(100)
                }
                CallToolResult(
                    content = listOf(TextContent(text = "Test")),
                )
            }

            println("✅ MCP SDK addTool lambda can use runBlocking (but is NOT a suspend lambda itself)")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }
}
