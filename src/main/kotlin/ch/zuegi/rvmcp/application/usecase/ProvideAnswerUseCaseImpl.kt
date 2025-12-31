package ch.zuegi.rvmcp.application.usecase

import ch.zuegi.rvmcp.domain.model.id.ExecutionId
import ch.zuegi.rvmcp.domain.model.process.ProcessExecution
import ch.zuegi.rvmcp.domain.port.input.ProvideAnswerUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import org.springframework.stereotype.Service

/**
 * Implementation of ProvideAnswerUseCase.
 *
 * Resumes a paused workflow execution by:
 * 1. Loading the execution from memory
 * 2. Validating it's in AWAITING_INPUT state
 * 3. Recording the user's answer
 * 4. Updating state to RUNNING
 * 5. Persisting the updated execution
 *
 * The actual workflow execution continuation happens in ExecuteProcessPhaseUseCase.
 */
@Service
class ProvideAnswerUseCaseImpl(
    private val memoryRepository: MemoryRepositoryPort,
) : ProvideAnswerUseCase {
    private val logger by rvmcpLogger()

    override suspend fun execute(
        executionId: ExecutionId,
        answer: String,
    ): ProcessExecution {
        logger.info("Providing answer for execution: ${executionId.value}")

        // Load execution context
        val context =
            memoryRepository.findByExecutionId(executionId)
                ?: throw NoSuchElementException("Execution not found: ${executionId.value}")

        // Get current execution state
        val execution =
            context.currentExecution
                ?: throw IllegalStateException("No active execution found in context")

        logger.info("Current state: ${execution.state}, awaiting input: ${execution.isAwaitingInput()}")

        // Validate state
        require(execution.isAwaitingInput()) {
            "Execution ${executionId.value} is not awaiting input (state: ${execution.state})"
        }

        // Resume with answer
        val resumedExecution = execution.resumeWithAnswer(answer)
        logger.info("Execution resumed, interaction history size: ${resumedExecution.interactionHistory.size}")

        // Update context with resumed execution
        val updatedContext = context.copy(currentExecution = resumedExecution)
        memoryRepository.save(updatedContext)

        logger.info("Answer recorded and execution resumed")
        return resumedExecution
    }
}
