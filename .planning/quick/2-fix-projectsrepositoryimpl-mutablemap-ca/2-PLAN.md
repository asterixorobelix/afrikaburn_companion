---
phase: quick
plan: 2
type: execute
wave: 1
depends_on: []
files_modified:
  - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt
  - mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImplTest.kt
autonomous: true
requirements: [CACHE-CONCURRENCY-01]

must_haves:
  truths:
    - "Concurrent coroutine reads never corrupt the cache"
    - "Only one coroutine loads data per ProjectType at a time (no duplicate loads)"
    - "All existing tests pass with the new implementation"
  artifacts:
    - path: "mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt"
      provides: "Thread-safe cache using kotlinx.coroutines.sync.Mutex"
      contains: "Mutex"
  key_links:
    - from: "ProjectsRepositoryImpl"
      to: "kotlinx.coroutines.sync.Mutex"
      via: "mutex.withLock { ... } protecting cache reads and writes"
      pattern: "mutex\\.withLock"
---

<objective>
Replace the unsafe `mutableMapOf` cache in `ProjectsRepositoryImpl` with a Mutex-guarded map to eliminate the race condition where concurrent coroutines can interleave cache reads, data loading, and cache writes for the same `ProjectType`.

Purpose: KMP commonMain cannot use JVM-only `ConcurrentHashMap`; `kotlinx.coroutines.sync.Mutex` is the idiomatic KMP-safe solution. The `withLock` pattern ensures only one coroutine at a time enters the cache check-and-load critical section.
Output: Thread-safe `ProjectsRepositoryImpl.kt` + passing tests.
</objective>

<execution_context>
@/root/.config/opencode/get-shit-done/workflows/execute-plan.md
@/root/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt
@mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImplTest.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Replace mutableMapOf with Mutex-protected cache</name>
  <files>mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt</files>
  <action>
    Add `import kotlinx.coroutines.sync.Mutex` and `import kotlinx.coroutines.sync.withLock` at the top of the file.

    Replace the bare `cache` field:
    ```kotlin
    private val cache = mutableMapOf<ProjectType, List<ProjectItem>>()
    ```
    with a Mutex-guarded pair:
    ```kotlin
    private val cacheMutex = Mutex()
    private val cache = mutableMapOf<ProjectType, List<ProjectItem>>()
    ```

    Wrap the entire body of `getProjectsByType` (the cache-check + load + cache-write block, inside the existing try) with `cacheMutex.withLock { ... }` so it becomes:
    ```kotlin
    return cacheMutex.withLock {
        try {
            cache[type]?.let { cachedProjects ->
                return@withLock Result.success(cachedProjects)
            }
            val projects = jsonDataSource.loadProjectsByType(type)
            cache[type] = projects
            Result.success(projects)
        } catch (e: DataSourceException) {
            Result.failure(RepositoryException("Unable to load ${type.displayName}", e))
        } catch (e: Exception) {
            Result.failure(RepositoryException("Unexpected error loading ${type.displayName}", e))
        }
    }
    ```

    Also wrap `clearCache()` body with `cacheMutex.withLock { cache.clear() }` and change it to `suspend fun clearCache()` since `withLock` is a suspend function.

    Note: `return@withLock` is used (not bare `return`) because `withLock` is an inline lambda, requiring a labeled return.

    Do NOT use `ConcurrentHashMap` — it is JVM-only and unavailable in commonMain KMP code.
  </action>
  <verify>
    Run: `./mobile/gradlew -p mobile :composeApp:compileKotlinMetadata 2>&1 | tail -20`
    Expected: BUILD SUCCESSFUL, no unresolved reference errors.
  </verify>
  <done>File compiles cleanly; `cacheMutex` field and `withLock` usage present in source; `ConcurrentHashMap` absent.</done>
</task>

<task type="auto">
  <name>Task 2: Update tests for suspend clearCache + add concurrency regression test</name>
  <files>mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImplTest.kt</files>
  <action>
    Since `clearCache()` is now `suspend`, update any direct calls to it in tests to be called within `runTest { }`.

    Add a new concurrency regression test after the existing tests:
    ```kotlin
    @Test
    fun `getProjectsByType should not duplicate-load under concurrent access`() = runTest {
        var loadCount = 0
        val dataSource = object : JsonResourceDataSource {
            override suspend fun loadProjectsByType(type: ProjectType): List<ProjectItem> {
                loadCount++
                return sampleProjects
            }
        }
        val repository = ProjectsRepositoryImpl(dataSource)

        // Launch concurrent coroutines all requesting the same type
        val jobs = (1..10).map {
            launch { repository.getProjectsByType(ProjectType.ART) }
        }
        jobs.forEach { it.join() }

        // Mutex ensures only ONE actual load happened despite 10 concurrent calls
        assertEquals(1, loadCount, "Expected exactly 1 data source load due to cache hit after first load")
    }
    ```

    Add required import if not present:
    ```kotlin
    import kotlinx.coroutines.launch
    ```
  </action>
  <verify>
    Run: `./mobile/gradlew -p mobile test 2>&1 | grep -E "ProjectsRepositoryImpl|BUILD|FAILED|tests"`
    Expected: All `ProjectsRepositoryImplTest` tests pass, BUILD SUCCESSFUL.
  </verify>
  <done>All existing 5 tests pass; new concurrency test passes; `loadCount == 1` assertion holds.</done>
</task>

<task type="auto">
  <name>Task 3: Detekt and coverage gate</name>
  <files></files>
  <action>
    Run detekt and coverage gate to ensure no new violations and 80% minimum is still met.
    Commands (run sequentially):
    1. `./mobile/gradlew -p mobile detekt`
    2. `./mobile/gradlew -p mobile jacocoTestCoverageVerification`

    If detekt flags `TooGenericExceptionCaught` on the existing `catch (e: Exception)` block, the existing `@Suppress("TooGenericExceptionCaught")` annotation is already present — no change needed.
  </action>
  <verify>
    Both Gradle tasks exit with BUILD SUCCESSFUL.
  </verify>
  <done>Zero detekt violations introduced; Jacoco coverage ≥ 80% maintained.</done>
</task>

</tasks>

<verification>
After all tasks:
1. `grep -n "Mutex\|withLock" mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt` — confirms Mutex usage
2. `grep -n "ConcurrentHashMap" mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt` — must return nothing
3. `./mobile/gradlew -p mobile test` — all tests green
</verification>

<success_criteria>
- `ProjectsRepositoryImpl` uses `kotlinx.coroutines.sync.Mutex` to protect cache access
- No `ConcurrentHashMap` in commonMain source
- Concurrent coroutines requesting the same `ProjectType` result in exactly 1 data source call
- All existing and new tests pass
- Detekt clean, Jacoco ≥ 80%
</success_criteria>

<output>
After completion, create `.planning/quick/2-fix-projectsrepositoryimpl-mutablemap-ca/2-SUMMARY.md`
</output>
