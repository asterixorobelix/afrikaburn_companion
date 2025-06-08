package com.example.myproject.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Invalid request", "message" to cause.message)
            )
        }
        
        exception<IllegalStateException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Invalid state", "message" to cause.message)
            )
        }
        
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError, 
                mapOf("error" to "Internal server error")
            )
        }
        
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Endpoint not found")
            )
        }
    }
}