package io.asterixorobelix.afrikaburn

import android.app.Application
import io.asterixorobelix.afrikaburn.di.appModule
import io.asterixorobelix.afrikaburn.domain.model.EventConfig
import io.asterixorobelix.afrikaburn.domain.service.EventDateService
import io.asterixorobelix.afrikaburn.domain.service.EventDateServiceImpl
import io.asterixorobelix.afrikaburn.domain.service.DefaultClock
import kotlinx.datetime.LocalDate
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AfrikaBurnTestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val testModule = module {
            single<EventDateService>(override = true) {
                EventDateServiceImpl(
                    clock = DefaultClock(),
                    bypassSurpriseMode = true,
                    config = EventConfig(
                        eventStartDate = LocalDate(2026, 4, 27),
                        eventEndDate = LocalDate(2026, 5, 3),
                        eventLatitude = -32.3266,
                        eventLongitude = 19.7437,
                        geofenceRadiusKm = 20.0
                    )
                )
            }
        }

        startKoin {
            androidContext(this@AfrikaBurnTestApplication)
            modules(appModule, testModule)
        }
    }
}
