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
import ch.zuegi.rvmcp.infrastructure.config.LlmProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Simple test to verify LLM connection works.
 *
 * IMPORTANT: Requires src/main/resources/application-local.yml with LLM configuration.
 * See application-local.yml.example for template.
 */
@SpringBootTest
@ActiveProfiles("local")
class SimpleLLMConnectionTest {
    @Autowired
    private lateinit var llmProperties: LlmProperties

    @Test
    fun `should connect to LLM and get response`() =
        runBlocking {
            println("\n🚀 Testing LLM Connection...")
            println("Provider: ${llmProperties.provider}")

            // Use LlmProperties from Spring
            val baseUrl = llmProperties.baseUrl
            val apiVersion = llmProperties.apiVersion
            val apiToken = llmProperties.apiToken

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
