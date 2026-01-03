# Responsible Vibe MCP

**Engineering-getriebene KI-Entwicklung mit Kotlin, Spring Boot und Kotlin Koog**

## Was ist Responsible Vibe MCP?

Ein **MCP (Model Context Protocol) Server**, der KI-Systeme aktiv durch bewährte Software-Engineering-Workflows führt – von der Planung über die Architektur bis zur Implementierung.

**MCP Server** = Standardisiertes Interface zwischen KI-Clients (Claude Desktop, Warp Agent, IDEs) und dem Responsible Vibe Engineering System.

*Ganz im Sinne von Think slow, act fast*

### Das Problem

KI-Tools generieren oft Code ohne methodisches Vorgehen. Das führt zu:
- Fehlender Architekturplanung
- Unnötigem Refactoring
- Unstrukturierter Entwicklung
- **Unvollständige Requirements**: LLM stellt zufällige Fragen statt systematischer Kataloge
- **Fehlende Dokumentation**: Antworten werden nicht strukturiert festgehalten

### Die Lösung

Responsible Vibe MCP strukturiert KI-gestützte Entwicklung in klare Phasen:
1. **Anforderungsanalyse** mit strukturierten Question Catalogs
2. **Architekturentwurf** mit dokumentierten Entscheidungen
3. **Dokumentation** als versionierte Markdown-Files (für AI-Coding-Tools nutzbar)
4. Wiederhole Schritte 1-3 und verbessere
5. **Implementierung** basierend auf dokumentierten Requirements
6. **Testing** mit definierter Strategie
7. Integration & Review

**Neu: Question Catalogs** – Strukturierte Fragelisten pro Phase garantieren Vollständigkeit und erzeugen wiederverwendbare Projektdokumentation.

## Features

- ✅ **MCP Server** – Standardisiertes Interface für KI-Systeme (Claude, Warp, IDEs)
- ✅ **Strukturierte Workflows** für verschiedene Entwicklungsszenarien
- ✅ **Persistentes Memory** über Sessions hinweg
- ✅ **Vibe Engineering** – Qualität vor Geschwindigkeit
- ✅ **Git-Integration** für Branch-Awareness
- ✅ **Agentic AI** powered by Kotlin Koog

## Tech Stack

- Kotlin
- Spring Boot
- Kotlin Koog 0.6.0 (Agentic AI Framework)
- Model Context Protocol (MCP)
- Maven
- Git

## Getting Started

Siehe [WARP.md](WARP.md) für detaillierte Informationen und nächste Schritte.

## Projektstruktur

```
src/
├── main/kotlin/ch/zuegi/rvmcp/
│   ├── domain/                # ✅ Domain Layer (vollständig)
│   │   ├── model/             # Entities & Value Objects
│   │   │   ├── process/       # EngineeringProcess, ProcessExecution
│   │   │   ├── phase/         # ProcessPhase, PhaseResult
│   │   │   ├── context/       # ExecutionContext
│   │   │   ├── vibe/          # VibeCheck, VibeCheckResult
│   │   │   ├── memory/        # Decision, Interaction, Artifact
│   │   │   ├── id/            # ProcessId, ExecutionId
│   │   │   └── status/        # ExecutionStatus, VibeCheckType
│   │   ├── port/              # ✅ Port Interfaces (vollständig)
│   │   │   ├── input/         # 3 Use Cases
│   │   │   └── output/        # 4 Repositories/Providers
│   │   └── service/           # ✅ Domain Services (3 Services)
│   ├── adapter/               # ✅ Dummy-Adapter (Testing)
│   │   └── output/            # 4 In-Memory Implementierungen
│   │       ├── workflow/      # ManualWorkflowExecutor
│   │       ├── memory/        # InMemoryMemoryRepository
│   │       ├── vibe/          # ConsoleVibeCheckEvaluator
│   │       └── process/       # InMemoryProcessRepository
│   ├── ManualTestRunner.kt    # ✅ CLI Test Program
│   └── application/           # 🚧 Application Layer (nächster Schritt)
└── test/kotlin/ch/zuegi/rvmcp/  # ✅ 36 Unit Tests
```

## Status

🎉 **Phase 1.6 Abgeschlossen** – End-to-End Architektur-Validierung erfolgreich!

**Fertiggestellt**:
- ✅ **Phase 1: Domain Layer**
  - Domain Model (Entities, Value Objects)
  - Port Interfaces (Input & Output)
  - Domain Services (Business Logic)
  - 36 Unit Tests

- ✅ **Phase 1.5: Kotlin Koog Integration**
  - YAML Workflow Templates:
    - `simple-test.yml` - Basic LLM connection test
    - `multi-node-test.yml` - Multi-node workflow test
    - `three-node-test.yml` - Complex workflow test
    - `interactive-test.yml` - User interaction test
    - `requirement-question-catalog.yml` - Question catalog workflow
  - RefactoredKoogWorkflowExecutor mit Single-Agent-Architektur
  - YamlToKoogStrategyTranslator
  - WorkflowPromptBuilder
  - 11x Performance-Verbesserung (10s/node → 900ms/node)
  - Context-Preservation verified
  - Azure OpenAI Gateway Integration

- ✅ **Phase 1.6: End-to-End Proof-of-Concept**
  - SimpleEndToEndTest mit 4 umfassenden Tests:
    - Single Phase Execution (Requirements Analysis)
    - Multi-Phase Execution (3 Phasen komplett)
    - Error Handling (Failed Vibe Check)
    - Error Handling (Process Not Found)
  - Architektur vollständig validiert:
    - Domain Services orchestrieren Flow
    - Ports & Adapters Pattern funktioniert
    - Koog Integration führt echte LLM Workflows aus
    - In-Memory Persistence funktional
  - **58 Tests, alle passing**

- ✅ **Phase 2a: MCP Server Implementation** 
  - MCP Protocol Integration (JSON-RPC 2.0, stdio Transport)
    - 6 MCP Tools:
      - `list_processes` - List available engineering processes
      - `start_process` - Start a new process execution
      - `execute_phase` - Execute current phase (async)
      - `get_phase_result` - Get async execution results
      - `complete_phase` - Complete phase and advance
      - `provide_answer` - Resume paused workflows
    - Integration mit Claude Desktop / Warp Agent
       - für die Verwendung des MCP Server im WARP Agent verwende
        -  die [MCP Server Konfiguration für den WARP Agent](warp-mcp-config.json)
        - eine [WARP Rule](warp-rule-mcp-server), welche besagt, dass der MCP Server verwendet werden soll

- ✅ **Phase 2c: Question Catalogs & Document Generation**
  - QuestionCatalogTool für strukturierte Fragelisten
  - QuestionCatalog Domain Model
  - Workflow Templates für Question-Driven Requirements
  - Integration mit Koog Workflow Executor

**Nächste Schritte (Current Focus)**:
- 🎯 **Phase 2d: Automatische Markdown-Dokumenten-Generierung**
  - Markdown-Files aus Workflow-Ergebnissen generieren
  - Git-Integration für versionierte Projektdokumentation
  - Context für AI-Coding-Tools (Cursor, Windsurf, etc.)
- Phase 2b: Memory & Persistenz (File-based Memory)
- Phase 3: Workflows erweitern (Bug-Fix, Refactoring, Testing)
- Phase 4: Tutorial & Documentation

## Lizenz

TBD
