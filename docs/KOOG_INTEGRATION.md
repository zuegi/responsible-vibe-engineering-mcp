# Kotlin Koog Integration Guide

## Overview

Responsible Vibe MCP uses **Kotlin Koog 0.6.0** as its LLM orchestration layer. Koog provides intelligent conversation management, history compression, and multi-model support.

## Current Status

✅ **Fully Integrated**: Complete Koog integration with Azure OpenAI  
✅ **Production Ready**: Real LLM execution via Koog agents  
✅ **Tool Support**: SimpleTool API with ask_user, create_file, get_question

## Architecture

```
┌─────────────────────────────────────────────┐
│ YAML Workflow Template                      │
│ (requirements-analysis.yml)                 │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│ WorkflowTemplateParser                      │
│ - Parses YAML                               │
│ - Validates structure                       │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│ KoogWorkflowExecutor                        │
│ - Creates AIAgent with real LLM executor   │
│ - Executes workflow via agent.run()        │
│ - Tool Registry (ask_user, create_file)    │
│ - InteractionContextElement for pause/resume│
│                                             │
│ ┌─────────────────────────────────────────┐ │
│ │ Real Koog Integration:                  │ │
│ │ - simpleAzureOpenAIExecutor             │ │
│ │ - AIAgent with strategy                 │ │
│ │ - Tool Registry with real tools         │ │
│ │ - CoroutineContext-based interaction    │ │
│ └─────────────────────────────────────────┘ │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│ Azure OpenAI Gateway                        │
│ - GPT-4o model                              │
│ - Streaming responses                       │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│ WorkflowExecutionResult                     │
│ - Summary from LLM                          │
│ - Decisions extracted                       │
│ - Vibe Check Results                        │
└─────────────────────────────────────────────┘
```

## Configuration

### application.yml (Production Config)

```yaml
llm:
  provider: azure-openai
  base-url: https://your-gateway.example.com/openai/deployments/gpt-4o/
  api-version: 2024-05-01-preview
  api-token: ${LLM_API_TOKEN}
```

### application-local.yml (Local Development)

```yaml
llm:
  provider: azure-openai
  base-url: https://your-gateway.example.com/...
  api-version: 2024-05-01-preview
  api-token: your-token-here
```

**Note:** Uses `llm:` prefix (not `koog:`), configured via `LlmProperties` in Spring Boot.

## Implementation Details

### Real Koog Agent Creation

The `KoogWorkflowExecutor` creates a real Koog agent with Azure OpenAI:

```kotlin
class KoogWorkflowExecutor(
    private val llmProperties: LlmProperties,
    private val userInteractionPort: UserInteractionPort,
) : WorkflowExecutionPort {
    
    // Real LLM executor (lazy for performance)
    private val llmExecutor by lazy {
        simpleAzureOpenAIExecutor(
            baseUrl = llmProperties.baseUrl,
            version = AzureOpenAIServiceVersion.fromString(llmProperties.apiVersion),
            apiToken = llmProperties.apiToken,
        )
    }
    
    override suspend fun executeWorkflow(
        template: String,
        context: ExecutionContext,
    ): WorkflowExecutionResult {
        // Parse YAML template
        val workflowTemplate = templateParser.parseTemplate(template)
        
        // Translate to Koog Strategy
        val strategy = strategyTranslator.translate(workflowTemplate)
        
        // Create InteractionContext for pause/resume
        val interactionContext = InteractionContextElement(
            executionId = context.executionId.value,
        )
        
        // Create Koog Agent with REAL LLM
        val agent = AIAgent<String, String>(
            promptExecutor = llmExecutor,  // Real Azure OpenAI!
            strategy = strategy,
            agentConfig = AIAgentConfig(
                prompt = prompt("workflow_${workflowTemplate.name}") {
                    system(systemPrompt)
                },
                model = OpenAIModels.Chat.GPT4o,
                maxAgentIterations = 10 + (llmNodeCount * 20),
            ),
            toolRegistry = ToolRegistry {
                tool(AskUserTool(userInteractionPort, interactionContext))
                tool(CreateFileTool { context.projectPath })
                tool(QuestionCatalogTool(QuestionCatalog.fromFile("...")))
            },
            installFeatures = { install(Tracing) },
        )
        
        // Execute workflow with real LLM
        val agentResponse = withContext(interactionContext) {
            agent.run(initialPrompt)
        }
        
        // Check if paused for user interaction
        if (interactionContext.hasRequest()) {
            return WorkflowExecutionResult(
                awaitingInput = true,
                interactionRequest = interactionContext.consumeRequest(),
                // ...
            )
        }
        
        // Extract results and return
        return WorkflowExecutionResult(
            success = true,
            summary = generateSummary(workflowTemplate, context, agentResponse),
            decisions = extractDecisions(workflowTemplate, agentResponse),
            // ...
        )
    }
}
```

**Key Points:**
- ✅ No console simulation - real LLM calls!
- ✅ `simpleAzureOpenAIExecutor` connects to Azure OpenAI
- ✅ `AIAgent` executes workflows with GPT-4o
- ✅ Tool Registry registers real tools
- ✅ InteractionContextElement enables workflow pause/resume

## Tool Integration (Koog 0.6.0 API)

### SimpleTool Constructor-Based API

Koog 0.6.0 changed from property-based to constructor-based tool definition:

```kotlin
// Koog 0.6.0 API
class AskUserTool(
    private val userInteractionPort: UserInteractionPort,
    private val interactionContext: InteractionContextElement? = null,
) : SimpleTool<AskUserTool.Args>(
    argsSerializer = serializer<Args>(),
    name = "ask_user",
    description = "Ask the user a question and wait for their response.",
) {
    @Serializable
    data class Args(val question: String)
    
    override suspend fun execute(args: Args): String {
        return userInteractionPort.askUser(
            question = args.question,
            context = emptyMap(),
        )
    }
}
```

**Changes from 0.5.1:**
- Constructor parameters instead of property overrides
- `execute()` method instead of `doExecute()`
- No `descriptor` needed (simplified API)

### Registered Tools

The `KoogWorkflowExecutor` registers these tools:

1. **ask_user** - User interaction (pause/resume capable)
2. **create_file** - File creation in project
3. **get_question** - Question catalog access

## User Interaction Flow

### Pause/Resume Mechanism

Uses CoroutineContext for thread-safe interaction signaling:

```kotlin
// 1. Create InteractionContext
val interactionContext = InteractionContextElement(executionId)

// 2. Execute workflow within context
withContext(interactionContext) {
    agent.run(initialPrompt)
}

// 3. Check if paused
if (interactionContext.hasRequest()) {
    val request = interactionContext.consumeRequest()
    return WorkflowExecutionResult(
        awaitingInput = true,
        interactionRequest = request,
    )
}
```

**Tools signal interruption via:**
```kotlin
// In McpAwareInteractionAdapter
override suspend fun askUser(question: String, context: Map<String, String>): String {
    val request = InteractionRequest(
        type = InteractionType.ASK_USER,
        question = question,
    )
    currentCoroutineContext()[InteractionContextElement]?.setRequest(request)
    return "[Awaiting user input: $question]"
}
```

See [ADR-0001](adr/0001-coroutine-context-for-workflow-interruption.md) for detailed rationale.

## Performance

### Single-Agent Architecture

**Old Approach (multi-agent):**
- Created new agent per workflow node
- Lost conversation context between nodes
- Slow (~30s+ per workflow)

**New Approach (single-agent):**
- One agent for entire workflow
- Preserves full conversation history
- Fast (~2-3s per workflow)
- **11x performance improvement**

### Lazy LLM Executor

The executor is lazy-initialized for performance:
```kotlin
private val llmExecutor by lazy {
    simpleAzureOpenAIExecutor(...)
}
```

Only created once per application lifecycle, reused across workflows.

## Testing

### LLM Integration Tests

```bash
# With LLM (requires application-local.yml)
mvn test -Dtest=KoogIntegrationTest

# Without LLM (unit tests only)
mvn test -Dtest='!KoogIntegrationTest,!SimpleLLMConnectionTest'
```

### Test Setup

```kotlin
@SpringBootTest
@ActiveProfiles("test")
class KoogIntegrationTest {
    @Autowired
    private lateinit var llmProperties: LlmProperties
    
    @Test
    fun testRealLLMConnection() = runBlocking {
        val executor = KoogWorkflowExecutor(llmProperties, mockUserPort)
        val result = executor.executeWorkflow("simple-test.yml", context)
        
        assertThat(result.success).isTrue()
        assertThat(result.summary).isNotEmpty()
    }
}
```

## Troubleshooting

### Common Issues

**1. LLM Connection Failed**
```
Error: Failed to connect to Azure OpenAI
```
→ Check `application-local.yml` configuration  
→ Verify `base-url` and `api-token`

**2. Agent Timeout**
```
Error: Agent execution timeout
```
→ Increase `maxAgentIterations` in `AIAgentConfig`  
→ Check if workflow has too many LLM nodes

**3. Tool Not Found**
```
Error: Tool 'ask_user' not found
```
→ Verify tool is registered in `ToolRegistry`  
→ Check tool name matches exactly

### Debug Logging

Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    ch.zuegi.rvmcp.adapter.output.workflow: DEBUG
    ai.koog: DEBUG
```

## Migration Notes

### From 0.5.1 to 0.6.0

See [KOOG_MIGRATION_0.5.1_to_0.6.0.md](../KOOG_MIGRATION_0.5.1_to_0.6.0.md) for detailed migration guide.

**Key Changes:**
- SimpleTool API: Constructor-based instead of property-based
- `execute()` instead of `doExecute()`
- No `descriptor` property needed

## Related Documentation

- [ADR-0001: CoroutineContext for Workflow Interruption](adr/0001-coroutine-context-for-workflow-interruption.md)
- [Configuration Guide](CONFIGURATION.md)
- [MCP Async Solution](MCP_ASYNC_SOLUTION.md)

---

**Last Updated:** January 3, 2026  
**Koog Version:** 0.6.0  
**Status:** ✅ Production Ready

