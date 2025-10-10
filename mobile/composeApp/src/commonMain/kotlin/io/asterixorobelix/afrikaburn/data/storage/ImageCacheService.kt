package io.asterixorobelix.afrikaburn.data.storage

/**
 * Mock image cache service
 * Placeholder implementation for compilation
 */
class ImageCacheService {
    
    suspend fun downloadAndCacheImage(
        url: String,
        contentId: String,
        priority: Int
    ): String? {
        // Mock implementation - returns null
        return null
    }
    
    suspend fun getCachedImagePaths(contentId: String): List<String> {
        // Mock implementation - returns empty list
        return emptyList()
    }
    
    suspend fun clearImagesForContent(contentId: String) {
        // Mock implementation
    }
    
    suspend fun clearOldImages(keepRecent: Int): Long {
        // Mock implementation - returns 0 bytes cleared
        return 0L
    }
    
    suspend fun getTotalCacheSizeBytes(): Long {
        // Mock implementation - returns 0 bytes
        return 0L
    }
}