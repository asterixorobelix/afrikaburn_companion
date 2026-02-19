package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.domain.service.DefaultClock
import io.asterixorobelix.afrikaburn.domain.service.EventDateService
import io.asterixorobelix.afrikaburn.domain.service.EventDateServiceImpl
import io.asterixorobelix.afrikaburn.domain.service.GeofenceService
import io.asterixorobelix.afrikaburn.domain.service.GeofenceServiceImpl
import io.asterixorobelix.afrikaburn.domain.service.UnlockConditionManager
import io.asterixorobelix.afrikaburn.domain.service.UnlockConditionManagerImpl
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.DeleteCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.ObserveCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.SaveCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.UpdateCampPinLocationUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.projects.GetAllProjectsUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.projects.GetProjectsByTypeUseCase
import kotlinx.datetime.Clock
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    // Event date detection for Surprise Mode
    single<Clock> { DefaultClock() }
    single<EventDateService> { EventDateServiceImpl(get()) }

    // Geofence detection for Surprise Mode
    single<GeofenceService> { GeofenceServiceImpl(get()) }

    // Unlock condition evaluation for Surprise Mode
    single<UnlockConditionManager> { UnlockConditionManagerImpl(get(), get(), get()) }

    // Use cases
    factoryOf(::GetProjectsByTypeUseCase)
    factoryOf(::GetAllProjectsUseCase)
    factoryOf(::ObserveCampPinUseCase)
    factoryOf(::SaveCampPinUseCase)
    factoryOf(::UpdateCampPinLocationUseCase)
    factoryOf(::DeleteCampPinUseCase)
}
