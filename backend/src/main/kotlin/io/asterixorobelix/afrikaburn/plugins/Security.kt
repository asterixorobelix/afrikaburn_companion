package io.asterixorobelix.afrikaburn.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-change-in-production"
    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "io.asterixorobelix.afrikaburn"
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "io.asterixorobelix.afrikaburn-users"
    
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
