package ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val text: String,
    val category: String,
    val validationRules: List<ValidationRule> = emptyList(),
    val followUpQuestions: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
