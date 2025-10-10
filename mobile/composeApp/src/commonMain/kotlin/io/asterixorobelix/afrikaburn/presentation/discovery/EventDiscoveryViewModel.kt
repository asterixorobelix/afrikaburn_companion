package io.asterixorobelix.afrikaburn.presentation.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.model.ArtInstallation
import io.asterixorobelix.afrikaburn.domain.model.EventPerformance
import io.asterixorobelix.afrikaburn.domain.model.MutantVehicle
import io.asterixorobelix.afrikaburn.domain.model.ThemeCamp
import io.asterixorobelix.afrikaburn.domain.model.ArtCarType
import io.asterixorobelix.afrikaburn.domain.model.FuelType
import io.asterixorobelix.afrikaburn.domain.model.PerformanceType
import io.asterixorobelix.afrikaburn.domain.model.ParticipationLevel
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import io.asterixorobelix.afrikaburn.domain.repository.ArtInstallationRepository
import io.asterixorobelix.afrikaburn.domain.repository.EventRepository
import io.asterixorobelix.afrikaburn.domain.repository.ThemeCampRepository
import io.asterixorobelix.afrikaburn.domain.usecase.GetEventsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDiscoveryUiState(
    val isLoading: Boolean = false,
    val currentEventId: String? = null,
    val themeCamps: List<ThemeCamp> = emptyList(),
    val artInstallations: List<ArtInstallation> = emptyList(),
    val mutantVehicles: List<MutantVehicle> = emptyList(),
    val performances: List<EventPerformance> = emptyList(),
    val error: String? = null
)

class EventDiscoveryViewModel(
    private val getEventsUseCase: GetEventsUseCase,
    private val themeCampRepository: ThemeCampRepository,
    private val artInstallationRepository: ArtInstallationRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EventDiscoveryUiState())
    val uiState: StateFlow<EventDiscoveryUiState> = _uiState.asStateFlow()

    init {
        loadEventContent()
    }

    private fun loadEventContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Get current event
                val currentEventResult = getEventsUseCase.getCurrentEvent()
                
                currentEventResult.fold(
                    onSuccess = { currentEvent ->
                        _uiState.update { it.copy(currentEventId = currentEvent.id) }
                        
                        // Load all content in parallel
                        launch { loadThemeCamps(currentEvent.id) }
                        launch { loadArtInstallations(currentEvent.id) }
                        launch { loadMutantVehicles(currentEvent.id) }
                        launch { loadPerformances(currentEvent.id) }
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
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load event content: ${e.message}"
                    ) 
                }
            }
        }
    }

    private suspend fun loadThemeCamps(eventId: String) {
        themeCampRepository.observeThemeCampsByEvent(eventId)
            .catch { 
                _uiState.update { it.copy(error = "Failed to load theme camps") }
            }
            .collect { camps ->
                _uiState.update { 
                    it.copy(
                        themeCamps = camps,
                        isLoading = false
                    ) 
                }
            }
    }

    private suspend fun loadArtInstallations(eventId: String) {
        artInstallationRepository.observeArtInstallationsByEvent(eventId)
            .catch { 
                _uiState.update { it.copy(error = "Failed to load art installations") }
            }
            .collect { art ->
                _uiState.update { 
                    it.copy(
                        artInstallations = art,
                        isLoading = false
                    ) 
                }
            }
    }

    private suspend fun loadMutantVehicles(eventId: String) {
        // Using event repository for now as we don't have a MutantVehicleRepository yet
        // This would typically come from a dedicated repository
        val mockVehicles = listOf(
            MutantVehicle(
                id = "1",
                eventId = eventId,
                name = "Desert Dragon",
                description = "Fire-breathing mechanical dragon that roams the playa",
                artCarType = ArtCarType.CUSTOM_BUILD,
                capacity = 20,
                currentLatitude = -32.25,
                currentLongitude = 20.05,
                lastLocationUpdate = getCurrentTimestamp(),
                baseLatitude = -32.25,
                baseLongitude = 20.05,
                registrationNumber = "MV001",
                contactInfo = "Fire Arts Collective - Channel 5",
                operatingHours = "Daily sunset to sunrise",
                fuelType = FuelType.DIESEL,
                qrCode = null,
                unlockTimestamp = null,
                isOfferingRides = true,
                features = listOf("Fire effects", "Sound system", "LED lighting"),
                safetyEquipment = listOf("Fire extinguishers", "First aid kit", "Safety railings"),
                photoUrls = emptyList(),
                isHidden = false,
                lastUpdated = getCurrentTimestamp()
            ),
            MutantVehicle(
                id = "2",
                eventId = eventId,
                name = "The Cosmic Submarine",
                description = "Underwater-themed art car with bubble machines",
                artCarType = ArtCarType.CUSTOM_BUILD,
                capacity = 15,
                currentLatitude = -32.26,
                currentLongitude = 20.06,
                lastLocationUpdate = getCurrentTimestamp(),
                baseLatitude = -32.26,
                baseLongitude = 20.06,
                registrationNumber = "MV002",
                contactInfo = "Aquatic Dreams Camp - Channel 7",
                operatingHours = "Night cruises 9pm-2am",
                fuelType = FuelType.SOLAR,
                qrCode = null,
                unlockTimestamp = null,
                isOfferingRides = true,
                features = listOf("Bubble machines", "Underwater lighting", "Aquarium windows"),
                safetyEquipment = listOf("Life vests", "Emergency lights", "Fire extinguisher"),
                photoUrls = emptyList(),
                isHidden = false,
                lastUpdated = getCurrentTimestamp()
            )
        )
        _uiState.update { 
            it.copy(
                mutantVehicles = mockVehicles,
                isLoading = false
            ) 
        }
    }

    private suspend fun loadPerformances(eventId: String) {
        // Using event repository for now as we don't have a PerformanceRepository yet
        // This would typically come from a dedicated repository
        val mockPerformances = listOf(
            EventPerformance(
                id = "1",
                eventId = eventId,
                title = "Desert Yoga at Dawn",
                description = "Start your day with sunrise yoga",
                performanceType = PerformanceType.YOGA_MOVEMENT,
                startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                endTime = Clock.System.now().plus(kotlin.time.Duration.parse("1h")).toLocalDateTime(TimeZone.currentSystemDefault()),
                latitude = -32.25,
                longitude = 20.05,
                venueName = "Temple of Tranquility",
                venueId = "venue_1",
                performers = listOf("Zen Warriors Camp"),
                capacity = 30,
                isRegistrationRequired = false,
                registrationUrl = null,
                contactInfo = "Find us at the Temple",
                accessibilityInfo = "Ground level, wheelchair accessible",
                ageRestrictions = "All ages welcome",
                participationLevel = ParticipationLevel.BEGINNER,
                tags = listOf("wellness", "yoga", "morning"),
                audioVisualRequirements = emptyList(),
                photoUrls = emptyList(),
                qrCode = null,
                unlockTimestamp = null,
                isHidden = false,
                lastUpdated = getCurrentTimestamp()
            ),
            EventPerformance(
                id = "2",
                eventId = eventId,
                title = "Fire Spinning Workshop",
                description = "Learn the basics of fire spinning safely",
                performanceType = PerformanceType.WORKSHOP,
                startTime = Clock.System.now().plus(kotlin.time.Duration.parse("8h")).toLocalDateTime(TimeZone.currentSystemDefault()),
                endTime = Clock.System.now().plus(kotlin.time.Duration.parse("9h")).toLocalDateTime(TimeZone.currentSystemDefault()),
                latitude = -32.26,
                longitude = 20.06,
                venueName = "Fire Circle",
                venueId = "venue_2",
                performers = listOf("Flame Dancers Collective"),
                capacity = 20,
                isRegistrationRequired = true,
                registrationUrl = "http://example.com/register",
                contactInfo = "Channel 8 for info",
                accessibilityInfo = "Sandy surface, may be difficult for wheelchairs",
                ageRestrictions = "18+ only",
                participationLevel = ParticipationLevel.INTERMEDIATE,
                tags = listOf("fire", "workshop", "performance arts"),
                audioVisualRequirements = listOf("Fire safety equipment"),
                photoUrls = emptyList(),
                qrCode = null,
                unlockTimestamp = null,
                isHidden = false,
                lastUpdated = getCurrentTimestamp()
            )
        )
        _uiState.update { 
            it.copy(
                performances = mockPerformances,
                isLoading = false
            ) 
        }
    }

    fun refresh() {
        loadEventContent()
    }
}