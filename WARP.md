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
- [x] Konzeptionelle Architektur definieren (Process vs. Workflow Trennung)
- [ ] Domain Model implementieren:
  - [ ] EngineeringProcess (Entity)
  - [ ] ProcessPhase (Value Object)
  - [ ] ProcessExecution (Entity)
  - [ ] ExecutionContext (Entity)
  - [ ] VibeCheck / VibeCheckResult (Value Objects)
  - [ ] Supporting: ProcessId, ExecutionId, ExecutionStatus, Decision, Interaction, Artifact
- [ ] Port Interfaces definieren:
  - [ ] input: ExecuteProcessPhaseUseCase
  - [ ] output: WorkflowExecutionPort, MemoryRepositoryPort
- [ ] YAML Workflow Templates erstellen:
  - [ ] requirements-analysis.yml
  - [ ] architecture-design.yml
  - [ ] implementation.yml
- [ ] Kotlin Koog Integration (Output Adapter):
  - [ ] KoogWorkflowExecutor (YAML → Koog Graph)
  - [ ] YAML Parser
- [ ] Application Layer:
  - [ ] ExecuteProcessPhaseUseCase (Implementierung)
  - [ ] VibeCheck Evaluation Logic
  - [ ] Human-in-the-Loop Trigger Logic
- [ ] Basis Memory-System (In-Memory Output Adapter)
- [ ] Spring Boot Basis-Applikation
- [ ] CLI Adapter (Input) für Testing

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

**Aktueller Stand**: Konzeptionelle Architektur definiert (Process vs. Workflow Trennung, Vibe Checks, Human-in-the-Loop)  
**Nächster Schritt**: Domain Model implementieren (EngineeringProcess, ProcessPhase, ExecutionContext, VibeCheck)

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

---

*"Zehn Minuten Design vermeiden drei Stunden Refactoring"*
