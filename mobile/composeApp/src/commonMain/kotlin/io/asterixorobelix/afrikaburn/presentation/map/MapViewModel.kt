package io.asterixorobelix.afrikaburn.presentation.map

import afrikaburn.composeapp.generated.resources.Res
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinRepository
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.platform.LocationService
import io.asterixorobelix.afrikaburn.platform.PermissionState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
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
 * provides location tracking for user position display, and handles camp pin operations.
 */
class MapViewModel(
    private val locationService: LocationService,
    private val userCampPinRepository: UserCampPinRepository
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
    private var campPinJob: Job? = null

    init {
        loadProjects()
        observeCampPin()
    }

    /**
     * Observe camp pin changes from database.
     */
    private fun observeCampPin() {
        campPinJob?.cancel()
        campPinJob = viewModelScope.launch {
            userCampPinRepository.observeCampPin().collect { pin ->
                val currentState = _uiState.value
                if (currentState is MapUiState.Success) {
                    _uiState.value = currentState.copy(
                        userCampPin = pin?.let {
                            CampPinState.Placed(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                name = it.name
                            )
                        } ?: CampPinState.None
                    )
                }
            }
        }
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
            } catch (@Suppress("SwallowedException", "TooGenericExceptionCaught") e: Exception) {
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
     * Check location permission state without requesting.
     *
     * Updates the UI state with the current permission state.
     * If permission is already granted, starts location tracking.
     * If NOT_DETERMINED, the UI layer should trigger the permission request.
     */
    fun checkLocationPermission() {
        viewModelScope.launch {
            val permission = locationService.checkPermission()
            updatePermissionState(permission)

            if (permission == PermissionState.GRANTED) {
                startLocationTracking()
            }
            // If NOT_DETERMINED, the UI layer will trigger requestPermission()
        }
    }

    /**
     * Handle the result from the UI-level permission request.
     *
     * Called by the Composable after the system permission dialog completes.
     *
     * @param granted True if location permission was granted
     */
    fun onPermissionResult(granted: Boolean) {
        val newState = if (granted) PermissionState.GRANTED else PermissionState.DENIED
        updatePermissionState(newState)

        if (granted) {
            startLocationTracking()
        }
    }

    /**
     * Check location permission and start tracking if granted.
     * @deprecated Use checkLocationPermission() and onPermissionResult() instead
     */
    @Deprecated("Use checkLocationPermission() and onPermissionResult() for proper Android permission handling")
    fun checkAndRequestLocation() {
        checkLocationPermission()
    }

    /**
     * Request location permission from the user.
     * @deprecated Use rememberLocationPermissionLauncher in UI layer instead
     */
    @Deprecated("Use rememberLocationPermissionLauncher in UI layer for Android permission handling")
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
     *
     * Increments the centerOnUserLocationRequest counter to trigger camera animation
     * in the UI layer via LaunchedEffect.
     */
    fun centerOnUserLocation() {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success && currentState.hasUserLocation) {
            _uiState.value = currentState.copy(
                centerOnUserLocationRequest = currentState.centerOnUserLocationRequest + 1
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
        campPinJob?.cancel()
    }

    // ===== Camp Pin Methods =====

    /**
     * Called when user long-presses on map.
     * If no pin exists, show place confirmation.
     * If pin exists and long-press is near pin, show options.
     * If pin exists and long-press elsewhere, show move confirmation.
     */
    fun onMapLongPress(latitude: Double, longitude: Double) {
        val currentState = _uiState.value
        if (currentState !is MapUiState.Success) return

        when (val pinState = currentState.userCampPin) {
            is CampPinState.None -> {
                // No pin - show place confirmation
                _uiState.value = currentState.copy(
                    campPinDialogState = CampPinDialogState.ConfirmPlace(latitude, longitude)
                )
            }
            is CampPinState.Placed -> {
                // Check if long-press is near existing pin (within ~50 meters)
                val isNearPin = isNearLocation(
                    lat1 = latitude, lon1 = longitude,
                    lat2 = pinState.latitude, lon2 = pinState.longitude,
                    thresholdMeters = NEAR_PIN_THRESHOLD_METERS
                )
                if (isNearPin) {
                    // Show pin options (move/delete)
                    _uiState.value = currentState.copy(
                        campPinDialogState = CampPinDialogState.PinOptions
                    )
                } else {
                    // Show move confirmation
                    _uiState.value = currentState.copy(
                        campPinDialogState = CampPinDialogState.ConfirmMove(latitude, longitude)
                    )
                }
            }
        }
    }

    /**
     * Dismiss any open dialog.
     */
    fun dismissDialog() {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success) {
            _uiState.value = currentState.copy(campPinDialogState = CampPinDialogState.Hidden)
        }
    }

    /**
     * Confirm placing pin at the pending location.
     */
    fun confirmPlacePin() {
        val currentState = _uiState.value
        if (currentState !is MapUiState.Success) return

        val dialogState = currentState.campPinDialogState
        if (dialogState is CampPinDialogState.ConfirmPlace) {
            viewModelScope.launch {
                userCampPinRepository.saveCampPin(
                    latitude = dialogState.latitude,
                    longitude = dialogState.longitude
                )
            }
            _uiState.value = currentState.copy(campPinDialogState = CampPinDialogState.Hidden)
        }
    }

    /**
     * Show delete confirmation dialog.
     */
    fun showDeleteConfirmation() {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success) {
            _uiState.value = currentState.copy(campPinDialogState = CampPinDialogState.ConfirmDelete)
        }
    }

    /**
     * Confirm deleting the pin.
     */
    fun confirmDeletePin() {
        val currentState = _uiState.value
        if (currentState is MapUiState.Success) {
            viewModelScope.launch {
                userCampPinRepository.deleteCampPin()
            }
            _uiState.value = currentState.copy(campPinDialogState = CampPinDialogState.Hidden)
        }
    }

    /**
     * Confirm moving pin to the pending location.
     */
    fun confirmMovePin() {
        val currentState = _uiState.value
        if (currentState !is MapUiState.Success) return

        val dialogState = currentState.campPinDialogState
        if (dialogState is CampPinDialogState.ConfirmMove) {
            viewModelScope.launch {
                userCampPinRepository.updateLocation(
                    latitude = dialogState.newLatitude,
                    longitude = dialogState.newLongitude
                )
            }
            _uiState.value = currentState.copy(campPinDialogState = CampPinDialogState.Hidden)
        }
    }

    /**
     * Simple distance check using haversine approximation.
     */
    private fun isNearLocation(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        thresholdMeters: Double
    ): Boolean {
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS_METERS * c
        return distance <= thresholdMeters
    }

    /**
     * Convert degrees to radians (Kotlin multiplatform compatible).
     */
    private fun toRadians(degrees: Double): Double = degrees * kotlin.math.PI / DEGREES_IN_HALF_CIRCLE

    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0
        private const val NEAR_PIN_THRESHOLD_METERS = 50.0
        private const val DEGREES_IN_HALF_CIRCLE = 180.0
    }
}
