package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.platform.AndroidLocationService
import io.asterixorobelix.afrikaburn.platform.LocationService
import io.asterixorobelix.afrikaburn.platform.AndroidFileStorage
import io.asterixorobelix.afrikaburn.platform.FileStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformLocationModule = module {
    single<LocationService> {
        AndroidLocationService(androidContext())
    }
    single<FileStorage> {
        AndroidFileStorage(androidContext())
    }
}