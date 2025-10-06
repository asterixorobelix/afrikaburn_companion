package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.SyncPriority
import io.asterixorobelix.afrikaburn.domain.model.SyncProgress
import io.asterixorobelix.afrikaburn.domain.model.SyncStatus
import io.asterixorobelix.afrikaburn.domain.repository.SyncRepository
import io.asterixorobelix.afrikaburn.data.local.SyncQueries
import io.asterixorobelix.afrikaburn.data.remote.SyncApi
import io.asterixorobelix.afrikaburn.data.storage.StoragePriorityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of SyncRepository with priority-based syncing
 * 
 * Handles intelligent data synchronization with 2GB storage limits,
 * priority-based content management, and offline-first architecture.
 */
class SyncRepositoryImpl(
    private val syncQueries: SyncQueries,
    private val syncApi: SyncApi,
    private val storagePriorityManager: StoragePriorityManager
) : SyncRepository {
    
    companion object {
        private const val MAX_STORAGE_BYTES = 2_000_000_000L // 2GB limit
        private const val SYNC_BATCH_SIZE = 50
        private const val HIGH_PRIORITY_THRESHOLD = 0.8 // 80% storage usage triggers cleanup
        private const val EMERGENCY_CLEANUP_THRESHOLD = 0.95 // 95% triggers emergency cleanup
        private const val MIN_AVAILABLE_STORAGE_RATIO = 0.1 // 10% minimum available storage
    }
    
    override suspend fun syncAllContent(
        eventId: String,
        forceRefresh: Boolean,
        priority: SyncPriority
    ): Flow<SyncProgress> = flow {
        try {
            emitInitialProgress(eventId)
            checkAndCleanupStorage(priority)
            
            val manifest = syncApi.getSyncManifest(eventId, forceRefresh)
            emitStartProgress(eventId, manifest.items.size)
            
            val completedItems = syncManifestItems(eventId, manifest.items, priority)
            updateLastSyncTimestamp(eventId, System.currentTimeMillis())
            emitCompletedProgress(eventId, completedItems, manifest.items.size)
            
        } catch (exception: Exception) {
            emitFailedProgress(eventId, exception)
        }
    }
    
    private suspend fun FlowCollector<SyncProgress>.emitInitialProgress(eventId: String) {
        emit(createSyncProgress(eventId, 0, 0, "Initializing sync...", SyncStatus.SYNCING))
    }
    
    private suspend fun FlowCollector<SyncProgress>.emitStartProgress(eventId: String, totalItems: Int) {
        emit(createSyncProgress(eventId, totalItems, 0, "Starting content sync...", SyncStatus.SYNCING))
    }
    
    private suspend fun FlowCollector<SyncProgress>.emitCompletedProgress(
        eventId: String, 
        completedItems: Int, 
        totalItems: Int
    ) {
        emit(createSyncProgress(eventId, totalItems, completedItems, "Sync completed successfully", SyncStatus.COMPLETED))
    }
    
    private suspend fun FlowCollector<SyncProgress>.emitFailedProgress(eventId: String, exception: Exception) {
        emit(createSyncProgress(eventId, 0, 0, "Sync failed: ${exception.message}", SyncStatus.FAILED))
    }
    
    private fun createSyncProgress(
        eventId: String,
        totalItems: Int,
        completedItems: Int,
        currentItem: String,
        status: SyncStatus
    ): SyncProgress {
        return SyncProgress(
            eventId = eventId,
            totalItems = totalItems,
            completedItems = completedItems,
            currentItem = currentItem,
            status = status,
            storageUsed = 0L, // Will be updated by caller
            storageLimit = MAX_STORAGE_BYTES
        )
    }
    
    override suspend fun syncIncrementalContent(
        eventId: String,
        lastSyncTimestamp: Long
    ): Flow<SyncProgress> = flow {
        try {
            emit(createSyncProgress(eventId, 0, 0, "Checking for updates...", SyncStatus.SYNCING))
            
            val changes = syncApi.getIncrementalChanges(eventId, lastSyncTimestamp)
            
            if (changes.isEmpty()) {
                emit(createSyncProgress(eventId, 0, 0, "No updates available", SyncStatus.COMPLETED))
                return@flow
            }
            
            processIncrementalChanges(eventId, changes)
            updateLastSyncTimestamp(eventId, System.currentTimeMillis())
            
        } catch (exception: Exception) {
            emit(createSyncProgress(eventId, 0, 0, "Incremental sync failed: ${exception.message}", SyncStatus.FAILED))
        }
    }
    
    override suspend fun getCurrentStorageUsage(): Long = withContext(Dispatchers.IO) {
        storagePriorityManager.getTotalStorageUsage()
    }
    
    override suspend fun getAvailableStorage(): Long = withContext(Dispatchers.IO) {
        MAX_STORAGE_BYTES - getCurrentStorageUsage()
    }
    
    override suspend fun getStorageUsageByCategory(): Map<String, Long> = withContext(Dispatchers.IO) {
        storagePriorityManager.getStorageUsageByCategory()
    }
    
    override suspend fun cleanupLowPriorityContent(targetBytes: Long): Long = withContext(Dispatchers.IO) {
        storagePriorityManager.cleanupLowPriorityContent(targetBytes)
    }
    
    override suspend fun getLastSyncTimestamp(eventId: String): Long = withContext(Dispatchers.IO) {
        syncQueries.selectLastSyncTimestamp(eventId).executeAsOneOrNull() ?: 0L
    }
    
    override suspend fun updateLastSyncTimestamp(eventId: String, timestamp: Long) = withContext(Dispatchers.IO) {
        syncQueries.insertOrUpdateSyncTimestamp(eventId, timestamp)
    }
    
    override suspend fun getSyncStatus(eventId: String): SyncStatus = withContext(Dispatchers.IO) {
        val statusString = syncQueries.selectSyncStatus(eventId).executeAsOneOrNull() ?: "NOT_SYNCED"
        SyncStatus.valueOf(statusString)
    }
    
    override suspend fun updateSyncStatus(eventId: String, status: SyncStatus) = withContext(Dispatchers.IO) {
        syncQueries.insertOrUpdateSyncStatus(eventId, status.name)
    }
    
    override suspend fun isSyncRequired(eventId: String, maxAgeHours: Int): Boolean = withContext(Dispatchers.IO) {
        val lastSync = getLastSyncTimestamp(eventId)
        val maxAgeMs = maxAgeHours * 60 * 60 * 1000L
        (System.currentTimeMillis() - lastSync) > maxAgeMs
    }
    
    override suspend fun estimateSyncSize(eventId: String): Long = withContext(Dispatchers.IO) {
        try {
            val manifest = syncApi.getSyncManifest(eventId, false)
            manifest.items.sumOf { it.sizeBytes }
        } catch (exception: Exception) {
            // Return estimate based on typical content size
            100_000_000L // 100MB estimate
        }
    }
    
    override suspend fun pauseSync(eventId: String) = withContext(Dispatchers.IO) {
        updateSyncStatus(eventId, SyncStatus.PAUSED)
    }
    
    override suspend fun resumeSync(eventId: String) = withContext(Dispatchers.IO) {
        updateSyncStatus(eventId, SyncStatus.SYNCING)
    }
    
    override suspend fun cancelSync(eventId: String) = withContext(Dispatchers.IO) {
        updateSyncStatus(eventId, SyncStatus.CANCELLED)
    }
    
    /**
     * Check storage and perform cleanup if needed
     */
    private suspend fun checkAndCleanupStorage(priority: SyncPriority) {
        val availableStorage = getAvailableStorage()
        val minAvailableThreshold = (MAX_STORAGE_BYTES * MIN_AVAILABLE_STORAGE_RATIO).toLong()
        if (availableStorage < minAvailableThreshold) {
            performStorageCleanup(priority)
        }
    }
    
    /**
     * Sync all items in manifest and return completed count
     */
    private suspend fun FlowCollector<SyncProgress>.syncManifestItems(
        eventId: String,
        items: List<SyncManifestItem>,
        priority: SyncPriority
    ): Int {
        var completedItems = 0
        val sortedItems = items.sortedByDescending { it.priority.ordinal }
        
        for (item in sortedItems) {
            if (canSyncItem(item, priority)) {
                emit(createSyncProgress(eventId, items.size, completedItems, "Syncing ${item.type}: ${item.name}", SyncStatus.SYNCING))
                syncItem(item)
                completedItems++
                emit(createSyncProgress(eventId, items.size, completedItems, "Completed ${item.type}: ${item.name}", SyncStatus.SYNCING))
            }
        }
        return completedItems
    }
    
    /**
     * Process incremental changes and emit progress
     */
    private suspend fun FlowCollector<SyncProgress>.processIncrementalChanges(
        eventId: String,
        changes: List<IncrementalChange>
    ) {
        val totalItems = changes.size
        var completedItems = 0
        
        emit(createSyncProgress(eventId, totalItems, 0, "Applying $totalItems updates...", SyncStatus.SYNCING))
        
        for (change in changes) {
            applyIncrementalChange(change)
            completedItems++
            emit(createSyncProgress(eventId, totalItems, completedItems, "Applied update: ${change.operation}", SyncStatus.SYNCING))
        }
        
        emit(createSyncProgress(eventId, totalItems, completedItems, "Incremental sync completed", SyncStatus.COMPLETED))
    }
    
    /**
     * Check if item can be synced based on storage constraints
     */
    private suspend fun canSyncItem(item: SyncManifestItem, priority: SyncPriority): Boolean {
        if (item.sizeBytes <= getAvailableStorage()) {
            return true
        }
        return freeUpSpace(item.sizeBytes, priority)
    }

    /**
     * Perform storage cleanup when approaching limits
     */
    private suspend fun performStorageCleanup(currentPriority: SyncPriority) {
        val currentUsage = getCurrentStorageUsage()
        val usageRatio = currentUsage.toDouble() / MAX_STORAGE_BYTES
        
        when {
            usageRatio > EMERGENCY_CLEANUP_THRESHOLD -> {
                // Emergency cleanup - remove low and medium priority content
                val targetCleanup = (MAX_STORAGE_BYTES * 0.3).toLong() // Free up 30%
                cleanupLowPriorityContent(targetCleanup)
            }
            usageRatio > HIGH_PRIORITY_THRESHOLD -> {
                // High usage - remove only low priority content
                val targetCleanup = (MAX_STORAGE_BYTES * 0.2).toLong() // Free up 20%
                cleanupLowPriorityContent(targetCleanup)
            }
        }
    }
    
    /**
     * Try to free up space for a specific item
     */
    private suspend fun freeUpSpace(requiredBytes: Long, currentPriority: SyncPriority): Boolean {
        val availableSpace = getAvailableStorage()
        if (availableSpace >= requiredBytes) return true
        
        val bytesToFree = requiredBytes - availableSpace + (requiredBytes * 0.1).toLong() // Add 10% buffer
        val freedBytes = cleanupLowPriorityContent(bytesToFree)
        
        return freedBytes >= bytesToFree
    }
    
    /**
     * Sync a single item based on its type
     */
    private suspend fun syncItem(item: SyncManifestItem) {
        when (item.type) {
            "event" -> syncEventData(item)
            "theme_camp" -> syncThemeCampData(item)
            "art_installation" -> syncArtInstallationData(item)
            "map" -> syncMapData(item)
            "emergency_contact" -> syncEmergencyContactData(item)
            "resource_location" -> syncResourceLocationData(item)
            else -> {
                // Log unknown item type
            }
        }
    }
    
    /**
     * Apply an incremental change
     */
    private suspend fun applyIncrementalChange(change: IncrementalChange) {
        when (change.operation) {
            "CREATE", "UPDATE" -> {
                // Apply create or update operation
                syncItem(change.item)
            }
            "DELETE" -> {
                // Apply delete operation
                deleteItem(change.item)
            }
        }
    }
    
    /**
     * Sync methods for different content types
     */
    private suspend fun syncEventData(item: SyncManifestItem) {
        // Implementation would delegate to EventRepository
    }
    
    private suspend fun syncThemeCampData(item: SyncManifestItem) {
        // Implementation would delegate to ThemeCampRepository
    }
    
    private suspend fun syncArtInstallationData(item: SyncManifestItem) {
        // Implementation would delegate to ArtInstallationRepository
    }
    
    private suspend fun syncMapData(item: SyncManifestItem) {
        // Implementation would delegate to MapRepository
    }
    
    private suspend fun syncEmergencyContactData(item: SyncManifestItem) {
        // Implementation would delegate to EmergencyRepository
    }
    
    private suspend fun syncResourceLocationData(item: SyncManifestItem) {
        // Implementation would delegate to ResourceRepository
    }
    
    /**
     * Delete an item based on its type
     */
    private suspend fun deleteItem(item: SyncManifestItem) {
        // Implementation would delegate to appropriate repository for deletion
    }
}

/**
 * Data classes for sync manifest and changes
 */
data class SyncManifest(
    val eventId: String,
    val items: List<SyncManifestItem>,
    val timestamp: Long
)

data class SyncManifestItem(
    val id: String,
    val type: String,
    val name: String,
    val sizeBytes: Long,
    val priority: SyncPriority,
    val checksum: String,
    val lastModified: Long
)

data class IncrementalChange(
    val operation: String, // CREATE, UPDATE, DELETE
    val item: SyncManifestItem,
    val timestamp: Long
)