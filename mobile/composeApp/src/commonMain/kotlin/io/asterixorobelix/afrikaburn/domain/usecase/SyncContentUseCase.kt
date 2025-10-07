package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.*
import io.asterixorobelix.afrikaburn.domain.repository.SyncRepository
import io.asterixorobelix.afrikaburn.domain.repository.NetworkRepository
import io.asterixorobelix.afrikaburn.domain.repository.StorageRepository
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
    private val syncRepository: SyncRepository,
    private val networkRepository: NetworkRepository,
    private val storageRepository: StorageRepository
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
            if (!networkRepository.isConnected()) {
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
                if (!networkRepository.isConnected()) {
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
            syncRepository.updateLastSyncTime(Clock.System.now())
            
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
        val lastSync = syncRepository.getLastSyncTime()
        val syncInterval = syncRepository.getSyncInterval()
        val currentTime = Clock.System.now()
        
        // Check if enough time has passed since last sync
        val timeSinceLastSync = currentTime - (lastSync ?: currentTime)
        if (timeSinceLastSync >= syncInterval) {
            return true
        }
        
        // Check if any critical content is missing
        if (!syncRepository.hasContent(ContentType.SAFETY)) {
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
        syncRepository.cancelSync()
    }

    private suspend fun checkStorageStatus(): StorageStatus {
        val usedBytes = storageRepository.getUsedStorageBytes()
        val availableBytes = storageRepository.getAvailableStorageBytes()
        val totalBytes = usedBytes + availableBytes
        val usagePercentage = if (totalBytes > 0) {
            (usedBytes.toFloat() / totalBytes) * 100
        } else {
            0f
        }
        
        return StorageStatus(
            usedBytes = usedBytes,
            totalBytes = totalBytes,
            availableBytes = availableBytes,
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
        if (shouldSyncContent(ContentType.SAFETY, SAFETY_CONTENT_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = ContentType.SAFETY,
                priority = SyncPriority.CRITICAL,
                estimatedSizeBytes = SAFETY_CONTENT_SIZE * 1024 * 1024
            ))
            estimatedSize += SAFETY_CONTENT_SIZE * 1024 * 1024
        }

        // Add maps if space available
        if (shouldSyncContent(ContentType.MAPS, MAPS_CONTENT_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = ContentType.MAPS,
                priority = SyncPriority.HIGH,
                estimatedSizeBytes = MAPS_CONTENT_SIZE * 1024 * 1024
            ))
            estimatedSize += MAPS_CONTENT_SIZE * 1024 * 1024
        }

        // Add static content if space available
        if (shouldSyncContent(ContentType.STATIC, STATIC_CONTENT_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = ContentType.STATIC,
                priority = SyncPriority.MEDIUM,
                estimatedSizeBytes = STATIC_CONTENT_SIZE * 1024 * 1024
            ))
            estimatedSize += STATIC_CONTENT_SIZE * 1024 * 1024
        }

        // Add community content if space available
        if (shouldSyncContent(ContentType.COMMUNITY, COMMUNITY_CONTENT_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = ContentType.COMMUNITY,
                priority = SyncPriority.LOW,
                estimatedSizeBytes = COMMUNITY_CONTENT_SIZE * 1024 * 1024
            ))
            estimatedSize += COMMUNITY_CONTENT_SIZE * 1024 * 1024
        }

        // Add event schedule if space available
        if (shouldSyncContent(ContentType.EVENT_SCHEDULE, EVENT_SCHEDULE_SIZE * 1024 * 1024, availableForSync, estimatedSize)) {
            items.add(SyncPlanItem(
                contentType = ContentType.EVENT_SCHEDULE,
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
        contentType: ContentType,
        contentSize: Long,
        availableBytes: Long,
        currentPlanSize: Long
    ): Boolean {
        // Check if content needs update
        if (!syncRepository.needsUpdate(contentType)) {
            return false
        }
        
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
            ContentType.SAFETY -> syncRepository.syncSafetyContent(forceSync)
            ContentType.MAPS -> syncRepository.syncMaps(forceSync)
            ContentType.STATIC -> syncRepository.syncStaticContent(forceSync)
            ContentType.COMMUNITY -> syncRepository.syncCommunityContent(forceSync)
            ContentType.EVENT_SCHEDULE -> syncRepository.syncEventSchedule(forceSync)
        }
    }

    private suspend fun performStorageCleanup(storageStatus: StorageStatus) {
        // Clean up in reverse priority order
        val cleanupOrder = listOf(
            ContentType.EVENT_SCHEDULE,
            ContentType.COMMUNITY,
            ContentType.STATIC,
            ContentType.MAPS
            // Never clean up SAFETY content
        )
        
        for (contentType in cleanupOrder) {
            if (storageStatus.usagePercentage < STORAGE_WARNING_THRESHOLD) {
                break // Stop cleanup if we're below warning threshold
            }
            
            storageRepository.cleanupOldContent(contentType)
        }
        
        // Clean up cache and temporary files
        storageRepository.cleanupCache()
    }
}

/**
 * Represents the progress of a sync operation.
 */
sealed class SyncProgress {
    data class StorageStatus(val status: io.asterixorobelix.afrikaburn.domain.usecase.StorageStatus) : SyncProgress()
    data class SyncPlan(val plan: io.asterixorobelix.afrikaburn.domain.usecase.SyncPlan) : SyncProgress()
    data class Syncing(val contentType: ContentType, val progress: Float, val message: String) : SyncProgress()
    data class ItemComplete(val contentType: ContentType, val totalProgress: Float) : SyncProgress()
    data class ItemFailed(val contentType: ContentType, val error: String, val progress: Float) : SyncProgress()
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
    val contentType: ContentType,
    val priority: SyncPriority,
    val estimatedSizeBytes: Long
)

/**
 * Content types that can be synced.
 */
enum class ContentType(val displayName: String) {
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