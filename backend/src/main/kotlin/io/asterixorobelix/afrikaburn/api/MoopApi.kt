package io.asterixorobelix.afrikaburn.api

import io.asterixorobelix.afrikaburn.domain.ErrorResponse
import io.asterixorobelix.afrikaburn.domain.MOOPReport
import io.asterixorobelix.afrikaburn.domain.MOOPReportRequest
import io.asterixorobelix.afrikaburn.domain.MOOPStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

/**
 * MOOP (Matter Out of Place) API endpoints implementation following the OpenAPI specification.
 * Handles submission of litter and debris reports for cleanup coordination.
 */
fun Route.moopApi() {
    route("/api/v1/events/{eventId}/moop-reports") {
        post {
            try {
                // Extract eventId from path parameters
                val eventId = call.parameters["eventId"]
                if (eventId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "missing_parameter",
                            message = "Event ID is required",
                            details = mapOf("parameter" to "eventId")
                        )
                    )
                    return@post
                }

                // Validate eventId is a valid UUID format
                try {
                    UUID.fromString(eventId)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "invalid_parameter",
                            message = "Event ID must be a valid UUID",
                            details = mapOf("parameter" to "eventId", "value" to eventId)
                        )
                    )
                    return@post
                }

                // Receive and validate request body
                val request = try {
                    call.receive<MOOPReportRequest>()
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "invalid_request_body",
                            message = "Request body is invalid or missing required fields",
                            details = mapOf("cause" to (e.message ?: "Unknown error"))
                        )
                    )
                    return@post
                }

                // Validate required fields and constraints
                val validationErrors = mutableMapOf<String, String>()

                // Validate latitude range (-90 to 90)
                if (request.latitude < -90 || request.latitude > 90) {
                    validationErrors["latitude"] = "Latitude must be between -90 and 90"
                }

                // Validate longitude range (-180 to 180)
                if (request.longitude < -180 || request.longitude > 180) {
                    validationErrors["longitude"] = "Longitude must be between -180 and 180"
                }

                // Validate description is not empty
                if (request.description.isBlank()) {
                    validationErrors["description"] = "Description cannot be empty"
                }

                // Validate description length (max 1000 characters as per spec)
                if (request.description.length > 1000) {
                    validationErrors["description"] = "Description cannot exceed 1000 characters"
                }

                // Return validation errors if any
                if (validationErrors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "validation_error",
                            message = "One or more fields failed validation",
                            details = validationErrors
                        )
                    )
                    return@post
                }

                // Process photo if provided (in production, this would upload to storage)
                var photoUrl: String? = null
                request.photoBase64?.let { base64Photo ->
                    // In production: decode base64, validate image, upload to storage
                    // For now, we'll just create a mock URL
                    photoUrl = "https://storage.afrikaburn.com/moop/${UUID.randomUUID()}.jpg"
                }

                // Create the MOOP report with generated ID and timestamp
                val moopReport = MOOPReport(
                    id = UUID.randomUUID().toString(),
                    latitude = request.latitude,
                    longitude = request.longitude,
                    description = request.description,
                    photoUrl = photoUrl,
                    severity = request.severity,
                    status = MOOPStatus.REPORTED,
                    reportedTimestamp = System.currentTimeMillis()
                )

                // In production: save to database
                // For now, we'll just return the created report

                // Return created MOOP report with 201 status
                call.respond(HttpStatusCode.Created, moopReport)

            } catch (e: Exception) {
                // Handle any unexpected errors
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "internal_server_error",
                        message = "An unexpected error occurred while creating MOOP report",
                        details = mapOf("cause" to (e.message ?: "Unknown error"))
                    )
                )
            }
        }
    }
}