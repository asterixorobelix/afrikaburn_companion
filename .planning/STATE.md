# Project State

**Last Updated:** 2026-01-18
**Current Phase:** Phase 3 - User Location (In Progress - Checkpoint)

## Current Position

Phase: 3 of 4 (User Location)
Plan: 1 of 1 in progress (awaiting verification)
Status: Code complete, awaiting human verification
Last activity: 2026-01-18 - Phase 3 plan 01 executed to checkpoint

Progress: █████████░ 70%

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-01-18)

**Core value:** Participants can discover and navigate to AfrikaBurn theme camps and artworks without internet connectivity.
**Current focus:** v3.0 Offline Map milestone - Phase 3 at checkpoint (human verification required)

## Milestone Progress

**v3.0 Offline Map**

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 1 | Foundation & Basic Map | Complete | 100% |
| 2 | Markers & Detail Navigation | Complete | 100% |
| 3 | User Location | At Checkpoint | 90% |
| 4 | User Camp Pin | Pending | 0% |

**Overall:** 11/18 requirements complete (61%), Phase 3 pending verification

## Phase 1 Plans (Complete)

| Plan | Name | Wave | Status | Dependencies |
|------|------|------|--------|--------------|
| 01-01 | Map Infrastructure & Dependencies | 1 | Complete | None |
| 01-02 | Navigation Integration & Gestures | 2 | Complete | 01-01 |

**Requirements completed:**
- [x] MAP-01: Offline map tiles covering Tankwa Karoo region
- [x] MAP-02: Pan map by dragging
- [x] MAP-03: Zoom map by pinch gesture
- [x] MAP-04: Zoom in by double-tapping
- [x] NAV-01: Map tab in bottom navigation
- [x] NAV-02: Switch between Projects, Map, Directions, About tabs

## Phase 2 Plans (Complete)

| Plan | Name | Wave | Status | Dependencies |
|------|------|------|--------|--------------|
| 02-01 | Interactive Marker Tap to Detail | 1 | Complete | 01-02 |

**Requirements completed:**
- [x] MARK-01: User sees camp locations displayed as markers with distinct icon (purple circles)
- [x] MARK-02: User sees artwork locations displayed as markers with distinct icon (teal circles)
- [x] MARK-03: User can tap a camp marker to view camp details
- [x] MARK-04: User can tap an artwork marker to view artwork details
- [x] MARK-05: User can visually distinguish camp markers from artwork markers (color-coded)

## Phase 3 Plans (In Progress)

| Plan | Name | Wave | Status | Dependencies |
|------|------|------|--------|--------------|
| 03-01 | User Location & My Location FAB | 1 | Checkpoint | 02-01 |

**Requirements to complete (pending verification):**
- [ ] LOC-01: User sees their current GPS location on the map
- [ ] LOC-02: User can tap a button to center the map on their location
- [ ] LOC-03: User is prompted to grant location permission when accessing map

**Code complete - awaiting human verification:**
- LocationService expect/actual for Android (FusedLocationProviderClient) and iOS (CLLocationManager)
- MapUiState extended with user location fields
- MapViewModel with location tracking and permission handling
- UserLocationMarker (blue dot) and MyLocationButton FAB composables
- MapScreen integration with lifecycle-aware tracking

## Key Decisions Log

| Date | Decision | Context |
|------|----------|---------|
| 2026-01-18 | MapLibre Compose v0.11.1 | Official KMP support, no licensing costs |
| 2026-01-18 | Bundled PMTiles (~574KB) | Extracted from Protomaps for Tankwa region |
| 2026-01-18 | SQLDelight for camp pin | Already in project infra |
| 2026-01-18 | Defer search to v3.1 | Reduce scope for v3.0 |
| 2026-01-18 | Camps + artworks only | Performances/events deferred |
| 2026-01-18 | Code-based marker matching | ProjectItem.code used to link GeoJSON features to project data |
| 2026-01-18 | CircleLayer for markers | Programmatic markers work better than symbol icons |
| 2026-01-18 | Dark mode map style | Background #1a1a2e to match app theme |
| 2026-01-18 | Custom LocationService | expect/actual pattern like CrashLogger, not moko-geo library |
| 2026-01-18 | Balanced power accuracy | Battery conservation critical in desert environment |

## Blockers

**Checkpoint Required:** Plan 03-01 needs human verification before completion.

## Session Continuity

Last session: 2026-01-18
Stopped at: Plan 03-01 checkpoint (human verification)
Resume file: `.planning/phases/03-user-location/03-01-SUMMARY.md`

## Next Steps

1. Verify user location feature on device (see SUMMARY.md for steps)
2. Type "approved" to complete Phase 3 plan 01
3. After Phase 3: Plan and execute Phase 4 (User Camp Pin)

---

*State updated: 2026-01-18 after Phase 3 plan 01 checkpoint reached*
