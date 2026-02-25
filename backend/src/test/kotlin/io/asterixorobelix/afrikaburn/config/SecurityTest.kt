package io.asterixorobelix.afrikaburn.config

import io.asterixorobelix.afrikaburn.plugins.configureSecurity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication

/** 64-character secret — minimum valid JWT_SECRET (= 32 bytes in hex from openssl rand -hex 32). */
private val VALID_SECRET = "a".repeat(64)
private const val VALID_ISSUER = "io.asterixorobelix.afrikaburn"
private const val VALID_AUDIENCE = "io.asterixorobelix.afrikaburn-users"

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

    // ── JWT_SECRET validation ────────────────────────────────────────────────

    test("missing JWT_SECRET throws IllegalStateException at startup") {
        testApplication {
            application {
                val ex = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(secret = null))
                }
                ex.message shouldContain "JWT_SECRET"
            }
        }
    }

    test("blank JWT_SECRET throws IllegalStateException at startup") {
        testApplication {
            application {
                val ex = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(secret = "   "))
                }
                ex.message shouldContain "JWT_SECRET"
            }
        }
    }

    test("JWT_SECRET shorter than 64 chars throws with correct counts in message") {
        testApplication {
            application {
                val ex = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(secret = "a".repeat(32)))
                }
                // Message must mention both the actual length and the required minimum
                ex.message shouldContain "32"
                ex.message shouldContain "64"
            }
        }
    }

    test("JWT_SECRET of exactly 64 chars is accepted") {
        testApplication {
            application { configureSecurity(env = envWith(secret = "a".repeat(64))) }
        }
    }

    test("JWT_SECRET longer than 64 chars is accepted") {
        testApplication {
            application { configureSecurity(env = envWith(secret = "a".repeat(128))) }
        }
    }

    // ── JWT_ISSUER validation ────────────────────────────────────────────────

    test("missing JWT_ISSUER throws IllegalStateException at startup") {
        testApplication {
            application {
                val ex = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(issuer = null))
                }
                ex.message shouldContain "JWT_ISSUER"
            }
        }
    }

    test("blank JWT_ISSUER throws IllegalStateException at startup") {
        testApplication {
            application {
                val ex = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(issuer = "   "))
                }
                ex.message shouldContain "JWT_ISSUER"
            }
        }
    }

    // ── JWT_AUDIENCE validation ──────────────────────────────────────────────

    test("missing JWT_AUDIENCE throws IllegalStateException at startup") {
        testApplication {
            application {
                val ex = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(audience = null))
                }
                ex.message shouldContain "JWT_AUDIENCE"
            }
        }
    }

    test("blank JWT_AUDIENCE throws IllegalStateException at startup") {
        testApplication {
            application {
                val ex = shouldThrow<IllegalStateException> {
                    configureSecurity(env = envWith(audience = "   "))
                }
                ex.message shouldContain "JWT_AUDIENCE"
            }
        }
    }

    // ── Happy path ───────────────────────────────────────────────────────────

    test("valid env vars install JWT auth plugin — unauthenticated request to protected route returns 401") {
        testApplication {
            application {
                configureSecurity(env = envWith())
                routing {
                    authenticate("auth-jwt") {
                        get("/protected") { /* unreachable without valid token */ }
                    }
                }
            }
            // A request with no Authorization header must be rejected by the JWT plugin
            val response = client.get("/protected")
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }
})
