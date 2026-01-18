# Requirements: AfrikaBurn Companion

**Defined:** 2026-01-18
**Core Value:** Participants can discover and navigate to AfrikaBurn theme camps and artworks without internet connectivity.

## v1 Requirements

Requirements for v3.0 release. Each maps to roadmap phases.

### Map Display

- [ ] **MAP-01**: User sees offline map tiles covering Tankwa Karoo region
- [ ] **MAP-02**: User can pan the map by dragging
- [ ] **MAP-03**: User can zoom the map by pinch gesture
- [ ] **MAP-04**: User can zoom in by double-tapping

### Markers

- [ ] **MARK-01**: User sees camp locations displayed as markers with distinct icon
- [ ] **MARK-02**: User sees artwork locations displayed as markers with distinct icon
- [ ] **MARK-03**: User can tap a camp marker to view camp details
- [ ] **MARK-04**: User can tap an artwork marker to view artwork details
- [ ] **MARK-05**: User can visually distinguish camp markers from artwork markers

### User Location

- [ ] **LOC-01**: User sees their current GPS location on the map
- [ ] **LOC-02**: User can tap a button to center the map on their location
- [ ] **LOC-03**: User is prompted to grant location permission when accessing map

### User Camp Pin

- [ ] **PIN-01**: User can place a camp pin by long-pressing on the map
- [ ] **PIN-02**: User's camp pin persists after closing and reopening the app
- [ ] **PIN-03**: User can move their camp pin to a new location
- [ ] **PIN-04**: User can delete their camp pin

### Navigation

- [ ] **NAV-01**: User sees a Map tab in the bottom navigation
- [ ] **NAV-02**: User can switch between Projects, Map, Directions, and About tabs

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Search

- **SRCH-01**: User can search camps and artworks by name on the map
- **SRCH-02**: User sees instant search results as they type
- **SRCH-03**: User can tap a search result to center the map on that location

### Enhanced Features

- **ENH-01**: User sees markers clustered when zoomed out
- **ENH-02**: User can enable compass mode (heading-up orientation)
- **ENH-03**: User can record their path for retracing steps
- **ENH-04**: User can add notes to their camp pin
- **ENH-05**: User sees performance/event markers on the map

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Live friend tracking | Requires network connectivity |
| Turn-by-turn navigation | Overkill for event, use external maps |
| 3D/AR views | Adds complexity without clear benefit |
| Continuous background GPS | Severe battery drain in remote environment |
| User accounts/profiles | Unnecessary friction, local-only is sufficient |
| Chat/messaging | Requires network, out of scope for companion app |
| Tile download management | Pre-bundled tiles simpler, guaranteed to work |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| MAP-01 | Phase 1 | Pending |
| MAP-02 | Phase 1 | Pending |
| MAP-03 | Phase 1 | Pending |
| MAP-04 | Phase 1 | Pending |
| NAV-01 | Phase 1 | Pending |
| NAV-02 | Phase 1 | Pending |
| MARK-01 | Phase 2 | Pending |
| MARK-02 | Phase 2 | Pending |
| MARK-03 | Phase 2 | Pending |
| MARK-04 | Phase 2 | Pending |
| MARK-05 | Phase 2 | Pending |
| LOC-01 | Phase 3 | Pending |
| LOC-02 | Phase 3 | Pending |
| LOC-03 | Phase 3 | Pending |
| PIN-01 | Phase 4 | Pending |
| PIN-02 | Phase 4 | Pending |
| PIN-03 | Phase 4 | Pending |
| PIN-04 | Phase 4 | Pending |

**Coverage:**
- v1 requirements: 18 total
- Mapped to phases: 18 ✓
- Unmapped: 0

---
*Requirements defined: 2026-01-18*
*Last updated: 2026-01-18 after initial definition*
