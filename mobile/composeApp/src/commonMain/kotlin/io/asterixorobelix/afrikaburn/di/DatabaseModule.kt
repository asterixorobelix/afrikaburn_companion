package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.database.AfrikaBurnDatabase
import io.asterixorobelix.afrikaburn.data.database.DatabaseDriverFactory
import io.asterixorobelix.afrikaburn.data.repository.UserCampPinRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinRepository
import org.koin.dsl.module

/**
 * Koin module for database dependencies.
 *
 * Provides:
 * - DatabaseDriverFactory (platform-specific via platformDatabaseModule)
 * - AfrikaBurnDatabase (SQLDelight generated)
 * - UserCampPinRepository (for camp pin persistence)
 */
val databaseModule = module {
    includes(platformDatabaseModule)

    // Database instance
    single {
        AfrikaBurnDatabase(get<DatabaseDriverFactory>().createDriver())
    }

    // Repository
    single<UserCampPinRepository> { UserCampPinRepositoryImpl(get()) }
}

/**
 * Platform-specific module for DatabaseDriverFactory.
 * Android provides Context, iOS doesn't need it.
 */
expect val platformDatabaseModule: org.koin.core.module.Module
