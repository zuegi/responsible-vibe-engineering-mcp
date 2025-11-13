package ch.zuegi.rvmcp.infrastructure.config

import ch.zuegi.rvmcp.adapter.input.mcp.ResponsibleVibeMcpServer
import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.StartProcessExecutionUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CountDownLatch

/**
 * Configuration for MCP Server.
 *
 * Starts the MCP Server automatically on application launch using Spring Boot context
 * for dependency injection.
 */
@Configuration
class McpServerConfiguration {
    @Bean
    fun mcpServerStarter(
        startProcessUseCase: StartProcessExecutionUseCase,
        executePhaseUseCase: ExecuteProcessPhaseUseCase,
        completePhaseUseCase: CompletePhaseUseCase,
        memoryRepository: MemoryRepositoryPort,
        processRepository: ProcessRepositoryPort,
    ): CommandLineRunner =
        CommandLineRunner {
            System.err.println("🚀 Starting Responsible Vibe MCP Server...")
            System.err.println("   Using Spring Boot context for dependency injection")
            System.err.println()

            val mcpServer =
                ResponsibleVibeMcpServer(
                    startProcessUseCase = startProcessUseCase,
                    executePhaseUseCase = executePhaseUseCase,
                    completePhaseUseCase = completePhaseUseCase,
                    memoryRepository = memoryRepository,
                    processRepository = processRepository,
                )

            System.err.println("✅ MCP Server configured. Starting stdio transport...")
            mcpServer.start()

            // Keep application running - MCP server needs to stay alive
            System.err.println("⏳ MCP Server is running. Press Ctrl+C to stop.")
            val keepAlive = CountDownLatch(1)
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    System.err.println("\n🛑 Shutting down MCP Server...")
                    keepAlive.countDown()
                },
            )
            keepAlive.await()
        }
}
