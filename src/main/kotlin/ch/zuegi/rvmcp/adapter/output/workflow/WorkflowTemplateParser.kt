package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.adapter.output.workflow.model.*
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.AskCatalogQuestionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.ConditionalNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.GetQuestionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.HumanInteractionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.LLMNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.ValidateAnswerNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.core.io.ClassPathResource

class WorkflowTemplateParser {
    private val yamlMapper =
        ObjectMapper(YAMLFactory()).apply {
            registerModule(KotlinModule.Builder().build())
        }

    fun parseTemplate(templateName: String): WorkflowTemplate {
        val normalizedName = if (templateName.endsWith(".yml")) templateName else "$templateName.yml"
        val resourcePath = if (normalizedName.startsWith("workflows/")) normalizedName else "workflows/$normalizedName"

        return try {
            val resource = ClassPathResource(resourcePath)
            if (!resource.exists()) {
                throw IllegalArgumentException("Workflow template not found: $resourcePath")
            }

            resource.inputStream.use { inputStream ->
                yamlMapper.readValue(inputStream, WorkflowTemplate::class.java)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse workflow template: $resourcePath", e)
        }
    }

    fun validateTemplate(template: WorkflowTemplate) {
        val nodeIds = template.getNodeIds()

        // Validate graph structure
        require(template.graph.start in nodeIds) {
            "Start node '${template.graph.start}' not found in nodes. Available: ${nodeIds.joinToString()}"
        }

        require(template.graph.end in nodeIds) {
            "End node '${template.graph.end}' not found in nodes. Available: ${nodeIds.joinToString()}"
        }

        // Validate edges
        template.graph.edges.forEach { edge ->
            require(edge.from in nodeIds) {
                "Edge references non-existent 'from' node: ${edge.from}. Available: ${nodeIds.joinToString()}"
            }
            require(edge.to in nodeIds) {
                "Edge references non-existent 'to' node: ${edge.to}. Available: ${nodeIds.joinToString()}"
            }
        }

        // Validate node-specific requirements using polymorphism
        template.nodes.forEach { node ->
            when (node) {
                is LLMNode -> validateLLMNode(node)
                is GetQuestionNode -> validateGetQuestionNode(node)
                is AskCatalogQuestionNode -> validateAskCatalogQuestionNode(node)
                is ValidateAnswerNode -> validateValidateAnswerNode(node)
                is ConditionalNode -> validateConditionalNode(node, nodeIds)
                is HumanInteractionNode -> validateHumanInteractionNode(node)
            }
        }
    }

    private fun validateLLMNode(node: LLMNode) {
        // prompt is already required in data class, so this is redundant
        // but kept for explicit validation messages
        require(node.prompt.isNotBlank()) {
            "LLM node '${node.id}' must have a non-empty prompt"
        }
    }

    private fun validateGetQuestionNode(node: GetQuestionNode) {
        require(node.questionId.isNotBlank()) {
            "GET_QUESTION node '${node.id}' must have a non-empty questionId"
        }
    }

    private fun validateAskCatalogQuestionNode(node: AskCatalogQuestionNode) {
        require(node.questionId.isNotBlank()) {
            "ASK_CATALOG_QUESTION node '${node.id}' must have a non-empty questionId"
        }
        require(node.maxRetries > 0) {
            "ASK_CATALOG_QUESTION node '${node.id}' must have maxRetries > 0"
        }
    }

    private fun validateValidateAnswerNode(node: ValidateAnswerNode) {
        require(node.questionId.isNotBlank()) {
            "VALIDATE_ANSWER node '${node.id}' must have a non-empty questionId"
        }
    }

    private fun validateConditionalNode(
        node: ConditionalNode,
        nodeIds: Set<String>,
    ) {
        require(node.condition.isNotBlank()) {
            "CONDITIONAL node '${node.id}' must have a non-empty condition"
        }

        // if_true and if_false are already required in data class
        require(node.if_true.isNotBlank()) {
            "CONDITIONAL node '${node.id}' must have a non-empty if_true target"
        }
        require(node.if_false.isNotBlank()) {
            "CONDITIONAL node '${node.id}' must have a non-empty if_false target"
        }

        // Optionally validate that targets exist (might be edge targets)
        // Commented out as edges might define the actual flow
        /*
        require(node.if_true in nodeIds) {
            "CONDITIONAL node '${node.id}' if_true target '${node.if_true}' not found in nodes"
        }
        require(node.if_false in nodeIds) {
            "CONDITIONAL node '${node.id}' if_false target '${node.if_false}' not found in nodes"
        }
         */
    }

    private fun validateHumanInteractionNode(node: HumanInteractionNode) {
        require(node.prompt.isNotBlank()) {
            "HUMAN_INTERACTION node '${node.id}' must have a non-empty prompt"
        }
    }
}
