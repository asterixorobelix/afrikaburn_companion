package io.asterixorobelix.afrikaburn.presentation.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.model.ThemeCamp
import io.asterixorobelix.afrikaburn.domain.repository.ThemeCampRepository
import io.asterixorobelix.afrikaburn.domain.usecase.GetEventsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ThemeCampsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allCamps: List<ThemeCamp> = emptyList(),
    val filteredCamps: List<ThemeCamp> = emptyList(),
    val searchQuery: String = "",
    val selectedActivities: Set<String> = emptySet(),
    val selectedAmenities: Set<String> = emptySet(),
    val favoriteCampIds: Set<String> = emptySet(),
    val availableActivities: List<String> = emptyList(),
    val availableAmenities: List<String> = emptyList()
) {
    val hasActiveFilters: Boolean
        get() = selectedActivities.isNotEmpty() || selectedAmenities.isNotEmpty()
}

class ThemeCampsViewModel(
    private val getEventsUseCase: GetEventsUseCase,
    private val themeCampRepository: ThemeCampRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ThemeCampsUiState())
    val uiState: StateFlow<ThemeCampsUiState> = _uiState.asStateFlow()
    
    init {
        loadThemeCamps()
    }
    
    private fun loadThemeCamps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentEventResult = getEventsUseCase.getCurrentEvent()
                
                currentEventResult.fold(
                    onSuccess = { currentEvent ->
                        themeCampRepository.observeThemeCampsByEvent(currentEvent.id)
                    .catch { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to load theme camps: ${exception.message}"
                            ) 
                        }
                    }
                    .collect { camps ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                allCamps = camps,
                                filteredCamps = filterCamps(
                                    camps = camps,
                                    searchQuery = state.searchQuery,
                                    activityFilters = state.selectedActivities,
                                    amenityFilters = state.selectedAmenities
                                ),
                                availableActivities = extractUniqueActivities(camps),
                                availableAmenities = extractUniqueAmenities(camps)
                            )
                        }
                    }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "No current event found: ${exception.message}"
                            ) 
                        }
                    }
                )
                
                // Load favorites from local storage
                loadFavorites()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load event: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val favoriteIds = themeCampRepository.getFavoriteCampIds()
                _uiState.update { it.copy(favoriteCampIds = favoriteIds) }
            } catch (e: Exception) {
                // Ignore favorites loading errors
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCamps = filterCamps(
                    camps = state.allCamps,
                    searchQuery = query,
                    activityFilters = state.selectedActivities,
                    amenityFilters = state.selectedAmenities
                )
            )
        }
    }
    
    fun toggleActivityFilter(activity: String) {
        _uiState.update { state ->
            val newActivities = if (state.selectedActivities.contains(activity)) {
                state.selectedActivities - activity
            } else {
                state.selectedActivities + activity
            }
            
            state.copy(
                selectedActivities = newActivities,
                filteredCamps = filterCamps(
                    camps = state.allCamps,
                    searchQuery = state.searchQuery,
                    activityFilters = newActivities,
                    amenityFilters = state.selectedAmenities
                )
            )
        }
    }
    
    fun toggleAmenityFilter(amenity: String) {
        _uiState.update { state ->
            val newAmenities = if (state.selectedAmenities.contains(amenity)) {
                state.selectedAmenities - amenity
            } else {
                state.selectedAmenities + amenity
            }
            
            state.copy(
                selectedAmenities = newAmenities,
                filteredCamps = filterCamps(
                    camps = state.allCamps,
                    searchQuery = state.searchQuery,
                    activityFilters = state.selectedActivities,
                    amenityFilters = newAmenities
                )
            )
        }
    }
    
    fun toggleFavorite(campId: String) {
        viewModelScope.launch {
            try {
                val currentFavorites = _uiState.value.favoriteCampIds
                val newFavorites = if (currentFavorites.contains(campId)) {
                    themeCampRepository.removeFavoriteCamp(campId)
                    currentFavorites - campId
                } else {
                    themeCampRepository.addFavoriteCamp(campId)
                    currentFavorites + campId
                }
                
                _uiState.update { it.copy(favoriteCampIds = newFavorites) }
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
    
    private fun filterCamps(
        camps: List<ThemeCamp>,
        searchQuery: String,
        activityFilters: Set<String>,
        amenityFilters: Set<String>
    ): List<ThemeCamp> {
        return camps.filter { camp ->
            val matchesSearch = searchQuery.isBlank() || 
                camp.name.contains(searchQuery, ignoreCase = true) ||
                camp.description?.contains(searchQuery, ignoreCase = true) == true
            
            val matchesActivities = activityFilters.isEmpty() ||
                activityFilters.any { filter -> 
                    camp.activities.any { it.contains(filter, ignoreCase = true) }
                }
            
            val matchesAmenities = amenityFilters.isEmpty() ||
                amenityFilters.any { filter ->
                    camp.amenities.any { it.contains(filter, ignoreCase = true) }
                }
            
            matchesSearch && matchesActivities && matchesAmenities
        }
    }
    
    private fun extractUniqueActivities(camps: List<ThemeCamp>): List<String> {
        return camps
            .flatMap { it.activities }
            .distinct()
            .sorted()
    }
    
    private fun extractUniqueAmenities(camps: List<ThemeCamp>): List<String> {
        return camps
            .flatMap { it.amenities }
            .distinct()
            .sorted()
    }
}