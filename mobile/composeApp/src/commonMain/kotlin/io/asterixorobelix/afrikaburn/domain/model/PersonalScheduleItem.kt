package io.asterixorobelix.afrikaburn.domain.model

/**
 * Domain model representing a user's personal schedule item
 * 
 * Personal schedule items can be either official events that users add to their
 * schedule, or custom events they create themselves. Includes conflict detection
 * and priority management for overlapping events.
 */
data class PersonalScheduleItem(
    val id: String,
    val deviceId: String,
    val title: String,
    val description: String?,
    val startTime: Long, // Unix timestamp
    val endTime: Long, // Unix timestamp
    val isOfficial: Boolean = false, // True if from official event data
    val officialEventId: String?, // Reference to official event/performance
    val eventType: ScheduleEventType,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val priority: SchedulePriority = SchedulePriority.NORMAL,
    val reminderMinutes: Int?, // Minutes before event to remind
    val hasConflict: Boolean = false,
    val conflictWith: List<String> = emptyList(), // IDs of conflicting schedule items
    val notes: String?,
    val isAllDay: Boolean = false,
    val backgroundColor: String?, // Hex color for calendar display
    val tags: List<String> = emptyList(),
    val createdAt: Long,
    val lastUpdated: Long
) {
    companion object {
        /**
         * Common event types for AfrikaBurn
         */
        val COMMON_EVENT_TYPES = listOf(
            ScheduleEventType.PERFORMANCE,
            ScheduleEventType.WORKSHOP,
            ScheduleEventType.ART_VIEWING,
            ScheduleEventType.THEME_CAMP_VISIT,
            ScheduleEventType.MEAL,
            ScheduleEventType.PERSONAL,
            ScheduleEventType.TRAVEL,
            ScheduleEventType.SLEEP
        )
        
        /**
         * Default reminder times in minutes
         */
        val DEFAULT_REMINDERS = mapOf(
            ScheduleEventType.PERFORMANCE to 30,
            ScheduleEventType.WORKSHOP to 15,
            ScheduleEventType.ART_VIEWING to 60,
            ScheduleEventType.THEME_CAMP_VISIT to 30,
            ScheduleEventType.MEAL to 15,
            ScheduleEventType.PERSONAL to 10,
            ScheduleEventType.TRAVEL to 5,
            ScheduleEventType.SLEEP to 0
        )
        
        /**
         * Maximum duration for events (in hours)
         */
        private const val MAX_EVENT_DURATION_HOURS = 24
        private const val MAX_EVENT_DURATION_MS = MAX_EVENT_DURATION_HOURS * 60 * 60 * 1000L
    }
    
    /**
     * Validation functions
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               deviceId.isNotBlank() &&
               title.isNotBlank() &&
               startTime > 0 &&
               endTime > startTime &&
               isValidDuration() &&
               isValidCoordinates() &&
               createdAt > 0 &&
               lastUpdated > 0
    }
    
    /**
     * Check if event duration is reasonable
     */
    fun isValidDuration(): Boolean {
        return (endTime - startTime) <= MAX_EVENT_DURATION_MS
    }
    
    /**
     * Check if coordinates are valid (if provided)
     */
    fun isValidCoordinates(): Boolean {
        return if (latitude != null && longitude != null) {
            latitude in -90.0..90.0 && longitude in -180.0..180.0
        } else {
            latitude == null && longitude == null
        }
    }
    
    /**
     * Get duration in minutes
     */
    fun getDurationMinutes(): Long {
        return (endTime - startTime) / (60 * 1000)
    }
    
    /**
     * Get duration in hours
     */
    fun getDurationHours(): Double {
        return getDurationMinutes() / 60.0
    }
    
    /**
     * Check if this event overlaps with another
     */
    fun overlapsWith(other: PersonalScheduleItem): Boolean {
        // Events overlap if one starts before the other ends
        return (startTime < other.endTime && endTime > other.startTime)
    }
    
    /**
     * Calculate overlap duration with another event in minutes
     */
    fun getOverlapMinutes(other: PersonalScheduleItem): Long {
        if (!overlapsWith(other)) return 0
        
        val overlapStart = maxOf(startTime, other.startTime)
        val overlapEnd = minOf(endTime, other.endTime)
        
        return (overlapEnd - overlapStart) / (60 * 1000)
    }
    
    /**
     * Check if event is currently happening
     */
    fun isHappeningNow(currentTimestamp: Long = getCurrentTimestamp()): Boolean {
        return currentTimestamp in startTime..endTime
    }
    
    /**
     * Check if event is starting soon
     */
    fun isStartingSoon(currentTimestamp: Long = getCurrentTimestamp(), minutesThreshold: Int = 30): Boolean {
        val timeUntilStart = startTime - currentTimestamp
        val thresholdMs = minutesThreshold * 60 * 1000L
        return timeUntilStart in 0..thresholdMs
    }
    
    /**
     * Check if event is in the past
     */
    fun isPast(currentTimestamp: Long = getCurrentTimestamp()): Boolean {
        return endTime < currentTimestamp
    }
    
    /**
     * Check if event is in the future
     */
    fun isFuture(currentTimestamp: Long = getCurrentTimestamp()): Boolean {
        return startTime > currentTimestamp
    }
    
    /**
     * Get time until event starts (in minutes)
     */
    fun getMinutesUntilStart(currentTimestamp: Long = getCurrentTimestamp()): Long {
        val timeUntil = startTime - currentTimestamp
        return if (timeUntil > 0) timeUntil / (60 * 1000) else 0
    }
    
    /**
     * Get human-readable time until event
     */
    fun getTimeUntilString(currentTimestamp: Long = getCurrentTimestamp()): String {
        val minutesUntil = getMinutesUntilStart(currentTimestamp)
        
        return when {
            isPast(currentTimestamp) -> "Ended"
            isHappeningNow(currentTimestamp) -> "Happening now"
            minutesUntil < 60 -> "${minutesUntil}m"
            minutesUntil < 1440 -> "${minutesUntil / 60}h ${minutesUntil % 60}m"
            else -> "${minutesUntil / 1440}d ${(minutesUntil % 1440) / 60}h"
        }
    }
    
    /**
     * Get formatted duration string
     */
    fun getDurationString(): String {
        val minutes = getDurationMinutes()
        return when {
            minutes < 60 -> "${minutes}min"
            minutes % 60 == 0L -> "${minutes / 60}h"
            else -> "${minutes / 60}h ${minutes % 60}min"
        }
    }
    
    /**
     * Check if reminder should be triggered
     */
    fun shouldTriggerReminder(currentTimestamp: Long = getCurrentTimestamp()): Boolean {
        val reminderTime = reminderMinutes ?: return false
        val reminderTimestamp = startTime - (reminderTime * 60 * 1000L)
        
        return currentTimestamp >= reminderTimestamp && 
               currentTimestamp < startTime &&
               !isPast(currentTimestamp)
    }
    
    /**
     * Get reminder time as timestamp
     */
    fun getReminderTimestamp(): Long? {
        return reminderMinutes?.let { minutes ->
            startTime - (minutes * 60 * 1000L)
        }
    }
    
    /**
     * Create a copy with conflict information updated
     */
    fun withConflictInfo(hasConflict: Boolean, conflictingIds: List<String>): PersonalScheduleItem {
        return copy(
            hasConflict = hasConflict,
            conflictWith = conflictingIds,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Create a copy with updated reminder
     */
    fun withReminder(minutes: Int?): PersonalScheduleItem {
        return copy(
            reminderMinutes = minutes,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Create a copy with updated notes
     */
    fun withNotes(notes: String?): PersonalScheduleItem {
        return copy(
            notes = notes,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Create a copy with updated tags
     */
    fun withTags(tags: List<String>): PersonalScheduleItem {
        return copy(
            tags = tags,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Check if event has specific tag
     */
    fun hasTag(tag: String): Boolean {
        return tags.any { it.equals(tag, ignoreCase = true) }
    }
    
    /**
     * Get conflict severity based on overlap and priority
     */
    fun getConflictSeverity(conflictingEvents: List<PersonalScheduleItem>): ConflictSeverity {
        if (!hasConflict || conflictingEvents.isEmpty()) return ConflictSeverity.NONE
        
        val maxOverlapMinutes = conflictingEvents.maxOfOrNull { getOverlapMinutes(it) } ?: 0
        val highPriorityConflicts = conflictingEvents.any { it.priority == SchedulePriority.HIGH }
        
        return when {
            highPriorityConflicts || maxOverlapMinutes > 60 -> ConflictSeverity.HIGH
            maxOverlapMinutes > 30 -> ConflictSeverity.MEDIUM
            else -> ConflictSeverity.LOW
        }
    }
    
    /**
     * Get suggested resolution for conflicts
     */
    fun getConflictResolutionSuggestions(conflictingEvents: List<PersonalScheduleItem>): List<ConflictResolution> {
        if (!hasConflict) return emptyList()
        
        val suggestions = mutableListOf<ConflictResolution>()
        
        conflictingEvents.forEach { conflicting ->
            val overlapMinutes = getOverlapMinutes(conflicting)
            
            if (overlapMinutes < getDurationMinutes() / 2) {
                suggestions.add(ConflictResolution.PARTIAL_ATTENDANCE)
            }
            
            if (priority != SchedulePriority.HIGH && conflicting.priority == SchedulePriority.HIGH) {
                suggestions.add(ConflictResolution.CHOOSE_HIGHER_PRIORITY)
            }
            
            suggestions.add(ConflictResolution.CHOOSE_MANUALLY)
        }
        
        return suggestions.distinct()
    }
    
    /**
     * Check if event is within Tankwa Karoo event bounds
     */
    fun isWithinEventBounds(): Boolean {
        if (latitude == null || longitude == null) return true // No location = valid
        
        // Tankwa Karoo approximate bounds (from Event model)
        val minLat = -32.35
        val maxLat = -32.15
        val minLng = 19.95
        val maxLng = 20.15
        
        return latitude in minLat..maxLat && longitude in minLng..maxLng
    }
}

/**
 * Types of schedule events
 */
enum class ScheduleEventType {
    PERFORMANCE,        // Official performances
    WORKSHOP,          // Learning/skill workshops
    ART_VIEWING,       // Visiting art installations
    THEME_CAMP_VISIT,  // Visiting theme camps
    MEAL,             // Eating/cooking
    PERSONAL,         // Personal time/activities
    TRAVEL,           // Moving between locations
    SLEEP,            // Rest time
    MEETING,          // Group meetings
    SHOPPING,         // Visiting vendor areas
    OTHER             // Custom category
}

/**
 * Priority levels for schedule items
 */
enum class SchedulePriority {
    LOW,
    NORMAL,
    HIGH
}

/**
 * Conflict severity levels
 */
enum class ConflictSeverity {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Conflict resolution options
 */
enum class ConflictResolution {
    PARTIAL_ATTENDANCE,      // Attend part of each event
    CHOOSE_HIGHER_PRIORITY,  // Choose the higher priority event
    CHOOSE_MANUALLY,         // User decides which to attend
    RESCHEDULE_CUSTOM,       // Reschedule custom events
    IGNORE_CONFLICT          // Accept the conflict
}