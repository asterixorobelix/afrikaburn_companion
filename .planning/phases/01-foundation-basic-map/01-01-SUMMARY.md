---
phase: 01-foundation-basic-map
plan: 01
subsystem: ui
tags: [maplibre, compose-multiplatform, offline-maps, mvvm]

# Dependency graph
requires: []
provides:
  - MapLibre Compose dependencies (v0.11.1)
  - Offline map style configuration (style.json)
  - MapUiState sealed interface
  - MapViewModel with camera state management
  - MapScreen composable
  - Koin DI integration for MapViewModel
affects: [01-02, phase-2-markers, phase-3-location, phase-4-camp-pin]

# Tech tracking
tech-stack:
  added:
    - org.maplibre.compose:maplibre-compose:0.11.1
    - org.maplibre.compose:maplibre-compose-material3:0.11.1
    - io.github.dellisd.spatialk (transitive - geojson)
  patterns:
    - MapUiState sealed interface for Loading/Success/Error states
    - StateFlow camera position management
    - MaplibreMap with BaseStyle.Uri for asset loading

key-files:
  created:
    - mobile/gradle/libs.versions.toml (MapLibre entries)
    - mobile/composeApp/src/commonMain/composeResources/files/maps/style.json
    - mobile/composeApp/src/commonMain/composeResources/files/maps/README.md
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/map/MapUiState.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/map/MapViewModel.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/screens/map/MapScreen.kt
  modified:
    - mobile/composeApp/build.gradle.kts
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/PresentationModule.kt
    - mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/KoinCompose.kt

key-decisions:
  - "Use org.maplibre.compose package (official KMP library)"
  - "Pin MapLibre Compose to v0.11.1 for API stability"
  - "Position from spatialk.geojson (library transitive dep)"

patterns-established:
  - "Map screen MVVM pattern: MapUiState + MapViewModel + MapScreen"
  - "Koin helper pattern: koinMapViewModel() composable function"

# Metrics
duration: 25min
completed: 2026-01-18
---

# Phase 01 Plan 01: Map Infrastructure & Dependencies Summary

**MapLibre Compose v0.11.1 integrated with offline style.json, MapViewModel MVVM pattern, and Koin DI - build compiles successfully**

## Performance

- **Duration:** 25 min
- **Started:** 2026-01-18T17:20:00Z
- **Completed:** 2026-01-18T17:45:00Z
- **Tasks:** 9
- **Files modified:** 9

## Accomplishments

- MapLibre Compose dependencies added to version catalog and build.gradle.kts
- Offline map style (style.json) created with OpenMapTiles schema for Tankwa Karoo
- MapUiState sealed interface with Loading/Success/Error states
- MapViewModel with camera position StateFlow management
- MapScreen composable with MaplibreMap integration
- Koin DI registration for MapViewModel

## Task Commits

Each task was committed atomically:

1. **Task 1: Add MapLibre Compose dependencies to version catalog** - `2340cbf` (feat)
2. **Task 2: Add MapLibre dependencies to build.gradle.kts** - `66aa1e6` (feat)
3. **Task 3: Create map resources directory and style.json** - `27564a9` (feat)
4. **Task 4: Create MapUiState sealed class** - `2692af6` (feat)
5. **Task 5: Create MapViewModel** - `2e4e9bc` (feat)
6. **Task 6: Create MapScreen composable** - `bcace13` (feat)
7. **Task 7: Register MapViewModel in Koin module** - `81e1bb2` (feat)
8. **Task 8: Add Koin injection helper** - `64d0d75` (feat)
9. **Task 9: Fix imports and verify build** - `1cce7a8` (fix)

## Files Created/Modified

- `mobile/gradle/libs.versions.toml` - Added maplibre-compose version and library entries
- `mobile/composeApp/build.gradle.kts` - Added maplibre-compose implementations to commonMain
- `mobile/composeApp/src/commonMain/composeResources/files/maps/style.json` - Offline map style with OpenMapTiles layers
- `mobile/composeApp/src/commonMain/composeResources/files/maps/README.md` - Documentation for PMTiles requirement
- `mobile/composeApp/src/commonMain/kotlin/.../presentation/map/MapUiState.kt` - Sealed interface for map states
- `mobile/composeApp/src/commonMain/kotlin/.../presentation/map/MapViewModel.kt` - ViewModel with camera state
- `mobile/composeApp/src/commonMain/kotlin/.../ui/screens/map/MapScreen.kt` - Main map composable
- `mobile/composeApp/src/commonMain/kotlin/.../di/PresentationModule.kt` - Koin factory for MapViewModel
- `mobile/composeApp/src/commonMain/kotlin/.../di/KoinCompose.kt` - koinMapViewModel() helper

## Decisions Made

1. **Package imports from maplibre-compose**: Used correct package structure:
   - `org.maplibre.compose.map.MaplibreMap`
   - `org.maplibre.compose.camera.CameraPosition`
   - `org.maplibre.compose.camera.rememberCameraState`
   - `org.maplibre.compose.style.BaseStyle`
   - `io.github.dellisd.spatialk.geojson.Position` (transitive dependency)

2. **Style.json structure**: Created minimal desert-optimized style with:
   - PMTiles source for offline tiles
   - Background, landuse, water, road, and place label layers
   - Desert-appropriate color scheme

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed MapLibre Compose import paths**
- **Found during:** Task 9 (Build verification)
- **Issue:** Initial imports used incorrect package paths (flat org.maplibre.compose instead of subpackages)
- **Fix:** Updated to correct paths: .map.MaplibreMap, .camera.CameraPosition, .style.BaseStyle
- **Files modified:** MapScreen.kt
- **Verification:** Build compiles successfully
- **Committed in:** 1cce7a8

---

**Total deviations:** 1 auto-fixed (blocking)
**Impact on plan:** Import fix was necessary for compilation. No scope creep.

## Issues Encountered

None - plan executed with one deviation to fix incorrect import paths.

## User Setup Required

**External services require manual configuration.** See map tiles requirement:

- **PMTiles file needed:** User must download/generate Tankwa Karoo PMTiles file
- **Location:** `mobile/composeApp/src/commonMain/composeResources/files/maps/tankwa-karoo.pmtiles`
- **Size:** 20-50MB covering zoom levels 10-16
- **Sources:** Protomaps, MapTiler Data, or custom extraction

See `mobile/composeApp/src/commonMain/composeResources/files/maps/README.md` for details.

## Next Phase Readiness

- Map infrastructure complete, ready for navigation integration in Plan 01-02
- MapScreen can be added to NavigationDestination
- Build compiles successfully with MapLibre Compose dependencies
- Map will display once PMTiles file is provided by user

---
*Phase: 01-foundation-basic-map*
*Completed: 2026-01-18*
