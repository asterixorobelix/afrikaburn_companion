package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
import io.asterixorobelix.afrikaburn.models.ProjectType
import org.koin.dsl.module

val presentationModule = module {
    factory { ProjectsViewModel(get()) }
    
    // Factory for ProjectTabViewModel with ProjectType parameter
    factory { (projectType: ProjectType) -> 
        ProjectTabViewModel(get(), projectType) 
    }
}