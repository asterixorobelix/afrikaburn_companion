package io.asterixorobelix.afrikaburn.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for User Preferences operations
 * 
 * Manages user settings, privacy preferences, and app configuration
 * with secure local storage and easy backup/restore capabilities.
 */
interface UserPreferencesRepository {
    
    // Theme and UI Preferences
    
    /**
     * Get dark mode preference
     */
    suspend fun isDarkModeEnabled(): Boolean
    
    /**
     * Set dark mode preference
     */
    suspend fun setDarkModeEnabled(enabled: Boolean)
    
    /**
     * Observe dark mode preference changes
     */
    fun observeDarkModeEnabled(): Flow<Boolean>
    
    /**
     * Get preferred language
     */
    suspend fun getPreferredLanguage(): String
    
    /**
     * Set preferred language
     */
    suspend fun setPreferredLanguage(languageCode: String)
    
    /**
     * Observe language preference changes
     */
    fun observePreferredLanguage(): Flow<String>
    
    // Location and Privacy Preferences
    
    /**
     * Get location sharing preference
     */
    suspend fun isLocationSharingEnabled(): Boolean
    
    /**
     * Set location sharing preference
     */
    suspend fun setLocationSharingEnabled(enabled: Boolean)
    
    /**
     * Get crash reporting preference
     */
    suspend fun isCrashReportingEnabled(): Boolean
    
    /**
     * Set crash reporting preference
     */
    suspend fun setCrashReportingEnabled(enabled: Boolean)
    
    /**
     * Get analytics preference
     */
    suspend fun isAnalyticsEnabled(): Boolean
    
    /**
     * Set analytics preference
     */
    suspend fun setAnalyticsEnabled(enabled: Boolean)
    
    // Notification Preferences
    
    /**
     * Get weather alerts preference
     */
    suspend fun areWeatherAlertsEnabled(): Boolean
    
    /**
     * Set weather alerts preference
     */
    suspend fun setWeatherAlertsEnabled(enabled: Boolean)
    
    /**
     * Get push notifications preference
     */
    suspend fun arePushNotificationsEnabled(): Boolean
    
    /**
     * Set push notifications preference
     */
    suspend fun setPushNotificationsEnabled(enabled: Boolean)
    
    /**
     * Get emergency alerts preference
     */
    suspend fun areEmergencyAlertsEnabled(): Boolean
    
    /**
     * Set emergency alerts preference
     */
    suspend fun setEmergencyAlertsEnabled(enabled: Boolean)
    
    // Storage and Sync Preferences
    
    /**
     * Get storage usage limit
     */
    suspend fun getStorageUsageLimit(): Long
    
    /**
     * Set storage usage limit
     */
    suspend fun setStorageUsageLimit(limitBytes: Long)
    
    /**
     * Get auto-sync preference
     */
    suspend fun isAutoSyncEnabled(): Boolean
    
    /**
     * Set auto-sync preference
     */
    suspend fun setAutoSyncEnabled(enabled: Boolean)
    
    /**
     * Get WiFi-only sync preference
     */
    suspend fun isSyncOnWifiOnly(): Boolean
    
    /**
     * Set WiFi-only sync preference
     */
    suspend fun setSyncOnWifiOnly(wifiOnly: Boolean)
    
    /**
     * Get cellular data sync preference
     */
    suspend fun isCellularSyncEnabled(): Boolean
    
    /**
     * Set cellular data sync preference
     */
    suspend fun setCellularSyncEnabled(enabled: Boolean)
    
    // Battery and Performance Preferences
    
    /**
     * Get battery optimization preference
     */
    suspend fun isBatteryOptimizationEnabled(): Boolean
    
    /**
     * Set battery optimization preference
     */
    suspend fun setBatteryOptimizationEnabled(enabled: Boolean)
    
    /**
     * Get GPS precision mode
     */
    suspend fun getGpsPrecisionMode(): GpsPrecisionMode
    
    /**
     * Set GPS precision mode
     */
    suspend fun setGpsPrecisionMode(mode: GpsPrecisionMode)
    
    /**
     * Get background sync frequency
     */
    suspend fun getBackgroundSyncFrequency(): SyncFrequency
    
    /**
     * Set background sync frequency
     */
    suspend fun setBackgroundSyncFrequency(frequency: SyncFrequency)
    
    // Onboarding and First-Time Setup
    
    /**
     * Check if user has completed onboarding
     */
    suspend fun hasCompletedOnboarding(): Boolean
    
    /**
     * Mark onboarding as completed
     */
    suspend fun setOnboardingCompleted(completed: Boolean)
    
    /**
     * Get onboarding step progress
     */
    suspend fun getOnboardingStep(): Int
    
    /**
     * Set onboarding step progress
     */
    suspend fun setOnboardingStep(step: Int)
    
    /**
     * Check if permissions tutorial was shown
     */
    suspend fun wasPermissionsTutorialShown(): Boolean
    
    /**
     * Mark permissions tutorial as shown
     */
    suspend fun setPermissionsTutorialShown(shown: Boolean)
    
    // Map and Navigation Preferences
    
    /**
     * Get map style preference
     */
    suspend fun getMapStyle(): MapStyle
    
    /**
     * Set map style preference
     */
    suspend fun setMapStyle(style: MapStyle)
    
    /**
     * Get navigation voice guidance preference
     */
    suspend fun isVoiceGuidanceEnabled(): Boolean
    
    /**
     * Set navigation voice guidance preference
     */
    suspend fun setVoiceGuidanceEnabled(enabled: Boolean)
    
    /**
     * Get compass calibration reminder preference
     */
    suspend fun isCompassReminderEnabled(): Boolean
    
    /**
     * Set compass calibration reminder preference
     */
    suspend fun setCompassReminderEnabled(enabled: Boolean)
    
    // Emergency and Safety Preferences
    
    /**
     * Get emergency contact info
     */
    suspend fun getEmergencyContactInfo(): EmergencyContactInfo?
    
    /**
     * Set emergency contact info
     */
    suspend fun setEmergencyContactInfo(contactInfo: EmergencyContactInfo)
    
    /**
     * Get medical information for emergencies
     */
    suspend fun getMedicalInfo(): MedicalInfo?
    
    /**
     * Set medical information for emergencies
     */
    suspend fun setMedicalInfo(medicalInfo: MedicalInfo)
    
    // Backup and Restore
    
    /**
     * Export all preferences as JSON
     */
    suspend fun exportPreferences(): String
    
    /**
     * Import preferences from JSON
     */
    suspend fun importPreferences(preferencesJson: String): Result<Unit>
    
    /**
     * Reset all preferences to defaults
     */
    suspend fun resetToDefaults()
    
    /**
     * Get preferences last modified timestamp
     */
    suspend fun getLastModifiedTimestamp(): Long
    
    /**
     * Clear all preferences (for logout/reset)
     */
    suspend fun clearAllPreferences()
}

/**
 * GPS precision modes for battery optimization
 */
enum class GpsPrecisionMode {
    HIGH_ACCURACY,      // Best accuracy, highest battery usage
    BALANCED,           // Good accuracy, moderate battery usage
    POWER_SAVER,        // Lower accuracy, minimal battery usage
    GPS_ONLY           // GPS only, no network assistance
}

/**
 * Background sync frequency options
 */
enum class SyncFrequency {
    NEVER,              // Manual sync only
    WIFI_ONLY,          // Only when on WiFi
    HOURLY,             // Every hour
    EVERY_4_HOURS,      // Every 4 hours
    DAILY,              // Once per day
    REAL_TIME          // Continuous sync (high battery usage)
}

/**
 * Map style options
 */
enum class MapStyle {
    STANDARD,           // Standard map view
    SATELLITE,          // Satellite imagery
    TERRAIN,            // Topographic view
    HYBRID,             // Satellite with labels
    DARK_MODE          // Dark theme optimized
}

/**
 * Emergency contact information
 */
data class EmergencyContactInfo(
    val name: String,
    val phoneNumber: String,
    val relationship: String, // e.g., "Partner", "Friend", "Family"
    val isEnabled: Boolean = true
)

/**
 * Medical information for emergencies
 */
data class MedicalInfo(
    val allergies: List<String> = emptyList(),
    val medications: List<String> = emptyList(),
    val medicalConditions: List<String> = emptyList(),
    val bloodType: String? = null,
    val additionalNotes: String? = null,
    val isEnabled: Boolean = false // User must explicitly enable
)