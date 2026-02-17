# Project Milestones: AfrikaBurn Companion

## v3.1 Event Surprise Mode (Shipped: 2026-02-17)

**Delivered:** Event Surprise Mode that hides Map and Projects tabs until the user is at AfrikaBurn (within 20km geofence) or the event has officially started, with permanent unlock persistence.

**Phases completed:** 5-8 (4 phases, 5 plans, 18 tasks)

**Key accomplishments:**

- EventConfig + EventDateService with Clock injection for testable date-based unlock detection
- Haversine-based geofence detection service (20km radius around AfrikaBurn)
- UnlockConditionManager combining date, geofence, and SQLDelight persistence (OR condition)
- Navigation tab filtering with conditional visibility and welcome snackbar on first unlock
- Timezone boundary (SAST) and permission denial edge case test coverage (55 unlock logic tests)

**Stats:**

- 83 files modified
- 15,805 lines of Kotlin (current total)
- 4 phases, 5 plans, 18 tasks
- 27 days from start to ship (2026-01-22 -> 2026-02-17)
- 187 total tests passing

**Git range:** `test(05-01)` -> `test(08-01)`

---

## v3.0 Offline Map (Shipped: 2026-01-20)

**Delivered:** Interactive offline map for AfrikaBurn with camp/artwork markers, GPS location display, and user camp pin persistence.

**Phases completed:** 1-4 (6 plans total)

**Key accomplishments:**

- MapLibre Compose v0.11.1 integration with bundled PMTiles for Tankwa Karoo terrain
- Color-coded markers: purple camps, teal artworks, tap-to-detail navigation
- GPS location display with blue dot and My Location FAB
- User camp pin with long-press placement, persistence via SQLDelight
- Cross-platform implementation with expect/actual patterns (Android/iOS)
- Full offline functionality for remote desert environment

**Stats:**

- 47 files created/modified
- 10,692 lines of Kotlin
- 4 phases, 6 plans, 18 requirements
- 3 days from start to ship (2026-01-18 -> 2026-01-20)

**Git range:** `feat(01-01)` -> `feat(map): improve legend UX`

---

*Milestone history for AfrikaBurn Companion*
