package io.asterixorobelix.afrikaburn.data.storage

/**
 * Mock tile storage service for offline maps
 * Placeholder implementation for compilation
 */
class TileStorageService {
    
    suspend fun downloadTiles(mapId: String, zoomLevels: List<Int>): Long {
        // Mock implementation - returns 0 bytes downloaded
        return 0L
    }
    
    suspend fun getTileStorageSize(mapId: String): Long {
        // Mock implementation - returns 0 bytes
        return 0L
    }
    
    suspend fun deleteTiles(mapId: String) {
        // Mock implementation
    }
    
    suspend fun getTotalStorageUsed(): Long {
        // Mock implementation - returns 0 bytes
        return 0L
    }
    
    suspend fun cleanupOldTiles(keepRecentDays: Int): Long {
        // Mock implementation - returns 0 bytes freed
        return 0L
    }
}