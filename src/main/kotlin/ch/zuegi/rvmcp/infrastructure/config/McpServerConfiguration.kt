package ch.zuegi.rvmcp.infrastructure.config

import ch.zuegi.rvmcp.adapter.input.mcp.ResponsibleVibeMcpServer
import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.StartProcessExecutionUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.CountDownLatch

/**
 * Configuration for MCP Server.
 *
 * Starts the MCP Server automatically on application launch using Spring Boot context
 * for dependency injection.
 *
 * Note: Only active when NOT in 'test' profile.
 * - Default: MCP Server starts (for Claude Desktop / Warp integration)
 * - local profile: MCP Server starts (local development with MCP)
 * - test profile: MCP Server does NOT start (unit tests)
 */
@Profile("!test")
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
            val log by rvmcpLogger()
            val mcpServer =
                ResponsibleVibeMcpServer(
                    startProcessUseCase = startProcessUseCase,
                    executePhaseUseCase = executePhaseUseCase,
                    completePhaseUseCase = completePhaseUseCase,
                    memoryRepository = memoryRepository,
                    processRepository = processRepository,
                )

            // runBlocking needed here because CommandLineRunner.run() is not suspend
            runBlocking {
                mcpServer.start()
            }
            val keepAlive = CountDownLatch(1)
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    log.info("\nShutting down MCP Server...")
                    keepAlive.countDown()
                },
            )
            keepAlive.await()
        }
}
