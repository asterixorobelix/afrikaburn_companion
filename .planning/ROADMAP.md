# Roadmap: AfrikaBurn Companion

## Overview

Continuing from v3.0 Offline Map milestone, this roadmap covers the Event Surprise Mode feature that keeps the Map and Projects tabs hidden until the user is either at the event location or the event has started.

## Milestones

- ✅ **v3.0 Offline Map** - Phases 1-4 (shipped 2026-01-20)
- 🚧 **v3.1 Event Surprise Mode** - Phases 5-8 (in progress)

## Phases

<details>
<summary>✅ v3.0 Offline Map (Phases 1-4) - SHIPPED 2026-01-20</summary>

### Phase 1: Foundation & Basic Map
**Goal**: Display an offline map in a new Map tab with basic navigation controls.
**Plans**: 2 plans

Plans:
- [x] 01-01: Map Infrastructure & Dependencies
- [x] 01-02: Navigation Integration & Gestures

### Phase 2: Markers & Detail Navigation
**Goal**: Display camp and artwork markers that navigate to detail pages when tapped.
**Plans**: 1 plan

Plans:
- [x] 02-01: Interactive Marker Tap to Detail

### Phase 3: User Location
**Goal**: Show the user's GPS location on the map with a "My Location" button.
**Plans**: 1 plan

Plans:
- [x] 03-01: User Location Display & My Location FAB

### Phase 4: User Camp Pin
**Goal**: Allow users to mark and persist their own camp location on the map.
**Plans**: 2 plans

Plans:
- [x] 04-01: SQLDelight Database Infrastructure
- [x] 04-02: Camp Pin UI Implementation

</details>

### 🚧 v3.1 Event Surprise Mode (In Progress)

**Milestone Goal:** Keep Map and Projects tabs hidden until the user is at the AfrikaBurn event (within 20km) or the event has officially started. Preserves the surprise experience for attendees.

#### Phase 5: Event Date Configuration
**Goal**: Store AfrikaBurn event date/location configuration and detect if current date is past event start.
**Depends on**: Phase 4 (SQLDelight infrastructure)
**Requirements**: [SURP-01, SURP-02, SURP-07]
**Success Criteria** (what must be TRUE):
  1. App has bundled event configuration (start date, location coordinates, geofence radius)
  2. App can correctly determine if current date/time is past event start date
  3. Event configuration can be updated annually without code changes (JSON or constants)
  4. Debug feature flag exists to bypass surprise mode (dev builds only)
**Plans**: TBD
**Research**: No

Plans:
- [x] 05-01: Event Configuration Data Source (with unit tests)

#### Phase 6: Geofence Detection
**Goal**: Detect if user is within 20km radius of AfrikaBurn event location.
**Depends on**: Phase 5, Phase 3 (LocationService)
**Requirements**: [SURP-03]
**Success Criteria** (what must be TRUE):
  1. App can calculate distance from user's GPS to event center
  2. App returns true/false for "within 20km geofence" check
  3. Leverages existing LocationService and Haversine formula from v3.0
**Plans**: 1 plan
**Research**: Yes (verify LocationService reuse)

Plans:
- [x] 06-01: Geofence Distance Calculator (with unit tests)

#### Phase 7: Tab Visibility Control
**Goal**: Conditionally show/hide Map and Projects tabs based on unlock conditions.
**Depends on**: Phase 5, Phase 6
**Requirements**: [SURP-04, SURP-05]
**Success Criteria** (what must be TRUE):
  1. Map and Projects tabs are completely hidden when locked
  2. Tabs become visible when either date OR geofence condition is met
  3. Unlock state persists permanently once triggered (SQLDelight)
  4. Navigation still works correctly with reduced tab set
**Plans**: TBD
**Research**: Yes (navigation architecture)

Plans:
- [ ] 07-01: Unlock Condition Manager (with unit tests)
- [ ] 07-02: Navigation Tab Filtering (with unit tests)

#### Phase 8: Polish & Edge Cases
**Goal**: Handle edge cases, add visual feedback, ensure smooth UX, and verify test coverage.
**Depends on**: Phase 7
**Requirements**: [SURP-06, SURP-08]
**Success Criteria** (what must be TRUE):
  1. Location permission denial handled gracefully (date unlock still works)
  2. Timezone handling works correctly for event start date
  3. App transitions smoothly when unlock conditions are met
  4. No way for users to bypass the surprise mode (except feature flag in dev)
  5. Unit tests cover all unlock logic with 80% minimum coverage
**Plans**: TBD
**Research**: No

Plans:
- [ ] 08-01: Edge Cases & Polish

## Progress

**Execution Order:**
Phases execute in numeric order: 5 → 6 → 7 → 8

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Foundation & Basic Map | v3.0 | 2/2 | Complete | 2026-01-18 |
| 2. Markers & Detail Navigation | v3.0 | 1/1 | Complete | 2026-01-19 |
| 3. User Location | v3.0 | 1/1 | Complete | 2026-01-19 |
| 4. User Camp Pin | v3.0 | 2/2 | Complete | 2026-01-20 |
| 5. Event Date Configuration | v3.1 | 1/1 | Complete | 2026-01-22 |
| 6. Geofence Detection | v3.1 | 1/1 | Complete | 2026-01-22 |
| 7. Tab Visibility Control | v3.1 | 0/2 | Not started | - |
| 8. Polish & Edge Cases | v3.1 | 0/1 | Not started | - |
