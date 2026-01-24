# Project State

**Last Updated:** 2026-01-24
**Current Phase:** Phase 7 - Tab Visibility Control (COMPLETE)

## Current Position

Phase: 7 (Tab Visibility Control)
Plan: 2 of 2 in current phase
Status: Complete
Last activity: 2026-01-24 — Completed 07-02-PLAN.md (Navigation Integration)

Progress: ███████▒░░ 75% (v3.1)

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-01-22)

**Core value:** Participants can discover and navigate to AfrikaBurn theme camps and artworks without internet connectivity.
**Current focus:** Event Surprise Mode - hide Map and Projects tabs until user is at event or event has started

## Milestone Progress

**v3.1 Event Surprise Mode** — IN PROGRESS

| Phase | Name | Status |
|-------|------|--------|
| 5 | Event Date Configuration | Complete |
| 6 | Geofence Detection | Complete |
| 7 | Tab Visibility Control | Complete |
| 8 | Polish & Edge Cases | Not started |

**Previous:** See `.planning/milestones/v3.0-ROADMAP.md`

## Key Decisions Log

See `.planning/PROJECT.md` Key Decisions table for complete history.

**v3.1 Decisions:**
- Tabs completely hidden when locked (not visible with lock icon)
- Unlock persists permanently once triggered
- 20km geofence radius for event proximity
- Date OR location unlocks (either condition sufficient)

**Phase 5 Decisions:**
- Africa/Johannesburg timezone for date calculations
- Clock injection pattern for testable time-based logic
- Bypass flag as constructor parameter for testing flexibility

**Phase 6 Decisions:**
- 1% tolerance for distance calculation tests
- Boundary inclusive (distance <= radius returns true)
- Null LocationData returns false (graceful handling)
- Earth radius 6371 km for Haversine calculations

**Phase 7 Plan 1 Decisions:**
- Epoch milliseconds for timestamp storage (consistent with UserCampPin)
- Bypass returns true without persisting (testing flexibility)
- Date OR geofence sufficient (OR condition, not AND)
- Once unlocked, always unlocked (no reversion)

**Phase 7 Plan 2 Decisions:**
- Directions and About tabs always visible (essential for pre-event use)
- Projects and Map tabs hidden until unlock (surprise mode)
- Welcome message only on fresh unlock (wasJustUnlocked tracking)
- Start destination changes based on unlock state (Directions/Projects)
- Default parameter in BottomNavigationBar for backward compatibility

## Blockers

None.

## Session Continuity

Last session: 2026-01-24
Stopped at: Completed 07-02-PLAN.md (Navigation Integration)
Resume file: Phase 8 planning required

## Phase 7 Completion Summary

**Plan 07-01: UnlockConditionManager**
- SQLDelight schema for permanent unlock persistence
- UnlockStateRepository interface and implementation
- UnlockConditionManager combining date, geofence, and persistence logic
- 17 comprehensive TDD tests

**Plan 07-02: Navigation Integration**
- getVisibleDestinations() function for filtered navigation
- BottomNavigationBar accepts destinations parameter
- App.kt integration with conditional tab visibility
- Welcome snackbar on first unlock
- wasJustUnlocked() tracking for one-time message
- Human verification checkpoint passed

## Next Steps

1. Plan Phase 8 (Polish & Edge Cases)
2. Address any edge cases discovered during Phase 7 testing
3. Prepare for v3.1 release

---

*State updated: 2026-01-24 after completing Phase 7 (Tab Visibility Control)*
