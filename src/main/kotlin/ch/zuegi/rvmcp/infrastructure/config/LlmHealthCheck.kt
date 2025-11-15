package ch.zuegi.rvmcp.infrastructure.config

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openai.azure.AzureOpenAIServiceVersion
import ai.koog.prompt.executor.llms.all.simpleAzureOpenAIExecutor
import ch.zuegi.rvmcp.adapter.output.workflow.strategy.YamlWorkflowStrategy
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Component

/**
 * Health check for LLM connection at startup.
 *
 * Makes a real LLM call to validate configuration (not just HEAD request).
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
        System.err.println("   API Token: ${llmProperties.apiToken}")

        try {
            runBlocking {
                val result =
                    withTimeoutOrNull(10000) {
                        // 10 second timeout
                        testLlmConnection()
                    }

                if (result == true) {
                    System.err.println("   ✅ LLM connection successful!")
                    System.err.println()
                } else {
                    System.err.println("   ⚠️ LLM connection timed out or failed")
                    System.err.println("   ⚠️ Workflows may fail - check configuration")
                    System.err.println()
                }
            }
        } catch (e: Exception) {
            System.err.println("   ❌ LLM connection failed: ${e.message}")
            System.err.println("   ⚠️ Workflows WILL fail until LLM is properly configured")
            System.err.println()
            System.err.println("   Please check:")
            System.err.println("   - LLM_BASE_URL is correct (current: ${llmProperties.baseUrl})")
            System.err.println("   - LLM_API_TOKEN is valid")
            System.err.println("   - Network connectivity")
            System.err.println()
        }
    }

    private suspend fun testLlmConnection(): Boolean =
        try {
            val executor =
                simpleAzureOpenAIExecutor(
                    baseUrl = llmProperties.baseUrl,
                    version = AzureOpenAIServiceVersion.fromString(llmProperties.apiVersion),
                    apiToken = llmProperties.apiToken,
                )

            val strategy = YamlWorkflowStrategy.simpleSingleShotStrategy()

            val config =
                AIAgentConfig(
                    prompt =
                        prompt("health_check") {
                            system("You are a health check. Respond with 'OK' only.")
                        },
                    model = OpenAIModels.Chat.GPT4o,
                    maxAgentIterations = 3,
                )

            val agent =
                AIAgent<String, String>(
                    promptExecutor = executor,
                    strategy = strategy,
                    agentConfig = config,
                    toolRegistry = ToolRegistry { },
                )

            val response = agent.run("Health check")
            response.isNotBlank()
        } catch (e: Exception) {
            System.err.println("   ❌ Test call failed: ${e.message}")
            false
        }
}
