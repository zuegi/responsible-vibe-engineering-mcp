package ch.zuegi.rvmcp.adapter.output.workflow

import aws.smithy.kotlin.runtime.util.type
import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.AskCatalogQuestionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.ConditionalNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.GetQuestionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.HumanInteractionNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.LLMNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.ValidateAnswerNode
import ch.zuegi.rvmcp.adapter.output.workflow.model.node.WorkflowNode
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext

/**
 * Builds comprehensive system prompts for Koog agents executing YAML workflows.
 *
 * The prompt includes:
 * - Workflow overview and purpose
 * - All workflow steps with descriptions
 * - Execution instructions (including catalog rules if applicable)
 * - Project context
 * - Individual node prompts
 */
class WorkflowPromptBuilder {
    fun buildNodeSpecificInstructions(nodes: List<WorkflowNode>): String =
        nodes
            .mapIndexed { index, node ->
                when (node) {
                    is LLMNode -> {
                        """
### Step ${index + 1}: ${node.id} [LLM]
${node.prompt ?: "No specific instructions"}
${node.tools?.let { "**Available Tools**: ${it.joinToString(", ")}" } ?: ""}
${node.output?.let { "**Output Variable**: $it" } ?: ""}
                    """.trim()
                    }

                    is GetQuestionNode -> {
                        """
### Step ${index + 1}: ${node.id} [CATALOG-LOAD]
**Action**: Load question from catalog
**Question ID**: ${node.questionId}
**Required Tool Call**: `get_question(questionId="${node.questionId}")`
${if (node.skipIfAnswered) "**Skip if already answered**" else ""}
                    """.trim()
                    }

                    is AskCatalogQuestionNode -> {
                        """
### Step ${index + 1}: ${node.id} [CATALOG-ASK]
**Action**: Ask user catalog question
**Question ID**: ${node.questionId}
**Required**: ${node.required}
**Process**:
  1. Call `get_question(questionId="${node.questionId}")`
  2. Call `ask_user(question="<text from step 1>")` with EXACT question text
  3. Wait for user's answer
${if (node.retryOnInvalid) "  4. If invalid, retry (max ${node.maxRetries} times)" else ""}
                    """.trim()
                    }

                    is ValidateAnswerNode -> {
                        """
### Step ${index + 1}: ${node.id} [VALIDATE]
**Action**: Validate user's answer
**Question ID**: ${node.questionId}
${node.validationRules?.let { "**Custom Rules**: ${it.joinToString()}" } ?: ""}
${node.on_failure?.let { "**On Failure**: $it" } ?: ""}
                    """.trim()
                    }

                    is ConditionalNode -> {
                        """
### Step ${index + 1}: ${node.id} [CONDITIONAL]
**Condition**: ${node.condition}
**If True**: ${node.if_true ?: "continue"}
**If False**: ${node.if_false ?: "continue"}
                    """.trim()
                    }

                    is HumanInteractionNode -> {
                        """
### Step ${index + 1}: ${node.id} [HUMAN_INTERACTION]
**Prompt**: ${node.prompt}
${node.inputs?.let { "**Inputs**: ${it.joinToString()}" } ?: ""}
                    """.trim()
                    }
                }
            }.joinToString("\n\n")
}
