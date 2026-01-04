# Responsible Vibe MCP – Engineering-getriebene KI-Entwicklung

> **Living Document** - Status: Januar 2026  
> Kombiniert Vision, Architecture & Implementation Roadmap

---

## 📋 Inhaltsverzeichnis

1. [Vision & Problemstellung](#vision--problemstellung)
2. [Funktionsprinzip](#funktionsprinzip)
3. [Architektur](#architektur)
4. [Aktueller Status](#aktueller-status)
5. [Implementation Roadmap](#implementation-roadmap)
6. [MCP Server Integration](#mcp-server-integration)
7. [Technologie-Stack](#technologie-stack)
8. [Architektur-Entscheidungen (ADRs)](#architektur-entscheidungen-adrs)

---

## Vision & Problemstellung

Viele KI-Codier-Tools funktionieren als „Autocompletes on Steroids" – sie durchsuchen Code, recherchieren online und generieren Lösungen. Doch **Softwareengineering umfasst weit mehr**: Architekturdenken, methodisches Vorgehen, Refactoring-Vermeidung und bewusste Planung.

### Das Problem

- **Planungsschritt wird übersprungen** → „Zehn Minuten Design hätten drei Stunden Refactoring vermieden"
- **LLMs stellen zufällige Fragen** statt systematischer Requirements-Erhebung
- **Keine strukturierte Dokumentation** der Anforderungen und Architektur-Entscheidungen
- **AI-Coding-Tools fehlt Kontext** früherer Entscheidungen

### Die Lösung

Responsible Vibe MCP führt KI-Systeme aktiv durch bewährte Engineering-Workflows mit:

- ✅ **Strukturierten Question Catalogs** für jede Phase
- ✅ **Automatischer Dokumenten-Generierung** (Requirements, Architecture, Features)
- ✅ **Flexiblen Persistence-Backend** (Git, Confluence, File-System - User wählt)
- ✅ **Persistentem Memory** über Sessions hinweg
- ✅ **MCP Server Interface** für universelle KI-Integration (Claude, Warp, IDEs)

---

## Funktionsprinzip

Responsible Vibe MCP leitet die KI systematisch durch klar definierte Projektphasen:

1. **Planung & Anforderungsanalyse**
2. **Architekturentwurf**
3. **Dokumentation des Projekts**
4. **Wiederhole Schritte 1-3** (Iterativ verfeinern)
5. **Implementierung**
6. **Testing**
7. **Integration & Review**

### Kernprinzipien: Vibe Engineering

„Vibe Engineering" beschreibt das Mindset hinter diesem Ansatz:

- **Qualität vor Geschwindigkeit**
- **Wartbarkeit & Testbarkeit**
- **Bewusste Architekturentscheidungen**
- **Aktive Reflexion**: „Passt das in die bestehende Architektur?"

Die KI wird zum **aktiven Entwicklungspartner**, nicht bloß zum Code-Generator.

### Unterstützte Workflows

**Aktuell implementiert:**
1. ✅ **Feature Development** - Vollständiger Workflow mit Question Catalogs

**In Entwicklung (Roadmap):**
2. 🔄 **Bug-Fix** - Systematische Bug-Analyse und Fix
3. 🔄 **Refactoring** - Code-Smell Detection und Transformation

---

## Architektur

### Hexagonal Architecture (Ports & Adapters)

Das Projekt folgt dem **Hexagonal Architecture**-Pattern für maximale Flexibilität:

```
┌─────────────────────────────────────────────────────────────┐
│                        DOMAIN (Kern)                        │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Model: EngineeringProcess, ProcessPhase, Context     │  │
│  │ Services: ExecuteProcessPhaseService, etc.           │  │
│  │                                                      │  │
│  │ Ports (Interfaces):                                 │  │
│  │  - MemoryRepositoryPort                            │  │
│  │  - DocumentPersistencePort                         │  │
│  │  - VersionControlPort                              │  │
│  │  - CollaborationPort                               │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Ports
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    ADAPTERS (Pluggable)                     │
│                                                             │
│  INPUT:                        OUTPUT:                      │
│  ├─ MCP Server                 ├─ In-Memory (MVP)          │
│  └─ CLI (optional)             ├─ Git (Post-MVP)           │
│                                ├─ Confluence (Post-MVP)     │
│                                ├─ File-System (Post-MVP)    │
│                                └─ Koog Workflow Executor    │
└─────────────────────────────────────────────────────────────┘
```

**Vorteile:**
- ✅ **Domain-Logik isoliert** - Framework-unabhängig
- ✅ **Austauschbare Backends** - User wählt Git, Confluence, oder File-System
- ✅ **Maximale Testbarkeit** - Keine externe Dependencies in Domain
- ✅ **Technologie-Unabhängigkeit** - Spring Boot, Koog sind austauschbar

### Schichten

**Domain** (Kern):
- `domain/model`: Entities & Value Objects
  - `process/`: EngineeringProcess, ProcessExecution, ProcessPhase
  - `context/`: ExecutionContext
  - `memory/`: Decision, Interaction, Artifact
  - `vibe/`: VibeCheck, VibeCheckResult
- `domain/port/input`: Use Cases (StartProcessUseCase, ExecutePhaseUseCase, etc.)
- `domain/port/output`: Port Interfaces (MemoryRepositoryPort, DocumentPersistencePort, etc.)
- `domain/service`: Domain Services (Business-Logik)

**Adapter**:
- `adapter/input/mcp`: MCP Server Implementation
- `adapter/output/workflow`: Koog Integration
- `adapter/output/persistence`: In-Memory (MVP), Git (Post-MVP), etc.
- `adapter/output/tools`: Koog Tools (AskUserTool, CreateFileTool, QuestionCatalogTool)

**Infrastructure**:
- Spring Boot Configuration, Dependency Injection

---

## Aktueller Status

### Was funktioniert (Januar 2026)

| Feature | Status | Details |
|---------|--------|---------|
| **MCP Server** | ✅ 90% | 6 Tools implementiert, Resources pending |
| **Koog Integration** | ✅ 100% | Azure OpenAI, Single-Agent, 11x Performance |
| **Question Catalogs** | ✅ 70% | Tool vorhanden, Pfade konfigurierbar machen |
| **Document Generation** | ❌ 10% | Templates fehlen noch |
| **Persistence** | 🟡 50% | In-Memory vorhanden, echter Backend fehlt |
| **Workflow Types** | 🟡 50% | Feature Dev ✅, Bug-Fix/Refactoring pending |

**Gesamtbewertung:** 🟡 **~60% der Vision umgesetzt**

### MCP Tools (Implementiert)

1. ✅ `list_processes` - List available engineering processes
2. ✅ `start_process` - Start a new process execution
3. ✅ `execute_phase` - Execute current phase (async)
4. ✅ `get_phase_result` - Get async execution results
5. ✅ `complete_phase` - Complete phase and advance
6. ✅ `provide_answer` - Resume paused workflows

---

## Implementation Roadmap

> **Strategie:** Interface-First Approach  
> Ports jetzt definieren, Adapters später implementieren (User wählt Backend)

### Phase 1: Foundation (Week 1) - **IN PROGRESS**

**Dauer:** 2-3 Tage  
**Ziel:** Persistence & Collaboration Ports definieren, In-Memory Dummy

#### Core Ports (Interfaces)

```kotlin
// Memory Persistence
interface MemoryRepositoryPort {
    suspend fun save(context: ExecutionContext)
    suspend fun load(projectPath: String, branch: String): ExecutionContext?
}

// Document Persistence (Generic - nicht Git-spezifisch!)
interface DocumentPersistencePort {
    suspend fun saveDocument(doc: GeneratedDocument, context: ExecutionContext): Result<Unit>
    suspend fun getDocument(filename: String, context: ExecutionContext): GeneratedDocument?
    suspend fun listDocuments(context: ExecutionContext): List<DocumentMetadata>
}

// Version Control (Generic!)
interface VersionControlPort {
    suspend fun commit(files: List<String>, message: String, context: ExecutionContext): Result<CommitInfo>
    suspend fun push(context: ExecutionContext): Result<Unit>
    suspend fun pull(context: ExecutionContext): Result<PullResult>
}

// Collaboration
interface CollaborationPort {
    suspend fun syncState(context: ExecutionContext): SyncResult
    suspend fun notifyTeam(event: CollaborationEvent, context: ExecutionContext)
}
```

#### In-Memory Dummy (MVP)

```kotlin
/**
 * In-Memory implementation for MVP.
 * 
 * Limitation: Not persistent across server restarts.
 * Benefit: Shows via logging what real implementation would do.
 * 
 * For production: Replace with Git/Confluence/File adapter.
 */
@Component
@ConditionalOnProperty("persistence.backend", havingValue = "inmemory", matchIfMissing = true)
class InMemoryPersistence : 
    MemoryRepositoryPort,
    DocumentPersistencePort,
    VersionControlPort,
    CollaborationPort {
    
    override suspend fun saveDocument(doc: GeneratedDocument, context: ExecutionContext): Result<Unit> {
        documents[doc.filename] = doc
        logger.info("📝 Document saved: ${doc.filename}")
        logger.info("   💡 To enable persistence:")
        logger.info("      persistence.backend=git")
        logger.info("      persistence.backend=confluence")
        return Result.success(Unit)
    }
    
    // ... weitere Methoden mit Educational Logging
}
```

**Acceptance Criteria:**
- ✅ Alle Ports definiert mit KDoc
- ✅ In-Memory Dummy implementiert mit Logging
- ✅ Tests für Dummy
- ✅ Domain Services nutzen Ports (nicht konkrete Klassen)

---

### Phase 2: Document Generation (Week 2-3)

**Dauer:** 8 Tage  
**Ziel:** Automatische Markdown-Generierung aus Workflow-Ergebnissen

#### Sprint 2.1: Document Templates (3 Tage)

**Template Engine:** Kotlin String Templates (zero dependencies)

```kotlin
object RequirementsTemplate {
    fun generate(
        projectName: String,
        summary: String,
        requirements: List<Requirement>,
        stakeholders: List<Stakeholder>,
        decisions: List<Decision>
    ): String = """
        # Requirements: $projectName
        
        ## Executive Summary
        $summary
        
        ## Stakeholders
        ${stakeholders.joinToString("\n") { "- **${it.role}**: ${it.name}" }}
        
        ## Functional Requirements
        ${requirements.filter { it.type == RequirementType.FUNCTIONAL }.joinToString("\n\n") { req ->
            """
            ### ${req.id}: ${req.title}
            ${req.description}
            
            **Acceptance Criteria:**
            ${req.acceptanceCriteria.joinToString("\n") { "- $it" }}
            """.trimIndent()
        }}
        
        ## Related Documents
        - [Architecture](Architecture.md)
        
        ---
        *Generated: ${Instant.now()}*
        """.trimIndent()
}
```

**Templates:** Requirements, Architecture, Feature Specs, User Stories, Test Plan

#### Sprint 2.2: Generation Service (5 Tage)

```kotlin
@Service
class DocumentGenerationService(
    private val persistencePort: DocumentPersistencePort
) {
    suspend fun generateRequirementsDoc(
        phaseResult: PhaseResult,
        context: ExecutionContext
    ): GeneratedDocument {
        // Extract data from phase result
        val requirements = extractRequirements(phaseResult)
        
        // Generate markdown
        val content = RequirementsTemplate.generate(...)
        
        // Persist via port (Backend-agnostic!)
        val doc = GeneratedDocument(
            filename = "docs/requirements.md",
            content = content
        )
        persistencePort.saveDocument(doc, context)
        
        return doc
    }
}
```

**Integration:** ExecuteProcessPhaseService ruft DocumentGenerationService nach Phase

**Acceptance Criteria:**
- ✅ Templates für Requirements, Architecture, Features
- ✅ Docs werden via Port persistiert (Backend-agnostic)
- ✅ Markdown korrekt formatiert
- ✅ Cross-Links zwischen Docs

---

### Phase 3: Multi-Workflow Types (Week 4-5)

**Dauer:** 6 Tage  
**Ziel:** Bug-Fix und Refactoring Workflows

#### Bug-Fix Process (3 Tage)

```yaml
# processes/bug-fix.yml
id: bug-fix
name: Bug Fix Process

phases:
  - name: Bug Analysis
    workflow: workflows/bug-analysis.yml
  - name: Root Cause Investigation
    workflow: workflows/root-cause.yml
  - name: Fix Implementation
    workflow: workflows/fix-implementation.yml
```

**Question Catalog:** `bug-fix-catalog.yml`

#### Refactoring Process (3 Tage)

Analog zu Bug-Fix mit refactoring-spezifischen Workflows

---

### Phase 4: Polish & MCP Resources (Week 6)

**Dauer:** 5 Tage

**Tasks:**
1. ✅ MCP Resources implementieren (context://, process://)
2. ✅ Bug Fixes & TODOs
3. ✅ Documentation

---

### Timeline & Milestones

```
Week 1:   Interfaces + Dummy  ━━━━━━━━ 2-3 Tage
Week 2-3: Doc Generation      ━━━━━━━━━━━━━━━━ 8 Tage
Week 4-5: Multi-Workflow      ━━━━━━━━━━━━━━━━ 6 Tage
Week 6:   Polish              ━━━━━━━━ 5 Tage

         Jan 6      Jan 10     Jan 24     Feb 7     Feb 14
           │          │          │           │         │
         Start    Milestone   Milestone  Milestone   MVP
                    M1         M2         M3          Complete
```

**Milestones:**
- **M1 (Jan 10):** Ports definiert, In-Memory Dummy funktioniert
- **M2 (Jan 24):** Document Generation funktioniert
- **M3 (Feb 7):** Bug-Fix & Refactoring Workflows
- **MVP (Feb 14):** Production-ready mit In-Memory Backend

**Post-MVP (On-Demand):**
- Git Adapter: +5 Tage (wenn User Git will)
- Confluence Adapter: +5 Tage (wenn User Confluence will)
- File Adapter: +2 Tage (wenn User File-based will)

---

### Post-MVP: Backend Adapters (User wählt)

#### Git Adapter (Optional)

```kotlin
@Configuration
@ConditionalOnProperty("persistence.backend", havingValue = "git")
class GitAdapterConfiguration {
    @Bean
    fun gitAdapter(): DocumentPersistencePort = GitDocumentAdapter(...)
}

class GitDocumentAdapter : DocumentPersistencePort {
    override suspend fun saveDocument(...): Result<Unit> {
        // Write file
        File(context.projectPath, doc.filename).writeText(doc.content)
        
        // Git commit
        gitPort.commit(listOf(doc.filename), message, context)
        
        // Optional: Git push
        if (config.autoPush) gitPort.push(context)
    }
}
```

**Configuration:**
```yaml
persistence:
  backend: git  # inmemory | file | git | confluence
  git:
    auto_push: true
```

#### Confluence Adapter (Optional)

```kotlin
class ConfluenceDocumentAdapter : DocumentPersistencePort {
    override suspend fun saveDocument(...): Result<Unit> {
        confluenceClient.createOrUpdatePage(
            spaceKey = config.spaceKey,
            title = doc.title,
            content = markdownToConfluence(doc.content)
        )
    }
}
```

---

## MCP Server Integration

### Was ist MCP (Model Context Protocol)?

**Model Context Protocol** ist ein standardisiertes Protokoll für die Kommunikation zwischen KI-Systemen und externen Tools/Services.

**Vorteile:**
- 🔌 **Standardisiert**: JSON-RPC 2.0
- 🛠️ **Tool-basiert**: Funktionen als "Tools" exposed
- 🌐 **Universal**: Funktioniert mit Claude, Warp, IDEs

### Communication Flow

```
[1] User: "Starte Feature Development Prozess"
    ↓
[2] MCP Client (Claude/Warp)
    │ JSON-RPC Request über stdio
    ↓
[3] MCP Server (Responsible Vibe)
    │ Route zu Tool
    ↓
[4] StartProcessTool
    │ Call Domain Service
    ↓
[5] StartProcessExecutionService
    │ Business Logic
    ↓
[6] MemoryRepository, DocumentPersistence (via Ports!)
    │ Persistence (Backend-agnostic)
    ↓
[7] Response → MCP Client → User
```

### Integration mit KI-Systemen

#### Claude Desktop

```json
// ~/.config/claude/mcp-servers.json
{
  "responsible-vibe-mcp": {
    "command": "java",
    "args": ["-jar", "/path/to/rvmcp.jar", "--mcp-mode"]
  }
}
```

#### Warp Agent

Siehe: [warp-mcp-config.json](warp-mcp-config.json) und [warp-rule-mcp-server](warp-rule-mcp-server/)

---

## Technologie-Stack

### Core Technologies

- **Sprache:** Kotlin
- **Framework:** Spring Boot
- **Agentic AI:** Kotlin Koog 0.6.0
- **Build Tool:** Maven
- **Interface:** Model Context Protocol (MCP)
- **Architektur:** Hexagonal Architecture (Ports & Adapters)

### Dependencies

**Produktion:**
- Spring Boot Starter
- Kotlin Koog 0.6.0 (Agentic AI)
- MCP SDK Kotlin
- kotlinx.serialization (JSON)
- kotlinx.coroutines (Async)

**Optional (Post-MVP):**
- JGit 6.8.0 (für Git Adapter)
- Atlassian Confluence Client (für Confluence Adapter)

**Tests:**
- JUnit 5
- MockK
- AssertJ
- Kotest

---

## Architektur-Entscheidungen (ADRs)

Alle architektonischen Entscheidungen werden als separate ADR-Dokumente in `docs/adr/` geführt.

### Übersicht

| ADR | Titel | Status | Datum |
|-----|-------|--------|-------|
| [0001](docs/adr/0001-coroutine-context-for-workflow-interruption.md) | CoroutineContext für Workflow Interruption | Accepted | 2026-01-01 |
| [0002](docs/adr/0002-process-vs-workflow-separation.md) | Process vs. Workflow Trennung | Accepted | 2025-12-15 |
| [0003](docs/adr/0003-interface-first-approach.md) | Interface-First Approach | Accepted | 2026-01-04 |
| [0004](docs/adr/0004-hybrid-human-in-the-loop.md) | Hybrid Human-in-the-Loop | Accepted | 2025-12-15 |
| [0005](docs/adr/0005-kotlin-string-templates.md) | Kotlin String Templates für Documents | Accepted | 2026-01-04 |
| [0006](docs/adr/0006-in-memory-dummy-educational-logging.md) | In-Memory Dummy mit Educational Logging | Accepted | 2026-01-04 |
| [0007](docs/adr/0007-question-catalogs-structured-requirements.md) | Question Catalogs für strukturierte Requirements | Accepted | 2026-01-03 |

### Kern-Entscheidungen (TL;DR)

**Interface-First Approach (ADR-003):**
- Ports jetzt, Adapters später
- User wählt Backend (Git, Confluence, File)
- MVP in 2-3 Tagen statt 9 Tagen

**Process vs. Workflow (ADR-002):**
- EngineeringProcess (Domain) ≠ Koog Workflow (Execution)
- Domain bleibt framework-unabhängig
- Koog austauschbar

**CoroutineContext Interruption (ADR-001):**
- CoroutineContext.Element statt ThreadLocal
- Coroutine-safe, folgt durch Thread-Switches
- Ermöglicht Workflow-Pause/Resume

**Question Catalogs (ADR-007):**
- Strukturierte, wiederverwendbare Fragelisten
- Garantiert Requirements-Vollständigkeit
- Basis für Dokumenten-Generierung

Für vollständige Details siehe einzelne ADR-Dokumente in [docs/adr/](docs/adr/).

---

## Getting Started

### Quick Start

1. **Clone Repository**
   ```bash
   git clone https://github.com/your-org/responsible-vibe-mcp.git
   cd responsible-vibe-mcp
   ```

2. **Configure LLM**
   ```bash
   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   # Edit application-local.yml mit deinem Azure OpenAI Endpoint
   ```

3. **Build & Run**
   ```bash
   mvn clean package
   java -jar target/responsible-vibe-mcp-0.1.0-SNAPSHOT.jar
   ```

4. **Configure MCP Client** (Claude Desktop / Warp)
   - Siehe [CONFIGURATION.md](docs/CONFIGURATION.md)

### Detailed Documentation

- **Setup:** [docs/CONFIGURATION.md](docs/CONFIGURATION.md)
- **Koog Integration:** [docs/KOOG_INTEGRATION.md](docs/KOOG_INTEGRATION.md)
- **MCP Async Pattern:** [docs/MCP_ASYNC_SOLUTION.md](docs/MCP_ASYNC_SOLUTION.md)
- **ADRs:** [docs/adr/](docs/adr/)

---

## Contributing

### Development Workflow

1. Create feature branch
2. Implement changes
3. Add/update tests
4. Update documentation
5. Create PR

### Testing

```bash
# All tests
mvn test

# Without LLM integration tests
mvn test -Dtest='!KoogIntegrationTest,!SimpleLLMConnectionTest'
```

---

## Success Criteria

### MVP Success (Feb 2026)

**Technical:**
- ✅ Alle Ports definiert
- ✅ In-Memory Dummy funktioniert
- ✅ Documents werden generiert
- ✅ 3 Workflow-Types (Feature, Bug, Refactoring)
- ✅ MCP Server fully functional
- ✅ Test Coverage >80%

**Business:**
- ✅ MVP funktioniert für Demo & Testing
- ✅ User kann Backend später wählen
- ✅ Real-World Projekt kann durchlaufen werden
- ✅ Generierte Docs sind nutzbar

**Architecture:**
- ✅ Clean Hexagonal Architecture
- ✅ Zero Backend-Commitment in Domain
- ✅ Easy to extend (neue Adapters)

---

## Roadmap

### Now (Jan 2026) - MVP
- ✅ Phase 1: Interfaces + In-Memory Dummy
- 🔄 Phase 2: Document Generation
- 🔄 Phase 3: Multi-Workflow Types
- 🔄 Phase 4: Polish

### Next (Feb-Mar 2026) - Production
- ⏳ Git Adapter (on-demand)
- ⏳ Confluence Adapter (on-demand)
- ⏳ File-based Adapter (on-demand)

### Future (Q2 2026)
- ⏳ Multiple Backends parallel
- ⏳ Advanced Vibe Checks (AI-gestützt)
- ⏳ Team Collaboration Features
- ⏳ CI/CD Integration

---

## License

[Your License Here]

---

## Contact & Support

- **Repository:** https://github.com/your-org/responsible-vibe-mcp
- **Issues:** https://github.com/your-org/responsible-vibe-mcp/issues
- **Docs:** [docs/](docs/)

---

*"Zehn Minuten Design vermeiden drei Stunden Refactoring"*

**Status:** MVP Development (Phase 1-2 in Progress)  
**Last Updated:** January 4, 2026  
**Version:** 0.1.0-SNAPSHOT

