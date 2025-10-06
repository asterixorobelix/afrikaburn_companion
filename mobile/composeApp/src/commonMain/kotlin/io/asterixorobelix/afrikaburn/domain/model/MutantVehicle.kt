package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing a Mutant Vehicle at AfrikaBurn
 * 
 * Mutant Vehicles are art cars that provide mobile art experiences and transportation.
 * They must be registered and follow safety protocols while roaming the event.
 * Includes real-time location tracking and ride-sharing capabilities.
 */
data class MutantVehicle(
    val id: String,
    val eventId: String,
    val name: String,
    val description: String?,
    val artCarType: ArtCarType,
    val capacity: Int,
    val isOfferingRides: Boolean = false,
    val currentLatitude: Double?,
    val currentLongitude: Double?,
    val lastLocationUpdate: Long?,
    val baseLatitude: Double?,
    val baseLongitude: Double?,
    val registrationNumber: String,
    val contactInfo: String?,
    val features: List<String> = emptyList(),
    val safetyEquipment: List<String> = emptyList(),
    val operatingHours: String?,
    val fuelType: FuelType,
    val qrCode: String?,
    val photoUrls: List<String> = emptyList(),
    val isHidden: Boolean = false,
    val unlockTimestamp: Long?,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Common art car types
         */
        val COMMON_ART_CAR_TYPES = listOf(
            ArtCarType.DECORATED_VEHICLE, ArtCarType.CUSTOM_BUILD, 
            ArtCarType.BIKE_COLLECTIVE, ArtCarType.MOBILE_INSTALLATION
        )
        
        /**
         * Required safety equipment
         */
        val REQUIRED_SAFETY_EQUIPMENT = listOf(
            "Fire Extinguisher", "First Aid Kit", "Reflective Tape",
            "Safety Flags", "Working Lights", "Emergency Radio"
        )
        
        /**
         * Common vehicle features
         */
        val COMMON_FEATURES = listOf(
            "Sound System", "LED Lighting", "Shade Structure", "Bar/Kitchen",
            "Dance Floor", "Art Display", "Seating Area", "Storage",
            "Solar Panels", "Generator", "Water Tank", "Coolers"
        )
        
        /**
         * QR code pattern validation
         */
        private val QR_CODE_PATTERN = Regex("^[A-Za-z0-9]{8,32}$")
        
        /**
         * Maximum location age (15 minutes) before considering stale
         */
        const val MAX_LOCATION_AGE_MS = 15 * 60 * 1000L
        
        /**
         * Minimum capacity for ride sharing
         */
        const val MIN_RIDE_CAPACITY = 2
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               eventId.isNotBlank() &&
               name.isNotBlank() &&
               registrationNumber.isNotBlank() &&
               capacity > 0 &&
               capacity <= 50 && // Reasonable upper limit
               isValidCoordinates() &&
               isValidQrCode() &&
               lastUpdated > 0
    }
    
    /**
     * Check if coordinates are valid (within reasonable bounds)
     */
    fun isValidCoordinates(): Boolean {
        val currentValid = currentLatitude?.let { lat ->
            currentLongitude?.let { lon ->
                lat in -90.0..90.0 && lon in -180.0..180.0
            } ?: false
        } ?: true
        
        val baseValid = baseLatitude?.let { lat ->
            baseLongitude?.let { lon ->
                lat in -90.0..90.0 && lon in -180.0..180.0
            } ?: false
        } ?: true
        
        return currentValid && baseValid
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
        
        // Location-based unlocking (if user location is provided and vehicle has current location)
        if (userLatitude != null && userLongitude != null && 
            currentLatitude != null && currentLongitude != null) {
            return isNearCurrentLocation(userLatitude, userLongitude)
        }
        
        return false
    }
    
    /**
     * Check if user is near the vehicle's current location
     */
    fun isNearCurrentLocation(userLatitude: Double, userLongitude: Double, radiusKm: Double = 0.2): Boolean {
        return currentLatitude?.let { lat ->
            currentLongitude?.let { lon ->
                val distance = calculateDistance(userLatitude, userLongitude, lat, lon)
                distance <= radiusKm
            }
        } ?: false
    }
    
    /**
     * Check if user is near the vehicle's base location
     */
    fun isNearBaseLocation(userLatitude: Double, userLongitude: Double, radiusKm: Double = 0.5): Boolean {
        return baseLatitude?.let { lat ->
            baseLongitude?.let { lon ->
                val distance = calculateDistance(userLatitude, userLongitude, lat, lon)
                distance <= radiusKm
            }
        } ?: false
    }
    
    /**
     * Get distance from user to current vehicle location
     */
    fun getCurrentDistanceFrom(userLatitude: Double, userLongitude: Double): Double? {
        return currentLatitude?.let { lat ->
            currentLongitude?.let { lon ->
                calculateDistance(userLatitude, userLongitude, lat, lon)
            }
        }
    }
    
    /**
     * Get distance from user to base location
     */
    fun getBaseDistanceFrom(userLatitude: Double, userLongitude: Double): Double? {
        return baseLatitude?.let { lat ->
            baseLongitude?.let { lon ->
                calculateDistance(userLatitude, userLongitude, lat, lon)
            }
        }
    }
    
    /**
     * Check if location data is recent and reliable
     */
    fun hasRecentLocation(currentTime: Long = getCurrentTimestamp()): Boolean {
        return lastLocationUpdate?.let { updateTime ->
            (currentTime - updateTime) < MAX_LOCATION_AGE_MS
        } ?: false
    }
    
    /**
     * Check if vehicle is currently offering rides
     */
    fun isAvailableForRides(): Boolean {
        return isOfferingRides && 
               capacity >= MIN_RIDE_CAPACITY &&
               hasRecentLocation() &&
               isCurrentlyOperating()
    }
    
    /**
     * Check if vehicle is currently operating (based on operating hours)
     */
    fun isCurrentlyOperating(): Boolean {
        // Simplified check - in real implementation, parse operating hours
        return operatingHours?.let { hours ->
            !hours.contains("closed", ignoreCase = true)
        } ?: true
    }
    
    /**
     * Check if vehicle has specific feature
     */
    fun hasFeature(feature: String): Boolean {
        return features.any { it.contains(feature, ignoreCase = true) }
    }
    
    /**
     * Check if vehicle has required safety equipment
     */
    fun hasRequiredSafetyEquipment(): Boolean {
        return REQUIRED_SAFETY_EQUIPMENT.all { required ->
            safetyEquipment.any { equipment ->
                equipment.contains(required, ignoreCase = true)
            }
        }
    }
    
    /**
     * Get safety compliance status
     */
    fun getSafetyComplianceStatus(): SafetyComplianceStatus {
        val hasRequired = hasRequiredSafetyEquipment()
        val hasRegistration = registrationNumber.isNotBlank()
        val hasContact = !contactInfo.isNullOrBlank()
        
        return when {
            hasRequired && hasRegistration && hasContact -> SafetyComplianceStatus.FULLY_COMPLIANT
            hasRequired && hasRegistration -> SafetyComplianceStatus.MOSTLY_COMPLIANT
            hasRegistration -> SafetyComplianceStatus.PARTIALLY_COMPLIANT
            else -> SafetyComplianceStatus.NON_COMPLIANT
        }
    }
    
    /**
     * Get estimated arrival time at destination (basic calculation)
     */
    fun getEstimatedArrivalTime(
        destinationLat: Double, 
        destinationLon: Double,
        averageSpeedKmh: Double = 10.0
    ): Long? {
        return currentLatitude?.let { lat ->
            currentLongitude?.let { lon ->
                val distance = calculateDistance(lat, lon, destinationLat, destinationLon)
                val travelTimeHours = distance / averageSpeedKmh
                val travelTimeMs = (travelTimeHours * 60 * 60 * 1000).toLong()
                getCurrentTimestamp() + travelTimeMs
            }
        }
    }
    
    /**
     * Get readable distance string for current location
     */
    fun getCurrentDistanceString(userLatitude: Double, userLongitude: Double): String {
        return getCurrentDistanceFrom(userLatitude, userLongitude)?.let { distance ->
            when {
                distance < 0.1 -> "Very close"
                distance < 0.5 -> "${(distance * 1000).toInt()}m away"
                distance < 1.0 -> "${(distance * 1000).toInt()}m away"
                else -> "${distance.formatToDecimalPlaces(1)}km away"
            }
        } ?: "Location unknown"
    }
    
    /**
     * Get location status description
     */
    fun getLocationStatus(): LocationStatus {
        return when {
            currentLatitude == null || currentLongitude == null -> LocationStatus.NO_LOCATION
            !hasRecentLocation() -> LocationStatus.STALE_LOCATION
            else -> LocationStatus.CURRENT_LOCATION
        }
    }
    
    /**
     * Get main photo URL
     */
    fun getMainPhotoUrl(): String? {
        return photoUrls.firstOrNull()
    }
    
    /**
     * Check if this is a community/shared vehicle
     */
    fun isCommunityVehicle(): Boolean {
        return artCarType == ArtCarType.BIKE_COLLECTIVE ||
               artCarType == ArtCarType.COMMUNITY_TRANSPORT ||
               name.contains("collective", ignoreCase = true) ||
               name.contains("community", ignoreCase = true)
    }
    
    /**
     * Get vehicle summary for display
     */
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        parts.add(artCarType.displayName)
        
        if (isOfferingRides) parts.add("Offering Rides")
        if (hasFeature("Sound")) parts.add("Sound System")
        if (hasFeature("Bar")) parts.add("Mobile Bar")
        if (isCommunityVehicle()) parts.add("Community Vehicle")
        
        return parts.joinToString(" • ")
    }
    
    
    /**
     * Update current location
     */
    fun updateLocation(latitude: Double, longitude: Double, timestamp: Long = getCurrentTimestamp()): MutantVehicle {
        return copy(
            currentLatitude = latitude,
            currentLongitude = longitude,
            lastLocationUpdate = timestamp,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Toggle ride offering status
     */
    fun toggleRideOffering(): MutantVehicle {
        return copy(isOfferingRides = !isOfferingRides, lastUpdated = getCurrentTimestamp())
    }
}

/**
 * Art car types
 */
enum class ArtCarType(val displayName: String) {
    DECORATED_VEHICLE("Decorated Vehicle"),
    CUSTOM_BUILD("Custom Build"),
    BIKE_COLLECTIVE("Bike Collective"),
    MOBILE_INSTALLATION("Mobile Installation"),
    SOUND_CAR("Sound Car"),
    THEME_VEHICLE("Theme Vehicle"),
    COMMUNITY_TRANSPORT("Community Transport"),
    MOBILE_BAR("Mobile Bar"),
    PERFORMANCE_STAGE("Mobile Stage"),
    WORKSHOP_MOBILE("Mobile Workshop")
}

/**
 * Fuel types for vehicles
 */
enum class FuelType(val displayName: String) {
    PETROL("Petrol"),
    DIESEL("Diesel"),
    ELECTRIC("Electric"),
    SOLAR("Solar"),
    PEDAL_POWER("Pedal Power"),
    HYBRID("Hybrid"),
    WIND_POWER("Wind Power"),
    OTHER("Other")
}

/**
 * Safety compliance status
 */
enum class SafetyComplianceStatus(val displayName: String) {
    FULLY_COMPLIANT("Fully Compliant"),
    MOSTLY_COMPLIANT("Mostly Compliant"),
    PARTIALLY_COMPLIANT("Partially Compliant"),
    NON_COMPLIANT("Non-Compliant")
}

/**
 * Location tracking status
 */
enum class LocationStatus(val displayName: String) {
    CURRENT_LOCATION("Current Location"),
    STALE_LOCATION("Stale Location"),
    NO_LOCATION("Location Unknown")
}