package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.Event
import io.asterixorobelix.afrikaburn.domain.repository.EventRepository
import io.asterixorobelix.afrikaburn.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

/**
 * Use case for determining content unlock status based on user location and time.
 * 
 * Implements the following unlock rules:
 * 1. Content is unlocked if user is within event boundary (5km radius)
 * 2. Content is unlocked during event dates regardless of location
 * 3. Content is unlocked after event ends for all users
 * 4. Hidden content remains hidden until unlock conditions are met
 * 5. GPS validation ensures location accuracy before unlocking
 */
class UnlockContentUseCase(
    private val eventRepository: EventRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    /**
     * Check if content should be unlocked for the current user.
     * 
     * @param userLocation Current user location, or null if location not available
     * @param forceRefresh Whether to force refresh event data
     * @return Flow of unlock status with detailed information
     */
    operator fun invoke(
        userLocation: UserLocation? = null,
        forceRefresh: Boolean = false
    ): Flow<ContentUnlockStatus> {
        return combine(
            eventRepository.observeCurrentEvent(),
            flow { emit(userPreferencesRepository.isLocationSharingEnabled()) }
        ) { currentEvent, locationSharingEnabled ->
            when {
                currentEvent == null -> ContentUnlockStatus.NoEvent
                else -> checkUnlockStatus(
                    event = currentEvent,
                    userLocation = userLocation,
                    locationSharingEnabled = locationSharingEnabled
                )
            }
        }
    }

    /**
     * Check unlock status for specific content.
     * 
     * @param contentId ID of the content to check
     * @param userLocation Current user location
     * @param contentType Type of content being checked
     * @return Flow of content-specific unlock status
     */
    fun checkContentUnlock(
        contentId: String,
        userLocation: UserLocation?,
        contentType: ContentType
    ): Flow<ContentUnlockResult> {
        return invoke(userLocation).combine(
            flow { emit(Clock.System.now()) }
        ) { unlockStatus, currentTime ->
            when (unlockStatus) {
                is ContentUnlockStatus.Unlocked -> {
                    ContentUnlockResult.Unlocked(
                        contentId = contentId,
                        contentType = contentType,
                        unlockedAt = currentTime,
                        unlockReason = unlockStatus.reason
                    )
                }
                is ContentUnlockStatus.Locked -> {
                    ContentUnlockResult.Locked(
                        contentId = contentId,
                        contentType = contentType,
                        unlockConditions = unlockStatus.unlockConditions,
                        estimatedUnlockTime = unlockStatus.estimatedUnlockTime
                    )
                }
                ContentUnlockStatus.NoEvent -> {
                    ContentUnlockResult.Unavailable(
                        contentId = contentId,
                        contentType = contentType,
                        reason = "No active event configured"
                    )
                }
            }
        }
    }

    /**
     * Get unlock progress for gamification features.
     * 
     * @param userLocation Current user location
     * @return Flow of unlock progress information
     */
    fun getUnlockProgress(userLocation: UserLocation?): Flow<UnlockProgress> {
        return invoke(userLocation).combine(
            eventRepository.observeCurrentEvent()
        ) { unlockStatus, currentEvent ->
            when {
                currentEvent == null -> UnlockProgress.NoProgress
                unlockStatus is ContentUnlockStatus.Unlocked -> {
                    UnlockProgress.Complete(
                        unlockedAt = Clock.System.now(),
                        method = unlockStatus.reason
                    )
                }
                else -> calculateProgress(currentEvent, userLocation)
            }
        }
    }

    /**
     * Validate GPS location for unlock purposes.
     * 
     * @param location Location to validate
     * @return True if location is valid and accurate enough for unlocking
     */
    fun validateGpsLocation(location: UserLocation): Boolean {
        return location.accuracy != null &&
               location.accuracy <= MAX_LOCATION_ACCURACY_METERS &&
               location.isFromGps &&
               location.timestamp > Clock.System.now().toEpochMilliseconds() - GPS_STALENESS_THRESHOLD_MS
    }

    /**
     * Check if user has ever unlocked content.
     * Useful for showing different UI states.
     */
    suspend fun hasEverUnlocked(): Boolean {
        // This would check stored preference or database
        // For now, return based on current status
        val currentEvent = eventRepository.getCurrentEvent()
        return currentEvent != null && currentEvent.isPast(getCurrentDate())
    }

    /**
     * Get time until content unlocks based on event schedule.
     * 
     * @return Duration until unlock, or null if already unlocked
     */
    suspend fun getTimeUntilUnlock(): kotlin.time.Duration? {
        val currentEvent = eventRepository.getCurrentEvent() ?: return null
        val currentDate = getCurrentDate()
        
        return when {
            currentEvent.isActive(currentDate) || currentEvent.isPast(currentDate) -> null
            else -> {
                val daysUntilStart = currentEvent.getDaysUntilStart(currentDate)
                if (daysUntilStart > 0) daysUntilStart.days else null
            }
        }
    }

    private fun checkUnlockStatus(
        event: Event,
        userLocation: UserLocation?,
        locationSharingEnabled: Boolean
    ): ContentUnlockStatus {
        val currentDate = getCurrentDate()
        
        // Check time-based unlocking first
        when {
            event.isPast(currentDate) -> {
                return ContentUnlockStatus.Unlocked(
                    reason = UnlockReason.EVENT_ENDED,
                    unlockedAt = event.endDate.toEpochDays() * 24 * 60 * 60 * 1000L
                )
            }
            event.isActive(currentDate) -> {
                return ContentUnlockStatus.Unlocked(
                    reason = UnlockReason.EVENT_ACTIVE,
                    unlockedAt = event.startDate.toEpochDays() * 24 * 60 * 60 * 1000L
                )
            }
        }
        
        // Check location-based unlocking
        if (userLocation != null && locationSharingEnabled) {
            if (!validateGpsLocation(userLocation)) {
                return ContentUnlockStatus.Locked(
                    unlockConditions = listOf(
                        UnlockCondition.IMPROVE_GPS_ACCURACY
                    ),
                    estimatedUnlockTime = null
                )
            }
            
            if (event.isWithinUnlockRadius(userLocation.latitude, userLocation.longitude)) {
                return ContentUnlockStatus.Unlocked(
                    reason = UnlockReason.WITHIN_BOUNDARY,
                    unlockedAt = Clock.System.now().toEpochMilliseconds()
                )
            }
        }
        
        // Content is locked - provide unlock conditions
        val unlockConditions = mutableListOf<UnlockCondition>()
        
        if (!locationSharingEnabled) {
            unlockConditions.add(UnlockCondition.ENABLE_LOCATION)
        } else if (userLocation == null) {
            unlockConditions.add(UnlockCondition.PROVIDE_LOCATION)
        } else {
            val distance = event.calculateDistance(
                userLocation.latitude,
                userLocation.longitude,
                event.centerLatitude,
                event.centerLongitude
            )
            unlockConditions.add(
                UnlockCondition.MOVE_TO_EVENT(distance - event.radiusKm)
            )
        }
        
        unlockConditions.add(
            UnlockCondition.WAIT_FOR_EVENT(event.getDaysUntilStart(currentDate))
        )
        
        return ContentUnlockStatus.Locked(
            unlockConditions = unlockConditions,
            estimatedUnlockTime = event.startDate.toEpochDays() * 24 * 60 * 60 * 1000L
        )
    }

    private fun calculateProgress(event: Event, userLocation: UserLocation?): UnlockProgress {
        val currentDate = getCurrentDate()
        val daysUntilEvent = event.getDaysUntilStart(currentDate).coerceAtLeast(0)
        val totalDays = 365 // Assume content locked for a year before event
        
        val timeProgress = if (daysUntilEvent == 0) {
            100
        } else {
            ((totalDays - daysUntilEvent) * 100 / totalDays).coerceIn(0, 99)
        }
        
        val locationProgress = if (userLocation != null && validateGpsLocation(userLocation)) {
            val distance = event.calculateDistance(
                userLocation.latitude,
                userLocation.longitude,
                event.centerLatitude,
                event.centerLongitude
            )
            
            when {
                distance <= event.radiusKm -> 100
                distance <= event.radiusKm * 2 -> 75
                distance <= event.radiusKm * 5 -> 50
                distance <= event.radiusKm * 10 -> 25
                else -> 0
            }
        } else {
            0
        }
        
        return UnlockProgress.InProgress(
            timeProgressPercent = timeProgress,
            locationProgressPercent = locationProgress,
            overallProgressPercent = maxOf(timeProgress, locationProgress),
            nextMilestone = when {
                locationProgress == 75 -> "Get within ${event.radiusKm}km of the event"
                locationProgress == 50 -> "Get within ${event.radiusKm * 2}km of the event"
                locationProgress == 25 -> "Get within ${event.radiusKm * 5}km of the event"
                daysUntilEvent > 30 -> "Wait ${daysUntilEvent} days until event"
                daysUntilEvent > 7 -> "Event starts in ${daysUntilEvent} days"
                daysUntilEvent > 0 -> "Event starts soon!"
                else -> "Content unlocked!"
            }
        )
    }

    private fun getCurrentDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    private fun Event.calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        // Use the utility function from ModelUtils
        return io.asterixorobelix.afrikaburn.domain.model.calculateDistance(lat1, lon1, lat2, lon2)
    }

    companion object {
        // Maximum GPS accuracy for valid unlock (meters)
        private const val MAX_LOCATION_ACCURACY_METERS = 50.0
        
        // GPS data staleness threshold (5 minutes)
        private const val GPS_STALENESS_THRESHOLD_MS = 5 * 60 * 1000
    }
}

/**
 * User location data for content unlocking.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double? = null,
    val altitude: Double? = null,
    val isFromGps: Boolean = true,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

/**
 * Content unlock status.
 */
sealed class ContentUnlockStatus {
    data class Unlocked(
        val reason: UnlockReason,
        val unlockedAt: Long
    ) : ContentUnlockStatus()
    
    data class Locked(
        val unlockConditions: List<UnlockCondition>,
        val estimatedUnlockTime: Long?
    ) : ContentUnlockStatus()
    
    object NoEvent : ContentUnlockStatus()
}

/**
 * Reasons why content was unlocked.
 */
enum class UnlockReason {
    WITHIN_BOUNDARY,    // User is within event boundary
    EVENT_ACTIVE,       // Event is currently happening
    EVENT_ENDED,        // Event has ended
    OVERRIDE           // Manual override (e.g., for testing)
}

/**
 * Conditions that must be met to unlock content.
 */
sealed class UnlockCondition {
    object ENABLE_LOCATION : UnlockCondition()
    object PROVIDE_LOCATION : UnlockCondition()
    object IMPROVE_GPS_ACCURACY : UnlockCondition()
    data class MOVE_TO_EVENT(val distanceKm: Double) : UnlockCondition()
    data class WAIT_FOR_EVENT(val daysUntil: Int) : UnlockCondition()
}

/**
 * Types of content that can be unlocked.
 */
enum class ContentType {
    THEME_CAMPS,
    ART_INSTALLATIONS,
    PERFORMANCES,
    WORKSHOPS,
    MUTANT_VEHICLES,
    EMERGENCY_INFO,
    SURVIVAL_GUIDE,
    COMMUNITY_BOARD,
    GIFT_ECONOMY,
    LEAVE_NO_TRACE
}

/**
 * Result of checking specific content unlock status.
 */
sealed class ContentUnlockResult {
    data class Unlocked(
        val contentId: String,
        val contentType: ContentType,
        val unlockedAt: kotlinx.datetime.Instant,
        val unlockReason: UnlockReason
    ) : ContentUnlockResult()
    
    data class Locked(
        val contentId: String,
        val contentType: ContentType,
        val unlockConditions: List<UnlockCondition>,
        val estimatedUnlockTime: Long?
    ) : ContentUnlockResult()
    
    data class Unavailable(
        val contentId: String,
        val contentType: ContentType,
        val reason: String
    ) : ContentUnlockResult()
}

/**
 * Progress toward unlocking content.
 */
sealed class UnlockProgress {
    data class InProgress(
        val timeProgressPercent: Int,
        val locationProgressPercent: Int,
        val overallProgressPercent: Int,
        val nextMilestone: String
    ) : UnlockProgress()
    
    data class Complete(
        val unlockedAt: kotlinx.datetime.Instant,
        val method: UnlockReason
    ) : UnlockProgress()
    
    object NoProgress : UnlockProgress()
}