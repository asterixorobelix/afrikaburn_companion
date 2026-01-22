package io.asterixorobelix.afrikaburn.domain.repository

import kotlinx.datetime.Instant

/**
 * Repository for managing permanent unlock state for surprise mode.
 *
 * Once the Map and Projects tabs are unlocked (via date or geofence condition),
 * the unlock state is persisted permanently. The unlock will never revert.
 */
interface UnlockStateRepository {
    /**
     * Check if the tabs have been permanently unlocked.
     */
    fun isUnlocked(): Boolean

    /**
     * Mark the tabs as permanently unlocked.
     * Records the current timestamp as the unlock time.
     */
    fun setUnlocked()

    /**
     * Get the timestamp when tabs were unlocked.
     * @return The unlock timestamp, or null if not yet unlocked
     */
    fun getUnlockedAt(): Instant?
}
