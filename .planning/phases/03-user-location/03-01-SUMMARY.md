# Phase 03-01 Summary: User Location Display

## Status: CHECKPOINT - Awaiting Human Verification

## What Was Built

### User Location Feature
Complete implementation of GPS location display on the map with:
- Blue dot marker showing user's current GPS position
- My Location FAB (Floating Action Button) to center map on user
- Proper permission handling for both Android and iOS
- Battery-conscious tracking that stops when leaving map screen

## Files Created/Modified

### New Files
| File | Purpose |
|------|---------|
| `mobile/.../platform/LocationService.kt` | Cross-platform location interface with LocationData, PermissionState |
| `mobile/.../platform/LocationService.android.kt` | Android implementation using FusedLocationProviderClient |
| `mobile/.../platform/LocationService.ios.kt` | iOS implementation using CLLocationManager |
| `mobile/.../di/LocationModule.kt` | Koin dependency injection module for LocationService |
| `mobile/.../ui/screens/map/UserLocationMarker.kt` | Blue dot composable (for preview reference) |
| `mobile/.../ui/screens/map/MyLocationButton.kt` | FAB composable for centering on location |

### Modified Files
| File | Changes |
|------|---------|
| `mobile/gradle/libs.versions.toml` | Added Play Services Location dependency |
| `mobile/composeApp/build.gradle.kts` | Added location dependency and detekt JVM target fix |
| `mobile/composeApp/src/androidMain/AndroidManifest.xml` | Added ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions |
| `mobile/iosApp/iosApp/Info.plist` | Added NSLocationWhenInUseUsageDescription |
| `mobile/.../di/AppModule.kt` | Registered locationModule |
| `mobile/.../presentation/map/MapUiState.kt` | Added userLatitude, userLongitude, locationPermissionState, isTrackingLocation fields |
| `mobile/.../presentation/map/MapViewModel.kt` | Added location tracking methods and LocationService injection |
| `mobile/.../di/PresentationModule.kt` | Updated MapViewModel factory to inject LocationService |
| `mobile/.../ui/screens/map/MapScreen.kt` | Integrated location marker, FAB, and lifecycle management |

## Architecture Decisions

### Decision: Custom expect/actual vs moko-geo Library
**Choice**: Custom expect/actual implementation
**Rationale**:
- Follows established pattern from CrashLogger
- Full control over implementation details
- Avoids potential library compatibility issues
- Uses native platform APIs directly (FusedLocationProviderClient on Android, CLLocationManager on iOS)

### Decision: Balanced Power Accuracy
**Choice**: Priority.PRIORITY_BALANCED_POWER_ACCURACY (Android), kCLLocationAccuracyHundredMeters (iOS)
**Rationale**: Battery conservation is critical in the desert environment with limited charging options

### Decision: When-in-Use Permission Only
**Choice**: Only request location while app is in foreground
**Rationale**: Background location not needed for map navigation; saves battery and respects user privacy

## Verification Checklist

- [x] `./gradlew :composeApp:assembleDebug` succeeds
- [x] `./gradlew :composeApp:testDebugUnitTest` passes all tests
- [x] `./gradlew detekt` passes (only pre-existing issues remain)
- [ ] User location dot appears on map when permission granted (NEEDS VERIFICATION)
- [ ] My Location FAB centers map on user (NEEDS VERIFICATION)
- [ ] Location tracking stops when leaving map screen (NEEDS VERIFICATION)

## Commits

1. `feat(03-01): add location library dependency`
2. `feat(03-01): add platform-specific location permissions`
3. `feat(03-01): create LocationService expect/actual interface`
4. `feat(03-01): create LocationModule and register in DI`
5. `feat(03-01): extend MapUiState with user location fields`
6. `feat(03-01): enhance MapViewModel with location tracking`
7. `feat(03-01): create UserLocationMarker composable`
8. `feat(03-01): create MyLocationButton FAB composable`
9. `feat(03-01): integrate location features into MapScreen`
10. `chore(03-01): fix detekt issues and configure jvm target`

## How to Verify (Human Checkpoint)

1. Run: `./gradlew :composeApp:installDebug` (or run from Android Studio)
2. Open the app and tap the Map tab
3. **Permission dialog**: Should see "Allow AfrikaBurn to access your location?" prompt
4. Tap "Allow" (or "While using the app")
5. **Blue dot**: After permission granted, a blue dot should appear at your current location
6. **My Location FAB**: A floating button should appear in bottom-right corner
7. Pan the map away from your location
8. Tap the My Location FAB - map should center back on the blue dot
9. Navigate away from Map tab (to Projects, Directions, or About)
10. Return to Map tab - location should still work, blue dot visible
11. **Denied permission test**: If possible, test with location permission denied - FAB should be hidden

## Known Limitations

1. iOS implementation uses `@Suppress("CONFLICTING_OVERLOADS")` for delegate callback - works but not ideal long-term
2. No heading/bearing indicator (just position dot)
3. No accuracy circle visualization
4. No offline location fallback

## Next Steps (After Verification)

- Phase 03-02: Add accuracy circle around user location
- Phase 03-03: Add compass/heading indicator
- Phase 03-04: Add "find me" animation when centering
