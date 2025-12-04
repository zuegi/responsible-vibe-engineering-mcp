import ch.zuegi.rvmcp.adapter.output.workflow.tools.QuestionCatalogTool
import ch.zuegi.rvmcp.adapter.output.workflow.tools.questioncatalog.QuestionCatalog
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class QuestionCatalogToolTest {
    // TODO Testdaten einlesen, die Daten sind aktuell noch hard codiert im QuestionCatalog
    val catalog: QuestionCatalog = QuestionCatalog.fromFile("src/main/resources/questions.json")

    private val tool = QuestionCatalogTool(catalog)

    @Test
    fun `doExecute returns correct question string`() {
        runBlocking {
            val args = QuestionCatalogTool.Args(questionId = "Q001")
            val result = tool.doExecute(args)
            assertThat(result).contains("Q001")
            assertThat(result).contains("What is the ISIN of the instrument?")
        }
    }

    @Test
    fun `doExecute throws for unknown questionId`() {
        runBlocking {
            val args = QuestionCatalogTool.Args(questionId = "Q999")
            assertThatThrownBy {
                runBlocking { tool.doExecute(args) }
            }.isInstanceOf(IllegalArgumentException::class.java)
        }
    }
}
