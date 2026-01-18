# Plan 02-01 Summary: Interactive Marker Tap to Detail

**Status**: COMPLETE
**Duration**: ~45 minutes
**Commits**: 6

## Objective

Enable users to tap map markers and navigate to project detail screens, linking map exploration with project information.

## Tasks Completed

### Task 1: Add coordinate fields to ProjectItem model
- Added nullable `latitude` and `longitude` fields with `@SerialName` annotations
- Added `hasCoordinates` computed property for map display checks
- Nullable fields maintain backwards compatibility with existing JSON data

### Task 2: Update mock-locations.geojson with project codes
- Added `code` property to all 16 features (8 camps, 8 artworks)
- Codes match WTFThemeCamps.json and WTFArtworks.json data
- Enables marker tap to project matching via code lookup

### Task 3: Extend MapUiState with projects list
- Added `projects: List<ProjectItem>` parameter to `MapUiState.Success`
- Imported `ProjectItem` model
- Default to empty list for backwards compatibility

### Task 4: Enhance MapViewModel with project loading and code lookup
- Load camps and artworks from JSON resources on init
- Added `findProjectByCode` method supporting comma-separated codes
- Updated Success state to include loaded projects
- Silent error handling maintains map display even if data load fails

### Task 5: Add marker tap handling to MapScreen
- Added `onProjectClick: (ProjectItem) -> Unit` callback parameter
- Implemented `onMapClick` handler with `queryRenderedFeatures`
- Extracts `code` property from tapped GeoJSON features
- Returns `ClickResult.Consume` when marker tapped, `Pass` otherwise

### Task 6: Wire MapScreen to navigation in App.kt
- Connected `onProjectClick` to existing navigation pattern
- Reuses `selectedProject` state holder
- Navigates to `PROJECT_DETAIL_ROUTE` on marker tap

### Task 7: Run full build and tests
- Debug APK builds successfully
- All 13 unit tests pass
- Note: Detekt has pre-existing JVM target issue (not related to these changes)

## Technical Decisions

1. **Code-based matching**: Used project codes to link GeoJSON features to ProjectItem data rather than name matching, which handles cases like comma-separated codes ("dis, ele")

2. **Silent data loading failure**: ViewModel shows map with empty projects list rather than error state if JSON loading fails - maintains usability

3. **ClickResult API**: Used `org.maplibre.compose.util.ClickResult` for proper event consumption

4. **Reused navigation pattern**: Marker taps use same `selectedProject` state and route as ProjectsScreen list taps

## Commits

```
fe657cf feat(02-01): add coordinate fields to ProjectItem model
0176c07 feat(02-01): add project codes to mock-locations GeoJSON
8dec2fc feat(02-01): extend MapUiState with projects list
b778dbd feat(02-01): enhance MapViewModel with project loading and code lookup
b2146a6 feat(02-01): add marker tap handling to MapScreen
2d202d1 feat(02-01): wire MapScreen marker taps to navigation
```

## Files Modified

- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/models/ProjectItem.kt`
- `mobile/composeApp/src/commonMain/composeResources/files/maps/mock-locations.geojson`
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/map/MapUiState.kt`
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/map/MapViewModel.kt`
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/screens/map/MapScreen.kt`
- `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt`

## Verification

- [x] Build succeeds: `./gradlew :composeApp:assembleDebug`
- [x] Tests pass: `./gradlew :composeApp:testDebugUnitTest` (13 tests)
- [ ] Human verification: Run app, tap markers, verify navigation to detail screen

## Notes

The detekt task fails with "Invalid value (23) passed to --jvm-target" - this is a pre-existing infrastructure issue where the host JVM version (23) is not supported by detekt 1.23.8. This is unrelated to plan 02-01 changes.
