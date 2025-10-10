package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.domain.model.ThemeCamp
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for ThemeCamp operations
 * 
 * Handles theme camp data with offline caching and location-based content unlocking.
 * Supports filtering by activities, amenities, and proximity to user location.
 */
interface ThemeCampRepository {
    
    /**
     * Get all theme camps for an event
     */
    suspend fun getThemeCampsByEvent(eventId: String): List<ThemeCamp>
    
    /**
     * Get theme camp by ID
     */
    suspend fun getThemeCampById(campId: String): ThemeCamp?
    
    /**
     * Observe theme camps for an event as Flow
     */
    fun observeThemeCampsByEvent(eventId: String): Flow<List<ThemeCamp>>
    
    /**
     * Get visible theme camps (respecting unlock rules)
     * @param eventId the event ID
     * @param currentTimestamp current time for unlock logic
     * @param userLatitude user's current latitude (null if location unavailable)
     * @param userLongitude user's current longitude (null if location unavailable)
     */
    suspend fun getVisibleThemeCamps(
        eventId: String,
        currentTimestamp: Long,
        userLatitude: Double? = null,
        userLongitude: Double? = null
    ): List<ThemeCamp>
    
    /**
     * Search theme camps by name, activities, or amenities
     */
    suspend fun searchThemeCamps(
        eventId: String,
        query: String
    ): List<ThemeCamp>
    
    /**
     * Get theme camps by activities
     */
    suspend fun getThemeCampsByActivities(
        eventId: String,
        activities: List<String>
    ): List<ThemeCamp>
    
    /**
     * Get theme camps by amenities
     */
    suspend fun getThemeCampsByAmenities(
        eventId: String,
        amenities: List<String>
    ): List<ThemeCamp>
    
    /**
     * Get theme camps within radius of location
     */
    suspend fun getThemeCampsNearLocation(
        eventId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 1.0
    ): List<ThemeCamp>
    
    /**
     * Save theme camp to local storage
     */
    suspend fun saveThemeCamp(themeCamp: ThemeCamp)
    
    /**
     * Save multiple theme camps (batch operation)
     */
    suspend fun saveThemeCamps(themeCamps: List<ThemeCamp>)
    
    /**
     * Update theme camp
     */
    suspend fun updateThemeCamp(themeCamp: ThemeCamp)
    
    /**
     * Delete theme camp by ID
     */
    suspend fun deleteThemeCamp(campId: String)
    
    /**
     * Delete all theme camps for an event
     */
    suspend fun deleteThemeCampsByEvent(eventId: String)
    
    /**
     * Sync theme camps from remote source
     * @param eventId the event to sync
     * @param forceRefresh if true, bypass cache and fetch from network
     */
    suspend fun syncThemeCamps(eventId: String, forceRefresh: Boolean = false): Result<List<ThemeCamp>>
    
    /**
     * Get theme camps by QR code
     */
    suspend fun getThemeCampByQrCode(qrCode: String): ThemeCamp?
    
    /**
     * Get all unique activities across all theme camps
     */
    suspend fun getAllActivities(eventId: String): List<String>
    
    /**
     * Get all unique amenities across all theme camps
     */
    suspend fun getAllAmenities(eventId: String): List<String>
    
    /**
     * Check if theme camp data is stale for an event
     */
    suspend fun isDataStale(eventId: String): Boolean
    
    /**
     * Get last sync timestamp for an event
     */
    suspend fun getLastSyncTimestamp(eventId: String): Long
    
    /**
     * Update last sync timestamp for an event
     */
    suspend fun updateLastSyncTimestamp(eventId: String, timestamp: Long)
    
    /**
     * Get theme camps that provide essential services
     */
    suspend fun getServiceThemeCamps(eventId: String): List<ThemeCamp>
    
    /**
     * Get favorite theme camp IDs
     */
    suspend fun getFavoriteCampIds(): Set<String>
    
    /**
     * Add theme camp to favorites
     */
    suspend fun addFavoriteCamp(campId: String)
    
    /**
     * Remove theme camp from favorites
     */
    suspend fun removeFavoriteCamp(campId: String)
}