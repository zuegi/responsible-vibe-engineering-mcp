# ADR-006: In-Memory Dummy mit Educational Logging

**Status:** Accepted  
**Datum:** 2026-01-04  
**Entscheider:** Engineering Team

---

## Context

ADR-003 (Interface-First Approach) sieht vor, dass wir Ports jetzt definieren aber Adapters erst später implementieren.

**Frage:** Was machen wir für MVP? System braucht _irgendeine_ Implementation der Ports.

**Optionen:**
1. **No-Op Implementation** (Methoden tun nichts)
2. **Exception-Throwing Implementation** (NotImplementedError)
3. **In-Memory Implementation** (funktioniert, aber nicht persistent)
4. **In-Memory + Educational Logging** (funktioniert + zeigt was echter Backend tun würde)

---

## Decision

**In-Memory Implementation mit detailliertem Educational Logging**

```kotlin
@Component
@ConditionalOnProperty("persistence.backend", havingValue = "inmemory", matchIfMissing = true)
class InMemoryPersistence : 
    MemoryRepositoryPort,
    DocumentPersistencePort,
    VersionControlPort,
    CollaborationPort {
    
    private val documents = ConcurrentHashMap<String, GeneratedDocument>()
    private val logger by rvmcpLogger()
    
    override suspend fun saveDocument(
        doc: GeneratedDocument, 
        context: ExecutionContext
    ): Result<Unit> {
        // 1. Funktionalität: Document speichern
        documents[doc.filename] = doc
        
        // 2. Educational Logging: Was würde echter Backend tun
        logger.info("📝 Document saved: ${doc.filename}")
        logger.info("   Size: ${doc.content.length} chars")
        logger.info("   Phase: ${context.currentPhase}")
        logger.info("")
        logger.info("   ⚠️  Using in-memory storage (not persistent across restarts)")
        logger.info("")
        logger.info("   💡 To enable persistence, configure in application.yml:")
        logger.info("      persistence.backend=file      # Local file storage")
        logger.info("      persistence.backend=git       # Git repository")
        logger.info("      persistence.backend=confluence # Confluence wiki")
        logger.info("")
        
        return Result.success(Unit)
    }
    
    override suspend fun commit(
        files: List<String>,
        message: String,
        context: ExecutionContext
    ): Result<CommitInfo> {
        // Educational Logging: Zeige was Git-Adapter tun würde
        logger.info("📝 Would commit to version control:")
        logger.info("   Files: ${files.joinToString(", ")}")
        logger.info("   Message: $message")
        logger.info("   Branch: ${context.gitBranch}")
        logger.info("")
        logger.info("   💡 To enable version control:")
        logger.info("      persistence.vcs=git")
        logger.info("")
        
        return Result.success(CommitInfo.NoOp)
    }
}
```

---

## Rationale

### Vorteile

1. **MVP funktioniert**
   - System ist lauffähig
   - Alle Domain-Logik kann getestet werden
   - Demo und Testing möglich

2. **Educational: Zeigt "Would-Be" Operations**
   - Logs dokumentieren exakt was echter Backend tun würde
   - Guide für Backend-Implementation
   - User versteht System-Behavior

3. **User versteht Limitation**
   - Klare Warnung: "not persistent"
   - Konkrete Anleitung zur Aktivierung echter Persistence
   - Keine Überraschungen

4. **Entwickler-Freundlich**
   - Logs helfen bei Backend-Implementation
   - Zeigen welche Operationen wann getriggert werden
   - Dokumentieren expected behavior

5. **Einfach zu implementieren**
   - 2-3 Tage statt 9 Tage (Git)
   - ConcurrentHashMap reicht
   - Keine externe Dependencies

### Nachteile

1. **Nicht persistent**
   - Daten gehen bei Server-Restart verloren
   - Für Production nicht geeignet

2. **Kein Team-Sync**
   - Jeder Entwickler hat eigene In-Memory Daten
   - Keine Collaboration möglich

---

## Consequences

### MVP Phase

**Was funktioniert:**
- ✅ Alle Domain-Logik
- ✅ Document Generation (in-memory)
- ✅ Workflow Execution
- ✅ Demo & Testing
- ✅ User sieht Ergebnisse (während Session)

**Was nicht funktioniert:**
- ❌ Persistence über Server-Restart
- ❌ Team Collaboration
- ❌ Git Commits
- ❌ Externe Storage

**User Experience:**
```
📝 Document saved: docs/requirements.md
   Size: 3421 chars
   Phase: Requirements Analysis
   
   ⚠️  Using in-memory storage (not persistent across restarts)
   
   💡 To enable persistence, configure in application.yml:
      persistence.backend=file      # Local file storage
      persistence.backend=git       # Git repository
      persistence.backend=confluence # Confluence wiki
```

### Post-MVP

Wenn echter Backend gewünscht:
```yaml
# application.yml
persistence:
  backend: git  # statt inmemory
```

→ GitAdapter wird geladen, InMemoryPersistence wird nicht verwendet

**Kein Code-Change nötig** - nur Config!

---

## Alternatives Considered

### Alternative 1: No-Op Implementation

```kotlin
class NoOpPersistence : DocumentPersistencePort {
    override suspend fun saveDocument(...) {
        // Do nothing
    }
}
```

**Pros:**
- Einfachste Implementation

**Cons:**
- ❌ System scheint zu funktionieren, tut aber nichts
- ❌ Keine Feedback für User
- ❌ Schwer zu debuggen

**Decision:** Rejected - zu irreführend

### Alternative 2: Exception-Throwing

```kotlin
class NotImplementedPersistence : DocumentPersistencePort {
    override suspend fun saveDocument(...) {
        throw NotImplementedError("Configure real backend")
    }
}
```

**Pros:**
- Klar dass nicht implemented

**Cons:**
- ❌ System ist nicht lauffähig
- ❌ Keine Demo möglich
- ❌ Kein Testing möglich

**Decision:** Rejected - MVP wäre nicht functional

### Alternative 3: In-Memory ohne Logging

```kotlin
class InMemoryPersistence : DocumentPersistencePort {
    override suspend fun saveDocument(...) {
        documents[filename] = doc
        // Kein Logging
    }
}
```

**Pros:**
- Funktioniert
- Einfach

**Cons:**
- ⚠️ User weiß nicht dass nicht persistent
- ⚠️ Keine Guidance für echten Backend
- ⚠️ Silent failure bei Restart

**Decision:** Rejected - nicht educational

### Alternative 4: In-Memory + Educational Logging (Chosen)

**Pros:**
- ✅ Funktioniert für MVP
- ✅ User versteht Limitation
- ✅ Guidance für echten Backend
- ✅ Hilft bei Implementation

**Cons:**
- ⚠️ Mehr Logging-Code

**Decision:** ✅ Accepted - beste Balance

---

## Implementation

### Spring Configuration

```kotlin
@Configuration
class PersistenceConfiguration {
    
    @Bean
    @ConditionalOnProperty("persistence.backend", havingValue = "inmemory", matchIfMissing = true)
    fun inMemoryPersistence(): DocumentPersistencePort {
        return InMemoryPersistence()
    }
    
    @Bean
    @ConditionalOnProperty("persistence.backend", havingValue = "git")
    fun gitPersistence(gitPort: GitPort): DocumentPersistencePort {
        return GitDocumentAdapter(gitPort)
    }
}
```

### Logging Guidelines

**Format:**
1. **Status Icon:** 📝 (saved), ⚠️ (warning), 💡 (tip)
2. **Action:** Was wurde getan
3. **Details:** Relevante Info (Size, Files, etc.)
4. **Warning:** "not persistent"
5. **Tip:** Wie aktiviert man echten Backend

**Beispiel:**
```
📝 Document saved: docs/requirements.md
   Size: 3421 chars
   
   ⚠️  Not persistent
   
   💡 To enable: persistence.backend=git
```

### Testing

```kotlin
@Test
fun `in-memory persistence should save document`() = runBlocking {
    val persistence = InMemoryPersistence()
    val doc = GeneratedDocument("test.md", "content")
    
    val result = persistence.saveDocument(doc, context)
    
    assertThat(result.isSuccess).isTrue()
    assertThat(persistence.getDocument("test.md", context)).isEqualTo(doc)
}
```

---

## Related Decisions

- [ADR-003: Interface-First Approach](0003-interface-first-approach.md) - Strategie: Ports jetzt, Adapters später
- Related to all Ports: MemoryRepositoryPort, DocumentPersistencePort, etc.

---

## Notes

Diese Educational Logging Strategie hat mehrere Zwecke:

1. **User-Facing:** Verständnis und Guidance
2. **Developer-Facing:** Implementation Guide
3. **Documentation:** Behavior Specification durch Logs

Das ist mehr als "just a dummy" - es ist eine **Living Specification** des erwarteten Verhaltens.

