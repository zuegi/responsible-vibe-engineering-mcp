# Responsible Vibe MCP – Engineering-getriebene KI-Entwicklung

## Vision & Problemstellung

Viele KI-Codier-Tools funktionieren als „Autocompletes on Steroids" – sie durchsuchen Code, recherchieren online und generieren Lösungen. Doch **Softwareengineering umfasst weit mehr**: Architekturdenken, methodisches Vorgehen, Refactoring-Vermeidung und bewusste Planung.

**Das Problem**: Der Planungsschritt wird oft übersprungen → „Zehn Minuten Design hätten drei Stunden Refactoring vermieden"

**Die Lösung**: Responsible Vibe MCP führt KI-Systeme aktiv durch bewährte Engineering-Workflows, sodass das Ergebnis nicht nur Code, sondern eine durchdachte, nachhaltige Lösung ist.

---

## Funktionsprinzip

Responsible Vibe MCP leitet die KI systematisch durch klar definierte Projektphasen:

1. **Planung & Anforderungsanalyse**
2. **Architekturentwurf**
3. **Dokumentation des Projekts**
3. **Implementierung**
4. **Testing**
5. **Integration & Review**

### Persistentes Gedächtnis

Der Agent besitzt ein **Long-Term Memory**, das frühere Entscheidungen, Architekturüberlegungen und Gesprächskontexte über mehrere Sitzungen hinweg bewahrt.

**Vorteile**:
- ✅ Kontinuität über Sessions hinweg
- ✅ Nachvollziehbarkeit von Entscheidungen
- ✅ Vermeidung von Wiederholungen

---

## Kernprinzipien: Vibe Engineering

„Vibe Engineering" beschreibt das Mindset hinter diesem Ansatz:

- **Qualität vor Geschwindigkeit**
- **Wartbarkeit & Testbarkeit**
- **Bewusste Architekturentscheidungen**
- **Aktive Reflexion**: „Passt das in die bestehende Architektur?"

Die KI wird zum **aktiven Entwicklungspartner**, nicht bloß zum Code-Generator.

---

## Workflows

Das System bietet mehrere Engineering-Workflows für verschiedene Szenarien:

### 1. Neues Projekt/Feature
- definiere die Projekt-Sprache: Deutsch, Englisch,...
- verbindliche Dokumentation definieren
- Anforderungen definieren
- Architektur entwerfen
- Technologie-Stack evaluieren
- Implementierungsplan erstellen
- Schrittweise Umsetzung mit Tests

### 2. Bug-Fix
- Problem analysieren
- Root-Cause identifizieren
- Lösung entwerfen
- Tests schreiben
- Fix implementieren
- Dokumentation erweitern

### 3. Refactoring
- Code-Smells identifizieren
- Zielarchitektur definieren
- Schrittweise Transformation
- Tests sicherstellen Regressionssicherheit
- Dokumentation erweitern

---

## Tech Stack

- **Sprache**: Kotlin
- **Framework**: Spring Boot
- **Agentic AI**: Kotlin Koog
- **Build Tool**: Maven
- **Version Control**: Git
- **Architektur**: Hexagonal Architecture (Ports & Adapters)
- **Interface**: Model Context Protocol (MCP) Server

---

## Architektur: Hexagonal Architecture

Das Projekt folgt dem **Hexagonal Architecture**-Pattern (Ports & Adapters), um:

- **Domain-Logik zu isolieren**: Workflow-Engine, Memory und Vibe-Engineering-Logik sind framework-unabhängig
- **Austauschbarkeit zu ermöglichen**: LLM-Provider, Persistence-Layer, Input-Interfaces sind austauschbar
- **Testbarkeit zu maximieren**: Domain-Logik ohne externe Dependencies testbar
- **Technologie-Unabhängigkeit**: Spring Boot, Kotlin Koog etc. sind austauschbare Implementierungsdetails

### Schichten

**Domain** (Kern):
- `domain/model`: Entities, Value Objects (z.B. Workflow, Phase, Context)
- `domain/port/input`: Use Case Interfaces (z.B. ExecuteWorkflowUseCase)
- `domain/port/output`: Output Interfaces (z.B. MemoryRepository, AIProvider)
- `domain/service`: Domain Services (Business-Logik)

**Application**:
- `application/workflow`: Workflow Orchestration (Use Case Implementierungen)

**Adapter**:
- `adapter/input`: Driving Adapters (CLI, MCP Protocol)
- `adapter/output`: Driven Adapters (Kotlin Koog, File-Memory, Git)

**Infrastructure**:
- Spring Boot Configuration, Dependency Injection

---

## MCP Server: Das Interface zur KI

### Was ist MCP (Model Context Protocol)?

**Model Context Protocol** ist ein standardisiertes Protokoll für die Kommunikation zwischen KI-Systemen (wie Claude, ChatGPT, Warp Agent) und externen Tools/Services.

**Vorteile**:
- 🔌 **Standardisiert**: JSON-RPC 2.0 basiertes Protokoll
- 🛠️ **Tool-basiert**: Funktionen werden als "Tools" exposed
- 📡 **Bidirektional**: Client ↔ Server Kommunikation
- 🔄 **Stateless**: Jeder Request ist unabhängig
- 🌐 **Universal**: Funktioniert mit jedem MCP-kompatiblen Client

### Wie funktioniert der MCP Server?

```
MCP Client (Claude Desktop, Warp Agent, IDE)       MCP Server (Responsible Vibe)
        │                                                     │
        │  JSON-RPC Request: "start_process"                 │
        │────────────────────────────────────────────────────>│
        │                                                     │
        │                                      Domain Service │
        │                                      orchestrates   │
        │                                      Business Logic │
        │                                                     │
        │  JSON-RPC Response: ProcessExecution                │
        │<────────────────────────────────────────────────────│
        │                                                     │
```

### MCP Server Architektur

```
adapter/input/mcp/
├── McpServerAdapter.kt          # MCP Server Entry Point (stdio Transport)
├── McpToolRegistry.kt           # Tool Registration & Discovery
├── McpRequestHandler.kt         # JSON-RPC Request Processing
├── tools/
│   ├── StartProcessTool.kt      # Tool: start_process
│   ├── ExecutePhaseTool.kt      # Tool: execute_phase
│   ├── CompletePhaseTool.kt     # Tool: complete_phase
│   ├── GetContextTool.kt        # Tool: get_context
│   └── ListProcessesTool.kt     # Tool: list_processes
└── resources/
    ├── ContextResource.kt        # Resource: context://project/branch
    └── ProcessResource.kt        # Resource: process://process-id
```

### MCP Tools

**1. start_process**
```json
{
  "name": "start_process",
  "description": "Startet einen Engineering-Prozess (Feature Development, Bug Fix, etc.)",
  "parameters": {
    "process_id": "feature-development",
    "project_path": "/path/to/project",
    "git_branch": "feature/new-feature"
  },
  "returns": "ProcessExecution"
}
```

**2. execute_phase**
```json
{
  "name": "execute_phase",
  "description": "Führt eine ProcessPhase aus (mit Koog Workflow und Vibe Checks)",
  "parameters": {
    "execution_id": "exec-12345",
    "phase_index": 0
  },
  "returns": "PhaseResult"
}
```

**3. complete_phase**
```json
{
  "name": "complete_phase",
  "description": "Schließt eine Phase ab und wechselt zur nächsten",
  "parameters": {
    "execution_id": "exec-12345"
  },
  "returns": "ProcessExecution"
}
```

**4. get_context**
```json
{
  "name": "get_context",
  "description": "Lädt den ExecutionContext für ein Projekt",
  "parameters": {
    "project_path": "/path/to/project",
    "git_branch": "feature/new-feature"
  },
  "returns": "ExecutionContext"
}
```

**5. list_processes**
```json
{
  "name": "list_processes",
  "description": "Listet verfügbare Engineering-Prozesse auf",
  "parameters": {},
  "returns": "List<EngineeringProcess>"
}
```

### MCP Resources

**Resources** sind lesbare Inhalte, die der MCP Client nutzen kann:

**1. Context Resource**
```
URI: context://project/path/branch/name
Content-Type: application/json

Bietet: ExecutionContext mit Phase History, Architectural Decisions, etc.
```

**2. Process Resource**
```
URI: process://feature-development
Content-Type: application/json

Bietet: EngineeringProcess Definition mit Phasen und Vibe Checks
```

### Integration mit KI-Systemen

#### Claude Desktop

**Konfiguration**: `~/.config/claude/mcp-servers.json`
```json
{
  "responsible-vibe-mcp": {
    "command": "java",
    "args": [
      "-jar",
      "/path/to/rvmcp.jar",
      "--mcp-mode"
    ]
  }
}
```

**Nutzung**:
```
User: "Starte einen Feature Development Prozess für mein OAuth2 Login"

Claude:
1. Ruft start_process auf
2. Führt execute_phase aus (Requirements Analysis)
3. Zeigt Vibe Check Ergebnisse
4. Fragt nach Bestätigung
5. Führt nächste Phase aus
```

#### Warp Agent

**Warp Agent** kann den MCP Server direkt nutzen:
```
User: "Implementiere OAuth2 Login strukturiert"

Warp Agent:
1. Startet MCP Server (falls nicht laufend)
2. Tool Call: start_process
3. Tool Call: execute_phase (Requirements)
4. Zeigt LLM-generierte Requirements
5. Tool Call: execute_phase (Architecture)
6. Zeigt Architektur-Vorschlag
7. Fragt: "Ready for Implementation?"
```

### Communication Flow: End-to-End

```
[1] User Request (via Claude/Warp)
    ↓
[2] MCP Client
    │ JSON-RPC Request über stdio
    ↓
[3] McpServerAdapter
    │ Parse Request, Route zu Tool
    ↓
[4] StartProcessTool
    │ Call Domain Service
    ↓
[5] StartProcessExecutionService
    │ Business Logic
    ↓
[6] ProcessRepository, MemoryRepository
    │ Persistence
    ↓
[7] ProcessExecution (Response)
    ↓
[8] McpServerAdapter
    │ JSON-RPC Response über stdio
    ↓
[9] MCP Client
    ↓
[10] User (Result anzeigen)
```

### Vorteile des MCP-Ansatzes

✅ **Universal**: Ein Server, viele Clients (Claude, Warp, IDEs)  
✅ **Standardisiert**: Keine proprietären Protokolle  
✅ **Erweiterbar**: Neue Tools einfach hinzufügen  
✅ **Testbar**: Tools können unabhängig getestet werden  
✅ **Framework-Unabhängig**: Domain Logic bleibt isoliert  

### Phase 2a: MCP Server Implementation

**Ziel**: Responsible Vibe Engineering als MCP Server verfügbar machen

**Scope**:
1. MCP Protocol Integration (JSON-RPC 2.0)
2. stdio Transport (für Claude Desktop / Warp)
3. Tool Implementations (5 Tools)
4. Resource Implementations (2 Resources)
5. Integration mit bestehenden Domain Services
6. MCP Server Tests

**Nicht in Phase 2a**:
- HTTP Transport (später)
- Authentication (später)
- Persistentes Memory (Phase 2b)

---

## Konzeptionelle Architektur

### Workflow vs. Process - Klare Trennung

**Problem**: Begriff "Workflow" hat zwei Bedeutungen
- **Engineering-Prozess**: Methodisches Vorgehen (Planung → Architektur → Implementation)
- **Kotlin Koog Workflow**: Ausführungs-Graph für Agent-Aktionen

**Lösung**: Konzeptionelle Trennung

#### EngineeringProcess (Domain)
```
EngineeringProcess "Feature Development"
├─ ProcessPhase "Requirements Analysis"
│  ├─ VibeChecks: ["Sind Requirements klar?", "Edge Cases?"]
│  └─ KoogWorkflowTemplate: "requirements-analysis.yml"
├─ ProcessPhase "Architecture Design"
│  ├─ VibeChecks: ["Passt in bestehende Architektur?", "Testbar?"]
│  └─ KoogWorkflowTemplate: "architecture-design.yml"
└─ ProcessPhase "Implementation"
   ├─ VibeChecks: ["Code-Qualität?", "Tests vorhanden?"]
   └─ KoogWorkflowTemplate: "implementation.yml"
```

#### Kotlin Koog Workflow (Execution)
```yaml
# workflows/requirements-analysis.yml
name: "Requirements Analysis"
graph:
  nodes:
    - id: gather_requirements
      type: llm
    - id: clarify_ambiguities
      type: conditional
    - id: human_input
      type: human_interaction
    - id: vibe_checks
      type: vibe_check
```

### Ablauf: Von User-Request zu strukturierter Entwicklung

```
[1] User Request: "Implementiere Feature X"
    ↓
[2] Process Selection
    → EngineeringProcess auswählen (Feature Development)
    → ProcessExecution erstellen
    ↓
[3] Phase-by-Phase Execution
    → ProcessPhase "Requirements Analysis"
    → Koog Workflow ausführen (requirements-analysis.yml)
    → Vibe Checks durchführen
    → Human-in-the-Loop (wenn nötig)
    → Phase abschließen & dokumentieren
    ↓
[4] Memory & Kontext speichern
    → ExecutionContext aktualisieren
    → Architectural Decisions dokumentieren
    → Nächste Phase starten
```

### Vibe Engineering Checks

**Konzept**: Quality Gates pro Phase

**Implementierung**: Eigene Domain-Objekte
```kotlin
VibeCheck {
  question: "Passt das in die bestehende Architektur?"
  type: ARCHITECTURE
  required: true
  validationCriteria: [...]
}
```

**Trigger-Points**:
- Nach jeder ProcessPhase (obligatorisch)
- Bei kritischen Architektur-Entscheidungen
- Wenn Vibe Check fehlschlägt
- Bei erkannten Unklarheiten

### Human-in-the-Loop

**Strategie**: Hybrid-Ansatz

**Obligatorisch**:
- Nach jeder Phase: Zusammenfassung + Bestätigung
- User behält Kontrolle

**Automatisch**:
- Kritische Architektur-Entscheidungen
- Breaking Changes
- Failed Vibe Checks
- Erkannte Ambiguitäten

**Balance**: User als Driver, aber nicht bei jedem LLM-Call unterbrochen

### Memory-Architektur: Zwei Ebenen

**Problem**: Wie integrieren wir Koog's Intelligent History Compression mit unserem Long-Term Memory?

**Lösung**: Zwei komplementäre Memory-Ebenen

#### Ebene 1: Koog's Intelligent History Compression (Kurzzeit)
**Zweck**: Conversational Memory während eines Workflow-Runs
- Komprimiert LLM-Konversationen innerhalb einer Phase
- Reduziert Token-Kosten
- Behält Kontext während der Ausführung

**Lebensdauer**: Während einer ProcessPhase / Koog Workflow Execution

**Beispiel**:
```
Phase: "Requirements Analysis"
  → Koog Workflow läuft
  → 50 LLM-Interaktionen
  → Koog komprimiert zu: "User wants Feature X with constraints Y, Z"
```

#### Ebene 2: ExecutionContext Memory (Langzeit)
**Zweck**: Persistent Memory über Sessions & Branches
- Speichert Architectural Decisions
- Projekt-Kontext (Git-Branch, Files)
- Phase-übergreifendes Wissen

**Lebensdauer**: Projekt-Lifetime (Tage, Wochen, Monate)

**Beispiel**:
```json
{
  "projectPath": "/path/to/project",
  "gitBranch": "feature/new-endpoint",
  "phaseHistory": [...],
  "architecturalDecisions": [
    {
      "phase": "Architecture Design",
      "decision": "Use Hexagonal Architecture",
      "reasoning": "Better testability and maintainability",
      "date": "2025-10-29"
    }
  ]
}
```

#### Integration: Memory-Bridge

```kotlin
// Start Phase: Long-Term → Koog
val context = memoryRepo.load(projectPath)
val koogWorkflow = KoogWorkflowExecutor(
    initialContext = context.toKoogContext()
)

// Ende Phase: Koog → Long-Term
val phaseSummary = koogWorkflow.getSummary()
context.addPhaseResult(
    phase = "Requirements Analysis",
    summary = phaseSummary.compressed,
    decisions = phaseSummary.decisions
)
memoryRepo.save(context)
```

**Vorteile**:
- ✅ Trennung der Verantwortlichkeiten
- ✅ Koog bleibt austauschbar
- ✅ Optimale Performance (komprimiert + persistent)
- ✅ Git-Aware (Branch-spezifische Contexts)

---

## Projektstruktur

```
responsible-vibe-mcp/
├── WARP.md                                    # Diese Datei
├── README.md                                  # Projektübersicht
├── pom.xml                                    # Maven Configuration
├── src/
│   ├── main/
│   │   ├── kotlin/ch/zuegi/rvmcp/
│   │   │   ├── RvmcpApplication.kt            # Spring Boot Application
│   │   │   ├── domain/                        # 🔷 Domain Layer (Kern)
│   │   │   │   ├── model/                     # ✅ Entities, Value Objects
│   │   │   │   │   ├── process/               # EngineeringProcess, ProcessExecution
│   │   │   │   │   ├── phase/                 # ProcessPhase, PhaseResult
│   │   │   │   │   ├── context/               # ExecutionContext
│   │   │   │   │   ├── vibe/                  # VibeCheck, VibeCheckResult
│   │   │   │   │   ├── memory/                # Decision, Interaction, Artifact
│   │   │   │   │   ├── id/                    # ProcessId, ExecutionId
│   │   │   │   │   └── status/                # ExecutionStatus, VibeCheckType, etc.
│   │   │   │   ├── port/                      # ✅ Port Interfaces
│   │   │   │   │   ├── input/                 # ✅ Use Case Interfaces (3)
│   │   │   │   │   └── output/                # ✅ Repository/Provider Interfaces (4)
│   │   │   │   │       └── model/             # ✅ Output Models (2)
│   │   │   │   └── service/                   # Domain Services
│   │   │   ├── application/                   # 🔷 Application Layer
│   │   │   │   └── workflow/                  # Use Case Implementations
│   │   │   ├── adapter/                       # 🔷 Adapter Layer
│   │   │   │   ├── input/
│   │   │   │   │   ├── cli/                   # CLI Interface
│   │   │   │   │   └── mcp/                   # MCP Protocol Handler
│   │   │   │   └── output/
│   │   │   │       ├── ai/                    # Kotlin Koog Integration
│   │   │   │       ├── memory/                # Persistence Implementations
│   │   │   │       └── git/                   # Git Integration
│   │   │   └── infrastructure/                # 🔷 Infrastructure
│   │   │       └── config/                    # Spring Configuration
│   │   └── resources/
│   │       ├── application.yml
│   │       └── workflows/                     # Workflow Definitions (YAML/JSON)
│   └── test/
│       └── kotlin/ch/zuegi/rvmcp/             # ✅ 36 Unit Tests
└── docs/
    ├── architecture.md                        # Architekturentscheidungen
    ├── workflows.md                           # Detaillierte Workflow-Beschreibungen
    └── tutorial.md                            # Getting Started Tutorial
```

---

## Nächste Schritte

### Phase 1: Grundgerüst (MVP) ✅ ABGESCHLOSSEN
- [x] Maven Projekt aufsetzen (pom.xml)
- [x] Hexagonale Architektur-Struktur erstellen
- [x] Konzeptionelle Architektur definieren (Process vs. Workflow Trennung)
- [x] Domain Model implementieren:
  - [x] EngineeringProcess (Entity)
  - [x] ProcessPhase (Value Object)
  - [x] ProcessExecution (Entity)
  - [x] ExecutionContext (Entity)
  - [x] VibeCheck / VibeCheckResult (Value Objects)
  - [x] Supporting: ProcessId, ExecutionId, ExecutionStatus, Decision, Interaction, Artifact
  - [x] Unit Tests (36 Tests erfolgreich)
- [x] Port Interfaces definieren:
  - [x] input: StartProcessExecutionUseCase, ExecuteProcessPhaseUseCase, CompletePhaseUseCase
  - [x] output: WorkflowExecutionPort, MemoryRepositoryPort, VibeCheckEvaluatorPort, ProcessRepositoryPort
  - [x] output/model: WorkflowExecutionResult, WorkflowSummary
- [x] Domain Services implementieren (Business Logic):
  - [x] StartProcessExecutionService
  - [x] ExecuteProcessPhaseService
  - [x] CompletePhaseService
- [x] Dummy-Adapter implementieren (Testing ohne KI):
  - [x] ManualWorkflowExecutor (CLI-basiert)
  - [x] ConsoleVibeCheckEvaluator (manuelle Ja/Nein Fragen)
  - [x] InMemoryMemoryRepository (temporärer Speicher)
  - [x] InMemoryProcessRepository (Process Definitions)
- [x] ManualTestRunner (End-to-End Test ohne KI)
- [x] Vollständiger Durchlauf: Feature Development Prozess mit 3 Phasen

### Phase 1.5: Kotlin Koog Integration ✅ ABGESCHLOSSEN
- [x] YAML Workflow Templates erstellen:
  - [x] simple-test.yml (1 LLM node)
  - [x] multi-node-test.yml (2 LLM nodes mit Context-Preservation)
  - [x] three-node-test.yml (3 LLM nodes mit Chain-Test)
- [x] Kotlin Koog Integration (Output Adapter):
  - [x] YamlToKoogStrategyTranslator (YAML → Koog Strategy Graph)
  - [x] WorkflowPromptBuilder (System Prompts für Workflows)
  - [x] RefactoredKoogWorkflowExecutor (Single-Agent-Architektur)
  - [x] YAML Parser (via YamlWorkflowTemplateParser)
- [x] Performance-Optimierung: 11x Speedup (900ms/node statt 10s/node)
- [x] Context-Preservation Tests (Secret Code, City-Landmark Chain)
- [x] Integration mit Azure OpenAI Gateway
- [x] Security Refactoring: Hardcoded URLs entfernt, LlmProperties Configuration
- [ ] Application Layer (Use Case Implementierungen) - verschoben zu Phase 2
- [ ] Spring Boot Configuration (Infrastructure Layer) - verschoben zu Phase 2

### Phase 1.6: End-to-End Proof-of-Concept ✅ ABGESCHLOSSEN
- [x] SimpleEndToEndTest implementiert (4 umfassende Tests):
  - [x] Single Phase Execution (Requirements Analysis mit LLM Workflow)
  - [x] Multi-Phase Execution (alle 3 Phasen: Requirements → Architecture → Implementation)
  - [x] Error Handling: Failed Required Vibe Check
  - [x] Error Handling: Process Not Found Exception
- [x] Helper Classes für Testing:
  - [x] AutoPassVibeCheckEvaluator (Success-Szenarien)
  - [x] FailingVibeCheckEvaluator (Error-Szenarien)
- [x] Architektur-Validierung End-to-End:
  - [x] Domain Services orchestrieren Flow korrekt
  - [x] Ports & Adapters Pattern funktioniert vollständig
  - [x] Koog Integration führt echte LLM Workflows aus
  - [x] In-Memory Persistence speichert Resultate korrekt
  - [x] Vibe Checks werden automatisiert durchgeführt
  - [x] Error Handling funktioniert wie erwartet

### Phase 2a: MCP Server Implementation ✅ 100% COMPLETE
- [x] MCP Protocol Library Integration
  - [x] JSON-RPC 2.0 Support (MCP SDK 0.7.6)
  - [x] stdio Transport (StdioServerTransport)
- [x] Hexagonal Architecture Implementation (Application Layer)
  - [x] StartProcessExecutionUseCaseImpl
  - [x] ExecuteProcessPhaseUseCaseImpl  
  - [x] CompletePhaseUseCaseImpl
  - [x] ApplicationConfiguration (Spring Bean wiring)
- [x] MCP Server Adapter implementieren
  - [x] ResponsibleVibeMcpServer.kt (Entry Point)
  - [x] MCP SDK API exploration (CallToolRequest.arguments)
  - [x] Parameter extraction via JsonElement.jsonPrimitive.content
- [x] MCP Tools implementieren (5 von 5 Tools) ✅
  - [x] list_processes (vollständig funktional)
  - [x] start_process (vollständig funktional)
  - [x] get_context (vollständig funktional)
  - [x] execute_phase (implementiert, siehe Bekannte Limitierungen)
  - [x] complete_phase (vollständig funktional mit Phase-Wechsel)
- [x] YAML-basierte Prozess-Initialisierung
  - [x] YamlProcessLoader (lädt Workflows aus YAML)
  - [x] ProcessInitializer (lädt Prozesse beim Start)
  - [x] simple-test.yml in src/main/resources kopiert
- [x] LLM Health Check beim Start
  - [x] LlmHealthCheck (@PostConstruct)
  - [x] Validiert Endpoint-Erreichbarkeit
  - [x] Zeigt Konfigurations-Probleme beim Start
- [x] Integration mit Domain Services (Use Cases rufen Domain Services auf)
- [x] MCP Server Tests (API Exploration Tests)
- [x] Main Entry Point für MCP Server Mode (McpServerConfiguration)
- [x] Integration Tests für MCP Protocol (6 Tests, alle passing)
- [x] Warp Agent Integration getestet (manueller End-to-End Test)
- [ ] MCP Resources implementieren (optional - verschoben zu Phase 3)
  - [ ] ContextResource (context://project/branch)
  - [ ] ProcessResource (process://process-id)

**Bekannte Limitierungen:**
- execute_phase mit komplexen Workflows (requirements-analysis.yml: 7 Nodes) dauert >2 Min
- Warp MCP-Call Timeout bei langen Workflows
- Workaround: simple-test.yml (1 LLM Node) für schnelle Tests
- SimpleLLMConnectionTest validiert LLM-Connection funktioniert (472ms Response)

### Phase 2b: Memory & Persistenz
- [ ] Persistentes Memory (Datei-basiert oder DB)
- [ ] Kontext-Speicherung & -Wiederherstellung
- [ ] Branch-Awareness (Git-Integration)

### Phase 3: Workflows erweitern
- [ ] Bug-Fix Workflow
- [ ] Refactoring Workflow
- [ ] Testing Workflow

### Phase 4: Tutorial & Documentation
- [ ] "To-Do-App im Terminal" Tutorial
- [ ] Workflow-Dokumentation
- [ ] Best Practices Guide

---

## Tutorial: Erste Schritte

**Ziel**: Eine einfache Terminal-To-Do-App bauen – strukturiert, reflektiert, schrittweise.

1. **Anforderungen definieren**
   - Was soll die App können?
   - Welche Commands? (add, list, done, delete)

2. **Architektur entwerfen**
   - Wie speichern wir die To-Dos?
   - Wie strukturieren wir den Code?

3. **Implementierung**
   - Command Parser
   - Storage Layer
   - Business Logic

4. **Testing**
   - Unit Tests
   - Integration Tests

5. **Iteration**
   - Features hinzufügen
   - Bugs fixen
   - Refactoring

---

## Kernaussagen

✅ **Struktur statt Wildwuchs**: KI arbeitet entlang definierter Engineering-Phasen  
✅ **Bewusstes Vorgehen**: Architektur, Tests und Integration werden gezielt reflektiert  
✅ **Persistenz & Kontinuität**: Projektkontexte bleiben über Sessions hinweg erhalten  
✅ **Flexible Workflows**: Unterschiedliche Szenarien erhalten passende Prozesse  
✅ **KI als Partner**: Die KI agiert als Mitentwicklerin, nicht bloß als Werkzeug

---

## Status

**Aktueller Stand**: 🎉 **Phase 2a: 95% COMPLETE** - MCP Server vollständig implementiert mit allen 5 Tools und Integration Tests!

### Implementiert
- ✅ **Phase 1-1.6 ABGESCHLOSSEN**: Komplette Domain & Workflow Engine
- ✅ Domain Model, Port Interfaces & Domain Services (36 Tests)
- ✅ YAML Workflow Templates (simple-test, multi-node-test, three-node-test)
- ✅ Kotlin Koog Integration mit Azure OpenAI Gateway
- ✅ KoogWorkflowExecutor (vorher RefactoredKoogWorkflowExecutor) mit 11x Speedup
- ✅ YamlToKoogStrategyTranslator (unterstützt 1-3 LLM nodes)
- ✅ WorkflowPromptBuilder für umfassende System-Prompts
- ✅ **Context-Preservation VERIFIED**: Agent behält Kontext über alle Nodes
- ✅ **Application Layer (Hexagonal Architecture)**:
  - StartProcessExecutionUseCaseImpl
  - ExecuteProcessPhaseUseCaseImpl
  - CompletePhaseUseCaseImpl
  - ApplicationConfiguration mit Spring Bean Wiring
- ✅ **MCP Server (5 von 5 Tools KOMPLETT)** 🎉:
  - ResponsibleVibeMcpServer mit stdio Transport
  - list_processes Tool (✅ komplett)
  - start_process Tool (✅ komplett)
  - get_context Tool (✅ komplett)
  - execute_phase Tool (✅ komplett - mit Execution State Management)
  - complete_phase Tool (✅ komplett - mit Phase-Wechsel)
  - CallToolRequest.arguments Parameter Extraction
- ✅ **McpServerConfiguration**: Main Entry Point mit keep-alive Mechanismus
  - Automatischer Start (außer in Tests mit @Profile("!local"))
  - CountDownLatch + ShutdownHook für sauberes Herunterfahren
- ✅ **Integration Tests**: McpProtocolIntegrationTest (6 Tests)
  - list_processes: Repository integration
  - start_process: Process execution
  - get_context: Memory retrieval
  - execute_phase: Workflow execution mit LLM
  - complete_phase: Phase completion und Phase-Wechsel
  - Error Handling: Process Not Found
- ✅ **End-to-End Tests**: SimpleEndToEndTest (4 Tests)
  - Single Phase Execution mit echtem LLM Workflow
  - Multi-Phase Execution (3 Phasen)
  - Error Handling (Failed Vibe Checks, Process Not Found)
- ✅ **Comprehensive Test Suite: 64 Tests (alle passing)**

### Performance-Verbesserung (Gemessen)
| Szenario | Alt | Neu | Speedup |
|----------|-----|-----|----------|
| 1 LLM Node | ~10s | **1.3s** | **7.7x** |
| 2 LLM Nodes | ~20s | **1.8s** | **11x** |
| 3 LLM Nodes | ~30s | **2.7s** | **11x** |
| Avg pro Node | 10s | **900ms** | **11x** |

**Grund**: 
- ✅ Einmaliger Agent statt Agent-per-Node
- ✅ Context-Preservation durch Koog Strategy Graph
- ✅ Lazy Executor Initialisierung
- ✅ CIO Engine statt Apache5 (keine extra Dependencies)

### Context-Preservation Tests (BESTANDEN)
- ✅ **Secret Code Test**: Agent erinnert sich an 4-stelligen Code aus Step 1
- ✅ **City-Landmark Chain**: Agent nutzt City aus Step 1 für Landmark in Step 2
- ✅ **3-Node Summary**: Agent fasst alle 3 Steps korrekt zusammen

### Test-Übersicht (64 Tests passing) 🎉
- ✅ 36 Domain Model Tests (Entities, Value Objects)
- ✅ 7 Port Output Model Tests
- ✅ 6 KoogIntegrationTests (Simple, Multi-Node, Three-Node, etc.)
- ✅ 1 SimpleLLMConnectionTest
- ✅ **4 End-to-End Tests (SimpleEndToEndTest)**:
  - Single Phase Execution (Requirements Analysis)
  - Multi-Phase Execution (Complete Feature Development)
  - Failed Required Vibe Check Handling
  - Process Not Found Exception
- ✅ **6 MCP Protocol Integration Tests (McpProtocolIntegrationTest)** 🆕:
  - list_processes tool call
  - start_process tool call and execution creation
  - get_context tool call and context retrieval
  - execute_phase tool call with LLM workflow
  - complete_phase tool call with phase advancement
  - Error handling for process not found
- ✅ 4 andere Tests

### Aktuelle Limitierungen
- YamlToKoogStrategyTranslator unterstützt max. 3 LLM-Nodes (TODO: beliebig viele)
- Conditional & Human-Interaction Nodes noch nicht unterstützt
- MCP Resources noch nicht implementiert (optional - verschoben zu Phase 3)
- Manueller Test mit Claude Desktop / Warp ausstehend

### Nächste Schritte (Phase 2a final abschließen)
1. ⏳ **Claude Desktop / Warp Integration** - Manueller Test mit echtem MCP Client
2. ✅ Dokumentation aktualisiert

### Phase 2b: Memory & Persistenz (Next)
1. ⏳ Persistentes Memory (Datei-basiert oder DB)
2. ⏳ Kontext-Speicherung & -Wiederherstellung
3. ⏳ Branch-Awareness (Git-Integration)

### Weitere Zukunft (Phase 3+)
4. ⏳ MCP Resources implementieren (optional)
5. ⏳ Erweitern auf beliebig viele LLM-Nodes im YamlToKoogStrategyTranslator
6. ⏳ Support für Conditional Nodes (Tool-based oder Strategy Branches)
7. ⏳ Support für Human-Interaction Nodes (Tool-based)
8. ⏳ Bug-Fix Workflow
9. ⏳ Refactoring Workflow

---

## Architektur-Entscheidungen

### ADR-001: Process vs. Workflow Trennung
**Entscheidung**: Engineering-Prozesse (Domain) getrennt von Kotlin Koog Workflows (Execution)  
**Begründung**: 
- Klarheit der Konzepte
- Domain bleibt framework-unabhängig
- Koog-Workflows sind austauschbar

### ADR-002: Vibe Checks als eigene Domain-Objekte
**Entscheidung**: VibeCheck als separates Value Object mit eigener Validation-Logik  
**Begründung**:
- Zentral für das Konzept
- Konfigurierbar pro ProcessPhase
- Erweiterbar (AI-gestützte Evaluation)
- Testbar

### ADR-003: Hybrid Human-in-the-Loop
**Entscheidung**: Obligatorisch nach jeder Phase + automatisch bei kritischen Entscheidungen  
**Begründung**:
- User behält Kontrolle
- Nicht bei jedem LLM-Call unterbrechen
- Balance zwischen Autonomie und Oversight

### ADR-004: YAML für Koog Workflows
**Entscheidung**: Workflow-Definitionen als YAML statt Kotlin DSL  
**Begründung**:
- Einfacher editierbar ohne Rebuild
- Nicht-Entwickler können Workflows anpassen
- Standard-Format für Workflow-Definitionen

### ADR-005: Zwei-Ebenen-Memory-Architektur
**Entscheidung**: Koog's Intelligent History Compression (Kurzzeit) + ExecutionContext (Langzeit)  
**Begründung**:
- **Trennung der Concerns**: Koog optimiert Conversational Memory, ExecutionContext speichert Projektwissen
- **Framework-Unabhängigkeit**: ExecutionContext bleibt stabil, auch wenn Koog ausgetauscht wird
- **Performance**: Koog komprimiert während Execution, nur Essentials landen im Long-Term Memory
- **Git-Awareness**: Branch-spezifische Contexts für parallele Feature-Entwicklung
- **Token-Effizienz**: Keine redundanten Informationen in LLM-Calls

**Implementierung**: Memory-Adapter als Bridge zwischen beiden Ebenen

### ADR-006: End-to-End Tests mit Helper Evaluators
**Entscheidung**: SimpleEndToEndTest mit AutoPassVibeCheckEvaluator und FailingVibeCheckEvaluator  
**Begründung**:
- **Testbarkeit**: End-to-End Tests brauchen deterministische Vibe Check Ergebnisse
- **Keine LLM-Calls für Vibe Checks**: Tests sollen unabhängig von LLM-Verfügbarkeit laufen
- **Success & Error Szenarien**: Beide Evaluators ermöglichen vollständige Test-Abdeckung
- **Schnelligkeit**: Keine echten LLM-Calls für Vibe Checks = schnellere Tests

**Implementierung**:
- `AutoPassVibeCheckEvaluator`: Alle Checks passen automatisch (Success-Pfad)
- `FailingVibeCheckEvaluator`: Required Checks failen automatisch (Error-Pfad)
- Helper Klassen im Test-File, wiederverwendbar für weitere Tests

---

*"Zehn Minuten Design vermeiden drei Stunden Refactoring"*
