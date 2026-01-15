# Coding Conventions

**Analysis Date:** 2026-01-15

## Naming Patterns

**Files:**
- PascalCase for all Kotlin files matching class name: `ProjectItem.kt`, `ProjectsViewModel.kt`
- PascalCase for Composable screen files: `ProjectsScreen.kt`, `DirectionsScreen.kt`
- `*Test.kt` suffix for test files: `ProjectItemTest.kt`, `ProjectsViewModelTest.kt`
- Platform suffixes: `.android.kt`, `.ios.kt` for expect/actual implementations

**Functions:**
- camelCase for regular functions: `matchesTimeFilter()`, `updateCurrentTab()`
- PascalCase for @Composable functions: `ProjectsScreen()`, `ProjectCard()`, `PageIndicator()`
- No special prefix for async functions (suspend keyword is sufficient)
- `handle*` prefix for event handlers: `handleSearch()`, `handleFilterChange()`

**Variables:**
- camelCase for variables: `screenState`, `currentTabIndex`, `projects`
- `_underscore` prefix for private backing properties: `_screenUiState`, `_state`
- UPPER_SNAKE_CASE for constants: `CONTENT_STATE_LOADING`, `DEFAULT_PORT`
- No underscore prefix for private class members

**Types:**
- PascalCase for interfaces: `ProjectsRepository`, `JsonResourceDataSource`, `CrashLogger`
- PascalCase for classes: `ProjectsViewModel`, `ProjectsRepositoryImpl`
- PascalCase for data classes: `ProjectItem`, `Artist`, `HealthResponse`
- PascalCase for sealed classes: `ProjectsUiState`, `NavigationDestination`
- PascalCase for enums: `ProjectType`, `TimeFilter`

## Code Style

**Formatting:**
- 4-space indentation (enforced via `mobile/detekt.yml`)
- 120 character max line length (enforced via `MaxLineLength` rule)
- No semicolons (enforced via `NoSemicolons: autoCorrect = true`)
- Final newlines required (enforced via `FinalNewline: true`)
- No trailing whitespace (enforced)

**Linting:**
- Detekt with `mobile/detekt.yml` and `backend/detekt.yml`
- Run: `./mobile/gradlew detekt`, `./backend/gradlew detekt`
- Auto-fix available: `./gradlew detektFormat`

**Compose-specific:**
- Wildcard imports allowed for Compose packages: `androidx.compose.foundation.*`, `androidx.compose.material3.*`, `androidx.compose.runtime.*`, `androidx.compose.ui.*`
- MagicNumber rule allows common dp values: 8, 16, 24, 32
- `@Composable` and `@Preview` excluded from naming rules

## Import Organization

**Order:**
1. Kotlin standard library (`kotlin.*`)
2. Kotlinx libraries (`kotlinx.*`)
3. Android/Androidx (`android.*`, `androidx.*`)
4. Third-party libraries (`io.ktor.*`, `org.koin.*`)
5. Internal modules (`io.asterixorobelix.afrikaburn.*`)

**Grouping:**
- Blank line between groups
- Alphabetical within each group

**Path Aliases:**
- Not used (Kotlin package imports used directly)

## Error Handling

**Patterns:**
- Result<T> for repository return types (success/failure)
- Sealed class UiState for UI layer (Loading, Success, Error)
- try/catch with `@Suppress("TooGenericExceptionCaught")` where necessary

**Error Types:**
- Throw exceptions in data sources for unexpected errors
- Wrap in Result.failure() at repository layer
- Map to UiState.Error at ViewModel layer with user-friendly message

**Logging:**
- Use `CrashLogger.logException()` for error tracking
- Include contextual information: `crashLogger.setCustomKey("key", "value")`

## Logging

**Framework (Mobile):**
- CrashLogger abstraction (platform-specific implementations)
- Firebase Crashlytics (Android), NSLog (iOS)

**Framework (Backend):**
- Logback with SLF4J
- Ktor CallLogging for HTTP requests

**Patterns:**
- Log at service boundaries, not in utility functions
- Include contextual keys for debugging: `crashLogger.setCustomKey("projectType", type.name)`
- No `println` or `System.out` in production code (enforced)

## Comments

**When to Comment:**
- KDoc for public API functions and data classes
- Inline comments for complex business logic
- Explain "why", not "what"

**KDoc/TSDoc:**
- Required for public functions and computed properties in data classes
- Format: `/** ... */` with proper tags

**TODO Comments:**
- Forbidden in committed code (enforced via `ForbiddenComment` rule)
- Detekt blocks: TODO, FIXME, STOPSHIP, HACK, XXX

**Example from `ProjectItem.kt`:**
```kotlin
/**
 * Determines if this project/camp is family-friendly based on the status field.
 * Returns true if status contains "Fam" (including "Fam(ish)")
 */
val isFamilyFriendly: Boolean
    get() = status.contains("Fam", ignoreCase = true)
```

## Function Design

**Size:**
- Max 60 lines per function (enforced via `LongMethod: threshold: 60`)
- Extract helpers for complex logic
- Complexity threshold: 15 (enforced via `ComplexMethod`)

**Parameters:**
- Max 6 parameters for functions (enforced via `LongParameterList: functionThreshold: 6`)
- Max 7 parameters for constructors (enforced via `constructorThreshold: 7`)
- Use data classes for parameter objects when needed

**Return Values:**
- Result<T> for operations that can fail
- Explicit return types for public functions
- Early returns for guard clauses allowed (`ReturnCount: max: 3`)

## Module Design

**Exports:**
- Internal visibility for implementation details
- Public visibility for interfaces and main classes
- Extension functions for utility operations

**Dependency Injection:**
- Koin modules per architectural layer
- `appModule` includes all sub-modules: `crashLoggingModule`, `dataModule`, `domainModule`, `presentationModule`
- Factory for ViewModels, single for repositories and data sources

## Compose-Specific Conventions

**String Resources:**
- MANDATORY: Use `stringResource(Res.string.key_name)` for all UI text
- NO hardcoded strings in Composables
- Resource file: `composeResources/values/strings.xml`

**Dimensions:**
- MANDATORY: Use `Dimens.*` object for all spacing/sizing
- NO hardcoded dp values (4.dp, 8.dp, 16.dp, etc.)
- Available: `paddingSmall`, `paddingMedium`, `paddingLarge`, `cornerRadiusMedium`

**Theme Usage:**
- Use `MaterialTheme.colorScheme` for all colors
- Use `MaterialTheme.typography` for all text styles
- Use `MaterialTheme.shapes` for all shapes

**Image Loading:**
- Use `AppAsyncImage()` component (not raw Coil AsyncImage)
- Location: `ui/components/AppAsyncImage.kt`

**Previews:**
- One preview per file: `@Preview @Composable private fun NamePreview()`
- Wrap in `AppTheme` for accurate theming

## Backend-Specific Conventions

**Plugin Pattern:**
- One file per feature: `HTTP.kt`, `Security.kt`, `Routing.kt`
- Extension function: `fun Application.configure*()`
- Called in order from `Application.module()`

**Constants:**
- Top-level object: `object CONSTANTS { const val ... }`
- UPPER_SNAKE_CASE names

**Environment Variables:**
- Access via `System.getenv("VAR_NAME") ?: defaultValue`
- Provide sensible defaults for development

---

*Convention analysis: 2026-01-15*
*Update when patterns change*
