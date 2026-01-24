---
phase: 07-tab-visibility-control
plan: 02
subsystem: presentation
tags: [compose, navigation, unlock-state, welcome-message, tab-visibility, ui-integration]

# Dependency graph
requires:
  - phase: 07-tab-visibility-control
    plan: 01
    provides: UnlockConditionManager with isUnlocked(), wasJustUnlocked(), and persistence
  - phase: 06-geofence
    provides: GeofenceService for location-based unlock
  - phase: 05-event-config
    provides: EventDateService for date-based unlock
provides:
  - Conditional tab visibility based on unlock state (2 tabs locked, 2 tabs always visible)
  - Dynamic start destination (Directions when locked, Projects when unlocked)
  - Welcome snackbar message on first unlock
  - getVisibleDestinations() function for filtered navigation
affects: [user-experience, navigation-flow, first-run-experience]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Default parameter pattern for backward-compatible API changes"
    - "In-memory session tracking for one-time UI effects (wasJustUnlocked)"
    - "LaunchedEffect with conditional trigger for side effects"

key-files:
  created: []
  modified:
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/navigation/NavigationDestination.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/navigation/BottomNavigationBar.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt
    - mobile/composeApp/src/commonMain/composeResources/values/strings.xml
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/service/UnlockConditionManager.kt
    - mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/domain/service/UnlockConditionManagerTest.kt

key-decisions:
  - "Directions and About tabs always visible (essential for pre-event use)"
  - "Projects and Map tabs hidden until unlock (surprise mode)"
  - "Welcome message only on fresh unlock (wasJustUnlocked tracking)"
  - "Start destination changes based on unlock state"
  - "Default parameter in BottomNavigationBar for backward compatibility"

patterns-established:
  - "Session-scoped state tracking via in-memory flags"
  - "Conditional navigation composition based on external state"

# Metrics
duration: ~35min (across sessions with human verification)
completed: 2026-01-24
---

# Phase 7 Plan 2: Navigation Integration Summary

**Tab visibility control integrated with UnlockConditionManager - Map and Projects tabs hidden until event starts or user is at event location, with welcome message on first unlock**

## Performance

- **Duration:** ~35 min (including human verification checkpoint)
- **Started:** 2026-01-22T15:43:00Z
- **Completed:** 2026-01-24
- **Tasks:** 5 (4 auto + 1 human checkpoint)
- **Files modified:** 6 (0 created, 6 modified)

## Accomplishments

- Added getVisibleDestinations() function to NavigationDestination with locked/unlocked filtering
- Updated BottomNavigationBar to accept destinations list parameter with default for backward compatibility
- Added welcome string resource for surprise mode unlock message
- Integrated UnlockConditionManager into App.kt for conditional tab visibility
- Implemented dynamic start destination (Directions when locked, Projects when unlocked)
- Added wasJustUnlocked() method to fix welcome message showing on every launch
- Passed human verification checkpoint with all test scenarios confirmed

## Task Commits

Each task was committed atomically:

1. **Task 1: Add getVisibleDestinations function** - `6077f4a` (feat)
2. **Task 2: Update BottomNavigationBar** - `7c36a7c` (feat)
3. **Task 3: Add welcome string resource** - `f5eac9d` (feat)
4. **Task 4: Integrate unlock logic into App.kt** - `dc74138` (feat)
5. **Task 5: Human verification** - APPROVED
6. **Post-checkpoint fix: wasJustUnlocked tracking** - `b6c9d2f` (fix)

## Files Modified

- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/navigation/NavigationDestination.kt` - Added companion object with getVisibleDestinations(), allDestinations, lockedDestinations
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/navigation/BottomNavigationBar.kt` - Added destinations parameter with default value
- `mobile/composeApp/src/commonMain/composeResources/values/strings.xml` - Added unlock_welcome_message string resource
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt` - Integrated UnlockConditionManager with navigation, added welcome message logic
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/service/UnlockConditionManager.kt` - Added wasJustUnlocked() method with session tracking
- `mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/domain/service/UnlockConditionManagerTest.kt` - Added 4 tests for wasJustUnlocked behavior

## Decisions Made

1. **Tab visibility:** Only Directions and About visible when locked; all 4 tabs when unlocked
2. **Start destination:** Directions when locked (event navigation focus), Projects when unlocked (content discovery)
3. **Welcome message timing:** Only on fresh unlock (wasJustUnlocked), not on every app launch
4. **API compatibility:** Default parameter for destinations maintains backward compatibility
5. **Session tracking:** In-memory flag (justUnlockedThisSession) not persisted, resets on app restart

## Human Verification Results

All test scenarios passed during manual verification:

1. **Locked state:** Only 2 tabs visible (Directions, About) - VERIFIED
2. **Unlocked state:** All 4 tabs visible when date moved to past - VERIFIED
3. **Persistence:** Tabs stay visible after reverting date - VERIFIED
4. **Welcome message fix:** No longer shows on every launch - VERIFIED

## Deviations from Plan

One post-checkpoint fix was required:

- **Issue:** Welcome message was showing on every app launch, not just on first unlock
- **Root cause:** Original logic checked `getUnlockedAt() != null`, which is true for persisted unlocks
- **Fix:** Added `wasJustUnlocked()` method to track fresh unlocks (in-memory session flag)
- **Tests added:** 4 new tests for wasJustUnlocked behavior

## Issues Encountered

1. **Welcome message on every launch** - Fixed with wasJustUnlocked tracking
2. No other issues encountered

## User Setup Required

None - feature works out of the box based on date and location conditions.

## Phase Completion Status

Phase 7 (Tab Visibility Control) is now complete:

- Plan 07-01: UnlockConditionManager - COMPLETE
- Plan 07-02: Navigation Integration - COMPLETE

All test scenarios verified:
- Tab hiding works in locked state
- Tab visibility works in unlocked state
- Unlock persistence works across app restarts
- Welcome message appears only once on fresh unlock

## Test Coverage

17 tests for UnlockConditionManager (13 original + 4 new for wasJustUnlocked):

- Already persisted unlock returns true
- Event date triggers unlock and persists
- Geofence triggers unlock and persists
- No conditions met returns false
- Bypass unlocks without persistence
- Once unlocked, always unlocked
- checkAndUpdateUnlockState persists when conditions met
- checkAndUpdateUnlockState does not persist when conditions not met
- getUnlockedAt returns null when not unlocked
- getUnlockedAt returns timestamp when unlocked
- Null location with event started still unlocks
- Bypass takes priority
- Either date OR geofence unlocks (OR condition)
- wasJustUnlocked returns false when not unlocked
- wasJustUnlocked returns true when fresh unlock happens this session
- wasJustUnlocked returns false when already unlocked from persistence
- wasJustUnlocked returns false when bypass is used

## Next Phase Readiness

Phase 8 (Polish & Edge Cases) can now begin:
- Tab visibility control fully functional
- Welcome message behavior correct
- All core surprise mode functionality complete
- Ready for edge case handling and polish

---
*Phase: 07-tab-visibility-control*
*Plan: 02*
*Completed: 2026-01-24*
