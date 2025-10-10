package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.models.MapCoordinates
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    /**
     * Get the current GPS location as map coordinates
     */
    fun getCurrentLocation(): Flow<MapCoordinates?>
    
    /**
     * Request location permission if not already granted
     */
    suspend fun requestLocationPermission(): Boolean
    
    /**
     * Check if location permission is granted
     */
    suspend fun isLocationPermissionGranted(): Boolean
    
    /**
     * Start tracking user location
     */
    suspend fun startLocationTracking()
    
    /**
     * Stop tracking user location
     */
    suspend fun stopLocationTracking()
    
    /**
     * Convert GPS coordinates to map coordinates
     */
    suspend fun convertGpsToMapCoordinates(latitude: Double, longitude: Double): MapCoordinates
}