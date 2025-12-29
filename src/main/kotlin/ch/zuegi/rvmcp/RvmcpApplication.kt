package ch.zuegi.rvmcp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

@SpringBootApplication
class RvmcpApplication

/**
 * Original stdin/stdout for MCP JSON-RPC protocol.
 * Captured at startup before any redirection.
 */
object McpStdio {
    // Use FileDescriptor to access REAL stdin/stdout (bypasses System.in/out redirection)
    val stdin = FileInputStream(FileDescriptor.`in`)
    val stdout = FileOutputStream(FileDescriptor.out)
}

/**
 * Main entry point for Responsible Vibe MCP.
 *
 * CRITICAL for MCP stdio mode:
 * - stdout is exclusively for JSON-RPC messages
 * - ALL application logs must go to stderr
 *
 * We redirect System.out to System.err for Spring Boot logs,
 * but ResponsibleVibeMcpServer uses McpStdio (FileDescriptor) for JSON-RPC.
 *
 * Usage: java -jar rvmcp.jar
 */
fun main(args: Array<String>) {
    // Redirect System.out to System.err for all Spring Boot logs
    System.setOut(System.err)

    runApplication<RvmcpApplication>(*args)
}
