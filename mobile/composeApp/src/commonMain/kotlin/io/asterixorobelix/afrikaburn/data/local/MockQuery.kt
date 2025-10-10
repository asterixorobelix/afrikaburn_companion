package io.asterixorobelix.afrikaburn.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Mock query class for SQLDelight replacement during compilation
 */
class MockQuery<T>(private val result: Any?) {
    fun executeAsList(): List<T> {
        return result as? List<T> ?: emptyList()
    }
    
    fun executeAsOneOrNull(): T? {
        return result as? T
    }
    
    fun asFlow(): Flow<MockQuery<T>> {
        return flowOf(this)
    }
    
    fun mapToList(): Flow<List<T>> {
        return flowOf(executeAsList())
    }
    
    fun mapToOneOrNull(): Flow<T?> {
        return flowOf(executeAsOneOrNull())
    }
}