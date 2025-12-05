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
 * - Execution instructions (including catalog rules if applicable)
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

        // Check if workflow uses question catalog
        val catalogNodes =
            workflow.nodes.filter {
                it.type in
                    listOf(
                        NodeType.GET_QUESTION,
                        NodeType.ASK_CATALOG_QUESTION,
                        NodeType.VALIDATE_ANSWER,
                    )
            }
        val hasCatalogNodes = catalogNodes.isNotEmpty()

        return """
You are an AI assistant executing a structured workflow: **${workflow.name}**

## Workflow Purpose
${workflow.description ?: "No description provided"}

## Workflow Steps
${workflow.nodes.mapIndexed { index, node ->
            val nodeLabel =
                when (node.type) {
                    NodeType.GET_QUESTION -> "[CATALOG-LOAD]"
                    NodeType.ASK_CATALOG_QUESTION -> "[CATALOG-ASK]"
                    NodeType.VALIDATE_ANSWER -> "[VALIDATE]"
                    NodeType.LLM -> "[LLM]"
                    else -> "[${node.type}]"
                }
            "$nodeLabel ${index + 1}. **${node.id}**: ${node.description ?: "No description"}${
                if (node.questionId != null) " (Question ID: ${node.questionId})" else ""
            }"
        }.joinToString("\n")}

## Execution Instructions
- Execute each step sequentially
- Use the output of previous steps as context for the next step
- Maintain conversation history to preserve context
- Be thorough but concise in your responses
- Document your reasoning and decisions

${if (hasCatalogNodes) buildCatalogRulesSection(catalogNodes) else ""}

## 📄 File Creation (IMPORTANT!)
- When you need to create a file, use the **create_file** tool
- Call create_file with:
  - path: relative path from project root (e.g., "docs/requirements.md")
  - content: the full file content
  - mimeType: file type (e.g., "text/markdown", "text/plain", "application/json")
- Example: create_file(path="docs/feature.md", content="# Feature\n...", mimeType="text/markdown")
- **Always provide complete content** - do not use placeholders
- For Markdown files: Use proper formatting (headers, lists, code blocks)
- After creating a file, confirm to the user what was created

## 👤 User Interaction (IMPORTANT!)
- When you need information from the user, use the **ask_user** tool
- Call ask_user with a clear, specific question
- Wait for the user's response before proceeding
- Example: ask_user(question="What should this feature do?")
- Do NOT make assumptions - always ask when information is missing
- **After gathering all information**: Provide a structured summary of what you learned
- Format the summary clearly with sections (e.g., "## Requirements Summary", "### Functionality", etc.)

## Current Project Context
- **Project Path**: ${context.projectPath}
- **Git Branch**: ${context.gitBranch ?: "main"}
- **Execution ID**: ${context.executionId.value}
${if (context.phaseHistory.isNotEmpty()) "- **Previous Phases**: ${context.phaseHistory.joinToString(", ")}" else ""}

## Node-Specific Instructions

${buildNodeSpecificInstructions(workflow.nodes)}

---

**Begin with Step 1: ${workflow.nodes.firstOrNull()?.id ?: "N/A"}**
            """.trimIndent()
    }

    /**
     * Builds catalog-specific rules section for the system prompt.
     * Only included when workflow contains catalog nodes.
     */
    private fun buildCatalogRulesSection(catalogNodes: List<ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowNode>): String =
        """
## 🚨 QUESTION CATALOG RULES (CRITICAL!)

This workflow uses an approved question catalog. You MUST follow these rules strictly:

### Rule 1: Question Loading (GET_QUESTION nodes)
- **ALWAYS** call the **get_question** tool with the specified questionId
- Example: `get_question(questionId="Q001")`
- This retrieves the canonical question text from the approved catalog
- **DO NOT** proceed without loading the question first

### Rule 2: Asking Questions (ASK_CATALOG_QUESTION nodes)
- **Step 1**: Call `get_question(questionId="...")` to load the question
- **Step 2**: Call `ask_user(question="<EXACT TEXT from get_question>")` 
- **CRITICAL**: Use the EXACT question text returned by get_question
- **DO NOT**:
  - Rephrase the question
  - Add your own wording
  - Skip the get_question call
  - Invent questions not in the catalog

### Rule 3: Answer Validation (VALIDATE_ANSWER nodes)
- Validation happens automatically based on catalog rules
- You do not need to manually validate
- If validation fails, the user will be prompted to retry
- Simply acknowledge validation results and proceed

### Rule 4: Question Flow
- Questions must be asked in the order specified by the workflow
- Each question has a unique ID (e.g., Q001, Q002)
- Follow-up questions are defined in the catalog
- Do not skip questions or change the order

### Rule 5: Prohibited Actions
- ❌ Never ask questions not from the catalog
- ❌ Never modify catalog question text
- ❌ Never skip the get_question tool call
- ❌ Never proceed without user's answer

### Example Correct Flow:
```
1. get_question(questionId="Q001")
   → Returns: "What is the ISIN of the instrument?"
2. ask_user(question="What is the ISIN of the instrument?")
   → User answers: "CH0012345678"
3. Proceed to next question
```

### Catalog Questions in This Workflow:
${catalogNodes.mapIndexed { index, node ->
            "${index + 1}. ${node.id} - Question ID: ${node.questionId ?: "N/A"}"
        }.joinToString("\n")}

**REMEMBER**: The catalog prevents hallucination. Always use exact catalog questions.

"""

    /**
     * Builds node-specific instructions section.
     * Handles both LLM nodes and catalog nodes.
     */
    private fun buildNodeSpecificInstructions(nodes: List<ch.zuegi.rvmcp.adapter.output.workflow.model.WorkflowNode>): String =
        nodes
            .mapIndexed { index, node ->
                when (node.type) {
                    NodeType.LLM -> {
                        """
### Step ${index + 1}: ${node.id} [LLM]
${node.prompt ?: "No specific instructions"}
${if (node.output != null) "\n**Output Variable**: ${node.output}" else ""}
                    """.trim()
                    }

                    NodeType.GET_QUESTION -> {
                        """
### Step ${index + 1}: ${node.id} [CATALOG-LOAD]
**Action**: Load question from catalog
**Question ID**: ${node.questionId ?: "ERROR: No questionId specified"}
**Required Tool Call**: `get_question(questionId="${node.questionId}")`

This step retrieves the canonical question text. Do not proceed without calling get_question.
                    """.trim()
                    }

                    NodeType.ASK_CATALOG_QUESTION -> {
                        """
### Step ${index + 1}: ${node.id} [CATALOG-ASK]
**Action**: Ask user catalog question
**Question ID**: ${node.questionId ?: "ERROR: No questionId specified"}
**Process**:
  1. Call `get_question(questionId="${node.questionId}")`
  2. Call `ask_user(question="<text from step 1>")` with EXACT question text
  3. Wait for user's answer
${if (node.retryOnInvalid) "  4. If answer is invalid, retry (max ${node.maxRetries} times)" else ""}

**CRITICAL**: Use the exact question text from get_question. Do not rephrase.
                    """.trim()
                    }

                    NodeType.VALIDATE_ANSWER -> {
                        """
### Step ${index + 1}: ${node.id} [VALIDATE]
**Action**: Validate user's answer
**Question ID**: ${node.questionId ?: "ERROR: No questionId specified"}

Validation is automatic based on catalog rules. Acknowledge the result and proceed.
                    """.trim()
                    }

                    else -> {
                        """
### Step ${index + 1}: ${node.id} [${node.type}]
${node.prompt ?: node.description ?: "No specific instructions"}
                    """.trim()
                    }
                }
            }.joinToString("\n\n")

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
        val firstNode = workflow.nodes.firstOrNull()

        val initialInstruction =
            when (firstNode?.type) {
                NodeType.GET_QUESTION -> {
                    """
Load the first question from the catalog.
Call: get_question(questionId="${firstNode.questionId}")
                    """.trimIndent()
                }

                NodeType.ASK_CATALOG_QUESTION -> {
                    """
Start by loading question ${firstNode.questionId} from the catalog, then ask the user.
Step 1: get_question(questionId="${firstNode.questionId}")
Step 2: ask_user(question="<text from step 1>")
                    """.trimIndent()
                }

                else -> {
                    "Start with the first step."
                }
            }

        return """
Execute workflow: ${workflow.name}

Context:
- Project: ${context.projectPath}
- Branch: ${context.gitBranch ?: "main"}

$initialInstruction
            """.trimIndent()
    }
}
