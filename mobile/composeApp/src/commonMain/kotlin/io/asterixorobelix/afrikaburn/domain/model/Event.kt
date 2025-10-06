package io.asterixorobelix.afrikaburn.domain.model

import kotlinx.datetime.LocalDate

/**
 * Domain model representing an AfrikaBurn event
 * 
 * Represents a specific year's AfrikaBurn event with geographic boundaries
 * and metadata for content unlocking and location-based features.
 */
data class Event(
    val id: String,
    val year: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusKm: Double = 5.0,
    val theme: String,
    val isCurrentYear: Boolean = false,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Tankwa Karoo geographic bounds (approximate)
         */
        const val TANKWA_MIN_LAT = -32.5
        const val TANKWA_MAX_LAT = -32.0
        const val TANKWA_MIN_LNG = 19.8
        const val TANKWA_MAX_LNG = 20.3
        
        /**
         * Default unlock radius in kilometers
         */
        const val DEFAULT_UNLOCK_RADIUS_KM = 5.0
        
        /**
         * Valid year range for events
         */
        val VALID_YEAR_RANGE = 2020..2030
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               year in VALID_YEAR_RANGE &&
               startDate < endDate &&
               isValidCoordinates() &&
               radiusKm > 0 &&
               theme.isNotBlank() &&
               lastUpdated > 0
    }
    
    /**
     * Check if coordinates are within Tankwa Karoo bounds
     */
    fun isValidCoordinates(): Boolean {
        return centerLatitude in TANKWA_MIN_LAT..TANKWA_MAX_LAT &&
               centerLongitude in TANKWA_MIN_LNG..TANKWA_MAX_LNG
    }
    
    /**
     * Check if given coordinates are within event unlock radius
     */
    fun isWithinUnlockRadius(latitude: Double, longitude: Double): Boolean {
        val distance = calculateDistance(latitude, longitude, centerLatitude, centerLongitude)
        return distance <= radiusKm
    }
    
    /**
     * Check if the event is currently active
     */
    fun isActive(currentDate: LocalDate): Boolean {
        return currentDate >= startDate && currentDate <= endDate
    }
    
    /**
     * Get event duration in days
     */
    fun getDurationDays(): Int {
        return (endDate.toEpochDays() - startDate.toEpochDays()).toInt()
    }
    
    /**
     * Check if this is a future event
     */
    fun isFuture(currentDate: LocalDate): Boolean {
        return currentDate < startDate
    }
    
    /**
     * Check if this is a past event
     */
    fun isPast(currentDate: LocalDate): Boolean {
        return currentDate > endDate
    }
    
    /**
     * Get days until event starts (negative if past)
     */
    fun getDaysUntilStart(currentDate: LocalDate): Int {
        return (startDate.toEpochDays() - currentDate.toEpochDays()).toInt()
    }
    
    /**
     * Get readable event status
     */
    fun getStatus(currentDate: LocalDate): EventStatus {
        return when {
            isFuture(currentDate) -> EventStatus.UPCOMING
            isActive(currentDate) -> EventStatus.ACTIVE
            else -> EventStatus.PAST
        }
    }
    
    
    /**
     * Create a copy with updated current year status
     */
    fun markAsCurrent(): Event {
        return copy(isCurrentYear = true, lastUpdated = getCurrentTimestamp())
    }
    
    /**
     * Create a copy removing current year status
     */
    fun markAsNotCurrent(): Event {
        return copy(isCurrentYear = false, lastUpdated = getCurrentTimestamp())
    }
}

/**
 * Event status enumeration
 */
enum class EventStatus {
    UPCOMING,
    ACTIVE, 
    PAST
}