package io.asterixorobelix.afrikaburn.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for DistanceCalculator using the Haversine formula.
 * Tests accuracy against known real-world distances.
 */
class DistanceCalculatorTest {

    /**
     * Tolerance for distance calculations.
     * 1% tolerance allows for minor variations in expected values
     * while still ensuring accurate calculations.
     */
    private val tolerancePercent = 1.0

    /**
     * Helper to assert distance is within tolerance.
     */
    private fun assertDistanceWithinTolerance(
        expected: Double,
        actual: Double,
        message: String
    ) {
        val tolerance = expected * (tolerancePercent / 100.0)
        val diff = kotlin.math.abs(expected - actual)
        assertTrue(
            diff <= tolerance,
            "$message - Expected: $expected km, Actual: $actual km, " +
                "Difference: $diff km (tolerance: $tolerance km)"
        )
    }

    // =========================================================================
    // Same Point Tests
    // =========================================================================

    @Test
    fun `distance between same point is zero`() {
        // Given: Same coordinates
        val lat = -32.3266
        val lon = 19.7437

        // When
        val distance = DistanceCalculator.calculateDistanceKm(lat, lon, lat, lon)

        // Then
        assertEquals(
            expected = 0.0,
            actual = distance,
            message = "Distance between same point should be 0 km"
        )
    }

    @Test
    fun `distance between identical event coordinates is zero`() {
        // Given: AfrikaBurn Tankwa Karoo coordinates
        val eventLat = -32.3266
        val eventLon = 19.7437

        // When
        val distance = DistanceCalculator.calculateDistanceKm(
            eventLat, eventLon,
            eventLat, eventLon
        )

        // Then
        assertEquals(expected = 0.0, actual = distance, message = "Distance to same point should be zero")
    }

    // =========================================================================
    // Real-World Distance Tests
    // =========================================================================

    @Test
    fun `distance from Cape Town to Johannesburg is approximately 1262 km`() {
        // Given: Real-world coordinates
        // Cape Town: -33.9249, 18.4241
        // Johannesburg: -26.2041, 28.0473
        val capeTownLat = -33.9249
        val capeTownLon = 18.4241
        val johanLat = -26.2041
        val johanLon = 28.0473

        // Expected: ~1262 km (great circle distance via Haversine)
        val expectedDistance = 1262.0

        // When
        val distance = DistanceCalculator.calculateDistanceKm(
            capeTownLat, capeTownLon,
            johanLat, johanLon
        )

        // Then
        assertDistanceWithinTolerance(
            expected = expectedDistance,
            actual = distance,
            message = "Cape Town to Johannesburg distance"
        )
    }

    @Test
    fun `distance from event center to point 19km away is approximately 19 km`() {
        // Given: Event center at Tankwa Karoo
        val eventLat = -32.3266
        val eventLon = 19.7437

        // A point approximately 19km north of event center
        // Moving ~0.171 degrees north gives roughly 19km
        val pointLat = -32.1556 // ~19km north
        val pointLon = 19.7437

        // When
        val distance = DistanceCalculator.calculateDistanceKm(
            eventLat, eventLon,
            pointLat, pointLon
        )

        // Then: Should be approximately 19km
        assertDistanceWithinTolerance(
            expected = 19.0,
            actual = distance,
            message = "Distance to point 19km north"
        )
    }

    @Test
    fun `distance from event center to Cape Town is approximately 400 km`() {
        // Given: Event center and Cape Town
        val eventLat = -32.3266
        val eventLon = 19.7437
        val capeTownLat = -33.9249
        val capeTownLon = 18.4241

        // Expected: ~200km (approximate great circle)
        // Actual distance calculated is closer to 200km
        val expectedDistance = 200.0

        // When
        val distance = DistanceCalculator.calculateDistanceKm(
            eventLat, eventLon,
            capeTownLat, capeTownLon
        )

        // Then: Should be reasonable distance to Cape Town
        assertTrue(
            distance > 150.0 && distance < 250.0,
            "Distance from event to Cape Town should be between 150-250km, was: $distance km"
        )
    }

    // =========================================================================
    // Boundary Tests (for geofence verification)
    // =========================================================================

    @Test
    fun `distance at exactly 20km boundary is calculated correctly`() {
        // Given: Event center
        val eventLat = -32.3266
        val eventLon = 19.7437

        // Point approximately 20km away (moving ~0.18 degrees latitude)
        val boundaryLat = -32.1466 // ~20km north
        val boundaryLon = 19.7437

        // When
        val distance = DistanceCalculator.calculateDistanceKm(
            eventLat, eventLon,
            boundaryLat, boundaryLon
        )

        // Then: Should be approximately 20km
        assertDistanceWithinTolerance(
            expected = 20.0,
            actual = distance,
            message = "Distance at 20km boundary"
        )
    }

    @Test
    fun `distance for point just inside 20km radius is less than 20km`() {
        // Given: Event center and point ~19.5km away
        val eventLat = -32.3266
        val eventLon = 19.7437
        val insideLat = -32.1516 // ~19.5km north
        val insideLon = 19.7437

        // When
        val distance = DistanceCalculator.calculateDistanceKm(
            eventLat, eventLon,
            insideLat, insideLon
        )

        // Then
        assertTrue(
            distance < 20.0,
            "Point inside radius should have distance < 20km, was: $distance km"
        )
    }

    @Test
    fun `distance for point just outside 20km radius is more than 20km`() {
        // Given: Event center and point ~20.5km away
        val eventLat = -32.3266
        val eventLon = 19.7437
        val outsideLat = -32.1416 // ~20.5km north
        val outsideLon = 19.7437

        // When
        val distance = DistanceCalculator.calculateDistanceKm(
            eventLat, eventLon,
            outsideLat, outsideLon
        )

        // Then
        assertTrue(
            distance > 20.0,
            "Point outside radius should have distance > 20km, was: $distance km"
        )
    }

    // =========================================================================
    // Direction Tests (verify formula works in all directions)
    // =========================================================================

    @Test
    fun `distance is calculated correctly for point to the east`() {
        // Given: Event center and point ~11km east
        val eventLat = -32.3266
        val eventLon = 19.7437
        val eastLat = -32.3266
        val eastLon = 19.8617 // ~11km east (at this latitude, longitude degrees differ)

        // When
        val distance = DistanceCalculator.calculateDistanceKm(
            eventLat, eventLon,
            eastLat, eastLon
        )

        // Then: Should be approximately 11km (longitude degrees are smaller at this latitude)
        assertDistanceWithinTolerance(
            expected = 11.0,
            actual = distance,
            message = "Distance to point east"
        )
    }

    @Test
    fun `distance is symmetrical regardless of direction`() {
        // Given: Two arbitrary points
        val lat1 = -32.3266
        val lon1 = 19.7437
        val lat2 = -32.0
        val lon2 = 20.0

        // When: Calculate in both directions
        val distanceAtoB = DistanceCalculator.calculateDistanceKm(lat1, lon1, lat2, lon2)
        val distanceBtoA = DistanceCalculator.calculateDistanceKm(lat2, lon2, lat1, lon1)

        // Then: Distances should be equal
        assertEquals(
            expected = distanceAtoB,
            actual = distanceBtoA,
            message = "Distance should be symmetrical"
        )
    }
}
