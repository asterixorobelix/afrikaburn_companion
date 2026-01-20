package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.database.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific module for DatabaseDriverFactory.
 * iOS doesn't need Context, so the factory is created directly.
 */
actual val platformDatabaseModule: Module = module {
    single { DatabaseDriverFactory() }
}
