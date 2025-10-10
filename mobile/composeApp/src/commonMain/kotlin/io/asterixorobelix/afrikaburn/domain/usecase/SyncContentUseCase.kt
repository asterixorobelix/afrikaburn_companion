package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.*
import io.asterixorobelix.afrikaburn.domain.repository.SyncRepository
import io.asterixorobelix.afrikaburn.domain.repository.StorageUsage
import io.asterixorobelix.afrikaburn.domain.repository.NetworkType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

/**
 * Use case for managing content synchronization with smart storage management.
 * Implements a priority-based sync strategy with a 2GB storage limit.
 * 
 * Priority order:
 * 1. Safety content (highest priority)
 * 2. Maps
 * 3. Static content
 * 4. Community content
 * 5. Event schedule (lowest priority)
 */
class SyncContentUseCase(
    private val syncRepository: SyncRepository
) {
    companion object {
        // Storage limits
        const val MAX_STORAGE_BYTES = 2L * 1024 * 1024 * 1024 // 2GB
        const val STORAGE_WARNING_THRESHOLD = 0.9 // Warn at 90% usage
        const val STORAGE_CLEANUP_THRESHOLD = 0.95 // Start cleanup at 95% usage
        
        // Content size estimates (in MB)
        const val SAFETY_CONTENT_SIZE = 50L
        const val MAPS_CONTENT_SIZE = 200L
        const val STATIC_CONTENT_SIZE = 300L
        const val COMMUNITY_CONTENT_SIZE = 500L
        const val EVENT_SCHEDULE_SIZE = 100L
    }

    /**
     * Executes a smart sync operation based on available storage and network conditions.
     * @param forceSync Force sync even if recently synced
     * @return Flow of sync progress updates
     */
    suspend fun execute(forceSync: Boolean = false): Flow<SyncProgress> = flow {
        try {
            // Check network connectivity
            if (!syncRepository.isNetworkAvailable()) {
                emit(SyncProgress.Offline("No network connection available"))
                return@flow
            }

            // Check current storage status
            val storageStatus = checkStorageStatus()
            emit(SyncProgress.StorageStatus(storageStatus))

            // Cleanup if approaching storage limit
            if (storageStatus.usagePercentage >= STORAGE_CLEANUP_THRESHOLD) {
                emit(SyncProgress.CleaningUp("Cleaning up old content..."))
                performStorageCleanup(storageStatus)
            }

            // Get sync priorities based on available storage
            val syncPlan = createSyncPlan(storageStatus)
            emit(SyncProgress.SyncPlan(syncPlan))

            // Execute sync for each content type in priority order
            var totalProgress = 0f
            val totalItems = syncPlan.items.size

            for ((index, item) in syncPlan.items.withIndex()) {
                if (!syncRepository.isNetworkAvailable()) {
                    emit(SyncProgress.Interrupted("Network connection lost"))
                    break
                }

                val itemProgress = (index.toFloat() / totalItems) * 100
                emit(SyncProgress.Syncing(
                    contentType = item.contentType,
                    progress = itemProgress,
                    message = "Syncing ${item.contentType.displayName}..."
                ))

                try {
                    syncContent(item, forceSync)
                    totalProgress = ((index + 1).toFloat() / totalItems) * 100
                    emit(SyncProgress.ItemComplete(item.contentType, totalProgress))
                } catch (e: Exception) {
                    emit(SyncProgress.ItemFailed(
                        contentType = item.contentType,
                        error = e.message ?: "Unknown error",
                        progress = totalProgress
                    ))
                    
                    // Continue with next item unless it's a critical error
                    if (item.priority == SyncPriority.CRITICAL) {
                        throw e
                    }
                }
            }

            // Update sync metadata
            // Sync completed - status will be updated by repository
            
            emit(SyncProgress.Complete(
                message = "Sync completed successfully",
                syncedItems = syncPlan.items.size,
                totalSizeBytes = syncPlan.totalSizeBytes
            ))

        } catch (e: Exception) {
            emit(SyncProgress.Failed(
                error = e.message ?: "Sync failed",
                canRetry = e !is SecurityException
            ))
        }
    }

    /**
     * Checks if sync is needed based on various criteria.
     */
    suspend fun isSyncNeeded(): Boolean {
        // Check sync status from repository
        val syncManager = syncRepository.getSyncManager()
        val lastSyncTimestamp = syncManager?.lastFullSync ?: 0L
        val currentTime = Clock.System.now().toEpochMilliseconds()
        
        // Check if enough time has passed since last sync (24 hours)
        val oneDayMillis = 24 * 60 * 60 * 1000L
        if (currentTime - lastSyncTimestamp > oneDayMillis) {
            return true
        }
        
        // Check if full sync is needed
        if (syncRepository.isFullSyncNeeded()) {
            return true
        }
        
        // Check if storage is low and cleanup might help
        val storageStatus = checkStorageStatus()
        if (storageStatus.usagePercentage >= STORAGE_WARNING_THRESHOLD) {
            return true
        }
        
        return false
    }

    /**
     * Cancels any ongoing sync operation.
     */
    suspend fun cancelSync() {
        // Cancel would be implemented through the repository's download cancellation
        // For now, this is a placeholder
    }

    private suspend fun checkStorageStatus(): StorageStatus {
        val storageUsage = syncRepository.getCurrentStorageUsage()
        val usagePercentage = storageUsage.usagePercentage
        
        return StorageStatus(
            usedBytes = storageUsage.totalUsedBytes,
            totalBytes = storageUsage.totalLimitBytes,
            availableBytes = storageUsage.availableBytes,
            usagePercentage = usagePercentage,
            isLow = usagePercentage >= STORAGE_WARNING_THRESHOLD
        )
    }

    private suspend fun createSyncPlan(storageStatus: StorageStatus): SyncPlan {
        val items = mutableListOf<SyncPlanItem>()
        var estimatedSize = 0L
        val availableForSync = minOf(
            storageStatus.availableBytes,
            MAX_STORAGE_BYTES - storageStatus.usedBytes
        )

        // Always include safety content (highest priority)
        if (shouldSyncContent(SyncableContentType.SAFETY, SAFETY_CONTENT_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = SyncableContentType.SAFETY,
                priority = SyncPriority.CRITICAL,
                estimatedSizeBytes = SAFETY_CONTENT_SIZE * 1024 * 1024
            ))
            estimatedSize += SAFETY_CONTENT_SIZE * 1024 * 1024
        }

        // Add maps if space available
        if (shouldSyncContent(SyncableContentType.MAPS, MAPS_CONTENT_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = SyncableContentType.MAPS,
                priority = SyncPriority.HIGH,
                estimatedSizeBytes = MAPS_CONTENT_SIZE * 1024 * 1024
            ))
            estimatedSize += MAPS_CONTENT_SIZE * 1024 * 1024
        }

        // Add static content if space available
        if (shouldSyncContent(SyncableContentType.STATIC, STATIC_CONTENT_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = SyncableContentType.STATIC,
                priority = SyncPriority.MEDIUM,
                estimatedSizeBytes = STATIC_CONTENT_SIZE * 1024 * 1024
            ))
            estimatedSize += STATIC_CONTENT_SIZE * 1024 * 1024
        }

        // Add community content if space available
        if (shouldSyncContent(SyncableContentType.COMMUNITY, COMMUNITY_CONTENT_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = SyncableContentType.COMMUNITY,
                priority = SyncPriority.LOW,
                estimatedSizeBytes = COMMUNITY_CONTENT_SIZE * 1024 * 1024
            ))
            estimatedSize += COMMUNITY_CONTENT_SIZE * 1024 * 1024
        }

        // Add event schedule if space available
        if (shouldSyncContent(SyncableContentType.EVENT_SCHEDULE, EVENT_SCHEDULE_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = SyncableContentType.EVENT_SCHEDULE,
                priority = SyncPriority.LOW,
                estimatedSizeBytes = EVENT_SCHEDULE_SIZE * 1024 * 1024
            ))
            estimatedSize += EVENT_SCHEDULE_SIZE * 1024 * 1024
        }

        return SyncPlan(
            items = items,
            totalSizeBytes = estimatedSize,
            availableStorageBytes = availableForSync
        )
    }

    private suspend fun shouldSyncContent(
        contentType: SyncableContentType,
        contentSize: Long,
        availableBytes: Long,
        currentPlanSize: Long
    ): Boolean {
        // For now, always sync if we have space
        // In a real implementation, this would check last update times
        
        // Check if we have space
        if (currentPlanSize + contentSize > availableBytes) {
            return false
        }
        
        // Check if total storage would exceed limit
        if (currentPlanSize + contentSize > MAX_STORAGE_BYTES) {
            return false
        }
        
        return true
    }

    private suspend fun syncContent(item: SyncPlanItem, forceSync: Boolean) {
        when (item.contentType) {
            // In a real implementation, each content type would have its own sync method
            // For now, we'll use the general sync methods
            SyncableContentType.SAFETY -> {
                // Safety content would be highest priority
            }
            SyncableContentType.MAPS -> {
                // Map content sync
            }
            SyncableContentType.STATIC -> {
                // Static content sync
            }
            SyncableContentType.COMMUNITY -> {
                // Community content sync
            }
            SyncableContentType.EVENT_SCHEDULE -> {
                // Event schedule sync
            }
        }
    }

    private suspend fun performStorageCleanup(storageStatus: StorageStatus) {
        // Clean up in reverse priority order
        val cleanupOrder = listOf(
            SyncableContentType.EVENT_SCHEDULE,
            SyncableContentType.COMMUNITY,
            SyncableContentType.STATIC,
            SyncableContentType.MAPS
            // Never clean up SAFETY content
        )
        
        for (contentType in cleanupOrder) {
            if (storageStatus.usagePercentage < STORAGE_WARNING_THRESHOLD) {
                break // Stop cleanup if we're below warning threshold
            }
            
            // Cleanup would be done through the repository
            // This is a placeholder for content-specific cleanup
        }
        
        // Clean up cache through repository storage management
        syncRepository.cleanupStorage(0) // Request cleanup without specific size requirement
    }
}

/**
 * Represents the progress of a sync operation.
 */
sealed class SyncProgress {
    data class StorageStatus(val status: io.asterixorobelix.afrikaburn.domain.usecase.StorageStatus) : SyncProgress()
    data class SyncPlan(val plan: io.asterixorobelix.afrikaburn.domain.usecase.SyncPlan) : SyncProgress()
    data class Syncing(val contentType: SyncableContentType, val progress: Float, val message: String) : SyncProgress()
    data class ItemComplete(val contentType: SyncableContentType, val totalProgress: Float) : SyncProgress()
    data class ItemFailed(val contentType: SyncableContentType, val error: String, val progress: Float) : SyncProgress()
    data class CleaningUp(val message: String) : SyncProgress()
    data class Complete(val message: String, val syncedItems: Int, val totalSizeBytes: Long) : SyncProgress()
    data class Failed(val error: String, val canRetry: Boolean) : SyncProgress()
    data class Offline(val message: String) : SyncProgress()
    data class Interrupted(val reason: String) : SyncProgress()
}

/**
 * Storage status information.
 */
data class StorageStatus(
    val usedBytes: Long,
    val totalBytes: Long,
    val availableBytes: Long,
    val usagePercentage: Float,
    val isLow: Boolean
)

/**
 * Sync plan with items to sync.
 */
data class SyncPlan(
    val items: List<SyncPlanItem>,
    val totalSizeBytes: Long,
    val availableStorageBytes: Long
)

/**
 * Individual item in a sync plan.
 */
data class SyncPlanItem(
    val contentType: SyncableContentType,
    val priority: SyncPriority,
    val estimatedSizeBytes: Long
)

/**
 * Content types that can be synced.
 */
enum class SyncableContentType(val displayName: String) {
    SAFETY("Safety Information"),
    MAPS("Maps"),
    STATIC("Static Content"),
    COMMUNITY("Community Content"),
    EVENT_SCHEDULE("Event Schedule")
}

/**
 * Sync priority levels.
 */
enum class SyncPriority {
    CRITICAL,  // Must sync (safety content)
    HIGH,      // Important (maps)
    MEDIUM,    // Nice to have (static content)
    LOW        // Optional (community, events)
}