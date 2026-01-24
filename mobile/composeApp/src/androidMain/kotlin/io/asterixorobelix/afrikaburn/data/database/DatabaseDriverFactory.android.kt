package io.asterixorobelix.afrikaburn.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseDriverFactory.
 * Uses AndroidSqliteDriver with application Context.
 * Schema migrations are handled via .sqm files in sqldelight directory.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = AfrikaBurnDatabase.Schema,
            context = context,
            name = "afrikaburn.db"
        )
    }
}
