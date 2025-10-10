package io.asterixorobelix.afrikaburn.database

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver

/**
 * Android-specific implementation of SQL driver
 */
actual fun getSqlDriver(): SqlDriver {
    // This will be properly injected via Koin with application context
    throw UnsupportedOperationException("SQL driver must be provided via dependency injection")
}

/**
 * Create Android SQL driver with context
 */
fun createAndroidSqlDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(
        schema = AfrikaBurnDatabase.Schema,
        context = context,
        name = "afrikaburn.db"
    )
}