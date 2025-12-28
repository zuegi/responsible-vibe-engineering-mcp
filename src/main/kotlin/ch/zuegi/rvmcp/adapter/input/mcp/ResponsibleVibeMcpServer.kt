package ch.zuegi.rvmcp.adapter.input.mcp

import ch.zuegi.rvmcp.domain.port.input.CompletePhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.ExecuteProcessPhaseUseCase
import ch.zuegi.rvmcp.domain.port.input.StartProcessExecutionUseCase
import ch.zuegi.rvmcp.domain.port.output.MemoryRepositoryPort
import ch.zuegi.rvmcp.domain.port.output.ProcessRepositoryPort
import ch.zuegi.rvmcp.shared.rvmcpLogger
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Responsible Vibe MCP Server
 *
 * Exposes Engineering Workflows via Model Context Protocol.
 * Compatible with Claude Desktop, Warp Agent, and other MCP clients.
 *
 * Note: Not annotated with @Component to avoid auto-instantiation.
 * Should be manually created when MCP server mode is requested.
 */
class ResponsibleVibeMcpServer(
    private val startProcessUseCase: StartProcessExecutionUseCase,
    private val executePhaseUseCase: ExecuteProcessPhaseUseCase,
    private val completePhaseUseCase: CompletePhaseUseCase,
    private val memoryRepository: MemoryRepositoryPort,
    private val processRepository: ProcessRepositoryPort,
) {
    private val log by rvmcpLogger()

    // Async Job Manager for background phase execution
    private val jobManager = AsyncJobManager()

    // Coroutine scope for background job execution
    private val jobScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val server: Server =
        Server(
            serverInfo =
                Implementation(
                    name = "responsible-vibe-mcp",
                    version = "0.1.0",
                ),
            options =
                ServerOptions(
                    capabilities =
                        ServerCapabilities(
                            tools = ServerCapabilities.Tools(listChanged = true),
                        ),
                ),
        )

    /**
     * Start the MCP Server with stdio transport.
     * Blocks until the server is stopped.
     */
    suspend fun start() {
        // Register all MCP Tools
        registerTools()

        // Register all MCP Resources
        registerResources()

        // Connect server with stdio transport (using kotlinx.io Source/Sink)
        val transport =
            StdioServerTransport(
                inputStream = System.`in`.asSource().buffered(),
                outputStream = System.out.asSink().buffered(),
            )

        // Connect is a suspend function - this properly starts the async event loop
        server.connect(transport)
        log.info("MCP Server ready (v0.1.0)")
    }

    private fun registerTools() {
        // Tool 1: list_processes
        server.addTool(
            name = "list_processes",
            description = "Lists all available engineering processes (Feature Development, Bug Fix, etc.)",
            inputSchema =
                Tool.Input(
                    properties = JsonObject(emptyMap()),
                    required = emptyList(),
                ),
        ) { _ ->
            // Call domain service to list processes
            val processes = processRepository.findAll()
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            text =
                                "Found ${processes.size} engineering processes:\n" +
                                    processes.joinToString("\n") {
                                        "- ${it.id.value}: ${it.name} - ${it.description} (${it.totalPhases()} phases)"
                                    },
                        ),
                    ),
            )
        }

        // Tool 2: start_process
        server.addTool(
            name = "start_process",
            description = "Starts a new engineering process execution (requires processId, projectPath, gitBranch)",
            inputSchema =
                Tool.Input(
                    properties =
                        JsonObject(
                            mapOf(
                                "processId" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put(
                                            "description",
                                            kotlinx.serialization.json.JsonPrimitive("Process identifier (e.g., 'feature-development')"),
                                        )
                                    },
                                "projectPath" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put(
                                            "description",
                                            kotlinx.serialization.json.JsonPrimitive("Absolute path to the project directory"),
                                        )
                                    },
                                "gitBranch" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put("description", kotlinx.serialization.json.JsonPrimitive("Git branch name for this execution"))
                                    },
                            ),
                        ),
                    required = listOf("processId", "projectPath", "gitBranch"),
                ),
        ) { request: CallToolRequest ->
            try {
                val args =
                    request.arguments ?: return@addTool CallToolResult(
                        content = listOf(TextContent(text = "Error: No arguments provided")),
                        isError = true,
                    )

                val processId =
                    args["processId"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: processId parameter is required")),
                            isError = true,
                        )
                val projectPath =
                    args["projectPath"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: projectPath parameter is required")),
                            isError = true,
                        )
                val gitBranch =
                    args["gitBranch"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: gitBranch parameter is required")),
                            isError = true,
                        )

                // MCP SDK has first-class coroutine support - no runBlocking needed!
                val processExecution =
                    startProcessUseCase.execute(
                        ch.zuegi.rvmcp.domain.model.id
                            .ProcessId(processId),
                        projectPath,
                        gitBranch,
                    )

                CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                text = """Process started successfully!
Process ID: $processId
Execution ID: ${processExecution.id.value}
Project: $projectPath
Branch: $gitBranch
Current Phase: ${processExecution.currentPhase().name}
Status: ${processExecution.status}""",
                            ),
                        ),
                )
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent(text = "Error starting process: ${e.message}")),
                    isError = true,
                )
            }
        }

        // Tool 3: execute_phase
        server.addTool(
            name = "execute_phase",
            description = "Executes the current phase of an active process (requires projectPath, gitBranch)",
            inputSchema =
                Tool.Input(
                    properties =
                        JsonObject(
                            mapOf(
                                "projectPath" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put(
                                            "description",
                                            kotlinx.serialization.json.JsonPrimitive("Absolute path to the project directory"),
                                        )
                                    },
                                "gitBranch" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put("description", kotlinx.serialization.json.JsonPrimitive("Git branch name"))
                                    },
                            ),
                        ),
                    required = listOf("projectPath", "gitBranch"),
                ),
        ) { request: CallToolRequest ->
            try {
                val args =
                    request.arguments ?: return@addTool CallToolResult(
                        content = listOf(TextContent(text = "Error: No arguments provided")),
                        isError = true,
                    )

                val projectPath =
                    args["projectPath"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: projectPath parameter is required")),
                            isError = true,
                        )
                val gitBranch =
                    args["gitBranch"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: gitBranch parameter is required")),
                            isError = true,
                        )

                // Load context to get process state
                val context =
                    memoryRepository.load(projectPath, gitBranch)
                        ?: return@addTool CallToolResult(
                            content =
                                listOf(
                                    TextContent(
                                        text =
                                            "Error: No execution context found for project: $projectPath, " +
                                                "branch: $gitBranch. Start a process first.",
                                    ),
                                ),
                            isError = true,
                        )

                val processId =
                    context.processId
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: No active process found in context. Start a process first.")),
                            isError = true,
                        )

                // Reconstruct ProcessExecution from context
                val process =
                    processRepository.findById(processId)
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: Process not found: ${processId.value}")),
                            isError = true,
                        )

                val currentPhase =
                    process.phases.getOrNull(context.currentPhaseIndex)
                        ?: return@addTool CallToolResult(
                            content =
                                listOf(
                                    TextContent(
                                        text =
                                            "Error: Phase index ${context.currentPhaseIndex} out of bounds. " +
                                                "Process has ${process.totalPhases()} phases.",
                                    ),
                                ),
                            isError = true,
                        )

                // Create job for async execution
                val jobId = jobManager.createJob()
                log.info("Starting async job: $jobId for phase: ${currentPhase.name}")

                // Launch background coroutine for phase execution
                jobScope.launch {
                    try {
                        val startTime = System.currentTimeMillis()

                        // Execute phase in IO dispatcher
                        val phaseResult =
                            withContext(Dispatchers.IO) {
                                executePhaseUseCase.execute(currentPhase, context)
                            }

                        val duration = System.currentTimeMillis() - startTime
                        log.info("Job $jobId completed in ${duration}ms")

                        jobManager.completeJob(jobId, phaseResult)
                    } catch (e: Exception) {
                        log.error("! Job $jobId: Failed - ${e.message}")
                        e.printStackTrace(System.err)
                        jobManager.failJob(jobId, e.message ?: "Unknown error")
                    }
                }

                // Return immediately with job ID
                CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                text =
                                    """Phase execution started in background!
Job ID: $jobId
Phase: ${currentPhase.name}
Status: RUNNING

Use get_phase_result tool with this jobId to check status and retrieve results.
Example: get_phase_result(jobId: "$jobId")""",
                            ),
                        ),
                )
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent(text = "Error executing phase: ${e.message}")),
                    isError = true,
                )
            }
        }

        // Tool 3b: get_phase_result
        server.addTool(
            name = "get_phase_result",
            description = "Gets the result of an async phase execution (requires jobId)",
            inputSchema =
                Tool.Input(
                    properties =
                        JsonObject(
                            mapOf(
                                "jobId" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put("description", kotlinx.serialization.json.JsonPrimitive("Job ID returned from execute_phase"))
                                    },
                            ),
                        ),
                    required = listOf("jobId"),
                ),
        ) { request: CallToolRequest ->
            try {
                val args =
                    request.arguments ?: return@addTool CallToolResult(
                        content = listOf(TextContent(text = "Error: No arguments provided")),
                        isError = true,
                    )

                val jobId =
                    args["jobId"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: jobId parameter is required")),
                            isError = true,
                        )

                // Get job from manager
                val job =
                    jobManager.getJob(jobId)
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: Job not found: $jobId")),
                            isError = true,
                        )

                // Return job status and result
                when (job.status) {
                    JobStatus.RUNNING -> {
                        CallToolResult(
                            content =
                                listOf(
                                    TextContent(
                                        text =
                                            """Job is still running...
Job ID: ${job.id}
Status: RUNNING

Please wait and try again in a few seconds.""",
                                    ),
                                ),
                        )
                    }

                    JobStatus.COMPLETED -> {
                        val phaseResult = job.result!!
                        CallToolResult(
                            content =
                                listOf(
                                    TextContent(
                                        text =
                                            """Phase executed successfully!
Job ID: ${job.id}
Status: COMPLETED

Phase: ${phaseResult.phaseName}
Execution Status: ${phaseResult.status}
Summary: ${phaseResult.summary}
Vibe Checks: ${phaseResult.vibeCheckResults.size} checks (${phaseResult.vibeCheckResults.count { it.passed }} passed)
Decisions: ${phaseResult.decisions.size} architectural decisions made
Duration: ${java.time.Duration.between(phaseResult.startedAt, phaseResult.completedAt).toMillis()}ms""",
                                    ),
                                ),
                        )
                    }

                    JobStatus.FAILED -> {
                        CallToolResult(
                            content =
                                listOf(
                                    TextContent(
                                        text =
                                            """Phase execution failed!
Job ID: ${job.id}
Status: FAILED
Error: ${job.error}""",
                                    ),
                                ),
                            isError = true,
                        )
                    }
                }
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent(text = "Error getting phase result: ${e.message}")),
                    isError = true,
                )
            }
        }

        // Tool 4: complete_phase
        server.addTool(
            name = "complete_phase",
            description = "Completes the current phase and advances to next (requires projectPath, gitBranch)",
            inputSchema =
                Tool.Input(
                    properties =
                        JsonObject(
                            mapOf(
                                "projectPath" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put(
                                            "description",
                                            kotlinx.serialization.json.JsonPrimitive("Absolute path to the project directory"),
                                        )
                                    },
                                "gitBranch" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put("description", kotlinx.serialization.json.JsonPrimitive("Git branch name"))
                                    },
                            ),
                        ),
                    required = listOf("projectPath", "gitBranch"),
                ),
        ) { request: CallToolRequest ->
            try {
                val args =
                    request.arguments ?: return@addTool CallToolResult(
                        content = listOf(TextContent(text = "Error: No arguments provided")),
                        isError = true,
                    )

                val projectPath =
                    args["projectPath"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: projectPath parameter is required")),
                            isError = true,
                        )
                val gitBranch =
                    args["gitBranch"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: gitBranch parameter is required")),
                            isError = true,
                        )

                // Load context
                val context =
                    memoryRepository.load(projectPath, gitBranch)
                        ?: return@addTool CallToolResult(
                            content =
                                listOf(
                                    TextContent(text = "Error: No execution context found for project: $projectPath, branch: $gitBranch"),
                                ),
                            isError = true,
                        )

                val processId =
                    context.processId
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: No active process found in context")),
                            isError = true,
                        )

                // Get last phase result from context
                val lastPhaseResult =
                    context.phaseHistory.lastOrNull()
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: No phase has been executed yet. Run execute_phase first.")),
                            isError = true,
                        )

                // Complete the phase via Use Case - direct suspend call
                val updatedExecution =
                    completePhaseUseCase.execute(
                        context.executionId,
                        lastPhaseResult,
                    )

                // Load updated context to check progress
                val updatedContext = memoryRepository.load(projectPath, gitBranch)!!
                val process = processRepository.findById(processId)!!

                val isCompleted =
                    updatedExecution.status == ch.zuegi.rvmcp.domain.model.status.ExecutionStatus.COMPLETED
                val nextPhase =
                    if (!isCompleted) updatedExecution.currentPhase().name else "None (all phases completed)"

                CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                text = """Phase completed successfully!
Completed Phase: ${lastPhaseResult.phaseName}
Process Status: ${updatedExecution.status}
Current Phase Index: ${updatedExecution.currentPhaseIndex}
Next Phase: $nextPhase
Total Phases: ${process.totalPhases()}
Phases Completed: ${updatedContext.phaseHistory.size}""",
                            ),
                        ),
                )
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent(text = "Error completing phase: ${e.message}")),
                    isError = true,
                )
            }
        }

        // Tool 5: get_context
        server.addTool(
            name = "get_context",
            description = "Retrieves stored memory/context for a process execution (requires projectPath, gitBranch)",
            inputSchema =
                Tool.Input(
                    properties =
                        JsonObject(
                            mapOf(
                                "projectPath" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put(
                                            "description",
                                            kotlinx.serialization.json.JsonPrimitive("Absolute path to the project directory"),
                                        )
                                    },
                                "gitBranch" to
                                    kotlinx.serialization.json.buildJsonObject {
                                        put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                                        put("description", kotlinx.serialization.json.JsonPrimitive("Git branch name"))
                                    },
                            ),
                        ),
                    required = listOf("projectPath", "gitBranch"),
                ),
        ) { request: CallToolRequest ->
            try {
                val args =
                    request.arguments ?: return@addTool CallToolResult(
                        content = listOf(TextContent(text = "Error: No arguments provided")),
                        isError = true,
                    )

                val projectPath =
                    args["projectPath"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: projectPath parameter is required")),
                            isError = true,
                        )
                val gitBranch =
                    args["gitBranch"]?.jsonPrimitive?.content
                        ?: return@addTool CallToolResult(
                            content = listOf(TextContent(text = "Error: gitBranch parameter is required")),
                            isError = true,
                        )

                val context = memoryRepository.load(projectPath, gitBranch)
                if (context == null) {
                    CallToolResult(
                        content = listOf(TextContent(text = "No context found for project: $projectPath, branch: $gitBranch")),
                    )
                } else {
                    CallToolResult(
                        content =
                            listOf(
                                TextContent(
                                    text = """Execution Context Found:
Project: ${context.projectPath}
Branch: ${context.gitBranch}
Execution ID: ${context.executionId.value}
Phase History: ${context.phaseHistory.size} phases completed
Architectural Decisions: ${context.architecturalDecisions.size}
Interactions: ${context.interactions.size}
Artifacts: ${context.artifacts.size}""",
                                ),
                            ),
                    )
                }
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent(text = "Error retrieving context: ${e.message}")),
                    isError = true,
                )
            }
        }
    }

    private fun registerResources() {
        // TODO: Study MCP SDK samples to understand correct Resource registration API
        // - How to register resource templates
        // - How to handle resource requests
        // - How to access request parameters
    }
}
