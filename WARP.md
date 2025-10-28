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

### 3. Refactoring
- Code-Smells identifizieren
- Zielarchitektur definieren
- Schrittweise Transformation
- Tests sicherstellen Regressionssicherheit

---

## Tech Stack

- **Sprache**: Kotlin
- **Framework**: Spring Boot
- **Agentic AI**: Kotlin Koog
- **Build Tool**: Maven
- **Version Control**: Git
- **Architektur**: Hexagonal Architecture (Ports & Adapters)

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
│   │   │   │   ├── model/                     # Entities, Value Objects
│   │   │   │   ├── port/
│   │   │   │   │   ├── input/                 # Use Case Interfaces
│   │   │   │   │   └── output/                # Repository/Provider Interfaces
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
│       └── kotlin/ch/zuegi/rvmcp/
└── docs/
    ├── architecture.md                        # Architekturentscheidungen
    ├── workflows.md                           # Detaillierte Workflow-Beschreibungen
    └── tutorial.md                            # Getting Started Tutorial
```

---

## Nächste Schritte

### Phase 1: Grundgerüst (MVP)
- [x] Maven Projekt aufsetzen (pom.xml)
- [x] Hexagonale Architektur-Struktur erstellen
- [ ] Domain Model definieren (Workflow, Phase, Context)
- [ ] Port Interfaces definieren (input/output)
- [ ] Spring Boot Basis-Applikation erstellen
- [ ] Kotlin Koog Integration (Output Adapter)
- [ ] Basis Memory-System (In-Memory Output Adapter)
- [ ] Einfacher Workflow: "Neues Feature"

### Phase 2: Memory & Persistenz
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

**Aktueller Stand**: Maven Projekt mit hexagonaler Architektur aufgesetzt  
**Nächster Schritt**: Domain Model definieren (Workflow, Phase, Context)

---

*"Zehn Minuten Design vermeiden drei Stunden Refactoring"*
