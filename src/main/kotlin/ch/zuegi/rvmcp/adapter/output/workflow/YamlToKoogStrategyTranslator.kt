package ch.zuegi.rvmcp.adapter.output.workflow

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate
import ch.zuegi.rvmcp.infrastructure.logging.rvmcpLogger

/**
 * Translates YAML workflow templates into Koog agent strategies.
 *
 * Converts multi-node YAML workflows into a single Koog strategy graph,
 * enabling the agent to preserve context across all workflow steps.
 *
 * Based on Koog 0.5.1 API structure, following the pattern from YamlWorkflowStrategy.
 */
class YamlToKoogStrategyTranslator {
    private val logger by rvmcpLogger()

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

            val nodes = workflow.nodes
            logger.info("Found ${nodes.size} nodes (${nodes.count { it.type == NodeType.LLM }} LLM nodes)")

            val llmNodes = workflow.nodes.filter { it.type == NodeType.LLM }
            logger.info("Found ${llmNodes.size} LLM nodes to translate")

            if (llmNodes.isEmpty()) {
                throw IllegalArgumentException("Workflow '${workflow.name}' has no LLM nodes")
            }

            // For single-node workflows, use simple single-shot strategy with tool support
            if (llmNodes.size == 1) {
                val singleNode by nodeLLMRequest(llmNodes.first().id, true)
                val toolExecNode by nodeExecuteTool("execute-tool")
                val toolResultNode by nodeLLMSendToolResult("send-tool-result")

                edge(nodeStart forwardTo singleNode)

                // Handle tool calls: LLM -> Execute Tool -> Send Result -> back to LLM
                edge(
                    singleNode forwardTo toolExecNode onToolCall { toolCall ->
                        logger.debug("Tool call: ${toolCall.tool}")
                        true
                    },
                )
                edge(toolExecNode forwardTo toolResultNode)

                // After tool result, LLM can make another tool call OR send assistant message
                edge(
                    toolResultNode forwardTo toolExecNode onToolCall { toolCall ->
                        logger.debug("Follow-up tool call: ${toolCall.tool}")
                        true
                    },
                )
                edge(
                    toolResultNode forwardTo singleNode onAssistantMessage {
                        true
                    },
                )

                // Handle completion: LLM -> Finish
                edge(
                    singleNode forwardTo nodeFinish onAssistantMessage {
                        true
                    },
                )
                return@strategy
            }

            // Multi-node workflow: Create all LLM nodes dynamically with tool support
            logger.debug("Creating ${llmNodes.size} LLM nodes...")

            // Create all nodes first (must be done before creating edges)
            val koogNodes =
                llmNodes.mapIndexed { index, yamlNode ->
                    when (yamlNode.type) {
                        NodeType.LLM -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created Koog node '${yamlNode.id}' (${index + 1}/${llmNodes.size})")
                            node
                        }

                        // folgende Node Types
                        NodeType.GET_QUESTION,
                        NodeType.ASK_CATALOG_QUESTION,
                        NodeType.VALIDATE_ANSWER,
                        -> {
                            // Diese Nodes werden als Tool-Calls behandelt
                            // Der Agent ruft automatisch die entsprechenden Tools auf
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created catalog node '${yamlNode.id}' (type: ${yamlNode.type})")
                            node
                        }

                        else -> {
                            throw IllegalArgumentException("Unsupported node type: ${yamlNode.type}")
                        }
                    }
                }

            // Create shared tool execution nodes for all LLM nodes
            val toolExecNode by nodeExecuteTool("execute-tool")
            val toolResultNode by nodeLLMSendToolResult("send-tool-result")

            // Create edges: Start -> Node1 -> Node2 -> ... -> NodeN -> Finish
            edge(nodeStart forwardTo koogNodes.first())
            logger.debug("Edge: START -> ${llmNodes[0].id}")

            // For each LLM node: add tool call support AND transition to next node
            for (i in 0 until koogNodes.size) {
                // Tool calls from this node: Node[i] -> Execute Tool -> Send Result -> back to Node[i]
                edge(
                    koogNodes[i] forwardTo toolExecNode onToolCall { toolCall ->
                        logger.debug("Tool call from ${llmNodes[i].id}: ${toolCall.tool}")
                        true
                    },
                )
                edge(toolExecNode forwardTo toolResultNode)

                // After tool result, LLM can make another tool call OR send assistant message
                edge(
                    toolResultNode forwardTo toolExecNode onToolCall { toolCall ->
                        logger.debug("Follow-up tool call: ${toolCall.tool}")
                        true
                    },
                )
                edge(
                    toolResultNode forwardTo koogNodes[i] onAssistantMessage {
                        true
                    },
                )

                // Transition to next node: Node[i] -> Node[i+1]
                if (i < koogNodes.size - 1) {
                    edge(
                        koogNodes[i] forwardTo koogNodes[i + 1] onAssistantMessage {
                            true
                        },
                    )
                    logger.debug("Edge: ${llmNodes[i].id} -> ${llmNodes[i + 1].id}")
                }
            }

            // Last node -> Finish
            edge(
                koogNodes.last() forwardTo nodeFinish onAssistantMessage {
                    true
                },
            )
            logger.debug("Edge: ${llmNodes.last().id} -> FINISH")

            logger.info("✅ Strategy created with ${llmNodes.size} LLM nodes, tool support, and edges")
        }
}
