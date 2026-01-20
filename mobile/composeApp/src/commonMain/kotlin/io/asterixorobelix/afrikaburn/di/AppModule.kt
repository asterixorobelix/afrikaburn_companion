package io.asterixorobelix.afrikaburn.di

import org.koin.dsl.module

val appModule = module {
    includes(
        crashLoggingModule,
        databaseModule,
        dataModule,
        domainModule,
        presentationModule,
        locationModule
    )
}
