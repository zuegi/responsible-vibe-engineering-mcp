# ADR-007: Question Catalogs für strukturierte Requirements

**Status:** Accepted  
**Datum:** 2026-01-03  
**Entscheider:** Engineering Team

---

## Context

Bei Requirements-Erhebung mit LLMs gibt es ein fundamentales Problem:

**LLMs stellen zufällige Fragen** → Keine Garantie für Vollständigkeit

**Probleme:**
- Wichtige Aspekte werden vergessen (z.B. Security, Performance, Edge Cases)
- Jedes Projekt bekommt andere Fragen
- Kein strukturierter, wiederholbarer Prozess
- Requirements sind unvollständig

**Beispiel (problematisch):**
```
LLM: "Was soll das System können?"
User: "OAuth2 Login"
LLM: "Ok, ich implementiere das"
→ Vergessen: Security, Sessions, Edge Cases, Error Handling, etc.
```

---

## Decision

**Vordefinierte, strukturierte Question Catalogs pro Engineering-Phase**

### Format: YAML-basiert

```yaml
# question-catalogs/requirements-analysis.yml
catalog_name: Requirements Analysis Questions
version: 1.0

questions:
  # Pflichtfragen
  - id: project-overview
    category: overview
    question: "Please provide a high-level description of the project"
    required: true
    validation:
      - type: min_length
        value: 50
  
  - id: target-audience
    category: stakeholders
    question: "Who are the primary users/stakeholders?"
    required: true
  
  - id: security-requirements
    category: non-functional
    question: "What are the security requirements?"
    required: true
  
  # Optionale Fragen
  - id: third-party-integrations
    category: technical
    question: "Are there any third-party services to integrate?"
    required: false
  
  # ... weitere strukturierte Fragen
```

### Workflow Integration

```yaml
# workflows/requirements-analysis.yml
nodes:
  - id: ask_project_overview
    type: ask_catalog_question
    questionId: project-overview
    output: project_overview
  
  - id: ask_target_audience
    type: ask_catalog_question
    questionId: target-audience
    output: target_audience
  
  # ... weitere Fragen aus Catalog
  
  - id: generate_requirements_doc
    type: llm
    prompt: |
      Based on the answers:
      ${project_overview}
      ${target_audience}
      ...
      
      Generate a structured requirements document.
```

---

## Rationale

### Vorteile

1. **Garantierte Vollständigkeit**
   - Alle wichtigen Aspekte werden abgefragt
   - Pflichtfragen müssen beantwortet werden
   - Keine vergessenen Requirements

2. **Strukturiert & Wiederholbar**
   - Gleiche Fragen für ähnliche Projekte
   - Konsistente Requirements-Qualität
   - Best-Practice kodifiziert

3. **Wiederverwendbare Templates**
   - Question Catalogs können geteilt werden
   - Org-spezifische Catalogs möglich
   - Continuous Improvement

4. **Validierung**
   - Validation Rules pro Frage
   - Type Checking (Text, Number, Boolean, etc.)
   - Min/Max Length, Format, etc.

5. **Basis für Dokumenten-Generierung**
   - Antworten sind strukturiert
   - Können direkt in Markdown-Template genutzt werden
   - Mapping zu Document Sections

6. **AIUP-inspiriert**
   - Bewährte Methodik aus AI-Powered Unified Process
   - Requirements Catalog Konzept übernommen
   - An unser System angepasst

### Nachteile

1. **Initial Effort**
   - Catalogs müssen erstellt werden
   - Pflege & Updates nötig

2. **Weniger flexibel**
   - LLM kann nicht frei fragen
   - Katalog muss für jeden Process-Type erstellt werden

3. **User muss mehr antworten**
   - Mehr strukturierte Fragen als bei freiem Gespräch
   - Kann sich repetitiv anfühlen

---

## Consequences

### Positive

- ✅ Requirements sind vollständiger
- ✅ Konsistente Qualität über Projekte
- ✅ Weniger vergessene Edge Cases
- ✅ Automatische Dokumenten-Generierung möglich

### Negative

- ⚠️ Catalogs müssen gepflegt werden
- ⚠️ User muss strukturiert antworten (nicht free-form)

### Neutral

- 🔄 Catalogs können erweitert werden
- 🔄 LLM kann zusätzliche Fragen stellen (nicht nur Catalog)

---

## Implementation

### QuestionCatalog Domain Model

```kotlin
data class QuestionCatalog(
    val name: String,
    val version: String,
    val questions: List<Question>
)

data class Question(
    val id: QuestionId,
    val category: QuestionCategory,
    val question: String,
    val required: Boolean,
    val validationRules: List<ValidationRule>
)

enum class QuestionCategory {
    OVERVIEW,
    STAKEHOLDERS,
    FUNCTIONAL,
    NON_FUNCTIONAL,
    TECHNICAL,
    CONSTRAINTS
}
```

### QuestionCatalogTool (Koog Integration)

```kotlin
class QuestionCatalogTool(
    private val catalog: QuestionCatalog
) : SimpleTool {
    
    data class Args(val questionId: String)
    
    override suspend fun execute(args: Args): String {
        val question = catalog.getQuestion(QuestionId(args.questionId))
            ?: throw IllegalArgumentException("Question not found: ${args.questionId}")
        
        return question.question
    }
    
    override val descriptor = ToolDescriptor(
        name = "get_question",
        description = "Get a question from the catalog by ID"
    )
}
```

### Workflow Node: ask_catalog_question

```kotlin
// YAML Node Type
enum class NodeType {
    // ... existing types
    ASK_CATALOG_QUESTION  // New!
}

// Execution in YamlToKoogStrategyTranslator
when (node.type) {
    NodeType.ASK_CATALOG_QUESTION -> {
        // 1. Get question from catalog
        val questionText = catalogTool.execute(Args(node.questionId))
        
        // 2. Ask user
        val answer = askUserTool.execute(Args(questionText))
        
        // 3. Validate answer
        validateAnswer(question, answer)
        
        // 4. Store in context
        context[node.output] = answer
    }
}
```

### Document Generation Integration

```kotlin
class DocumentGenerationService {
    suspend fun generateRequirementsDoc(
        answers: Map<QuestionId, String>,
        catalog: QuestionCatalog
    ): GeneratedDocument {
        
        // Extract structured data from answers
        val projectOverview = answers[QuestionId("project-overview")]
        val targetAudience = answers[QuestionId("target-audience")]
        val securityRequirements = answers[QuestionId("security-requirements")]
        
        // Generate markdown using template
        val content = RequirementsTemplate.generate(
            overview = projectOverview,
            audience = targetAudience,
            security = securityRequirements,
            // ... weitere Answers
        )
        
        return GeneratedDocument("docs/requirements.md", content)
    }
}
```

---

## Validation

### Example Validation Rules

```kotlin
sealed class ValidationRule {
    data class MinLength(val length: Int) : ValidationRule()
    data class MaxLength(val length: Int) : ValidationRule()
    data class Regex(val pattern: String) : ValidationRule()
    data class NotEmpty : ValidationRule()
    data class OneOf(val options: List<String>) : ValidationRule()
}

// Validation Engine
class QuestionValidator {
    fun validate(answer: String, rules: List<ValidationRule>): ValidationResult {
        rules.forEach { rule ->
            when (rule) {
                is ValidationRule.MinLength -> {
                    if (answer.length < rule.length) {
                        return ValidationResult.Invalid("Answer too short (min: ${rule.length})")
                    }
                }
                is ValidationRule.NotEmpty -> {
                    if (answer.isBlank()) {
                        return ValidationResult.Invalid("Answer cannot be empty")
                    }
                }
                // ... weitere Rules
            }
        }
        return ValidationResult.Valid
    }
}
```

---

## Alternatives Considered

### Alternative 1: Free-Form LLM Conversation

**Pros:**
- Natürlicher Dialog
- Flexibel
- LLM kann kontextabhängig fragen

**Cons:**
- ❌ Keine Garantie für Vollständigkeit
- ❌ Jedes Projekt bekommt andere Fragen
- ❌ Schwer zu validieren
- ❌ Nicht wiederholbar

**Decision:** Rejected - zu unstrukturiert

### Alternative 2: Fixed Form (Web UI)

**Pros:**
- Strukturiert
- Validierung einfach

**Cons:**
- ❌ Kein MCP Server möglich
- ❌ Keine LLM-Integration
- ❌ Separate UI nötig

**Decision:** Rejected - nicht kompatibel mit MCP

### Alternative 3: Hybrid (LLM + Catalog)

**Pros:**
- LLM kann Fragen erklären/anpassen
- Aber strukturierte Basis

**Cons:**
- ⚠️ Komplexer

**Decision:** Considered for future enhancement

### Alternative 4: Question Catalogs (Chosen)

**Pros:**
- ✅ Strukturiert & vollständig
- ✅ Wiederverwendbar
- ✅ MCP-kompatibel
- ✅ Basis für Doc-Generation

**Cons:**
- ⚠️ Initial Effort

**Decision:** ✅ Accepted - beste Balance

---

## Catalog Development Process

1. **Analyze Process Type**
   - Requirements Analysis braucht andere Fragen als Architecture Design

2. **Define Categories**
   - Overview, Stakeholders, Functional, Non-Functional, Technical, Constraints

3. **Create Questions**
   - Pflichtfragen für jede Kategorie
   - Optionale Vertiefungsfragen

4. **Add Validation**
   - min_length, not_empty, etc.

5. **Test with Real Projects**
   - Iterative Verbesserung

6. **Version & Share**
   - Catalogs sind versioniert
   - Können geteilt werden (GitHub, internal)

---

## Related Decisions

- [ADR-005: Kotlin String Templates](0005-kotlin-string-templates.md) - Templates nutzen Catalog-Antworten
- Inspired by: AIUP (AI-Powered Unified Process) Requirements Catalog

---

## Future Enhancements

1. **Conditional Questions**
   - "If answer X is Y, ask follow-up Z"

2. **LLM-Enhanced**
   - LLM kann Fragen erklären
   - LLM kann basierend auf Antwort tiefere Fragen generieren

3. **Community Catalogs**
   - Shared Question Catalogs für Web-Apps, Mobile Apps, etc.
   - Best-Practice Kataloge

4. **Multi-Language**
   - Catalogs in verschiedenen Sprachen

---

## Notes

Diese Entscheidung adressiert ein fundamentales Problem bei LLM-gestützter Requirements-Erhebung: **Zufälligkeit und Unvollständigkeit**.

Durch strukturierte Catalogs garantieren wir Qualität und Vollständigkeit, während wir trotzdem LLM-Power für Dokumenten-Generierung nutzen.

