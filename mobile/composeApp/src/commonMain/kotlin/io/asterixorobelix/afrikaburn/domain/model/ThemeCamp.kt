package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing a Theme Camp at AfrikaBurn
 * 
 * Theme camps are organized camps that provide activities, entertainment,
 * or services to the broader community. Includes location-based unlocking
 * and content visibility controls.
 */
data class ThemeCamp(
    val id: String,
    val eventId: String,
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val contactInfo: String?,
    val activities: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val qrCode: String?,
    val photoUrl: String?,
    val isHidden: Boolean = false,
    val unlockTimestamp: Long?,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Common camp activities
         */
        val COMMON_ACTIVITIES = listOf(
            "Workshops", "Live Music", "Art Creation", "Meditation",
            "Yoga", "Dancing", "Fire Spinning", "Performances",
            "Learning", "Community Building", "Healing", "Crafts"
        )
        
        /**
         * Common camp amenities
         */
        val COMMON_AMENITIES = listOf(
            "Shade", "Seating", "Water", "Sound System", "Lighting",
            "Stage", "Kitchen", "Bathroom", "Charging Station", 
            "WiFi", "First Aid", "Information Board"
        )
        
        /**
         * QR code pattern validation
         */
        private val QR_CODE_PATTERN = Regex("^[A-Za-z0-9]{8,32}$")
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               eventId.isNotBlank() &&
               name.isNotBlank() &&
               isValidCoordinates() &&
               isValidQrCode() &&
               lastUpdated > 0
    }
    
    /**
     * Check if coordinates are valid (within reasonable bounds)
     */
    fun isValidCoordinates(): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
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
        
        // Location-based unlocking (if user location is provided)
        if (userLatitude != null && userLongitude != null) {
            return isNearLocation(userLatitude, userLongitude)
        }
        
        return false
    }
    
    /**
     * Check if user is near this camp location
     */
    fun isNearLocation(userLatitude: Double, userLongitude: Double, radiusKm: Double = 0.5): Boolean {
        val distance = calculateDistance(userLatitude, userLongitude, latitude, longitude)
        return distance <= radiusKm
    }
    
    /**
     * Get distance from user location
     */
    fun getDistanceFrom(userLatitude: Double, userLongitude: Double): Double {
        return calculateDistance(userLatitude, userLongitude, latitude, longitude)
    }
    
    /**
     * Check if camp has specific activity
     */
    fun hasActivity(activity: String): Boolean {
        return activities.any { it.contains(activity, ignoreCase = true) }
    }
    
    /**
     * Check if camp has specific amenity
     */
    fun hasAmenity(amenity: String): Boolean {
        return amenities.any { it.contains(amenity, ignoreCase = true) }
    }
    
    /**
     * Get camp type based on activities
     */
    fun getCampType(): CampType {
        return when {
            hasActivity("Music") || hasActivity("Performance") -> CampType.ENTERTAINMENT
            hasActivity("Workshop") || hasActivity("Learning") -> CampType.EDUCATIONAL
            hasActivity("Healing") || hasActivity("Meditation") -> CampType.WELLNESS
            hasActivity("Art") || hasActivity("Craft") -> CampType.ARTISTIC
            else -> CampType.COMMUNITY
        }
    }
    
    /**
     * Check if camp provides essential services
     */
    fun isServiceCamp(): Boolean {
        return hasAmenity("First Aid") || 
               hasAmenity("Information") || 
               hasAmenity("Charging") ||
               hasAmenity("WiFi")
    }
    
    /**
     * Get readable distance string
     */
    fun getDistanceString(userLatitude: Double, userLongitude: Double): String {
        val distance = getDistanceFrom(userLatitude, userLongitude)
        return when {
            distance < 0.1 -> "Very close"
            distance < 0.5 -> "${(distance * 1000).toInt()}m away"
            distance < 1.0 -> "${(distance * 1000).toInt()}m away"
            else -> "${distance.formatToDecimalPlaces(1)}km away"
        }
    }
    
    
    /**
     * Create a copy with updated visibility
     */
    fun updateVisibility(isHidden: Boolean): ThemeCamp {
        return copy(isHidden = isHidden, lastUpdated = getCurrentTimestamp())
    }
    
    /**
     * Create a copy with updated unlock timestamp
     */
    fun updateUnlockTimestamp(timestamp: Long?): ThemeCamp {
        return copy(unlockTimestamp = timestamp, lastUpdated = getCurrentTimestamp())
    }
}

/**
 * Theme camp classification
 */
enum class CampType {
    ENTERTAINMENT,
    EDUCATIONAL,
    WELLNESS,
    ARTISTIC,
    COMMUNITY,
    SERVICE
}