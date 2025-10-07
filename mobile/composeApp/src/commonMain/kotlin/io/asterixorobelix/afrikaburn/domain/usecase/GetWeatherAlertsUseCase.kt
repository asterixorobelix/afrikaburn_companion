package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.WeatherAlert
import io.asterixorobelix.afrikaburn.domain.model.WeatherAlertSeverity
import io.asterixorobelix.afrikaburn.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

/**
 * Use case for retrieving weather alerts for the event location.
 * Supports offline caching with 24-hour update intervals and filtering by severity.
 */
class GetWeatherAlertsUseCase(
    private val weatherRepository: WeatherRepository
) {
    /**
     * Gets active weather alerts for the event location.
     * 
     * @param minSeverity Optional minimum severity level to filter alerts
     * @param forceRefresh Whether to force a refresh from the network (bypassing 24-hour cache)
     * @return Flow of weather alerts, updated when new data is available
     */
    operator fun invoke(
        minSeverity: WeatherAlertSeverity? = null,
        forceRefresh: Boolean = false
    ): Flow<List<WeatherAlert>> {
        return weatherRepository.getWeatherAlerts(
            latitude = EVENT_LATITUDE,
            longitude = EVENT_LONGITUDE,
            forceRefresh = forceRefresh
        ).map { alerts ->
            val now = Clock.System.now()
            
            // Filter active alerts
            val activeAlerts = alerts.filter { alert ->
                alert.startTime <= now && alert.endTime > now
            }
            
            // Apply severity filter if requested
            val filteredAlerts = if (minSeverity != null) {
                activeAlerts.filter { alert ->
                    alert.severity.ordinal >= minSeverity.ordinal
                }
            } else {
                activeAlerts
            }
            
            // Sort by severity (highest first) and then by start time
            filteredAlerts.sortedWith(
                compareByDescending<WeatherAlert> { it.severity.ordinal }
                    .thenBy { it.startTime }
            )
        }
    }
    
    /**
     * Checks if weather data needs refreshing based on 24-hour update interval.
     * 
     * @param lastUpdateTime The timestamp of the last successful update
     * @return True if data is older than 24 hours and should be refreshed
     */
    fun shouldRefreshAlerts(lastUpdateTime: Instant?): Boolean {
        if (lastUpdateTime == null) return true
        
        val now = Clock.System.now()
        val timeSinceUpdate = now - lastUpdateTime
        
        return timeSinceUpdate >= UPDATE_INTERVAL
    }
    
    /**
     * Gets only critical weather alerts (extreme severity).
     * Includes dust storms, extreme heat, and other life-threatening conditions.
     */
    fun getCriticalAlerts(): Flow<List<WeatherAlert>> {
        return invoke(minSeverity = WeatherAlertSeverity.EXTREME)
    }
    
    /**
     * Gets weather alerts of specific types.
     * 
     * @param alertTypes List of alert types to filter (e.g., "DUST_STORM", "EXTREME_HEAT")
     * @return Flow of weather alerts matching the specified types
     */
    fun getAlertsByType(vararg alertTypes: String): Flow<List<WeatherAlert>> {
        val typeSet = alertTypes.toSet()
        
        return invoke().map { alerts ->
            alerts.filter { alert ->
                typeSet.contains(alert.type)
            }
        }
    }
    
    /**
     * Gets a summary of current weather alert conditions.
     * Useful for displaying a quick status in the UI.
     */
    fun getAlertSummary(): Flow<WeatherAlertSummary> {
        return invoke().map { alerts ->
            WeatherAlertSummary(
                totalActiveAlerts = alerts.size,
                criticalAlerts = alerts.count { it.severity == WeatherAlertSeverity.EXTREME },
                severeAlerts = alerts.count { it.severity == WeatherAlertSeverity.SEVERE },
                moderateAlerts = alerts.count { it.severity == WeatherAlertSeverity.MODERATE },
                minorAlerts = alerts.count { it.severity == WeatherAlertSeverity.MINOR },
                hasDustStorm = alerts.any { it.type == ALERT_TYPE_DUST_STORM },
                hasExtremeHeat = alerts.any { it.type == ALERT_TYPE_EXTREME_HEAT }
            )
        }
    }
    
    companion object {
        // AfrikaBurn event location coordinates (Tankwa Karoo)
        private const val EVENT_LATITUDE = -32.3281
        private const val EVENT_LONGITUDE = 19.7527
        
        // Update interval for weather data
        private val UPDATE_INTERVAL = 24.hours
        
        // Common alert types for AfrikaBurn
        const val ALERT_TYPE_DUST_STORM = "DUST_STORM"
        const val ALERT_TYPE_EXTREME_HEAT = "EXTREME_HEAT"
        const val ALERT_TYPE_HIGH_WIND = "HIGH_WIND"
        const val ALERT_TYPE_THUNDERSTORM = "THUNDERSTORM"
        const val ALERT_TYPE_FLASH_FLOOD = "FLASH_FLOOD"
    }
}

/**
 * Summary of current weather alert status.
 */
data class WeatherAlertSummary(
    val totalActiveAlerts: Int,
    val criticalAlerts: Int,
    val severeAlerts: Int,
    val moderateAlerts: Int,
    val minorAlerts: Int,
    val hasDustStorm: Boolean,
    val hasExtremeHeat: Boolean
) {
    val hasAnyAlerts: Boolean = totalActiveAlerts > 0
    val hasCriticalConditions: Boolean = criticalAlerts > 0 || hasDustStorm
}