package io.asterixorobelix.afrikaburn

import io.asterixorobelix.afrikaburn.di.appModule
import org.koin.core.context.startKoin

actual object KoinInitializer {
    actual fun init() {
        // On Android, initialization is handled by AfrikaBurnApplication
        // This is only used for previews or if Application class fails
        startKoin {
            modules(appModule)
        }
    }
}