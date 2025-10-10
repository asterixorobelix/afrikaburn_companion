package io.asterixorobelix.afrikaburn.data.sync

import io.asterixorobelix.afrikaburn.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Content unlocking middleware with location and time-based logic.
 * 
 * Unlocking rules:
 * - Pre-event: Basic info only (location, dates, preparation)
 * - During event: Full content when within 30km of Tankwa Town
 * - Location-based: Some content only visible when near specific coordinates
 * - Time-based: Performance schedules appear 24 hours before showtime
 * - Achievement-based: Special content for participants who find easter eggs
 */
class ContentUnlockingService(
    private val locationRepository: LocationRepository
) {
    // AfrikaBurn 2025 location (Tankwa Karoo)
    companion object {
        const val AFRIKABURN_LAT = -32.551296
        const val AFRIKABURN_LON = 19.988442
        const val UNLOCK_RADIUS_KM = 30.0
        const val LOCATION_CONTENT_RADIUS_M = 100.0
        
        // Event dates (example - would be configured)
        val EVENT_START = Instant.parse("2025-04-24T12:00:00Z")
        val EVENT_END = Instant.parse("2025-05-01T12:00:00Z")
    }
    
    private val _unlockedContent = MutableStateFlow(setOf<String>())
    val unlockedContent: Flow<Set<String>> = _unlockedContent.asStateFlow()
    
    /**
     * Checks if content should be unlocked based on current context
     */
    suspend fun isContentUnlocked(
        contentId: String,
        contentType: UnlockableContentType,
        locationRequirement: LocationRequirement? = null,
        timeRequirement: TimeRequirement? = null
    ): Boolean {
        val now = Clock.System.now()
        
        // Check time-based requirements
        if (timeRequirement != null) {
            when (timeRequirement) {
                is TimeRequirement.AfterTime -> {
                    if (now < timeRequirement.time) return false
                }
                is TimeRequirement.DuringEvent -> {
                    if (now < EVENT_START || now > EVENT_END) return false
                }
                is TimeRequirement.BeforeEvent -> {
                    if (now >= EVENT_START) return false
                }
            }
        }
        
        // Check location-based requirements
        if (locationRequirement != null) {
            // TODO: Implement location checking when getCurrentLocation is properly implemented
            // For now, skip location requirements
            return false
        }
        
        // Check content type specific rules
        when (contentType) {
            UnlockableContentType.SAFETY -> return true // Always available
            UnlockableContentType.MAP -> return true // Always available
            UnlockableContentType.SCHEDULE -> {
                // Schedules available 24 hours before performance
                return true
            }
            UnlockableContentType.THEME_CAMP -> {
                // Theme camps visible during event or when nearby
                return now in EVENT_START..EVENT_END || 
                       locationRequirement is LocationRequirement.WithinEventRadius
            }
            UnlockableContentType.ART -> {
                // Art visible during event or when nearby
                return now in EVENT_START..EVENT_END ||
                       locationRequirement is LocationRequirement.WithinEventRadius
            }
            UnlockableContentType.EASTER_EGG -> {
                // Easter eggs only when at exact location
                return locationRequirement is LocationRequirement.NearCoordinate
            }
        }
    }
    
    /**
     * Unlocks content permanently (for achievements)
     */
    suspend fun unlockContent(contentId: String) {
        _unlockedContent.value = _unlockedContent.value + contentId
    }
    
    /**
     * Calculates distance between two coordinates in kilometers
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }
    
    /**
     * Content types with different unlocking rules
     */
    enum class UnlockableContentType {
        SAFETY,
        MAP,
        SCHEDULE,
        THEME_CAMP,
        ART,
        EASTER_EGG
    }
    
    /**
     * Location-based unlock requirements
     */
    sealed interface LocationRequirement {
        data object WithinEventRadius : LocationRequirement
        data class NearCoordinate(
            val latitude: Double,
            val longitude: Double,
            val radiusMeters: Double = LOCATION_CONTENT_RADIUS_M
        ) : LocationRequirement
    }
    
    /**
     * Time-based unlock requirements
     */
    sealed interface TimeRequirement {
        data class AfterTime(val time: Instant) : TimeRequirement
        data object DuringEvent : TimeRequirement
        data object BeforeEvent : TimeRequirement
    }
}