#!/bin/bash

# Project Setup Script
# This script helps you quickly set up a new Kotlin project with backend and mobile components
# It replaces template placeholders with your custom project name and package

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
DEFAULT_PROJECT_NAME="myproject"
DEFAULT_PACKAGE_NAME="com.example.myproject"
DEFAULT_AUTHOR="Developer"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to validate inputs
validate_project_name() {
    if [[ ! "$1" =~ ^[a-zA-Z][a-zA-Z0-9_-]*$ ]]; then
        print_error "Project name must start with a letter and contain only letters, numbers, hyphens, and underscores"
        return 1
    fi
}

validate_package_name() {
    if [[ ! "$1" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$ ]]; then
        print_error "Package name must be in format like 'com.example.myproject' (lowercase, dots separated)"
        return 1
    fi
    
    # Check for reserved keywords that confuse Android Studio
    if [[ "$1" =~ (^|\.)(this|is)(\.|$) ]]; then
        print_error "Package name cannot contain 'this' or 'is' as they confuse Android Studio"
        return 1
    fi
}

# Function to get user input
get_user_input() {
    echo ""
    echo "=== 🚀 Kotlin Project Setup ==="
    echo ""
    
    # Get project name
    read -p "Enter project name [$DEFAULT_PROJECT_NAME]: " PROJECT_NAME
    PROJECT_NAME=${PROJECT_NAME:-$DEFAULT_PROJECT_NAME}
    validate_project_name "$PROJECT_NAME"
    
    # Get package name
    read -p "Enter package name [$DEFAULT_PACKAGE_NAME]: " PACKAGE_NAME
    PACKAGE_NAME=${PACKAGE_NAME:-$DEFAULT_PACKAGE_NAME}
    validate_package_name "$PACKAGE_NAME"
    
    # Get author name
    read -p "Enter author name [$DEFAULT_AUTHOR]: " AUTHOR_NAME
    AUTHOR_NAME=${AUTHOR_NAME:-$DEFAULT_AUTHOR}
    
    # Show summary
    echo ""
    echo "=== Configuration Summary ==="
    echo "Project Name: $PROJECT_NAME"
    echo "Package Name: $PACKAGE_NAME"
    echo "Author: $AUTHOR_NAME"
    echo ""
    
    read -p "Continue with this configuration? [Y/n]: " CONFIRM
    if [[ $CONFIRM =~ ^[Nn]$ ]]; then
        print_warning "Setup cancelled"
        exit 0
    fi
}

# Function to create directory structure
create_backend_structure() {
    print_status "Creating backend directory structure..."
    
    # Create main directory structure
    mkdir -p "backend/src/main/kotlin/${PACKAGE_NAME//.//}"
    mkdir -p "backend/src/main/resources"
    mkdir -p "backend/src/test/kotlin/${PACKAGE_NAME//.//}"
    
    # Create subdirectories
    mkdir -p "backend/src/main/kotlin/${PACKAGE_NAME//.//}/domain"
    mkdir -p "backend/src/main/kotlin/${PACKAGE_NAME//.//}/infrastructure/database"
    mkdir -p "backend/src/main/kotlin/${PACKAGE_NAME//.//}/infrastructure/web"
    mkdir -p "backend/src/main/kotlin/${PACKAGE_NAME//.//}/plugins"
    mkdir -p "backend/src/main/kotlin/${PACKAGE_NAME//.//}/configuration"
    mkdir -p "backend/src/main/kotlin/${PACKAGE_NAME//.//}/services"
    mkdir -p "backend/src/main/kotlin/${PACKAGE_NAME//.//}/util"
    
    print_success "Backend structure created"
}


# Function to create backend files
create_backend_files() {
    print_status "Creating backend configuration files..."
    
    # Create build.gradle.kts
    cat > "backend/build.gradle.kts" << EOF
val ktor_version = "2.3.12"
val kotlin_version = "1.9.24"
val logback_version = "1.4.11"
val exposed_version = "0.44.1"

plugins {
    kotlin("jvm") version "1.9.24"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
}

group = "$PACKAGE_NAME"
version = "0.0.1"

application {
    mainClass.set("$PACKAGE_NAME.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=\$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:\$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:\$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:\$ktor_version")
    
    // Content & Serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:\$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:\$ktor_version")
    
    // Security
    implementation("io.ktor:ktor-server-auth-jvm:\$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:\$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:\$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:\$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:\$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:\$ktor_version")
    implementation("io.ktor:ktor-server-rate-limit-jvm:\$ktor_version")
    implementation("com.auth0:java-jwt:4.4.0")
    
    // Database (Exposed ORM)
    implementation("org.jetbrains.exposed:exposed-core:\$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:\$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:\$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:\$exposed_version")
    implementation("com.zaxxer:HikariCP:5.0.1")
    
    // Database Drivers
    implementation("com.h2database:h2:2.1.214")         // Development
    implementation("org.postgresql:postgresql:42.6.0")  // Production
    
    // HTTP Client for AI APIs
    implementation("io.ktor:ktor-client-core:\$ktor_version")
    implementation("io.ktor:ktor-client-cio:\$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:\$ktor_version")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:\$logback_version")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:\$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:\$kotlin_version")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.mockk:mockk:1.13.8")
}

// Fat JAR for deployment
tasks.jar {
    manifest {
        attributes["Main-Class"] = "$PACKAGE_NAME.ApplicationKt"
    }
    from(configurations.runtimeClasspath.get().map { 
        if (it.isDirectory) it else zipTree(it) 
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Detekt configuration
detekt {
    toolVersion = "1.23.1"
    config.from(file("detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
EOF

    # Create Application.kt
    cat > "backend/src/main/kotlin/${PACKAGE_NAME//.//}/Application.kt" << EOF
package $PACKAGE_NAME

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import $PACKAGE_NAME.plugins.*

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureDatabases()
    configureHTTP()
    configureMonitoring()
    configureStatusPages()
    configureRouting()
}
EOF

    # Create application.conf
    cat > "backend/src/main/resources/application.conf" << EOF
ktor {
    deployment {
        port = 8080
        port = \${?PORT}
    }
    application {
        modules = [ $PACKAGE_NAME.ApplicationKt.module ]
    }
}

database {
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    url = \${?DATABASE_URL}
}

jwt {
    secret = \${?JWT_SECRET}
    issuer = \${?JWT_ISSUER}  
    audience = \${?JWT_AUDIENCE}
}
EOF

    # Create Databases.kt plugin
    cat > "backend/src/main/kotlin/${PACKAGE_NAME//.//}/plugins/Databases.kt" << EOF
package $PACKAGE_NAME.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Application.configureDatabases() {
    val databaseUrl = System.getenv("DATABASE_URL") 
        ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    
    val (url, driver, user, password) = when {
        databaseUrl.startsWith("jdbc:postgresql") -> {
            listOf(
                databaseUrl, 
                "org.postgresql.Driver", 
                System.getenv("DATABASE_USER") ?: "", 
                System.getenv("DATABASE_PASSWORD") ?: ""
            )
        }
        else -> listOf(databaseUrl, "org.h2.Driver", "sa", "")
    }
    
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        driverClassName = driver
        username = user
        setPassword(password)
        maximumPoolSize = System.getenv("DB_POOL_SIZE")?.toInt() ?: 10
        connectionTimeout = 30000
        idleTimeout = 600000
        maxLifetime = 1800000
    }
    
    Database.connect(HikariDataSource(hikariConfig))
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
EOF

    # Create other plugin files
    cat > "backend/src/main/kotlin/${PACKAGE_NAME//.//}/plugins/HTTP.kt" << EOF
package $PACKAGE_NAME.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        
        // Development CORS - adjust for production
        allowHost("localhost:3000")
        allowHost("localhost:3001")
        allowHost("127.0.0.1:3000")
        
        // Add your production domains here
        // allowHost("yourdomain.com", schemes = listOf("https"))
    }
    
    install(DefaultHeaders) {
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("X-XSS-Protection", "1; mode=block")
        header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
    }
}
EOF

    cat > "backend/src/main/kotlin/${PACKAGE_NAME//.//}/plugins/Monitoring.kt" << EOF
package $PACKAGE_NAME.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}
EOF

    cat > "backend/src/main/kotlin/${PACKAGE_NAME//.//}/plugins/Routing.kt" << EOF
package $PACKAGE_NAME.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String, val timestamp: Long)

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    status = "healthy",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
EOF

    cat > "backend/src/main/kotlin/${PACKAGE_NAME//.//}/plugins/Security.kt" << EOF
package $PACKAGE_NAME.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-change-in-production"
    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "$PACKAGE_NAME"
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "$PACKAGE_NAME-users"
    
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "MyProject API"
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
}
EOF

    cat > "backend/src/main/kotlin/${PACKAGE_NAME//.//}/plugins/Serialization.kt" << EOF
package $PACKAGE_NAME.plugins

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
EOF

    cat > "backend/src/main/kotlin/${PACKAGE_NAME//.//}/plugins/StatusPages.kt" << EOF
package $PACKAGE_NAME.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String, val message: String)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("bad_request", cause.message ?: "Invalid request")
                    )
                }
                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("internal_error", "Internal server error")
                    )
                }
            }
        }
    }
}
EOF

    # Clean up template files if they exist
    if [ -d "backend/src/main/kotlin/com/example/myproject" ] && [ "$PACKAGE_NAME" != "com.example.myproject" ]; then
        print_status "Removing template files..."
        rm -rf "backend/src/main/kotlin/com/example/myproject"
        # Also remove empty parent directories if they exist
        [ -d "backend/src/main/kotlin/com/example" ] && rmdir "backend/src/main/kotlin/com/example" 2>/dev/null || true
        [ -d "backend/src/main/kotlin/com" ] && rmdir "backend/src/main/kotlin/com" 2>/dev/null || true
    fi
    
    # Clean up template test files if they exist
    if [ -d "backend/src/test/kotlin/com/example/myproject" ] && [ "$PACKAGE_NAME" != "com.example.myproject" ]; then
        rm -rf "backend/src/test/kotlin/com/example/myproject"
        # Also remove empty parent directories if they exist
        [ -d "backend/src/test/kotlin/com/example" ] && rmdir "backend/src/test/kotlin/com/example" 2>/dev/null || true
        [ -d "backend/src/test/kotlin/com" ] && rmdir "backend/src/test/kotlin/com" 2>/dev/null || true
    fi

    print_success "Backend files created"
}

# Function to update mobile project files
update_mobile_files() {
    print_status "Updating mobile project files..."
    
    # Create resource project name (lowercase, no hyphens/underscores)
    RESOURCE_PROJECT_NAME=$(echo "$PROJECT_NAME" | tr -d '_-' | tr '[:upper:]' '[:lower:]')
    
    # Update package names in existing files
    print_status "Updating package names and project references..."
    
    # Update composeApp/build.gradle.kts - replace namespace and applicationId
    if [ -f "mobile/composeApp/build.gradle.kts" ]; then
        sed -i '' "s/namespace = \".*\"/namespace = \"$PACKAGE_NAME\"/" mobile/composeApp/build.gradle.kts
        sed -i '' "s/applicationId = \".*\"/applicationId = \"$PACKAGE_NAME\"/" mobile/composeApp/build.gradle.kts
    fi
    
    # Move source files to new package structure
    OLD_PACKAGE_PATH="mobile/composeApp/src/commonMain/kotlin/com/example/myproject"
    NEW_PACKAGE_PATH="mobile/composeApp/src/commonMain/kotlin/${PACKAGE_NAME//.//}"
    ANDROID_OLD_PATH="mobile/composeApp/src/androidMain/kotlin/com/example/myproject"
    ANDROID_NEW_PATH="mobile/composeApp/src/androidMain/kotlin/${PACKAGE_NAME//.//}"
    IOS_OLD_PATH="mobile/composeApp/src/iosMain/kotlin/com/example/myproject"
    IOS_NEW_PATH="mobile/composeApp/src/iosMain/kotlin/${PACKAGE_NAME//.//}"
    
    # Create new package directories
    mkdir -p "$NEW_PACKAGE_PATH"
    mkdir -p "$ANDROID_NEW_PATH"
    mkdir -p "$IOS_NEW_PATH"
    
    # Move and update commonMain files
    if [ -d "$OLD_PACKAGE_PATH" ]; then
        # Copy entire directory structure with cp -r, then update package declarations
        cp -r "$OLD_PACKAGE_PATH"/* "$NEW_PACKAGE_PATH/"
        
        # Update package declarations and imports in all Kotlin files recursively
        find "$NEW_PACKAGE_PATH" -name "*.kt" -type f -exec sed -i '' "s/package com\.example\.myproject/package $PACKAGE_NAME/" {} \;
        find "$NEW_PACKAGE_PATH" -name "*.kt" -type f -exec sed -i '' "s/import com\.example\.myproject/import $PACKAGE_NAME/g" {} \;
        
        # Update generated resource imports (these use project name without hyphens/underscores, lowercase)
        find "$NEW_PACKAGE_PATH" -name "*.kt" -type f -exec sed -i '' "s/import myproject\.composeapp\.generated\.resources\./import ${RESOURCE_PROJECT_NAME}\.composeapp\.generated\.resources\./g" {} \;
        
        # Remove old directory structure
        rm -rf "mobile/composeApp/src/commonMain/kotlin/com"
    fi
    
    # Move and update androidMain files
    if [ -d "$ANDROID_OLD_PATH" ]; then
        # Copy entire directory structure with cp -r, then update package declarations
        cp -r "$ANDROID_OLD_PATH"/* "$ANDROID_NEW_PATH/"
        
        # Update package declarations and imports in all Kotlin files recursively
        find "$ANDROID_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/package com\.example\.myproject/package $PACKAGE_NAME/" {} \;
        find "$ANDROID_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/import com\.example\.myproject/import $PACKAGE_NAME/g" {} \;
        
        # Update generated resource imports (these use project name without hyphens/underscores, lowercase)
        find "$ANDROID_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/import myproject\.composeapp\.generated\.resources\./import ${RESOURCE_PROJECT_NAME}\.composeapp\.generated\.resources\./g" {} \;
        
        # Remove old directory structure
        rm -rf "mobile/composeApp/src/androidMain/kotlin/com"
    fi
    
    # Move and update iosMain files  
    if [ -d "$IOS_OLD_PATH" ]; then
        # Copy entire directory structure with cp -r, then update package declarations
        cp -r "$IOS_OLD_PATH"/* "$IOS_NEW_PATH/"
        
        # Update package declarations and imports in all Kotlin files recursively
        find "$IOS_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/package com\.example\.myproject/package $PACKAGE_NAME/" {} \;
        find "$IOS_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/import com\.example\.myproject/import $PACKAGE_NAME/g" {} \;
        
        # Update generated resource imports (these use project name without hyphens/underscores, lowercase)
        find "$IOS_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/import myproject\.composeapp\.generated\.resources\./import ${RESOURCE_PROJECT_NAME}\.composeapp\.generated\.resources\./g" {} \;
        
        # Remove old directory structure
        rm -rf "mobile/composeApp/src/iosMain/kotlin/com"
    fi
    
    # Move and update commonTest files
    COMMONTEST_OLD_PATH="mobile/composeApp/src/commonTest/kotlin/com/example/myproject"
    COMMONTEST_NEW_PATH="mobile/composeApp/src/commonTest/kotlin/${PACKAGE_NAME//.//}"
    
    if [ -d "$COMMONTEST_OLD_PATH" ]; then
        mkdir -p "$COMMONTEST_NEW_PATH"
        # Copy entire directory structure with cp -r, then update package declarations
        cp -r "$COMMONTEST_OLD_PATH"/* "$COMMONTEST_NEW_PATH/"
        
        # Update package declarations and imports in all Kotlin files recursively
        find "$COMMONTEST_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/package com\.example\.myproject/package $PACKAGE_NAME/" {} \;
        find "$COMMONTEST_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/import com\.example\.myproject/import $PACKAGE_NAME/g" {} \;
        
        # Update generated resource imports (these use project name without hyphens/underscores, lowercase)
        find "$COMMONTEST_NEW_PATH" -name "*.kt" -type f -exec sed -i '' "s/import myproject\.composeapp\.generated\.resources\./import ${RESOURCE_PROJECT_NAME}\.composeapp\.generated\.resources\./g" {} \;
        
        # Remove old directory structure
        rm -rf "mobile/composeApp/src/commonTest/kotlin/com"
    fi
    
    # Update settings.gradle.kts with project name
    if [ -f "mobile/settings.gradle.kts" ]; then
        sed -i '' "s/rootProject\.name = \".*\"/rootProject.name = \"$PROJECT_NAME\"/" mobile/settings.gradle.kts
    fi
    
    # Update iOS Configuration
    if [ -f "mobile/iosApp/Configuration/Config.xcconfig" ]; then
        sed -i '' "s/PRODUCT_NAME=.*/PRODUCT_NAME=$PROJECT_NAME/" mobile/iosApp/Configuration/Config.xcconfig
        sed -i '' "s/PRODUCT_BUNDLE_IDENTIFIER=.*/PRODUCT_BUNDLE_IDENTIFIER=$PACKAGE_NAME/" mobile/iosApp/Configuration/Config.xcconfig
    fi
    
    # Update iOS project file - replace product name references
    if [ -f "mobile/iosApp/iosApp.xcodeproj/project.pbxproj" ]; then
        sed -i '' "s/myproject\.app/$PROJECT_NAME.app/g" mobile/iosApp/iosApp.xcodeproj/project.pbxproj
        sed -i '' "s/productName = iosApp;/productName = $PROJECT_NAME;/" mobile/iosApp/iosApp.xcodeproj/project.pbxproj
    fi
    
    # Create/update Android strings.xml with project name
    mkdir -p "mobile/composeApp/src/androidMain/res/values"
    cat > "mobile/composeApp/src/androidMain/res/values/strings.xml" << EOF
<resources>
    <string name="app_name">$PROJECT_NAME</string>
</resources>
EOF

    # Update resource imports in App.kt to use correct package
    if [ -f "$NEW_PACKAGE_PATH/App.kt" ]; then
        # Update resource imports to match new package structure
        RESOURCE_PACKAGE=$(echo $PACKAGE_NAME | tr '.' '_')
        sed -i '' "s/import myproject\.composeapp\.generated\.resources\./import ${RESOURCE_PACKAGE}.composeapp.generated.resources./" "$NEW_PACKAGE_PATH/App.kt"
    fi
    
    # Create missing source files if they don't exist
    if [ ! -f "$NEW_PACKAGE_PATH/Greeting.kt" ] && [ -f "$OLD_PACKAGE_PATH/Greeting.kt" ]; then
        sed "s/package com\.example\.myproject/package $PACKAGE_NAME/" "$OLD_PACKAGE_PATH/Greeting.kt" > "$NEW_PACKAGE_PATH/Greeting.kt"
    fi
    
    if [ ! -f "$NEW_PACKAGE_PATH/Platform.kt" ] && [ -f "$OLD_PACKAGE_PATH/Platform.kt" ]; then
        sed "s/package com\.example\.myproject/package $PACKAGE_NAME/" "$OLD_PACKAGE_PATH/Platform.kt" > "$NEW_PACKAGE_PATH/Platform.kt"
    fi
    
    # Handle iOS-specific files
    if [ ! -f "$IOS_NEW_PATH/MainViewController.kt" ] && [ -f "$IOS_OLD_PATH/MainViewController.kt" ]; then
        sed "s/package com\.example\.myproject/package $PACKAGE_NAME/" "$IOS_OLD_PATH/MainViewController.kt" > "$IOS_NEW_PATH/MainViewController.kt"
    fi
    
    if [ ! -f "$IOS_NEW_PATH/Platform.ios.kt" ] && [ -f "$IOS_OLD_PATH/Platform.ios.kt" ]; then
        sed "s/package com\.example\.myproject/package $PACKAGE_NAME/" "$IOS_OLD_PATH/Platform.ios.kt" > "$IOS_NEW_PATH/Platform.ios.kt"
    fi
    
    # Create MainViewController.kt if it doesn't exist (for iOS)
    if [ ! -f "$IOS_NEW_PATH/MainViewController.kt" ]; then
        cat > "$IOS_NEW_PATH/MainViewController.kt" << EOF
package $PACKAGE_NAME

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }
EOF
    fi
    
    print_success "Mobile project files updated"
}

# Note: CI/CD workflow files (.github/workflows/backend-ci.yml and mobile-ci.yml) 
# are already included in the template repository

# Note: Detekt configuration files (backend/detekt.yml and mobile/detekt.yml) 
# are already included in the template repository

# Function to update documentation
update_documentation() {
    print_status "Updating project documentation..."
    
    # Update main README
    cat > "README.md" << EOF
# $PROJECT_NAME

A Kotlin project template with backend (Ktor) and mobile (Compose Multiplatform) components.

## 🚀 Quick Start

This project was generated using the Kotlin project template. It includes:

- **Backend**: Ktor server with Kotlin and PostgreSQL
- **Mobile**: Compose Multiplatform app (Android + iOS)
- **CI/CD**: GitHub Actions for automated testing and deployment
- **Code Quality**: Detekt for static analysis

## 📁 Project Structure

\`\`\`
$PROJECT_NAME/
├── backend/              # Ktor backend server
│   ├── src/main/kotlin/$PACKAGE_NAME/
│   ├── build.gradle.kts
│   └── detekt.yml
├── mobile/               # Compose Multiplatform app
│   ├── composeApp/       # Shared UI code
│   ├── iosApp/          # iOS application
│   ├── build.gradle.kts
│   └── detekt.yml
├── .github/workflows/    # CI/CD pipelines
├── setup.sh             # Project setup script
└── README.md
\`\`\`

## 🛠️ Setup

### Prerequisites

- JDK 17+
- Android Studio (for mobile development)
- Xcode (for iOS development, macOS only)
- IntelliJ IDEA (for backend development)

### Backend Setup

1. Navigate to the backend directory:
   \`\`\`bash
   cd backend
   \`\`\`

2. Run the application:
   \`\`\`bash
   ./gradlew run
   \`\`\`

3. Test the API:
   \`\`\`bash
   curl http://localhost:8080/health
   \`\`\`

### Mobile Setup

1. Navigate to the mobile directory:
   \`\`\`bash
   cd mobile
   \`\`\`

2. Build the project:
   \`\`\`bash
   ./gradlew build
   \`\`\`

3. Run on Android:
   \`\`\`bash
   ./gradlew composeApp:installDebug
   \`\`\`

4. For iOS: Open \`iosApp/iosApp.xcodeproj\` in Xcode and run

## 🧪 Testing

### Backend Tests
\`\`\`bash
cd backend
./gradlew test
./gradlew detekt
\`\`\`

### Mobile Tests
\`\`\`bash
cd mobile
./gradlew test
./gradlew detekt
\`\`\`

## 🚀 Deployment

### Backend Deployment (Railway)

1. Connect your GitHub repository to Railway
2. Set environment variables:
   \`\`\`env
   PORT=8080
   DATABASE_URL=postgresql://...
   JWT_SECRET=your-secret-key
   APP_ENVIRONMENT=production
   \`\`\`
3. Deploy automatically on push to main branch

### Mobile Deployment

- **Android**: Build APK/AAB and deploy to Google Play
- **iOS**: Build and deploy to App Store

## 📊 Code Quality

This project uses Detekt for code quality and style checking:

\`\`\`bash
# Run detekt on backend
cd backend && ./gradlew detekt

# Run detekt on mobile
cd mobile && ./gradlew detekt
\`\`\`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: \`git checkout -b feature-name\`
3. Make your changes
4. Run tests and detekt: \`./gradlew test detekt\`
5. Commit your changes: \`git commit -am 'Add feature'\`
6. Push to the branch: \`git push origin feature-name\`
7. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙋‍♂️ Support

For questions and support:
- Create an issue in the GitHub repository
- Check the documentation in each component's directory
- Review the CI/CD pipeline logs for deployment issues

---

**Author**: $AUTHOR_NAME  
**Package**: $PACKAGE_NAME  
**Generated**: $(date +"%Y-%m-%d")
EOF

    print_success "Documentation updated"
}

# Function to create Gradle wrapper
create_gradle_wrapper() {
    print_status "Creating Gradle wrapper files..."
    
    # Backend gradle wrapper
    mkdir -p "backend/gradle/wrapper"
    cat > "backend/gradle/wrapper/gradle-wrapper.properties" << EOF
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.11.1-bin.zip
networkTimeout=10000
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

    # Create gradlew scripts
    cat > "backend/gradlew" << 'EOF'
#!/bin/sh

# Gradle start up script for UN*X

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi
        # Roll the args list around exactly as many times as the number of
        # args, so each arg winds up back in the position where it started, but
        # possibly modified.
        #
        # NB: a `for` loop captures its iteration list before it begins, so
        # changing the positional parameters here affects neither the number of
        # iterations, nor the values presented in `arg`.
        shift                   # remove old arg
        set -- "$@" "$arg"      # push replacement arg
    done
fi

# Collect all arguments for the java command;
#   * $DEFAULT_JVM_OPTS, $JAVA_OPTS, and $GRADLE_OPTS can contain fragments of
#     shell script including quotes and variable substitutions, so put them in
#     double quotes to make sure that they get re-expanded; and
#   * put everything else in single quotes to prevent shell expansion.
set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

# Stop when "xargs" is not available.
if ! command -v xargs >/dev/null 2>&1
then
    die "xargs is not available"
fi

# Use "xargs" to parse quoted args.
#
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
#
# In Bash we could simply go:
#
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#   set -- "${ARGS[@]}" "$var"
#
# but POSIX shell has neither arrays nor command substitution, so instead we
# post-process each arg (as a line of input to sed) to backslash-escape any
# character that might be a shell metacharacter, then use eval to reverse
# that process (while maintaining the separation between arguments).
#
# If the command-line args contain a '--', it will be preserved and passed to
# the underlying gradle command unchanged.

eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" \
        | xargs -n1 \
        | sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' \
        | tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"
EOF

    chmod +x "backend/gradlew"
    
    # Mobile gradle wrapper (copy structure)
    cp -r "backend/gradle" "mobile/"
    cp "backend/gradlew" "mobile/gradlew"
    chmod +x "mobile/gradlew"
    
    print_success "Gradle wrapper created"
}

# Function to create IntelliJ run configuration
create_run_configuration() {
    print_status "Setting up IntelliJ run configuration..."
    
    # Create .idea directory structure
    mkdir -p ".idea/runConfigurations"
    
    # Copy and update the template run configuration
    if [ -f "backend/Backend_Application.xml" ]; then
        # Update the template with actual package name
        sed "s/com\.example\.myproject/$PACKAGE_NAME/g" "backend/Backend_Application.xml" > ".idea/runConfigurations/Backend_Application.xml"
        print_success "IntelliJ run configuration created"
    else
        print_warning "Backend run configuration template not found, skipping..."
    fi
}

# Function to verify git repository (no modifications)
verify_git_repo() {
    print_status "Verifying Git repository..."
    
    if [ ! -d ".git" ]; then
        print_error "This script should be run in the root of a git repository"
        exit 1
    fi
    
    print_success "Git repository verified"
}

# Main execution function
main() {
    print_status "Starting Kotlin project setup..."
    
    # Verify we're in a git repository
    verify_git_repo
    
    # Get user input
    get_user_input
    
    # Create backend structure and files
    create_backend_structure
    create_backend_files
    create_gradle_wrapper
    
    # Update existing mobile project files
    update_mobile_files
    
    # Note: CI/CD files are already included in template
    
    # Update documentation
    update_documentation
    
    # Create IntelliJ run configuration
    create_run_configuration
    
    # Final summary
    echo ""
    echo "=== 🎉 Setup Complete! ==="
    echo ""
    echo "Your Kotlin project has been set up with:"
    echo "✅ Backend (Ktor) with package: $PACKAGE_NAME"
    echo "✅ Mobile (Compose Multiplatform) with package: $PACKAGE_NAME"
    echo "✅ Firebase Crashlytics integration"
    echo "✅ CI/CD pipelines for GitHub Actions"
    echo "✅ Detekt code quality tools"
    echo "✅ Gradle build configuration"
    echo "✅ Documentation and setup guides"
    echo ""
    echo "Next steps:"
    echo "1. 🔥 IMPORTANT - Set up Firebase Crashlytics:"
    echo "   ⚠️  The mobile app includes a DEFAULT google-services.json template"
    echo "   ⚠️  Crashlytics will NOT work until you replace it with your Firebase config"
    echo ""
    echo "   Quick setup:"
    echo "   - Create Firebase project at https://console.firebase.google.com"
    echo "   - Add Android app with package: $PACKAGE_NAME"
    echo "   - Download google-services.json to mobile/composeApp/"
    echo "   - See mobile/FIREBASE_SETUP.md for detailed instructions"
    echo ""
    echo "2. Set up GitHub repository (IMPORTANT):"
    echo "   📋 See GITHUB_SETUP.md for complete repository configuration"
    echo "   🔑 Add Firebase secrets for production Crashlytics"
    echo "   🔄 Configure CI/CD for automated testing and releases"
    echo ""
    echo "3. cd backend && ./gradlew run     # Start the backend server"
    echo "4. cd mobile && ./gradlew build    # Build the mobile app (compiles with default config)"
    echo "5. git add . && git commit -m 'Initial project setup'"
    echo "6. git push origin main             # Push to GitHub (triggers CI/CD)"
    echo ""
    print_success "🎉 Setup complete! See GITHUB_SETUP.md for next steps."
}

# Run the main function
main "$@"