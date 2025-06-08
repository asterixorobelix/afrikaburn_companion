package io.asterixorobelix.afrikaburn.plugins

import io.asterixorobelix.afrikaburn.domain.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("bad_request", cause.message ?: "Invalid request")
                    )
                }
                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("internal_error", "Internal server error")
                    )
                }
            }
        }
    }
}
