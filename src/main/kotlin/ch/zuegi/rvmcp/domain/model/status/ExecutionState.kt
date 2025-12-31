package ch.zuegi.rvmcp.domain.model.status

/**
 * State of a workflow execution.
 *
 * Represents the lifecycle of a ProcessExecution:
 * - RUNNING: Workflow is actively executing
 * - AWAITING_INPUT: Workflow is paused, waiting for user interaction
 * - COMPLETED: Workflow finished successfully
 * - FAILED: Workflow encountered an error and stopped
 */
enum class ExecutionState {
    /**
     * Workflow is actively executing.
     */
    RUNNING,

    /**
     * Workflow is paused, waiting for user interaction.
     * Can be resumed with provide_answer.
     */
    AWAITING_INPUT,

    /**
     * Workflow finished successfully.
     */
    COMPLETED,

    /**
     * Workflow encountered an error and stopped.
     */
    FAILED,
}
