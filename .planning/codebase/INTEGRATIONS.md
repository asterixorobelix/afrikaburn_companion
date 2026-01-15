# External Integrations

**Analysis Date:** 2026-01-15

## APIs & External Services

**Payment Processing:**
- Not detected

**Email/SMS:**
- Not detected

**External APIs:**
- Supabase PostgreSQL Backend (planned)
  - Server URL: `https://afrikaburn-companion.supabase.co/api/v1` - `specs/001-a-comprehensive-mobile/contracts/api-spec.yaml`
  - Development URL: `http://localhost:8080/api/v1`
  - Type: REST API with OpenAPI 3.0.3 specification

**Location Services:**
- GPS/Location-based features planned
  - Latitude/longitude parameters for API endpoints - `specs/001-a-comprehensive-mobile/contracts/api-spec.yaml`
  - Purpose: Location-based content unlocking, resource location queries

## Data Storage

**Databases:**
- PostgreSQL (Production) - Backend primary data store
  - Connection: `DATABASE_URL` environment variable - `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Databases.kt`
  - Client: Exposed ORM 0.61.0 - `backend/build.gradle.kts`
  - PostGIS extension for geospatial queries - Referenced in `CLAUDE.md`
- H2 (Development) - Backend development database
  - Connection: Auto-detected via JDBC URL pattern - `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Databases.kt`
  - Used when DATABASE_URL contains "h2"

**File Storage:**
- Not detected (no cloud storage integration)

**Caching:**
- In-memory caching in mobile app
  - Location: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt`
  - Pattern: Simple Map-based cache without TTL

**Local Data:**
- Embedded JSON resources for offline-first mobile
  - Location: `mobile/composeApp/composeResources/files/`
  - Files: `WTFThemeCamps.json`, `WTFArtworks.json`, `WTFEvents.json`, `WTFMutantVehicles.json`, `WTFPerformances.json`, `WTFRovingArtworks.json`
  - Loaded via: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/datasource/JsonResourceDataSourceImpl.kt`

## Authentication & Identity

**Auth Provider:**
- JWT Authentication (Backend)
  - Implementation: Auth0 JWT library with HMAC256 - `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Security.kt`
  - Configuration: `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE` environment variables
  - Token validation: Ktor JWT plugin

**OAuth Integrations:**
- Not detected

## Monitoring & Observability

**Error Tracking:**
- Firebase Crashlytics (Mobile)
  - Android: `mobile/composeApp/src/androidMain/kotlin/io/asterixorobelix/afrikaburn/platform/CrashLogger.android.kt`
  - iOS: `mobile/composeApp/src/iosMain/kotlin/io/asterixorobelix/afrikaburn/platform/CrashLogger.ios.kt`
  - Version: Firebase BOM 33.15.0 - `mobile/gradle/libs.versions.toml`
  - Fallback: Native platform logging when Firebase unavailable

**Analytics:**
- Firebase Analytics (optional, conditional loading)
  - Location: `mobile/composeApp/build.gradle.kts`
  - Status: Conditional based on google-services.json presence

**Logs:**
- Backend: Logback 1.5.18 with Ktor call logging
  - Configuration: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Monitoring.kt`
- Mobile: Platform-native logging (Android Log, iOS NSLog)

## CI/CD & Deployment

**Hosting:**
- Backend: Railway (planned) - Fat JAR deployment
  - Configuration: `backend/build.gradle.kts` (shadowJar task)
  - Port: 9080 default, configurable via `PORT` env var
- Mobile: App stores (Play Store, App Store)
  - Fastlane configuration: `mobile/fastlane/`

**CI Pipeline:**
- GitHub Actions - `.github/workflows/`
  - `mobile-ci.yml` - Mobile testing, detekt, build verification
  - `mobile-cd.yml` - Mobile release pipeline
  - `backend-ci.yml` - Backend testing, quality gates
  - `pr-validation.yaml` - Pull request checks
  - `auto-review.yml` - Automated code review

**Code Quality:**
- Detekt static analysis - `mobile/detekt.yml`, `backend/detekt.yml`
- Jacoco coverage (80% minimum) - Referenced in `CLAUDE.md`
- Dependabot for dependency updates - `.github/dependabot.yml`

## Environment Configuration

**Development:**
- Required env vars (Backend): `DATABASE_URL` (optional - defaults to H2)
- Secrets location: Environment variables or IDE run configuration
- Mock/stub services: H2 embedded database, local JSON resources

**Staging:**
- Not explicitly configured
- Uses separate environment variable set

**Production:**
- Secrets management: Railway environment variables (backend), App Store configs (mobile)
- Required vars: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `JWT_SECRET`
- Database: PostgreSQL with daily backups (Supabase)

## Webhooks & Callbacks

**Incoming:**
- Not detected

**Outgoing:**
- Not detected

---

*Integration audit: 2026-01-15*
*Update when adding/removing external services*
