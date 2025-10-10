package io.asterixorobelix.afrikaburn.domain.model

import kotlin.math.log10
import kotlin.math.pow

/**
 * Domain model representing offline map data for the AfrikaBurn event
 * 
 * Manages offline map tiles, layers, and associated metadata for use in the
 * harsh Tankwa Karoo desert environment where network connectivity is limited.
 * Includes storage management within the 2GB app storage limit.
 */
data class OfflineMap(
    val id: String,
    val name: String,
    val description: String?,
    val mapType: OfflineMapType,
    val zoomLevels: IntRange, // Zoom levels included (e.g., 10..18)
    val boundingBox: MapBoundingBox, // Geographic bounds of the map
    val centerLatitude: Double,
    val centerLongitude: Double,
    val isEventMap: Boolean = true, // True for AfrikaBurn event maps
    val isOfficial: Boolean = false, // True for official event maps
    val tileCount: Long, // Number of map tiles
    val totalSizeBytes: Long, // Total storage size
    val compressionRatio: Double?, // Compression ratio if applicable
    val mapLayers: List<MapLayer> = emptyList(), // Different map layers
    val downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val downloadProgress: Float = 0f, // 0.0 to 1.0
    val downloadSpeed: Long?, // bytes per second
    val estimatedDownloadTime: Long?, // minutes
    val lastDownloadAttempt: Long?,
    val downloadUrl: String?, // URL for downloading map data
    val checksum: String?, // Verification checksum
    val version: String, // Map version for updates
    val priority: MapPriority = MapPriority.NORMAL,
    val isAutoDownload: Boolean = false, // Auto-download when available
    val requiresWifi: Boolean = true, // Only download on WiFi
    val expirationTime: Long?, // When map data expires
    val lastUsed: Long?, // Last time map was viewed
    val useCount: Int = 0, // Number of times accessed
    val isCorrupted: Boolean = false, // Data integrity flag
    val errorMessage: String?, // Last error if any
    val createdAt: Long,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Standard zoom levels for different map types
         */
        val OVERVIEW_ZOOM_LEVELS = 8..14    // Overview of entire area
        val DETAIL_ZOOM_LEVELS = 15..18     // Detailed view for navigation
        val NAVIGATION_ZOOM_LEVELS = 16..20 // High detail for walking
        
        /**
         * Maximum file size per map (1GB limit per map)
         */
        const val MAX_MAP_SIZE_BYTES = 1_073_741_824L // 1GB
        
        /**
         * Tankwa Karoo event area bounds
         */
        val EVENT_BOUNDING_BOX = MapBoundingBox(
            north = -32.15,
            south = -32.35,
            east = 20.15,
            west = 19.95
        )
        
        /**
         * Default map priorities
         */
        val PRIORITY_ORDER = listOf(
            MapPriority.CRITICAL,   // Essential navigation
            MapPriority.HIGH,       // Important areas
            MapPriority.NORMAL,     // General areas
            MapPriority.LOW         // Optional extras
        )
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               name.isNotBlank() &&
               version.isNotBlank() &&
               isValidBoundingBox() &&
               isValidZoomLevels() &&
               isValidSize() &&
               createdAt > 0 &&
               lastUpdated > 0
    }
    
    /**
     * Validate bounding box coordinates
     */
    fun isValidBoundingBox(): Boolean {
        return boundingBox.isValid() &&
               boundingBox.north >= centerLatitude &&
               boundingBox.south <= centerLatitude &&
               boundingBox.east >= centerLongitude &&
               boundingBox.west <= centerLongitude
    }
    
    /**
     * Validate zoom levels
     */
    fun isValidZoomLevels(): Boolean {
        return zoomLevels.first >= 0 &&
               zoomLevels.last <= 20 &&
               zoomLevels.first <= zoomLevels.last
    }
    
    /**
     * Validate map size
     */
    fun isValidSize(): Boolean {
        return totalSizeBytes > 0 && totalSizeBytes <= MAX_MAP_SIZE_BYTES
    }
    
    /**
     * Check if map is downloaded and available
     */
    fun isAvailable(): Boolean {
        return downloadStatus == DownloadStatus.COMPLETED && !isCorrupted
    }
    
    /**
     * Check if map is currently being downloaded
     */
    fun isDownloading(): Boolean {
        return downloadStatus == DownloadStatus.DOWNLOADING
    }
    
    /**
     * Check if map download failed
     */
    fun hasFailed(): Boolean {
        return downloadStatus == DownloadStatus.FAILED || isCorrupted
    }
    
    /**
     * Check if map data is expired
     */
    fun isExpired(currentTime: Long = getCurrentTimestamp()): Boolean {
        return expirationTime != null && currentTime > expirationTime
    }
    
    /**
     * Get estimated tiles per zoom level
     */
    fun getEstimatedTilesForZoom(zoomLevel: Int): Long {
        if (zoomLevel !in zoomLevels) return 0
        
        val latRange = boundingBox.north - boundingBox.south
        val lngRange = boundingBox.east - boundingBox.west
        val tilesPerDegree = Math.pow(2.0, zoomLevel.toDouble())
        
        val latTiles = (latRange * tilesPerDegree).toLong()
        val lngTiles = (lngRange * tilesPerDegree).toLong()
        
        return latTiles * lngTiles
    }
    
    /**
     * Get total estimated tiles across all zoom levels
     */
    fun getTotalEstimatedTiles(): Long {
        return zoomLevels.sumOf { getEstimatedTilesForZoom(it) }
    }
    
    /**
     * Get size in human-readable format
     */
    fun getSizeString(): String {
        return totalSizeBytes.toHumanReadableSize()
    }
    
    /**
     * Get download progress percentage
     */
    fun getDownloadProgressPercent(): Int {
        return (downloadProgress * 100).toInt()
    }
    
    /**
     * Get remaining download time estimate
     */
    fun getRemainingDownloadTime(): String {
        if (downloadSpeed == null || downloadStatus != DownloadStatus.DOWNLOADING) {
            return "Unknown"
        }
        
        val remainingBytes = (totalSizeBytes * (1 - downloadProgress)).toLong()
        val remainingSeconds = remainingBytes / downloadSpeed
        
        return when {
            remainingSeconds < 60 -> "${remainingSeconds}s"
            remainingSeconds < 3600 -> "${remainingSeconds / 60}m ${remainingSeconds % 60}s"
            else -> "${remainingSeconds / 3600}h ${(remainingSeconds % 3600) / 60}m"
        }
    }
    
    /**
     * Check if point is within map bounds
     */
    fun containsPoint(latitude: Double, longitude: Double): Boolean {
        return boundingBox.contains(latitude, longitude)
    }
    
    /**
     * Check if map overlaps with another map
     */
    fun overlapsWith(other: OfflineMap): Boolean {
        return boundingBox.overlapsWith(other.boundingBox)
    }
    
    /**
     * Get overlap area with another map (0.0 to 1.0)
     */
    fun getOverlapRatio(other: OfflineMap): Double {
        if (!overlapsWith(other)) return 0.0
        
        val thisArea = boundingBox.getArea()
        val overlapArea = boundingBox.getOverlapArea(other.boundingBox)
        
        return overlapArea / thisArea
    }
    
    /**
     * Check if this map is redundant with another map
     */
    fun isRedundantWith(other: OfflineMap): Boolean {
        // Consider redundant if 80% or more overlap and same type
        return mapType == other.mapType && 
               getOverlapRatio(other) > 0.8 &&
               zoomLevels.intersect(other.zoomLevels.toSet()).isNotEmpty()
    }
    
    /**
     * Get storage priority score for cleanup decisions
     */
    fun getStoragePriorityScore(): Int {
        var score = priority.score
        
        // Boost score based on usage
        score += (useCount * 5)
        
        // Boost score for recent usage
        lastUsed?.let { lastUsedTime ->
            val daysSinceUsed = (getCurrentTimestamp() - lastUsedTime) / (24 * 60 * 60 * 1000)
            score += when {
                daysSinceUsed < 1 -> 20
                daysSinceUsed < 7 -> 10
                daysSinceUsed < 30 -> 5
                else -> 0
            }
        }
        
        // Penalty for large size
        val sizePenalty = (totalSizeBytes / (100 * 1024 * 1024)).toInt() // Per 100MB
        score -= sizePenalty
        
        // Bonus for event maps
        if (isEventMap) score += 10
        if (isOfficial) score += 15
        
        return maxOf(0, score)
    }
    
    /**
     * Update download progress
     */
    fun updateDownloadProgress(
        progress: Float,
        speed: Long? = null,
        status: DownloadStatus? = null
    ): OfflineMap {
        return copy(
            downloadProgress = progress.coerceIn(0f, 1f),
            downloadSpeed = speed,
            downloadStatus = status ?: downloadStatus,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Mark download as completed
     */
    fun markDownloadCompleted(): OfflineMap {
        return copy(
            downloadStatus = DownloadStatus.COMPLETED,
            downloadProgress = 1f,
            downloadSpeed = null,
            isCorrupted = false,
            errorMessage = null,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Mark download as failed
     */
    fun markDownloadFailed(errorMessage: String): OfflineMap {
        return copy(
            downloadStatus = DownloadStatus.FAILED,
            downloadSpeed = null,
            errorMessage = errorMessage,
            lastDownloadAttempt = getCurrentTimestamp(),
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Mark as accessed/used
     */
    fun markAsUsed(): OfflineMap {
        return copy(
            lastUsed = getCurrentTimestamp(),
            useCount = useCount + 1,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Mark as corrupted
     */
    fun markAsCorrupted(reason: String): OfflineMap {
        return copy(
            isCorrupted = true,
            errorMessage = reason,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Check if map can be auto-downloaded based on settings
     */
    fun canAutoDownload(isWifiConnected: Boolean, availableStorage: Long): Boolean {
        return isAutoDownload &&
               downloadStatus == DownloadStatus.NOT_DOWNLOADED &&
               (!requiresWifi || isWifiConnected) &&
               totalSizeBytes <= availableStorage &&
               !isExpired()
    }
    
    /**
     * Get recommended action for this map
     */
    fun getRecommendedAction(availableStorage: Long): MapAction {
        return when {
            isExpired() -> MapAction.UPDATE
            isCorrupted -> MapAction.REDOWNLOAD
            downloadStatus == DownloadStatus.FAILED -> MapAction.RETRY
            downloadStatus == DownloadStatus.NOT_DOWNLOADED && 
                totalSizeBytes <= availableStorage -> MapAction.DOWNLOAD
            downloadStatus == DownloadStatus.DOWNLOADING -> MapAction.WAIT
            isAvailable() -> MapAction.USE
            totalSizeBytes > availableStorage -> MapAction.INSUFFICIENT_STORAGE
            else -> MapAction.NONE
        }
    }
}

/**
 * Types of offline maps
 */
enum class OfflineMapType {
    SATELLITE,        // Satellite imagery
    TERRAIN,          // Topographic/terrain map
    STREET,           // Street map style
    HYBRID,           // Satellite + street overlay
    EVENT_LAYOUT,     // Custom event layout map
    NIGHT_MODE        // Dark mode optimized map
}

/**
 * Map download status
 */
enum class DownloadStatus {
    NOT_DOWNLOADED,   // Map not yet downloaded
    QUEUED,          // Queued for download
    DOWNLOADING,     // Currently downloading
    PAUSED,          // Download paused
    COMPLETED,       // Successfully downloaded
    FAILED,          // Download failed
    CANCELLED        // Download cancelled by user
}

/**
 * Map priority levels for storage management
 */
enum class MapPriority(val score: Int) {
    CRITICAL(100),    // Essential for safety/navigation
    HIGH(75),         // Important areas
    NORMAL(50),       // General use
    LOW(25)          // Optional/nice-to-have
}

/**
 * Recommended actions for maps
 */
enum class MapAction {
    DOWNLOAD,         // Download the map
    UPDATE,          // Update existing map
    REDOWNLOAD,      // Re-download corrupted map
    RETRY,           // Retry failed download
    USE,             // Map is ready to use
    WAIT,            // Download in progress
    INSUFFICIENT_STORAGE, // Not enough storage space
    NONE             // No action needed
}

/**
 * Map bounding box for geographic bounds
 */
data class MapBoundingBox(
    val north: Double,   // Maximum latitude
    val south: Double,   // Minimum latitude
    val east: Double,    // Maximum longitude
    val west: Double     // Minimum longitude
) {
    fun isValid(): Boolean {
        return north > south && 
               east > west &&
               north <= 90.0 && south >= -90.0 &&
               east <= 180.0 && west >= -180.0
    }
    
    fun contains(latitude: Double, longitude: Double): Boolean {
        return latitude in south..north && longitude in west..east
    }
    
    fun overlapsWith(other: MapBoundingBox): Boolean {
        return north > other.south && 
               south < other.north && 
               east > other.west && 
               west < other.east
    }
    
    fun getArea(): Double {
        return (north - south) * (east - west)
    }
    
    fun getOverlapArea(other: MapBoundingBox): Double {
        if (!overlapsWith(other)) return 0.0
        
        val overlapNorth = minOf(north, other.north)
        val overlapSouth = maxOf(south, other.south)
        val overlapEast = minOf(east, other.east)
        val overlapWest = maxOf(west, other.west)
        
        return (overlapNorth - overlapSouth) * (overlapEast - overlapWest)
    }
    
    fun getCenter(): Pair<Double, Double> {
        return Pair(
            (north + south) / 2.0,
            (east + west) / 2.0
        )
    }
}

/**
 * Map layer information
 */
data class MapLayer(
    val id: String,
    val name: String,
    val type: LayerType,
    val isVisible: Boolean = true,
    val opacity: Float = 1.0f, // 0.0 to 1.0
    val zoomRange: IntRange? = null, // Zoom levels where layer is visible
    val sizeBytes: Long = 0,
    val lastUpdated: Long
)

/**
 * Types of map layers
 */
enum class LayerType {
    BASE_MAP,         // Base map tiles
    THEME_CAMPS,      // Theme camp locations
    ART_INSTALLATIONS, // Art installation markers
    RESOURCES,        // Water, food, toilets, etc.
    ROADS,           // Road network
    EMERGENCY,       // Emergency services
    USER_MARKERS,    // User-created markers
    EVENT_BOUNDARIES // Event area boundaries
}

/**
 * Extension function to convert bytes to human-readable size format
 */
fun Long.toHumanReadableSize(): String {
    if (this <= 0) return "0 B"
    
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    
    return "%.1f %s".format(
        this / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}