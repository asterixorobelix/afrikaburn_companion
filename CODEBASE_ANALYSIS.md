# AfrikaBurn Companion - Codebase Analysis

**Date**: 2026-02-12
**Overall Assessment**: 8/10 — Strong Clean Architecture foundation, good MD3 compliance, 198 test cases. Null safety, error logging, accessibility, and navigation all improved. Key areas remaining: security hardening, favorites, and offline maps.

---

## Table of Contents

1. [Security](#1-security)
2. [Stability & Code Quality](#2-stability--code-quality)
3. [User Experience Enhancements](#3-user-experience-enhancements)
4. [Architectural Improvements](#4-architectural-improvements)
5. [Priority Action Plan](#5-priority-action-plan)

---

## 1. Security

### Critical

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| S1 | **Weak default JWT secret** — Hardcoded fallback `"default-secret-change-in-production"` lets attackers forge tokens if env var is missing | `backend/.../plugins/Security.kt:12` | Attackers can bypass authentication entirely |
| S2 | **Signing secrets potentially logged** — `-verbose` flag on jarsigner could expose passwords in CI logs | `.github/workflows/mobile-cd.yml:182` | Credential leakage in public CI logs |

### High

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| S3 | No rate limiting configured | Backend has the dependency but never installs it | DDoS/brute-force risk |
| S4 | No authentication on routes | `backend/.../plugins/Routing.kt` | JWT configured but never applied to any route |
| S5 | No database encryption at rest | SQLDelight stores camp pins (location data) unencrypted | Extractable on rooted/jailbroken devices |
| S6 | `android:allowBackup="true"` without exclusion rules | `AndroidManifest.xml:9` | DB extractable via `adb backup` |
| S7 | R8/ProGuard disabled for release | `build.gradle.kts` release build: `isMinifyEnabled = false` | Easy reverse engineering of APK |
| S8 | No Android network security config | Missing `networkSecurityConfig` in manifest | No cleartext traffic enforcement |

### Medium

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| S9 | Overly permissive CORS — dev-only `localhost` origins, no production config | `backend/.../plugins/HTTP.kt:19-22` | Accepts localhost requests in production |
| S10 | No input validation on endpoints | `backend/.../plugins/Routing.kt` | Potential injection/malformed data |
| S11 | Firebase template with dummy key in CodeQL workflow | `.github/workflows/codeql.yml:166` | Confusing, low actual risk |
| S12 | `android:allowBackup="true"` without backup rules | `AndroidManifest.xml:9` | User data included in unencrypted backups |
| S13 | Backend CI workflow permissions too broad | `.github/workflows/backend-ci.yml:17-21` | Compromised workflow could modify PRs/issues |
| S14 | No JSON schema validation for bundled data | `JsonResourceDataSourceImpl.kt` | Malformed JSON could cause crashes |

### Low

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| S15 | Generic error messages could leak stack traces | `backend/.../plugins/StatusPages.kt:21-24` | Internal details exposure |
| S16 | Database credentials in `DATABASE_URL` env var | `backend/.../plugins/Databases.kt:15-28` | Credentials in process listings |
| S17 | No network security config (Android) | `AndroidManifest.xml` | No HTTPS enforcement |
| S18 | Dependency review only on PRs, not scheduled | `.github/workflows/dependency-review.yml:4` | Misses vulnerabilities between PRs |
| S19 | User ID logged to Crashlytics without anonymization | `CrashLogger.android.kt:89-104` | Potential GDPR violation if PII |

### Positive Security Practices Already In Place

- Firebase configuration properly gitignored
- Security headers configured (X-Frame-Options, HSTS, X-Content-Type-Options)
- CodeQL + Dependabot + dependency-review enabled
- No WebViews (eliminates XSS)
- SQLDelight parameterized queries prevent SQL injection
- Signing keystore cleaned up after CI use
- Secrets stored as GitHub Actions secrets (not in code)
- Offline-first architecture reduces network attack surface

---

## 2. Stability & Code Quality

### Critical Bugs

#### Bug 4: Missing Permission Check Race Condition

**Location**: `LocationService.android.kt:64-68`

`@Suppress("MissingPermission")` annotation hides a potential race where permissions can be revoked between check and use on Android 12+.

### High Priority Issues

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| Q5 | **Tab ViewModels in MutableMap** — bounds checks and cleanup added, but still not Koin lifecycle-managed | `ProjectsViewModel.kt:23-29` | Minor leak risk on repeated tab creation |
| Q6 | **UnlockConditionManager** — `@Volatile` added for thread-safe visibility, but no mutex for full atomicity | `UnlockConditionManager.kt:91-109` | Low risk of double-persist on concurrent calls |
| Q7 | No `CancellationException` handling in ViewModel coroutines | All ViewModels using `viewModelScope.launch` | Unnecessary state updates after clear |

### Medium Priority Issues

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| Q8 | Database operations not wrapped in transactions | `UserCampPin.sq` | Architectural gap for future features |
| Q10 | No retry logic for database initialization failures | Database module | Catastrophic on corrupted DB |
| Q11 | `ProjectsUiState` is data class, not sealed interface | `ProjectsUiState.kt` | Inconsistent with `MapUiState` pattern |
| Q12 | `selectedProject` passed via mutable state in App.kt | `App.kt:149` | Not type-safe, should use Navigation args |

### Performance

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| Q13 | Chained filter operations create multiple intermediate collections | `ProjectTabViewModel.kt:118-146` | Low — fine at current data scale |
| Q14 | No Use Case layer — ViewModels call repositories directly | All ViewModels | Violates project conventions |

### Testing Gaps

- **MapViewModel** — No test file found (location tracking, camp pin interactions, permission flow)
- **UnlockConditionManager** — Missing concurrent access and event year transition tests
- **Platform implementations** — LocationService.android.kt, LocationService.ios.kt untested

### Positive Quality Findings

- Excellent separation of concerns (Clean Architecture properly implemented)
- Proper use of `Result` type for error handling in repositories
- Sealed interfaces for UI state (type-safe state management)
- `StateFlow` used correctly (no exposed `MutableStateFlow`)
- 198 test cases with proper `StandardTestDispatcher` setup
- In-memory caching in `ProjectsRepositoryImpl`
- Proper `viewModelScope` usage with auto-cancellation

---

## 3. User Experience Enhancements

### High-Value Missing Features

| # | Feature | Why It Matters |
|---|---------|---------------|
| U1 | **Favorites/Bookmarks** | No way to save interesting projects — users browsing 100+ items need this |
| U3 | **Sort options** | Can't sort by name, distance, or category |
| U4 | **Search across all tabs** | Search is per-tab only |
| U5 | **Schedule/timeline view** | Events have time data but no chronological view |
| U6 | **Deep linking** | Can't share specific projects between attendees |
| U7 | **Offline indicator** | No visual cue when user is offline |
| U8 | **Pull-to-refresh** | No way to manually refresh data |
| U9 | **Share functionality** | No way to share project info with other attendees |
| U10 | **Distance indicators** | Show proximity to user on project cards |

### Polish Opportunities

| # | Opportunity | Current State |
|---|------------|---------------|
| U19 | Map loading skeleton | Generic spinner instead of placeholder map |
| U20 | Inline loading for camp pin save/delete | No action feedback |
| U21 | Haptic feedback on key actions | No vibration on save/delete |
| U22 | Undo for destructive actions | Camp pin delete is permanent, no undo |
| U23 | Shared element transitions | No spatial continuity between list and detail |
| U24 | Dynamic colors (Material You) | Not enabled on Android 12+ |

### Current UX Strengths

- Excellent skeleton loaders with staggered animations
- Polished micro-interactions (press scale, bounce click, fade transitions)
- Consistent sealed state pattern across all ViewModels
- Beautiful About screen with parallax carousel
- Camp pin dialogs with proper confirmation flow
- Comprehensive accessibility: contentDescription, semantic headings, liveRegion announcements
- "Show on Map" navigation from project detail
- Tab scroll position and search query preserved across tab switches
- Item counts displayed on tab labels
- External link confirmation dialogs

---

## 4. Architectural Improvements

### Backend Severely Underutilized

Only `/` and `/health` endpoints exist. The entire backend infrastructure (JWT, Exposed ORM, HikariCP, PostgreSQL) is set up but unused.

**Opportunities**:
- `/api/v1/projects` — serve fresh project data instead of bundled JSON
- `/api/v1/analytics` — anonymous usage telemetry
- `/api/v1/sync` — sync favorites/bookmarks across devices

### Offline Maps Not Integrated

`composeResources/files/maps/` contains `style.json`, `.mbtiles`, and `.geojson` files but MapLibre is configured for server-based tiles. The map requires internet, violating the offline-first design principle.

### CORS Configuration Hardcoded for Development

`backend/.../HTTP.kt` allows `localhost:3000/3001` only — no environment-based production CORS.

### Missing Use Cases Layer

ViewModels call repositories directly. Per project conventions, business logic should go through Use Case classes with `factoryOf` registration in Koin.

### Navigation Uses State Passing

`selectedProject` in `App.kt` uses mutable state rather than type-safe Compose Navigation serializable arguments.

---

## 5. Priority Action Plan

### Immediate — Security & Crash Fixes

| # | Action | Effort |
|---|--------|--------|
| 1 | Remove default JWT secret — fail on startup if unset | 10 min |
| 3 | Add `::add-mask::` and remove `-verbose` from CI signing | 10 min |

### Short-Term — Stability

| # | Action | Effort |
|---|--------|--------|
| 5 | Enable R8 minification for release builds | 30 min |
| 6 | Add `android:allowBackup="false"` or backup exclusion rules | 10 min |
| 7 | Add Android network security config enforcing HTTPS | 15 min |
| 8 | Integrate offline map tiles with MapLibre PMTiles provider | 2-4 hrs |
| 9 | Refactor tab ViewModels to be fully Koin lifecycle-managed | 1 hr |
| 10 | Add mutex to `UnlockConditionManager.isUnlocked()` for full atomicity | 30 min |

### Medium-Term — UX

| # | Action | Effort |
|---|--------|--------|
| 11 | Add favorites/bookmarks feature (SQLDelight table + UI) | 4-6 hrs |
| 13 | Add sort options and cross-tab search | 2-3 hrs |
| 16 | Add haptic feedback and undo for destructive actions | 1-2 hrs |

### Long-Term — Architecture

| # | Action | Effort |
|---|--------|--------|
| 17 | Expand backend with project sync API | 1-2 days |
| 18 | Add Use Case layer between ViewModels and Repositories | 4-6 hrs |
| 19 | Implement deep linking for project sharing | 3-4 hrs |
| 20 | Add schedule/timeline view for events | 4-6 hrs |
| 21 | Type-safe Navigation with serializable arguments | 2-3 hrs |
| 22 | Add MapViewModel test suite | 2-3 hrs |

---

## Appendix: Grades by Category

| Category | Grade | Notes |
|----------|-------|-------|
| Architecture | 9/10 | Excellent Clean Architecture, proper separation |
| State Management | 9/10 | Consistent sealed states, proper StateFlow |
| Testing | 8/10 | 198 tests, gaps in MapViewModel and platform code |
| Performance | 8/10 | Well optimized, minor filter chaining issue |
| Security | 6/10 | Good practices exist but critical gaps in backend |
| Error Handling | 7/10 | CrashLogger integrated, exceptions now logged |
| Null Safety | 8/10 | Force unwraps replaced with null-safe patterns |
| UX Completeness | 7/10 | Show on Map, tab counts, accessibility done; missing favorites/search/sharing |
| Platform Specifics | 8/10 | iOS delegate leak fixed |
