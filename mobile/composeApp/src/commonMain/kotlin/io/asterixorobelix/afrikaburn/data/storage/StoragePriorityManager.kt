package io.asterixorobelix.afrikaburn.data.storage

import io.asterixorobelix.afrikaburn.domain.model.ContentPackage
import io.asterixorobelix.afrikaburn.domain.model.PackagePriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages storage priorities for offline content within 2GB limit.
 * 
 * Priority levels (1 = highest):
 * 1. Safety & Emergency Content (50MB) - Always kept
 * 2. Offline Maps (500MB) - Essential for navigation
 * 3. Static Content (300MB) - Theme camps, art, basic info
 * 4. Community Features (200MB) - Messages, updates
 * 5. Event Details (950MB) - Performances, detailed descriptions
 * 
 * Automatic cleanup when space needed:
 * - Removes lowest priority content first
 * - Keeps partial packages when possible
 * - Never removes safety content
 */
class StoragePriorityManager {
    private val _storageState = MutableStateFlow(StorageState())
    val storageState: Flow<StorageState> = _storageState.asStateFlow()
    
    companion object {
        const val MAX_STORAGE_BYTES = 2L * 1024 * 1024 * 1024 // 2GB
        const val CLEANUP_THRESHOLD = 0.9f // Start cleanup at 90% full
        const val TARGET_FREE_SPACE = 0.2f // Try to keep 20% free
    }
    
    /**
     * Allocates space for content, removing lower priority items if needed
     */
    suspend fun allocateSpace(
        requestedPackage: ContentPackage,
        requestedSize: Long
    ): AllocationResult {
        val currentState = _storageState.value
        val availableSpace = MAX_STORAGE_BYTES - currentState.usedBytes
        
        // If enough space, allocate immediately
        if (requestedSize <= availableSpace) {
            updateStorageState(
                currentState.copy(
                    usedBytes = currentState.usedBytes + requestedSize,
                    packages = currentState.packages + PackageInfo(
                        packageId = requestedPackage.id,
                        priority = requestedPackage.priority,
                        sizeBytes = requestedSize
                    )
                )
            )
            return AllocationResult.Success
        }
        
        // Try to free space
        val spaceNeeded = requestedSize - availableSpace
        val packagesToRemove = selectPackagesToRemove(
            currentState.packages,
            spaceNeeded,
            requestedPackage.priority
        )
        
        if (packagesToRemove.isEmpty()) {
            return AllocationResult.InsufficientSpace(
                "Cannot free enough space for ${requestedPackage.name}"
            )
        }
        
        // Remove selected packages
        val freedSpace = packagesToRemove.sumOf { it.sizeBytes }
        val remainingPackages = currentState.packages - packagesToRemove.toSet()
        
        updateStorageState(
            currentState.copy(
                usedBytes = currentState.usedBytes - freedSpace + requestedSize,
                packages = remainingPackages + PackageInfo(
                    packageId = requestedPackage.id,
                    priority = requestedPackage.priority,
                    sizeBytes = requestedSize
                )
            )
        )
        
        return AllocationResult.SuccessWithCleanup(
            removedPackages = packagesToRemove.map { it.packageId }
        )
    }
    
    /**
     * Performs routine cleanup to maintain free space
     */
    suspend fun performCleanup() {
        val currentState = _storageState.value
        val usageRatio = currentState.usedBytes.toFloat() / MAX_STORAGE_BYTES
        
        if (usageRatio < CLEANUP_THRESHOLD) return
        
        val targetBytes = (MAX_STORAGE_BYTES * (1 - TARGET_FREE_SPACE)).toLong()
        val bytesToFree = currentState.usedBytes - targetBytes
        
        val packagesToRemove = selectPackagesToRemove(
            currentState.packages,
            bytesToFree,
            minPriorityToKeep = PackagePriority.NORMAL
        )
        
        if (packagesToRemove.isNotEmpty()) {
            val freedSpace = packagesToRemove.sumOf { it.sizeBytes }
            updateStorageState(
                currentState.copy(
                    usedBytes = currentState.usedBytes - freedSpace,
                    packages = currentState.packages - packagesToRemove.toSet()
                )
            )
        }
    }
    
    /**
     * Selects packages to remove based on priority
     */
    private fun selectPackagesToRemove(
        packages: List<PackageInfo>,
        spaceNeeded: Long,
        minPriorityToKeep: PackagePriority
    ): List<PackageInfo> {
        val removablePackages = packages
            .filter { it.priority.ordinal > minPriorityToKeep.ordinal }
            .sortedByDescending { it.priority.ordinal } // Lowest priority first
        
        val packagesToRemove = mutableListOf<PackageInfo>()
        var freedSpace = 0L
        
        for (pkg in removablePackages) {
            if (freedSpace >= spaceNeeded) break
            packagesToRemove.add(pkg)
            freedSpace += pkg.sizeBytes
        }
        
        return packagesToRemove
    }
    
    /**
     * Updates the storage state
     */
    private fun updateStorageState(newState: StorageState) {
        _storageState.value = newState
    }
    
    /**
     * Gets current storage usage statistics
     */
    fun getStorageStats(): StorageStats {
        val state = _storageState.value
        return StorageStats(
            totalBytes = MAX_STORAGE_BYTES,
            usedBytes = state.usedBytes,
            freeBytes = MAX_STORAGE_BYTES - state.usedBytes,
            usagePercentage = (state.usedBytes.toFloat() / MAX_STORAGE_BYTES * 100).toInt(),
            packageCount = state.packages.size,
            packagesByPriority = state.packages.groupBy { it.priority }
                .mapValues { (_, pkgs) -> pkgs.sumOf { it.sizeBytes } }
        )
    }
    
    /**
     * Current storage state
     */
    data class StorageState(
        val usedBytes: Long = 0,
        val packages: List<PackageInfo> = emptyList()
    )
    
    /**
     * Information about a stored package
     */
    data class PackageInfo(
        val packageId: String,
        val priority: PackagePriority,
        val sizeBytes: Long
    )
    
    /**
     * Result of space allocation attempt
     */
    sealed interface AllocationResult {
        data object Success : AllocationResult
        data class SuccessWithCleanup(
            val removedPackages: List<String>
        ) : AllocationResult
        data class InsufficientSpace(
            val reason: String
        ) : AllocationResult
    }
    
    /**
     * Storage usage statistics
     */
    data class StorageStats(
        val totalBytes: Long,
        val usedBytes: Long,
        val freeBytes: Long,
        val usagePercentage: Int,
        val packageCount: Int,
        val packagesByPriority: Map<PackagePriority, Long>
    )
}