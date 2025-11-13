package ch.zuegi.rvmcp.infrastructure.config

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URL

/**
 * Health check for LLM connection at startup.
 *
 * Validates that the configured LLM endpoint is reachable
 * before the MCP server starts accepting requests.
 */
@Component
class LlmHealthCheck(
    private val llmProperties: LlmProperties,
) {
    @PostConstruct
    fun checkLlmConnection() {
        System.err.println("🔍 Checking LLM connection...")
        System.err.println("   Provider: ${llmProperties.provider}")
        System.err.println("   Base URL: ${llmProperties.baseUrl}")

        try {
            val url = URL(llmProperties.baseUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000 // 5 seconds
            connection.readTimeout = 5000
            connection.setRequestProperty("Authorization", "Bearer ${llmProperties.apiToken}")

            val responseCode = connection.responseCode

            when {
                responseCode in 200..299 || responseCode == 401 -> {
                    // 200-299: Success
                    // 401: Endpoint reachable, but auth might be wrong (acceptable for health check)
                    System.err.println("   ✅ LLM endpoint is reachable (HTTP $responseCode)")
                }
                else -> {
                    System.err.println("   ⚠️ LLM endpoint returned HTTP $responseCode")
                    System.err.println("   ⚠️ Workflows may fail if LLM is not properly configured")
                }
            }

            connection.disconnect()
        } catch (e: Exception) {
            System.err.println("   ❌ LLM connection failed: ${e.message}")
            System.err.println("   ⚠️ Workflows WILL fail until LLM is properly configured")
            System.err.println()
            System.err.println("   Please check:")
            System.err.println("   - LLM_BASE_URL is correct")
            System.err.println("   - LLM_API_TOKEN is valid")
            System.err.println("   - Network connectivity")
            System.err.println()
        }
    }
}
