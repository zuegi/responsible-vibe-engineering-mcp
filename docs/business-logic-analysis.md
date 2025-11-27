# Business Logic Analyse: Responsible Vibe Engineering MCP

**Datum**: 2025-11-16  
**Zweck**: Vollständige Analyse der Business Logic, User-Interaktionen und Workflow-Execution

---

## 📋 Inhaltsverzeichnis

1. [Executive Summary](#executive-summary)
2. [Architektur-Überblick](#architektur-überblick)
3. [Business Logic Flow](#business-logic-flow)
4. [User-Interaktions-Punkte](#user-interaktions-punkte)
5. [Workflow-Execution: YAML vs. Manual](#workflow-execution-yaml-vs-manual)
6. [Identifizierte Probleme](#identifizierte-probleme)
7. [Test-Strategie](#test-strategie)
8. [Lösungsempfehlungen](#lösungsempfehlungen)

---

## Executive Summary

### Kernproblem
Die aktuelle Implementierung hat **zwei parallele Workflow-Systeme**, die nicht kohärent zusammenarbeiten:

1. **YAML-basierte Workflows** (requirements-analysis.yml, architecture-design.yml, implementation.yml)
   - Komplex, mit 7+ Nodes, Conditional Logic, Human-Interaction
   - Werden von `KoogWorkflowExecutor` ausgeführt (mit LLM)
   
2. **ManualWorkflowExecutor** (für Tests)
   - Ignoriert YAML-Workflows komplett
   - Hardcoded Steps ohne echte User-Interaktion
   - Simuliert keine realistische Workflow-Execution

**Konsequenz**: Tests prüfen nicht die echte Business Logic, sondern eine vereinfachte Dummy-Variante.

### Kernaussage
> **Die Business Logic (Services) ist korrekt implementiert.**  
> **Die Test-Adapter (ManualWorkflowExecutor) bilden nicht die Realität ab.**

---

## Architektur-Überblick

### Hexagonal Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer (Core)                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Domain Services (Business Logic)                    │  │
│  │  • StartProcessExecutionService                      │  │
│  │  • ExecuteProcessPhaseService                        │  │
│  │  • CompletePhaseService                              │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Port Interfaces (Contracts)                         │  │
│  │  • WorkflowExecutionPort                             │  │
│  │  • VibeCheckEvaluatorPort                            │  │
│  │  • MemoryRepositoryPort                              │  │
│  │  • ProcessRepositoryPort                             │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
              ▲                                    ▲
              │                                    │
┌─────────────┴─────────────┐      ┌──────────────┴──────────────┐
│   Adapter Layer (Input)   │      │  Adapter Layer (Output)      │
│                            │      │                              │
│  • MCP Server              │      │  • KoogWorkflowExecutor      │
│  • CLI                     │      │  • ManualWorkflowExecutor    │
│                            │      │  • ConsoleVibeCheckEvaluator │
│                            │      │  • InMemoryMemoryRepository  │
└────────────────────────────┘      └──────────────────────────────┘
```

### Kern-Komponenten

| Komponente | Zweck | Layer |
|------------|-------|-------|
| `StartProcessExecutionService` | Initialisiert Prozess-Ausführung | Domain Service |
| `ExecuteProcessPhaseService` | Führt eine Phase aus (Workflow + Vibe Checks) | Domain Service |
| `CompletePhaseService` | Schließt Phase ab, wechselt zur nächsten | Domain Service |
| `WorkflowExecutionPort` | Interface für Workflow-Ausführung | Domain Port |
| `KoogWorkflowExecutor` | Führt YAML-Workflows mit LLM aus | Output Adapter |
| `ManualWorkflowExecutor` | Test-Dummy für Workflows (PROBLEMATISCH) | Output Adapter |

---

## Business Logic Flow

### 1. Process Start: `StartProcessExecutionService`

**Zweck**: Initialisiert eine neue Prozess-Ausführung

**Ablauf**:
```
User Request: "Starte Feature Development"
    ↓
[StartProcessExecutionService.execute()]
    ↓
1. Load Process Definition (ProcessRepository)
   → EngineeringProcess mit 3 Phasen
    ↓
2. Load or Create ExecutionContext (MemoryRepository)
   → ExecutionContext(projectPath, gitBranch)
    ↓
3. Create ProcessExecution
   → ProcessExecution(process, currentPhaseIndex=0)
    ↓
4. Persist Context
   → memoryRepository.save(context)
    ↓
Return: ProcessExecution (Status: IN_PROGRESS)
```

**Key Insight**: Dieser Service hat **keine User-Interaktion**. Er ist rein organisatorisch.

---

### 2. Phase Execution: `ExecuteProcessPhaseService`

**Zweck**: Führt eine ProcessPhase aus (Kern der Business Logic)

**Ablauf**:
```
Input: ProcessPhase, ExecutionContext
    ↓
[ExecuteProcessPhaseService.execute()]
    ↓
1. Execute Workflow (via WorkflowExecutionPort)
   → workflowExecutor.executeWorkflow(template, context)
   → 🔴 HIER findet die User-Interaktion statt!
   → Returns: WorkflowExecutionResult(summary, decisions)
    ↓
2. Evaluate Vibe Checks (via VibeCheckEvaluatorPort)
   → vibeCheckEvaluator.evaluateBatch(vibeChecks, context)
   → 🔴 HIER wird der User nach Quality Gates gefragt!
   → Returns: List<VibeCheckResult>
    ↓
3. Check if all Vibe Checks passed
   → If failed + required: Return FAILED PhaseResult
   → If passed: Continue
    ↓
4. Create PhaseResult
   → PhaseResult(summary, vibeCheckResults, decisions)
    ↓
Return: PhaseResult (Status: PHASE_COMPLETED or FAILED)
```

**Key Insight**: 
- **Workflow-Ausführung** (Schritt 1) sollte User-Interaktion enthalten
- **Vibe Checks** (Schritt 2) sind eine zweite Interaktions-Ebene (Quality Gates)

---

### 3. Phase Completion: `CompletePhaseService`

**Zweck**: Schließt Phase ab, wechselt zur nächsten

**Ablauf**:
```
Input: ProcessExecution, ExecutionContext, PhaseResult
    ↓
[CompletePhaseService.execute()]
    ↓
1. Update Context with PhaseResult
   → context.addPhaseResult(phaseResult)
    ↓
2. Check if more phases exist
   → If yes: context.advanceToNextPhase()
   → If no: Mark as COMPLETED
    ↓
3. Persist updated Context
   → memoryRepository.save(context)
    ↓
4. Return updated ProcessExecution
   → If more phases: execution.nextPhase()
   → If done: execution.complete()
    ↓
Return: ProcessExecution (Status: PHASE_COMPLETED or COMPLETED)
```

**Key Insight**: Dieser Service hat **keine User-Interaktion**. Er ist rein organisatorisch.

---

## User-Interaktions-Punkte

### Konzeptionelles Interaktions-Modell

```
┌─────────────────────────────────────────────────────────────┐
│                    User Interaction Layers                  │
└─────────────────────────────────────────────────────────────┘

Layer 1: Process Control
┌─────────────────────────────────────────────────────────────┐
│  "Starte Feature Development"                               │
│  → User wählt Prozess aus                                   │
│  → User startet Prozess                                     │
└─────────────────────────────────────────────────────────────┘
                          ↓
Layer 2: Workflow Execution (🔴 CRITICAL)
┌─────────────────────────────────────────────────────────────┐
│  Requirements Analysis Workflow                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Node 1: gather_requirements (LLM)                     │  │
│  │  LLM: "Was soll das Feature tun?"                     │  │
│  │  User: "Ein OAuth2 Login-System"                      │  │
│  │  LLM: "Welche Provider sollen unterstützt werden?"    │  │
│  │  User: "Google, GitHub, Microsoft"                    │  │
│  │  → Output: requirements_draft                         │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Node 2: identify_edge_cases (LLM)                     │  │
│  │  LLM: "Edge Cases erkannt:"                           │  │
│  │   - Token Expiry                                      │  │
│  │   - Network Failure                                   │  │
│  │   - Invalid Credentials                               │  │
│  │  → Output: edge_cases                                 │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Node 3: check_ambiguities (Conditional)               │  │
│  │  Condition: Gibt es unklare Requirements?            │  │
│  │  → If yes: request_clarification                     │  │
│  │  → If no: analyze_existing_architecture              │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Node 4: request_clarification (Human Interaction)     │  │
│  │  LLM: "Folgende Punkte sind unklar:"                 │  │
│  │   - Soll Single-Sign-On unterstützt werden?          │  │
│  │  User: "Nein, nur direkter Login"                    │  │
│  │  → Output: clarifications                            │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Node 5-7: ... (weitere Nodes)                         │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  → WorkflowExecutionResult(summary, decisions)              │
└─────────────────────────────────────────────────────────────┘
                          ↓
Layer 3: Vibe Checks (Quality Gates)
┌─────────────────────────────────────────────────────────────┐
│  Vibe Check 1: "Sind alle Requirements klar?"              │
│  → User: [j/n] j                                            │
│                                                             │
│  Vibe Check 2: "Wurden Edge Cases identifiziert?"          │
│  → User: [j/n] j                                            │
│                                                             │
│  → All passed: Phase COMPLETED                             │
└─────────────────────────────────────────────────────────────┘
                          ↓
Layer 4: Phase Completion
┌─────────────────────────────────────────────────────────────┐
│  Phase "Requirements Analysis" abgeschlossen                │
│  → Nächste Phase: "Architecture Design"                     │
│  → User: "Weiter zur nächsten Phase?" [Enter]              │
└─────────────────────────────────────────────────────────────┘
```

### Wo findet User-Interaktion statt?

| Layer | Service | Adapter | User-Interaktion? |
|-------|---------|---------|-------------------|
| 1. Process Start | `StartProcessExecutionService` | - | ❌ Nein (automatisch) |
| 2. Workflow Execution | `ExecuteProcessPhaseService` | `WorkflowExecutionPort` | ✅ **JA** (LLM-Dialog + Human-Interaction Nodes) |
| 3. Vibe Checks | `ExecuteProcessPhaseService` | `VibeCheckEvaluatorPort` | ✅ **JA** (Quality Gates) |
| 4. Phase Completion | `CompletePhaseService` | - | ⚠️ Optional (Bestätigung) |

### Kritischer Punkt: Workflow-Execution

Die **Workflow-Execution** (Layer 2) ist der **Kern der User-Interaktion**.

**Erwartetes Verhalten** (laut YAML):
```yaml
# requirements-analysis.yml, Node 1
- id: gather_requirements
  type: llm
  prompt: |
    Du bist ein Software-Architekt...
    Stelle gezielte Fragen:
    1. Was soll das Feature tun?
    2. Welche Eingaben gibt es?
    3. Welche Ausgaben werden erwartet?
  max_iterations: 3
```

**Reales Verhalten** (`KoogWorkflowExecutor`):
- LLM stellt Fragen → User antwortet → LLM sammelt Informationen
- Multi-Turn Conversation (max_iterations: 3)
- Output: `requirements_draft`

**Problem** (`ManualWorkflowExecutor`):
```kotlin
// ManualWorkflowExecutor.kt, Line 28-33
steps.forEach { step ->
    println("\n→ $step")
    print("  Fertig? (Enter drücken)")
    readlnOrNull()
}
```
→ **Keine echte Interaktion!** User drückt nur Enter, ohne Fragen zu beantworten.

---

## Workflow-Execution: YAML vs. Manual

### YAML-Workflow: requirements-analysis.yml

**Struktur**:
- **7 Nodes**: gather_requirements, identify_edge_cases, check_ambiguities, request_clarification, analyze_existing_architecture, document_requirements, prepare_vibe_checks
- **Node-Typen**: `llm`, `conditional`, `human_interaction`, `aggregation`
- **Graph**: Komplexer Ablauf mit Bedingungen

**Beispiel-Node** (Human Interaction):
```yaml
- id: request_clarification
  type: human_interaction
  prompt: |
    **Es gibt noch offene Fragen:**
    {{edge_cases}}
    
    Bitte kläre folgende Punkte:
    - Welche Edge Cases sind relevant?
    - Wie sollen widersprüchliche Anforderungen aufgelöst werden?
  output: clarifications
  required: true
```

**Erwartung**: 
- Workflow pausiert bei diesem Node
- User wird explizit nach Input gefragt
- Input wird als `clarifications` gespeichert und in folgenden Nodes verwendet

---

### ManualWorkflowExecutor: Hardcoded Steps

**Code** (ManualWorkflowExecutor.kt, Line 61-82):
```kotlin
private fun getWorkflowSteps(template: String): List<String> {
    return when {
        template.contains("requirements") ->
            listOf(
                "Sammle Anforderungen vom User",
                "Identifiziere Edge Cases",
                "Dokumentiere Requirements in requirements.md",
            )
        // ...
    }
}
```

**Probleme**:
1. ❌ **Ignoriert YAML-Struktur**: Nutzt nur Template-Namen, nicht den Inhalt
2. ❌ **Keine Nodes**: Workflows haben 7+ Nodes, hier nur 3 hardcoded Steps
3. ❌ **Keine Conditional Logic**: Keine `check_ambiguities`, keine Branches
4. ❌ **Keine Human Interaction**: Kein `request_clarification` Node
5. ❌ **Keine Variablen**: Kein `requirements_draft`, `edge_cases`, `clarifications`

**Konsequenz**: 
> **ManualWorkflowExecutor testet nicht die echte Business Logic, sondern eine Dummy-Variante.**

---

### Vergleich: KoogWorkflowExecutor vs. ManualWorkflowExecutor

| Aspekt | KoogWorkflowExecutor (Produktion) | ManualWorkflowExecutor (Test) |
|--------|-----------------------------------|-------------------------------|
| **YAML Parsing** | ✅ Liest und parst YAML-Dateien | ❌ Ignoriert YAML komplett |
| **Node-Typen** | ✅ `llm`, `conditional`, `human_interaction` | ❌ Nur lineare Steps |
| **Graph-Logik** | ✅ Edges, Conditions, Branches | ❌ Keine Branching-Logik |
| **LLM Integration** | ✅ Kotlin Koog + Azure OpenAI | ❌ Console-Input (dummy) |
| **User Interaction** | ✅ Multi-Turn Conversations | ⚠️ Nur "Enter drücken" |
| **Context Preservation** | ✅ Variablen zwischen Nodes | ❌ Keine Variablen |
| **Output Format** | ✅ `WorkflowExecutionResult` | ✅ `WorkflowExecutionResult` (aber Inhalt leer) |

**Fazit**: ManualWorkflowExecutor ist **keine realistische Simulation** der Workflow-Execution.

---

## Identifizierte Probleme

### Problem 1: Fehlende User-Interaktion im Workflow

**Symptom**: User sieht keine Fragen, gibt keine Antworten

**Root Cause**: `ManualWorkflowExecutor` stellt keine Fragen aus den YAML-Prompts

**Impact**: 
- User versteht nicht, was passiert
- Keine realistische Test-Erfahrung
- Business Logic wird nicht korrekt getestet

**Beispiel** (requirements-analysis.yml, Node 1):
```yaml
prompt: |
  Stelle gezielte Fragen:
  1. Was soll das Feature tun?
  2. Welche Eingaben gibt es?
  3. Welche Ausgaben werden erwartet?
```

**Aktueller ManualWorkflowExecutor**:
```
→ Sammle Anforderungen vom User
  Fertig? (Enter drücken)
```

**Erwarteter Ablauf**:
```
=== Requirements Gathering ===

🤖 LLM: Was soll das Feature genau tun?
👤 User: [Eingabe]

🤖 LLM: Welche Eingaben gibt es?
👤 User: [Eingabe]

🤖 LLM: Welche Ausgaben werden erwartet?
👤 User: [Eingabe]

✓ Requirements Draft erstellt
```

---

### Problem 2: ManualTestRunner stellt User vor vollendete Tatsachen

**Code** (ManualTestRunner.kt, Line 84-94):
```kotlin
print("\nPhase ausführen? (Enter drücken)")
readlnOrNull()

// Phase ausführen
val phaseResult = runBlocking {
    executePhaseService.execute(
        phase = processExecution.currentPhase(),
        context = context,
    )
}
```

**Ablauf**:
1. User drückt Enter
2. Workflow läuft automatisch durch (ohne User-Beteiligung)
3. Vibe Checks werden gestellt
4. Phase abgeschlossen

**Problem**: User hat **keine Chance**, während der Workflow-Execution zu interagieren.

**Erwartung**: Workflow sollte **während** der Execution User-Input anfordern.

---

### Problem 3: Diskrepanz zwischen Test und Produktion

**Test** (ManualTestRunner):
- Nutzt `ManualWorkflowExecutor`
- Hardcoded Steps
- Keine YAML-Interpretation

**Produktion** (MCP Server):
- Nutzt `KoogWorkflowExecutor`
- YAML-basierte Workflows
- LLM-gestützte Execution

**Konsequenz**: 
> **Tests validieren nicht die echte Business Logic!**

**Analogie**: 
- Auto-Hersteller testet Prototyp mit Fahrrad-Rädern
- Produktions-Auto hat Auto-Räder
- Test sagt: "Fährt!" → Aber nicht das echte Produkt

---

### Problem 4: Business Logic ist unklar getestet

**Frage**: Testet `ManualTestRunner` die echte Business Logic?

**Antwort**: **Teilweise**

| Komponente | Getestet? | Kommentar |
|------------|-----------|-----------|
| `StartProcessExecutionService` | ✅ Ja | Service-Logik korrekt |
| `ExecuteProcessPhaseService` | ⚠️ Teilweise | Service ja, aber mit falschen Adaptern |
| `CompletePhaseService` | ✅ Ja | Service-Logik korrekt |
| **Workflow-Execution** | ❌ Nein | `ManualWorkflowExecutor` ≠ `KoogWorkflowExecutor` |
| **Vibe Checks** | ✅ Ja | `ConsoleVibeCheckEvaluator` ist realistisch |
| **Memory Persistence** | ✅ Ja | `InMemoryMemoryRepository` ist realistisch |

**Kernproblem**: Die **Workflow-Execution** wird nicht realistisch getestet.

---

## Test-Strategie

### Aktueller Ansatz: ManualTestRunner

**Zweck**: Business Logic ohne Spring Boot / LLM testen

**Setup**:
```kotlin
val processRepository = InMemoryProcessRepository()
val memoryRepository = InMemoryMemoryRepository()
val workflowExecutor = ManualWorkflowExecutor()  // 🔴 PROBLEMATISCH
val vibeCheckEvaluator = ConsoleVibeCheckEvaluator()

val startService = StartProcessExecutionService(...)
val executePhaseService = ExecuteProcessPhaseService(...)
val completePhaseService = CompletePhaseService(...)
```

**Was funktioniert**:
- ✅ Domain Services werden instanziiert
- ✅ Process-Definitionen werden geladen
- ✅ ExecutionContext wird gespeichert
- ✅ Vibe Checks werden evaluiert

**Was nicht funktioniert**:
- ❌ Workflow-Execution ist nicht realistisch
- ❌ User-Interaktion fehlt im Workflow
- ❌ YAML-Workflows werden ignoriert

---

### Empfohlener Ansatz: InteractiveTestRunner mit KoogWorkflowExecutor

**Ziel**: Realistische Workflow-Execution mit echtem LLM

**Key Insight**: 
> **Wir haben bereits einen vollständigen Workflow-Executor: `KoogWorkflowExecutor`**  
> Dieser führt YAML-Workflows mit echtem LLM aus. Wir müssen ihn nur im Test nutzen!

**Key Differences**:

| Aspekt | ManualTestRunner (alt) | InteractiveTestRunner (neu) |
|--------|------------------------|-----------------------------|
| Workflow Executor | `ManualWorkflowExecutor` | `KoogWorkflowExecutor` |
| YAML Parsing | ❌ Nein | ✅ Ja (via Koog) |
| User Prompts | ❌ Nein | ✅ Ja (echte LLM-Konversation) |
| Node-Typen | ❌ Keine | ✅ `llm`, `conditional`, `human_interaction` |
| LLM | ❌ Nein | ✅ **Echtes LLM** (Azure OpenAI) |

**Konzept**:
```kotlin
/**
 * Interactive test runner mit echtem KoogWorkflowExecutor.
 * 
 * Unterschied zu ManualTestRunner:
 * - Nutzt KoogWorkflowExecutor statt ManualWorkflowExecutor
 * - Echte YAML-Workflows werden ausgeführt
 * - Echter LLM-Dialog mit User
 * - Testet EXAKT die Produktions-Logik
 */
fun main() {
    println("╔════════════════════════════════════════════════════════╗")
    println("║   Interactive Test Runner - Mit echtem LLM            ║")
    println("║   Testing Business Logic mit KoogWorkflowExecutor     ║")
    println("╚════════════════════════════════════════════════════════╝")

    // Setup: Echte Adapter verwenden
    val processRepository = InMemoryProcessRepository()
    val memoryRepository = InMemoryMemoryRepository()
    
    // 🔑 KEY CHANGE: KoogWorkflowExecutor statt ManualWorkflowExecutor
    val workflowExecutor = KoogWorkflowExecutor(
        llmProperties = loadLlmProperties(),
        yamlParser = YamlWorkflowTemplateParser()
    )
    
    val vibeCheckEvaluator = ConsoleVibeCheckEvaluator()

    // Services (unverändert - genau wie in Produktion)
    val startService = StartProcessExecutionService(
        processRepository, memoryRepository
    )
    val executePhaseService = ExecuteProcessPhaseService(
        workflowExecutor = workflowExecutor,  // Echter Executor!
        vibeCheckEvaluator = vibeCheckEvaluator,
    )
    val completePhaseService = CompletePhaseService(memoryRepository)

    // Process laden und ausführen
    val featureDevelopmentProcess = loadFeatureDevelopmentProcess()
    processRepository.save(featureDevelopmentProcess)

    // Process starten
    var processExecution = runBlocking {
        startService.execute(
            processId = featureDevelopmentProcess.id,
            projectPath = "/Users/groot/test-project",
            gitBranch = "feature/oauth2-login",
        )
    }

    // Context laden
    var context = memoryRepository.load(
        projectPath = "/Users/groot/test-project",
        gitBranch = "feature/oauth2-login",
    ) ?: throw IllegalStateException("Context not found")

    // Phasen durchlaufen
    while (processExecution.status == ExecutionStatus.IN_PROGRESS ||
           processExecution.status == ExecutionStatus.PHASE_COMPLETED) {
        
        println("\n" + "=".repeat(60))
        println("📍 Phase: ${processExecution.currentPhase().name}")
        println("   ${processExecution.currentPhaseIndex + 1}/${processExecution.process.totalPhases()}")
        println("   Template: ${processExecution.currentPhase().koogWorkflowTemplate}")
        
        print("\nPhase starten? (Enter drücken)")
        readlnOrNull()

        // 🔑 HIER passiert die echte LLM-Interaktion!
        // Der User wird vom LLM interviewt (via requirements-analysis.yml)
        val phaseResult = runBlocking {
            executePhaseService.execute(
                phase = processExecution.currentPhase(),
                context = context,
            )
        }

        // Context aktualisieren
        context = context.addPhaseResult(phaseResult)

        // Vibe Checks wurden bereits gestellt (via ConsoleVibeCheckEvaluator)
        
        // Phase abschließen
        processExecution = runBlocking {
            completePhaseService.execute(
                execution = processExecution,
                context = context,
                phaseResult = phaseResult,
            )
        }
    }

    // Zusammenfassung
    printSummary(context, processExecution)
}
```

**Vorteile**:
- ✅ Nutzt **echten** `KoogWorkflowExecutor` (wie in Produktion)
- ✅ Führt **echte YAML-Workflows** aus
- ✅ **Echter LLM-Dialog** mit User (keine Simulation)
- ✅ **Context-Preservation** zwischen Nodes (von Koog)
- ✅ Testet **exakt** die Produktions-Logik
- ✅ **Kein zusätzlicher Code** nötig (Koog existiert bereits)

---

## Lösungsempfehlungen

### Sofort: ManualTestRunner durch InteractiveTestRunner ersetzen

**Ziel**: Realistische Tests mit echtem `KoogWorkflowExecutor`

**Erkenntnis**:
> **Wir brauchen keinen neuen Executor zu bauen!**  
> `KoogWorkflowExecutor` existiert bereits und funktioniert. Wir müssen ihn nur im Test nutzen.

**Maßnahmen**:
1. **Neuer `InteractiveTestRunner.kt`** erstellen:
   - Kopiere `ManualTestRunner.kt` als Basis
   - Ersetze `ManualWorkflowExecutor` durch `KoogWorkflowExecutor`
   - LLM Properties laden (aus application.yml oder Environment)
   - Sonst alles gleich lassen!

2. **`ManualTestRunner.kt` umbenennen/dokumentieren**:
   - Prefix: `Legacy_ManualTestRunner.kt` (optional)
   - Kommentar: "Proof-of-Concept ohne LLM - nicht mehr aktuell"
   - Kann gelöscht werden, wenn InteractiveTestRunner funktioniert

**Code-Änderungen** (minimal!):
```kotlin
// InteractiveTestRunner.kt (NEU - nur 3 Zeilen ändern!)

// ALT (ManualTestRunner):
val workflowExecutor = ManualWorkflowExecutor()

// NEU (InteractiveTestRunner):
val llmProperties = LlmProperties(
    baseUrls = listOf(System.getenv("AZURE_OPENAI_ENDPOINT") ?: "http://localhost:8080"),
    apiKey = System.getenv("AZURE_OPENAI_API_KEY") ?: "dummy",
    deploymentName = "gpt-4",
    apiVersion = "2024-02-15-preview"
)
val workflowExecutor = KoogWorkflowExecutor(
    llmProperties = llmProperties,
    yamlParser = YamlWorkflowTemplateParser()
)

// Rest bleibt EXAKT gleich!
```

**Vorteile**:
- ✅ **Minimaler Aufwand**: Nur 10 Zeilen Code ändern
- ✅ **Testet echte Logik**: Genau wie in Produktion
- ✅ **Echte LLM-Interaktion**: User erlebt realistischen Dialog
- ✅ **Keine neuen Abstraktionen**: Nutzt existierenden Code

---

### Mittelfristig: Automatisierte E2E Tests mit KoogWorkflowExecutor

**Ziel**: Automatisierte Tests mit echtem LLM (ohne manuelle Interaktion)

**Ansatz**: Prepared User Responses für deterministisches Testing

```kotlin
@Test
fun `full feature development process with real LLM`() {
    // Setup: Echter KoogWorkflowExecutor
    val koogExecutor = KoogWorkflowExecutor(
        llmProperties = testLlmProperties,
        yamlParser = YamlWorkflowTemplateParser()
    )
    
    // Vibe Checks: Auto-Pass für schnellere Tests
    val vibeCheckEvaluator = AutoPassVibeCheckEvaluator()
    
    val executePhaseService = ExecuteProcessPhaseService(
        workflowExecutor = koogExecutor,
        vibeCheckEvaluator = vibeCheckEvaluator
    )
    
    // Execute: Echter Workflow läuft durch
    val phaseResult = runBlocking {
        executePhaseService.execute(
            phase = requirementsPhase,
            context = testContext
        )
    }
    
    // Assert: Verify LLM-Output
    assertThat(phaseResult.status).isEqualTo(ExecutionStatus.PHASE_COMPLETED)
    assertThat(phaseResult.summary).isNotBlank()
    assertThat(phaseResult.decisions).isNotEmpty()
    
    // Optional: Verify specific content (if LLM is deterministic)
    // assertThat(phaseResult.summary).contains("OAuth2")
}
```

**Vorteile**:
- ✅ Testet **exakt** die Produktions-Logik
- ✅ Findet Integrations-Bugs
- ✅ Verifiziert LLM-Interaktion
- ✅ Kann in CI/CD Pipeline laufen

**Nachteile**:
- ⚠️ Langsam (~10-30s pro Test, je nach Workflow)
- ⚠️ Kostet (OpenAI API - aber vertretbar für wichtige Tests)
- ⚠️ Non-deterministisch (LLM-Output variiert leicht)

**Best Practice**:
- Nur für **kritische Workflows** (z.B. requirements-analysis.yml)
- Markieren mit `@Tag("slow")` oder `@Tag("llm")`
- Separate Test-Suite für lokale vs. CI/CD Runs

---

## Zusammenfassung

### Key Takeaways

1. **Business Logic ist gut implementiert**
   - Domain Services (`StartProcessExecutionService`, `ExecuteProcessPhaseService`, `CompletePhaseService`) sind korrekt
   - Port Interfaces sind sauber definiert
   - Hexagonal Architecture ist konsequent umgesetzt

2. **Workflow-Execution ist das Kernproblem**
   - `ManualWorkflowExecutor` bildet nicht die Realität ab
   - YAML-Workflows werden ignoriert
   - User-Interaktion fehlt

3. **Tests sind nicht aussagekräftig**
   - `ManualTestRunner` testet eine Dummy-Variante
   - Echte Workflows (`KoogWorkflowExecutor`) werden nicht getestet
   - Gap zwischen Test und Produktion

4. **User-Interaktion findet an zwei Stellen statt**
   - **Layer 2: Workflow-Execution** (LLM-Dialog, Human-Interaction Nodes)
   - **Layer 3: Vibe Checks** (Quality Gates)
   - Aktuell: Nur Layer 3 funktioniert im Test

### Empfohlene Nächste Schritte

**Wichtigste Erkenntnis**: 
> **Wir brauchen keinen neuen Executor!** `KoogWorkflowExecutor` existiert bereits.  
> Die Lösung ist, ihn im Test zu nutzen statt einen Dummy zu verwenden.

**Action Plan**:

1. **Sofort**: `InteractiveTestRunner.kt` erstellen
   - Kopiere `ManualTestRunner.kt`
   - Ersetze `ManualWorkflowExecutor` durch `KoogWorkflowExecutor`
   - LLM Properties aus Environment laden
   - **Aufwand**: ~30 Minuten

2. **Sofort**: `ManualTestRunner.kt` dokumentieren
   - Kommentar hinzufügen: "Legacy - Proof-of-Concept ohne LLM"
   - Optional: Umbenennen zu `Legacy_ManualTestRunner.kt`
   - Oder: Löschen, wenn InteractiveTestRunner funktioniert
   - **Aufwand**: ~5 Minuten

3. **Kurzfristig**: Einmal manuell durchlaufen
   - `InteractiveTestRunner` ausführen
   - Durchlaufen: Requirements Analysis Phase
   - Validieren: LLM stellt echte Fragen, User antwortet
   - **Aufwand**: ~15 Minuten

4. **Mittelfristig**: Automatisierte E2E Tests (optional)
   - Tests mit `@Tag("llm")` markieren
   - In CI/CD nur bei wichtigen Änderungen laufen lassen
   - **Aufwand**: ~2 Stunden

### Offene Fragen

1. **Soll ManualWorkflowExecutor erhalten bleiben?**
   - Pro: Schneller Test ohne YAML-Parsing
   - Contra: Bildet nicht die Realität ab

2. **Wie soll User-Interaktion im Test simuliert werden?**
   - Option A: User tippt manuell (langsam, aber realistisch)
   - Option B: Prepared Responses (schnell, deterministisch)
   - Option C: Hybrid (wichtige Fragen manuell, Rest auto)

3. **Brauchen wir Tests auf allen Ebenen?**
   - Unit Tests (Domain Services) → Ja
   - Integration Tests (mit Dummy-Adaptern) → Ja
   - E2E Tests (mit KoogWorkflowExecutor) → Optional

---

**Autor**: Warp Agent  
**Review**: Pending  
**Status**: Draft
