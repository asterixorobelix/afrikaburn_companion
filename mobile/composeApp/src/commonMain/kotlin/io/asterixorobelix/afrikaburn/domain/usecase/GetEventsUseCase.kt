package io.asterixorobelix.afrikaburn.domain.usecase

import io.asterixorobelix.afrikaburn.domain.model.Event
import io.asterixorobelix.afrikaburn.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case for retrieving events with optional filtering and offline-first behavior
 * 
 * This use case follows clean architecture patterns and provides:
 * - Offline-first data retrieval from EventRepository
 * - Optional filtering by current year
 * - Reactive updates via Flow
 * - Proper error handling with Result type
 * 
 * @property eventRepository Repository for accessing event data
 */
class GetEventsUseCase(
    private val eventRepository: EventRepository
) {
    
    /**
     * Parameters for configuring event retrieval
     * 
     * @property filterCurrentYear If true, only returns events from the current year
     * @property forceRefresh If true, attempts to sync with remote source before returning data
     */
    data class Params(
        val filterCurrentYear: Boolean = false,
        val forceRefresh: Boolean = false
    )
    
    /**
     * Get events as a Flow with optional filtering
     * 
     * @param params Configuration parameters for event retrieval
     * @return Flow emitting list of events, with automatic updates on data changes
     */
    operator fun invoke(params: Params = Params()): Flow<Result<List<Event>>> {
        return eventRepository.observeAllEvents()
            .onStart {
                // Attempt to sync if requested, but don't fail if offline
                if (params.forceRefresh) {
                    try {
                        eventRepository.syncEvents(forceRefresh = true)
                    } catch (e: Exception) {
                        // Log but don't fail - we're offline-first
                        // In a real app, you'd use a proper logging framework
                        println("Failed to sync events: ${e.message}")
                    }
                }
            }
            .map { events ->
                val filteredEvents = if (params.filterCurrentYear) {
                    filterByCurrentYear(events)
                } else {
                    events
                }
                Result.success(filteredEvents)
            }
            .catch { exception ->
                // Emit error result but continue observing
                emit(Result.failure(exception))
            }
    }
    
    /**
     * Get events synchronously (suspend function)
     * 
     * @param params Configuration parameters for event retrieval
     * @return Result containing list of events or error
     */
    suspend fun getEvents(params: Params = Params()): Result<List<Event>> {
        return try {
            // Attempt sync if requested
            if (params.forceRefresh) {
                val syncResult = eventRepository.syncEvents(forceRefresh = true)
                if (syncResult.isFailure) {
                    // Log sync failure but continue with local data
                    println("Sync failed, using local data: ${syncResult.exceptionOrNull()?.message}")
                }
            }
            
            // Get events from local storage
            val events = eventRepository.getAllEvents()
            
            val filteredEvents = if (params.filterCurrentYear) {
                filterByCurrentYear(events)
            } else {
                events
            }
            
            Result.success(filteredEvents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a single event by ID
     * 
     * @param eventId The unique identifier of the event
     * @return Result containing the event or error
     */
    suspend fun getEventById(eventId: String): Result<Event> {
        return try {
            val event = eventRepository.getEventById(eventId)
            if (event != null) {
                Result.success(event)
            } else {
                Result.failure(EventNotFoundException(eventId))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the current active event
     * 
     * @return Result containing the current event or error
     */
    suspend fun getCurrentEvent(): Result<Event> {
        return try {
            val event = eventRepository.getCurrentEvent()
            if (event != null) {
                Result.success(event)
            } else {
                Result.failure(NoCurrentEventException())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe the current active event as a Flow
     * 
     * @return Flow emitting the current event or null
     */
    fun observeCurrentEvent(): Flow<Result<Event?>> {
        return eventRepository.observeCurrentEvent()
            .map { event ->
                Result.success(event)
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }
    
    /**
     * Check if event data needs refresh
     * 
     * @return true if data is stale and should be refreshed
     */
    suspend fun shouldRefreshData(): Boolean {
        return try {
            eventRepository.isDataStale()
        } catch (e: Exception) {
            // If we can't determine staleness, assume we need refresh
            true
        }
    }
    
    /**
     * Filter events by current year
     * 
     * @param events List of all events
     * @return List of events from the current year only
     */
    private fun filterByCurrentYear(events: List<Event>): List<Event> {
        val currentYear = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year
        
        return events.filter { event ->
            event.year == currentYear
        }
    }
    
    /**
     * Custom exceptions for event-related errors
     */
    class EventNotFoundException(eventId: String) : 
        Exception("Event not found with ID: $eventId")
    
    class NoCurrentEventException : 
        Exception("No current active event found")
    
    companion object {
        /**
         * Default sync threshold in hours
         * Data older than this is considered stale
         */
        const val DEFAULT_SYNC_THRESHOLD_HOURS = 24
    }
}