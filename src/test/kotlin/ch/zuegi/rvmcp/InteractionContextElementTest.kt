package ch.zuegi.rvmcp

import ch.zuegi.rvmcp.adapter.output.workflow.InteractionContextElement
import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import ch.zuegi.rvmcp.domain.model.interaction.InteractionType
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for InteractionContextElement.
 *
 * Validates that the CoroutineContext-based interaction mechanism works correctly:
 * - Context propagation across coroutine boundaries
 * - Request storage and retrieval
 * - Thread-safety
 */
class InteractionContextElementTest {
    @Test
    fun shouldStoreAndRetrieveInteractionRequest() =
        runBlocking<Unit> {
            val context = InteractionContextElement()
            val request =
                InteractionRequest(
                    type = InteractionType.ASK_USER,
                    question = "Test question?",
                )

            // Store request
            context.setRequest(request)

            // Retrieve request
            assertThat(context.hasRequest()).isTrue()
            val retrieved = context.consumeRequest()
            assertThat(retrieved).isEqualTo(request)
            assertThat(context.hasRequest()).isFalse()
        }

    @Test
    fun shouldPropagateContextAcrossWithContextBoundary() =
        runBlocking<Unit> {
            val context = InteractionContextElement()

            withContext(context) {
                // Set request inside withContext
                val request =
                    InteractionRequest(
                        type = InteractionType.ASK_USER,
                        question = "Inner question?",
                    )
                currentCoroutineContext()[InteractionContextElement]?.setRequest(request)
            }

            // Check request is available outside withContext
            assertThat(context.hasRequest()).isTrue()
            assertThat(context.consumeRequest()?.question).isEqualTo("Inner question?")
        }

    @Test
    fun shouldAccessContextFromSuspendFunction() =
        runBlocking<Unit> {
            val context = InteractionContextElement()

            suspend fun setRequestInSuspendFun() {
                val request =
                    InteractionRequest(
                        type = InteractionType.ASK_CATALOG_QUESTION,
                        question = "Catalog question?",
                        questionId = "Q1",
                    )
                currentCoroutineContext()[InteractionContextElement]?.setRequest(request)
            }

            withContext(context) {
                setRequestInSuspendFun()
            }

            assertThat(context.hasRequest()).isTrue()
            val retrieved = context.consumeRequest()
            assertThat(retrieved?.questionId).isEqualTo("Q1")
        }

    @Test
    fun shouldHandleMultipleSetConsumeCycles() =
        runBlocking<Unit> {
            val context = InteractionContextElement()

            // Cycle 1
            context.setRequest(
                InteractionRequest(
                    type = InteractionType.ASK_USER,
                    question = "Question 1?",
                ),
            )
            assertThat(context.consumeRequest()?.question).isEqualTo("Question 1?")

            // Cycle 2
            context.setRequest(
                InteractionRequest(
                    type = InteractionType.APPROVAL,
                    question = "Question 2?",
                ),
            )
            assertThat(context.consumeRequest()?.question).isEqualTo("Question 2?")

            // No request pending
            assertThat(context.hasRequest()).isFalse()
        }

    @Test
    fun shouldClearPendingRequest() =
        runBlocking<Unit> {
            val context = InteractionContextElement()

            context.setRequest(
                InteractionRequest(
                    type = InteractionType.ASK_USER,
                    question = "To be cleared",
                ),
            )

            assertThat(context.hasRequest()).isTrue()
            context.clear()
            assertThat(context.hasRequest()).isFalse()
        }

    @Test
    fun shouldBeAccessibleFromNestedCoroutineContexts() =
        runBlocking<Unit> {
            val context = InteractionContextElement()

            withContext(context) {
                // Access from nested context
                val element = currentCoroutineContext()[InteractionContextElement]
                assertThat(element).isNotNull
                assertThat(element).isSameAs(context)
            }
        }
}
