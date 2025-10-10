package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.repository.LocationRepository
import io.asterixorobelix.afrikaburn.models.MapCoordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

/**
 * Use case for location-based community messaging.
 * 
 * Features:
 * - Post messages visible only within radius
 * - Discover messages near current location
 * - Filter by message type (gift, help, announcement)
 * - Time-limited messages (auto-expire)
 * - Anonymous posting (no user accounts)
 */
class LocationBasedMessagingUseCase(
    private val locationRepository: LocationRepository
) {
    private val _messages = MutableStateFlow<List<LocationMessage>>(emptyList())
    val messages: Flow<List<LocationMessage>> = _messages.asStateFlow()
    
    companion object {
        const val DEFAULT_RADIUS_METERS = 500.0
        const val MAX_RADIUS_METERS = 2000.0
        const val MESSAGE_EXPIRY_HOURS = 24L
    }
    
    /**
     * Posts a new location-based message
     */
    suspend fun postMessage(
        content: String,
        type: MessageType,
        radiusMeters: Double = DEFAULT_RADIUS_METERS,
        expiryHours: Long = MESSAGE_EXPIRY_HOURS
    ): Result<LocationMessage> {
        val coordinates = locationRepository.getCurrentLocation().first()
            ?: return Result.failure(Exception("Location not available"))
        
        val message = LocationMessage(
            id = generateMessageId(),
            content = content,
            type = type,
            coordinates = coordinates,
            radiusMeters = radiusMeters.coerceIn(100.0, MAX_RADIUS_METERS),
            timestamp = Clock.System.now(),
            expiryTime = Clock.System.now().plus(expiryHours.hours),
            deviceId = getAnonymousDeviceId()
        )
        
        // Add to local storage (in production, would sync to backend)
        _messages.value = (_messages.value + message).sortedByDescending { it.timestamp }
        
        return Result.success(message)
    }
    
    /**
     * Gets messages visible from current location
     */
    fun getMessagesNearby(
        filterType: MessageType? = null,
        maxRadiusMeters: Double = MAX_RADIUS_METERS
    ): Flow<List<LocationMessage>> {
        return locationRepository.getCurrentLocation().map { currentCoordinates ->
            if (currentCoordinates == null) {
                emptyList()
            } else {
                val now = Clock.System.now()
                
                _messages.value
                    .filter { message ->
                        // Filter expired messages
                        message.expiryTime > now &&
                        // Filter by type if specified
                        (filterType == null || message.type == filterType) &&
                        // Filter by distance
                        isWithinRadius(currentCoordinates, message.coordinates, message.radiusMeters) &&
                        // Ensure within max search radius
                        calculateDistance(currentCoordinates, message.coordinates) <= maxRadiusMeters
                    }
                    .sortedBy { message ->
                        // Sort by distance, closest first
                        calculateDistance(currentCoordinates, message.coordinates)
                    }
            }
        }
    }
    
    /**
     * Deletes a message (only own messages)
     */
    suspend fun deleteMessage(messageId: String): Result<Unit> {
        val deviceId = getAnonymousDeviceId()
        val message = _messages.value.find { it.id == messageId }
        
        return if (message?.deviceId == deviceId) {
            _messages.value = _messages.value.filter { it.id != messageId }
            Result.success(Unit)
        } else {
            Result.failure(Exception("Can only delete own messages"))
        }
    }
    
    /**
     * Cleans up expired messages
     */
    suspend fun cleanupExpiredMessages() {
        val now = Clock.System.now()
        _messages.value = _messages.value.filter { it.expiryTime > now }
    }
    
    /**
     * Checks if a location is within radius of a message
     */
    private fun isWithinRadius(
        userCoordinates: MapCoordinates,
        messageCoordinates: MapCoordinates,
        radiusMeters: Double
    ): Boolean {
        val distance = calculateDistance(userCoordinates, messageCoordinates)
        return distance <= radiusMeters
    }
    
    /**
     * Calculates distance between two locations in meters
     */
    private fun calculateDistance(
        coord1: MapCoordinates,
        coord2: MapCoordinates
    ): Double {
        // Simple euclidean distance approximation for map coordinates
        // In a real implementation, this would convert x,y to lat/lon first
        val deltaX = (coord2.x - coord1.x).toDouble()
        val deltaY = (coord2.y - coord1.y).toDouble()
        
        // Approximate conversion factor (1 unit ≈ 100 meters)
        val conversionFactor = 100.0
        
        return kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY) * conversionFactor
    }
    
    /**
     * Generates a unique message ID
     */
    private fun generateMessageId(): String {
        return "${Clock.System.now().epochSeconds}-${(0..9999).random()}"
    }
    
    /**
     * Gets anonymous device identifier
     */
    private fun getAnonymousDeviceId(): String {
        // In production, use platform-specific device ID
        return "device-${(1000..9999).random()}"
    }
    
    /**
     * Location-based message
     */
    data class LocationMessage(
        val id: String,
        val content: String,
        val type: MessageType,
        val coordinates: MapCoordinates,
        val radiusMeters: Double,
        val timestamp: Instant,
        val expiryTime: Instant,
        val deviceId: String
    )
    
    /**
     * Types of community messages
     */
    enum class MessageType {
        GIFT,          // Offering something
        NEED_HELP,     // Requesting assistance
        ANNOUNCEMENT,  // General announcement
        LOST_FOUND,    // Lost or found items
        MEETUP        // Organizing gatherings
    }
}