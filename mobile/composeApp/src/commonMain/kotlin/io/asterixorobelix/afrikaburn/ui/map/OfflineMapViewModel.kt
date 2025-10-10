package io.asterixorobelix.afrikaburn.ui.map

import androidx.compose.ui.geometry.Offset
import io.asterixorobelix.afrikaburn.models.MapCoordinates
import io.asterixorobelix.afrikaburn.models.MapLocation
import io.asterixorobelix.afrikaburn.models.MapLocationType
import io.asterixorobelix.afrikaburn.domain.repository.MapRepository
import io.asterixorobelix.afrikaburn.domain.repository.LocationRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OfflineMapUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val userLocation: MapCoordinates? = null,
    val userCampId: String? = null,
    val selectedLocation: MapLocation? = null,
    val camps: List<MapLocation> = emptyList(),
    val artInstallations: List<MapLocation> = emptyList(),
    val facilities: List<MapLocation> = emptyList(),
    val emergencyPoints: List<MapLocation> = emptyList(),
    val showCamps: Boolean = true,
    val showArt: Boolean = true,
    val showFacilities: Boolean = true,
    val showEmergency: Boolean = false
) {
    val allLocations: List<MapLocation>
        get() = buildList {
            if (showCamps) addAll(camps)
            if (showArt) addAll(artInstallations)
            if (showFacilities) addAll(facilities)
            if (showEmergency) addAll(emergencyPoints)
        }
}

enum class MapLayer {
    CAMPS, ART, FACILITIES, EMERGENCY
}

open class OfflineMapViewModel(
    private val mapRepository: MapRepository? = null,
    private val locationRepository: LocationRepository? = null
) : ViewModel() {
    
    companion object {
        private const val ZOOM_IN_FACTOR = 1.2f
        private const val ZOOM_OUT_FACTOR = 0.8f
        private const val MAX_ZOOM = 5f
        private const val MIN_ZOOM = 0.5f
        private const val CENTER_OFFSET = 500f
        private const val CENTER_ZOOM_SCALE = 2f
    }
    
    protected val uiStateInternal = MutableStateFlow(OfflineMapUiState())
    val uiState: StateFlow<OfflineMapUiState> = uiStateInternal.asStateFlow()
    
    init {
        loadMapData()
        observeUserLocation()
    }
    
    private fun loadMapData() {
        viewModelScope.launch {
            uiStateInternal.update { it.copy(isLoading = true) }
            
            try {
                // Load map locations from repository
                // Implement proper data loading once MapRepository methods are available
                uiStateInternal.update { it.copy(isLoading = false) }
            } catch (e: RuntimeException) {
                uiStateInternal.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load map data: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun observeUserLocation() {
        viewModelScope.launch {
            locationRepository?.getCurrentLocation()?.collect { location ->
                uiStateInternal.update { it.copy(userLocation = location) }
            }
        }
    }
    
    fun toggleLayer(layer: MapLayer) {
        uiStateInternal.update { state ->
            when (layer) {
                MapLayer.CAMPS -> state.copy(showCamps = !state.showCamps)
                MapLayer.ART -> state.copy(showArt = !state.showArt)
                MapLayer.FACILITIES -> state.copy(showFacilities = !state.showFacilities)
                MapLayer.EMERGENCY -> state.copy(showEmergency = !state.showEmergency)
            }
        }
    }
    
    fun updateMapTransform(scale: Float, offset: Offset) {
        uiStateInternal.update { it.copy(scale = scale, offset = offset) }
    }
    
    fun zoomIn() {
        val currentScale = uiStateInternal.value.scale
        val newScale = (currentScale * ZOOM_IN_FACTOR).coerceAtMost(MAX_ZOOM)
        uiStateInternal.update { it.copy(scale = newScale) }
    }
    
    fun zoomOut() {
        val currentScale = uiStateInternal.value.scale
        val newScale = (currentScale * ZOOM_OUT_FACTOR).coerceAtLeast(MIN_ZOOM)
        uiStateInternal.update { it.copy(scale = newScale) }
    }
    
    fun centerOnUserLocation() {
        uiStateInternal.value.userLocation?.let { location ->
            // Calculate offset to center the user location
            // This is simplified - in real app would consider viewport size
            val centerOffset = Offset(-location.x + CENTER_OFFSET, -location.y + CENTER_OFFSET)
            uiStateInternal.update { 
                it.copy(
                    offset = centerOffset,
                    scale = CENTER_ZOOM_SCALE
                )
            }
        }
    }
    
    fun selectLocation(location: MapLocation) {
        uiStateInternal.update { it.copy(selectedLocation = location) }
    }
    
    fun clearSelection() {
        uiStateInternal.update { it.copy(selectedLocation = null) }
    }
    
    fun markAsUserCamp(location: MapLocation) {
        viewModelScope.launch {
            try {
                // Implement once MapRepository has setUserCampId method
                uiStateInternal.update { 
                    it.copy(
                        userCampId = location.id,
                        selectedLocation = null
                    )
                }
            } catch (e: RuntimeException) {
                uiStateInternal.update { 
                    it.copy(error = "Failed to save camp location: ${e.message}")
                }
            }
        }
    }
}