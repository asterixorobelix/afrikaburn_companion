package io.asterixorobelix.afrikaburn.domain.service

import io.asterixorobelix.afrikaburn.domain.model.EventConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive tests for EventDateService functionality.
 * Tests date-based event unlock detection and debug bypass flag.
 */
class EventDateServiceTest {

    /**
     * Fake Clock implementation for testing.
     * Allows us to control the "current time" in tests.
     */
    private class FakeClock(private val fixedInstant: Instant) : Clock {
        override fun now(): Instant = fixedInstant
    }

    /**
     * Helper function to create an Instant from a LocalDate.
     * Uses noon UTC to avoid timezone edge cases.
     */
    private fun localDateToInstant(date: LocalDate): Instant {
        // Create instant at noon UTC on the given date
        val dateTimeString = "${date}T12:00:00Z"
        return Instant.parse(dateTimeString)
    }

    // =========================================================================
    // isEventStarted() Tests
    // =========================================================================

    @Test
    fun `isEventStarted returns false before event start date`() {
        // Given: A date well before the event start (e.g., January 2026)
        val beforeEventDate = LocalDate(2026, 1, 15)
        val fakeClock = FakeClock(localDateToInstant(beforeEventDate))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isEventStarted()

        // Then
        assertFalse(result, "isEventStarted should return false before event start date")
    }

    @Test
    fun `isEventStarted returns true on event start date`() {
        // Given: The exact event start date (April 27, 2026)
        val eventStartDate = LocalDate(2026, 4, 27)
        val fakeClock = FakeClock(localDateToInstant(eventStartDate))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isEventStarted()

        // Then
        assertTrue(result, "isEventStarted should return true on event start date")
    }

    @Test
    fun `isEventStarted returns true after event start date`() {
        // Given: A date after the event start (e.g., June 2026)
        val afterEventDate = LocalDate(2026, 6, 15)
        val fakeClock = FakeClock(localDateToInstant(afterEventDate))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isEventStarted()

        // Then
        assertTrue(result, "isEventStarted should return true after event start date")
    }

    // =========================================================================
    // isEventActive() Tests
    // =========================================================================

    @Test
    fun `isEventActive returns false before event`() {
        // Given: A date before the event
        val beforeEventDate = LocalDate(2026, 4, 20)
        val fakeClock = FakeClock(localDateToInstant(beforeEventDate))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isEventActive()

        // Then
        assertFalse(result, "isEventActive should return false before event starts")
    }

    @Test
    fun `isEventActive returns true during event`() {
        // Given: A date during the event (April 29, 2026 - middle of event)
        val duringEventDate = LocalDate(2026, 4, 29)
        val fakeClock = FakeClock(localDateToInstant(duringEventDate))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isEventActive()

        // Then
        assertTrue(result, "isEventActive should return true during the event")
    }

    @Test
    fun `isEventActive returns true on event start date`() {
        // Given: The event start date
        val eventStartDate = LocalDate(2026, 4, 27)
        val fakeClock = FakeClock(localDateToInstant(eventStartDate))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isEventActive()

        // Then
        assertTrue(result, "isEventActive should return true on event start date")
    }

    @Test
    fun `isEventActive returns true on event end date`() {
        // Given: The event end date
        val eventEndDate = LocalDate(2026, 5, 3)
        val fakeClock = FakeClock(localDateToInstant(eventEndDate))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isEventActive()

        // Then
        assertTrue(result, "isEventActive should return true on event end date")
    }

    @Test
    fun `isEventActive returns false after event`() {
        // Given: A date after the event ends
        val afterEventDate = LocalDate(2026, 5, 10)
        val fakeClock = FakeClock(localDateToInstant(afterEventDate))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isEventActive()

        // Then
        assertFalse(result, "isEventActive should return false after event ends")
    }

    // =========================================================================
    // getEventConfig() Tests
    // =========================================================================

    @Test
    fun `getEventConfig returns expected configuration`() {
        // Given
        val fakeClock = FakeClock(Instant.parse("2026-01-01T12:00:00Z"))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val config = service.getEventConfig()

        // Then: Verify AfrikaBurn 2026 configuration
        assertEquals(
            LocalDate(2026, 4, 27),
            config.eventStartDate,
            "Event should start on April 27, 2026"
        )
        assertEquals(
            LocalDate(2026, 5, 3),
            config.eventEndDate,
            "Event should end on May 3, 2026"
        )
        assertEquals(
            -32.3266,
            config.eventLatitude,
            "Event latitude should be Tankwa Karoo coordinates"
        )
        assertEquals(
            19.7437,
            config.eventLongitude,
            "Event longitude should be Tankwa Karoo coordinates"
        )
        assertEquals(
            20.0,
            config.geofenceRadiusKm,
            "Geofence radius should be 20 km"
        )
    }

    // =========================================================================
    // Debug Bypass Flag Tests
    // =========================================================================

    @Test
    fun `debug flag bypass returns false when disabled (default)`() {
        // Given: Default configuration (bypass disabled)
        val fakeClock = FakeClock(Instant.parse("2026-01-01T12:00:00Z"))
        val service = EventDateServiceImpl(fakeClock)

        // When
        val result = service.isUnlockBypassed()

        // Then
        assertFalse(result, "Debug bypass should be false by default")
    }

    @Test
    fun `debug flag bypass returns true when enabled`() {
        // Given: Service with bypass enabled
        val fakeClock = FakeClock(Instant.parse("2026-01-01T12:00:00Z"))
        val service = EventDateServiceImpl(fakeClock, bypassSurpriseMode = true)

        // When
        val result = service.isUnlockBypassed()

        // Then
        assertTrue(result, "Debug bypass should return true when enabled")
    }

    // =========================================================================
    // EventConfig Data Class Tests
    // =========================================================================

    @Test
    fun `EventConfig DEFAULT contains AfrikaBurn 2026 values`() {
        // When
        val config = EventConfig.DEFAULT

        // Then
        assertEquals(LocalDate(2026, 4, 27), config.eventStartDate)
        assertEquals(LocalDate(2026, 5, 3), config.eventEndDate)
        assertEquals(-32.3266, config.eventLatitude)
        assertEquals(19.7437, config.eventLongitude)
        assertEquals(20.0, config.geofenceRadiusKm)
    }
}
