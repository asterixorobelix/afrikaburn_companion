# Phase 1: Repository & Project Foundation (Time: 30 minutes)

## Contents

- [Step 1: Create Unified Repository Structure](#step-1-create-unified-repository-structure)
- [Step 2: Create Root Configuration Files](#step-2-create-root-configuration-files)
- [Step 3: Backend Project Setup](#step-3-backend-project-setup)
- [Step 4: Shared Module Setup](#step-4-shared-module-setup)
- [Step 5: Backend Application Structure](#step-5-backend-application-structure)
- [Step 6: Create Essential Scripts](#step-6-create-essential-scripts)
- [Step 7: Create Comprehensive Documentation](#step-7-create-comprehensive-documentation)
- [Step 8: Create GitHub Repository](#step-8-create-github-repository)

---

### Step 1: Create Unified Repository Structure

**Execute these commands in order:**

```bash
# Create main project directory
mkdir my-kotlin-project
cd my-kotlin-project

# Initialize git
git init

# Create unified project structure
mkdir -p {backend,mobile,shared,docs,scripts}

# Backend structure (Ktor)
mkdir -p backend/src/main/kotlin/com/yourapp/{domain,infrastructure,plugins,configuration,util}
mkdir -p backend/src/test/kotlin/com/yourapp
mkdir -p backend/src/main/resources

# Mobile structure (Compose Multiplatform)
mkdir -p mobile/shared/src/{commonMain,androidMain,iosMain}/kotlin/com/yourapp
mkdir -p mobile/androidApp/src/main/kotlin/com/yourapp
mkdir -p mobile/iosApp

# Shared domain models
mkdir -p shared/src/{commonMain,androidMain,iosMain,jvmMain}/kotlin/com/yourapp

# Documentation and scripts
mkdir -p docs/{api,mobile,deployment}
mkdir -p scripts/{deployment,development}
```

### Step 2: Create Root Configuration Files

**Create `settings.gradle.kts` (Root):**
```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "my-kotlin-project"

// Backend module
include(":backend")

// Shared domain models
include(":shared")

// Mobile modules (when ready)
// include(":mobile:shared")
// include(":mobile:androidApp")
```

**Create `build.gradle.kts` (Root):**
```kotlin
plugins {
    // Apply plugins to subprojects
    kotlin("jvm") version "1.9.10" apply false
    kotlin("multiplatform") version "1.9.10" apply false
    kotlin("android") version "1.9.10" apply false
    kotlin("plugin.serialization") version "1.9.10" apply false
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
}

allprojects {
    group = "com.yourapp"
    version = "1.0.0"
}
```

### Step 3: Backend Project Setup

**Create `backend/build.gradle.kts`:**
```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

application {
    mainClass.set("com.yourapp.ApplicationKt")
}

// Build variants for dev and prod
val buildVariant = project.findProperty("buildVariant") as String? ?: "dev"

// Essential for deployment
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.yourapp.ApplicationKt"
        attributes["Build-Variant"] = buildVariant
    }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Environment-specific tasks
tasks.register("runDev") {
    group = "application"
    description = "Run the application with development configuration"
    dependsOn("classes")
    doLast {
        javaexec {
            classpath = sourceSets.main.get().runtimeClasspath
            mainClass.set("com.yourapp.ApplicationKt")
            environment("BUILD_VARIANT", "dev")
            environment("KTOR_ENV", "development")
        }
    }
}

tasks.register("runProd") {
    group = "application"
    description = "Run the application with production configuration"
    dependsOn("classes")
    doLast {
        javaexec {
            classpath = sourceSets.main.get().runtimeClasspath
            mainClass.set("com.yourapp.ApplicationKt")
            environment("BUILD_VARIANT", "prod")
            environment("KTOR_ENV", "production")
        }
    }
}

tasks.register("buildDev") {
    group = "build"
    description = "Build the application for development"
    dependsOn("jar")
    doFirst {
        project.setProperty("buildVariant", "dev")
    }
}

tasks.register("buildProd") {
    group = "build"
    description = "Build the application for production"
    dependsOn("jar")
    doFirst {
        project.setProperty("buildVariant", "prod")
    }
}

val ktor_version = "2.3.5"
val kotlin_version = "1.9.10"
val logback_version = "1.4.11"
val exposed_version = "0.44.1"

dependencies {
    // Ktor core
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")

    // Serialization & Content Negotiation
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")

    // Security & Headers
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // AI API Clients
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### Step 4: Shared Module Setup

**Create `shared/build.gradle.kts`:**
```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm() // For backend

    // Mobile targets (uncomment when ready)
    // android()
    // iosX64()
    // iosArm64()
    // iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            }
        }

        val jvmMain by getting {
            dependencies {
                // JVM/Backend specific dependencies
            }
        }

        // Mobile dependencies (when ready)
        // val androidMain by getting
        // val iosMain by getting
    }
}
```

### Step 5: Backend Application Structure

**Create `backend/src/main/kotlin/com/yourapp/Application.kt`:**
```kotlin
package com.yourapp

import com.yourapp.plugins.*
import com.yourapp.configuration.Environment
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val environment = Environment.current()
    val port = System.getenv("PORT")?.toInt() ?: when (environment) {
        Environment.DEVELOPMENT -> 8080
        Environment.STAGING -> 8080
        Environment.PRODUCTION -> 8080
    }

    println("Starting application in ${environment.name} mode on port $port")

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val env = Environment.current()

    // Configure plugins based on environment
    configureSecurity(env)
    configureSerialization()
    configureDatabases(env)
    configureHTTP(env)
    configureMonitoring(env)
    configureRouting(env)

    if (env != Environment.PRODUCTION) {
        configureDevTools()
    }
}
```

**Create AI Service Configuration `backend/src/main/kotlin/com/yourapp/configuration/AIConfig.kt`:**
```kotlin
package com.yourapp.configuration

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Environment detection
enum class Environment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION;

    companion object {
        fun current(): Environment {
            val buildVariant = System.getenv("BUILD_VARIANT")?.uppercase()
            val ktorEnv = System.getenv("KTOR_ENV")?.uppercase()
            val appEnv = System.getenv("APP_ENVIRONMENT")?.uppercase()

            return when {
                buildVariant == "PROD" || ktorEnv == "PRODUCTION" || appEnv == "PRODUCTION" -> PRODUCTION
                buildVariant == "STAGING" || ktorEnv == "STAGING" || appEnv == "STAGING" -> STAGING
                else -> DEVELOPMENT
            }
        }
    }

    val isDevelopment: Boolean get() = this == DEVELOPMENT
    val isProduction: Boolean get() = this == PRODUCTION
    val isStaging: Boolean get() = this == STAGING
}

data class AIConfig(
    val primaryProvider: AIProvider = AIProvider.CLAUDE,
    val providers: Map<AIProvider, AIProviderConfig>,
    val environment: Environment
) {
    companion object {
        fun forEnvironment(env: Environment): AIConfig {
            val providers = when (env) {
                Environment.DEVELOPMENT -> mapOf(
                    AIProvider.CLAUDE to AIProviderConfig(
                        apiKey = System.getenv("CLAUDE_API_KEY_DEV") ?: System.getenv("CLAUDE_API_KEY") ?: "",
                        baseUrl = "https://api.anthropic.com/v1/messages",
                        model = "claude-3-5-sonnet-20241022",
                        maxTokens = 1000 // Lower for dev to save costs
                    ),
                    AIProvider.OPENAI to AIProviderConfig(
                        apiKey = System.getenv("OPENAI_API_KEY_DEV") ?: System.getenv("OPENAI_API_KEY") ?: "",
                        baseUrl = "https://api.openai.com/v1/chat/completions",
                        model = "gpt-4o-mini", // Use cheaper model for dev
                        maxTokens = 500
                    ),
                    AIProvider.GEMINI to AIProviderConfig(
                        apiKey = System.getenv("GEMINI_API_KEY_DEV") ?: System.getenv("GEMINI_API_KEY") ?: "",
                        baseUrl = "https://generativelanguage.googleapis.com/v1beta/models",
                        model = "gemini-1.5-flash",
                        maxTokens = 500
                    )
                )
                Environment.STAGING -> mapOf(
                    AIProvider.CLAUDE to AIProviderConfig(
                        apiKey = System.getenv("CLAUDE_API_KEY_STAGING") ?: System.getenv("CLAUDE_API_KEY") ?: "",
                        baseUrl = "https://api.anthropic.com/v1/messages",
                        model = "claude-3-5-sonnet-20241022",
                        maxTokens = 2000
                    ),
                    AIProvider.OPENAI to AIProviderConfig(
                        apiKey = System.getenv("OPENAI_API_KEY_STAGING") ?: System.getenv("OPENAI_API_KEY") ?: "",
                        baseUrl = "https://api.openai.com/v1/chat/completions",
                        model = "gpt-4o-mini",
                        maxTokens = 1000
                    ),
                    AIProvider.GEMINI to AIProviderConfig(
                        apiKey = System.getenv("GEMINI_API_KEY_STAGING") ?: System.getenv("GEMINI_API_KEY") ?: "",
                        baseUrl = "https://generativelanguage.googleapis.com/v1beta/models",
                        model = "gemini-1.5-flash",
                        maxTokens = 1000
                    )
                )
                Environment.PRODUCTION -> mapOf(
                    AIProvider.CLAUDE to AIProviderConfig(
                        apiKey = System.getenv("CLAUDE_API_KEY") ?: "",
                        baseUrl = "https://api.anthropic.com/v1/messages",
                        model = "claude-3-5-sonnet-20241022",
                        maxTokens = 4000
                    ),
                    AIProvider.OPENAI to AIProviderConfig(
                        apiKey = System.getenv("OPENAI_API_KEY") ?: "",
                        baseUrl = "https://api.openai.com/v1/chat/completions",
                        model = "gpt-4o",
                        maxTokens = 2000
                    ),
                    AIProvider.GEMINI to AIProviderConfig(
                        apiKey = System.getenv("GEMINI_API_KEY") ?: "",
                        baseUrl = "https://generativelanguage.googleapis.com/v1beta/models",
                        model = "gemini-1.5-pro",
                        maxTokens = 2000
                    )
                )
            }

            return AIConfig(
                primaryProvider = AIProvider.CLAUDE,
                providers = providers,
                environment = env
            )
        }
    }
}

enum class AIProvider {
    CLAUDE, OPENAI, GEMINI
}

data class AIProviderConfig(
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val maxTokens: Int = 1000
)

// AI HTTP Client with environment-specific logging
object AIClient {
    fun httpClientForEnvironment(env: Environment) = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = env.isDevelopment
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = when (env) {
                Environment.DEVELOPMENT -> LogLevel.ALL
                Environment.STAGING -> LogLevel.INFO
                Environment.PRODUCTION -> LogLevel.NONE
            }
        }
    }
}
```

**Create `backend/src/main/kotlin/com/yourapp/plugins/Databases.kt`:**
```kotlin
package com.yourapp.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.yourapp.configuration.Environment
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases(env: Environment = Environment.current()) {
    val (url, driver, user, password, poolSize) = when (env) {
        Environment.DEVELOPMENT -> {
            val devUrl = System.getenv("DATABASE_URL_DEV")
                ?: System.getenv("DATABASE_URL")
                ?: "jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"

            if (devUrl.startsWith("jdbc:postgresql")) {
                listOf(
                    devUrl,
                    "org.postgresql.Driver",
                    System.getenv("DATABASE_USER_DEV") ?: System.getenv("DATABASE_USER") ?: "postgres",
                    System.getenv("DATABASE_PASSWORD_DEV") ?: System.getenv("DATABASE_PASSWORD") ?: "",
                    5 // Small pool for dev
                )
            } else {
                listOf(devUrl, "org.h2.Driver", "sa", "", 3)
            }
        }

        Environment.STAGING -> {
            val stagingUrl = System.getenv("DATABASE_URL_STAGING")
                ?: System.getenv("DATABASE_URL")
                ?: "jdbc:h2:mem:stagingdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"

            if (stagingUrl.startsWith("jdbc:postgresql")) {
                listOf(
                    stagingUrl,
                    "org.postgresql.Driver",
                    System.getenv("DATABASE_USER_STAGING") ?: System.getenv("DATABASE_USER") ?: "postgres",
                    System.getenv("DATABASE_PASSWORD_STAGING") ?: System.getenv("DATABASE_PASSWORD") ?: "",
                    10 // Medium pool for staging
                )
            } else {
                listOf(stagingUrl, "org.h2.Driver", "sa", "", 5)
            }
        }

        Environment.PRODUCTION -> {
            val prodUrl = System.getenv("DATABASE_URL")
                ?: throw IllegalStateException("DATABASE_URL is required for production")

            listOf(
                prodUrl,
                if (prodUrl.startsWith("jdbc:postgresql")) "org.postgresql.Driver" else "org.h2.Driver",
                System.getenv("DATABASE_USER") ?: "postgres",
                System.getenv("DATABASE_PASSWORD") ?: "",
                System.getenv("DB_POOL_SIZE")?.toInt() ?: 20 // Large pool for production
            )
        }
    }

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        driverClassName = driver
        username = user
        password = password
        maximumPoolSize = poolSize

        // Environment-specific connection settings
        when (env) {
            Environment.DEVELOPMENT -> {
                connectionTimeout = 10000 // 10 seconds
                idleTimeout = 300000 // 5 minutes
                maxLifetime = 900000 // 15 minutes
                leakDetectionThreshold = 10000 // 10 seconds for dev debugging
            }
            Environment.STAGING -> {
                connectionTimeout = 20000 // 20 seconds
                idleTimeout = 600000 // 10 minutes
                maxLifetime = 1200000 // 20 minutes
                leakDetectionThreshold = 30000 // 30 seconds
            }
            Environment.PRODUCTION -> {
                connectionTimeout = 30000 // 30 seconds
                idleTimeout = 600000 // 10 minutes
                maxLifetime = 1800000 // 30 minutes
                leakDetectionThreshold = 0 // Disabled in production
            }
        }
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    log.info("Database configured for ${env.name} environment with pool size: $poolSize")
}
```

**Create other essential plugin files:**

`backend/src/main/kotlin/com/yourapp/plugins/Security.kt`:
```kotlin
package com.yourapp.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.yourapp.configuration.Environment
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

fun Application.configureSecurity(env: Environment = Environment.current()) {
    val jwtSecret = when (env) {
        Environment.DEVELOPMENT -> System.getenv("JWT_SECRET_DEV")
            ?: System.getenv("JWT_SECRET")
            ?: "development-secret-key-not-for-production-use-only-dev"
        Environment.STAGING -> System.getenv("JWT_SECRET_STAGING")
            ?: System.getenv("JWT_SECRET")
            ?: throw IllegalStateException("JWT_SECRET_STAGING is required")
        Environment.PRODUCTION -> System.getenv("JWT_SECRET")
            ?: throw IllegalStateException("JWT_SECRET is required for production")
    }

    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "your-app-${env.name.lowercase()}"
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "your-app-users-${env.name.lowercase()}"

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Your App ${env.name}"
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

    // Environment-specific rate limiting
    install(RateLimit) {
        register(RateLimitName("api")) {
            val (limit, period) = when (env) {
                Environment.DEVELOPMENT -> Pair(1000, 1.minutes) // Very permissive for dev
                Environment.STAGING -> Pair(200, 1.minutes) // Moderate for staging
                Environment.PRODUCTION -> Pair(100, 1.minutes) // Strict for production
            }

            rateLimiter(limit = limit, refillPeriod = period)
            requestKey { call ->
                call.request.origin.remoteHost
            }
        }
    }

    log.info("Security configured for ${env.name} environment")
}
```

`backend/src/main/kotlin/com/yourapp/plugins/HTTP.kt`:
```kotlin
package com.yourapp.plugins

import com.yourapp.configuration.Environment
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP(env: Environment = Environment.current()) {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("X-XSS-Protection", "1; mode=block")

        // Environment-specific security headers
        when (env) {
            Environment.DEVELOPMENT -> {
                // Relaxed headers for development
                header("X-Environment", "development")
            }
            Environment.STAGING -> {
                header("Strict-Transport-Security", "max-age=86400; includeSubDomains") // 1 day
                header("X-Environment", "staging")
            }
            Environment.PRODUCTION -> {
                header("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload") // 1 year
                header("Content-Security-Policy", "default-src 'self'")
                header("X-Environment", "production")
            }
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        // Environment-specific CORS configuration
        when (env) {
            Environment.DEVELOPMENT -> {
                // Very permissive for development
                anyHost()
                allowCredentials = true
                allowNonSimpleContentTypes = true
            }
            Environment.STAGING -> {
                // Moderate restrictions for staging
                allowHost("localhost:3000")
                allowHost("localhost:3001")
                allowHost("localhost:8080")
                allowHost("staging-frontend.yourapp.com")
                allowHost("staging.yourapp.com")
                allowCredentials = true
            }
            Environment.PRODUCTION -> {
                // Strict CORS for production
                allowHost("yourapp.com")
                allowHost("www.yourapp.com")
                allowHost("app.yourapp.com")
                System.getenv("ALLOWED_ORIGINS")?.split(",")?.forEach { origin ->
                    allowHost(origin.trim())
                }
                allowCredentials = false
            }
        }
    }

    log.info("HTTP configured for ${env.name} environment")
}
```

`backend/src/main/kotlin/com/yourapp/plugins/Serialization.kt`:
```kotlin
package com.yourapp.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}
```

`backend/src/main/kotlin/com/yourapp/plugins/Monitoring.kt`:
```kotlin
package com.yourapp.plugins

import com.yourapp.configuration.Environment
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring(env: Environment = Environment.current()) {
    install(CallLogging) {
        level = when (env) {
            Environment.DEVELOPMENT -> Level.DEBUG
            Environment.STAGING -> Level.INFO
            Environment.PRODUCTION -> Level.WARN
        }

        // Environment-specific logging filters
        filter { call ->
            when (env) {
                Environment.DEVELOPMENT -> true // Log everything in dev
                Environment.STAGING -> call.request.path().startsWith("/api") // Only API calls in staging
                Environment.PRODUCTION -> call.request.path().startsWith("/api") &&
                    !call.request.path().contains("/health") // Exclude health checks in prod
            }
        }

        // Custom format based on environment
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            val path = call.request.path()

            when (env) {
                Environment.DEVELOPMENT -> "$httpMethod $path - $status - $userAgent"
                Environment.STAGING -> "$httpMethod $path - $status"
                Environment.PRODUCTION -> "$httpMethod $path - $status"
            }
        }
    }

    log.info("Monitoring configured for ${env.name} environment")
}

// Development-specific tools
fun Application.configureDevTools() {
    log.info("Development tools enabled")

    // You can add development-specific plugins here
    // For example: swagger UI, request/response logging, etc.
}
```

`backend/src/main/kotlin/com/yourapp/plugins/Routing.kt`:
```kotlin
package com.yourapp.plugins

import com.yourapp.configuration.AIConfig
import com.yourapp.configuration.Environment
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(env: Environment = Environment.current()) {
    val aiConfig = AIConfig.forEnvironment(env)

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
                        "database_type" to if (System.getenv("DATABASE_URL_DEV")?.contains("postgresql") == true) "postgresql" else "h2"
                    ))
                }
                Environment.STAGING -> {
                    call.respond(mapOf(
                        "environment" to env.name.lowercase(),
                        "ai_provider" to aiConfig.primaryProvider.name,
                        "version" to "1.0.0"
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

            // AI endpoints (secured)
            authenticate("auth-jwt", optional = env.isDevelopment) {
                route("/ai") {
                    post("/chat") {
                        // AI chat implementation will go here
                        call.respond(
                            HttpStatusCode.NotImplemented,
                            mapOf(
                                "message" to "Chat endpoint coming soon",
                                "environment" to env.name.lowercase(),
                                "provider" to aiConfig.primaryProvider.name
                            )
                        )
                    }
                }
            }
        }
    }

    log.info("Routing configured for ${env.name} environment")
}
```

**Create `backend/src/main/resources/application.conf`:**
```hocon
# Main application configuration
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
        environment = development
        environment = ${?KTOR_ENV}
    }
    application {
        modules = [ com.yourapp.ApplicationKt.module ]
    }
}

# Include environment-specific configurations
include "application-dev.conf"
include "application-staging.conf"
include "application-prod.conf"
```

**Create `backend/src/main/resources/application-dev.conf`:**
```hocon
# Development environment configuration
dev {
    database {
        url = "jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
        url = ${?DATABASE_URL_DEV}
        url = ${?DATABASE_URL}
        user = "sa"
        user = ${?DATABASE_USER_DEV}
        user = ${?DATABASE_USER}
        password = ""
        password = ${?DATABASE_PASSWORD_DEV}
        password = ${?DATABASE_PASSWORD}
        pool_size = 5
        pool_size = ${?DB_POOL_SIZE_DEV}
    }

    jwt {
        secret = "development-secret-key-not-for-production-use-only-dev"
        secret = ${?JWT_SECRET_DEV}
        secret = ${?JWT_SECRET}
        issuer = "your-app-dev"
        issuer = ${?JWT_ISSUER_DEV}
        issuer = ${?JWT_ISSUER}
        audience = "your-app-users-dev"
        audience = ${?JWT_AUDIENCE_DEV}
        audience = ${?JWT_AUDIENCE}
        expiration = 86400000
    }

    ai {
        primary_provider = "claude"
        primary_provider = ${?AI_PRIMARY_PROVIDER_DEV}
        primary_provider = ${?AI_PRIMARY_PROVIDER}

        claude {
            api_key = ${?CLAUDE_API_KEY_DEV}
            api_key = ${?CLAUDE_API_KEY}
            model = "claude-3-5-sonnet-20241022"
            max_tokens = 1000
        }

        openai {
            api_key = ${?OPENAI_API_KEY_DEV}
            api_key = ${?OPENAI_API_KEY}
            model = "gpt-4o-mini"
            max_tokens = 500
        }

        gemini {
            api_key = ${?GEMINI_API_KEY_DEV}
            api_key = ${?GEMINI_API_KEY}
            model = "gemini-1.5-flash"
            max_tokens = 500
        }
    }
}
```

**Create `backend/src/main/resources/application-staging.conf`:**
```hocon
# Staging environment configuration
staging {
    database {
        url = ${?DATABASE_URL_STAGING}
        url = ${?DATABASE_URL}
        user = "postgres"
        user = ${?DATABASE_USER_STAGING}
        user = ${?DATABASE_USER}
        password = ${?DATABASE_PASSWORD_STAGING}
        password = ${?DATABASE_PASSWORD}
        pool_size = 10
        pool_size = ${?DB_POOL_SIZE_STAGING}
    }

    jwt {
        secret = ${?JWT_SECRET_STAGING}
        secret = ${?JWT_SECRET}
        issuer = "your-app-staging"
        issuer = ${?JWT_ISSUER_STAGING}
        issuer = ${?JWT_ISSUER}
        audience = "your-app-users-staging"
        audience = ${?JWT_AUDIENCE_STAGING}
        audience = ${?JWT_AUDIENCE}
        expiration = 86400000
    }

    ai {
        primary_provider = "claude"
        primary_provider = ${?AI_PRIMARY_PROVIDER_STAGING}
        primary_provider = ${?AI_PRIMARY_PROVIDER}

        claude {
            api_key = ${?CLAUDE_API_KEY_STAGING}
            api_key = ${?CLAUDE_API_KEY}
            model = "claude-3-5-sonnet-20241022"
            max_tokens = 2000
        }

        openai {
            api_key = ${?OPENAI_API_KEY_STAGING}
            api_key = ${?OPENAI_API_KEY}
            model = "gpt-4o-mini"
            max_tokens = 1000
        }

        gemini {
            api_key = ${?GEMINI_API_KEY_STAGING}
            api_key = ${?GEMINI_API_KEY}
            model = "gemini-1.5-flash"
            max_tokens = 1000
        }
    }
}
```

**Create `backend/src/main/resources/application-prod.conf`:**
```hocon
# Production environment configuration
prod {
    database {
        url = ${DATABASE_URL}
        user = ${DATABASE_USER}
        password = ${DATABASE_PASSWORD}
        pool_size = 20
        pool_size = ${?DB_POOL_SIZE}
    }

    jwt {
        secret = ${JWT_SECRET}
        issuer = ${JWT_ISSUER}
        audience = ${JWT_AUDIENCE}
        expiration = 86400000
    }

    ai {
        primary_provider = "claude"
        primary_provider = ${?AI_PRIMARY_PROVIDER}

        claude {
            api_key = ${CLAUDE_API_KEY}
            model = "claude-3-5-sonnet-20241022"
            max_tokens = 4000
        }

        openai {
            api_key = ${OPENAI_API_KEY}
            model = "gpt-4o"
            max_tokens = 2000
        }

        gemini {
            api_key = ${GEMINI_API_KEY}
            model = "gemini-1.5-pro"
            max_tokens = 2000
        }
    }
}
```

### Step 6: Create Essential Scripts

**Create `scripts/development/setup.sh`:**
```bash
#!/bin/bash
set -e

echo "Setting up Kotlin Backend + Mobile development environment..."

# Check dependencies
command -v java >/dev/null 2>&1 || { echo "Java is required but not installed."; exit 1; }
command -v git >/dev/null 2>&1 || { echo "Git is required but not installed."; exit 1; }

echo "Dependencies check passed"

# Create environment files for different stages
create_env_file() {
    local env_name=$1
    local env_file=".env.${env_name}"

    if [ ! -f "$env_file" ]; then
        echo "Creating $env_file file..."
        case $env_name in
            "dev")
                cat > "$env_file" << 'EOF'
# Development Environment Configuration
BUILD_VARIANT=dev
APP_ENVIRONMENT=development
KTOR_ENV=development
PORT=8080

# Database Configuration (Development)
DATABASE_URL_DEV=jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
DATABASE_USER_DEV=sa
DATABASE_PASSWORD_DEV=
DB_POOL_SIZE_DEV=5

# JWT Configuration (Development)
JWT_SECRET_DEV=development-jwt-secret-key-minimum-32-characters-long-for-dev-only
JWT_ISSUER_DEV=your-app-dev
JWT_AUDIENCE_DEV=your-app-users-dev

# AI API Configuration (Development - Lower Limits)
AI_PRIMARY_PROVIDER_DEV=claude
CLAUDE_API_KEY_DEV=your_claude_api_key_for_dev_here
OPENAI_API_KEY_DEV=your_openai_api_key_for_dev_here
GEMINI_API_KEY_DEV=your_gemini_api_key_for_dev_here

# Monitoring (Development)
SENTRY_DSN_DEV=your_sentry_dsn_for_dev_environment
LOG_LEVEL=DEBUG
EOF
                ;;
            "staging")
                cat > "$env_file" << 'EOF'
# Staging Environment Configuration
BUILD_VARIANT=staging
APP_ENVIRONMENT=staging
KTOR_ENV=staging
PORT=8080

# Database Configuration (Staging)
DATABASE_URL_STAGING=postgresql://username:password@staging-db-host:5432/staging_db
DATABASE_USER_STAGING=staging_user
DATABASE_PASSWORD_STAGING=staging_password
DB_POOL_SIZE_STAGING=10

# JWT Configuration (Staging)
JWT_SECRET_STAGING=staging-jwt-secret-key-minimum-32-characters-long-for-staging
JWT_ISSUER_STAGING=your-app-staging
JWT_AUDIENCE_STAGING=your-app-users-staging

# AI API Configuration (Staging)
AI_PRIMARY_PROVIDER_STAGING=claude
CLAUDE_API_KEY_STAGING=your_claude_api_key_for_staging_here
OPENAI_API_KEY_STAGING=your_openai_api_key_for_staging_here
GEMINI_API_KEY_STAGING=your_gemini_api_key_for_staging_here

# Monitoring (Staging)
SENTRY_DSN_STAGING=your_sentry_dsn_for_staging_environment
LOG_LEVEL=INFO
EOF
                ;;
            "prod")
                cat > "$env_file" << 'EOF'
# Production Environment Configuration
BUILD_VARIANT=prod
APP_ENVIRONMENT=production
KTOR_ENV=production
PORT=8080

# Database Configuration (Production)
DATABASE_URL=postgresql://username:password@prod-db-host:5432/prod_db
DATABASE_USER=prod_user
DATABASE_PASSWORD=prod_password
DB_POOL_SIZE=20

# JWT Configuration (Production)
JWT_SECRET=production-jwt-secret-key-minimum-32-characters-long-CHANGE-THIS
JWT_ISSUER=your-app
JWT_AUDIENCE=your-app-users

# AI API Configuration (Production)
AI_PRIMARY_PROVIDER=claude
CLAUDE_API_KEY=your_claude_api_key_for_production_here
OPENAI_API_KEY=your_openai_api_key_for_production_here
GEMINI_API_KEY=your_gemini_api_key_for_production_here

# Monitoring (Production)
SENTRY_DSN=your_sentry_dsn_for_production_environment
LOG_LEVEL=WARN

# Security (Production)
ALLOWED_ORIGINS=yourapp.com,www.yourapp.com,app.yourapp.com
EOF
                ;;
        esac
        echo "$env_file file created."
    else
        echo "$env_file file already exists"
    fi
}

# Create environment files
create_env_file "dev"
create_env_file "staging"
create_env_file "prod"

# Create symlink for default development environment
if [ ! -f .env ]; then
    ln -s .env.dev .env
    echo "Created .env symlink pointing to .env.dev"
fi

# Make gradlew executable
chmod +x backend/gradlew || echo "gradlew not found yet"

# Create run scripts for different environments
cat > "scripts/development/run-dev.sh" << 'EOF'
#!/bin/bash
echo "Starting development server..."
export $(cat .env.dev | xargs)
cd backend && ./gradlew runDev
EOF

cat > "scripts/development/run-staging.sh" << 'EOF'
#!/bin/bash
echo "Starting staging server..."
export $(cat .env.staging | xargs)
cd backend && ./gradlew runProd
EOF

cat > "scripts/development/test-dev.sh" << 'EOF'
#!/bin/bash
echo "Running tests in development mode..."
export $(cat .env.dev | xargs)
cd backend && ./gradlew test
EOF

chmod +x scripts/development/*.sh

echo "Setup complete! Next steps:"
echo ""
echo "Environment Configuration:"
echo "  1. Update .env.dev with your development API keys"
echo "  2. Update .env.staging with your staging credentials"
echo "  3. Update .env.prod with your production credentials"
echo ""
echo "Running the Application:"
echo "  Development: ./scripts/development/run-dev.sh"
echo "  Staging:     ./scripts/development/run-staging.sh"
echo "  Manual:      cd backend && ./gradlew runDev"
echo ""
echo "Testing:"
echo "  Run tests:   ./scripts/development/test-dev.sh"
echo "  Manual:      cd backend && ./gradlew test"
echo ""
echo "Test Endpoints:"
echo "  Health:      curl http://localhost:8080/health"
echo "  Info:        curl http://localhost:8080/info"
echo "  API Status:  curl http://localhost:8080/api/v1/status"
```

**Create `scripts/deployment/railway-deploy.sh`:**
```bash
#!/bin/bash
set -e

# Default to staging if no environment specified
ENVIRONMENT=${1:-staging}

echo "Deploying to Railway ($ENVIRONMENT environment)..."

# Check if railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "Railway CLI not found. Install from: https://railway.app/cli"
    exit 1
fi

# Login check
if ! railway whoami &> /dev/null; then
    echo "Please login to Railway first:"
    railway login
fi

# Function to deploy to specific environment
deploy_to_environment() {
    local env=$1
    local service_name="my-kotlin-project-$env"

    echo "Building and deploying to $env environment..."

    case $env in
        "staging")
            echo "Configuring for staging deployment..."

            # Set environment variables for staging
            railway environment use staging || railway environment create staging

            # Set staging-specific environment variables
            railway variables set BUILD_VARIANT=staging
            railway variables set APP_ENVIRONMENT=staging
            railway variables set KTOR_ENV=staging
            railway variables set PORT=8080

            # Database (Railway will provide DATABASE_URL)
            railway variables set DATABASE_URL='${{Postgres.DATABASE_URL}}'

            # Load staging secrets (you'll need to set these manually)
            echo "Please ensure these staging variables are set in Railway:"
            echo "   - JWT_SECRET_STAGING"
            echo "   - CLAUDE_API_KEY_STAGING"
            echo "   - OPENAI_API_KEY_STAGING (optional)"
            echo "   - GEMINI_API_KEY_STAGING (optional)"
            echo "   - SENTRY_DSN_STAGING (optional)"
            ;;

        "production")
            echo "Configuring for production deployment..."

            # Set environment variables for production
            railway environment use production || railway environment create production

            # Set production-specific environment variables
            railway variables set BUILD_VARIANT=prod
            railway variables set APP_ENVIRONMENT=production
            railway variables set KTOR_ENV=production
            railway variables set PORT=8080

            # Database (Railway will provide DATABASE_URL)
            railway variables set DATABASE_URL='${{Postgres.DATABASE_URL}}'

            # Load production secrets (you'll need to set these manually)
            echo "Please ensure these production variables are set in Railway:"
            echo "   - JWT_SECRET"
            echo "   - CLAUDE_API_KEY"
            echo "   - OPENAI_API_KEY (optional)"
            echo "   - GEMINI_API_KEY (optional)"
            echo "   - SENTRY_DSN (optional)"
            echo "   - ALLOWED_ORIGINS"
            ;;

        *)
            echo "Invalid environment: $env. Use 'staging' or 'production'"
            exit 1
            ;;
    esac

    # Build the application for the specific environment
    echo "Building application for $env..."
    if [ "$env" = "production" ]; then
        cd backend && ./gradlew buildProd && cd ..
    else
        cd backend && ./gradlew buildDev && cd ..
    fi

    # Deploy
    echo "Deploying to Railway..."
    railway up

    echo "Deployment to $env initiated!"
    echo "Check your Railway dashboard for deployment status"
    echo "Monitor logs with: railway logs"
}

# Validate environment
case $ENVIRONMENT in
    "staging"|"production")
        deploy_to_environment $ENVIRONMENT
        ;;
    *)
        echo "Invalid environment: $ENVIRONMENT"
        echo "Usage: $0 [staging|production]"
        echo "Examples:"
        echo "  $0 staging     # Deploy to staging"
        echo "  $0 production  # Deploy to production"
        exit 1
        ;;
esac
```

**Create `scripts/deployment/railway-staging.sh`:**
```bash
#!/bin/bash
# Quick staging deployment
./scripts/deployment/railway-deploy.sh staging
```

**Create `scripts/deployment/railway-production.sh`:**
```bash
#!/bin/bash
set -e

echo "PRODUCTION DEPLOYMENT WARNING"
echo "You are about to deploy to PRODUCTION environment."
echo "This will affect live users and real data."
echo ""
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Production deployment cancelled."
    exit 1
fi

echo "Proceeding with production deployment..."
./scripts/deployment/railway-deploy.sh production
```

**Create `scripts/deployment/setup-railway-environments.sh`:**
```bash
#!/bin/bash
set -e

echo "Setting up Railway environments..."

# Check if railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "Railway CLI not found. Install from: https://railway.app/cli"
    exit 1
fi

# Login check
if ! railway whoami &> /dev/null; then
    echo "Please login to Railway first:"
    railway login
fi

# Function to setup environment
setup_environment() {
    local env_name=$1
    echo "Setting up $env_name environment..."

    # Create or switch to environment
    railway environment use $env_name || railway environment create $env_name

    # Add PostgreSQL database
    echo "Adding PostgreSQL database for $env_name..."
    railway add postgresql || echo "Database already exists"

    case $env_name in
        "staging")
            echo "Configuring staging environment variables..."
            railway variables set BUILD_VARIANT=staging
            railway variables set APP_ENVIRONMENT=staging
            railway variables set KTOR_ENV=staging
            railway variables set LOG_LEVEL=INFO
            ;;
        "production")
            echo "Configuring production environment variables..."
            railway variables set BUILD_VARIANT=prod
            railway variables set APP_ENVIRONMENT=production
            railway variables set KTOR_ENV=production
            railway variables set LOG_LEVEL=WARN
            ;;
    esac

    echo "$env_name environment setup complete"
    echo ""
}

# Setup both environments
setup_environment "staging"
setup_environment "production"

echo "Railway environments setup complete!"
echo ""
echo "Next steps:"
echo "1. Set your API keys in each environment:"
echo "   railway environment use staging"
echo "   railway variables set JWT_SECRET_STAGING=your-staging-jwt-secret"
echo "   railway variables set CLAUDE_API_KEY_STAGING=your-staging-claude-key"
echo ""
echo "   railway environment use production"
echo "   railway variables set JWT_SECRET=your-production-jwt-secret"
echo "   railway variables set CLAUDE_API_KEY=your-production-claude-key"
echo ""
echo "2. Deploy to staging: ./scripts/deployment/railway-staging.sh"
echo "3. Deploy to production: ./scripts/deployment/railway-production.sh"
```

### Step 7: Create Comprehensive Documentation

**Create `README.md`:**
```markdown
# Kotlin Backend + Mobile Project

[![CI/CD](https://github.com/username/project/workflows/CI/badge.svg)](https://github.com/username/project/actions)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> Full-stack Kotlin project with backend API and mobile app using Compose Multiplatform

## Architecture

```
my-kotlin-project/
├── backend/          # Ktor API server
├── mobile/           # Compose Multiplatform app
├── shared/           # Shared domain models
├── docs/            # Documentation
└── scripts/         # Automation scripts
```

## Quick Start

### Prerequisites
- JDK 17+
- Git
- Android Studio (for mobile development)
- AI API keys (Claude/OpenAI/Gemini)

### Setup
```bash
# Clone and setup
git clone https://github.com/username/my-kotlin-project.git
cd my-kotlin-project
chmod +x scripts/development/setup.sh
./scripts/development/setup.sh

# Update .env with your API keys
nano .env

# Run backend
cd backend
./gradlew run

# Test API
curl http://localhost:8080/health
```

## Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `PORT` | Server port | No | 8080 |
| `DATABASE_URL` | Database connection | No | H2 in-memory |
| `JWT_SECRET` | JWT signing secret | Yes | - |
| `CLAUDE_API_KEY` | Claude API key | Recommended | - |
| `OPENAI_API_KEY` | OpenAI API key | Optional | - |
| `GEMINI_API_KEY` | Gemini API key | Optional | - |

## AI Integration

This project supports multiple AI providers with automatic fallback:

- **Primary**: Claude 3.5 Sonnet (best for development)
- **Alternative**: OpenAI GPT-4o mini (cost-effective)
- **Budget**: Google Gemini 1.5 Flash (cheapest)

### AI Costs (Approximate)
- Claude 3.5 Sonnet: $3/$15 per million tokens
- GPT-4o mini: $0.15/$0.60 per million tokens
- Gemini 1.5 Flash: $0.07/$0.30 per million tokens

## Mobile Development (Coming Soon)

The mobile component will use Compose Multiplatform for:
- iOS and Android apps
- Shared business logic
- Native platform integrations

## Deployment

### Railway (Recommended)
```bash
# Install Railway CLI
npm install -g @railway/cli

# Deploy
./scripts/deployment/railway-deploy.sh
```

### Cost Estimates
- **Development**: $0-5/month (free tiers)
- **Small Production**: $15-30/month
- **Growing App**: $30-75/month

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/health` | Health check | No |
| GET | `/api/v1/status` | API status | No |
| POST | `/api/v1/ai/chat` | AI chat | Yes |

## Testing

```bash
# Backend tests
cd backend
./gradlew test

# Mobile tests (when available)
cd mobile
./gradlew test
```

## Documentation

- [Backend API Documentation](docs/api/)
- [Mobile Development Guide](docs/mobile/)
- [Deployment Guide](docs/deployment/)

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file.
```

### Step 8: Create GitHub Repository

```bash
# Add all files
git add .
git commit -m "Initial project setup: Backend + Mobile foundation"

# Create GitHub repository (replace YOUR_USERNAME)
# Go to https://github.com/new and create repository
git remote add origin https://github.com/YOUR_USERNAME/my-kotlin-project.git
git branch -M main
git push -u origin main
```
