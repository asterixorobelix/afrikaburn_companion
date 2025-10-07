package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.PersonalScheduleItem
import io.asterixorobelix.afrikaburn.domain.model.ScheduleConflict
import io.asterixorobelix.afrikaburn.domain.model.ScheduleItemType
import io.asterixorobelix.afrikaburn.domain.model.ScheduleValidationResult
import io.asterixorobelix.afrikaburn.domain.repository.PersonalScheduleRepository
import io.asterixorobelix.afrikaburn.domain.repository.PerformanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Use case for managing personal schedule items with conflict detection
 * Supports adding/removing/updating both event performances and custom items
 * Handles offline storage and time conflict validation
 */
class ManagePersonalScheduleUseCase(
    private val personalScheduleRepository: PersonalScheduleRepository,
    private val performanceRepository: PerformanceRepository
) {
    /**
     * Add a new item to personal schedule
     * @param item The schedule item to add
     * @return Validation result with any conflicts
     */
    suspend fun addScheduleItem(item: PersonalScheduleItem): ScheduleValidationResult {
        val conflicts = detectConflicts(item)
        
        return if (conflicts.isEmpty()) {
            personalScheduleRepository.addScheduleItem(item)
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
        personalScheduleRepository.addScheduleItem(item)
        
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
        personalScheduleRepository.removeScheduleItem(itemId)
    }

    /**
     * Update existing schedule item
     * @param item Updated schedule item
     * @return Validation result with any new conflicts
     */
    suspend fun updateScheduleItem(item: PersonalScheduleItem): ScheduleValidationResult {
        val conflicts = detectConflicts(item, excludeItemId = item.id)
        
        return if (conflicts.isEmpty()) {
            personalScheduleRepository.updateScheduleItem(item)
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
        personalScheduleRepository.updateScheduleItem(item)
        
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
        return personalScheduleRepository.getAllScheduleItems()
            .map { items ->
                items.map { item ->
                    PersonalScheduleItemWithConflicts(
                        item = item,
                        conflicts = detectConflictsForItem(item, items)
                    )
                }
            }
    }

    /**
     * Get schedule for specific date range
     */
    fun getScheduleForDateRange(
        startTime: Instant,
        endTime: Instant
    ): Flow<List<PersonalScheduleItemWithConflicts>> {
        return personalScheduleRepository.getScheduleItemsForDateRange(startTime, endTime)
            .map { items ->
                items.map { item ->
                    PersonalScheduleItemWithConflicts(
                        item = item,
                        conflicts = detectConflictsForItem(item, items)
                    )
                }
            }
    }

    /**
     * Add event performance to personal schedule
     */
    suspend fun addPerformanceToSchedule(performanceId: String): ScheduleValidationResult {
        val performance = performanceRepository.getPerformanceById(performanceId)
            ?: throw IllegalArgumentException("Performance not found: $performanceId")

        val scheduleItem = PersonalScheduleItem(
            id = "perf_${performanceId}_${System.currentTimeMillis()}",
            type = ScheduleItemType.EVENT_PERFORMANCE,
            title = performance.title,
            description = "${performance.artistName} at ${performance.stageName}",
            startTime = performance.startTime,
            endTime = performance.endTime,
            location = performance.location,
            referenceId = performanceId,
            color = null,
            reminder = null,
            notes = null
        )

        return addScheduleItem(scheduleItem)
    }

    /**
     * Get all conflicts across the entire schedule
     */
    fun getAllConflicts(): Flow<List<ScheduleConflict>> {
        return personalScheduleRepository.getAllScheduleItems()
            .map { items ->
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
                
                conflicts
            }
    }

    /**
     * Clear all personal schedule items
     */
    suspend fun clearSchedule() {
        personalScheduleRepository.clearAllScheduleItems()
    }

    /**
     * Detect conflicts for a new or updated item
     */
    private suspend fun detectConflicts(
        item: PersonalScheduleItem,
        excludeItemId: String? = null
    ): List<ScheduleConflict> {
        val existingItems = personalScheduleRepository.getAllScheduleItems()
            .map { items ->
                if (excludeItemId != null) {
                    items.filter { it.id != excludeItemId }
                } else {
                    items
                }
            }
            .map { items ->
                items.filter { existingItem ->
                    hasTimeOverlap(item, existingItem)
                }
            }
            .map { conflictingItems ->
                conflictingItems.map { conflictingItem ->
                    ScheduleConflict(
                        item1 = item,
                        item2 = conflictingItem,
                        overlapStartTime = maxOf(item.startTime, conflictingItem.startTime),
                        overlapEndTime = minOf(item.endTime, conflictingItem.endTime)
                    )
                }
            }
            .let { flow ->
                val conflicts = mutableListOf<ScheduleConflict>()
                flow.collect { conflicts.addAll(it) }
                conflicts
            }

        return existingItems
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
        val durationHours = (item.endTime.epochSeconds - item.startTime.epochSeconds) / 3600
        if (durationHours > 24) {
            errors.add("Schedule item duration cannot exceed 24 hours")
        }

        return errors
    }

    /**
     * Get schedule statistics
     */
    fun getScheduleStats(): Flow<ScheduleStats> {
        return combine(
            personalScheduleRepository.getAllScheduleItems(),
            getAllConflicts()
        ) { items, conflicts ->
            ScheduleStats(
                totalItems = items.size,
                eventPerformances = items.count { it.type == ScheduleItemType.EVENT_PERFORMANCE },
                customItems = items.count { it.type == ScheduleItemType.CUSTOM },
                totalConflicts = conflicts.size,
                itemsWithReminders = items.count { it.reminder != null }
            )
        }
    }
}

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