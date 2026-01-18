# AfrikaBurn Companion

## What This Is

A mobile app (Android/iOS) that helps AfrikaBurn participants discover theme camps, artworks, performances, and events. Currently provides offline access to event listings, location directions, and event information. The v3.0 milestone adds interactive offline mapping to visualize camp and artwork locations.

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

### Active

<!-- Current scope. Building toward these. -->

- [ ] Display offline map of AfrikaBurn event area
- [ ] Show theme camp locations on map with markers
- [ ] Show artwork locations on map with markers
- [ ] Tap marker to view camp/artwork details
- [ ] Search camps/artworks by name on map
- [ ] Show user's GPS location on map
- [ ] User can pin their own camp location on map
- [ ] User's camp pin persists across app restarts

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- Performance/event markers on map — Focus v3.0 on camps + artworks; extend in v3.1+
- Route navigation/directions — Map is for discovery, external maps for routing
- Sharing camp location with others — Local only; defer social features
- Real-time location updates from other users — No backend integration needed
- MapLibre fallback switching at runtime — Start with MapLibre only
- Dust storm mode / night mode styling — Nice-to-have for future milestone

## Context

**Technical Environment:**
- Kotlin Multiplatform with Compose Multiplatform (1.9.0)
- MVVM + Clean Architecture pattern established
- Koin for dependency injection
- SQLDelight infrastructure available (not actively used yet)
- Existing OFFLINE_MAP_IMPLEMENTATION_PLAN.md provides architecture guidance

**Data:**
- Camp/artwork JSON files will include latitude/longitude coordinates
- User camp pin stored in local SQLDelight database
- Map tiles must be bundled or cached for offline use (Tankwa Karoo region)

**Platform:**
- Android 24+ (min SDK)
- iOS 14+
- Location permissions required for GPS functionality

## Constraints

- **Offline-First**: All map functionality must work without network — critical for Tankwa Karoo
- **Tech Stack**: MapLibre Native SDK — open-source, no API costs
- **Platform**: Expect/actual pattern for platform-specific map implementations
- **Storage**: Map tiles budget ~50MB for essential region coverage
- **Battery**: GPS usage must be mindful of battery in remote environment

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| MapLibre over Mapbox | No licensing costs, open-source, sufficient for needs | — Pending |
| New 4th tab for Map | Keep Directions screen for GPS/travel info | — Pending |
| Camps + Artworks only | Focused scope for v3.0, extend later | — Pending |
| Local-only camp pin | Simpler architecture, no backend changes needed | — Pending |
| SQLDelight for user data | Infrastructure already in project | — Pending |

---
*Last updated: 2026-01-18 after milestone v3.0 initialization*
