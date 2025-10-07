package io.asterixorobelix.afrikaburn.plugins

import io.asterixorobelix.afrikaburn.api.artInstallationsApi
import io.asterixorobelix.afrikaburn.api.emergencyApi
import io.asterixorobelix.afrikaburn.api.eventsApi
import io.asterixorobelix.afrikaburn.api.moopApi
import io.asterixorobelix.afrikaburn.api.performancesApi
import io.asterixorobelix.afrikaburn.api.mutantVehiclesApi
import io.asterixorobelix.afrikaburn.api.resourcesApi
import io.asterixorobelix.afrikaburn.api.syncApi
import io.asterixorobelix.afrikaburn.api.themeCampsApi
import io.asterixorobelix.afrikaburn.api.weatherRoutes
import io.asterixorobelix.afrikaburn.domain.HealthResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    status = "healthy",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // API routes
        eventsApi()
        themeCampsApi()
        artInstallationsApi()
        mutantVehiclesApi()
        performancesApi()
        syncApi()
        emergencyApi()
        resourcesApi()
        moopApi()
        weatherRoutes()
    }
}
