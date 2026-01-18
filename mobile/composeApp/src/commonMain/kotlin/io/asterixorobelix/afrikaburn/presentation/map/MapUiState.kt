package io.asterixorobelix.afrikaburn.presentation.map

import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.platform.PermissionState

/**
 * UI state for the map screen.
 *
 * Represents the different states the map can be in:
 * - Loading: Initial state while map resources are being prepared
 * - Success: Map is ready with camera position
 * - Error: Something went wrong loading the map
 */
sealed interface MapUiState {
    /**
     * Map is loading/initializing.
     */
    data object Loading : MapUiState

    /**
     * Map is ready and displaying.
     *
     * @param centerLatitude The latitude of the map center (default: Tankwa Karoo center)
     * @param centerLongitude The longitude of the map center (default: Tankwa Karoo center)
     * @param zoomLevel The current zoom level (default: 12.0 for overview)
     * @param projects List of projects for marker tap matching
     * @param userLatitude The user's current GPS latitude (null if not tracking)
     * @param userLongitude The user's current GPS longitude (null if not tracking)
     * @param locationPermissionState The current location permission state
     * @param isTrackingLocation Whether location tracking is currently active
     */
    data class Success(
        val centerLatitude: Double = DEFAULT_CENTER_LATITUDE,
        val centerLongitude: Double = DEFAULT_CENTER_LONGITUDE,
        val zoomLevel: Double = DEFAULT_ZOOM_LEVEL,
        val projects: List<ProjectItem> = emptyList(),
        val userLatitude: Double? = null,
        val userLongitude: Double? = null,
        val locationPermissionState: PermissionState = PermissionState.NOT_DETERMINED,
        val isTrackingLocation: Boolean = false
    ) : MapUiState {
        /** True if we have valid user location coordinates */
        val hasUserLocation: Boolean
            get() = userLatitude != null && userLongitude != null
    }

    /**
     * An error occurred while loading the map.
     *
     * @param message The error message to display
     */
    data class Error(val message: String) : MapUiState

    companion object {
        /** AfrikaBurn event location latitude (from Directions tab) */
        const val DEFAULT_CENTER_LATITUDE = -32.482474

        /** AfrikaBurn event location longitude (from Directions tab) */
        const val DEFAULT_CENTER_LONGITUDE = 19.897824

        /** Default zoom level for initial map display */
        const val DEFAULT_ZOOM_LEVEL = 14.0
    }
}
