package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing a camp location that users can mark and navigate to
 * 
 * Camp locations can be personal (user-marked) or official (from event data).
 * Includes GPS coordinates, navigation helpers, and sharing capabilities for
 * the harsh Tankwa Karoo desert environment.
 */
data class CampLocation(
    val id: String,
    val deviceId: String?, // Device that created this location (null for official)
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?, // Elevation in meters (useful for desert navigation)
    val accuracy: Double?, // GPS accuracy when marked (meters)
    val isUserMarked: Boolean = true, // False for official locations
    val isOfficial: Boolean = false, // True for official camp/resource locations
    val locationType: CampLocationType,
    val address: String?, // Human-readable location description
    val whatThreeWords: String?, // What3Words location code for desert navigation
    val campType: CampType?,
    val capacity: Int?, // Number of people/vehicles
    val amenities: List<String> = emptyList(),
    val contacts: List<String> = emptyList(), // Contact methods (radio, phone)
    val operatingHours: String?, // "24/7", "8am-10pm", etc.
    val isActive: Boolean = true, // Currently operational
    val isShared: Boolean = false, // Shared with community
    val shareCode: String?, // Code for others to find this location
    val sharedByDeviceId: String?, // Original device that shared
    val privacyLevel: LocationPrivacy = LocationPrivacy.PRIVATE,
    val tags: List<String> = emptyList(),
    val notes: String?, // Personal notes about the location
    val photos: List<String> = emptyList(), // Photo URLs/paths
    val lastVisited: Long?, // Last time user was at this location
    val visitCount: Int = 0, // Number of times visited
    val isEmergencyLocation: Boolean = false, // Critical safety location
    val emergencyType: EmergencyType?,
    val createdAt: Long,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Common camp amenities in the desert
         */
        val COMMON_AMENITIES = listOf(
            "Shade", "Water", "Food", "Fuel", "Charging", "WiFi",
            "Toilets", "Showers", "Medical", "Tools", "Workshop",
            "Sound System", "Lighting", "Seating", "Kitchen"
        )
        
        /**
         * Emergency contact methods
         */
        val EMERGENCY_CONTACTS = listOf(
            "Radio Channel 1", "Radio Channel 2", "Emergency Phone",
            "Medic Radio", "Ranger Radio", "Gate Radio"
        )
        
        /**
         * Tankwa Karoo event boundaries for validation
         */
        private const val MIN_LATITUDE = -32.35
        private const val MAX_LATITUDE = -32.15
        private const val MIN_LONGITUDE = 19.95
        private const val MAX_LONGITUDE = 20.15
        
        /**
         * Maximum distance for "nearby" locations (meters)
         */
        private const val NEARBY_DISTANCE_THRESHOLD = 500.0
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               name.isNotBlank() &&
               isValidCoordinates() &&
               isValidCampType() &&
               createdAt > 0 &&
               lastUpdated > 0
    }
    
    /**
     * Check if coordinates are valid and within reasonable bounds
     */
    fun isValidCoordinates(): Boolean {
        return latitude in -90.0..90.0 && 
               longitude in -180.0..180.0 &&
               isWithinEventBounds()
    }
    
    /**
     * Check if camp type is valid for the location type
     */
    fun isValidCampType(): Boolean {
        return when (locationType) {
            CampLocationType.PERSONAL_CAMP -> campType != null
            CampLocationType.THEME_CAMP -> campType in listOf(CampType.THEME, CampType.COMMUNITY)
            CampLocationType.RESOURCE -> campType in listOf(CampType.SERVICE, CampType.EMERGENCY)
            CampLocationType.ART_LOCATION -> campType == null || campType == CampType.ART
            CampLocationType.LANDMARK -> campType == null
            CampLocationType.MEETING_POINT -> campType == null || campType == CampType.COMMUNITY
        }
    }
    
    /**
     * Check if location is within AfrikaBurn event boundaries
     */
    fun isWithinEventBounds(): Boolean {
        return latitude in MIN_LATITUDE..MAX_LATITUDE && 
               longitude in MIN_LONGITUDE..MAX_LONGITUDE
    }
    
    /**
     * Calculate distance to another location
     */
    fun getDistanceTo(otherLatitude: Double, otherLongitude: Double): Double {
        return calculateDistance(latitude, longitude, otherLatitude, otherLongitude)
    }
    
    /**
     * Calculate distance to user's current location
     */
    fun getDistanceFromUser(userLatitude: Double, userLongitude: Double): Double {
        return getDistanceTo(userLatitude, userLongitude)
    }
    
    /**
     * Calculate bearing to another location
     */
    fun getBearingTo(otherLatitude: Double, otherLongitude: Double): Double {
        return calculateBearing(latitude, longitude, otherLatitude, otherLongitude)
    }
    
    /**
     * Get human-readable distance string
     */
    fun getDistanceString(userLatitude: Double, userLongitude: Double): String {
        val distance = getDistanceFromUser(userLatitude, userLongitude)
        return when {
            distance < 0.05 -> "Very close"
            distance < 0.1 -> "50m away"
            distance < 1.0 -> "${(distance * 1000).toInt()}m away"
            else -> "${distance.formatToDecimalPlaces(1)}km away"
        }
    }
    
    /**
     * Get compass bearing string
     */
    fun getBearingString(userLatitude: Double, userLongitude: Double): String {
        val bearing = getBearingTo(userLatitude, userLongitude)
        return bearing.toCompassDirection()
    }
    
    /**
     * Get navigation instructions to this location
     */
    fun getNavigationInstructions(
        userLatitude: Double, 
        userLongitude: Double,
        userBearing: Double? = null
    ): NavigationInstructions {
        val distance = getDistanceFromUser(userLatitude, userLongitude)
        val bearing = getBearingTo(userLatitude, userLongitude)
        val compassDirection = bearing.toCompassDirection()
        
        val instructions = when {
            distance < 0.05 -> "You are very close to ${name}"
            distance < 0.1 -> "Walk ${(distance * 1000).toInt()}m ${compassDirection} to ${name}"
            distance < 1.0 -> "Walk ${(distance * 1000).toInt()}m ${compassDirection} to ${name}"
            else -> "Head ${distance.formatToDecimalPlaces(1)}km ${compassDirection} to ${name}"
        }
        
        val turnInstructions = if (userBearing != null) {
            calculateTurnInstructions(userBearing, bearing)
        } else null
        
        return NavigationInstructions(
            mainInstruction = instructions,
            distance = distance,
            bearing = bearing,
            compassDirection = compassDirection,
            turnInstruction = turnInstructions,
            estimatedWalkingTime = calculateWalkingTime(distance)
        )
    }
    
    /**
     * Calculate estimated walking time in minutes
     */
    fun calculateWalkingTime(distance: Double): Int {
        // Assume average walking speed of 4 km/h in desert conditions
        val walkingSpeedKmh = 4.0
        val timeHours = distance / walkingSpeedKmh
        return (timeHours * 60).toInt()
    }
    
    /**
     * Calculate turn instructions based on current bearing
     */
    fun calculateTurnInstructions(userBearing: Double, targetBearing: Double): String {
        val angleDiff = ((targetBearing - userBearing + 360) % 360)
        
        return when {
            angleDiff < 15 || angleDiff > 345 -> "Continue straight"
            angleDiff < 45 -> "Turn slightly right"
            angleDiff < 135 -> "Turn right"
            angleDiff < 180 -> "Turn sharp right"
            angleDiff < 225 -> "Turn around"
            angleDiff < 315 -> "Turn left"
            else -> "Turn slightly left"
        }
    }
    
    /**
     * Check if this location is nearby another location
     */
    fun isNearby(otherLatitude: Double, otherLongitude: Double): Boolean {
        return getDistanceTo(otherLatitude, otherLongitude) <= NEARBY_DISTANCE_THRESHOLD / 1000.0
    }
    
    /**
     * Check if location provides specific amenity
     */
    fun hasAmenity(amenity: String): Boolean {
        return amenities.any { it.contains(amenity, ignoreCase = true) }
    }
    
    /**
     * Check if location has emergency services
     */
    fun hasEmergencyServices(): Boolean {
        return isEmergencyLocation || 
               hasAmenity("Medical") || 
               hasAmenity("Emergency") ||
               emergencyType != null
    }
    
    /**
     * Check if location provides basic survival needs
     */
    fun hasSurvivalNeeds(): Boolean {
        return hasAmenity("Water") || 
               hasAmenity("Shade") || 
               hasAmenity("Food") ||
               hasAmenity("Medical")
    }
    
    /**
     * Get priority score for emergency navigation
     */
    fun getEmergencyPriority(): Int {
        return when {
            isEmergencyLocation -> 100
            hasAmenity("Medical") -> 90
            hasAmenity("Water") -> 80
            hasAmenity("Shade") -> 70
            hasAmenity("Food") -> 60
            else -> 0
        }
    }
    
    /**
     * Update visit information
     */
    fun markAsVisited(timestamp: Long = getCurrentTimestamp()): CampLocation {
        return copy(
            lastVisited = timestamp,
            visitCount = visitCount + 1,
            lastUpdated = timestamp
        )
    }
    
    /**
     * Create a copy with updated notes
     */
    fun withNotes(notes: String?): CampLocation {
        return copy(
            notes = notes,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Create a copy with updated tags
     */
    fun withTags(tags: List<String>): CampLocation {
        return copy(
            tags = tags,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Create a copy with updated amenities
     */
    fun withAmenities(amenities: List<String>): CampLocation {
        return copy(
            amenities = amenities,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Share this location with community
     */
    fun shareWithCommunity(shareCode: String): CampLocation {
        return copy(
            isShared = true,
            shareCode = shareCode,
            privacyLevel = LocationPrivacy.COMMUNITY,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Stop sharing this location
     */
    fun stopSharing(): CampLocation {
        return copy(
            isShared = false,
            shareCode = null,
            privacyLevel = LocationPrivacy.PRIVATE,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Check if location can be shared
     */
    fun canBeShared(): Boolean {
        return isUserMarked && !isShared && privacyLevel != LocationPrivacy.PRIVATE
    }
    
    /**
     * Get display name for UI
     */
    fun getDisplayName(): String {
        return when {
            name.isNotBlank() -> name
            locationType == CampLocationType.PERSONAL_CAMP -> "My Camp"
            locationType == CampLocationType.RESOURCE -> "Resource Location"
            else -> "Unnamed Location"
        }
    }
    
    /**
     * Get status indicator
     */
    fun getStatusIndicator(): LocationStatus {
        return when {
            !isActive -> LocationStatus.INACTIVE
            isEmergencyLocation -> LocationStatus.EMERGENCY
            hasEmergencyServices() -> LocationStatus.MEDICAL
            hasSurvivalNeeds() -> LocationStatus.ESSENTIAL
            isShared -> LocationStatus.SHARED
            else -> LocationStatus.NORMAL
        }
    }
    
    /**
     * Generate What3Words code if coordinates are available
     */
    fun generateWhat3WordsCode(): String {
        // This would integrate with What3Words API in real implementation
        // For now, return a mock format
        val lat = latitude.toString().take(6).replace(".", "")
        val lng = longitude.toString().take(6).replace(".", "")
        return "desert.camp.${lat.take(3)}"
    }
    
    /**
     * Validate for sharing (ensure no sensitive information)
     */
    fun validateForSharing(): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (!isUserMarked) {
            errors.add("Only user-marked locations can be shared")
        }
        
        if (privacyLevel == LocationPrivacy.PRIVATE) {
            errors.add("Private locations cannot be shared")
        }
        
        if (!isWithinEventBounds()) {
            errors.add("Location is outside event boundaries")
        }
        
        if (deviceId.isNullOrBlank()) {
            errors.add("Location must have a valid device ID")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

/**
 * Types of camp locations
 */
enum class CampLocationType {
    PERSONAL_CAMP,    // User's personal camp/RV/tent location
    THEME_CAMP,       // Official theme camps
    RESOURCE,         // Water, food, fuel, toilets, etc.
    ART_LOCATION,     // Art installations and galleries
    LANDMARK,         // Notable landmarks for navigation
    MEETING_POINT     // Designated meeting points
}

/**
 * Types of camps
 */
enum class CampType {
    PERSONAL,         // Individual/family camp
    THEME,           // Official theme camp
    COMMUNITY,       // Community gathering space
    SERVICE,         // Service providers (water, food, etc.)
    EMERGENCY,       // Emergency services
    ART              // Art installation area
}

/**
 * Privacy levels for location sharing
 */
enum class LocationPrivacy {
    PRIVATE,         // Only visible to creator
    FRIENDS,         // Visible to connected friends
    COMMUNITY,       // Visible to event community
    PUBLIC          // Visible to everyone
}

/**
 * Location status indicators
 */
enum class LocationStatus {
    NORMAL,
    SHARED,
    ESSENTIAL,       // Has survival necessities
    MEDICAL,         // Has medical services
    EMERGENCY,       // Emergency location
    INACTIVE         // Temporarily closed/unavailable
}

/**
 * Emergency types for emergency locations
 */
enum class EmergencyType {
    MEDICAL,
    FIRE_SAFETY,
    SECURITY,
    SEARCH_RESCUE,
    COMMUNICATION,
    GENERAL
}

/**
 * Navigation instructions data
 */
data class NavigationInstructions(
    val mainInstruction: String,
    val distance: Double, // km
    val bearing: Double, // degrees
    val compassDirection: String,
    val turnInstruction: String?,
    val estimatedWalkingTime: Int // minutes
)

/**
 * Validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)