package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.repository.ProjectsRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import org.koin.dsl.module

val domainModule = module {
    single<ProjectsRepository> { ProjectsRepositoryImpl(get()) }
}