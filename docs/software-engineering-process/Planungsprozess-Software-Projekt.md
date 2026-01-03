# Planungsprozess für Software Projekte
Unter der Verwendung einer Agentic AI Software (responsible-vibe-engineering-mcp) und einer LLM.

> 📖 **Für detaillierte Sprint-Planung und Day-by-Day Breakdown siehe:**  
> [Software Engineering Process Guide](ai-generated-docs/software-engineering-process.md) - Vollständiger Agile/Scrum Prozess mit Timings

## 📊 Output: Dokumentenstruktur
Aus dem Fragenkatalog erstellt nachfolgende Dokumente.
Erzwinge bei der LLM, dass sie immer diese Dokumente referenziert und auch erweitert.

### **Requirements Document**
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
        - Liste 
        - Detaillierte Beschreibung
   - User Flows
   - Business Rules
5. Non-Functional Requirements
   - Performance
   - Security
   - Scalability
6. Technical Constraints
7. Acceptance Criteria
8. Abhängigkeiten klären
9. Timeline & Budget
10. Risks & Assumptions
11. Appendix
```

###  **Architecture Document**
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

### **Feature Implementation Document**
```text
1. Die Feature Spezifikationen bleiben als Reference bestehen und werden nicht mehr verändert.
2. Erweitere die Feature Spezifikationen mit den technischen Spezifikationen aus dem Architektur Dokkument.
3. Challenge die Feature und Architektur Dokumentation mit Prototypen
4. An welchen Stellen werden Metriken erfasst und Logs geschrieben
5. An welchen Stellen soll ein Alerting erstellt werden
6. Führe Erkenntnisse aus der Feature Dokumentation und der Prototypen zurück in die Architektur
7. Repetiere die Schritte 1 -3 falls notwendig
```

### **User Stories für Planning**
User Stories sollen generiert werden aus den
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

### **Implementierungsphase**
Anhand der Stories wird das erste MVP Produkt erstellt.
Die Phase der Implementierung soll möglichst kurz gehalten werden.
Unit und Integrationtests werden pro User Story abgebildet und gehören in die Definition of Done

### **Deployment**

### **System Testing**
```
- Deploy to Test Environment
  - Execute test scenarios
  - Exploratory testing
  - Performance testing
```

### **UAT (User Acceptance Testing)**
```
- Business users test
- Verify acceptance criteria
- Log bugs in Jira
- Regression testing
```
