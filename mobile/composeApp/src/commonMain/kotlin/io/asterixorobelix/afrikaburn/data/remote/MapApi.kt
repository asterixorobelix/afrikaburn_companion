package io.asterixorobelix.afrikaburn.data.remote

import io.asterixorobelix.afrikaburn.domain.model.OfflineMap
import io.asterixorobelix.afrikaburn.domain.model.MapPin

/**
 * Mock API client for Map data
 * Placeholder implementation for compilation
 */
class MapApi {
    
    suspend fun getOfflineMaps(eventId: String): List<OfflineMap> {
        // Mock implementation - returns empty list
        return emptyList()
    }
    
    suspend fun getMapPins(eventId: String): List<MapPin> {
        // Mock implementation - returns empty list
        return emptyList()
    }
}