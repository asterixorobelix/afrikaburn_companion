package io.asterixorobelix.afrikaburn.data.local

import io.asterixorobelix.afrikaburn.data.repository.DatabaseEvent

/**
 * Mock SQLDelight queries for Event
 * Placeholder implementation for compilation
 */
class EventQueries {
    
    fun selectAllEvents(): MockQuery<DatabaseEvent> {
        return MockQuery<DatabaseEvent>(emptyList<DatabaseEvent>())
    }
    
    fun selectEventById(eventId: String): MockQuery<DatabaseEvent?> {
        return MockQuery<DatabaseEvent?>(null)
    }
    
    fun selectCurrentEvent(): MockQuery<DatabaseEvent?> {
        return MockQuery<DatabaseEvent?>(null)
    }
    
    fun selectEventsByDateRange(startDate: Long, endDate: Long): MockQuery<DatabaseEvent> {
        return MockQuery<DatabaseEvent>(emptyList<DatabaseEvent>())
    }
    
    fun insertEvent(
        id: String,
        year: Long,
        startDate: Long,
        endDate: Long,
        centerLatitude: Double,
        centerLongitude: Double,
        radiusKm: Double,
        theme: String,
        isCurrentYear: Long,
        lastUpdated: Long
    ) {
        // Mock implementation
    }
    
    fun updateEvent(
        year: Long,
        startDate: Long,
        endDate: Long,
        centerLatitude: Double,
        centerLongitude: Double,
        radiusKm: Double,
        theme: String,
        isCurrentYear: Long,
        lastUpdated: Long,
        id: String
    ) {
        // Mock implementation
    }
    
    fun deleteEvent(eventId: String) {
        // Mock implementation
    }
    
    fun deleteAllEvents() {
        // Mock implementation
    }
    
    fun selectLastSyncTimestamp(): MockQuery<Long?> {
        return MockQuery<Long?>(null)
    }
    
    fun insertOrUpdateSyncTimestamp(timestamp: Long) {
        // Mock implementation
    }
    
    fun transaction(body: () -> Unit) {
        body()
    }
}


