package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.domain.model.EventPerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository interface for event performances
 */
interface PerformanceRepository {
    fun getEventPerformances(eventId: String): Flow<List<EventPerformance>>
    suspend fun getPerformanceById(performanceId: String): EventPerformance?
    suspend fun savePerformances(performances: List<EventPerformance>)
}