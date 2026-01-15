# Architecture

**Analysis Date:** 2026-01-15

## Pattern Overview

**Overall:** Two-Tier Distributed Architecture (Mobile + Backend API)

**Key Characteristics:**
- Offline-first mobile design with embedded data
- Clean Architecture with MVVM presentation pattern
- Kotlin Multiplatform for iOS/Android code sharing
- Ktor plugin-based backend configuration

## Layers

### Mobile Application

**Data Layer:**
- Purpose: Data access and caching
- Contains: Repository implementations, data sources
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/`
- Depends on: Kotlinx Serialization, Compose Resources
- Used by: Domain layer (via interfaces)

**Domain Layer:**
- Purpose: Business logic contracts
- Contains: Repository interfaces, domain models
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/`
- Depends on: Core Kotlin types only
- Used by: Presentation layer

**Presentation Layer:**
- Purpose: UI state management
- Contains: ViewModels, UI state classes
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/`
- Depends on: Domain layer, Kotlinx Coroutines
- Used by: UI layer

**UI Layer:**
- Purpose: User interface rendering
- Contains: Composable screens, components
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/`
- Depends on: Presentation layer, Compose Multiplatform
- Used by: App entry point

**DI Layer:**
- Purpose: Dependency injection configuration
- Contains: Koin modules for all layers
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/`
- Depends on: All other layers
- Used by: App initialization

**Platform Layer:**
- Purpose: Platform-specific abstractions
- Contains: Expect/actual declarations (CrashLogger, etc.)
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/platform/`
- Depends on: Kotlin Multiplatform
- Used by: DI layer, UI layer

### Backend Application

**Application Layer:**
- Purpose: Server configuration and startup
- Contains: Main function, module configuration
- Location: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/Application.kt`
- Depends on: All plugins
- Used by: Ktor server

**Plugin Layer:**
- Purpose: Feature configuration modules
- Contains: HTTP, Database, Security, Routing, Monitoring plugins
- Location: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/`
- Depends on: Ktor, Exposed, JWT libraries
- Used by: Application module

**Domain Layer:**
- Purpose: Response models and business types
- Contains: ErrorResponse, HealthResponse
- Location: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/domain/`
- Depends on: Kotlinx Serialization
- Used by: Routing plugin

## Data Flow

**Mobile - Load Projects Flow:**

1. User opens ProjectsScreen
2. ProjectsScreen collects state from ProjectsViewModel
3. ProjectsViewModel calls `projectsRepository.getProjectsByType(type)`
4. ProjectsRepositoryImpl checks in-memory cache
5. If cache miss: calls JsonResourceDataSource.loadProjectsByType()
6. JsonResourceDataSourceImpl loads from embedded JSON resources
7. Repository caches result and returns Result.success(projects)
8. ViewModel updates StateFlow with Success state
9. Screen recomposes with project list

**Backend - HTTP Request Flow:**

1. HTTP request received by Netty server
2. Security plugin validates JWT (if protected route)
3. Serialization plugin handles JSON content type
4. Monitoring plugin logs request details
5. Routing plugin matches endpoint handler
6. Handler executes business logic
7. Response serialized to JSON
8. StatusPages handles any exceptions
9. HTTP response returned to client

**State Management:**
- Mobile: StateFlow in ViewModels for reactive UI
- Backend: Stateless request handling per-request

## Key Abstractions

**Repository (Mobile):**
- Purpose: Abstract data access from business logic
- Examples: `ProjectsRepository` interface, `ProjectsRepositoryImpl` implementation
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/`
- Pattern: Repository pattern with Result<T> return type

**DataSource (Mobile):**
- Purpose: Raw data loading from sources
- Examples: `JsonResourceDataSource` interface, `JsonResourceDataSourceImpl` implementation
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/datasource/`
- Pattern: Data source abstraction for testability

**ViewModel (Mobile):**
- Purpose: Manage UI state and user interactions
- Examples: `ProjectsViewModel`, `ProjectTabViewModel`
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/projects/`
- Pattern: MVVM with StateFlow

**UiState (Mobile):**
- Purpose: Represent all possible screen states
- Examples: `ProjectsUiState` (Loading, Success, Error), `ProjectsScreenUiState`
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/projects/`
- Pattern: Sealed class for exhaustive state handling

**Plugin (Backend):**
- Purpose: Modular server feature configuration
- Examples: `configureHTTP()`, `configureSecurity()`, `configureRouting()`
- Location: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/`
- Pattern: Ktor plugin architecture

## Entry Points

**Mobile - Android:**
- Location: `mobile/composeApp/src/androidMain/kotlin/` (MainActivity)
- Triggers: Android launcher activity
- Responsibilities: Initialize Koin, load App composable

**Mobile - iOS:**
- Location: `mobile/iosApp/` (MainViewController)
- Triggers: iOS app delegate
- Responsibilities: Initialize Koin, load App composable

**Mobile - Common:**
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt`
- Triggers: Platform entry points
- Responsibilities: Theme setup, navigation, crash logger init

**Backend:**
- Location: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/Application.kt`
- Triggers: `fun main()` - JVM startup
- Responsibilities: Create Netty server, configure modules, start listening

## Error Handling

**Strategy (Mobile):** Result-based error handling with UI state propagation

**Patterns:**
- Data layer: Catch exceptions, wrap in Result.failure()
- Repository: Propagate Result to ViewModel
- ViewModel: Map Result to UiState (Success/Error)
- UI: Display error state with retry option

**Strategy (Backend):** Exception handling via StatusPages plugin

**Patterns:**
- Throw exceptions for error conditions
- StatusPages catches and maps to HTTP error responses
- Structured ErrorResponse for consistent API errors

## Cross-Cutting Concerns

**Logging (Mobile):**
- CrashLogger abstraction with platform implementations
- Firebase Crashlytics (Android), NSLog (iOS)
- Injected via Koin: `koinInject<CrashLogger>()`

**Logging (Backend):**
- Logback with SLF4J
- Ktor CallLogging for request/response logging
- Location: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Monitoring.kt`

**Validation (Mobile):**
- Data class computed properties for business rules
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/models/ProjectItem.kt`

**Authentication (Backend):**
- JWT validation middleware
- Location: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Security.kt`

**Dependency Injection (Mobile):**
- Koin multiplatform DI
- Module hierarchy: appModule → crashLoggingModule, dataModule, domainModule, presentationModule
- Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/`

---

*Architecture analysis: 2026-01-15*
*Update when major patterns change*
