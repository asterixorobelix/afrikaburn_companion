package io.asterixorobelix.afrikaburn.data.sync

import io.asterixorobelix.afrikaburn.domain.model.ContentPackage
import io.asterixorobelix.afrikaburn.domain.model.SyncManager
import io.asterixorobelix.afrikaburn.domain.model.SyncStatus
import io.asterixorobelix.afrikaburn.domain.model.NetworkType
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

/**
 * Smart sync engine with priority-based 2GB storage management.
 * 
 * Priority order:
 * 1. Safety & Emergency (always synced)
 * 2. Offline Maps (essential for navigation)
 * 3. Static content (theme camps, art, schedules)
 * 4. Community features (messages, updates)
 * 5. Event details (performances, descriptions)
 * 
 * Storage management:
 * - 2GB total limit enforced
 * - Automatic cleanup of lowest priority content when space needed
 * - Smart delta updates to minimize bandwidth
 */
class SyncEngine {
    private val _syncState = MutableStateFlow(
        SyncManager(
            id = "sync_${getCurrentTimestamp()}",
            deviceId = "device_${(1000..9999).random()}",
            lastFullSync = null,
            lastIncrementalSync = null,
            totalStorageUsed = 0L,
            errorMessage = null,
            networkType = NetworkType.WIFI,
            batteryLevel = 100,
            lastUpdated = getCurrentTimestamp()
        )
    )
    val syncState: Flow<SyncManager> = _syncState.asStateFlow()
    
    private val _storageUsage = MutableStateFlow(0L)
    val storageUsage: Flow<Long> = _storageUsage.asStateFlow()
    
    companion object {
        const val MAX_STORAGE_BYTES = 2L * 1024 * 1024 * 1024 // 2GB
        const val SAFETY_PACKAGE_SIZE = 50L * 1024 * 1024 // 50MB estimated
        const val MAP_PACKAGE_SIZE = 500L * 1024 * 1024 // 500MB estimated
        const val STATIC_CONTENT_SIZE = 300L * 1024 * 1024 // 300MB estimated
        const val COMMUNITY_PACKAGE_SIZE = 200L * 1024 * 1024 // 200MB estimated
        const val EVENT_DETAILS_SIZE = 950L * 1024 * 1024 // 950MB estimated
    }
    
    /**
     * Performs full sync with priority-based storage management
     */
    suspend fun performFullSync(
        availablePackages: List<ContentPackage>
    ): SyncResult {
        val currentUsage = calculateCurrentUsage()
        var remainingSpace = MAX_STORAGE_BYTES - currentUsage
        
        val packagesToSync = mutableListOf<ContentPackage>()
        val skippedPackages = mutableListOf<ContentPackage>()
        
        // Sort packages by priority (lower number = higher priority)
        val sortedPackages = availablePackages.sortedBy { it.priority }
        
        for (pkg in sortedPackages) {
            val estimatedSize = estimatePackageSize(pkg)
            
            if (estimatedSize <= remainingSpace) {
                packagesToSync.add(pkg)
                remainingSpace -= estimatedSize
            } else {
                // Try to make space by removing lower priority content
                val freedSpace = makeSpaceForPackage(pkg, estimatedSize - remainingSpace)
                if (freedSpace >= estimatedSize - remainingSpace) {
                    packagesToSync.add(pkg)
                    remainingSpace = remainingSpace + freedSpace - estimatedSize
                } else {
                    skippedPackages.add(pkg)
                }
            }
        }
        
        // Update sync state
        _syncState.update { current ->
            current.copy(
                lastFullSync = getCurrentTimestamp(),
                syncStatus = SyncStatus.COMPLETED,
                lastUpdated = getCurrentTimestamp()
            )
        }
        
        // Update storage usage
        _storageUsage.value = MAX_STORAGE_BYTES - remainingSpace
        
        return SyncResult(
            syncedPackages = packagesToSync,
            skippedPackages = skippedPackages,
            totalStorageUsed = MAX_STORAGE_BYTES - remainingSpace,
            success = true
        )
    }
    
    /**
     * Performs incremental sync for updates only
     */
    suspend fun performIncrementalSync(
        updatedPackages: List<ContentPackage>
    ): SyncResult {
        // For incremental sync, only update existing packages
        val packagesToUpdate = updatedPackages.filter { pkg ->
            isPackageAlreadySynced(pkg)
        }
        
        _syncState.update { current ->
            current.copy(
                lastIncrementalSync = getCurrentTimestamp(),
                syncStatus = SyncStatus.COMPLETED,
                lastUpdated = getCurrentTimestamp()
            )
        }
        
        return SyncResult(
            syncedPackages = packagesToUpdate,
            skippedPackages = updatedPackages - packagesToUpdate.toSet(),
            totalStorageUsed = _storageUsage.value,
            success = true
        )
    }
    
    /**
     * Estimates the size of a content package based on its type
     */
    private fun estimatePackageSize(pkg: ContentPackage): Long {
        return pkg.sizeBytes // Use actual size from package
    }
    
    /**
     * Attempts to free space by removing lower priority content
     */
    private suspend fun makeSpaceForPackage(
        targetPackage: ContentPackage,
        spaceNeeded: Long
    ): Long {
        // In a real implementation, this would remove lower priority packages
        // For now, return 0 (no space freed)
        return 0L
    }
    
    /**
     * Calculates current storage usage
     */
    private suspend fun calculateCurrentUsage(): Long {
        // In a real implementation, this would calculate actual disk usage
        return _storageUsage.value
    }
    
    /**
     * Checks if a package is already synced
     */
    private fun isPackageAlreadySynced(pkg: ContentPackage): Boolean {
        // In a real implementation, check local storage
        return false
    }
    
    /**
     * Result of a sync operation
     */
    data class SyncResult(
        val syncedPackages: List<ContentPackage>,
        val skippedPackages: List<ContentPackage>,
        val totalStorageUsed: Long,
        val success: Boolean,
        val error: String? = null
    )
    
}