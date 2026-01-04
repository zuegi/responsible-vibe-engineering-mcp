# ADR-005: Kotlin String Templates für Documents

**Status:** Accepted  
**Datum:** 2026-01-04  
**Entscheider:** Engineering Team

---

## Context

Für die automatische Dokumenten-Generierung (Requirements, Architecture, Features) benötigen wir ein Template System.

**Optionen:**
1. **Externe Template Engine** (Mustache, FreeMarker, Thymeleaf)
2. **Kotlin String Templates** (Multiline Strings mit Interpolation)
3. **DSL-basiert** (Kotlin DSL für Markdown)

**Anforderungen:**
- Markdown-Generierung
- Type-safe
- Einfach zu warten
- Performant

---

## Decision

**Kotlin multiline strings mit String Interpolation für Document Templates**

```kotlin
object RequirementsTemplate {
    fun generate(
        projectName: String,
        summary: String,
        requirements: List<Requirement>
    ): String = """
        # Requirements: $projectName
        
        ## Executive Summary
        $summary
        
        ## Functional Requirements
        ${requirements.joinToString("\n\n") { req ->
            """
            ### ${req.id}: ${req.title}
            ${req.description}
            
            **Acceptance Criteria:**
            ${req.acceptanceCriteria.joinToString("\n") { "- $it" }}
            """.trimIndent()
        }}
        
        ---
        *Generated: ${Instant.now()}*
        """.trimIndent()
}
```

---

## Rationale

### Vorteile

1. **Zero Dependencies**
   - Keine externe Library nötig
   - Kein Dependency-Management
   - Weniger Maintenance

2. **Type-Safe**
   - Compiler prüft Syntax
   - Refactoring-safe
   - Auto-completion in IDE

3. **IDE Support**
   - Syntax Highlighting
   - Code Navigation
   - Refactoring Tools

4. **Ausreichend für Markdown**
   - Markdown ist einfach genug
   - Keine komplexe Template-Logic nötig
   - String-Interpolation reicht

5. **Performance**
   - Keine Template-Parsing zur Laufzeit
   - Direkt zu String kompiliert

6. **Einfach zu debuggen**
   - Normaler Kotlin Code
   - Breakpoints möglich
   - Stack Traces verständlich

### Nachteile

1. **Templates sind Code**
   - Erfordern Rebuild bei Änderungen
   - Nicht editierbar zur Laufzeit
   - Nicht für nicht-Entwickler editierbar

2. **Weniger Features als Template Engines**
   - Keine Template-Inheritance
   - Keine Partials
   - Keine komplexe Conditional Logic

3. **Verbose bei komplexen Templates**
   - Lange Strings können unübersichtlich werden
   - Manuelle Indentation nötig

---

## Consequences

### Positive

- ✅ Einfache, wartbare Lösung
- ✅ Keine externe Dependencies
- ✅ Type-safe und refactoring-friendly
- ✅ Gute IDE-Integration

### Negative

- ⚠️ Templates erfordern Rebuild
- ⚠️ Nicht editierbar ohne Kotlin-Kenntnisse

### Neutral

- 🔄 Für komplexere Templates kann später auf Template Engine gewechselt werden
- 🔄 Templates sind durch Interfaces abstrahiert (DocumentTemplate interface möglich)

---

## Alternatives Considered

### Alternative 1: Mustache Template Engine

**Pros:**
- Logic-less Templates
- Zur Laufzeit editierbar
- Gut für nicht-Entwickler

**Cons:**
- ❌ Externe Dependency
- ❌ Nicht type-safe
- ❌ Runtime-Parsing
- ❌ Weniger IDE-Support

**Decision:** Rejected - Overhead für unseren Use-Case

### Alternative 2: FreeMarker

**Pros:**
- Sehr mächtig
- Komplexe Logic möglich
- Etabliert

**Cons:**
- ❌ Heavy-weight für Markdown
- ❌ Komplexe Template-Syntax
- ❌ Nicht type-safe
- ❌ Overhead

**Decision:** Rejected - zu komplex

### Alternative 3: Kotlin DSL

**Pros:**
- Type-safe
- Kotlin-idiomatisch
- Sehr flexibel

**Cons:**
- ❌ Entwicklungsaufwand für DSL
- ❌ Abstraktion über Abstraktion
- ❌ Overkill für Markdown

**Decision:** Rejected - YAGNI

### Alternative 4: Kotlin String Templates (Chosen)

**Pros:**
- ✅ Zero dependencies
- ✅ Type-safe
- ✅ Simple & effective
- ✅ Ausreichend für Markdown

**Cons:**
- ⚠️ Rebuild nötig

**Decision:** ✅ Accepted - KISS-Prinzip

---

## Implementation

### Template Objects

```kotlin
// One object per document type
object RequirementsTemplate { ... }
object ArchitectureTemplate { ... }
object FeatureSpecTemplate { ... }
```

### Usage

```kotlin
@Service
class DocumentGenerationService {
    suspend fun generateRequirementsDoc(data: PhaseResult): GeneratedDocument {
        val content = RequirementsTemplate.generate(
            projectName = data.projectName,
            summary = data.summary,
            requirements = extractRequirements(data)
        )
        
        return GeneratedDocument(
            filename = "docs/requirements.md",
            content = content
        )
    }
}
```

### Testing

```kotlin
@Test
fun `should generate requirements document`() {
    val content = RequirementsTemplate.generate(
        projectName = "Test Project",
        summary = "Test Summary",
        requirements = listOf(/* ... */)
    )
    
    assertThat(content).contains("# Requirements: Test Project")
    assertThat(content).contains("## Executive Summary")
}
```

---

## Migration Path

Falls später Template Engine gewünscht:

```kotlin
// 1. Interface definieren
interface DocumentTemplate<T> {
    fun generate(data: T): String
}

// 2. String Template Implementation
class RequirementsStringTemplate : DocumentTemplate<RequirementsData> {
    override fun generate(data: RequirementsData) = """
        # Requirements: ${data.projectName}
        ...
    """.trimIndent()
}

// 3. Optional: Mustache Implementation
class RequirementsMustacheTemplate : DocumentTemplate<RequirementsData> {
    override fun generate(data: RequirementsData): String {
        val template = Mustache.compile("requirements.mustache")
        return template.execute(data)
    }
}

// 4. Service nutzt Interface
class DocumentGenerationService(
    private val template: DocumentTemplate<RequirementsData>
) { ... }
```

**Kein Breaking Change** - nur neue Implementation hinzufügen!

---

## Related Decisions

- [ADR-003: Interface-First Approach](0003-interface-first-approach.md) - Gleiche Philosophy: Simple jetzt, erweitern später
- [ADR-007: Question Catalogs](0007-question-catalogs-structured-requirements.md) - Basiert auf Templates

---

## Notes

Diese Entscheidung folgt dem **KISS-Prinzip** (Keep It Simple, Stupid) und **YAGNI** (You Aren't Gonna Need It). 

Für Markdown-Generierung reichen Kotlin String Templates vollkommen aus. Sollte sich später herausstellen, dass komplexere Templates nötig sind, kann problemlos migriert werden.

