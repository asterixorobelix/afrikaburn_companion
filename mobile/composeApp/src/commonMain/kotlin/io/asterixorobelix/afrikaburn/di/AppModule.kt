package io.asterixorobelix.afrikaburn.di

import org.koin.dsl.module

val appModule = module {
    includes(
        crashLoggingModule,
        dataModule,
        domainModule,
        presentationModule,
        locationModule
    )
}
