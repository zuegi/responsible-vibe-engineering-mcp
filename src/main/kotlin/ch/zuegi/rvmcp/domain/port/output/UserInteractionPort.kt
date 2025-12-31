package ch.zuegi.rvmcp.domain.port.output

import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest

/**
 * Output Port for user interaction.
 *
 * Abstraction layer for asking the user questions and receiving answers.
 * Different adapters can implement this for different contexts:
 * - MCP Mode: Throws InteractionRequiredException (workflow pauses)
 * - CLI Mode: Uses stdin/stdout for direct interaction
 * - Test Mode: Returns predefined answers
 *
 * This follows the Hexagonal Architecture pattern - the domain doesn't know
 * HOW the user is asked, just that it CAN ask.
 */
interface UserInteractionPort {
    /**
     * Ask the user a question (LLM-driven, flexible).
     *
     * @param question The question to ask
     * @param context Optional context information
     * @return The user's answer
     * @throws InteractionRequiredException in MCP mode (workflow should pause)
     */
    suspend fun askUser(
        question: String,
        context: Map<String, String> = emptyMap(),
    ): String

    /**
     * Ask the user a question from the approved catalog (structured, validated).
     *
     * @param questionId The ID of the question in the catalog
     * @param question The question text (from catalog)
     * @param context Optional context information
     * @return The user's answer
     * @throws InteractionRequiredException in MCP mode (workflow should pause)
     */
    suspend fun askCatalogQuestion(
        questionId: String,
        question: String,
        context: Map<String, String> = emptyMap(),
    ): String

    /**
     * Request approval from the user (explicit confirmation).
     *
     * @param question The approval request
     * @param context Optional context information
     * @return The user's decision (typically "yes"/"no" or similar)
     * @throws InteractionRequiredException in MCP mode (workflow should pause)
     */
    suspend fun requestApproval(
        question: String,
        context: Map<String, String> = emptyMap(),
    ): String

    /**
     * Create an InteractionRequest without immediately asking.
     * Used for workflows that want to prepare requests before pausing.
     */
    fun createInteractionRequest(
        question: String,
        questionId: String? = null,
        context: Map<String, String> = emptyMap(),
    ): InteractionRequest
}
