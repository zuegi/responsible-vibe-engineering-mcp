package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class WorkflowTemplateParser {
    private val yamlMapper =
        ObjectMapper(YAMLFactory()).apply {
            registerModule(KotlinModule.Builder().build())
        }

    fun parseTemplate(templateName: String): WorkflowTemplate {
        val resourcePath = "workflows/$templateName"

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
        val nodeIds = template.nodes.map { it.id }.toSet()

        require(template.graph.start in nodeIds) {
            "Start node '${template.graph.start}' not found in nodes"
        }

        require(template.graph.end in nodeIds) {
            "End node '${template.graph.end}' not found in nodes"
        }

        template.graph.edges.forEach { edge ->
            require(edge.from in nodeIds) {
                "Edge references non-existent 'from' node: ${edge.from}"
            }
            require(edge.to in nodeIds) {
                "Edge references non-existent 'to' node: ${edge.to}"
            }
        }

        template.nodes.filter { it.type == NodeType.CONDITIONAL }
            .forEach { node ->
                require(node.condition != null) {
                    "Conditional node '${node.id}' must have a condition"
                }
                require(node.ifTrue != null && node.ifFalse != null) {
                    "Conditional node '${node.id}' must have both if_true and if_false"
                }
            }

        template.nodes.filter { it.type == NodeType.LLM }
            .forEach { node ->
                require(node.prompt != null) {
                    "LLM node '${node.id}' must have a prompt"
                }
            }
    }
}
