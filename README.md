# Responsible Vibe MCP

**Engineering-getriebene KI-Entwicklung mit Kotlin, Spring Boot und Kotlin Koog**

## Was ist Responsible Vibe MCP?

Ein Framework, das KI-Systeme aktiv durch bewährte Software-Engineering-Workflows führt – von der Planung über die Architektur bis zur Implementierung.

### Das Problem

KI-Tools generieren oft Code ohne methodisches Vorgehen. Das führt zu:
- Fehlender Architekturplanung
- Unnötigem Refactoring
- Unstrukturierter Entwicklung

### Die Lösung

Responsible Vibe MCP strukturiert KI-gestützte Entwicklung in klare Phasen:
1. Anforderungsanalyse
2. Planung
3. Architekturentwurf
4. Wiederhole Schritte 1 - 3 und verbessere
5. Implementierung
6. Testing
7. Integration & Review


Idee: Das Dokument soll zum Schluss in Tranchen aufgteilt werden, welche in Stories/Changes umgesetzt werden kann, bzw. eine LLM kann daraus Stories erstellen.

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
- Kotlin Koog (Agentic AI Framework)
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
  - YAML Workflow Templates (simple-test, multi-node-test, three-node-test)
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
    - 5 MCP Tools (start_process, execute_phase, complete_phase, get_context, list_processes)
    - 2 MCP Resources (context://, process://)
    - Integration mit Claude Desktop / Warp Agent
       - für die Verwendung des MCP Server im WARP Agent verwende
        -  die [MCP Server Konfiguration für den WARP Agent](warp-mcp-config.json)
        - eine [WARP Rule](warp-rule-mcp-server), welche besagt, dass der MCP Server verwendet werden soll
 
**Nächste Schritte**:
- **Phase 2a: MCP Server Implementation** ⏳ (IN ARBEIT)
  - MCP Protocol Integration (JSON-RPC 2.0, stdio Transport)
  - 5 MCP Tools (start_process, execute_phase, complete_phase, get_context, list_processes)
  - 2 MCP Resources (context://, process://)
  - Integration mit Claude Desktop / Warp Agent
- Phase 2b: Memory & Persistenz (File-based Memory)
- Phase 3: Workflows erweitern (Bug-Fix, Refactoring, Testing)
- Phase 4: Tutorial & Documentation

## Lizenz

TBD
