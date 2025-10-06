package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.OfflineMap
import io.asterixorobelix.afrikaburn.domain.model.MapPin
import io.asterixorobelix.afrikaburn.domain.model.CampLocation
import io.asterixorobelix.afrikaburn.domain.repository.MapRepository
import io.asterixorobelix.afrikaburn.domain.repository.DownloadProgress
import io.asterixorobelix.afrikaburn.domain.repository.DownloadStatus
import io.asterixorobelix.afrikaburn.domain.repository.MapStorageStats
import io.asterixorobelix.afrikaburn.data.local.OfflineMapQueries
import io.asterixorobelix.afrikaburn.data.local.MapPinQueries
import io.asterixorobelix.afrikaburn.data.local.CampLocationQueries
import io.asterixorobelix.afrikaburn.data.remote.MapApi
import io.asterixorobelix.afrikaburn.data.storage.TileStorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of MapRepository with offline tile management
 * 
 * Handles offline map tiles, map pins, and user-marked locations
 * with intelligent storage management within the 2GB app limit.
 */
class MapRepositoryImpl(
    private val offlineMapQueries: OfflineMapQueries,
    private val mapPinQueries: MapPinQueries,
    private val campLocationQueries: CampLocationQueries,
    private val mapApi: MapApi,
    private val tileStorageService: TileStorageService
) : MapRepository {
    
    companion object {
        private const val MAX_TOTAL_MAP_STORAGE_BYTES = 500_000_000L // 500MB for maps (25% of 2GB)
        private const val DOWNLOAD_CHUNK_SIZE = 1024 * 1024 // 1MB chunks
        private const val CLEANUP_THRESHOLD = 0.9 // Clean up when 90% full
    }
    
    // Offline Maps
    
    override suspend fun getAllOfflineMaps(): List<OfflineMap> = withContext(Dispatchers.IO) {
        offlineMapQueries.selectAllOfflineMaps().executeAsList().map { it.toOfflineMap() }
    }
    
    override suspend fun getOfflineMapById(mapId: String): OfflineMap? = withContext(Dispatchers.IO) {
        offlineMapQueries.selectOfflineMapById(mapId).executeAsOneOrNull()?.toOfflineMap()
    }
    
    override suspend fun getOfflineMapsByEvent(eventId: String): List<OfflineMap> = withContext(Dispatchers.IO) {
        offlineMapQueries.selectOfflineMapsByEvent(eventId).executeAsList().map { it.toOfflineMap() }
    }
    
    override fun observeOfflineMaps(): Flow<List<OfflineMap>> {
        return offlineMapQueries.selectAllOfflineMaps().asFlow().mapToList().map { list ->
            list.map { it.toOfflineMap() }
        }
    }
    
    override suspend fun saveOfflineMap(offlineMap: OfflineMap) = withContext(Dispatchers.IO) {
        offlineMapQueries.insertOfflineMap(
            id = offlineMap.id,
            name = offlineMap.name,
            description = offlineMap.description,
            mapType = offlineMap.mapType.name,
            zoomLevelsStart = offlineMap.zoomLevels.first,
            zoomLevelsEnd = offlineMap.zoomLevels.last,
            boundingBoxNorth = offlineMap.boundingBox.north,
            boundingBoxSouth = offlineMap.boundingBox.south,
            boundingBoxEast = offlineMap.boundingBox.east,
            boundingBoxWest = offlineMap.boundingBox.west,
            centerLatitude = offlineMap.centerLatitude,
            centerLongitude = offlineMap.centerLongitude,
            isEventMap = if (offlineMap.isEventMap) 1L else 0L,
            isOfficial = if (offlineMap.isOfficial) 1L else 0L,
            tileCount = offlineMap.tileCount,
            totalSizeBytes = offlineMap.totalSizeBytes,
            compressionRatio = offlineMap.compressionRatio,
            downloadStatus = offlineMap.downloadStatus.name,
            downloadProgress = offlineMap.downloadProgress,
            downloadSpeed = offlineMap.downloadSpeed,
            estimatedDownloadTime = offlineMap.estimatedDownloadTime,
            lastDownloadAttempt = offlineMap.lastDownloadAttempt,
            downloadUrl = offlineMap.downloadUrl,
            checksum = offlineMap.checksum,
            version = offlineMap.version,
            priority = offlineMap.priority.name,
            isAutoDownload = if (offlineMap.isAutoDownload) 1L else 0L,
            requiresWifi = if (offlineMap.requiresWifi) 1L else 0L,
            expirationTime = offlineMap.expirationTime,
            lastUsed = offlineMap.lastUsed,
            useCount = offlineMap.useCount.toLong(),
            isCorrupted = if (offlineMap.isCorrupted) 1L else 0L,
            errorMessage = offlineMap.errorMessage,
            createdAt = offlineMap.createdAt,
            lastUpdated = offlineMap.lastUpdated
        )
    }
    
    override suspend fun updateOfflineMap(offlineMap: OfflineMap) = withContext(Dispatchers.IO) {
        offlineMapQueries.updateOfflineMap(
            name = offlineMap.name,
            description = offlineMap.description,
            downloadStatus = offlineMap.downloadStatus.name,
            downloadProgress = offlineMap.downloadProgress,
            downloadSpeed = offlineMap.downloadSpeed,
            estimatedDownloadTime = offlineMap.estimatedDownloadTime,
            lastDownloadAttempt = offlineMap.lastDownloadAttempt,
            lastUsed = offlineMap.lastUsed,
            useCount = offlineMap.useCount.toLong(),
            isCorrupted = if (offlineMap.isCorrupted) 1L else 0L,
            errorMessage = offlineMap.errorMessage,
            lastUpdated = offlineMap.lastUpdated,
            id = offlineMap.id
        )
    }
    
    override suspend fun deleteOfflineMap(mapId: String) = withContext(Dispatchers.IO) {
        // Delete associated tiles first
        tileStorageService.deleteTilesForMap(mapId)
        // Delete map pins for this map
        mapPinQueries.deleteMapPinsByMap(mapId)
        // Delete the map record
        offlineMapQueries.deleteOfflineMap(mapId)
    }
    
    override suspend fun downloadOfflineMap(mapId: String, priority: Int): Flow<DownloadProgress> = flow {
        try {
            val map = getOfflineMapById(mapId) ?: throw IllegalArgumentException("Map not found")
            
            // Check storage space
            val availableSpace = getAvailableStorageBytes()
            if (map.totalSizeBytes > availableSpace) {
                throw IllegalStateException("Insufficient storage space")
            }
            
            // Update status to downloading
            updateOfflineMap(map.updateDownloadProgress(0f, null, io.asterixorobelix.afrikaburn.domain.model.DownloadStatus.DOWNLOADING))
            
            emit(DownloadProgress(mapId, 0f, 0L, map.totalSizeBytes, DownloadStatus.DOWNLOADING))
            
            // Download tiles using tile storage service
            tileStorageService.downloadMapTiles(
                mapId = mapId,
                boundingBox = map.boundingBox,
                zoomLevels = map.zoomLevels,
                downloadUrl = map.downloadUrl ?: throw IllegalArgumentException("No download URL")
            ).collect { progress ->
                // Update database with progress
                updateOfflineMap(map.updateDownloadProgress(progress.progress, progress.downloadedBytes / progress.duration))
                
                emit(DownloadProgress(
                    mapId = mapId,
                    progress = progress.progress,
                    downloadedBytes = progress.downloadedBytes,
                    totalBytes = map.totalSizeBytes,
                    status = if (progress.progress >= 1f) DownloadStatus.COMPLETED else DownloadStatus.DOWNLOADING
                ))
            }
            
            // Mark as completed
            updateOfflineMap(map.markDownloadCompleted())
            emit(DownloadProgress(mapId, 1f, map.totalSizeBytes, map.totalSizeBytes, DownloadStatus.COMPLETED))
            
        } catch (exception: Exception) {
            val map = getOfflineMapById(mapId)
            map?.let {
                updateOfflineMap(it.markDownloadFailed(exception.message ?: "Unknown error"))
            }
            emit(DownloadProgress(mapId, 0f, 0L, 0L, DownloadStatus.FAILED))
        }
    }
    
    override suspend fun isOfflineMapDownloaded(mapId: String): Boolean = withContext(Dispatchers.IO) {
        val map = getOfflineMapById(mapId)
        map?.isAvailable() ?: false
    }
    
    override suspend fun getDownloadProgress(mapId: String): Float = withContext(Dispatchers.IO) {
        val map = getOfflineMapById(mapId)
        map?.downloadProgress ?: 0f
    }
    
    override suspend fun cancelDownload(mapId: String) = withContext(Dispatchers.IO) {
        tileStorageService.cancelDownload(mapId)
        val map = getOfflineMapById(mapId)
        map?.let {
            updateOfflineMap(it.copy(downloadStatus = io.asterixorobelix.afrikaburn.domain.model.DownloadStatus.CANCELLED))
        }
    }
    
    // Map Pins
    
    override suspend fun getMapPinsByMap(mapId: String): List<MapPin> = withContext(Dispatchers.IO) {
        mapPinQueries.selectMapPinsByMap(mapId).executeAsList().map { it.toMapPin() }
    }
    
    override suspend fun getMapPinsByContentType(mapId: String, contentType: String): List<MapPin> = withContext(Dispatchers.IO) {
        mapPinQueries.selectMapPinsByContentType(mapId, contentType).executeAsList().map { it.toMapPin() }
    }
    
    override suspend fun getMapPinsNearLocation(
        mapId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<MapPin> = withContext(Dispatchers.IO) {
        // Simple bounding box calculation for efficiency
        val latDelta = radiusKm / 111.0 // Rough conversion: 1 degree = 111 km
        val lngDelta = radiusKm / (111.0 * kotlin.math.cos(kotlin.math.toRadians(latitude)))
        
        mapPinQueries.selectMapPinsInBounds(
            mapId = mapId,
            minLat = latitude - latDelta,
            maxLat = latitude + latDelta,
            minLng = longitude - lngDelta,
            maxLng = longitude + lngDelta
        ).executeAsList().map { it.toMapPin() }.filter { pin ->
            // Precise distance calculation
            pin.getDistanceTo(latitude, longitude) <= radiusKm
        }
    }
    
    override suspend fun saveMapPin(mapPin: MapPin) = withContext(Dispatchers.IO) {
        mapPinQueries.insertMapPin(
            id = mapPin.id,
            title = mapPin.title,
            subtitle = mapPin.subtitle,
            description = mapPin.description,
            latitude = mapPin.latitude,
            longitude = mapPin.longitude,
            altitude = mapPin.altitude,
            pinType = mapPin.pinType.name,
            category = mapPin.category.name,
            iconName = mapPin.iconName,
            iconColor = mapPin.iconColor,
            backgroundColor = mapPin.backgroundColor,
            isOfficial = if (mapPin.isOfficial) 1L else 0L,
            isUserCreated = if (mapPin.isUserCreated) 1L else 0L,
            isVisible = if (mapPin.isVisible) 1L else 0L,
            zoomLevelRangeStart = mapPin.zoomLevelRange?.first,
            zoomLevelRangeEnd = mapPin.zoomLevelRange?.last,
            priority = mapPin.priority.name,
            relatedContentId = mapPin.relatedContentId,
            relatedContentType = mapPin.relatedContentType,
            address = mapPin.address,
            operatingHours = mapPin.operatingHours,
            contact = mapPin.contact,
            website = mapPin.website,
            socialMedia = mapPin.socialMedia.joinToString(","),
            tags = mapPin.tags.joinToString(","),
            amenities = mapPin.amenities.joinToString(","),
            accessibility = mapPin.accessibility.joinToString(","),
            photos = mapPin.photos.joinToString(","),
            rating = mapPin.rating,
            reviewCount = mapPin.reviewCount.toLong(),
            isAccessible = if (mapPin.isAccessible) 1L else 0L,
            hasShade = if (mapPin.hasShade) 1L else 0L,
            hasWater = if (mapPin.hasWater) 1L else 0L,
            hasFood = if (mapPin.hasFood) 1L else 0L,
            hasToilets = if (mapPin.hasToilets) 1L else 0L,
            hasMedical = if (mapPin.hasMedical) 1L else 0L,
            hasCharging = if (mapPin.hasCharging) 1L else 0L,
            hasWifi = if (mapPin.hasWifi) 1L else 0L,
            isEmergency = if (mapPin.isEmergency) 1L else 0L,
            emergencyType = mapPin.emergencyType?.name,
            capacity = mapPin.capacity?.toLong(),
            currentOccupancy = mapPin.currentOccupancy?.toLong(),
            status = mapPin.status.name,
            lastVerified = mapPin.lastVerified,
            reportedIssues = mapPin.reportedIssues.joinToString(","),
            distanceFromCenter = mapPin.distanceFromCenter,
            walkingTimeMinutes = mapPin.walkingTimeMinutes?.toLong(),
            isBookmarked = if (mapPin.isBookmarked) 1L else 0L,
            visitCount = mapPin.visitCount.toLong(),
            lastVisited = mapPin.lastVisited,
            createdBy = mapPin.createdBy,
            createdAt = mapPin.createdAt,
            lastUpdated = mapPin.lastUpdated
        )
    }
    
    override suspend fun saveMapPins(mapPins: List<MapPin>) = withContext(Dispatchers.IO) {
        mapPinQueries.transaction {
            mapPins.forEach { pin ->
                saveMapPin(pin)
            }
        }
    }
    
    override suspend fun updateMapPin(mapPin: MapPin) = withContext(Dispatchers.IO) {
        // Implementation similar to saveMapPin but using update query
        mapPinQueries.updateMapPin(
            title = mapPin.title,
            subtitle = mapPin.subtitle,
            description = mapPin.description,
            isVisible = if (mapPin.isVisible) 1L else 0L,
            currentOccupancy = mapPin.currentOccupancy?.toLong(),
            status = mapPin.status.name,
            lastVerified = mapPin.lastVerified,
            reportedIssues = mapPin.reportedIssues.joinToString(","),
            isBookmarked = if (mapPin.isBookmarked) 1L else 0L,
            visitCount = mapPin.visitCount.toLong(),
            lastVisited = mapPin.lastVisited,
            lastUpdated = mapPin.lastUpdated,
            id = mapPin.id
        )
    }
    
    override suspend fun deleteMapPin(pinId: String) = withContext(Dispatchers.IO) {
        mapPinQueries.deleteMapPin(pinId)
    }
    
    override suspend fun deleteMapPinsByContentId(contentId: String) = withContext(Dispatchers.IO) {
        mapPinQueries.deleteMapPinsByContentId(contentId)
    }
    
    // Camp Locations
    
    override suspend fun getCampLocationsByDevice(deviceId: String): List<CampLocation> = withContext(Dispatchers.IO) {
        campLocationQueries.selectCampLocationsByDevice(deviceId).executeAsList().map { it.toCampLocation() }
    }
    
    override suspend fun getCampLocationById(locationId: String): CampLocation? = withContext(Dispatchers.IO) {
        campLocationQueries.selectCampLocationById(locationId).executeAsOneOrNull()?.toCampLocation()
    }
    
    override fun observeCampLocationsByDevice(deviceId: String): Flow<List<CampLocation>> {
        return campLocationQueries.selectCampLocationsByDevice(deviceId).asFlow().mapToList().map { list ->
            list.map { it.toCampLocation() }
        }
    }
    
    override suspend fun saveCampLocation(campLocation: CampLocation) = withContext(Dispatchers.IO) {
        campLocationQueries.insertCampLocation(
            id = campLocation.id,
            deviceId = campLocation.deviceId,
            name = campLocation.name,
            description = campLocation.description,
            latitude = campLocation.latitude,
            longitude = campLocation.longitude,
            altitude = campLocation.altitude,
            accuracy = campLocation.accuracy,
            isUserMarked = if (campLocation.isUserMarked) 1L else 0L,
            isOfficial = if (campLocation.isOfficial) 1L else 0L,
            locationType = campLocation.locationType.name,
            address = campLocation.address,
            whatThreeWords = campLocation.whatThreeWords,
            campType = campLocation.campType?.name,
            capacity = campLocation.capacity?.toLong(),
            amenities = campLocation.amenities.joinToString(","),
            contacts = campLocation.contacts.joinToString(","),
            operatingHours = campLocation.operatingHours,
            isActive = if (campLocation.isActive) 1L else 0L,
            isShared = if (campLocation.isShared) 1L else 0L,
            shareCode = campLocation.shareCode,
            sharedByDeviceId = campLocation.sharedByDeviceId,
            privacyLevel = campLocation.privacyLevel.name,
            tags = campLocation.tags.joinToString(","),
            notes = campLocation.notes,
            photos = campLocation.photos.joinToString(","),
            lastVisited = campLocation.lastVisited,
            visitCount = campLocation.visitCount.toLong(),
            isEmergencyLocation = if (campLocation.isEmergencyLocation) 1L else 0L,
            emergencyType = campLocation.emergencyType?.name,
            createdAt = campLocation.createdAt,
            lastUpdated = campLocation.lastUpdated
        )
    }
    
    override suspend fun updateCampLocation(campLocation: CampLocation) = withContext(Dispatchers.IO) {
        campLocationQueries.updateCampLocation(
            name = campLocation.name,
            description = campLocation.description,
            operatingHours = campLocation.operatingHours,
            isActive = if (campLocation.isActive) 1L else 0L,
            isShared = if (campLocation.isShared) 1L else 0L,
            shareCode = campLocation.shareCode,
            privacyLevel = campLocation.privacyLevel.name,
            tags = campLocation.tags.joinToString(","),
            notes = campLocation.notes,
            photos = campLocation.photos.joinToString(","),
            lastVisited = campLocation.lastVisited,
            visitCount = campLocation.visitCount.toLong(),
            lastUpdated = campLocation.lastUpdated,
            id = campLocation.id
        )
    }
    
    override suspend fun deleteCampLocation(locationId: String) = withContext(Dispatchers.IO) {
        campLocationQueries.deleteCampLocation(locationId)
    }
    
    override suspend fun getActiveCampLocation(deviceId: String): CampLocation? = withContext(Dispatchers.IO) {
        campLocationQueries.selectActiveCampLocation(deviceId).executeAsOneOrNull()?.toCampLocation()
    }
    
    override suspend fun setActiveCampLocation(locationId: String) = withContext(Dispatchers.IO) {
        val location = getCampLocationById(locationId) ?: return@withContext
        
        campLocationQueries.transaction {
            // Deactivate all other camp locations for this device
            campLocationQueries.deactivateAllCampLocations(location.deviceId ?: "")
            // Activate the selected location
            campLocationQueries.activateCampLocation(locationId)
        }
    }
    
    // Storage Management
    
    override suspend fun getTotalMapStorageBytes(): Long = withContext(Dispatchers.IO) {
        tileStorageService.getTotalStorageBytes()
    }
    
    override suspend fun cleanupMapStorage(targetSizeBytes: Long): Long = withContext(Dispatchers.IO) {
        val currentSize = getTotalMapStorageBytes()
        if (currentSize <= targetSizeBytes) return@withContext 0L
        
        val bytesToClean = currentSize - targetSizeBytes
        
        // Get maps sorted by priority and last usage
        val allMaps = getAllOfflineMaps().sortedWith(
            compareBy<OfflineMap> { it.getStoragePriorityScore() }
                .thenBy { it.lastUsed ?: 0L }
        )
        
        var cleanedBytes = 0L
        for (map in allMaps) {
            if (cleanedBytes >= bytesToClean) break
            
            if (map.priority != io.asterixorobelix.afrikaburn.domain.model.MapPriority.CRITICAL) {
                deleteOfflineMap(map.id)
                cleanedBytes += map.totalSizeBytes
            }
        }
        
        cleanedBytes
    }
    
    override suspend fun getMapStorageStats(): MapStorageStats = withContext(Dispatchers.IO) {
        val totalSize = getTotalMapStorageBytes()
        val tileCount = tileStorageService.getTotalTileCount()
        val mapCount = getAllOfflineMaps().size
        
        MapStorageStats(
            totalSizeBytes = totalSize,
            tilesCount = tileCount,
            mapsCount = mapCount,
            lastCleanupTimestamp = tileStorageService.getLastCleanupTimestamp()
        )
    }
    
    override suspend fun getAvailableStorageBytes(): Long = withContext(Dispatchers.IO) {
        val usedBytes = getTotalMapStorageBytes()
        MAX_TOTAL_MAP_STORAGE_BYTES - usedBytes
    }
    
    override suspend fun syncMapData(eventId: String, forceRefresh: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!forceRefresh) {
                // Check if we have recent map data
                val maps = getOfflineMapsByEvent(eventId)
                if (maps.isNotEmpty()) {
                    return@withContext Result.success(Unit)
                }
            }
            
            // Fetch map metadata from API
            val mapMetadata = mapApi.getMapMetadata(eventId)
            
            // Save map metadata
            mapMetadata.forEach { map ->
                saveOfflineMap(map)
            }
            
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    override suspend fun validateMapBoundaries(
        eventLatitude: Double,
        eventLongitude: Double,
        eventRadiusKm: Double
    ): Boolean = withContext(Dispatchers.IO) {
        val allMaps = getAllOfflineMaps()
        
        allMaps.any { map ->
            val distance = calculateDistance(
                eventLatitude, eventLongitude,
                map.centerLatitude, map.centerLongitude
            )
            distance <= eventRadiusKm
        }
    }
}

/**
 * Extension functions to convert database entities to domain models
 */
private fun DatabaseOfflineMap.toOfflineMap(): OfflineMap {
    return OfflineMap(
        id = this.id,
        name = this.name,
        description = this.description,
        mapType = io.asterixorobelix.afrikaburn.domain.model.MapType.valueOf(this.mapType),
        zoomLevels = this.zoomLevelsStart.toInt()..this.zoomLevelsEnd.toInt(),
        boundingBox = io.asterixorobelix.afrikaburn.domain.model.BoundingBox(
            north = this.boundingBoxNorth,
            south = this.boundingBoxSouth,
            east = this.boundingBoxEast,
            west = this.boundingBoxWest
        ),
        centerLatitude = this.centerLatitude,
        centerLongitude = this.centerLongitude,
        isEventMap = this.isEventMap == 1L,
        isOfficial = this.isOfficial == 1L,
        tileCount = this.tileCount.toInt(),
        totalSizeBytes = this.totalSizeBytes,
        compressionRatio = this.compressionRatio.toFloat(),
        downloadStatus = io.asterixorobelix.afrikaburn.domain.model.DownloadStatus.valueOf(this.downloadStatus),
        downloadProgress = this.downloadProgress.toFloat(),
        downloadSpeed = this.downloadSpeed,
        estimatedDownloadTime = this.estimatedDownloadTime,
        lastDownloadAttempt = this.lastDownloadAttempt,
        downloadUrl = this.downloadUrl,
        checksum = this.checksum,
        version = this.version,
        priority = io.asterixorobelix.afrikaburn.domain.model.MapPriority.valueOf(this.priority),
        isAutoDownload = this.isAutoDownload == 1L,
        requiresWifi = this.requiresWifi == 1L,
        expirationTime = this.expirationTime,
        lastUsed = this.lastUsed,
        useCount = this.useCount.toInt(),
        isCorrupted = this.isCorrupted == 1L,
        errorMessage = this.errorMessage,
        createdAt = this.createdAt,
        lastUpdated = this.lastUpdated
    )
}

private fun DatabaseMapPin.toMapPin(): MapPin {
    return MapPin(
        id = this.id,
        title = this.title,
        subtitle = this.subtitle,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        altitude = this.altitude,
        pinType = io.asterixorobelix.afrikaburn.domain.model.PinType.valueOf(this.pinType),
        category = io.asterixorobelix.afrikaburn.domain.model.PinCategory.valueOf(this.category),
        iconName = this.iconName,
        iconColor = this.iconColor,
        backgroundColor = this.backgroundColor,
        isOfficial = this.isOfficial == 1L,
        isUserCreated = this.isUserCreated == 1L,
        isVisible = this.isVisible == 1L,
        zoomLevelRange = if (this.zoomLevelRangeStart != null && this.zoomLevelRangeEnd != null) {
            this.zoomLevelRangeStart.toInt()..this.zoomLevelRangeEnd.toInt()
        } else null,
        priority = io.asterixorobelix.afrikaburn.domain.model.PinPriority.valueOf(this.priority),
        relatedContentId = this.relatedContentId,
        relatedContentType = this.relatedContentType,
        address = this.address,
        operatingHours = this.operatingHours,
        contact = this.contact,
        website = this.website,
        socialMedia = if (this.socialMedia.isBlank()) emptyList() else this.socialMedia.split(",").map { it.trim() },
        tags = if (this.tags.isBlank()) emptyList() else this.tags.split(",").map { it.trim() },
        amenities = if (this.amenities.isBlank()) emptyList() else this.amenities.split(",").map { it.trim() },
        accessibility = if (this.accessibility.isBlank()) emptyList() else this.accessibility.split(",").map { it.trim() },
        photos = if (this.photos.isBlank()) emptyList() else this.photos.split(",").map { it.trim() },
        rating = this.rating,
        reviewCount = this.reviewCount.toInt(),
        isAccessible = this.isAccessible == 1L,
        hasShade = this.hasShade == 1L,
        hasWater = this.hasWater == 1L,
        hasFood = this.hasFood == 1L,
        hasToilets = this.hasToilets == 1L,
        hasMedical = this.hasMedical == 1L,
        hasCharging = this.hasCharging == 1L,
        hasWifi = this.hasWifi == 1L,
        isEmergency = this.isEmergency == 1L,
        emergencyType = this.emergencyType?.let { io.asterixorobelix.afrikaburn.domain.model.EmergencyType.valueOf(it) },
        capacity = this.capacity?.toInt(),
        currentOccupancy = this.currentOccupancy?.toInt(),
        status = io.asterixorobelix.afrikaburn.domain.model.PinStatus.valueOf(this.status),
        lastVerified = this.lastVerified,
        reportedIssues = if (this.reportedIssues.isBlank()) emptyList() else this.reportedIssues.split(",").map { it.trim() },
        distanceFromCenter = this.distanceFromCenter,
        walkingTimeMinutes = this.walkingTimeMinutes?.toInt(),
        isBookmarked = this.isBookmarked == 1L,
        visitCount = this.visitCount.toInt(),
        lastVisited = this.lastVisited,
        createdBy = this.createdBy,
        createdAt = this.createdAt,
        lastUpdated = this.lastUpdated
    )
}

private fun DatabaseCampLocation.toCampLocation(): CampLocation {
    return CampLocation(
        id = this.id,
        deviceId = this.deviceId,
        name = this.name,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        altitude = this.altitude,
        accuracy = this.accuracy.toFloat(),
        isUserMarked = this.isUserMarked == 1L,
        isOfficial = this.isOfficial == 1L,
        locationType = io.asterixorobelix.afrikaburn.domain.model.LocationType.valueOf(this.locationType),
        address = this.address,
        whatThreeWords = this.whatThreeWords,
        campType = this.campType?.let { io.asterixorobelix.afrikaburn.domain.model.CampType.valueOf(it) },
        capacity = this.capacity?.toInt(),
        amenities = if (this.amenities.isBlank()) emptyList() else this.amenities.split(",").map { it.trim() },
        contacts = if (this.contacts.isBlank()) emptyList() else this.contacts.split(",").map { it.trim() },
        operatingHours = this.operatingHours,
        isActive = this.isActive == 1L,
        isShared = this.isShared == 1L,
        shareCode = this.shareCode,
        sharedByDeviceId = this.sharedByDeviceId,
        privacyLevel = io.asterixorobelix.afrikaburn.domain.model.PrivacyLevel.valueOf(this.privacyLevel),
        tags = if (this.tags.isBlank()) emptyList() else this.tags.split(",").map { it.trim() },
        notes = this.notes,
        photos = if (this.photos.isBlank()) emptyList() else this.photos.split(",").map { it.trim() },
        lastVisited = this.lastVisited,
        visitCount = this.visitCount.toInt(),
        isEmergencyLocation = this.isEmergencyLocation == 1L,
        emergencyType = this.emergencyType?.let { io.asterixorobelix.afrikaburn.domain.model.EmergencyType.valueOf(it) },
        createdAt = this.createdAt,
        lastUpdated = this.lastUpdated
    )
}

/**
 * Calculate distance between two points using Haversine formula
 */
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Earth's radius in km
    val dLat = kotlin.math.toRadians(lat2 - lat1)
    val dLon = kotlin.math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(kotlin.math.toRadians(lat1)) * kotlin.math.cos(kotlin.math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return R * c
}

/**
 * Placeholder database entities
 * These would be generated by SQLDelight from the actual schema
 */
data class DatabaseOfflineMap(
    val id: String,
    val name: String = "Offline Map",
    val description: String? = null,
    val mapType: String = "SATELLITE",
    val zoomLevelsStart: Long = 10,
    val zoomLevelsEnd: Long = 18,
    val boundingBoxNorth: Double = -30.0,
    val boundingBoxSouth: Double = -35.0,
    val boundingBoxEast: Double = 20.0,
    val boundingBoxWest: Double = 15.0,
    val centerLatitude: Double = -32.5,
    val centerLongitude: Double = 17.5,
    val isEventMap: Long = 1,
    val isOfficial: Long = 1,
    val tileCount: Long = 1000,
    val totalSizeBytes: Long = 50_000_000L,
    val compressionRatio: Double = 0.7,
    val downloadStatus: String = "NOT_DOWNLOADED",
    val downloadProgress: Double = 0.0,
    val downloadSpeed: Double? = null,
    val estimatedDownloadTime: Long? = null,
    val lastDownloadAttempt: Long? = null,
    val downloadUrl: String? = "https://example.com/map.zip",
    val checksum: String? = "abc123",
    val version: String = "1.0",
    val priority: String = "HIGH",
    val isAutoDownload: Long = 0,
    val requiresWifi: Long = 1,
    val expirationTime: Long? = null,
    val lastUsed: Long? = null,
    val useCount: Long = 0,
    val isCorrupted: Long = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class DatabaseMapPin(
    val id: String,
    val title: String = "Map Pin",
    val subtitle: String? = "Location marker",
    val description: String? = "Generated map pin",
    val latitude: Double = -32.5,
    val longitude: Double = 17.5,
    val altitude: Double? = null,
    val pinType: String = "LOCATION",
    val category: String = "GENERAL",
    val iconName: String = "location_pin",
    val iconColor: String = "#FF0000",
    val backgroundColor: String = "#FFFFFF",
    val isOfficial: Long = 1,
    val isUserCreated: Long = 0,
    val isVisible: Long = 1,
    val zoomLevelRangeStart: Long? = 10,
    val zoomLevelRangeEnd: Long? = 18,
    val priority: String = "MEDIUM",
    val relatedContentId: String? = null,
    val relatedContentType: String? = null,
    val address: String? = null,
    val operatingHours: String? = null,
    val contact: String? = null,
    val website: String? = null,
    val socialMedia: String = "", // Comma-separated
    val tags: String = "", // Comma-separated
    val amenities: String = "", // Comma-separated
    val accessibility: String = "", // Comma-separated
    val photos: String = "", // Comma-separated
    val rating: Double? = null,
    val reviewCount: Long = 0,
    val isAccessible: Long = 0,
    val hasShade: Long = 0,
    val hasWater: Long = 0,
    val hasFood: Long = 0,
    val hasToilets: Long = 0,
    val hasMedical: Long = 0,
    val hasCharging: Long = 0,
    val hasWifi: Long = 0,
    val isEmergency: Long = 0,
    val emergencyType: String? = null,
    val capacity: Long? = null,
    val currentOccupancy: Long? = null,
    val status: String = "ACTIVE",
    val lastVerified: Long? = null,
    val reportedIssues: String = "", // Comma-separated
    val distanceFromCenter: Double = 0.0,
    val walkingTimeMinutes: Long? = null,
    val isBookmarked: Long = 0,
    val visitCount: Long = 0,
    val lastVisited: Long? = null,
    val createdBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class DatabaseCampLocation(
    val id: String,
    val deviceId: String = "device123",
    val name: String = "Camp Location",
    val description: String? = "Generated camp location",
    val latitude: Double = -32.5,
    val longitude: Double = 17.5,
    val altitude: Double? = null,
    val accuracy: Double = 5.0,
    val isUserMarked: Long = 1,
    val isOfficial: Long = 0,
    val locationType: String = "CAMP",
    val address: String? = null,
    val whatThreeWords: String? = null,
    val campType: String? = "TENT",
    val capacity: Long? = null,
    val amenities: String = "", // Comma-separated
    val contacts: String = "", // Comma-separated
    val operatingHours: String? = null,
    val isActive: Long = 1,
    val isShared: Long = 0,
    val shareCode: String? = null,
    val sharedByDeviceId: String? = null,
    val privacyLevel: String = "PRIVATE",
    val tags: String = "", // Comma-separated
    val notes: String? = null,
    val photos: String = "", // Comma-separated
    val lastVisited: Long? = null,
    val visitCount: Long = 0,
    val isEmergencyLocation: Long = 0,
    val emergencyType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)