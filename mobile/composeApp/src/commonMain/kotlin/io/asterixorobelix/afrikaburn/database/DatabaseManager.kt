package io.asterixorobelix.afrikaburn.database

import app.cash.sqldelight.db.SqlDriver
import io.asterixorobelix.afrikaburn.database.AfrikaBurnDatabase

/**
 * Database manager that provides access to the SQLDelight database instance
 * Handles database creation and initialization
 */
class DatabaseManager(private val driver: SqlDriver) {
    
    private val database = AfrikaBurnDatabase(driver)
    
    fun getDatabase(): AfrikaBurnDatabase = database
    
    /**
     * Initialize the database with required data
     */
    fun initializeDatabase() {
        // Create singleton SyncManager record if it doesn't exist
        database.syncManagerQueries.selectSyncManager().executeAsOneOrNull()
            ?: database.syncManagerQueries.insertOrUpdateSyncManager(
                lastFullSync = 0,
                lastIncrementalSync = 0,
                totalStorageUsed = 0,
                maxStorageLimit = 2_000_000_000, // 2GB
                syncStatus = "idle",
                errorMessage = null,
                networkType = null
            )
    }
}

/**
 * Expect function to get platform-specific SQL driver
 */
expect fun getSqlDriver(): SqlDriver