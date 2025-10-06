package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing an AfrikaBurn event participant
 * 
 * Represents a user of the app with their device-specific preferences and settings.
 * Uses device ID as primary identifier to maintain privacy and avoid personal data collection.
 */
data class Participant(
    val deviceId: String,
    val isDarkModeEnabled: Boolean = false,
    val preferredLanguage: String = "en",
    val hasCompletedOnboarding: Boolean = false,
    val lastSyncTimestamp: Long = 0,
    val batteryOptimizationEnabled: Boolean = true
) {
    companion object {
        /**
         * Supported language codes
         */
        val SUPPORTED_LANGUAGES = listOf("en", "af", "zu", "xh")
        
        /**
         * Default participant for new devices
         */
        fun createDefault(deviceId: String): Participant {
            return Participant(
                deviceId = deviceId,
                isDarkModeEnabled = false,
                preferredLanguage = "en",
                hasCompletedOnboarding = false,
                lastSyncTimestamp = 0,
                batteryOptimizationEnabled = true
            )
        }
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return deviceId.isNotBlank() && 
               preferredLanguage in SUPPORTED_LANGUAGES &&
               lastSyncTimestamp >= 0
    }
    
    /**
     * Check if sync is needed based on timestamp
     */
    fun needsSync(currentTime: Long = getCurrentTimestamp()): Boolean {
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        return (currentTime - lastSyncTimestamp) > oneDayInMillis
    }
    
    /**
     * Update sync timestamp
     */
    fun updateSyncTimestamp(timestamp: Long = getCurrentTimestamp()): Participant {
        return copy(lastSyncTimestamp = timestamp)
    }
    
    /**
     * Complete onboarding
     */
    fun completeOnboarding(): Participant {
        return copy(hasCompletedOnboarding = true)
    }
    
    /**
     * Toggle dark mode
     */
    fun toggleDarkMode(): Participant {
        return copy(isDarkModeEnabled = !isDarkModeEnabled)
    }
    
    /**
     * Update language preference
     */
    fun updateLanguage(language: String): Participant {
        require(language in SUPPORTED_LANGUAGES) { 
            "Unsupported language: $language. Supported: $SUPPORTED_LANGUAGES" 
        }
        return copy(preferredLanguage = language)
    }
    
    /**
     * Toggle battery optimization
     */
    fun toggleBatteryOptimization(): Participant {
        return copy(batteryOptimizationEnabled = !batteryOptimizationEnabled)
    }
}