# Complete Kotlin Backend + Mobile Repository Setup Guide
*For AI Assistants: Everything needed to set up a professional repository with both backend and mobile components*

---

## 📋 Document Reading Order

**If you're setting up a GitHub repository and beginning coding, follow this exact order:**

1. **Read this document first** - Complete overview and setup instructions
2. **Execute Phase 1** - Basic project setup and repository creation  
3. **Execute Phase 2** - Backend development and deployment
4. **Execute Phase 3** - Mobile app integration (if needed)
5. **Execute Phase 4** - Production features and scaling

---

## 🚀 Phase 1: Repository & Project Foundation (Time: 30 minutes)

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
    
    println("🚀 Starting application in ${environment.name} mode on port $port")
    
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
    
    log.info("🗄️ Database configured for ${env.name} environment with pool size: $poolSize")
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
    
    log.info("🔐 Security configured for ${env.name} environment")
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
    
    log.info("🌐 HTTP configured for ${env.name} environment")
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
    
    log.info("📊 Monitoring configured for ${env.name} environment")
}

// Development-specific tools
fun Application.configureDevTools() {
    log.info("🛠️ Development tools enabled")
    
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
    
    log.info("🛣️ Routing configured for ${env.name} environment")
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

### Step 6: Create Essential Scripts

**Create `scripts/development/setup.sh`:**
```bash
#!/bin/bash
set -e

echo "🚀 Setting up Kotlin Backend + Mobile development environment..."

# Check dependencies
command -v java >/dev/null 2>&1 || { echo "❌ Java is required but not installed."; exit 1; }
command -v git >/dev/null 2>&1 || { echo "❌ Git is required but not installed."; exit 1; }

echo "✅ Dependencies check passed"

# Create environment files for different stages
create_env_file() {
    local env_name=$1
    local env_file=".env.${env_name}"
    
    if [ ! -f "$env_file" ]; then
        echo "📝 Creating $env_file file..."
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
        echo "✅ $env_file file created."
    else
        echo "✅ $env_file file already exists"
    fi
}

# Create environment files
create_env_file "dev"
create_env_file "staging"
create_env_file "prod"

# Create symlink for default development environment
if [ ! -f .env ]; then
    ln -s .env.dev .env
    echo "✅ Created .env symlink pointing to .env.dev"
fi

# Make gradlew executable
chmod +x backend/gradlew || echo "⚠️  gradlew not found yet"

# Create run scripts for different environments
cat > "scripts/development/run-dev.sh" << 'EOF'
#!/bin/bash
echo "🚀 Starting development server..."
export $(cat .env.dev | xargs)
cd backend && ./gradlew runDev
EOF

cat > "scripts/development/run-staging.sh" << 'EOF'
#!/bin/bash
echo "🚀 Starting staging server..."
export $(cat .env.staging | xargs)
cd backend && ./gradlew runProd
EOF

cat > "scripts/development/test-dev.sh" << 'EOF'
#!/bin/bash
echo "🧪 Running tests in development mode..."
export $(cat .env.dev | xargs)
cd backend && ./gradlew test
EOF

chmod +x scripts/development/*.sh

echo "🎉 Setup complete! Next steps:"
echo ""
echo "📝 Environment Configuration:"
echo "  1. Update .env.dev with your development API keys"
echo "  2. Update .env.staging with your staging credentials" 
echo "  3. Update .env.prod with your production credentials"
echo ""
echo "🚀 Running the Application:"
echo "  Development: ./scripts/development/run-dev.sh"
echo "  Staging:     ./scripts/development/run-staging.sh"  
echo "  Manual:      cd backend && ./gradlew runDev"
echo ""
echo "🧪 Testing:"
echo "  Run tests:   ./scripts/development/test-dev.sh"
echo "  Manual:      cd backend && ./gradlew test"
echo ""
echo "🌐 Test Endpoints:"
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

echo "🚀 Deploying to Railway ($ENVIRONMENT environment)..."

# Check if railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI not found. Install from: https://railway.app/cli"
    exit 1
fi

# Login check
if ! railway whoami &> /dev/null; then
    echo "🔐 Please login to Railway first:"
    railway login
fi

# Function to deploy to specific environment
deploy_to_environment() {
    local env=$1
    local service_name="my-kotlin-project-$env"
    
    echo "📦 Building and deploying to $env environment..."
    
    case $env in
        "staging")
            echo "🔧 Configuring for staging deployment..."
            
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
            echo "⚠️  Please ensure these staging variables are set in Railway:"
            echo "   - JWT_SECRET_STAGING"
            echo "   - CLAUDE_API_KEY_STAGING"
            echo "   - OPENAI_API_KEY_STAGING (optional)"
            echo "   - GEMINI_API_KEY_STAGING (optional)"
            echo "   - SENTRY_DSN_STAGING (optional)"
            ;;
            
        "production")
            echo "🔧 Configuring for production deployment..."
            
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
            echo "⚠️  Please ensure these production variables are set in Railway:"
            echo "   - JWT_SECRET"
            echo "   - CLAUDE_API_KEY"
            echo "   - OPENAI_API_KEY (optional)"
            echo "   - GEMINI_API_KEY (optional)"
            echo "   - SENTRY_DSN (optional)"
            echo "   - ALLOWED_ORIGINS"
            ;;
            
        *)
            echo "❌ Invalid environment: $env. Use 'staging' or 'production'"
            exit 1
            ;;
    esac
    
    # Build the application for the specific environment
    echo "🔨 Building application for $env..."
    if [ "$env" = "production" ]; then
        cd backend && ./gradlew buildProd && cd ..
    else
        cd backend && ./gradlew buildDev && cd ..
    fi
    
    # Deploy
    echo "🚀 Deploying to Railway..."
    railway up
    
    echo "✅ Deployment to $env initiated!"
    echo "🌐 Check your Railway dashboard for deployment status"
    echo "📊 Monitor logs with: railway logs"
}

# Validate environment
case $ENVIRONMENT in
    "staging"|"production")
        deploy_to_environment $ENVIRONMENT
        ;;
    *)
        echo "❌ Invalid environment: $ENVIRONMENT"
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

echo "🚨 PRODUCTION DEPLOYMENT WARNING 🚨"
echo "You are about to deploy to PRODUCTION environment."
echo "This will affect live users and real data."
echo ""
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Production deployment cancelled."
    exit 1
fi

echo "🔒 Proceeding with production deployment..."
./scripts/deployment/railway-deploy.sh production
```

**Create `scripts/deployment/setup-railway-environments.sh`:**
```bash
#!/bin/bash
set -e

echo "🛠️ Setting up Railway environments..."

# Check if railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI not found. Install from: https://railway.app/cli"
    exit 1
fi

# Login check
if ! railway whoami &> /dev/null; then
    echo "🔐 Please login to Railway first:"
    railway login
fi

# Function to setup environment
setup_environment() {
    local env_name=$1
    echo "📝 Setting up $env_name environment..."
    
    # Create or switch to environment
    railway environment use $env_name || railway environment create $env_name
    
    # Add PostgreSQL database
    echo "🗄️ Adding PostgreSQL database for $env_name..."
    railway add postgresql || echo "Database already exists"
    
    case $env_name in
        "staging")
            echo "⚙️ Configuring staging environment variables..."
            railway variables set BUILD_VARIANT=staging
            railway variables set APP_ENVIRONMENT=staging
            railway variables set KTOR_ENV=staging
            railway variables set LOG_LEVEL=INFO
            ;;
        "production")
            echo "⚙️ Configuring production environment variables..."
            railway variables set BUILD_VARIANT=prod
            railway variables set APP_ENVIRONMENT=production
            railway variables set KTOR_ENV=production
            railway variables set LOG_LEVEL=WARN
            ;;
    esac
    
    echo "✅ $env_name environment setup complete"
    echo ""
}

# Setup both environments
setup_environment "staging"
setup_environment "production"

echo "🎉 Railway environments setup complete!"
echo ""
echo "📝 Next steps:"
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

## 🏗️ Architecture

```
my-kotlin-project/
├── backend/          # Ktor API server
├── mobile/           # Compose Multiplatform app
├── shared/           # Shared domain models
├── docs/            # Documentation
└── scripts/         # Automation scripts
```

## 🚀 Quick Start

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

## 🔑 Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `PORT` | Server port | No | 8080 |
| `DATABASE_URL` | Database connection | No | H2 in-memory |
| `JWT_SECRET` | JWT signing secret | Yes | - |
| `CLAUDE_API_KEY` | Claude API key | Recommended | - |
| `OPENAI_API_KEY` | OpenAI API key | Optional | - |
| `GEMINI_API_KEY` | Gemini API key | Optional | - |

## 🤖 AI Integration

This project supports multiple AI providers with automatic fallback:

- **Primary**: Claude 3.5 Sonnet (best for development)
- **Alternative**: OpenAI GPT-4o mini (cost-effective)
- **Budget**: Google Gemini 1.5 Flash (cheapest)

### AI Costs (Approximate)
- Claude 3.5 Sonnet: $3/$15 per million tokens
- GPT-4o mini: $0.15/$0.60 per million tokens  
- Gemini 1.5 Flash: $0.07/$0.30 per million tokens

## 📱 Mobile Development (Coming Soon)

The mobile component will use Compose Multiplatform for:
- iOS and Android apps
- Shared business logic
- Native platform integrations

## 🚀 Deployment

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

## 📊 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/health` | Health check | No |
| GET | `/api/v1/status` | API status | No |
| POST | `/api/v1/ai/chat` | AI chat | Yes |

## 🧪 Testing

```bash
# Backend tests
cd backend
./gradlew test

# Mobile tests (when available)
cd mobile
./gradlew test
```

## 📚 Documentation

- [Backend API Documentation](docs/api/)
- [Mobile Development Guide](docs/mobile/)
- [Deployment Guide](docs/deployment/)

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## 📄 License

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

---

## 🚀 Phase 2: Backend Development & Deployment (Time: 60 minutes)

### Step 1: Test Backend Locally

**Test with Development Environment:**
```bash
# Run development server (uses .env.dev)
./scripts/development/run-dev.sh

# OR manually:
export $(cat .env.dev | xargs)
cd backend && ./gradlew runDev

# Test endpoints
curl http://localhost:8080/health
curl http://localhost:8080/info  # Shows environment info
curl http://localhost:8080/api/v1/status
```

**Test with Staging Configuration Locally:**
```bash
# Run with staging config (uses .env.staging)
./scripts/development/run-staging.sh

# OR manually:
export $(cat .env.staging | xargs)
cd backend && ./gradlew runProd
```

**Run Tests:**
```bash
# Run tests in development mode
./scripts/development/test-dev.sh

# OR manually:
export $(cat .env.dev | xargs)
cd backend && ./gradlew test
```

**Environment-specific Testing:**
```bash
# Test development environment response
curl http://localhost:8080/info
# Should return: {"environment":"development","ai_provider":"claude","debug_mode":true,...}

# Test health check
curl http://localhost:8080/health  
# Should return: {"status":"healthy","environment":"development","build_variant":"dev",...}
```

### Step 2: Deploy to Railway

**First, set up Railway environments:**

1. **Install Railway CLI and setup environments**:
   ```bash
   # Install Railway CLI
   npm install -g @railway/cli
   
   # Setup environments
   chmod +x scripts/deployment/setup-railway-environments.sh
   ./scripts/deployment/setup-railway-environments.sh
   ```

2. **Configure Environment Variables**:

   **For Staging:**
   ```bash
   railway environment use staging
   railway variables set JWT_SECRET_STAGING=your-super-secret-jwt-key-minimum-32-characters-staging
   railway variables set CLAUDE_API_KEY_STAGING=your_claude_api_key_for_staging
   railway variables set OPENAI_API_KEY_STAGING=your_openai_api_key_for_staging  # optional
   railway variables set GEMINI_API_KEY_STAGING=your_gemini_api_key_for_staging  # optional
   ```

   **For Production:**
   ```bash
   railway environment use production
   railway variables set JWT_SECRET=your-super-secret-jwt-key-minimum-32-characters-production
   railway variables set CLAUDE_API_KEY=your_claude_api_key_for_production
   railway variables set OPENAI_API_KEY=your_openai_api_key_for_production      # optional
   railway variables set GEMINI_API_KEY=your_gemini_api_key_for_production      # optional
   railway variables set ALLOWED_ORIGINS=yourapp.com,www.yourapp.com
   ```

3. **Deploy to Different Environments**:

   **Deploy to Staging:**
   ```bash
   # Deploy to staging (for testing)
   ./scripts/deployment/railway-staging.sh
   ```

   **Deploy to Production:**
   ```bash
   # Deploy to production (requires confirmation)
   ./scripts/deployment/railway-production.sh
   ```

4. **Environment URLs**:
   - **Staging**: `https://my-kotlin-project-staging.up.railway.app`
   - **Production**: `https://my-kotlin-project-production.up.railway.app`

### Step 3: Set Up CI/CD Pipeline

**Create `.github/workflows/ci.yml`:**
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

env:
  JAVA_VERSION: '17'
  JAVA_DISTRIBUTION: 'temurin'

jobs:
  test:
    name: Test & Quality Checks
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
        ports: [5432:5432]
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Set up JDK ${{ env.JAVA_VERSION }}
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: ${{ env.JAVA_DISTRIBUTION }}
    
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3
      with:
        cache-read-only: ${{ github.event_name == 'pull_request' }}
    
    - name: Grant execute permission for gradlew
      run: chmod +x backend/gradlew
    
    - name: Run backend tests (Development)
      run: |
        cd backend
        ./gradlew test
      env:
        BUILD_VARIANT: dev
        APP_ENVIRONMENT: development
        KTOR_ENV: development
        DATABASE_URL_DEV: jdbc:postgresql://localhost:5432/testdb?user=testuser&password=testpass
        JWT_SECRET_DEV: test-secret-key-for-ci-pipeline-development-testing
        CLAUDE_API_KEY_DEV: test-key  # Mock for testing
    
    - name: Build backend (Development)
      run: |
        cd backend
        ./gradlew buildDev
    
    - name: Run backend tests (Production Build)
      run: |
        cd backend
        ./gradlew test
      env:
        BUILD_VARIANT: prod
        APP_ENVIRONMENT: production
        KTOR_ENV: production
        DATABASE_URL: jdbc:postgresql://localhost:5432/testdb?user=testuser&password=testpass
        JWT_SECRET: test-secret-key-for-ci-pipeline-production-testing
        CLAUDE_API_KEY: test-key  # Mock for testing
    
    - name: Build backend (Production)
      run: |
        cd backend
        ./gradlew buildProd
    
    - name: Upload build artifacts
      uses: actions/upload-artifact@v4
      with:
        name: backend-jar-${{ github.sha }}
        path: backend/build/libs/*.jar
        retention-days: 5

  security-scan:
    name: Security Scan
    runs-on: ubuntu-latest
    needs: test
    if: github.event_name == 'push'
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK ${{ env.JAVA_VERSION }}
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: ${{ env.JAVA_DISTRIBUTION }}
    
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3
    
    - name: Run dependency check
      run: |
        cd backend
        chmod +x gradlew
        ./gradlew dependencyCheckAnalyze --info || true
    
    - name: Upload security scan results
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: security-scan-${{ github.sha }}
        path: backend/build/reports/dependency-check-report.html

  deploy-staging:
    name: Deploy to Staging
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    if: github.ref == 'refs/heads/develop' && github.event_name == 'push'
    environment: staging
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Download build artifacts
      uses: actions/download-artifact@v4
      with:
        name: backend-jar-${{ github.sha }}
        path: backend/build/libs/
    
    - name: Install Railway CLI
      run: npm install -g @railway/cli
    
    - name: Deploy to Railway Staging
      run: |
        railway login --token ${{ secrets.RAILWAY_TOKEN }}
        railway environment use staging
        railway up --detach
      env:
        RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
    
    - name: Verify staging deployment
      run: |
        echo "🚀 Staging deployment completed"
        echo "🌐 Staging URL: https://my-kotlin-project-staging.up.railway.app"
        echo "📊 Health check in 30 seconds..."
        sleep 30
        curl -f https://my-kotlin-project-staging.up.railway.app/health || echo "Health check failed - deployment may still be starting"

  deploy-production:
    name: Deploy to Production
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    environment: production
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Download build artifacts
      uses: actions/download-artifact@v4
      with:
        name: backend-jar-${{ github.sha }}
        path: backend/build/libs/
    
    - name: Install Railway CLI
      run: npm install -g @railway/cli
    
    - name: Deploy to Railway Production
      run: |
        railway login --token ${{ secrets.RAILWAY_TOKEN }}
        railway environment use production
        railway up --detach
      env:
        RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
    
    - name: Verify production deployment
      run: |
        echo "🚀 Production deployment completed"
        echo "🌐 Production URL: https://my-kotlin-project-production.up.railway.app"
        echo "📊 Health check in 60 seconds..."
        sleep 60
        curl -f https://my-kotlin-project-production.up.railway.app/health || echo "Health check failed - deployment may still be starting"

  # Future: mobile tests and builds
  # mobile-test:
  #   name: Mobile Tests
  #   runs-on: macos-latest
  #   steps:
  #   - uses: actions/checkout@v4
  #   - name: Run mobile tests
  #     run: echo "Mobile tests coming soon"
```

---

## 📱 Phase 3: Mobile App Integration (When Ready)

### Step 1: Add Mobile Modules

**Update `settings.gradle.kts`:**
```kotlin
// Uncomment when ready for mobile
include(":mobile:shared")
include(":mobile:androidApp")
```

### Step 2: Create Mobile Shared Module

**Create `mobile/shared/build.gradle.kts`:**
```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
}

kotlin {
    android()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation("io.ktor:ktor-client-core:2.3.5")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:2.3.5")
            }
        }
        
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.5")
            }
        }
    }
}

android {
    namespace = "com.yourapp.mobile.shared"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
}
```

### Step 3: API Client for Mobile

**Create `mobile/shared/src/commonMain/kotlin/com/yourapp/mobile/api/ApiClient.kt`:**
```kotlin
package com.yourapp.mobile.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Environment configuration for mobile
enum class AppEnvironment(val baseUrl: String) {
    DEVELOPMENT("http://localhost:8080"),
    STAGING("https://my-kotlin-project-staging.up.railway.app"),
    PRODUCTION("https://my-kotlin-project-production.up.railway.app");
    
    companion object {
        fun current(): AppEnvironment {
            // You can implement platform-specific environment detection here
            // For now, default to development
            return DEVELOPMENT
        }
    }
}

class ApiClient(
    private val environment: AppEnvironment = AppEnvironment.current()
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        // Environment-specific logging
        install(Logging) {
            level = when (environment) {
                AppEnvironment.DEVELOPMENT -> LogLevel.ALL
                AppEnvironment.STAGING -> LogLevel.INFO
                AppEnvironment.PRODUCTION -> LogLevel.NONE
            }
        }
    }
    
    // Health check
    suspend fun getHealth(): HealthResponse {
        return client.get("${environment.baseUrl}/health").body()
    }
    
    // Environment info
    suspend fun getInfo(): InfoResponse {
        return client.get("${environment.baseUrl}/info").body()
    }
    
    // API status
    suspend fun getStatus(): ApiResponse {
        return client.get("${environment.baseUrl}/api/v1/status").body()
    }
    
    // Chat with AI
    suspend fun chatWithAI(message: String, token: String? = null): ChatResponse {
        return client.post("${environment.baseUrl}/api/v1/ai/chat") {
            if (token != null) {
                header("Authorization", "Bearer $token")
            }
            setBody(ChatRequest(message))
        }.body()
    }
    
    // Get AI providers (dev/staging only)
    suspend fun getAIProviders(): AIProvidersResponse? {
        return if (environment != AppEnvironment.PRODUCTION) {
            try {
                client.get("${environment.baseUrl}/api/v1/ai/providers").body()
            } catch (e: Exception) {
                null // Endpoint might not be available
            }
        } else {
            null // Not available in production
        }
    }
    
    // Environment-specific features
    suspend fun getDebugConfig(): DebugConfigResponse? {
        return if (environment == AppEnvironment.DEVELOPMENT) {
            try {
                client.get("${environment.baseUrl}/api/v1/debug/config").body()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    suspend fun getUsageStats(): UsageStatsResponse? {
        return if (environment == AppEnvironment.DEVELOPMENT) {
            try {
                client.get("${environment.baseUrl}/api/v1/debug/usage").body()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}

// Response data classes
@kotlinx.serialization.Serializable
data class HealthResponse(
    val status: String,
    val timestamp: Long,
    val version: String,
    val environment: String,
    val build_variant: String? = null
)

@kotlinx.serialization.Serializable
data class InfoResponse(
    val environment: String,
    val ai_provider: String? = null,
    val debug_mode: Boolean? = null,
    val database_type: String? = null,
    val max_tokens: Int? = null,
    val version: String? = null,
    val daily_usage: Map<String, Int>? = null
)

@kotlinx.serialization.Serializable
data class ApiResponse(
    val message: String,
    val environment: String,
    val ai_provider: String
)

@kotlinx.serialization.Serializable
data class ChatRequest(val message: String)

@kotlinx.serialization.Serializable
data class ChatResponse(
    val response: String,
    val environment: String? = null,
    val timestamp: Long? = null
)

@kotlinx.serialization.Serializable
data class AIProvidersResponse(
    val primary: String,
    val available: List<String>,
    val models: Map<String, String>
)

@kotlinx.serialization.Serializable
data class DebugConfigResponse(
    val environment: String,
    val ai_providers: Map<String, Map<String, Any>>
)

@kotlinx.serialization.Serializable
data class UsageStatsResponse(
    val daily_usage: Map<String, Int>
)

// Environment switcher for testing
class EnvironmentSwitcher {
    companion object {
        fun createClientForEnvironment(env: AppEnvironment): ApiClient {
            return ApiClient(env)
        }
        
        fun getAllEnvironmentClients(): Map<AppEnvironment, ApiClient> {
            return AppEnvironment.values().associateWith { ApiClient(it) }
        }
    }
}
```

**Create environment-specific configuration `mobile/shared/src/commonMain/kotlin/com/yourapp/mobile/config/AppConfig.kt`:**
```kotlin
package com.yourapp.mobile.config

import com.yourapp.mobile.api.AppEnvironment

object AppConfig {
    // Environment-specific settings
    fun getConfig(environment: AppEnvironment): MobileAppConfig {
        return when (environment) {
            AppEnvironment.DEVELOPMENT -> MobileAppConfig(
                environment = environment,
                enableDebugFeatures = true,
                enableLogging = true,
                apiTimeout = 30_000L, // 30 seconds for development
                retryAttempts = 3,
                cacheTtl = 300_000L, // 5 minutes
                enableAnalytics = false
            )
            AppEnvironment.STAGING -> MobileAppConfig(
                environment = environment,
                enableDebugFeatures = true,
                enableLogging = true,
                apiTimeout = 15_000L, // 15 seconds for staging
                retryAttempts = 2,
                cacheTtl = 600_000L, // 10 minutes
                enableAnalytics = true
            )
            AppEnvironment.PRODUCTION -> MobileAppConfig(
                environment = environment,
                enableDebugFeatures = false,
                enableLogging = false,
                apiTimeout = 10_000L, // 10 seconds for production
                retryAttempts = 1,
                cacheTtl = 1_800_000L, // 30 minutes
                enableAnalytics = true
            )
        }
    }
}

data class MobileAppConfig(
    val environment: AppEnvironment,
    val enableDebugFeatures: Boolean,
    val enableLogging: Boolean,
    val apiTimeout: Long,
    val retryAttempts: Int,
    val cacheTtl: Long,
    val enableAnalytics: Boolean
)
```

---

## 🛡️ Phase 4: Production Features & Monitoring

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
                    
                    logger.info("✅ AI response successful - Provider: $provider, Environment: ${config.environment.name}, Attempt: $attempt")
                    return response
                    
                } catch (e: Exception) {
                    lastException = e
                    logger.warn("⚠️ AI request failed - Provider: $provider, Environment: ${config.environment.name}, Attempt: $attempt, Error: ${e.message}")
                    
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
        
        logger.error("❌ All AI providers failed - Environment: ${config.environment.name}, Last error: ${lastException?.message}")
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
        // Rough estimation: 1 token ≈ 4 characters
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
                logger.error("🚨 DAILY LIMIT EXCEEDED - Provider: $provider, Environment: $environment, Usage: $currentUsage/$limits tokens")
            }
            currentUsage > (limits * 0.8) -> {
                logger.warn("⚠️ Approaching daily limit - Provider: $provider, Environment: $environment, Usage: $currentUsage/$limits tokens")
            }
            currentUsage > (limits * 0.5) -> {
                logger.info("📊 Half daily limit reached - Provider: $provider, Environment: $environment, Usage: $currentUsage/$limits tokens")
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
    
    log.info("🛣️ Routing configured for ${env.name} environment")
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

---

## 📋 Essential Environment Variables

### Development Environment (.env.dev)

```env
# Server Configuration
BUILD_VARIANT=dev
APP_ENVIRONMENT=development
KTOR_ENV=development
PORT=8080

# Database Configuration (Development - H2 for simplicity)
DATABASE_URL_DEV=jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
DATABASE_USER_DEV=sa
DATABASE_PASSWORD_DEV=
DB_POOL_SIZE_DEV=5

# JWT Security (Development - Less secure for testing)
JWT_SECRET_DEV=development-jwt-secret-minimum-32-characters-for-dev-only
JWT_ISSUER_DEV=your-app-dev
JWT_AUDIENCE_DEV=your-app-users-dev

# AI API Configuration (Development - Lower token limits)
AI_PRIMARY_PROVIDER_DEV=claude
CLAUDE_API_KEY_DEV=your_claude_api_key_for_dev_here
OPENAI_API_KEY_DEV=your_openai_api_key_for_dev_here
GEMINI_API_KEY_DEV=your_gemini_api_key_for_dev_here
```

### Staging Environment (.env.staging)

```env
# Server Configuration
BUILD_VARIANT=staging
APP_ENVIRONMENT=staging
KTOR_ENV=staging
PORT=8080

# Database Configuration (Staging - PostgreSQL)
DATABASE_URL_STAGING=postgresql://user:pass@staging-host:5432/staging_db
DATABASE_USER_STAGING=staging_user
DATABASE_PASSWORD_STAGING=staging_password
DB_POOL_SIZE_STAGING=10

# JWT Security (Staging - Production-like security)
JWT_SECRET_STAGING=staging-jwt-secret-minimum-32-characters-long-for-staging
JWT_ISSUER_STAGING=your-app-staging
JWT_AUDIENCE_STAGING=your-app-users-staging

# AI API Configuration (Staging - Medium token limits)
AI_PRIMARY_PROVIDER_STAGING=claude
CLAUDE_API_KEY_STAGING=your_claude_api_key_for_staging_here
OPENAI_API_KEY_STAGING=your_openai_api_key_for_staging_here
GEMINI_API_KEY_STAGING=your_gemini_api_key_for_staging_here
```

### Production Environment (.env.prod)

```env
# Server Configuration
BUILD_VARIANT=prod
APP_ENVIRONMENT=production
KTOR_ENV=production
PORT=8080

# Database Configuration (Production - PostgreSQL with connection pooling)
DATABASE_URL=postgresql://user:pass@prod-host:5432/prod_db
DATABASE_USER=prod_user
DATABASE_PASSWORD=prod_password
DB_POOL_SIZE=20

# JWT Security (Production - Maximum security)
JWT_SECRET=production-jwt-secret-minimum-32-characters-long-CHANGE-THIS
JWT_ISSUER=your-app
JWT_AUDIENCE=your-app-users

# AI API Configuration (Production - High token limits)
AI_PRIMARY_PROVIDER=claude
CLAUDE_API_KEY=your_claude_api_key_for_production_here
OPENAI_API_KEY=your_openai_api_key_for_production_here
GEMINI_API_KEY=your_gemini_api_key_for_production_here

# Security & Monitoring (Production)
ALLOWED_ORIGINS=yourapp.com,www.yourapp.com,app.yourapp.com
SENTRY_DSN=your_sentry_dsn_for_production_environment
```

---

## 🚀 Quick Deploy Commands

### Local Development
```bash
# Start development server
./scripts/development/run-dev.sh

# Start with staging configuration (locally)
./scripts/development/run-staging.sh

# Run tests
./scripts/development/test-dev.sh
```

### Test API Endpoints
```bash
# Test development environment
curl http://localhost:8080/health
curl http://localhost:8080/info
curl http://localhost:8080/api/v1/status

# Test AI endpoint (when implemented)
curl -X POST http://localhost:8080/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello AI!"}'
```

### Deploy to Railway
```bash
# Setup Railway environments (one-time)
./scripts/deployment/setup-railway-environments.sh

# Deploy to staging
./scripts/deployment/railway-staging.sh

# Deploy to production (with confirmation)
./scripts/deployment/railway-production.sh
```

### Test Deployed Environments
```bash
# Test staging deployment
curl https://my-kotlin-project-staging.up.railway.app/health
curl https://my-kotlin-project-staging.up.railway.app/info

# Test production deployment  
curl https://my-kotlin-project-production.up.railway.app/health
curl https://my-kotlin-project-production.up.railway.app/info
```

### Environment Comparison
```bash
# Development
curl http://localhost:8080/info
# Returns: {"environment":"development","debug_mode":true,...}

# Staging  
curl https://my-kotlin-project-staging.up.railway.app/info
# Returns: {"environment":"staging","ai_provider":"claude",...}

# Production
curl https://my-kotlin-project-production.up.railway.app/info  
# Returns: {"environment":"production","version":"1.0.0",...}
```

---

## 💰 Cost Management

### Monthly Cost Estimates by Environment

| Component | Development | Staging | Production |
|-----------|-------------|---------|------------|
| **Railway Hosting** | Free (local) | $5/month | $5-20/month |
| **PostgreSQL** | Free (H2) | $5/month | $5-15/month |
| **Claude API** | $1-5/month | $5-15/month | $20-100/month |
| **Monitoring** | Free tools | Free tools | $0-26/month |

**Environment-Specific Totals:**
- **Development**: $1-5/month (mostly local, minimal API usage)
- **Staging**: $15-25/month (testing and validation)
- **Production**: $30-160/month (full features, real usage)

### Cost Optimization by Environment

#### Development Environment
- **Use H2 database** (free, in-memory)
- **Lower AI token limits** (1000 tokens max)
- **Cheaper AI models** (GPT-4o mini, Gemini Flash)
- **Minimal logging** (reduce storage costs)
- **Local development** (no hosting costs)

#### Staging Environment  
- **Shared PostgreSQL** (small instance)
- **Medium AI token limits** (2000 tokens max)
- **Mid-tier AI models** (Claude 3.5 Sonnet)
- **Moderate logging** (info level)
- **Single Railway instance** ($5/month)

#### Production Environment
- **Optimized PostgreSQL** (connection pooling)
- **High AI token limits** (4000 tokens max)
- **Best AI models** (Claude 3.5 Sonnet, GPT-4o)
- **Minimal logging** (warn level, performance)
- **Scalable Railway deployment** ($5-20/month)

### Environment-Specific Monitoring

#### Track API Usage by Environment
```kotlin
// Add to AIService.kt
class AIUsageTracker(private val environment: Environment) {
    private val dailyUsage = mutableMapOf<String, AtomicInteger>()
    
    fun trackUsage(provider: AIProvider, tokens: Int) {
        val limits = when (environment) {
            Environment.DEVELOPMENT -> 10_000   // 10K tokens/day
            Environment.STAGING -> 50_000      // 50K tokens/day  
            Environment.PRODUCTION -> 200_000  // 200K tokens/day
        }
        
        val key = "${provider.name}-${LocalDate.now()}"
        val usage = dailyUsage.getOrPut(key) { AtomicInteger(0) }
        
        if (usage.addAndGet(tokens) > limits) {
            // Log warning or switch to fallback provider
        }
    }
}
```

### Cost Alerts Setup

#### Environment-Specific Monitoring
```env
# Development - Conservative limits
MAX_DAILY_API_CALLS_DEV=100
MAX_AI_TOKENS_DAILY_DEV=10000

# Staging - Moderate limits  
MAX_DAILY_API_CALLS_STAGING=500
MAX_AI_TOKENS_DAILY_STAGING=50000

# Production - High limits with monitoring
MAX_DAILY_API_CALLS_PROD=5000
MAX_AI_TOKENS_DAILY_PROD=200000
COST_ALERT_EMAIL=admin@yourapp.com
```

---

## 📚 AI Assistant Guidelines

When working with this project structure:

1. **Always use environment-aware configuration** - Check current environment and adjust behavior accordingly
2. **Implement proper environment separation** - Use dev/staging/prod specific API keys and settings
3. **Include proper error handling** for all AI API calls with environment-specific fallback logic
4. **Monitor costs carefully** with usage tracking per environment
5. **Test in development first** before deploying to staging, then production
6. **Keep API keys secure** - never commit to git, use environment-specific variables
7. **Use environment variables** for all configuration, with environment-specific fallbacks
8. **Follow the established project structure** - maintain separation between environments
9. **Include both backend and mobile considerations** - ensure mobile apps can connect to different environment backends
10. **Document all AI integrations clearly** - specify environment requirements and configurations

### Environment-Specific Development Practices

#### Development Environment
- **Use lower token limits** to save costs during development
- **Enable verbose logging** for debugging
- **Use H2 database** for simplicity
- **Allow permissive CORS** for frontend development
- **Mock AI responses** when API keys not available

#### Staging Environment  
- **Mirror production configuration** but with staging-specific keys
- **Enable detailed monitoring** for testing
- **Use moderate resource limits**
- **Test production-like scenarios**
- **Validate CI/CD pipeline**

#### Production Environment
- **Strict security headers** and CORS policies  
- **Minimal logging** for performance
- **Optimized resource usage**
- **Real monitoring and alerting**
- **High availability configuration**

### Environment Testing Checklist

Before deploying to any environment, verify:
- [ ] Environment variables are correctly set
- [ ] Database connections work
- [ ] AI API keys are valid and have appropriate limits
- [ ] Security headers are appropriate for environment
- [ ] Logging levels are correct
- [ ] CORS policies match frontend requirements
- [ ] Health checks return expected environment information

---

## 🎯 Quick Reference Commands

### Project Setup
```bash
# Initial setup (creates all environment files)
./scripts/development/setup.sh

# Setup Railway environments (one-time)
./scripts/deployment/setup-railway-environments.sh
```

### Local Development
```bash
# Development environment
./scripts/development/run-dev.sh
# OR: export $(cat .env.dev | xargs) && cd backend && ./gradlew runDev

# Staging configuration (local)
./scripts/development/run-staging.sh  
# OR: export $(cat .env.staging | xargs) && cd backend && ./gradlew runProd

# Run tests
./scripts/development/test-dev.sh
# OR: export $(cat .env.dev | xargs) && cd backend && ./gradlew test
```

### Build Commands
```bash
cd backend

# Development build
./gradlew buildDev

# Production build  
./gradlew buildProd

# Run specific environment
./gradlew runDev    # Development
./gradlew runProd   # Production
```

### Deployment Commands
```bash
# Deploy to staging
./scripts/deployment/railway-staging.sh

# Deploy to production (with confirmation)
./scripts/deployment/railway-production.sh

# Manual Railway commands
railway environment use staging && railway up
railway environment use production && railway up
```

### Testing Commands
```bash
# Local endpoints
curl http://localhost:8080/health
curl http://localhost:8080/info
curl http://localhost:8080/api/v1/status

# Staging endpoints
curl https://my-kotlin-project-staging.up.railway.app/health
curl https://my-kotlin-project-staging.up.railway.app/info

# Production endpoints
curl https://my-kotlin-project-production.up.railway.app/health
curl https://my-kotlin-project-production.up.railway.app/info

# AI endpoint test
curl -X POST http://localhost:8080/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Test message"}'
```

### Environment Management
```bash
# Switch between environment files
ln -sf .env.dev .env      # Use development
ln -sf .env.staging .env  # Use staging  
ln -sf .env.prod .env     # Use production

# Load specific environment
export $(cat .env.dev | xargs)     # Development
export $(cat .env.staging | xargs) # Staging
export $(cat .env.prod | xargs)    # Production
```

### Mobile Development (When Ready)
```bash
# Uncomment mobile modules in settings.gradle.kts
# Then run:
cd mobile && ./gradlew assembleDebug           # Android
cd mobile && ./gradlew iosSimulatorArm64Test  # iOS Simulator
```

### Database Commands
```bash
# Development (H2 - automatic)
# No setup required

# Staging/Production (PostgreSQL via Railway)
railway environment use staging
railway connect postgres  # Connect to staging DB

railway environment use production  
railway connect postgres  # Connect to production DB
```

### Monitoring Commands
```bash
# View Railway logs
railway logs --environment staging
railway logs --environment production

# Check environment status
railway status --environment staging
railway status --environment production
```

---

## 🚨 Important Notes for AI Assistants

### Environment-Aware Development Principles

1. **This guide is optimized for multi-environment development** - Always consider dev/staging/prod implications when making suggestions
2. **Environment separation is critical** - Use environment-specific API keys, databases, and configurations
3. **Cost optimization varies by environment** - Development should minimize costs, production should optimize performance
4. **Security increases with environment maturity** - Development is permissive, production is strict
5. **Testing strategy is environment-dependent** - Test in dev, validate in staging, monitor in production

### Environment-Specific Configurations

#### Development Environment
- **Purpose**: Local development and debugging
- **Database**: H2 in-memory (free, fast setup)
- **AI Models**: Cheaper options (GPT-4o mini, Gemini Flash)
- **Token Limits**: Low (1000 tokens) to control costs
- **Logging**: Verbose (DEBUG level)
- **Security**: Permissive CORS, optional JWT
- **Features**: Debug endpoints, usage tracking, config inspection

#### Staging Environment
- **Purpose**: Pre-production testing and validation
- **Database**: PostgreSQL (production-like)
- **AI Models**: Mid-tier options (Claude 3.5 Sonnet)
- **Token Limits**: Medium (2000 tokens)
- **Logging**: Moderate (INFO level)
- **Security**: Production-like with staging-specific keys
- **Features**: Limited debug endpoints, monitoring

#### Production Environment
- **Purpose**: Live user-facing application
- **Database**: Optimized PostgreSQL with connection pooling
- **AI Models**: Best available (Claude 3.5 Sonnet, GPT-4o)
- **Token Limits**: High (4000 tokens) for full functionality
- **Logging**: Minimal (WARN level) for performance
- **Security**: Strict CORS, mandatory JWT, security headers
- **Features**: No debug endpoints, full monitoring

### Critical Development Guidelines

#### Environment Variable Management
- **Never mix environment variables** between dev/staging/prod
- **Use environment-specific fallbacks** in configuration
- **Validate required variables** per environment
- **Document environment-specific requirements**

#### Testing Strategy
```bash
# Development Testing
export $(cat .env.dev | xargs) && cd backend && ./gradlew test

# Staging Validation  
export $(cat .env.staging | xargs) && cd backend && ./gradlew test

# Production Readiness
export $(cat .env.prod | xargs) && cd backend && ./gradlew buildProd
```

#### Cost Management Rules
- **Development**: Minimize AI API calls, use mocks when possible
- **Staging**: Monitor usage closely, set conservative limits
- **Production**: Optimize for performance, implement proper caching

#### Security Progression
- **Development**: Focus on functionality over security
- **Staging**: Implement production-like security measures
- **Production**: Maximum security, regular audits

### Mobile App Environment Considerations

#### Backend Environment Connections
- **Development**: Mobile connects to localhost:8080
- **Staging**: Mobile connects to staging Railway URL
- **Production**: Mobile connects to production Railway URL

#### Environment Detection in Mobile
```kotlin
// Platform-specific environment detection
expect fun getCurrentEnvironment(): AppEnvironment

// iOS Implementation
actual fun getCurrentEnvironment(): AppEnvironment {
    return if (DEBUG) AppEnvironment.DEVELOPMENT else AppEnvironment.PRODUCTION
}

// Android Implementation  
actual fun getCurrentEnvironment(): AppEnvironment {
    return if (BuildConfig.DEBUG) AppEnvironment.DEVELOPMENT else AppEnvironment.PRODUCTION
}
```

### Deployment Best Practices

#### Progressive Deployment Strategy
1. **Develop locally** with development environment
2. **Test thoroughly** before deploying to staging
3. **Validate in staging** with production-like data
4. **Deploy to production** only after staging validation
5. **Monitor production** deployment for issues

#### CI/CD Environment Handling
- **Pull Requests**: Test with development configuration
- **Develop Branch**: Auto-deploy to staging
- **Main Branch**: Manual deploy to production with confirmation
- **Feature Branches**: Local testing only

#### Environment-Specific Secrets
```bash
# Development Secrets (can be less secure)
JWT_SECRET_DEV=dev-secret-minimum-32-chars
CLAUDE_API_KEY_DEV=dev-claude-key

# Staging Secrets (production-like)
JWT_SECRET_STAGING=staging-secret-minimum-32-chars-secure
CLAUDE_API_KEY_STAGING=staging-claude-key

# Production Secrets (maximum security)
JWT_SECRET=production-secret-minimum-32-chars-maximum-security
CLAUDE_API_KEY=production-claude-key
```

### Repository Structure Benefits

#### Unified Context for AI Assistants
- **Single repository** contains both backend and mobile code
- **Shared domain models** ensure consistency
- **Environment configurations** are centralized
- **AI assistants** have full context of both components

#### Environment Consistency
- **Configuration patterns** are consistent across backend/mobile
- **API contracts** are synchronized between environments
- **Testing strategies** cover full stack in each environment
- **Deployment** maintains environment separation

### Troubleshooting by Environment

#### Development Issues
- **Check .env.dev file** for correct configuration
- **Verify H2 database** starts properly
- **Confirm API keys** are set (can be test keys)
- **Enable debug logging** for detailed information

#### Staging Issues
- **Validate staging database** connection
- **Check staging API keys** and quotas
- **Review CI/CD pipeline** logs
- **Compare with development** configuration

#### Production Issues
- **Check production environment** variables in Railway
- **Monitor production logs** (minimal due to WARN level)
- **Verify database** connection and performance
- **Review security headers** and CORS policies

This comprehensive environment separation ensures safe development practices while maintaining production reliability and cost efficiency.