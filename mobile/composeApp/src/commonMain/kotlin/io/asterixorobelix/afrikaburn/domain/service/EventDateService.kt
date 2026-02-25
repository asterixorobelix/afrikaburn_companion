package io.asterixorobelix.afrikaburn.domain.service

import io.asterixorobelix.afrikaburn.domain.model.EventConfig
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Service for managing event date detection and surprise mode unlock logic.
 * Used to determine if the event has started or is currently active.
 */
interface EventDateService {
    /**
     * Check if the event has started (current date >= event start date).
     * Once true, this should remain true for the rest of time.
     */
    fun isEventStarted(): Boolean

    /**
     * Check if the event is currently active (current date is between start and end dates).
     * Returns true only during the actual event window.
     */
    fun isEventActive(): Boolean

    /**
     * Get the bundled event configuration.
     */
    fun getEventConfig(): EventConfig

    /**
     * Check if surprise mode unlock is bypassed (for debug/testing).
     */
    fun isUnlockBypassed(): Boolean
}

/**
 * Default Clock implementation using the system clock.
 */
@OptIn(ExperimentalTime::class)
class DefaultClock : Clock {
    override fun now(): Instant = Clock.System.now()
}

/**
 * Implementation of EventDateService with injectable Clock for testability.
 *
 * @param clock Clock instance for getting current time (use DefaultClock in production)
 * @param bypassSurpriseMode Debug flag to bypass surprise mode (defaults to false)
 * @param config Event configuration to use (defaults to EventConfig.DEFAULT)
 */
@OptIn(ExperimentalTime::class)
class EventDateServiceImpl(
    private val clock: Clock,
    private val bypassSurpriseMode: Boolean = false,
    private val config: EventConfig = EventConfig.DEFAULT
) : EventDateService {

    private val eventTimezone = TimeZone.of("Africa/Johannesburg")

    override fun isEventStarted(): Boolean {
        val currentDate = getCurrentDate()
        return currentDate >= config.eventStartDate
    }

    override fun isEventActive(): Boolean {
        val currentDate = getCurrentDate()
        return currentDate >= config.eventStartDate && currentDate <= config.eventEndDate
    }

    override fun getEventConfig(): EventConfig = config

    override fun isUnlockBypassed(): Boolean = bypassSurpriseMode

    /**
     * Get the current date in the event timezone (South Africa).
     */
    private fun getCurrentDate(): LocalDate {
        return clock.now().toLocalDateTime(eventTimezone).date
    }
}
