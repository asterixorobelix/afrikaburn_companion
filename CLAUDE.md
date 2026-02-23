# AfrikaBurn Companion

## Critical Patterns

- **Geofence Unlock:** `UnlockConditionManager` controls tab visibility based on location
- **Offline Maps:** PMTiles + GeoJSON in `composeResources/files/`
- **Theme:** Single `AppTheme` at root only — never wrap individual screens
- **Offline-First:** All core functionality must work without network connectivity

## Data Flow

```
JSON Files (composeResources/files/WTF*.json)
  -> JsonResourceDataSource -> ProjectsRepository -> ProjectsViewModel -> UI
```

## Key Entry Points

- **Mobile Root:** `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt`
- **Backend Root:** `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/Application.kt`

## Tech Stack

- **Mobile**: Kotlin with Compose Multiplatform, Koin, SQLDelight, Material Design 3
- **Backend**: Kotlin with Ktor, Exposed, PostgreSQL
- **Architecture**: MVVM + Clean Architecture, offline-first

## Commands

```bash
# Mobile
./mobile/gradlew -p mobile test jacocoTestReport
./mobile/gradlew -p mobile jacocoTestCoverageVerification  # 80% min
./mobile/gradlew -p mobile :composeApp:installDebug
./mobile/gradlew -p mobile detekt

# Backend
./backend/gradlew -p backend run
./backend/gradlew -p backend test jacocoTestReport
./backend/gradlew -p backend detekt
```

## Conventions

- Conventional commits: feat:, fix:, refactor:, chore:, docs:, test:
- Material Design 3 tokens exclusively — no hardcoded values
- TDD: 80% minimum coverage (Jacoco)
