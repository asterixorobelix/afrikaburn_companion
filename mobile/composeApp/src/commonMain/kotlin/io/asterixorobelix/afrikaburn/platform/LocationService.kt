package io.asterixorobelix.afrikaburn.platform

import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific location service interface
 * Provides GPS location updates and permission management
 */
interface LocationService {
    /**
     * Flow of location updates as pairs of (latitude, longitude)
     */
    fun getLocationUpdates(): Flow<Pair<Double, Double>>
    
    /**
     * Get current location once
     * @return Pair of (latitude, longitude) or null if unavailable
     */
    suspend fun getCurrentLocation(): Pair<Double, Double>?
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean
    
    /**
     * Request location permissions from the user
     * @return true if permissions were granted
     */
    suspend fun requestLocationPermission(): Boolean
    
    /**
     * Start location updates
     */
    fun startLocationUpdates()
    
    /**
     * Stop location updates to save battery
     */
    fun stopLocationUpdates()
    
    /**
     * Calculate distance between two points in meters
     */
    fun calculateDistance(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double
    
    /**
     * Check if a location is within the event boundary
     * @param latitude Current latitude
     * @param longitude Current longitude
     * @param eventCenterLat Event center latitude
     * @param eventCenterLon Event center longitude
     * @param radiusKm Event radius in kilometers
     */
    fun isWithinEventBoundary(
        latitude: Double,
        longitude: Double,
        eventCenterLat: Double,
        eventCenterLon: Double,
        radiusKm: Double = 5.0
    ): Boolean {
        val distance = calculateDistance(latitude, longitude, eventCenterLat, eventCenterLon)
        return distance <= radiusKm * 1000 // Convert km to meters
    }
}

/**
 * Configuration for location updates
 */
data class LocationConfig(
    val updateIntervalMillis: Long = 5000, // 5 seconds
    val fastestUpdateIntervalMillis: Long = 2000, // 2 seconds
    val minDisplacementMeters: Float = 10f, // 10 meters
    val highAccuracy: Boolean = true
)

/**
 * Location update priority levels
 */
enum class LocationPriority {
    /**
     * High accuracy, high power usage
     * Use for active navigation
     */
    HIGH_ACCURACY,
    
    /**
     * Balanced accuracy and power
     * Use for general location tracking
     */
    BALANCED,
    
    /**
     * Low power usage, city-level accuracy
     * Use for background updates
     */
    LOW_POWER,
    
    /**
     * No active location updates
     * Use when app is in background or user disabled location
     */
    NO_POWER
}

/**
 * Common location utilities
 */
object LocationUtils {
    /**
     * Validate coordinates are within valid ranges
     */
    fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
    
    /**
     * Check if coordinates are within Tankwa Karoo region
     * Approximate bounds for the event area
     */
    fun isInTankwaKaroo(latitude: Double, longitude: Double): Boolean {
        // Approximate bounds for Tankwa Karoo
        val minLat = -32.5
        val maxLat = -32.3
        val minLon = 19.3
        val maxLon = 19.5
        
        return latitude in minLat..maxLat && longitude in minLon..maxLon
    }
    
    /**
     * Format coordinates for display
     */
    fun formatCoordinates(latitude: Double, longitude: Double): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        
        return "%.4f°%s, %.4f°%s".format(
            kotlin.math.abs(latitude),
            latDir,
            kotlin.math.abs(longitude),
            lonDir
        )
    }
}

/**
 * Expected platform implementations:
 * - Android: Uses FusedLocationProviderClient
 * - iOS: Uses CLLocationManager
 */
expect fun createLocationService(): LocationService