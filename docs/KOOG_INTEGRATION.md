# Kotlin Koog Integration Guide

## Overview

Responsible Vibe MCP uses **Kotlin Koog** as its LLM orchestration layer. Koog provides intelligent conversation management, history compression, and multi-model support.

## Current Status

✅ **Framework Ready**: Workflow execution framework fully implemented  
⚠️ **LLM Integration**: Console-based simulation (real Koog integration pending)

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
│ - Executes node graph                       │
│ - Manages execution state                   │
│ - Variable interpolation                    │
│                                             │
│ ┌─────────────────────────────────────────┐ │
│ │ Node Handlers:                          │ │
│ │ - LLM Node → Koog Model API (TODO)      │ │
│ │ - Conditional → Expression Evaluator    │ │
│ │ - Human Interaction → Console Input     │ │
│ │ - Aggregation → State Collection        │ │
│ │ - System Command → Process Execution    │ │
│ └─────────────────────────────────────────┘ │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│ WorkflowExecutionResult                     │
│ - Summary                                   │
│ - Decisions                                 │
│ - Vibe Check Results                        │
└─────────────────────────────────────────────┘
```

## Configuration

### application.yml

```yaml
koog:
  model:
    provider: ANTHROPIC
    name: claude-3-5-sonnet-20241022
    temperature: 0.7
    max-tokens: 4096
```

### Environment Variables

```bash
# Anthropic (Claude)
export ANTHROPIC_API_KEY=sk-ant-...

# OpenAI (GPT)
export OPENAI_API_KEY=sk-...

# OpenRouter (Multi-Model)
export OPENROUTER_API_KEY=sk-or-...
```

## Implementation Steps (TODO)

### Step 1: Koog Model Integration

Replace console simulation in `KoogWorkflowExecutor.executeLLMNode()`:

**Current (Console-based)**:
```kotlin
private fun executeLLMNode(...): String {
    val prompt = interpolateVariables(node.prompt!!, context)
    
    // Console simulation
    print("Enter simulated LLM response: ")
    val response = readlnOrNull() ?: "Default response"
    
    executionState[node.output!!] = response
    return findNextNode(node.id, template)
}
```

**Target (Koog-based)**:
```kotlin
private fun executeLLMNode(..., model: ai.koog.model.Model): String {
    val prompt = interpolateVariables(node.prompt!!, context)
    
    // Real LLM call via Koog
    val response = model.invoke(prompt)
    
    executionState[node.output!!] = response
    return findNextNode(node.id, template)
}
```

### Step 2: Model Initialization

Create Koog Model instance at workflow start:

```kotlin
private fun createKoogModel(template: WorkflowTemplate, context: ExecutionContext): Model {
    val systemPrompt = """
        You are an AI assistant executing: ${template.name}
        
        Project: ${context.projectPath}
        Branch: ${context.gitBranch}
        
        Your role: ${template.description}
    """.trimIndent()
    
    return Model(
        provider = ModelProvider.ANTHROPIC,
        name = "claude-3-5-sonnet-20241022",
        systemPrompt = systemPrompt,
        temperature = 0.7
    )
}
```

### Step 3: Conversation History

Leverage Koog's intelligent history compression:

```kotlin
// Koog automatically manages conversation history
// Each model.invoke() call maintains context
val response1 = model.invoke("Analyze requirements")
val response2 = model.invoke("Now identify edge cases") // Has context from response1
```

### Step 4: Streaming Responses (Optional)

For better UX with long-running LLM calls:

```kotlin
model.invokeStreaming(prompt) { chunk ->
    print(chunk)
    // Update UI in real-time
}
```

### Step 5: Multi-Model Support

Support different models for different phases:

```kotlin
val requirementsModel = Model(provider = ANTHROPIC, name = "claude-3-5-sonnet")
val codeGenModel = Model(provider = OPENAI, name = "gpt-4-turbo")

when (phase) {
    "Requirements Analysis" -> requirementsModel.invoke(prompt)
    "Implementation" -> codeGenModel.invoke(prompt)
}
```

## Testing

### Without API Keys (Console Mode)

```bash
mvn test
# Uses console-based simulation
```

### With API Keys (Real LLM)

```bash
export ANTHROPIC_API_KEY=sk-ant-...
mvn test -Dkoog.enable.real.llm=true
```

## Dependencies

Already configured in `pom.xml`:

```xml
<dependency>
    <groupId>ai.koog</groupId>
    <artifactId>koog-spring-boot-starter</artifactId>
    <version>0.5.1</version>
</dependency>
```

## Performance Considerations

### Token Usage

Koog's intelligent compression reduces token costs:
- Compresses conversation history automatically
- Maintains semantic meaning
- Reduces redundancy

**Estimate** (per workflow phase):
- Input tokens: ~2,000 (system prompt + context)
- Output tokens: ~1,500 (structured analysis)
- **Total per phase**: ~3,500 tokens
- **Cost (Claude Sonnet)**: ~$0.04 per phase

### Caching

Spring Boot caches:
- YAML templates (parsed once)
- Koog Model instances (reused per session)

## Security

### API Key Management

**❌ Never** commit API keys to Git:
```yaml
# Bad
koog:
  api-key: sk-ant-12345...
```

**✅ Use** environment variables:
```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

### Prompt Injection Protection

Koog provides built-in safeguards:
- System prompt isolation
- Input sanitization
- Output validation

## Troubleshooting

### Model Not Found

```
Error: Unresolved reference 'Model'
```

**Solution**: Ensure Koog dependency is loaded:
```bash
mvn clean compile
```

### API Key Not Set

```
Error: ANTHROPIC_API_KEY environment variable not set
```

**Solution**: Export API key before running:
```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

### Rate Limiting

If hitting API rate limits, adjust:

```yaml
koog:
  rate-limit:
    requests-per-minute: 50
    retry-strategy: exponential-backoff
```

## Next Steps

1. **Implement Real LLM Integration** (Phase 1.5 - Part 3)
   - Replace console simulation with Koog API
   - Add streaming support
   - Implement error handling

2. **Add Advanced Features** (Phase 2)
   - Multi-model orchestration
   - Parallel LLM calls
   - Context window management

3. **Production Readiness** (Phase 3)
   - Monitoring & observability
   - Cost tracking
   - Performance optimization

## References

- [Kotlin Koog Documentation](https://github.com/koogio/koog)
- [Responsible Vibe MCP Architecture](../WARP.md)
- [Workflow Templates](../src/main/resources/workflows/)
