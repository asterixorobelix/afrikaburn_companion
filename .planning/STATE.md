# Project State

**Last Updated:** 2026-01-22
**Current Phase:** Phase 7 - Tab Visibility Control

## Current Position

Phase: 7 (Tab Visibility Control)
Plan: 1 of 2 in current phase
Status: In progress
Last activity: 2026-01-22 — Completed 07-01-PLAN.md

Progress: ███████░░░ 62.5% (v3.1)

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
| 7 | Tab Visibility Control | In progress (1/2 plans) |
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

## Blockers

None.

## Session Continuity

Last session: 2026-01-22
Stopped at: Completed 07-01-PLAN.md (UnlockConditionManager)
Resume file: .planning/phases/07-tab-visibility-control/07-02-PLAN.md

## Next Steps

1. Run `/gsd:execute-phase` to continue Phase 7
2. Plan 07-02 integrates unlock logic with navigation
3. Has human-verify checkpoint for visual testing

---

*State updated: 2026-01-22 after completing 07-01-PLAN.md*
