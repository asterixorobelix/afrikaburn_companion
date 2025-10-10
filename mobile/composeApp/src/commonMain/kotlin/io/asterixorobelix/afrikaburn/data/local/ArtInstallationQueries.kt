package io.asterixorobelix.afrikaburn.data.local

import io.asterixorobelix.afrikaburn.data.repository.DatabaseArtInstallation

/**
 * Mock SQLDelight queries for ArtInstallation
 * Placeholder implementation for compilation
 */
class ArtInstallationQueries {
    
    fun selectArtInstallationsByEvent(eventId: String): MockQuery<DatabaseArtInstallation> {
        return MockQuery<DatabaseArtInstallation>(emptyList<DatabaseArtInstallation>())
    }
    
    fun selectArtInstallationById(installationId: String): MockQuery<DatabaseArtInstallation?> {
        return MockQuery<DatabaseArtInstallation?>(null)
    }
    
    fun selectArtInstallationsByArtist(eventId: String, artistName: String): MockQuery<DatabaseArtInstallation> {
        return MockQuery<DatabaseArtInstallation>(emptyList<DatabaseArtInstallation>())
    }
    
    fun selectArtInstallationByQrCode(qrCode: String): MockQuery<DatabaseArtInstallation?> {
        return MockQuery<DatabaseArtInstallation?>(null)
    }
    
    fun searchArtInstallations(eventId: String, query: String): MockQuery<DatabaseArtInstallation> {
        return MockQuery<DatabaseArtInstallation>(emptyList<DatabaseArtInstallation>())
    }
    
    fun insertArtInstallation(
        id: String,
        eventId: String,
        name: String,
        artistName: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        photoUrls: String,
        artistBio: String?,
        interactiveFeatures: String,
        isHidden: Long,
        unlockTimestamp: Long?,
        qrCode: String?,
        lastUpdated: Long
    ) {
        // Mock implementation
    }
    
    fun updateArtInstallation(
        name: String,
        artistName: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        photoUrls: String,
        artistBio: String?,
        interactiveFeatures: String,
        isHidden: Long,
        unlockTimestamp: Long?,
        qrCode: String?,
        lastUpdated: Long,
        id: String
    ) {
        // Mock implementation
    }
    
    fun deleteArtInstallation(installationId: String) {
        // Mock implementation
    }
    
    fun deleteArtInstallationsByEvent(eventId: String) {
        // Mock implementation
    }
    
    fun selectLastSyncTimestamp(eventId: String): MockQuery<Long?> {
        return MockQuery<Long?>(null)
    }
    
    fun insertOrUpdateSyncTimestamp(eventId: String, timestamp: Long) {
        // Mock implementation
    }
    
    fun transaction(body: () -> Unit) {
        body()
    }
}


