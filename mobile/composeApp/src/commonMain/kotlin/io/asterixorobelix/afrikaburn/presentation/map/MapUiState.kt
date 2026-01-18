package io.asterixorobelix.afrikaburn.presentation.map

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
     */
    data class Success(
        val centerLatitude: Double = DEFAULT_CENTER_LATITUDE,
        val centerLongitude: Double = DEFAULT_CENTER_LONGITUDE,
        val zoomLevel: Double = DEFAULT_ZOOM_LEVEL
    ) : MapUiState

    /**
     * An error occurred while loading the map.
     *
     * @param message The error message to display
     */
    data class Error(val message: String) : MapUiState

    companion object {
        /** Tankwa Karoo center latitude (approximately) */
        const val DEFAULT_CENTER_LATITUDE = -32.35

        /** Tankwa Karoo center longitude (approximately) */
        const val DEFAULT_CENTER_LONGITUDE = 19.45

        /** Default zoom level for initial map display */
        const val DEFAULT_ZOOM_LEVEL = 12.0
    }
}
