package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.repository.LocationRepository
import io.asterixorobelix.afrikaburn.models.MapCoordinates
import io.asterixorobelix.afrikaburn.platform.LocationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LocationRepositoryImpl(
    private val locationService: LocationService
) : LocationRepository {
    override fun getCurrentLocation(): Flow<MapCoordinates?> {
        return locationService.getLocationUpdates().map { locationPair ->
            convertGpsToMapCoordinates(locationPair.first, locationPair.second)
        }
    }
    
    override suspend fun requestLocationPermission(): Boolean {
        return locationService.requestLocationPermission()
    }
    
    override suspend fun startLocationTracking() {
        locationService.startLocationUpdates()
    }
    
    override suspend fun stopLocationTracking() {
        locationService.stopLocationUpdates()
    }
    
    override suspend fun isLocationPermissionGranted(): Boolean {
        return locationService.hasLocationPermission()
    }
    
    override suspend fun convertGpsToMapCoordinates(latitude: Double, longitude: Double): MapCoordinates {
        // For AfrikaBurn, convert GPS to simplified map coordinates
        // This is a simplified conversion - in production, use proper projection
        val mapCenterLat = -32.482474
        val mapCenterLon = 19.897824
        val scale = 10000.0 // Arbitrary scale factor
        
        val x = ((longitude - mapCenterLon) * scale).toFloat()
        val y = ((latitude - mapCenterLat) * scale).toFloat()
        
        return MapCoordinates(x, y)
    }
}