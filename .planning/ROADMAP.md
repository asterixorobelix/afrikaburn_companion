# Roadmap: v3.0 Offline Map

**Created:** 2026-01-18
**Core Value:** Participants can discover and navigate to AfrikaBurn theme camps and artworks without internet connectivity.

## Overview

| Phase | Name | Requirements | Status |
|-------|------|--------------|--------|
| 1 | Foundation & Basic Map | MAP-01, MAP-02, MAP-03, MAP-04, NAV-01, NAV-02 | ✓ Complete |
| 2 | Markers & Detail Navigation | MARK-01, MARK-02, MARK-03, MARK-04, MARK-05 | ✓ Complete |
| 3 | User Location | LOC-01, LOC-02, LOC-03 | ✓ Complete |
| 4 | User Camp Pin | PIN-01, PIN-02, PIN-03, PIN-04 | Pending |

**Total:** 4 phases | 18 requirements | 100% coverage

---

## Phase 1: Foundation & Basic Map

**Goal:** Display an offline map in a new Map tab with basic navigation controls.

**Requirements:**
- [x] MAP-01: User sees offline map tiles covering Tankwa Karoo region (infrastructure ready, PMTiles user-provided)
- [x] MAP-02: User can pan the map by dragging
- [x] MAP-03: User can zoom the map by pinch gesture
- [x] MAP-04: User can zoom in by double-tapping
- [x] NAV-01: User sees a Map tab in the bottom navigation
- [x] NAV-02: User can switch between Projects, Map, Directions, and About tabs

**Success Criteria:**
1. Map tab appears in bottom navigation between Projects and Directions
2. Tapping Map tab shows interactive map of Tankwa Karoo region
3. Map works in airplane mode (fully offline)
4. User can pan, pinch-zoom, and double-tap zoom on the map
5. Switching between tabs preserves map state

**Technical Notes:**
- Add MapLibre Compose dependency
- Bundle PMTiles and style assets
- Create MapScreen composable
- Add Map to NavigationDestination
- Handle offline tile loading

---

## Phase 2: Markers & Detail Navigation

**Goal:** Display camp and artwork markers that navigate to detail pages when tapped.

**Requirements:**
- [x] MARK-01: User sees camp locations displayed as markers with distinct icon
- [x] MARK-02: User sees artwork locations displayed as markers with distinct icon
- [x] MARK-03: User can tap a camp marker to view camp details
- [x] MARK-04: User can tap an artwork marker to view artwork details
- [x] MARK-05: User can visually distinguish camp markers from artwork markers

**Success Criteria:**
1. Camp markers appear on map at correct coordinates
2. Artwork markers appear on map at correct coordinates
3. Camp markers use tent/camp icon, artwork markers use art/sculpture icon
4. Tapping camp marker opens existing ProjectDetailScreen for that camp
5. Tapping artwork marker opens existing ProjectDetailScreen for that artwork

**Technical Notes:**
- Extend ProjectItem model with latitude/longitude fields
- Create SymbolLayer for camp markers
- Create SymbolLayer for artwork markers
- Wire marker tap to navigation
- Reuse existing ProjectDetailScreen

---

## Phase 3: User Location

**Goal:** Show the user's GPS location on the map with a "My Location" button.

**Requirements:**
- [x] LOC-01: User sees their current GPS location on the map
- [x] LOC-02: User can tap a button to center the map on their location
- [x] LOC-03: User is prompted to grant location permission when accessing map

**Success Criteria:**
1. Blue dot appears on map at user's GPS position
2. Floating action button centers map on user when tapped
3. Location permission dialog appears on first map access
4. App handles permission denied gracefully (show message, hide location features)
5. GPS tracking stops when leaving map screen (battery conservation)

**Technical Notes:**
- Add Compass library for KMP location services
- Implement LocationService expect/actual
- Add location permission to Android manifest
- Add location usage description to iOS Info.plist
- Implement battery-efficient tracking (5-second intervals)

---

## Phase 4: User Camp Pin

**Goal:** Allow users to mark and persist their own camp location on the map.

**Requirements:**
- [ ] PIN-01: User can place a camp pin by long-pressing on the map
- [ ] PIN-02: User's camp pin persists after closing and reopening the app
- [ ] PIN-03: User can move their camp pin to a new location
- [ ] PIN-04: User can delete their camp pin

**Success Criteria:**
1. Long-pressing map shows confirmation and places pin at that location
2. User camp pin has distinct icon (house/home icon)
3. After app restart, user's camp pin appears in same location
4. Long-pressing while pin exists offers move/delete options
5. Deleting pin removes it from map and database

**Technical Notes:**
- Add SQLDelight for local persistence
- Create user_camp table schema
- Implement UserCampRepository
- Add long-press gesture handler
- Show confirmation dialog before placing/moving/deleting

---

## Phase Dependencies

```
Phase 1 (Foundation)
    │
    ├──▶ Phase 2 (Markers) ──▶ Phase 4 (Camp Pin)
    │
    └──▶ Phase 3 (Location)
```

- Phase 2 depends on Phase 1 (needs map to display markers)
- Phase 3 depends on Phase 1 (needs map to show location)
- Phase 4 depends on Phase 1 (needs map for long-press)
- Phases 2, 3, 4 can be worked in parallel after Phase 1

---

## Risk Mitigation

| Risk | Phase | Mitigation |
|------|-------|------------|
| MapLibre Compose API changes | 1 | Pin to v0.11.1, test before upgrading |
| Offline tiles not working | 1 | Bundle ALL resources locally, test airplane mode early |
| iOS framework issues (Xcode 16.3+) | 1 | Use Kotlin 2.1.21+, test iOS build first |
| Battery drain from GPS | 3 | Use balanced accuracy, stop tracking on background |
| Memory leaks on iOS nav | 2-4 | Proper cleanup in DisposableEffect |

---

*Roadmap created: 2026-01-18*
*Last updated: 2026-01-20 (Phase 3 complete)*
