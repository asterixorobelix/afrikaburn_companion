# Phase 3: Mobile App Integration (When Ready)

## Contents

- [Step 1: Add Mobile Modules](#step-1-add-mobile-modules)
- [Step 2: Create Mobile Shared Module](#step-2-create-mobile-shared-module)
- [Step 3: API Client for Mobile](#step-3-api-client-for-mobile)

---

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
