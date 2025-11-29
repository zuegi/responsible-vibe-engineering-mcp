# Von Requirements zu Stories - Der verfeinerte Prozess

## 🔄 Der richtige Flow

```
Requirements Doc
      ↓
Feature Specification Doc (NEU!) ← DAS FEHLT!
      ↓
Architecture Doc
      ↓
User Stories
```

---

## 📋 Phase 1: Requirements Document (Hast du bereits)

**Output**: Requirements.md
- Business Context
- User Personas
- High-Level Features
- Non-Functional Requirements
- Constraints

**Beispiel**:
```markdown
## Features Overview
- Portfolio Management
- Position Tracking
- Performance Analytics
- Reporting
```

---

## 📝 Phase 2: Feature Specification Document (DER FEHLENDE SCHRITT!)

### **Zweck:**
- **Detailliert** jedes Feature beschreiben
- **User Flows** dokumentieren
- **Business Rules** festhalten
- **Akzeptanzkriterien** definieren
- **Dependencies** klären

### **Template: Feature Specification Document**

```markdown
# Feature Specification Document

## Document Info
- **Version**: 1.0
- **Date**: 2025-01-21
- **Authors**: Product Owner, Tech Lead
- **Status**: Draft / Review / Approved

---

## 1. Feature: Portfolio Management

### 1.1 Overview
**Feature ID**: F-001  
**Feature Name**: Portfolio Management  
**Priority**: Must Have (MVP)  
**Epic**: PROJ-EPIC-001

**Description**:
Users can create, view, edit, and delete investment portfolios. 
Each portfolio contains a collection of positions (instruments) 
and tracks performance over time.

**Business Value**:
- Enables structured organization of investments
- Replaces error-prone Excel spreadsheets
- Foundation for all other features

**Success Metrics**:
- 90% of users create at least one portfolio within first week
- Average time to create portfolio < 2 minutes
- Zero data loss incidents

---

### 1.2 User Personas

**Primary Users**:
- Portfolio Manager (Heavy User, Daily)
- Financial Analyst (Medium User, Weekly)

**Secondary Users**:
- Compliance Officer (View Only, Monthly)

---

### 1.3 Functional Requirements

#### FR-001: Create Portfolio

**User Story (High-Level)**:
> Als Portfolio Manager möchte ich ein neues Portfolio erstellen,
> damit ich meine Investments organisieren kann

**Detailed Flow**:

**Pre-conditions**:
- User is authenticated
- User has not reached max portfolio limit (100)

**Main Flow**:
1. User navigates to Portfolio Overview page
2. User clicks "New Portfolio" button
3. System displays "Create Portfolio" form with fields:
   - Portfolio Name (text, required)
   - Currency (dropdown, required)
   - Description (textarea, optional)
   - Owner (auto-filled, read-only)
4. User fills in required fields
5. User clicks "Create" button
6. System validates input (see Validation Rules)
7. System creates portfolio in database
8. System generates Portfolio ID (UUID)
9. System sets created_at timestamp
10. System redirects to Portfolio Detail page
11. System displays success message: "Portfolio '{name}' created successfully"

**Alternative Flows**:

**Alt-1: Validation Fails**
- At Step 6: System detects validation error
- System displays inline error messages next to invalid fields
- User corrects errors
- User clicks "Create" again
- Continue with Step 7

**Alt-2: Duplicate Name**
- At Step 7: System detects duplicate portfolio name
- System displays error: "Portfolio name already exists"
- User changes name
- Continue with Step 5

**Alt-3: User Cancels**
- At any point: User clicks "Cancel" button
- System displays confirmation dialog: "Discard changes?"
- If confirmed: System returns to Portfolio Overview
- If cancelled: User stays on form

**Exception Flows**:

**Exc-1: Database Error**
- At Step 8: Database is unavailable
- System displays error: "Unable to create portfolio. Please try again."
- System logs error for monitoring
- User can retry

**Exc-2: Network Timeout**
- At Step 8: Request times out (>30s)
- System displays error: "Request timed out. Please check your connection."
- User can retry

**Post-conditions (Success)**:
- Portfolio exists in database
- Portfolio appears in user's portfolio list
- User is on Portfolio Detail page
- Audit log entry created

**Post-conditions (Failure)**:
- No portfolio created
- No changes to database
- User remains on form with error messages

---

#### Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| Portfolio Name | Required | "Portfolio name is required" |
| Portfolio Name | Length 2-100 chars | "Name must be between 2 and 100 characters" |
| Portfolio Name | No special chars except dash/space | "Name can only contain letters, numbers, spaces and dashes" |
| Portfolio Name | Unique per user | "Portfolio name already exists" |
| Currency | Required | "Please select a currency" |
| Currency | Must be valid ISO 4217 code | "Invalid currency code" |
| Description | Max 500 chars | "Description cannot exceed 500 characters" |

---

#### Business Rules

**BR-001**: Portfolio Name Uniqueness
- Portfolio names must be unique per user
- Case-insensitive comparison
- "My Portfolio" = "my portfolio" = "MY PORTFOLIO"

**BR-002**: Currency Selection
- Currency is set at creation
- Currency CANNOT be changed after creation
- Reason: Historical position prices are in original currency

**BR-003**: Portfolio Limits
- Free users: Max 5 portfolios
- Premium users: Max 100 portfolios
- Enterprise users: Unlimited

**BR-004**: Soft Delete
- Portfolios are never hard-deleted
- Deleted portfolios are marked as "archived"
- Archived portfolios don't count towards limit
- Can be restored within 30 days

**BR-005**: Auto-Generated Fields
- ID: System-generated UUID
- Owner: Current authenticated user
- Created At: Server timestamp (UTC)
- Updated At: Server timestamp (UTC)

---

#### Data Model

```kotlin
data class Portfolio(
    val id: UUID,                    // System-generated
    val userId: UUID,                // From authentication
    val name: String,                // User input (2-100 chars)
    val currency: Currency,          // User input (ISO 4217)
    val description: String?,        // Optional (max 500 chars)
    val status: PortfolioStatus,     // ACTIVE, ARCHIVED
    val totalValue: Money,           // Calculated field
    val createdAt: Instant,          // System-generated
    val updatedAt: Instant           // System-generated
)

enum class Currency {
    USD, EUR, CHF, GBP, JPY, // ... etc
}

enum class PortfolioStatus {
    ACTIVE, ARCHIVED
}
```

---

#### API Contract

**Endpoint**: `POST /api/portfolios`

**Request**:
```json
{
  "name": "My Investment Portfolio",
  "currency": "CHF",
  "description": "Long-term investments"
}
```

**Response (201 Created)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My Investment Portfolio",
  "currency": "CHF",
  "description": "Long-term investments",
  "owner": "user@example.com",
  "status": "ACTIVE",
  "totalValue": {
    "amount": 0.0,
    "currency": "CHF"
  },
  "createdAt": "2025-01-21T10:30:00Z",
  "updatedAt": "2025-01-21T10:30:00Z"
}
```

**Response (400 Bad Request)**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2025-01-21T10:30:00Z",
  "validationErrors": [
    {
      "field": "name",
      "rejectedValue": "",
      "message": "Portfolio name is required"
    }
  ]
}
```

**Response (409 Conflict)**:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Portfolio name already exists",
  "timestamp": "2025-01-21T10:30:00Z"
}
```

---

#### UI Wireframe

```
┌──────────────────────────────────────────────────┐
│  Create Portfolio                            [X] │
├──────────────────────────────────────────────────┤
│                                                  │
│  Portfolio Name *                                │
│  ┌────────────────────────────────────────────┐ │
│  │ My Investment Portfolio                    │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  Currency *                                      │
│  ┌────────────────────────────────────────────┐ │
│  │ CHF - Swiss Franc                      ▼  │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  Description (Optional)                          │
│  ┌────────────────────────────────────────────┐ │
│  │ Long-term investments                      │ │
│  │                                            │ │
│  └────────────────────────────────────────────┘ │
│  0 / 500 characters                              │
│                                                  │
│         [Cancel]              [Create Portfolio] │
└──────────────────────────────────────────────────┘
```

---

#### Acceptance Criteria (Testable!)

**AC-001**: Portfolio Creation Success
```gherkin
Given I am logged in as a Portfolio Manager
And I have fewer than 100 portfolios
When I navigate to "Portfolios" page
And I click "New Portfolio" button
And I enter "My Portfolio" in name field
And I select "CHF" from currency dropdown
And I click "Create" button
Then I see success message "Portfolio 'My Portfolio' created successfully"
And I am redirected to Portfolio Detail page
And the portfolio appears in my portfolio list
And the portfolio has status "ACTIVE"
And the portfolio has totalValue 0.00 CHF
```

**AC-002**: Validation - Missing Name
```gherkin
Given I am on "Create Portfolio" form
When I leave name field empty
And I click "Create" button
Then I see error "Portfolio name is required"
And the portfolio is NOT created
And I remain on the form
```

**AC-003**: Duplicate Name Prevention
```gherkin
Given I already have a portfolio named "My Portfolio"
When I try to create another portfolio with name "My Portfolio"
Then I see error "Portfolio name already exists"
And the portfolio is NOT created
```

**AC-004**: Currency Cannot Be Changed
```gherkin
Given I have created a portfolio with currency "CHF"
When I view the portfolio details
Then I cannot change the currency
And the currency field is read-only
```

---

#### Dependencies

**Depends On**:
- User Authentication (PROJ-001)
- Database Schema (PROJ-002)

**Blocks**:
- Add Position to Portfolio (F-002)
- Edit Portfolio (F-003)
- Delete Portfolio (F-004)

---

#### Non-Functional Requirements

**Performance**:
- Portfolio creation must complete in < 500ms (P95)
- API response time < 200ms (P95)

**Security**:
- Only authenticated users can create portfolios
- Users can only see their own portfolios
- SQL injection prevention via prepared statements
- XSS prevention via input sanitization

**Usability**:
- Form must be keyboard accessible (Tab navigation)
- Success message must be visible for 3 seconds
- Error messages must be inline near field

**Scalability**:
- System must support 100,000 portfolios
- Database must handle 1,000 concurrent portfolio creations

---

#### Testing Strategy

**Unit Tests** (Domain Layer):
- Portfolio creation with valid data
- Validation rules enforcement
- Business rules enforcement
- Edge cases (empty strings, special chars, etc.)

**Integration Tests** (API Layer):
- POST /api/portfolios with valid data → 201
- POST /api/portfolios with missing name → 400
- POST /api/portfolios with duplicate name → 409
- POST /api/portfolios without authentication → 401

**E2E Tests** (UI Layer):
- Complete user flow: Login → Create Portfolio → Verify
- Validation error handling
- Cancel button functionality

---

#### Risks & Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Currency list outdated | Medium | Low | Auto-update from ISO source |
| Duplicate detection fails | Low | High | Unique constraint in DB |
| Performance degrades at scale | Medium | Medium | Database indexing, caching |

---

#### Open Questions

- [ ] Should we support custom currency codes?
- [ ] What happens if user reaches 100 portfolios?
- [ ] Should archived portfolios be visible in list?
- [ ] Can users share portfolios with other users?

---

#### Implementation Notes

**Architecture**:
- Domain: `Portfolio` aggregate root
- Port: `PortfolioRepository` interface
- Adapter: `PortfolioPersistenceAdapter` implementation

**Database**:
- Table: `portfolios`
- Indexes: `user_id`, `(user_id, name)` unique

**Estimated Effort**: 5 Story Points (~2-3 days)

---

### 1.4 Feature: View Portfolio List

[Similar detailed specification...]

### 1.5 Feature: Edit Portfolio

[Similar detailed specification...]

### 1.6 Feature: Delete Portfolio

[Similar detailed specification...]

---

## 2. Feature: Position Management

### 2.1 Overview
...

### 2.2 User Personas
...

### 2.3 Functional Requirements

#### FR-010: Add Position to Portfolio
...

[Continue for all features...]

---

# Summary

This Feature Specification Document provides:
✅ Detailed user flows for every feature
✅ Complete validation rules
✅ Business logic clearly defined
✅ API contracts specified
✅ Testable acceptance criteria
✅ Clear dependencies
✅ Risk assessment

**Next Steps**:
1. Review & Approval by Product Owner
2. Architecture Design based on features
3. Break down into User Stories for Sprint Planning
```

---

## 🔄 Der neue Flow im Detail

### **Schritt 1: Requirements Doc → Feature Spec Doc**

**Input**: Requirements.md (High-Level)
```markdown
## Features
- Portfolio Management
- Position Tracking
- Performance Analytics
```

**Process**: Feature Specification Workshop (1-2 Wochen)
- Product Owner führt durch jedes Feature
- Team fragt nach Details
- UX Designer sketcht Wireframes
- Tech Lead identifiziert technische Constraints
- QA definiert Testfälle

**Output**: FeatureSpec.md (Detailed)
```markdown
## Feature: Portfolio Management
### FR-001: Create Portfolio
- Detailed Flow (10+ Schritte)
- Validation Rules (6+ Regeln)
- Business Rules (5+ Regeln)
- API Contract
- Acceptance Criteria (4+ ACs)
- Dependencies
```

---

### **Schritt 2: Feature Spec Doc → Architecture Doc**

**Input**: FeatureSpec.md

**Process**: Architecture Workshop (3-5 Tage)
- Tech Lead analysiert Features
- Identifiziert Aggregates (Portfolio, Position)
- Definiert Ports & Adapters
- Erstellt ADRs
- Designed Data Model
- Plant Integrations

**Output**: Architecture.md
```markdown
## Architecture Decisions
- ADR-001: Hexagonal Architecture
- ADR-002: PostgreSQL for Persistence
- ADR-003: Event-Driven for Notifications

## Component Design
- Portfolio Aggregate
- Position Entity
- PortfolioRepository Port
```

---

### **Schritt 3: Feature Spec Doc → User Stories**

**Input**: FeatureSpec.md (Specifically FR-001: Create Portfolio)

**Process**: Story Breakdown Session (1 Tag)
- Nehme EINE Functional Requirement (z.B. FR-001)
- Breche in technische Stories
- Jede Story = vertikaler Slice (UI → API → DB)

**Output**: 4-6 User Stories

```markdown
### Von FR-001 zu Stories:

FR-001: Create Portfolio
    ↓
Epic: PROJ-10 - Portfolio Creation
    ↓
Stories:
├── PROJ-11: Portfolio Domain Model (3 pts)
├── PROJ-12: Portfolio Repository Port (2 pts)
├── PROJ-13: Portfolio Persistence Adapter (5 pts)
├── PROJ-14: Create Portfolio API Endpoint (5 pts)
├── PROJ-15: Portfolio Validation (3 pts)
└── PROJ-16: Create Portfolio UI Form (8 pts)
```

---

## 🎯 Story Generation aus Feature Spec (Praktisch)

### **Automatisierter Ansatz:**

```kotlin
// Story Generator Tool
class StoryGenerator {
    
    fun generateStories(featureSpec: FeatureSpec): List<UserStory> {
        val stories = mutableListOf<UserStory>()
        
        // 1. Domain Model Story
        stories.add(createDomainStory(featureSpec))
        
        // 2. Repository Story
        stories.add(createRepositoryStory(featureSpec))
        
        // 3. Persistence Story
        stories.add(createPersistenceStory(featureSpec))
        
        // 4. API Story
        stories.add(createApiStory(featureSpec))
        
        // 5. Validation Story
        stories.add(createValidationStory(featureSpec))
        
        // 6. UI Story
        stories.add(createUiStory(featureSpec))
        
        return stories
    }
    
    private fun createDomainStory(spec: FeatureSpec): UserStory {
        return UserStory(
            title = "Implement ${spec.name} Domain Model",
            description = """
                Als Developer möchte ich das Domain Model für ${spec.name} implementieren,
                damit die Business Logic strukturiert ist.
                
                **Scope**:
                - Domain Entity: ${spec.entityName}
                - Value Objects: ${spec.valueObjects.joinToString()}
                - Business Rules: ${spec.businessRules.size} rules
            """.trimIndent(),
            acceptanceCriteria = spec.businessRules.map { rule ->
                "Given ${rule.given} When ${rule.when} Then ${rule.then}"
            },
            storyPoints = estimateDomainComplexity(spec),
            labels = listOf("domain", "backend")
        )
    }
    
    // ... weitere Generator-Methoden
}
```

---

## 📋 Template: Jira Story (Generiert aus Feature Spec)

```markdown
**Story**: PROJ-14 - Create Portfolio API Endpoint

**Epic**: PROJ-10 - Portfolio Creation

**Generated From**: Feature Spec FR-001 (Create Portfolio)

---

## Description

Als Developer möchte ich den API Endpoint für Portfolio-Erstellung implementieren,
damit Users Portfolios über die API erstellen können.

**Extracted From Feature Spec**:
- Feature ID: F-001
- Functional Requirement: FR-001
- Section: 1.3 Functional Requirements

---

## Scope (From Feature Spec)

### API Contract (Copied from Feature Spec)
```
POST /api/portfolios

Request Body:
{
"name": "string (2-100 chars)",
"currency": "string (ISO 4217)",
"description": "string (optional, max 500 chars)"
}

Response: 201 Created
Response: 400 Bad Request (validation errors)
Response: 409 Conflict (duplicate name)
```

### Validation Rules (Copied from Feature Spec)
- Portfolio name: Required, 2-100 chars, unique per user
- Currency: Required, valid ISO 4217
- Description: Optional, max 500 chars

### Business Rules (Referenced from Feature Spec)
- BR-001: Portfolio name uniqueness (case-insensitive)
- BR-002: Currency cannot be changed after creation
- BR-003: Portfolio limits (Free: 5, Premium: 100)
- BR-005: Auto-generated fields (ID, timestamps)

---

## Acceptance Criteria (Extracted from Feature Spec)

### AC-001: Successful Creation
```gherkin
Given I am authenticated
And I have < 100 portfolios
When I POST /api/portfolios with valid data
Then I receive 201 Created
And response contains portfolio with ID
And portfolio appears in database
```

### AC-002: Validation Errors
```gherkin
Given I POST /api/portfolios with missing name
Then I receive 400 Bad Request
And response contains validation errors
And portfolio is NOT created
```

### AC-003: Duplicate Name
```gherkin
Given I have portfolio "My Portfolio"
When I POST /api/portfolios with name "My Portfolio"
Then I receive 409 Conflict
And response contains error "Portfolio name already exists"
```

---

## Technical Implementation

### Files to Create/Modify
```
src/main/kotlin/adapter/inbound/rest/
  └── PortfolioController.kt

src/main/kotlin/application/exception/
  └── GlobalExceptionHandler.kt

src/test/kotlin/adapter/inbound/rest/
  └── PortfolioControllerTest.kt
```

### Dependencies
- **Depends On**:
    - PROJ-11: Portfolio Domain Model
    - PROJ-12: Portfolio Repository Port
    - PROJ-13: Portfolio Persistence Adapter
    - PROJ-15: Portfolio Validation

- **Blocks**:
    - PROJ-16: Portfolio UI Form

---

## Testing (From Feature Spec Testing Strategy)

### Integration Tests
- [ ] POST with valid data → 201 Created
- [ ] POST with missing name → 400 Bad Request
- [ ] POST with duplicate name → 409 Conflict
- [ ] POST without authentication → 401 Unauthorized
- [ ] POST with invalid currency → 400 Bad Request

### Performance Tests
- [ ] API response time < 200ms (P95)
- [ ] Portfolio creation < 500ms (P95)

---

## Non-Functional Requirements (From Feature Spec)

- **Security**: Only authenticated users, input sanitization
- **Performance**: Response time < 200ms (P95)
- **Validation**: All fields validated before persistence

---

## Story Points: 5

**Estimation Based On**:
- API endpoint implementation: Medium complexity
- Validation logic: 6+ rules
- Error handling: 3 error cases
- Testing: 5+ integration tests

---

## Labels
`backend` `api` `mvp` `portfolio-management`

---

## Feature Spec Reference

📄 **Source Document**: FeatureSpec.md  
📍 **Section**: 1.3 Functional Requirements → FR-001  
🔗 **Link**: [View Feature Spec](link-to-confluence/FR-001)
```

---

## 🛠️ Tooling-Ansatz

### **Option 1: Template-Based Generation**

```bash
# Script: generate-stories.sh

#!/bin/bash

FEATURE_SPEC="docs/FeatureSpec.md"
OUTPUT_DIR="stories"

# Parse Feature Spec
# For each Functional Requirement (FR-XXX):

for fr in $(grep "^#### FR-" $FEATURE_SPEC); do
    FR_ID=$(echo $fr | cut -d: -f1)
    
    # Generate Domain Story
    cat > "$OUTPUT_DIR/${FR_ID}-domain.md" << EOF
Title: Implement ${FR_ID} Domain Model
[Template...]
EOF
    
    # Generate API Story
    cat > "$OUTPUT_DIR/${FR_ID}-api.md" << EOF
Title: Implement ${FR_ID} API Endpoint
[Template...]
EOF
    
    # Generate UI Story
    cat > "$OUTPUT_DIR/${FR_ID}-ui.md" << EOF
Title: Implement ${FR_ID} UI
[Template...]
EOF
done
```

### **Option 2: LLM-Based Generation**

```kotlin
// Use LLM to generate stories from Feature Spec

val prompt = """
You are a Story Generator.

Input: Feature Specification Document (Markdown)
Output: Jira User Stories

For each Functional Requirement in the Feature Spec:
1. Extract key information (flow, rules, API contract)
2. Generate 4-6 technical stories
3. Include acceptance criteria from Feature Spec
4. Estimate story points based on complexity

Feature Spec:
${featureSpecContent}

Generate stories in Jira format.
"""

val stories = llm.generate(prompt)
```

---

## ✅ Vorteile dieses Ansatzes

### **Feature Spec als Single Source of Truth**
- ✅ Alle Details an einem Ort
- ✅ Product Owner & Team arbeiten am selben Dokument
- ✅ Stories referenzieren Feature Spec (Traceability)

### **Klare Struktur**
- ✅ Feature Spec = WAS & WARUM
- ✅ Architecture Doc = WIE (technisch)
- ✅ Stories = WER macht WAS WANN

### **Einfache Story-Generierung**
- ✅ Feature Spec ist detailliert genug
- ✅ Stories können semi-automatisch generiert werden
- ✅ Jede Story hat klare Acceptance Criteria

### **Bessere Kommunikation**
- ✅ PO versteht Feature Spec (business language)
- ✅ Developers verstehen Feature Spec (technical details)
- ✅ QA versteht Feature Spec (test cases)

---

## 🎯 Zusammenfassung

**Alter Flow (zu schnell)**:
```
Requirements Doc (High-Level)
    ↓
Architecture Doc
    ↓
Stories (ohne Details)
```

**Neuer Flow (strukturiert)**:
```
Requirements Doc (High-Level)
    ↓
Feature Specification Doc (DETAILED!) ← NEU!
    ├─→ Architecture Doc (Technical Design)
    └─→ User Stories (Generated from Feature Spec)
```

**Das Feature Spec Doc ist der Schlüssel!**
- Product Owner schreibt es (mit Team Input)
- Enthält ALLE Details für eine Feature
- Ist Grundlage für Architecture & Stories
- Bleibt als Referenz bestehen

Macht das Sinn für euch? 🎯