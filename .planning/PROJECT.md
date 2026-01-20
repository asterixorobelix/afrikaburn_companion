# AfrikaBurn Companion

## What This Is

A mobile app (Android/iOS) that helps AfrikaBurn participants discover theme camps, artworks, performances, and events. Provides offline access to event listings, location directions, event information, and an interactive offline map for navigating the Tankwa Karoo without connectivity.

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

### Active

<!-- Current scope. Building toward these. -->

- [ ] Search camps/artworks by name on map
- [ ] Cluster markers when zoomed out
- [ ] Show performance/event markers on map
- [ ] Compass mode (heading-up orientation)

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- Route navigation/directions — Map is for discovery, external maps for routing
- Sharing camp location with others — Local only; defer social features
- Real-time location updates from other users — No backend integration needed
- MapLibre fallback switching at runtime — Start with MapLibre only
- Continuous background GPS — Severe battery drain in remote environment
- Live friend tracking — Requires network connectivity
- Turn-by-turn navigation — Overkill for event, use external maps
- 3D/AR views — Adds complexity without clear benefit
- User accounts/profiles — Unnecessary friction, local-only is sufficient
- Chat/messaging — Requires network, out of scope for companion app

## Context

**Current State (v3.0):**
- Shipped v3.0 with 10,692 LOC Kotlin
- Tech stack: Kotlin Multiplatform, Compose Multiplatform 1.8.1+, MapLibre Compose 0.11.1, SQLDelight 2.0.2, Koin DI
- Platform support: Android 24+, iOS 14+
- Offline map with bundled PMTiles (~574KB Tankwa Karoo terrain)
- 4 marker types: camps (purple), artworks (teal), user location (blue), camp pin (orange)

**Technical Environment:**
- MVVM + Clean Architecture pattern established
- expect/actual patterns for platform-specific implementations (LocationService, DatabaseDriverFactory)
- SQLDelight infrastructure for local persistence

**Next Milestone Goals (v3.1):**
- Map search functionality (SRCH-01, SRCH-02, SRCH-03)
- Marker clustering (ENH-01)
- Performance/event markers (ENH-05)

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

---
*Last updated: 2026-01-20 after v3.0 milestone*
