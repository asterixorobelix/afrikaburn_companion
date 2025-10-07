package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.*
import io.asterixorobelix.afrikaburn.domain.repository.DownloadProgress
import io.asterixorobelix.afrikaburn.domain.repository.DownloadStatus
import io.asterixorobelix.afrikaburn.domain.repository.MapStorageStats
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MapRepositoryImplTest {
    
    private val mockOfflineMapQueries = MockOfflineMapQueries()
    private val mockMapPinQueries = MockMapPinQueries()
    private val mockCampLocationQueries = MockCampLocationQueries()
    private val mockMapApi = MockMapApi()
    private val mockTileStorageService = MockTileStorageService()
    
    private val repository = MapRepositoryImpl(
        offlineMapQueries = mockOfflineMapQueries,
        mapPinQueries = mockMapPinQueries,
        campLocationQueries = mockCampLocationQueries,
        mapApi = mockMapApi,
        tileStorageService = mockTileStorageService
    )
    
    @Test
    fun `getAllOfflineMaps should return all maps`() = runTest {
        // Given
        val map1 = createTestOfflineMap("map1", "AfrikaBurn 2025 Main Map")
        val map2 = createTestOfflineMap("map2", "AfrikaBurn 2025 Art Map")
        mockOfflineMapQueries.maps = listOf(map1.toDatabaseMap(), map2.toDatabaseMap())
        
        // When
        val result = repository.getAllOfflineMaps()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("map1", result[0].id)
        assertEquals("map2", result[1].id)
        assertEquals("AfrikaBurn 2025 Main Map", result[0].name)
        assertEquals("AfrikaBurn 2025 Art Map", result[1].name)
    }
    
    @Test
    fun `getOfflineMapById should return correct map`() = runTest {
        // Given
        val map = createTestOfflineMap("target-map", "Target Map")
        mockOfflineMapQueries.maps = listOf(map.toDatabaseMap())
        
        // When
        val result = repository.getOfflineMapById("target-map")
        
        // Then
        assertNotNull(result)
        assertEquals("target-map", result.id)
        assertEquals("Target Map", result.name)
    }
    
    @Test
    fun `getOfflineMapById should return null for non-existent map`() = runTest {
        // Given
        mockOfflineMapQueries.maps = emptyList()
        
        // When
        val result = repository.getOfflineMapById("non-existent")
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getOfflineMapsByEvent should filter by event ID`() = runTest {
        // Given
        val eventMap = createTestOfflineMap("event-map", "Event Map", isEventMap = true)
        val generalMap = createTestOfflineMap("general-map", "General Map", isEventMap = false)
        mockOfflineMapQueries.maps = listOf(eventMap.toDatabaseMap(), generalMap.toDatabaseMap())
        
        // When
        val result = repository.getOfflineMapsByEvent("afrikaburn-2025")
        
        // Then
        assertEquals(1, result.size)
        assertEquals("event-map", result[0].id)
        assertTrue(result[0].isEventMap)
    }
    
    @Test
    fun `saveOfflineMap should store map correctly`() = runTest {
        // Given
        val map = createTestOfflineMap("new-map", "New Test Map")
        
        // When
        repository.saveOfflineMap(map)
        
        // Then
        assertEquals(1, mockOfflineMapQueries.maps.size)
        assertEquals("new-map", mockOfflineMapQueries.maps[0].id)
        assertEquals("New Test Map", mockOfflineMapQueries.maps[0].name)
    }
    
    @Test
    fun `updateOfflineMap should modify existing map`() = runTest {
        // Given
        val originalMap = createTestOfflineMap("update-map", "Original Name")
        mockOfflineMapQueries.maps = mutableListOf(originalMap.toDatabaseMap())
        
        val updatedMap = originalMap.copy(
            name = "Updated Name",
            downloadProgress = 0.5f,
            downloadStatus = DownloadStatus.DOWNLOADING
        )
        
        // When
        repository.updateOfflineMap(updatedMap)
        
        // Then
        assertEquals(1, mockOfflineMapQueries.maps.size)
        assertEquals("Updated Name", mockOfflineMapQueries.maps[0].name)
        assertEquals(0.5, mockOfflineMapQueries.maps[0].downloadProgress)
        assertEquals("DOWNLOADING", mockOfflineMapQueries.maps[0].downloadStatus)
    }
    
    @Test
    fun `deleteOfflineMap should remove map and associated data`() = runTest {
        // Given
        val map = createTestOfflineMap("delete-map", "Map to Delete")
        mockOfflineMapQueries.maps = mutableListOf(map.toDatabaseMap())
        mockMapPinQueries.pins = mutableListOf(createTestMapPin("pin1", "delete-map").toDatabasePin())
        mockTileStorageService.tilesForMap["delete-map"] = 100L
        
        // When
        repository.deleteOfflineMap("delete-map")
        
        // Then
        assertEquals(0, mockOfflineMapQueries.maps.size)
        assertEquals(0, mockMapPinQueries.pins.size)
        assertTrue(mockTileStorageService.deletedMaps.contains("delete-map"))
    }
    
    @Test
    fun `downloadOfflineMap should track progress correctly`() = runTest {
        // Given
        val map = createTestOfflineMap("download-map", "Download Map", totalSizeBytes = 10_000_000L)
        mockOfflineMapQueries.maps = mutableListOf(map.toDatabaseMap())
        mockTileStorageService.availableSpace = 500_000_000L // 500MB available
        
        // Configure mock to emit progress updates
        mockTileStorageService.downloadProgress = listOf(0.0f, 0.3f, 0.7f, 1.0f)
        
        // When
        val progressList = repository.downloadOfflineMap("download-map", priority = 1).toList()
        
        // Then
        assertTrue(progressList.size >= 4, "Should emit multiple progress updates")
        
        // Check initial progress
        val initialProgress = progressList.first()
        assertEquals("download-map", initialProgress.mapId)
        assertEquals(DownloadStatus.DOWNLOADING, initialProgress.status)
        
        // Check final progress
        val finalProgress = progressList.last()
        assertEquals(1.0f, finalProgress.progress)
        assertEquals(DownloadStatus.COMPLETED, finalProgress.status)
        assertEquals(10_000_000L, finalProgress.totalBytes)
    }
    
    @Test
    fun `downloadOfflineMap should fail with insufficient storage`() = runTest {
        // Given
        val largeMap = createTestOfflineMap("large-map", "Large Map", totalSizeBytes = 1_000_000_000L) // 1GB
        mockOfflineMapQueries.maps = mutableListOf(largeMap.toDatabaseMap())
        mockTileStorageService.availableSpace = 100_000_000L // Only 100MB available
        
        // When
        val progressList = repository.downloadOfflineMap("large-map", priority = 1).toList()
        
        // Then
        val finalProgress = progressList.last()
        assertEquals(DownloadStatus.FAILED, finalProgress.status)
        assertTrue(finalProgress.mapId == "large-map")
    }
    
    @Test
    fun `isOfflineMapDownloaded should check availability correctly`() = runTest {
        // Given
        val downloadedMap = createTestOfflineMap("downloaded-map", "Downloaded Map", 
            downloadStatus = DownloadStatus.COMPLETED)
        val pendingMap = createTestOfflineMap("pending-map", "Pending Map", 
            downloadStatus = DownloadStatus.NOT_DOWNLOADED)
        
        mockOfflineMapQueries.maps = mutableListOf(
            downloadedMap.toDatabaseMap(),
            pendingMap.toDatabaseMap()
        )
        
        // When & Then
        assertTrue(repository.isOfflineMapDownloaded("downloaded-map"))
        assertFalse(repository.isOfflineMapDownloaded("pending-map"))
        assertFalse(repository.isOfflineMapDownloaded("non-existent"))
    }
    
    @Test
    fun `getDownloadProgress should return correct progress`() = runTest {
        // Given
        val map = createTestOfflineMap("progress-map", "Progress Map", downloadProgress = 0.75f)
        mockOfflineMapQueries.maps = mutableListOf(map.toDatabaseMap())
        
        // When
        val progress = repository.getDownloadProgress("progress-map")
        
        // Then
        assertEquals(0.75f, progress)
    }
    
    @Test
    fun `cancelDownload should update status and cancel storage operation`() = runTest {
        // Given
        val map = createTestOfflineMap("cancel-map", "Cancel Map", downloadStatus = DownloadStatus.DOWNLOADING)
        mockOfflineMapQueries.maps = mutableListOf(map.toDatabaseMap())
        
        // When
        repository.cancelDownload("cancel-map")
        
        // Then
        assertTrue(mockTileStorageService.cancelledDownloads.contains("cancel-map"))
        assertEquals("CANCELLED", mockOfflineMapQueries.maps[0].downloadStatus)
    }
    
    @Test
    fun `getMapPinsByMap should return pins for specific map`() = runTest {
        // Given
        val pin1 = createTestMapPin("pin1", "map1")
        val pin2 = createTestMapPin("pin2", "map1") 
        val pin3 = createTestMapPin("pin3", "map2")
        
        mockMapPinQueries.pins = mutableListOf(
            pin1.toDatabasePin(),
            pin2.toDatabasePin(),
            pin3.toDatabasePin()
        )
        
        // When
        val result = repository.getMapPinsByMap("map1")
        
        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.id.startsWith("pin") && it.id != "pin3" })
    }
    
    @Test
    fun `getMapPinsByContentType should filter by content type`() = runTest {
        // Given
        val artPin = createTestMapPin("art-pin", "map1", relatedContentType = "art_installation")
        val campPin = createTestMapPin("camp-pin", "map1", relatedContentType = "theme_camp")
        val generalPin = createTestMapPin("general-pin", "map1", relatedContentType = null)
        
        mockMapPinQueries.pins = mutableListOf(
            artPin.toDatabasePin(),
            campPin.toDatabasePin(),
            generalPin.toDatabasePin()
        )
        
        // When
        val artPins = repository.getMapPinsByContentType("map1", "art_installation")
        
        // Then
        assertEquals(1, artPins.size)
        assertEquals("art-pin", artPins[0].id)
    }
    
    @Test
    fun `getMapPinsNearLocation should return pins within radius`() = runTest {
        // Given
        val centerLat = -32.5
        val centerLng = 17.5
        val radiusKm = 5.0
        
        val nearPin = createTestMapPin("near-pin", "map1", latitude = -32.501, longitude = 17.501)
        val farPin = createTestMapPin("far-pin", "map1", latitude = -33.0, longitude = 18.0) // ~70km away
        
        mockMapPinQueries.pins = mutableListOf(
            nearPin.toDatabasePin(),
            farPin.toDatabasePin()
        )
        
        // When
        val result = repository.getMapPinsNearLocation("map1", centerLat, centerLng, radiusKm)
        
        // Then
        assertEquals(1, result.size)
        assertEquals("near-pin", result[0].id)
    }
    
    @Test
    fun `saveMapPin should store pin correctly`() = runTest {
        // Given
        val pin = createTestMapPin("new-pin", "map1")
        
        // When
        repository.saveMapPin(pin)
        
        // Then
        assertEquals(1, mockMapPinQueries.pins.size)
        assertEquals("new-pin", mockMapPinQueries.pins[0].id)
    }
    
    @Test
    fun `saveMapPins should store multiple pins in transaction`() = runTest {
        // Given
        val pins = listOf(
            createTestMapPin("pin1", "map1"),
            createTestMapPin("pin2", "map1"),
            createTestMapPin("pin3", "map1")
        )
        
        // When
        repository.saveMapPins(pins)
        
        // Then
        assertEquals(3, mockMapPinQueries.pins.size)
        assertTrue(mockMapPinQueries.transactionUsed)
    }
    
    @Test
    fun `updateMapPin should modify existing pin`() = runTest {
        // Given
        val originalPin = createTestMapPin("update-pin", "map1")
        mockMapPinQueries.pins = mutableListOf(originalPin.toDatabasePin())
        
        val updatedPin = originalPin.copy(
            title = "Updated Title",
            isBookmarked = true,
            visitCount = 5
        )
        
        // When
        repository.updateMapPin(updatedPin)
        
        // Then
        assertEquals("Updated Title", mockMapPinQueries.pins[0].title)
        assertEquals(1L, mockMapPinQueries.pins[0].isBookmarked)
        assertEquals(5L, mockMapPinQueries.pins[0].visitCount)
    }
    
    @Test
    fun `deleteMapPin should remove pin`() = runTest {
        // Given
        val pin = createTestMapPin("delete-pin", "map1")
        mockMapPinQueries.pins = mutableListOf(pin.toDatabasePin())
        
        // When
        repository.deleteMapPin("delete-pin")
        
        // Then
        assertEquals(0, mockMapPinQueries.pins.size)
    }
    
    @Test
    fun `getCampLocationsByDevice should filter by device ID`() = runTest {
        // Given
        val location1 = createTestCampLocation("loc1", "device1")
        val location2 = createTestCampLocation("loc2", "device1")
        val location3 = createTestCampLocation("loc3", "device2")
        
        mockCampLocationQueries.locations = mutableListOf(
            location1.toDatabaseLocation(),
            location2.toDatabaseLocation(),
            location3.toDatabaseLocation()
        )
        
        // When
        val result = repository.getCampLocationsByDevice("device1")
        
        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.deviceId == "device1" })
    }
    
    @Test
    fun `saveCampLocation should store location correctly`() = runTest {
        // Given
        val location = createTestCampLocation("new-location", "device1")
        
        // When
        repository.saveCampLocation(location)
        
        // Then
        assertEquals(1, mockCampLocationQueries.locations.size)
        assertEquals("new-location", mockCampLocationQueries.locations[0].id)
        assertEquals("device1", mockCampLocationQueries.locations[0].deviceId)
    }
    
    @Test
    fun `setActiveCampLocation should activate one and deactivate others`() = runTest {
        // Given
        val location1 = createTestCampLocation("loc1", "device1", isActive = true)
        val location2 = createTestCampLocation("loc2", "device1", isActive = false)
        mockCampLocationQueries.locations = mutableListOf(
            location1.toDatabaseLocation(),
            location2.toDatabaseLocation()
        )
        
        // When
        repository.setActiveCampLocation("loc2")
        
        // Then
        assertTrue(mockCampLocationQueries.transactionUsed)
        assertTrue(mockCampLocationQueries.deactivatedDevices.contains("device1"))
        assertTrue(mockCampLocationQueries.activatedLocations.contains("loc2"))
    }
    
    @Test
    fun `getTotalMapStorageBytes should delegate to storage service`() = runTest {
        // Given
        mockTileStorageService.totalStorageBytes = 150_000_000L // 150MB
        
        // When
        val result = repository.getTotalMapStorageBytes()
        
        // Then
        assertEquals(150_000_000L, result)
    }
    
    @Test
    fun `cleanupMapStorage should remove low-priority maps`() = runTest {
        // Given
        val criticalMap = createTestOfflineMap("critical", "Critical Map", 
            priority = MapPriority.CRITICAL, totalSizeBytes = 50_000_000L)
        val lowMap = createTestOfflineMap("low", "Low Priority Map", 
            priority = MapPriority.LOW, totalSizeBytes = 100_000_000L)
        
        mockOfflineMapQueries.maps = mutableListOf(
            criticalMap.toDatabaseMap(),
            lowMap.toDatabaseMap()
        )
        mockTileStorageService.totalStorageBytes = 200_000_000L // 200MB total
        
        // When
        val cleanedBytes = repository.cleanupMapStorage(targetSizeBytes = 100_000_000L)
        
        // Then
        assertEquals(100_000_000L, cleanedBytes) // Should clean the low priority map
        assertEquals(1, mockOfflineMapQueries.maps.size) // Only critical map remains
        assertEquals("critical", mockOfflineMapQueries.maps[0].id)
    }
    
    @Test
    fun `getMapStorageStats should return comprehensive stats`() = runTest {
        // Given
        mockTileStorageService.totalStorageBytes = 300_000_000L // 300MB
        mockTileStorageService.totalTileCount = 15000
        mockTileStorageService.lastCleanupTimestamp = 1234567890L
        
        val map1 = createTestOfflineMap("map1", "Map 1")
        val map2 = createTestOfflineMap("map2", "Map 2")
        mockOfflineMapQueries.maps = mutableListOf(map1.toDatabaseMap(), map2.toDatabaseMap())
        
        // When
        val stats = repository.getMapStorageStats()
        
        // Then
        assertEquals(300_000_000L, stats.totalSizeBytes)
        assertEquals(15000, stats.tilesCount)
        assertEquals(2, stats.mapsCount)
        assertEquals(1234567890L, stats.lastCleanupTimestamp)
    }
    
    @Test
    fun `getAvailableStorageBytes should calculate correctly`() = runTest {
        // Given
        val maxStorage = 500_000_000L // 500MB limit from MapRepositoryImpl.MAX_TOTAL_MAP_STORAGE_BYTES
        mockTileStorageService.totalStorageBytes = 200_000_000L // 200MB used
        
        // When
        val available = repository.getAvailableStorageBytes()
        
        // Then
        assertEquals(300_000_000L, available) // 500MB - 200MB = 300MB
    }
    
    @Test
    fun `syncMapData should fetch and store maps`() = runTest {
        // Given
        val remoteMap = createTestOfflineMap("remote-map", "Remote Map")
        mockMapApi.mapMetadata = listOf(remoteMap)
        
        // When
        val result = repository.syncMapData("afrikaburn-2025", forceRefresh = false)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, mockOfflineMapQueries.maps.size)
        assertEquals("remote-map", mockOfflineMapQueries.maps[0].id)
    }
    
    @Test
    fun `syncMapData should handle API errors gracefully`() = runTest {
        // Given
        mockMapApi.shouldThrowError = true
        
        // When
        val result = repository.syncMapData("afrikaburn-2025", forceRefresh = false)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `validateMapBoundaries should check if maps cover event area`() = runTest {
        // Given
        val eventLat = -32.5
        val eventLng = 17.5
        val eventRadius = 10.0 // 10km
        
        val nearbyMap = createTestOfflineMap("nearby", "Nearby Map", 
            centerLatitude = -32.51, centerLongitude = 17.51) // ~1km away
        val farMap = createTestOfflineMap("far", "Far Map",
            centerLatitude = -33.0, centerLongitude = 18.0) // ~70km away
        
        mockOfflineMapQueries.maps = mutableListOf(nearbyMap.toDatabaseMap(), farMap.toDatabaseMap())
        
        // When
        val result = repository.validateMapBoundaries(eventLat, eventLng, eventRadius)
        
        // Then
        assertTrue(result) // Should find the nearby map within radius
    }
    
    // Extension functions to convert domain models to database models
    private fun OfflineMap.toDatabaseMap(): DatabaseOfflineMap {
        return DatabaseOfflineMap(
            id = this.id,
            name = this.name,
            description = this.description,
            mapType = this.mapType.name,
            zoomLevelsStart = this.zoomLevels.first.toLong(),
            zoomLevelsEnd = this.zoomLevels.last.toLong(),
            boundingBoxNorth = this.boundingBox.north,
            boundingBoxSouth = this.boundingBox.south,
            boundingBoxEast = this.boundingBox.east,
            boundingBoxWest = this.boundingBox.west,
            centerLatitude = this.centerLatitude,
            centerLongitude = this.centerLongitude,
            isEventMap = if (this.isEventMap) 1L else 0L,
            isOfficial = if (this.isOfficial) 1L else 0L,
            tileCount = this.tileCount.toLong(),
            totalSizeBytes = this.totalSizeBytes,
            compressionRatio = this.compressionRatio.toDouble(),
            downloadStatus = this.downloadStatus.name,
            downloadProgress = this.downloadProgress.toDouble(),
            downloadSpeed = this.downloadSpeed,
            estimatedDownloadTime = this.estimatedDownloadTime,
            lastDownloadAttempt = this.lastDownloadAttempt,
            downloadUrl = this.downloadUrl,
            checksum = this.checksum,
            version = this.version,
            priority = this.priority.name,
            isAutoDownload = if (this.isAutoDownload) 1L else 0L,
            requiresWifi = if (this.requiresWifi) 1L else 0L,
            expirationTime = this.expirationTime,
            lastUsed = this.lastUsed,
            useCount = this.useCount.toLong(),
            isCorrupted = if (this.isCorrupted) 1L else 0L,
            errorMessage = this.errorMessage,
            createdAt = this.createdAt,
            lastUpdated = this.lastUpdated
        )
    }
    
    private fun MapPin.toDatabasePin(): DatabaseMapPin {
        return DatabaseMapPin(
            id = this.id,
            title = this.title,
            subtitle = this.subtitle,
            description = this.description,
            latitude = this.latitude,
            longitude = this.longitude,
            altitude = this.altitude,
            pinType = this.pinType.name,
            category = this.category.name,
            iconName = this.iconName,
            iconColor = this.iconColor,
            backgroundColor = this.backgroundColor,
            isOfficial = if (this.isOfficial) 1L else 0L,
            isUserCreated = if (this.isUserCreated) 1L else 0L,
            isVisible = if (this.isVisible) 1L else 0L,
            zoomLevelRangeStart = this.zoomLevelRange?.first?.toLong(),
            zoomLevelRangeEnd = this.zoomLevelRange?.last?.toLong(),
            priority = this.priority.name,
            relatedContentId = this.relatedContentId,
            relatedContentType = this.relatedContentType,
            address = this.address,
            operatingHours = this.operatingHours,
            contact = this.contact,
            website = this.website,
            socialMedia = this.socialMedia.joinToString(","),
            tags = this.tags.joinToString(","),
            amenities = this.amenities.joinToString(","),
            accessibility = this.accessibility.joinToString(","),
            photos = this.photos.joinToString(","),
            rating = this.rating,
            reviewCount = this.reviewCount.toLong(),
            isAccessible = if (this.isAccessible) 1L else 0L,
            hasShade = if (this.hasShade) 1L else 0L,
            hasWater = if (this.hasWater) 1L else 0L,
            hasFood = if (this.hasFood) 1L else 0L,
            hasToilets = if (this.hasToilets) 1L else 0L,
            hasMedical = if (this.hasMedical) 1L else 0L,
            hasCharging = if (this.hasCharging) 1L else 0L,
            hasWifi = if (this.hasWifi) 1L else 0L,
            isEmergency = if (this.isEmergency) 1L else 0L,
            emergencyType = this.emergencyType?.name,
            capacity = this.capacity?.toLong(),
            currentOccupancy = this.currentOccupancy?.toLong(),
            status = this.status.name,
            lastVerified = this.lastVerified,
            reportedIssues = this.reportedIssues.joinToString(","),
            distanceFromCenter = this.distanceFromCenter,
            walkingTimeMinutes = this.walkingTimeMinutes?.toLong(),
            isBookmarked = if (this.isBookmarked) 1L else 0L,
            visitCount = this.visitCount.toLong(),
            lastVisited = this.lastVisited,
            createdBy = this.createdBy,
            createdAt = this.createdAt,
            lastUpdated = this.lastUpdated
        )
    }
    
    private fun CampLocation.toDatabaseLocation(): DatabaseCampLocation {
        return DatabaseCampLocation(
            id = this.id,
            deviceId = this.deviceId ?: "unknown",
            name = this.name,
            description = this.description,
            latitude = this.latitude,
            longitude = this.longitude,
            altitude = this.altitude,
            accuracy = this.accuracy.toDouble(),
            isUserMarked = if (this.isUserMarked) 1L else 0L,
            isOfficial = if (this.isOfficial) 1L else 0L,
            locationType = this.locationType.name,
            address = this.address,
            whatThreeWords = this.whatThreeWords,
            campType = this.campType?.name,
            capacity = this.capacity?.toLong(),
            amenities = this.amenities.joinToString(","),
            contacts = this.contacts.joinToString(","),
            operatingHours = this.operatingHours,
            isActive = if (this.isActive) 1L else 0L,
            isShared = if (this.isShared) 1L else 0L,
            shareCode = this.shareCode,
            sharedByDeviceId = this.sharedByDeviceId,
            privacyLevel = this.privacyLevel.name,
            tags = this.tags.joinToString(","),
            notes = this.notes,
            photos = this.photos.joinToString(","),
            lastVisited = this.lastVisited,
            visitCount = this.visitCount.toLong(),
            isEmergencyLocation = if (this.isEmergencyLocation) 1L else 0L,
            emergencyType = this.emergencyType?.name,
            createdAt = this.createdAt,
            lastUpdated = this.lastUpdated
        )
    }

    // Helper functions for creating test data
    private fun createTestOfflineMap(
        id: String,
        name: String,
        isEventMap: Boolean = true,
        downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
        downloadProgress: Float = 0f,
        priority: MapPriority = MapPriority.HIGH,
        totalSizeBytes: Long = 50_000_000L,
        centerLatitude: Double = -32.5,
        centerLongitude: Double = 17.5
    ): OfflineMap {
        return OfflineMap(
            id = id,
            name = name,
            description = "Test map description",
            mapType = MapType.SATELLITE,
            zoomLevels = 10..18,
            boundingBox = BoundingBox(
                north = -30.0,
                south = -35.0,
                east = 20.0,
                west = 15.0
            ),
            centerLatitude = centerLatitude,
            centerLongitude = centerLongitude,
            isEventMap = isEventMap,
            isOfficial = true,
            tileCount = 1000,
            totalSizeBytes = totalSizeBytes,
            compressionRatio = 0.7f,
            downloadStatus = downloadStatus,
            downloadProgress = downloadProgress,
            downloadSpeed = null,
            estimatedDownloadTime = null,
            lastDownloadAttempt = null,
            downloadUrl = "https://example.com/map.zip",
            checksum = "abc123",
            version = "1.0",
            priority = priority,
            isAutoDownload = false,
            requiresWifi = true,
            expirationTime = null,
            lastUsed = null,
            useCount = 0,
            isCorrupted = false,
            errorMessage = null,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun createTestMapPin(
        id: String,
        mapId: String = "test-map",
        latitude: Double = -32.5,
        longitude: Double = 17.5,
        relatedContentType: String? = null
    ): MapPin {
        return MapPin(
            id = id,
            title = "Test Pin",
            subtitle = "Test subtitle",
            description = "Test description",
            latitude = latitude,
            longitude = longitude,
            altitude = null,
            pinType = PinType.LOCATION,
            category = PinCategory.GENERAL,
            iconName = "location_pin",
            iconColor = "#FF0000",
            backgroundColor = "#FFFFFF",
            isOfficial = true,
            isUserCreated = false,
            isVisible = true,
            zoomLevelRange = 10..18,
            priority = PinPriority.MEDIUM,
            relatedContentId = null,
            relatedContentType = relatedContentType,
            address = null,
            operatingHours = null,
            contact = null,
            website = null,
            socialMedia = emptyList(),
            tags = emptyList(),
            amenities = emptyList(),
            accessibility = emptyList(),
            photos = emptyList(),
            rating = null,
            reviewCount = 0,
            isAccessible = false,
            hasShade = false,
            hasWater = false,
            hasFood = false,
            hasToilets = false,
            hasMedical = false,
            hasCharging = false,
            hasWifi = false,
            isEmergency = false,
            emergencyType = null,
            capacity = null,
            currentOccupancy = null,
            status = PinStatus.ACTIVE,
            lastVerified = null,
            reportedIssues = emptyList(),
            distanceFromCenter = 0.0,
            walkingTimeMinutes = null,
            isBookmarked = false,
            visitCount = 0,
            lastVisited = null,
            createdBy = null,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun createTestCampLocation(
        id: String,
        deviceId: String,
        isActive: Boolean = false
    ): CampLocation {
        return CampLocation(
            id = id,
            deviceId = deviceId,
            name = "Test Camp",
            description = "Test camp description",
            latitude = -32.5,
            longitude = 17.5,
            altitude = null,
            accuracy = 5.0f,
            isUserMarked = true,
            isOfficial = false,
            locationType = LocationType.CAMP,
            address = null,
            whatThreeWords = null,
            campType = CampType.TENT,
            capacity = null,
            amenities = emptyList(),
            contacts = emptyList(),
            operatingHours = null,
            isActive = isActive,
            isShared = false,
            shareCode = null,
            sharedByDeviceId = null,
            privacyLevel = PrivacyLevel.PRIVATE,
            tags = emptyList(),
            notes = null,
            photos = emptyList(),
            lastVisited = null,
            visitCount = 0,
            isEmergencyLocation = false,
            emergencyType = null,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}

/**
 * Mock implementations for testing MapRepository
 */

// Mock database queries
class MockOfflineMapQueries {
    var maps = mutableListOf<DatabaseOfflineMap>()
    
    fun selectAllOfflineMaps(): MockQuery<List<DatabaseOfflineMap>> {
        return MockQuery(maps)
    }
    
    fun selectOfflineMapById(id: String): MockQuery<DatabaseOfflineMap?> {
        return MockQuery(maps.find { it.id == id })
    }
    
    fun selectOfflineMapsByEvent(eventId: String): MockQuery<List<DatabaseOfflineMap>> {
        return MockQuery(maps.filter { it.isEventMap == 1L })
    }
    
    fun insertOfflineMap(
        id: String, name: String, description: String?, mapType: String,
        zoomLevelsStart: Long, zoomLevelsEnd: Long,
        boundingBoxNorth: Double, boundingBoxSouth: Double, boundingBoxEast: Double, boundingBoxWest: Double,
        centerLatitude: Double, centerLongitude: Double,
        isEventMap: Long, isOfficial: Long, tileCount: Long, totalSizeBytes: Long,
        compressionRatio: Double, downloadStatus: String, downloadProgress: Double,
        downloadSpeed: Double?, estimatedDownloadTime: Long?, lastDownloadAttempt: Long?,
        downloadUrl: String?, checksum: String?, version: String, priority: String,
        isAutoDownload: Long, requiresWifi: Long, expirationTime: Long?,
        lastUsed: Long?, useCount: Long, isCorrupted: Long, errorMessage: String?,
        createdAt: Long, lastUpdated: Long
    ) {
        maps.add(DatabaseOfflineMap(
            id, name, description, mapType, zoomLevelsStart, zoomLevelsEnd,
            boundingBoxNorth, boundingBoxSouth, boundingBoxEast, boundingBoxWest,
            centerLatitude, centerLongitude, isEventMap, isOfficial, tileCount, totalSizeBytes,
            compressionRatio, downloadStatus, downloadProgress, downloadSpeed, estimatedDownloadTime,
            lastDownloadAttempt, downloadUrl, checksum, version, priority, isAutoDownload,
            requiresWifi, expirationTime, lastUsed, useCount, isCorrupted, errorMessage,
            createdAt, lastUpdated
        ))
    }
    
    fun updateOfflineMap(
        name: String, description: String?, downloadStatus: String, downloadProgress: Double,
        downloadSpeed: Double?, estimatedDownloadTime: Long?, lastDownloadAttempt: Long?,
        lastUsed: Long?, useCount: Long, isCorrupted: Long, errorMessage: String?,
        lastUpdated: Long, id: String
    ) {
        val index = maps.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = maps[index]
            maps[index] = existing.copy(
                name = name, description = description, downloadStatus = downloadStatus,
                downloadProgress = downloadProgress, downloadSpeed = downloadSpeed,
                estimatedDownloadTime = estimatedDownloadTime, lastDownloadAttempt = lastDownloadAttempt,
                lastUsed = lastUsed, useCount = useCount, isCorrupted = isCorrupted,
                errorMessage = errorMessage, lastUpdated = lastUpdated
            )
        }
    }
    
    fun deleteOfflineMap(id: String) {
        maps.removeIf { it.id == id }
    }
}

class MockMapPinQueries {
    var pins = mutableListOf<DatabaseMapPin>()
    var transactionUsed = false
    
    fun selectMapPinsByMap(mapId: String): MockQuery<List<DatabaseMapPin>> {
        return MockQuery(pins) // Filter would be done in real implementation
    }
    
    fun selectMapPinsByContentType(mapId: String, contentType: String): MockQuery<List<DatabaseMapPin>> {
        return MockQuery(pins.filter { it.relatedContentType == contentType })
    }
    
    fun selectMapPinsInBounds(
        mapId: String, minLat: Double, maxLat: Double, minLng: Double, maxLng: Double
    ): MockQuery<List<DatabaseMapPin>> {
        return MockQuery(pins.filter { 
            it.latitude in minLat..maxLat && it.longitude in minLng..maxLng 
        })
    }
    
    fun insertMapPin(
        id: String, title: String, subtitle: String?, description: String?,
        latitude: Double, longitude: Double, altitude: Double?, pinType: String, category: String,
        iconName: String, iconColor: String, backgroundColor: String,
        isOfficial: Long, isUserCreated: Long, isVisible: Long,
        zoomLevelRangeStart: Long?, zoomLevelRangeEnd: Long?, priority: String,
        relatedContentId: String?, relatedContentType: String?,
        address: String?, operatingHours: String?, contact: String?, website: String?,
        socialMedia: String, tags: String, amenities: String, accessibility: String,
        photos: String, rating: Double?, reviewCount: Long,
        isAccessible: Long, hasShade: Long, hasWater: Long, hasFood: Long,
        hasToilets: Long, hasMedical: Long, hasCharging: Long, hasWifi: Long,
        isEmergency: Long, emergencyType: String?, capacity: Long?, currentOccupancy: Long?,
        status: String, lastVerified: Long?, reportedIssues: String,
        distanceFromCenter: Double, walkingTimeMinutes: Long?, isBookmarked: Long,
        visitCount: Long, lastVisited: Long?, createdBy: String?,
        createdAt: Long, lastUpdated: Long
    ) {
        pins.add(DatabaseMapPin(
            id, title, subtitle, description, latitude, longitude, altitude,
            pinType, category, iconName, iconColor, backgroundColor,
            isOfficial, isUserCreated, isVisible, zoomLevelRangeStart, zoomLevelRangeEnd,
            priority, relatedContentId, relatedContentType, address, operatingHours,
            contact, website, socialMedia, tags, amenities, accessibility, photos,
            rating, reviewCount, isAccessible, hasShade, hasWater, hasFood,
            hasToilets, hasMedical, hasCharging, hasWifi, isEmergency, emergencyType,
            capacity, currentOccupancy, status, lastVerified, reportedIssues,
            distanceFromCenter, walkingTimeMinutes, isBookmarked, visitCount,
            lastVisited, createdBy, createdAt, lastUpdated
        ))
    }
    
    fun updateMapPin(
        title: String, subtitle: String?, description: String?, isVisible: Long,
        currentOccupancy: Long?, status: String, lastVerified: Long?,
        reportedIssues: String, isBookmarked: Long, visitCount: Long,
        lastVisited: Long?, lastUpdated: Long, id: String
    ) {
        val index = pins.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = pins[index]
            pins[index] = existing.copy(
                title = title, subtitle = subtitle, description = description,
                isVisible = isVisible, currentOccupancy = currentOccupancy,
                status = status, lastVerified = lastVerified, reportedIssues = reportedIssues,
                isBookmarked = isBookmarked, visitCount = visitCount,
                lastVisited = lastVisited, lastUpdated = lastUpdated
            )
        }
    }
    
    fun deleteMapPin(id: String) {
        pins.removeIf { it.id == id }
    }
    
    fun deleteMapPinsByMap(mapId: String) {
        // Would filter by mapId in real implementation
        pins.clear()
    }
    
    fun deleteMapPinsByContentId(contentId: String) {
        pins.removeIf { it.relatedContentId == contentId }
    }
    
    fun transaction(block: () -> Unit) {
        transactionUsed = true
        block()
    }
}

class MockCampLocationQueries {
    var locations = mutableListOf<DatabaseCampLocation>()
    var transactionUsed = false
    var deactivatedDevices = mutableListOf<String>()
    var activatedLocations = mutableListOf<String>()
    
    fun selectCampLocationsByDevice(deviceId: String): MockQuery<List<DatabaseCampLocation>> {
        return MockQuery(locations.filter { it.deviceId == deviceId })
    }
    
    fun selectCampLocationById(id: String): MockQuery<DatabaseCampLocation?> {
        return MockQuery(locations.find { it.id == id })
    }
    
    fun selectActiveCampLocation(deviceId: String): MockQuery<DatabaseCampLocation?> {
        return MockQuery(locations.find { it.deviceId == deviceId && it.isActive == 1L })
    }
    
    fun insertCampLocation(
        id: String, deviceId: String, name: String, description: String?,
        latitude: Double, longitude: Double, altitude: Double?, accuracy: Double,
        isUserMarked: Long, isOfficial: Long, locationType: String,
        address: String?, whatThreeWords: String?, campType: String?, capacity: Long?,
        amenities: String, contacts: String, operatingHours: String?,
        isActive: Long, isShared: Long, shareCode: String?, sharedByDeviceId: String?,
        privacyLevel: String, tags: String, notes: String?, photos: String,
        lastVisited: Long?, visitCount: Long, isEmergencyLocation: Long,
        emergencyType: String?, createdAt: Long, lastUpdated: Long
    ) {
        locations.add(DatabaseCampLocation(
            id, deviceId, name, description, latitude, longitude, altitude, accuracy,
            isUserMarked, isOfficial, locationType, address, whatThreeWords, campType,
            capacity, amenities, contacts, operatingHours, isActive, isShared,
            shareCode, sharedByDeviceId, privacyLevel, tags, notes, photos,
            lastVisited, visitCount, isEmergencyLocation, emergencyType, createdAt, lastUpdated
        ))
    }
    
    fun updateCampLocation(
        name: String, description: String?, operatingHours: String?, isActive: Long,
        isShared: Long, shareCode: String?, privacyLevel: String, tags: String,
        notes: String?, photos: String, lastVisited: Long?, visitCount: Long,
        lastUpdated: Long, id: String
    ) {
        val index = locations.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = locations[index]
            locations[index] = existing.copy(
                name = name, description = description, operatingHours = operatingHours,
                isActive = isActive, isShared = isShared, shareCode = shareCode,
                privacyLevel = privacyLevel, tags = tags, notes = notes, photos = photos,
                lastVisited = lastVisited, visitCount = visitCount, lastUpdated = lastUpdated
            )
        }
    }
    
    fun deleteCampLocation(id: String) {
        locations.removeIf { it.id == id }
    }
    
    fun deactivateAllCampLocations(deviceId: String) {
        deactivatedDevices.add(deviceId)
        locations.forEachIndexed { index, location ->
            if (location.deviceId == deviceId) {
                locations[index] = location.copy(isActive = 0L)
            }
        }
    }
    
    fun activateCampLocation(locationId: String) {
        activatedLocations.add(locationId)
        val index = locations.indexOfFirst { it.id == locationId }
        if (index >= 0) {
            locations[index] = locations[index].copy(isActive = 1L)
        }
    }
    
    fun transaction(block: () -> Unit) {
        transactionUsed = true
        block()
    }
}

// Mock API and services
class MockMapApi {
    var mapMetadata = emptyList<OfflineMap>()
    var shouldThrowError = false
    
    suspend fun getMapMetadata(eventId: String): List<OfflineMap> {
        if (shouldThrowError) throw RuntimeException("API Error")
        return mapMetadata
    }
}

class MockTileStorageService {
    var tilesForMap = mutableMapOf<String, Long>()
    var deletedMaps = mutableListOf<String>()
    var cancelledDownloads = mutableListOf<String>()
    var totalStorageBytes = 0L
    var totalTileCount = 0
    var lastCleanupTimestamp = 0L
    var availableSpace = Long.MAX_VALUE
    var downloadProgress = listOf<Float>()
    
    suspend fun deleteTilesForMap(mapId: String) {
        deletedMaps.add(mapId)
        tilesForMap.remove(mapId)
    }
    
    suspend fun downloadMapTiles(
        mapId: String, boundingBox: BoundingBox, zoomLevels: IntRange, downloadUrl: String
    ): kotlinx.coroutines.flow.Flow<TileDownloadProgress> {
        return kotlinx.coroutines.flow.flow {
            downloadProgress.forEach { progress ->
                emit(TileDownloadProgress(
                    mapId = mapId,
                    progress = progress,
                    downloadedBytes = (progress * 10_000_000L).toLong(),
                    duration = 1000L // 1 second
                ))
            }
        }
    }
    
    suspend fun cancelDownload(mapId: String) {
        cancelledDownloads.add(mapId)
    }
    
    suspend fun getTotalStorageBytes(): Long = totalStorageBytes
    
    suspend fun getTotalTileCount(): Int = totalTileCount
    
    suspend fun getLastCleanupTimestamp(): Long = lastCleanupTimestamp
}

// Helper data classes for mocks
class MockQuery<T>(private val data: T) {
    fun executeAsList(): List<T> = if (data is List<*>) data as List<T> else listOf(data)
    fun executeAsOneOrNull(): T? = data
    fun asFlow(): MockFlow<T> = MockFlow(data)
}

class MockFlow<T>(private val data: T) {
    fun mapToList(): MockFlow<List<T>> = MockFlow(if (data is List<*>) data as List<T> else listOf(data))
}

data class TileDownloadProgress(
    val mapId: String,
    val progress: Float,
    val downloadedBytes: Long,
    val duration: Long
)

// Import the database entity classes that are already defined in MapRepositoryImpl.kt
// These would normally be generated by SQLDelight