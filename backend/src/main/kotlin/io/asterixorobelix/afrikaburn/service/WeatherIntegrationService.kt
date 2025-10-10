package io.asterixorobelix.afrikaburn.service

import io.asterixorobelix.afrikaburn.domain.WeatherAlert
import io.asterixorobelix.afrikaburn.domain.WeatherCondition
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

/**
 * Weather integration service for 24-hour updates.
 * 
 * Monitors desert conditions and provides alerts for:
 * - Dust storms (visibility warnings)
 * - Extreme temperatures (heat/cold warnings)
 * - Wind conditions (structure safety)
 * - UV index (sun protection reminders)
 * 
 * In production, this would integrate with weather APIs.
 * Currently provides realistic mock data for the Tankwa Karoo.
 */
class WeatherIntegrationService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var updateJob: Job? = null
    
    companion object {
        const val UPDATE_INTERVAL_HOURS = 24L
        const val TANKWA_LAT = -32.551296
        const val TANKWA_LON = 19.988442
        
        // Typical Tankwa Karoo conditions in April/May
        const val TEMP_DAY_MIN = 20
        const val TEMP_DAY_MAX = 35
        const val TEMP_NIGHT_MIN = 5
        const val TEMP_NIGHT_MAX = 15
        const val WIND_SPEED_MIN = 10
        const val WIND_SPEED_MAX = 50
        const val UV_INDEX_MIN = 8
        const val UV_INDEX_MAX = 11
    }
    
    /**
     * Starts the weather update service
     */
    fun startWeatherUpdates() {
        updateJob = scope.launch {
            while (isActive) {
                try {
                    updateWeatherData()
                    delay(UPDATE_INTERVAL_HOURS.hours)
                } catch (e: Exception) {
                    // Log error and continue
                    println("Weather update failed: ${e.message}")
                    delay(1.hours) // Retry after 1 hour on failure
                }
            }
        }
    }
    
    /**
     * Stops the weather update service
     */
    fun stopWeatherUpdates() {
        updateJob?.cancel()
        updateJob = null
    }
    
    /**
     * Gets current weather conditions
     */
    suspend fun getCurrentWeather(): WeatherCondition {
        // In production, fetch from weather API
        // For now, generate realistic mock data
        val now = Clock.System.now()
        val localTime = now.toLocalDateTime(TimeZone.of("Africa/Johannesburg"))
        val hour = localTime.hour
        
        val isNight = hour < 6 || hour > 18
        val temperature = if (isNight) {
            Random.nextInt(TEMP_NIGHT_MIN, TEMP_NIGHT_MAX + 1)
        } else {
            Random.nextInt(TEMP_DAY_MIN, TEMP_DAY_MAX + 1)
        }
        
        return WeatherCondition(
            temperature = temperature,
            feelsLike = temperature + Random.nextInt(-3, 4),
            humidity = Random.nextInt(10, 30), // Desert is dry
            windSpeed = Random.nextInt(WIND_SPEED_MIN, WIND_SPEED_MAX + 1),
            windDirection = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW").random(),
            uvIndex = if (isNight) 0 else Random.nextInt(UV_INDEX_MIN, UV_INDEX_MAX + 1),
            visibility = if (Random.nextFloat() < 0.1) {
                Random.nextInt(100, 1000) // 10% chance of dust
            } else {
                10000 // Clear visibility
            },
            conditions = generateConditions(temperature, isNight),
            lastUpdated = now
        )
    }
    
    /**
     * Gets active weather alerts
     */
    suspend fun getActiveAlerts(): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()
        val now = Clock.System.now()
        val weather = getCurrentWeather()
        
        // High temperature alert
        if (weather.temperature >= 32) {
            alerts.add(
                WeatherAlert(
                    id = "heat-${now.epochSeconds}",
                    severity = WeatherAlert.Severity.HIGH,
                    type = WeatherAlert.Type.EXTREME_HEAT,
                    title = "Extreme Heat Warning",
                    description = "Temperature ${weather.temperature}°C. Stay hydrated, seek shade during peak hours, and check on campmates.",
                    startTime = now,
                    endTime = now.plus(4.hours),
                    affectedAreas = listOf("Entire Event")
                )
            )
        }
        
        // Low temperature alert
        if (weather.temperature <= 10) {
            alerts.add(
                WeatherAlert(
                    id = "cold-${now.epochSeconds}",
                    severity = WeatherAlert.Severity.MEDIUM,
                    type = WeatherAlert.Type.COLD,
                    title = "Cold Weather Advisory",
                    description = "Temperature ${weather.temperature}°C. Wear warm clothing and check heating in camps.",
                    startTime = now,
                    endTime = now.plus(6.hours),
                    affectedAreas = listOf("Entire Event")
                )
            )
        }
        
        // Wind alert
        if (weather.windSpeed >= 35) {
            alerts.add(
                WeatherAlert(
                    id = "wind-${now.epochSeconds}",
                    severity = WeatherAlert.Severity.HIGH,
                    type = WeatherAlert.Type.HIGH_WIND,
                    title = "High Wind Warning",
                    description = "Wind speed ${weather.windSpeed} km/h. Secure all structures, art, and camp items. Avoid tall structures.",
                    startTime = now,
                    endTime = now.plus(2.hours),
                    affectedAreas = listOf("Entire Event")
                )
            )
        }
        
        // Dust storm alert
        if (weather.visibility < 1000) {
            alerts.add(
                WeatherAlert(
                    id = "dust-${now.epochSeconds}",
                    severity = WeatherAlert.Severity.HIGH,
                    type = WeatherAlert.Type.DUST_STORM,
                    title = "Dust Storm Warning",
                    description = "Visibility reduced to ${weather.visibility}m. Wear goggles and dust masks. Avoid driving if possible.",
                    startTime = now,
                    endTime = now.plus(1.hours),
                    affectedAreas = listOf("Entire Event")
                )
            )
        }
        
        // UV alert
        if (weather.uvIndex >= 8) {
            alerts.add(
                WeatherAlert(
                    id = "uv-${now.epochSeconds}",
                    severity = WeatherAlert.Severity.MEDIUM,
                    type = WeatherAlert.Type.HIGH_UV,
                    title = "High UV Index",
                    description = "UV Index ${weather.uvIndex}. Apply sunscreen regularly, wear protective clothing and hats.",
                    startTime = now.plus(2.hours), // Usually starts mid-morning
                    endTime = now.plus(8.hours),
                    affectedAreas = listOf("Entire Event")
                )
            )
        }
        
        return alerts.sortedByDescending { it.severity.priority }
    }
    
    /**
     * Updates weather data (would call external API in production)
     */
    private suspend fun updateWeatherData() {
        // In production, this would:
        // 1. Call weather API with TANKWA_LAT, TANKWA_LON
        // 2. Parse response
        // 3. Store in database
        // 4. Generate alerts based on conditions
        
        val weather = getCurrentWeather()
        val alerts = getActiveAlerts()
        
        // Log for monitoring
        println("Weather updated: ${weather.temperature}°C, Wind: ${weather.windSpeed}km/h, Alerts: ${alerts.size}")
    }
    
    /**
     * Generates weather condition description
     */
    private fun generateConditions(temperature: Int, isNight: Boolean): String {
        return when {
            temperature >= 32 -> "Hot and dry"
            temperature <= 10 -> "Cold and clear"
            isNight -> "Clear night"
            else -> "Clear and sunny"
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        scope.cancel()
    }
}