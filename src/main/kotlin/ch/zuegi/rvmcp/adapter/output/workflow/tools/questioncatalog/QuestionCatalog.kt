package ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog

import kotlin.collections.filter

/**
 * Fragenkatalog als separate Datei/DB/Config ladbar
 * WICHTIG: Katalog ist NICHT im System Prompt → spart Tokens
 */
class QuestionCatalog(
    private val questions: Map<String, Question>,
) {
    fun getQuestion(id: String): Question =
        questions[id]
            ?: throw IllegalArgumentException("Question $id not found in catalog")

    fun getAllQuestionIds(): List<String> = questions.keys.toList()

    fun getQuestionsByCategory(category: String): List<Question> = questions.values.filter { it.category == category }

    companion object {
        fun fromFile(path: String): QuestionCatalog {
            // Load from JSON/YAML/DB
            // Hier würdest du aus einer Datei laden
            return QuestionCatalog(loadQuestionsFromFile(path))
        }

        private fun loadQuestionsFromFile(path: String): Map<String, Question> {
            // Placeholder - würde aus JSON/YAML laden
            return mapOf(
                "Q001" to
                    Question(
                        id = "Q001",
                        text = "What is the ISIN of the instrument?",
                        category = "instrument_identification",
                        validationRules =
                            listOf(
                                ValidationRule.Required(),
                                ValidationRule.Regex("^[A-Z]{2}[A-Z0-9]{9}[0-9]$"),
                            ),
                    ),
                "Q002" to
                    Question(
                        id = "Q002",
                        text = "What is the instrument type (BOND, STOCK, OPTION, FUTURE)?",
                        category = "instrument_identification",
                        validationRules =
                            listOf(
                                ValidationRule.Enum(listOf("BOND", "STOCK", "OPTION", "FUTURE")),
                            ),
                        followUpQuestions = listOf("Q003"),
                    ),
                "Q003" to
                    Question(
                        id = "Q003",
                        text = "What is the currency of the instrument?",
                        category = "instrument_details",
                        validationRules =
                            listOf(
                                ValidationRule.Regex("^[A-Z]{3}$"),
                            ),
                    ),
                "Q004" to
                    Question(
                        id = "Q004",
                        text = "What is the current market price?",
                        category = "pricing",
                        validationRules =
                            listOf(
                                ValidationRule.Required(false),
                            ),
                    ),
            )
        }
    }
}
