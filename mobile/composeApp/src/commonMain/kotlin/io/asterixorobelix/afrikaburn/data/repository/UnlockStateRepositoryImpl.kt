package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.data.database.AfrikaBurnDatabase
import io.asterixorobelix.afrikaburn.domain.repository.UnlockStateRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * SQLDelight-based implementation of UnlockStateRepository.
 *
 * Persists unlock state permanently using a single-row table pattern.
 * Once setUnlocked() is called, isUnlocked() will always return true.
 */
class UnlockStateRepositoryImpl(
    private val database: AfrikaBurnDatabase,
    private val clock: Clock = Clock.System
) : UnlockStateRepository {

    private val queries get() = database.unlockStateQueries

    override fun isUnlocked(): Boolean {
        return queries.isUnlocked().executeAsOne()
    }

    override fun setUnlocked() {
        val now = clock.now().toEpochMilliseconds()
        queries.setUnlocked(now)
    }

    override fun getUnlockedAt(): Instant? {
        return queries.getUnlockState().executeAsOneOrNull()?.let { state ->
            Instant.fromEpochMilliseconds(state.unlockedAt)
        }
    }
}
