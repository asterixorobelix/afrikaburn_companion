package io.asterixorobelix.afrikaburn.presentation.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the map screen.
 *
 * Manages camera position state and handles map interactions.
 * Initially loads with Tankwa Karoo center coordinates.
 */
class MapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)

    /**
     * The current UI state of the map screen.
     */
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // Initialize with default Tankwa Karoo center position
        _uiState.value = MapUiState.Success()
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
        _uiState.value = MapUiState.Success()
    }
}
