package io.asterixorobelix.afrikaburn.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.asterixorobelix.afrikaburn.domain.model.EventPerformance
import io.asterixorobelix.afrikaburn.domain.model.PersonalScheduleItem
import io.asterixorobelix.afrikaburn.domain.model.ScheduleEventType
import io.asterixorobelix.afrikaburn.domain.model.SchedulePriority
import io.asterixorobelix.afrikaburn.domain.model.getCurrentTimestamp
import io.asterixorobelix.afrikaburn.domain.repository.EventRepository
import io.asterixorobelix.afrikaburn.domain.repository.PerformanceRepository
import io.asterixorobelix.afrikaburn.domain.usecase.ManagePersonalScheduleUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.PersonalScheduleItemWithConflicts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.Instant

data class PersonalScheduleUiState(
    val isLoading: Boolean = false,
    val scheduleItems: List<PersonalScheduleItem> = emptyList(),
    val allPerformances: List<EventPerformance> = emptyList(),
    val selectedDate: LocalDate? = null,
    val showOnlyMySchedule: Boolean = false,
    val conflicts: Set<String> = emptySet(),
    val error: String? = null,
    val selectedPerformance: EventPerformance? = null,
    val isAddingToSchedule: Boolean = false
)

class PersonalScheduleViewModel(
    private val managePersonalScheduleUseCase: ManagePersonalScheduleUseCase,
    private val eventRepository: EventRepository,
    private val performanceRepository: PerformanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalScheduleUiState())
    val uiState: StateFlow<PersonalScheduleUiState> = _uiState.asStateFlow()

    init {
        loadScheduleData()
    }

    private fun loadScheduleData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Get current event
                val currentEvent = eventRepository.getCurrentEvent()
                
                if (currentEvent != null) {
                    // Load performances and personal schedule in parallel
                    combine(
                        performanceRepository.getEventPerformances(currentEvent.id),
                        managePersonalScheduleUseCase.getPersonalSchedule()
                    ) { performances, scheduleItemsWithConflicts ->
                        Pair(performances, scheduleItemsWithConflicts)
                    }.catch { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to load schedule: ${e.message}"
                            ) 
                        }
                    }.collect { (performances, scheduleItemsWithConflicts) ->
                        // Extract just the schedule items and conflicts
                        val scheduleItems = scheduleItemsWithConflicts.map { itemWithConflicts -> itemWithConflicts.item }
                        val conflicts = scheduleItemsWithConflicts
                            .filter { itemWithConflicts -> itemWithConflicts.hasConflicts }
                            .flatMap { itemWithConflicts -> itemWithConflicts.conflicts }
                            .flatMap { conflict -> listOf(conflict.item1.id, conflict.item2.id) }
                            .toSet()
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                allPerformances = performances,
                                scheduleItems = scheduleItems,
                                conflicts = conflicts,
                                error = null
                            ) 
                        }
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No current event found"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load event data: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun detectConflicts(items: List<PersonalScheduleItem>): Set<String> {
        val conflicts = mutableSetOf<String>()
        
        for (i in items.indices) {
            for (j in i + 1 until items.size) {
                val item1 = items[i]
                val item2 = items[j]
                
                // Check if times overlap
                if (item1.startTime < item2.endTime && item2.startTime < item1.endTime) {
                    conflicts.add(item1.id)
                    conflicts.add(item2.id)
                }
            }
        }
        
        return conflicts
    }

    fun addToSchedule(performance: EventPerformance) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToSchedule = true) }
            
            try {
                val result = managePersonalScheduleUseCase.addPerformanceToSchedule(performance.id)
                
                if (result.isValid) {
                    _uiState.update { 
                        it.copy(
                            isAddingToSchedule = false,
                            selectedPerformance = null
                        ) 
                    }
                    // Reload to get updated conflicts
                    loadScheduleData()
                } else {
                    _uiState.update { 
                        it.copy(
                            isAddingToSchedule = false,
                            error = "Schedule conflict detected"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isAddingToSchedule = false,
                        error = "Failed to add to schedule: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun removeFromSchedule(scheduleItemId: String) {
        viewModelScope.launch {
            try {
                managePersonalScheduleUseCase.removeScheduleItem(scheduleItemId)
                // Reload to get updated conflicts
                loadScheduleData()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to remove from schedule: ${e.message}") 
                }
            }
        }
    }

    fun addCustomEvent(
        title: String,
        description: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ) {
        viewModelScope.launch {
            try {
                // Convert LocalDateTime to timestamps
                val startTimestamp = startTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val endTimestamp = endTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                
                val customItem = PersonalScheduleItem(
                    id = "custom_${getCurrentTimestamp()}",
                    deviceId = "device_id", // This should come from a device service
                    title = title,
                    description = description,
                    startTime = startTimestamp,
                    endTime = endTimestamp,
                    isOfficial = false,
                    officialEventId = null,
                    eventType = ScheduleEventType.PERSONAL,
                    location = null,
                    latitude = null,
                    longitude = null,
                    priority = SchedulePriority.NORMAL,
                    reminderMinutes = null,
                    hasConflict = false,
                    conflictWith = emptyList(),
                    notes = null,
                    isAllDay = false,
                    backgroundColor = null,
                    tags = emptyList(),
                    createdAt = getCurrentTimestamp(),
                    lastUpdated = getCurrentTimestamp()
                )
                
                managePersonalScheduleUseCase.addScheduleItem(customItem)
                // Reload to get updated conflicts
                loadScheduleData()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to add custom event: ${e.message}") 
                }
            }
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun toggleShowOnlyMySchedule() {
        _uiState.update { it.copy(showOnlyMySchedule = !it.showOnlyMySchedule) }
    }

    fun selectPerformance(performance: EventPerformance) {
        _uiState.update { it.copy(selectedPerformance = performance) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedPerformance = null) }
    }

    fun getFilteredItems(): List<Any> {
        val state = _uiState.value
        val selectedDate = state.selectedDate
        
        return if (state.showOnlyMySchedule) {
            // Show only personal schedule items
            state.scheduleItems.filter { item ->
                selectedDate == null || 
                Instant.fromEpochMilliseconds(item.startTime).toLocalDateTime(TimeZone.currentSystemDefault()).date == selectedDate
            }
        } else {
            // Show all performances, highlighting those in schedule
            val scheduledPerformanceIds = state.scheduleItems
                .mapNotNull { it.officialEventId }
                .toSet()
            
            state.allPerformances
                .filter { performance ->
                    selectedDate == null || 
                    performance.startTime.date == selectedDate
                }
                .map { performance ->
                    if (scheduledPerformanceIds.contains(performance.id)) {
                        // Return the corresponding schedule item
                        state.scheduleItems.find { it.officialEventId == performance.id }
                            ?: performance
                    } else {
                        performance
                    }
                }
        }
    }

    fun isInSchedule(performanceId: String): Boolean {
        return _uiState.value.scheduleItems.any { it.officialEventId == performanceId }
    }

    fun hasConflict(itemId: String): Boolean {
        return _uiState.value.conflicts.contains(itemId)
    }

    fun refresh() {
        loadScheduleData()
    }
}