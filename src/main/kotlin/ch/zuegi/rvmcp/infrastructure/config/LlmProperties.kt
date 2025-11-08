package ch.zuegi.rvmcp.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Generic LLM configuration properties.
 *
 * Supports multiple LLM providers (Azure OpenAI, OpenAI, Anthropic, etc.)
 * Configure via application.yml or environment variables.
 *
 * See docs/CONFIGURATION.md for setup instructions.
 */
@Configuration
@ConfigurationProperties(prefix = "llm")
data class LlmProperties(
    /**
     * Base URL for the LLM API.
     * Examples:
     * - Azure OpenAI: https://your-gateway.example.com/openai/deployments/gpt-4o/
     * - OpenAI: https://api.openai.com/v1/
     * - Custom: https://your-llm-gateway.com/v1/
     */
    var baseUrl: String = "https://api.openai.com/v1/",
    /**
     * API version (if applicable).
     * Used by Azure OpenAI, ignored by others.
     */
    var apiVersion: String = "2024-05-01-preview",
    /**
     * API token/key for authentication.
     * Can be:
     * - Real API key
     * - "dummy" for internal gateways without auth
     * - Empty string if auth is handled differently
     */
    var apiToken: String = "dummy",
    /**
     * Provider type (optional, for logging/debugging).
     * Examples: "azure-openai", "openai", "anthropic", "custom"
     */
    var provider: String = "openai",
)
