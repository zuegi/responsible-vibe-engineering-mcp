# Architektur-Umbau Plan: Koog Workflow Integration

## Problem

**Aktuell:**
- Wir erstellen **pro LLM-Node einen neuen Agent**
- Jeder Agent hat **keinen Kontext** von vorherigen Nodes
- Das widerspricht dem Konzept eines zusammenhängenden Workflows
- Performance: 4 LLM-Nodes = 40s (10s pro Node)

**Ziel:**
- **Ein Agent** für den gesamten Workflow
- Agent behält Kontext über alle Nodes
- YAML-Workflow wird in Koog-Strategy-Graph übersetzt

---

## Konzeptioneller Ansatz

### Aktuelles Modell (FALSCH)
```
Workflow "Requirements Analysis"
  → Node 1 (LLM) → Agent 1 erstellen → LLM Call → Vergessen
  → Node 2 (LLM) → Agent 2 erstellen → LLM Call → Vergessen  ❌ Kein Kontext!
  → Node 3 (LLM) → Agent 3 erstellen → LLM Call → Vergessen  ❌ Kein Kontext!
```

### Neues Modell (RICHTIG)
```
Workflow "Requirements Analysis"
  → Agent erstellen mit YAML-basierter Koog-Strategy
  → Agent.run("Start workflow")
     → Koog führt intern alle Nodes aus
     → Behält Kontext über Conversation History
     → LLM sieht alle vorherigen Messages
  → Result mit vollständigem Kontext  ✅
```

---

## Technischer Plan

### Phase 1: YAML zu Koog Strategy übersetzen ✨

**Aufgabe**: YAML Workflow Nodes → Koog Strategy Graph

**Konzept**:
```kotlin
fun yamlToKoogStrategy(workflow: WorkflowTemplate): Strategy<String, String> {
    return strategy<String, String>("yaml-${workflow.name}") {
        // LLM Nodes als nodeL LLMRequest
        workflow.nodes.filter { it.type == NodeType.LLM }.forEach { node ->
            val nodeId by nodeLLMRequest(node.id, expandableChat = true)
        }
        
        // Edges als Koog-Graph-Edges
        workflow.graph.edges.forEach { edge ->
            edge(findNode(edge.from) forwardTo findNode(edge.to) onAssistantMessage { true })
        }
    }
}
```

**Beispiel**: YAML
```yaml
nodes:
  - id: gather_requirements
    type: llm
    prompt: "Sammle Anforderungen..."
  - id: analyze_architecture
    type: llm
    prompt: "Analysiere Architektur..."
    
graph:
  edges:
    - from: gather_requirements
      to: analyze_architecture
```

**Wird zu**: Koog Strategy
```kotlin
strategy<String, String>("requirements-analysis") {
    val gatherReqs by nodeLLMRequest("gather_requirements", expandableChat = true)
    val analyzeArch by nodeLLMRequest("analyze_architecture", expandableChat = true)
    
    edge(nodeStart forwardTo gatherReqs)
    edge(gatherReqs forwardTo analyzeArch onAssistantMessage { true })
    edge(analyzeArch forwardTo nodeFinish onAssistantMessage { true })
}
```

---

### Phase 2: Prompt-Engineering für Multi-Node Workflows

**Problem**: Koog Agent braucht ein **System-Prompt**, das den gesamten Workflow beschreibt.

**Lösung**: Multi-Node-System-Prompt
```kotlin
fun buildWorkflowSystemPrompt(workflow: WorkflowTemplate): String {
    return """
    You are an AI assistant executing a structured workflow: ${workflow.name}
    
    **Workflow Steps:**
    ${workflow.nodes.filter { it.type == NodeType.LLM }.mapIndexed { i, node ->
        "${i+1}. ${node.id}: ${node.description}"
    }.joinToString("\n")}
    
    **Instructions:**
    - Execute each step sequentially
    - Use the output of previous steps as context for the next
    - Ask clarifying questions when needed
    - Document your reasoning
    
    **Current Project Context:**
    - Path: {{context.project_path}}
    - Branch: {{context.git_branch}}
    
    Begin with step 1: ${workflow.nodes.first().id}
    """.trimIndent()
}
```

**Oder**: Node-spezifische Prompts dynamisch injizieren
```kotlin
// Koog kann Prompts während der Ausführung anpassen
edge(gatherReqs forwardTo analyzeArch onAssistantMessage { message ->
    // Inject next node's prompt into conversation
    injectUserMessage(workflow.findNode("analyze_architecture").prompt)
    true
})
```

---

### Phase 3: Conditional & Human-Interaction Nodes

**Challenge**: YAML hat `conditional` und `human_interaction` Nodes - Koog hat das nicht direkt.

**Lösung 1: Tool-based**
```kotlin
// Conditional als Koog Tool
val checkConditionTool = tool("check_condition") {
    description = "Evaluate a condition"
    parameter("condition", "Condition to evaluate")
    
    execute { params ->
        evaluateCondition(params["condition"] as String)
    }
}

// Human-Interaction als Koog Tool
val askHumanTool = tool("ask_human") {
    description = "Ask human for input"
    parameter("question", "Question to ask")
    
    execute { params ->
        println(params["question"])
        readlnOrNull() ?: ""
    }
}
```

**Lösung 2: Strategy mit Branches**
```kotlin
edge(checkAmbiguities forwardTo requestClarification onAssistantMessage { msg ->
    evaluateCondition(msg.content)  // true = go to clarification
})

edge(checkAmbiguities forwardTo analyzeArchitecture onAssistantMessage { msg ->
    !evaluateCondition(msg.content)  // false = skip clarification
})
```

---

### Phase 4: Implementierung

**Schritt 1: YamlToKoogStrategyTranslator**
```kotlin
class YamlToKoogStrategyTranslator {
    fun translate(workflow: WorkflowTemplate): Strategy<String, String> {
        // Convert YAML workflow to Koog strategy graph
    }
    
    private fun createLLMNode(node: WorkflowNode): NodeLLMRequest { ... }
    private fun createConditionalEdge(edge: WorkflowEdge): Edge { ... }
}
```

**Schritt 2: Workflow Executor umbauen**
```kotlin
class KoogWorkflowExecutor(...) {
    override fun executeWorkflow(template: String, context: ExecutionContext): WorkflowExecutionResult {
        val workflowTemplate = templateParser.parseTemplate(template)
        
        // NEW: Translate YAML to Koog Strategy
        val strategy = yamlStrategyTranslator.translate(workflowTemplate)
        
        // NEW: Create ONE agent for entire workflow
        val agent = AIAgent(
            promptExecutor = azureExecutor,
            strategy = strategy,  // YAML-derived strategy
            agentConfig = AIAgentConfig(
                prompt = buildWorkflowSystemPrompt(workflowTemplate, context),
                model = OpenAIModels.Chat.GPT4o,
                maxAgentIterations = workflowTemplate.nodes.size * 2,  // Genug für alle Nodes
            ),
            toolRegistry = ToolRegistry { 
                // Register conditional & human-interaction tools
            }
        )
        
        // NEW: Single agent run for entire workflow
        val result = agent.run("Execute workflow")
        
        return WorkflowExecutionResult(...)
    }
}
```

---

## Vorteile

✅ **Kontext bleibt erhalten**: LLM sieht alle vorherigen Messages  
✅ **Performance**: 1 Agent-Erstellung statt 4-6  
✅ **Semantisch korrekt**: Ein Workflow = Ein Agent-Run  
✅ **Koog-native**: Nutzt Koog's Stärken (Strategy, Conversation History)  
✅ **Flexibel**: Neue Node-Types einfach als Koog-Tools hinzufügen

---

## Risiken & Mitigations

**Risiko 1**: YAML → Koog Translation komplex  
→ **Mitigation**: Schrittweise, beginnend mit LLM-Nodes only

**Risiko 2**: Koog maxAgentIterations reicht nicht  
→ **Mitigation**: Dynamisch basierend auf Workflow-Größe berechnen

**Risiko 3**: Conditional/Human-Interaction nicht 1:1 abbildbar  
→ **Mitigation**: Hybrid-Ansatz mit Tools + Custom Logic

---

## Timeline (Schätzung)

1. **YamlToKoogStrategyTranslator** (LLM nodes only): 4-6h
2. **System-Prompt Engineering**: 2-3h
3. **Executor Refactoring**: 3-4h
4. **Conditional/Human Tools**: 2-3h
5. **Testing & Debugging**: 4-6h

**Total**: ~15-22h (2-3 Arbeitstage)

---

## Nächste Schritte

1. ✅ Quick Fix: Simple Test Workflow (1 LLM Node) - **DONE** (1.3s)
2. ✅ Implementiere YamlToKoogStrategyTranslator (nur LLM nodes) - **DONE** (1-3 nodes)
3. ✅ Test mit Multi-Node workflows - **DONE** (2-node: 1.8s, 3-node: 2.7s)
4. ✅ Context-Preservation Verification - **DONE** (Secret code, City-Landmark chain)
5. ✅ Performance-Messung - **DONE** (11x Speedup vs. alt)
6. ⏳ Erweitere auf beliebig viele LLM-Nodes
7. ⏳ Erweitere um Conditional/Human-Interaction

## Ergebnisse

### Performance (Gemessen)
| Test | Nodes | Duration | Avg/Node | vs. Alt (10s/node) |
|------|-------|----------|----------|--------------------|
| Simple | 1 | 1.3s | 1.3s | **7.7x faster** |
| Multi-Node | 2 | 1.8s | 900ms | **11x faster** |
| Three-Node | 3 | 2.7s | 900ms | **11x faster** |

### Context-Preservation (Verified)
- ✅ **Secret Code Test**: Agent remembered 4-digit code "8473" from step 1 in step 2
- ✅ **City-Landmark Chain**: Agent correctly used "Paris" from step 1 to name "Eiffel Tower" in step 2
- ✅ **3-Node Summary**: Agent summarized complete chain: City=Paris, Landmark=Eiffel Tower

### Test Coverage
- ✅ 54 Tests passing (36 domain + 6 integration + 12 others)
- ✅ SimpleLLMConnectionTest: 693ms response time
- ✅ KoogIntegrationTest: 6 scenarios including multi-node chains

---

## Fragen für Diskussion

- Sollen wir YAML überhaupt behalten oder direkt Koog-Strategien schreiben?
- Ist die Tool-based Lösung für Conditionals gut genug?
- Wie gehen wir mit Complex Workflows um (>10 Nodes)?
