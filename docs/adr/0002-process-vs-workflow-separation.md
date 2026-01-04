# ADR-002: Process vs. Workflow Trennung

**Status:** Accepted  
**Datum:** 2025-12-15  
**Entscheider:** Engineering Team

---

## Context

Im System gibt es zwei unterschiedliche Konzepte die beide als "Workflow" bezeichnet werden könnten:

1. **Engineering-Prozess:** Methodisches Vorgehen (Requirements → Architecture → Implementation)
2. **Kotlin Koog Workflow:** Technischer Execution-Graph für Agent-Aktionen (YAML-basiert)

Diese begriffliche Überlappung führte zu Verwirrung und unklar strukturiertem Code.

---

## Decision

Wir trennen konzeptionell klar:

- **EngineeringProcess (Domain):** Business-orientierte Prozess-Definition
  - Definiert Phasen (Requirements Analysis, Architecture Design, etc.)
  - Enthält Vibe Checks
  - Referenziert Koog Workflow Templates
  - Framework-unabhängig

- **Koog Workflow (Execution):** Technische Ausführungs-Definition
  - YAML-basierte Workflow-Definition
  - Nodes, Edges, Conditions
  - Koog-spezifisch
  - Austauschbar

---

## Rationale

### Vorteile

1. **Klarheit der Konzepte**
   - Engineering-Prozess beschreibt "Was" und "Warum"
   - Koog Workflow beschreibt "Wie" technisch

2. **Domain bleibt framework-unabhängig**
   - EngineeringProcess kennt Koog nicht
   - Koog könnte durch anderes Framework ersetzt werden
   - Business-Logik unabhängig von Execution-Framework

3. **Koog-Workflows sind austauschbar**
   - Gleicher EngineeringProcess kann verschiedene Workflows nutzen
   - Workflows können optimiert/ersetzt werden ohne Domain zu ändern

4. **Bessere Testbarkeit**
   - Domain-Tests ohne Koog-Abhängigkeit
   - Workflow-Tests isoliert

---

## Consequences

### Positive

- ✅ Klare Architektur-Schichten
- ✅ Domain unabhängig von Koog
- ✅ Workflows können ausgetauscht werden
- ✅ Bessere Verständlichkeit des Codes

### Negative

- ⚠️ Zusätzliche Abstraktionsschicht
- ⚠️ Mapping zwischen Domain und Koog nötig

### Neutral

- 🔄 EngineeringProcess referenziert Koog-Template über String (Pfad)
- 🔄 WorkflowExecutionPort als Boundary zwischen Domain und Koog

---

## Implementation

```kotlin
// Domain Layer
data class EngineeringProcess(
    val id: ProcessId,
    val name: String,
    val phases: List<ProcessPhase>
)

data class ProcessPhase(
    val name: String,
    val vibeChecks: List<VibeCheck>,
    val koogWorkflowTemplate: String  // "workflows/requirements-analysis.yml"
)

// Adapter Layer
class KoogWorkflowExecutor : WorkflowExecutionPort {
    override suspend fun executeWorkflow(
        template: String,
        context: ExecutionContext
    ): WorkflowExecutionResult {
        // Load YAML, translate to Koog Strategy, execute
    }
}
```

---

## Related Decisions

- [ADR-004: Hybrid Human-in-the-Loop](0004-hybrid-human-in-the-loop.md)
- [ADR-008: CoroutineContext für Workflow Interruption](0001-coroutine-context-for-workflow-interruption.md)

---

## Notes

Diese Trennung hat sich als fundamental für die Architektur erwiesen. Die klare Boundary zwischen Business-Prozess und technischer Execution ermöglicht Flexibilität und Testbarkeit.

