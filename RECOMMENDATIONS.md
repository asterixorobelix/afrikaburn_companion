# AfrikaBurn Companion - Improvement Recommendations


## Contents

- [Executive Summary](#executive-summary)
- [Part 1: UI/UX Improvements](#part-1-uiux-improvements)
  - [1.1 Immediate UI Enhancements (High Impact, Low Effort)](#11-immediate-ui-enhancements-high-impact-low-effort)
  - [1.2 Navigation & Information Architecture](#12-navigation-information-architecture)
  - [1.3 Accessibility Improvements](#13-accessibility-improvements)
  - [1.4 Theme & Visual Polish](#14-theme-visual-polish)
- [Part 2: Feature Recommendations](#part-2-feature-recommendations)
  - [2.1 High-Priority Features (Must-Have for MVP)](#21-high-priority-features-must-have-for-mvp)
  - [2.2 Medium-Priority Features (v1.1)](#22-medium-priority-features-v11)
  - [2.3 Future Features (v2.0+)](#23-future-features-v20)
- [Part 3: Solo Developer Workflow Optimization](#part-3-solo-developer-workflow-optimization)
  - [3.1 Reduce Backend Burden](#31-reduce-backend-burden)
  - [3.2 Development Tooling Improvements](#32-development-tooling-improvements)
  - [3.3 Simplify Testing Strategy](#33-simplify-testing-strategy)
  - [3.4 Feature Flag System](#34-feature-flag-system)
  - [3.5 Reduce Scope Creep](#35-reduce-scope-creep)
  - [3.6 Fastlane for Automated Deployments](#36-fastlane-for-automated-deployments)
  - [3.7 Documentation Improvements](#37-documentation-improvements)
- [Part 4: Technical Debt & Code Quality](#part-4-technical-debt-code-quality)
  - [4.1 Immediate Refactoring](#41-immediate-refactoring)
  - [4.2 Architecture Improvements](#42-architecture-improvements)
  - [4.3 Performance Optimizations](#43-performance-optimizations)
- [Part 5: Prioritized Action Plan](#part-5-prioritized-action-plan)
  - [Phase 1: Quick Wins (1-2 days)](#phase-1-quick-wins-1-2-days)
  - [Phase 2: MVP Features (1-2 weeks)](#phase-2-mvp-features-1-2-weeks)
  - [Phase 3: Polish (3-5 days)](#phase-3-polish-3-5-days)
  - [Phase 4: Release (2-3 days)](#phase-4-release-2-3-days)
- [Appendix: Recommended Libraries](#appendix-recommended-libraries)
  - [Immediate Additions](#immediate-additions)
  - [Future Considerations](#future-considerations)
- [Conclusion](#conclusion)

A comprehensive analysis and recommendations for improving the app, with special focus on UI/UX, feature additions, and solo developer workflow optimization.

---

## Executive Summary

The AfrikaBurn Companion has a solid architectural foundation with Clean Architecture, MVVM, and Compose Multiplatform. However, significant work remains to deliver the full feature set. This document prioritizes improvements that maximize impact while minimizing complexity for a solo developer.

**Current Status:**
- **Mobile**: 3 screens implemented (Projects, Directions, About)
- **Backend**: Infrastructure only (no business endpoints)
- **Tests**: ~1,400 LOC with good patterns
- **Completion**: ~15% of planned features

---

## Part 1: UI/UX Improvements

### 1.1 Immediate UI Enhancements (High Impact, Low Effort)

#### A. Improve Visual Hierarchy on ProjectsScreen
**Current Issue**: The ProjectsScreen (557 LOC) is dense and could benefit from better visual breathing room.

**Recommendations:**
- Add section headers between filter chips and content
- Increase spacing between project cards (currently feels cramped)
- Add subtle animations for state transitions (loading → content)
- Consider a "sticky" search bar that collapses on scroll

```kotlin
// Suggested spacing improvements in Theme.kt Dimens
val cardSpacing = 16.dp  // Currently using 8.dp
val sectionHeaderPadding = 24.dp
```

#### B. Enhanced Empty States
**Current**: Generic "No results found" messages.

**Recommendation**: Create illustrated empty states that:
- Show context-aware suggestions ("Try searching for 'music' or 'workshop'")
- Include call-to-action buttons
- Use custom illustrations (even simple SVG icons)

#### C. Pull-to-Refresh Pattern
**Missing Feature**: Users expect pull-to-refresh on mobile lists.

**Implementation**: Add SwipeRefresh to ProjectsList for future sync integration:
```kotlin
// Use accompanist's SwipeRefresh or Material3's PullRefresh
PullRefreshIndicator(refreshing, pullRefreshState)
```

#### D. Loading Skeletons
**Current**: Circular progress indicator.

**Recommendation**: Replace with shimmer/skeleton loading that matches the card layout:
- Reduces perceived loading time
- More professional appearance
- Better UX for slow connections

### 1.2 Navigation & Information Architecture

#### A. Tab Navigation Improvements
**Current**: 6 horizontal tabs that require scrolling on smaller screens.

**Recommendations:**
1. Consider a vertical drawer or bottom sheet for category selection on phones
2. Add tab badges showing item counts: "Events (42)"
3. Remember last selected tab across sessions
4. Add gesture navigation between tabs (swipe left/right)

#### B. Search Experience Enhancement
**Current**: Basic text field with immediate filtering.

**Recommendations:**
1. Add search history/suggestions
2. Implement voice search (especially useful in dusty conditions with gloves)
3. Add quick filter chips below search: "Near Me", "Happening Now", "Family Friendly"
4. Highlight search terms in results

#### C. Detail Screen Pattern
**Missing**: Tapping a project card has no detail view.

**Critical Addition**: Create `ProjectDetailScreen` with:
- Full description
- Photo gallery (when available)
- Map location preview
- "Add to Schedule" action
- Share functionality
- Related projects section

### 1.3 Accessibility Improvements

#### A. Content Descriptions
**Current**: Basic contentDescription on some elements.

**Recommendations:**
1. Audit all interactive elements for screen reader support
2. Add semantic headings for TalkBack navigation
3. Ensure minimum touch target sizes (48dp)
4. Support dynamic type scaling

#### B. High Contrast Mode
**Recommendation**: Add high contrast theme option for bright sunlight conditions (critical for desert environment).

#### C. One-Handed Mode
**Recommendation**: Consider reachability for large phones - move critical actions to bottom of screen.

### 1.4 Theme & Visual Polish

#### A. Dynamic Theming
**Current**: Static Material 3 theme.

**Enhancement Options:**
1. Add event-year theming (each AfrikaBurn has a theme - reflect it in colors)
2. Support system dynamic colors on Android 12+
3. Add "desert mode" - warm amber tones for night use

#### B. Micro-Interactions
**Missing**: Static UI lacks delight.

**Add:**
- Card press animations (scale down slightly)
- Tab switch transitions
- Filter chip selection feedback
- Success/error haptic feedback

---

## Part 2: Feature Recommendations

### 2.1 High-Priority Features (Must-Have for MVP)

#### A. Offline Maps with GPS
**Priority**: CRITICAL (core value proposition)

**Implementation Approach:**
1. Use MapLibre GL (open-source, offline-capable)
2. Pre-package Tankwa region tiles (~50-100MB)
3. Show user location with accuracy circle
4. Mark saved camp location
5. Display content pins with clustering

**Solo Dev Simplification:**
- Start with static map image + markers (much simpler)
- Add interactive map in v2
- Consider MapTiler or Mapbox with offline packs

#### B. Personal Schedule Builder
**Priority**: HIGH (differentiator feature)

**MVP Scope:**
1. Add events to personal schedule from any listing
2. Basic time conflict detection
3. "What's happening now" quick view
4. Simple list view (no calendar UI needed for v1)

**Solo Dev Tip:** Don't build a calendar component - use a simple chronological list grouped by day.

#### C. Location-Based Content Unlock
**Priority**: HIGH (protects sensitive info)

**Implementation:**
1. Check GPS position on app open
2. Unlock content when within 5km of event location
3. Cache unlock state for offline use
4. Manual override for emergencies

#### D. Emergency Information Screen
**Priority**: HIGH (safety critical)

**Content:**
- Ranger station locations
- Medical tent locations
- Emergency phone numbers
- Basic first aid info
- "Share my location" emergency button

### 2.2 Medium-Priority Features (v1.1)

#### A. Packing Checklist
**Effort**: Low
**Value**: High (practical utility)

**Implementation:**
- Pre-populated checklist based on AfrikaBurn requirements
- Checkable items with persistence
- Categories: Essential, Camping, Food, Costumes, Emergency
- Exportable/shareable

#### B. Weather Dashboard
**Effort**: Medium
**Value**: High (safety)

**Implementation:**
- Integrate with weather API (OpenWeatherMap free tier)
- Cache last known conditions for offline
- Dust storm warnings
- UV index display
- Wind speed (important for tent setup)

#### C. MOOP Reporting
**Effort**: Medium
**Value**: Medium (community goodwill)

**Simplified MVP:**
- Photo capture with location stamp
- Queue for upload when connectivity returns
- No backend needed for v1 - just local tracking

### 2.3 Future Features (v2.0+)

#### A. Community Features
- Location-based messages
- Gift sharing board
- Theme camp "check-ins"

**Note:** These require significant backend work. Defer until core features solid.

#### B. AR Features
- Point camera at art installation for info overlay
- QR code scanning for quick lookup

#### C. Multi-Event Support
- Support multiple Burning Man events
- Historical archive of past years

---

## Part 3: Solo Developer Workflow Optimization

### 3.1 Reduce Backend Burden

#### A. Backend-Optional Architecture
**Current Problem:** Backend has no business logic but mobile app needs it.

**Recommendation:** Make mobile app fully functional without backend:

1. **Bundle all 2024 data as JSON resources** (already doing this)
2. **Use SQLDelight purely for user data** (schedule, settings, MOOP drafts)
3. **Defer sync to v2** - smart sync is complex and not needed for event

```
Priority:
1. Offline-first mobile ✓
2. Local user data persistence
3. Backend sync (future)
```

#### B. Eliminate Backend for MVP
**Radical Simplification:**

The backend is currently empty. Consider:
- Remove backend from MVP scope entirely
- All content bundled in app
- User data stored locally only
- Backend only needed for:
  - Real-time content updates (can wait)
  - Community features (can wait)
  - Analytics (optional)

**Savings:** ~40% of planned work eliminated

### 3.2 Development Tooling Improvements

#### A. Add Makefile or Task Runner
**Current:** Must remember gradle commands.

**Create** `Makefile` for common tasks:
```makefile
.PHONY: test build run-android run-ios lint

test:
	./mobile/gradlew -p mobile test

test-coverage:
	./mobile/gradlew -p mobile test jacocoTestReport
	open mobile/composeApp/build/reports/jacoco/test/html/index.html

build-android:
	./mobile/gradlew -p mobile :composeApp:assembleDebug

install-android:
	./mobile/gradlew -p mobile :composeApp:installDebug

lint:
	./mobile/gradlew -p mobile detekt

clean:
	./mobile/gradlew -p mobile clean
```

#### B. Pre-commit Hooks
**Add** `.pre-commit-config.yaml`:
```yaml
repos:
  - repo: local
    hooks:
      - id: detekt
        name: Detekt
        entry: ./mobile/gradlew -p mobile detekt
        language: system
        pass_filenames: false
      - id: ktfmt
        name: Kotlin Format Check
        entry: ./mobile/gradlew -p mobile ktfmtCheck
        language: system
        pass_filenames: false
```

#### C. VS Code / Cursor Task Definitions
**Create** `.vscode/tasks.json`:
```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Run Tests",
      "type": "shell",
      "command": "./mobile/gradlew -p mobile test",
      "problemMatcher": []
    },
    {
      "label": "Build Android Debug",
      "type": "shell",
      "command": "./mobile/gradlew -p mobile :composeApp:assembleDebug"
    }
  ]
}
```

### 3.3 Simplify Testing Strategy

#### A. Prioritize Test Types
**Current:** Attempting 80% coverage on everything.

**Recommendation for Solo Dev:**
```
Priority 1: ViewModel tests (business logic) - KEEP
Priority 2: Repository tests (data layer) - KEEP
Priority 3: Integration tests (critical flows) - SELECTIVE
Priority 4: UI tests - SKIP for now (use manual testing)
```

#### B. Use Snapshot Testing for UI
**Instead of** writing UI assertions, use screenshot tests:
- Paparazzi for Android
- Captures UI state automatically
- Visual diff on changes
- Much faster to maintain

#### C. Reduce Test Ceremony
**Current Tests:** Sometimes verbose setup.

**Use Test Fixtures:**
```kotlin
// Create shared test fixtures
object TestFixtures {
    val sampleProject = ProjectItem(
        id = "test-1",
        name = "Test Camp",
        // ... defaults
    )

    val sampleProjects = listOf(sampleProject, ...)
}
```

### 3.4 Feature Flag System

**Add simple feature flags** for incremental rollout:
```kotlin
object FeatureFlags {
    val OFFLINE_MAPS_ENABLED = false
    val SCHEDULE_BUILDER_ENABLED = false
    val COMMUNITY_FEATURES_ENABLED = false
}
```

**Benefits:**
- Ship incomplete features hidden
- A/B testing later
- Quick disable if bugs found
- Gradual rollout

### 3.5 Reduce Scope Creep

#### A. Define MVP Clearly
**Proposed MVP Feature Set:**
1. ✅ Browse projects by category
2. ✅ Search and filter
3. ✅ Directions to event
4. ✅ About information
5. ⬜ Offline map with GPS (static image OK)
6. ⬜ Emergency contacts screen
7. ⬜ Save camp location
8. ⬜ Basic schedule (add/remove events)

**That's it.** Everything else is v1.1+.

#### B. Time-Box Features
**Rule:** If a feature takes >3 days, split it or defer it.

#### C. Use GitHub Projects
**Set up** a simple Kanban board:
- Backlog
- This Sprint
- In Progress (max 2 items)
- Done

### 3.6 Fastlane for Automated Deployments

Fastlane is a **game-changer for solo developers** - it automates the most tedious parts of app releases. One command can build, sign, screenshot, and deploy to both stores.

#### A. What Fastlane Automates

| Task | Manual Time | With Fastlane |
|------|-------------|---------------|
| Build release APK/IPA | 5-10 min | Automated |
| Code signing setup | 30-60 min | Once, then automatic |
| Upload to TestFlight | 10-15 min | 1 command |
| Upload to Play Console | 10-15 min | 1 command |
| Generate screenshots | 2-4 hours | Automated |
| Increment version numbers | 5 min | Automated |
| Create release notes | 10 min | From git commits |

**Time Saved Per Release:** ~3-4 hours

#### B. Recommended Directory Structure

```
mobile/
├── fastlane/
│   ├── Fastfile              # Main lane definitions
│   ├── Appfile               # App identifiers
│   ├── Matchfile             # Code signing config (iOS)
│   ├── Pluginfile            # Fastlane plugins
│   ├── metadata/
│   │   ├── android/          # Play Store metadata
│   │   │   ├── en-US/
│   │   │   │   ├── title.txt
│   │   │   │   ├── short_description.txt
│   │   │   │   ├── full_description.txt
│   │   │   │   └── changelogs/
│   │   │   └── images/
│   │   └── ios/              # App Store metadata
│   └── screenshots/          # Auto-generated screenshots
├── iosApp/
└── composeApp/
```

#### C. Sample Fastfile for Compose Multiplatform

```ruby
# mobile/fastlane/Fastfile

default_platform(:android)

# ============== ANDROID ==============
platform :android do
  desc "Run tests"
  lane :test do
    gradle(
      project_dir: "..",
      task: ":composeApp:testDebugUnitTest"
    )
  end

  desc "Build debug APK"
  lane :build_debug do
    gradle(
      project_dir: "..",
      task: ":composeApp:assembleDebug"
    )
  end

  desc "Build release AAB for Play Store"
  lane :build_release do
    # Increment version code
    increment_version_code(
      gradle_file_path: "../composeApp/build.gradle.kts"
    )

    gradle(
      project_dir: "..",
      task: ":composeApp:bundleRelease",
      properties: {
        "android.injected.signing.store.file" => ENV["KEYSTORE_PATH"],
        "android.injected.signing.store.password" => ENV["KEYSTORE_PASSWORD"],
        "android.injected.signing.key.alias" => ENV["KEY_ALIAS"],
        "android.injected.signing.key.password" => ENV["KEY_PASSWORD"]
      }
    )
  end

  desc "Deploy to Play Store Internal Testing"
  lane :internal do
    build_release
    upload_to_play_store(
      track: "internal",
      aab: "../composeApp/build/outputs/bundle/release/composeApp-release.aab",
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true
    )
  end

  desc "Deploy to Play Store Beta"
  lane :beta do
    build_release
    upload_to_play_store(
      track: "beta",
      aab: "../composeApp/build/outputs/bundle/release/composeApp-release.aab"
    )
  end

  desc "Deploy to Play Store Production"
  lane :release do
    build_release
    upload_to_play_store(
      track: "production",
      aab: "../composeApp/build/outputs/bundle/release/composeApp-release.aab",
      rollout: "0.1"  # 10% rollout initially
    )
  end
end

# ============== iOS ==============
platform :ios do
  desc "Sync certificates and profiles with match"
  lane :certificates do
    match(type: "appstore", readonly: true)
  end

  desc "Build iOS app"
  lane :build do
    certificates

    build_app(
      workspace: "../iosApp/iosApp.xcworkspace",
      scheme: "iosApp",
      export_method: "app-store",
      output_directory: "./build",
      output_name: "AfrikaBurnCompanion.ipa"
    )
  end

  desc "Deploy to TestFlight"
  lane :beta do
    build
    upload_to_testflight(
      skip_waiting_for_build_processing: true,
      distribute_external: false
    )
  end

  desc "Deploy to App Store"
  lane :release do
    build
    upload_to_app_store(
      submit_for_review: false,
      automatic_release: false,
      force: true
    )
  end
end

# ============== SHARED ==============
desc "Generate release notes from git commits"
lane :release_notes do
  changelog = changelog_from_git_commits(
    commits_count: 10,
    pretty: "- %s"
  )
  puts changelog
end
```

#### D. Appfile Configuration

```ruby
# mobile/fastlane/Appfile

# Android
json_key_file("path/to/play-store-credentials.json")
package_name("io.asterixorobelix.afrikaburn")

# iOS
app_identifier("io.asterixorobelix.afrikaburn")
apple_id("your@email.com")
itc_team_id("YOUR_ITC_TEAM_ID")
team_id("YOUR_DEV_TEAM_ID")
```

#### E. iOS Code Signing with Match

**Problem:** iOS code signing is notoriously complex.

**Solution:** Use `match` to store certificates in a private git repo:

```ruby
# mobile/fastlane/Matchfile
git_url("git@github.com:yourusername/certificates.git")
storage_mode("git")
type("appstore")
app_identifier(["io.asterixorobelix.afrikaburn"])
username("your@email.com")
```

**Setup once:**
```bash
cd mobile
fastlane match init
fastlane match appstore  # Creates and stores certificates
```

**Benefits:**
- New machine? Run `fastlane match` and you're ready
- CI/CD ready out of the box
- No more "code signing hell"

#### F. Recommended Lanes for Solo Developer

**Daily Development:**
```bash
fastlane android test          # Run tests
fastlane android build_debug   # Quick debug build
```

**Weekly Beta Release:**
```bash
fastlane android internal      # Push to Play Store internal
fastlane ios beta              # Push to TestFlight
```

**Production Release:**
```bash
fastlane android release       # Play Store (10% rollout)
fastlane ios release           # App Store submission
```

#### G. CI/CD Integration (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Ruby for Fastlane
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: '3.2'
          bundler-cache: true
          working-directory: mobile

      - name: Deploy to Play Store
        working-directory: mobile
        env:
          KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: bundle exec fastlane android internal

  deploy-ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Ruby for Fastlane
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: '3.2'
          bundler-cache: true
          working-directory: mobile

      - name: Deploy to TestFlight
        working-directory: mobile
        env:
          MATCH_PASSWORD: ${{ secrets.MATCH_PASSWORD }}
          MATCH_GIT_BASIC_AUTHORIZATION: ${{ secrets.MATCH_GIT_AUTH }}
        run: bundle exec fastlane ios beta
```

#### H. Useful Fastlane Plugins

```ruby
# mobile/fastlane/Pluginfile
gem 'fastlane-plugin-increment_version_code'  # Android version bumping
gem 'fastlane-plugin-changelog'                # Changelog management
gem 'fastlane-plugin-badge'                    # Add beta badges to icons
```

#### I. Quick Setup Commands

```bash
# Install Fastlane
cd mobile
brew install fastlane  # macOS

# Initialize Fastlane
fastlane init

# Create Gemfile for version locking
bundle init
echo "gem 'fastlane'" >> Gemfile
bundle install

# First Android deploy (sets up Play Store connection)
fastlane supply init

# First iOS deploy (sets up App Store connection)
fastlane deliver init
```

#### J. Time Investment vs. Payoff

| Setup Task | One-Time Effort | Saves Per Release |
|------------|-----------------|-------------------|
| Basic Fastfile | 2-3 hours | 1-2 hours |
| Match (iOS signing) | 1-2 hours | 30 min + headaches |
| CI/CD integration | 2-4 hours | Manual work eliminated |
| Screenshot automation | 3-4 hours | 2-4 hours |

**Break-even:** After 3-4 releases, Fastlane pays for itself entirely.

---

### 3.7 Documentation Improvements

#### A. Simplify CLAUDE.md
**Current:** Very detailed but overwhelming.

**Add Quick Reference Section:**
```markdown
## Quick Start for New Session

### Most Common Commands
- Run tests: `./mobile/gradlew -p mobile test`
- Build Android: `./mobile/gradlew -p mobile :composeApp:installDebug`
- Lint: `./mobile/gradlew -p mobile detekt`

### Key Files to Know
- Entry point: `/mobile/composeApp/src/commonMain/kotlin/.../App.kt`
- Theme: `/mobile/composeApp/src/commonMain/kotlin/.../Theme.kt`
- Navigation: `/mobile/composeApp/src/commonMain/kotlin/.../navigation/`
```

#### B. Add Architecture Decision Records (ADRs)
**Create** `/docs/decisions/` with simple markdown files:
- `001-offline-first.md` - Why offline-first architecture
- `002-compose-multiplatform.md` - Why CMP over Flutter/RN
- `003-no-backend-mvp.md` - Why backend deferred

---

## Part 4: Technical Debt & Code Quality

### 4.1 Immediate Refactoring

#### A. Split ProjectsScreen.kt
**Current:** 557 LOC single file.

**Split into:**
- `ProjectsScreen.kt` - Main screen scaffold (100 LOC)
- `ProjectSearchBar.kt` - Search component
- `ProjectFilterChips.kt` - Filter UI
- `ProjectCard.kt` - Individual card
- `ProjectList.kt` - LazyColumn wrapper

#### B. Extract String Resources
**Current:** Some hardcoded strings remain.

**Action:** Move all user-visible strings to `strings.xml`

#### C. Centralize Error Messages
**Create** `ErrorMessages.kt`:
```kotlin
object ErrorMessages {
    fun forProjectLoad(cause: Throwable): String = when (cause) {
        is IOException -> "Unable to load data. Check your connection."
        is JsonException -> "Data format error. Please update the app."
        else -> "Something went wrong. Please try again."
    }
}
```

### 4.2 Architecture Improvements

#### A. Add Use Case Layer
**Current:** ViewModels call repositories directly.

**Recommendation:** Add use cases for complex operations:
```kotlin
class GetFilteredProjectsUseCase(
    private val repository: ProjectsRepository
) {
    suspend operator fun invoke(
        type: ProjectType,
        query: String,
        filters: FilterState
    ): Result<List<ProjectItem>>
}
```

**Benefits:**
- Easier to test business logic
- Reusable across ViewModels
- Clearer responsibilities

#### B. Implement Result Type Consistently
**Current:** Mix of nullable returns and Result type.

**Standardize** on sealed Result:
```kotlin
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val exception: Throwable) : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()
}
```

### 4.3 Performance Optimizations

#### A. Image Loading
**Current:** Images loaded synchronously from resources.

**Add:** Coil for async image loading with caching:
```kotlin
AsyncImage(
    model = imageUrl,
    contentDescription = null,
    placeholder = painterResource(R.drawable.placeholder)
)
```

#### B. List Performance
**Current:** LazyColumn without key specification.

**Improve:**
```kotlin
LazyColumn {
    items(
        items = projects,
        key = { it.id }  // Stable keys for better diffing
    ) { project ->
        ProjectCard(project)
    }
}
```

---

## Part 5: Prioritized Action Plan

### Phase 1: Quick Wins (1-2 days)
1. Create Makefile for common commands
2. Add .vscode/tasks.json
3. Split ProjectsScreen.kt into smaller components
4. Add loading skeletons
5. Improve empty states

### Phase 2: MVP Features (1-2 weeks)
1. Emergency contacts screen
2. Static offline map with GPS dot
3. Save camp location (local storage)
4. Basic schedule (add/view events)

### Phase 3: Polish (3-5 days)
1. Pull-to-refresh
2. Search improvements
3. Detail screen for projects
4. Accessibility audit
5. Performance testing

### Phase 4: Release (2-3 days)
1. App icon generation
2. Store listing assets
3. TestFlight/Play Console setup
4. Beta testing

---

## Appendix: Recommended Libraries

### Immediate Additions
| Library | Purpose | Effort |
|---------|---------|--------|
| Coil | Async image loading | Low |
| MapLibre | Offline maps | Medium |
| accompanist-permissions | Runtime permissions | Low |

### Future Considerations
| Library | Purpose | When |
|---------|---------|------|
| Room/SQLDelight | Local database | When needed |
| WorkManager | Background sync | v1.1 |
| Firebase Analytics | Usage tracking | Post-launch |

---

## Conclusion

The AfrikaBurn Companion has excellent architectural bones. The key to shipping as a solo developer is **ruthless prioritization**:

1. **Defer the backend** - Mobile-only MVP is fully viable
2. **Bundle content** - JSON resources are sufficient for one event
3. **Focus on core value** - Offline maps + schedule + emergency info
4. **Ship early** - Get user feedback before building community features

The app can be feature-complete for AfrikaBurn 2025 with focused effort on the essential offline capabilities that users need when they're in the desert without connectivity.
