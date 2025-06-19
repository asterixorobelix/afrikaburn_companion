package io.asterixorobelix.afrikaburn

import android.app.Application
import io.asterixorobelix.afrikaburn.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AfrikaBurnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@AfrikaBurnApplication)
            modules(appModule)
        }
    }
}