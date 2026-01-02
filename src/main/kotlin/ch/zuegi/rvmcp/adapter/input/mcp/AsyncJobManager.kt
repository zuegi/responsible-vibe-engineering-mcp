package ch.zuegi.rvmcp.adapter.input.mcp

import ch.zuegi.rvmcp.domain.model.interaction.InteractionRequest
import ch.zuegi.rvmcp.domain.model.phase.PhaseResult
import java.util.concurrent.ConcurrentHashMap

/**
 * Async Job Manager for background phase execution.
 *
 * Manages long-running phase executions (e.g. LLM workflows) that cannot block MCP tool handlers.
 */
class AsyncJobManager {
    private val jobs = ConcurrentHashMap<String, AsyncJob>()

    /**
     * Creates a new job and returns its ID.
     */
    fun createJob(): String {
        val jobId = "job-${System.currentTimeMillis()}-${(1000..9999).random()}"
        jobs[jobId] =
            AsyncJob(
                id = jobId,
                status = JobStatus.RUNNING,
                result = null,
                error = null,
            )
        return jobId
    }

    /**
     * Marks a job as completed with result.
     */
    fun completeJob(
        jobId: String,
        result: PhaseResult,
    ) {
        jobs[jobId] =
            AsyncJob(
                id = jobId,
                status = JobStatus.COMPLETED,
                result = result,
                error = null,
            )
    }

    /**
     * Marks a job as awaiting user input (paused for interaction).
     */
    fun pauseJobForInput(
        jobId: String,
        interactionRequest: InteractionRequest,
    ) {
        jobs[jobId] =
            AsyncJob(
                id = jobId,
                status = JobStatus.AWAITING_INPUT,
                result = null,
                error = null,
                interactionRequest = interactionRequest,
            )
    }

    /**
     * Marks a job as failed with error.
     */
    fun failJob(
        jobId: String,
        error: String,
    ) {
        jobs[jobId] =
            AsyncJob(
                id = jobId,
                status = JobStatus.FAILED,
                result = null,
                error = error,
            )
    }

    /**
     * Gets job status and result.
     */
    fun getJob(jobId: String): AsyncJob? {
        return jobs[jobId]
    }

    /**
     * Lists all jobs (for debugging).
     */
    fun listJobs(): List<AsyncJob> {
        return jobs.values.toList()
    }
}

/**
 * Job status.
 */
enum class JobStatus {
    RUNNING,
    AWAITING_INPUT,
    COMPLETED,
    FAILED,
}

/**
 * Async job.
 */
data class AsyncJob(
    val id: String,
    val status: JobStatus,
    val result: PhaseResult? = null,
    val error: String? = null,
    val interactionRequest: InteractionRequest? = null,
)
