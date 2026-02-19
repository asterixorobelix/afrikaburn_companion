package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.presentation.map.MapServices
import io.asterixorobelix.afrikaburn.presentation.map.MapUseCases
import io.asterixorobelix.afrikaburn.presentation.map.MapViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::ProjectsViewModel)

    // ViewModel with ProjectType parameter
    viewModelOf(::ProjectTabViewModel)

    factory {
        MapUseCases(
            observeCampPin = get(),
            saveCampPin = get(),
            updateCampPinLocation = get(),
            deleteCampPin = get(),
            getAllProjects = get()
        )
    }

    factory {
        MapServices(
            locationService = get(),
            crashLogger = get()
        )
    }

    viewModelOf(::MapViewModel)
}
