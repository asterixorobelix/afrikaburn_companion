# Quickstart: AfrikaBurn Companion Mobile App

**Date**: 2025-09-29  
**Purpose**: Validation scenarios for successful implementation


## Contents

- [Prerequisites](#prerequisites)
  - [Development Environment](#development-environment)
  - [Required Dependencies](#required-dependencies)
- [Quick Validation Tests](#quick-validation-tests)
  - [1. Offline-First Architecture Test](#1-offline-first-architecture-test)
  - [2. Smart Sync Test](#2-smart-sync-test)
  - [3. Location-Based Content Unlocking Test](#3-location-based-content-unlocking-test)
  - [4. Personal Schedule Builder Test](#4-personal-schedule-builder-test)
  - [5. MOOP Reporting Test](#5-moop-reporting-test)
  - [6. Material Design 3 Consistency Test](#6-material-design-3-consistency-test)
  - [7. Cross-Platform Parity Test](#7-cross-platform-parity-test)
- [Performance Benchmarks](#performance-benchmarks)
  - [Battery Life Validation](#battery-life-validation)
  - [Map Loading Performance](#map-loading-performance)
  - [Storage Usage Monitoring](#storage-usage-monitoring)
- [Backend Integration Tests](#backend-integration-tests)
  - [API Contract Validation](#api-contract-validation)
  - [Data Consistency Test](#data-consistency-test)
  - [Weather Integration Test](#weather-integration-test)
- [Success Validation Checklist](#success-validation-checklist)
- [Deployment Validation](#deployment-validation)
  - [Pre-Release Checklist](#pre-release-checklist)
  - [Post-Deployment Monitoring](#post-deployment-monitoring)
- [Troubleshooting Common Issues](#troubleshooting-common-issues)
  - [GPS Location Not Working](#gps-location-not-working)
  - [Content Not Unlocking](#content-not-unlocking)
  - [Storage Issues](#storage-issues)
  - [Sync Problems](#sync-problems)

## Prerequisites

### Development Environment
- Kotlin 2.1.21+ installed
- Android Studio with Compose Multiplatform plugin
- Xcode 15+ (for iOS development)
- Supabase account and project setup
- Device/simulator with GPS capabilities

### Required Dependencies
```kotlin
// composeApp/build.gradle.kts
dependencies {
    // Compose Multiplatform
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    
    // Architecture
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.8.0")
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
    
    // Dependency Injection
    implementation("io.insert-koin:koin-compose:1.1.5")
    
    // Database
    implementation("app.cash.sqldelight:runtime:2.0.1")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
    
    // Networking
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.insert-koin:koin-test:3.5.3")
    testImplementation("io.mockk:mockk:1.13.8")
}
```

## Quick Validation Tests

### 1. Offline-First Architecture Test
**Goal**: Verify app functions completely offline
**Steps**:
1. Install app on device/simulator
2. Enable airplane mode (disable all network connectivity)
3. Launch app - should display cached content
4. Navigate to map screen - should show offline maps
5. Try to mark camp location - should work with local GPS
6. Create personal schedule item - should save locally
7. Access safety information - should display without network

**Success Criteria**: All core features functional without network

### 2. Smart Sync Test  
**Goal**: Validate 2GB storage limit and priority-based content management
**Steps**:
1. Connect to WiFi
2. Initiate full sync for current event
3. Monitor storage usage during sync
4. Verify sync stops at 2GB limit
5. Check content priority: Safety > Maps > Static > Community > Event schedule
6. Simulate low storage - verify automatic cleanup

**Success Criteria**: 
- Sync respects 2GB limit
- Priority order enforced
- Storage cleanup works

### 3. Location-Based Content Unlocking Test
**Goal**: Test event content visibility based on GPS location
**Steps**:
1. Set device location outside Tankwa Karoo (use simulator)
2. Launch app - event-specific content should be hidden
3. Access driving directions and safety info - should be visible
4. Change device location to within 5km of event center coordinates
5. Refresh app - hidden content should become visible
6. Verify theme camps, art installations, performances now accessible

**Success Criteria**: 
- Content properly hidden/shown based on location
- Non-sensitive content always accessible

### 4. Personal Schedule Builder Test
**Goal**: Validate schedule conflicts and user experience
**Steps**:
1. Add first event to personal schedule (e.g., 2PM-4PM Workshop)
2. Add overlapping event (e.g., 3PM-5PM Performance)
3. Verify conflict is highlighted but addition allowed
4. Check schedule view shows both events with conflict indicator
5. Navigate to event locations using GPS

**Success Criteria**:
- Conflicts detected and highlighted
- Both events saved successfully
- Navigation works for event locations

### 5. MOOP Reporting Test
**Goal**: Test offline environmental reporting
**Steps**:
1. Disable network connectivity
2. Navigate to MOOP reporting feature
3. Select location on map or use current GPS
4. Add description and photo (if available)
5. Submit report - should save locally
6. Re-enable network
7. Verify report syncs to backend within 48 hours

**Success Criteria**:
- Reports save offline
- Sync works when connectivity restored

### 6. Material Design 3 Consistency Test
**Goal**: Verify UI follows Material Design 3 standards
**Steps**:
1. Navigate through all major screens
2. Toggle dark mode - all screens should adapt
3. Check for hardcoded colors/dimensions (should be none)
4. Verify string externalization (no hardcoded text)
5. Test accessibility features (screen reader, large text)

**Success Criteria**:
- Consistent MD3 theming throughout
- Dark mode works correctly
- No hardcoded values found

### 7. Cross-Platform Parity Test
**Goal**: Ensure feature parity between iOS and Android
**Steps**:
1. Deploy same build to iOS and Android devices
2. Test identical user flows on both platforms
3. Verify UI consistency and performance
4. Test platform-specific features (GPS, permissions)
5. Compare offline storage behavior

**Success Criteria**:
- Identical functionality on both platforms
- Consistent UI/UX experience
- Platform integrations work correctly

## Performance Benchmarks

### Battery Life Validation
**Target**: 24+ hours moderate usage
**Test Process**:
1. Fully charge device
2. Use app with typical AfrikaBurn usage pattern:
   - Check map/navigation: 20% of time
   - Browse events/art: 30% of time
   - Personal schedule: 10% of time  
   - Idle with GPS enabled: 40% of time
3. Monitor battery drain over 24 hours
4. Verify app continues functioning throughout

### Map Loading Performance
**Target**: <3 second map loads
**Test Process**:
1. Clear map cache
2. Load event map with all pins
3. Measure initial load time
4. Test zoom/pan performance
5. Verify smooth 60fps operation

### Storage Usage Monitoring
**Target**: Efficient use of 2GB allocation
**Test Process**:
1. Monitor storage during full sync
2. Verify compression of images
3. Test cache cleanup mechanisms
4. Validate priority-based storage management

## Backend Integration Tests

### API Contract Validation
**Test**: Verify all API endpoints match OpenAPI spec
```bash
# Run contract tests
./gradlew contractTest

# Expected: All endpoints return correct schemas
# Expected: Error responses match spec
# Expected: Authentication works properly
```

### Data Consistency Test
**Test**: Ensure mobile and backend data models align
1. Sync event data from backend
2. Verify all fields map correctly
3. Test data validation rules
4. Confirm error handling for invalid data

### Weather Integration Test  
**Test**: 24-hour weather alert updates
1. Configure weather service integration
2. Verify alerts update every 24 hours
3. Test dust storm warning delivery
4. Confirm offline caching of weather data

## Success Validation Checklist

**Core Functionality**:
- [ ] App launches and functions completely offline
- [ ] GPS navigation to event location works
- [ ] Offline maps display with all pinned locations
- [ ] Personal camp marking and navigation works
- [ ] Event discovery features accessible (when unlocked)
- [ ] Personal schedule builder with conflict detection
- [ ] Safety/emergency information always available
- [ ] MOOP reporting works offline with sync

**Technical Requirements**:
- [ ] Material Design 3 theming throughout
- [ ] Dark mode support functional
- [ ] Cross-platform parity achieved
- [ ] 2GB storage limit respected
- [ ] 24+ hour battery life achieved
- [ ] <3 second map loading performance
- [ ] 80% backend test coverage achieved

**Constitutional Compliance**:
- [ ] Offline-first architecture implemented
- [ ] Community-centric design principles followed
- [ ] Test-first development process used
- [ ] Portfolio-quality code and documentation
- [ ] Event information secrecy rules enforced
- [ ] Remote observability with privacy protection

## Deployment Validation

### Pre-Release Checklist
1. **Code Quality**:
   - All tests passing
   - Detekt static analysis clean
   - No hardcoded strings or values
   - Documentation complete

2. **Performance Validation**:
   - Battery life benchmarks met
   - Map performance acceptable
   - Storage management working

3. **User Experience**:
   - Onboarding flow smooth
   - Navigation intuitive
   - Offline functionality obvious to users
   - Error states handled gracefully

4. **Backend Readiness**:
   - APIs deployed and stable
   - Database migrations applied
   - Weather integration active
   - Monitoring and logging operational

### Post-Deployment Monitoring
- Monitor crash reports and error logs
- Track battery usage across different devices
- Validate GPS accuracy and location unlocking
- Measure sync performance and success rates
- Collect user feedback on offline functionality

## Troubleshooting Common Issues

### GPS Location Not Working
1. Check location permissions granted
2. Verify GPS simulation settings in development
3. Test with actual device outdoors
4. Confirm location service integration

### Content Not Unlocking
1. Verify device location within 5km radius
2. Check event dates and time-based unlocking
3. Confirm network connectivity for initial sync
4. Review backend logs for unlock logic

### Storage Issues
1. Monitor storage usage patterns  
2. Verify cache cleanup triggers
3. Test priority-based content removal
4. Check compression settings for images

### Sync Problems
1. Validate network connectivity
2. Check backend API availability
3. Review sync conflict resolution
4. Test incremental vs full sync

This quickstart provides comprehensive validation that the AfrikaBurn Companion app meets all functional, technical, and constitutional requirements for deployment.