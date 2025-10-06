package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model for managing data synchronization with 2GB storage limits
 */
data class SyncManager(
    val id: String,
    val deviceId: String,
    val lastFullSync: Long?,
    val lastIncrementalSync: Long?,
    val totalStorageUsed: Long, // bytes
    val maxStorageLimit: Long = 2_147_483_648L, // 2GB
    val syncPriorities: List<SyncPriority> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val pendingSyncItems: List<String> = emptyList(),
    val failedSyncItems: List<String> = emptyList(),
    val errorMessage: String?,
    val networkType: NetworkType?,
    val batteryLevel: Int?, // percentage
    val isBackgroundSyncEnabled: Boolean = true,
    val lastUpdated: Long
) {
    companion object {
        const val MAX_STORAGE_BYTES = 2_147_483_648L // 2GB
        const val WARNING_THRESHOLD = 0.8 // 80% of storage
        const val CRITICAL_THRESHOLD = 0.95 // 95% of storage
    }
    
    fun isValid(): Boolean = id.isNotBlank() && deviceId.isNotBlank()
    fun getStorageUsagePercentage(): Double = totalStorageUsed.toDouble() / maxStorageLimit
    fun isStorageWarning(): Boolean = getStorageUsagePercentage() > WARNING_THRESHOLD
    fun isStorageCritical(): Boolean = getStorageUsagePercentage() > CRITICAL_THRESHOLD
    fun canSync(): Boolean = syncStatus != SyncStatus.SYNCING && !isStorageCritical()
    fun getRemainingStorage(): Long = maxStorageLimit - totalStorageUsed
}

enum class SyncStatus { IDLE, SYNCING, PAUSED, FAILED, COMPLETED }
enum class NetworkType { WIFI, CELLULAR, NONE }
enum class SyncPriority { CRITICAL, HIGH, NORMAL, LOW }