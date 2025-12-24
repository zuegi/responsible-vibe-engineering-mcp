package ch.zuegi.rvmcp.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.core.io.ClassPathResource

/**
 * Utility class for setting up and verifying LLM configuration.
 *
 * Provides methods to:
 * - Load LLM properties from YAML files
 * - Display configuration details
 * - Verify LLM connectivity with user interaction
 *
 * This class is primarily used by test runners and CLI tools that need to bootstrap
 * LLM configuration outside of Spring's dependency injection context.
 */
object LlmSetup {
    /**
     * Loads LLM properties from application-local.yml or application.yml.
     *
     * Falls back to application.yml if application-local.yml is not found.
     * Environment variables can override YAML values.
     *
     * Config structure in YAML:
     * ```yaml
     * llm:
     *   provider: azure-openai
     *   base-url: https://...
     *   api-version: 2024-05-01-preview
     *   api-token: dummy
     * ```
     *
     * Environment variable overrides:
     * - AZURE_OPENAI_ENDPOINT → base-url
     * - AZURE_OPENAI_API_KEY → api-token
     * - AZURE_OPENAI_API_VERSION → api-version
     * - LLM_PROVIDER → provider
     *
     * @return Validated LLM properties
     * @throws IllegalStateException if configuration cannot be loaded
     */
    fun loadFromYaml(): LlmProperties {
        val yamlMapper =
            ObjectMapper(YAMLFactory()).apply {
                registerModule(KotlinModule.Builder().build())
            }

        // Try application-local.yml first, then application.yml
        val configFiles = listOf("application-local.yml", "application.yml")
        var config: Map<String, Any>? = null

        for (configFile in configFiles) {
            try {
                val resource = ClassPathResource(configFile)
                if (resource.exists()) {
                    config =
                        resource.inputStream.use { inputStream ->
                            @Suppress("UNCHECKED_CAST")
                            yamlMapper.readValue(inputStream, Map::class.java) as Map<String, Any>
                        }
                    println("  Loaded from: $configFile")
                    break
                }
            } catch (_: Exception) {
                // Continue to next file
            }
        }

        if (config == null) {
            throw IllegalStateException(
                """
                Could not find application-local.yml or application.yml!
                
                Please create src/main/resources/application-local.yml with:
                llm:
                  provider: azure-openai
                  base-url: https://your-gateway.example.com/openai/deployments/gpt-4o/
                  api-version: 2024-05-01-preview
                  api-token: dummy
                """.trimIndent(),
            )
        }

        // Extract llm config
        @Suppress("UNCHECKED_CAST")
        val llmConfig =
            config["llm"] as? Map<String, Any>
                ?: throw IllegalStateException("No 'llm' section found in config!")

        // Extract values with optional environment variable overrides
        val baseUrl =
            System.getenv("AZURE_OPENAI_ENDPOINT")
                ?: llmConfig["base-url"] as? String
                ?: llmConfig["baseUrl"] as? String
                ?: throw IllegalStateException("base-url not configured!")

        val apiToken =
            System.getenv("AZURE_OPENAI_API_KEY")
                ?: llmConfig["api-token"] as? String
                ?: llmConfig["apiToken"] as? String
                ?: "dummy"

        val apiVersion =
            System.getenv("AZURE_OPENAI_API_VERSION")
                ?: llmConfig["api-version"] as? String
                ?: llmConfig["apiVersion"] as? String
                ?: "2024-05-01-preview"

        val provider =
            System.getenv("LLM_PROVIDER")
                ?: llmConfig["provider"] as? String
                ?: "azure-openai"

        return LlmProperties(
            baseUrl = baseUrl,
            apiVersion = apiVersion,
            apiToken = apiToken,
            provider = provider,
        )
    }

    /**
     * Sets up LLM configuration by loading from YAML and displaying configuration details.
     *
     * @return Validated LLM properties
     * @throws IllegalStateException if configuration cannot be loaded
     */
    fun setupConfiguration(): LlmProperties {
        val llmProperties = loadFromYaml()

        println("\n✓ LLM Configuration loaded")
        println("  Provider: ${llmProperties.provider}")
        println("  Base URL: ${llmProperties.baseUrl}")
        println("  API Version: ${llmProperties.apiVersion}")

        return llmProperties
    }

    /**
     * Verifies LLM connection with health check.
     * Prompts user to continue if connection fails.
     *
     * @param llmProperties LLM configuration to test
     * @param promptOnFailure Whether to prompt user to continue on failure (default: true)
     * @return true if connection is verified or user chooses to continue anyway, false if user aborts
     */
    fun verifyConnection(
        llmProperties: LlmProperties,
        promptOnFailure: Boolean = true,
    ): Boolean {
        println("\n🔍 Testing LLM connection (this may take ~10 seconds)...")
        val healthCheck = LlmHealthCheck(llmProperties)

        try {
            healthCheck.checkLlmConnection()
            println("✓ LLM connection verified")
            return true
        } catch (e: Exception) {
            println("\n❌ LLM Health Check failed!")
            println("   Error: ${e.message}")

            if (!promptOnFailure) {
                return false
            }

            println("\n⚠️  Workflows will likely fail. Do you want to continue anyway?")
            print("   Continue? (j/n): ")
            System.out.flush()

            val input =
                try {
                    readlnOrNull()
                } catch (_: Exception) {
                    "n"
                }

            if (input?.lowercase() != "j" && input?.lowercase() != "y") {
                println("\n⛔ Aborted by user")
                return false
            }

            println("\n⚠️  Continuing despite health check failure...")
            return true
        }
    }

    /**
     * Convenience method to setup and verify LLM configuration in one call.
     *
     * @param promptOnFailure Whether to prompt user to continue on verification failure (default: true)
     * @return Validated LLM properties, or null if verification failed and user aborted
     */
    fun setupAndVerify(promptOnFailure: Boolean = true): LlmProperties? {
        val llmProperties = setupConfiguration()

        return if (verifyConnection(llmProperties, promptOnFailure)) {
            llmProperties
        } else {
            null
        }
    }
}
