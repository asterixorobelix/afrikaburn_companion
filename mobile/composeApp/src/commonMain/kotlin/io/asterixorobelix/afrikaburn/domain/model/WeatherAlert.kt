package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model for weather alerts critical to Tankwa Karoo desert safety
 */
data class WeatherAlert(
    val id: String,
    val alertType: WeatherAlertType,
    val severity: WeatherSeverity,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long?,
    val isActive: Boolean = true,
    val affectedAreas: List<String> = emptyList(),
    val safetyInstructions: List<String> = emptyList(),
    val temperature: Double?, // Celsius
    val windSpeed: Double?, // km/h
    val visibility: Double?, // km
    val recommendedActions: List<String> = emptyList(),
    val lastUpdated: Long,
    val createdAt: Long
) {
    companion object {
        val CRITICAL_ALERT_TYPES = listOf(
            WeatherAlertType.DUST_STORM,
            WeatherAlertType.EXTREME_HEAT,
            WeatherAlertType.HIGH_WIND
        )
    }
    
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               title.isNotBlank() &&
               description.isNotBlank() &&
               startTime > 0 &&
               (endTime == null || endTime > startTime)
    }
    
    fun isCritical(): Boolean = alertType in CRITICAL_ALERT_TYPES || severity == WeatherSeverity.EXTREME
    fun isCurrentlyActive(): Boolean = isActive && startTime <= getCurrentTimestamp() && 
                                      (endTime == null || endTime > getCurrentTimestamp())
    fun getDisplayColor(): String = when (severity) {
        WeatherSeverity.LOW -> "#FFA500"      // Orange
        WeatherSeverity.MEDIUM -> "#FF6347"   // Red-Orange  
        WeatherSeverity.HIGH -> "#FF0000"     // Red
        WeatherSeverity.EXTREME -> "#8B0000"  // Dark Red
    }
}

enum class WeatherAlertType {
    DUST_STORM, HIGH_WIND, EXTREME_HEAT, SEVERE_WEATHER, FLASH_FLOOD, COLD_SNAP
}

enum class WeatherSeverity {
    LOW, MEDIUM, HIGH, EXTREME
}