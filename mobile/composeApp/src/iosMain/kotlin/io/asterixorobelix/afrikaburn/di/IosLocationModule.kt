package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.platform.IosLocationService
import io.asterixorobelix.afrikaburn.platform.LocationService
import io.asterixorobelix.afrikaburn.platform.IosFileStorage
import io.asterixorobelix.afrikaburn.platform.FileStorage
import org.koin.dsl.module

actual val platformLocationModule = module {
    single<LocationService> {
        IosLocationService()
    }
    single<FileStorage> {
        IosFileStorage()
    }
}