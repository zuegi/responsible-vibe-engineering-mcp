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
│   │   └── port/              # ✅ Port Interfaces (vollständig)
│   │       ├── input/         # 3 Use Cases
│   │       └── output/        # 4 Repositories/Providers
│   ├── application/           # 🚧 Application Layer
│   └── adapter/               # 🚧 Adapter Layer
└── test/kotlin/ch/zuegi/rvmcp/  # ✅ 36 Unit Tests
```

## Status

🚧 **In Entwicklung** – Phase 1: Grundgerüst (MVP)

**Fertiggestellt**:
- ✅ Domain Model (Entities, Value Objects)
- ✅ Port Interfaces (Input & Output)
- ✅ 36 Unit Tests (alle erfolgreich)

**Nächste Schritte**:
- YAML Workflow Templates
- Kotlin Koog Integration (Output Adapter)
- Application Layer (Use Case Implementierungen)

## Lizenz

TBD
