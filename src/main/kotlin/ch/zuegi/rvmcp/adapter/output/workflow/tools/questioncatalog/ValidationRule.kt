package ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kotlinx.serialization.Serializable

@Serializable
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ValidationRule.Required::class, name = "REQUIRED"),
    JsonSubTypes.Type(value = ValidationRule.Regex::class, name = "REGEX"),
    JsonSubTypes.Type(value = ValidationRule.Enum::class, name = "ENUM"),
    JsonSubTypes.Type(value = ValidationRule.MinLength::class, name = "MIN_LENGTH"),
    JsonSubTypes.Type(value = ValidationRule.MaxLength::class, name = "MAX_LENGTH"),
)
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
        @JsonProperty("values")
        val allowedValues: List<String>,
    ) : ValidationRule()

    @Serializable
    data class Required(
        val required: Boolean = true,
    ) : ValidationRule()
}
