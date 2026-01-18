package io.asterixorobelix.afrikaburn.platform

import kotlinx.coroutines.flow.Flow

/**
 * Location data from GPS.
 *
 * @param latitude The latitude coordinate
 * @param longitude The longitude coordinate
 * @param accuracy The accuracy of the location in meters (optional)
 * @param timestamp The timestamp when this location was recorded
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = 0L
)

/**
 * Location permission state.
 */
enum class PermissionState {
    /** Permission has been granted by the user */
    GRANTED,
    /** Permission has been denied by the user */
    DENIED,
    /** Permission has not been requested yet */
    NOT_DETERMINED
}

/**
 * Cross-platform location service interface.
 *
 * Provides location tracking capabilities for showing user position
 * on the map. Implementations should conserve battery by using
 * balanced power accuracy settings.
 */
interface LocationService {
    /**
     * Check current permission state without prompting user.
     *
     * @return The current [PermissionState]
     */
    suspend fun checkPermission(): PermissionState

    /**
     * Request location permission from user.
     *
     * This will show the system permission dialog if permission
     * has not been determined yet.
     *
     * @return The new [PermissionState] after user responds
     */
    suspend fun requestPermission(): PermissionState

    /**
     * Start location updates.
     *
     * Emits [LocationData] at regular intervals while tracking.
     * Call [stopLocationUpdates] when done to conserve battery.
     *
     * @return A [Flow] of location updates
     */
    fun startLocationUpdates(): Flow<LocationData>

    /**
     * Stop location updates to conserve battery.
     *
     * Should be called when leaving the map screen.
     */
    fun stopLocationUpdates()

    /**
     * Get a single location fix (one-shot).
     *
     * @return The current [LocationData], or null if unavailable
     */
    suspend fun getCurrentLocation(): LocationData?
}

/**
 * Factory function for creating platform-specific LocationService.
 */
expect fun createLocationService(): LocationService
