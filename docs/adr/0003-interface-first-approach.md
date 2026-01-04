# ADR-003: Interface-First Approach

**Status:** Accepted  
**Datum:** 2026-01-04  
**Entscheider:** Engineering Team

---

## Context

Die Vision sieht "Git-versionierte Projektdokumentation" vor. Initial wurde geplant, direkt Git-Integration zu implementieren (JGit, 9 Tage Entwicklungszeit).

Während der Planung wurden folgende Fragen aufgeworfen:
- Was wenn User Confluence statt Git will?
- Was wenn User File-System statt Git will?
- Ist Git-Integration wirklich critical path für MVP?
- Multi-User Collaboration erfordert möglicherweise unterschiedliche Backends

---

## Decision

**Wir implementieren Ports (Interfaces) jetzt, Adapters (Implementierungen) später.**

```kotlin
// Domain Ports (Jetzt)
interface MemoryRepositoryPort { ... }
interface DocumentPersistencePort { ... }
interface VersionControlPort { ... }
interface CollaborationPort { ... }

// Adapters (Später, User-wählbar)
class GitAdapter : DocumentPersistencePort { ... }        // wenn User Git will
class ConfluenceAdapter : DocumentPersistencePort { ... } // wenn User Confluence will
class FileAdapter : DocumentPersistencePort { ... }       // wenn User File will
```

**MVP nutzt In-Memory Dummy** mit Educational Logging:
```kotlin
class InMemoryPersistence : DocumentPersistencePort {
    override suspend fun saveDocument(...) {
        logger.info("📝 Document saved (in-memory)")
        logger.info("   💡 To enable persistence: persistence.backend=git")
    }
}
```

---

## Rationale

### Vorteile

1. **Schneller zu MVP**
   - 2-3 Tage statt 9 Tage für Phase 1
   - 70% Zeitersparnis
   - Frühere Validierung des Konzepts

2. **Keine vorzeitige Festlegung**
   - Kein Commitment auf Git
   - User kann Backend später wählen
   - Bessere Entscheidungen basierend auf echten Use-Cases

3. **Maximum Flexibility**
   - Git, Confluence, File, oder Custom Backend
   - Multiple Backends parallel möglich
   - Backend-spezifische Features nutzbar

4. **Perfekte Hexagonal Architecture**
   - Domain 100% unabhängig von Infrastructure
   - Ports klar definiert
   - Adapters plug-and-play

5. **Weniger technisches Risiko**
   - Keine Git-Complexity im MVP
   - Keine JGit Dependency Probleme
   - Keine Merge-Conflict Logik nötig

### Nachteile

1. **MVP nicht persistent über Server-Restart**
   - In-Memory Dummy verliert Daten
   - Für Demo/Testing OK, für Production nicht

2. **Echter Backend später nötig**
   - 5-7 Tage für Git Adapter
   - 5-7 Tage für Confluence Adapter
   - Kann aber nach MVP parallelisiert werden

---

## Consequences

### MVP (Phase 1-4)

**Was funktioniert:**
- ✅ Alle Domain-Logik
- ✅ Document Generation
- ✅ Workflow Execution
- ✅ MCP Server Integration
- ✅ Demo & Testing

**Was nicht funktioniert:**
- ❌ Persistence über Restart
- ❌ Team Collaboration (kein Sync)
- ❌ Git Commits
- ❌ Confluence Pages

**User Experience:**
```
📝 Document saved: docs/requirements.md
   💡 Using in-memory storage (not persistent)
   
   To enable persistence, configure:
      persistence.backend=file      # Local files
      persistence.backend=git       # Git repository  
      persistence.backend=confluence # Confluence wiki
```

### Post-MVP (On-Demand)

**User konfiguriert Backend:**
```yaml
# application.yml
persistence:
  backend: git  # oder: confluence, file, inmemory
```

**Implementation Time:**
- Git Adapter: 5 Tage
- Confluence Adapter: 5 Tage
- File Adapter: 2 Tage

**Kein Refactoring nötig** - nur neuer Adapter implementieren!

---

## Alternatives Considered

### Alternative 1: Git sofort implementieren

**Pros:**
- MVP wäre persistent
- Git-versioniert wie in Vision

**Cons:**
- 9 Tage statt 2-3 Tage
- Vorzeitige Festlegung auf Git
- Git-Complexity (Conflicts, Merge, etc.)
- User will vielleicht Confluence

**Decision:** ❌ Rejected - zu langsam, zu inflexibel

### Alternative 2: File-based sofort implementieren

**Pros:**
- Schneller als Git (2 Tage)
- Persistent über Restart
- Keine Dependencies

**Cons:**
- Nicht Git-versioniert
- Kein Team-Sync
- User will vielleicht doch Git

**Decision:** ❌ Rejected - immer noch Festlegung

### Alternative 3: Interface-First (Chosen)

**Pros:**
- Schnellster MVP (2-3 Tage)
- Maximum Flexibility
- User wählt Backend
- Clean Architecture

**Cons:**
- MVP nicht persistent

**Decision:** ✅ **Accepted** - Best Balance Speed/Flexibility

---

## Implementation

### Phase 1: Ports definieren

```kotlin
interface DocumentPersistencePort {
    suspend fun saveDocument(doc: GeneratedDocument, context: ExecutionContext): Result<Unit>
    suspend fun getDocument(filename: String, context: ExecutionContext): GeneratedDocument?
    suspend fun listDocuments(context: ExecutionContext): List<DocumentMetadata>
}
```

### Phase 1: In-Memory Dummy

```kotlin
@Component
@ConditionalOnProperty("persistence.backend", havingValue = "inmemory", matchIfMissing = true)
class InMemoryPersistence : DocumentPersistencePort {
    private val documents = ConcurrentHashMap<String, GeneratedDocument>()
    
    override suspend fun saveDocument(doc: GeneratedDocument, context: ExecutionContext): Result<Unit> {
        documents[doc.filename] = doc
        logger.info("📝 Saved: ${doc.filename} (in-memory)")
        return Result.success(Unit)
    }
}
```

### Post-MVP: Git Adapter

```kotlin
@Component
@ConditionalOnProperty("persistence.backend", havingValue = "git")
class GitDocumentAdapter(private val gitPort: GitPort) : DocumentPersistencePort {
    override suspend fun saveDocument(doc: GeneratedDocument, context: ExecutionContext): Result<Unit> {
        File(context.projectPath, doc.filename).writeText(doc.content)
        gitPort.commit(listOf(doc.filename), "docs: ${doc.metadata.phase}", context)
        return Result.success(Unit)
    }
}
```

---

## Validation

### Success Criteria (MVP)

- ✅ Alle Ports definiert
- ✅ In-Memory Dummy funktioniert
- ✅ Domain Services nutzen Ports (nicht Concrete Classes)
- ✅ Tests mock Ports (nicht Implementations)
- ✅ Logging zeigt was echter Backend tun würde

### Success Criteria (Post-MVP)

- ✅ Mindestens ein echter Adapter implementiert
- ✅ Backend via Config wählbar
- ✅ Kein Domain-Code geändert (nur neuer Adapter)
- ✅ Tests weiterhin grün

---

## Related Decisions

- [ADR-002: Process vs. Workflow Trennung](0002-process-vs-workflow-separation.md)
- [ADR-006: In-Memory Dummy mit Educational Logging](0006-in-memory-dummy-educational-logging.md)

---

## Timeline Impact

**Vorher (Full Git Implementation):**
- Phase 1: 9 Tage
- MVP: Ende Februar 2026

**Nachher (Interface-First):**
- Phase 1: 2-3 Tage
- MVP: Mitte Februar 2026
- **2 Wochen gespart!**

---

## Notes

Diese Entscheidung wurde durch User-Feedback bestätigt:
> "Wie wäre es, wenn wir einfach die Interfaces erstellen und die Implementation (Git, Confluence, etc.) auf später aufschieben?"

Dies ist ein klassisches Beispiel für "Defer Implementation Details" - eine Best Practice in Software Architecture.

