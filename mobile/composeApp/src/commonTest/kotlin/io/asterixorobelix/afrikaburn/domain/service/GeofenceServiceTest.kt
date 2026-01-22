package io.asterixorobelix.afrikaburn.domain.service

import io.asterixorobelix.afrikaburn.domain.model.EventConfig
import io.asterixorobelix.afrikaburn.platform.LocationData
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for GeofenceService.
 * Tests geofence detection based on user proximity to event location.
 *
 * AfrikaBurn event location: -32.3266, 19.7437 (Tankwa Karoo)
 * Geofence radius: 20 km
 */
class GeofenceServiceTest {

    /**
     * Fake Clock for testing - EventDateService requires a Clock.
     */
    private class FakeClock(private val fixedInstant: Instant) : Clock {
        override fun now(): Instant = fixedInstant
    }

    /**
     * Create a test instance of GeofenceService with default event config.
     */
    private fun createGeofenceService(): GeofenceService {
        val fakeClock = FakeClock(Instant.parse("2026-01-01T12:00:00Z"))
        val eventDateService = EventDateServiceImpl(fakeClock)
        return GeofenceServiceImpl(eventDateService)
    }

    /**
     * Create a test instance with custom event config.
     */
    private fun createGeofenceServiceWithConfig(config: EventConfig): GeofenceService {
        val fakeClock = FakeClock(Instant.parse("2026-01-01T12:00:00Z"))
        val eventDateService = EventDateServiceImpl(fakeClock, config = config)
        return GeofenceServiceImpl(eventDateService)
    }

    // Event center coordinates (Tankwa Karoo)
    private val eventLat = -32.3266
    private val eventLon = 19.7437

    // =========================================================================
    // isUserWithinGeofence(latitude, longitude) Tests
    // =========================================================================

    @Test
    fun `user at event center is within geofence`() {
        // Given: User is exactly at event center (0 km away)
        val service = createGeofenceService()

        // When
        val result = service.isUserWithinGeofence(eventLat, eventLon)

        // Then
        assertTrue(result, "User at event center should be within geofence")
    }

    @Test
    fun `user 10km from event is within geofence`() {
        // Given: User is approximately 10km north of event
        val service = createGeofenceService()
        val userLat = -32.2366 // ~10km north
        val userLon = 19.7437

        // When
        val result = service.isUserWithinGeofence(userLat, userLon)

        // Then
        assertTrue(result, "User 10km from event should be within 20km geofence")
    }

    @Test
    fun `user 19km from event is within geofence`() {
        // Given: User is approximately 19km north of event (just inside boundary)
        val service = createGeofenceService()
        val userLat = -32.1556 // ~19km north
        val userLon = 19.7437

        // When
        val result = service.isUserWithinGeofence(userLat, userLon)

        // Then
        assertTrue(result, "User 19km from event should be within 20km geofence")
    }

    @Test
    fun `user at approximately 20km boundary is within geofence`() {
        // Given: User is approximately on 20km boundary (19.9km)
        val service = createGeofenceService()
        val userLat = -32.1476 // ~19.9km north (just inside)
        val userLon = 19.7437

        // When
        val result = service.isUserWithinGeofence(userLat, userLon)

        // Then
        assertTrue(result, "User at ~20km boundary should be within geofence")
    }

    @Test
    fun `user just outside 20km boundary is not within geofence`() {
        // Given: User is approximately 20.5km north of event (just outside)
        val service = createGeofenceService()
        val userLat = -32.1416 // ~20.5km north
        val userLon = 19.7437

        // When
        val result = service.isUserWithinGeofence(userLat, userLon)

        // Then
        assertFalse(result, "User just outside 20km should not be within geofence")
    }

    @Test
    fun `user 50km from event is not within geofence`() {
        // Given: User is approximately 50km away
        val service = createGeofenceService()
        val userLat = -31.8766 // ~50km north
        val userLon = 19.7437

        // When
        val result = service.isUserWithinGeofence(userLat, userLon)

        // Then
        assertFalse(result, "User 50km from event should not be within geofence")
    }

    @Test
    fun `user in Cape Town is not within geofence`() {
        // Given: User is in Cape Town (~200km away)
        val service = createGeofenceService()
        val capeTownLat = -33.9249
        val capeTownLon = 18.4241

        // When
        val result = service.isUserWithinGeofence(capeTownLat, capeTownLon)

        // Then
        assertFalse(result, "User in Cape Town (~200km away) should not be within geofence")
    }

    // =========================================================================
    // isUserWithinGeofence(LocationData?) Tests
    // =========================================================================

    @Test
    fun `null location returns false`() {
        // Given: No location data available
        val service = createGeofenceService()

        // When
        val result = service.isUserWithinGeofence(null)

        // Then
        assertFalse(result, "Null location should return false (graceful handling)")
    }

    @Test
    fun `valid location within geofence returns true`() {
        // Given: Valid LocationData at event center
        val service = createGeofenceService()
        val location = LocationData(
            latitude = eventLat,
            longitude = eventLon,
            accuracy = 10f,
            timestamp = System.currentTimeMillis()
        )

        // When
        val result = service.isUserWithinGeofence(location)

        // Then
        assertTrue(result, "Valid location at event center should return true")
    }

    @Test
    fun `valid location outside geofence returns false`() {
        // Given: Valid LocationData far from event
        val service = createGeofenceService()
        val location = LocationData(
            latitude = -33.9249, // Cape Town
            longitude = 18.4241,
            accuracy = 10f,
            timestamp = System.currentTimeMillis()
        )

        // When
        val result = service.isUserWithinGeofence(location)

        // Then
        assertFalse(result, "Valid location in Cape Town should return false")
    }

    // =========================================================================
    // getDistanceToEventKm Tests
    // =========================================================================

    @Test
    fun `distance at event center is zero`() {
        // Given: User at event center
        val service = createGeofenceService()

        // When
        val distance = service.getDistanceToEventKm(eventLat, eventLon)

        // Then
        assertEquals(
            expected = 0.0,
            actual = distance,
            message = "Distance at event center should be 0 km"
        )
    }

    @Test
    fun `distance returns accurate value for known location`() {
        // Given: User approximately 19km north
        val service = createGeofenceService()
        val userLat = -32.1556 // ~19km north
        val userLon = 19.7437

        // When
        val distance = service.getDistanceToEventKm(userLat, userLon)

        // Then: Should be approximately 19km (within 1km tolerance)
        val expectedDistance = 19.0
        val tolerance = 1.0
        assertTrue(
            kotlin.math.abs(distance - expectedDistance) <= tolerance,
            "Distance should be approximately $expectedDistance km, was: $distance km"
        )
    }

    @Test
    fun `distance to Cape Town is approximately 200km`() {
        // Given: Cape Town coordinates
        val service = createGeofenceService()
        val capeTownLat = -33.9249
        val capeTownLon = 18.4241

        // When
        val distance = service.getDistanceToEventKm(capeTownLat, capeTownLon)

        // Then: Should be approximately 200km (between 150-250km)
        assertTrue(
            distance > 150.0 && distance < 250.0,
            "Distance to Cape Town should be between 150-250km, was: $distance km"
        )
    }

    // =========================================================================
    // Custom Geofence Radius Tests
    // =========================================================================

    @Test
    fun `custom geofence radius of 10km excludes user at 15km`() {
        // Given: Service with 10km geofence radius
        val config = EventConfig.DEFAULT.copy(geofenceRadiusKm = 10.0)
        val service = createGeofenceServiceWithConfig(config)

        // User at ~15km north
        val userLat = -32.1916
        val userLon = 19.7437

        // When
        val result = service.isUserWithinGeofence(userLat, userLon)

        // Then
        assertFalse(result, "User at 15km should be outside 10km geofence")
    }

    @Test
    fun `custom geofence radius of 30km includes user at 25km`() {
        // Given: Service with 30km geofence radius
        val config = EventConfig.DEFAULT.copy(geofenceRadiusKm = 30.0)
        val service = createGeofenceServiceWithConfig(config)

        // User at ~25km north
        val userLat = -32.1016
        val userLon = 19.7437

        // When
        val result = service.isUserWithinGeofence(userLat, userLon)

        // Then
        assertTrue(result, "User at 25km should be inside 30km geofence")
    }
}
