package io.asterixorobelix.afrikaburn.data.remote

import io.asterixorobelix.afrikaburn.domain.model.ThemeCamp

/**
 * Mock API client for ThemeCamp
 * Placeholder implementation for compilation
 */
class ThemeCampApi {
    
    suspend fun getThemeCamps(eventId: String): List<ThemeCamp> {
        // Mock implementation - returns empty list
        return emptyList()
    }
}