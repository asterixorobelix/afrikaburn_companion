package io.asterixorobelix.afrikaburn.domain.service

import io.asterixorobelix.afrikaburn.domain.model.EventConfig
import io.asterixorobelix.afrikaburn.domain.repository.UnlockStateRepository
import io.asterixorobelix.afrikaburn.platform.LocationData
import kotlin.time.Instant
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlin.test.Test
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
 * - Event year reset (tabs re-lock when event year changes)
 *
 * Key behaviors:
 * - Once unlocked, returns unlocked for the same event year
 * - Date OR geofence unlocks (either condition sufficient)
 * - Bypass flag unlocks without persistence
 * - Changing event year clears persisted unlock state
 */
@OptIn(ExperimentalTime::class)
class UnlockConditionManagerTest {

    // =========================================================================
    // Fake implementations for testing
    // =========================================================================

    /**
     * Fake UnlockStateRepository for in-memory testing.
     */
    private class FakeUnlockStateRepository : UnlockStateRepository {
        private var unlocked = false
        private var unlockedAtTime: Instant? = null
        private var storedEventYear: Int? = null

        override fun isUnlocked(): Boolean = unlocked

        override fun setUnlocked(eventYear: Int) {
            unlocked = true
            unlockedAtTime = Instant.parse("2026-04-27T10:00:00Z")
            storedEventYear = eventYear
        }

        override fun getUnlockedAt(): Instant? = unlockedAtTime

        override fun getEventYear(): Int? = storedEventYear

        override fun clearUnlockState() {
            unlocked = false
            unlockedAtTime = null
            storedEventYear = null
        }

        // Test helper to check if setUnlocked was called
        fun wasSetUnlockedCalled(): Boolean = unlocked
    }

    /**
     * Fake EventDateService for controllable date conditions.
     */
    private class FakeEventDateService(
        private var eventStarted: Boolean = false,
        private var bypassed: Boolean = false,
        private var config: EventConfig = EventConfig.DEFAULT
    ) : EventDateService {
        override fun isEventStarted(): Boolean = eventStarted
        override fun isEventActive(): Boolean = eventStarted
        override fun getEventConfig(): EventConfig = config
        override fun isUnlockBypassed(): Boolean = bypassed

        fun setEventStarted(started: Boolean) {
            eventStarted = started
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
    // Test 1: Already persisted as unlocked (same event year)
    // =========================================================================

    @Test
    fun `isUnlocked returns true when already persisted as unlocked`() {
        // Given: Unlock state is already persisted for current event year
        val repository = FakeUnlockStateRepository()
        repository.setUnlocked(EventConfig.DEFAULT.eventStartDate.year)

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
    // Test 6: Once unlocked, always returns true (same event year)
    // =========================================================================

    @Test
    fun `once unlocked always returns true even if conditions change`() {
        // Given: Initially unlock via event date
        val eventDateService = FakeEventDateService(eventStarted = true)
        val (manager, _, _) = createManager(eventDateService = eventDateService)

        // First call triggers unlock
        val firstResult = manager.isUnlocked(null)
        assertTrue(firstResult, "Should be unlocked initially")

        // When: Conditions change (hypothetically event "unstarts")
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
        assertTrue(result != null, "Should return unlock timestamp")
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
        repository.setUnlocked(EventConfig.DEFAULT.eventStartDate.year)

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

    // =========================================================================
    // Test 9: wasJustUnlocked tracking
    // =========================================================================

    @Test
    fun `wasJustUnlocked returns false when not unlocked`() {
        // Given: Not unlocked
        val (manager, _, _) = createManager()

        // When
        manager.isUnlocked(null) // This won't unlock since no conditions met

        // Then
        assertFalse(manager.wasJustUnlocked(), "Should return false when not unlocked")
    }

    @Test
    fun `wasJustUnlocked returns true when fresh unlock happens this session`() {
        // Given: Event started (will trigger fresh unlock)
        val eventDateService = FakeEventDateService(eventStarted = true)
        val (manager, _, _) = createManager(eventDateService = eventDateService)

        // When
        manager.isUnlocked(null)

        // Then
        assertTrue(manager.wasJustUnlocked(), "Should return true after fresh unlock")
    }

    @Test
    fun `wasJustUnlocked returns false when already unlocked from persistence`() {
        // Given: Already persisted as unlocked (from previous session)
        val repository = FakeUnlockStateRepository()
        repository.setUnlocked(EventConfig.DEFAULT.eventStartDate.year)

        val (manager, _, _) = createManager(unlockStateRepository = repository)

        // When
        manager.isUnlocked(null)

        // Then: Should not be "just unlocked" since it was from persistence
        assertFalse(manager.wasJustUnlocked(), "Should return false for persisted unlock")
    }

    @Test
    fun `wasJustUnlocked returns false when bypass is used`() {
        // Given: Bypass enabled
        val eventDateService = FakeEventDateService(bypassed = true)
        val (manager, _, _) = createManager(eventDateService = eventDateService)

        // When
        manager.isUnlocked(null)

        // Then: Bypass doesn't count as "just unlocked" (no persistence)
        assertFalse(manager.wasJustUnlocked(), "Should return false for bypass")
    }

    // =========================================================================
    // Test 10: Event year reset — tabs re-lock when event year changes
    // =========================================================================

    @Test
    fun `unlock state is cleared when event year changes`() {
        // Given: Previously unlocked for 2026
        val repository = FakeUnlockStateRepository()
        repository.setUnlocked(2026)

        // But now the event config is for 2027
        val config2027 = EventConfig(
            eventStartDate = LocalDate(2027, 4, 26),
            eventEndDate = LocalDate(2027, 5, 2),
            eventLatitude = -32.3266,
            eventLongitude = 19.7437,
            geofenceRadiusKm = 20.0
        )
        val eventDateService = FakeEventDateService(config = config2027)

        // When: Manager is created (init triggers year check)
        val manager = UnlockConditionManagerImpl(
            eventDateService = eventDateService,
            geofenceService = FakeGeofenceService(),
            unlockStateRepository = repository
        )

        // Then: Unlock state should have been cleared
        assertFalse(manager.isUnlocked(null), "Should be locked after event year change")
        assertFalse(repository.isUnlocked(), "Repository should be cleared")
        assertNull(repository.getEventYear(), "Stored event year should be null")
    }

    @Test
    fun `unlock state is preserved when event year matches`() {
        // Given: Previously unlocked for 2026, config is still 2026
        val repository = FakeUnlockStateRepository()
        repository.setUnlocked(2026)

        val eventDateService = FakeEventDateService(config = EventConfig.DEFAULT)

        // When: Manager is created
        val manager = UnlockConditionManagerImpl(
            eventDateService = eventDateService,
            geofenceService = FakeGeofenceService(),
            unlockStateRepository = repository
        )

        // Then: Should still be unlocked
        assertTrue(manager.isUnlocked(null), "Should remain unlocked when year matches")
    }

    @Test
    fun `fresh unlock after year reset persists new event year`() {
        // Given: Previously unlocked for 2026, config changed to 2027
        val repository = FakeUnlockStateRepository()
        repository.setUnlocked(2026)

        val config2027 = EventConfig(
            eventStartDate = LocalDate(2027, 4, 26),
            eventEndDate = LocalDate(2027, 5, 2),
            eventLatitude = -32.3266,
            eventLongitude = 19.7437,
            geofenceRadiusKm = 20.0
        )
        val eventDateService = FakeEventDateService(
            eventStarted = true,
            config = config2027
        )

        // When: Manager is created (clears old state), then conditions trigger new unlock
        val manager = UnlockConditionManagerImpl(
            eventDateService = eventDateService,
            geofenceService = FakeGeofenceService(),
            unlockStateRepository = repository
        )
        manager.isUnlocked(null)

        // Then: Should be unlocked with new event year
        assertTrue(repository.isUnlocked(), "Should be unlocked again")
        assertTrue(
            repository.getEventYear() == 2027,
            "Should store new event year"
        )
    }

    @Test
    fun `no stored state does not trigger year reset`() {
        // Given: No previous unlock state at all (fresh install)
        val repository = FakeUnlockStateRepository()
        val eventDateService = FakeEventDateService()

        // When: Manager is created
        val manager = UnlockConditionManagerImpl(
            eventDateService = eventDateService,
            geofenceService = FakeGeofenceService(),
            unlockStateRepository = repository
        )

        // Then: Should simply be locked (no crash, no error)
        assertFalse(manager.isUnlocked(null), "Should be locked on fresh install")
    }

    // =========================================================================
    // Permission Denial Edge Case Tests
    // =========================================================================

    @Test
    fun `date unlock works when location is null (permission denied scenario)`() {
        // Given: Event has started, but location is null (location permission denied by user)
        val eventDateService = FakeEventDateService(eventStarted = true)
        val (manager, repository, _) = createManager(eventDateService = eventDateService)

        // When: isUnlocked called with null location (simulating denied permission)
        val result = manager.isUnlocked(null)

        // Then: Should still unlock via date condition, not fail due to missing location
        assertTrue(result, "Should unlock via date even when location is null (permission denied)")
        assertTrue(repository.wasSetUnlockedCalled(), "Unlock should be persisted even with null location")
    }

    @Test
    fun `date unlock takes priority over null location when both conditions possible`() {
        // Given: Event started, geofence not triggered (as would happen with null location)
        val eventDateService = FakeEventDateService(eventStarted = true)
        val geofenceService = FakeGeofenceService(withinGeofence = false)
        val (manager, repository, _) = createManager(
            eventDateService = eventDateService,
            geofenceService = geofenceService
        )

        // When: isUnlocked called with null location (no geofence possible)
        val result = manager.isUnlocked(null)

        // Then: Date condition alone is sufficient — null location doesn't block unlock
        assertTrue(result, "Date unlock should succeed regardless of null location")
        assertTrue(repository.wasSetUnlockedCalled(), "Date-triggered unlock should be persisted")
    }

    @Test
    fun `null location does not block date-based unlock persistence across sessions`() {
        // Given: First session — event started, null location (permission denied)
        val eventDateService = FakeEventDateService(eventStarted = true)
        val repository = FakeUnlockStateRepository()

        val firstManager = UnlockConditionManagerImpl(
            eventDateService = eventDateService,
            geofenceService = FakeGeofenceService(),
            unlockStateRepository = repository
        )

        // First session triggers unlock with null location
        val firstResult = firstManager.isUnlocked(null)
        assertTrue(firstResult, "Should unlock in first session via date with null location")
        assertTrue(repository.wasSetUnlockedCalled(), "Should persist after first unlock")

        // When: Second session — new manager instance, same persisted repository state
        // This simulates app restart (same repository with persisted state)
        val secondManager = UnlockConditionManagerImpl(
            eventDateService = FakeEventDateService(eventStarted = false), // date condition no longer true
            geofenceService = FakeGeofenceService(),
            unlockStateRepository = repository
        )

        // Then: Should still be unlocked from persistence, even with null location
        val secondResult = secondManager.isUnlocked(null)
        assertTrue(secondResult, "Should remain unlocked from persistence on app restart")
        assertFalse(secondManager.wasJustUnlocked(), "Should not be 'just unlocked' in second session")
    }
}
