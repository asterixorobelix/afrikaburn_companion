package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.repository.*
import io.asterixorobelix.afrikaburn.domain.repository.*
import io.asterixorobelix.afrikaburn.domain.usecase.*
import org.koin.dsl.module

val domainModule = module {
    // Existing repository
    single<ProjectsRepository> { ProjectsRepositoryImpl(get()) }
    
    // New repositories
    single<EventRepository> { EventRepositoryImpl(get(), get()) }
    single<ThemeCampRepository> { ThemeCampRepositoryImpl(get(), get()) }
    single<ArtInstallationRepository> { ArtInstallationRepositoryImpl(get(), get(), get()) }
    single<MapRepository> { MapRepositoryImpl(get(), get(), get(), get(), get()) }
    // Repository implementations are in DataModule now
    single<LocationRepository> { LocationRepositoryImpl(get()) }
    
    // Use Cases
    single { GetEventsUseCase(get()) }
    single { SyncContentUseCase(get()) }
    single { UnlockContentUseCase(get(), get()) }
    single { ManagePersonalScheduleUseCase() }
    single { MarkCampLocationUseCase(get()) }
    single { ReportMOOPUseCase(get(), get(), get()) }
    single { GetWeatherAlertsUseCase(get()) }
    single { LocationBasedMessagingUseCase(get()) }
}