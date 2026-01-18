# Stack Research: Offline Maps

**Research Date:** 2026-01-18
**Milestone:** v3.0 Offline Map

## Recommended Stack

### MapLibre SDK

**Compose Multiplatform Wrapper (Primary Recommendation)**
- **Library:** [MapLibre Compose](https://maplibre.org/maplibre-compose/) v0.11.1
- **Dependencies:**
  ```kotlin
  // In commonMain
  implementation("org.maplibre.compose:maplibre-compose:0.11.1")
  implementation("org.maplibre.compose:maplibre-compose-material3:0.11.1")
  ```
- **Platform Support:** Android, iOS, Desktop (partial), Web (partial)
- **Status:** Active development, pre-1.0 (API may change)

**Underlying Native SDKs (used internally by MapLibre Compose)**
- **Android:** MapLibre Native Android v11.13.1+
  - Maven: `org.maplibre.gl:android-sdk:11.13.1`
  - Vulkan support for improved performance (replaces OpenGL ES)
  - PMTiles support since v11.8.0
- **iOS:** MapLibre Native iOS v6.19.1+
  - Swift Package Manager: `https://github.com/maplibre/maplibre-gl-native-distribution`
  - Also available via CocoaPods
  - Metal rendering support (improved performance over OpenGL)
  - PMTiles support since v6.10.0

**KMP Integration Pattern**

MapLibre Compose uses the following architecture:
- **Desktop:** Direct JNI bindings to MapLibre Native C++ core
- **Android:** Wraps MapLibre Native Android Java/Kotlin bindings
- **iOS:** Wraps MapLibre Native iOS Obj-C bindings via Kotlin/Native cinterop

The library abstracts platform differences, providing a unified Composable API:

```kotlin
@Composable
fun MapScreen() {
    MapLibreMap(
        styleUri = "asset://style.json",
        modifier = Modifier.fillMaxSize()
    )
}
```

**Key Composables:**
- `MapLibreMap` - Main map container
- `SymbolLayer` - Markers and custom icons with SDF support
- `CircleLayer`, `LineLayer`, `FillLayer` - Geometric shapes
- `GeoJsonSource` - Data source for layers

### Offline Tiles

**Format Recommendation: PMTiles (Primary) + MBTiles (Fallback)**

| Format | Pros | Cons |
|--------|------|------|
| **PMTiles** | Cloud-native, efficient range requests, no server needed, supported since 2025 | Newer format, no offline pack downloads in MapLibre yet |
| **MBTiles** | Battle-tested, SQLite-based, wide tooling support | Requires tile server for web, larger file overhead |

**PMTiles Integration:**
```kotlin
// Android/iOS - Prefix with pmtiles://
val styleJson = """
{
  "sources": {
    "openmaptiles": {
      "type": "vector",
      "url": "pmtiles://asset://tiles/afrikaburn.pmtiles"
    }
  }
}
"""
```

**Loading Methods:**
- `pmtiles://https://...` - Remote PMTiles file
- `pmtiles://asset://...` - Bundled asset (recommended for offline)
- `pmtiles://file://...` - Local file storage

**MBTiles Integration (Alternative):**
```kotlin
// Style JSON source configuration
val styleJson = """
{
  "sources": {
    "tiles": {
      "type": "vector",
      "url": "mbtiles:///path/to/tiles.mbtiles"
    }
  }
}
"""
```

**Tile Sources for Africa/South Africa:**

| Provider | Format | License | Notes |
|----------|--------|---------|-------|
| [MapTiler Data](https://data.maptiler.com/downloads/dataset/osm/africa/) | PMTiles/MBTiles | Free for non-commercial/evaluation | OpenMapTiles schema, zoom 0-14 |
| [OpenMapTiles](https://openmaptiles.org/) | MBTiles | BSD/CC-BY (attribution) | Self-generate or download |
| [Protomaps](https://protomaps.com/) | PMTiles | Open | Daily OSM builds available |
| [HOT Export Tool](https://export.hotosm.org/) | MBTiles | ODbL | Custom area extraction, 5000 tile limit |

**Recommended Approach for AfrikaBurn:**
1. Download/generate PMTiles for Tankwa Karoo region at zoom levels 10-16
2. Bundle in `composeResources/files/` (max ~50MB recommended)
3. Use Protomaps styles compatible with OpenMapTiles schema

**Bundling Strategy:**
```
mobile/composeApp/composeResources/files/
  maps/
    tankwa-karoo.pmtiles      # Vector tiles (~20-50MB)
    style.json                 # Map style definition
    sprites/                   # Icon sprites
    fonts/                     # Glyph fonts (optional if using system)
```

### Location Services

**Recommended Library: [Compass](https://github.com/jordond/compass) v1.6.7+**

A comprehensive Kotlin Multiplatform location toolkit:

```kotlin
// In commonMain
implementation("dev.jordond.compass:geolocation:1.6.7")
implementation("dev.jordond.compass:geolocation-mobile:1.6.7")
implementation("dev.jordond.compass:permissions-mobile:1.6.7")
```

**Features:**
- Real-time location tracking
- Geocoding and reverse geocoding
- Built-in permission handling
- Place autocomplete (optional, requires API)

**Usage Pattern:**
```kotlin
// Common code
class LocationService(private val geolocator: Geolocator) {
    suspend fun getCurrentLocation(): Location? {
        return geolocator.current()
    }

    fun trackLocation(): Flow<Location> {
        return geolocator.track()
    }
}
```

**Alternative: [moko-geo](https://github.com/icerockdev/moko-geo) v0.8.0**
```kotlin
implementation("dev.icerock.moko:geo:0.8.0")
implementation("dev.icerock.moko:geo-compose:0.8.0")
```
- Compose Multiplatform support
- Simpler API, fewer features
- IceRock maintained

**Platform Permissions:**

| Platform | Permission | Notes |
|----------|------------|-------|
| Android | `ACCESS_FINE_LOCATION` | Precise GPS coordinates |
| Android | `ACCESS_COARSE_LOCATION` | Approximate location |
| Android | `ACCESS_BACKGROUND_LOCATION` | Background tracking (optional) |
| iOS | `NSLocationWhenInUseUsageDescription` | Foreground location |
| iOS | `NSLocationAlwaysAndWhenInUseUsageDescription` | Background (optional) |

### Supporting Libraries

**Marker/Pin Rendering with MapLibre Compose:**
```kotlin
@Composable
fun MarkerLayer(pins: List<UserPin>) {
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            FeatureCollection(
                pins.map { pin ->
                    Feature(
                        geometry = Point(Position(pin.longitude, pin.latitude)),
                        properties = mapOf("title" to pin.title)
                    )
                }
            )
        )
    )

    SymbolLayer(
        id = "user-pins",
        source = source,
        iconImage = image(painterResource(Res.drawable.pin), drawAsSdf = true),
        iconSize = const(1.5f),
        iconAnchor = IconAnchor.Bottom
    )
}
```

**Additional Dependencies:**

| Library | Purpose | Version |
|---------|---------|---------|
| `kotlinx-serialization-json` | GeoJSON parsing | 1.9.0 (already in project) |
| `kotlinx-datetime` | Timestamp handling | 0.6.2 (already in project) |
| SQLDelight | Local pin storage | Add if needed |

## Alternatives Considered

### 1. Native expect/actual Implementation (Manual)
**Approach:** Write platform-specific MapLibre bindings manually

**Pros:**
- Full control over API surface
- Direct access to all native features
- No wrapper library dependencies

**Cons:**
- Significant development effort (~2-4 weeks)
- Must maintain two implementations
- Feature parity challenges between platforms

**Verdict:** Not recommended given MapLibre Compose availability

### 2. Ramani Maps
**Repository:** [github.com/ramani-maps/ramani-maps](https://github.com/ramani-maps/ramani-maps)

**Pros:**
- Mature Android Compose integration
- Good offline support documentation
- Active community

**Cons:**
- Android-only (no iOS support)
- Would require separate iOS implementation
- Less ecosystem alignment than official MapLibre

**Verdict:** Consider if MapLibre Compose proves unstable

### 3. Google Maps SDK
**Approach:** Use Google Maps with KMP wrapper

**Pros:**
- Familiar API
- Excellent documentation
- Strong support

**Cons:**
- Requires internet for tiles (no true offline)
- API key costs at scale
- Not open source
- Vendor lock-in

**Verdict:** Not suitable for offline-first festival use case

### 4. Mapbox SDK
**Approach:** Use Mapbox Native SDKs with expect/actual

**Pros:**
- Feature-rich
- Good offline support
- Professional documentation

**Cons:**
- Non-open source license (post-2020)
- Tile hosting costs
- No official KMP support

**Verdict:** License/cost concerns for open-source project

## Integration Notes

### Architecture Integration

```
presentation/
  screens/
    map/
      MapScreen.kt              # Main map composable
      MapViewModel.kt           # Location state, pins

domain/
  repositories/
    LocationRepository.kt       # Location data interface
    PinRepository.kt           # User pin CRUD

data/
  datasources/
    LocationDataSource.kt       # Compass wrapper
    LocalPinDataSource.kt       # SQLDelight/Room
  repositories/
    LocationRepositoryImpl.kt
    PinRepositoryImpl.kt
```

### Koin DI Setup

```kotlin
val mapModule = module {
    single { Geolocator.mobile() }
    single<LocationRepository> { LocationRepositoryImpl(get()) }
    single<PinRepository> { PinRepositoryImpl(get()) }
    viewModel { MapViewModel(get(), get()) }
}
```

### Known Limitations

1. **MapLibre Compose Offline Manager:** Not yet implemented in v0.11.x. Offline support relies on pre-bundled tiles rather than dynamic region downloads.

2. **PMTiles Caching:** PMTiles sources do not currently support caching or offline pack downloads in MapLibre Native. Pre-bundle tiles for guaranteed offline access.

3. **API Stability:** MapLibre Compose is pre-1.0; expect breaking changes. Pin dependency versions.

4. **iOS Build Size:** MapLibre Native iOS adds ~15-20MB to app size. Consider app thinning strategies.

5. **Desktop/Web:** Partial support only. Focus on Android/iOS for v3.0.

### Implementation Phases

**Phase 1: Basic Map Display**
- Add MapLibre Compose dependency
- Display map with bundled style
- Basic gesture handling

**Phase 2: Offline Tiles**
- Generate/download Tankwa Karoo PMTiles
- Bundle tiles in app resources
- Configure offline style

**Phase 3: Location Services**
- Add Compass dependency
- Implement permission flow
- Display user location puck

**Phase 4: User Pins**
- SQLDelight schema for pins
- SymbolLayer for pin rendering
- Pin CRUD operations

### Testing Strategy

- **Unit Tests:** ViewModel logic, repository implementations
- **Integration Tests:** Location service mocking with Compass test utilities
- **UI Tests:** Map rendering verification (platform-specific)
- **Manual Testing:** Airplane mode verification for offline functionality

### Performance Considerations

- PMTiles ~20-50MB for regional coverage (zoom 10-16)
- Vector tiles render faster than raster at high zoom
- Limit concurrent layer count to <15 for smooth performance
- Use `iconAllowOverlap = false` for dense pin clusters

---

*Research completed: 2026-01-18*
*Sources consulted: MapLibre documentation, GitHub repositories, community articles*

## References

- [MapLibre Compose](https://maplibre.org/maplibre-compose/) - Official documentation
- [MapLibre Compose GitHub](https://github.com/maplibre/maplibre-compose) - Source code
- [MapLibre Native](https://github.com/maplibre/maplibre-native) - Underlying SDK
- [Compass Library](https://github.com/jordond/compass) - KMP location toolkit
- [PMTiles Documentation](https://docs.protomaps.com/pmtiles/maplibre) - Tile format guide
- [MapTiler Data](https://data.maptiler.com/) - Tile downloads
- [OpenMapTiles](https://openmaptiles.org/) - Self-hosted tiles
