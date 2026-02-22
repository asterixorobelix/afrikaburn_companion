
# Implementation Plan: AfrikaBurn Companion Mobile App

**Branch**: `001-a-comprehensive-mobile` | **Date**: 2025-09-29 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-a-comprehensive-mobile/spec.md`


## Contents

- [Execution Flow (/plan command scope)](#execution-flow-plan-command-scope)
- [Summary](#summary)
- [Technical Context](#technical-context)
- [Constitution Check](#constitution-check)
- [Project Structure](#project-structure)
  - [Documentation (this feature)](#documentation-this-feature)
  - [Source Code (repository root)](#source-code-repository-root)
- [Phase 0: Outline & Research](#phase-0-outline-research)
- [Phase 1: Design & Contracts](#phase-1-design-contracts)
- [Phase 2: Task Planning Approach](#phase-2-task-planning-approach)
- [Phase 3+: Future Implementation](#phase-3-future-implementation)
- [Complexity Tracking](#complexity-tracking)
- [Progress Tracking](#progress-tracking)

## Execution Flow (/plan command scope)
```
1. Load feature spec from Input path
   → If not found: ERROR "No feature spec at {path}"
2. Fill Technical Context (scan for NEEDS CLARIFICATION)
   → Detect Project Type from file system structure or context (web=frontend+backend, mobile=app+api)
   → Set Structure Decision based on project type
3. Fill the Constitution Check section based on the content of the constitution document.
4. Evaluate Constitution Check section below
   → If violations exist: Document in Complexity Tracking
   → If no justification possible: ERROR "Simplify approach first"
   → Update Progress Tracking: Initial Constitution Check
5. Execute Phase 0 → research.md
   → If NEEDS CLARIFICATION remain: ERROR "Resolve unknowns"
6. Execute Phase 1 → contracts, data-model.md, quickstart.md, agent-specific template file (e.g., `CLAUDE.md` for Claude Code, `.github/copilot-instructions.md` for GitHub Copilot, `GEMINI.md` for Gemini CLI, `QWEN.md` for Qwen Code or `AGENTS.md` for opencode).
7. Re-evaluate Constitution Check section
   → If new violations: Refactor design, return to Phase 1
   → Update Progress Tracking: Post-Design Constitution Check
8. Plan Phase 2 → Describe task generation approach (DO NOT create tasks.md)
9. STOP - Ready for /tasks command
```

**IMPORTANT**: The /plan command STOPS at step 7. Phases 2-4 are executed by other commands:
- Phase 2: /tasks command creates tasks.md
- Phase 3-4: Implementation execution (manual or via tools)

## Summary
A comprehensive offline-first mobile application for AfrikaBurn participants built with Compose Multiplatform for iOS and Android. The app provides essential survival tools, GPS navigation, interactive maps, event discovery, and community features that work completely offline in the remote Tankwa Karoo desert. Key features include 2GB smart sync, location-based content unlocking, personal schedule building, safety/emergency tools, and MOOP environmental tracking - all following Material Design 3 and MVVM clean architecture patterns.

## Technical Context
**Language/Version**: Kotlin 2.1.21+ with Compose Multiplatform 1.8.1+  
**Primary Dependencies**: Compose Multiplatform, Koin DI, SQLDelight, Ktor Client, Material Design 3  
**Storage**: SQLDelight with local database, 2GB offline content storage, Supabase backend  
**Testing**: Kotlin Test, UI Testing Framework, JUnit for backend, Jacoco coverage reports, 80% minimum coverage requirement  
**Target Platform**: iOS 15+ and Android API 24+, Compose Multiplatform shared UI
**Project Type**: Mobile + API (Compose Multiplatform with Kotlin backend)  
**Performance Goals**: 24+ hour battery life, <3 second map loads, 60fps smooth UI  
**Constraints**: Complete offline functionality, 2GB storage limit, desert environment durability  
**Scale/Scope**: 5000+ participants, 50+ screens, complex offline sync, GPS-heavy usage

## Constitution Check
*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Offline-First Architecture**: Complete offline functionality implemented with SQLDelight local storage and smart sync
- [x] **Community-Centric Design**: Features align with AfrikaBurn's Ten Principles, supports gifting and non-commercial interactions
- [x] **Material Design 3 Consistency**: Compose Multiplatform with MD3 tokens, centralized Dimens, no hardcoded values
- [x] **Test-First Development**: TDD with Kotlin Test, Jacoco coverage verification, 80% minimum coverage for all code, UI tests for critical flows
- [x] **Cross-Platform Code Sharing**: Compose Multiplatform maximizes shared code, platform-specific only for native APIs
- [x] **Portfolio-Quality Development**: Clean Architecture + MVVM, demonstrates modern mobile development patterns
- [x] **Event Information Secrecy**: Location and time-based content unlocking system implemented
- [x] **Remote Observability**: Comprehensive logging for desert debugging with privacy protection

**Status**: PASS - All constitutional requirements addressed in technical approach

## Project Structure

### Documentation (this feature)
```
specs/[###-feature]/
├── plan.md              # This file (/plan command output)
├── research.md          # Phase 0 output (/plan command)
├── data-model.md        # Phase 1 output (/plan command)
├── quickstart.md        # Phase 1 output (/plan command)
├── contracts/           # Phase 1 output (/plan command)
└── tasks.md             # Phase 2 output (/tasks command - NOT created by /plan)
```

### Source Code (repository root)
```
# Compose Multiplatform Mobile App + Kotlin Backend
composeApp/                          # Compose Multiplatform shared code
├── src/
│   ├── commonMain/kotlin/
│   │   ├── ui/                     # Shared UI components (Compose)
│   │   │   ├── screens/            # Screen composables
│   │   │   ├── components/         # Reusable UI components
│   │   │   ├── theme/              # Material Design 3 theme
│   │   │   └── navigation/         # Navigation setup
│   │   ├── domain/                 # Business logic layer
│   │   │   ├── model/              # Domain models
│   │   │   ├── repository/         # Repository interfaces
│   │   │   └── usecase/            # Use cases (business logic)
│   │   ├── data/                   # Data layer
│   │   │   ├── local/              # SQLDelight database
│   │   │   ├── remote/             # Ktor client APIs
│   │   │   ├── repository/         # Repository implementations
│   │   │   └── sync/               # Smart sync engine
│   │   └── di/                     # Koin dependency injection
│   ├── androidMain/kotlin/         # Android-specific code
│   │   └── platform/               # Platform implementations
│   └── iosMain/kotlin/             # iOS-specific code
│       └── platform/               # Platform implementations
├── src/commonTest/kotlin/          # Shared tests
├── src/androidUnitTest/kotlin/     # Android unit tests
└── src/iosTest/kotlin/             # iOS tests

backend/                            # Kotlin backend (Supabase hosted)
├── src/main/kotlin/
│   ├── api/                        # Ktor API endpoints
│   ├── domain/                     # Business logic
│   ├── data/                       # Database models
│   ├── service/                    # Business services
│   └── util/                       # Utilities
└── src/test/kotlin/                # Backend tests

shared/                             # Shared models between app and backend
├── src/commonMain/kotlin/
│   └── model/                      # Data transfer objects
```

**Structure Decision**: Mobile + API structure selected to support Compose Multiplatform with shared UI/business logic and platform-specific implementations, plus Kotlin backend for event data management and smart sync coordination.

## Phase 0: Outline & Research
1. **Extract unknowns from Technical Context** above:
   - For each NEEDS CLARIFICATION → research task
   - For each dependency → best practices task
   - For each integration → patterns task

2. **Generate and dispatch research agents**:
   ```
   For each unknown in Technical Context:
     Task: "Research {unknown} for {feature context}"
   For each technology choice:
     Task: "Find best practices for {tech} in {domain}"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: [what was chosen]
   - Rationale: [why chosen]
   - Alternatives considered: [what else evaluated]

**Output**: research.md with all NEEDS CLARIFICATION resolved

## Phase 1: Design & Contracts
*Prerequisites: research.md complete*

1. **Extract entities from feature spec** → `data-model.md`:
   - Entity name, fields, relationships
   - Validation rules from requirements
   - State transitions if applicable

2. **Generate API contracts** from functional requirements:
   - For each user action → endpoint
   - Use standard REST/GraphQL patterns
   - Output OpenAPI/GraphQL schema to `/contracts/`

3. **Generate contract tests** from contracts:
   - One test file per endpoint
   - Assert request/response schemas
   - Tests must pass with proper implementation
   - Ensure 80% code coverage with Jacoco verification

4. **Extract test scenarios** from user stories:
   - Each story → integration test scenario with 80% coverage target
   - Quickstart test = story validation steps with coverage verification

5. **Update agent file incrementally** (O(1) operation):
   - Run `.specify/scripts/bash/update-agent-context.sh claude`
     **IMPORTANT**: Execute it exactly as specified above. Do not add or remove any arguments.
   - If exists: Add only NEW tech from current plan
   - Preserve manual additions between markers
   - Update recent changes (keep last 3)
   - Keep under 150 lines for token efficiency
   - Output to repository root

**Output**: data-model.md, /contracts/*, passing tests with 80% coverage, quickstart.md, agent-specific file

## Phase 2: Task Planning Approach
*This section describes what the /tasks command will do - DO NOT execute during /plan*

**Task Generation Strategy**:
- Load `.specify/templates/tasks-template.md` as base
- Generate tasks from Phase 1 design docs (contracts, data model, quickstart)
- Each contract → contract test task with 80% coverage [P]
- Each entity → model creation task with unit tests [P] 
- Each user story → integration test task with coverage verification
- Implementation tasks to achieve passing tests and 80% coverage

**Ordering Strategy**:
- TDD order: Write comprehensive tests achieving 80% coverage before implementation 
- Dependency order: Models before services before UI
- Coverage verification: Jacoco reports must show 80% minimum before moving to next task
- Mark [P] for parallel execution (independent files)

**Estimated Output**: 25-30 numbered, ordered tasks in tasks.md

**IMPORTANT**: This phase is executed by the /tasks command, NOT by /plan

## Phase 3+: Future Implementation
*These phases are beyond the scope of the /plan command*

**Phase 3**: Task execution (/tasks command creates tasks.md)  
**Phase 4**: Implementation (execute tasks.md following constitutional principles)  
**Phase 5**: Validation (run tests, execute quickstart.md, performance validation)

## Complexity Tracking
*Fill ONLY if Constitution Check has violations that must be justified*

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |


## Progress Tracking
*This checklist is updated during execution flow*

**Phase Status**:
- [x] Phase 0: Research complete (/plan command)
- [x] Phase 1: Design complete (/plan command)
- [x] Phase 2: Task planning complete (/plan command - describe approach only)
- [ ] Phase 3: Tasks generated (/tasks command)
- [ ] Phase 4: Implementation complete
- [ ] Phase 5: Validation passed

**Gate Status**:
- [x] Initial Constitution Check: PASS
- [x] Post-Design Constitution Check: PASS
- [x] All NEEDS CLARIFICATION resolved
- [x] Complexity deviations documented

---
*Based on Constitution v1.3.1 - See `/memory/constitution.md`*
