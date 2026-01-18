# Architecture Research: Offline Maps

**Research Date:** 2026-01-18
**Milestone:** v3.0 Offline Map

## Executive Summary

This document analyzes the architecture for integrating MapLibre offline maps into the existing AfrikaBurn Companion app. The integration leverages the official **MapLibre Compose** library (v0.11.1+) which provides first-party Compose Multiplatform support with offline tile capabilities. The analysis covers component structure, data flow patterns, platform abstractions, and build sequencing while maintaining compatibility with the existing Clean Architecture implementation.

---

## Component Structure

### New Components Required

```
mobile/composeApp/src/
├── commonMain/kotlin/io/asterixorobelix/afrikaburn/
│   ├── data/
│   │   ├── datasource/
│   │   │   └── MapTileDataSource.kt          # Offline tile management
│   │   └── repository/
│   │       ├── UserCampRepositoryImpl.kt     # Camp pin persistence
│   │       └── LocationRepositoryImpl.kt     # GPS location data
│   │
│   ├── domain/
│   │   ├── repository/
│   │   │   ├── UserCampRepository.kt         # Interface for camp pins
│   │   │   └── LocationRepository.kt         # Interface for location
│   │   └── model/
│   │       ├── MapMarker.kt                  # Marker domain model
│   │       ├── UserCamp.kt                   # User camp location model
│   │       └── MapRegion.kt                  # Offline region definition
│   │
│   ├── presentation/
│   │   ├── map/
│   │   │   ├── MapViewModel.kt               # Map state management
│   │   │   └── MapUiState.kt                 # UI state sealed class
│   │   └── location/
│   │       └── LocationViewModel.kt          # GPS location ViewModel
│   │
│   ├── ui/
│   │   └── map/
│   │       ├── MapScreen.kt                  # Main map Composable
│   │       ├── MapMarkerOverlay.kt           # Project markers layer
│   │       ├── UserCampMarker.kt             # User's camp pin component
│   │       ├── OfflineDownloadSheet.kt       # Tile download UI
│   │       └── MapControls.kt                # Zoom/location buttons
│   │
│   ├── di/
│   │   └── MapModule.kt                      # Koin module for map DI
│   │
│   └── platform/
│       ├── LocationService.kt                # expect declaration
│       └── OfflineMapManager.kt              # expect declaration
│
├── androidMain/kotlin/io/asterixorobelix/afrikaburn/platform/
│   ├── LocationService.android.kt            # Android GPS actual
│   └── OfflineMapManager.android.kt          # Android tile actual
│
└── iosMain/kotlin/io/asterixorobelix/afrikaburn/platform/
    ├── LocationService.ios.kt                # iOS GPS actual
    └── OfflineMapManager.ios.kt              # iOS tile actual
```

### Integration with Existing Architecture

The map feature follows the established patterns:

| Layer | Existing Pattern | Map Feature Alignment |
|-------|------------------|----------------------|
| Data | `JsonResourceDataSource` + Repository | `MapTileDataSource` + `UserCampRepositoryImpl` |
| Domain | `ProjectsRepository` interface | `UserCampRepository`, `LocationRepository` interfaces |
| Presentation | `ProjectsViewModel` with `StateFlow` | `MapViewModel` with `MapUiState` sealed class |
| UI | `ProjectsScreen`, `ProjectDetailScreen` | `MapScreen`, `MapMarkerOverlay` |
| DI | `dataModule`, `domainModule`, `presentationModule` | `mapModule` following same pattern |
| Platform | `CrashLogger` expect/actual | `LocationService`, `OfflineMapManager` expect/actual |

---

## Data Flow

### 1. Map Display Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         MapScreen                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  MapLibreCompose (commonMain)                           │    │
│  │  - Style URL: Offline MBTiles or Online fallback        │    │
│  │  - Annotations: Project markers from ProjectsRepository │    │
│  │  - User location: From LocationService                  │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        MapViewModel                              │
│  - mapUiState: StateFlow<MapUiState>                            │
│  - projectMarkers: StateFlow<List<MapMarker>>                   │
│  - userCampLocation: StateFlow<UserCamp?>                       │
│  - onMarkerClick(markerId: String)                              │
│  - onMapLongPress(latitude: Double, longitude: Double)          │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ ProjectsRepo    │  │ UserCampRepo    │  │ LocationRepo    │
│ (existing)      │  │ (new)           │  │ (new)           │
│                 │  │                 │  │                 │
│ getProjects()   │  │ getUserCamp()   │  │ getLocation()   │
│ → List<Project> │  │ saveUserCamp()  │  │ startTracking() │
└─────────────────┘  └─────────────────┘  └─────────────────┘
        │                    │                    │
        ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ JsonDataSource  │  │ SQLDelight      │  │ LocationService │
│ (existing)      │  │ (new)           │  │ (expect/actual) │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### 2. Marker Tap → Detail Screen Navigation

```kotlin
// MapViewModel.kt
class MapViewModel(
    private val projectsRepository: ProjectsRepository,
    private val userCampRepository: UserCampRepository
) : ViewModel() {

    private val _selectedProject = MutableSharedFlow<ProjectItem?>()
    val selectedProject: SharedFlow<ProjectItem?> = _selectedProject.asSharedFlow()

    fun onMarkerClick(projectCode: String) {
        viewModelScope.launch {
            val projects = projectsRepository.getAllProjects()
            val project = projects.find { it.code == projectCode }
            _selectedProject.emit(project)
        }
    }
}

// MapScreen.kt
@Composable
fun MapScreen(
    viewModel: MapViewModel = koinInject(),
    onProjectSelected: (ProjectItem) -> Unit  // Hoisted to App.kt NavHost
) {
    val selectedProject by viewModel.selectedProject.collectAsState(null)

    LaunchedEffect(selectedProject) {
        selectedProject?.let { onProjectSelected(it) }
    }

    MapLibreMap(
        styleUri = /* ... */,
        onAnnotationClick = { annotation ->
            viewModel.onMarkerClick(annotation.id)
        }
    )
}
```

### 3. Offline Tile Download Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    OfflineDownloadSheet                          │
│  - Download Tankwa region button                                 │
│  - Progress indicator                                            │
│  - Storage space indicator                                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        MapViewModel                              │
│  - downloadState: StateFlow<DownloadState>                       │
│  - downloadRegion(region: MapRegion)                            │
│  - cancelDownload()                                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    OfflineMapManager (expect)                    │
│  - downloadTiles(bounds, minZoom, maxZoom)                       │
│  - deleteOfflineData()                                           │
│  - getDownloadProgress(): Flow<Float>                            │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
┌──────────────────────────┐    ┌──────────────────────────┐
│ Android (actual)          │    │ iOS (actual)             │
│ - OfflineManager          │    │ - MLNOfflineStorage      │
│ - OfflineRegionDefinition │    │ - MLNTilePyramidOffline  │
└──────────────────────────┘    └──────────────────────────┘
```

### 4. User Camp Pin Persistence Flow

```
User Long-Press on Map
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                        MapViewModel                              │
│  onMapLongPress(lat, lng) {                                     │
│      userCampRepository.saveUserCamp(                           │
│          UserCamp(latitude = lat, longitude = lng)              │
│      )                                                          │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    UserCampRepositoryImpl                        │
│  - Uses SQLDelight database                                      │
│  - Single row table (user has one camp)                          │
│  - Replaces on update                                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SQLDelight Database                           │
│                                                                  │
│  CREATE TABLE user_camp (                                        │
│      id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,                  │
│      latitude REAL NOT NULL,                                     │
│      longitude REAL NOT NULL,                                    │
│      name TEXT,                                                  │
│      updated_at INTEGER NOT NULL                                 │
│  );                                                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Platform Abstraction

### LocationService Expect/Actual Pattern

```kotlin
// commonMain: platform/LocationService.kt
package io.asterixorobelix.afrikaburn.platform

import kotlinx.coroutines.flow.Flow

data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

sealed class LocationPermissionState {
    object Granted : LocationPermissionState()
    object Denied : LocationPermissionState()
    object DeniedPermanently : LocationPermissionState()
    object NotRequested : LocationPermissionState()
}

interface LocationService {
    val locationUpdates: Flow<GpsLocation>
    val permissionState: Flow<LocationPermissionState>

    suspend fun requestPermission()
    fun startTracking()
    fun stopTracking()
}

expect fun createLocationService(): LocationService
```

```kotlin
// androidMain: platform/LocationService.android.kt
package io.asterixorobelix.afrikaburn.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class AndroidLocationService(
    private val context: Context
) : LocationService {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _permissionState = MutableStateFlow<LocationPermissionState>(
        LocationPermissionState.NotRequested
    )

    override val permissionState: Flow<LocationPermissionState> = _permissionState.asStateFlow()

    override val locationUpdates: Flow<GpsLocation> = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toGpsLocation())
                }
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override suspend fun requestPermission() {
        // Implementation uses Activity result APIs
    }

    override fun startTracking() { /* Start location updates */ }
    override fun stopTracking() { /* Stop location updates */ }

    private fun Location.toGpsLocation() = GpsLocation(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        timestamp = time
    )
}

actual fun createLocationService(): LocationService {
    // Context provided via Koin
    throw NotImplementedError("Use Koin injection with context parameter")
}
```

```kotlin
// iosMain: platform/LocationService.ios.kt
package io.asterixorobelix.afrikaburn.platform

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import platform.CoreLocation.*
import platform.darwin.NSObject

class IOSLocationService : LocationService, NSObject(), CLLocationManagerDelegateProtocol {

    private val locationManager = CLLocationManager()
    private val _locationUpdates = MutableSharedFlow<GpsLocation>()
    private val _permissionState = MutableStateFlow<LocationPermissionState>(
        LocationPermissionState.NotRequested
    )

    init {
        locationManager.delegate = this
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
    }

    override val locationUpdates: Flow<GpsLocation> = _locationUpdates.asSharedFlow()
    override val permissionState: Flow<LocationPermissionState> = _permissionState.asStateFlow()

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        (didUpdateLocations.lastOrNull() as? CLLocation)?.let { location ->
            _locationUpdates.tryEmit(
                GpsLocation(
                    latitude = location.coordinate.latitude,
                    longitude = location.coordinate.longitude,
                    accuracy = location.horizontalAccuracy.toFloat(),
                    timestamp = (location.timestamp.timeIntervalSince1970 * 1000).toLong()
                )
            )
        }
    }

    override suspend fun requestPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    override fun startTracking() {
        locationManager.startUpdatingLocation()
    }

    override fun stopTracking() {
        locationManager.stopUpdatingLocation()
    }
}

actual fun createLocationService(): LocationService = IOSLocationService()
```

### OfflineMapManager Expect/Actual Pattern

```kotlin
// commonMain: platform/OfflineMapManager.kt
package io.asterixorobelix.afrikaburn.platform

import kotlinx.coroutines.flow.Flow

data class OfflineRegion(
    val name: String,
    val bounds: LatLngBounds,
    val minZoom: Int,
    val maxZoom: Int
)

data class LatLngBounds(
    val northLat: Double,
    val southLat: Double,
    val eastLng: Double,
    val westLng: Double
)

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float, val tilesDownloaded: Long) : DownloadState()
    object Complete : DownloadState()
    data class Error(val message: String) : DownloadState()
}

interface OfflineMapManager {
    val downloadState: Flow<DownloadState>

    suspend fun downloadRegion(region: OfflineRegion)
    suspend fun deleteRegion(name: String)
    suspend fun getDownloadedRegions(): List<String>
    fun cancelDownload()
}

expect fun createOfflineMapManager(): OfflineMapManager
```

### MapLibre Compose Integration

MapLibre Compose (v0.11.1+) handles the platform-specific map rendering internally. The common API is used directly:

```kotlin
// commonMain: ui/map/MapScreen.kt
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinInject(),
    onProjectSelected: (ProjectItem) -> Unit
) {
    val uiState by viewModel.mapUiState.collectAsState()
    val markers by viewModel.projectMarkers.collectAsState()
    val userCamp by viewModel.userCampLocation.collectAsState()

    val cameraState = rememberCameraState {
        // Center on AfrikaBurn venue (Tankwa Town)
        center = Coordinates(latitude = -32.3167, longitude = 19.75)
        zoom = 14.0
    }

    MapLibreMap(
        modifier = modifier.fillMaxSize(),
        styleUri = uiState.styleUri,  // Offline or online style
        cameraState = cameraState,
        gestureSettings = GestureSettings(
            scrollEnabled = true,
            zoomEnabled = true,
            rotateEnabled = true
        ),
        onMapLongClick = { coordinates ->
            viewModel.onMapLongPress(coordinates.latitude, coordinates.longitude)
        }
    ) {
        // Project markers
        markers.forEach { marker ->
            Marker(
                coordinates = Coordinates(marker.latitude, marker.longitude),
                icon = rememberMarkerIcon(marker.type),
                onClick = {
                    viewModel.onMarkerClick(marker.projectCode)
                    true
                }
            ) {
                // Optional callout content
                MarkerCallout(marker.name)
            }
        }

        // User camp marker
        userCamp?.let { camp ->
            Marker(
                coordinates = Coordinates(camp.latitude, camp.longitude),
                icon = rememberUserCampIcon(),
                onClick = {
                    viewModel.onUserCampClick()
                    true
                }
            )
        }
    }
}
```

---

## Build Order

### Phase 1: Foundation (Week 1)

**Dependencies:** None

| Order | Task | Files | Depends On |
|-------|------|-------|------------|
| 1.1 | Add MapLibre Compose dependency | `libs.versions.toml`, `build.gradle.kts` | - |
| 1.2 | Add SQLDelight dependency | `libs.versions.toml`, `build.gradle.kts` | - |
| 1.3 | Create SQLDelight schema | `commonMain/sqldelight/afrikaburn.sq` | 1.2 |
| 1.4 | Create domain models | `domain/model/MapMarker.kt`, `UserCamp.kt`, `MapRegion.kt` | - |
| 1.5 | Create platform interfaces | `platform/LocationService.kt`, `platform/OfflineMapManager.kt` | - |

**Dependencies to add in `libs.versions.toml`:**
```toml
[versions]
maplibreCompose = "0.11.1"
sqldelight = "2.0.2"

[libraries]
maplibre-compose = { module = "org.maplibre.compose:maplibre-compose", version.ref = "maplibreCompose" }
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

### Phase 2: Data Layer (Week 2)

**Dependencies:** Phase 1 complete

| Order | Task | Files | Depends On |
|-------|------|-------|------------|
| 2.1 | Create UserCamp repository interface | `domain/repository/UserCampRepository.kt` | 1.4 |
| 2.2 | Create Location repository interface | `domain/repository/LocationRepository.kt` | 1.4 |
| 2.3 | Implement UserCamp repository with SQLDelight | `data/repository/UserCampRepositoryImpl.kt` | 1.3, 2.1 |
| 2.4 | Implement Android LocationService | `androidMain/.../LocationService.android.kt` | 1.5 |
| 2.5 | Implement iOS LocationService | `iosMain/.../LocationService.ios.kt` | 1.5 |
| 2.6 | Implement Location repository | `data/repository/LocationRepositoryImpl.kt` | 2.2, 2.4, 2.5 |

### Phase 3: Presentation Layer (Week 3)

**Dependencies:** Phase 2 complete

| Order | Task | Files | Depends On |
|-------|------|-------|------------|
| 3.1 | Create MapUiState sealed class | `presentation/map/MapUiState.kt` | - |
| 3.2 | Create MapViewModel | `presentation/map/MapViewModel.kt` | 2.1, 2.2, 3.1 |
| 3.3 | Create Koin MapModule | `di/MapModule.kt` | 2.3, 2.6, 3.2 |
| 3.4 | Update AppModule to include MapModule | `di/AppModule.kt` | 3.3 |

### Phase 4: UI Layer (Week 4)

**Dependencies:** Phase 3 complete

| Order | Task | Files | Depends On |
|-------|------|-------|------------|
| 4.1 | Create basic MapScreen | `ui/map/MapScreen.kt` | 3.2 |
| 4.2 | Create MapMarkerOverlay | `ui/map/MapMarkerOverlay.kt` | 4.1 |
| 4.3 | Create UserCampMarker | `ui/map/UserCampMarker.kt` | 4.1 |
| 4.4 | Create MapControls | `ui/map/MapControls.kt` | 4.1 |
| 4.5 | Add Map navigation destination | `navigation/NavigationDestination.kt` | 4.1 |
| 4.6 | Integrate MapScreen into NavHost | `App.kt` | 4.1, 4.5 |

### Phase 5: Offline Tiles (Week 5)

**Dependencies:** Phase 4 complete

| Order | Task | Files | Depends On |
|-------|------|-------|------------|
| 5.1 | Implement Android OfflineMapManager | `androidMain/.../OfflineMapManager.android.kt` | 1.5 |
| 5.2 | Implement iOS OfflineMapManager | `iosMain/.../OfflineMapManager.ios.kt` | 1.5 |
| 5.3 | Create OfflineDownloadSheet UI | `ui/map/OfflineDownloadSheet.kt` | 5.1, 5.2 |
| 5.4 | Add offline state to MapViewModel | `presentation/map/MapViewModel.kt` | 5.1, 5.2 |
| 5.5 | Pre-configure Tankwa region bounds | `domain/model/MapRegion.kt` | 5.3 |

### Phase 6: Integration & Polish (Week 6)

**Dependencies:** Phase 5 complete

| Order | Task | Files | Depends On |
|-------|------|-------|------------|
| 6.1 | Connect marker tap to ProjectDetailScreen | `App.kt`, `ui/map/MapScreen.kt` | 4.6 |
| 6.2 | Add location permission handling | `ui/map/MapScreen.kt` | 2.4, 2.5 |
| 6.3 | Add string resources for map | `composeResources/values/strings.xml` | - |
| 6.4 | Write unit tests for MapViewModel | `commonTest/.../MapViewModelTest.kt` | 3.2 |
| 6.5 | Write repository tests | `commonTest/.../UserCampRepositoryTest.kt` | 2.3 |
| 6.6 | Update iOS privacy manifest | `iosApp/PrivacyInfo.xcprivacy` | 2.5 |

---

## Integration Points

### 1. Navigation Integration

The map screen integrates with the existing navigation structure:

```kotlin
// navigation/NavigationDestination.kt (modified)
sealed class NavigationDestination(/* ... */) {
    // Existing destinations...

    object Map : NavigationDestination(
        route = "map",
        title = "Map",
        contentDescription = "Map icon",
        icon = Icons.Default.Map
    )

    companion object {
        val allDestinations: List<NavigationDestination> by lazy {
            listOf(Projects, Map, Directions, About)  // Map added
        }
    }
}
```

```kotlin
// App.kt (modified NavHost)
NavHost(/* ... */) {
    // Existing routes...

    composable(NavigationDestination.Map.route) {
        MapScreen(
            onProjectSelected = { project ->
                selectedProject = project
                navController.navigate(PROJECT_DETAIL_ROUTE)
            }
        )
    }
}
```

### 2. ProjectsRepository Integration

The map uses the existing `ProjectsRepository` to get project locations:

```kotlin
// MapViewModel.kt
class MapViewModel(
    private val projectsRepository: ProjectsRepository,  // Existing
    private val userCampRepository: UserCampRepository,  // New
    private val locationRepository: LocationRepository   // New
) : ViewModel() {

    val projectMarkers: StateFlow<List<MapMarker>> = flow {
        val allProjects = mutableListOf<ProjectItem>()
        ProjectType.entries.forEach { type ->
            projectsRepository.getProjectsByType(type)
                .onSuccess { allProjects.addAll(it) }
        }
        emit(allProjects.mapNotNull { it.toMapMarker() })
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
```

### 3. Koin DI Integration

New module follows existing pattern:

```kotlin
// di/MapModule.kt
val mapModule = module {
    // Data sources
    single { createSqlDelightDriver() }
    single { AfrikaBurnDatabase(get()) }

    // Repositories
    single<UserCampRepository> { UserCampRepositoryImpl(get()) }
    single<LocationRepository> { LocationRepositoryImpl(get()) }

    // Platform services
    single { createLocationService() }
    single { createOfflineMapManager() }

    // ViewModels
    factory { MapViewModel(get(), get(), get()) }
}

// di/AppModule.kt (modified)
val appModule = module {
    includes(
        dataModule,
        domainModule,
        presentationModule,
        crashLoggingModule,
        mapModule  // Added
    )
}
```

### 4. SQLDelight Database Integration

New database schema alongside existing data sources:

```sql
-- commonMain/sqldelight/io/asterixorobelix/afrikaburn/AfrikaBurn.sq

-- User's camp location (single row, replaced on update)
CREATE TABLE user_camp (
    id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    name TEXT,
    updated_at INTEGER NOT NULL
);

-- Insert or replace user camp
insertOrReplace:
INSERT OR REPLACE INTO user_camp (id, latitude, longitude, name, updated_at)
VALUES (1, ?, ?, ?, ?);

-- Get user camp
getUserCamp:
SELECT * FROM user_camp WHERE id = 1;

-- Delete user camp
deleteUserCamp:
DELETE FROM user_camp WHERE id = 1;

-- Offline regions metadata (for tracking downloaded areas)
CREATE TABLE offline_region (
    name TEXT PRIMARY KEY NOT NULL,
    north_lat REAL NOT NULL,
    south_lat REAL NOT NULL,
    east_lng REAL NOT NULL,
    west_lng REAL NOT NULL,
    min_zoom INTEGER NOT NULL,
    max_zoom INTEGER NOT NULL,
    downloaded_at INTEGER NOT NULL
);

insertOfflineRegion:
INSERT OR REPLACE INTO offline_region (name, north_lat, south_lat, east_lng, west_lng, min_zoom, max_zoom, downloaded_at)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);

getOfflineRegion:
SELECT * FROM offline_region WHERE name = ?;

getAllOfflineRegions:
SELECT name FROM offline_region;

deleteOfflineRegion:
DELETE FROM offline_region WHERE name = ?;
```

### 5. Existing Detail Screen Navigation

Map markers navigate to the existing `ProjectDetailScreen`:

```kotlin
// App.kt - Navigation flow
MapScreen(
    onProjectSelected = { project ->
        // Uses same pattern as ProjectsScreen
        selectedProject = project
        navController.navigate(PROJECT_DETAIL_ROUTE)
    }
)
```

### 6. Theme Integration

Map UI components follow existing Material 3 theming:

```kotlin
// ui/map/MapControls.kt
@Composable
fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onMyLocation: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(Dimens.paddingMedium),  // Uses existing Dimens
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        FloatingActionButton(
            onClick = onMyLocation,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = stringResource(Res.string.cd_my_location)
            )
        }
    }
}
```

---

## Risk Considerations

### Platform-Specific Risks

| Risk | Platform | Mitigation |
|------|----------|------------|
| iOS location permission dialogs | iOS | Add `NSLocationWhenInUseUsageDescription` to Info.plist |
| Background location on Android | Android | Only request foreground permission initially |
| MapLibre Compose API changes | Both | Pin to specific version (0.11.1+) |
| Offline tile storage limits | Both | Limit zoom levels (12-16) for reasonable size |
| SQLDelight WASM limitations | Web | Not targeting web platform currently |

### Performance Considerations

| Concern | Impact | Solution |
|---------|--------|----------|
| Large marker count | UI jank | Use clustering for 50+ markers |
| Offline tile size | Storage | Pre-define Tankwa region (~50MB at zoom 12-16) |
| Location updates | Battery | Use 5-second intervals, stop when backgrounded |
| Map style loading | Startup | Cache style JSON locally |

---

## Testing Strategy

### Unit Tests (commonTest)

```kotlin
// MapViewModelTest.kt
class MapViewModelTest {
    @Test
    fun `onMarkerClick emits selected project`() = runTest {
        val mockRepo = MockProjectsRepository()
        val viewModel = MapViewModel(mockRepo, mockUserCampRepo, mockLocationRepo)

        viewModel.onMarkerClick("ART-2024-042")

        val selected = viewModel.selectedProject.first()
        assertEquals("ART-2024-042", selected?.code)
    }

    @Test
    fun `onMapLongPress saves user camp location`() = runTest {
        val viewModel = MapViewModel(mockProjectsRepo, mockUserCampRepo, mockLocationRepo)

        viewModel.onMapLongPress(-32.3167, 19.75)

        verify { mockUserCampRepo.saveUserCamp(any()) }
    }
}
```

### Integration Tests

- Test SQLDelight queries on both Android and iOS
- Verify location permission flows on real devices
- Test offline tile download/retrieval cycle

---

## References

### Official Documentation
- [MapLibre Compose](https://maplibre.org/maplibre-compose/) - Official KMP map library
- [MapLibre Compose GitHub](https://github.com/maplibre/maplibre-compose) - Source and examples
- [SQLDelight Documentation](https://sqldelight.github.io/sqldelight/2.2.1/) - KMP database
- [Kotlin Multiplatform Location Services](https://github.com/jordond/compass) - Location toolkit

### Community Resources
- [Maps with Compose Multiplatform Part 1](https://medium.com/@youssef.mu.saber/maps-with-compose-multiplatform-part-1-ee328939b913)
- [Maps with Compose Multiplatform Part 2](https://medium.com/@youssef.mu.saber/maps-with-compose-multiplatform-part-2-0f26bb127644)
- [A Practical Guide to MapLibre Compose](https://medium.com/@joy458963214/a-practical-guide-to-maplibre-compose-94a8cb6f79c4)
- [MapLibre Offline Demo](https://github.com/TonyGnk/map-libre-demo) - Android offline implementation
- [Building a KMP SDK for Location Services](https://medium.com/rapido-labs/building-a-kotlin-multiplatform-mobile-sdk-for-location-related-services-488a2855ab23)

---
*Research completed: 2026-01-18*
