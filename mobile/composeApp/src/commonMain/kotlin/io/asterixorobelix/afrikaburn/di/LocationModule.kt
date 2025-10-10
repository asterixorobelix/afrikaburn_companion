package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.repository.LocationRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.LocationRepository
import io.asterixorobelix.afrikaburn.platform.FileStorage
import org.koin.dsl.module

expect val platformLocationModule: org.koin.core.module.Module

val locationModule = module {
    includes(platformLocationModule)
    
    // FileStorage will be provided by platform-specific module
    
    // Location Repository
    single<LocationRepository> { LocationRepositoryImpl(get()) }
}