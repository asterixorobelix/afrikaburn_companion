package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.ContentPackage
import io.asterixorobelix.afrikaburn.domain.model.SyncManager
import io.asterixorobelix.afrikaburn.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stub implementation of SyncRepository for development
 * This provides minimal functionality to allow compilation and basic testing
 */
class SyncRepositoryStub : SyncRepository {
    
    private val _syncManager = MutableStateFlow<SyncManager?>(null)
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    
    override suspend fun getSyncManager(): SyncManager? = _syncManager.value
    
    override suspend fun saveSyncManager(syncManager: SyncManager) {
        _syncManager.value = syncManager
    }
    
    override suspend fun updateSyncManager(syncManager: SyncManager) {
        _syncManager.value = syncManager
    }
    
    override fun observeSyncManager(): Flow<SyncManager?> = _syncManager.asStateFlow()
    
    override suspend fun performFullSync(eventId: String, maxStorageBytes: Long): Result<SyncResult> {
        return Result.success(SyncResult(
            success = true,
            syncedItems = 0,
            errorCount = 0,
            totalSizeBytes = 0,
            duration = 0,
            lastSyncTimestamp = System.currentTimeMillis()
        ))
    }
    
    override suspend fun performIncrementalSync(eventId: String, lastSyncTimestamp: Long): Result<SyncResult> {
        return Result.success(SyncResult(
            success = true,
            syncedItems = 0,
            errorCount = 0,
            totalSizeBytes = 0,
            duration = 0,
            lastSyncTimestamp = System.currentTimeMillis()
        ))
    }
    
    override suspend fun isFullSyncNeeded(): Boolean = false
    
    override suspend fun getEstimatedSyncSize(eventId: String): Long = 0
    
    override suspend fun getAllContentPackages(): List<ContentPackage> = emptyList()
    
    override suspend fun getContentPackagesByPriority(): List<ContentPackage> = emptyList()
    
    override suspend fun getDownloadedContentPackages(): List<ContentPackage> = emptyList()
    
    override suspend fun saveContentPackage(contentPackage: ContentPackage) {
        // No-op for stub
    }
    
    override suspend fun updateContentPackage(contentPackage: ContentPackage) {
        // No-op for stub
    }
    
    override suspend fun deleteContentPackage(packageId: String) {
        // No-op for stub
    }
    
    override suspend fun downloadContentPackage(packageId: String): Flow<io.asterixorobelix.afrikaburn.domain.repository.DownloadProgress> {
        return flowOf(io.asterixorobelix.afrikaburn.domain.repository.DownloadProgress(
            mapId = packageId,
            progress = 1.0f,
            downloadedBytes = 0,
            totalBytes = 0,
            status = io.asterixorobelix.afrikaburn.domain.repository.DownloadStatus.COMPLETED
        ))
    }
    
    override suspend fun cancelContentPackageDownload(packageId: String) {
        // No-op for stub
    }
    
    override suspend fun getSyncPriorities(): List<SyncPriority> = listOf(
        SyncPriority("safety", 1, 100_000_000L, true),
        SyncPriority("maps", 2, 200_000_000L, true),
        SyncPriority("static", 3, 500_000_000L, false),
        SyncPriority("community", 4, 300_000_000L, false),
        SyncPriority("events", 5, 400_000_000L, false)
    )
    
    override suspend fun updateSyncPriorities(priorities: List<SyncPriority>) {
        // No-op for stub
    }
    
    override suspend fun applyPriorityCleanup(targetSizeBytes: Long): Long = 0
    
    override suspend fun getCurrentStorageUsage(): StorageUsage = StorageUsage(
        totalUsedBytes = 0,
        totalLimitBytes = 2_000_000_000L,
        availableBytes = 2_000_000_000L,
        usagePercentage = 0f,
        categoryBreakdown = emptyMap()
    )
    
    override suspend fun wouldExceedStorageLimit(additionalBytes: Long): Boolean = false
    
    override suspend fun cleanupStorage(requiredBytes: Long): Long = 0
    
    override suspend fun getStorageUsageByCategory(): Map<String, Long> = emptyMap()
    
    override suspend fun isNetworkAvailable(): Boolean = true
    
    override suspend fun getNetworkType(): NetworkType = NetworkType.WIFI
    
    override suspend fun shouldSyncOnCurrentNetwork(): Boolean = true
    
    override suspend fun updateNetworkPreferences(wifiOnly: Boolean, cellularEnabled: Boolean) {
        // No-op for stub
    }
    
    override suspend fun getSyncStatus(): SyncStatus = _syncStatus.value
    
    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()
    
    override suspend fun getLastSyncError(): String? = null
    
    override suspend fun clearSyncError() {
        // No-op for stub
    }
    
    override suspend fun getSyncStatistics(): SyncStatistics = SyncStatistics(
        totalSyncs = 0,
        successfulSyncs = 0,
        failedSyncs = 0,
        lastSuccessfulSync = 0,
        lastFailedSync = 0,
        totalDataSynced = 0,
        averageSyncDuration = 0
    )
    
    override suspend fun resetSyncStatistics() {
        // No-op for stub
    }
}

