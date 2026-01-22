---
phase: 07-tab-visibility-control
plan: 01
subsystem: domain
tags: [sqldelight, koin, tdd, unlock-state, geofence, event-date, persistence]

# Dependency graph
requires:
  - phase: 05-event-config
    provides: EventDateService with isEventStarted() and isUnlockBypassed()
  - phase: 06-geofence
    provides: GeofenceService with isUserWithinGeofence(location)
provides:
  - UnlockState SQLDelight schema for permanent unlock persistence
  - UnlockStateRepository for unlock state access
  - UnlockConditionManager combining date, geofence, and persistence logic
  - Comprehensive TDD test suite (13 tests)
affects: [07-02-navigation-integration, tab-visibility-ui]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Single-row table pattern for permanent state persistence"
    - "Service composition for multi-condition evaluation"
    - "TDD with fake implementations for dependency injection testing"

key-files:
  created:
    - mobile/composeApp/src/commonMain/sqldelight/io/asterixorobelix/afrikaburn/UnlockState.sq
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/UnlockStateRepository.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/UnlockStateRepositoryImpl.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/service/UnlockConditionManager.kt
    - mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/domain/service/UnlockConditionManagerTest.kt
  modified:
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/DatabaseModule.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/DomainModule.kt

key-decisions:
  - "Epoch milliseconds for timestamp storage (consistent with UserCampPin pattern)"
  - "Bypass flag returns true without persisting (for testing flexibility)"
  - "Date OR geofence sufficient for unlock (either condition)"
  - "Once unlocked, always unlocked (no reversion)"

patterns-established:
  - "Fake service implementations for isolated unit testing"
  - "Service composition pattern for multi-dependency logic"

# Metrics
duration: 4min
completed: 2026-01-22
---

# Phase 7 Plan 1: Unlock Condition Manager Summary

**UnlockConditionManager service combining EventDateService, GeofenceService, and SQLDelight persistence with 13 comprehensive TDD tests**

## Performance

- **Duration:** 4 min
- **Started:** 2026-01-22T13:46:10Z
- **Completed:** 2026-01-22T13:50:01Z
- **Tasks:** 3
- **Files modified:** 7 (5 created, 2 modified)

## Accomplishments

- Created UnlockState SQLDelight schema for permanent unlock persistence
- Implemented UnlockStateRepository interface and implementation
- Built UnlockConditionManager combining date, geofence, and persistence logic
- Added comprehensive TDD test suite with 13 passing tests
- Registered all services in Koin DI modules (DatabaseModule, DomainModule)

## Task Commits

Each task was committed atomically:

1. **Task 1: Create SQLDelight schema** - `e8b40a9` (feat)
2. **Task 2: Create UnlockStateRepository** - `cab4b14` (feat)
3. **Task 3: Create UnlockConditionManager with TDD** - `de321c9` (feat)

## Files Created/Modified

- `mobile/composeApp/src/commonMain/sqldelight/io/asterixorobelix/afrikaburn/UnlockState.sq` - SQLDelight schema with single-row pattern
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/UnlockStateRepository.kt` - Repository interface
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/UnlockStateRepositoryImpl.kt` - SQLDelight implementation
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/service/UnlockConditionManager.kt` - Central unlock logic service
- `mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/domain/service/UnlockConditionManagerTest.kt` - 13 TDD tests
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/DatabaseModule.kt` - Added UnlockStateRepository registration
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/DomainModule.kt` - Added UnlockConditionManager registration

## Decisions Made

1. **Persistence pattern:** Used single-row table (id = 1) following UserCampPin pattern
2. **Timestamp format:** Epoch milliseconds for consistency with existing codebase
3. **Bypass behavior:** Returns true without persisting (enables testing without database side effects)
4. **Unlock logic:** OR condition (date OR geofence), not AND (both required)
5. **Refactored return statements:** Reduced from 4 to 2 to satisfy detekt ReturnCount rule

## Deviations from Plan

None - plan executed exactly as written.

## TDD Cycle Summary

### Test Coverage (13 tests)

1. Already persisted unlock returns true
2. Event date triggers unlock and persists
3. Geofence triggers unlock and persists
4. No conditions met returns false
5. Bypass unlocks without persistence
6. Once unlocked, always unlocked
7. checkAndUpdateUnlockState persists when conditions met
8. checkAndUpdateUnlockState does not persist when conditions not met
9. getUnlockedAt returns null when not unlocked
10. getUnlockedAt returns timestamp when unlocked
11. Null location with event started still unlocks
12. Bypass takes priority
13. Either date OR geofence unlocks (OR condition)

### Fake Implementations

- `FakeUnlockStateRepository` - In-memory boolean flag
- `FakeEventDateService` - Controllable isEventStarted, isUnlockBypassed
- `FakeGeofenceService` - Controllable isUserWithinGeofence

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- UnlockConditionManager ready for Plan 07-02 (Navigation Integration)
- Service properly registered in Koin DI
- Can be injected into navigation components to control tab visibility
- All 156 project tests pass (143 existing + 13 new)

---
*Phase: 07-tab-visibility-control*
*Completed: 2026-01-22*
