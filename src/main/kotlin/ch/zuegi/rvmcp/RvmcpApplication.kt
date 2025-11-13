package ch.zuegi.rvmcp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RvmcpApplication

/**
 * Main entry point for Responsible Vibe MCP.
 *
 * Starts Spring Boot application with MCP Server enabled by default.
 * Uses Spring Boot context for dependency injection, ensuring all beans
 * (repositories, services, etc.) are properly initialized.
 *
 * Usage: java -jar rvmcp.jar
 */
fun main(args: Array<String>) {
    runApplication<RvmcpApplication>(*args)
}
