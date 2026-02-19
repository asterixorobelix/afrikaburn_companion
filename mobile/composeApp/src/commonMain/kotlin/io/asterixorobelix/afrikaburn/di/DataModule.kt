package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSourceImpl
import io.asterixorobelix.afrikaburn.data.repository.ProjectsRepositoryImpl
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single<JsonResourceDataSource> { JsonResourceDataSourceImpl() }
    singleOf(::ProjectsRepositoryImpl)
    single<ProjectsRepository> { get<ProjectsRepositoryImpl>() }
}
