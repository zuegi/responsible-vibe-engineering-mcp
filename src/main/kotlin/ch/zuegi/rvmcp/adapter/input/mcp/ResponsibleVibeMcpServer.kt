package ch.zuegi.rvmcp.adapter.input.mcp

import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.StartProcessExecutionUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

/**
 * Responsible Vibe MCP Server
 *
 * Exposes Engineering Workflows via Model Context Protocol.
 * Compatible with Claude Desktop, Warp Agent, and other MCP clients.
 *
 * Note: Not annotated with @Component to avoid auto-instantiation.
 * Should be manually created when MCP server mode is requested.
 */
class ResponsibleVibeMcpServer(
    private val startProcessUseCase: StartProcessExecutionUseCase,
    private val executePhaseUseCase: ExecuteProcessPhaseUseCase,
    private val completePhaseUseCase: CompletePhaseUseCase,
    private val memoryRepository: MemoryRepositoryPort,
    private val processRepository: ProcessRepositoryPort,
) {
    private val server: Server =
        Server(
            serverInfo =
                Implementation(
                    name = "responsible-vibe-mcp",
                    version = "0.1.0",
                ),
            options =
                ServerOptions(
                    capabilities =
                        ServerCapabilities(
                            tools = ServerCapabilities.Tools(listChanged = true),
                        ),
                ),
        )

    /**
     * Start the MCP Server with stdio transport.
     * Blocks until the server is stopped.
     */
    fun start(): Unit =
        runBlocking {
            System.err.println("🚀 Starting Responsible Vibe MCP Server...")
            System.err.println("   Version: 0.1.0")
            System.err.println("   Transport: stdio")
            System.err.println("   Listening for MCP clients (Claude Desktop, Warp Agent, etc.)")

            // Register all MCP Tools
            registerTools()

            // Register all MCP Resources
            registerResources()

            System.err.println("✅ MCP Server configured. Ready to connect.")

            // Create server session with stdio transport (using kotlinx.io Source/Sink)
            val transport =
                StdioServerTransport(
                    inputStream = System.`in`.asSource().buffered(),
                    outputStream = System.out.asSink().buffered(),
                )

            server.createSession(transport)
            System.err.println("✅ MCP Server session created and connected.")
        }

    private fun registerTools() {
        System.err.println("   📦 Registering MCP Tools...")

        // Tool 1: list_processes
        server.addTool(
            name = "list_processes",
            description = "Lists all available engineering processes (Feature Development, Bug Fix, etc.)",
        ) { _ ->
            // Call domain service to list processes
            val processes = processRepository.findAll()
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text =
                                "Found ${processes.size} engineering processes:\n" +
                                    processes.joinToString("\n") {
                                        "- ${it.id.value}: ${it.name} - ${it.description} (${it.totalPhases()} phases)"
                                    },
                        ),
                    ),
            )
        }

        System.err.println("      ✅ Registered: list_processes")

        // Tool 2: start_process
        server.addTool(
            name = "start_process",
            description = "Starts a new engineering process execution (requires processId, projectPath, gitBranch)",
        ) { request ->
            // TODO: Parse arguments from request when SDK API is clarified
            // For now, return placeholder
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text = "⚠️ start_process: Implementation in progress. Need to parse request arguments.",
                        ),
                    ),
            )
        }

        System.err.println("      ✅ Registered: start_process")

        // Tool 3: execute_phase
        server.addTool(
            name = "execute_phase",
            description = "Executes a specific phase of an active process",
        ) { request ->
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text = "⚠️ execute_phase: Implementation in progress.",
                        ),
                    ),
            )
        }

        System.err.println("      ✅ Registered: execute_phase")

        // Tool 4: complete_phase
        server.addTool(
            name = "complete_phase",
            description = "Marks a phase as completed and provides results",
        ) { request ->
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text = "⚠️ complete_phase: Implementation in progress.",
                        ),
                    ),
            )
        }

        System.err.println("      ✅ Registered: complete_phase")

        // Tool 5: get_memory
        server.addTool(
            name = "get_memory",
            description = "Retrieves stored memory/context for a process execution (requires projectPath, gitBranch)",
        ) { request ->
            // TODO: Parse arguments from request
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text = "⚠️ get_memory: Implementation in progress. Need to parse request arguments.",
                        ),
                    ),
            )
        }

        System.err.println("      ✅ Registered: get_memory")
    }

    private fun registerResources() {
        System.err.println("   📂 Registering MCP Resources...")
        System.err.println("      ⚠️ Resource registration: API study needed")
        // TODO: Study MCP SDK samples to understand correct Resource registration API
        // - How to register resource templates
        // - How to handle resource requests
        // - How to access request parameters
    }
}
