package io.asterixorobelix.afrikaburn.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    val jwtSecret = System.getenv("JWT_SECRET")?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException(
            "JWT_SECRET environment variable is not set or is blank. " +
                "Provide a random string of at least 32 characters (e.g. openssl rand -hex 32). " +
                "See backend/.env.example for all required variables."
        )
    check(jwtSecret.length >= 32) {
        "JWT_SECRET must be at least 32 characters long (got ${jwtSecret.length}). " +
            "Generate one with: openssl rand -hex 32"
    }
    val jwtIssuer = System.getenv("JWT_ISSUER")?.trim()?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException(
            "JWT_ISSUER environment variable is not set or is blank. " +
                "Expected format: reverse domain, e.g. 'io.asterixorobelix.afrikaburn'. " +
                "See backend/.env.example for all required variables."
        )
    val jwtAudience = System.getenv("JWT_AUDIENCE")?.trim()?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException(
            "JWT_AUDIENCE environment variable is not set or is blank. " +
                "Expected format: reverse domain with suffix, e.g. 'io.asterixorobelix.afrikaburn-users'. " +
                "See backend/.env.example for all required variables."
        )
    
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "MyProject API"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
