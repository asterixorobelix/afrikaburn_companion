package io.asterixorobelix.afrikaburn.service

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.*

/**
 * Backend service for content unlocking with GPS validation.
 * 
 * Validates location-based content access rules:
 * - Pre-event: Limited content available globally
 * - During event: Full content when within 30km radius
 * - Location-specific: Content visible only near specific GPS coordinates
 * - Time-gated: Content appears at scheduled times
 */
class ContentUnlockingService {
    companion object {
        // AfrikaBurn location in Tankwa Karoo
        const val EVENT_LAT = -32.551296
        const val EVENT_LON = 19.988442
        const val EVENT_RADIUS_KM = 30.0
        
        // Event dates (would be configured per year)
        val EVENT_START = Instant.parse("2025-04-24T12:00:00Z")
        val EVENT_END = Instant.parse("2025-05-01T12:00:00Z")
    }
    
    /**
     * Validates if content should be accessible based on request context
     */
    fun validateContentAccess(
        contentType: ContentType,
        userLat: Double?,
        userLon: Double?,
        requestTime: Instant = Clock.System.now(),
        specificLocation: LocationRequirement? = null
    ): AccessValidation {
        // Safety content is always accessible
        if (contentType == ContentType.SAFETY || contentType == ContentType.EMERGENCY) {
            return AccessValidation.Granted("Safety content always available")
        }
        
        // Check if event is active
        val isEventActive = requestTime in EVENT_START..EVENT_END
        
        // Validate location if provided
        val isWithinEventRadius = if (userLat != null && userLon != null) {
            calculateDistance(userLat, userLon, EVENT_LAT, EVENT_LON) <= EVENT_RADIUS_KM
        } else false
        
        // Apply content-specific rules
        return when (contentType) {
            ContentType.SAFETY, ContentType.EMERGENCY -> {
                AccessValidation.Granted("Safety content always available")
            }
            
            ContentType.MAP -> {
                when {
                    isEventActive && isWithinEventRadius -> 
                        AccessValidation.Granted("Full map access during event")
                    isEventActive -> 
                        AccessValidation.Limited("Basic map only - come to the event for full access")
                    else -> 
                        AccessValidation.Limited("Pre-event map preview available")
                }
            }
            
            ContentType.THEME_CAMP -> {
                when {
                    isEventActive && isWithinEventRadius ->
                        AccessValidation.Granted("All theme camps visible at event")
                    isEventActive ->
                        AccessValidation.Limited("Limited theme camp info - visit AfrikaBurn for full details")
                    else ->
                        AccessValidation.Denied("Theme camps available during event only")
                }
            }
            
            ContentType.ART_INSTALLATION -> {
                when {
                    isEventActive && isWithinEventRadius ->
                        AccessValidation.Granted("All art installations visible")
                    specificLocation != null && validateSpecificLocation(
                        userLat, userLon, specificLocation
                    ) ->
                        AccessValidation.Granted("Location-based art unlocked!")
                    else ->
                        AccessValidation.Denied("Visit AfrikaBurn to discover the art")
                }
            }
            
            ContentType.PERFORMANCE -> {
                when {
                    isEventActive && isWithinEventRadius ->
                        AccessValidation.Granted("Full performance schedule available")
                    isEventActive ->
                        AccessValidation.Limited("Basic schedule only - come experience it live")
                    requestTime.epochSeconds >= (EVENT_START.epochSeconds - 86400) ->
                        AccessValidation.Limited("Preview available 24 hours before event")
                    else ->
                        AccessValidation.Denied("Performance schedule coming soon")
                }
            }
            
            ContentType.EASTER_EGG -> {
                if (specificLocation != null && userLat != null && userLon != null) {
                    if (validateSpecificLocation(userLat, userLon, specificLocation)) {
                        AccessValidation.Granted("🎉 Easter egg discovered!")
                    } else {
                        AccessValidation.Denied("Keep exploring...")
                    }
                } else {
                    AccessValidation.Denied("Location required for easter eggs")
                }
            }
        }
    }
    
    /**
     * Validates if user is at a specific location
     */
    private fun validateSpecificLocation(
        userLat: Double?,
        userLon: Double?,
        requirement: LocationRequirement
    ): Boolean {
        if (userLat == null || userLon == null) return false
        
        val distance = calculateDistance(
            userLat, userLon,
            requirement.latitude, requirement.longitude
        )
        
        // Convert km to meters for precise location checks
        return (distance * 1000) <= requirement.radiusMeters
    }
    
    /**
     * Calculates distance between two GPS coordinates in kilometers
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadiusKm * c
    }
    
    /**
     * Types of content with different access rules
     */
    enum class ContentType {
        SAFETY,
        EMERGENCY,
        MAP,
        THEME_CAMP,
        ART_INSTALLATION,
        PERFORMANCE,
        EASTER_EGG
    }
    
    /**
     * Location requirement for specific content
     */
    data class LocationRequirement(
        val latitude: Double,
        val longitude: Double,
        val radiusMeters: Double = 100.0 // Default 100m radius
    )
    
    /**
     * Content access validation result
     */
    sealed class AccessValidation {
        data class Granted(val message: String) : AccessValidation()
        data class Limited(val message: String) : AccessValidation()
        data class Denied(val reason: String) : AccessValidation()
    }
}