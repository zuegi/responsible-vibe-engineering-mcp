package ch.zuegi.rvmcp.adapter.output.workflow

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowTemplate
import ch.zuegi.rvmcp.domain.model.context.ExecutionContext

/**
 * Builds comprehensive system prompts for Koog agents executing YAML workflows.
 *
 * The prompt includes:
 * - Workflow overview and purpose
 * - All workflow steps with descriptions
 * - Execution instructions
 * - Project context
 * - Individual node prompts
 */
class WorkflowPromptBuilder {
    /**
     * Builds a complete system prompt for executing a workflow.
     *
     * @param workflow The workflow template
     * @param context The execution context (project path, branch, etc.)
     * @return A comprehensive system prompt
     */
    fun buildWorkflowSystemPrompt(
        workflow: WorkflowTemplate,
        context: ExecutionContext,
    ): String {
        val llmNodes = workflow.nodes.filter { it.type == NodeType.LLM }

        return """
You are an AI assistant executing a structured workflow: **${workflow.name}**

## Workflow Purpose
${workflow.description ?: "No description provided"}

## Workflow Steps
${llmNodes.mapIndexed { index, node ->
            "${index + 1}. **${node.id}**: ${node.description ?: "No description"}"
        }.joinToString("\n")}

## Execution Instructions
- Execute each step sequentially
- Use the output of previous steps as context for the next step
- Maintain conversation history to preserve context
- Be thorough but concise in your responses
- Document your reasoning and decisions

## Current Project Context
- **Project Path**: ${context.projectPath}
- **Git Branch**: ${context.gitBranch ?: "main"}
- **Execution ID**: ${context.executionId.value}
${if (context.phaseHistory.isNotEmpty()) "- **Previous Phases**: ${context.phaseHistory.joinToString(", ")}" else ""}

## Node-Specific Instructions

${llmNodes.mapIndexed { index, node ->
            """
### Step ${index + 1}: ${node.id}
${node.prompt ?: "No specific instructions"}
${if (node.output != null) "\n**Output Variable**: ${node.output}" else ""}
    """.trim()
        }.joinToString("\n\n")}

---

**Begin with Step 1: ${llmNodes.firstOrNull()?.id ?: "N/A"}**
            """.trimIndent()
    }

    /**
     * Builds a simplified prompt for the initial agent run.
     * Individual node prompts will be injected during execution.
     *
     * @param workflow The workflow template
     * @param context The execution context
     * @return A simplified initial prompt
     */
    fun buildInitialPrompt(
        workflow: WorkflowTemplate,
        context: ExecutionContext,
    ): String {
        return """
Execute workflow: ${workflow.name}

Context:
- Project: ${context.projectPath}
- Branch: ${context.gitBranch ?: "main"}

Start with the first step.
            """.trimIndent()
    }
}
