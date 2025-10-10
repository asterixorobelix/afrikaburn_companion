package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSourceImpl
import io.asterixorobelix.afrikaburn.data.local.*
import io.asterixorobelix.afrikaburn.data.remote.*
import io.asterixorobelix.afrikaburn.data.storage.*
import io.asterixorobelix.afrikaburn.data.repository.SyncRepositoryStub
import io.asterixorobelix.afrikaburn.data.repository.UserPreferencesRepositoryStub
import io.asterixorobelix.afrikaburn.domain.repository.SyncRepository
import io.asterixorobelix.afrikaburn.domain.repository.UserPreferencesRepository
import org.koin.dsl.module

val dataModule = module {
    single<JsonResourceDataSource> { JsonResourceDataSourceImpl() }
    
    // Local Database Queries (Mock implementations for now)
    single { ArtInstallationQueries() }
    single { CampLocationQueries() }
    single { EventQueries() }
    single { MapPinQueries() }
    single { OfflineMapQueries() }
    single { ThemeCampQueries() }
    
    // Remote APIs (Mock implementations for now)
    single { ArtInstallationApi() }
    single { EventApi() }
    single { MapApi() }
    single { ThemeCampApi() }
    
    // Storage Services
    single { ImageCacheService() }
    single { TileStorageService() }
    single { StoragePriorityManager() }
    
    // Repository implementations
    single<SyncRepository> { SyncRepositoryStub() }
    single<UserPreferencesRepository> { UserPreferencesRepositoryStub() }
    single<io.asterixorobelix.afrikaburn.domain.repository.WeatherRepository> { 
        io.asterixorobelix.afrikaburn.data.repository.WeatherRepositoryStub() 
    }
    single<io.asterixorobelix.afrikaburn.domain.repository.PerformanceRepository> {
        io.asterixorobelix.afrikaburn.data.repository.PerformanceRepositoryStub()
    }
    single<io.asterixorobelix.afrikaburn.domain.repository.MOOPRepository> {
        io.asterixorobelix.afrikaburn.data.repository.MOOPRepositoryStub()
    }
    single<io.asterixorobelix.afrikaburn.platform.DeviceIdProvider> {
        io.asterixorobelix.afrikaburn.platform.DeviceIdProviderStub()
    }
}