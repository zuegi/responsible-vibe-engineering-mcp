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
3. **Dokumentation** als Markdown-Files (in Entwicklung)
4. Wiederhole Schritte 1-3 und verbessere
5. **Implementierung** basierend auf dokumentierten Requirements
6. **Testing** mit definierter Strategie
7. Integration & Review

**Neu: Question Catalogs** – Strukturierte Fragelisten pro Phase garantieren Vollständigkeit und erzeugen wiederverwendbare Projektdokumentation.

## Features

- ✅ **MCP Server** – Standardisiertes Interface für KI-Systeme (Claude, Warp, IDEs)
- ✅ **Strukturierte Workflows** – Engineering-Prozesse für Feature Development, Bug-Fix, Refactoring
- ✅ **Question Catalogs** – Strukturierte Requirements-Erhebung garantiert Vollständigkeit
- ✅ **Vibe Engineering** – Qualität vor Geschwindigkeit
- ✅ **Hexagonal Architecture** – Domain unabhängig, flexible Backend-Wahl
- ✅ **Agentic AI** powered by Kotlin Koog 0.6.0

**In Entwicklung:**
- 🔄 **Document Generation** – Automatische Markdown-Dokumentation
- 🔄 **Flexible Persistence** – User wählt Backend (Git, Confluence, File) - aktuell In-Memory

## Tech Stack

**Core:**
- Kotlin
- Spring Boot
- Kotlin Koog 0.6.0 (Agentic AI Framework)
- Model Context Protocol (MCP)
- Maven

**Optional Backends (Post-MVP):**
- JGit (Git Integration)
- Confluence Client
- File System

## Getting Started

Siehe [WARP.md](WARP.md) für detaillierte Informationen und nächste Schritte.

## Projektstruktur

```
src/
├── main/kotlin/ch/zuegi/rvmcp/
│   ├── domain/                       # ✅ Domain Layer
│   │   ├── model/                    # Entities & Value Objects
│   │   │   ├── process/              # EngineeringProcess, ProcessExecution
│   │   │   ├── phase/                # ProcessPhase, PhaseResult
│   │   │   ├── context/              # ExecutionContext
│   │   │   └── ...
│   │   ├── port/                     # ✅ Port Interfaces
│   │   │   ├── input/                # Use Cases
│   │   │   └── output/               # Repository Ports
│   │   └── service/                  # ✅ Domain Services
│   ├── adapter/                      # ✅ Adapters
│   │   ├── input/mcp/                # MCP Server (6 Tools)
│   │   └── output/
│   │       ├── workflow/             # KoogWorkflowExecutor
│   │       ├── tools/                # Koog Tools (AskUser, CreateFile, QuestionCatalog)
│   │       ├── memory/               # InMemoryMemoryRepository
│   │       ├── vibe/                 # AutoPassVibeCheckEvaluator
│   │       └── process/              # YamlProcessLoader
│   ├── infrastructure/               # Spring Boot Configuration
│   └── InteractiveTestRunner.kt     # ✅ Test Runner
├── test/kotlin/ch/zuegi/rvmcp/       # ✅ 15 Test Classes
└── resources/
    └── workflows/                    # ✅ YAML Workflow Templates
```

## Status

**Current:** MVP Development (Phase 1-2 in Progress)  
**Version:** 0.1.0-SNAPSHOT

### Was funktioniert (Januar 2026)

| Feature | Status |
|---------|--------|
| MCP Server (6 Tools) | ✅ 90% |
| Koog Integration | ✅ 100% |
| Question Catalogs | ✅ 70% |
| Document Generation | 🔄 In Progress |
| Persistence Layer | 🟡 In-Memory (MVP) |
| Workflow Types | 🟡 Feature Dev only |

**Test Coverage:** 15 test classes, alle passing

### Roadmap

Siehe [WARP.md](WARP.md#implementation-roadmap) für detaillierte Implementation Roadmap mit Timeline & Milestones.

**Next Milestone:** MVP (Mitte Februar 2026)
- Phase 1: ✅ Interfaces + In-Memory Dummy
- Phase 2: 🔄 Document Generation (in progress)
- Phase 3: ⏳ Multi-Workflow Types
- Phase 4: ⏳ Polish & MCP Resources

## Lizenz

TBD
