package io.asterixorobelix.afrikaburn.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model representing a performance or scheduled activity at AfrikaBurn
 * 
 * Covers performances, workshops, talks, rituals, and other scheduled activities
 * happening at theme camps, art installations, or dedicated venues.
 * Includes time-based scheduling and conflict detection.
 */
data class EventPerformance(
    val id: String,
    val eventId: String,
    val title: String,
    val description: String?,
    val performanceType: PerformanceType,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val latitude: Double?,
    val longitude: Double?,
    val venueName: String?,
    val venueId: String?, // Links to ThemeCamp or ArtInstallation
    val performers: List<String> = emptyList(),
    val capacity: Int?,
    val isRegistrationRequired: Boolean = false,
    val registrationUrl: String?,
    val contactInfo: String?,
    val tags: List<String> = emptyList(),
    val audioVisualRequirements: List<String> = emptyList(),
    val accessibilityInfo: String?,
    val ageRestrictions: String?,
    val participationLevel: ParticipationLevel,
    val qrCode: String?,
    val photoUrls: List<String> = emptyList(),
    val isHidden: Boolean = false,
    val unlockTimestamp: Long?,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Common performance types at AfrikaBurn
         */
        val COMMON_PERFORMANCE_TYPES = listOf(
            PerformanceType.MUSIC_PERFORMANCE, PerformanceType.WORKSHOP,
            PerformanceType.TALK_LECTURE, PerformanceType.RITUAL_CEREMONY,
            PerformanceType.COMMUNITY_GATHERING, PerformanceType.ART_CREATION
        )
        
        /**
         * Common tags for categorizing performances
         */
        val COMMON_TAGS = listOf(
            "Music", "Dance", "Art", "Healing", "Learning", "Community",
            "Fire", "Ritual", "Interactive", "Workshop", "Kids Friendly",
            "LGBTQ+", "Beginner Friendly", "Advanced", "Spiritual",
            "Educational", "Creative", "Physical", "Mental Health"
        )
        
        /**
         * Common AV requirements
         */
        val COMMON_AV_REQUIREMENTS = listOf(
            "Sound System", "Microphone", "Lighting", "Power Supply",
            "Projector", "Stage", "Chairs", "Tables", "Shade Structure",
            "Dance Floor", "Fire Safety Equipment"
        )
        
        /**
         * QR code pattern validation
         */
        private val QR_CODE_PATTERN = Regex("^[A-Za-z0-9]{8,32}$")
        
        /**
         * Maximum performance duration in hours
         */
        const val MAX_DURATION_HOURS = 12
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               eventId.isNotBlank() &&
               title.isNotBlank() &&
               startTime < endTime &&
               getDurationHours() <= MAX_DURATION_HOURS &&
               isValidCoordinates() &&
               isValidQrCode() &&
               lastUpdated > 0
    }
    
    /**
     * Check if coordinates are valid (within reasonable bounds)
     */
    fun isValidCoordinates(): Boolean {
        return if (latitude != null && longitude != null) {
            latitude in -90.0..90.0 && longitude in -180.0..180.0
        } else {
            // Performance without specific coordinates is valid (venue-based)
            true
        }
    }
    
    /**
     * Check if QR code format is valid
     */
    fun isValidQrCode(): Boolean {
        return qrCode?.let { QR_CODE_PATTERN.matches(it) } ?: true
    }
    
    /**
     * Check if content should be visible based on unlock rules
     */
    fun isVisibleAt(currentTimestamp: Long, userLatitude: Double?, userLongitude: Double?): Boolean {
        if (!isHidden) return true
        
        // Time-based unlocking
        unlockTimestamp?.let { unlockTime ->
            if (currentTimestamp >= unlockTime) return true
        }
        
        // Location-based unlocking (if user location and performance location are provided)
        if (userLatitude != null && userLongitude != null && 
            latitude != null && longitude != null) {
            return isNearLocation(userLatitude, userLongitude)
        }
        
        return false
    }
    
    /**
     * Check if user is near this performance location
     */
    fun isNearLocation(userLatitude: Double, userLongitude: Double, radiusKm: Double = 0.5): Boolean {
        return if (latitude != null && longitude != null) {
            val distance = calculateDistance(userLatitude, userLongitude, latitude!!, longitude!!)
            distance <= radiusKm
        } else {
            false
        }
    }
    
    /**
     * Get distance from user location
     */
    fun getDistanceFrom(userLatitude: Double, userLongitude: Double): Double? {
        return if (latitude != null && longitude != null) {
            calculateDistance(userLatitude, userLongitude, latitude!!, longitude!!)
        } else {
            null
        }
    }
    
    /**
     * Get performance duration in hours
     */
    fun getDurationHours(): Double {
        // Simple calculation using date/time components
        val startHours = startTime.dayOfYear * 24 + startTime.hour + startTime.minute / 60.0
        val endHours = endTime.dayOfYear * 24 + endTime.hour + endTime.minute / 60.0
        return endHours - startHours
    }
    
    /**
     * Get performance duration in minutes
     */
    fun getDurationMinutes(): Int {
        return (getDurationHours() * 60).toInt()
    }
    
    /**
     * Check if performance is currently happening
     */
    fun isHappeningNow(currentTime: LocalDateTime): Boolean {
        return currentTime >= startTime && currentTime <= endTime
    }
    
    /**
     * Check if performance is in the future
     */
    fun isUpcoming(currentTime: LocalDateTime): Boolean {
        return currentTime < startTime
    }
    
    /**
     * Check if performance is in the past
     */
    fun isPast(currentTime: LocalDateTime): Boolean {
        return currentTime > endTime
    }
    
    /**
     * Get minutes until performance starts (negative if past/ongoing)
     */
    fun getMinutesUntilStart(currentTime: LocalDateTime): Int {
        // Simple calculation using date/time components
        val currentMinutes = currentTime.dayOfYear * 24 * 60 + currentTime.hour * 60 + currentTime.minute
        val startMinutes = startTime.dayOfYear * 24 * 60 + startTime.hour * 60 + startTime.minute
        return startMinutes - currentMinutes
    }
    
    /**
     * Check if performance has specific tag
     */
    fun hasTag(tag: String): Boolean {
        return tags.any { it.contains(tag, ignoreCase = true) }
    }
    
    /**
     * Check if performance involves specific performer
     */
    fun hasPerformer(performer: String): Boolean {
        return performers.any { it.contains(performer, ignoreCase = true) }
    }
    
    /**
     * Check if performance requires specific AV equipment
     */
    fun requiresAV(requirement: String): Boolean {
        return audioVisualRequirements.any { it.contains(requirement, ignoreCase = true) }
    }
    
    /**
     * Check if performance is kid-friendly
     */
    fun isKidFriendly(): Boolean {
        return hasTag("Kids") || hasTag("Family") || hasTag("Children") ||
               !hasTag("Adult") && ageRestrictions?.contains("18+") != true
    }
    
    /**
     * Check if performance is beginner-friendly
     */
    fun isBeginnerFriendly(): Boolean {
        return hasTag("Beginner") || hasTag("Intro") || hasTag("Basic") ||
               participationLevel == ParticipationLevel.BEGINNER
    }
    
    /**
     * Check if performance has space available (if capacity is specified)
     */
    fun hasSpaceAvailable(currentAttendees: Int = 0): Boolean? {
        return capacity?.let { cap ->
            currentAttendees < cap
        }
    }
    
    /**
     * Get performance status
     */
    fun getStatus(currentTime: LocalDateTime): PerformanceStatus {
        return when {
            isUpcoming(currentTime) -> PerformanceStatus.UPCOMING
            isHappeningNow(currentTime) -> PerformanceStatus.HAPPENING_NOW
            else -> PerformanceStatus.FINISHED
        }
    }
    
    /**
     * Check if this performance conflicts with another (time overlap)
     */
    fun conflictsWith(other: EventPerformance): Boolean {
        return startTime < other.endTime && endTime > other.startTime
    }
    
    /**
     * Get time until performance in readable format
     */
    fun getTimeUntilString(currentTime: LocalDateTime): String {
        val minutes = getMinutesUntilStart(currentTime)
        return when {
            minutes < 0 -> "Started"
            minutes == 0 -> "Starting now"
            minutes < 60 -> "${minutes}m"
            minutes < 1440 -> "${minutes / 60}h ${minutes % 60}m"
            else -> "${minutes / 1440}d ${(minutes % 1440) / 60}h"
        }
    }
    
    /**
     * Get readable distance string
     */
    fun getDistanceString(userLatitude: Double, userLongitude: Double): String? {
        return getDistanceFrom(userLatitude, userLongitude)?.let { distance ->
            when {
                distance < 0.1 -> "Very close"
                distance < 0.5 -> "${(distance * 1000).toInt()}m away"
                distance < 1.0 -> "${(distance * 1000).toInt()}m away"
                else -> "${distance.formatToDecimalPlaces(1)}km away"
            }
        }
    }
    
    /**
     * Get main photo URL
     */
    fun getMainPhotoUrl(): String? {
        return photoUrls.firstOrNull()
    }
    
    /**
     * Get performance summary for display
     */
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        parts.add(performanceType.displayName)
        parts.add("${getDurationMinutes()}min")
        
        if (isRegistrationRequired) parts.add("Registration Required")
        if (capacity != null) parts.add("Max ${capacity}")
        if (isKidFriendly()) parts.add("Family Friendly")
        if (participationLevel == ParticipationLevel.INTERACTIVE) parts.add("Interactive")
        
        return parts.joinToString(" • ")
    }
    
    /**
     * Get formatted time range string
     */
    fun getTimeRangeString(): String {
        val startStr = "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}"
        val endStr = "${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}"
        return "$startStr - $endStr"
    }
    
    
    /**
     * Create a copy with updated visibility
     */
    fun updateVisibility(isHidden: Boolean): EventPerformance {
        return copy(isHidden = isHidden, lastUpdated = getCurrentTimestamp())
    }
    
    /**
     * Create a copy with updated unlock timestamp
     */
    fun updateUnlockTimestamp(timestamp: Long?): EventPerformance {
        return copy(unlockTimestamp = timestamp, lastUpdated = getCurrentTimestamp())
    }
}

/**
 * Types of performances and activities
 */
enum class PerformanceType(val displayName: String) {
    MUSIC_PERFORMANCE("Music Performance"),
    DANCE_PERFORMANCE("Dance Performance"),
    THEATER_PERFORMANCE("Theater Performance"),
    WORKSHOP("Workshop"),
    TALK_LECTURE("Talk/Lecture"),
    PANEL_DISCUSSION("Panel Discussion"),
    RITUAL_CEREMONY("Ritual/Ceremony"),
    COMMUNITY_GATHERING("Community Gathering"),
    ART_CREATION("Art Creation"),
    HEALING_SESSION("Healing Session"),
    MEDITATION("Meditation"),
    YOGA_MOVEMENT("Yoga/Movement"),
    FIRE_PERFORMANCE("Fire Performance"),
    INTERACTIVE_EXPERIENCE("Interactive Experience"),
    SKILL_SHARE("Skill Share"),
    STORYTELLING("Storytelling"),
    GAME_ACTIVITY("Game/Activity"),
    FOOD_EXPERIENCE("Food Experience"),
    SUNRISE_SUNSET("Sunrise/Sunset Gathering"),
    BURN_CEREMONY("Burn Ceremony")
}

/**
 * Level of audience participation expected
 */
enum class ParticipationLevel(val displayName: String) {
    PASSIVE("Watch/Listen"),
    INTERACTIVE("Interactive"),
    PARTICIPATORY("Full Participation"),
    BEGINNER("Beginner Level"),
    INTERMEDIATE("Intermediate Level"),
    ADVANCED("Advanced Level")
}

/**
 * Current status of the performance
 */
enum class PerformanceStatus(val displayName: String) {
    UPCOMING("Upcoming"),
    HAPPENING_NOW("Happening Now"),
    FINISHED("Finished")
}