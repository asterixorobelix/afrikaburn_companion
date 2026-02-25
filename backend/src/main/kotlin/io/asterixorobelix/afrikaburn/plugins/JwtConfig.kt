package io.asterixorobelix.afrikaburn.plugins

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
internal const val JWT_SECRET_MIN_LENGTH = 64

/**
 * Reads and validates JWT environment variables.
 * Throws [IllegalStateException] immediately if any required variable is missing,
 * blank, or (for JWT_SECRET) shorter than [JWT_SECRET_MIN_LENGTH] characters.
 *
 * Extracted from [configureSecurity] so it can be unit-tested without a Ktor application context.
 */
fun resolveJwtConfig(env: (String) -> String? = System::getenv): JwtConfig {
    val jwtSecret = requireEnvVar(
        env = env,
        key = "JWT_SECRET",
        hint = "Provide a random string of at least $JWT_SECRET_MIN_LENGTH characters " +
            "(e.g. openssl rand -hex 32). See backend/.env.example.",
    )
    check(jwtSecret.length >= JWT_SECRET_MIN_LENGTH) {
        "JWT_SECRET must be at least $JWT_SECRET_MIN_LENGTH characters long " +
            "(got ${jwtSecret.length} after trimming). Generate one with: openssl rand -hex 32"
    }
    val jwtIssuer = requireEnvVar(
        env = env,
        key = "JWT_ISSUER",
        hint = "Expected format: reverse domain, e.g. 'io.asterixorobelix.afrikaburn'.",
    )
    val jwtAudience = requireEnvVar(
        env = env,
        key = "JWT_AUDIENCE",
        hint = "Expected format: reverse domain with suffix, e.g. 'io.asterixorobelix.afrikaburn-users'.",
    )
    return JwtConfig(secret = jwtSecret, issuer = jwtIssuer, audience = jwtAudience)
}

/**
 * Reads an environment variable, throwing [IllegalStateException] if it is absent or blank.
 */
private fun requireEnvVar(
    env: (String) -> String?,
    key: String,
    hint: String,
): String =
    env(key)?.trim()?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException(
            "$key environment variable is not set or is blank. $hint See backend/.env.example.",
        )
