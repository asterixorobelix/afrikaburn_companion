---
phase: 05-event-config
plan: 01
subsystem: domain
tags: [kotlinx-datetime, koin, tdd, event-config, geofence]

# Dependency graph
requires:
  - phase: none
    provides: First phase of v3.1 milestone
provides:
  - EventConfig data class with AfrikaBurn 2026 defaults
  - EventDateService interface and implementation
  - Clock abstraction for testable time-based logic
  - Debug bypass flag for development testing
affects: [06-geofence-detection, 07-tab-visibility, 08-polish]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - Clock injection for testable date logic
    - Interface-based service design for DI

key-files:
  created:
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/EventConfig.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/service/EventDateService.kt
    - mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/domain/service/EventDateServiceTest.kt
  modified:
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/DomainModule.kt

key-decisions:
  - "Used Africa/Johannesburg timezone for date calculations"
  - "Clock interface for testability with FakeClock pattern"
  - "Bypass flag as constructor parameter for easy testing"

patterns-established:
  - "Clock injection pattern for time-sensitive services"
  - "FakeClock pattern in tests for controlling time"

# Metrics
duration: 4min
completed: 2026-01-22
---

# Phase 5 Plan 1: Event Date Configuration Summary

**EventConfig data class and EventDateService with Clock injection for testable date-based event unlock detection**

## Performance

- **Duration:** 4 min
- **Started:** 2026-01-22T10:16:27Z
- **Completed:** 2026-01-22T10:20:23Z
- **Tasks:** 3
- **Files modified:** 4

## Accomplishments

- Created EventConfig data class with AfrikaBurn 2026 hardcoded values (dates, coordinates, geofence radius)
- Implemented EventDateService interface with isEventStarted(), isEventActive(), getEventConfig(), and isUnlockBypassed() methods
- Established Clock injection pattern for testable time-based logic
- Added debug bypass flag for development testing
- Registered services in Koin DI module
- Full TDD coverage with 12 passing tests

## Task Commits

Each task was committed atomically following TDD:

1. **Task 1 (RED): Write failing tests for EventDateService** - `500b3e5` (test)
2. **Task 2 (GREEN): Implement EventConfig and EventDateService** - `55ec377` (feat)
3. **Task 3 (REFACTOR): Register in DI and verify coverage** - `3f6a5c3` (refactor)

## Files Created/Modified

- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/EventConfig.kt` - Data class with AfrikaBurn 2026 event configuration
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/service/EventDateService.kt` - Service interface and implementation for date detection
- `mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/domain/service/EventDateServiceTest.kt` - Comprehensive TDD test suite (12 tests)
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/DomainModule.kt` - Updated with Clock and EventDateService registrations

## Decisions Made

1. **Timezone handling:** Used Africa/Johannesburg timezone for date calculations to match event location
2. **Clock abstraction:** Created Clock interface extending kotlinx.datetime.Clock for dependency injection and testability
3. **Bypass implementation:** Implemented bypass flag as constructor parameter rather than companion object constant for easier testing
4. **Event dates:** AfrikaBurn 2026 dates set to April 27 - May 3, 2026 (typical late April timing)

## Deviations from Plan

None - plan executed exactly as written.

## TDD Cycle Summary

### RED Phase
- Created EventDateServiceTest with 12 comprehensive test cases
- Tests covered: isEventStarted (3), isEventActive (5), getEventConfig (1), bypass flag (2), EventConfig.DEFAULT (1)
- Tests failed with compilation errors as expected (production code not implemented)
- Commit: `500b3e5`

### GREEN Phase
- Created EventConfig data class with DEFAULT companion object containing AfrikaBurn 2026 values
- Created EventDateService interface and EventDateServiceImpl
- Created DefaultClock implementation
- Used Africa/Johannesburg timezone for proper date handling
- All 12 tests passed
- Commit: `55ec377`

### REFACTOR Phase
- Registered Clock and EventDateService in Koin DomainModule
- Verified all tests still pass (12 EventDateService tests + full suite)
- No detekt violations in new code
- Commit: `3f6a5c3`

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- EventConfig ready for Phase 6 (Geofence Detection) to use coordinates and radius
- EventDateService ready for Phase 7 (Tab Visibility) to check isEventStarted()
- Debug bypass flag ready for Phase 8 (Polish & Edge Cases) testing scenarios
- Requirements satisfied: SURP-01, SURP-02, SURP-07

---
*Phase: 05-event-config*
*Completed: 2026-01-22*
