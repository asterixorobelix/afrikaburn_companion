package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.domain.model.OfflineMap
import io.asterixorobelix.afrikaburn.domain.model.MapPin
import io.asterixorobelix.afrikaburn.domain.model.CampLocation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Map operations
 * 
 * Handles offline map tile management, map pins, and user-marked locations
 * with intelligent storage management within the 2GB app limit.
 */
interface MapRepository {
    
    /**
     * Get all offline maps
     */
    suspend fun getAllOfflineMaps(): List<OfflineMap>
    
    /**
     * Get offline map by ID
     */
    suspend fun getOfflineMapById(mapId: String): OfflineMap?
    
    /**
     * Get offline maps for an event
     */
    suspend fun getOfflineMapsByEvent(eventId: String): List<OfflineMap>
    
    /**
     * Observe offline maps as Flow
     */
    fun observeOfflineMaps(): Flow<List<OfflineMap>>
    
    /**
     * Save offline map
     */
    suspend fun saveOfflineMap(offlineMap: OfflineMap)
    
    /**
     * Update offline map
     */
    suspend fun updateOfflineMap(offlineMap: OfflineMap)
    
    /**
     * Delete offline map
     */
    suspend fun deleteOfflineMap(mapId: String)
    
    /**
     * Download offline map tiles
     * @param mapId the map to download
     * @param priority download priority for storage management
     */
    suspend fun downloadOfflineMap(mapId: String, priority: Int = 0): Flow<DownloadProgress>
    
    /**
     * Check if offline map is downloaded
     */
    suspend fun isOfflineMapDownloaded(mapId: String): Boolean
    
    /**
     * Get download progress for offline map
     */
    suspend fun getDownloadProgress(mapId: String): Float
    
    /**
     * Cancel offline map download
     */
    suspend fun cancelDownload(mapId: String)
    
    // Map Pins
    
    /**
     * Get all map pins for a map
     */
    suspend fun getMapPinsByMap(mapId: String): List<MapPin>
    
    /**
     * Get map pins by content type
     */
    suspend fun getMapPinsByContentType(mapId: String, contentType: String): List<MapPin>
    
    /**
     * Get map pins within radius
     */
    suspend fun getMapPinsNearLocation(
        mapId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 1.0
    ): List<MapPin>
    
    /**
     * Save map pin
     */
    suspend fun saveMapPin(mapPin: MapPin)
    
    /**
     * Save multiple map pins (batch operation)
     */
    suspend fun saveMapPins(mapPins: List<MapPin>)
    
    /**
     * Update map pin
     */
    suspend fun updateMapPin(mapPin: MapPin)
    
    /**
     * Delete map pin
     */
    suspend fun deleteMapPin(pinId: String)
    
    /**
     * Delete map pins by content ID
     */
    suspend fun deleteMapPinsByContentId(contentId: String)
    
    // Camp Locations (User-marked)
    
    /**
     * Get all camp locations for a device
     */
    suspend fun getCampLocationsByDevice(deviceId: String): List<CampLocation>
    
    /**
     * Get camp location by ID
     */
    suspend fun getCampLocationById(locationId: String): CampLocation?
    
    /**
     * Observe camp locations for a device as Flow
     */
    fun observeCampLocationsByDevice(deviceId: String): Flow<List<CampLocation>>
    
    /**
     * Save camp location
     */
    suspend fun saveCampLocation(campLocation: CampLocation)
    
    /**
     * Update camp location
     */
    suspend fun updateCampLocation(campLocation: CampLocation)
    
    /**
     * Delete camp location
     */
    suspend fun deleteCampLocation(locationId: String)
    
    /**
     * Get active camp location for device
     */
    suspend fun getActiveCampLocation(deviceId: String): CampLocation?
    
    /**
     * Mark camp location as active (deactivates others)
     */
    suspend fun setActiveCampLocation(locationId: String)
    
    // Storage Management
    
    /**
     * Get total storage used by maps
     */
    suspend fun getTotalMapStorageBytes(): Long
    
    /**
     * Clean up map storage based on priority and usage
     */
    suspend fun cleanupMapStorage(targetSizeBytes: Long): Long
    
    /**
     * Get map storage statistics
     */
    suspend fun getMapStorageStats(): MapStorageStats
    
    /**
     * Check available storage space
     */
    suspend fun getAvailableStorageBytes(): Long
    
    /**
     * Sync map data from remote source
     */
    suspend fun syncMapData(eventId: String, forceRefresh: Boolean = false): Result<Unit>
    
    /**
     * Validate map boundaries against event location
     */
    suspend fun validateMapBoundaries(
        eventLatitude: Double,
        eventLongitude: Double,
        eventRadiusKm: Double
    ): Boolean
}

/**
 * Data class for map storage statistics
 */
data class MapStorageStats(
    val totalSizeBytes: Long,
    val tilesCount: Long,
    val mapsCount: Int,
    val lastCleanupTimestamp: Long
)

/**
 * Data class for download progress tracking
 */
data class DownloadProgress(
    val mapId: String,
    val progress: Float, // 0.0 to 1.0
    val downloadedBytes: Long,
    val totalBytes: Long,
    val status: DownloadStatus
)

/**
 * Download status enumeration
 */
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}