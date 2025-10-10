package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.repository.WeatherRepository
import io.asterixorobelix.afrikaburn.domain.model.WeatherAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Stub implementation of WeatherRepository for development
 */
class WeatherRepositoryStub : WeatherRepository {
    override fun getWeatherAlerts(forceRefresh: Boolean): Flow<List<WeatherAlert>> {
        return flowOf(emptyList())
    }
}