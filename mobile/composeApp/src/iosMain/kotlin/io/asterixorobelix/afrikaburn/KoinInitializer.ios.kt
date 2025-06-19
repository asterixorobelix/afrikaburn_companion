package io.asterixorobelix.afrikaburn

import io.asterixorobelix.afrikaburn.di.appModule
import org.koin.core.context.startKoin

actual object KoinInitializer {
    actual fun init() {
        startKoin {
            modules(appModule)
        }
    }
}