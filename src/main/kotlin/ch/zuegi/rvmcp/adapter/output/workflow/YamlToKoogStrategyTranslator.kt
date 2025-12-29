package ch.zuegi.rvmcp.adapter.output.workflow

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.AggregationNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.AskCatalogQuestionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.ConditionalNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.GetQuestionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.HumanInteractionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.LLMNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.SystemCommandNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.ValidateAnswerNode
import ch.zuegi.rvmcp.shared.rvmcpLogger

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

    fun translate(workflow: WorkflowTemplate) =
        strategy<String, String>("yaml-${workflow.name}") {
            logger.info("Translating workflow with ${workflow.nodes.size} nodes...")

            val koogNodes =
                workflow.nodes.mapIndexed { index, yamlNode ->
                    when (yamlNode) {
                        is LLMNode -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created LLM node '${yamlNode.id}'")
                            node
                        }

                        is GetQuestionNode -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created GET_QUESTION node '${yamlNode.id}'")
                            node
                        }

                        is AskCatalogQuestionNode -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created ASK_CATALOG_QUESTION node '${yamlNode.id}'")
                            node
                        }

                        is ValidateAnswerNode -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created VALIDATE_ANSWER node '${yamlNode.id}'")
                            node
                        }

                        is ConditionalNode -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created CONDITIONAL node '${yamlNode.id}'")
                            node
                        }

                        is HumanInteractionNode -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created HUMAN_INTERACTION node '${yamlNode.id}'")
                            node
                        }

                        is AggregationNode -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created AGGREGATION node '${yamlNode.id}'")
                            node
                        }

                        is SystemCommandNode -> {
                            val node by nodeLLMRequest(yamlNode.id, true)
                            logger.debug("Created SYSTEM_COMMAND node '${yamlNode.id}'")
                            node
                        }
                    }
                }

            // Tool execution infrastructure
            val toolExecNode by nodeExecuteTool("execute-tool")
            val toolResultNode by nodeLLMSendToolResult("send-tool-result")

            // Build edges using edge() method for proper reachability analysis
            edge(nodeStart forwardTo koogNodes.first())

            // Connect nodes sequentially with tool support
            for (i in 0 until koogNodes.size) {
                val currentNode = koogNodes[i]
                val isLastNode = i == koogNodes.size - 1

                // Tool execution flow
                edge(currentNode forwardTo toolExecNode onToolCall { true })
                edge(toolExecNode forwardTo toolResultNode)
                edge(toolResultNode forwardTo toolExecNode onToolCall { true })
                edge(toolResultNode forwardTo currentNode onAssistantMessage { true })

                // Forward flow
                if (!isLastNode) {
                    edge(currentNode forwardTo koogNodes[i + 1] onAssistantMessage { true })
                } else {
                    edge(currentNode forwardTo nodeFinish onAssistantMessage { true })
                }
            }

            logger.info("Strategy created")
        }
}
