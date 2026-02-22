# AfrikaBurn Companion Development Guidelines


## Contents

- [Repository Structure](#repository-structure)
  - [Mobile Application (`/mobile/`)](#mobile-application-mobile)
  - [Backend Application (`/backend/`)](#backend-application-backend)
  - [Specifications (`/specs/`)](#specifications-specs)
- [Active Technologies](#active-technologies)
- [Key Development Principles](#key-development-principles)
- [Common Commands](#common-commands)
  - [Mobile Development](#mobile-development)
  - [Backend Development](#backend-development)
- [Code Style](#code-style)
- [File Path Conventions](#file-path-conventions)
- [Codebase Quick Reference (Pre-Read Context)](#codebase-quick-reference-pre-read-context)
  - [Key Entry Points](#key-entry-points)
  - [Critical Patterns](#critical-patterns)
  - [Data Flow](#data-flow)
  - [Version Info](#version-info)

Last updated: 2026-02-19

## Repository Structure

### Mobile Application (`/mobile/`)
**Tech Stack**: Kotlin 2.2.20 with Compose Multiplatform 1.9.0, Koin 4.1.1, SQLDelight 2.0.2, Material Design 3
```
mobile/
├── composeApp/                      # Compose Multiplatform shared code
│   ├── src/
│   │   ├── commonMain/kotlin/       # Shared business logic and UI
│   │   │   ├── io/asterixorobelix/afrikaburn/
│   │   │   │   ├── data/            # Data layer (repositories, datasources)
│   │   │   │   ├── domain/          # Business logic (repositories, models)
│   │   │   │   ├── di/              # Koin dependency injection
│   │   │   │   ├── navigation/      # Navigation components
│   │   │   │   ├── presentation/    # ViewModels and UI state
│   │   │   │   ├── ui/              # Composable screens and components
│   │   │   │   ├── models/          # Data models
│   │   │   │   └── platform/        # Platform abstractions
│   │   ├── androidMain/kotlin/      # Android-specific implementations
│   │   │   └── io/asterixorobelix/afrikaburn/platform/
│   │   ├── iosMain/kotlin/          # iOS-specific implementations
│   │   │   └── io/asterixorobelix/afrikaburn/platform/
│   │   └── commonTest/kotlin/       # Shared tests
│   └── composeResources/            # Shared resources (images, strings, JSON data)
├── iosApp/                          # iOS native wrapper
└── detekt.yml                       # Code quality configuration
```

### Backend Application (`/backend/`)
**Tech Stack**: Kotlin with Ktor 3.1.3, Exposed 0.61.0, PostgreSQL
```
backend/
├── src/
│   ├── main/kotlin/
│   │   └── io/asterixorobelix/afrikaburn/
│   │       ├── Application.kt       # Main application entry point
│   │       ├── domain/              # Domain models and responses
│   │       └── plugins/             # Ktor plugins (HTTP, DB, Security, etc.)
│   └── main/resources/
│       └── application.conf         # Application configuration
├── build.gradle.kts                 # Backend dependencies
└── detekt.yml                       # Code quality configuration
```

### Specifications (`/specs/`)
Contains feature specifications and implementation plans:
```
specs/001-a-comprehensive-mobile/
├── spec.md                          # Feature requirements
├── plan.md                          # Technical implementation plan
├── data-model.md                    # Entity definitions
├── tasks.md                         # Implementation task list
├── quickstart.md                    # Validation scenarios
├── research.md                      # Technical research findings
└── contracts/api-spec.yaml          # OpenAPI specification
```

## Active Technologies
- **Mobile**: Kotlin 2.2.20 with Compose Multiplatform 1.9.0, Koin 4.1.1, SQLDelight 2.0.2, Ktor Client, Material Design 3
- **Backend**: Kotlin with Ktor 3.1.3, Exposed 0.61.0, PostgreSQL/H2
- **Architecture**: MVVM + Clean Architecture, offline-first design
- **Testing**: Kotlin Test + MockK in `commonTest` (no backend tests yet)

## Key Development Principles
- **Offline-First**: All core functionality must work without network connectivity
- **Material Design 3**: Consistent UI using MD3 tokens, no hardcoded values
- **Test-Driven Development**: Prefer tests for business logic and ViewModels
- **Cross-Platform**: Maximize code sharing between iOS and Android

## Common Commands

### Mobile Development
```bash
# Run mobile tests with coverage
./mobile/gradlew -p mobile test jacocoTestReport

# Verify 80% coverage requirement
./mobile/gradlew -p mobile jacocoTestCoverageVerification

# Run Android app
./mobile/gradlew -p mobile :composeApp:installDebug

# Run iOS app (requires Xcode)
cd mobile/iosApp && xcodebuild

# Detekt code analysis
./mobile/gradlew -p mobile detekt
```

### Backend Development
```bash
# Run backend locally
./backend/gradlew -p backend run

# Run backend tests with coverage
./backend/gradlew -p backend test jacocoTestReport

# Verify 80% coverage requirement
./backend/gradlew -p backend jacocoTestCoverageVerification

# Detekt code analysis
./backend/gradlew -p backend detekt
```

## Code Style
- **Kotlin**: Follow official Kotlin coding conventions
- **Compose**: Use Material Design 3 tokens exclusively
- **Architecture**: Repository pattern, Clean Architecture layers
- **Testing**: Prefer TDD; keep `commonTest` coverage growing
- **Documentation**: Clear inline comments for complex business logic

## File Path Conventions
- **Mobile shared code**: `/mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/`
- **Android-specific**: `/mobile/composeApp/src/androidMain/kotlin/io/asterixorobelix/afrikaburn/`
- **iOS-specific**: `/mobile/composeApp/src/iosMain/kotlin/io/asterixorobelix/afrikaburn/`
- **Mobile tests**: `/mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/`
- **Backend code**: `/backend/src/main/kotlin/io/asterixorobelix/afrikaburn/`
- **Backend tests**: `/backend/src/test/kotlin/io/asterixorobelix/afrikaburn/`

<!-- MANUAL ADDITIONS START -->

## Codebase Quick Reference (Pre-Read Context)

### Key Entry Points
- **Mobile Root:** `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt`
- **Backend Root:** `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/Application.kt`

### Critical Patterns
- **Geofence Unlock:** `UnlockConditionManager` controls tab visibility based on location
- **Offline Maps:** PMTiles + GeoJSON in `composeResources/files/`
- **Theme:** Single `AppTheme` at root only, never wrap individual screens

### Data Flow
```
JSON Files (composeResources/files/WTF*.json)
  → JsonResourceDataSource → ProjectsRepository → ProjectsViewModel → UI
```

### Version Info
- Kotlin 2.2.20, Compose 1.9.0, Ktor 3.1.3, Koin 4.1.1, SQLDelight 2.0.2

<!-- MANUAL ADDITIONS END -->
