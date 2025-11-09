package ch.zuegi.rvmcp.adapter.input.mcp

import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.StartProcessExecutionUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

/**
 * Responsible Vibe MCP Server
 *
 * Exposes Engineering Workflows via Model Context Protocol.
 * Compatible with Claude Desktop, Warp Agent, and other MCP clients.
 */
@Component
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
                            tools = ServerCapabilities.Tools(),
                            resources = ServerCapabilities.Resources(),
                        ),
                ),
        ) {
            "Responsible Vibe Engineering - Structured AI-driven software development"
        }

    /**
     * Start the MCP Server with stdio transport.
     * Blocks until the server is stopped.
     */
    fun start() {
        println("🚀 Starting Responsible Vibe MCP Server...")
        println("   Version: 0.1.0")
        println("   Transport: stdio")
        println("   Listening for MCP clients (Claude Desktop, Warp Agent, etc.)")

        // Register all MCP Tools
        registerTools()

        // Register all MCP Resources
        registerResources()

        // Start server with stdio transport
        val transport = StdioServerTransport(System.`in`, System.out)

        runBlocking {
            server.connect(transport)
            println("✅ MCP Server started successfully")
        }
    }

    private fun registerTools() {
        println("   📦 Registering MCP Tools...")

        // Tool 1: list_processes
        server.addTool(
            tool =
                Tool(
                    name = "list_processes",
                    description = "Lists all available engineering processes (Feature Development, Bug Fix, etc.)",
                    inputSchema = emptyMap(), // No parameters
                ),
        ) { _ ->
            // Call domain service to list processes
            val processes = processRepository.findAll()
            mapOf(
                "processes" to
                    processes.map {
                        mapOf(
                            "id" to it.id.value,
                            "name" to it.name,
                            "description" to it.description,
                            "phases" to it.totalPhases(),
                        )
                    },
            )
        }

        println("      ✅ Registered: list_processes")
        // TODO: Register remaining 4 tools
    }

    private fun registerResources() {
        println("   📂 Registering MCP Resources...")
        // TODO: Register 2 MCP Resources
        // - context://
        // - process://
    }
}
