package io.asterixorobelix.afrikaburn.data.local

import io.asterixorobelix.afrikaburn.data.repository.DatabaseOfflineMap

/**
 * Mock SQLDelight queries for OfflineMap
 * Placeholder implementation for compilation
 */
class OfflineMapQueries {
    
    fun selectAllOfflineMaps(): MockQuery<DatabaseOfflineMap> {
        return MockQuery<DatabaseOfflineMap>(emptyList<DatabaseOfflineMap>())
    }
    
    fun selectOfflineMapById(mapId: String): MockQuery<DatabaseOfflineMap?> {
        return MockQuery<DatabaseOfflineMap?>(null)
    }
    
    fun selectOfflineMapsByEvent(eventId: String): MockQuery<DatabaseOfflineMap> {
        return MockQuery<DatabaseOfflineMap>(emptyList<DatabaseOfflineMap>())
    }
    
    fun insertOfflineMap(
        id: String,
        eventId: String,
        boundaryNorthLat: Double,
        boundarySouthLat: Double,
        boundaryEastLng: Double,
        boundaryWestLng: Double,
        zoomLevels: String,
        tileStoragePath: String,
        sizeBytes: Long,
        lastUpdated: Long
    ) {
        // Mock implementation
    }
    
    fun updateOfflineMap(
        eventId: String,
        boundaryNorthLat: Double,
        boundarySouthLat: Double,
        boundaryEastLng: Double,
        boundaryWestLng: Double,
        zoomLevels: String,
        tileStoragePath: String,
        sizeBytes: Long,
        lastUpdated: Long,
        id: String
    ) {
        // Mock implementation
    }
    
    fun deleteOfflineMap(mapId: String) {
        // Mock implementation
    }
    
    fun transaction(body: () -> Unit) {
        body()
    }
}