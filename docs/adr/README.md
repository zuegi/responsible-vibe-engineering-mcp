# Architecture Decision Records (ADRs)

Dieses Verzeichnis enthält alle architektonischen Entscheidungen für Responsible Vibe MCP.

## Was sind ADRs?

Architecture Decision Records dokumentieren wichtige architektonische Entscheidungen im Projekt. Sie helfen:
- Entscheidungen nachzuvollziehen
- Kontext für zukünftige Entwickler zu bewahren
- Rationale transparent zu machen

## Format

Jeder ADR folgt dieser Struktur:
- **Status:** Proposed / Accepted / Deprecated / Superseded
- **Datum:** Wann wurde die Entscheidung getroffen
- **Context:** Warum brauchten wir eine Entscheidung
- **Decision:** Was haben wir entschieden
- **Rationale:** Warum haben wir so entschieden
- **Consequences:** Was sind die Konsequenzen

## ADR Index

| #    | Titel | Status | Datum |
|------|-------|--------|-------|
| 0001 | [CoroutineContext für Workflow Interruption](0001-coroutine-context-for-workflow-interruption.md) | Accepted | 2026-01-01 |
| 0002 | [Process vs. Workflow Trennung](0002-process-vs-workflow-separation.md) | Accepted | 2025-12-15 |
| 0003 | [Interface-First Approach](0003-interface-first-approach.md) | Accepted | 2026-01-04 |
| 0004 | [Hybrid Human-in-the-Loop](0004-hybrid-human-in-the-loop.md) | Accepted | 2025-12-15 |
| 0005 | [Kotlin String Templates](0005-kotlin-string-templates.md) | Accepted | 2026-01-04 |
| 0006 | [In-Memory Dummy mit Educational Logging](0006-in-memory-dummy-educational-logging.md) | Accepted | 2026-01-04 |
| 0007 | [Question Catalogs für strukturierte Requirements](0007-question-catalogs-structured-requirements.md) | Accepted | 2026-01-03 |

## Kern-Entscheidungen

### Interface-First (ADR-003)
Definiere Ports jetzt, implementiere Adapters später → User wählt Backend (Git, Confluence, File)

### Process ≠ Workflow (ADR-002)
EngineeringProcess (Domain) ist getrennt von Koog Workflow (Execution) → Framework-Unabhängigkeit

### CoroutineContext (ADR-001)
Nutze CoroutineContext.Element statt ThreadLocal → Coroutine-safe Workflow-Pause

### Question Catalogs (ADR-007)
Strukturierte Fragelisten statt zufällige LLM-Fragen → Garantierte Vollständigkeit

## Neue ADRs erstellen

```bash
cd docs/adr
cp 0001-template.md 00XX-your-decision.md
# Edit file
```

Nummern werden sequenziell vergeben. Status startet als "Proposed", wird zu "Accepted" wenn implementiert.

## Weiterführende Links

- [ADR Konzept by Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [WARP.md - ADR Übersicht](../../WARP.md#architektur-entscheidungen-adrs)

