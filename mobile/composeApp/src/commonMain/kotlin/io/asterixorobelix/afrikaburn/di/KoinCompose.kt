package io.asterixorobelix.afrikaburn.di

import androidx.compose.runtime.Composable
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.presentation.map.MapViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun koinProjectsViewModel(): ProjectsViewModel {
    return koinViewModel()
}

@Composable
fun koinProjectTabViewModel(projectType: ProjectType): ProjectTabViewModel {
    return koinViewModel(parameters = { parametersOf(projectType) })
}

@Composable
fun koinMapViewModel(): MapViewModel {
    return koinViewModel()
}
