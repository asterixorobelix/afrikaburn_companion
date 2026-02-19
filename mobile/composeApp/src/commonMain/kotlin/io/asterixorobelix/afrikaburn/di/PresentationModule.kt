package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.presentation.map.MapViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::ProjectsViewModel)

    // ViewModel with ProjectType parameter
    viewModelOf(::ProjectTabViewModel)

    viewModelOf(::MapViewModel)
}
