# AfrikaBurn Companion - Codebase Analysis

**Date**: 2026-02-19

## Overall Summary

The mobile app has a solid UI foundation (projects, map, directions, about) with offline JSON data and Koin DI. The backend is a scaffold with only `/` and `/health` routes. Several project conventions are not yet enforced (use cases, sealed UI state, Koin `viewModelOf`). Testing exists only in mobile `commonTest` (20 test files).

---

## Key Findings

### Architecture

1. ViewModels depend directly on repositories instead of use cases. `ProjectsViewModel` and `ProjectTabViewModel` call repositories directly. `MapViewModel` loads JSON resources directly. Suggested fix: introduce use cases and push resource loading into data/domain layers. `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/projects/ProjectsViewModel.kt:15`, `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/projects/ProjectTabViewModel.kt:15`, `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/map/MapViewModel.kt:83`
2. UI state does not follow the required sealed interface convention across screens. `ProjectsUiState` and `ProjectsScreenUiState` are data classes. Suggested fix: sealed interface with Loading/Content/Error/Empty and `isRefreshing` in Content. `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/projects/ProjectsUiState.kt:6`, `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/projects/ProjectsViewModel.kt:10`
3. Koin bindings do not use `viewModelOf`, and repositories are registered in the domain module. Suggested fix: move repository bindings to data module, use `singleOf`/`factoryOf`/`viewModelOf` consistently. `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/PresentationModule.kt:9`, `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/DomainModule.kt:15`

### Security

1. Backend JWT has a hardcoded fallback secret. Suggested fix: fail fast when `JWT_SECRET` is missing. `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Security.kt:12`
2. Backend auth is configured but not applied to any routes. Suggested fix: wrap protected routes with `authenticate("auth-jwt")`. `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/Routing.kt:12`
3. CORS allows only dev hosts and has no environment-based production config. Suggested fix: drive CORS from config. `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/plugins/HTTP.kt:17`

### Stability & UX

1. Projects tab ViewModels are manually cached in a mutable map, bypassing lifecycle management. Suggested fix: let Koin manage per-tab ViewModels or centralize state. `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/projects/ProjectsViewModel.kt:22`
2. MapViewModel loads JSON synchronously in ViewModel scope and reports errors as empty success. Suggested fix: route through repository/use case and surface error state. `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/map/MapViewModel.kt:83`

### Testing

- Mobile: 20 test files in `commonTest` (projects, filters, data source, unlock services)
- Backend: no tests
- MapViewModel has no tests (location flow, camp pin state, permissions)

---

## Priority Action Plan

1. Add use cases and route ViewModels through them (projects + map data).
2. Convert `ProjectsUiState` and `ProjectsScreenUiState` to sealed interfaces with Loading/Content/Error/Empty.
3. Move repository bindings to data module and update Koin bindings to `singleOf`/`factoryOf`/`viewModelOf`.
4. Remove default JWT secret and enforce required env vars for backend.
5. Add backend route auth (or remove unused auth config until endpoints exist).
6. Add MapViewModel test coverage.
