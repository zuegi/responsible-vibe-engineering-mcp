# Von Requirements zu Stories - Praktischer Workflow

## 🔄 Der komplette Prozess

```
Discovery → Requirements → Architecture → Stories → Sprint Planning
   ↓            ↓              ↓            ↓           ↓
Fragen      Document       ADRs        Jira      Execution
```

---

## 📝 Schritt 1: Discovery Interview (1-2 Wochen)

### **Meeting 1: Kick-Off (2h)**
**Teilnehmer**: Product Owner, Key Stakeholders, Tech Lead

**Agenda:**
1. Problem & Vision (30 min)
    - Was ist das Problem?
    - Warum jetzt lösen?
    - Vision Statement

2. Users & Stakeholders (30 min)
    - Wer sind die User?
    - Welche Personas?
    - Wie viele User?

3. Scope & Prioritäten (30 min)
    - Was ist MVP?
    - Top 5 Features
    - Was ist out of scope?

4. Constraints (30 min)
    - Timeline
    - Budget
    - Technology Stack

**Output**:
- Problem Statement (1 Seite)
- 3-5 High-Level User Stories
- MoSCoW Priorisierung

---

### **Meeting 2: Functional Deep Dive (3h)**
**Teilnehmer**: Product Owner, Domain Experts, Tech Lead, UX

**Agenda:**
1. Feature Walkthrough (2h)
    - Für jedes Feature:
        - User Flow zeigen/skizzieren
        - Business Rules klären
        - Edge Cases diskutieren
        - Abhängigkeiten identifizieren

2. Integration Requirements (30 min)
    - Welche Systeme?
    - Welche APIs?
    - Datenaustausch?

3. Reporting & Analytics (30 min)
    - Welche Reports?
    - Welche KPIs?
    - Export Formate?

**Output**:
- Feature List mit Details
- User Flow Diagramme
- Integration Map

---

### **Meeting 3: Non-Functional Requirements (2h)**
**Teilnehmer**: Product Owner, Tech Lead, DevOps, Security

**Agenda:**
1. Performance & Scale (45 min)
    - Concurrent Users
    - Response Time
    - Data Volume
    - Growth Projection

2. Security & Compliance (45 min)
    - Sensitive Data
    - Authentication
    - Authorization
    - Compliance (GDPR, etc.)

3. Operations (30 min)
    - Availability
    - Monitoring
    - Backups
    - Support

**Output**:
- NFR Document
- Security Requirements
- SLA Definitions

---

## 📄 Schritt 2: Requirements Document erstellen (3-5 Tage)

### **Template: Requirements Document**

```markdown
# Requirements Document: [Project Name]

## 1. Executive Summary
- **Problem**: [1-2 Sätze]
- **Solution**: [1-2 Sätze]
- **Success Metrics**: [3-5 KPIs]
- **Timeline**: [Go-Live Date]
- **Budget**: [Amount]

## 2. Business Context

### 2.1 Problem Statement
[Detaillierte Beschreibung des Problems]

### 2.2 Vision
[Was soll erreicht werden?]

### 2.3 Goals & Metrics
| Goal | Metric | Target |
|------|--------|--------|
| User Adoption | Active Users | 10,000 in 6 months |
| Performance | Response Time | < 200ms |
| Satisfaction | NPS Score | > 50 |

## 3. Users & Stakeholders

### 3.1 Personas

**Persona 1: Portfolio Manager**
- **Name**: Anna Schmidt
- **Role**: Portfolio Manager
- **Goals**: Schnell Portfolios verwalten, Performance tracken
- **Pain Points**: Manuelle Excel-Listen, keine Real-Time Daten
- **Technical Skills**: Medium
- **Frequency**: Daily

[Weitere Personas...]

### 3.2 Stakeholder Map
| Stakeholder | Role | Interest | Influence |
|-------------|------|----------|-----------|
| CFO | Sponsor | High | High |
| IT Manager | Approver | Medium | High |
| End Users | Users | High | Low |

## 4. Functional Requirements

### 4.1 Features Overview

#### Feature 1: Portfolio Management
**Description**: User can create, view, update, delete portfolios

**Priority**: Must Have (MVP)

**User Stories**:
- As Portfolio Manager, I want to create portfolios, so I can organize my investments
- As Portfolio Manager, I want to view all portfolios, so I can get an overview
- As Portfolio Manager, I want to edit portfolio details, so I can keep data current

**Business Rules**:
- Portfolio name must be unique per user
- Currency cannot be changed after creation
- Max 100 portfolios per user

**User Flow**:
1. User clicks "New Portfolio"
2. User enters name, currency, description
3. System validates input
4. System creates portfolio
5. User sees success message
6. User is redirected to portfolio detail page

**Edge Cases**:
- Duplicate portfolio name → Show error "Name already exists"
- Invalid currency → Show error "Please select valid currency"
- User has 100 portfolios → Disable "New Portfolio" button

**Acceptance Criteria**:
- [ ] User can create portfolio with valid data
- [ ] System validates all required fields
- [ ] Duplicate names are prevented
- [ ] Success message is shown
- [ ] Portfolio appears in list immediately

#### Feature 2: Position Management
[Similar structure...]

### 4.2 Integration Requirements

#### Integration 1: Market Data Provider
- **System**: Bloomberg API
- **Direction**: Inbound (Pull)
- **Frequency**: Real-time
- **Data**: Instrument prices, market data
- **Format**: REST API
- **Authentication**: API Key
- **Fallback**: Cache last known prices for 15 minutes

## 5. Non-Functional Requirements

### 5.1 Performance
| Metric | Target | Measured |
|--------|--------|----------|
| API Response Time | < 200ms | P95 |
| Page Load Time | < 2s | Average |
| Concurrent Users | 1,000 | Peak |
| Database Queries | < 100ms | P95 |

### 5.2 Scalability
- Horizontal scaling via Kubernetes
- Auto-scaling based on CPU (>70%) and Memory (>80%)
- Support up to 10,000 concurrent users
- Database read replicas for reporting

### 5.3 Security
- **Authentication**: OAuth 2.0 + JWT
- **Authorization**: Role-Based Access Control (RBAC)
- **Encryption**: 
  - At Rest: AES-256
  - In Transit: TLS 1.3
- **Compliance**: GDPR compliant
- **Audit Logging**: All data changes logged
- **Penetration Testing**: Quarterly

### 5.4 Availability
- **Uptime**: 99.9% (43 min downtime/month)
- **RTO**: 1 hour (Recovery Time Objective)
- **RPO**: 15 minutes (Recovery Point Objective)
- **Backup**: Daily full, hourly incremental
- **Disaster Recovery**: Multi-region setup

## 6. Technical Constraints

### 6.1 Technology Stack
- **Backend**: Kotlin + Spring Boot 3.2
- **Database**: PostgreSQL 15
- **Cache**: Redis
- **Message Queue**: RabbitMQ
- **Frontend**: React + TypeScript
- **Cloud**: AWS (EKS, RDS, ElastiCache)

### 6.2 Architecture Style
- Hexagonal Architecture (Ports & Adapters)
- Microservices (if needed)
- Event-Driven for async operations

## 7. Timeline & Milestones

| Milestone | Date | Deliverables |
|-----------|------|--------------|
| Architecture Review | Week 2 | Architecture Doc, ADRs |
| MVP Development Start | Week 3 | Sprint 1 kickoff |
| Alpha Release | Week 8 | Core features done |
| Beta Release | Week 12 | All features done |
| Go-Live | Week 16 | Production release |

## 8. Risks & Assumptions

### Risks
| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| API Integration delayed | High | Medium | Mock API for development |
| Team member leaves | Medium | Low | Knowledge sharing sessions |

### Assumptions
- Bloomberg API is available and documented
- Team has Kotlin experience
- AWS infrastructure can be provisioned

## 9. Open Questions
- [ ] How to handle multi-currency portfolios?
- [ ] What happens to positions when instrument is delisted?
- [ ] Should we support fractional shares?

## Appendix
- Wireframes
- API Contracts
- Data Model Diagrams
```

---

## 🏗️ Schritt 3: Architecture Document (1 Woche)

### **Basierend auf Requirements, erstelle:**

**1. System Context Diagram (C4 Model)**
```
┌─────────────┐
│   User      │
└──────┬──────┘
       │
       ↓
┌─────────────────────────────┐
│   Portfolio Management      │
│   System                    │
└──────┬──────────────────────┘
       │
       ├─→ Bloomberg API
       ├─→ Email Service
       └─→ Database
```

**2. Architecture Decisions (ADRs)**
- ADR-0001: Use Kotlin
- ADR-0002: Hexagonal Architecture
- ADR-0003: PostgreSQL for persistence
- ADR-0004: Event-driven for notifications

**3. Component Design**
```
domain/
  ├── model/        # Aggregates, Entities, VOs
  ├── service/      # Domain Services
  └── port/         # Interfaces

adapter/
  ├── inbound/      # REST Controllers
  └── outbound/     # Database, APIs
```

---

## 📋 Schritt 4: Stories erstellen (2-3 Tage)

### **Von Feature zu Epic zu Story:**

```
Feature: Portfolio Management
  ↓
Epic: Create & Manage Portfolios
  ↓
Stories:
  ├── Story 1: Create Portfolio
  ├── Story 2: View Portfolio List
  ├── Story 3: View Portfolio Details
  ├── Story 4: Edit Portfolio
  └── Story 5: Delete Portfolio
```

### **Story Template (Jira):**

```markdown
**Title**: Als Portfolio Manager möchte ich Portfolio erstellen

**Description**:
Als Portfolio Manager
möchte ich ein neues Portfolio erstellen
damit ich meine Investments organisieren kann

**Context**:
Aktuell werden Portfolios in Excel verwaltet, was fehleranfällig ist.
Das neue System soll eine strukturierte Portfolio-Verwaltung ermöglichen.

**Acceptance Criteria**:
- [ ] Gegeben ich bin eingeloggt
      Wenn ich auf "Neues Portfolio" klicke
      Dann sehe ich ein Formular mit: Name, Currency, Description

- [ ] Gegeben ich fülle alle Pflichtfelder aus
      Wenn ich auf "Speichern" klicke
      Dann wird das Portfolio erstellt
      Und ich sehe eine Success Message
      Und ich werde zur Portfolio-Detailseite weitergeleitet

- [ ] Gegeben der Portfolio-Name existiert bereits
      Wenn ich versuche zu speichern
      Dann sehe ich Error "Name bereits vergeben"
      Und das Portfolio wird nicht erstellt

**Technical Notes**:
- POST /api/portfolios
- Validierung: Name (2-100 chars), Currency (ISO 4217)
- Response: 201 Created mit Portfolio-Objekt

**Test Strategy**:
- Unit Tests für Portfolio Domain Entity
- Integration Test für POST endpoint
- E2E Test für complete user flow

**Dependencies**:
- Depends on: PROJ-001 (Database Schema)
- Blocks: PROJ-015 (Add Position to Portfolio)

**Story Points**: 5

**Labels**: backend, domain, mvp
```

---

## 🗂️ Schritt 5: Sprint Planning (1 Tag)

### **Epics & Stories priorisieren:**

**Sprint 1 (MVP - Core Features):**
```
Epic: Portfolio Management
  ├── Create Portfolio (5 pts)
  ├── View Portfolio List (3 pts)
  ├── View Portfolio Details (3 pts)
  └── Delete Portfolio (2 pts)
Total: 13 pts

Epic: Persistence Layer
  ├── JPA Entities (8 pts)
  ├── Repositories (5 pts)
  └── Mappers (3 pts)
Total: 16 pts
```

**Sprint 2:**
```
Epic: Position Management
  ├── Add Position (8 pts)
  ├── View Positions (5 pts)
  └── Calculate Performance (8 pts)
Total: 21 pts
```

---

## 🎯 Story Sizing Guide

| Points | Complexity | Time | Example |
|--------|-----------|------|---------|
| 1 | Trivial | 2-4h | Fix typo, update config |
| 2 | Simple | 4-8h | Add field to entity |
| 3 | Easy | 1 day | CRUD endpoint |
| 5 | Medium | 2-3 days | Feature with validation |
| 8 | Complex | 3-5 days | Integration with external API |
| 13 | Very Complex | 1 week | Complete feature with multiple components |
| 21 | Epic | 2 weeks | Should be broken down |

---

## 📊 Roadmap Visualization

```
Q1 2025
├── Sprint 1-2: MVP (Portfolio Management)
├── Sprint 3-4: Position Management
└── Sprint 5-6: Reporting

Q2 2025
├── Sprint 7-8: Advanced Analytics
├── Sprint 9-10: Mobile App
└── Sprint 11-12: Performance Optimization

Q3 2025
├── Sprint 13-14: Multi-Currency Support
└── Sprint 15-16: Advanced Integrations
```

---

## ✅ Quality Gates

Nach jedem Schritt validieren:

**Nach Requirements:**
- [ ] Stakeholder haben reviewed & approved
- [ ] Alle offenen Fragen beantwortet
- [ ] Prioritäten sind klar

**Nach Architecture:**
- [ ] Tech Lead hat approved
- [ ] ADRs sind dokumentiert
- [ ] Security Review durchgeführt

**Nach Stories:**
- [ ] Product Owner hat approved
- [ ] Acceptance Criteria sind testbar
- [ ] Dependencies sind identifiziert

**Nach Sprint Planning:**
- [ ] Team hat Kapazität
- [ ] Stories sind gesized
- [ ] Sprint Goal ist definiert

---

## 💡 Pro-Tipps

### **Requirements Phase:**
- ✅ Visualisiere (Wireframes, Diagramme)
- ✅ Prototypen zeigen (Mockups)
- ✅ Kontinuierlich validieren
- ✅ "Show, don't tell" - Beispiele nutzen

### **Architecture Phase:**
- ✅ ADRs für jede wichtige Decision
- ✅ Diagramme > lange Texte
- ✅ Trade-offs dokumentieren
- ✅ Tech Spike für Unsicherheiten

### **Story Phase:**
- ✅ INVEST Kriterien (Independent, Negotiable, Valuable, Estimable, Small, Testable)
- ✅ Acceptance Criteria = Tests
- ✅ Definition of Done definieren
- ✅ Stories vertikal schneiden (nicht horizontal)

---

## 🚀 Quick Start Checklist

Für ein **neues Feature**, durchlaufe:

- [ ] Discovery Interview (2-4h)
- [ ] Requirements schreiben (1-2 Tage)
- [ ] Architecture anpassen (wenn nötig)
- [ ] Stories erstellen (2-4h)
- [ ] Stories sizen (Team Estimation)
- [ ] Sprint Planning (Stories in Sprint)
- [ ] Entwicklung starten

**Zeitaufwand gesamt**: ~1 Woche von Idee bis erste Story in Development! 🎯