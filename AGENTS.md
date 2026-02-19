# AfrikaBurn Companion Development Guidelines

## Repository Structure

### Mobile Application (`/mobile/`)
**Tech Stack**: Kotlin 2.1.21+ with Compose Multiplatform 1.8.1+, Koin DI, SQLDelight, Material Design 3
```
mobile/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/kotlin/io/asterixorobelix/afrikaburn/
│   │   │   ├── data/            # Data layer (repositories, datasources)
│   │   │   ├── domain/          # Business logic (repositories, models)
│   │   │   ├── di/              # Koin dependency injection
│   │   │   ├── navigation/      # Navigation components
│   │   │   ├── presentation/    # ViewModels and UI state
│   │   │   ├── ui/              # Composable screens and components
│   │   │   ├── models/          # Data models
│   │   │   └── platform/        # Platform abstractions
│   │   ├── androidMain/kotlin/  # Android-specific implementations
│   │   ├── iosMain/kotlin/      # iOS-specific implementations
│   │   └── commonTest/kotlin/   # Shared tests
│   └── composeResources/        # Shared resources (images, strings, JSON data)
├── iosApp/                      # iOS native wrapper
└── detekt.yml                   # Code quality configuration
```

### Backend Application (`/backend/`)
**Tech Stack**: Kotlin with Ktor 3.1.3+, Supabase PostgreSQL
```
backend/
├── src/main/kotlin/io/asterixorobelix/afrikaburn/
│   ├── Application.kt       # Main entry point
│   ├── domain/              # Domain models and responses
│   └── plugins/             # Ktor plugins (HTTP, DB, Security, etc.)
└── build.gradle.kts
```

### Specifications (`/specs/`)
Contains feature specifications and implementation plans.

## Key Development Principles
- **Offline-First**: All core functionality must work without network connectivity
- **Material Design 3**: Consistent UI using MD3 tokens, no hardcoded values
- **Test-Driven Development**: 80% minimum coverage, measure with Jacoco
- **Cross-Platform**: Maximize code sharing between iOS and Android

## Common Commands

```bash
# Mobile tests with coverage
./mobile/gradlew -p mobile test jacocoTestReport

# Verify 80% coverage
./mobile/gradlew -p mobile jacocoTestCoverageVerification

# Run Android app
./mobile/gradlew -p mobile :composeApp:installDebug

# Detekt code analysis
./mobile/gradlew -p mobile detekt

# Backend
./backend/gradlew -p backend run
./backend/gradlew -p backend test jacocoTestReport
```

## Key Entry Points
- **Mobile Root:** `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt`
- **Backend Root:** `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/Application.kt`

## Critical Patterns
- **Geofence Unlock:** `UnlockConditionManager` controls tab visibility based on location
- **Offline Maps:** PMTiles + GeoJSON in `composeResources/files/`
- **Theme:** Single `AppTheme` at root only, never wrap individual screens

## Data Flow
```
JSON Files (composeResources/files/WTF*.json)
  → JsonResourceDataSource → ProjectsRepository → ProjectsViewModel → UI
```

## Version Info
- Kotlin 2.2.20, Compose 1.9.0, Ktor 3.1.3, Koin 4.1.1, SQLDelight 2.0.2

## Conventions
- Conventional commits: feat:, fix:, refactor:, chore:, docs:, test:
- Architecture: MVVM + Clean Architecture, offline-first design
