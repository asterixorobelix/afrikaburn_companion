package io.asterixorobelix.afrikaburn.presentation.map

import afrikaburn.composeapp.generated.resources.Res
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.platform.LocationService
import io.asterixorobelix.afrikaburn.platform.PermissionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * ViewModel for the map screen.
 *
 * Manages camera position state, loads project data, handles map interactions,
 * and provides location tracking for user position display.
 */
class MapViewModel(
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)

    /**
     * The current UI state of the map screen.
     */
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var loadedProjects: List<ProjectItem> = emptyList()
    private var locationJob: Job? = null

    init {
        loadProjects()
    }

    /**
     * Loads camp and artwork data from JSON resources.
     */
    @OptIn(ExperimentalResourceApi::class)
    private fun loadProjects() {
        viewModelScope.launch {
            try {
                val campsJson = Res.readBytes("files/WTFThemeCamps.json").decodeToString()
                val camps = json.decodeFromString<List<ProjectItem>>(campsJson)

                val artworksJson = Res.readBytes("files/WTFArtworks.json").decodeToString()
                val artworks = json.decodeFromString<List<ProjectItem>>(artworksJson)

                loadedProjects = camps + artworks

                _uiState.value = MapUiState.Success(projects = loadedProjects)
            } catch (@Suppress("SwallowedException") e: Exception) {
                // Even if loading fails, show map with default position but no projects
                _uiState.value = MapUiState.Success(projects = emptyList())
            }
        }
    }

    /**
     * Finds a project by its code, supporting comma-separated codes.
     *
     * @param code The code from a tapped GeoJSON feature
     * @return The matching ProjectItem, or null if not found
     */
    fun findProjectByCode(code: String): ProjectItem? {
        return loadedProjects.find { project ->
            // Handle comma-separated codes like "dis, ele"
            project.code.split(",")
                .map { it.trim() }
                .any { it.equals(code.trim(), ignoreCase = true) }
        }
    }

    /**
     * Called when the camera position changes due to user interaction.
     *
     * @param latitude The new center latitude
     * @param longitude The new center longitude
     * @param zoom The new zoom level
     */
    fun onCameraPositionChanged(latitude: Double, longitude: Double, zoom: Double) {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success) {
            _uiState.value = currentState.copy(
                centerLatitude = latitude,
                centerLongitude = longitude,
                zoomLevel = zoom
            )
        }
    }

    /**
     * Called when the map fails to load.
     *
     * @param message The error message describing what went wrong
     */
    fun onMapLoadError(message: String) {
        _uiState.value = MapUiState.Error(message)
    }

    /**
     * Resets the map to its initial state with default coordinates.
     */
    fun resetToDefaultPosition() {
        _uiState.value = MapUiState.Success(projects = loadedProjects)
    }

    // ===== Location Tracking Methods =====

    /**
     * Check location permission and start tracking if granted.
     */
    fun checkAndRequestLocation() {
        viewModelScope.launch {
            val permission = locationService.checkPermission()
            updatePermissionState(permission)

            if (permission == PermissionState.GRANTED) {
                startLocationTracking()
            }
        }
    }

    /**
     * Request location permission from the user.
     */
    fun requestLocationPermission() {
        viewModelScope.launch {
            val permission = locationService.requestPermission()
            updatePermissionState(permission)

            if (permission == PermissionState.GRANTED) {
                startLocationTracking()
            }
        }
    }

    /**
     * Start tracking user location.
     */
    private fun startLocationTracking() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationService.startLocationUpdates()
                .catch { /* Log error, continue without location */ }
                .collect { location ->
                    updateUserLocation(location.latitude, location.longitude)
                }
        }
        updateTrackingState(isTracking = true)
    }

    /**
     * Stop location tracking to conserve battery.
     */
    fun stopLocationTracking() {
        locationJob?.cancel()
        locationJob = null
        locationService.stopLocationUpdates()
        updateTrackingState(isTracking = false)
    }

    /**
     * Center map on user's current location.
     */
    fun centerOnUserLocation() {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success && currentState.hasUserLocation) {
            _uiState.value = currentState.copy(
                centerLatitude = currentState.userLatitude!!,
                centerLongitude = currentState.userLongitude!!
            )
        }
    }

    private fun updateUserLocation(lat: Double, lng: Double) {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success) {
            _uiState.value = currentState.copy(
                userLatitude = lat,
                userLongitude = lng
            )
        }
    }

    private fun updatePermissionState(state: PermissionState) {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success) {
            _uiState.value = currentState.copy(locationPermissionState = state)
        }
    }

    private fun updateTrackingState(isTracking: Boolean) {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success) {
            _uiState.value = currentState.copy(isTrackingLocation = isTracking)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking() // Clean up when ViewModel is destroyed
    }
}
