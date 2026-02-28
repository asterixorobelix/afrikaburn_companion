package io.asterixorobelix.afrikaburn.domain.repository

import kotlin.time.Instant

/**
 * Repository for managing unlock state for surprise mode, scoped to an event year.
 *
 * Once the Map and Projects tabs are unlocked (via date or geofence condition),
 * the unlock state is persisted for the current event year. When the event year
 * changes (e.g., updating from 2026 to 2027), the state is cleared so tabs
 * re-lock until the new event's conditions are met.
 */
interface UnlockStateRepository {
    /**
     * Check if the tabs have been unlocked for the current event year.
     */
    fun isUnlocked(): Boolean

    /**
     * Mark the tabs as unlocked for the given event year.
     * Records the current timestamp as the unlock time.
     *
     * @param eventYear The event year this unlock applies to
     */
    fun setUnlocked(eventYear: Int)

    /**
     * Get the timestamp when tabs were unlocked.
     * @return The unlock timestamp, or null if not yet unlocked
     */
    fun getUnlockedAt(): Instant?

    /**
     * Get the event year stored with the current unlock state.
     * @return The event year, or null if no unlock state exists
     */
    fun getEventYear(): Int?

    /**
     * Clear the unlock state. Used when the event year changes
     * so tabs re-lock for the new event cycle.
     */
    fun clearUnlockState()
}
