package io.asterixorobelix.afrikaburn.platform

/**
 * Platform-specific file storage interface
 * Handles offline content storage, caching, and file management
 */
interface FileStorage {
    /**
     * Get the root directory for app storage
     */
    fun getAppStorageDirectory(): String
    
    /**
     * Get directory for cached files (can be cleared by system)
     */
    fun getCacheDirectory(): String
    
    /**
     * Get directory for offline map tiles
     */
    fun getMapTilesDirectory(): String
    
    /**
     * Get directory for downloaded images
     */
    fun getImagesDirectory(): String
    
    /**
     * Get directory for content packages
     */
    fun getContentPackagesDirectory(): String
    
    /**
     * Save data to a file
     * @param relativePath Path relative to app storage directory
     * @param data Byte array to save
     * @return true if successful
     */
    suspend fun saveFile(relativePath: String, data: ByteArray): Boolean
    
    /**
     * Read data from a file
     * @param relativePath Path relative to app storage directory
     * @return Byte array of file contents or null if not found
     */
    suspend fun readFile(relativePath: String): ByteArray?
    
    /**
     * Delete a file
     * @param relativePath Path relative to app storage directory
     * @return true if successful
     */
    suspend fun deleteFile(relativePath: String): Boolean
    
    /**
     * Check if a file exists
     * @param relativePath Path relative to app storage directory
     */
    fun fileExists(relativePath: String): Boolean
    
    /**
     * Get file size in bytes
     * @param relativePath Path relative to app storage directory
     * @return Size in bytes or -1 if file doesn't exist
     */
    fun getFileSize(relativePath: String): Long
    
    /**
     * Get total storage used by the app in bytes
     */
    suspend fun getTotalStorageUsed(): Long
    
    /**
     * Get available storage space in bytes
     */
    fun getAvailableStorageSpace(): Long
    
    /**
     * Clear all cached files
     * @return Amount of space freed in bytes
     */
    suspend fun clearCache(): Long
    
    /**
     * Clear old files based on last accessed time
     * @param maxAgeMillis Maximum age in milliseconds
     * @return Amount of space freed in bytes
     */
    suspend fun clearOldFiles(maxAgeMillis: Long): Long
    
    /**
     * Create directory if it doesn't exist
     * @param relativePath Path relative to app storage directory
     */
    fun createDirectory(relativePath: String): Boolean
    
    /**
     * List files in a directory
     * @param relativePath Path relative to app storage directory
     * @return List of file names or null if directory doesn't exist
     */
    fun listFiles(relativePath: String): List<String>?
    
    /**
     * Copy file from one location to another
     * @param sourcePath Source path relative to app storage
     * @param destinationPath Destination path relative to app storage
     */
    suspend fun copyFile(sourcePath: String, destinationPath: String): Boolean
    
    /**
     * Move file from one location to another
     * @param sourcePath Source path relative to app storage
     * @param destinationPath Destination path relative to app storage
     */
    suspend fun moveFile(sourcePath: String, destinationPath: String): Boolean
}

/**
 * File storage utilities
 */
object FileStorageUtils {
    /**
     * Format bytes to human-readable string
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            else -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
    
    /**
     * Check if enough storage is available
     */
    fun hasEnoughStorage(requiredBytes: Long, availableBytes: Long): Boolean {
        // Keep 100MB buffer
        val buffer = 100 * 1024 * 1024
        return availableBytes - buffer >= requiredBytes
    }
    
    /**
     * Generate file path for map tile
     */
    fun getMapTilePath(zoom: Int, x: Int, y: Int): String {
        return "tiles/$zoom/$x/$y.png"
    }
    
    /**
     * Generate file path for cached image
     */
    fun getImageCachePath(imageId: String, size: String = "original"): String {
        return "images/$size/$imageId"
    }
}

/**
 * Storage priority for different content types
 */
enum class StoragePriority(val value: Int) {
    CRITICAL(1),    // Safety info, emergency contacts
    HIGH(2),        // Maps and navigation
    MEDIUM(3),      // Event schedule, camp info
    LOW(4),         // Images, additional content
    OPTIONAL(5)     // Nice-to-have content
}

/**
 * Content cleanup policy
 */
data class CleanupPolicy(
    val maxCacheSize: Long = 500 * 1024 * 1024, // 500MB
    val maxCacheAge: Long = 7 * 24 * 60 * 60 * 1000, // 7 days
    val minFreeSpace: Long = 200 * 1024 * 1024, // 200MB
    val priorityThreshold: StoragePriority = StoragePriority.LOW
)

/**
 * Expected platform implementations:
 * - Android: Uses Context.filesDir and Context.cacheDir
 * - iOS: Uses NSDocumentDirectory and NSCachesDirectory
 */
expect fun createFileStorage(): FileStorage