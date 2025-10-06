package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.domain.model.ArtInstallation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for ArtInstallation operations
 * 
 * Handles art installation data with image caching, location-based unlocking,
 * and artist information management for the AfrikaBurn event.
 */
interface ArtInstallationRepository {
    
    /**
     * Get all art installations for an event
     */
    suspend fun getArtInstallationsByEvent(eventId: String): List<ArtInstallation>
    
    /**
     * Get art installation by ID
     */
    suspend fun getArtInstallationById(installationId: String): ArtInstallation?
    
    /**
     * Observe art installations for an event as Flow
     */
    fun observeArtInstallationsByEvent(eventId: String): Flow<List<ArtInstallation>>
    
    /**
     * Get visible art installations (respecting unlock rules)
     * @param eventId the event ID
     * @param currentTimestamp current time for unlock logic
     * @param userLatitude user's current latitude (null if location unavailable)
     * @param userLongitude user's current longitude (null if location unavailable)
     */
    suspend fun getVisibleArtInstallations(
        eventId: String,
        currentTimestamp: Long,
        userLatitude: Double? = null,
        userLongitude: Double? = null
    ): List<ArtInstallation>
    
    /**
     * Search art installations by name, artist, or description
     */
    suspend fun searchArtInstallations(
        eventId: String,
        query: String
    ): List<ArtInstallation>
    
    /**
     * Get art installations by artist
     */
    suspend fun getArtInstallationsByArtist(
        eventId: String,
        artistName: String
    ): List<ArtInstallation>
    
    /**
     * Get art installations with interactive features
     */
    suspend fun getInteractiveArtInstallations(eventId: String): List<ArtInstallation>
    
    /**
     * Get art installations within radius of location
     */
    suspend fun getArtInstallationsNearLocation(
        eventId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 1.0
    ): List<ArtInstallation>
    
    /**
     * Get art installations by QR code
     */
    suspend fun getArtInstallationByQrCode(qrCode: String): ArtInstallation?
    
    /**
     * Save art installation to local storage
     */
    suspend fun saveArtInstallation(artInstallation: ArtInstallation)
    
    /**
     * Save multiple art installations (batch operation)
     */
    suspend fun saveArtInstallations(artInstallations: List<ArtInstallation>)
    
    /**
     * Update art installation
     */
    suspend fun updateArtInstallation(artInstallation: ArtInstallation)
    
    /**
     * Delete art installation by ID
     */
    suspend fun deleteArtInstallation(installationId: String)
    
    /**
     * Delete all art installations for an event
     */
    suspend fun deleteArtInstallationsByEvent(eventId: String)
    
    /**
     * Sync art installations from remote source
     * @param eventId the event to sync
     * @param forceRefresh if true, bypass cache and fetch from network
     */
    suspend fun syncArtInstallations(eventId: String, forceRefresh: Boolean = false): Result<List<ArtInstallation>>
    
    /**
     * Download and cache images for art installation
     * @param installationId the installation ID
     * @param priority download priority (higher = download first)
     */
    suspend fun downloadImages(installationId: String, priority: Int = 0): Result<List<String>>
    
    /**
     * Get cached image paths for art installation
     */
    suspend fun getCachedImagePaths(installationId: String): List<String>
    
    /**
     * Check if images are cached for installation
     */
    suspend fun areImagesCached(installationId: String): Boolean
    
    /**
     * Get all unique artists across all installations
     */
    suspend fun getAllArtists(eventId: String): List<String>
    
    /**
     * Get all unique interactive features
     */
    suspend fun getAllInteractiveFeatures(eventId: String): List<String>
    
    /**
     * Get installations with available cached images
     */
    suspend fun getArtInstallationsWithImages(eventId: String): List<ArtInstallation>
    
    /**
     * Check if art installation data is stale for an event
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
     * Clear cached images for storage management
     * @param keepRecent number of recently accessed images to keep
     */
    suspend fun clearImageCache(keepRecent: Int = 10)
    
    /**
     * Get total size of cached images
     */
    suspend fun getCachedImagesSizeBytes(): Long
}