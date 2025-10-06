package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing a map pin/marker for locations on the AfrikaBurn map
 * 
 * Map pins provide visual markers for various points of interest including
 * theme camps, art installations, resources, and user-created locations.
 * Designed for the harsh Tankwa Karoo desert environment.
 */
data class MapPin(
    val id: String,
    val title: String,
    val subtitle: String?,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?, // Elevation for 3D positioning
    val pinType: MapPinType,
    val category: PinCategory,
    val iconName: String?, // Icon identifier for UI
    val iconColor: String?, // Hex color code
    val backgroundColor: String?, // Background color for pin
    val isOfficial: Boolean = false, // Official event content
    val isUserCreated: Boolean = false, // Created by user
    val isVisible: Boolean = true, // Visibility toggle
    val zoomLevelRange: IntRange?, // Zoom levels where pin is visible
    val priority: PinPriority = PinPriority.NORMAL,
    val relatedContentId: String?, // ID of related content (theme camp, art, etc.)
    val relatedContentType: String?, // Type of related content
    val address: String?, // Human-readable location
    val operatingHours: String?, // Operating hours if applicable
    val contact: String?, // Contact information
    val website: String?, // Website URL
    val socialMedia: List<String> = emptyList(), // Social media links
    val tags: List<String> = emptyList(), // Searchable tags
    val amenities: List<String> = emptyList(), // Available amenities
    val accessibility: List<String> = emptyList(), // Accessibility features
    val photos: List<String> = emptyList(), // Photo URLs/paths
    val rating: Double?, // User rating 0.0-5.0
    val reviewCount: Int = 0, // Number of reviews
    val isAccessible: Boolean = false, // Wheelchair/mobility accessible
    val hasShade: Boolean = false, // Important in desert
    val hasWater: Boolean = false, // Critical resource marker
    val hasFood: Boolean = false, // Food availability
    val hasToilets: Boolean = false, // Sanitation facilities
    val hasMedical: Boolean = false, // Medical services
    val hasCharging: Boolean = false, // Device charging available
    val hasWifi: Boolean = false, // Internet connectivity
    val isEmergency: Boolean = false, // Emergency services location
    val emergencyType: EmergencyServiceType?,
    val capacity: Int?, // Maximum occupancy/vehicles
    val currentOccupancy: Int?, // Current occupancy if tracked
    val status: PinStatus = PinStatus.ACTIVE, // Current status
    val lastVerified: Long?, // Last time information was verified
    val reportedIssues: List<String> = emptyList(), // User-reported issues
    val distanceFromCenter: Double?, // Distance from event center (km)
    val walkingTimeMinutes: Int?, // Estimated walking time from center
    val isBookmarked: Boolean = false, // User bookmarked
    val visitCount: Int = 0, // Number of times user visited
    val lastVisited: Long?, // Last visit timestamp
    val createdBy: String?, // Device/user who created pin
    val createdAt: Long,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Maximum distance from event center for validity (km)
         */
        const val MAX_DISTANCE_FROM_CENTER = 10.0
        
        /**
         * Common amenities in the desert environment
         */
        val DESERT_AMENITIES = listOf(
            "Shade", "Water", "Food", "Toilets", "Medical", "Charging",
            "WiFi", "Fuel", "Tools", "Workshop", "Seating", "Kitchen",
            "Sound System", "Lighting", "First Aid", "Information"
        )
        
        /**
         * Emergency service types
         */
        val EMERGENCY_SERVICES = listOf(
            "Medical", "Fire Safety", "Security", "Search & Rescue",
            "Rangers", "Gate", "Communication", "Vehicle Recovery"
        )
        
        /**
         * Default zoom levels for different pin types
         */
        val DEFAULT_ZOOM_RANGES = mapOf(
            PinCategory.THEME_CAMP to 12..20,
            PinCategory.ART_INSTALLATION to 14..20,
            PinCategory.RESOURCE to 13..20,
            PinCategory.EMERGENCY to 10..20,
            PinCategory.LANDMARK to 10..18,
            PinCategory.USER_LOCATION to 15..20
        )
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               title.isNotBlank() &&
               isValidCoordinates() &&
               isValidZoomRange() &&
               isValidCapacity() &&
               createdAt > 0 &&
               lastUpdated > 0
    }
    
    /**
     * Validate coordinates
     */
    fun isValidCoordinates(): Boolean {
        return latitude in -90.0..90.0 && 
               longitude in -180.0..180.0 &&
               isWithinEventArea()
    }
    
    /**
     * Check if pin is within AfrikaBurn event area
     */
    fun isWithinEventArea(): Boolean {
        // Tankwa Karoo approximate bounds
        val minLat = -32.35
        val maxLat = -32.15
        val minLng = 19.95
        val maxLng = 20.15
        
        return latitude in minLat..maxLat && longitude in minLng..maxLng
    }
    
    /**
     * Validate zoom level range
     */
    fun isValidZoomRange(): Boolean {
        return zoomLevelRange?.let { range ->
            range.first >= 0 && range.last <= 20 && range.first <= range.last
        } ?: true
    }
    
    /**
     * Validate capacity values
     */
    fun isValidCapacity(): Boolean {
        return (capacity == null || capacity > 0) &&
               (currentOccupancy == null || currentOccupancy >= 0) &&
               (currentOccupancy == null || capacity == null || currentOccupancy <= capacity)
    }
    
    /**
     * Check if pin should be visible at zoom level
     */
    fun isVisibleAtZoom(zoomLevel: Int): Boolean {
        if (!isVisible) return false
        return zoomLevelRange?.contains(zoomLevel) ?: true
    }
    
    /**
     * Calculate distance to user location
     */
    fun getDistanceTo(userLatitude: Double, userLongitude: Double): Double {
        return calculateDistance(latitude, longitude, userLatitude, userLongitude)
    }
    
    /**
     * Calculate bearing from user location
     */
    fun getBearingFrom(userLatitude: Double, userLongitude: Double): Double {
        return calculateBearing(userLatitude, userLongitude, latitude, longitude)
    }
    
    /**
     * Get human-readable distance string
     */
    fun getDistanceString(userLatitude: Double, userLongitude: Double): String {
        val distance = getDistanceTo(userLatitude, userLongitude)
        return when {
            distance < 0.05 -> "Very close"
            distance < 0.1 -> "${(distance * 1000).toInt()}m"
            distance < 1.0 -> "${(distance * 1000).toInt()}m"
            else -> "${distance.formatToDecimalPlaces(1)}km"
        }
    }
    
    /**
     * Check if pin provides essential desert survival needs
     */
    fun providesEssentials(): Boolean {
        return hasWater || hasShade || hasMedical || hasFood
    }
    
    /**
     * Get survival priority score
     */
    fun getSurvivalPriority(): Int {
        var score = 0
        if (hasWater) score += 100    // Water is critical in desert
        if (hasMedical) score += 90   // Medical services
        if (hasShade) score += 70     // Shade from sun
        if (hasFood) score += 50      // Food availability
        if (hasToilets) score += 30   // Sanitation
        if (hasCharging) score += 20  // Device charging
        if (isEmergency) score += 150 // Emergency services
        return score
    }
    
    /**
     * Check if location is crowded
     */
    fun isCrowded(): Boolean {
        return if (capacity != null && currentOccupancy != null) {
            currentOccupancy.toDouble() / capacity > 0.8 // 80% capacity
        } else false
    }
    
    /**
     * Check if location has space available
     */
    fun hasSpaceAvailable(): Boolean {
        return if (capacity != null && currentOccupancy != null) {
            currentOccupancy < capacity
        } else true // Unknown capacity assumes space available
    }
    
    /**
     * Get occupancy percentage
     */
    fun getOccupancyPercentage(): Int? {
        return if (capacity != null && currentOccupancy != null && capacity > 0) {
            ((currentOccupancy.toDouble() / capacity) * 100).toInt()
        } else null
    }
    
    /**
     * Get estimated walking time from user location
     */
    fun getWalkingTimeFrom(userLatitude: Double, userLongitude: Double): Int {
        val distance = getDistanceTo(userLatitude, userLongitude)
        // Assume 4 km/h walking speed in desert conditions
        return ((distance / 4.0) * 60).toInt() // minutes
    }
    
    /**
     * Check if pin information is stale
     */
    fun isInformationStale(currentTime: Long = getCurrentTimestamp()): Boolean {
        return lastVerified?.let { verified ->
            (currentTime - verified) > (24 * 60 * 60 * 1000) // 24 hours
        } ?: true
    }
    
    /**
     * Get display priority score for map rendering
     */
    fun getDisplayPriority(): Int {
        var score = priority.value
        
        // Boost emergency locations
        if (isEmergency) score += 100
        
        // Boost locations with essential services
        score += getSurvivalPriority() / 10
        
        // Boost official content
        if (isOfficial) score += 50
        
        // Boost bookmarked locations
        if (isBookmarked) score += 30
        
        // Boost frequently visited locations
        score += minOf(visitCount * 5, 25)
        
        // Penalty for inactive status
        if (status != PinStatus.ACTIVE) score -= 50
        
        return maxOf(0, score)
    }
    
    /**
     * Get recommended icon based on pin characteristics
     */
    fun getRecommendedIcon(): String {
        return when {
            isEmergency -> "emergency"
            hasMedical -> "medical"
            hasWater -> "water"
            category == PinCategory.THEME_CAMP -> "camp"
            category == PinCategory.ART_INSTALLATION -> "art"
            category == PinCategory.RESOURCE -> "resource"
            category == PinCategory.LANDMARK -> "landmark"
            isUserCreated -> "user_pin"
            else -> "default"
        }
    }
    
    /**
     * Update visit information
     */
    fun markAsVisited(timestamp: Long = getCurrentTimestamp()): MapPin {
        return copy(
            visitCount = visitCount + 1,
            lastVisited = timestamp,
            lastUpdated = timestamp
        )
    }
    
    /**
     * Toggle bookmark status
     */
    fun toggleBookmark(): MapPin {
        return copy(
            isBookmarked = !isBookmarked,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Update occupancy information
     */
    fun updateOccupancy(newOccupancy: Int): MapPin {
        return copy(
            currentOccupancy = newOccupancy.coerceIn(0, capacity ?: Int.MAX_VALUE),
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Report an issue with this location
     */
    fun reportIssue(issue: String): MapPin {
        return copy(
            reportedIssues = reportedIssues + issue,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Clear reported issues
     */
    fun clearIssues(): MapPin {
        return copy(
            reportedIssues = emptyList(),
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Update verification timestamp
     */
    fun markAsVerified(timestamp: Long = getCurrentTimestamp()): MapPin {
        return copy(
            lastVerified = timestamp,
            status = PinStatus.ACTIVE,
            lastUpdated = timestamp
        )
    }
    
    /**
     * Create summary for quick display
     */
    fun getSummary(): PinSummary {
        val features = mutableListOf<String>()
        if (hasWater) features.add("Water")
        if (hasFood) features.add("Food")
        if (hasShade) features.add("Shade")
        if (hasMedical) features.add("Medical")
        if (hasToilets) features.add("Toilets")
        if (hasCharging) features.add("Charging")
        if (hasWifi) features.add("WiFi")
        
        return PinSummary(
            id = id,
            title = title,
            subtitle = subtitle,
            category = category,
            latitude = latitude,
            longitude = longitude,
            isEmergency = isEmergency,
            features = features,
            rating = rating,
            isBookmarked = isBookmarked
        )
    }
}

/**
 * Types of map pins
 */
enum class MapPinType {
    STANDARD,         // Standard location pin
    CLUSTER,          // Cluster of multiple pins
    AREA,            // Area/polygon marker
    ROUTE,           // Route/path marker
    TEMPORARY,       // Temporary event marker
    EMERGENCY        // Emergency service marker
}

/**
 * Categories of map pins
 */
enum class PinCategory {
    THEME_CAMP,       // Theme camps
    ART_INSTALLATION, // Art installations
    RESOURCE,         // Resources (water, food, etc.)
    EMERGENCY,        // Emergency services
    LANDMARK,         // Notable landmarks
    USER_LOCATION,    // User-created locations
    PERFORMANCE,      // Performance venues
    WORKSHOP,         // Workshop locations
    VENDOR,          // Vendor/market areas
    PARKING,         // Parking areas
    OTHER            // Other/miscellaneous
}

/**
 * Pin priority levels
 */
enum class PinPriority(val value: Int) {
    CRITICAL(100),    // Emergency/safety critical
    HIGH(75),         // Important locations
    NORMAL(50),       // Standard locations
    LOW(25)          // Optional/supplementary
}

/**
 * Pin status
 */
enum class PinStatus {
    ACTIVE,          // Currently active/operational
    INACTIVE,        // Temporarily inactive
    CLOSED,          // Permanently closed
    UNDER_CONSTRUCTION, // Being built/modified
    UNKNOWN          // Status unknown/unverified
}

/**
 * Emergency service types
 */
enum class EmergencyServiceType {
    MEDICAL,
    FIRE_SAFETY,
    SECURITY,
    SEARCH_RESCUE,
    RANGERS,
    GATE,
    COMMUNICATION,
    VEHICLE_RECOVERY
}

/**
 * Simplified pin summary for lists and quick display
 */
data class PinSummary(
    val id: String,
    val title: String,
    val subtitle: String?,
    val category: PinCategory,
    val latitude: Double,
    val longitude: Double,
    val isEmergency: Boolean,
    val features: List<String>,
    val rating: Double?,
    val isBookmarked: Boolean
)