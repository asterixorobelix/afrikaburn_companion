# AfrikaBurn Companion - Offline Map Implementation Plan

## Overview

This document outlines the implementation strategy for adding offline mapping capabilities to the AfrikaBurn Companion app using **Mapbox SDK as the primary solution** with **MapLibre Native as a fallback option**. The approach uses a provider abstraction layer to enable seamless switching between mapping solutions.

## Table of Contents

1. [Technology Stack Strategy](#technology-stack-strategy)
2. [Architecture Design](#architecture-design)
3. [Map Provider Abstraction](#map-provider-abstraction)
4. [Offline Data Strategy](#offline-data-strategy)
5. [Integration with Existing App](#integration-with-existing-app)
6. [Implementation Phases](#implementation-phases)
7. [Platform-Specific Considerations](#platform-specific-considerations)
8. [AfrikaBurn-Specific Features](#afrikaburn-specific-features)
9. [Risk Mitigation](#risk-mitigation)
10. [Technical Specifications](#technical-specifications)

## Technology Stack Strategy

### Primary Solution: Mapbox SDK
- **Rationale**: Industry-leading offline support, excellent KMP compatibility, robust Compose integration
- **Advantages**:
  - Official Compose Multiplatform support
  - Comprehensive offline mapping with style downloads
  - Advanced vector tile rendering
  - Rich navigation features
  - Excellent documentation and community support
- **Considerations**: Requires API key and has usage-based pricing

### Fallback Solution: MapLibre Native
- **Rationale**: Open-source alternative with similar capabilities
- **Advantages**:
  - No licensing costs or API limits
  - BSD 2-Clause license
  - Compatible with Mapbox vector tile format
  - Community-driven development
- **Considerations**: Requires custom KMP wrapper implementation

### Decision Matrix
| Feature | Mapbox SDK | MapLibre Native |
|---------|------------|----------------|
| KMP Support | ✅ Official | ⚠️ Custom wrapper |
| Offline Maps | ✅ Excellent | ✅ Good |
| Compose Integration | ✅ Native | ⚠️ Custom implementation |
| Licensing Cost | ❌ Usage-based | ✅ Free |
| Documentation | ✅ Comprehensive | ✅ Good |
| Development Speed | ✅ Fast | ⚠️ Slower |

## Architecture Design

### Core Module Structure
```
mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/
├── map/
│   ├── data/
│   │   ├── models/
│   │   │   ├── MapLocation.kt
│   │   │   ├── MapBounds.kt
│   │   │   ├── CameraPosition.kt
│   │   │   ├── MapStyle.kt
│   │   │   └── AfrikaBurnPOI.kt
│   │   ├── repository/
│   │   │   ├── MapRepository.kt
│   │   │   └── MapRepositoryImpl.kt
│   │   └── datasource/
│   │       ├── OfflineMapDataSource.kt
│   │       ├── LocationDataSource.kt
│   │       └── MapProviderDataSource.kt
│   ├── domain/
│   │   ├── models/
│   │   │   ├── MapProvider.kt
│   │   │   └── MapConfiguration.kt
│   │   ├── usecases/
│   │   │   ├── LoadOfflineMapUseCase.kt
│   │   │   ├── GetUserLocationUseCase.kt
│   │   │   ├── SearchMapLocationsUseCase.kt
│   │   │   └── SwitchMapProviderUseCase.kt
│   │   └── repository/
│   │       └── MapRepository.kt
│   ├── presentation/
│   │   ├── MapScreen.kt
│   │   ├── MapViewModel.kt
│   │   ├── MapUiState.kt
│   │   └── components/
│   │       ├── UnifiedMapView.kt
│   │       ├── MapControls.kt
│   │       ├── LocationMarker.kt
│   │       ├── MapProviderSelector.kt
│   │       └── OfflineMapDownloader.kt
│   ├── providers/
│   │   ├── MapProvider.kt              # Abstract interface
│   │   ├── mapbox/
│   │   │   ├── MapboxProvider.kt
│   │   │   ├── MapboxMapView.kt
│   │   │   └── MapboxOfflineManager.kt
│   │   └── maplibre/
│   │       ├── MapLibreProvider.kt
│   │       ├── MapLibreMapView.kt
│   │       └── MapLibreOfflineManager.kt
│   └── platform/
│       ├── LocationService.kt
│       ├── MapTileCache.kt
│       └── PermissionManager.kt
```

## Map Provider Abstraction

### Core Abstraction Interface
```kotlin
interface MapProvider {
    val providerType: MapProviderType
    val isAvailable: Boolean
    
    suspend fun initialize(apiKey: String? = null): Result<Unit>
    suspend fun loadOfflineRegion(bounds: MapBounds, styleUrl: String): Result<Unit>
    suspend fun isOfflineRegionAvailable(bounds: MapBounds): Boolean
    
    @Composable
    fun MapView(
        modifier: Modifier = Modifier,
        cameraPosition: CameraPosition,
        onCameraPositionChanged: (CameraPosition) -> Unit,
        onMapClick: (MapLocation) -> Unit,
        markers: List<MapMarker> = emptyList()
    )
}

enum class MapProviderType {
    MAPBOX, MAPLIBRE
}

sealed class MapProviderResult<T> {
    data class Success<T>(val data: T) : MapProviderResult<T>()
    data class Error<T>(val exception: Throwable) : MapProviderResult<T>()
}
```

### Provider Factory
```kotlin
class MapProviderFactory {
    companion object {
        fun createProvider(
            preferredType: MapProviderType = MapProviderType.MAPBOX,
            context: PlatformContext
        ): MapProvider {
            return when {
                preferredType == MapProviderType.MAPBOX && MapboxProvider.isAvailable -> {
                    MapboxProvider(context)
                }
                MapLibreProvider.isAvailable -> {
                    MapLibreProvider(context)
                }
                else -> {
                    throw IllegalStateException("No map provider available")
                }
            }
        }
    }
}
```

### Unified Map Component
```kotlin
@Composable
fun UnifiedMapView(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val provider = uiState.currentProvider) {
        is MapProvider -> {
            provider.MapView(
                modifier = modifier,
                cameraPosition = uiState.cameraPosition,
                onCameraPositionChanged = viewModel::updateCameraPosition,
                onMapClick = viewModel::onMapClick,
                markers = uiState.markers
            )
        }
        null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.map_provider_unavailable),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
```

## Offline Data Strategy

### Data Storage Architecture
```kotlin
// SQLDelight database schema
CREATE TABLE offline_map_regions (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    provider_type TEXT NOT NULL,
    bounds_north REAL NOT NULL,
    bounds_south REAL NOT NULL,
    bounds_east REAL NOT NULL,
    bounds_west REAL NOT NULL,
    style_url TEXT NOT NULL,
    download_size_bytes INTEGER NOT NULL,
    downloaded_at INTEGER,
    status TEXT NOT NULL DEFAULT 'pending'
);

CREATE TABLE afrikaburn_locations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    location_type TEXT NOT NULL,
    project_id TEXT,
    icon_type TEXT NOT NULL,
    is_family_friendly INTEGER DEFAULT 0,
    created_at INTEGER NOT NULL
);
```

### Offline Map Configuration
```kotlin
data class OfflineMapConfiguration(
    val regionName: String = "AfrikaBurn 2024",
    val bounds: MapBounds = MapBounds(
        north = -32.0, south = -33.0,  // Tankwa Karoo region
        east = 20.0, west = 19.0
    ),
    val minZoom: Int = 10,
    val maxZoom: Int = 18,
    val styleUrl: String = "mapbox://styles/afrikaburn/desert-style",
    val estimatedSizeBytes: Long = 50_000_000L // ~50MB
)
```

### Tile Management Strategy
- **Pre-bundled Data**: Include essential region tiles in app bundle (20-30MB)
- **On-demand Downloads**: Additional detail levels when connected
- **Storage Management**: LRU cache with configurable size limits
- **Update Strategy**: Delta updates for POI data, full re-download for base map changes

## Integration with Existing App

### Navigation Integration
```kotlin
// Update NavigationDestination.kt
sealed class NavigationDestination(
    val route: String,
    val titleRes: StringResource,
    val icon: ImageVector
) {
    object Projects : NavigationDestination("projects", Res.string.nav_projects, Icons.Default.Event)
    object Directions : NavigationDestination("directions", Res.string.nav_directions, Icons.Default.Navigation)
    object Map : NavigationDestination("map", Res.string.nav_map, Icons.Default.Map) // NEW
    object About : NavigationDestination("about", Res.string.nav_about, Icons.Default.Info)
}
```

### Data Model Integration
```kotlin
// Extend existing ProjectItem with location data
data class ProjectLocation(
    val projectItem: ProjectItem,
    val location: MapLocation,
    val locationType: LocationType
)

enum class LocationType {
    THEME_CAMP,
    ARTWORK,
    PERFORMANCE_VENUE,
    MUTANT_VEHICLE_AREA,
    INFRASTRUCTURE
}
```

### Dependency Injection Updates
```kotlin
// Add to existing modules in di/
val mapModule = module {
    single<MapProvider> { 
        MapProviderFactory.createProvider(
            preferredType = MapProviderType.MAPBOX,
            context = get()
        )
    }
    single<MapRepository> { MapRepositoryImpl(get(), get(), get()) }
    single { LoadOfflineMapUseCase(get()) }
    single { GetUserLocationUseCase(get()) }
    single { SearchMapLocationsUseCase(get()) }
    factory { MapViewModel(get(), get(), get(), get()) }
}
```

### Version Catalog Updates (libs.versions.toml)
```toml
[versions]
mapbox-maps = "11.7.1"
mapbox-common = "24.7.1"
maplibre-android = "11.5.2"

[libraries]
# Mapbox
mapbox-maps-android = { module = "com.mapbox.maps:android", version.ref = "mapbox-maps" }
mapbox-maps-compose = { module = "com.mapbox.extension:maps-compose", version.ref = "mapbox-maps" }
mapbox-common = { module = "com.mapbox.common:common", version.ref = "mapbox-common" }

# MapLibre (fallback)
maplibre-android = { module = "org.maplibre.gl:android-sdk", version.ref = "maplibre-android" }

# Location services
play-services-location = { module = "com.google.android.gms:play-services-location", version = "21.3.0" }
```

## Implementation Phases

### Phase 1: Foundation & Provider Abstraction (Sprint 1-2)
**Objectives**: Establish core architecture and provider switching capability

**Tasks**:
1. Create map provider abstraction interfaces
2. Implement Mapbox provider with basic functionality
3. Create unified MapView component
4. Add Map tab to navigation
5. Implement basic offline region download

**Deliverables**:
- Working map display with Mapbox
- Provider abstraction layer
- Basic offline map download capability

### Phase 2: MapLibre Fallback Implementation (Sprint 3)
**Objectives**: Add fallback provider and provider switching logic

**Tasks**:
1. Implement MapLibre provider with KMP wrapper
2. Add provider availability detection
3. Implement automatic fallback logic
4. Create provider selection UI
5. Test provider switching functionality

**Deliverables**:
- Working MapLibre fallback implementation
- Seamless provider switching
- Provider selection interface

### Phase 3: AfrikaBurn Integration (Sprint 4-5)
**Objectives**: Integrate with existing app data and add AfrikaBurn-specific features

**Tasks**:
1. Import AfrikaBurn location data
2. Create custom map markers for different location types
3. Link map locations to existing ProjectItem data
4. Implement map-based filtering
5. Add search functionality

**Deliverables**:
- AfrikaBurn POI display on map
- Integration with existing project data
- Map-based project discovery

### Phase 4: Advanced Features (Sprint 6-7)
**Objectives**: Add navigation and user experience enhancements

**Tasks**:
1. Implement user location tracking
2. Add GPS navigation features
3. Create offline route planning
4. Implement map clustering for dense areas
5. Add accessibility improvements

**Deliverables**:
- GPS navigation functionality
- Route planning capability
- Enhanced user experience

### Phase 5: Optimization & Polish (Sprint 8)
**Objectives**: Performance optimization and production readiness

**Tasks**:
1. Optimize battery usage and performance
2. Implement comprehensive testing
3. Add analytics and crash reporting
4. Create user documentation
5. Prepare for production deployment

**Deliverables**:
- Production-ready map functionality
- Comprehensive test coverage
- Performance-optimized implementation

## Platform-Specific Considerations

### Android Implementation
```kotlin
// Mapbox Android Integration
actual class MapboxProvider(private val context: Context) : MapProvider {
    private lateinit var mapboxMap: MapboxMap
    
    @Composable
    actual override fun MapView(
        modifier: Modifier,
        cameraPosition: CameraPosition,
        onCameraPositionChanged: (CameraPosition) -> Unit,
        onMapClick: (MapLocation) -> Unit,
        markers: List<MapMarker>
    ) {
        MapboxMap(
            Modifier.fillMaxSize(),
            mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(cameraPosition.longitude, cameraPosition.latitude))
                    zoom(cameraPosition.zoom)
                }
            }
        ) {
            // Add markers and event handlers
        }
    }
}
```

### iOS Implementation
```kotlin
// iOS-specific location permissions and MapKit integration
actual class LocationService {
    actual suspend fun requestLocationPermission(): Boolean {
        return withContext(Dispatchers.Main) {
            // iOS Core Location permission request
            val locationManager = CLLocationManager()
            locationManager.requestWhenInUseAuthorization()
            // Handle permission result
        }
    }
}
```

## AfrikaBurn-Specific Features

### Custom Map Styling
```json
{
  "version": 8,
  "name": "AfrikaBurn Desert Theme",
  "metadata": {
    "mapbox:type": "template"
  },
  "sources": {
    "composite": {
      "type": "vector",
      "url": "mapbox://mapbox.mapbox-streets-v8"
    },
    "afrikaburn-data": {
      "type": "geojson",
      "data": "asset://afrikaburn-locations.geojson"
    }
  },
  "layers": [
    {
      "id": "background",
      "type": "background",
      "paint": {
        "background-color": "#f5f3f0"
      }
    },
    {
      "id": "camps",
      "type": "symbol",
      "source": "afrikaburn-data",
      "filter": ["==", "type", "camp"],
      "layout": {
        "icon-image": "camp-15",
        "icon-size": 1.5,
        "text-field": "{name}",
        "text-font": ["Open Sans Bold", "Arial Unicode MS Bold"],
        "text-size": 12,
        "text-anchor": "top",
        "text-offset": [0, 1]
      },
      "paint": {
        "text-color": "#8B4513",
        "text-halo-color": "#FFFFFF",
        "text-halo-width": 1
      }
    }
  ]
}
```

### POI Categories and Icons
```kotlin
enum class AfrikaBurnPOIType(val iconName: String, val color: String) {
    THEME_CAMP("camp", "#E74C3C"),
    ARTWORK("art", "#9B59B6"),
    PERFORMANCE_VENUE("stage", "#3498DB"),
    MEDICAL("medical", "#E67E22"),
    BATHROOM("restroom", "#2ECC71"),
    WATER_STATION("water", "#1ABC9C"),
    RANGER_STATION("security", "#34495E"),
    GATE("gate", "#F39C12"),
    CENTER_CAMP("center", "#E74C3C"),
    EXODUS("exodus", "#95A5A6")
}
```

### Smart Features
1. **Dust Storm Mode**: High contrast styling for poor visibility
2. **Night Mode**: Red-filtered display to preserve night vision
3. **Battery Saver**: Reduced update frequency and simplified rendering
4. **Offline-First**: All features work without internet connection
5. **Camp Finder**: Search camps by theme, amenities, or proximity
6. **Art Tour**: Curated routes through art installations

## Risk Mitigation

### Provider Availability Issues
- **Risk**: Mapbox API limits or service unavailability
- **Mitigation**: Automatic fallback to MapLibre with user notification
- **Testing**: Regular provider switching tests in CI/CD

### Data Size and Storage
- **Risk**: Large offline map files impact app size and user storage
- **Mitigation**: 
  - Tiered download strategy (essential vs. detailed)
  - User-configurable storage limits
  - Automatic cleanup of old cached data

### Performance Issues
- **Risk**: Map rendering impacts battery life and performance
- **Mitigation**:
  - GPU-accelerated rendering
  - Configurable frame rates
  - Background processing optimization
  - Memory leak detection

### License and Cost Management
- **Risk**: Unexpected Mapbox usage costs
- **Mitigation**:
  - Usage monitoring and alerts
  - Fallback to MapLibre for high-usage scenarios
  - Clear documentation of API key management

## Technical Specifications

### Supported Zoom Levels
- **Minimum**: Level 10 (regional overview)
- **Maximum**: Level 18 (detailed navigation)
- **Default**: Level 14 (camp overview)

### Map Data Requirements
- **Coverage Area**: Tankwa Karoo, Northern Cape, South Africa
- **Coordinate System**: WGS84 (EPSG:4326)
- **Data Format**: Vector tiles (MVT) for base map, GeoJSON for POI
- **Update Frequency**: Base map annually, POI data real-time during event

### Performance Targets
- **Initial Load Time**: < 3 seconds for cached maps
- **Frame Rate**: 60fps during navigation
- **Memory Usage**: < 200MB peak usage
- **Battery Impact**: < 5% additional drain during active use

### Accessibility Features
- **Screen Reader**: Full VoiceOver/TalkBack support
- **High Contrast**: Alternative color schemes
- **Large Text**: Scalable map labels
- **Motor Accessibility**: Gesture alternatives for map interaction

### Security Considerations
- **API Key Management**: Secure storage and rotation
- **Location Privacy**: User consent and data minimization
- **Offline Security**: Encrypted local storage for sensitive data
- **Network Security**: Certificate pinning for API calls

---

## Conclusion

This implementation plan provides a robust, flexible approach to adding offline mapping capabilities to the AfrikaBurn Companion app. By using Mapbox as the primary solution with MapLibre as a fallback, we ensure reliable mapping functionality while maintaining cost control and open-source alternatives.

The provider abstraction layer enables seamless switching between mapping solutions, future-proofing the implementation against changes in provider availability or business requirements. The phased approach allows for incremental delivery and testing, ensuring a stable and user-friendly mapping experience for AfrikaBurn participants in the remote Tankwa Karoo environment.