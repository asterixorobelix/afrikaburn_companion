package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.domain.model.Event
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Event operations
 * 
 * Provides access to event data from both local and remote sources,
 * following the repository pattern for clean architecture separation.
 */
interface EventRepository {
    
    /**
     * Get all events from local storage
     */
    suspend fun getAllEvents(): List<Event>
    
    /**
     * Get event by ID
     */
    suspend fun getEventById(eventId: String): Event?
    
    /**
     * Get current active event
     */
    suspend fun getCurrentEvent(): Event?
    
    /**
     * Observe all events as Flow for reactive UI updates
     */
    fun observeAllEvents(): Flow<List<Event>>
    
    /**
     * Observe current event as Flow
     */
    fun observeCurrentEvent(): Flow<Event?>
    
    /**
     * Save event to local storage
     */
    suspend fun saveEvent(event: Event)
    
    /**
     * Save multiple events (batch operation)
     */
    suspend fun saveEvents(events: List<Event>)
    
    /**
     * Update event
     */
    suspend fun updateEvent(event: Event)
    
    /**
     * Delete event by ID
     */
    suspend fun deleteEvent(eventId: String)
    
    /**
     * Delete all events (for fresh sync)
     */
    suspend fun deleteAllEvents()
    
    /**
     * Sync events from remote source
     * @param forceRefresh if true, bypass cache and fetch from network
     */
    suspend fun syncEvents(forceRefresh: Boolean = false): Result<List<Event>>
    
    /**
     * Get events within date range
     */
    suspend fun getEventsByDateRange(startDate: Long, endDate: Long): List<Event>
    
    /**
     * Check if local events data is stale and needs refresh
     */
    suspend fun isDataStale(): Boolean
    
    /**
     * Get last sync timestamp
     */
    suspend fun getLastSyncTimestamp(): Long
    
    /**
     * Update last sync timestamp
     */
    suspend fun updateLastSyncTimestamp(timestamp: Long)
}