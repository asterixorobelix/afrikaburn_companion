# Technology Stack

**Analysis Date:** 2026-01-15

## Languages

**Primary:**
- Kotlin 2.2.20 - All application code (mobile + backend) - `mobile/gradle/libs.versions.toml`, `backend/build.gradle.kts`

**Secondary:**
- Java 11 (JVM target) - Build tooling, Android compatibility - `mobile/composeApp/build.gradle.kts`
- Swift - Minimal iOS wrapper - `mobile/iosApp/`

## Runtime

**Environment:**
- JVM 11 (Backend) - Ktor server runtime - `backend/build.gradle.kts`
- Android 24+ (min SDK) - Mobile Android runtime - `mobile/gradle/libs.versions.toml`
- iOS 14+ - Compose Multiplatform iOS support
- Netty - Backend HTTP server engine - `backend/build.gradle.kts`

**Package Manager:**
- Gradle 8.11.1 (Gradle Wrapper) - `gradlew`
- Gradle Version Catalog - `mobile/gradle/libs.versions.toml`
- Lockfile: `gradle.lockfile` not present (version catalog preferred)

## Frameworks

**Core:**
- Compose Multiplatform 1.9.0 - UI framework for iOS/Android - `mobile/gradle/libs.versions.toml`
- Ktor Server 3.1.3 - Backend REST API - `backend/build.gradle.kts`
- Material Design 3 - UI design system - `mobile/composeApp/build.gradle.kts`

**Testing:**
- Kotlin Test + JUnit 4.13.2 - Mobile unit tests - `mobile/gradle/libs.versions.toml`
- Kotest 6.0.3 + JUnit 5 - Backend testing - `backend/build.gradle.kts`
- MockK 1.14.6 - Kotlin mocking library - `mobile/gradle/libs.versions.toml`
- Kotlinx Coroutines Test - Async testing - `mobile/gradle/libs.versions.toml`

**Build/Dev:**
- Kotlin Multiplatform Plugin 2.2.20 - Cross-platform compilation - `mobile/gradle/libs.versions.toml`
- Detekt 1.23.8 - Static code analysis - `mobile/detekt.yml`, `backend/detekt.yml`
- Jacoco - Test coverage reporting - `mobile/build.gradle.kts`, `backend/build.gradle.kts`

## Key Dependencies

**Critical (Mobile):**
- Koin 4.1.1 - Dependency injection (multiplatform) - `mobile/gradle/libs.versions.toml`
- Kotlinx Serialization 1.9.0 - JSON serialization - `mobile/gradle/libs.versions.toml`
- Kotlinx DateTime 0.6.2 - Date/time handling - `mobile/gradle/libs.versions.toml`
- Coil 3.1.0 - Async image loading - `mobile/gradle/libs.versions.toml`
- Androidx Lifecycle 2.9.4 - ViewModel lifecycle - `mobile/gradle/libs.versions.toml`
- Androidx Navigation 2.9.0 - Navigation framework - `mobile/gradle/libs.versions.toml`

**Critical (Backend):**
- Exposed ORM 0.61.0 - Database access - `backend/build.gradle.kts`
- HikariCP 7.0.2 - Connection pooling - `backend/build.gradle.kts`
- Auth0 JWT 4.5.0 - JWT authentication - `backend/build.gradle.kts`
- Logback 1.5.18 - Logging framework - `backend/build.gradle.kts`

**Infrastructure:**
- PostgreSQL Driver 42.7.8 - Production database - `backend/build.gradle.kts`
- H2 Database 2.4.240 - Development database - `backend/build.gradle.kts`
- Firebase BOM 33.15.0 - Crashlytics/Analytics - `mobile/gradle/libs.versions.toml`

## Configuration

**Environment:**
- Backend: Environment variables for `PORT`, `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `DB_POOL_SIZE`, `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE` - `backend/src/main/resources/application.conf`
- Mobile: Embedded JSON resources for offline data - `mobile/composeApp/composeResources/files/`

**Build:**
- `mobile/gradle/libs.versions.toml` - Dependency version catalog
- `mobile/composeApp/build.gradle.kts` - Mobile build configuration
- `backend/build.gradle.kts` - Backend build configuration
- `backend/src/main/resources/application.conf` - HOCON server configuration
- `mobile/detekt.yml`, `backend/detekt.yml` - Code analysis rules

## Platform Requirements

**Development:**
- macOS/Linux/Windows with JDK 11+
- Android SDK 24+ for mobile development
- Xcode for iOS builds (macOS only)
- No Docker required for development (H2 embedded database)

**Production:**
- Backend: Docker container or JVM with Netty - Fat JAR via `backend/build.gradle.kts`
- Mobile: Play Store (Android), App Store (iOS)
- Database: PostgreSQL with PostGIS extension
- Hosting: Railway (backend), App stores (mobile)

---

*Stack analysis: 2026-01-15*
*Update after major dependency changes*
