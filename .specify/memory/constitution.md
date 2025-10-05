<!--
Sync Impact Report:
Version change: 1.3.0 → 1.3.1
Modified principles: None
Added sections: Remote Observability Requirements under Development Standards
Removed sections: None
Templates requiring updates:
  ✅ plan-template.md version reference updated (1.3.0 → 1.3.1)
  ✅ spec-template.md alignment maintained
  ✅ tasks-template.md alignment maintained
Follow-up TODOs: None - all content requirements met
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

### VI. Portfolio-Quality Development (NON-NEGOTIABLE)
This codebase MUST serve as a mobile developer portfolio project demonstrating industry best practices and modern architecture patterns. All code, documentation, and architectural decisions MUST reflect professional mobile development standards suitable for career advancement. Single developer velocity considerations MUST be balanced with best practice adherence - choose patterns that enable rapid development while maintaining code quality and demonstrable expertise.

**Rationale**: The project serves dual purposes as both a functional app and a career portfolio piece. Professional-grade architecture and best practices are essential for demonstrating competency to potential employers while enabling efficient solo development.

### VII. Event Information Secrecy & Data Management (NON-NEGOTIABLE)
Event-specific information MUST remain hidden until the event begins or the user enters the Tankwa Karoo location. Sensitive information includes: theme camps, mutant vehicles, performances, art installations, events, and mobile art. Location-based or temporal gates MUST be implemented to control access. Non-sensitive, year-to-year consistent information (driving directions, general About content, safety guidelines) may be displayed at any time.

Previous year's event information is NOT sensitive and may be used for development, testing, and demonstration purposes. Current year information MUST be kept secret until appropriate disclosure conditions are met. The architecture MUST support graceful handling of incomplete, messy, or last-minute data from the AfrikaBurn organization, including partial data sets, format inconsistencies, and late-arriving updates.

**Rationale**: AfrikaBurn's annual secrecy tradition preserves the mystery and discovery aspect essential to the event experience. Early disclosure would undermine community anticipation and the principle of immediacy. The organization's volunteer nature and complex logistics often result in incomplete or last-minute data that the app must handle gracefully without compromising user experience.

## Development Standards

### Code Quality Requirements
- Detekt static analysis MUST pass before commits
- Dependency injection using Koin framework throughout
- Repository pattern for all data access
- Clean Architecture with MVVM presentation layer
- Domain-Driven Design principles in backend services

### Portfolio & Solo Developer Standards
- Modern mobile architecture patterns (MVVM, Repository, Clean Architecture) prominently demonstrated
- Comprehensive documentation for portfolio presentation and future maintenance
- Git commit history MUST demonstrate professional development practices and clear progression
- Code organization MUST showcase understanding of separation of concerns and modular design
- Feature branches and descriptive commit messages for demonstrable development workflow
- Automated CI/CD pipeline showcasing DevOps understanding
- Performance optimization and memory management best practices clearly implemented
- Solo developer productivity patterns: prefer composition over complex inheritance, favor proven libraries over custom solutions, prioritize readable code over clever optimizations

### Technology Stack Compliance
- Kotlin 2.1.21+ for all components
- Ktor 3.1.3+ for backend services
- JWT authentication for secure features
- SQLDelight for local data persistence
- Multi-provider AI integration support

### Data Management Architecture
- Year-based data versioning with clear separation between current/previous year datasets
- Robust data validation with graceful degradation for incomplete information
- Flexible schema support for inconsistent data formats from external sources
- Fallback mechanisms when current year data is unavailable or incomplete
- Previous year data integration for development and testing environments
- Temporal and location-based access controls for sensitive information disclosure

### Security Standards
- Input sanitization on all user inputs
- Rate limiting on all API endpoints
- CORS configuration for web interfaces
- Environment variables for all secrets
- No hardcoded credentials or API keys

### Remote Observability Requirements
- Comprehensive remote logging for desert environment debugging where direct device access is impossible
- Structured logging with contextual information (user actions, app state, network conditions)
- Crash reporting and error tracking with sufficient detail for remote diagnosis
- Performance monitoring for memory usage, battery consumption, and app responsiveness
- Privacy-conscious logging: no sensitive user data or location information beyond general area
- Offline log storage with intelligent sync when connectivity is available
- Debug-level logging in development builds, essential-only logging in production builds

## Quality Gates

### Pre-Commit Requirements
1. All tests pass (unit, integration, UI where applicable)
2. Detekt analysis passes with zero violations
3. Constitution compliance verified
4. Code coverage thresholds met (80% backend minimum)
5. String externalization complete (no hardcoded text)
6. Portfolio quality assessment: architecture patterns clearly demonstrated
7. Documentation updated for portfolio presentation where applicable
8. Remote observability verification: logging and error tracking implemented for new features

### Review Process (Solo Developer Self-Review)
- All PRs require constitution compliance check
- Material Design 3 token usage verified
- Offline functionality tested for new features
- Community principle alignment assessed
- Cross-platform compatibility validated
- Portfolio value assessment: does this change demonstrate professional mobile development skills?
- Solo developer efficiency check: is this the simplest sustainable solution that meets requirements?
- Remote observability assessment: can issues with this feature be diagnosed remotely in the desert?

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

**Version**: 1.3.1 | **Ratified**: 2025-09-29 | **Last Amended**: 2025-09-29