package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.platform.CrashLogger
import io.asterixorobelix.afrikaburn.platform.createCrashLogger
import org.koin.dsl.module

/**
 * Koin module for crash logging dependencies
 */
val crashLoggingModule = module {
    single<CrashLogger> { createCrashLogger() }
}