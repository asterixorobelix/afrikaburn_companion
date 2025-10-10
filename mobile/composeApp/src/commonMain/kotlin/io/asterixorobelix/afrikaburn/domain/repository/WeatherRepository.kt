package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.domain.model.WeatherAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository interface for weather data
 */
interface WeatherRepository {
    fun getWeatherAlerts(forceRefresh: Boolean = false): Flow<List<WeatherAlert>>
}