package io.asterixorobelix.afrikaburn.presentation.map

import afrikaburn.composeapp.generated.resources.Res
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.models.ProjectItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * ViewModel for the map screen.
 *
 * Manages camera position state, loads project data, and handles map interactions.
 * Initially loads with Tankwa Karoo center coordinates.
 */
class MapViewModel : ViewModel() {

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
            } catch (e: Exception) {
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
}
