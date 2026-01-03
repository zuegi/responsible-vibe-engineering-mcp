package ch.zuegi.rvmcp.adapter.output.workflow.tools

import ai.koog.agents.core.tools.SimpleTool
import ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog.QuestionCatalog
import ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog.ValidationRule
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class QuestionCatalogTool(
    private val catalog: QuestionCatalog,
) : SimpleTool<QuestionCatalogTool.Args>(
        argsSerializer = serializer<Args>(),
        name = "get_question",
        description =
            """
            Retrieves a canonical question from the approved question catalog.
            MUST be called before asking the user any question.
            Returns the exact question text that should be presented to the user.
            """.trimIndent(),
    ) {
    @Serializable
    data class Args(
        val questionId: String,
        val includeMetadata: Boolean = false,
    )

    override suspend fun execute(args: Args): String {
        val question = catalog.getQuestion(args.questionId)

        val result =
            QuestionResult(
                questionId = question.id,
                questionText = question.text,
                category = question.category,
                validationRules = if (args.includeMetadata) question.validationRules else emptyList(),
                metadata = if (args.includeMetadata) question.metadata else emptyMap(),
            )
        return Json.encodeToString(result)
    }

    @Serializable
    data class QuestionResult(
        val questionId: String,
        val questionText: String,
        val category: String,
        val validationRules: List<ValidationRule> = emptyList(),
        val metadata: Map<String, String> = emptyMap(),
    )
}
