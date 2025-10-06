package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing emergency contact information for AfrikaBurn
 * 
 * Critical safety information for the harsh Tankwa Karoo desert environment
 * including radio channels, emergency services, and contact protocols.
 */
data class EmergencyContact(
    val id: String,
    val name: String,
    val type: EmergencyContactType,
    val priority: EmergencyPriority,
    val description: String?,
    val primaryContact: ContactMethod,
    val secondaryContacts: List<ContactMethod> = emptyList(),
    val availability: ContactAvailability,
    val location: String?, // General location description
    val latitude: Double?, // Exact coordinates if available
    val longitude: Double?,
    val radioChannel: String?, // Radio channel for communication
    val radioFrequency: String?, // Radio frequency
    val phoneNumber: String?, // Phone number (may not work in desert)
    val alternativeNumbers: List<String> = emptyList(),
    val emailAddress: String?, // Email for non-urgent matters
    val isActive: Boolean = true,
    val isAvailable24Hours: Boolean = false,
    val operatingHours: String?, // When service is available
    val responseTimeMinutes: Int?, // Expected response time
    val serviceArea: String?, // Area of coverage
    val capabilities: List<String> = emptyList(), // What services they provide
    val equipment: List<String> = emptyList(), // Available equipment
    val languages: List<String> = emptyList(), // Spoken languages
    val instructions: String?, // How to contact/what to say
    val emergencyProtocol: String?, // Emergency procedure
    val lastVerified: Long?, // When information was last verified
    val notes: String?, // Additional important notes
    val createdAt: Long,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Standard radio channels for AfrikaBurn
         */
        val STANDARD_RADIO_CHANNELS = mapOf(
            "EMERGENCY" to "Channel 1",
            "MEDICAL" to "Channel 2", 
            "RANGERS" to "Channel 3",
            "GATE" to "Channel 4",
            "GENERAL" to "Channel 5"
        )
        
        /**
         * Emergency response priorities
         */
        val RESPONSE_TIMES = mapOf(
            EmergencyPriority.LIFE_THREATENING to 5, // 5 minutes
            EmergencyPriority.URGENT to 15,        // 15 minutes
            EmergencyPriority.HIGH to 30,          // 30 minutes
            EmergencyPriority.NORMAL to 60         // 1 hour
        )
        
        /**
         * Common emergency capabilities
         */
        val EMERGENCY_CAPABILITIES = listOf(
            "First Aid", "Medical Emergency", "Fire Suppression", "Search & Rescue",
            "Vehicle Recovery", "Security Response", "Mental Health Support",
            "Communications", "Evacuation", "Emergency Transport"
        )
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               name.isNotBlank() &&
               primaryContact.isValid() &&
               isValidCoordinates() &&
               isValidAvailability() &&
               createdAt > 0 &&
               lastUpdated > 0
    }
    
    /**
     * Validate coordinates if provided
     */
    fun isValidCoordinates(): Boolean {
        return if (latitude != null && longitude != null) {
            latitude in -90.0..90.0 && longitude in -180.0..180.0
        } else {
            latitude == null && longitude == null
        }
    }
    
    /**
     * Validate availability information
     */
    fun isValidAvailability(): Boolean {
        return if (availability == ContactAvailability.SCHEDULED) {
            !operatingHours.isNullOrBlank()
        } else true
    }
    
    /**
     * Check if contact is currently available
     */
    fun isCurrentlyAvailable(currentTime: Long = getCurrentTimestamp()): Boolean {
        if (!isActive) return false
        
        return when (availability) {
            ContactAvailability.ALWAYS -> true
            ContactAvailability.BUSINESS_HOURS -> isBusinessHours(currentTime)
            ContactAvailability.EMERGENCY_ONLY -> type.isEmergencyType()
            ContactAvailability.SCHEDULED -> isWithinOperatingHours(currentTime)
            ContactAvailability.ON_CALL -> true // Assume on-call is available
            ContactAvailability.UNAVAILABLE -> false
        }
    }
    
    /**
     * Check if it's business hours (desert time zone)
     */
    private fun isBusinessHours(currentTime: Long): Boolean {
        // Simplified: assume 8 AM to 6 PM is business hours
        // Real implementation would use proper timezone handling
        val hour = (currentTime / (60 * 60 * 1000) % 24).toInt()
        return hour in 8..18
    }
    
    /**
     * Check if within operating hours
     */
    private fun isWithinOperatingHours(currentTime: Long): Boolean {
        // This would parse operatingHours string and check current time
        // Simplified implementation
        return operatingHours?.contains("24") == true || isBusinessHours(currentTime)
    }
    
    /**
     * Get best contact method for current situation
     */
    fun getBestContactMethod(urgency: EmergencyPriority): ContactMethod {
        return when (urgency) {
            EmergencyPriority.LIFE_THREATENING -> {
                // Radio is fastest in desert
                if (primaryContact.type == ContactType.RADIO) {
                    primaryContact
                } else {
                    secondaryContacts.find { it.type == ContactType.RADIO } ?: primaryContact
                }
            }
            EmergencyPriority.URGENT -> {
                // Radio or phone
                if (primaryContact.type in listOf(ContactType.RADIO, ContactType.PHONE)) {
                    primaryContact
                } else {
                    secondaryContacts.find { it.type in listOf(ContactType.RADIO, ContactType.PHONE) } ?: primaryContact
                }
            }
            else -> primaryContact
        }
    }
    
    /**
     * Get all available contact methods
     */
    fun getAllContactMethods(): List<ContactMethod> {
        return listOf(primaryContact) + secondaryContacts
    }
    
    /**
     * Get contact instruction for specific urgency
     */
    fun getContactInstructions(urgency: EmergencyPriority): String {
        val contactMethod = getBestContactMethod(urgency)
        val baseInstructions = instructions ?: "Contact ${name}"
        
        return when (urgency) {
            EmergencyPriority.LIFE_THREATENING -> {
                "EMERGENCY: ${baseInstructions} immediately via ${contactMethod.getDisplayText()}"
            }
            EmergencyPriority.URGENT -> {
                "URGENT: ${baseInstructions} via ${contactMethod.getDisplayText()}"
            }
            else -> {
                "${baseInstructions} via ${contactMethod.getDisplayText()}"
            }
        }
    }
    
    /**
     * Calculate distance to contact location
     */
    fun getDistanceFrom(userLatitude: Double, userLongitude: Double): Double? {
        return if (latitude != null && longitude != null) {
            calculateDistance(userLatitude, userLongitude, latitude, longitude)
        } else null
    }
    
    /**
     * Get expected response time based on distance and priority
     */
    fun getExpectedResponseTime(userLatitude: Double?, userLongitude: Double?): Int? {
        val baseTime = responseTimeMinutes ?: RESPONSE_TIMES[priority] ?: 60
        
        if (userLatitude != null && userLongitude != null) {
            val distance = getDistanceFrom(userLatitude, userLongitude)
            if (distance != null) {
                // Add travel time (assume 30 km/h for emergency vehicles in desert)
                val travelTime = (distance / 30.0 * 60).toInt()
                return baseTime + travelTime
            }
        }
        
        return baseTime
    }
    
    /**
     * Check if contact can handle specific emergency type
     */
    fun canHandleEmergency(emergencyType: EmergencyType): Boolean {
        return when (emergencyType) {
            EmergencyType.MEDICAL -> {
                type in listOf(EmergencyContactType.MEDICAL, EmergencyContactType.EMERGENCY_SERVICES) ||
                capabilities.any { it.contains("medical", ignoreCase = true) }
            }
            EmergencyType.FIRE -> {
                type in listOf(EmergencyContactType.FIRE_SAFETY, EmergencyContactType.EMERGENCY_SERVICES) ||
                capabilities.any { it.contains("fire", ignoreCase = true) }
            }
            EmergencyType.SECURITY -> {
                type in listOf(EmergencyContactType.SECURITY, EmergencyContactType.RANGERS) ||
                capabilities.any { it.contains("security", ignoreCase = true) }
            }
            EmergencyType.VEHICLE -> {
                type == EmergencyContactType.VEHICLE_RECOVERY ||
                capabilities.any { it.contains("vehicle", ignoreCase = true) || it.contains("recovery", ignoreCase = true) }
            }
            EmergencyType.SEARCH_RESCUE -> {
                type in listOf(EmergencyContactType.SEARCH_RESCUE, EmergencyContactType.RANGERS) ||
                capabilities.any { it.contains("search", ignoreCase = true) || it.contains("rescue", ignoreCase = true) }
            }
            EmergencyType.COMMUNICATION -> {
                type == EmergencyContactType.COMMUNICATION ||
                capabilities.any { it.contains("communication", ignoreCase = true) || it.contains("radio", ignoreCase = true) }
            }
            EmergencyType.GENERAL -> true // General contacts can handle any emergency
        }
    }
    
    /**
     * Get language compatibility score
     */
    fun getLanguageCompatibility(userLanguages: List<String>): Double {
        if (languages.isEmpty() || userLanguages.isEmpty()) return 0.5 // Neutral
        
        val matchingLanguages = languages.intersect(userLanguages.toSet()).size
        return matchingLanguages.toDouble() / userLanguages.size
    }
    
    /**
     * Check if information is outdated
     */
    fun isInformationStale(currentTime: Long = getCurrentTimestamp()): Boolean {
        return lastVerified?.let { verified ->
            (currentTime - verified) > (7 * 24 * 60 * 60 * 1000) // 7 days
        } ?: true
    }
    
    /**
     * Update verification timestamp
     */
    fun markAsVerified(timestamp: Long = getCurrentTimestamp()): EmergencyContact {
        return copy(
            lastVerified = timestamp,
            lastUpdated = timestamp
        )
    }
    
    /**
     * Update availability status
     */
    fun updateAvailability(newAvailability: ContactAvailability): EmergencyContact {
        return copy(
            availability = newAvailability,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Add capability
     */
    fun addCapability(capability: String): EmergencyContact {
        return copy(
            capabilities = (capabilities + capability).distinct(),
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Create emergency contact summary for quick reference
     */
    fun createSummary(): EmergencyContactSummary {
        return EmergencyContactSummary(
            id = id,
            name = name,
            type = type,
            priority = priority,
            primaryContact = primaryContact,
            isAvailable = isCurrentlyAvailable(),
            responseTimeMinutes = responseTimeMinutes,
            radioChannel = radioChannel
        )
    }
}

/**
 * Types of emergency contacts
 */
enum class EmergencyContactType {
    EMERGENCY_SERVICES,   // General emergency services
    MEDICAL,             // Medical/health services
    FIRE_SAFETY,         // Fire prevention and response
    SECURITY,            // Security services
    RANGERS,             // Event rangers
    SEARCH_RESCUE,       // Search and rescue
    VEHICLE_RECOVERY,    // Vehicle assistance
    GATE,               // Event gate services
    COMMUNICATION,       // Communication services
    MENTAL_HEALTH,       // Mental health support
    VOLUNTEER_SUPPORT,   // General volunteer assistance
    OTHER;               // Other emergency services
    
    fun isEmergencyType(): Boolean {
        return this in listOf(
            EMERGENCY_SERVICES, MEDICAL, FIRE_SAFETY, 
            SECURITY, SEARCH_RESCUE
        )
    }
}

/**
 * Emergency priority levels
 */
enum class EmergencyPriority {
    LIFE_THREATENING,    // Immediate life threat
    URGENT,             // Serious but not life threatening
    HIGH,               // Important but not urgent
    NORMAL,             // General assistance
    LOW                 // Non-urgent support
}

/**
 * Contact availability status
 */
enum class ContactAvailability {
    ALWAYS,             // 24/7 availability
    BUSINESS_HOURS,     // Standard business hours
    EMERGENCY_ONLY,     // Only for emergencies
    SCHEDULED,          // Specific scheduled times
    ON_CALL,           // On-call basis
    UNAVAILABLE        // Currently unavailable
}

/**
 * Contact method information
 */
data class ContactMethod(
    val type: ContactType,
    val value: String, // Phone number, radio channel, email, etc.
    val description: String?, // Human-readable description
    val isPreferred: Boolean = false,
    val reliability: ContactReliability = ContactReliability.NORMAL
) {
    fun isValid(): Boolean {
        return value.isNotBlank()
    }
    
    fun getDisplayText(): String {
        return when (type) {
            ContactType.RADIO -> "Radio ${value}"
            ContactType.PHONE -> "Phone ${value}"
            ContactType.EMAIL -> "Email ${value}"
            ContactType.SATELLITE_PHONE -> "Sat Phone ${value}"
            ContactType.OTHER -> description ?: value
        }
    }
}

/**
 * Types of contact methods
 */
enum class ContactType {
    RADIO,              // Radio communication
    PHONE,              // Regular phone
    SATELLITE_PHONE,    // Satellite phone for remote areas
    EMAIL,              // Email address
    OTHER               // Other communication method
}

/**
 * Contact method reliability in desert environment
 */
enum class ContactReliability {
    HIGH,               // Very reliable
    NORMAL,             // Generally reliable
    LOW,                // Sometimes unreliable
    EMERGENCY_ONLY      // Only works in emergencies
}

/**
 * Emergency types for contact matching
 */
enum class EmergencyType {
    MEDICAL,
    FIRE,
    SECURITY,
    VEHICLE,
    SEARCH_RESCUE,
    COMMUNICATION,
    GENERAL
}

/**
 * Simplified emergency contact summary
 */
data class EmergencyContactSummary(
    val id: String,
    val name: String,
    val type: EmergencyContactType,
    val priority: EmergencyPriority,
    val primaryContact: ContactMethod,
    val isAvailable: Boolean,
    val responseTimeMinutes: Int?,
    val radioChannel: String?
)