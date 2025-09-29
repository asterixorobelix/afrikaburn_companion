# AfrikaBurn Companion Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-09-29

## Repository Structure

### Mobile Application (`/mobile/`)
**Tech Stack**: Kotlin 2.1.21+ with Compose Multiplatform 1.8.1+, Koin DI, SQLDelight, Material Design 3
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
**Tech Stack**: Kotlin with Ktor 3.1.3+, Supabase PostgreSQL
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
- **Mobile**: Kotlin 2.1.21+ with Compose Multiplatform 1.8.1+, Koin DI, SQLDelight, Ktor Client, Material Design 3
- **Backend**: Kotlin with Ktor 3.1.3+, Supabase PostgreSQL hosting
- **Architecture**: MVVM + Clean Architecture, offline-first design
- **Testing**: Kotlin Test framework, 80% backend coverage requirement

## Key Development Principles
- **Offline-First**: All core functionality must work without network connectivity
- **Material Design 3**: Consistent UI using MD3 tokens, no hardcoded values
- **Test-Driven Development**: Write failing tests before implementation
- **Cross-Platform**: Maximize code sharing between iOS and Android
- **Portfolio Quality**: Professional-grade architecture demonstrating best practices

## Common Commands

### Mobile Development
```bash
# Run mobile tests
./mobile/gradlew -p mobile test

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

# Run backend tests
./backend/gradlew -p backend test

# Detekt code analysis
./backend/gradlew -p backend detekt
```

## Code Style
- **Kotlin**: Follow official Kotlin coding conventions
- **Compose**: Use Material Design 3 tokens exclusively
- **Architecture**: Repository pattern, Clean Architecture layers
- **Testing**: TDD approach with comprehensive test coverage
- **Documentation**: Clear inline comments for complex business logic

## File Path Conventions
- **Mobile shared code**: `/mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/`
- **Android-specific**: `/mobile/composeApp/src/androidMain/kotlin/io/asterixorobelix/afrikaburn/`
- **iOS-specific**: `/mobile/composeApp/src/iosMain/kotlin/io/asterixorobelix/afrikaburn/`
- **Mobile tests**: `/mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/`
- **Backend code**: `/backend/src/main/kotlin/io/asterixorobelix/afrikaburn/`
- **Backend tests**: `/backend/src/test/kotlin/io/asterixorobelix/afrikaburn/`

## Recent Changes
- 001-a-comprehensive-mobile: Comprehensive mobile app specification with offline-first architecture
- Added community features coverage (location-based messaging, gift sharing)
- Enhanced performance optimization targets with measurable metrics
- Updated task dependencies for community feature implementation

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->