package ch.zuegi.rvmcp.adapter.output.workflow

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate
import ch.zuegi.rvmcp.infrastructure.logging.logger
import org.springframework.stereotype.Component

/**
 * Translates YAML workflow templates into Koog agent strategies.
 *
 * Converts multi-node YAML workflows into a single Koog strategy graph,
 * enabling the agent to preserve context across all workflow steps.
 *
 * Based on Koog 0.5.1 API structure, following the pattern from YamlWorkflowStrategy.
 */
@Component
class YamlToKoogStrategyTranslator {
    private val logger by logger()

    /**
     * Translates a YAML workflow into a Koog strategy.
     *
     * Creates a strategy graph with:
     * - One LLM node per YAML LLM node
     * - Edges connecting nodes based on YAML workflow graph
     * - Context preservation through expandable chat
     */
    fun translate(workflow: WorkflowTemplate) =
        strategy<String, String>("yaml-${workflow.name}") {
            logger.info("Translating YAML workflow '${workflow.name}' to Koog strategy...")

            // Filter to only LLM nodes (skip conditional, human-interaction, etc.)
            val llmNodes = workflow.nodes.filter { it.type == NodeType.LLM }
            logger.info("Found ${llmNodes.size} LLM nodes to translate")

            if (llmNodes.isEmpty()) {
                throw IllegalArgumentException("Workflow '${workflow.name}' has no LLM nodes")
            }

            // For single-node workflows, use simple single-shot strategy
            if (llmNodes.size == 1) {
                val singleNode by nodeLLMRequest(llmNodes.first().id, true)
                edge(nodeStart forwardTo singleNode)
                edge(
                    singleNode forwardTo nodeFinish onAssistantMessage {
                        true
                    },
                )
                return@strategy
            }

            // Multi-node workflow: Create all LLM nodes first
            // Store in list to preserve order and avoid issues with delegated properties in loop
            val firstNode by nodeLLMRequest(llmNodes[0].id, true)
            logger.debug("Created Koog node '${llmNodes[0].id}'")

            // Start edge
            edge(nodeStart forwardTo firstNode)

            // For workflows with exactly 2 nodes
            if (llmNodes.size == 2) {
                val secondNode by nodeLLMRequest(llmNodes[1].id, true)
                logger.debug("Created Koog node '${llmNodes[1].id}'")

                edge(
                    firstNode forwardTo secondNode onAssistantMessage {
                        true
                    },
                )
                edge(
                    secondNode forwardTo nodeFinish onAssistantMessage {
                        true
                    },
                )
                return@strategy
            }

            // For workflows with 3+ nodes - limit to first 3 for now
            // TODO: Support arbitrary number of nodes (requires dynamic edge creation)
            if (llmNodes.size >= 3) {
                val secondNode by nodeLLMRequest(llmNodes[1].id, true)
                val thirdNode by nodeLLMRequest(llmNodes[2].id, true)

                logger.debug("Created multi-node strategy with 3 nodes")

                edge(
                    firstNode forwardTo secondNode onAssistantMessage {
                        true
                    },
                )
                edge(
                    secondNode forwardTo thirdNode onAssistantMessage {
                        true
                    },
                )
                edge(
                    thirdNode forwardTo nodeFinish onAssistantMessage {
                        true
                    },
                )

                if (llmNodes.size > 3) {
                    logger.warn(
                        "Workflow has ${llmNodes.size} LLM nodes, but only first 3 are supported. Additional nodes will be skipped.",
                    )
                }
            }
        }
}
