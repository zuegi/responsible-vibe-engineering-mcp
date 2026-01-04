# Implementation Roadmap - Master Plan

**Datum:** 4. Januar 2026  
**Status:** Konsolidierter Master-Plan  
**Ansatz:** Interface-First (Defer Implementation Details)  
**Ziel:** Vision umsetzen mit maximaler Flexibilität

---

## 📊 EXECUTIVE SUMMARY

### Vision vs. Reality

| Feature | Vision | Code | Gap | Priority |
|---------|--------|------|-----|----------|
| MCP Server | ✅ | ✅ 90% | 🟡 Klein | P2 |
| Koog Integration | ✅ | ✅ 100% | ✅ Kein | - |
| Question Catalogs | ✅ | ✅ 70% | 🟡 Klein | P3 |
| **Document Generation** | ✅ | ❌ 10% | 🔴 Groß | **P1** |
| **Persistence Layer** | ✅ | 🟡 50% | 🟡 Mittel | **P1** |
| Workflow Types | ✅ 3 Types | 🟡 1.5 | 🟡 Mittel | P2 |

**Status:** 60% der Vision umgesetzt  
**Nächster Fokus:** Document Generation + Persistence (Interface-First)

---

## 🎯 STRATEGISCHER ANSATZ

### Interface-First Philosophy

> **"Define ports now, implement adapters later"**

**Warum:**
- ✅ Schneller zu MVP (2-3 Tage statt 9 Tage)
- ✅ Keine vorzeitige Festlegung auf Git/Confluence/etc.
- ✅ Perfekte Hexagonal Architecture
- ✅ Backend später wählbar durch User
- ✅ Weniger technisches Risiko

**Prinzip:**
```
Jetzt:   Domain Ports (Interfaces) definieren
Später:  Backend Adapters (Git, Confluence, File) implementieren
```

---

## 🚀 UMSETZUNGSPLAN

### Phase 1: Foundation (Week 1) - Interfaces + Dummy

**Dauer:** 2-3 Tage  
**Ziel:** Persistence & Collaboration Ports ohne Backend-Commitment

#### Tasks:

**1. Core Ports definieren (1 Tag)**

```kotlin
// Memory Persistence
interface MemoryRepositoryPort {
    suspend fun save(context: ExecutionContext)
    suspend fun load(projectPath: String, branch: String): ExecutionContext?
    suspend fun delete(projectPath: String, branch: String)
}

// Document Persistence
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
    suspend fun hasRemote(context: ExecutionContext): Boolean
    suspend fun getStatus(context: ExecutionContext): VCSStatus
}

// Collaboration
interface CollaborationPort {
    suspend fun syncState(context: ExecutionContext): SyncResult
    suspend fun notifyTeam(event: CollaborationEvent, context: ExecutionContext)
    suspend fun getMode(context: ExecutionContext): CollaborationMode
}
```

**Key Insight:** Ports sind **generic** - nicht Git-spezifisch!

**2. In-Memory Dummy Implementation (1-2 Tage)**

```kotlin
/**
 * In-Memory implementation für MVP Testing.
 * 
 * Limitation: Nicht persistent über Server-Restart.
 * Benefit: Zeigt via Logging was echte Implementation tun würde.
 * 
 * Für Production: Ersetze durch echten Adapter (Git, Confluence, etc.)
 */
@Component
@ConditionalOnProperty("persistence.backend", havingValue = "inmemory", matchIfMissing = true)
class InMemoryPersistence : 
    MemoryRepositoryPort,
    DocumentPersistencePort,
    VersionControlPort,
    CollaborationPort {
    
    private val memory = ConcurrentHashMap<String, ExecutionContext>()
    private val documents = ConcurrentHashMap<String, GeneratedDocument>()
    private val logger by rvmcpLogger()
    
    override suspend fun save(context: ExecutionContext) {
        memory[context.key] = context
        logger.info("💾 Context saved (in-memory)")
        logger.warn("⚠️  Not persistent across restarts - configure real backend in application.yml")
    }
    
    override suspend fun saveDocument(doc: GeneratedDocument, context: ExecutionContext): Result<Unit> {
        documents[doc.filename] = doc
        logger.info("📝 Document saved: ${doc.filename}")
        logger.info("   Size: ${doc.content.length} chars")
        logger.info("")
        logger.info("   💡 To enable persistence, configure:")
        logger.info("      persistence.backend=file    # Local file storage")
        logger.info("      persistence.backend=git     # Git repository")
        logger.info("      persistence.backend=confluence  # Confluence wiki")
        return Result.success(Unit)
    }
    
    override suspend fun commit(files: List<String>, message: String, context: ExecutionContext): Result<CommitInfo> {
        logger.info("📝 Would commit to version control:")
        logger.info("   Files: ${files.joinToString(", ")}")
        logger.info("   Message: $message")
        logger.info("")
        logger.info("   💡 Enable version control:")
        logger.info("      persistence.vcs=git")
        return Result.success(CommitInfo.NoOp)
    }
    
    override suspend fun syncState(context: ExecutionContext): SyncResult {
        logger.info("ℹ️  No sync needed (in-memory mode)")
        return SyncResult.NoOpSuccess
    }
    
    // ... weitere Methoden
}
```

**Benefit:** Logging zeigt exakt was passieren würde → Guide für echte Implementation!

**Acceptance Criteria:**
- ✅ Alle Ports definiert mit KDoc
- ✅ In-Memory Dummy implementiert
- ✅ Logging zeigt "would-be" Operationen
- ✅ Tests für Dummy
- ✅ Domain Services nutzen Ports (nicht konkrete Klassen)

---

### Phase 2: Document Generation (Week 2-3)

**Dauer:** 8 Tage  
**Ziel:** Automatische Markdown-Generierung aus Workflow-Results

#### Sprint 2.1: Document Templates (3 Tage)

**1. Template Engine Setup**

Entscheidung: **Kotlin String Templates** (kein externes Framework)
- ✅ Zero dependencies
- ✅ Type-safe
- ✅ IDE support
- ✅ Ausreichend für Markdown

**2. Core Templates**

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
            
            **Priority:** ${req.priority}
            
            **Acceptance Criteria:**
            ${req.acceptanceCriteria.joinToString("\n") { "- $it" }}
            """.trimIndent()
        }}
        
        ## Non-Functional Requirements
        ${requirements.filter { it.type == RequirementType.NON_FUNCTIONAL }.joinToString("\n\n") { req ->
            "### ${req.id}: ${req.title}\n${req.description}"
        }}
        
        ## Architectural Decisions
        ${decisions.joinToString("\n\n") { dec ->
            """
            ### ${dec.title}
            **Context:** ${dec.context}
            **Decision:** ${dec.decision}
            **Rationale:** ${dec.rationale}
            """.trimIndent()
        }}
        
        ## Related Documents
        - [Architecture](Architecture.md)
        - [Implementation Plan](Implementation.md)
        
        ---
        *Generated: ${Instant.now()}*
        *Process: ${projectName}*
        """.trimIndent()
}

object ArchitectureTemplate {
    fun generate(...): String = """
        # Architecture: $projectName
        
        ## System Context
        ...
        
        ## Component Design
        ...
        
        ## Technology Stack
        ...
        
        ## Related Documents
        - [Requirements](Requirements.md)
        """.trimIndent()
}

// Weitere Templates: FeatureSpec, UserStories, TestPlan
```

**3. Data Extraction Logic**

```kotlin
class DocumentDataExtractor {
    fun extractRequirements(phaseResult: PhaseResult): List<Requirement> {
        // Parse LLM output → structured Requirements
        return phaseResult.decisions
            .filter { it.type == DecisionType.REQUIREMENT }
            .map { /* convert to Requirement */ }
    }
    
    fun extractStakeholders(phaseResult: PhaseResult): List<Stakeholder> {
        // Extract from decisions or context
    }
}
```

#### Sprint 2.2: Document Generation Service (5 Tage)

**1. Generation Service**

```kotlin
@Service
class DocumentGenerationService(
    private val persistencePort: DocumentPersistencePort,
    private val dataExtractor: DocumentDataExtractor
) {
    suspend fun generateRequirementsDoc(
        phaseResult: PhaseResult,
        context: ExecutionContext
    ): GeneratedDocument {
        // 1. Extract data from phase result
        val requirements = dataExtractor.extractRequirements(phaseResult)
        val stakeholders = dataExtractor.extractStakeholders(phaseResult)
        val decisions = phaseResult.decisions
        
        // 2. Generate markdown
        val content = RequirementsTemplate.generate(
            projectName = context.projectPath.name,
            summary = phaseResult.summary,
            requirements = requirements,
            stakeholders = stakeholders,
            decisions = decisions
        )
        
        // 3. Create document
        val doc = GeneratedDocument(
            filename = "docs/requirements.md",
            content = content,
            metadata = DocumentMetadata(
                phase = phaseResult.phaseName,
                generatedAt = Instant.now()
            )
        )
        
        // 4. Persist via port
        persistencePort.saveDocument(doc, context)
        
        return doc
    }
    
    suspend fun generateArchitectureDoc(...): GeneratedDocument { ... }
    suspend fun generateFeatureSpecs(...): List<GeneratedDocument> { ... }
}
```

**2. Integration in ExecuteProcessPhaseService**

```kotlin
class ExecuteProcessPhaseService(
    private val workflowExecutor: WorkflowExecutionPort,
    private val docGenerator: DocumentGenerationService
) {
    suspend fun execute(phase: ProcessPhase, context: ExecutionContext): PhaseResult {
        // 1. Execute workflow
        val result = workflowExecutor.executeWorkflow(phase.workflow, context)
        
        // 2. Generate appropriate docs
        when (phase.name) {
            "Requirements Analysis" -> {
                docGenerator.generateRequirementsDoc(result, context)
            }
            "Architecture Design" -> {
                docGenerator.generateArchitectureDoc(result, context)
            }
            "Implementation Planning" -> {
                docGenerator.generateFeatureSpecs(result, context)
            }
        }
        
        return result
    }
}
```

**Acceptance Criteria:**
- ✅ Templates für Requirements, Architecture, Features
- ✅ Data Extraction funktioniert
- ✅ Docs werden via Port persistiert
- ✅ Markdown ist korrekt formatiert
- ✅ Cross-Links zwischen Docs funktionieren

---

### Phase 3: Multi-Workflow Types (Week 4-5)

**Dauer:** 6 Tage  
**Ziel:** Bug-Fix und Refactoring Workflows zusätzlich zu Feature Development

#### Sprint 3.1: Bug-Fix Process (3 Tage)

**1. Process Definition**

```yaml
# processes/bug-fix.yml
id: bug-fix
name: Bug Fix Process
description: Systematic bug investigation and fix

phases:
  - name: Bug Analysis
    workflow: workflows/bug-analysis.yml
    
  - name: Root Cause Investigation  
    workflow: workflows/root-cause.yml
    
  - name: Fix Design
    workflow: workflows/fix-design.yml
    
  - name: Implementation & Testing
    workflow: workflows/fix-implementation.yml
    
  - name: Documentation Update
    workflow: workflows/update-docs.yml
```

**2. Bug-spezifische Workflows**

```yaml
# workflows/bug-analysis.yml
name: Bug Analysis
nodes:
  - id: get_bug_description
    type: ask_catalog_question
    questionId: bug-description
    output: bug_description
    
  - id: analyze_symptoms
    type: llm
    prompt: |
      Analyze the following bug report:
      ${bug_description}
      
      Identify:
      1. Symptoms
      2. Affected components
      3. Reproduction steps
      4. Expected vs. Actual behavior
    output: analysis
    
  - id: document_analysis
    type: system_command
    command: create_file
    parameters:
      path: "docs/bugs/${bug_id}/analysis.md"
      content: "${analysis}"
```

**3. Bug Question Catalog**

```yaml
# question-catalogs/bug-fix-catalog.yml
catalog_name: Bug Fix Questions
questions:
  - id: bug-description
    category: analysis
    text: "Please provide the bug report or description"
    validation_rules:
      - type: not_empty
      - type: min_length
        value: 50
        
  - id: reproduction-steps
    category: analysis
    text: "What are the steps to reproduce the bug?"
    
  - id: expected-behavior
    category: analysis
    text: "What was the expected behavior?"
```

**Acceptance Criteria:**
- ✅ bug-fix Process definiert
- ✅ Bug-spezifische Workflows implementiert
- ✅ Bug Question Catalog erstellt
- ✅ `start_process(processId: "bug-fix")` funktioniert

#### Sprint 3.2: Refactoring Process (3 Tage)

Analog zu Bug-Fix, mit refactoring-spezifischen Workflows.

---

### Phase 4: Polish & MCP Resources (Week 6)

**Dauer:** 5 Tage  
**Ziel:** Production-Readiness

#### Tasks:

**1. MCP Resources (2 Tage)**

```kotlin
// MCP Resource: context://project/branch
server.addResource(
    uri = "context://{project}/{branch}",
    name = "Execution Context",
    description = "Current execution context for project and branch"
) { request ->
    val project = request.params["project"]
    val branch = request.params["branch"]
    
    val context = memoryRepository.load(project, branch)
    
    ResourceContent(
        uri = request.uri,
        mimeType = "application/json",
        text = Json.encodeToString(context)
    )
}

// MCP Resource: process://process-id
server.addResource(
    uri = "process://{processId}",
    name = "Process Definition",
    description = "Engineering process definition"
) { request ->
    val processId = request.params["processId"]
    val process = processRepository.findById(ProcessId(processId))
    
    ResourceContent(
        uri = request.uri,
        mimeType = "application/json",
        text = Json.encodeToString(process)
    )
}
```

**2. Bug Fixes & TODOs (2 Tage)**

- ✅ Fix hardcoded Question Catalog paths
- ✅ Remove FIXME comments
- ✅ Improve error messages
- ✅ Add input validation

**3. Documentation (1 Tag)**

- ✅ Update README.md
- ✅ Usage Guide
- ✅ Configuration Examples
- ✅ Adapter Implementation Guide (für Post-MVP)

---

## 📅 TIMELINE & MILESTONES

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
- **M3 (Feb 7):** Bug-Fix & Refactoring Workflows implementiert
- **MVP (Feb 14):** Production-ready mit In-Memory Backend

**Post-MVP (on-demand):**
- Git Adapter: +5 Tage (wenn User Git will)
- Confluence Adapter: +5 Tage (wenn User Confluence will)
- File Adapter: +2 Tage (wenn User File-based will)

---

## 🔌 POST-MVP: Backend Adapters (On-Demand)

### Git Adapter Implementation

**Nur wenn User explizit Git als Backend wählt!**

```kotlin
@Configuration
@ConditionalOnProperty("persistence.backend", havingValue = "git")
class GitAdapterConfiguration {
    
    @Bean
    fun gitAdapter(
        gitPort: GitPort,
        fileSystem: FileSystem
    ): DocumentPersistencePort {
        return GitDocumentAdapter(gitPort, fileSystem)
    }
}

class GitDocumentAdapter(
    private val gitPort: GitPort,
    private val fileSystem: FileSystem
) : DocumentPersistencePort {
    
    override suspend fun saveDocument(
        doc: GeneratedDocument,
        context: ExecutionContext
    ): Result<Unit> {
        return try {
            // 1. Write file
            val file = File(context.projectPath, doc.filename)
            file.parentFile.mkdirs()
            file.writeText(doc.content)
            
            // 2. Git commit
            gitPort.commit(
                files = listOf(doc.filename),
                message = generateCommitMessage(doc, context),
                context = context
            )
            
            // 3. Git push (optional, basierend auf config)
            if (config.autoPush) {
                gitPort.push(context)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Configuration:**
```yaml
persistence:
  backend: git  # inmemory | file | git | confluence
  
  git:
    auto_push: true
    commit_message_template: "docs: {{phase}} - {{summary}}"
```

### Confluence Adapter Implementation

```kotlin
class ConfluenceDocumentAdapter(
    private val confluenceClient: ConfluenceClient
) : DocumentPersistencePort {
    
    override suspend fun saveDocument(doc: GeneratedDocument, context: ExecutionContext): Result<Unit> {
        return try {
            confluenceClient.createOrUpdatePage(
                spaceKey = config.spaceKey,
                title = doc.title,
                content = markdownToConfluenceStorage(doc.content),
                parentPageId = findOrCreateProjectPage(context)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 📊 EFFORT ESTIMATION

| Phase | Duration | Dev Days | Dependencies |
|-------|----------|----------|--------------|
| Phase 1: Interfaces + Dummy | 3 days | 2-3 days | - |
| Phase 2: Doc Generation | 2 weeks | 8 days | Phase 1 |
| Phase 3: Multi-Workflow | 2 weeks | 6 days | Phase 2 |
| Phase 4: Polish | 1 week | 5 days | Phase 3 |
| **MVP TOTAL** | **~6 weeks** | **21-22 days** | - |

**Post-MVP (on-demand):**
| Git Adapter | 1 week | 5 days | User wünscht Git |
| Confluence Adapter | 1 week | 5 days | User wünscht Confluence |
| File Adapter | 2-3 days | 2 days | User wünscht File-based |

**Assumptions:**
- 1 Full-Time Developer
- ~4 effective days per week
- No major blockers

**MVP Date:** Mitte Februar 2026 ✅

---

## ✅ SUCCESS CRITERIA

### Technical Success

- ✅ Alle Ports definiert und dokumentiert
- ✅ In-Memory Dummy funktioniert
- ✅ Documents werden automatisch generiert
- ✅ Markdown ist korrekt und lesbar
- ✅ 3 Workflow-Types (Feature, Bug, Refactoring)
- ✅ MCP Server fully functional
- ✅ Test Coverage >80%
- ✅ Keine TODOs/FIXMEs im Code

### Business Success

- ✅ MVP funktioniert für Demo & Testing
- ✅ User kann Backend später wählen
- ✅ Ein Real-World Projekt kann durchlaufen werden
- ✅ Generierte Docs sind nutzbar

### Architecture Success

- ✅ Clean Hexagonal Architecture
- ✅ Ports & Adapters korrekt getrennt
- ✅ Zero Backend-Commitment in Domain
- ✅ Easy to extend (neue Adapter)

---

## 💡 ARCHITECTURE DECISIONS

### ADR-001: Interface-First Approach

**Context:** Unsicherheit über besten Persistence-Backend (Git vs. Confluence vs. File)  
**Decision:** Ports jetzt, Adapters später  
**Rationale:**
- ✅ Schneller zu MVP
- ✅ Keine vorzeitige Festlegung
- ✅ User kann Backend wählen
- ✅ Multiple Backends parallel möglich
- ✅ Perfekte Hexagonal Architecture

**Consequences:**
- ✅ MVP nutzt In-Memory (nicht persistent)
- ✅ Echter Adapter in 5-7 Tagen implementierbar
- ✅ Kein Refactoring nötig für Adapter-Integration

### ADR-002: Kotlin String Templates für Documents

**Context:** Brauchen Template Engine für Markdown  
**Decision:** Kotlin multiline strings statt Mustache/FreeMarker  
**Rationale:**
- ✅ Zero dependencies
- ✅ Type-safe
- ✅ IDE support
- ✅ Ausreichend für Markdown

### ADR-003: In-Memory Dummy mit Educational Logging

**Context:** MVP ohne echten Backend  
**Decision:** In-Memory mit detailliertem Logging  
**Rationale:**
- ✅ Funktioniert für Testing/Demo
- ✅ Logs zeigen was passieren würde
- ✅ Guide für echte Implementation
- ✅ User versteht System-Behavior

---

## 🚀 QUICK START (Diese Woche)

### Monday: Port Definitions

```bash
cd src/main/kotlin/ch/zuegi/rvmcp/domain/port/output

# Create port interfaces
touch MemoryRepositoryPort.kt
touch DocumentPersistencePort.kt
touch VersionControlPort.kt
touch CollaborationPort.kt
```

**Content:** Interface + KDoc

### Tuesday-Wednesday: In-Memory Dummy

```bash
cd src/main/kotlin/ch/zuegi/rvmcp/adapter/output/persistence

touch InMemoryPersistence.kt
```

**Content:** Implement all ports with in-memory + logging

### Thursday: Integration & Tests

```bash
# Update Services to use ports
# Write integration tests
```

### Friday: Documentation

```bash
# Update README
# Document in-memory limitations
# Write adapter implementation guide
```

**By Friday:** MVP infrastructure ready! ✅

---

## 📚 RELATED DOCUMENTATION

### Keep:
- ✅ README.md (project overview)
- ✅ WARP.md (vision & philosophy)
- ✅ CONFIGURATION.md (setup guide)
- ✅ KOOG_INTEGRATION.md (Koog 0.6.0 guide)
- ✅ MCP_ASYNC_SOLUTION.md (async pattern)
- ✅ adr/0001-coroutine-context... (ADR)

### Archive (superseded by this document):
- ⚠️ GIT_INTEGRATION_ALTERNATIVES.md (now: Interface-First)
- ⚠️ MULTI_USER_COLLABORATION.md (solved by Adapters)
- ⚠️ IMPLEMENTATION_PLAN_REVISION.md (consolidated here)
- ⚠️ INTERFACE_FIRST_APPROACH.md (consolidated here)
- ⚠️ CONSOLIDATION_ANALYSIS.md (done)

**Recommendation:** Move to `docs/archive/` folder

---

## 🎯 FINAL NOTES

### Why This Plan Works

1. **Interface-First = Maximum Flexibility**
   - Backend kann später gewählt werden
   - Keine Technical Debt durch vorzeitige Entscheidungen
   - User hat die Wahl

2. **Schneller zu MVP**
   - 2-3 Tage statt 9 Tage für Phase 1
   - MVP Mitte Februar statt Ende Februar

3. **Clean Architecture**
   - Perfekte Hexagonal Pattern
   - Domain unabhängig von Infrastructure
   - Leicht testbar

4. **Incremental Value**
   - MVP funktioniert (in-memory)
   - Production-Backend später (5-7 Tage)
   - Kein Refactoring nötig

### Next Steps

1. **Approve Plan** - Review & Sign-off
2. **Start Phase 1** - Interfaces + Dummy (diese Woche)
3. **Continue incrementally** - Phase by Phase
4. **Add Adapters on-demand** - Nach MVP, wenn User Backend wählt

---

**Erstellt am:** 4. Januar 2026  
**Version:** 1.0 (Master Plan - Konsolidiert)  
**Status:** Ready for Implementation  
**Next Review:** Nach MVP (Mitte Februar 2026)

