# Project State

**Last Updated:** 2026-01-20
**Current Phase:** Phase 4 - User Camp Pin (Complete)

## Current Position

Phase: 4 of 4 (User Camp Pin)
Plan: 2 of 2 in current phase
Status: Complete
Last activity: 2026-01-20 - Completed 04-02-PLAN.md (Camp Pin UI Implementation)

Progress: ██████████ 100%

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-01-18)

**Core value:** Participants can discover and navigate to AfrikaBurn theme camps and artworks without internet connectivity.
**Current focus:** v3.0 Offline Map milestone - Phase 3 complete, ready for Phase 4

## Milestone Progress

**v3.0 Offline Map**

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 1 | Foundation & Basic Map | ✓ Complete | 100% |
| 2 | Markers & Detail Navigation | ✓ Complete | 100% |
| 3 | User Location | ✓ Complete | 100% |
| 4 | User Camp Pin | ✓ Complete | 100% |

**Overall:** 18/18 requirements complete (100%)

## Phase 1 Plans (Complete)

| Plan | Name | Wave | Status | Dependencies |
|------|------|------|--------|--------------|
| 01-01 | Map Infrastructure & Dependencies | 1 | ✓ Complete | None |
| 01-02 | Navigation Integration & Gestures | 2 | ✓ Complete | 01-01 |

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
| 02-01 | Interactive Marker Tap to Detail | 1 | ✓ Complete | 01-02 |

**Requirements completed:**
- [x] MARK-01: User sees camp locations displayed as markers with distinct icon (purple circles)
- [x] MARK-02: User sees artwork locations displayed as markers with distinct icon (teal circles)
- [x] MARK-03: User can tap a camp marker to view camp details
- [x] MARK-04: User can tap an artwork marker to view artwork details
- [x] MARK-05: User can visually distinguish camp markers from artwork markers (color-coded)

## Phase 3 Plans (Complete)

| Plan | Name | Wave | Status | Dependencies |
|------|------|------|--------|--------------|
| 03-01 | User Location & My Location FAB | 1 | ✓ Complete | 02-01 |

**Requirements completed:**
- [x] LOC-01: User sees their current GPS location on the map (blue dot)
- [x] LOC-02: User can tap a button to center the map on their location (My Location FAB)
- [x] LOC-03: User is prompted to grant location permission when accessing map

**Implementation highlights:**
- LocationService expect/actual for Android (FusedLocationProviderClient) and iOS (CLLocationManager)
- LocationPermissionHandler for proper Android runtime permission dialogs
- MapUiState extended with user location fields
- MapViewModel with location tracking and permission handling
- UserLocationMarker (blue dot) and MyLocationButton FAB composables
- MapScreen integration with lifecycle-aware tracking
- Camera animation to user location on FAB tap

## Phase 4 Plans (Complete)

| Plan | Name | Wave | Status | Dependencies |
|------|------|------|--------|--------------|
| 04-01 | SQLDelight Database Infrastructure | 1 | ✓ Complete | 03-01 |
| 04-02 | Camp Pin UI Implementation | 2 | ✓ Complete | 04-01 |

**Requirements completed:**
- [x] PIN-01: User can mark their camp location by long-pressing on the map
- [x] PIN-02: User sees a confirmation dialog before placing their camp pin
- [x] PIN-03: User sees their camp pin displayed as a distinct marker (orange)
- [x] PIN-04: User can move or delete their camp pin

**Implementation highlights (04-01):**
- SQLDelight 2.0.2 configured with AfrikaBurnDatabase generation
- UserCampPin.sq schema with CRUD queries
- DatabaseDriverFactory expect/actual for Android/iOS
- UserCampPinRepository interface and implementation
- Platform-specific Koin modules (DatabaseModule)

**Implementation highlights (04-02):**
- CampPinState (None, Placed) and CampPinDialogState sealed interfaces
- MapViewModel camp pin operations with repository integration
- CampPinDialog composables (Place, Options, Move, Delete)
- Orange camp pin marker layer (distinct from other markers)
- Long-press gesture handling via MapLibre Compose onMapLongClick
- Dialog state management and screen integration

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
| 2026-01-20 | Custom LocationService | expect/actual pattern like CrashLogger, not moko-geo library |
| 2026-01-20 | Balanced power accuracy | Battery conservation critical in desert environment |
| 2026-01-20 | UI-layer permission handling | Android runtime permissions require ActivityResultLauncher |
| 2026-01-20 | Camera animation pattern | Request counter + LaunchedEffect for map camera control |
| 2026-01-20 | SQLDelight 2.0.2 | Required for Kotlin 2.x compatibility |
| 2026-01-20 | expect/actual for Koin modules | Android needs Context for DB driver, iOS doesn't |
| 2026-01-20 | kotlinx-datetime Clock | Multiplatform time API for timestamps |
| 2026-01-20 | MapLibre onMapLongClick | Native parameter for long-press, not custom gesture |
| 2026-01-20 | Haversine with toRadians() | Multiplatform distance calculation via kotlin.math.PI |
| 2026-01-20 | 50m threshold for near-pin | Balance targeting ease vs accidental triggers |
| 2026-01-20 | Orange (#FF9800) camp pin | Distinct from purple camps, teal artworks, blue user |

## Blockers

None.

## Session Continuity

Last session: 2026-01-20
Stopped at: Completed 04-02-PLAN.md - v3.0 Offline Map milestone complete
Resume file: None

## Next Steps

1. Human verification checkpoint for v3.0 Offline Map milestone
2. Test camp pin functionality on device (long-press, place, move, delete)
3. Plan next milestone (v3.1 or feature enhancements)

---

*State updated: 2026-01-20 after 04-02 plan complete - v3.0 Offline Map milestone finished*
