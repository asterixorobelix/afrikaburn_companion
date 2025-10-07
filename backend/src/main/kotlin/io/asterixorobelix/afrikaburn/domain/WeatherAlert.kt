package io.asterixorobelix.afrikaburn.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherAlert(
    val id: String,
    @SerialName("alert_type")
    val alertType: String, // dust_storm, high_wind, extreme_heat, severe_weather
    val severity: String, // low, medium, high, extreme
    val title: String,
    val description: String,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("last_updated")
    val lastUpdated: Long
)