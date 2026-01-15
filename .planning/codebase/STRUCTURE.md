# Codebase Structure

**Analysis Date:** 2026-01-15

## Directory Layout

```
afrikaburn_companion/
├── mobile/                     # Compose Multiplatform mobile app
│   ├── composeApp/            # Shared Compose application code
│   │   ├── src/
│   │   │   ├── commonMain/    # Shared Kotlin code
│   │   │   ├── androidMain/   # Android-specific implementations
│   │   │   ├── iosMain/       # iOS-specific implementations
│   │   │   └── commonTest/    # Shared test code
│   │   └── composeResources/  # Shared resources (images, strings, JSON)
│   ├── iosApp/                # iOS native wrapper
│   ├── fastlane/              # Fastlane deployment configuration
│   └── gradle/                # Gradle configuration
├── backend/                    # Ktor backend server
│   └── src/main/kotlin/       # Backend Kotlin source
├── specs/                      # Feature specifications
│   └── 001-a-comprehensive-mobile/
├── .github/                    # GitHub Actions workflows
├── .planning/                  # Planning documents (this directory)
├── CLAUDE.md                   # Claude Code instructions
└── README.md                   # Project documentation
```

## Directory Purposes

**mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/**
- Purpose: Shared application code for iOS and Android
- Contains: All business logic, UI, data access
- Key files: `App.kt` (entry), `Theme.kt` (design system)
- Subdirectories: `data/`, `domain/`, `presentation/`, `ui/`, `di/`, `models/`, `navigation/`, `platform/`

**mobile/composeApp/src/androidMain/kotlin/io/asterixorobelix/afrikaburn/platform/**
- Purpose: Android-specific platform implementations
- Contains: CrashLogger.android.kt, FirebaseConfigChecker, KoinInitializer
- Key files: `CrashLogger.android.kt` (Firebase Crashlytics integration)

**mobile/composeApp/src/iosMain/kotlin/io/asterixorobelix/afrikaburn/platform/**
- Purpose: iOS-specific platform implementations
- Contains: CrashLogger.ios.kt, KoinInitializer
- Key files: `CrashLogger.ios.kt` (NSLog-based logging)

**mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/**
- Purpose: Shared unit and integration tests
- Contains: Model tests, ViewModel tests, Repository tests
- Key files: `ProjectItemTest.kt`, `ProjectsViewModelTest.kt`

**mobile/composeApp/composeResources/**
- Purpose: Shared resources loaded by Compose Multiplatform
- Contains: `values/strings.xml`, `drawable/`, `files/` (JSON data)
- Key files: `files/WTFThemeCamps.json`, etc.

**backend/src/main/kotlin/io/asterixorobelix/afrikaburn/**
- Purpose: Ktor server application code
- Contains: Application entry, plugins, domain models
- Key files: `Application.kt` (main entry)
- Subdirectories: `plugins/`, `domain/`

**backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/**
- Purpose: Ktor server feature modules
- Contains: HTTP, Security, Routing, Databases, Monitoring, StatusPages, Serialization
- Key files: `Routing.kt` (API endpoints), `Security.kt` (JWT auth)

**specs/001-a-comprehensive-mobile/**
- Purpose: Feature specification and planning documents
- Contains: spec.md, plan.md, data-model.md, tasks.md, contracts/
- Key files: `contracts/api-spec.yaml` (OpenAPI definition)

## Key File Locations

**Entry Points:**
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt` - Mobile app root composable
- `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/Application.kt` - Backend server main()

**Configuration:**
- `mobile/gradle/libs.versions.toml` - Dependency version catalog
- `mobile/composeApp/build.gradle.kts` - Mobile build configuration
- `backend/build.gradle.kts` - Backend build configuration
- `backend/src/main/resources/application.conf` - Ktor HOCON configuration
- `mobile/detekt.yml` - Mobile code analysis rules
- `backend/detekt.yml` - Backend code analysis rules

**Core Logic:**
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt` - Data repository
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/datasource/JsonResourceDataSourceImpl.kt` - JSON loading
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/projects/ProjectsViewModel.kt` - Main ViewModel

**Testing:**
- `mobile/composeApp/src/commonTest/kotlin/` - Mobile unit tests
- `backend/src/test/kotlin/` - Backend tests (if present)

**Documentation:**
- `CLAUDE.md` - Developer instructions for Claude Code
- `README.md` - Project README
- `specs/` - Feature specifications

## Naming Conventions

**Files:**
- PascalCase.kt: Kotlin files matching class name (`ProjectItem.kt`, `ProjectsViewModel.kt`)
- camelCase.kt: Module files without primary class (`appModule.kt` pattern, but actually uses `AppModule.kt`)
- *.test.kt: Test files (`ProjectItemTest.kt`)

**Directories:**
- lowercase: All directories (`data/`, `domain/`, `presentation/`)
- Feature-based: UI organized by feature (`ui/projects/`, `ui/about/`, `ui/directions/`)
- Layer-based: Architecture layers as directories (`data/`, `domain/`, `presentation/`)

**Special Patterns:**
- `*Impl.kt`: Interface implementations (`ProjectsRepositoryImpl.kt`)
- `*ViewModel.kt`: ViewModels (`ProjectsViewModel.kt`)
- `*Screen.kt`: Composable screens (`ProjectsScreen.kt`)
- `*.android.kt`, `*.ios.kt`: Platform-specific actual implementations

## Where to Add New Code

**New Feature:**
- Primary code: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/`
- Tests: `mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/`
- Resources: `mobile/composeApp/composeResources/`

**New Screen:**
- Implementation: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/{feature}/`
- ViewModel: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/{feature}/`
- Navigation: Add route to `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/navigation/NavigationDestination.kt`

**New Data Model:**
- Implementation: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/models/`
- Tests: `mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/models/`

**New API Endpoint:**
- Definition: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Routing.kt`
- Response models: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/domain/`

**Utilities:**
- Shared helpers: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/components/`
- Type definitions: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/models/`

**DI Module:**
- Implementation: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/`
- Include in: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/AppModule.kt`

## Special Directories

**mobile/composeApp/composeResources/**
- Purpose: Compose Multiplatform resources (loaded at runtime)
- Source: Manually created/updated
- Committed: Yes

**build/**
- Purpose: Build output artifacts
- Source: Generated by Gradle
- Committed: No (.gitignore)

**.planning/**
- Purpose: Planning and analysis documents
- Source: GSD workflow output
- Committed: Yes

**.github/workflows/**
- Purpose: CI/CD pipeline definitions
- Source: Manually created
- Committed: Yes

**mobile/fastlane/**
- Purpose: App store deployment automation
- Source: Fastlane configuration
- Committed: Yes (except secrets)

---

*Structure analysis: 2026-01-15*
*Update when directory structure changes*
