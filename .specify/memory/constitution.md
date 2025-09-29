<!--
Sync Impact Report:
Version change: [CONSTITUTION_VERSION] → 1.0.0
Modified principles: Complete initialization from template
Added sections: All core principles, Development Standards, Quality Gates, Governance
Removed sections: Template placeholders removed
Templates requiring updates:
  ✅ Updated plan-template.md version reference
  ✅ Updated spec-template.md alignment
  ✅ Updated tasks-template.md alignment
Follow-up TODOs: None - all placeholders resolved
-->

# AfrikaBurn Companion App Constitution

## Core Principles

### I. Offline-First Architecture
Every feature MUST function completely offline. The app serves festival participants in the remote Tankwa Karoo desert where connectivity is unreliable or absent. Online features are enhancements, never dependencies. All core functionality including maps, schedules, safety tools, and community features must work without network access.

**Rationale**: AfrikaBurn occurs in an isolated desert environment where network infrastructure is minimal. Participants rely on the app for critical safety and navigation information that cannot be compromised by connectivity issues.

### II. Community-Centric Design
All features MUST align with AfrikaBurn's Ten Principles: radical inclusion, gifting, decommodification, radical self-reliance, radical self-expression, communal effort, civic responsibility, leaving no trace, participation, and immediacy. The app facilitates community connection while preserving the event's non-commercial, participatory spirit.

**Rationale**: AfrikaBurn is built on specific cultural values that differentiate it from commercial festivals. The app must reinforce rather than undermine these principles.

### III. Material Design 3 Consistency (NON-NEGOTIABLE)
All UI components MUST use Material Design 3 tokens exclusively. No hardcoded colors, dimensions, or typography. String resources required for all user-facing text. Spacing MUST use the centralized Dimens object. Each Composable file MUST include exactly one Preview function.

**Rationale**: Consistent design language ensures accessibility, maintainability, and professional user experience across all platforms and screen sizes.

### IV. Test-First Development
TDD is mandatory: Tests written → User approved → Tests fail → Then implement. Red-Green-Refactor cycle strictly enforced. Backend requires 80% minimum test coverage. Critical user flows require UI tests. All business logic requires unit tests.

**Rationale**: Given the app's safety-critical nature and offline requirements, reliability through comprehensive testing is non-negotiable.

### V. Cross-Platform Code Sharing
Compose Multiplatform enables maximum code sharing between Android and iOS. Platform-specific code requires explicit justification. Business logic, UI components, and data models MUST be shared. Only platform integration points (permissions, native APIs) may diverge.

**Rationale**: Resource efficiency and feature parity across platforms ensure all participants have equal access regardless of device choice.

## Development Standards

### Code Quality Requirements
- Detekt static analysis MUST pass before commits
- Dependency injection using Koin framework throughout
- Repository pattern for all data access
- Clean Architecture with MVVM presentation layer
- Domain-Driven Design principles in backend services

### Technology Stack Compliance
- Kotlin 2.1.21+ for all components
- Ktor 3.1.3+ for backend services
- JWT authentication for secure features
- SQLDelight for local data persistence
- Multi-provider AI integration support

### Security Standards
- Input sanitization on all user inputs
- Rate limiting on all API endpoints
- CORS configuration for web interfaces
- Environment variables for all secrets
- No hardcoded credentials or API keys

## Quality Gates

### Pre-Commit Requirements
1. All tests pass (unit, integration, UI where applicable)
2. Detekt analysis passes with zero violations
3. Constitution compliance verified
4. Code coverage thresholds met (80% backend minimum)
5. String externalization complete (no hardcoded text)

### Review Process
- All PRs require constitution compliance check
- Material Design 3 token usage verified
- Offline functionality tested for new features
- Community principle alignment assessed
- Cross-platform compatibility validated

### Release Criteria
- Full offline functionality demonstration
- Community principle compliance audit
- Performance benchmarks met
- Accessibility standards verified
- Desert environment testing completed

## Governance

### Amendment Process
Constitution changes require:
1. Technical impact assessment
2. Community principle alignment review
3. Template consistency update
4. Version increment following semantic versioning
5. Migration plan for affected code

### Versioning Policy
- MAJOR: Principle removals or fundamental architecture changes
- MINOR: New principles added or expanded guidance
- PATCH: Clarifications, wording improvements, non-semantic refinements

### Compliance Review
All development activities MUST verify compliance with this constitution. Project leads conduct quarterly compliance audits. Non-compliance blocks releases until resolved.

**Version**: 1.0.0 | **Ratified**: 2025-09-29 | **Last Amended**: 2025-09-29