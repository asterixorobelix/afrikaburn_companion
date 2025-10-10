package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stub implementation of UserPreferencesRepository for development
 * Uses in-memory storage for preferences
 */
class UserPreferencesRepositoryStub : UserPreferencesRepository {
    
    // Theme preferences
    private val _darkMode = MutableStateFlow(false)
    private val _language = MutableStateFlow("en")
    
    // Location preferences
    private val _locationSharing = MutableStateFlow(true)
    private val _crashReporting = MutableStateFlow(true)
    private val _analytics = MutableStateFlow(false)
    
    // Notification preferences
    private val _weatherAlerts = MutableStateFlow(true)
    private val _pushNotifications = MutableStateFlow(true)
    private val _emergencyAlerts = MutableStateFlow(true)
    
    // Storage preferences
    private val _storageLimit = MutableStateFlow(2_000_000_000L) // 2GB default
    private val _autoSync = MutableStateFlow(true)
    private val _wifiOnly = MutableStateFlow(true)
    private val _cellularSync = MutableStateFlow(false)
    
    // Battery preferences
    private val _batteryOptimization = MutableStateFlow(true)
    private val _gpsPrecision = MutableStateFlow(GpsPrecisionMode.BALANCED)
    private val _syncFrequency = MutableStateFlow(SyncFrequency.WIFI_ONLY)
    
    // Onboarding
    private val _onboardingCompleted = MutableStateFlow(false)
    private val _onboardingStep = MutableStateFlow(0)
    private val _permissionsTutorialShown = MutableStateFlow(false)
    
    // Map preferences
    private val _mapStyle = MutableStateFlow(MapStyle.STANDARD)
    private val _voiceGuidance = MutableStateFlow(false)
    private val _compassReminder = MutableStateFlow(true)
    
    // Emergency info
    private val _emergencyContactInfo = MutableStateFlow<EmergencyContactInfo?>(null)
    private val _medicalInfo = MutableStateFlow<MedicalInfo?>(null)
    
    override suspend fun isDarkModeEnabled(): Boolean = _darkMode.value
    override suspend fun setDarkModeEnabled(enabled: Boolean) { _darkMode.value = enabled }
    override fun observeDarkModeEnabled(): Flow<Boolean> = _darkMode.asStateFlow()
    
    override suspend fun getPreferredLanguage(): String = _language.value
    override suspend fun setPreferredLanguage(languageCode: String) { _language.value = languageCode }
    override fun observePreferredLanguage(): Flow<String> = _language.asStateFlow()
    
    override suspend fun isLocationSharingEnabled(): Boolean = _locationSharing.value
    override suspend fun setLocationSharingEnabled(enabled: Boolean) { _locationSharing.value = enabled }
    
    override suspend fun isCrashReportingEnabled(): Boolean = _crashReporting.value
    override suspend fun setCrashReportingEnabled(enabled: Boolean) { _crashReporting.value = enabled }
    
    override suspend fun isAnalyticsEnabled(): Boolean = _analytics.value
    override suspend fun setAnalyticsEnabled(enabled: Boolean) { _analytics.value = enabled }
    
    override suspend fun areWeatherAlertsEnabled(): Boolean = _weatherAlerts.value
    override suspend fun setWeatherAlertsEnabled(enabled: Boolean) { _weatherAlerts.value = enabled }
    
    override suspend fun arePushNotificationsEnabled(): Boolean = _pushNotifications.value
    override suspend fun setPushNotificationsEnabled(enabled: Boolean) { _pushNotifications.value = enabled }
    
    override suspend fun areEmergencyAlertsEnabled(): Boolean = _emergencyAlerts.value
    override suspend fun setEmergencyAlertsEnabled(enabled: Boolean) { _emergencyAlerts.value = enabled }
    
    override suspend fun getStorageUsageLimit(): Long = _storageLimit.value
    override suspend fun setStorageUsageLimit(limitBytes: Long) { _storageLimit.value = limitBytes }
    
    override suspend fun isAutoSyncEnabled(): Boolean = _autoSync.value
    override suspend fun setAutoSyncEnabled(enabled: Boolean) { _autoSync.value = enabled }
    
    override suspend fun isSyncOnWifiOnly(): Boolean = _wifiOnly.value
    override suspend fun setSyncOnWifiOnly(wifiOnly: Boolean) { _wifiOnly.value = wifiOnly }
    
    override suspend fun isCellularSyncEnabled(): Boolean = _cellularSync.value
    override suspend fun setCellularSyncEnabled(enabled: Boolean) { _cellularSync.value = enabled }
    
    override suspend fun isBatteryOptimizationEnabled(): Boolean = _batteryOptimization.value
    override suspend fun setBatteryOptimizationEnabled(enabled: Boolean) { _batteryOptimization.value = enabled }
    
    override suspend fun getGpsPrecisionMode(): GpsPrecisionMode = _gpsPrecision.value
    override suspend fun setGpsPrecisionMode(mode: GpsPrecisionMode) { _gpsPrecision.value = mode }
    
    override suspend fun getBackgroundSyncFrequency(): SyncFrequency = _syncFrequency.value
    override suspend fun setBackgroundSyncFrequency(frequency: SyncFrequency) { _syncFrequency.value = frequency }
    
    override suspend fun hasCompletedOnboarding(): Boolean = _onboardingCompleted.value
    override suspend fun setOnboardingCompleted(completed: Boolean) { _onboardingCompleted.value = completed }
    
    override suspend fun getOnboardingStep(): Int = _onboardingStep.value
    override suspend fun setOnboardingStep(step: Int) { _onboardingStep.value = step }
    
    override suspend fun wasPermissionsTutorialShown(): Boolean = _permissionsTutorialShown.value
    override suspend fun setPermissionsTutorialShown(shown: Boolean) { _permissionsTutorialShown.value = shown }
    
    override suspend fun getMapStyle(): MapStyle = _mapStyle.value
    override suspend fun setMapStyle(style: MapStyle) { _mapStyle.value = style }
    
    override suspend fun isVoiceGuidanceEnabled(): Boolean = _voiceGuidance.value
    override suspend fun setVoiceGuidanceEnabled(enabled: Boolean) { _voiceGuidance.value = enabled }
    
    override suspend fun isCompassReminderEnabled(): Boolean = _compassReminder.value
    override suspend fun setCompassReminderEnabled(enabled: Boolean) { _compassReminder.value = enabled }
    
    override suspend fun getEmergencyContactInfo(): EmergencyContactInfo? = _emergencyContactInfo.value
    override suspend fun setEmergencyContactInfo(contactInfo: EmergencyContactInfo) { _emergencyContactInfo.value = contactInfo }
    
    override suspend fun getMedicalInfo(): MedicalInfo? = _medicalInfo.value
    override suspend fun setMedicalInfo(medicalInfo: MedicalInfo) { _medicalInfo.value = medicalInfo }
    
    override suspend fun exportPreferences(): String {
        // Simple JSON-like string export
        return """
            {
                "darkMode": ${_darkMode.value},
                "language": "${_language.value}",
                "locationSharing": ${_locationSharing.value},
                "crashReporting": ${_crashReporting.value},
                "analytics": ${_analytics.value},
                "weatherAlerts": ${_weatherAlerts.value},
                "pushNotifications": ${_pushNotifications.value},
                "emergencyAlerts": ${_emergencyAlerts.value},
                "storageLimit": ${_storageLimit.value},
                "autoSync": ${_autoSync.value},
                "wifiOnly": ${_wifiOnly.value},
                "cellularSync": ${_cellularSync.value},
                "batteryOptimization": ${_batteryOptimization.value},
                "gpsPrecision": "${_gpsPrecision.value}",
                "syncFrequency": "${_syncFrequency.value}",
                "onboardingCompleted": ${_onboardingCompleted.value},
                "onboardingStep": ${_onboardingStep.value},
                "permissionsTutorialShown": ${_permissionsTutorialShown.value},
                "mapStyle": "${_mapStyle.value}",
                "voiceGuidance": ${_voiceGuidance.value},
                "compassReminder": ${_compassReminder.value}
            }
        """.trimIndent()
    }
    
    override suspend fun importPreferences(preferencesJson: String): Result<Unit> {
        // For stub, just return success
        return Result.success(Unit)
    }
    
    override suspend fun resetToDefaults() {
        _darkMode.value = false
        _language.value = "en"
        _locationSharing.value = true
        _crashReporting.value = true
        _analytics.value = false
        _weatherAlerts.value = true
        _pushNotifications.value = true
        _emergencyAlerts.value = true
        _storageLimit.value = 2_000_000_000L
        _autoSync.value = true
        _wifiOnly.value = true
        _cellularSync.value = false
        _batteryOptimization.value = true
        _gpsPrecision.value = GpsPrecisionMode.BALANCED
        _syncFrequency.value = SyncFrequency.WIFI_ONLY
        _onboardingCompleted.value = false
        _onboardingStep.value = 0
        _permissionsTutorialShown.value = false
        _mapStyle.value = MapStyle.STANDARD
        _voiceGuidance.value = false
        _compassReminder.value = true
        _emergencyContactInfo.value = null
        _medicalInfo.value = null
    }
    
    override suspend fun getLastModifiedTimestamp(): Long = System.currentTimeMillis()
    
    override suspend fun clearAllPreferences() {
        resetToDefaults()
    }
}