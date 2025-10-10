package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing an art installation at AfrikaBurn
 * 
 * Art installations are creative works placed throughout the event,
 * ranging from sculptures to interactive experiences. Includes location-based
 * discovery and content visibility controls.
 */
data class ArtInstallation(
    val id: String,
    val eventId: String,
    val name: String,
    val artistName: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val artType: ArtType,
    val materials: List<String> = emptyList(),
    val dimensions: String?,
    val isInteractive: Boolean = false,
    val isNightIlluminated: Boolean = false,
    val accessibilityNotes: String?,
    val qrCode: String?,
    val photoUrls: List<String> = emptyList(),
    val artistBio: String? = null,
    val interactiveFeatures: List<String> = emptyList(),
    val isHidden: Boolean = false,
    val unlockTimestamp: Long?,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Common art types at AfrikaBurn
         */
        val COMMON_ART_TYPES = listOf(
            ArtType.SCULPTURE, ArtType.INSTALLATION, ArtType.INTERACTIVE,
            ArtType.PERFORMANCE_SPACE, ArtType.MURAL, ArtType.SOUND_ART
        )
        
        /**
         * Common materials used in desert art
         */
        val COMMON_MATERIALS = listOf(
            "Metal", "Wood", "Fabric", "Recycled Materials", "Light",
            "Sound", "Digital", "Stone", "Glass", "Rope", "Wire",
            "Solar Panels", "Mirrors", "Fire", "Water"
        )
        
        /**
         * QR code pattern validation
         */
        private val QR_CODE_PATTERN = Regex("^[A-Za-z0-9]{8,32}$")
        
        /**
         * Maximum number of photos per installation
         */
        const val MAX_PHOTOS = 10
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               eventId.isNotBlank() &&
               name.isNotBlank() &&
               artistName.isNotBlank() &&
               isValidCoordinates() &&
               isValidQrCode() &&
               photoUrls.size <= MAX_PHOTOS &&
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
     * Check if user is near this art installation
     */
    fun isNearLocation(userLatitude: Double, userLongitude: Double, radiusKm: Double = 0.1): Boolean {
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
     * Check if art uses specific material
     */
    fun usesMaterial(material: String): Boolean {
        return materials.any { it.contains(material, ignoreCase = true) }
    }
    
    /**
     * Check if this is large-scale art (size estimation based on dimensions)
     */
    fun isLargeScale(): Boolean {
        return dimensions?.let { dims ->
            // Simple heuristic: if dimensions contain numbers > 5 (meters)
            val numbers = Regex("\\d+").findAll(dims).map { it.value.toIntOrNull() ?: 0 }
            numbers.any { it > 5 }
        } ?: false
    }
    
    /**
     * Check if art is best viewed at night
     */
    fun isBestViewedAtNight(): Boolean {
        return isNightIlluminated || 
               usesMaterial("Light") || 
               usesMaterial("LED") ||
               artType == ArtType.LIGHT_INSTALLATION
    }
    
    /**
     * Get accessibility information
     */
    fun getAccessibilityInfo(): AccessibilityInfo {
        return AccessibilityInfo(
            isWheelchairAccessible = accessibilityNotes?.contains("wheelchair", ignoreCase = true) ?: false,
            requiresPhysicalInteraction = isInteractive,
            notes = accessibilityNotes
        )
    }
    
    /**
     * Get optimal viewing times based on art characteristics
     */
    fun getOptimalViewingTimes(): List<ViewingTime> {
        val times = mutableListOf<ViewingTime>()
        
        // All art can be viewed during day
        times.add(ViewingTime.DAY)
        
        // Night viewing for illuminated art
        if (isBestViewedAtNight()) {
            times.add(ViewingTime.NIGHT)
        }
        
        // Interactive art might be better during quieter times
        if (isInteractive) {
            times.add(ViewingTime.EARLY_MORNING)
        }
        
        return times
    }
    
    /**
     * Get readable distance string
     */
    fun getDistanceString(userLatitude: Double, userLongitude: Double): String {
        val distance = getDistanceFrom(userLatitude, userLongitude)
        return when {
            distance < 0.05 -> "Very close"
            distance < 0.1 -> "${(distance * 1000).toInt()}m away"
            distance < 1.0 -> "${(distance * 1000).toInt()}m away"
            else -> "${distance.formatToDecimalPlaces(1)}km away"
        }
    }
    
    /**
     * Get main photo URL (first in list)
     */
    fun getMainPhotoUrl(): String? {
        return photoUrls.firstOrNull()
    }
    
    /**
     * Get all photos except main
     */
    fun getAdditionalPhotoUrls(): List<String> {
        return photoUrls.drop(1)
    }
    
    /**
     * Check if art has multiple photos
     */
    fun hasMultiplePhotos(): Boolean {
        return photoUrls.size > 1
    }
    
    /**
     * Get art summary for display
     */
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        parts.add(artType.displayName)
        
        if (isInteractive) parts.add("Interactive")
        if (isNightIlluminated) parts.add("Night Illuminated")
        if (isLargeScale()) parts.add("Large Scale")
        
        return parts.joinToString(" • ")
    }
    
    
    /**
     * Create a copy with updated visibility
     */
    fun updateVisibility(isHidden: Boolean): ArtInstallation {
        return copy(isHidden = isHidden, lastUpdated = getCurrentTimestamp())
    }
    
    /**
     * Create a copy with updated unlock timestamp
     */
    fun updateUnlockTimestamp(timestamp: Long?): ArtInstallation {
        return copy(unlockTimestamp = timestamp, lastUpdated = getCurrentTimestamp())
    }
}

/**
 * Art installation types
 */
enum class ArtType(val displayName: String) {
    SCULPTURE("Sculpture"),
    INSTALLATION("Installation"),
    INTERACTIVE("Interactive Art"),
    PERFORMANCE_SPACE("Performance Space"),
    MURAL("Mural"),
    SOUND_ART("Sound Art"),
    LIGHT_INSTALLATION("Light Installation"),
    KINETIC("Kinetic Art"),
    DIGITAL("Digital Art"),
    ENVIRONMENTAL("Environmental Art"),
    PARTICIPATORY("Participatory Art"),
    TEMPORARY("Temporary Structure")
}

/**
 * Optimal viewing times for art
 */
enum class ViewingTime(val displayName: String) {
    DAY("Daytime"),
    NIGHT("Nighttime"),
    SUNRISE("Sunrise"),
    SUNSET("Sunset"),
    EARLY_MORNING("Early Morning"),
    LATE_EVENING("Late Evening")
}

/**
 * Accessibility information for art installations
 */
data class AccessibilityInfo(
    val isWheelchairAccessible: Boolean,
    val requiresPhysicalInteraction: Boolean,
    val notes: String?
) {
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        
        if (isWheelchairAccessible) {
            parts.add("Wheelchair accessible")
        }
        
        if (requiresPhysicalInteraction) {
            parts.add("Interactive experience")
        }
        
        notes?.let { parts.add(it) }
        
        return if (parts.isEmpty()) {
            "No specific accessibility information"
        } else {
            parts.joinToString(". ")
        }
    }
}