# Phase 4: Production Features & Monitoring

## Contents

- [Step 1: Add Production Monitoring](#step-1-add-production-monitoring)
- [Step 2: Add Security Features](#step-2-add-security-features)
- [Step 3: Add AI Service Implementation](#step-3-add-ai-service-implementation)
- [Step 4: Update Routing with AI](#step-4-update-routing-with-ai)

---

### Step 1: Add Production Monitoring

**Update `backend/build.gradle.kts` with monitoring:**
```kotlin
dependencies {
    // Add to existing dependencies
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.5")
    implementation("io.sentry:sentry-kotlin-multiplatform:6.34.0")
}
```

**Create `backend/src/main/kotlin/com/yourapp/plugins/StatusPages.kt`:**
```kotlin
package com.yourapp.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // Log error (add Sentry in production)
            application.log.error("Unhandled exception", cause)

            when (cause) {
                is IllegalArgumentException -> {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
                }
                else -> {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }
        }
    }
}
```

### Step 2: Add Security Features

**Create rate limiting:**
```kotlin
// Add to plugins/Security.kt
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

fun Application.configureSecurity() {
    // Existing JWT config...

    install(RateLimit) {
        register(RateLimitName("api")) {
            rateLimiter(limit = 100, refillPeriod = 1.minutes)
            requestKey { call ->
                call.request.origin.remoteHost
            }
        }
    }
}
```

### Step 3: Add AI Service Implementation

**Create `backend/src/main/kotlin/com/yourapp/services/AIService.kt`:**
```kotlin
package com.yourapp.services

import com.yourapp.configuration.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

class AIService(
    private val config: AIConfig,
    private val usageTracker: AIUsageTracker? = null
) {
    private val logger = LoggerFactory.getLogger(AIService::class.java)
    private val httpClient = AIClient.httpClientForEnvironment(config.environment)

    suspend fun chat(message: String, maxRetries: Int = 2): String {
        var attempt = 0
        var lastException: Exception? = null

        val providers = when (config.environment) {
            Environment.DEVELOPMENT -> listOf(config.primaryProvider) // Single provider for dev
            Environment.STAGING -> listOf(config.primaryProvider, AIProvider.OPENAI) // Fallback for staging
            Environment.PRODUCTION -> listOf(config.primaryProvider, AIProvider.OPENAI, AIProvider.GEMINI) // Full fallback
        }

        for (provider in providers) {
            repeat(maxRetries + 1) {
                attempt++
                try {
                    val response = when (provider) {
                        AIProvider.CLAUDE -> chatWithClaude(message)
                        AIProvider.OPENAI -> chatWithOpenAI(message)
                        AIProvider.GEMINI -> chatWithGemini(message)
                    }

                    // Track usage for cost monitoring
                    usageTracker?.trackUsage(provider, estimateTokens(message + response))

                    logger.info("AI response successful - Provider: $provider, Environment: ${config.environment.name}, Attempt: $attempt")
                    return response

                } catch (e: Exception) {
                    lastException = e
                    logger.warn("AI request failed - Provider: $provider, Environment: ${config.environment.name}, Attempt: $attempt, Error: ${e.message}")

                    if (attempt <= maxRetries) {
                        kotlinx.coroutines.delay(1000L * attempt) // Exponential backoff
                    }
                }
            }
        }

        // All providers failed
        val fallbackMessage = when (config.environment) {
            Environment.DEVELOPMENT -> "AI service unavailable in development. Please check your API keys in .env.dev"
            Environment.STAGING -> "AI service temporarily unavailable in staging environment. Please try again later."
            Environment.PRODUCTION -> "I'm sorry, I'm having trouble processing your request right now. Please try again in a moment."
        }

        logger.error("All AI providers failed - Environment: ${config.environment.name}, Last error: ${lastException?.message}")
        return fallbackMessage
    }

    private suspend fun chatWithClaude(message: String): String {
        val claudeConfig = config.providers[AIProvider.CLAUDE]
            ?: throw IllegalStateException("Claude configuration not found")

        if (claudeConfig.apiKey.isBlank()) {
            throw IllegalStateException("Claude API key not configured for ${config.environment.name}")
        }

        val response = httpClient.post(claudeConfig.baseUrl) {
            header("Authorization", "Bearer ${claudeConfig.apiKey}")
            header("Content-Type", "application/json")
            header("anthropic-version", "2023-06-01")

            setBody(Json.encodeToString(mapOf(
                "model" to claudeConfig.model,
                "max_tokens" to claudeConfig.maxTokens,
                "messages" to listOf(
                    mapOf("role" to "user", "content" to message)
                )
            )))
        }

        val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
        return jsonResponse.jsonObject["content"]
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.content
            ?: "Sorry, I couldn't process your request."
    }

    private suspend fun chatWithOpenAI(message: String): String {
        val openaiConfig = config.providers[AIProvider.OPENAI]
            ?: throw IllegalStateException("OpenAI configuration not found")

        if (openaiConfig.apiKey.isBlank()) {
            throw IllegalStateException("OpenAI API key not configured for ${config.environment.name}")
        }

        val response = httpClient.post(openaiConfig.baseUrl) {
            header("Authorization", "Bearer ${openaiConfig.apiKey}")
            header("Content-Type", "application/json")

            setBody(Json.encodeToString(mapOf(
                "model" to openaiConfig.model,
                "max_tokens" to openaiConfig.maxTokens,
                "messages" to listOf(
                    mapOf("role" to "user", "content" to message)
                )
            )))
        }

        val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
        return jsonResponse.jsonObject["choices"]
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.content
            ?: "Sorry, I couldn't process your request."
    }

    private suspend fun chatWithGemini(message: String): String {
        val geminiConfig = config.providers[AIProvider.GEMINI]
            ?: throw IllegalStateException("Gemini configuration not found")

        if (geminiConfig.apiKey.isBlank()) {
            throw IllegalStateException("Gemini API key not configured for ${config.environment.name}")
        }

        val response = httpClient.post("${geminiConfig.baseUrl}/${geminiConfig.model}:generateContent?key=${geminiConfig.apiKey}") {
            header("Content-Type", "application/json")

            setBody(Json.encodeToString(mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(mapOf("text" to message)))
                ),
                "generationConfig" to mapOf(
                    "maxOutputTokens" to geminiConfig.maxTokens
                )
            )))
        }

        val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
        return jsonResponse.jsonObject["candidates"]
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("content")
            ?.jsonObject?.get("parts")
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.content
            ?: "Sorry, I couldn't process your request."
    }

    private fun estimateTokens(text: String): Int {
        // Rough estimation: 1 token ~ 4 characters
        return (text.length / 4).coerceAtLeast(1)
    }
}

// Cost and usage tracking
class AIUsageTracker(private val environment: Environment) {
    private val logger = LoggerFactory.getLogger(AIUsageTracker::class.java)
    private val dailyUsage = mutableMapOf<String, java.util.concurrent.atomic.AtomicInteger>()

    fun trackUsage(provider: AIProvider, tokens: Int) {
        val limits = when (environment) {
            Environment.DEVELOPMENT -> 10_000   // 10K tokens/day - conservative for dev
            Environment.STAGING -> 50_000      // 50K tokens/day - moderate for testing
            Environment.PRODUCTION -> 200_000  // 200K tokens/day - high for production
        }

        val today = java.time.LocalDate.now()
        val key = "${provider.name}-$today"
        val usage = dailyUsage.getOrPut(key) { java.util.concurrent.atomic.AtomicInteger(0) }

        val currentUsage = usage.addAndGet(tokens)

        // Log usage warnings
        when {
            currentUsage > limits -> {
                logger.error("DAILY LIMIT EXCEEDED - Provider: $provider, Environment: $environment, Usage: $currentUsage/$limits tokens")
            }
            currentUsage > (limits * 0.8) -> {
                logger.warn("Approaching daily limit - Provider: $provider, Environment: $environment, Usage: $currentUsage/$limits tokens")
            }
            currentUsage > (limits * 0.5) -> {
                logger.info("Half daily limit reached - Provider: $provider, Environment: $environment, Usage: $currentUsage/$limits tokens")
            }
        }
    }

    fun getDailyUsage(provider: AIProvider): Int {
        val today = java.time.LocalDate.now()
        val key = "${provider.name}-$today"
        return dailyUsage[key]?.get() ?: 0
    }
}
```

### Step 4: Update Routing with AI

**Update `backend/src/main/kotlin/com/yourapp/plugins/Routing.kt`:**
```kotlin
package com.yourapp.plugins

import com.yourapp.configuration.AIConfig
import com.yourapp.configuration.Environment
import com.yourapp.services.AIService
import com.yourapp.services.AIUsageTracker
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting(env: Environment = Environment.current()) {
    val aiConfig = AIConfig.forEnvironment(env)
    val aiUsageTracker = AIUsageTracker(env)
    val aiService = AIService(aiConfig, aiUsageTracker)

    routing {
        // Health check with environment info
        get("/health") {
            call.respond(mapOf(
                "status" to "healthy",
                "timestamp" to System.currentTimeMillis(),
                "version" to "1.0.0",
                "environment" to env.name.lowercase(),
                "build_variant" to (System.getenv("BUILD_VARIANT") ?: "dev")
            ))
        }

        // Environment-specific info endpoint
        get("/info") {
            when (env) {
                Environment.DEVELOPMENT -> {
                    call.respond(mapOf(
                        "environment" to env.name.lowercase(),
                        "ai_provider" to aiConfig.primaryProvider.name,
                        "ai_configured" to aiConfig.providers[aiConfig.primaryProvider]?.apiKey?.isNotEmpty(),
                        "debug_mode" to true,
                        "database_type" to if (System.getenv("DATABASE_URL_DEV")?.contains("postgresql") == true) "postgresql" else "h2",
                        "max_tokens" to aiConfig.providers[aiConfig.primaryProvider]?.maxTokens,
                        "daily_usage" to mapOf(
                            "claude" to aiUsageTracker.getDailyUsage(com.yourapp.configuration.AIProvider.CLAUDE),
                            "openai" to aiUsageTracker.getDailyUsage(com.yourapp.configuration.AIProvider.OPENAI),
                            "gemini" to aiUsageTracker.getDailyUsage(com.yourapp.configuration.AIProvider.GEMINI)
                        )
                    ))
                }
                Environment.STAGING -> {
                    call.respond(mapOf(
                        "environment" to env.name.lowercase(),
                        "ai_provider" to aiConfig.primaryProvider.name,
                        "version" to "1.0.0",
                        "max_tokens" to aiConfig.providers[aiConfig.primaryProvider]?.maxTokens
                    ))
                }
                Environment.PRODUCTION -> {
                    call.respond(mapOf(
                        "environment" to env.name.lowercase(),
                        "version" to "1.0.0",
                        "status" to "operational"
                    ))
                }
            }
        }

        // API routes
        route("/api/v1") {
            get("/status") {
                call.respond(mapOf(
                    "message" to "API is running",
                    "environment" to env.name.lowercase(),
                    "ai_provider" to aiConfig.primaryProvider.name
                ))
            }

            // Environment-specific features
            when (env) {
                Environment.DEVELOPMENT -> {
                    // Development-only endpoints
                    get("/debug/config") {
                        call.respond(mapOf(
                            "environment" to env.name,
                            "ai_providers" to aiConfig.providers.mapValues { (_, config) ->
                                mapOf(
                                    "model" to config.model,
                                    "max_tokens" to config.maxTokens,
                                    "api_key_configured" to config.apiKey.isNotEmpty()
                                )
                            }
                        ))
                    }

                    get("/debug/usage") {
                        call.respond(mapOf(
                            "daily_usage" to mapOf(
                                "claude" to aiUsageTracker.getDailyUsage(com.yourapp.configuration.AIProvider.CLAUDE),
                                "openai" to aiUsageTracker.getDailyUsage(com.yourapp.configuration.AIProvider.OPENAI),
                                "gemini" to aiUsageTracker.getDailyUsage(com.yourapp.configuration.AIProvider.GEMINI)
                            )
                        ))
                    }
                }
                else -> { /* No debug endpoints in staging/production */ }
            }

            // AI endpoints (authentication based on environment)
            authenticate("auth-jwt", optional = env.isDevelopment) {
                route("/ai") {
                    post("/chat") {
                        try {
                            val request = call.receive<ChatRequest>()

                            // Environment-specific validation
                            val maxMessageLength = when (env) {
                                Environment.DEVELOPMENT -> 500   // Short messages in dev
                                Environment.STAGING -> 1000     // Medium messages in staging
                                Environment.PRODUCTION -> 2000  // Long messages in production
                            }

                            if (request.message.length > maxMessageLength) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Message too long for ${env.name.lowercase()} environment. Max: $maxMessageLength characters")
                                )
                                return@post
                            }

                            if (request.message.isBlank()) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Message cannot be empty")
                                )
                                return@post
                            }

                            val response = aiService.chat(request.message)
                            call.respond(ChatResponse(response, env.name.lowercase()))

                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf(
                                    "error" to "Failed to process chat request",
                                    "environment" to env.name.lowercase()
                                )
                            )
                        }
                    }

                    // Environment-specific AI endpoints
                    if (env != Environment.PRODUCTION) {
                        get("/providers") {
                            call.respond(mapOf(
                                "primary" to aiConfig.primaryProvider.name,
                                "available" to aiConfig.providers.keys.map { it.name },
                                "models" to aiConfig.providers.mapValues { it.value.model }
                            ))
                        }
                    }
                }
            }
        }
    }

    log.info("Routing configured for ${env.name} environment")
}

@Serializable
data class ChatRequest(val message: String)

@Serializable
data class ChatResponse(
    val response: String,
    val environment: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```
