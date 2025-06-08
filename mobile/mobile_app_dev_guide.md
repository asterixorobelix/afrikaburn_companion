# Complete Mobile App Development Guide for AI Assistants

## 📋 Table of Contents

1. [Project Overview & Architecture](#project-overview--architecture)
2. [Prerequisites & Tools](#prerequisites--tools)
3. [Document Processing Order](#document-processing-order)
4. [Initial Setup Process](#initial-setup-process)
5. [Project Structure & Configuration](#project-structure--configuration)
6. [Development Workflow](#development-workflow)
7. [Testing & Quality Assurance](#testing--quality-assurance)
8. [Store Deployment](#store-deployment)
9. [AI Assistant Context](#ai-assistant-context)
10. [Common Commands & Scripts](#common-commands--scripts)

---

## 🏗️ Project Overview & Architecture

### Technology Stack
- **Framework**: Compose Multiplatform (iOS + Android)
- **Language**: Kotlin
- **Architecture**: Clean Architecture with MVVM pattern
- **UI**: Jetpack Compose with Material Design 3
- **DI**: Koin dependency injection
- **Navigation**: Voyager navigation library
- **State Management**: Compose state + Kotlin coroutines
- **Testing**: Kotest + MockK for unit tests
- **CI/CD**: GitHub Actions
- **Code Quality**: Detekt static analysis

### Repository Structure Decision
**Frontend + Backend in Same Repository** (Recommended)
- Enables full context for AI assistants
- Simplifies dependency management
- Ensures version compatibility
- Facilitates coordinated deployments

### Module Organization
```
project-root/
├── mobile/                     # Mobile app (Compose Multiplatform)
│   ├── shared/                # Shared business logic
│   ├── androidApp/            # Android-specific code
│   └── iosApp/               # iOS-specific code
├── backend/                   # Backend services
│   ├── api/                  # REST API
│   ├── database/             # Database schemas
│   └── services/             # Business services
├── shared-models/             # Shared data models
├── docs/                     # Documentation
└── scripts/                  # Automation scripts
```

---

## 🛠️ Prerequisites & Tools

### Required Software
```bash
# Core Requirements
- Java JDK 17+ (https://adoptium.net/)
- Git 2.0+ (https://git-scm.com/)
- GitHub CLI (https://cli.github.com/)

# Development Environment
- Android Studio Giraffe (2023.2.1) or newer
- Xcode 15.0+ (for iOS development, macOS only)

# Optional but Recommended
- ImageMagick (for asset generation)
- Node.js (for any web tools)
```

### GitHub CLI Installation
```bash
# macOS
brew install gh

# Ubuntu/Debian
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update && sudo apt install gh

# Windows
winget install --id GitHub.cli

# Authenticate
gh auth login
```

### Prerequisites Verification Script
```bash
#!/bin/bash
# scripts/verify-prerequisites.sh

echo "🔍 Checking prerequisites..."

# Java (JDK 17+)
if java -version 2>&1 | grep -q "17\|18\|19\|20\|21"; then
    echo "✅ Java JDK 17+ installed"
else
    echo "❌ Java JDK 17+ required. Install from: https://adoptium.net/"
fi

# Git
if git --version 2>&1 | grep -q "git version"; then
    echo "✅ Git installed"
else
    echo "❌ Git required. Install from: https://git-scm.com/"
fi

# GitHub CLI
if gh --version 2>&1 | grep -q "gh version"; then
    echo "✅ GitHub CLI installed"
else
    echo "❌ GitHub CLI required. See installation instructions."
fi

# GitHub CLI authentication
if gh auth status &>/dev/null; then
    echo "✅ GitHub CLI authenticated"
else
    echo "❌ GitHub CLI not authenticated. Run: gh auth login"
fi

echo "🎉 Prerequisites check complete!"
```

---

## 📚 Document Processing Order

When setting up a new project, follow this order:

### Phase 1: Planning & Setup (Documents 1-3)
1. **App Names** - Research app name availability across stores and domains
2. **Project Setup Steps** - Understand the overall checklist
3. **Quick Setup Guide** - Follow the streamlined 30-minute setup

### Phase 2: Implementation (Documents 4-6)
4. **Compose Multiplatform Setup Guide** - Detailed technical implementation
5. **Complete Project Template** - Reference for all configuration files
6. **GitHub Repository Setup** - Professional repository configuration

### Phase 3: Deployment (Documents 7-8)
7. **Store Deployment Guide** - App store submission process
8. **Template Repository Setup** - Create reusable templates

### Phase 4: Reference (Documents 9-10)
9. **REMEMBER.md** - AI assistant context and patterns
10. **Development Workflow** - Ongoing development practices

---

## 🚀 Initial Setup Process

### 1. Repository Initialization (5 minutes)
```bash
# Set project variables
export PROJECT_NAME="your-app-name"
export GITHUB_USERNAME="your-username"
export PACKAGE_NAME="com.yourcompany.yourapp"

# Create GitHub repository
gh repo create $PROJECT_NAME --public --clone --description "Cross-platform mobile app with backend services"
cd $PROJECT_NAME

# Initialize project structure
mkdir -p {mobile/{shared/src/{commonMain,androidMain,iosMain,commonTest}/kotlin,androidApp/src/main/kotlin,iosApp},backend/{api,database,services},shared-models,docs,scripts}
```

### 2. Core Configuration Files

#### Root `build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("runAllTests") {
    group = "verification"
    description = "Run all tests for all modules"
    dependsOn(
        ":mobile:shared:testDebugUnitTest",
        ":mobile:androidApp:testDebugUnitTest"
    )
}

// Build variant tasks
tasks.register("buildDev") {
    group = "build"
    description = "Build development variant"
    dependsOn(
        ":mobile:androidApp:assembleDevDebug",
        ":mobile:shared:iosSimulatorArm64Test"
    )
}

tasks.register("buildProd") {
    group = "build"
    description = "Build production variant"
    dependsOn(
        ":mobile:androidApp:assembleProdRelease"
    )
}
```

#### `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "YourAppName"
include(":mobile:shared")
include(":mobile:androidApp")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")
```

#### `gradle/libs.versions.toml`
```toml
[versions]
kotlin = "1.9.22"
compose = "1.5.11"
agp = "8.2.2"
compileSdk = "34"
targetSdk = "34"
minSdk = "24"
koin = "3.5.3"
detekt = "1.23.5"
coroutines = "1.7.3"
ktor = "2.3.7"
voyager = "1.0.0"
serialization = "1.6.2"
napier = "2.7.1"

[libraries]
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

# Compose Multiplatform
compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "compose" }
compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "compose" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "compose" }
compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "compose" }
compose-ui-tooling-preview = { module = "org.jetbrains.compose.ui:ui-tooling-preview", version.ref = "compose" }

# Dependency Injection
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-test = { module = "io.insert-koin:koin-test", version.ref = "koin" }

# Navigation
voyager-navigator = { module = "cafe.adriel.voyager:voyager-navigator", version.ref = "voyager" }
voyager-screenmodel = { module = "cafe.adriel.voyager:voyager-screenmodel", version.ref = "voyager" }
voyager-koin = { module = "cafe.adriel.voyager:voyager-koin", version.ref = "voyager" }

# Networking
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

# Logging
napier = { module = "io.github.aakira:napier", version.ref = "napier" }

# Android
androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.12.0" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version = "1.8.2" }
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version = "2.7.0" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose" }
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }

[bundles]
ktor-common = ["ktor-client-core", "ktor-client-content-negotiation", "ktor-client-logging", "ktor-serialization-kotlinx-json"]
voyager = ["voyager-navigator", "voyager-screenmodel", "voyager-koin"]
```

### 3. Mobile App Structure Setup

#### Shared Module (`mobile/shared/build.gradle.kts`)
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget()
    
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.voyager.navigator)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.yourcompany.yourapp.shared"
    compileSdk = 34
    
    defaultConfig { 
        minSdk = 24
    }
    
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.yourapp.com\"")
            buildConfigField("String", "APP_VARIANT", "\"dev\"")
            buildConfigField("boolean", "DEBUG_MODE", "true")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://api.yourapp.com\"")
            buildConfigField("String", "APP_VARIANT", "\"prod\"")
            buildConfigField("boolean", "DEBUG_MODE", "false")
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

#### Theme Setup (`mobile/shared/src/commonMain/kotlin/ui/theme/Theme.kt`)
```kotlin
package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Primary = Color(0xFF6200EE)
    val DarkPrimary = Color(0xFFBB86FC)
    val Background = Color(0xFFFFFBFE)
    val DarkBackground = Color(0xFF121212)
    val Surface = Color(0xFFFFFBFE)
    val DarkSurface = Color(0xFF121212)
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    background = AppColors.Background,
    surface = AppColors.Surface
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.DarkPrimary,
    background = AppColors.DarkBackground,
    surface = AppColors.DarkSurface
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

#### Dependency Injection (`mobile/shared/src/commonMain/kotlin/di/AppModule.kt`)
```kotlin
package di

import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    // Add your dependencies here
}

fun initKoin() = startKoin {
    modules(appModule)
}
```

#### Main App (`mobile/shared/src/commonMain/kotlin/App.kt`)
```kotlin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import di.initKoin
import ui.theme.AppTheme

@Composable
fun App() {
    LaunchedEffect(Unit) {
        initKoin()
    }
    
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Navigator(HomeScreen())
        }
    }
}
```

#### Android App Module (`mobile/androidApp/build.gradle.kts`)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(project(":mobile:shared"))
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
        }
    }
}

android {
    namespace = "com.yourcompany.yourapp"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    // Product flavors for dev/prod variants
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "YourApp Dev")
            buildConfigField("String", "BASE_URL", "\"https://api-dev.yourapp.com\"")
            buildConfigField("boolean", "DEBUG_FEATURES", "true")
        }
        
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "YourApp")
            buildConfigField("String", "BASE_URL", "\"https://api.yourapp.com\"")
            buildConfigField("boolean", "DEBUG_FEATURES", "false")
        }
    }
    
    signingConfigs {
        create("dev") {
            // Debug signing for dev builds
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = java.util.Properties()
                keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
                
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }
    
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("dev")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    // Variant-specific configurations
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val variantName = variant.name.replaceFirstChar { it.uppercaseChar() }
            output.outputFileName = "YourApp-${variant.versionName}-${variantName}.apk"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

### 4. Environment Configuration

#### Environment Configuration (`mobile/shared/src/commonMain/kotlin/config/AppConfig.kt`)
```kotlin
package config

object AppConfig {
    const val DEV_API_URL = "https://api-dev.yourapp.com"
    const val PROD_API_URL = "https://api.yourapp.com"
    
    const val DEV_WS_URL = "wss://ws-dev.yourapp.com"
    const val PROD_WS_URL = "wss://ws.yourapp.com"
    
    // Feature flags
    const val ENABLE_DEBUG_MENU = true // Will be overridden by build config
    const val ENABLE_ANALYTICS = true
    const val ENABLE_CRASH_REPORTING = true
}

enum class AppEnvironment {
    DEV, PROD
}

expect object PlatformConfig {
    val environment: AppEnvironment
    val apiBaseUrl: String
    val wsBaseUrl: String
    val enableDebugFeatures: Boolean
    val enableLogging: Boolean
}
```

#### Android Platform Config (`mobile/shared/src/androidMain/kotlin/config/PlatformConfig.android.kt`)
```kotlin
package config

import com.yourcompany.yourapp.shared.BuildConfig

actual object PlatformConfig {
    actual val environment: AppEnvironment = when (BuildConfig.APP_VARIANT) {
        "dev" -> AppEnvironment.DEV
        "prod" -> AppEnvironment.PROD
        else -> AppEnvironment.DEV
    }
    
    actual val apiBaseUrl: String = BuildConfig.API_BASE_URL
    
    actual val wsBaseUrl: String = when (environment) {
        AppEnvironment.DEV -> AppConfig.DEV_WS_URL
        AppEnvironment.PROD -> AppConfig.PROD_WS_URL
    }
    
    actual val enableDebugFeatures: Boolean = BuildConfig.DEBUG_MODE
    actual val enableLogging: Boolean = BuildConfig.DEBUG_MODE
}
```

#### iOS Platform Config (`mobile/shared/src/iosMain/kotlin/config/PlatformConfig.ios.kt`)
```kotlin
package config

import platform.Foundation.NSBundle

actual object PlatformConfig {
    private val bundle = NSBundle.mainBundle
    
    actual val environment: AppEnvironment = when (
        bundle.objectForInfoDictionaryKey("APP_VARIANT") as? String ?: "dev"
    ) {
        "dev" -> AppEnvironment.DEV
        "prod" -> AppEnvironment.PROD
        else -> AppEnvironment.DEV
    }
    
    actual val apiBaseUrl: String = when (environment) {
        AppEnvironment.DEV -> AppConfig.DEV_API_URL
        AppEnvironment.PROD -> AppConfig.PROD_API_URL
    }
    
    actual val wsBaseUrl: String = when (environment) {
        AppEnvironment.DEV -> AppConfig.DEV_WS_URL
        AppEnvironment.PROD -> AppConfig.PROD_WS_URL
    }
    
    actual val enableDebugFeatures: Boolean = environment == AppEnvironment.DEV
    actual val enableLogging: Boolean = environment == AppEnvironment.DEV
}
```

#### Network Configuration (`mobile/shared/src/commonMain/kotlin/data/network/NetworkConfig.kt`)
```kotlin
package data.network

import config.PlatformConfig
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NetworkConfig {
    fun createHttpClient(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            
            install(DefaultRequest) {
                url(PlatformConfig.apiBaseUrl)
                headers.append("User-Agent", "YourApp/1.0.0")
            }
            
            if (PlatformConfig.enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 30000
            }
        }
    }
}
```

### 5. Testing Infrastructure

#### Sample Test (`mobile/shared/src/commonTest/kotlin/AppTest.kt`)
```kotlin
import kotlin.test.Test
import kotlin.test.assertTrue

class AppTest {
    @Test
    fun testExample() {
        assertTrue(true, "Basic test passes")
    }
}
```

### 5. Code Quality Setup

#### Detekt Configuration (`detekt.yml`)
```yaml
build:
  maxIssues: 0
  excludeCorrectable: false

config:
  validation: true
  warningsAsErrors: false

style:
  MaxLineLength:
    maxLineLength: 120
  MagicNumber:
    active: false

complexity:
  LongMethod:
    threshold: 60
  LongParameterList:
    functionThreshold: 8

naming:
  FunctionNaming:
    active: true
  VariableNaming:
    active: true
```

### 6. CI/CD Pipeline (`.github/workflows/ci.yml`)
```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2
        
      - name: Run Tests
        run: ./gradlew runAllTests
        
      - name: Run Detekt
        run: ./gradlew detekt

  android-build-dev:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Build Android Dev Debug
        run: ./gradlew :mobile:androidApp:assembleDevDebug
        
      - name: Upload Dev APK
        uses: actions/upload-artifact@v3
        with:
          name: android-dev-debug-apk
          path: mobile/androidApp/build/outputs/apk/dev/debug/
          retention-days: 7

  android-build-prod:
    runs-on: ubuntu-latest
    needs: test
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Decode Keystore
        if: env.KEYSTORE_BASE64 != ''
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: |
          echo "$KEYSTORE_BASE64" | base64 -d > mobile/androidApp/release-keystore.jks
          
      - name: Create keystore.properties
        if: env.KEYSTORE_PASSWORD != ''
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          echo "storePassword=$KEYSTORE_PASSWORD" > keystore.properties
          echo "keyPassword=$KEY_PASSWORD" >> keystore.properties
          echo "keyAlias=$KEY_ALIAS" >> keystore.properties
          echo "storeFile=mobile/androidApp/release-keystore.jks" >> keystore.properties
          
      - name: Build Android Prod Release
        run: ./gradlew :mobile:androidApp:assembleProdRelease
        
      - name: Upload Prod APK
        uses: actions/upload-artifact@v3
        with:
          name: android-prod-release-apk
          path: mobile/androidApp/build/outputs/apk/prod/release/
          retention-days: 30

  ios-build:
    runs-on: macos-latest
    needs: test
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Build iOS Framework
        run: ./gradlew :mobile:shared:iosSimulatorArm64Test
        
      - name: Upload iOS Build Logs
        uses: actions/upload-artifact@v3
        if: failure()
        with:
          name: ios-build-logs
          path: build/logs/

  deploy-dev:
    runs-on: ubuntu-latest
    needs: [android-build-dev]
    if: github.ref == 'refs/heads/develop'
    steps:
      - uses: actions/checkout@v4
      
      - name: Download Dev APK
        uses: actions/download-artifact@v3
        with:
          name: android-dev-debug-apk
          path: ./apk/
          
      - name: Deploy to Development
        run: |
          echo "🚀 Deploying to development environment..."
          # Add your deployment script here
          # Example: Firebase App Distribution, internal testing, etc.
```

#### Release Pipeline (`.github/workflows/release.yml`)
```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
          
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2
        
      - name: Decode Keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: |
          echo "$KEYSTORE_BASE64" | base64 -d > mobile/androidApp/release-keystore.jks
          
      - name: Create keystore.properties
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          echo "storePassword=$KEYSTORE_PASSWORD" > keystore.properties
          echo "keyPassword=$KEY_PASSWORD" >> keystore.properties
          echo "keyAlias=$KEY_ALIAS" >> keystore.properties
          echo "storeFile=mobile/androidApp/release-keystore.jks" >> keystore.properties
          
      - name: Build Production Release
        run: ./gradlew :mobile:androidApp:bundleProdRelease
        
      - name: Generate Release Notes
        id: release_notes
        run: |
          echo "RELEASE_NOTES<<EOF" >> $GITHUB_OUTPUT
          git log --pretty=format:"- %s" $(git describe --tags --abbrev=0 HEAD~1)..HEAD >> $GITHUB_OUTPUT
          echo "EOF" >> $GITHUB_OUTPUT
          
      - name: Create GitHub Release
        uses: actions/create-release@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          tag_name: ${{ github.ref_name }}
          release_name: Release ${{ github.ref_name }}
          body: ${{ steps.release_notes.outputs.RELEASE_NOTES }}
          draft: false
          prerelease: false
```

---

## 🎯 Development Workflow

### Daily Development Commands
```bash
# Start development environment
./scripts/dev.sh start

# Build and install development variant
./scripts/dev.sh build-dev
./scripts/variant-manager.sh quick-dev

# Build production variant
./scripts/dev.sh build-prod
./scripts/variant-manager.sh quick-prod

# Run tests
./gradlew runAllTests

# Code quality check
./gradlew detekt

# Build all platforms
./gradlew build

# Variant-specific commands
./gradlew :mobile:androidApp:installDevDebug     # Install dev variant
./gradlew :mobile:androidApp:installProdDebug    # Install prod variant

# Asset generation
./scripts/generate-icons.sh

# Setup keystore for release
./scripts/generate-keystore.sh
```

### Build Variants Overview
```bash
# View all available variants
./scripts/variant-manager.sh show

# Development Variant:
#   - Package: com.yourcompany.yourapp.dev
#   - API: https://api-dev.yourapp.com
#   - Features: Debug menu, detailed logging, test data
#   - Can be installed alongside production version

# Production Variant:
#   - Package: com.yourcompany.yourapp
#   - API: https://api.yourapp.com
#   - Features: Optimized performance, minimal logging
#   - Used for app store releases
```

### iOS Configuration for Variants

#### iOS Build Schemes Setup
For iOS variants, you'll need to configure different schemes in Xcode:

1. **Open `mobile/iosApp/iosApp.xcworkspace` in Xcode**
2. **Create Build Schemes:**
   - Product → Scheme → Manage Schemes
   - Duplicate the existing scheme
   - Rename to "iosApp Dev" and "iosApp Prod"

3. **Configure Info.plist variants:**

Create `mobile/iosApp/iosApp/Info-Dev.plist`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDisplayName</key>
    <string>YourApp Dev</string>
    <key>CFBundleIdentifier</key>
    <string>com.yourcompany.yourapp.dev</string>
    <key>APP_VARIANT</key>
    <string>dev</string>
    <key>API_BASE_URL</key>
    <string>https://api-dev.yourapp.com</string>
    <!-- Other iOS configuration -->
</dict>
</plist>
```

Create `mobile/iosApp/iosApp/Info-Prod.plist`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDisplayName</key>
    <string>YourApp</string>
    <key>CFBundleIdentifier</key>
    <string>com.yourcompany.yourapp</string>
    <key>APP_VARIANT</key>
    <string>prod</string>
    <key>API_BASE_URL</key>
    <string>https://api.yourapp.com</string>
    <!-- Other iOS configuration -->
</dict>
</plist>
```

4. **Configure Build Settings:**
   - Select your target in Xcode
   - Go to Build Settings
   - Add custom build settings for each configuration:
     - `INFOPLIST_FILE` = `iosApp/Info-Dev.plist` (for Dev)
     - `INFOPLIST_FILE` = `iosApp/Info-Prod.plist` (for Prod)

### Environment-Specific Testing
```bash
# Test development environment
./scripts/variant-manager.sh install dev debug
# Verify API calls go to dev environment
# Check debug features are enabled

# Test production environment  
./scripts/variant-manager.sh install prod debug
# Verify API calls go to production environment
# Check production optimizations
```

### Development Scripts (`scripts/dev.sh`)
```bash
#!/bin/bash

case "$1" in
    "start")
        echo "🚀 Starting development environment..."
        ./gradlew build
        ./gradlew runAllTests
        echo "✅ Ready for development!"
        ;;
    "test")
        echo "🧪 Running all tests..."
        ./gradlew runAllTests
        ;;
    "quality")
        echo "📋 Running code quality checks..."
        ./gradlew detekt
        ;;
    "build")
        echo "🔨 Building all platforms..."
        ./gradlew build
        ;;
    "build-dev")
        echo "🔨 Building development variant..."
        ./gradlew buildDev
        ./gradlew :mobile:androidApp:installDevDebug
        echo "✅ Dev build installed!"
        ;;
    "build-prod")
        echo "🔨 Building production variant..."
        ./gradlew buildProd
        echo "✅ Production build complete!"
        ;;
    "install-dev")
        echo "📱 Installing development app..."
        ./gradlew :mobile:androidApp:installDevDebug
        echo "✅ Dev app installed!"
        ;;
    "install-prod")
        echo "📱 Installing production app..."
        ./gradlew :mobile:androidApp:installProdDebug
        echo "✅ Production app installed!"
        ;;
    "clean")
        echo "🧹 Cleaning build artifacts..."
        ./gradlew clean
        ;;
    "variant-info")
        echo "📊 Build Variant Information:"
        echo ""
        echo "Development Variant:"
        echo "  - Package: com.yourcompany.yourapp.dev"
        echo "  - API: https://api-dev.yourapp.com"
        echo "  - Features: Debug menu enabled"
        echo "  - Install: ./scripts/dev.sh install-dev"
        echo ""
        echo "Production Variant:"
        echo "  - Package: com.yourcompany.yourapp"
        echo "  - API: https://api.yourapp.com"
        echo "  - Features: Production optimized"
        echo "  - Install: ./scripts/dev.sh install-prod"
        ;;
    "info")
        echo "📱 Project Information:"
        echo "  - Android Dev: ./gradlew :mobile:androidApp:installDevDebug"
        echo "  - Android Prod: ./gradlew :mobile:androidApp:installProdDebug"
        echo "  - iOS: Open mobile/iosApp/iosApp.xcworkspace in Xcode"
        echo "  - Tests: ./gradlew runAllTests"
        echo "  - Quality: ./gradlew detekt"
        echo "  - Variants: ./scripts/dev.sh variant-info"
        ;;
    *)
        echo "Usage: $0 {start|test|quality|build|build-dev|build-prod|install-dev|install-prod|clean|variant-info|info}"
        ;;
esac
```

### Variant Management Script (`scripts/variant-manager.sh`)
```bash
#!/bin/bash

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}    Build Variant Manager${NC}"
    echo -e "${BLUE}================================${NC}"
    echo ""
}

show_variants() {
    echo -e "${YELLOW}Available Build Variants:${NC}"
    echo ""
    echo -e "${GREEN}Development (dev):${NC}"
    echo "  📱 Package: com.yourcompany.yourapp.dev"
    echo "  🌐 API: https://api-dev.yourapp.com"
    echo "  🔧 Features: Debug menu, detailed logging"
    echo "  🎯 Purpose: Testing and development"
    echo ""
    echo -e "${GREEN}Production (prod):${NC}"
    echo "  📱 Package: com.yourcompany.yourapp"
    echo "  🌐 API: https://api.yourapp.com"
    echo "  🔧 Features: Optimized, minimal logging"
    echo "  🎯 Purpose: App store releases"
    echo ""
}

build_variant() {
    local variant=$1
    local build_type=${2:-debug}
    
    echo -e "${YELLOW}Building ${variant} variant (${build_type})...${NC}"
    
    case "${variant}-${build_type}" in
        "dev-debug")
            ./gradlew :mobile:androidApp:assembleDevDebug
            ;;
        "dev-release")
            ./gradlew :mobile:androidApp:assembleDevRelease
            ;;
        "prod-debug")
            ./gradlew :mobile:androidApp:assembleProdDebug
            ;;
        "prod-release")
            ./gradlew :mobile:androidApp:assembleProdRelease
            ;;
        *)
            echo -e "${RED}Invalid variant-buildtype combination: ${variant}-${build_type}${NC}"
            return 1
            ;;
    esac
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Build successful!${NC}"
        
        # Show APK location
        APK_DIR="mobile/androidApp/build/outputs/apk/${variant}/${build_type}"
        if [ -d "$APK_DIR" ]; then
            echo -e "${BLUE}📦 APK location: ${APK_DIR}${NC}"
            ls -la "$APK_DIR"/*.apk 2>/dev/null || echo "APK files not found"
        fi
    else
        echo -e "${RED}❌ Build failed!${NC}"
        return 1
    fi
}

install_variant() {
    local variant=$1
    local build_type=${2:-debug}
    
    echo -e "${YELLOW}Installing ${variant} variant (${build_type})...${NC}"
    
    case "${variant}-${build_type}" in
        "dev-debug")
            ./gradlew :mobile:androidApp:installDevDebug
            ;;
        "dev-release")
            ./gradlew :mobile:androidApp:installDevRelease
            ;;
        "prod-debug")
            ./gradlew :mobile:androidApp:installProdDebug
            ;;
        "prod-release")
            ./gradlew :mobile:androidApp:installProdRelease
            ;;
        *)
            echo -e "${RED}Invalid variant-buildtype combination: ${variant}-${build_type}${NC}"
            return 1
            ;;
    esac
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Installation successful!${NC}"
        echo -e "${BLUE}📱 App installed and ready to use${NC}"
    else
        echo -e "${RED}❌ Installation failed!${NC}"
        return 1
    fi
}

uninstall_variant() {
    local variant=$1
    
    case "$variant" in
        "dev")
            adb uninstall com.yourcompany.yourapp.dev
            ;;
        "prod")
            adb uninstall com.yourcompany.yourapp
            ;;
        "all")
            adb uninstall com.yourcompany.yourapp.dev
            adb uninstall com.yourcompany.yourapp
            ;;
        *)
            echo -e "${RED}Invalid variant: $variant${NC}"
            return 1
            ;;
    esac
    
    echo -e "${GREEN}✅ Uninstallation complete${NC}"
}

case "$1" in
    "show"|"list")
        print_header
        show_variants
        ;;
    "build")
        print_header
        if [ -z "$2" ]; then
            echo -e "${RED}Usage: $0 build <variant> [build_type]${NC}"
            echo -e "${YELLOW}Variants: dev, prod${NC}"
            echo -e "${YELLOW}Build types: debug, release${NC}"
            exit 1
        fi
        build_variant "$2" "$3"
        ;;
    "install")
        print_header
        if [ -z "$2" ]; then
            echo -e "${RED}Usage: $0 install <variant> [build_type]${NC}"
            echo -e "${YELLOW}Variants: dev, prod${NC}"
            echo -e "${YELLOW}Build types: debug, release${NC}"
            exit 1
        fi
        install_variant "$2" "$3"
        ;;
    "uninstall")
        print_header
        if [ -z "$2" ]; then
            echo -e "${RED}Usage: $0 uninstall <variant|all>${NC}"
            echo -e "${YELLOW}Variants: dev, prod, all${NC}"
            exit 1
        fi
        uninstall_variant "$2"
        ;;
    "quick-dev")
        print_header
        echo -e "${YELLOW}Quick development setup...${NC}"
        build_variant "dev" "debug"
        if [ $? -eq 0 ]; then
            install_variant "dev" "debug"
        fi
        ;;
    "quick-prod")
        print_header
        echo -e "${YELLOW}Quick production setup...${NC}"
        build_variant "prod" "release"
        ;;
    *)
        print_header
        echo -e "${YELLOW}Usage: $0 {show|build|install|uninstall|quick-dev|quick-prod}${NC}"
        echo ""
        echo -e "${BLUE}Commands:${NC}"
        echo "  show                     - Show available variants"
        echo "  build <variant> [type]   - Build specific variant"
        echo "  install <variant> [type] - Install specific variant"
        echo "  uninstall <variant|all>  - Uninstall variant(s)"
        echo "  quick-dev               - Build and install dev variant"
        echo "  quick-prod              - Build production variant"
        echo ""
        echo -e "${BLUE}Examples:${NC}"
        echo "  $0 build dev debug"
        echo "  $0 install prod release"
        echo "  $0 quick-dev"
        ;;
esac
```

### Git Hooks Setup (`scripts/install-hooks.sh`)
```bash
#!/bin/bash

echo "📋 Installing Git hooks..."

# Pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
echo "🔍 Running pre-commit checks..."

# Run detekt
./gradlew detekt
if [ $? -ne 0 ]; then
    echo "❌ Detekt failed. Please fix issues before committing."
    exit 1
fi

# Run tests
./gradlew runAllTests
if [ $? -ne 0 ]; then
    echo "❌ Tests failed. Please fix failing tests before committing."
    exit 1
fi

echo "✅ All checks passed!"
EOF

chmod +x .git/hooks/pre-commit
echo "✅ Git hooks installed!"
```

---

## 🧪 Testing & Quality Assurance

### Testing Strategy
1. **Unit Tests**: Business logic in shared module
2. **Integration Tests**: API and database interactions
3. **UI Tests**: Critical user flows
4. **Performance Tests**: App startup and memory usage

### Test Structure
```
mobile/shared/src/commonTest/kotlin/
├── data/repository/        # Repository tests
├── domain/usecase/         # Use case tests
├── di/                     # DI configuration tests
└── ui/                     # UI component tests
```

### Sample Test Patterns
```kotlin
// Repository test
class SampleRepositoryTest {
    @Test
    fun `test data fetching success`() = runTest {
        val repository = SampleRepositoryImpl()
        val result = repository.fetchData()
        assertTrue(result.isSuccess)
    }
}

// DI test
class KoinTest : KoinTest {
    @BeforeTest
    fun setup() {
        startKoin { modules(testModule) }
    }
    
    @AfterTest
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `verify dependency injection`() {
        val repository: SampleRepository by inject()
        assertNotNull(repository)
    }
}
```

### Quality Gates
- **Code Coverage**: Minimum 80%
- **Detekt**: Zero issues allowed
- **Build Time**: Under 5 minutes
- **App Size**: APK under 50MB

---

## 📱 Store Deployment

### Android (Google Play Store)

#### 1. Keystore Generation for Different Variants
```bash
#!/bin/bash
# scripts/generate-keystore.sh

echo "🔐 Generating Android keystores..."

# Debug keystore for development (auto-generated, can be shared)
echo "📝 Debug keystore will be auto-generated for dev builds"

# Release keystore for production (NEVER SHARE)
keytool -genkey -v -keystore mobile/androidApp/release-keystore.jks \
    -keyalg RSA -keysize 2048 -validity 10000 -alias release \
    -dname "CN=YourCompany, OU=Mobile, O=YourCompany, L=YourCity, S=YourState, C=YourCountry"

echo "✅ Production keystore generated!"
echo "📝 Create keystore.properties with your passwords (never commit this file)"
echo ""
echo "keystore.properties format:"
echo "storePassword=YOUR_STORE_PASSWORD"
echo "keyPassword=YOUR_KEY_PASSWORD"
echo "keyAlias=release"
echo "storeFile=mobile/androidApp/release-keystore.jks"
```

#### 2. Build Configuration with Variants
The Android build configuration already includes variant support. Use these commands:

```bash
# Development builds (can be installed alongside production)
./gradlew :mobile:androidApp:assembleDevDebug      # Debug build
./gradlew :mobile:androidApp:assembleDevRelease    # Release build for internal testing

# Production builds (for app store)
./gradlew :mobile:androidApp:assembleProdRelease   # APK for direct distribution
./gradlew :mobile:androidApp:bundleProdRelease     # AAB for Google Play (recommended)
```

#### 3. Variant-Specific Release Process
```bash
#!/bin/bash
# scripts/release-android.sh

VARIANT=${1:-prod}
BUILD_TYPE=${2:-release}

echo "🚀 Building Android $VARIANT $BUILD_TYPE..."

case "$VARIANT" in
    "dev")
        echo "📱 Building development variant for internal testing..."
        ./gradlew :mobile:androidApp:assembleDevRelease
        echo "✅ Dev release ready for internal distribution"
        ;;
    "prod")
        echo "📱 Building production variant for app store..."
        ./gradlew :mobile:androidApp:bundleProdRelease
        echo "✅ Production AAB ready for Google Play Store"
        echo "📦 Location: mobile/androidApp/build/outputs/bundle/prodRelease/"
        ;;
    *)
        echo "❌ Invalid variant: $VARIANT"
        echo "Usage: $0 {dev|prod} [debug|release]"
        exit 1
        ;;
esac
```

### iOS (App Store)

#### 1. Xcode Configuration for Variants
Configure different schemes for Dev and Prod environments:

```bash
# Build development scheme
xcodebuild -workspace mobile/iosApp/iosApp.xcworkspace \
    -scheme "iosApp Dev" \
    -configuration Debug \
    -destination "generic/platform=iOS Simulator" \
    build

# Build production scheme  
xcodebuild -workspace mobile/iosApp/iosApp.xcworkspace \
    -scheme "iosApp Prod" \
    -configuration Release \
    -archivePath build/iosApp-Prod.xcarchive \
    archive
```

#### 2. Export Options for Different Variants

Create `mobile/iosApp/exportOptions-Dev.plist`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>development</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>compileBitcode</key>
    <false/>
    <key>uploadSymbols</key>
    <true/>
    <key>signingStyle</key>
    <string>automatic</string>
</dict>
</plist>
```

Create `mobile/iosApp/exportOptions-Prod.plist`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>app-store</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>uploadBitcode</key>
    <false/>
    <key>uploadSymbols</key>
    <true/>
    <key>signingStyle</key>
    <string>automatic</string>
    <key>destination</key>
    <string>upload</string>
</dict>
</plist>
```

#### 3. iOS Release Script
```bash
#!/bin/bash
# scripts/release-ios.sh

VARIANT=${1:-prod}
SCHEME="iosApp ${VARIANT^}"  # Capitalize first letter

echo "🍎 Building iOS $VARIANT variant..."

case "$VARIANT" in
    "dev")
        echo "📱 Building development variant for internal testing..."
        xcodebuild -workspace mobile/iosApp/iosApp.xcworkspace \
            -scheme "$SCHEME" \
            -configuration Debug \
            -archivePath "build/iosApp-Dev.xcarchive" \
            archive
            
        xcodebuild -exportArchive \
            -archivePath "build/iosApp-Dev.xcarchive" \
            -exportPath "build/ios-dev/" \
            -exportOptionsPlist "mobile/iosApp/exportOptions-Dev.plist"
        ;;
    "prod")
        echo "📱 Building production variant for App Store..."
        xcodebuild -workspace mobile/iosApp/iosApp.xcworkspace \
            -scheme "$SCHEME" \
            -configuration Release \
            -archivePath "build/iosApp-Prod.xcarchive" \
            archive
            
        xcodebuild -exportArchive \
            -archivePath "build/iosApp-Prod.xcarchive" \
            -exportPath "build/ios-prod/" \
            -exportOptionsPlist "mobile/iosApp/exportOptions-Prod.plist"
        ;;
    *)
        echo "❌ Invalid variant: $VARIANT"
        echo "Usage: $0 {dev|prod}"
        exit 1
        ;;
esac
```

### Marketing Assets Generation with Variants
```bash
#!/bin/bash
# scripts/generate-marketing-assets.sh

VARIANT=${1:-prod}
MASTER_ICON="assets/icons/icon-1024.png"

echo "🎨 Generating marketing assets for $VARIANT variant..."

case "$VARIANT" in
    "dev")
        # Development assets with "DEV" badge
        convert $MASTER_ICON \
            \( -background red -fill white -gravity center -size 200x60 caption:"DEV" \) \
            -gravity southeast -composite \
            -resize 512x512 assets/store/android/icon-512-dev.png
            
        # Feature graphic with dev branding
        convert -size 1024x500 gradient:blue-darkblue \
            \( $MASTER_ICON -resize 200x200 \) -gravity west -geometry +50+0 -composite \
            \( -background red -fill white -gravity center -size 150x50 caption:"DEV" \) \
            -gravity northeast -geometry +20+20 -composite \
            -font Arial -pointsize 48 -fill white \
            -gravity center -annotate +150+0 "Your App DEV" \
            assets/store/android/feature-graphic-dev.png
        ;;
    "prod")
        # Production assets
        convert $MASTER_ICON -resize 512x512 assets/store/android/icon-512.png
        cp $MASTER_ICON assets/store/ios/app-store-icon.png
        
        # Feature graphic for Google Play (1024x500)
        convert -size 1024x500 gradient:blue-lightblue \
            \( $MASTER_ICON -resize 200x200 \) -gravity west -geometry +50+0 -composite \
            -font Arial -pointsize 48 -fill white \
            -gravity center -annotate +150+0 "Your App" \
            assets/store/android/feature-graphic.png
        ;;
    *)
        echo "❌ Invalid variant: $VARIANT"
        echo "Usage: $0 {dev|prod}"
        exit 1
        ;;
esac

echo "✅ Marketing assets generated for $VARIANT!"
```

### Release Management Strategy

#### Development Releases
- **Purpose**: Internal testing, QA, stakeholder reviews
- **Frequency**: Daily/weekly builds from develop branch
- **Distribution**: Internal testers, Firebase App Distribution, TestFlight
- **Features**: Debug tools enabled, test data, detailed logging

#### Production Releases
- **Purpose**: App store distribution to end users
- **Frequency**: Bi-weekly/monthly releases from main branch
- **Distribution**: Google Play Store, Apple App Store
- **Features**: Optimized performance, analytics, crash reporting

#### Release Automation
```bash
# Tag-based releases
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# This triggers GitHub Actions release pipeline which:
# 1. Builds production variants
# 2. Runs all tests
# 3. Creates GitHub release
# 4. Uploads to app stores (if configured)
```

---

## 🤖 AI Assistant Context

### Key Information for AI Assistants

#### Project Architecture Understanding
- **Clean Architecture**: Data → Domain → Presentation layers
- **Shared Module**: Contains business logic, UI components, data models
- **Platform Modules**: Android and iOS specific implementations
- **DI Pattern**: Koin for dependency injection throughout the app

#### Common Patterns to Follow
```kotlin
// Dependency Injection
@Composable
fun MyScreen() {
    val repository: Repository = koinInject()
}

// Navigation
val navigator = LocalNavigator.current
navigator?.push(DetailScreen(id))

// State Management
@Composable
fun MyScreen() {
    var uiState by remember { mutableStateOf(UiState.Loading) }
    val scope = rememberCoroutineScope()
}
```

#### Important Constraints
- **Platform Compatibility**: Always consider iOS and Android differences
- **Build System**: Use established Gradle patterns
- **Code Quality**: Must pass Detekt analysis
- **Testing**: All business logic must be testable
- **CI/CD**: Changes must not break automated pipeline

#### File Structure Expectations
```
mobile/shared/src/commonMain/kotlin/
├── ui/theme/           # MaterialTheme configuration
├── ui/components/      # Reusable UI components
├── ui/screens/         # Screen-level composables
├── data/repository/    # Data access layer
├── domain/usecase/     # Business logic
├── di/                 # Dependency injection
└── utils/              # Utility functions
```

#### Common Commands
```bash
# Development workflow
./gradlew build                    # Build entire project
./gradlew runAllTests             # Run all tests
./gradlew detekt                  # Code quality check
./scripts/dev.sh start            # Development environment setup

# Platform-specific
./gradlew :mobile:androidApp:installDebug    # Install Android debug
./gradlew :mobile:shared:iosSimulatorArm64Test  # iOS build test
```

### Problem-Solving Approach
1. **Check compatibility** across both platforms first
2. **Use established patterns** from the existing codebase
3. **Maintain test coverage** for any new functionality
4. **Follow Clean Architecture** principles
5. **Ensure CI/CD pipeline** continues to pass
6. **Consider variant-specific requirements** (dev vs prod behavior)

#### Build Variants Context
When working with build variants, AI assistants should understand:
- **Development variants** enable debug features, use test APIs, allow side-by-side installation
- **Production variants** are optimized for release, use production APIs, target app stores
- **Configuration differences** are managed through build config and platform config
- **Environment switching** should be seamless and automatic based on variant

#### Variant-Specific Development Patterns
```kotlin
// Environment-aware API client
class ApiClient {
    private val httpClient = NetworkConfig().createHttpClient(engine)
    
    suspend fun fetchData(): Result<DataResponse> {
        return try {
            // PlatformConfig.apiBaseUrl automatically switches based on variant
            val response = httpClient.get("${PlatformConfig.apiBaseUrl}/api/data")
            Result.success(response.body())
        } catch (e: Exception) {
            if (PlatformConfig.enableLogging) {
                println("API Error: ${e.message}")
            }
            Result.failure(e)
        }
    }
}

// Debug-only features
@Composable
fun MainScreen() {
    var showDebugInfo by remember { mutableStateOf(false) }
    
    Column {
        // Main app content
        AppContent()
        
        // Debug features only in development builds
        if (PlatformConfig.enableDebugFeatures) {
            DebugInfoPanel(
                visible = showDebugInfo,
                onToggle = { showDebugInfo = !showDebugInfo }
            )
        }
    }
}
```

---

## 📞 Quick Reference Commands

### Project Management
```bash
# Initialize new project
curl -fsSL https://raw.githubusercontent.com/username/template/main/setup.sh | bash -s -- "app-name" "username"

# Development environment
./scripts/dev.sh start
./scripts/dev.sh info

# Repository setup
gh repo create app-name --public --clone
gh repo edit --enable-issues --enable-projects
```

### Build & Test
```bash
# Full build and test cycle
./gradlew clean build runAllTests detekt

# Variant-specific builds
./gradlew buildDev                              # Build dev variant (Android + iOS framework)
./gradlew buildProd                             # Build prod variant
./gradlew :mobile:androidApp:assembleDevDebug  # Android dev debug
./gradlew :mobile:androidApp:assembleProdRelease # Android prod release

# Quick variant setup
./scripts/variant-manager.sh quick-dev         # Build and install dev
./scripts/variant-manager.sh quick-prod        # Build prod variant

# iOS builds (macOS only)
./gradlew :mobile:shared:iosSimulatorArm64Test
./scripts/release-ios.sh dev                   # iOS dev build
./scripts/release-ios.sh prod                  # iOS prod build
```

### Variant Management
```bash
# View available variants
./scripts/variant-manager.sh show

# Build specific variants
./scripts/variant-manager.sh build dev debug
./scripts/variant-manager.sh build prod release

# Install variants (Android)
./scripts/variant-manager.sh install dev debug
./scripts/variant-manager.sh install prod debug

# Uninstall variants
./scripts/variant-manager.sh uninstall dev     # Remove dev variant
./scripts/variant-manager.sh uninstall all     # Remove all variants

# Development workflow
./scripts/dev.sh build-dev                     # Build and install dev
./scripts/dev.sh install-dev                   # Install dev variant
./scripts/dev.sh variant-info                  # Show variant details
```

### Asset Generation
```bash
# Generate all icons
./scripts/generate-icons.sh

# Generate variant-specific marketing assets
./scripts/generate-marketing-assets.sh dev     # Dev assets with badge
./scripts/generate-marketing-assets.sh prod    # Production assets

# Create keystore for Android
./scripts/generate-keystore.sh

# Release builds
./scripts/release-android.sh dev               # Android dev release
./scripts/release-android.sh prod              # Android prod release
```

### Quality Assurance
```bash
# Code quality
./gradlew detekt
./gradlew detektFormat

# Test coverage
./gradlew runAllTests --info

# Environment testing
./scripts/variant-manager.sh install dev debug  # Test dev environment
./scripts/variant-manager.sh install prod debug # Test prod environment

# Dependency updates
./gradlew dependencyUpdates
```

### Environment Variables (CI/CD)
```bash
# Required secrets for GitHub Actions
KEYSTORE_BASE64                    # Base64 encoded release keystore
KEYSTORE_PASSWORD                  # Keystore password
KEY_ALIAS                         # Key alias
KEY_PASSWORD                      # Key password

# Optional for deployment
GOOGLE_PLAY_SERVICE_ACCOUNT       # Play Store deployment JSON
FIREBASE_TOKEN                    # Firebase App Distribution
```

---

## 🎯 Success Criteria

### Development Ready
- [ ] Project builds successfully on first try
- [ ] All tests pass
- [ ] CI/CD pipeline runs without errors
- [ ] Development scripts work correctly
- [ ] Documentation is complete
- [ ] **Both dev and prod variants build successfully**
- [ ] **Variants can be installed side-by-side (Android)**
- [ ] **Environment configuration switches correctly**

### Store Ready
- [ ] App icons generated for all platforms
- [ ] Screenshots captured and optimized
- [ ] Store listings written and reviewed
- [ ] Privacy policy and support pages created
- [ ] Release builds signed and tested
- [ ] **Production variant thoroughly tested**
- [ ] **Marketing assets generated for both variants**
- [ ] **Release automation working**

### Production Ready
- [ ] Performance benchmarks met
- [ ] Security review completed
- [ ] Analytics and crash reporting configured
- [ ] Beta testing feedback incorporated
- [ ] Store approval received
- [ ] **Production environment fully tested**
- [ ] **Monitoring and alerting set up**
- [ ] **Rollback strategy defined**

## 🔄 Build Variant Best Practices

### Development Workflow
1. **Always develop using the dev variant** - catches environment-specific issues early
2. **Test critical features in both variants** - ensures consistency across environments
3. **Use dev variant for demos and stakeholder reviews** - clearly distinguishable from production
4. **Automate variant switching** - minimize manual configuration

### Environment Management
```bash
# Daily development routine
./scripts/variant-manager.sh quick-dev          # Start with dev environment
# Develop features...
./scripts/dev.sh test                          # Run tests
./scripts/variant-manager.sh install prod debug # Test in prod environment
```

### Release Strategy
1. **Dev releases**: Daily/weekly for internal testing
2. **Prod releases**: Bi-weekly/monthly for app stores
3. **Feature flags**: Use for gradual rollouts
4. **Environment parity**: Keep dev and prod as similar as possible

### Security Considerations
- **Never commit keystore files** to version control
- **Use different API keys** for dev and prod environments
- **Implement certificate pinning** for production builds
- **Enable/disable debug features** based on build variant

### Monitoring & Analytics
- **Separate analytics** for dev and prod environments
- **Different crash reporting** configurations
- **Environment-specific logging** levels
- **Feature usage tracking** per variant

This guide provides everything needed to set up, develop, and deploy professional Compose Multiplatform mobile applications with full AI assistant support, automation, and proper environment management through build variants.