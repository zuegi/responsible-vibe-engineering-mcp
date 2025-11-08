package ch.zuegi.rvmcp

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.tracing.feature.Tracing
import ai.koog.agents.features.tracing.writer.TraceFeatureMessageLogWriter
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openai.azure.AzureOpenAIServiceVersion
import ai.koog.prompt.executor.llms.all.simpleAzureOpenAIExecutor
import ch.zuegi.rvmcp.adapter.output.workflow.strategy.YamlWorkflowStrategy
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Simple test to verify Azure OpenAI LLM connection works.
 *
 * IMPORTANT: Requires src/test/resources/application-test.yml with Azure OpenAI configuration.
 * See application-test.yml.example for template.
 */
class SimpleLLMConnectionTest {
    @Test
    fun `should connect to Azure OpenAI and get response`() =
        runBlocking {
            println("\n🚀 Testing Azure OpenAI Connection...")

            // Load config from environment or application-test.yml
            val baseUrl =
                System.getenv("AZURE_OPENAI_BASE_URL")
                    ?: System.getProperty("azure.openai.base-url")
                    ?: "https://api.openai.com/v1/" // Fallback to OpenAI
            val apiVersion = System.getenv("AZURE_OPENAI_API_VERSION") ?: "2024-05-01-preview"
            val apiToken = System.getenv("AZURE_OPENAI_API_TOKEN") ?: "dummy"

            println("Base URL: ${baseUrl.take(30)}...")

            val startTime = System.currentTimeMillis()

            // Create executor
            println("📡 Creating executor...")
            val executor =
                simpleAzureOpenAIExecutor(
                    baseUrl = baseUrl,
                    version = AzureOpenAIServiceVersion.fromString(apiVersion),
                    apiToken = apiToken,
                )
            println("✅ Executor created (${System.currentTimeMillis() - startTime}ms)")

            // Create strategy
            val strategy = YamlWorkflowStrategy.simpleSingleShotStrategy()

            // Create config with simple prompt
            val config =
                AIAgentConfig(
                    prompt =
                        prompt("simple_test") {
                            system("You are a helpful AI assistant. Answer very briefly.")
                        },
                    model = OpenAIModels.Chat.GPT4o,
                    maxAgentIterations = 3,
                )

            // Create agent with tracing
            println("🤖 Creating agent...")
            val agent =
                AIAgent<String, String>(
                    promptExecutor = executor,
                    strategy = strategy,
                    agentConfig = config,
                    toolRegistry = ToolRegistry { },
                    installFeatures = {
                        install(Tracing) {
                            addMessageProcessor(TraceFeatureMessageLogWriter(KotlinLogging.logger("KoogTrace")))
                        }
                        println("✅ Tracing installed")
                    },
                )
            println("✅ Agent created (${System.currentTimeMillis() - startTime}ms)")

            // Send simple query
            val query = "Say hello in one word"
            println("\n📤 Sending query: '$query'")
            println("⏱️  Waiting for LLM response...")

            val queryStart = System.currentTimeMillis()
            val response = agent.run(query)
            val queryDuration = System.currentTimeMillis() - queryStart

            println("\n✅ Response received in ${queryDuration}ms")
            println("📝 Response: '$response'")
            println("📊 Total duration: ${System.currentTimeMillis() - startTime}ms")

            // Verify we got a response
            assert(response.isNotBlank()) { "Response should not be blank" }
            println("\n🎉 LLM Connection Test PASSED!")
        }
}
