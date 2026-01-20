package io.asterixorobelix.afrikaburn.data.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQLDelight database drivers.
 * Uses expect/actual pattern for multiplatform support.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
