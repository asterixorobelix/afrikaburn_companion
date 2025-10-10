package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.ThemeCamp
import io.asterixorobelix.afrikaburn.domain.repository.ThemeCampRepository
import io.asterixorobelix.afrikaburn.data.local.ThemeCampQueries
import io.asterixorobelix.afrikaburn.data.remote.ThemeCampApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Database entity for ThemeCamp
 */
data class DatabaseThemeCamp(
    val id: String,
    val eventId: String,
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val contactInfo: String?,
    val activities: String?,
    val amenities: String?,
    val qrCode: String?,
    val photoUrl: String?,
    val isHidden: Long,
    val unlockTimestamp: Long?,
    val lastUpdated: Long
)

/**
 * Implementation of ThemeCampRepository with offline caching
 * 
 * Handles theme camp data with location-based content unlocking,
 * intelligent caching, and full-text search capabilities.
 */
class ThemeCampRepositoryImpl(
    private val themeCampQueries: ThemeCampQueries,
    private val themeCampApi: ThemeCampApi
) : ThemeCampRepository {
    
    companion object {
        private const val CACHE_EXPIRY_HOURS = 12 // Theme camps update more frequently during event
        private const val CACHE_EXPIRY_MS = CACHE_EXPIRY_HOURS * 60 * 60 * 1000L
        private const val SEARCH_MIN_LENGTH = 2
    }
    
    override suspend fun getThemeCampsByEvent(eventId: String): List<ThemeCamp> = withContext(Dispatchers.IO) {
        themeCampQueries.selectThemeCampsByEvent(eventId).executeAsList().map { it.toThemeCamp() }
    }
    
    override suspend fun getThemeCampById(campId: String): ThemeCamp? = withContext(Dispatchers.IO) {
        themeCampQueries.selectThemeCampById(campId).executeAsOneOrNull()?.toThemeCamp()
    }
    
    override fun observeThemeCampsByEvent(eventId: String): Flow<List<ThemeCamp>> {
        return themeCampQueries.selectThemeCampsByEvent(eventId).mapToList().map { list ->
            list.map { it.toThemeCamp() }
        }
    }
    
    override suspend fun getVisibleThemeCamps(
        eventId: String,
        currentTimestamp: Long,
        userLatitude: Double?,
        userLongitude: Double?
    ): List<ThemeCamp> = withContext(Dispatchers.IO) {
        val allCamps = getThemeCampsByEvent(eventId)
        
        allCamps.filter { camp ->
            camp.isVisibleAt(currentTimestamp, userLatitude, userLongitude)
        }
    }
    
    override suspend fun searchThemeCamps(eventId: String, query: String): List<ThemeCamp> = withContext(Dispatchers.IO) {
        if (query.length < SEARCH_MIN_LENGTH) {
            return@withContext emptyList()
        }
        
        themeCampQueries.searchThemeCamps(eventId, "%$query%").executeAsList().map { it.toThemeCamp() }
    }
    
    override suspend fun getThemeCampsByActivities(
        eventId: String,
        activities: List<String>
    ): List<ThemeCamp> = withContext(Dispatchers.IO) {
        val allCamps = getThemeCampsByEvent(eventId)
        
        allCamps.filter { camp ->
            activities.any { activity ->
                camp.hasActivity(activity)
            }
        }
    }
    
    override suspend fun getThemeCampsByAmenities(
        eventId: String,
        amenities: List<String>
    ): List<ThemeCamp> = withContext(Dispatchers.IO) {
        val allCamps = getThemeCampsByEvent(eventId)
        
        allCamps.filter { camp ->
            amenities.any { amenity ->
                camp.hasAmenity(amenity)
            }
        }
    }
    
    override suspend fun getThemeCampsNearLocation(
        eventId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<ThemeCamp> = withContext(Dispatchers.IO) {
        val allCamps = getThemeCampsByEvent(eventId)
        
        allCamps.filter { camp ->
            camp.isNearLocation(latitude, longitude, radiusKm)
        }.sortedBy { camp ->
            camp.getDistanceFrom(latitude, longitude)
        }
    }
    
    override suspend fun saveThemeCamp(themeCamp: ThemeCamp) = withContext(Dispatchers.IO) {
        themeCampQueries.insertThemeCamp(
            id = themeCamp.id,
            eventId = themeCamp.eventId,
            name = themeCamp.name,
            description = themeCamp.description,
            contactEmail = themeCamp.contactInfo, // Using contactInfo as email for now
            websiteUrl = null, // No website URL in domain model
            latitude = themeCamp.latitude,
            longitude = themeCamp.longitude,
            photoUrls = themeCamp.photoUrl ?: "", // Single photo URL as string
            lastUpdated = themeCamp.lastUpdated
        )
    }
    
    override suspend fun saveThemeCamps(themeCamps: List<ThemeCamp>) = withContext(Dispatchers.IO) {
        themeCamps.forEach { camp ->
            saveThemeCamp(camp)
        }
    }
    
    override suspend fun updateThemeCamp(themeCamp: ThemeCamp) = withContext(Dispatchers.IO) {
        themeCampQueries.updateThemeCamp(
            name = themeCamp.name,
            description = themeCamp.description,
            contactEmail = themeCamp.contactInfo, // Using contactInfo as email for now
            websiteUrl = null, // No website URL in domain model
            latitude = themeCamp.latitude,
            longitude = themeCamp.longitude,
            photoUrls = themeCamp.photoUrl ?: "", // Single photo URL as string
            lastUpdated = themeCamp.lastUpdated,
            id = themeCamp.id
        )
    }
    
    override suspend fun deleteThemeCamp(campId: String) = withContext(Dispatchers.IO) {
        themeCampQueries.deleteThemeCamp(campId)
    }
    
    override suspend fun deleteThemeCampsByEvent(eventId: String) = withContext(Dispatchers.IO) {
        themeCampQueries.deleteThemeCampsByEvent(eventId)
    }
    
    override suspend fun syncThemeCamps(eventId: String, forceRefresh: Boolean): Result<List<ThemeCamp>> = withContext(Dispatchers.IO) {
        try {
            // Check if we need to sync
            if (!forceRefresh && !isDataStale(eventId)) {
                return@withContext Result.success(getThemeCampsByEvent(eventId))
            }
            
            // Fetch theme camps from remote API
            val remoteCamps = themeCampApi.getThemeCamps(eventId)
            
            // Save to local database (replace existing)
            deleteThemeCampsByEvent(eventId)
            saveThemeCamps(remoteCamps)
            
            // Update sync timestamp
            updateLastSyncTimestamp(eventId, getCurrentTimestamp())
            
            Result.success(remoteCamps)
        } catch (exception: Exception) {
            // Return cached data if available, otherwise return error
            val cachedCamps = getThemeCampsByEvent(eventId)
            if (cachedCamps.isNotEmpty()) {
                Result.success(cachedCamps)
            } else {
                Result.failure(exception)
            }
        }
    }
    
    override suspend fun getThemeCampByQrCode(qrCode: String): ThemeCamp? = withContext(Dispatchers.IO) {
        themeCampQueries.selectThemeCampByQrCode(qrCode).executeAsOneOrNull()?.toThemeCamp()
    }
    
    override suspend fun getAllActivities(eventId: String): List<String> = withContext(Dispatchers.IO) {
        val allCamps = getThemeCampsByEvent(eventId)
        allCamps.flatMap { it.activities }.distinct().sorted()
    }
    
    override suspend fun getAllAmenities(eventId: String): List<String> = withContext(Dispatchers.IO) {
        val allCamps = getThemeCampsByEvent(eventId)
        allCamps.flatMap { it.amenities }.distinct().sorted()
    }
    
    override suspend fun isDataStale(eventId: String): Boolean = withContext(Dispatchers.IO) {
        val lastSync = getLastSyncTimestamp(eventId)
        val currentTime = getCurrentTimestamp()
        (currentTime - lastSync) > CACHE_EXPIRY_MS
    }
    
    override suspend fun getLastSyncTimestamp(eventId: String): Long = withContext(Dispatchers.IO) {
        themeCampQueries.selectLastSyncTimestamp(eventId).executeAsOneOrNull() ?: 0L
    }
    
    override suspend fun updateLastSyncTimestamp(eventId: String, timestamp: Long) = withContext(Dispatchers.IO) {
        themeCampQueries.insertOrUpdateSyncTimestamp(eventId, timestamp)
    }
    
    override suspend fun getServiceThemeCamps(eventId: String): List<ThemeCamp> = withContext(Dispatchers.IO) {
        val allCamps = getThemeCampsByEvent(eventId)
        allCamps.filter { it.isServiceCamp() }
    }
    
    override suspend fun getFavoriteCampIds(): Set<String> = withContext(Dispatchers.IO) {
        // Using preferences or a simple table to store favorites
        themeCampQueries.selectFavoriteCampIds().executeAsList().toSet()
    }
    
    override suspend fun addFavoriteCamp(campId: String) = withContext(Dispatchers.IO) {
        themeCampQueries.insertFavoriteCamp(campId)
    }
    
    override suspend fun removeFavoriteCamp(campId: String) = withContext(Dispatchers.IO) {
        themeCampQueries.deleteFavoriteCamp(campId)
    }
    
    /**
     * Get current timestamp
     */
    private fun getCurrentTimestamp(): Long = System.currentTimeMillis()
}

/**
 * Extension function to convert database theme camp to domain model
 */
private fun DatabaseThemeCamp.toThemeCamp(): ThemeCamp {
    return ThemeCamp(
        id = id,
        eventId = eventId,
        name = name,
        description = description,
        latitude = latitude,
        longitude = longitude,
        contactInfo = contactInfo,
        activities = if (activities.isNullOrBlank()) emptyList() else activities.split(",").map { it.trim() },
        amenities = if (amenities.isNullOrBlank()) emptyList() else amenities.split(",").map { it.trim() },
        qrCode = qrCode,
        photoUrl = photoUrl,
        isHidden = isHidden == 1L,
        unlockTimestamp = unlockTimestamp,
        lastUpdated = lastUpdated
    )
}

