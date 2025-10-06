package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.SyncPriority
import io.asterixorobelix.afrikaburn.domain.model.SyncStatus
import io.asterixorobelix.afrikaburn.data.local.SyncQueries
import io.asterixorobelix.afrikaburn.data.remote.SyncApi
import io.asterixorobelix.afrikaburn.data.storage.StoragePriorityManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SyncRepositoryImplTest {
    
    private val mockSyncQueries = MockSyncQueries()
    private val mockSyncApi = MockSyncApi()
    private val mockStorageManager = MockStoragePriorityManager()
    
    private val repository = SyncRepositoryImpl(
        syncQueries = mockSyncQueries,
        syncApi = mockSyncApi,
        storagePriorityManager = mockStorageManager
    )
    
    @Test
    fun `syncAllContent should emit progress updates correctly`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val manifest = SyncManifest(
            eventId = eventId,
            items = listOf(
                SyncManifestItem(
                    id = "item1",
                    type = "event",
                    name = "Event Data",
                    sizeBytes = 1_000_000L, // 1MB
                    priority = SyncPriority.HIGH,
                    checksum = "abc123",
                    lastModified = System.currentTimeMillis()
                ),
                SyncManifestItem(
                    id = "item2",
                    type = "theme_camp",
                    name = "Theme Camps",
                    sizeBytes = 5_000_000L, // 5MB
                    priority = SyncPriority.MEDIUM,
                    checksum = "def456",
                    lastModified = System.currentTimeMillis()
                )
            ),
            timestamp = System.currentTimeMillis()
        )
        mockSyncApi.manifest = manifest
        mockStorageManager.currentUsage = 500_000_000L // 500MB used
        
        // When
        val progressList = repository.syncAllContent(
            eventId = eventId,
            forceRefresh = false,
            priority = SyncPriority.HIGH
        ).toList()
        
        // Then
        assertTrue(progressList.size >= 4, "Should emit at least 4 progress updates")
        
        // Check initial progress
        val initialProgress = progressList.first()
        assertEquals(SyncStatus.SYNCING, initialProgress.status)
        assertEquals(eventId, initialProgress.eventId)
        assertEquals("Initializing sync...", initialProgress.currentItem)
        
        // Check final progress
        val finalProgress = progressList.last()
        assertEquals(SyncStatus.COMPLETED, finalProgress.status)
        assertEquals(2, finalProgress.totalItems)
        assertEquals(2, finalProgress.completedItems)
        assertTrue(finalProgress.currentItem.contains("completed"))
    }
    
    @Test
    fun `syncAllContent should handle storage limits correctly`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val largeItem = SyncManifestItem(
            id = "large-item",
            type = "map",
            name = "Large Map",
            sizeBytes = 1_800_000_000L, // 1.8GB - close to limit
            priority = SyncPriority.HIGH,
            checksum = "large123",
            lastModified = System.currentTimeMillis()
        )
        val manifest = SyncManifest(
            eventId = eventId,
            items = listOf(largeItem),
            timestamp = System.currentTimeMillis()
        )
        mockSyncApi.manifest = manifest
        mockStorageManager.currentUsage = 100_000_000L // 100MB used initially
        
        // When
        val progressList = repository.syncAllContent(
            eventId = eventId,
            forceRefresh = false,
            priority = SyncPriority.HIGH
        ).toList()
        
        // Then
        val finalProgress = progressList.last()
        assertEquals(SyncStatus.COMPLETED, finalProgress.status)
        assertTrue(mockStorageManager.cleanupCalled, "Storage cleanup should be called for large items")
    }
    
    @Test
    fun `syncAllContent should skip items when insufficient storage`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val hugeItem = SyncManifestItem(
            id = "huge-item",
            type = "map",
            name = "Huge Map",
            sizeBytes = 3_000_000_000L, // 3GB - exceeds total limit
            priority = SyncPriority.LOW,
            checksum = "huge123",
            lastModified = System.currentTimeMillis()
        )
        val manifest = SyncManifest(
            eventId = eventId,
            items = listOf(hugeItem),
            timestamp = System.currentTimeMillis()
        )
        mockSyncApi.manifest = manifest
        mockStorageManager.currentUsage = 500_000_000L // 500MB used
        mockStorageManager.canFreeUpSpace = false // Cannot free enough space
        
        // When
        val progressList = repository.syncAllContent(
            eventId = eventId,
            forceRefresh = false,
            priority = SyncPriority.HIGH
        ).toList()
        
        // Then
        val finalProgress = progressList.last()
        assertEquals(SyncStatus.COMPLETED, finalProgress.status)
        assertEquals(0, finalProgress.completedItems) // Item should be skipped
        assertTrue(mockStorageManager.cleanupCalled, "Should attempt cleanup")
    }
    
    @Test
    fun `syncIncrementalContent should handle no updates correctly`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val lastSyncTimestamp = System.currentTimeMillis() - 3600000L // 1 hour ago
        mockSyncApi.incrementalChanges = emptyList() // No changes
        
        // When
        val progressList = repository.syncIncrementalContent(
            eventId = eventId,
            lastSyncTimestamp = lastSyncTimestamp
        ).toList()
        
        // Then
        assertTrue(progressList.size >= 2, "Should emit at least 2 progress updates")
        
        val finalProgress = progressList.last()
        assertEquals(SyncStatus.COMPLETED, finalProgress.status)
        assertEquals("No updates available", finalProgress.currentItem)
        assertEquals(0, finalProgress.totalItems)
    }
    
    @Test
    fun `syncIncrementalContent should apply updates correctly`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val lastSyncTimestamp = System.currentTimeMillis() - 3600000L
        val changes = listOf(
            IncrementalChange(
                operation = "UPDATE",
                item = SyncManifestItem(
                    id = "updated-item",
                    type = "event",
                    name = "Updated Event",
                    sizeBytes = 100_000L,
                    priority = SyncPriority.HIGH,
                    checksum = "updated123",
                    lastModified = System.currentTimeMillis()
                ),
                timestamp = System.currentTimeMillis()
            ),
            IncrementalChange(
                operation = "DELETE",
                item = SyncManifestItem(
                    id = "deleted-item",
                    type = "theme_camp",
                    name = "Deleted Camp",
                    sizeBytes = 50_000L,
                    priority = SyncPriority.LOW,
                    checksum = "deleted123",
                    lastModified = System.currentTimeMillis()
                ),
                timestamp = System.currentTimeMillis()
            )
        )
        mockSyncApi.incrementalChanges = changes
        
        // When
        val progressList = repository.syncIncrementalContent(
            eventId = eventId,
            lastSyncTimestamp = lastSyncTimestamp
        ).toList()
        
        // Then
        val finalProgress = progressList.last()
        assertEquals(SyncStatus.COMPLETED, finalProgress.status)
        assertEquals(2, finalProgress.totalItems)
        assertEquals(2, finalProgress.completedItems)
        assertTrue(finalProgress.currentItem.contains("completed"))
    }
    
    @Test
    fun `getCurrentStorageUsage should return correct value`() = runTest {
        // Given
        val expectedUsage = 750_000_000L // 750MB
        mockStorageManager.currentUsage = expectedUsage
        
        // When
        val actualUsage = repository.getCurrentStorageUsage()
        
        // Then
        assertEquals(expectedUsage, actualUsage)
    }
    
    @Test
    fun `getAvailableStorage should calculate correctly`() = runTest {
        // Given
        val usedStorage = 800_000_000L // 800MB
        val expectedAvailable = 2_000_000_000L - usedStorage // 2GB - 800MB = 1.2GB
        mockStorageManager.currentUsage = usedStorage
        
        // When
        val availableStorage = repository.getAvailableStorage()
        
        // Then
        assertEquals(expectedAvailable, availableStorage)
    }
    
    @Test
    fun `getStorageUsageByCategory should return breakdown`() = runTest {
        // Given
        val expectedBreakdown = mapOf(
            "events" to 100_000_000L,
            "theme_camps" to 200_000_000L,
            "art_installations" to 300_000_000L,
            "maps" to 400_000_000L
        )
        mockStorageManager.storageBreakdown = expectedBreakdown
        
        // When
        val actualBreakdown = repository.getStorageUsageByCategory()
        
        // Then
        assertEquals(expectedBreakdown, actualBreakdown)
    }
    
    @Test
    fun `cleanupLowPriorityContent should delegate to storage manager`() = runTest {
        // Given
        val targetBytes = 500_000_000L
        val expectedFreed = 300_000_000L
        mockStorageManager.freedBytes = expectedFreed
        
        // When
        val actualFreed = repository.cleanupLowPriorityContent(targetBytes)
        
        // Then
        assertEquals(expectedFreed, actualFreed)
        assertTrue(mockStorageManager.cleanupCalled)
        assertEquals(targetBytes, mockStorageManager.lastCleanupTarget)
    }
    
    @Test
    fun `sync timestamps should be managed correctly`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val timestamp = System.currentTimeMillis()
        
        // When - Update timestamp
        repository.updateLastSyncTimestamp(eventId, timestamp)
        
        // Then - Should be stored
        assertEquals(timestamp, mockSyncQueries.syncTimestamps[eventId])
        
        // When - Get timestamp
        val retrievedTimestamp = repository.getLastSyncTimestamp(eventId)
        
        // Then - Should match
        assertEquals(timestamp, retrievedTimestamp)
    }
    
    @Test
    fun `sync status should be managed correctly`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val status = SyncStatus.SYNCING
        
        // When - Update status
        repository.updateSyncStatus(eventId, status)
        
        // Then - Should be stored
        assertEquals(status.name, mockSyncQueries.syncStatuses[eventId])
        
        // When - Get status
        val retrievedStatus = repository.getSyncStatus(eventId)
        
        // Then - Should match
        assertEquals(status, retrievedStatus)
    }
    
    @Test
    fun `isSyncRequired should check age correctly`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val oldTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L) // 25 hours ago
        val recentTimestamp = System.currentTimeMillis() - (1 * 60 * 60 * 1000L) // 1 hour ago
        
        // When - Old sync
        mockSyncQueries.syncTimestamps[eventId] = oldTimestamp
        val shouldSyncOld = repository.isSyncRequired(eventId, maxAgeHours = 24)
        
        // When - Recent sync
        mockSyncQueries.syncTimestamps[eventId] = recentTimestamp
        val shouldSyncRecent = repository.isSyncRequired(eventId, maxAgeHours = 24)
        
        // Then
        assertTrue(shouldSyncOld, "Should require sync for old data")
        assertFalse(shouldSyncRecent, "Should not require sync for recent data")
    }
    
    @Test
    fun `estimateSyncSize should return manifest size`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val items = listOf(
            SyncManifestItem("1", "event", "Event", 1_000_000L, SyncPriority.HIGH, "abc", 0L),
            SyncManifestItem("2", "camp", "Camp", 2_000_000L, SyncPriority.MEDIUM, "def", 0L),
            SyncManifestItem("3", "art", "Art", 3_000_000L, SyncPriority.LOW, "ghi", 0L)
        )
        val manifest = SyncManifest(eventId, items, System.currentTimeMillis())
        mockSyncApi.manifest = manifest
        
        // When
        val estimatedSize = repository.estimateSyncSize(eventId)
        
        // Then
        assertEquals(6_000_000L, estimatedSize) // Sum of all item sizes
    }
    
    @Test
    fun `estimateSyncSize should return default on error`() = runTest {
        // Given
        val eventId = "test-event-2025"
        mockSyncApi.shouldThrowError = true
        
        // When
        val estimatedSize = repository.estimateSyncSize(eventId)
        
        // Then
        assertEquals(100_000_000L, estimatedSize) // Default estimate
    }
    
    @Test
    fun `pauseSync should update status to paused`() = runTest {
        // Given
        val eventId = "test-event-2025"
        
        // When
        repository.pauseSync(eventId)
        
        // Then
        assertEquals(SyncStatus.PAUSED.name, mockSyncQueries.syncStatuses[eventId])
    }
    
    @Test
    fun `resumeSync should update status to syncing`() = runTest {
        // Given
        val eventId = "test-event-2025"
        
        // When
        repository.resumeSync(eventId)
        
        // Then
        assertEquals(SyncStatus.SYNCING.name, mockSyncQueries.syncStatuses[eventId])
    }
    
    @Test
    fun `cancelSync should update status to cancelled`() = runTest {
        // Given
        val eventId = "test-event-2025"
        
        // When
        repository.cancelSync(eventId)
        
        // Then
        assertEquals(SyncStatus.CANCELLED.name, mockSyncQueries.syncStatuses[eventId])
    }
    
    @Test
    fun `syncAllContent should handle API errors gracefully`() = runTest {
        // Given
        val eventId = "test-event-2025"
        mockSyncApi.shouldThrowError = true
        
        // When
        val progressList = repository.syncAllContent(
            eventId = eventId,
            forceRefresh = false,
            priority = SyncPriority.HIGH
        ).toList()
        
        // Then
        val finalProgress = progressList.last()
        assertEquals(SyncStatus.FAILED, finalProgress.status)
        assertTrue(finalProgress.currentItem.contains("failed"))
    }
    
    @Test
    fun `syncIncrementalContent should handle API errors gracefully`() = runTest {
        // Given
        val eventId = "test-event-2025"
        val lastSyncTimestamp = System.currentTimeMillis() - 3600000L
        mockSyncApi.shouldThrowError = true
        
        // When
        val progressList = repository.syncIncrementalContent(
            eventId = eventId,
            lastSyncTimestamp = lastSyncTimestamp
        ).toList()
        
        // Then
        val finalProgress = progressList.last()
        assertEquals(SyncStatus.FAILED, finalProgress.status)
        assertTrue(finalProgress.currentItem.contains("failed"))
    }
}

/**
 * Mock implementations for testing
 */
class MockSyncQueries : SyncQueries {
    val syncTimestamps = mutableMapOf<String, Long>()
    val syncStatuses = mutableMapOf<String, String>()
    
    override fun selectLastSyncTimestamp(eventId: String): MockQuery<Long> {
        return MockQuery(syncTimestamps[eventId])
    }
    
    override fun insertOrUpdateSyncTimestamp(eventId: String, timestamp: Long) {
        syncTimestamps[eventId] = timestamp
    }
    
    override fun selectSyncStatus(eventId: String): MockQuery<String> {
        return MockQuery(syncStatuses[eventId])
    }
    
    override fun insertOrUpdateSyncStatus(eventId: String, status: String) {
        syncStatuses[eventId] = status
    }
}

class MockSyncApi : SyncApi {
    var manifest: SyncManifest? = null
    var incrementalChanges: List<IncrementalChange> = emptyList()
    var shouldThrowError = false
    
    override suspend fun getSyncManifest(eventId: String, forceRefresh: Boolean): SyncManifest {
        if (shouldThrowError) throw RuntimeException("API Error")
        return manifest ?: SyncManifest(eventId, emptyList(), System.currentTimeMillis())
    }
    
    override suspend fun getIncrementalChanges(eventId: String, since: Long): List<IncrementalChange> {
        if (shouldThrowError) throw RuntimeException("API Error")
        return incrementalChanges
    }
}

class MockStoragePriorityManager : StoragePriorityManager {
    var currentUsage = 0L
    var storageBreakdown = mapOf<String, Long>()
    var freedBytes = 0L
    var cleanupCalled = false
    var lastCleanupTarget = 0L
    var canFreeUpSpace = true
    
    override suspend fun getTotalStorageUsage(): Long = currentUsage
    
    override suspend fun getStorageUsageByCategory(): Map<String, Long> = storageBreakdown
    
    override suspend fun cleanupLowPriorityContent(targetBytes: Long): Long {
        cleanupCalled = true
        lastCleanupTarget = targetBytes
        return if (canFreeUpSpace) freedBytes else 0L
    }
}

class MockQuery<T>(private val value: T?) {
    fun executeAsOneOrNull(): T? = value
}

/**
 * Mock interfaces for testing (these would be defined elsewhere in a real implementation)
 */
interface SyncQueries {
    fun selectLastSyncTimestamp(eventId: String): MockQuery<Long>
    fun insertOrUpdateSyncTimestamp(eventId: String, timestamp: Long)
    fun selectSyncStatus(eventId: String): MockQuery<String>
    fun insertOrUpdateSyncStatus(eventId: String, status: String)
}

interface SyncApi {
    suspend fun getSyncManifest(eventId: String, forceRefresh: Boolean): SyncManifest
    suspend fun getIncrementalChanges(eventId: String, since: Long): List<IncrementalChange>
}

interface StoragePriorityManager {
    suspend fun getTotalStorageUsage(): Long
    suspend fun getStorageUsageByCategory(): Map<String, Long>
    suspend fun cleanupLowPriorityContent(targetBytes: Long): Long
}