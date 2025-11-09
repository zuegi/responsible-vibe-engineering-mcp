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
2. Architekturentwurf
3. Implementierung
4. Testing
5. Integration & Review

## Features

- ✅ **Strukturierte Workflows** für verschiedene Entwicklungsszenarien
- ✅ **Persistentes Memory** über Sessions hinweg
- ✅ **Vibe Engineering** – Qualität vor Geschwindigkeit
- ✅ **Git-Integration** für Branch-Awareness
- ✅ **Agentic AI** powered by Kotlin Koog

## Tech Stack

- Kotlin
- Spring Boot
- Kotlin Koog (Agentic AI Framework)
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

**Nächste Schritte**:
- Phase 2: Memory & Persistenz (File-based oder DB)
- Phase 3: Workflows erweitern (Bug-Fix, Refactoring, Testing)
- Phase 4: Tutorial & Documentation

## Lizenz

TBD
