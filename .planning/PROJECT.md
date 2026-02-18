# AfrikaBurn Companion

## What This Is

A mobile app (Android/iOS) that helps AfrikaBurn participants discover theme camps, artworks, performances, and events. Provides offline access to event listings, location directions, event information, and an interactive offline map for navigating the Tankwa Karoo without connectivity. Features an Event Surprise Mode that hides content discovery tabs until the user arrives at the event or the event has started.

## Core Value

Participants can discover and navigate to AfrikaBurn theme camps and artworks without internet connectivity in the remote Tankwa Karoo.

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

- ✓ Browse theme camps, artworks, performances, events, mobile art, vehicles — v2.x
- ✓ Search projects by name, description, artist — v2.x
- ✓ Filter by family-friendly and time of day — v2.x
- ✓ View project details — v2.x
- ✓ Directions to event with GPS coordinates — v2.x
- ✓ Event information and Quaggapedia — v2.x
- ✓ Offline-first architecture with bundled data — v2.x
- ✓ Display offline map of AfrikaBurn event area — v3.0
- ✓ Show theme camp locations on map with markers — v3.0
- ✓ Show artwork locations on map with markers — v3.0
- ✓ Tap marker to view camp/artwork details — v3.0
- ✓ Show user's GPS location on map — v3.0
- ✓ User can pin their own camp location on map — v3.0
- ✓ User's camp pin persists across app restarts — v3.0
- ✓ App has bundled event configuration (start date, location, geofence radius) — v3.1
- ✓ App detects if current date/time is past event start date — v3.1
- ✓ App detects if user is within 20km radius of event location — v3.1
- ✓ Map and Projects tabs hidden when locked (neither condition met) — v3.1
- ✓ Tabs become visible when date OR geofence condition is met — v3.1
- ✓ Unlock state persists permanently once triggered — v3.1
- ✓ Debug feature flag to bypass surprise mode for testing (dev builds only) — v3.1
- ✓ Unit tests for all unlock logic (date, geofence, persistence, feature flag) — v3.1

### Active

<!-- Current scope. Building toward these. -->

**Map enhancements:**
- [ ] Search camps/artworks by name on map
- [ ] Cluster markers when zoomed out
- [ ] Show performance/event markers on map
- [ ] Compass mode (heading-up orientation)

**New features:**
- [ ] Firebase Crashlytics integration
- [ ] Packing list (checklist for event preparation)
- [ ] Emergency info page (contacts, safety, medical)
- [ ] Personal schedule (save/bookmark events)
- [ ] How to volunteer page

**Offline utilities:**
- [ ] Sunrise/sunset times for event dates
- [ ] Water/ice station locations on map
- [ ] Burn schedule countdown (main burn, clan burn, temple burn)
- [ ] Dark mode toggle
- [ ] Offline first-aid guide (heat stroke, dehydration, dust exposure)
- [ ] Offline weather summary (bundled historical averages for the week)
- [ ] Battery saver mode (reduce GPS polling, dim map)

**Community/engagement:**
- [ ] Gift tracker (log gifts given/received)
- [ ] Photo journal/diary (offline daily log with photos, export after event)
- [ ] Leave No Trace checklist (cleanup for strike day)
- [ ] Theme camp reviews/notes (personal notes on visited camps)

**Practical:**
- [ ] Share app (native share sheet with link to landing page with iOS/Android store links, like commute_check ShareService pattern)
- [ ] Share app via QR/NFC (offline sharing for campmates without data)
- [ ] Home screen widget (next bookmarked event or countdown)

**Map-specific:**
- [ ] Toilet/porto locations on map
- [ ] Walking time estimates between points
- [ ] Breadcrumb trail (trace path to find way back at night)

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- Route navigation/directions — Map is for discovery, external maps for routing
- Sharing camp location with others — Local only; defer social features
- Real-time location updates from other users — No backend integration needed
- Continuous background GPS — Severe battery drain in remote environment
- Live friend tracking — Requires network connectivity
- Turn-by-turn navigation — Overkill for event, use external maps
- 3D/AR views — Adds complexity without clear benefit
- User accounts/profiles — Unnecessary friction, local-only is sufficient
- Chat/messaging — Requires network, out of scope for companion app

## Context

**Current State (v3.1):**
- Shipped v3.1 with 15,805 LOC Kotlin
- Tech stack: Kotlin Multiplatform, Compose Multiplatform 1.8.1+, MapLibre Compose 0.11.1, SQLDelight 2.0.2, Koin DI
- Platform support: Android 24+, iOS 14+
- 187 unit tests passing, detekt clean
- Offline map with bundled PMTiles (~574KB Tankwa Karoo terrain)
- Event Surprise Mode with date/geofence unlock and permanent persistence

**Technical Environment:**
- MVVM + Clean Architecture pattern established
- expect/actual patterns for platform-specific implementations (LocationService, DatabaseDriverFactory)
- SQLDelight infrastructure for local persistence (UserCampPin, UnlockState)
- Clock injection pattern for testable time-based logic
- TDD with comprehensive test coverage (55 unlock logic tests)

## Constraints

- **Offline-First**: All map functionality must work without network — critical for Tankwa Karoo
- **Tech Stack**: MapLibre Native SDK — open-source, no API costs
- **Platform**: Expect/actual pattern for platform-specific implementations
- **Storage**: Map tiles budget ~50MB for essential region coverage
- **Battery**: GPS usage must be mindful of battery in remote environment

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| MapLibre over Mapbox | No licensing costs, open-source, sufficient for needs | ✓ Good |
| New 4th tab for Map | Keep Directions screen for GPS/travel info | ✓ Good |
| Camps + Artworks only for v3.0 | Focused scope, extend later | ✓ Good |
| Local-only camp pin | Simpler architecture, no backend changes needed | ✓ Good |
| SQLDelight 2.0.2 | Kotlin 2.x compatibility | ✓ Good |
| Custom LocationService expect/actual | Full control, follows CrashLogger pattern | ✓ Good |
| CircleLayer for markers | Programmatic works better than symbol icons | ✓ Good |
| Dark mode map style | Match app theme (#1a1a2e background) | ✓ Good |
| Balanced power GPS accuracy | Battery conservation critical in desert | ✓ Good |
| 50m threshold for near-pin | Balance targeting ease vs accidental triggers | ✓ Good |
| Orange camp pin color | Distinct from purple/teal/blue markers | ✓ Good |
| Tabs hidden when locked | Cleaner surprise, no teasing users with locked features | ✓ Good |
| Permanent unlock persistence | Once unlocked, stays unlocked forever - better UX | ✓ Good |
| 20km geofence radius | Balance between close enough and GPS accuracy margins | ✓ Good |
| Date OR location unlock | Either condition sufficient - flexible for early arrivals | ✓ Good |
| Africa/Johannesburg timezone | Match event physical location for date calculations | ✓ Good |
| Clock injection pattern | Testable time-based logic without flaky time-dependent tests | ✓ Good |
| Epoch milliseconds storage | Consistent with existing UserCampPin pattern | ✓ Good |
| Welcome message on fresh unlock only | Prevents annoying repeat messages on every launch | ✓ Good |

---
*Last updated: 2026-02-17 after v3.1 milestone*
