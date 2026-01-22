package io.asterixorobelix.afrikaburn.domain.service

import io.asterixorobelix.afrikaburn.domain.model.EventConfig
import io.asterixorobelix.afrikaburn.domain.repository.UnlockStateRepository
import io.asterixorobelix.afrikaburn.platform.LocationData
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive TDD tests for UnlockConditionManager.
 *
 * Tests unlock logic combining:
 * - Persisted unlock state
 * - Event date condition (via EventDateService)
 * - Geofence condition (via GeofenceService)
 * - Debug bypass flag
 *
 * Key behaviors:
 * - Once unlocked, always returns unlocked (persistence)
 * - Date OR geofence unlocks (either condition sufficient)
 * - Bypass flag unlocks without persistence
 */
class UnlockConditionManagerTest {

    // =========================================================================
    // Fake implementations for testing
    // =========================================================================

    /**
     * Fake Clock for testing.
     */
    private class FakeClock(private val fixedInstant: Instant) : Clock {
        override fun now(): Instant = fixedInstant
    }

    /**
     * Fake UnlockStateRepository for in-memory testing.
     */
    private class FakeUnlockStateRepository : UnlockStateRepository {
        private var unlocked = false
        private var unlockedAtTime: Instant? = null

        override fun isUnlocked(): Boolean = unlocked

        override fun setUnlocked() {
            unlocked = true
            unlockedAtTime = Instant.parse("2026-04-27T10:00:00Z")
        }

        override fun getUnlockedAt(): Instant? = unlockedAtTime

        // Test helper to check if setUnlocked was called
        fun wasSetUnlockedCalled(): Boolean = unlocked
    }

    /**
     * Fake EventDateService for controllable date conditions.
     */
    private class FakeEventDateService(
        private var eventStarted: Boolean = false,
        private var bypassed: Boolean = false
    ) : EventDateService {
        override fun isEventStarted(): Boolean = eventStarted
        override fun isEventActive(): Boolean = eventStarted
        override fun getEventConfig(): EventConfig = EventConfig.DEFAULT
        override fun isUnlockBypassed(): Boolean = bypassed

        fun setEventStarted(started: Boolean) {
            eventStarted = started
        }

        fun setBypassed(bypass: Boolean) {
            bypassed = bypass
        }
    }

    /**
     * Fake GeofenceService for controllable geofence conditions.
     */
    private class FakeGeofenceService(
        private var withinGeofence: Boolean = false
    ) : GeofenceService {
        override fun isUserWithinGeofence(latitude: Double, longitude: Double): Boolean =
            withinGeofence

        override fun isUserWithinGeofence(location: LocationData?): Boolean =
            if (location == null) false else withinGeofence

        override fun getDistanceToEventKm(latitude: Double, longitude: Double): Double = 0.0

        fun setWithinGeofence(within: Boolean) {
            withinGeofence = within
        }
    }

    // =========================================================================
    // Test factory helper
    // =========================================================================

    private fun createManager(
        unlockStateRepository: FakeUnlockStateRepository = FakeUnlockStateRepository(),
        eventDateService: FakeEventDateService = FakeEventDateService(),
        geofenceService: FakeGeofenceService = FakeGeofenceService()
    ): Triple<UnlockConditionManager, FakeUnlockStateRepository, FakeEventDateService> {
        val manager = UnlockConditionManagerImpl(
            eventDateService = eventDateService,
            geofenceService = geofenceService,
            unlockStateRepository = unlockStateRepository
        )
        return Triple(manager, unlockStateRepository, eventDateService)
    }

    // Sample location data for tests
    private val sampleLocation = LocationData(
        latitude = -32.3266,
        longitude = 19.7437,
        accuracy = 10f,
        timestamp = System.currentTimeMillis()
    )

    // =========================================================================
    // Test 1: Already persisted as unlocked
    // =========================================================================

    @Test
    fun `isUnlocked returns true when already persisted as unlocked`() {
        // Given: Unlock state is already persisted
        val repository = FakeUnlockStateRepository()
        repository.setUnlocked() // Pre-persist unlock

        val (manager, _, _) = createManager(unlockStateRepository = repository)

        // When
        val result = manager.isUnlocked(null)

        // Then
        assertTrue(result, "Should return true when already persisted as unlocked")
    }

    // =========================================================================
    // Test 2: Event date condition triggers unlock and persists
    // =========================================================================

    @Test
    fun `isUnlocked returns true when eventDateService isEventStarted is true and persists`() {
        // Given: Event has started
        val eventDateService = FakeEventDateService(eventStarted = true)
        val (manager, repository, _) = createManager(eventDateService = eventDateService)

        // When
        val result = manager.isUnlocked(null)

        // Then
        assertTrue(result, "Should return true when event has started")
        assertTrue(repository.wasSetUnlockedCalled(), "Should persist unlock state")
    }

    // =========================================================================
    // Test 3: Geofence condition triggers unlock and persists
    // =========================================================================

    @Test
    fun `isUnlocked returns true when geofenceService isUserWithinGeofence is true and persists`() {
        // Given: User is within geofence
        val geofenceService = FakeGeofenceService(withinGeofence = true)
        val (manager, repository, _) = createManager(geofenceService = geofenceService)

        // When
        val result = manager.isUnlocked(sampleLocation)

        // Then
        assertTrue(result, "Should return true when user is within geofence")
        assertTrue(repository.wasSetUnlockedCalled(), "Should persist unlock state")
    }

    // =========================================================================
    // Test 4: No conditions met and not persisted
    // =========================================================================

    @Test
    fun `isUnlocked returns false when no conditions met and not persisted`() {
        // Given: No conditions are met
        val (manager, repository, _) = createManager()

        // When
        val result = manager.isUnlocked(null)

        // Then
        assertFalse(result, "Should return false when no conditions met")
        assertFalse(repository.wasSetUnlockedCalled(), "Should not persist unlock state")
    }

    // =========================================================================
    // Test 5: Bypass flag unlocks without persistence
    // =========================================================================

    @Test
    fun `isUnlocked returns true when bypass flag is enabled but does NOT persist`() {
        // Given: Bypass is enabled
        val eventDateService = FakeEventDateService(bypassed = true)
        val (manager, repository, _) = createManager(eventDateService = eventDateService)

        // When
        val result = manager.isUnlocked(null)

        // Then
        assertTrue(result, "Should return true when bypass is enabled")
        assertFalse(repository.wasSetUnlockedCalled(), "Should NOT persist unlock state for bypass")
    }

    // =========================================================================
    // Test 6: Once unlocked, always returns true
    // =========================================================================

    @Test
    fun `once unlocked always returns true even if conditions change`() {
        // Given: Initially unlock via event date
        val eventDateService = FakeEventDateService(eventStarted = true)
        val (manager, _, _) = createManager(eventDateService = eventDateService)

        // First call triggers unlock
        val firstResult = manager.isUnlocked(null)
        assertTrue(firstResult, "Should be unlocked initially")

        // When: Conditions change (hypothetically event "unstarts" - shouldn't happen but test persistence)
        eventDateService.setEventStarted(false)
        val secondResult = manager.isUnlocked(null)

        // Then: Should still be unlocked due to persistence
        assertTrue(secondResult, "Should remain unlocked after conditions change")
    }

    // =========================================================================
    // Test 7: checkAndUpdateUnlockState evaluates and persists
    // =========================================================================

    @Test
    fun `checkAndUpdateUnlockState evaluates conditions and persists if met`() {
        // Given: User is within geofence
        val geofenceService = FakeGeofenceService(withinGeofence = true)
        val (manager, repository, _) = createManager(geofenceService = geofenceService)

        // When
        manager.checkAndUpdateUnlockState(sampleLocation)

        // Then
        assertTrue(repository.wasSetUnlockedCalled(), "Should persist unlock state")
    }

    @Test
    fun `checkAndUpdateUnlockState does not persist if conditions not met`() {
        // Given: No conditions met
        val (manager, repository, _) = createManager()

        // When
        manager.checkAndUpdateUnlockState(sampleLocation)

        // Then
        assertFalse(repository.wasSetUnlockedCalled(), "Should not persist when conditions not met")
    }

    // =========================================================================
    // Test 8: getUnlockedAt returns appropriate values
    // =========================================================================

    @Test
    fun `getUnlockedAt returns null when not unlocked`() {
        // Given: Not unlocked
        val (manager, _, _) = createManager()

        // When
        val result = manager.getUnlockedAt()

        // Then
        assertNull(result, "Should return null when not unlocked")
    }

    @Test
    fun `getUnlockedAt returns timestamp when unlocked`() {
        // Given: Unlocked via event date
        val eventDateService = FakeEventDateService(eventStarted = true)
        val (manager, _, _) = createManager(eventDateService = eventDateService)

        // Trigger unlock
        manager.isUnlocked(null)

        // When
        val result = manager.getUnlockedAt()

        // Then
        assertEquals(
            Instant.parse("2026-04-27T10:00:00Z"),
            result,
            "Should return unlock timestamp"
        )
    }

    // =========================================================================
    // Additional edge case tests
    // =========================================================================

    @Test
    fun `null location with event started still unlocks`() {
        // Given: Event started but no location
        val eventDateService = FakeEventDateService(eventStarted = true)
        val (manager, repository, _) = createManager(eventDateService = eventDateService)

        // When
        val result = manager.isUnlocked(null)

        // Then
        assertTrue(result, "Should unlock via date even with null location")
        assertTrue(repository.wasSetUnlockedCalled(), "Should persist")
    }

    @Test
    fun `bypass takes priority over checking persistence first`() {
        // Given: Already persisted AND bypass enabled
        val repository = FakeUnlockStateRepository()
        repository.setUnlocked() // Pre-persist

        val eventDateService = FakeEventDateService(bypassed = true)
        val (manager, _, _) = createManager(
            unlockStateRepository = repository,
            eventDateService = eventDateService
        )

        // When
        val result = manager.isUnlocked(null)

        // Then: Should return true (either reason works)
        assertTrue(result, "Should return true when bypassed")
    }

    @Test
    fun `either date OR geofence unlocks - both not required`() {
        // Given: Only geofence, not date
        val eventDateService = FakeEventDateService(eventStarted = false)
        val geofenceService = FakeGeofenceService(withinGeofence = true)
        val (manager, repository, _) = createManager(
            eventDateService = eventDateService,
            geofenceService = geofenceService
        )

        // When
        val result = manager.isUnlocked(sampleLocation)

        // Then: Geofence alone should unlock
        assertTrue(result, "Geofence alone should unlock (OR condition)")
        assertTrue(repository.wasSetUnlockedCalled(), "Should persist")
    }
}
