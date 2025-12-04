package ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog

import kotlinx.serialization.Serializable

@Serializable
sealed class ValidationRule {
    @Serializable
    data class MinLength(
        val length: Int,
    ) : ValidationRule()

    @Serializable
    data class MaxLength(
        val length: Int,
    ) : ValidationRule()

    @Serializable
    data class Regex(
        val pattern: String,
    ) : ValidationRule()

    @Serializable
    data class Enum(
        val allowedValues: List<String>,
    ) : ValidationRule()

    @Serializable
    data class Required(
        val required: Boolean = true,
    ) : ValidationRule()
}
