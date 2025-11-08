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
- [ ] Application Layer (Use Case Implementierungen) - verschoben zu Phase 2
- [ ] Spring Boot Configuration (Infrastructure Layer) - verschoben zu Phase 2

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

**Aktueller Stand**: ✅ **Phase 1.5 ABGESCHLOSSEN** - Refactored Koog Integration mit Context-Preservation!

### Implementiert
- ✅ Domain Model, Port Interfaces & Domain Services (36 Tests)
- ✅ YAML Workflow Templates (simple-test, multi-node-test, three-node-test)
- ✅ Kotlin Koog Integration mit Azure OpenAI Gateway
- ✅ **REFACTORED**: Single-Agent-per-Workflow Architektur
- ✅ YamlToKoogStrategyTranslator (unterstützt 1-3 LLM nodes)
- ✅ WorkflowPromptBuilder für umfassende System-Prompts
- ✅ RefactoredKoogWorkflowExecutor mit dramatisch verbesserter Performance
- ✅ **Context-Preservation VERIFIED**: Agent behält Kontext über alle Nodes
- ✅ Comprehensive Test Suite (54 Tests, alle passing)

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

### Test-Übersicht (54 Tests passing)
- ✅ 36 Domain Model Tests (Entities, Value Objects)
- ✅ 7 Port Output Model Tests
- ✅ 6 KoogIntegrationTests (Simple, Multi-Node, Three-Node, etc.)
- ✅ 1 SimpleLLMConnectionTest
- ✅ 4 andere Tests

### Aktuelle Limitierungen
- Translator unterstützt max. 3 LLM-Nodes (TODO: beliebig viele)
- Conditional & Human-Interaction Nodes noch nicht unterstützt
- Aggregation & System-Command Nodes werden übersprungen
- Old KoogWorkflowExecutor noch vorhanden (zur Referenz)

### Nächste Schritte
1. ⏳ Erweitern auf beliebig viele LLM-Nodes
2. ⏳ Support für Conditional Nodes (Tool-based oder Strategy Branches)
3. ⏳ Support für Human-Interaction Nodes (Tool-based)
4. ⏳ Application Layer (Use Case Implementierungen)
5. ⏳ Old KoogWorkflowExecutor entfernen

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
