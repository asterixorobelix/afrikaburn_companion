package io.asterixorobelix.afrikaburn.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity(
    env: (String) -> String? = System::getenv,
) {
    val config = resolveJwtConfig(env)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "AfrikaBurn API"
            verifier(
                JWT.require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { credential ->
                if (!credential.payload.getClaim("username").isNull &&
                    credential.payload.getClaim("username").asString()?.isNotBlank() == true
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
