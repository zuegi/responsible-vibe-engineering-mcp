# Interactive Test Runner - Usage Guide

## Übersicht

Der **InteractiveTestRunner** testet die Business Logic mit **echtem LLM** und **echten YAML-Workflows**.

### Unterschied zu ManualTestRunner

| Aspekt | ManualTestRunner (Legacy) | InteractiveTestRunner (Neu) |
|--------|---------------------------|------------------------------|
| **Workflow Executor** | `ManualWorkflowExecutor` (dummy) | `KoogWorkflowExecutor` (real LLM) |
| **YAML Workflows** | ❌ Ignoriert | ✅ Führt aus (requirements-analysis.yml, etc.) |
| **LLM Interaktion** | ❌ Nur "Enter drücken" | ✅ Echter Dialog mit LLM |
| **Testet Produktion** | ❌ Nein (Dummy) | ✅ Ja (exakt gleiche Logic) |

## Setup

### 1. Environment Variables setzen

```bash
# Azure OpenAI Endpoint (required)
export AZURE_OPENAI_ENDPOINT="https://your-gateway.example.com/openai/deployments/gpt-4o/"

# API Key (required, oder "dummy" für gateways ohne auth)
export AZURE_OPENAI_API_KEY="your-api-key"

# Optional: API Version (default: 2024-05-01-preview)
export AZURE_OPENAI_API_VERSION="2024-05-01-preview"

# Optional: Provider name (default: azure-openai)
export LLM_PROVIDER="azure-openai"
```

### 2. Projekt kompilieren

```bash
mvn clean compile
```

### 3. InteractiveTestRunner ausführen

#### Option A: Mit Maven

```bash
mvn exec:java -Dexec.mainClass="ch.zuegi.rvmcp.InteractiveTestRunnerKt"
```

#### Option B: Direkt mit Kotlin (nach mvn compile)

```bash
kotlin -classpath target/classes ch.zuegi.rvmcp.InteractiveTestRunnerKt
```

#### Option C: IntelliJ/IDE

1. Öffne `InteractiveTestRunner.kt`
2. Klicke auf das grüne Play-Icon neben `fun main()`
3. Environment Variables in Run Configuration setzen

## Ablauf

### Phase 1: Setup
```
╔════════════════════════════════════════════════════════╗
║   Responsible Vibe MCP - Interactive Test Runner     ║
║   Testing Business Logic MIT ECHTEM LLM              ║
╚════════════════════════════════════════════════════════╝

✓ LLM Configuration loaded
  Provider: azure-openai
  Base URL: https://...
  API Version: 2024-05-01-preview

✓ Initializing KoogWorkflowExecutor (with REAL LLM)...
✓ Setup abgeschlossen
  Process: Feature Development
  Phasen: 3
```

### Phase 2: Process Start
```
🚀 Bereit zum Starten des Feature Development Prozesses
   Projekt: /Users/groot/test-project
   Branch: feature/new-feature

💡 HINWEIS: Der LLM wird dich jetzt interviewen!
   - Beantworte die Fragen des LLMs
   - Am Ende jeder Phase: Vibe Checks bestätigen

Enter drücken zum Starten...
```

### Phase 3: Requirements Analysis (REAL LLM!)
```
📍 Current Phase: Requirements Analysis
   Phase 1/3
   Template: requirements-analysis.yml

💡 ACHTUNG: Jetzt startet die LLM-Konversation!
   Der LLM wird dir Fragen stellen (aus dem YAML-Workflow).
   Beantworte sie so, als würdest du ein echtes Feature planen.

Phase starten? (Enter drücken):

[... LLM stellt Fragen aus requirements-analysis.yml ...]

🤖 LLM: Was soll das Feature genau tun?
👤 User: [Deine Antwort]

🤖 LLM: Welche Eingaben gibt es?
👤 User: [Deine Antwort]

[... weitere LLM-Fragen ...]
```

### Phase 4: Vibe Checks (Quality Gates)
```
=== Vibe Check ===
Frage: Sind die Requirements klar und vollständig?
Typ: REQUIREMENTS
Obligatorisch: true

Besteht der Check? (j/n oder y/n): j

=== Vibe Check ===
Frage: Wurden Edge Cases identifiziert?
Typ: REQUIREMENTS
Obligatorisch: false

Besteht der Check? (j/n oder y/n): j
```

### Phase 5: Zusammenfassung
```
📊 Zusammenfassung
============================================================
Status: COMPLETED

Abgeschlossene Phasen:
  ✓ Requirements Analysis
    Summary: Workflow: Requirements Analysis
Project: /Users/groot/test-project...
    Vibe Checks: 2/2 bestanden

Architektur-Entscheidungen: 3
  • [Requirements Analysis] Completed step: gather_requirements
    → Sammle und analysiere Anforderungen

Artifacts: 0

🎉 Interactive Test abgeschlossen!

✅ Du hast gerade die ECHTE Business Logic getestet:
   - KoogWorkflowExecutor mit echtem LLM
   - YAML-Workflows (requirements-analysis.yml, etc.)
   - Domain Services (Start, Execute, Complete)
   - Vibe Checks (Quality Gates)
   - Memory Persistence (ExecutionContext)
```

## Was wird getestet?

### ✅ Getestet (genau wie in Produktion)

- **Domain Services**:
  - `StartProcessExecutionService` (Process initialisieren)
  - `ExecuteProcessPhaseService` (Phase ausführen)
  - `CompletePhaseService` (Phase abschließen)

- **Workflow Execution**:
  - `KoogWorkflowExecutor` (ECHTER LLM!)
  - YAML-Workflows (`requirements-analysis.yml`, `architecture-design.yml`, `implementation.yml`)
  - Context-Preservation zwischen Nodes

- **Vibe Checks**:
  - `ConsoleVibeCheckEvaluator` (manuelle Quality Gates)
  - Required vs. Optional Checks
  - Fehlerbehandlung bei Failed Checks

- **Memory Persistence**:
  - `InMemoryMemoryRepository` (Context speichern)
  - `ExecutionContext` (Phase History, Architectural Decisions)

### ❌ Nicht getestet (bewusst)

- **MCP Server**: Nicht gestartet (nur Domain Logic)
- **Spring Boot**: Nicht verwendet (direkte Service-Instanziierung)
- **File-basierte Persistence**: In-Memory statt Dateien

## Tipps & Tricks

### 1. Kürzere Tests: Nur 1 Phase

Passe `createFeatureDevelopmentProcess()` an:

```kotlin
return EngineeringProcess(
    id = ProcessId("feature-development"),
    name = "Feature Development",
    description = "Strukturierter Prozess für neue Features",
    phases = listOf(requirementsPhase), // Nur Requirements!
)
```

### 2. Auto-Pass Vibe Checks (schneller)

Ersetze `ConsoleVibeCheckEvaluator` durch `AutoPassVibeCheckEvaluator`:

```kotlin
// Statt:
val vibeCheckEvaluator = ConsoleVibeCheckEvaluator()

// Nutze:
val vibeCheckEvaluator = AutoPassVibeCheckEvaluator()
```

### 3. Test-Workflow statt Production

Nutze `simple-test.yml` statt `requirements-analysis.yml`:

```kotlin
koogWorkflowTemplate = "simple-test.yml", // Nur 1 LLM Node
```

### 4. Debugging: LLM Output sehen

Setze Log-Level auf DEBUG:

```bash
export LOG_LEVEL=DEBUG
```

Oder in IntelliJ: Run Configuration → VM Options:
```
-Dlogging.level.ch.zuegi.rvmcp=DEBUG
```

## Troubleshooting

### Fehler: "Environment variable AZURE_OPENAI_ENDPOINT not set!"

**Lösung**: Setze Environment Variable:
```bash
export AZURE_OPENAI_ENDPOINT="https://your-gateway.example.com/openai/deployments/gpt-4o/"
```

### Fehler: "Workflow execution failed"

**Mögliche Ursachen**:
1. LLM Endpoint nicht erreichbar
2. API Key ungültig
3. YAML-Workflow hat Fehler

**Debugging**:
```bash
export LOG_LEVEL=DEBUG
mvn exec:java -Dexec.mainClass="ch.zuegi.rvmcp.InteractiveTestRunnerKt"
```

### stdin Blocking Issue (bei Vibe Checks)

**Problem**: Vibe Checks blockieren, wenn stdin vom MCP Protocol genutzt wird

**Lösung**: Nutze `AutoPassVibeCheckEvaluator` statt `ConsoleVibeCheckEvaluator`

## Nächste Schritte

### 1. Einmal durchlaufen

- Führe InteractiveTestRunner aus
- Durchlaufe 1 Phase (Requirements Analysis)
- Validiere: LLM stellt echte Fragen

**Aufwand**: ~15 Minuten

### 2. Alle 3 Phasen testen

- Durchlaufe alle Phasen (Requirements, Architecture, Implementation)
- Validiere: Context wird zwischen Phasen erhalten

**Aufwand**: ~45 Minuten (mit LLM-Interaktion)

### 3. Automatisierte E2E Tests (optional)

Siehe `docs/business-logic-analysis.md` → "Automatisierte E2E Tests mit KoogWorkflowExecutor"

## Vergleich: ManualTestRunner vs. InteractiveTestRunner

| Feature | ManualTestRunner | InteractiveTestRunner |
|---------|------------------|----------------------|
| LLM | ❌ Nein (dummy) | ✅ Ja (echt) |
| YAML-Workflows | ❌ Ignoriert | ✅ Ausgeführt |
| User-Interaktion | ⚠️ Nur "Enter" | ✅ Echter Dialog |
| Testet Produktion | ❌ Nein | ✅ Ja |
| Geschwindigkeit | ⚡ Schnell (~1 Min) | 🐢 Langsam (~15 Min) |
| Kosten | 💰 Keine | 💰 LLM API Calls |
| Use Case | Quick Smoke Test | Realistic Integration Test |

## FAQ

### Q: Kann ich einen anderen LLM Provider nutzen?

**A**: Ja! Passe `loadLlmPropertiesFromEnvironment()` an:

```kotlin
val baseUrl = System.getenv("OPENAI_ENDPOINT") ?: "https://api.openai.com/v1/"
// ... anpassen für deinen Provider
```

### Q: Wie teste ich ohne LLM-Kosten?

**A**: Nutze `ManualTestRunner.kt` (Legacy) für schnelle Smoke Tests ohne LLM.

### Q: Kann ich prepared responses nutzen?

**A**: Aktuell nein. Für automatisierte Tests siehe `docs/business-logic-analysis.md` → "Automatisierte E2E Tests".

### Q: Wo finde ich die YAML-Workflows?

**A**: In `src/main/resources/workflows/`:
- `requirements-analysis.yml`
- `architecture-design.yml`
- `implementation.yml`
- `simple-test.yml` (für schnelle Tests)

---

**Autor**: Warp Agent  
**Letzte Aktualisierung**: 2025-11-17  
**Status**: Ready to use
