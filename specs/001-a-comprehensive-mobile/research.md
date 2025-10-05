# Research: AfrikaBurn Companion Mobile App

**Date**: 2025-09-29  
**Phase**: 0 - Technology Research and Architecture Decisions

## Key Technology Decisions

### Compose Multiplatform Architecture
**Decision**: Use Compose Multiplatform 1.8.1+ with shared UI and business logic
**Rationale**: 
- Maximizes code reuse between iOS and Android (constitutional requirement)
- Single UI codebase reduces development time and maintenance
- Kotlin-native solution enables type-safe shared models
- Material Design 3 support built-in for consistent UI

**Alternatives considered**: 
- Flutter: Rejected due to Dart vs Kotlin preference and existing codebase
- React Native: Rejected due to JavaScript vs Kotlin and performance concerns
- Native development: Rejected due to code duplication and maintenance overhead

### Offline-First Data Architecture
**Decision**: SQLDelight for local database with smart sync engine
**Rationale**:
- Type-safe SQL queries generated at compile time
- Cross-platform database support (iOS/Android)
- Offline-first design required by constitutional mandate
- Enables complex relational queries for map data and event information

**Alternatives considered**:
- Room: Android-only, doesn't support Compose Multiplatform
- Realm: Kotlin Multiplatform support limited, licensing concerns
- File-based storage: Insufficient for complex relational data

### Backend Technology Stack
**Decision**: Ktor 3.1.3+ server with Supabase hosting
**Rationale**:
- Kotlin-native backend enables shared models with mobile app
- Ktor provides lightweight, coroutine-based HTTP server
- Supabase offers PostgreSQL database with real-time features
- Cost-effective hosting for single developer project

**Alternatives considered**:
- Spring Boot: Too heavyweight for mobile backend needs
- Node.js/Express: Different language increases complexity
- Firebase: Vendor lock-in concerns, less control over data

### Dependency Injection
**Decision**: Koin 3.5.3+ for dependency injection
**Rationale**:
- Kotlin-first DI framework with Compose Multiplatform support
- Lightweight runtime overhead important for mobile
- Easy testing with mock injection
- Good documentation and community support

**Alternatives considered**:
- Dagger/Hilt: Android-only, complex setup
- Manual DI: Increases boilerplate and testing difficulty
- Kodein: Less mature ecosystem

### GPS and Mapping
**Decision**: Platform-specific GPS with shared business logic
**Rationale**:
- GPS functionality requires platform-specific permissions and APIs
- Shared coordinate models and business logic in common code
- Offline map storage using platform-specific implementations
- Location-based content unlocking requires precise GPS handling

**Alternatives considered**:
- Third-party mapping SDKs: Licensing costs and offline limitations
- Web-based maps: Require internet connectivity
- OpenStreetMap: Complex offline tile management

### Testing Strategy
**Decision**: Multi-layered testing with Kotlin Test framework
**Rationale**:
- Kotlin Test supports Compose Multiplatform shared testing
- Unit tests for business logic with 80% coverage requirement
- UI tests for critical user flows (TDD constitutional requirement)
- Contract tests for backend API integration

**Alternatives considered**:
- JUnit only: Doesn't support shared multiplatform tests
- Platform-specific test frameworks: Increases maintenance

## Architecture Patterns

### MVVM + Clean Architecture
**Decision**: Implement Clean Architecture with MVVM presentation layer
**Rationale**:
- Separation of concerns improves testability and maintainability
- Portfolio-quality architecture (constitutional requirement)
- ViewModels handle UI state and business logic coordination
- Repository pattern abstracts data sources (local/remote)

### Smart Sync Engine
**Decision**: Custom sync engine with conflict resolution and priority-based storage
**Rationale**:
- 2GB storage limit requires intelligent data management
- Priority system: Safety > Maps > Static > Community > Event schedule
- Handles incomplete/messy data from event organizers
- Location and time-based content unlocking logic

## Performance Considerations

### Battery Optimization
**Decision**: Background task optimization with location services management
**Rationale**:
- 24+ hour battery life requirement in remote desert environment
- Minimize GPS polling frequency when not actively navigating
- Efficient image caching and lazy loading for artwork photos
- Dark mode implementation for nighttime desert use

### Storage Management
**Decision**: Tiered storage with automatic cleanup and compression
**Rationale**:
- 2GB limit for comprehensive content including images
- Image compression and multiple resolution support
- Automatic cache cleanup based on usage patterns
- Offline-first with intelligent background sync

## Security and Privacy

### Anonymous Authentication
**Decision**: Device-based identity with privacy-conscious community features
**Rationale**:
- Aligns with AfrikaBurn's inclusive, non-commercial principles
- No personal data collection beyond device preferences
- MOOP reporting and community features work without user accounts
- Location data used only for content unlocking, not tracking

### Remote Logging
**Decision**: Privacy-conscious diagnostic logging for desert troubleshooting
**Rationale**:
- Remote debugging required when users can't be directly accessed
- No sensitive location data beyond general Tankwa Karoo area
- Crash reporting with automatic PII scrubbing
- Structured logging for effective issue diagnosis

## Material Design 3 Implementation

### Theme System
**Decision**: Centralized theme with Material Design 3 tokens
**Rationale**:
- Constitutional requirement for consistent design language
- No hardcoded colors, dimensions, or typography allowed
- Supports both light and dark modes (desert nighttime requirement)
- Accessibility compliance built into Material Design system

### Component Architecture
**Decision**: Reusable composable components with preview functions
**Rationale**:
- Each composable file must include exactly one preview function
- Centralized Dimens object for all spacing values
- String resources for all user-facing text (no hardcoded strings)
- Component library approach for maintainability

## Research Validation

All technical decisions align with constitutional requirements:
- ✅ Offline-first architecture implemented
- ✅ Cross-platform code sharing maximized
- ✅ Material Design 3 consistency enforced
- ✅ Test-first development supported
- ✅ Portfolio-quality patterns demonstrated
- ✅ Community-centric design principles maintained
- ✅ Remote observability with privacy protection

**Status**: Research complete, all technology choices validated against requirements