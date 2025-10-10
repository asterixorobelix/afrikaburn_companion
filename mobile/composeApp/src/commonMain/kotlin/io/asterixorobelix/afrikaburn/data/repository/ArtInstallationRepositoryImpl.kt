package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.ArtInstallation
import io.asterixorobelix.afrikaburn.domain.repository.ArtInstallationRepository
import io.asterixorobelix.afrikaburn.data.local.ArtInstallationQueries
import io.asterixorobelix.afrikaburn.data.remote.ArtInstallationApi
import io.asterixorobelix.afrikaburn.data.storage.ImageCacheService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of ArtInstallationRepository with image caching
 * 
 * Handles art installation data with intelligent image caching,
 * location-based unlocking, and artist information management.
 */
class ArtInstallationRepositoryImpl(
    private val artInstallationQueries: ArtInstallationQueries,
    private val artInstallationApi: ArtInstallationApi,
    private val imageCacheService: ImageCacheService
) : ArtInstallationRepository {
    
    companion object {
        private const val CACHE_EXPIRY_HOURS = 12 // Art installations update during event
        private const val CACHE_EXPIRY_MS = CACHE_EXPIRY_HOURS * 60 * 60 * 1000L
        private const val SEARCH_MIN_LENGTH = 2
        private const val DEFAULT_IMAGE_PRIORITY = 50
    }
    
    override suspend fun getArtInstallationsByEvent(eventId: String): List<ArtInstallation> = withContext(Dispatchers.IO) {
        artInstallationQueries.selectArtInstallationsByEvent(eventId).executeAsList().map { it.toArtInstallation() }
    }
    
    override suspend fun getArtInstallationById(installationId: String): ArtInstallation? = withContext(Dispatchers.IO) {
        artInstallationQueries.selectArtInstallationById(installationId).executeAsOneOrNull()?.toArtInstallation()
    }
    
    override fun observeArtInstallationsByEvent(eventId: String): Flow<List<ArtInstallation>> {
        return artInstallationQueries.selectArtInstallationsByEvent(eventId).mapToList().map { list ->
            list.map { it.toArtInstallation() }
        }
    }
    
    override suspend fun getVisibleArtInstallations(
        eventId: String,
        currentTimestamp: Long,
        userLatitude: Double?,
        userLongitude: Double?
    ): List<ArtInstallation> = withContext(Dispatchers.IO) {
        val allInstallations = getArtInstallationsByEvent(eventId)
        
        allInstallations.filter { installation ->
            installation.isVisibleAt(currentTimestamp, userLatitude, userLongitude)
        }
    }
    
    override suspend fun searchArtInstallations(eventId: String, query: String): List<ArtInstallation> = withContext(Dispatchers.IO) {
        if (query.length < SEARCH_MIN_LENGTH) {
            return@withContext emptyList()
        }
        
        artInstallationQueries.searchArtInstallations(eventId, "%$query%").executeAsList().map { it.toArtInstallation() }
    }
    
    override suspend fun getArtInstallationsByArtist(
        eventId: String,
        artistName: String
    ): List<ArtInstallation> = withContext(Dispatchers.IO) {
        artInstallationQueries.selectArtInstallationsByArtist(eventId, artistName).executeAsList().map { it.toArtInstallation() }
    }
    
    override suspend fun getInteractiveArtInstallations(eventId: String): List<ArtInstallation> = withContext(Dispatchers.IO) {
        val allInstallations = getArtInstallationsByEvent(eventId)
        allInstallations.filter { it.interactiveFeatures.isNotEmpty() }
    }
    
    override suspend fun getArtInstallationsNearLocation(
        eventId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<ArtInstallation> = withContext(Dispatchers.IO) {
        val allInstallations = getArtInstallationsByEvent(eventId)
        
        allInstallations.filter { installation ->
            installation.isNearLocation(latitude, longitude, radiusKm)
        }.sortedBy { installation ->
            installation.getDistanceFrom(latitude, longitude)
        }
    }
    
    override suspend fun getArtInstallationByQrCode(qrCode: String): ArtInstallation? = withContext(Dispatchers.IO) {
        artInstallationQueries.selectArtInstallationByQrCode(qrCode).executeAsOneOrNull()?.toArtInstallation()
    }
    
    override suspend fun saveArtInstallation(artInstallation: ArtInstallation) = withContext(Dispatchers.IO) {
        artInstallationQueries.insertArtInstallation(
            id = artInstallation.id,
            eventId = artInstallation.eventId,
            name = artInstallation.name,
            artistName = artInstallation.artistName,
            description = artInstallation.description,
            latitude = artInstallation.latitude,
            longitude = artInstallation.longitude,
            photoUrls = artInstallation.photoUrls.joinToString(","),
            artistBio = artInstallation.artistBio,
            interactiveFeatures = artInstallation.interactiveFeatures.joinToString(","),
            isHidden = if (artInstallation.isHidden) 1L else 0L,
            unlockTimestamp = artInstallation.unlockTimestamp,
            qrCode = artInstallation.qrCode,
            lastUpdated = artInstallation.lastUpdated
        )
    }
    
    override suspend fun saveArtInstallations(artInstallations: List<ArtInstallation>) = withContext(Dispatchers.IO) {
        artInstallationQueries.transaction {
            artInstallations.forEach { installation ->
                artInstallationQueries.insertArtInstallation(
                    id = installation.id,
                    eventId = installation.eventId,
                    name = installation.name,
                    artistName = installation.artistName,
                    description = installation.description,
                    latitude = installation.latitude,
                    longitude = installation.longitude,
                    photoUrls = installation.photoUrls.joinToString(","),
                    artistBio = installation.artistBio,
                    interactiveFeatures = installation.interactiveFeatures.joinToString(","),
                    isHidden = if (installation.isHidden) 1L else 0L,
                    unlockTimestamp = installation.unlockTimestamp,
                    qrCode = installation.qrCode,
                    lastUpdated = installation.lastUpdated
                )
            }
        }
    }
    
    override suspend fun updateArtInstallation(artInstallation: ArtInstallation) = withContext(Dispatchers.IO) {
        artInstallationQueries.updateArtInstallation(
            name = artInstallation.name,
            artistName = artInstallation.artistName,
            description = artInstallation.description,
            latitude = artInstallation.latitude,
            longitude = artInstallation.longitude,
            photoUrls = artInstallation.photoUrls.joinToString(","),
            artistBio = artInstallation.artistBio,
            interactiveFeatures = artInstallation.interactiveFeatures.joinToString(","),
            isHidden = if (artInstallation.isHidden) 1L else 0L,
            unlockTimestamp = artInstallation.unlockTimestamp,
            qrCode = artInstallation.qrCode,
            lastUpdated = artInstallation.lastUpdated,
            id = artInstallation.id
        )
    }
    
    override suspend fun deleteArtInstallation(installationId: String) = withContext(Dispatchers.IO) {
        // Also remove cached images
        imageCacheService.clearImagesForContent(installationId)
        artInstallationQueries.deleteArtInstallation(installationId)
    }
    
    override suspend fun deleteArtInstallationsByEvent(eventId: String) = withContext(Dispatchers.IO) {
        // Get installation IDs first to clear their images
        val installations = getArtInstallationsByEvent(eventId)
        installations.forEach { installation ->
            imageCacheService.clearImagesForContent(installation.id)
        }
        
        artInstallationQueries.deleteArtInstallationsByEvent(eventId)
    }
    
    override suspend fun syncArtInstallations(eventId: String, forceRefresh: Boolean): Result<List<ArtInstallation>> = withContext(Dispatchers.IO) {
        try {
            // Check if we need to sync
            if (!forceRefresh && !isDataStale(eventId)) {
                return@withContext Result.success(getArtInstallationsByEvent(eventId))
            }
            
            // Fetch art installations from remote API
            val remoteInstallations = artInstallationApi.getArtInstallations(eventId)
            
            // Save to local database (replace existing)
            deleteArtInstallationsByEvent(eventId)
            saveArtInstallations(remoteInstallations)
            
            // Update sync timestamp
            updateLastSyncTimestamp(eventId, getCurrentTimestamp())
            
            Result.success(remoteInstallations)
        } catch (exception: Exception) {
            // Return cached data if available, otherwise return error
            val cachedInstallations = getArtInstallationsByEvent(eventId)
            if (cachedInstallations.isNotEmpty()) {
                Result.success(cachedInstallations)
            } else {
                Result.failure(exception)
            }
        }
    }
    
    override suspend fun downloadImages(installationId: String, priority: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val installation = getArtInstallationById(installationId)
                ?: return@withContext Result.failure(IllegalArgumentException("Installation not found"))
            
            val cachedPaths = mutableListOf<String>()
            
            installation.photoUrls.forEach { url ->
                val cachedPath = imageCacheService.downloadAndCacheImage(
                    url = url,
                    contentId = installationId,
                    priority = priority
                )
                cachedPath?.let { cachedPaths.add(it) }
            }
            
            Result.success(cachedPaths)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    override suspend fun getCachedImagePaths(installationId: String): List<String> = withContext(Dispatchers.IO) {
        imageCacheService.getCachedImagePaths(installationId)
    }
    
    override suspend fun areImagesCached(installationId: String): Boolean = withContext(Dispatchers.IO) {
        val installation = getArtInstallationById(installationId) ?: return@withContext false
        val cachedPaths = getCachedImagePaths(installationId)
        cachedPaths.size == installation.photoUrls.size
    }
    
    override suspend fun getAllArtists(eventId: String): List<String> = withContext(Dispatchers.IO) {
        val allInstallations = getArtInstallationsByEvent(eventId)
        allInstallations.map { it.artistName }.distinct().sorted()
    }
    
    override suspend fun getAllInteractiveFeatures(eventId: String): List<String> = withContext(Dispatchers.IO) {
        val allInstallations = getArtInstallationsByEvent(eventId)
        allInstallations.flatMap { it.interactiveFeatures }.distinct().sorted()
    }
    
    override suspend fun getArtInstallationsWithImages(eventId: String): List<ArtInstallation> = withContext(Dispatchers.IO) {
        val allInstallations = getArtInstallationsByEvent(eventId)
        allInstallations.filter { it.photoUrls.isNotEmpty() }
    }
    
    override suspend fun isDataStale(eventId: String): Boolean = withContext(Dispatchers.IO) {
        val lastSync = getLastSyncTimestamp(eventId)
        val currentTime = getCurrentTimestamp()
        (currentTime - lastSync) > CACHE_EXPIRY_MS
    }
    
    override suspend fun getLastSyncTimestamp(eventId: String): Long = withContext(Dispatchers.IO) {
        artInstallationQueries.selectLastSyncTimestamp(eventId).executeAsOneOrNull() ?: 0L
    }
    
    override suspend fun updateLastSyncTimestamp(eventId: String, timestamp: Long) = withContext(Dispatchers.IO) {
        artInstallationQueries.insertOrUpdateSyncTimestamp(eventId, timestamp)
    }
    
    override suspend fun clearImageCache(keepRecent: Int): Unit = withContext(Dispatchers.IO) {
        imageCacheService.clearOldImages(keepRecent)
    }
    
    override suspend fun getCachedImagesSizeBytes(): Long = withContext(Dispatchers.IO) {
        imageCacheService.getTotalCacheSizeBytes()
    }
    
    /**
     * Get current timestamp
     */
    private fun getCurrentTimestamp(): Long = System.currentTimeMillis()
}

/**
 * Database entity for ArtInstallation
 */
data class DatabaseArtInstallation(
    val id: String,
    val eventId: String,
    val name: String,
    val artistName: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val photoUrls: String?,
    val artistBio: String?,
    val interactiveFeatures: String?,
    val isHidden: Long,
    val unlockTimestamp: Long?,
    val qrCode: String?,
    val lastUpdated: Long
)

/**
 * Extension function to convert database art installation to domain model
 */
private fun DatabaseArtInstallation.toArtInstallation(): ArtInstallation {
    return ArtInstallation(
        id = id,
        eventId = eventId,
        name = name,
        artistName = artistName,
        description = description,
        latitude = latitude,
        longitude = longitude,
        artType = io.asterixorobelix.afrikaburn.domain.model.ArtType.SCULPTURE, // Default value
        dimensions = null, // Not stored in database for now
        accessibilityNotes = null, // Not stored in database for now
        photoUrls = if (photoUrls.isNullOrBlank()) emptyList() else photoUrls.split(",").map { it.trim() },
        artistBio = artistBio,
        interactiveFeatures = if (interactiveFeatures.isNullOrBlank()) emptyList() else interactiveFeatures.split(",").map { it.trim() },
        isHidden = isHidden == 1L,
        unlockTimestamp = unlockTimestamp,
        qrCode = qrCode,
        lastUpdated = lastUpdated
    )
}