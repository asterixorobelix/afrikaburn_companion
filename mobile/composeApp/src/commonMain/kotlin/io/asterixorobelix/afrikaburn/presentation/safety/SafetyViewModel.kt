package io.asterixorobelix.afrikaburn.presentation.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.model.EmergencyContact
import io.asterixorobelix.afrikaburn.domain.model.ResourceLocation
import io.asterixorobelix.afrikaburn.domain.model.ResourceType
import io.asterixorobelix.afrikaburn.domain.model.EmergencyContactType
import io.asterixorobelix.afrikaburn.domain.repository.EventRepository
import io.asterixorobelix.afrikaburn.domain.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SafetyUiState(
    val isLoading: Boolean = false,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val resourceLocations: List<ResourceLocation> = emptyList(),
    val selectedTab: SafetyTab = SafetyTab.EMERGENCY,
    val selectedContact: EmergencyContact? = null,
    val selectedResource: ResourceLocation? = null,
    val nearestWaterPoint: ResourceLocation? = null,
    val nearestMedicalPoint: ResourceLocation? = null,
    val error: String? = null,
    val userLocation: Pair<Double, Double>? = null
)

enum class SafetyTab {
    EMERGENCY, RESOURCES, SAFETY_TIPS
}

class SafetyViewModel(
    private val eventRepository: EventRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyUiState())
    val uiState: StateFlow<SafetyUiState> = _uiState.asStateFlow()

    init {
        loadSafetyData()
        observeUserLocation()
    }

    private fun loadSafetyData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Get current event
                val currentEvent = eventRepository.getCurrentEvent()
                
                if (currentEvent != null) {
                    // TODO: Replace with actual repository methods when implemented
                    // For now, use mock data
                    val mockContacts = getMockEmergencyContacts()
                    val mockResources = getMockResourceLocations()
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            emergencyContacts = mockContacts.sortedByDescending { it.priority.ordinal },
                            resourceLocations = mockResources,
                            error = null
                        )
                    }
                    
                    // Update nearest resources based on user location
                    updateNearestResources(_uiState.value)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No current event found"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load event data: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun observeUserLocation() {
        viewModelScope.launch {
            locationRepository.getCurrentLocation()
                .catch { /* Ignore location errors */ }
                .collect { mapCoordinates ->
                    if (mapCoordinates != null) {
                        // TODO: Convert map coordinates to GPS coordinates
                        // For now, just use x,y as rough approximation
                        _uiState.update { state ->
                            state.copy(
                                userLocation = Pair(mapCoordinates.x.toDouble(), mapCoordinates.y.toDouble())
                            )
                        }
                        updateNearestResources(_uiState.value)
                    }
                }
        }
    }

    private fun updateNearestResources(state: SafetyUiState) {
        val userLocation = state.userLocation ?: return
        
        // Find nearest water point
        val nearestWater = state.resourceLocations
            .filter { it.resourceType == ResourceType.WATER && it.isActive }
            .minByOrNull { resource ->
                calculateDistance(
                    userLocation.first, userLocation.second,
                    resource.latitude, resource.longitude
                )
            }
        
        // Find nearest medical point
        val nearestMedical = state.resourceLocations
            .filter { it.resourceType == ResourceType.MEDICAL }
            .minByOrNull { resource ->
                calculateDistance(
                    userLocation.first, userLocation.second,
                    resource.latitude, resource.longitude
                )
            }
        
        _uiState.update { 
            it.copy(
                nearestWaterPoint = nearestWater,
                nearestMedicalPoint = nearestMedical
            ) 
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        // Simple euclidean distance for now
        // In a real app, use proper geographic distance calculation
        val deltaLat = lat2 - lat1
        val deltaLon = lon2 - lon1
        return kotlin.math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon)
    }

    fun selectTab(tab: SafetyTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectContact(contact: EmergencyContact) {
        _uiState.update { it.copy(selectedContact = contact) }
    }

    fun selectResource(resource: ResourceLocation) {
        _uiState.update { it.copy(selectedResource = resource) }
    }

    fun clearSelection() {
        _uiState.update { 
            it.copy(
                selectedContact = null,
                selectedResource = null
            ) 
        }
    }

    fun getContactsByType(type: EmergencyContactType): List<EmergencyContact> {
        return _uiState.value.emergencyContacts
            .filter { it.type == type }
    }

    fun getResourcesByType(type: ResourceType): List<ResourceLocation> {
        return _uiState.value.resourceLocations
            .filter { it.resourceType == type }
    }

    fun isResourceNearby(resource: ResourceLocation, maxDistanceKm: Double = 1.0): Boolean {
        val userLocation = _uiState.value.userLocation ?: return false
        val distance = calculateDistance(
            userLocation.first, userLocation.second,
            resource.latitude, resource.longitude
        )
        // Convert to approximate km (simplified)
        return distance * 111.0 <= maxDistanceKm
    }

    fun refresh() {
        loadSafetyData()
    }

    // Mock data until repository methods are implemented
    private fun getMockEmergencyContacts(): List<EmergencyContact> {
        // TODO: Replace with actual repository method when implemented
        return emptyList()
    }
    
    private fun getMockResourceLocations(): List<ResourceLocation> {
        // TODO: Replace with actual repository method when implemented
        return emptyList()
    }
    
    companion object {
        // Safety tips content
        val SAFETY_TIPS = listOf(
            SafetyTip(
                title = "Stay Hydrated",
                description = "Drink water regularly, even if you don't feel thirsty. The desert climate can dehydrate you quickly.",
                icon = "water"
            ),
            SafetyTip(
                title = "Protect from Sun",
                description = "Wear sunscreen, a hat, and light-colored clothing. The African sun is intense.",
                icon = "sun"
            ),
            SafetyTip(
                title = "Dust Storms",
                description = "If caught in a dust storm, seek shelter immediately. Cover your face with a bandana or mask.",
                icon = "dust"
            ),
            SafetyTip(
                title = "Night Visibility",
                description = "Always carry lights at night. The playa is dark and vehicles may not see you.",
                icon = "light"
            ),
            SafetyTip(
                title = "Know Your Limits",
                description = "Rest when needed. The combination of heat, dust, and activities can be exhausting.",
                icon = "rest"
            ),
            SafetyTip(
                title = "Emergency Contacts",
                description = "Save emergency contacts in your phone and know where the nearest medical tent is.",
                icon = "emergency"
            )
        )
    }
}

data class SafetyTip(
    val title: String,
    val description: String,
    val icon: String
)