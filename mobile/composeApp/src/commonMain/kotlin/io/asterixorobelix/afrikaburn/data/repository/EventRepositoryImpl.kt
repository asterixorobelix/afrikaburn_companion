package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.Event
import io.asterixorobelix.afrikaburn.domain.repository.EventRepository
import io.asterixorobelix.afrikaburn.data.local.EventQueries
import io.asterixorobelix.afrikaburn.data.remote.EventApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Implementation of EventRepository using SQLDelight for local storage
 * and Ktor client for remote API communication.
 * 
 * Follows offline-first architecture with automatic data freshness management.
 */
class EventRepositoryImpl(
    private val eventQueries: EventQueries,
    private val eventApi: EventApi
) : EventRepository {
    
    companion object {
        private const val CACHE_EXPIRY_HOURS = 24 // Events data is relatively stable
        private const val CACHE_EXPIRY_MS = CACHE_EXPIRY_HOURS * 60 * 60 * 1000L
    }
    
    override suspend fun getAllEvents(): List<Event> = withContext(Dispatchers.IO) {
        eventQueries.selectAllEvents().executeAsList().map { it.toEvent() }
    }
    
    override suspend fun getEventById(eventId: String): Event? = withContext(Dispatchers.IO) {
        eventQueries.selectEventById(eventId).executeAsOneOrNull()?.toEvent()
    }
    
    override suspend fun getCurrentEvent(): Event? = withContext(Dispatchers.IO) {
        eventQueries.selectCurrentEvent().executeAsOneOrNull()?.toEvent()
    }
    
    override fun observeAllEvents(): Flow<List<Event>> {
        return eventQueries.selectAllEvents().mapToList().map { list ->
            list.map { it.toEvent() }
        }
    }
    
    override fun observeCurrentEvent(): Flow<Event?> {
        return eventQueries.selectCurrentEvent().mapToOneOrNull().map { 
            it?.toEvent() 
        }
    }
    
    override suspend fun saveEvent(event: Event) = withContext(Dispatchers.IO) {
        eventQueries.insertEvent(
            id = event.id,
            year = event.year.toLong(),
            startDate = event.startDate.toEpochDays().toLong(),
            endDate = event.endDate.toEpochDays().toLong(),
            centerLatitude = event.centerLatitude,
            centerLongitude = event.centerLongitude,
            radiusKm = event.radiusKm,
            theme = event.theme,
            isCurrentYear = if (event.isCurrentYear) 1L else 0L,
            lastUpdated = event.lastUpdated
        )
    }
    
    override suspend fun saveEvents(events: List<Event>) = withContext(Dispatchers.IO) {
        eventQueries.transaction {
            events.forEach { event ->
                eventQueries.insertEvent(
                    id = event.id,
                    year = event.year.toLong(),
                    startDate = event.startDate.toEpochDays().toLong(),
                    endDate = event.endDate.toEpochDays().toLong(),
                    centerLatitude = event.centerLatitude,
                    centerLongitude = event.centerLongitude,
                    radiusKm = event.radiusKm,
                    theme = event.theme,
                    isCurrentYear = if (event.isCurrentYear) 1L else 0L,
                    lastUpdated = event.lastUpdated
                )
            }
        }
    }
    
    override suspend fun updateEvent(event: Event) = withContext(Dispatchers.IO) {
        eventQueries.updateEvent(
            year = event.year.toLong(),
            startDate = event.startDate.toEpochDays().toLong(),
            endDate = event.endDate.toEpochDays().toLong(),
            centerLatitude = event.centerLatitude,
            centerLongitude = event.centerLongitude,
            radiusKm = event.radiusKm,
            theme = event.theme,
            isCurrentYear = if (event.isCurrentYear) 1L else 0L,
            lastUpdated = event.lastUpdated,
            id = event.id
        )
    }
    
    override suspend fun deleteEvent(eventId: String) = withContext(Dispatchers.IO) {
        eventQueries.deleteEvent(eventId)
    }
    
    override suspend fun deleteAllEvents() = withContext(Dispatchers.IO) {
        eventQueries.deleteAllEvents()
    }
    
    override suspend fun syncEvents(forceRefresh: Boolean): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            // Check if we need to sync
            if (!forceRefresh && !isDataStale()) {
                return@withContext Result.success(getAllEvents())
            }
            
            // Fetch events from remote API
            val remoteEvents = eventApi.getEvents()
            
            // Save to local database
            saveEvents(remoteEvents)
            
            // Update sync timestamp
            updateLastSyncTimestamp(getCurrentTimestamp())
            
            Result.success(remoteEvents)
        } catch (exception: Exception) {
            // Return cached data if available, otherwise return error
            val cachedEvents = getAllEvents()
            if (cachedEvents.isNotEmpty()) {
                Result.success(cachedEvents)
            } else {
                Result.failure(exception)
            }
        }
    }
    
    override suspend fun getEventsByDateRange(startDate: Long, endDate: Long): List<Event> = withContext(Dispatchers.IO) {
        eventQueries.selectEventsByDateRange(startDate, endDate).executeAsList().map { it.toEvent() }
    }
    
    override suspend fun isDataStale(): Boolean = withContext(Dispatchers.IO) {
        val lastSync = getLastSyncTimestamp()
        val currentTime = getCurrentTimestamp()
        (currentTime - lastSync) > CACHE_EXPIRY_MS
    }
    
    override suspend fun getLastSyncTimestamp(): Long = withContext(Dispatchers.IO) {
        eventQueries.selectLastSyncTimestamp().executeAsOneOrNull() ?: 0L
    }
    
    override suspend fun updateLastSyncTimestamp(timestamp: Long) = withContext(Dispatchers.IO) {
        eventQueries.insertOrUpdateSyncTimestamp(timestamp)
    }
    
    /**
     * Get current timestamp
     */
    private fun getCurrentTimestamp(): Long = System.currentTimeMillis()
}

/**
 * Database entity for Event
 */
data class DatabaseEvent(
    val id: String,
    val year: Long,
    val startDate: Long,
    val endDate: Long,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusKm: Double,
    val theme: String,
    val isCurrentYear: Long,
    val lastUpdated: Long
)

/**
 * Extension function to convert database event to domain model
 */
private fun DatabaseEvent.toEvent(): Event {
    return Event(
        id = id,
        year = year.toInt(),
        startDate = LocalDate.fromEpochDays(startDate.toInt()),
        endDate = LocalDate.fromEpochDays(endDate.toInt()),
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        radiusKm = radiusKm,
        theme = theme,
        isCurrentYear = isCurrentYear == 1L,
        lastUpdated = lastUpdated
    )
}