package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.PersonalScheduleItem
import io.asterixorobelix.afrikaburn.domain.model.ScheduleEventType
import io.asterixorobelix.afrikaburn.domain.model.SchedulePriority
import io.asterixorobelix.afrikaburn.domain.model.EventPerformance
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock

/**
 * Use case for managing personal schedule items with conflict detection
 * Supports adding/removing/updating both event performances and custom items
 * Handles offline storage and time conflict validation
 */
class ManagePersonalScheduleUseCase() {
    // Mock storage for schedule items until proper repository is implemented
    private val scheduleItems = mutableListOf<PersonalScheduleItem>()
    /**
     * Add a new item to personal schedule
     * @param item The schedule item to add
     * @return Validation result with any conflicts
     */
    suspend fun addScheduleItem(item: PersonalScheduleItem): ScheduleValidationResult {
        val conflicts = detectConflicts(item)
        
        return if (conflicts.isEmpty()) {
            scheduleItems.add(item)
            ScheduleValidationResult(
                isValid = true,
                conflicts = emptyList(),
                item = item
            )
        } else {
            ScheduleValidationResult(
                isValid = false,
                conflicts = conflicts,
                item = item
            )
        }
    }

    /**
     * Add with force option to add even with conflicts
     */
    suspend fun addScheduleItemForce(item: PersonalScheduleItem): ScheduleValidationResult {
        val conflicts = detectConflicts(item)
        scheduleItems.add(item)
        
        return ScheduleValidationResult(
            isValid = conflicts.isEmpty(),
            conflicts = conflicts,
            item = item
        )
    }

    /**
     * Remove item from personal schedule
     */
    suspend fun removeScheduleItem(itemId: String) {
        scheduleItems.removeAll { it.id == itemId }
    }

    /**
     * Update existing schedule item
     * @param item Updated schedule item
     * @return Validation result with any new conflicts
     */
    suspend fun updateScheduleItem(item: PersonalScheduleItem): ScheduleValidationResult {
        val conflicts = detectConflicts(item, excludeItemId = item.id)
        
        return if (conflicts.isEmpty()) {
            scheduleItems.removeAll { it.id == item.id }
            scheduleItems.add(item)
            ScheduleValidationResult(
                isValid = true,
                conflicts = emptyList(),
                item = item
            )
        } else {
            ScheduleValidationResult(
                isValid = false,
                conflicts = conflicts,
                item = item
            )
        }
    }

    /**
     * Update with force option to update even with conflicts
     */
    suspend fun updateScheduleItemForce(item: PersonalScheduleItem): ScheduleValidationResult {
        val conflicts = detectConflicts(item, excludeItemId = item.id)
        scheduleItems.removeAll { it.id == item.id }
        scheduleItems.add(item)
        
        return ScheduleValidationResult(
            isValid = conflicts.isEmpty(),
            conflicts = conflicts,
            item = item
        )
    }

    /**
     * Get all personal schedule items with conflict indicators
     */
    fun getPersonalSchedule(): Flow<List<PersonalScheduleItemWithConflicts>> {
        return flow {
            val items = scheduleItems.toList()
            emit(items.map { item ->
                PersonalScheduleItemWithConflicts(
                    item = item,
                    conflicts = detectConflictsForItem(item, items)
                )
            })
        }
    }

    /**
     * Get schedule for specific date range
     */
    fun getScheduleForDateRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<PersonalScheduleItemWithConflicts>> {
        return flow {
            val items = scheduleItems.filter { item ->
                item.startTime >= startTime && item.endTime <= endTime
            }
            emit(items.map { item ->
                PersonalScheduleItemWithConflicts(
                    item = item,
                    conflicts = detectConflictsForItem(item, items)
                )
            })
        }
    }

    /**
     * Add event performance to personal schedule
     */
    suspend fun addPerformanceToSchedule(performanceId: String): ScheduleValidationResult {
        // Mock performance since repository doesn't exist
        val performance = EventPerformance(
            id = performanceId,
            eventId = "event1",
            title = "Mock Performance",
            description = "Mock Description",
            performanceType = io.asterixorobelix.afrikaburn.domain.model.PerformanceType.MUSIC_PERFORMANCE,
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().plus(kotlin.time.Duration.parse("1h")).toLocalDateTime(TimeZone.currentSystemDefault()),
            latitude = -32.25,
            longitude = 20.05,
            venueName = "Main Stage",
            venueId = "venue1",
            performers = listOf("Artist Name"),
            capacity = 100,
            isRegistrationRequired = false,
            registrationUrl = null,
            contactInfo = "Contact info",
            accessibilityInfo = "Accessible",
            ageRestrictions = "All ages",
            participationLevel = io.asterixorobelix.afrikaburn.domain.model.ParticipationLevel.PASSIVE,
            tags = emptyList(),
            audioVisualRequirements = emptyList(),
            photoUrls = emptyList(),
            qrCode = null,
            unlockTimestamp = null,
            isHidden = false,
            lastUpdated = getCurrentTimestamp()
        )

        val scheduleItem = PersonalScheduleItem(
            id = "perf_${performanceId}_${getCurrentTimestamp()}",
            deviceId = "device_id",
            title = performance.title,
            description = performance.performers.joinToString(", ") + " at " + performance.venueName,
            startTime = performance.startTime.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            endTime = performance.endTime.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            isOfficial = true,
            officialEventId = performanceId,
            eventType = ScheduleEventType.PERFORMANCE,
            location = performance.venueName,
            latitude = performance.latitude,
            longitude = performance.longitude,
            priority = SchedulePriority.NORMAL,
            reminderMinutes = null,
            hasConflict = false,
            conflictWith = emptyList(),
            notes = null,
            isAllDay = false,
            backgroundColor = null,
            tags = performance.tags,
            createdAt = getCurrentTimestamp(),
            lastUpdated = getCurrentTimestamp()
        )

        return addScheduleItem(scheduleItem)
    }

    /**
     * Get all conflicts across the entire schedule
     */
    fun getAllConflicts(): Flow<List<ScheduleConflict>> {
        return flow {
            val items = scheduleItems.toList()
            val conflicts = mutableListOf<ScheduleConflict>()
            
            items.forEachIndexed { index, item1 ->
                items.drop(index + 1).forEach { item2 ->
                    if (hasTimeOverlap(item1, item2)) {
                        conflicts.add(
                            ScheduleConflict(
                                item1 = item1,
                                item2 = item2,
                                overlapStartTime = maxOf(item1.startTime, item2.startTime),
                                overlapEndTime = minOf(item1.endTime, item2.endTime)
                            )
                        )
                    }
                }
            }
            
            emit(conflicts)
        }
    }

    /**
     * Clear all personal schedule items
     */
    suspend fun clearSchedule() {
        scheduleItems.clear()
    }

    /**
     * Detect conflicts for a new or updated item
     */
    private suspend fun detectConflicts(
        item: PersonalScheduleItem,
        excludeItemId: String? = null
    ): List<ScheduleConflict> {
        val existingItems = if (excludeItemId != null) {
            scheduleItems.filter { it.id != excludeItemId }
        } else {
            scheduleItems
        }
        
        return existingItems
            .filter { existingItem -> hasTimeOverlap(item, existingItem) }
            .map { conflictingItem ->
                ScheduleConflict(
                    item1 = item,
                    item2 = conflictingItem,
                    overlapStartTime = maxOf(item.startTime, conflictingItem.startTime),
                    overlapEndTime = minOf(item.endTime, conflictingItem.endTime)
                )
            }
    }

    /**
     * Detect conflicts for an item within a list
     */
    private fun detectConflictsForItem(
        item: PersonalScheduleItem,
        allItems: List<PersonalScheduleItem>
    ): List<ScheduleConflict> {
        return allItems
            .filter { it.id != item.id && hasTimeOverlap(item, it) }
            .map { conflictingItem ->
                ScheduleConflict(
                    item1 = item,
                    item2 = conflictingItem,
                    overlapStartTime = maxOf(item.startTime, conflictingItem.startTime),
                    overlapEndTime = minOf(item.endTime, conflictingItem.endTime)
                )
            }
    }

    /**
     * Check if two schedule items have overlapping times
     */
    private fun hasTimeOverlap(item1: PersonalScheduleItem, item2: PersonalScheduleItem): Boolean {
        // Two time ranges overlap if one starts before the other ends
        return item1.startTime < item2.endTime && item2.startTime < item1.endTime
    }

    /**
     * Validate schedule item times
     */
    fun validateScheduleItem(item: PersonalScheduleItem): List<String> {
        val errors = mutableListOf<String>()

        if (item.startTime >= item.endTime) {
            errors.add("End time must be after start time")
        }

        if (item.title.isBlank()) {
            errors.add("Title cannot be empty")
        }

        // Check for reasonable duration (e.g., not longer than 24 hours)
        val durationHours = (item.endTime - item.startTime) / (3600 * 1000)
        if (durationHours > 24) {
            errors.add("Schedule item duration cannot exceed 24 hours")
        }

        return errors
    }

    /**
     * Get schedule statistics
     */
    fun getScheduleStats(): Flow<ScheduleStats> {
        return flow {
            val items = scheduleItems.toList()
            val conflictsFlow = getAllConflicts()
            val conflicts = mutableListOf<ScheduleConflict>()
            conflictsFlow.collect { conflicts.addAll(it) }
            
            emit(ScheduleStats(
                totalItems = items.size,
                eventPerformances = items.count { it.eventType == ScheduleEventType.PERFORMANCE },
                customItems = items.count { it.eventType == ScheduleEventType.PERSONAL },
                totalConflicts = conflicts.size,
                itemsWithReminders = items.count { it.reminderMinutes != null }
            ))
        }
    }
}

/**
 * Schedule conflict between two items
 */
data class ScheduleConflict(
    val item1: PersonalScheduleItem,
    val item2: PersonalScheduleItem,
    val overlapStartTime: Long,
    val overlapEndTime: Long
)

/**
 * Schedule validation result
 */
data class ScheduleValidationResult(
    val isValid: Boolean,
    val conflicts: List<ScheduleConflict>,
    val item: PersonalScheduleItem
)

/**
 * Personal schedule item with associated conflicts
 */
data class PersonalScheduleItemWithConflicts(
    val item: PersonalScheduleItem,
    val conflicts: List<ScheduleConflict>
) {
    val hasConflicts: Boolean get() = conflicts.isNotEmpty()
}

/**
 * Schedule statistics
 */
data class ScheduleStats(
    val totalItems: Int,
    val eventPerformances: Int,
    val customItems: Int,
    val totalConflicts: Int,
    val itemsWithReminders: Int
)