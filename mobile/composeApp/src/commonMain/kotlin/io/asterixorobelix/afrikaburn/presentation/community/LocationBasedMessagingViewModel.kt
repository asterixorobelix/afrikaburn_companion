package io.asterixorobelix.afrikaburn.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class LocationMessage(
    val id: String,
    val message: String,
    val authorName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val radiusMeters: Int = 100,
    val expiresAt: Instant? = null,
    val tags: List<String> = emptyList()
)

data class LocationBasedMessagingUiState(
    val isLoading: Boolean = false,
    val nearbyMessages: List<LocationMessage> = emptyList(),
    val myMessages: List<LocationMessage> = emptyList(),
    val selectedMessage: LocationMessage? = null,
    val userLocation: Pair<Double, Double>? = null,
    val scanRadius: Int = 500, // meters
    val error: String? = null,
    val isPostingMessage: Boolean = false,
    val messageFilter: MessageFilter = MessageFilter.ALL
)

enum class MessageFilter {
    ALL, GIFTS, EVENTS, HELP, SOCIAL
}

class LocationBasedMessagingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocationBasedMessagingUiState())
    val uiState: StateFlow<LocationBasedMessagingUiState> = _uiState.asStateFlow()

    init {
        // Mock data for demonstration
        loadMockMessages()
    }

    private fun loadMockMessages() {
        val mockMessages = listOf(
            LocationMessage(
                id = "1",
                message = "Free ice cold water at our camp! Come hydrate!",
                authorName = "Oasis Camp",
                latitude = -32.395,
                longitude = 19.382,
                timestamp = Clock.System.now(),
                tags = listOf("gifts", "water")
            ),
            LocationMessage(
                id = "2",
                message = "Sunset drum circle starting in 30 minutes",
                authorName = "Rhythm Collective",
                latitude = -32.396,
                longitude = 19.383,
                timestamp = Clock.System.now(),
                tags = listOf("events", "music")
            ),
            LocationMessage(
                id = "3",
                message = "Lost: Blue backpack with camp supplies. Please help!",
                authorName = "Desert Wanderer",
                latitude = -32.394,
                longitude = 19.381,
                timestamp = Clock.System.now(),
                tags = listOf("help", "lost")
            )
        )
        
        _uiState.update { 
            it.copy(
                nearbyMessages = mockMessages,
                isLoading = false
            ) 
        }
    }

    fun postMessage(
        message: String,
        tags: List<String>,
        expirationHours: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPostingMessage = true, error = null) }
            
            try {
                val userLocation = _uiState.value.userLocation
                if (userLocation == null) {
                    _uiState.update { 
                        it.copy(
                            isPostingMessage = false,
                            error = "Location required to post messages"
                        ) 
                    }
                    return@launch
                }
                
                // Mock message creation
                val newMessage = LocationMessage(
                    id = System.currentTimeMillis().toString(),
                    message = message,
                    authorName = "Me",
                    latitude = userLocation.first,
                    longitude = userLocation.second,
                    timestamp = Clock.System.now(),
                    expiresAt = expirationHours?.let {
                        Clock.System.now().plus(it.toDuration(DurationUnit.HOURS))
                    },
                    tags = tags
                )
                
                _uiState.update { state ->
                    state.copy(
                        isPostingMessage = false,
                        myMessages = state.myMessages + newMessage,
                        nearbyMessages = state.nearbyMessages + newMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isPostingMessage = false,
                        error = "Failed to post message: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        _uiState.update { state ->
            state.copy(
                myMessages = state.myMessages.filter { it.id != messageId },
                nearbyMessages = state.nearbyMessages.filter { it.id != messageId }
            )
        }
    }

    fun updateScanRadius(radiusMeters: Int) {
        _uiState.update { it.copy(scanRadius = radiusMeters) }
        refreshNearbyMessages()
    }

    fun setMessageFilter(filter: MessageFilter) {
        _uiState.update { it.copy(messageFilter = filter) }
    }

    fun selectMessage(message: LocationMessage) {
        _uiState.update { it.copy(selectedMessage = message) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedMessage = null) }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(userLocation = Pair(latitude, longitude)) }
        refreshNearbyMessages()
    }

    private fun refreshNearbyMessages() {
        val userLocation = _uiState.value.userLocation ?: return
        val scanRadius = _uiState.value.scanRadius
        
        // Filter messages by distance and active status
        val filtered = _uiState.value.nearbyMessages.filter { message ->
            val distance = calculateDistance(
                userLocation.first, userLocation.second,
                message.latitude, message.longitude
            )
            distance <= scanRadius && (message.expiresAt == null || message.expiresAt > Clock.System.now())
        }
        
        _uiState.update { it.copy(nearbyMessages = filtered) }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        // Simplified distance calculation in meters
        val deltaLat = (lat2 - lat1) * 111000
        val deltaLon = (lon2 - lon1) * 111000 * kotlin.math.cos(lat1 * kotlin.math.PI / 180)
        return kotlin.math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon)
    }

    fun getFilteredMessages(): List<LocationMessage> {
        val messages = _uiState.value.nearbyMessages
        val filter = _uiState.value.messageFilter
        
        return when (filter) {
            MessageFilter.ALL -> messages
            MessageFilter.GIFTS -> messages.filter { "gifts" in it.tags }
            MessageFilter.EVENTS -> messages.filter { "events" in it.tags }
            MessageFilter.HELP -> messages.filter { "help" in it.tags }
            MessageFilter.SOCIAL -> messages.filter { "social" in it.tags }
        }
    }
}