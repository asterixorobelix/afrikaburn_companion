# Testing Patterns

**Analysis Date:** 2026-01-15

## Test Framework

**Runner (Mobile):**
- Kotlin Test with JUnit 4.13.2
- Config: Test configuration in `mobile/composeApp/build.gradle.kts`

**Runner (Backend):**
- JUnit 5 with Kotest 6.0.3
- Config: `backend/build.gradle.kts` with `useJUnitPlatform()`

**Assertion Library:**
- Mobile: Kotlin Test built-in assertions (`assertEquals`, `assertTrue`, `assertNotNull`)
- Backend: Kotest matchers

**Run Commands:**
```bash
# Mobile - Run all tests
./mobile/gradlew -p mobile test

# Mobile - Run with coverage
./mobile/gradlew -p mobile test jacocoTestReport

# Mobile - Verify coverage (80% minimum)
./mobile/gradlew -p mobile jacocoTestCoverageVerification

# Backend - Run all tests
./backend/gradlew -p backend test

# Backend - Run with coverage
./backend/gradlew -p backend test jacocoTestReport
```

## Test File Organization

**Location:**
- Mobile: `mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/`
- Backend: `backend/src/test/kotlin/io/asterixorobelix/afrikaburn/`

**Naming:**
- Unit tests: `ClassNameTest.kt` (e.g., `ProjectItemTest.kt`)
- Integration tests: `FeatureNameIntegrationTest.kt` (e.g., `TimeFilterIntegrationTest.kt`)
- ViewModel tests: `ViewModelNameTest.kt` (e.g., `ProjectsViewModelTest.kt`)

**Structure:**
```
mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/
├── models/
│   └── ProjectItemTest.kt
├── presentation/
│   └── projects/
│       ├── ProjectsViewModelTest.kt
│       ├── ProjectTabViewModelTest.kt
│       └── FamilyFilterViewModelTest.kt
├── data/
│   └── repository/
│       └── ProjectsRepositoryImplTest.kt
└── integration/
    ├── TimeFilterIntegrationTest.kt
    ├── FamilyFilterIntegrationTest.kt
    └── ProjectsIntegrationTest.kt
```

## Test Structure

**Suite Organization:**
```kotlin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectItemTest {

    @Test
    fun `ProjectItem should deserialize from JSON correctly`() {
        // Given JSON with all fields
        val jsonString = """..."""

        // When deserializing
        val projectItem = json.decodeFromString<ProjectItem>(jsonString)

        // Then all fields should be mapped correctly
        assertEquals("Test Art Installation", projectItem.name)
    }

    @Test
    fun `isFamilyFriendly should return true when status contains Fam`() {
        // Arrange
        val project = ProjectItem(name = "Test", description = "Desc", status = "Fam(ish)")

        // Act & Assert
        assertTrue(project.isFamilyFriendly)
    }
}
```

**Patterns:**
- Backtick-quoted descriptive test names (BDD style)
- Given/When/Then or Arrange/Act/Assert structure
- One assertion focus per test (multiple assertions OK for related checks)
- `@Test` annotation from kotlin.test

## Mocking

**Framework (Mobile):**
- MockK 1.14.6 (Android unit tests)
- Manual mock implementations for commonTest

**Framework (Backend):**
- MockK 1.14.5

**Patterns (Manual Mock for commonTest):**
```kotlin
internal class MockJsonResourceDataSourceForRepository : JsonResourceDataSource {
    private var shouldThrowError = false
    private var errorMessage = ""
    private var projects = emptyList<ProjectItem>()
    var lastRequestedType: ProjectType? = null

    fun setSuccessResponse(projectList: List<ProjectItem>) {
        shouldThrowError = false
        projects = projectList
    }

    fun setErrorResponse(message: String) {
        shouldThrowError = true
        errorMessage = message
    }

    override suspend fun loadProjectsByType(type: ProjectType): List<ProjectItem> {
        lastRequestedType = type
        if (shouldThrowError) throw Exception(errorMessage)
        return projects
    }
}
```

**What to Mock:**
- Data sources (JsonResourceDataSource)
- External dependencies (network, file system)
- Platform-specific implementations

**What NOT to Mock:**
- Data classes and models
- Pure functions and utilities
- Kotlin standard library

## Fixtures and Factories

**Test Data:**
```kotlin
// Inline test data
val testProject = ProjectItem(
    name = "Test Art Installation",
    description = "Test description",
    artist = Artist(name = "Test Artist"),
    code = "AB-001",
    status = "Fam(ish)"
)

// JSON fixtures for serialization tests
private val testJsonString = """
{
    "Name": "Test Art Installation",
    "Description": "A beautiful test artwork",
    "Artist": {"name": "Test Artist"},
    "code": "TEST-001",
    "status": "Day Time"
}
"""
```

**Location:**
- Inline in test file when simple
- Test helper classes in same test file (e.g., `MockJsonResourceDataSourceForRepository`)

## Coverage

**Requirements:**
- 80% minimum line coverage (enforced via Jacoco)
- CI blocks merges if coverage fails

**Configuration:**
- Tool: Jacoco (configured in `build.gradle.kts`)
- Reports: `build/reports/jacoco/test/html/`

**View Coverage:**
```bash
# Generate coverage report
./mobile/gradlew -p mobile test jacocoTestReport

# Open HTML report
open mobile/composeApp/build/reports/jacoco/test/html/index.html

# Verify 80% threshold
./mobile/gradlew -p mobile jacocoTestCoverageVerification
```

## Test Types

**Unit Tests:**
- Scope: Single function/class in isolation
- Mocking: Mock all external dependencies
- Speed: <100ms per test
- Examples: `ProjectItemTest.kt`, `FamilyFilterUiStateTest.kt`

**Integration Tests:**
- Scope: Multiple modules together
- Mocking: Mock only external boundaries
- Examples: `TimeFilterIntegrationTest.kt`, `FamilyFilterIntegrationTest.kt`, `ProjectsIntegrationTest.kt`

**ViewModel Tests:**
- Scope: ViewModel + mocked repository
- Pattern: Test state transitions and data flow
- Examples: `ProjectsViewModelTest.kt`, `ProjectTabViewModelTest.kt`

**E2E Tests:**
- Not currently implemented
- Mobile UI testing planned via Compose testing library

## Common Patterns

**Async Testing:**
```kotlin
@Test
fun `repository should return projects when data source succeeds`() = runTest {
    // Arrange
    mockDataSource.setSuccessResponse(listOf(testProject))

    // Act
    val result = repository.getProjectsByType(ProjectType.THEME_CAMPS)

    // Assert
    assertTrue(result.isSuccess)
    assertEquals(1, result.getOrNull()?.size)
}
```

**Error Testing:**
```kotlin
@Test
fun `repository should return failure when data source throws`() = runTest {
    // Arrange
    mockDataSource.setErrorResponse("Network error")

    // Act
    val result = repository.getProjectsByType(ProjectType.ARTWORKS)

    // Assert
    assertTrue(result.isFailure)
}
```

**Serialization Testing:**
```kotlin
@Test
fun `ProjectItem should deserialize from JSON correctly`() {
    val jsonString = """{"Name": "Test", "Description": "Desc"}"""
    val projectItem = json.decodeFromString<ProjectItem>(jsonString)
    assertEquals("Test", projectItem.name)
}
```

**Computed Property Testing:**
```kotlin
@Test
fun `isFamilyFriendly should return true when status contains Fam`() {
    val project = ProjectItem(name = "Test", description = "Desc", status = "Fam(ish)")
    assertTrue(project.isFamilyFriendly)
}

@Test
fun `isDaytime should return true when status contains Day Time`() {
    val project = ProjectItem(name = "Test", description = "Desc", status = "Day Time")
    assertTrue(project.isDaytime)
}
```

**Snapshot Testing:**
- Not used in this codebase
- Prefer explicit assertions for clarity

---

*Testing analysis: 2026-01-15*
*Update when test patterns change*
