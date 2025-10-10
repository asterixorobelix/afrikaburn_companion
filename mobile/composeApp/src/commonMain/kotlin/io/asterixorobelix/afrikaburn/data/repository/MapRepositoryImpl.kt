package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.OfflineMap
import io.asterixorobelix.afrikaburn.domain.model.OfflineMapType
import io.asterixorobelix.afrikaburn.domain.model.MapBoundingBox
import io.asterixorobelix.afrikaburn.domain.model.MapPin
import io.asterixorobelix.afrikaburn.domain.model.MapPinType
import io.asterixorobelix.afrikaburn.domain.model.PinCategory
import io.asterixorobelix.afrikaburn.domain.model.PinPriority
import io.asterixorobelix.afrikaburn.domain.model.PinStatus
import io.asterixorobelix.afrikaburn.domain.model.EmergencyServiceType
import io.asterixorobelix.afrikaburn.domain.model.CampLocation
import io.asterixorobelix.afrikaburn.domain.model.CampLocationType
import io.asterixorobelix.afrikaburn.domain.model.EmergencyType
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Database entity for OfflineMap
 */
data class DatabaseOfflineMap(
    val id: String,
    val eventId: String,
    val boundaryNorthLat: Double,
    val boundarySouthLat: Double,
    val boundaryEastLng: Double,
    val boundaryWestLng: Double,
    val zoomLevels: String,
    val tileStoragePath: String,
    val sizeBytes: Long,
    val lastUpdated: Long
)

/**
 * Database entity for MapPin
 */
data class DatabaseMapPin(
    val id: String,
    val mapId: String,
    val contentType: String,
    val contentId: String,
    val latitude: Double,
    val longitude: Double,
    val iconType: String,
    val layerId: String,
    val isVisible: Long,
    val priority: Long
)

/**
 * Database entity for CampLocation
 */
data class DatabaseCampLocation(
    val id: String,
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val name: String?,
    val notes: String?,
    val markedTimestamp: Long,
    val isActive: Long
)

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
    }
    
    // Offline Maps
    
    override suspend fun getAllOfflineMaps(): List<OfflineMap> = withContext(Dispatchers.Default) {
        offlineMapQueries.selectAllOfflineMaps().executeAsList().map { it.toOfflineMap() }
    }
    
    override suspend fun getOfflineMapById(mapId: String): OfflineMap? = withContext(Dispatchers.Default) {
        offlineMapQueries.selectOfflineMapById(mapId).executeAsOneOrNull()?.toOfflineMap()
    }
    
    override suspend fun getOfflineMapsByEvent(eventId: String): List<OfflineMap> = withContext(Dispatchers.Default) {
        offlineMapQueries.selectOfflineMapsByEvent(eventId).executeAsList().map { it.toOfflineMap() }
    }
    
    override fun observeOfflineMaps(): Flow<List<OfflineMap>> {
        return flow {
            val maps = offlineMapQueries.selectAllOfflineMaps().executeAsList()
            emit(maps.map { it.toOfflineMap() })
        }
    }
    
    override suspend fun saveOfflineMap(offlineMap: OfflineMap) = withContext(Dispatchers.Default) {
        offlineMapQueries.insertOfflineMap(
            id = offlineMap.id,
            eventId = "default_event", // TODO: Get from context or offlineMap
            boundaryNorthLat = offlineMap.boundingBox.north,
            boundarySouthLat = offlineMap.boundingBox.south,
            boundaryEastLng = offlineMap.boundingBox.east,
            boundaryWestLng = offlineMap.boundingBox.west,
            zoomLevels = (offlineMap.zoomLevels.first..offlineMap.zoomLevels.last).joinToString(","),
            tileStoragePath = "maps/${offlineMap.id}", // Default path
            sizeBytes = offlineMap.totalSizeBytes,
            lastUpdated = offlineMap.lastUpdated
        )
    }
    
    override suspend fun updateOfflineMap(offlineMap: OfflineMap) = withContext(Dispatchers.Default) {
        offlineMapQueries.updateOfflineMap(
            eventId = "default_event", // TODO: Get from context or offlineMap
            boundaryNorthLat = offlineMap.boundingBox.north,
            boundarySouthLat = offlineMap.boundingBox.south,
            boundaryEastLng = offlineMap.boundingBox.east,
            boundaryWestLng = offlineMap.boundingBox.west,
            zoomLevels = (offlineMap.zoomLevels.first..offlineMap.zoomLevels.last).joinToString(","),
            tileStoragePath = "maps/${offlineMap.id}", // Default path
            sizeBytes = offlineMap.totalSizeBytes,
            lastUpdated = offlineMap.lastUpdated,
            id = offlineMap.id
        )
    }
    
    override suspend fun deleteOfflineMap(mapId: String) = withContext(Dispatchers.Default) {
        offlineMapQueries.deleteOfflineMap(mapId)
    }
    
    override suspend fun downloadOfflineMap(mapId: String, priority: Int): Flow<DownloadProgress> = flow {
        emit(DownloadProgress(mapId, 0f, 0L, 100_000_000L, DownloadStatus.PENDING))
        // Mock download progress
        for (progress in 1..100 step 10) {
            emit(DownloadProgress(mapId, progress / 100f, (progress * 1_000_000L), 100_000_000L, DownloadStatus.DOWNLOADING))
            kotlinx.coroutines.delay(100)
        }
        emit(DownloadProgress(mapId, 1f, 100_000_000L, 100_000_000L, DownloadStatus.COMPLETED))
    }
    
    override suspend fun isOfflineMapDownloaded(mapId: String): Boolean = withContext(Dispatchers.Default) {
        false // Mock implementation
    }
    
    override suspend fun getDownloadProgress(mapId: String): Float = withContext(Dispatchers.Default) {
        0f // Mock implementation
    }
    
    override suspend fun cancelDownload(mapId: String) = withContext(Dispatchers.Default) {
        // Mock implementation
    }
    
    // Map Pins
    
    override suspend fun getMapPinsByMap(mapId: String): List<MapPin> = withContext(Dispatchers.Default) {
        mapPinQueries.selectMapPinsByMap(mapId).executeAsList().map { it.toMapPin() }
    }
    
    override suspend fun getMapPinsByContentType(mapId: String, contentType: String): List<MapPin> = withContext(Dispatchers.Default) {
        mapPinQueries.selectMapPinsByContentType(mapId, contentType).executeAsList().map { it.toMapPin() }
    }
    
    override suspend fun getMapPinsNearLocation(
        mapId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<MapPin> = withContext(Dispatchers.Default) {
        mapPinQueries.selectMapPinsNearLocation(mapId, latitude, longitude, radiusKm).executeAsList().map { it.toMapPin() }
    }
    
    override suspend fun saveMapPin(mapPin: MapPin) = withContext(Dispatchers.Default) {
        mapPinQueries.insertMapPin(
            id = mapPin.id,
            mapId = "default", // TODO: Add mapId to MapPin domain model or get from context
            contentType = mapPin.relatedContentType ?: "unknown",
            contentId = mapPin.relatedContentId ?: "",
            latitude = mapPin.latitude,
            longitude = mapPin.longitude,
            iconType = mapPin.iconName ?: "default",
            layerId = mapPin.category.name, // Use category as layer ID
            isVisible = if (mapPin.isVisible) 1L else 0L,
            priority = mapPin.priority.value.toLong()
        )
    }
    
    override suspend fun saveMapPins(mapPins: List<MapPin>) = withContext(Dispatchers.Default) {
        mapPinQueries.transaction {
            mapPins.forEach { pin ->
                mapPinQueries.insertMapPin(
                    id = pin.id,
                    mapId = "default", // TODO: Add mapId to MapPin domain model or get from context
                    contentType = pin.relatedContentType ?: "unknown",
                    contentId = pin.relatedContentId ?: "",
                    latitude = pin.latitude,
                    longitude = pin.longitude,
                    iconType = pin.iconName ?: "default",
                    layerId = pin.category.name, // Use category as layer ID
                    isVisible = if (pin.isVisible) 1L else 0L,
                    priority = pin.priority.value.toLong()
                )
            }
        }
    }
    
    override suspend fun updateMapPin(mapPin: MapPin) = withContext(Dispatchers.Default) {
        mapPinQueries.updateMapPin(
            mapId = "default", // TODO: Add mapId to MapPin domain model or get from context
            contentType = mapPin.relatedContentType ?: "unknown",
            contentId = mapPin.relatedContentId ?: "",
            latitude = mapPin.latitude,
            longitude = mapPin.longitude,
            iconType = mapPin.iconName ?: "default",
            layerId = mapPin.category.name, // Use category as layer ID
            isVisible = if (mapPin.isVisible) 1L else 0L,
            priority = mapPin.priority.value.toLong(),
            id = mapPin.id
        )
    }
    
    override suspend fun deleteMapPin(pinId: String) = withContext(Dispatchers.Default) {
        mapPinQueries.deleteMapPin(pinId)
    }
    
    override suspend fun deleteMapPinsByContentId(contentId: String) = withContext(Dispatchers.Default) {
        mapPinQueries.deleteMapPinsByContentId(contentId)
    }
    
    // Camp Locations
    
    override suspend fun getCampLocationsByDevice(deviceId: String): List<CampLocation> = withContext(Dispatchers.Default) {
        campLocationQueries.selectCampLocationsByDevice(deviceId).executeAsList().map { it.toCampLocation() }
    }
    
    override suspend fun getCampLocationById(locationId: String): CampLocation? = withContext(Dispatchers.Default) {
        campLocationQueries.selectCampLocationById(locationId).executeAsOneOrNull()?.toCampLocation()
    }
    
    override fun observeCampLocationsByDevice(deviceId: String): Flow<List<CampLocation>> {
        return flow {
            val locations = campLocationQueries.selectCampLocationsByDevice(deviceId).executeAsList()
            emit(locations.map { it.toCampLocation() })
        }
    }
    
    override suspend fun saveCampLocation(campLocation: CampLocation) = withContext(Dispatchers.Default) {
        campLocationQueries.insertCampLocation(
            id = campLocation.id,
            deviceId = campLocation.deviceId ?: "", // Provide empty string if null
            latitude = campLocation.latitude,
            longitude = campLocation.longitude,
            name = campLocation.name,
            notes = campLocation.notes,
            markedTimestamp = campLocation.createdAt,
            isActive = if (campLocation.isActive) 1L else 0L
        )
    }
    
    override suspend fun updateCampLocation(campLocation: CampLocation) = withContext(Dispatchers.Default) {
        campLocationQueries.updateCampLocation(
            deviceId = campLocation.deviceId ?: "", // Provide empty string if null
            latitude = campLocation.latitude,
            longitude = campLocation.longitude,
            name = campLocation.name,
            notes = campLocation.notes,
            markedTimestamp = campLocation.createdAt,
            isActive = if (campLocation.isActive) 1L else 0L,
            id = campLocation.id
        )
    }
    
    override suspend fun deleteCampLocation(locationId: String) = withContext(Dispatchers.Default) {
        campLocationQueries.deleteCampLocation(locationId)
    }
    
    override suspend fun getActiveCampLocation(deviceId: String): CampLocation? = withContext(Dispatchers.Default) {
        campLocationQueries.selectActiveCampLocation(deviceId).executeAsOneOrNull()?.toCampLocation()
    }
    
    override suspend fun setActiveCampLocation(locationId: String) = withContext(Dispatchers.Default) {
        campLocationQueries.setActiveCampLocation(locationId)
    }
    
    // Storage Management
    
    override suspend fun getTotalMapStorageBytes(): Long = withContext(Dispatchers.Default) {
        tileStorageService.getTotalStorageUsed()
    }
    
    override suspend fun cleanupMapStorage(targetSizeBytes: Long): Long = withContext(Dispatchers.Default) {
        tileStorageService.cleanupOldTiles(7) // Keep last 7 days
    }
    
    override suspend fun getMapStorageStats(): MapStorageStats = withContext(Dispatchers.Default) {
        val totalSize = getTotalMapStorageBytes()
        val mapCount = getAllOfflineMaps().size
        
        MapStorageStats(
            totalSizeBytes = totalSize,
            tilesCount = 0L, // Mock value
            mapsCount = mapCount,
            lastCleanupTimestamp = getCurrentTimestamp()
        )
    }
    
    override suspend fun getAvailableStorageBytes(): Long = withContext(Dispatchers.Default) {
        val usedBytes = getTotalMapStorageBytes()
        MAX_TOTAL_MAP_STORAGE_BYTES - usedBytes
    }
    
    override suspend fun syncMapData(eventId: String, forceRefresh: Boolean): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            if (!forceRefresh) {
                val maps = getOfflineMapsByEvent(eventId)
                if (maps.isNotEmpty()) {
                    return@withContext Result.success(Unit)
                }
            }
            
            val mapData = mapApi.getOfflineMaps(eventId)
            mapData.forEach { map ->
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
    ): Boolean = withContext(Dispatchers.Default) {
        true // Mock validation - always passes
    }
}

/**
 * Extension functions to convert database entities to domain models
 */
private fun DatabaseOfflineMap.toOfflineMap(): OfflineMap {
    val zoomRange = if (zoomLevels.isBlank()) {
        10..18 // Default zoom range
    } else {
        val levels = zoomLevels.split(",").map { it.toInt() }
        levels.minOrNull()!!..levels.maxOrNull()!!
    }
    
    return OfflineMap(
        id = id,
        name = "Event Map ${eventId}", // Default name
        description = "Offline map for event $eventId",
        mapType = OfflineMapType.EVENT_LAYOUT,
        zoomLevels = zoomRange,
        boundingBox = MapBoundingBox(
            north = boundaryNorthLat,
            south = boundarySouthLat,
            east = boundaryEastLng,
            west = boundaryWestLng
        ),
        centerLatitude = (boundaryNorthLat + boundarySouthLat) / 2,
        centerLongitude = (boundaryEastLng + boundaryWestLng) / 2,
        tileCount = 0, // Would need to be calculated
        totalSizeBytes = sizeBytes,
        compressionRatio = null,
        downloadUrl = null,
        checksum = null,
        version = "1.0",
        downloadSpeed = null,
        estimatedDownloadTime = null,
        lastDownloadAttempt = null,
        expirationTime = null,
        lastUsed = null,
        errorMessage = null,
        createdAt = lastUpdated,
        lastUpdated = lastUpdated
    )
}

private fun DatabaseMapPin.toMapPin(): MapPin {
    return MapPin(
        id = id,
        title = "Location", // Default title
        subtitle = null,
        description = null,
        latitude = latitude,
        longitude = longitude,
        altitude = null,
        pinType = MapPinType.STANDARD, // TODO: Map from iconType
        category = PinCategory.OTHER, // TODO: Map from contentType
        iconName = iconType,
        iconColor = null,
        backgroundColor = null,
        isOfficial = false,
        isUserCreated = false,
        isVisible = isVisible == 1L,
        zoomLevelRange = null,
        priority = when(priority.toInt()) {
            100 -> PinPriority.CRITICAL
            75 -> PinPriority.HIGH
            50 -> PinPriority.NORMAL
            25 -> PinPriority.LOW
            else -> PinPriority.NORMAL
        },
        relatedContentId = contentId,
        relatedContentType = contentType,
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
        distanceFromCenter = null,
        walkingTimeMinutes = null,
        isBookmarked = false,
        visitCount = 0,
        lastVisited = null,
        createdBy = null,
        createdAt = getCurrentTimestamp(),
        lastUpdated = getCurrentTimestamp()
    )
}

private fun DatabaseCampLocation.toCampLocation(): CampLocation {
    return CampLocation(
        id = id,
        deviceId = deviceId,
        name = name ?: "Camp Location",
        description = null,
        latitude = latitude,
        longitude = longitude,
        altitude = null,
        accuracy = null,
        locationType = CampLocationType.PERSONAL_CAMP,
        address = null,
        whatThreeWords = null,
        campType = null,
        capacity = null,
        operatingHours = null,
        shareCode = null,
        sharedByDeviceId = null,
        notes = notes,
        lastVisited = null,
        emergencyType = null,
        createdAt = markedTimestamp,
        lastUpdated = markedTimestamp,
        isActive = isActive == 1L
    )
}