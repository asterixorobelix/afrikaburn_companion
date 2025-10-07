# Tasks: AfrikaBurn Companion Mobile App

**Input**: Design documents from `/specs/001-a-comprehensive-mobile/`
**Prerequisites**: plan.md, research.md, data-model.md, contracts/api-spec.yaml

## Execution Flow (main)
```
1. Load plan.md from feature directory
   → Extracted: Kotlin 2.1.21+ with Compose Multiplatform, SQLDelight, Koin DI, Material Design 3
2. Load design documents:
   → data-model.md: 20+ entities including Participant, Event, ThemeCamp, ArtInstallation
   → contracts/api-spec.yaml: 15+ endpoints for sync, events, maps, safety
   → research.md: Technology decisions for offline-first architecture
3. Generate tasks by category with existing codebase modifications
4. Apply TDD rules and parallel execution markers
5. Number tasks sequentially with dependencies
6. SUCCESS: Tasks ready for execution
```

## Format: `[ID] [P?] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- File paths relative to repository root

## Path Conventions (Existing Codebase)
- **Mobile**: `/mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/`
- **Backend**: `/backend/src/main/kotlin/io/asterixorobelix/afrikaburn/`
- **Tests**: `/mobile/composeApp/src/commonTest/kotlin/io/asterixorobelix/afrikaburn/`

## Phase 3.1: Setup and Dependencies

- [X] T001 Update mobile/composeApp/build.gradle.kts with new dependencies (SQLDelight, Ktor Client, Location services)
- [X] T002 Update backend/build.gradle.kts with Supabase PostgreSQL driver and additional Ktor modules  
- [X] T003 [P] Configure SQLDelight database schema in mobile/composeApp/src/commonMain/sqldelight/
- [X] T004 [P] Update mobile/detekt.yml with constitutional code quality rules
- [X] T005 Create mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/database/ directory structure

## Phase 3.2: Database and Data Models (TDD) ⚠️ MUST COMPLETE BEFORE 3.3

**CRITICAL: These tests MUST be written and MUST FAIL before ANY implementation**

### Contract Tests for Backend API
- [X] T006 [P] Contract test GET /events in backend/src/test/kotlin/api/EventsApiTest.kt
- [X] T007 [P] Contract test GET /events/{eventId}/theme-camps in backend/src/test/kotlin/api/ThemeCampsApiTest.kt
- [X] T008 [P] Contract test GET /events/{eventId}/art-installations in backend/src/test/kotlin/api/ArtInstallationsApiTest.kt
- [X] T009 [P] Contract test GET /events/{eventId}/mutant-vehicles in backend/src/test/kotlin/api/MutantVehiclesApiTest.kt
- [X] T010 [P] Contract test POST /sync/full in backend/src/test/kotlin/api/SyncApiTest.kt
- [X] T011 [P] Contract test POST /events/{eventId}/moop-reports in backend/src/test/kotlin/api/MoopReportsApiTest.kt
- [X] T012 [P] Contract test GET /events/{eventId}/weather-alerts in backend/src/test/kotlin/api/WeatherAlertsApiTest.kt

### Integration Tests for Mobile Features
- [X] T013 [P] Integration test offline map loading in mobile/composeApp/src/commonTest/kotlin/integration/OfflineMapTest.kt
- [X] T014 [P] Integration test personal schedule conflict detection in mobile/composeApp/src/commonTest/kotlin/integration/ScheduleConflictTest.kt
- [X] T015 [P] Integration test location-based content unlocking in mobile/composeApp/src/commonTest/kotlin/integration/ContentUnlockingTest.kt
- [X] T016 [P] Integration test MOOP reporting offline sync in mobile/composeApp/src/commonTest/kotlin/integration/MoopOfflineSyncTest.kt
- [X] T017 [P] Integration test smart sync priority system in mobile/composeApp/src/commonTest/kotlin/integration/SmartSyncPriorityTest.kt
- [X] T018 [P] Integration test camp location marking and navigation in mobile/composeApp/src/commonTest/kotlin/integration/CampLocationTest.kt

## Phase 3.3: Core Data Models (ONLY after tests are failing)

### Database Schema and Models
- [X] T019 [P] Participant entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/Participant.kt
- [X] T020 [P] Event entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/Event.kt
- [X] T021 [P] ThemeCamp entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/ThemeCamp.kt
- [X] T022 [P] ArtInstallation entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/ArtInstallation.kt
- [X] T023 [P] MutantVehicle entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/MutantVehicle.kt
- [X] T024 [P] EventPerformance entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/EventPerformance.kt
- [X] T025 [P] PersonalScheduleItem entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/PersonalScheduleItem.kt
- [X] T026 [P] CampLocation entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/CampLocation.kt
- [X] T027 [P] OfflineMap entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/OfflineMap.kt
- [X] T028 [P] MapPin entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/MapPin.kt
- [X] T029 [P] EmergencyContact entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/EmergencyContact.kt
- [X] T030 [P] ResourceLocation entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/ResourceLocation.kt
- [X] T031 [P] MOOPReport entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/MOOPReport.kt
- [X] T032 [P] WeatherAlert entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/WeatherAlert.kt
- [X] T033 [P] SyncManager entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/SyncManager.kt
- [X] T034 [P] ContentPackage entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/ContentPackage.kt

## Phase 3.4: Repository Layer and Data Access

### Repository Interfaces
- [X] T035 [P] EventRepository interface in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/EventRepository.kt
- [X] T036 [P] ThemeCampRepository interface in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/ThemeCampRepository.kt  
- [X] T037 [P] ArtInstallationRepository interface in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/ArtInstallationRepository.kt
- [X] T038 [P] MapRepository interface in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/MapRepository.kt
- [X] T039 [P] SyncRepository interface in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/SyncRepository.kt
- [X] T040 [P] UserPreferencesRepository interface in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/repository/UserPreferencesRepository.kt

### Repository Implementations  
- [X] T041 EventRepositoryImpl with SQLDelight local storage in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/EventRepositoryImpl.kt
- [X] T042 ThemeCampRepositoryImpl with offline caching in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ThemeCampRepositoryImpl.kt
- [X] T043 ArtInstallationRepositoryImpl with image caching in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/ArtInstallationRepositoryImpl.kt
- [X] T044 MapRepositoryImpl with offline tile management in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/MapRepositoryImpl.kt
- [X] T045 SyncRepositoryImpl with priority-based syncing in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/repository/SyncRepositoryImpl.kt

## Phase 3.5: Use Cases and Business Logic

### Core Use Cases
- [X] T046 [P] GetEventsUseCase in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/usecase/GetEventsUseCase.kt
- [X] T047 [P] SyncContentUseCase with 2GB limit logic in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/usecase/SyncContentUseCase.kt
- [X] T048 [P] UnlockContentUseCase with location/time logic in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/usecase/UnlockContentUseCase.kt
- [X] T049 [P] ManagePersonalScheduleUseCase with conflict detection in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/usecase/ManagePersonalScheduleUseCase.kt
- [X] T050 [P] MarkCampLocationUseCase in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/usecase/MarkCampLocationUseCase.kt
- [X] T051 [P] ReportMOOPUseCase with offline storage in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/usecase/ReportMOOPUseCase.kt
- [X] T052 [P] GetWeatherAlertsUseCase in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/usecase/GetWeatherAlertsUseCase.kt

## Phase 3.6: Backend API Implementation

### Event Management Endpoints
- [X] T053 GET /events endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/EventsApi.kt
- [X] T054 GET /events/{eventId}/theme-camps endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/ThemeCampsApi.kt  
- [X] T055 GET /events/{eventId}/art-installations endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/ArtInstallationsApi.kt
- [X] T056 GET /events/{eventId}/mutant-vehicles endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/MutantVehiclesApi.kt
- [X] T057 GET /events/{eventId}/performances endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/PerformancesApi.kt

### Smart Sync Endpoints
- [X] T058 POST /sync/full endpoint with 2GB limit logic in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/SyncApi.kt
- [X] T059 POST /sync/incremental endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/SyncApi.kt (same file as T058)

### Safety and Emergency Endpoints  
- [X] T060 GET /events/{eventId}/emergency-contacts endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/EmergencyApi.kt
- [X] T061 GET /events/{eventId}/resource-locations endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/ResourcesApi.kt
- [X] T062 POST /events/{eventId}/moop-reports endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/MoopApi.kt
- [X] T063 GET /events/{eventId}/weather-alerts endpoint in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/api/WeatherApi.kt

## Phase 3.7: Mobile UI - Update Existing Screens

### Update Navigation and Theme
- [ ] T064 Update NavigationDestination.kt to include new screens (Maps, Schedule, Safety, Discovery)
- [ ] T065 Update Theme.kt to include Material Design 3 dark mode tokens for desert nighttime use
- [ ] T066 Update BottomNavigationBar.kt to include new navigation items

### Update Existing Screens  
- [ ] T067 Replace ProjectsScreen.kt with EventDiscoveryScreen.kt for browsing events, art, and camps
- [ ] T068 Update AboutScreen.kt to include AfrikaBurn information and current year theme
- [ ] T069 Update DirectionsScreen.kt with offline GPS navigation to event location

### New Core Screens
- [ ] T070 [P] OfflineMapScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/map/OfflineMapScreen.kt
- [ ] T071 [P] PersonalScheduleScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/schedule/PersonalScheduleScreen.kt
- [ ] T072 [P] ThemeCampsScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/discovery/ThemeCampsScreen.kt
- [ ] T073 [P] ArtInstallationsScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/discovery/ArtInstallationsScreen.kt
- [ ] T074 [P] SafetyScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/safety/SafetyScreen.kt
- [ ] T075 [P] MOOPReportingScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/moop/MOOPReportingScreen.kt
- [ ] T076 [P] WeatherAlertsScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/weather/WeatherAlertsScreen.kt

## Phase 3.8: ViewModels and Presentation Layer

### Update Existing ViewModels
- [ ] T077 Replace ProjectsViewModel.kt with EventDiscoveryViewModel.kt
- [ ] T078 Update presentation/projects/ to presentation/discovery/ package structure

### New ViewModels  
- [ ] T079 [P] OfflineMapViewModel.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/map/OfflineMapViewModel.kt
- [ ] T080 [P] PersonalScheduleViewModel.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/schedule/PersonalScheduleViewModel.kt
- [ ] T081 [P] ThemeCampsViewModel.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/discovery/ThemeCampsViewModel.kt
- [ ] T082 [P] SafetyViewModel.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/safety/SafetyViewModel.kt
- [ ] T083 [P] SyncViewModel.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/sync/SyncViewModel.kt

### Community and Social ViewModels
- [ ] T114 [P] LocationBasedMessagingViewModel.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/community/LocationBasedMessagingViewModel.kt
- [ ] T115 [P] GiftSharingViewModel.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/community/GiftSharingViewModel.kt
- [ ] T116 [P] CommunityMessagesViewModel.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/presentation/community/CommunityMessagesViewModel.kt

## Phase 3.9: Platform-Specific Implementations

### GPS and Location Services
- [ ] T084 [P] Android GPS implementation in mobile/composeApp/src/androidMain/kotlin/io/asterixorobelix/afrikaburn/platform/LocationService.android.kt  
- [ ] T085 [P] iOS GPS implementation in mobile/composeApp/src/iosMain/kotlin/io/asterixorobelix/afrikaburn/platform/LocationService.ios.kt
- [ ] T086 [P] Common location interface in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/platform/LocationService.kt

### File Storage and Caching
- [ ] T087 [P] Android file storage implementation in mobile/composeApp/src/androidMain/kotlin/io/asterixorobelix/afrikaburn/platform/FileStorage.android.kt
- [ ] T088 [P] iOS file storage implementation in mobile/composeApp/src/iosMain/kotlin/io/asterixorobelix/afrikaburn/platform/FileStorage.ios.kt

## Phase 3.10: Dependency Injection Updates

### Update Existing DI Modules
- [ ] T089 Update DataModule.kt to include new repositories and data sources
- [ ] T090 Update DomainModule.kt to include new use cases
- [ ] T091 Update PresentationModule.kt to include new ViewModels
- [ ] T092 Add new LocationModule.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/di/LocationModule.kt

## Phase 3.11: Integration and Middleware

### Smart Sync Engine
- [ ] T093 SyncEngine implementation with priority-based 2GB storage management in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/sync/SyncEngine.kt
- [ ] T094 Content unlocking middleware with location/time logic in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/sync/ContentUnlockingService.kt

### Offline Storage Management
- [ ] T095 Storage priority manager in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/storage/StoragePriorityManager.kt
- [ ] T096 Image compression and caching service in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/data/storage/ImageCacheService.kt

### Backend Services and Business Logic
- [ ] T097 ContentUnlockingService with GPS validation in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/service/ContentUnlockingService.kt
- [ ] T098 WeatherIntegrationService for 24-hour updates in backend/src/main/kotlin/io/asterixorobelix/afrikaburn/service/WeatherIntegrationService.kt

### Community Features Implementation
- [ ] T117 [P] LocationBasedMessagingUseCase in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/usecase/LocationBasedMessagingUseCase.kt
- [ ] T118 [P] GiftSharingScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/community/GiftSharingScreen.kt
- [ ] T119 [P] CommunityMessagesScreen.kt in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/ui/community/CommunityMessagesScreen.kt

## Phase 3.12: Polish and Performance

### Unit Tests
- [ ] T099 [P] Unit tests for SyncEngine storage limits in mobile/composeApp/src/commonTest/kotlin/unit/SyncEngineTest.kt
- [ ] T100 [P] Unit tests for location-based unlocking in mobile/composeApp/src/commonTest/kotlin/unit/ContentUnlockingTest.kt
- [ ] T101 [P] Unit tests for schedule conflict detection in mobile/composeApp/src/commonTest/kotlin/unit/ScheduleConflictTest.kt
- [ ] T102 [P] Unit tests for MOOP reporting validation in mobile/composeApp/src/commonTest/kotlin/unit/MoopValidationTest.kt
- [ ] T120 [P] Unit tests for community messaging features in mobile/composeApp/src/commonTest/kotlin/unit/CommunityFeaturesTest.kt

### Performance Optimization
- [ ] T103 Battery optimization for GPS usage and background sync (target: 24+ hours moderate usage)
- [ ] T104 Image loading optimization for <3 second map loads (target: <200ms image cache hits, <3s cold loads)
- [ ] T105 Memory management for 2GB offline storage (target: <500MB RAM usage, efficient data paging)
- [ ] T106 UI performance optimization for 60fps smooth scrolling (target: <16ms frame rendering, 60fps list scrolling)

### Documentation Updates
- [ ] T107 [P] Update mobile/readme.md with new features and offline-first architecture
- [ ] T108 [P] Update backend/readme.md with API endpoints and Supabase setup
- [ ] T109 [P] Create OFFLINE_USAGE_GUIDE.md for users in desert environment

### Final Validation
- [ ] T110 Run quickstart.md validation scenarios
- [ ] T111 Verify Material Design 3 compliance (no hardcoded values)  
- [ ] T112 Test cross-platform parity (iOS/Android)
- [ ] T113 Validate constitutional requirements (offline-first, TDD, clean architecture)

## Dependencies

### Phase Dependencies  
- Setup (T001-T005) before all other phases
- Tests (T006-T018) before implementation (T019-T098)  
- Models (T019-T034) before repositories (T035-T045)
- Repositories before use cases (T046-T052)
- Use cases before ViewModels (T079-T083)
- Backend endpoints (T053-T063) can run parallel to mobile development
- Platform implementations (T084-T088) after common interfaces
- Integration (T093-T098) after core implementation
- Polish (T099-T113) after all core features

### Critical Blocking Dependencies
- T041 (EventRepositoryImpl) blocks T046 (GetEventsUseCase)
- T045 (SyncRepositoryImpl) blocks T047 (SyncContentUseCase)  
- T089-T091 (DI updates) block T077-T083, T114-T116 (ViewModels)
- T093 (SyncEngine) blocks T103-T105 (Performance optimization)
- T117 (LocationBasedMessagingUseCase) blocks T114 (LocationBasedMessagingViewModel)
- Community features (T114-T120) can run in parallel with other UI implementation

## Parallel Execution Examples

### Setup Phase (can run simultaneously)
```bash
Task: "Update mobile/composeApp/build.gradle.kts with SQLDelight, Ktor Client dependencies"
Task: "Update backend/build.gradle.kts with Supabase PostgreSQL driver" 
Task: "Configure SQLDelight database schema in mobile/composeApp/src/commonMain/sqldelight/"
Task: "Update mobile/detekt.yml with constitutional code quality rules"
```

### Contract Tests Phase (all parallel - different files)
```bash
Task: "Contract test GET /events in backend/src/test/kotlin/api/EventsApiTest.kt"
Task: "Contract test GET /events/{eventId}/theme-camps in backend/src/test/kotlin/api/ThemeCampsApiTest.kt"
Task: "Contract test POST /sync/full in backend/src/test/kotlin/api/SyncApiTest.kt"
Task: "Integration test offline map loading in mobile/composeApp/src/commonTest/kotlin/integration/OfflineMapTest.kt"
```

### Data Models Phase (all parallel - different files)
```bash  
Task: "Participant entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/Participant.kt"
Task: "Event entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/Event.kt"
Task: "ThemeCamp entity in mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/domain/model/ThemeCamp.kt"
```

## Notes
- [P] tasks = different files, no dependencies
- TDD: Verify tests fail before implementing  
- Existing codebase: Update rather than replace where possible
- Constitutional compliance: Material Design 3, offline-first, clean architecture
- Performance targets: 24+ hour battery, <3 second maps, 2GB storage limit
- All file paths are relative to repository root

## Validation Checklist
*GATE: Checked before task execution*

- [x] All API contracts have corresponding tests (T006-T012)
- [x] All entities have model creation tasks (T019-T034)  
- [x] All tests come before implementation (T006-T018 before T019+)
- [x] Parallel tasks are truly independent (different files)
- [x] Each task specifies exact file path
- [x] No task modifies same file as another [P] task
- [x] Existing codebase integration planned (update vs replace)
- [x] Constitutional requirements addressed (offline-first, Material Design 3, TDD)