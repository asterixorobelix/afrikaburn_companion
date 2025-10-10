package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.repository.PerformanceRepository
import io.asterixorobelix.afrikaburn.domain.model.EventPerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Stub implementation of PerformanceRepository for development
 */
class PerformanceRepositoryStub : PerformanceRepository {
    override fun getEventPerformances(eventId: String): Flow<List<EventPerformance>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getPerformanceById(performanceId: String): EventPerformance? = null
    
    override suspend fun savePerformances(performances: List<EventPerformance>) {
        // No-op for stub
    }
}