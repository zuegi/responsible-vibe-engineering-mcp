package ch.zuegi.rvmcp.infrastructure.config

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openai.azure.AzureOpenAIServiceVersion
import ai.koog.prompt.executor.llms.all.simpleAzureOpenAIExecutor
import ch.zuegi.rvmcp.adapter.output.workflow.strategy.YamlWorkflowStrategy
import ch.zuegi.rvmcp.shared.rvmcpLogger
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
    private val log by rvmcpLogger()

    @PostConstruct
    fun checkLlmConnection() {
        log.info("🔍 Checking LLM connection...")
        log.debug("   Provider: ${llmProperties.provider}")
        log.debug("   Base URL: ${llmProperties.baseUrl}")
        log.debug("   API Token: ${llmProperties.apiToken}")

        try {
            runBlocking {
                val result =
                    withTimeoutOrNull(10000) {
                        // 10 second timeout
                        testLlmConnection()
                    }

                if (result == true) {
                    log.info("   ✅ LLM connection successful!")
                } else {
                    log.error("   ⚠️ LLM connection timed out or failed")
                    log.error("   ⚠️ Workflows may fail - check configuration")
                }
            }
        } catch (e: Exception) {
            log.error("   ❌ LLM connection failed: ${e.message}")
            log.error("   ⚠️ Workflows WILL fail until LLM is properly configured")
            log.error("   Please check:")
            log.error("   - LLM_BASE_URL is correct (current: ${llmProperties.baseUrl})")
            log.error("   - LLM_API_TOKEN is valid")
            log.error("   - Network connectivity")
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
            log.error("   ❌ Test call failed: ${e.message}")
            false
        }
}
