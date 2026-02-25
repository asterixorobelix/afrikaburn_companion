package io.asterixorobelix.afrikaburn.config

import io.asterixorobelix.afrikaburn.plugins.configureSecurity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.ktor.server.testing.testApplication

/** A 64-character hex string — the minimum valid JWT_SECRET (non-const so repeat() is allowed). */
private val VALID_SECRET = "a".repeat(64)
private const val VALID_ISSUER = "io.asterixorobelix.afrikaburn"
private const val VALID_AUDIENCE = "io.asterixorobelix.afrikaburn-users"

/** Returns an env-lookup function that yields the supplied values for the three JWT keys. */
private fun envWith(
    secret: String? = VALID_SECRET,
    issuer: String? = VALID_ISSUER,
    audience: String? = VALID_AUDIENCE,
): (String) -> String? = { key ->
    when (key) {
        "JWT_SECRET" -> secret
        "JWT_ISSUER" -> issuer
        "JWT_AUDIENCE" -> audience
        else -> null
    }
}

class SecurityTest : FunSpec({

    test("missing JWT_SECRET throws IllegalStateException at startup") {
        testApplication {
            application {
                val exception = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(secret = null))
                }
                exception.message shouldContain "JWT_SECRET"
            }
        }
    }

    test("blank JWT_SECRET throws IllegalStateException at startup") {
        testApplication {
            application {
                val exception = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(secret = "   "))
                }
                exception.message shouldContain "JWT_SECRET"
            }
        }
    }

    test("JWT_SECRET shorter than 64 chars throws IllegalStateException at startup") {
        testApplication {
            application {
                val exception = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(secret = "a".repeat(63)))
                }
                exception.message shouldContain "64"
            }
        }
    }

    test("JWT_SECRET exactly 63 chars reports correct length in error message") {
        testApplication {
            application {
                val exception = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(secret = "x".repeat(63)))
                }
                exception.message shouldContain "63"
            }
        }
    }

    test("missing JWT_ISSUER throws IllegalStateException at startup") {
        testApplication {
            application {
                val exception = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(issuer = null))
                }
                exception.message shouldContain "JWT_ISSUER"
            }
        }
    }

    test("missing JWT_AUDIENCE throws IllegalStateException at startup") {
        testApplication {
            application {
                val exception = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(audience = null))
                }
                exception.message shouldContain "JWT_AUDIENCE"
            }
        }
    }

    test("valid JWT_SECRET, JWT_ISSUER, and JWT_AUDIENCE allow app to start normally") {
        // No exception thrown means the security gate passed
        testApplication {
            application {
                configureSecurity(env = envWith())
            }
        }
    }

    test("JWT_SECRET of exactly 64 chars is accepted") {
        testApplication {
            application {
                configureSecurity(env = envWith(secret = "a".repeat(64)))
            }
        }
    }

    test("JWT_SECRET longer than 64 chars is accepted") {
        testApplication {
            application {
                configureSecurity(env = envWith(secret = "a".repeat(128)))
            }
        }
    }
})
