package io.asterixorobelix.afrikaburn.domain.util

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility object for calculating distances between GPS coordinates
 * using the Haversine formula.
 *
 * The Haversine formula determines the great-circle distance between
 * two points on a sphere given their longitudes and latitudes.
 */
object DistanceCalculator {

    /**
     * Earth's radius in kilometers.
     */
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Degrees to radians conversion factor.
     */
    private const val DEGREES_TO_RADIANS = PI / 180.0

    /**
     * Calculate the great-circle distance between two GPS coordinates.
     *
     * Uses the Haversine formula which provides accurate results for
     * distances on Earth's surface.
     *
     * @param lat1 Latitude of the first point in degrees
     * @param lon1 Longitude of the first point in degrees
     * @param lat2 Latitude of the second point in degrees
     * @param lon2 Longitude of the second point in degrees
     * @return Distance between the two points in kilometers
     */
    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        // Convert latitude and longitude differences to radians
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)

        // Convert latitudes to radians
        val lat1Rad = toRadians(lat1)
        val lat2Rad = toRadians(lat2)

        // Haversine formula
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Convert degrees to radians.
     *
     * @param degrees Angle in degrees
     * @return Angle in radians
     */
    private fun toRadians(degrees: Double): Double = degrees * DEGREES_TO_RADIANS
}
