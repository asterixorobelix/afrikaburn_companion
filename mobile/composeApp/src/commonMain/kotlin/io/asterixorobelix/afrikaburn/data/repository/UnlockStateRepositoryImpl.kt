package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.data.database.AfrikaBurnDatabase
import io.asterixorobelix.afrikaburn.domain.repository.UnlockStateRepository
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * SQLDelight-based implementation of UnlockStateRepository.
 *
 * Persists unlock state using a single-row table pattern, scoped to an event year.
 * When the event year changes, the state can be cleared so tabs re-lock.
 */
class UnlockStateRepositoryImpl(
    private val database: AfrikaBurnDatabase,
    private val clock: Clock = Clock.System
) : UnlockStateRepository {

    private val queries get() = database.unlockStateQueries

    override fun isUnlocked(): Boolean {
        return queries.isUnlocked().executeAsOne()
    }

    override fun setUnlocked(eventYear: Int) {
        val now = clock.now().toEpochMilliseconds()
        queries.setUnlocked(now, eventYear.toLong())
    }

    override fun getUnlockedAt(): Instant? {
        return queries.getUnlockState().executeAsOneOrNull()?.let { state ->
            Instant.fromEpochMilliseconds(state.unlockedAt)
        }
    }

    override fun getEventYear(): Int? {
        return queries.getEventYear().executeAsOneOrNull()?.toInt()
    }

    override fun clearUnlockState() {
        queries.clearUnlockState()
    }
}
