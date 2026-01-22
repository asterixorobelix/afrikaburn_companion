package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.repository.ProjectsRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.domain.service.DefaultClock
import io.asterixorobelix.afrikaburn.domain.service.EventDateService
import io.asterixorobelix.afrikaburn.domain.service.EventDateServiceImpl
import io.asterixorobelix.afrikaburn.domain.service.GeofenceService
import io.asterixorobelix.afrikaburn.domain.service.GeofenceServiceImpl
import io.asterixorobelix.afrikaburn.domain.service.UnlockConditionManager
import io.asterixorobelix.afrikaburn.domain.service.UnlockConditionManagerImpl
import kotlinx.datetime.Clock
import org.koin.dsl.module

val domainModule = module {
    single<ProjectsRepository> { ProjectsRepositoryImpl(get()) }

    // Event date detection for Surprise Mode
    single<Clock> { DefaultClock() }
    single<EventDateService> { EventDateServiceImpl(get()) }

    // Geofence detection for Surprise Mode
    single<GeofenceService> { GeofenceServiceImpl(get()) }

    // Unlock condition evaluation for Surprise Mode
    single<UnlockConditionManager> { UnlockConditionManagerImpl(get(), get(), get()) }
}