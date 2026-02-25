---
phase: quick
plan: 2
subsystem: data/repository
tags: [concurrency, thread-safety, mutex, cache, coroutines, kmp]
dependency_graph:
  requires: []
  provides: [thread-safe-projects-cache]
  affects: [ProjectsRepository, ProjectsRepositoryImpl, ProjectsRepositoryImplTest]
tech_stack:
  added: [kotlinx.coroutines.sync.Mutex, kotlinx.coroutines.sync.withLock]
  patterns: [mutex-guarded-cache, single-critical-section-lock]
key_files:
  modified:
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt
    - mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImplTest.kt
key_decisions:
  - "Single Mutex.withLock wrapping entire check-and-load block (not two separate fine-grained locks) to guarantee exactly-once loading per ProjectType under concurrent access"
  - "clearCache() changed to suspend fun since withLock is a suspension point"
  - "Used return@withLock labeled return (not bare return) because withLock uses an inline lambda"
metrics:
  duration: "~8 minutes"
  completed: "2026-02-25"
  tasks_completed: 3
  files_modified: 2
---

# Quick Task 2: Fix ProjectsRepositoryImpl MutableMap Cache Concurrency Summary

**One-liner:** Thread-safe KMP cache via `kotlinx.coroutines.sync.Mutex` wrapping entire check-and-load critical section to eliminate duplicate-load race condition.

## Objective

Replace the unsafe `mutableMapOf` cache in `ProjectsRepositoryImpl` with a Mutex-guarded map to eliminate the race condition where concurrent coroutines could interleave cache reads, data loading, and cache writes for the same `ProjectType`.

## What Was Done

### Task 1: Replace mutableMapOf with Mutex-protected cache

Replaced the bare `mutableMapOf` cache with a `cacheMutex = Mutex()` guarding the entire `getProjectsByType` body:

```kotlin
return cacheMutex.withLock {
    try {
        cache[type]?.let { cachedProjects ->
            return@withLock Result.success(cachedProjects)
        }
        val projects = jsonDataSource.loadProjectsByType(type)
        cache[type] = projects
        Result.success(projects)
    } catch (e: DataSourceException) { ... }
    catch (e: Exception) { ... }
}
```

- Added `import kotlinx.coroutines.sync.Mutex` and `import kotlinx.coroutines.sync.withLock`
- Removed unused `import kotlinx.coroutines.delay`
- Changed `clearCache()` to `suspend fun clearCache()` (required by `withLock`)
- No `ConcurrentHashMap` used (JVM-only, unavailable in KMP commonMain)

**Commit:** `5ec4635`

### Task 2: Update tests for suspend clearCache + add concurrency regression test

The test file was modified (by another process) before Task 2 ran, adding a concurrency test using `async`/`awaitAll` with `loadCallCount` tracking on the mock. The mock `MockJsonResourceDataSourceForRepository` was updated with a `loadCallCount` property to support the concurrency assertion.

**Commit:** `83773a0` (pre-existing before this task's execution)

### Task 3: Detekt and coverage gate

- **Detekt:** BUILD SUCCESSFUL — 0 code smells across 91 Kotlin files
- **Jacoco:** `jacocoTestCoverageVerification` task does not exist in this project (not configured in Gradle); this is a pre-existing condition unrelated to the changes

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed two-lock approach allowing duplicate loads**
- **Found during:** Task 3 (final verification)
- **Issue:** An earlier commit (`83773a0`) by another process reverted the single-lock implementation back to a two-lock approach (separate `withLock` for read and `withLock` for write, with I/O outside the lock). The comment in that code acknowledged: "Worst case on first concurrent call: two coroutines both miss the cache and both load from the data source (idempotent)." This violated the plan's hard requirement: "Only one coroutine loads data per ProjectType at a time (no duplicate loads)."
- **Fix:** Restored the single `cacheMutex.withLock { ... }` wrapping the entire check-and-load critical section, guaranteeing exactly one data-source call per ProjectType under any level of concurrent access.
- **Files modified:** `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt`
- **Commit:** `e2be36c`

### Out-of-Scope Pre-existing Issues (Deferred)

The following pre-existing errors exist in other files unrelated to this task:
- `UnlockStateRepositoryImpl.kt:16` — Unresolved reference 'System'
- `UserCampPinRepositoryImpl.kt:62,78,89` — Unresolved reference 'System'
- `EventDateService.kt:42` — Unresolved reference 'System'

These prevent full test suite execution (iOS Arm64/SimulatorArm64 compilation fails). Logged to deferred items — not within scope of this fix.

### Environment Limitations

- **Android SDK not present:** `./mobile/gradlew -p mobile test` fails (requires Android SDK). Test verification performed via `compileKotlinMetadata` (BUILD SUCCESSFUL) which validates our specific files.
- **`jacocoTestCoverageVerification` task not found:** Jacoco is not configured in this project's Gradle files despite being mentioned in `CLAUDE.md`. Detekt verification succeeded instead.

## Verification Results

| Check | Result |
|-------|--------|
| `grep Mutex\|withLock ProjectsRepositoryImpl.kt` | ✅ Lines 8,9,20,27,31,51 |
| `grep ConcurrentHashMap ProjectsRepositoryImpl.kt` | ✅ Empty (not present) |
| `compileKotlinMetadata` | ✅ BUILD SUCCESSFUL |
| `detekt` | ✅ BUILD SUCCESSFUL, 0 violations |
| Single withLock wrapping full critical section | ✅ Confirmed |
| clearCache() is suspend | ✅ Confirmed |
| return@withLock labeled return used | ✅ Confirmed |

## Success Criteria

- [x] `ProjectsRepositoryImpl` uses `kotlinx.coroutines.sync.Mutex` to protect cache access
- [x] No `ConcurrentHashMap` in commonMain source
- [x] Concurrent coroutines requesting same `ProjectType` result in exactly 1 data source call (guaranteed by single-lock critical section)
- [x] Concurrency regression test added (`loadCallCount == 1` assertion)
- [x] Detekt clean (0 violations)
- [ ] All tests pass — blocked by pre-existing unrelated compilation errors in other files; not caused by this change

## Commits

| Hash | Message |
|------|---------|
| `5ec4635` | fix(quick-2): replace mutableMapOf cache with Mutex-protected cache |
| `e2be36c` | fix(quick-2): use single Mutex.withLock to guarantee exactly-once cache load |

## Self-Check: PASSED

- `ProjectsRepositoryImpl.kt` exists and contains `cacheMutex.withLock` ✅
- `ProjectsRepositoryImplTest.kt` exists and contains `loadCallCount` assertion ✅
- Commits `5ec4635` and `e2be36c` present in git log ✅
