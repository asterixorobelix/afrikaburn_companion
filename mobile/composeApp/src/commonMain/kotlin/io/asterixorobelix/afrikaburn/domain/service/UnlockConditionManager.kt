package io.asterixorobelix.afrikaburn.domain.service

import io.asterixorobelix.afrikaburn.domain.repository.UnlockStateRepository
import io.asterixorobelix.afrikaburn.platform.LocationData
import kotlin.concurrent.Volatile
import kotlinx.datetime.Instant

/**
 * Manager for evaluating unlock conditions for surprise mode.
 *
 * Determines if the Map and Projects tabs should be visible based on:
 * - Previously persisted unlock state
 * - Event date condition (event has started)
 * - Geofence condition (user is near event location)
 * - Debug bypass flag
 *
 * Key behaviors:
 * - Once unlocked, state is persisted permanently
 * - Date OR geofence condition is sufficient (not both required)
 * - Bypass flag returns unlocked but does NOT persist
 */
interface UnlockConditionManager {

    /**
     * Check if the user has been granted access to hidden tabs.
     *
     * Evaluation order:
     * 1. If bypass enabled -> return true (no persist)
     * 2. If already persisted unlocked -> return true
     * 3. If event started OR within geofence -> persist and return true
     * 4. Otherwise -> return false
     *
     * @param location User's current location, or null if unavailable
     * @return true if tabs should be visible, false otherwise
     */
    fun isUnlocked(location: LocationData?): Boolean

    /**
     * Explicitly check and update unlock state based on current conditions.
     *
     * Call this when location changes to potentially trigger unlock.
     * If conditions are met, the unlock state will be persisted.
     *
     * @param location User's current location, or null if unavailable
     */
    fun checkAndUpdateUnlockState(location: LocationData?)

    /**
     * Get the timestamp when tabs were unlocked.
     *
     * @return The unlock timestamp, or null if not yet unlocked
     */
    fun getUnlockedAt(): Instant?

    /**
     * Check if unlock happened during this session (fresh unlock).
     *
     * Returns true only if the unlock was triggered during THIS app session,
     * not if it was already unlocked from a previous session.
     * Use this to show one-time welcome messages.
     *
     * @return true if unlock happened this session, false otherwise
     */
    fun wasJustUnlocked(): Boolean
}

/**
 * Implementation of UnlockConditionManager.
 *
 * @param eventDateService For checking event date and bypass flag
 * @param geofenceService For checking user proximity to event
 * @param unlockStateRepository For persisting unlock state
 */
class UnlockConditionManagerImpl(
    private val eventDateService: EventDateService,
    private val geofenceService: GeofenceService,
    private val unlockStateRepository: UnlockStateRepository
) : UnlockConditionManager {

    // Tracks if unlock happened during THIS session (in-memory only)
    @Volatile
    private var justUnlockedThisSession = false

    // Current event year from configuration
    private val currentEventYear: Int
        get() = eventDateService.getEventConfig().eventStartDate.year

    init {
        // Clear persisted unlock if the event year has changed (e.g., 2026 -> 2027 update)
        clearStateIfEventYearChanged()
    }

    override fun isUnlocked(location: LocationData?): Boolean {
        // 1. Check bypass flag first (returns true without persisting)
        if (eventDateService.isUnlockBypassed()) {
            return true
        }

        // 2. Check if already persisted as unlocked (from previous session)
        if (unlockStateRepository.isUnlocked()) {
            return true
        }

        // 3. Check conditions and persist if met (fresh unlock)
        val conditionsMet = shouldUnlock(location)
        if (conditionsMet) {
            unlockStateRepository.setUnlocked(currentEventYear)
            justUnlockedThisSession = true
        }

        return conditionsMet
    }

    override fun checkAndUpdateUnlockState(location: LocationData?) {
        // Don't persist if already unlocked or bypassed
        if (eventDateService.isUnlockBypassed() || unlockStateRepository.isUnlocked()) {
            return
        }

        // Persist if conditions are met (fresh unlock)
        if (shouldUnlock(location)) {
            unlockStateRepository.setUnlocked(currentEventYear)
            justUnlockedThisSession = true
        }
    }

    /**
     * Clears persisted unlock state if the stored event year differs
     * from the current event configuration year.
     * This ensures tabs re-lock when an app update ships new event dates.
     */
    private fun clearStateIfEventYearChanged() {
        val storedYear = unlockStateRepository.getEventYear() ?: return
        if (storedYear != currentEventYear) {
            unlockStateRepository.clearUnlockState()
        }
    }

    override fun getUnlockedAt(): Instant? {
        return unlockStateRepository.getUnlockedAt()
    }

    override fun wasJustUnlocked(): Boolean {
        return justUnlockedThisSession
    }

    /**
     * Check if unlock conditions are met (date OR geofence).
     */
    private fun shouldUnlock(location: LocationData?): Boolean {
        // Date condition: event has started
        if (eventDateService.isEventStarted()) {
            return true
        }

        // Geofence condition: user is within event geofence
        if (geofenceService.isUserWithinGeofence(location)) {
            return true
        }

        return false
    }
}
