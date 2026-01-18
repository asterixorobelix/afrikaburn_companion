# Project State

**Last Updated:** 2026-01-18
**Current Phase:** Phase 2 - Markers & Detail Navigation (Plan 01 Complete)

## Current Position

Phase: 2 of 4 (Markers & Detail Navigation)
Plan: 1 of 1 in current phase (COMPLETE)
Status: Ready for human verification checkpoint
Last activity: 2026-01-18 - Executed Plan 02-01 (Interactive Marker Tap to Detail)

Progress: ████████░░ 50%

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-01-18)

**Core value:** Participants can discover and navigate to AfrikaBurn theme camps and artworks without internet connectivity.
**Current focus:** v3.0 Offline Map milestone - Phase 2 Plan 01 complete, pending human verification

## Milestone Progress

**v3.0 Offline Map**

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 1 | Foundation & Basic Map | ✓ Complete | 100% |
| 2 | Markers & Detail Navigation | ◐ In Progress | 100% (pending verification) |
| 3 | User Location | ○ Pending | 0% |
| 4 | User Camp Pin | ○ Pending | 0% |

**Overall:** 11/18 requirements complete (61%)

## Phase 1 Plans (Complete)

| Plan | Name | Wave | Status | Dependencies |
|------|------|------|--------|--------------|
| 01-01 | Map Infrastructure & Dependencies | 1 | ✓ Complete | None |
| 01-02 | Navigation Integration & Gestures | 2 | ✓ Complete | 01-01 |

**Requirements completed:**
- [x] MAP-01: Offline map tiles covering Tankwa Karoo region (infrastructure ready, PMTiles user-provided)
- [x] MAP-02: Pan map by dragging
- [x] MAP-03: Zoom map by pinch gesture
- [x] MAP-04: Zoom in by double-tapping
- [x] NAV-01: Map tab in bottom navigation
- [x] NAV-02: Switch between Projects, Map, Directions, About tabs

## Phase 2 Plans (In Progress)

| Plan | Name | Wave | Status | Dependencies |
|------|------|------|--------|--------------|
| 02-01 | Interactive Marker Tap to Detail | 1 | ✓ Code Complete | 01-02 |

**Requirements completed (pending human verification):**
- [x] MARK-01: User sees camp locations displayed as markers with distinct icon (purple circles)
- [x] MARK-02: User sees artwork locations displayed as markers with distinct icon (teal circles)
- [x] MARK-03: User can tap a camp marker to view camp details
- [x] MARK-04: User can tap an artwork marker to view artwork details
- [x] MARK-05: User can visually distinguish camp markers from artwork markers (color-coded)

## Key Decisions Log

| Date | Decision | Context |
|------|----------|---------|
| 2026-01-18 | MapLibre Compose v0.11.1 | Official KMP support, no licensing costs |
| 2026-01-18 | Bundled PMTiles (~50MB) | Guaranteed offline, no download UX needed |
| 2026-01-18 | SQLDelight for camp pin | Already in project infra |
| 2026-01-18 | Defer search to v3.1 | Reduce scope for v3.0 |
| 2026-01-18 | Camps + artworks only | Performances/events deferred |
| 2026-01-18 | spatialk.geojson.Position | MapLibre uses this for coordinates (transitive dep) |
| 2026-01-18 | CircleLayer for markers | Programmatic markers work better than style.json inline GeoJSON |
| 2026-01-18 | Dark mode map style | Background #1a1a2e to match app theme |
| 2026-01-18 | Code-based marker matching | ProjectItem.code used to link GeoJSON features to project data |
| 2026-01-18 | ClickResult.Consume pattern | Marker taps consume click, non-marker taps pass through |
| 2026-01-18 | Reuse navigation pattern | Marker taps use same selectedProject + PROJECT_DETAIL_ROUTE as list |

## Blockers

**User Action Required (before map shows terrain/roads):**
- Download/generate PMTiles file for Tankwa Karoo region (~20-50MB, zoom 10-16)
- Place at: `mobile/composeApp/src/commonMain/composeResources/files/maps/tankwa-karoo.pmtiles`
- Sources: Protomaps, MapTiler Data, or custom extraction

**Known Issue (pre-existing, not blocking):**
- Detekt task fails with "Invalid value (23) passed to --jvm-target"
- Host JVM 23 not supported by detekt 1.23.8
- Build and tests work; only detekt affected

## Session Continuity

Last session: 2026-01-18
Stopped at: Plan 02-01 code complete, checkpoint pending
Resume file: None

## Human Verification Checkpoint

**Required before proceeding to Phase 3:**

1. Run app on device/emulator
2. Navigate to Map tab
3. Verify purple markers show for camps
4. Verify teal markers show for artworks
5. Tap a marker and verify navigation to detail screen

**Build command:** `./gradlew :composeApp:installDebug`

## Next Steps

1. Complete human verification checkpoint (tap markers, verify navigation)
2. After verification: Proceed to Phase 3 (User Location)

---

*State updated: 2026-01-18 after Plan 02-01 execution complete*
