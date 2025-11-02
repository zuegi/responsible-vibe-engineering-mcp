package ch.zuegi.rvmcp.adapter.output.workflow.strategy

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.onAssistantMessage

/**
 * Custom Koog Strategy for YAML-based workflows.
 *
 * Uses Koog DSL to define agent strategies similar to HighPerformanceAgentStrategies.
 */
object YamlWorkflowStrategy {
    /**
     * Simple single-shot strategy for YAML workflows.
     *
     * This is a simplified strategy that executes one LLM request per YAML node
     * and finishes immediately. The actual workflow orchestration (node-by-node)
     * is handled by KoogWorkflowExecutor.
     *
     * Flow: Start → LLM → Finish
     */
    fun simpleSingleShotStrategy() =
        strategy<String, String>("yaml-workflow-strategy") {
            val nodeCallLLM by nodeLLMRequest("node-call-llm", true)

            // Start: User Input -> LLM
            edge(nodeStart forwardTo nodeCallLLM)

            // LLM provides assistant message -> Finish immediately
            edge(
                nodeCallLLM forwardTo nodeFinish onAssistantMessage { message ->
                    true // Always finish after LLM response
                },
            )
        }
}
