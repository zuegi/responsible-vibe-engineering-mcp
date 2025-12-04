import ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog.QuestionCatalog
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class QuestionCatalogTest {
    // TODO Testdaten einlesen, die Daten sind aktuell noch hard codiert im QuestionCatalog
    val catalog: QuestionCatalog = QuestionCatalog.fromFile("src/main/resources/questions.json")

    @Test
    fun `getQuestion returns correct question`() {
        val question = catalog.getQuestion("Q001")
        assertThat(question.id).isEqualTo("Q001")
        assertThat(question.text).isEqualTo("What is the ISIN of the instrument?")
    }

    @Test
    fun `getQuestion throws for unknown id`() {
        assertThatThrownBy { catalog.getQuestion("Q999") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `getAllQuestionIds returns all ids`() {
        val ids = catalog.getAllQuestionIds()
        assertThat(ids).containsExactlyInAnyOrder("Q001", "Q002", "Q003", "Q004")
        assertThat(ids).hasSize(4)
    }

    @Test
    fun `getQuestionsByCategory returns correct questions`() {
        val questions = catalog.getQuestionsByCategory("instrument_identification")
        assertThat(questions).hasSize(2)
        assertThat(questions).allMatch { it.category == "instrument_identification" }
    }
}
