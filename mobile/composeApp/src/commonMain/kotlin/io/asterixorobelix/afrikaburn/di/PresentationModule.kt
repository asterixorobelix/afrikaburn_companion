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

    // Factory for MapViewModel with LocationService and UserCampPinRepository injection
    factory { MapViewModel(get(), get()) }
}