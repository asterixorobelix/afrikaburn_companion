# Codebase Concerns

**Analysis Date:** 2026-01-15

## Tech Debt

**Firebase Configuration Always Disabled:**
- Issue: `IS_USING_DEFAULT_TEMPLATE = true` is hardcoded, permanently disabling Firebase
- File: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/platform/FirebaseConfigChecker.kt`
- Why: Placeholder implementation during development
- Impact: Crash reporting (Crashlytics) never works in production
- Fix approach: Make this a build configuration parameter or detect actual Firebase config

**In-Memory Cache Without TTL:**
- Issue: Repository cache uses simple Map without time-to-live or size limits
- File: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ProjectsRepositoryImpl.kt`
- Why: Simple implementation for offline-first app
- Impact: Memory could grow unbounded in long-running sessions
- Fix approach: Add TTL expiration or size limits to cache entries

**Large UI Components:**
- Issue: Several Composable files exceed 250 lines
- Files: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/projects/EmptyStateContent.kt` (345 lines), `ProjectsScreen.kt` (317 lines), `ProjectDetailScreen.kt` (305 lines), `ProjectFilterChips.kt` (253 lines)
- Why: Organic feature growth
- Impact: Harder to test, maintain, and reuse components
- Fix approach: Extract sub-components (EmptyStateCard, FilterChipRow, etc.)

## Known Bugs

**No known bugs documented**

The codebase appears well-maintained with no TODO/FIXME comments or documented issues.

## Security Considerations

**Hardcoded Default JWT Secret:**
- Risk: Production deployments could use default secret if JWT_SECRET env var missing
- File: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Security.kt`
- Current mitigation: Default value warns it needs changing ("default-secret-change-in-production")
- Recommendations: Fail fast (throw exception) if JWT_SECRET missing in production environment

**Empty Database Password Default:**
- Risk: H2 development database exposed without authentication
- File: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Databases.kt`
- Current mitigation: Only affects development (H2); PostgreSQL requires proper credentials
- Recommendations: Validate DATABASE_PASSWORD is set for PostgreSQL connections

**CORS Configuration Hardcoded:**
- Risk: Development CORS origins included in production if not updated
- File: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/HTTP.kt`
- Current mitigation: Comment says "Add production domains here"
- Recommendations: Use environment-based CORS configuration

## Performance Bottlenecks

**No significant performance concerns detected**

The codebase follows good patterns:
- In-memory caching in repositories
- Async image loading with Coil
- StateFlow for efficient UI updates
- Connection pooling (HikariCP) in backend

## Fragile Areas

**Exception Handling in UI Layer:**
- Why fragile: Limited try-catch blocks (only 5 found in commonMain)
- File: Various UI composables, especially `ProjectDetailScreen.kt`, `ProjectsScreen.kt`
- Common failures: Unhandled exceptions in resource loading could crash app
- Safe modification: Wrap resource loading and image loading in try-catch
- Test coverage: Good ViewModel coverage, but edge cases in UI untested

**Platform-Specific Crash Logger:**
- Why fragile: Uses reflection to load Firebase Crashlytics (can fail silently)
- File: `mobile/composeApp/src/androidMain/kotlin/io/asterixorobelix/afrikaburn/platform/CrashLogger.android.kt`
- Common failures: ClassNotFoundException if Firebase not properly configured
- Safe modification: Add fallback logging for all failure paths (already done)
- Test coverage: Not tested (platform-specific)

## Scaling Limits

**Not applicable** - Early-stage project, no scaling concerns yet.

## Dependencies at Risk

**No high-risk dependencies detected**

All major dependencies are actively maintained:
- Compose Multiplatform (JetBrains - active development)
- Ktor (JetBrains - active development)
- Koin (InsertKoin - active development)
- Firebase (Google - active development)

## Missing Critical Features

**Backend API Endpoints:**
- Problem: Only `/` and `/health` routes implemented, no project API
- File: `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Routing.kt`
- Current workaround: Mobile uses embedded JSON resources (offline-first)
- Blocks: Server-side data management, real-time updates
- Implementation complexity: Medium (infrastructure in place, routes need adding)

**Backend .env.example Missing:**
- Problem: No template documenting required environment variables
- Blocks: Developer onboarding, production deployment clarity
- Implementation complexity: Low (create documentation file)

## Test Coverage Gaps

**Backend Test Files:**
- What's not tested: Backend Kotlin code has no test files
- Risk: Database operations, API routes, security logic untested
- Priority: Medium (backend is minimal currently)
- Difficulty to test: Low (Ktor testing utilities available)

**Integration Tests for JSON Parsing:**
- What's not tested: Edge cases like malformed JSON, missing resource files
- File: `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/datasource/JsonResourceDataSourceImpl.kt`
- Risk: App crash if JSON resources are corrupted
- Priority: Low (JSON is embedded and controlled)
- Difficulty to test: Low (mock bad data scenarios)

---

## Positive Findings

✓ **Clean codebase** - No TODO/FIXME/HACK comments
✓ **Good test coverage** - 16+ test files covering models, ViewModels, repositories
✓ **Proper error handling** - Result-based pattern in data layer
✓ **Strong DI setup** - Koin properly configured across all layers
✓ **Security headers configured** - HSTS, X-Frame-Options, etc. in backend
✓ **Database connection pooling** - HikariCP with proper timeouts
✓ **Code quality enforcement** - Detekt with comprehensive rules

---

*Concerns audit: 2026-01-15*
*Update as issues are fixed or new ones discovered*
