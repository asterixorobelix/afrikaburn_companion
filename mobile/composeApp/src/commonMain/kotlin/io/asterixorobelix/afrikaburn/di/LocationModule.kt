package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.platform.LocationService
import io.asterixorobelix.afrikaburn.platform.createLocationService
import org.koin.dsl.module

/**
 * Koin module for location service dependencies.
 *
 * Provides the platform-specific [LocationService] implementation
 * for GPS location tracking on the map.
 */
val locationModule = module {
    single<LocationService> { createLocationService() }
}
