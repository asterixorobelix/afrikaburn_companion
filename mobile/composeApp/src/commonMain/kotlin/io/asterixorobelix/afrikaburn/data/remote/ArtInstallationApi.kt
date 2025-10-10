package io.asterixorobelix.afrikaburn.data.remote

import io.asterixorobelix.afrikaburn.domain.model.ArtInstallation

/**
 * Mock API client for ArtInstallation
 * Placeholder implementation for compilation
 */
class ArtInstallationApi {
    
    suspend fun getArtInstallations(eventId: String): List<ArtInstallation> {
        // Mock implementation - returns empty list
        return emptyList()
    }
}