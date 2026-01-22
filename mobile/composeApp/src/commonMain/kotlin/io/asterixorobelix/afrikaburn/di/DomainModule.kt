package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.repository.ProjectsRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.domain.service.DefaultClock
import io.asterixorobelix.afrikaburn.domain.service.EventDateService
import io.asterixorobelix.afrikaburn.domain.service.EventDateServiceImpl
import kotlinx.datetime.Clock
import org.koin.dsl.module

val domainModule = module {
    single<ProjectsRepository> { ProjectsRepositoryImpl(get()) }

    // Event date detection for Surprise Mode
    single<Clock> { DefaultClock() }
    single<EventDateService> { EventDateServiceImpl(get()) }
}