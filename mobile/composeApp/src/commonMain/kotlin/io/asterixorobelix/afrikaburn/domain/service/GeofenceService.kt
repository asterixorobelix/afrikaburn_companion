package io.asterixorobelix.afrikaburn.domain.service

import io.asterixorobelix.afrikaburn.domain.util.DistanceCalculator
import io.asterixorobelix.afrikaburn.platform.LocationData

/**
 * Service for detecting if user is within the event geofence.
 *
 * Used to determine if the user is physically close enough to the
 * AfrikaBurn event to unlock the surprise mode (hidden tabs).
 */
interface GeofenceService {

    /**
     * Check if user is within the event geofence.
     *
     * @param latitude User's current latitude
     * @param longitude User's current longitude
     * @return True if user is within the geofence radius, false otherwise
     */
    fun isUserWithinGeofence(latitude: Double, longitude: Double): Boolean

    /**
     * Check if user is within the event geofence using LocationData.
     *
     * Convenience overload that handles null location gracefully.
     *
     * @param location User's current location, or null if unavailable
     * @return True if location is valid and within geofence, false otherwise
     */
    fun isUserWithinGeofence(location: LocationData?): Boolean

    /**
     * Get the distance from user's location to the event center.
     *
     * Useful for UI display purposes (e.g., "You are X km from the event").
     *
     * @param latitude User's current latitude
     * @param longitude User's current longitude
     * @return Distance to event center in kilometers
     */
    fun getDistanceToEventKm(latitude: Double, longitude: Double): Double
}

/**
 * Implementation of GeofenceService using EventDateService for event configuration.
 *
 * Uses the Haversine formula (via DistanceCalculator) to calculate distances
 * and compares against the configured geofence radius.
 *
 * @param eventDateService Provides event configuration including coordinates and geofence radius
 */
class GeofenceServiceImpl(
    private val eventDateService: EventDateService
) : GeofenceService {

    override fun isUserWithinGeofence(latitude: Double, longitude: Double): Boolean {
        val distance = getDistanceToEventKm(latitude, longitude)
        val config = eventDateService.getEventConfig()
        return distance <= config.geofenceRadiusKm
    }

    override fun isUserWithinGeofence(location: LocationData?): Boolean {
        if (location == null) return false
        return isUserWithinGeofence(location.latitude, location.longitude)
    }

    override fun getDistanceToEventKm(latitude: Double, longitude: Double): Double {
        val config = eventDateService.getEventConfig()
        return DistanceCalculator.calculateDistanceKm(
            lat1 = latitude,
            lon1 = longitude,
            lat2 = config.eventLatitude,
            lon2 = config.eventLongitude
        )
    }
}
