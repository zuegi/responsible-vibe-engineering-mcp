# ADR-004: Hybrid Human-in-the-Loop

**Status:** Accepted  
**Datum:** 2025-12-15  
**Entscheider:** Engineering Team

---

## Context

AI-gestützte Entwicklung kann vollautomatisch oder mit menschlicher Oversight erfolgen. 

**Probleme:**
- **Zu viel Automatisierung:** Führt zu unkontrollierten Ergebnissen, User verliert Kontrolle
- **Zu viel manuelle Intervention:** Unterbricht Flow, macht KI-Vorteile zunichte

**Frage:** Wann soll der User in den Workflow eingreifen?

---

## Decision

**Hybrid-Ansatz: Obligatorisch nach jeder Phase + automatisch bei kritischen Entscheidungen**

### Obligatorische Interventionspunkte:
1. **Nach jeder ProcessPhase**
   - System zeigt Zusammenfassung der Phase
   - User bestätigt oder lehnt ab
   - Bei Ablehnung: Phase wiederholen

### Automatische Interventionspunkte:
2. **Bei kritischen Architektur-Entscheidungen**
3. **Bei Breaking Changes**
4. **Bei Failed Vibe Checks**
5. **Bei erkannten Ambiguitäten**

---

## Rationale

### Vorteile

1. **User behält Kontrolle**
   - Über Projekt-Richtung
   - Kann jederzeit korrigieren
   - Transparenz über System-Aktionen

2. **Flow bleibt erhalten**
   - Nicht bei jedem LLM-Call unterbrechen
   - Nur an sinnvollen Checkpoints
   - Balance zwischen Autonomie und Oversight

3. **Qualitätssicherung**
   - Vibe Checks als Quality Gates
   - User als finaler Validator
   - Frühe Fehler-Erkennung

4. **Flexibility**
   - User kann Phase wiederholen
   - User kann Workflow pausieren
   - User kann Feedback geben

---

## Consequences

### Positive

- ✅ User hat Kontrolle ohne Mikromanagement
- ✅ Automatisierung wo möglich, menschliche Expertise wo nötig
- ✅ Transparente System-Aktionen
- ✅ Frühe Fehler-Erkennung

### Negative

- ⚠️ User muss nach jeder Phase interagieren (kann nicht komplett autonom laufen)
- ⚠️ Bei vielen Phasen mehrfache Unterbrechungen

### Neutral

- 🔄 User kann Workflow pausieren und später fortsetzen
- 🔄 System muss Pause/Resume unterstützen (siehe ADR-001)

---

## Implementation

### Via InteractionContextElement

```kotlin
// Workflow wird pausiert bei ask_user
suspend fun askUser(question: String): String {
    val element = coroutineContext[InteractionContextElement]
        ?: throw IllegalStateException("No interaction context")
    
    // Pause workflow
    element.pendingQuestion = question
    throw WorkflowPausedException("Waiting for user input")
}
```

### Via AskUserTool (Koog Integration)

```kotlin
class AskUserTool : SimpleTool {
    override suspend fun execute(args: Args): String {
        // Pause Koog workflow
        // MCP Client receives pending interaction
        // User provides answer via provide_answer tool
        // Workflow resumes
    }
}
```

### User Experience Flow

```
1. execute_phase → Workflow startet
2. Phase läuft (LLM, Tools, etc.)
3. Phase completed → System zeigt Zusammenfassung
4. User: complete_phase → Bestätigung
5. System: Nächste Phase oder Process complete
```

---

## Alternatives Considered

### Alternative 1: Vollautomatisch

**Pros:**
- Schneller, keine Unterbrechungen
- Kann unbeaufsichtigt laufen

**Cons:**
- ❌ User verliert Kontrolle
- ❌ Fehler werden spät erkannt
- ❌ Keine Anpassungsmöglichkeit

**Decision:** Rejected - zu riskant

### Alternative 2: Mikromanagement

**Pros:**
- Maximale Kontrolle
- Jede Entscheidung bestätigt

**Cons:**
- ❌ Flow wird komplett unterbrochen
- ❌ User wird mit Details überflutet
- ❌ KI-Vorteile gehen verloren

**Decision:** Rejected - zu aufwändig

### Alternative 3: Hybrid (Chosen)

**Pros:**
- ✅ Balance zwischen Autonomie und Kontrolle
- ✅ Checkpoints an sinnvollen Stellen
- ✅ Flow weitgehend erhalten

**Cons:**
- ⚠️ Komplexere Implementation (Pause/Resume)

**Decision:** ✅ Accepted - beste Balance

---

## Validation

### Success Criteria

- ✅ User kann nach jeder Phase interagieren
- ✅ User kann Phase ablehnen und wiederholen
- ✅ Workflow kann pausiert und resumed werden
- ✅ Kritische Entscheidungen triggern automatisch Intervention
- ✅ Flow ist nicht übermäßig unterbrochen

### Metrics

- Durchschnittliche Anzahl Interventionen pro Process
- User-Zufriedenheit mit Balance
- Anzahl abgelehnter Phasen

---

## Related Decisions

- [ADR-001: CoroutineContext für Workflow Interruption](0001-coroutine-context-for-workflow-interruption.md) - Technische Grundlage für Pause/Resume
- [ADR-002: Process vs. Workflow Trennung](0002-process-vs-workflow-separation.md) - Definiert ProcessPhase als Interventionspunkt

---

## Notes

Diese Balance zwischen Autonomie und Kontrolle ist zentral für "Responsible Vibe Engineering". Der User ist aktiver Entwicklungspartner, nicht passiver Konsument von KI-generierten Lösungen.

