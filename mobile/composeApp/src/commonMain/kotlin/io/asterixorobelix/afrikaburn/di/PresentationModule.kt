package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.presentation.map.MapViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsViewModel
import org.koin.dsl.module

val presentationModule = module {
    factory { ProjectsViewModel(get()) }

    // Factory for ProjectTabViewModel with ProjectType parameter
    factory { (projectType: ProjectType) ->
        ProjectTabViewModel(get(), projectType)
    }

    // Singleton MapViewModel shared between MapScreen and navigation (for navigateToLocation)
    single { MapViewModel(get(), get(), get()) }
}