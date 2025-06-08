package com.example.myproject.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-change-in-production"
    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "myproject"
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "myproject-users"
    
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