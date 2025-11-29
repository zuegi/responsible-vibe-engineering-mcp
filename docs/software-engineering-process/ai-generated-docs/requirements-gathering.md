# Requirements Gathering - Vollständiger Fragenkatalog

## 🎯 Phase 1: Business Context & Vision

### 1.1 Problem & Motivation
- **Was ist das Problem**, das gelöst werden soll?
- **Wer** hat dieses Problem? (Zielgruppe, Personas)
- **Wie** wird das Problem aktuell gelöst? (Workarounds, manuelle Prozesse)
- **Warum** muss es jetzt gelöst werden? (Business Driver, Urgency)
- **Was passiert**, wenn wir nichts tun? (Consequences)

### 1.2 Vision & Ziele
- **Was** soll das Produkt erreichen? (Vision Statement)
- **Welche messbaren Ziele** gibt es? (KPIs, Success Metrics)
    - Performance-Ziele (z.B. "Response Time < 200ms")
    - Business-Ziele (z.B. "10.000 Users in 6 Monaten")
    - User-Ziele (z.B. "90% Task Success Rate")
- **Wann** soll das Produkt fertig sein? (Timeline, Milestones)
- **Was ist der ROI?** (Return on Investment)

### 1.3 Scope & Prioritäten
- **Was ist IN SCOPE** für V1.0? (Must-Haves)
- **Was ist OUT OF SCOPE?** (Nice-to-Haves, Future)
- **Was sind die Top 3 Prioritäten?**
- **Was sind die Deal-Breaker?** (Absolut notwendige Features)

---

## 👥 Phase 2: Stakeholders & Users

### 2.1 Stakeholder Identification
- **Wer sind alle Stakeholder?**
    - Product Owner
    - End Users
    - Business Sponsors
    - IT/Operations
    - Legal/Compliance
    - Support Team
- **Wer sind die Decision Makers?**
- **Wer hat Veto-Recht?**

### 2.2 User Personas
- **Wer sind die primären User?**
    - Name, Rolle, Alter, Tech-Savviness
    - Ziele, Motivationen
    - Pain Points
    - Typischer Arbeitstag
- **Wie viele User gibt es?** (Expected Load)
- **Wo arbeiten die User?** (Office, Remote, Mobile)
- **Welche Devices nutzen sie?** (Desktop, Mobile, Tablet)

### 2.3 User Stories (High-Level)
- **Als [Rolle] möchte ich [Aktion], damit [Nutzen]**
- Beispiele:
    - Als Portfolio Manager möchte ich meine Positionen sehen, damit ich informierte Entscheidungen treffen kann
    - Als Analyst möchte ich Reports exportieren, damit ich sie präsentieren kann

---

## ⚙️ Phase 3: Funktionale Anforderungen

### 3.1 Core Features
Für jedes Feature fragen:

**Feature Description:**
- **Was** macht dieses Feature?
- **Warum** brauchen wir es?
- **Wer** nutzt es?
- **Wie oft** wird es genutzt? (Daily, Weekly, Rarely)

**User Flow:**
- **Wie** startet der User die Interaktion?
- **Welche Schritte** durchläuft der User?
- **Was** ist das erwartete Ergebnis?
- **Was passiert bei Fehlern?**

**Business Rules:**
- **Welche Validierungen** sind nötig?
- **Welche Berechnungen** müssen durchgeführt werden?
- **Welche Constraints** gibt es? (z.B. "Max 100 Positionen pro Portfolio")
- **Welche Abhängigkeiten** existieren? (z.B. "User muss eingeloggt sein")

### 3.2 CRUD Operations
Für jede Entity (z.B. Portfolio, Instrument):
- **Create**: Wer darf erstellen? Welche Felder sind required/optional?
- **Read**: Wer darf lesen? Welche Filter/Suchen sind nötig?
- **Update**: Wer darf ändern? Was ist editierbar?
- **Delete**: Wer darf löschen? Soft Delete oder Hard Delete?

### 3.3 Workflows & Prozesse
- **Welche komplexen Workflows** gibt es?
- **Wie sieht der Happy Path** aus?
- **Welche Alternative Paths** existieren?
- **Was sind Edge Cases?**
- **Wie werden Fehler behandelt?**

### 3.4 Integrations
- **Mit welchen Systemen** muss integriert werden?
- **Welche Daten** werden ausgetauscht?
- **In welche Richtung** fließen Daten? (Push, Pull, Both)
- **Wie oft** erfolgt der Datenaustausch? (Real-time, Batch, Event-driven)
- **Welches Format?** (REST, SOAP, Files, Messaging)

### 3.5 Reporting & Analytics
- **Welche Reports** werden benötigt?
- **Welche KPIs** sollen getrackt werden?
- **Wer braucht welche Reports?**
- **Export-Formate?** (PDF, Excel, CSV)
- **Wie oft?** (On-demand, Scheduled)

---

## 🛡️ Phase 4: Non-Funktionale Anforderungen

### 4.1 Performance
- **Wie viele User gleichzeitig?** (Concurrent Users)
- **Wie viele Requests pro Sekunde?** (Throughput)
- **Wie schnell muss die App sein?** (Response Time)
    - API Response: < 200ms?
    - Page Load: < 2s?
    - Report Generation: < 30s?
- **Wie viele Daten?** (Data Volume)
    - Anzahl Records: 1M, 10M, 100M?
    - Datengröße: GB, TB?
- **Peak Load?** (z.B. Monatsende, Quartalsende)

### 4.2 Scalability
- **Wie schnell wächst die Nutzerbasis?** (Growth Rate)
- **Muss horizontal skaliert werden?**
- **Gibt es saisonale Spitzen?**
- **Wie viel Reserve** ist nötig? (z.B. 3x Peak Load)

### 4.3 Availability & Reliability
- **Wie viel Uptime** ist nötig? (99%, 99.9%, 99.99%)
- **Wann darf die App down sein?** (Maintenance Windows)
- **Was ist die maximale Downtime?** (MTTR - Mean Time To Recovery)
- **Wie kritisch ist die App?** (Mission Critical, Business Critical, Nice-to-Have)

### 4.4 Security
- **Welche Daten sind sensibel?** (PII, Financial, Health)
- **Wer darf auf welche Daten zugreifen?** (Authorization)
- **Wie werden User authentifiziert?** (Username/Password, SSO, OAuth, MFA)
- **Wie werden Daten verschlüsselt?** (At Rest, In Transit)
- **Welche Compliance-Anforderungen** gibt es? (GDPR, SOC2, ISO27001)
- **Audit Logging?** (Was muss geloggt werden?)
- **Penetration Testing?** (Erforderlich?)

### 4.5 Data Management
- **Wie lange müssen Daten aufbewahrt werden?** (Retention Policy)
- **Wie oft Backups?** (Daily, Hourly, Real-time)
- **Wie schnell Recovery?** (RTO - Recovery Time Objective)
- **Wie viel Datenverlust akzeptabel?** (RPO - Recovery Point Objective)
- **Gibt es Archivierungsanforderungen?**

### 4.6 Usability
- **Welche Sprachen** werden benötigt? (i18n)
- **Accessibility Requirements?** (WCAG 2.1 AA?)
- **Mobile Responsiveness?** (Must? Nice-to-Have?)
- **Browser Support?** (Chrome, Firefox, Safari, Edge)
- **Offline Funktionalität?**

### 4.7 Maintainability
- **Wie oft werden Updates** deployed? (Daily, Weekly, Monthly)
- **Zero-Downtime Deployments** nötig?
- **Wie wird Monitoring** gemacht?
- **Was soll geloggt werden?**
- **Wie werden Fehler reportet?** (Error Tracking: Sentry, etc.)

---

## 🏗️ Phase 5: Technische Constraints

### 5.1 Technology Stack
- **Gibt es Technology Vorgaben?**
    - Programming Language: Kotlin? Java? Python?
    - Framework: Spring Boot? Ktor?
    - Database: PostgreSQL? MongoDB?
    - Cloud: AWS? Azure? GCP? On-Premise?
- **Warum diese Technologien?** (Team Skills, Legacy, Standards)
- **Was ist verboten?** (Blacklist)

### 5.2 Infrastructure
- **Wo wird deployed?** (Cloud, On-Premise, Hybrid)
- **Containerization?** (Docker, Kubernetes)
- **CI/CD Pipeline?** (GitHub Actions, GitLab CI, Jenkins)
- **Wie sieht die Umgebung aus?**
    - Development
    - Staging
    - Production
    - DR (Disaster Recovery)

### 5.3 Existing Systems
- **Welche Systeme existieren bereits?**
- **Muss das neue System integriert werden?**
- **Welche APIs sind verfügbar?**
- **Welche Legacy Systems** müssen migriert werden?
- **Gibt es technische Schulden?**

### 5.4 Dependencies
- **Von welchen externen Services** hängt das System ab?
    - Payment Providers
    - Email Services
    - SMS Gateways
    - Third-Party APIs
- **Was passiert wenn diese ausfallen?** (Fallback?)

---

## 💰 Phase 6: Budget & Resources

### 6.1 Budget
- **Wie hoch ist das Budget?**
- **Was ist im Budget enthalten?**
    - Development
    - Infrastructure
    - Licenses
    - Third-Party Services
    - Maintenance
- **Gibt es laufende Kosten?** (Hosting, Licenses)

### 6.2 Team
- **Wer ist im Team?**
    - Developers (wie viele?)
    - QA Engineers
    - DevOps
    - UX/UI Designer
    - Product Owner
- **Welche Skills hat das Team?**
- **Welche Skills fehlen?** (Training? Hiring?)
- **Wer ist der Technical Lead?**

### 6.3 Timeline
- **Wann soll gestartet werden?**
- **Wann ist Go-Live?** (Hard Deadline? Soft Deadline?)
- **Gibt es Milestones?**
- **Gibt es externe Dependencies?** (Events, Deadlines)

---

## 📄 Phase 7: Documentation & Communication

### 7.1 Documentation Requirements
- **Welche Dokumentation ist nötig?**
    - User Manual
    - Admin Guide
    - API Documentation
    - Architecture Documentation
    - Runbooks
- **Für wen** ist die Dokumentation? (Technical, Non-Technical)
- **Wo** wird dokumentiert? (Confluence, Notion, GitHub Wiki)

### 7.2 Communication
- **Wie oft sind Meetings?** (Daily Standup, Sprint Planning)
- **Wer nimmt teil?**
- **Welche Reporting-Struktur?** (Weekly Updates, Dashboards)
- **Welcher Communication Channel?** (Slack, Teams, Email)

---

## ✅ Phase 8: Acceptance Criteria & Testing

### 8.1 Definition of Done
- **Wann ist ein Feature "done"?**
    - Code reviewed?
    - Tests geschrieben?
    - Documentation updated?
    - Deployed to staging?
    - User Acceptance Testing passed?

### 8.2 Testing Strategy
- **Welche Tests sind nötig?**
    - Unit Tests (Coverage: 80%?)
    - Integration Tests
    - E2E Tests
    - Performance Tests
    - Security Tests
    - User Acceptance Tests
- **Wer testet?** (Developers, QA, Business Users)
- **Test Environment?** (Separate Test DB?)

### 8.3 Acceptance Criteria
Für jedes Feature:
- **Gegeben** [Kontext]
- **Wenn** [Aktion]
- **Dann** [Erwartetes Ergebnis]

Beispiel:
- **Gegeben** ich bin eingeloggt
- **Wenn** ich auf "Portfolio erstellen" klicke
- **Dann** sehe ich das Formular mit Name, Currency, Owner

---

## 🚀 Phase 9: Deployment & Operations

### 9.1 Deployment
- **Wie wird deployed?** (Blue-Green, Canary, Rolling)
- **Wer darf deployen?**
- **Wie wird Rollback** gemacht?
- **Wie werden Database Migrations** gehandhabt?

### 9.2 Monitoring
- **Was muss überwacht werden?**
    - Application Health
    - Error Rates
    - Performance Metrics
    - Business Metrics
- **Welche Tools?** (Prometheus, Grafana, DataDog)
- **Wer wird alarmiert?** (On-Call Rotation)

### 9.3 Support
- **Wie wird Support** organisiert?
    - Level 1: Helpdesk
    - Level 2: Application Support
    - Level 3: Development Team
- **Wie werden Bugs** getrackt? (Jira, GitHub Issues)
- **Was ist die SLA?** (Response Time, Resolution Time)

---

## 🔄 Phase 10: Change Management

### 10.1 Change Requests
- **Wie werden Change Requests** gehandhabt?
- **Wer entscheidet** über Changes?
- **Wie werden Changes priorisiert?**

### 10.2 Training
- **Brauchen User Training?**
- **Welche Materialien?** (Videos, Workshops, Documentation)
- **Wer führt Training durch?**

### 10.3 Migration
- **Müssen Daten migriert werden?** (Legacy System → New System)
- **Wie viele Daten?**
- **Downtime erlaubt?**
- **Rollback-Plan?**

---

## 📊 Output: Dokumentenstruktur

Aus diesen Fragen erstellt man:

### 1️⃣ **Requirements Document**
```
1. Executive Summary
2. Business Context
   - Problem Statement
   - Vision & Goals
   - Success Metrics
3. Stakeholders & Users
   - Personas
   - User Stories
4. Functional Requirements
   - Features
   - User Flows
   - Business Rules
5. Non-Functional Requirements
   - Performance
   - Security
   - Scalability
6. Technical Constraints
7. Acceptance Criteria
8. Timeline & Budget
9. Risks & Assumptions
10. Appendix
```

### 2️⃣ **Architecture Document**
```
1. Architecture Overview
   - System Context
   - Containers (High-Level)
2. Architecture Decisions (ADRs)
   - Technology Choices
   - Patterns (Hexagonal, etc.)
3. Component Design
   - Domain Model
   - Adapters
   - Ports
4. Data Architecture
   - Database Schema
   - Data Flow
5. Integration Architecture
   - APIs
   - Events
   - Third-Party Services
6. Deployment Architecture
   - Infrastructure
   - CI/CD
7. Security Architecture
8. Performance & Scalability
9. Monitoring & Observability
```

### 3️⃣ **User Stories für Planning**
```
Epic: Portfolio Management
├── Story 1: Als User möchte ich Portfolio erstellen
│   ├── AC1: Form mit Name, Currency
│   ├── AC2: Validierung
│   └── AC3: Success Message
├── Story 2: Als User möchte ich Instrument kaufen
├── Story 3: Als User möchte ich Performance sehen
└── ...

Epic: Reporting
├── Story 1: Als Manager möchte ich Excel Export
└── ...
```

---

## 🎯 Priorisierung Framework: MoSCoW

Nach dem Fragen stellen, kategorisieren:

- **Must Have**: Ohne geht's nicht (MVP)
- **Should Have**: Wichtig, aber nicht kritisch
- **Could Have**: Nice-to-Have
- **Won't Have**: Für später

---

## 💡 Pro-Tipps

### ✅ DO's
- **Offene Fragen stellen** ("Wie..." statt "Ist...")
- **"Warum?" 5x fragen** (5 Whys Technique)
- **Beispiele verlangen** ("Zeig mir wie du das heute machst")
- **Edge Cases besprechen** ("Was wenn...")
- **Prioritäten klären** ("Was ist wichtiger: A oder B?")
- **Stakeholder validieren** ("Habe ich das richtig verstanden?")

### ❌ DON'Ts
- **Nicht Lösungen vorschlagen** zu früh (erst Problem verstehen)
- **Nicht Annahmen machen** (immer nachfragen!)
- **Nicht überspringen** (auch "dumme" Fragen stellen)
- **Nicht nur mit einem Stakeholder** sprechen
- **Nicht alles aufeinmal** fragen (iterativ!)

---

## 🔄 Iterativer Prozess

```
1. Initial Discovery (Phase 1-3)
   → High-Level Requirements Doc
   
2. Deep Dive (Phase 4-6)
   → Detailed Requirements + Architecture Draft
   
3. Validation (Phase 7-8)
   → Refined Requirements + Stories
   
4. Planning (Phase 9-10)
   → Sprint Planning + Roadmap
```

---

## 📋 Quick Start Checklist

Für ein **neues Projekt**, frage mindestens:

- [ ] Was ist das Problem? (Problem Statement)
- [ ] Wer sind die User? (Personas)
- [ ] Was sind die Top 3 Features? (Core Features)
- [ ] Wie viele User? (Scale)
- [ ] Wie schnell muss es sein? (Performance)
- [ ] Welche Systeme existieren? (Integrations)
- [ ] Was ist das Budget? (Resources)
- [ ] Wann ist Go-Live? (Timeline)
- [ ] Wer entscheidet? (Decision Makers)
- [ ] Was ist Security-relevant? (Compliance)

Diese 10 Fragen ergeben **80% der Information** die du brauchst! 🎯