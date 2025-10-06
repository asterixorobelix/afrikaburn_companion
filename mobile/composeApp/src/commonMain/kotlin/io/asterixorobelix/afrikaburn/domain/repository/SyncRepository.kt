package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.domain.model.SyncManager
import io.asterixorobelix.afrikaburn.domain.model.ContentPackage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Sync operations
 * 
 * Manages priority-based data synchronization with 2GB storage limits,
 * intelligent content prioritization, and offline-first architecture.
 */
interface SyncRepository {
    
    // Sync Manager Operations
    
    /**
     * Get sync manager state
     */
    suspend fun getSyncManager(): SyncManager?
    
    /**
     * Save sync manager state
     */
    suspend fun saveSyncManager(syncManager: SyncManager)
    
    /**
     * Update sync manager state
     */
    suspend fun updateSyncManager(syncManager: SyncManager)
    
    /**
     * Observe sync manager state as Flow
     */
    fun observeSyncManager(): Flow<SyncManager?>
    
    // Full Sync Operations
    
    /**
     * Perform full sync of all content
     * @param eventId the event to sync
     * @param maxStorageBytes maximum storage to use (respects 2GB limit)
     */
    suspend fun performFullSync(eventId: String, maxStorageBytes: Long = 2_000_000_000L): Result<SyncResult>
    
    /**
     * Perform incremental sync (only changed content)
     * @param eventId the event to sync
     * @param lastSyncTimestamp timestamp of last successful sync
     */
    suspend fun performIncrementalSync(eventId: String, lastSyncTimestamp: Long): Result<SyncResult>
    
    /**
     * Check if full sync is needed
     */
    suspend fun isFullSyncNeeded(): Boolean
    
    /**
     * Get estimated sync size before starting
     */
    suspend fun getEstimatedSyncSize(eventId: String): Long
    
    // Content Package Management
    
    /**
     * Get all content packages
     */
    suspend fun getAllContentPackages(): List<ContentPackage>
    
    /**
     * Get content packages by priority
     */
    suspend fun getContentPackagesByPriority(): List<ContentPackage>
    
    /**
     * Get downloaded content packages
     */
    suspend fun getDownloadedContentPackages(): List<ContentPackage>
    
    /**
     * Save content package
     */
    suspend fun saveContentPackage(contentPackage: ContentPackage)
    
    /**
     * Update content package
     */
    suspend fun updateContentPackage(contentPackage: ContentPackage)
    
    /**
     * Delete content package
     */
    suspend fun deleteContentPackage(packageId: String)
    
    /**
     * Download specific content package
     */
    suspend fun downloadContentPackage(packageId: String): Flow<DownloadProgress>
    
    /**
     * Cancel content package download
     */
    suspend fun cancelContentPackageDownload(packageId: String)
    
    // Priority Management
    
    /**
     * Get sync priorities in order
     * 1. Safety & Emergency info
     * 2. Maps & Navigation
     * 3. Static content (schedules, camps, art)
     * 4. Community features
     * 5. Event schedules & performance info
     */
    suspend fun getSyncPriorities(): List<SyncPriority>
    
    /**
     * Update sync priorities
     */
    suspend fun updateSyncPriorities(priorities: List<SyncPriority>)
    
    /**
     * Apply storage cleanup based on priorities
     */
    suspend fun applyPriorityCleanup(targetSizeBytes: Long): Long
    
    // Storage Management
    
    /**
     * Get current storage usage
     */
    suspend fun getCurrentStorageUsage(): StorageUsage
    
    /**
     * Check if storage limit would be exceeded
     */
    suspend fun wouldExceedStorageLimit(additionalBytes: Long): Boolean
    
    /**
     * Clean up low-priority content to make space
     */
    suspend fun cleanupStorage(requiredBytes: Long): Long
    
    /**
     * Get storage usage by category
     */
    suspend fun getStorageUsageByCategory(): Map<String, Long>
    
    // Network and Connectivity
    
    /**
     * Check if network is available for sync
     */
    suspend fun isNetworkAvailable(): Boolean
    
    /**
     * Get current network type
     */
    suspend fun getNetworkType(): NetworkType
    
    /**
     * Should sync run on current network
     */
    suspend fun shouldSyncOnCurrentNetwork(): Boolean
    
    /**
     * Update network preferences
     */
    suspend fun updateNetworkPreferences(wifiOnly: Boolean, cellularEnabled: Boolean)
    
    // Sync Status and Monitoring
    
    /**
     * Get sync status
     */
    suspend fun getSyncStatus(): SyncStatus
    
    /**
     * Observe sync status as Flow
     */
    fun observeSyncStatus(): Flow<SyncStatus>
    
    /**
     * Get last sync error message
     */
    suspend fun getLastSyncError(): String?
    
    /**
     * Clear sync error
     */
    suspend fun clearSyncError()
    
    /**
     * Get sync statistics
     */
    suspend fun getSyncStatistics(): SyncStatistics
    
    /**
     * Reset sync statistics
     */
    suspend fun resetSyncStatistics()
}

/**
 * Sync result data class
 */
data class SyncResult(
    val success: Boolean,
    val syncedItems: Int,
    val errorCount: Int,
    val totalSizeBytes: Long,
    val duration: Long, // milliseconds
    val lastSyncTimestamp: Long
)

/**
 * Sync priority definition
 */
data class SyncPriority(
    val category: String,
    val priority: Int, // 1 = highest priority
    val maxSizeBytes: Long,
    val isRequired: Boolean
)

/**
 * Storage usage information
 */
data class StorageUsage(
    val totalUsedBytes: Long,
    val totalLimitBytes: Long,
    val availableBytes: Long,
    val usagePercentage: Float,
    val categoryBreakdown: Map<String, Long>
)

/**
 * Network type enumeration
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    NONE
}

/**
 * Sync status enumeration
 */
enum class SyncStatus {
    IDLE,
    PREPARING,
    SYNCING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Sync statistics
 */
data class SyncStatistics(
    val totalSyncs: Int,
    val successfulSyncs: Int,
    val failedSyncs: Int,
    val lastSuccessfulSync: Long,
    val lastFailedSync: Long,
    val totalDataSynced: Long,
    val averageSyncDuration: Long
)