package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.database.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific module for DatabaseDriverFactory.
 * Uses Koin's androidContext to provide Context to the factory.
 */
actual val platformDatabaseModule: Module = module {
    single { DatabaseDriverFactory(get()) }
}
