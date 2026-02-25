package io.asterixorobelix.afrikaburn.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

/**
 * Resolved JWT configuration after environment variable validation.
 * Constructed by [resolveJwtConfig]; all fields are guaranteed non-blank.
 */
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
)

/** Minimum required length for JWT_SECRET to ensure adequate entropy. */
private const val JWT_SECRET_MIN_LENGTH = 64

/**
 * Reads and validates JWT environment variables.
 * Throws [IllegalStateException] immediately if any required variable is missing,
 * blank, or (for JWT_SECRET) shorter than [JWT_SECRET_MIN_LENGTH] characters.
 *
 * Extracted from [configureSecurity] so it can be unit-tested without a Ktor application context.
 */
fun resolveJwtConfig(env: (String) -> String? = System::getenv): JwtConfig {
    val jwtSecret = env("JWT_SECRET")?.trim()?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException(
            "JWT_SECRET environment variable is not set or is blank. " +
                "Provide a random string of at least $JWT_SECRET_MIN_LENGTH characters (e.g. openssl rand -hex 32). " +
                "See backend/.env.example for all required variables."
        )
    check(jwtSecret.length >= JWT_SECRET_MIN_LENGTH) {
        "JWT_SECRET must be at least $JWT_SECRET_MIN_LENGTH characters long (got ${jwtSecret.length} characters after trimming). " +
            "Generate one with: openssl rand -hex 32"
    }
    val jwtIssuer = env("JWT_ISSUER")?.trim()?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException(
            "JWT_ISSUER environment variable is not set or is blank. " +
                "Expected format: reverse domain, e.g. 'io.asterixorobelix.afrikaburn'. " +
                "See backend/.env.example for all required variables."
        )
    val jwtAudience = env("JWT_AUDIENCE")?.trim()?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException(
            "JWT_AUDIENCE environment variable is not set or is blank. " +
                "Expected format: reverse domain with suffix, e.g. 'io.asterixorobelix.afrikaburn-users'. " +
                "See backend/.env.example for all required variables."
        )
    return JwtConfig(secret = jwtSecret, issuer = jwtIssuer, audience = jwtAudience)
}

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
