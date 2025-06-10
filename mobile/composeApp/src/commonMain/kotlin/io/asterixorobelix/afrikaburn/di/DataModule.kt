package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSourceImpl
import org.koin.dsl.module

val dataModule = module {
    single<JsonResourceDataSource> { JsonResourceDataSourceImpl() }
}