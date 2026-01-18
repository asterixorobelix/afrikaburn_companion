# Pitfalls Research: Offline Maps

**Research Date:** 2026-01-18
**Milestone:** v3.0 Offline Map

---

## Critical Pitfalls

### 1. MapLibre Compose Library API Instability

**Description**: MapLibre Compose for Kotlin Multiplatform is relatively new (first released late 2024). A large subset of MapLibre's features are supported, but the full breadth of the MapLibre SDKs is not yet covered. API stability is not guaranteed as they're still exploring how best to express an interactive map API in Compose.

**Warning Signs**:
- Breaking changes between minor versions
- Features documented in MapLibre Native not available in Compose wrapper
- Inconsistent behavior between Android and iOS implementations

**Impact**: HIGH - May require significant code changes during development

**Prevention**:
- Pin to a specific MapLibre Compose version
- Test thoroughly on both platforms before upgrading
- Keep a migration strategy ready for API changes
- Monitor the [MapLibre Compose roadmap](https://maplibre.org/maplibre-compose/roadmap/)

**Sources**:
- [MapLibre Compose Roadmap](https://maplibre.org/maplibre-compose/roadmap/)
- [MapLibre Native Compose Multiplatform Library Issue](https://github.com/maplibre/maplibre-native/issues/2638)

---

### 2. Duplicate Class Conflicts (Android)

**Description**: MapLibre was forked from Mapbox, and some package names were not updated from "com.mapbox.*" to "org.maplibre.*", specifically for plugins. Both `org.maplibre.gl:android-sdk-geojson` and `com.mapbox.mapboxsdk:mapbox-sdk-geojson` use the same class names, causing duplicate class errors.

**Warning Signs**:
- Build failures with "Duplicate class" errors
- Runtime crashes with class loading issues
- Conflicts when adding other mapping-related dependencies

**Impact**: CRITICAL - Blocks Android builds completely

**Prevention**:
- Audit all transitive dependencies for Mapbox/MapLibre conflicts
- Use Gradle dependency resolution strategies to exclude duplicates
- Avoid using both MapLibre and Mapbox libraries in the same project
- Check plugin dependencies carefully before adding

**Sources**:
- [MapLibre Native Compose Multiplatform Discussion](https://github.com/maplibre/maplibre-native/issues/2638)

---

### 3. iOS Framework Not Found (Xcode 16.3+)

**Description**: Users with Xcode 16.3+ encounter issues where the MapLibre framework is not found, resulting in linker warnings like "Could not find or use auto-linked framework 'MapLibre'" and undefined symbols errors for architecture arm64.

**Warning Signs**:
- Linker errors after Xcode update
- "Framework not found" errors in Xcode
- Build succeeds on older Xcode versions but fails on newer ones

**Impact**: CRITICAL - Blocks iOS builds

**Prevention**:
- Update Kotlin to version 2.1.21+ to resolve Xcode compatibility issues
- Verify CocoaPods integration is using correct paths
- Always open `.xcworkspace`, never `.xcodeproj` when using CocoaPods
- Test builds after every Xcode update

**Sources**:
- [MapLibre Native Issues](https://github.com/maplibre/maplibre-native/issues/2638)
- [Kotlin Slack Multiplatform Channel](https://slack-chats.kotlinlang.org/t/28525432/i-am-trying-to-use-cocoapods-gradle-plugin-to-get-maplibre-w)

---

### 4. Offline Tiles Not Displaying When Offline

**Description**: Even after downloading offline regions, tiles may not display when the device is truly offline. This can happen when the style requires online resources (fonts/glyphs, sprites) or when cache headers prevent offline use.

**Warning Signs**:
- Map works during download but blank when offline
- Tiles visible online but missing offline
- Style loads but no features render

**Impact**: CRITICAL - Core offline functionality broken in the Tankwa Karoo

**Prevention**:
- Download ALL required resources: tiles, style JSON, fonts/glyphs, sprites
- Host style resources locally as bundled assets
- Use local asset paths in style: `"sprite": "asset://sprites/bright-v8"`, `"glyphs": "asset://glyphs/{fontstack}/{range}.pbf"`
- Test in airplane mode BEFORE going to the event
- Reset database and clear ambient cache to troubleshoot

**Sources**:
- [Tiles Offline Regions do not display while offline](https://github.com/maplibre/maplibre-native/issues/633)
- [How to display offline maps using Maplibre on Android](https://medium.com/@ty2/how-to-display-offline-maps-using-maplibre-mapbox-39ad0f3c7543)

---

### 5. Memory Crashes (Out of Memory)

**Description**: Since MapLibre 11.8.0+, there are reports of frequent crashes caused by system out of memory, particularly during rendering. The crashes occur on the RenderThread with `java.lang.Error: std::bad_alloc` errors.

**Warning Signs**:
- App crashes during map interactions (zooming, panning)
- Crashes more frequent on lower-end devices
- Memory warnings before crash
- Crashes in native layer (RenderThread)

**Impact**: CRITICAL - App crashes lose user data and trust

**Prevention**:
- Test on low-memory devices (Android Go, older iPhones)
- Limit zoom levels in offline regions (fewer tiles = less memory)
- Implement proper cleanup when map is disposed
- Consider downgrading MapLibre version if crashes occur
- Monitor memory usage during development

**Sources**:
- [Very frequent crashes caused by system out of memory](https://github.com/maplibre/maplibre-native/issues/3309)
- [Memory leak on map.remove()](https://github.com/maplibre/maplibre-gl-js/issues/4811)

---

## Performance Pitfalls

### 6. Battery Drain from Continuous GPS

**Description**: Continuous GPS usage for location tracking can drain battery extremely fast - critical in the Tankwa Karoo where charging is limited. High accuracy GPS is particularly power-hungry.

**Warning Signs**:
- Battery draining rapidly when app is open
- Device warming up during map use
- Users reporting poor battery life

**Impact**: HIGH - Users may not be able to use the app when they need it most

**Prevention**:
- Use `PRIORITY_BALANCED_POWER_ACCURACY` instead of high accuracy when possible
- Implement location update timeouts - don't track forever
- Stop location updates immediately when not needed (`stopUpdatingLocation()` / `removeLocationUpdates()`)
- On iOS, set `pausesLocationUpdatesAutomatically = true`
- Set appropriate `activityType` on iOS Core Location
- Use deferred location updates for background tracking
- Allow users to control location tracking manually
- Show battery impact warnings to users

**Sources**:
- [Android Location and Battery Life](https://developer.android.com/develop/sensors-and-location/location/battery)
- [Apple Energy Efficiency Guide - Location Best Practices](https://developer.apple.com/library/archive/documentation/Performance/Conceptual/EnergyGuide-iOS/LocationBestPractices.html)
- [Optimizing iOS location services](https://rangle.io/blog/optimizing-ios-location-services)

---

### 7. Location Updates Not Stopped Properly

**Description**: A common source of battery drain is failing to remove location updates when they're no longer needed. This happens when `requestLocationUpdates()` is called in `onStart()`/`onResume()` without a corresponding `removeLocationUpdates()` in `onPause()`/`onStop()`.

**Warning Signs**:
- GPS icon stays active after leaving map screen
- Battery usage high even when app backgrounded
- Location callbacks firing when not expected

**Impact**: HIGH - Severe battery drain, poor user reviews

**Prevention**:
- Always pair location request with removal in lifecycle methods
- Set reasonable timeout for location updates
- Use lifecycle-aware location components
- Test that GPS actually stops when leaving map screen
- Monitor GPS indicator during QA testing

**Sources**:
- [Android Location Battery Optimization](https://developer.android.com/develop/sensors-and-location/location/battery/optimize)

---

### 8. Style Diffing Causes Full Map Reload

**Description**: Changes in sprites (icons) or glyphs (fonts) cannot be diffed incrementally. If sprites or fonts differ in any way between styles, MapLibre forces a full map reload, removing the current style and rebuilding from scratch.

**Warning Signs**:
- Map flickers when switching themes
- Slow style changes
- High CPU during theme switching
- Map goes blank momentarily during style updates

**Impact**: MEDIUM - Poor user experience, unnecessary rendering work

**Prevention**:
- Use consistent sprite and glyph sets across all styles
- Avoid runtime style switching if possible
- Pre-load all styles at app startup
- Consider using a single style with dynamic layers instead of multiple styles

**Sources**:
- [MapLibre Style Spec - Root](https://maplibre.org/maplibre-style-spec/root/)

---

### 9. OpenGL/Metal Renderer Crashes

**Description**: MapLibre uses OpenGL on Android and is transitioning to Metal on iOS (OpenGL deprecated). Both renderers have known crash scenarios, particularly with complex styles or on emulators.

**Warning Signs**:
- Crashes on Android emulator API <= 30
- `GL error 0x500` errors
- Metal rendering crashes on iOS (especially CarPlay)
- `EXC_BAD_ACCESS` on complex camera movements

**Impact**: HIGH - App crashes, particularly on older devices/emulators

**Prevention**:
- Test on real devices, not just emulators
- Avoid extremely complex styles with many layers
- On iOS, Metal is still being stabilized - test thoroughly
- For Android emulator, use API > 30 for development
- Keep styles simple for better compatibility

**Sources**:
- [Metal Renderer iOS Early Access](https://github.com/maplibre/maplibre-native/issues/1609)
- [Crash on startup with Android Emulator API <= 30](https://github.com/maplibre/maplibre-native/issues/2369)
- [iOS Metal CarPlay issues](https://github.com/maplibre/maplibre-native/issues/375)

---

## Integration Pitfalls

### 10. UIKitView Memory Leaks (iOS)

**Description**: `ComposeUIViewController` references are not freed when navigating back. Memory usage increases on navigation and doesn't decrease, eventually causing OOM crashes.

**Warning Signs**:
- Memory grows with each map screen visit
- `DisposableEffect.onDispose` not called
- App crashes after navigating to map multiple times

**Impact**: HIGH - Memory leaks accumulate, causing crashes

**Prevention**:
- Use `AndroidView` overload with `onReset` callback for view reuse
- Implement proper cleanup: `onRelease = { view -> view.lifecycle = null }`
- Test navigation memory profile thoroughly
- Consider using `placedAsOverlay` flag on iOS for better lifecycle handling
- Monitor memory during repeated navigation to map screen

**Sources**:
- [ComposeUIViewController resources not released upon back navigation](https://github.com/JetBrains/compose-multiplatform/issues/3958)
- [UIKitView items recomposing on scroll](https://github.com/JetBrains/compose-multiplatform/issues/3458)

---

### 11. Third-Party iOS Library Integration Limitations

**Description**: While you can instantiate UIViews from UIKit directly in Kotlin code, you cannot import third-party iOS libraries (like MapLibre iOS SDK) directly in the iosMain source set. Kotlin/Native can only interop with Objective-C, not Swift directly.

**Warning Signs**:
- Cannot call MapLibre Swift APIs directly from Kotlin
- Compilation errors when trying to use iOS SDK methods
- Need for complex bridging code

**Impact**: MEDIUM - Requires architecture workarounds

**Prevention**:
- Use CocoaPods for iOS dependencies (but see CocoaPods pitfalls)
- Use expect/actual pattern with Swift implementations
- Create Objective-C bridging headers for Swift code
- Consider using MapLibre Compose which handles this internally
- If using Koin, inject native implementations via interface

**Sources**:
- [Adding iOS dependencies](https://kotlinlang.org/docs/multiplatform-ios-dependencies.html)
- [Compose Multiplatform Native Code Requirements](https://proandroiddev.com/why-your-compose-multiplatform-app-still-needs-native-code-a7e56bffeaea)

---

### 12. Desktop Support Not Production Ready

**Description**: MapLibre Compose for desktop platforms (macOS, Windows, Linux) requires integrating with MapLibre Native C++ core. Linux currently causes segfaults, and the development setup is brittle.

**Warning Signs**:
- Segfaults on Linux
- Complex build setup for desktop
- Limited testing on all platforms

**Impact**: LOW for this project (Android/iOS only), but good to know

**Prevention**:
- Don't target desktop for v3.0
- If desktop needed later, wait for MapLibre Compose maturity

**Sources**:
- [MapLibre Compose Roadmap](https://maplibre.org/maplibre-compose/roadmap/)

---

### 13. Location Puck Out of Sync

**Description**: MapLibre has documented issues where the user location puck, the map camera, and the actual user location can become out of sync, particularly during animations or navigation.

**Warning Signs**:
- User dot appears in wrong location
- Camera doesn't follow user properly
- Jerky movement during tracking

**Impact**: MEDIUM - Confusing user experience, users think GPS is broken

**Prevention**:
- Enable frame-by-frame course view tracking for critical moments
- Test location tracking with actual movement
- Avoid complex camera animations while tracking
- Consider implementing custom location tracking logic

**Sources**:
- [User's location, UserPuckCourseView, and map camera are out of sync](https://github.com/maplibre/maplibre-navigation-ios/issues/94)

---

### 14. Android 12+ Location Permission Changes

**Description**: When targeting SDK 31+, Android requires requesting `ACCESS_COARSE_LOCATION` when requesting `ACCESS_FINE_LOCATION`. Missing coarse permission causes crashes or permission denials.

**Warning Signs**:
- Permission crashes on Android 12+ devices
- Location not working despite granting "fine" permission
- SecurityException at runtime

**Impact**: HIGH - App crashes or location broken on newer Android

**Prevention**:
- Always request both `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`
- Update AndroidManifest.xml to include both permissions
- Test on Android 12+ devices specifically
- Handle permission denial gracefully

**Sources**:
- [Android Location permission on Android 12](https://github.com/maplibre/maplibre-native/issues/275)
- [MapLibre LocationComponent Documentation](https://maplibre.org/maplibre-native/android/examples/location-component/)

---

## Tile/Data Pitfalls

### 15. Offline Tile Budget Management (~50MB)

**Description**: Tile storage can quickly exceed budgets. Full world coverage at detail level is 107GB+. Even regional extracts need careful zoom level selection.

**Warning Signs**:
- Download takes too long
- Storage exceeds device capacity
- Users can't download due to size

**Impact**: HIGH - Either maps are too large or too low resolution

**Sizing Guidelines**:
- Zoom level 0-6 (country level): ~4MB
- Single city with detail: ~50-100MB
- Regional extract (small country): ~50MB-1GB depending on zoom
- Each additional zoom level roughly 4x the tiles

**Prevention**:
- Calculate tile count before downloading: tiles = 4^zoom_level per region
- Limit zoom levels (e.g., 8-14 instead of 0-20)
- Use vector tiles (much smaller than raster)
- Consider PMTiles format for single-file distribution
- Pre-generate and bundle tiles in app (no download needed)
- Test actual size on representative regions

**Sources**:
- [Protomaps Creating PMTiles](https://docs.protomaps.com/pmtiles/create)
- [Offline Maps with Flutter MapLibre GL](https://docs.stadiamaps.com/tutorials/offline-maps-with-flutter-maplibre-gl/)

---

### 16. Missing Fonts and Sprites Break Offline Maps

**Description**: Map style JSON references remote URLs for fonts (glyphs) and icons (sprites). Without bundling these locally, maps render without text or icons when offline.

**Warning Signs**:
- Map renders but no labels visible
- Icons missing from POIs
- Console errors about failed glyph/sprite requests

**Impact**: CRITICAL - Maps are unusable without labels/icons

**Prevention**:
- Download fonts from [openmaptiles/fonts](https://github.com/openmaptiles/fonts)
- Copy sprite files (sprite.json, sprite.png, sprite@2x.json, sprite@2x.png)
- Update style JSON to use local paths:
  - `"glyphs": "asset://fonts/{fontstack}/{range}.pbf"`
  - `"sprite": "asset://sprites/osm-bright"`
- Include all font variants used in style (Open Sans, Noto Sans, etc.)
- Test with network completely disabled

**Sources**:
- [MapLibre GL JS OpenMapTiles Guide](https://openmaptiles.org/docs/website/maplibre-gl-js/)
- [Offline Styles Discussion](https://github.com/maplibre/maplibre-gl-js/discussions/1975)

---

### 17. Cache Eviction Deletes Downloaded Tiles

**Description**: MapLibre's cache evicts tiles based on count or size, not age. Heavy app use in other areas can cause previously downloaded offline data to be deleted.

**Warning Signs**:
- Downloaded region disappears after using map elsewhere
- Users report offline data "went away"
- Cache size limits exceeded

**Impact**: HIGH - Users download tiles at home, arrive at event with no tiles

**Prevention**:
- Use dedicated offline regions instead of relying on ambient cache
- Set appropriate `setMaximumAmbientCacheSize`
- Download regions as late as practical before going offline
- Consider disabling ambient cache (set to 0) if only using offline regions
- Warn users not to browse other map areas after downloading

**Sources**:
- [Add OfflineManager::setMaximumAmbientCacheAge](https://github.com/maplibre/maplibre-native/issues/2300)
- [MapLibre OfflineManager](https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.offline/-offline-manager/index.html)

---

### 18. PMTiles Sources Don't Cache for Offline

**Description**: PMTiles sources do not appear to be cached in a way usable offline. Previously downloaded tiles from non-PMTiles sources work offline, but PMTiles behavior differs.

**Warning Signs**:
- PMTiles work online, blank offline
- PMTiles files present but not used
- Different behavior than MBTiles

**Impact**: MEDIUM - May need to use MBTiles instead

**Prevention**:
- Test PMTiles thoroughly in offline mode before committing to format
- Consider using MBTiles for offline if PMTiles don't work
- Bundle PMTiles file with app rather than downloading
- Watch for updates in MapLibre Native PMTiles support

**Sources**:
- [Allow Caching Requests for PMTiles Sources](https://github.com/maplibre/maplibre-native/issues/3690)

---

### 19. Offline Region Download Stuck/Incomplete

**Description**: Some users report offline region downloads proceeding but getting stuck at the last few resources, never completing. This can happen with certain style configurations.

**Warning Signs**:
- Progress reaches 95%+ and stalls
- Download callback never fires completion
- Some tiles missing from downloaded region

**Impact**: HIGH - Users think download succeeded but region is incomplete

**Prevention**:
- Implement download timeout and retry logic
- Show detailed progress (not just percentage)
- Allow users to verify download before going offline
- Implement download verification (compare expected vs actual tile count)
- Handle partial downloads gracefully (re-download missing tiles)

**Sources**:
- [OfflineRegion download can not be completed](https://github.com/maplibre/maplibre-native/issues/3215)

---

### 20. Database Corruption After Errors

**Description**: If tile downloads fail or the app crashes during download, the offline database can become corrupted, causing tiles not to display even after re-downloading.

**Warning Signs**:
- Previously working offline regions stop working
- Download succeeds but tiles don't show
- Errors in OfflineRegionObserver

**Impact**: HIGH - Requires database reset, losing all downloaded data

**Prevention**:
- Implement database health checks
- Provide "Reset Database" option in settings
- Use tile invalidation (`invalidate()`) rather than delete + re-download
- Catch and handle download errors gracefully
- Back up offline database if possible

**Sources**:
- [Android Offline errors in v11](https://github.com/maplibre/maplibre-native/issues/2566)
- [MapLibre OfflineRegion](https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.offline/-offline-region/index.html)

---

## AfrikaBurn-Specific Recommendations

### Pre-Event Checklist

1. **Download tiles at home** (stable internet, power available)
2. **Test in airplane mode** before leaving
3. **Verify fonts and icons render** when offline
4. **Check battery impact** during 30-minute map session
5. **Test location tracking** works accurately
6. **Have fallback plan** (paper map, GPS coordinates)

### Recommended Configuration

```kotlin
// Tile storage budget
const val MAX_OFFLINE_TILES = 10000  // Conservative for 50MB
const val ZOOM_RANGE = 8..14         // Useful range, not wasteful

// Battery optimization
const val LOCATION_UPDATE_INTERVAL_MS = 5000L  // Not too frequent
const val LOCATION_FASTEST_INTERVAL_MS = 2000L
const val LOCATION_PRIORITY = PRIORITY_BALANCED_POWER_ACCURACY

// Cache management
const val AMBIENT_CACHE_SIZE_MB = 10  // Small, rely on offline regions
```

### Known Working Stack (as of 2026-01)

- MapLibre Compose: 0.11.1+
- Kotlin: 2.1.21+
- Compose Multiplatform: 1.8.1+
- MapLibre Native Android: 11.x
- MapLibre iOS: 6.9.0+

---

## Quick Reference: Pitfall Severity

| Category | Pitfall | Severity | Likelihood |
|----------|---------|----------|------------|
| Critical | Offline tiles not displaying | CRITICAL | HIGH |
| Critical | Memory crashes | CRITICAL | MEDIUM |
| Critical | iOS framework not found | CRITICAL | MEDIUM |
| Critical | Missing fonts/sprites | CRITICAL | HIGH |
| Performance | Battery drain from GPS | HIGH | HIGH |
| Performance | Location not stopped | HIGH | HIGH |
| Integration | UIKitView memory leaks | HIGH | MEDIUM |
| Integration | Android 12+ permissions | HIGH | HIGH |
| Tiles | Cache eviction | HIGH | MEDIUM |
| Tiles | Download stuck | HIGH | LOW |

---

*Research completed: 2026-01-18*

**Sources Referenced:**
- [MapLibre Compose](https://maplibre.org/maplibre-compose/)
- [MapLibre Native GitHub](https://github.com/maplibre/maplibre-native)
- [Android Developer - Location Battery](https://developer.android.com/develop/sensors-and-location/location/battery)
- [Apple Energy Efficiency Guide](https://developer.apple.com/library/archive/documentation/Performance/Conceptual/EnergyGuide-iOS/LocationBestPractices.html)
- [JetBrains Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [Protomaps Documentation](https://docs.protomaps.com/)
- [OpenMapTiles](https://openmaptiles.org/)
