# Project Milestones: AfrikaBurn Companion

## v3.1 Event Surprise Mode (In Progress)

**Goal:** Keep Map and Projects tabs hidden until user is at AfrikaBurn event (within 20km) or event has officially started. Preserves the surprise experience for attendees.

**Phases:** 5-8 (4 phases, 5 plans)

**Key features:**
- Event configuration with start date and GPS coordinates
- Geofence detection using existing LocationService
- Conditional tab visibility (completely hidden when locked)
- Permanent unlock persistence once conditions met

**Started:** 2026-01-22

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
- 3 days from start to ship (2026-01-18 → 2026-01-20)

**Git range:** `feat(01-01)` → `feat(map): improve legend UX`

**What's next:** v3.1 with search functionality, marker clustering, and enhanced features

---

*Milestone history for AfrikaBurn Companion*
