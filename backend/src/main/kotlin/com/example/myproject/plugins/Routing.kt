package com.example.myproject.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: Long,
    val version: String
)

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        
        get("/health") {
            call.respond(
                HealthResponse(
                    status = "healthy",
                    timestamp = System.currentTimeMillis(),
                    version = "1.0.0"
                )
            )
        }
        
        route("/api/v1") {
            get("/status") {
                call.respond(mapOf("api" to "running", "version" to "1.0.0"))
            }
        }
    }
}