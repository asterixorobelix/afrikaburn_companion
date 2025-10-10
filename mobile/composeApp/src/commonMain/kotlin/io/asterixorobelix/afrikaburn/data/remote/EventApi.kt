package io.asterixorobelix.afrikaburn.data.remote

import io.asterixorobelix.afrikaburn.domain.model.Event

/**
 * Mock API client for Event
 * Placeholder implementation for compilation
 */
class EventApi {
    
    suspend fun getEvents(): List<Event> {
        // Mock implementation - returns empty list
        return emptyList()
    }
}