# AfrikaBurn Companion - Mobile


## Contents

- [Build Commands](#build-commands)
- [Architecture](#architecture)
- [Important Practices](#important-practices)
- [Project-Specific Conventions](#project-specific-conventions)
  - [Dimens Object (MANDATORY)](#dimens-object-mandatory)
  - [Data Classes](#data-classes)
  - [Image Loading (Coil)](#image-loading-coil)
  - [Micro-Interactions](#micro-interactions)
  - [Skeleton Loading](#skeleton-loading)
  - [String Resource Imports](#string-resource-imports)
  - [Preview Imports](#preview-imports)
  - [Fastlane Deployment](#fastlane-deployment)
- [Platform Targets](#platform-targets)
- [General Conventions](#general-conventions)

Compose Multiplatform app (iOS + Android) for the AfrikaBurn event in the Tankwa Karoo.

## Build Commands

```bash
make pre-commit                    # Lint + test (RECOMMENDED)
make build                         # Debug APK
make install                       # Build and install on device
make test                          # Unit tests
make lint                          # Detekt analysis
make test-coverage                 # Jacoco report
```

<details>
<summary>Direct Gradle commands</summary>

```bash
./gradlew build
./gradlew composeApp:installDebug
./gradlew :composeApp:assembleReleaseXCFramework
./gradlew test
./gradlew detekt
./gradlew :composeApp:testDebugUnitTest
```
</details>

## Architecture

- `/composeApp` - Shared code (commonMain, androidMain, iosMain)
- `/iosApp` - iOS entry point
- **DI**: Koin, **Serialization**: Kotlinx with `@SerialName`
- Clean Architecture (Presentation → Domain → Data)

## Important Practices

1. **Check git branch first** before debugging any issues
2. **Run detekt + tests before every commit** - zero tolerance for violations
3. **Verify compilation** after ~5 file changes

## Project-Specific Conventions

### Dimens Object (MANDATORY)
All spacing uses `io.asterixorobelix.afrikaburn.Dimens`. NEVER hardcode dp values.

Key values: `paddingExtraSmall(4)`, `paddingSmall(8)`, `paddingMedium(16)`, `paddingLarge(24)`, `paddingExtraLarge(32)`, `spacingSmall(8)`, `spacingMedium(12)`, `spacingLarge(16)`, `sectionSpacing(20)`, `iconSizeSmall(16)`, `iconSizeMedium(24)`, `iconSizeLarge(48)`, `elevationSmall(2)`, `elevationMedium(4)`.

Add new dimensions to the existing Dimens object in Theme.kt. Never create separate dimension objects.

### Data Classes
- One data class per file in `models/` package
- Use `@Serializable` and `@SerialName` annotations
- Import models from `io.asterixorobelix.afrikaburn.models.*`
- NEVER define data classes inside UI files

### Image Loading (Coil)
Use `AppAsyncImage` variants from `io.asterixorobelix.afrikaburn.ui.components`:
- `AppAsyncImage` - standard usage
- `AppAsyncImageSimple` - when custom states not needed
- `AppAsyncImageWithState` - for custom loading/error handling
- NEVER use raw Coil `AsyncImage` directly

### Micro-Interactions
Available from `io.asterixorobelix.afrikaburn.ui.components`:
- `pressableScale` - for cards/buttons (0.96f press scale)
- `bounceClick` - for chips/toggles
- `animateSelectionScale` - for toggleable items
- Use `Dimens.animationDurationShort/Medium/Long` for timing

### Skeleton Loading
Use `ProjectListSkeleton` for list loading states, `ShimmerBox` for custom skeletons. Never use `CircularProgressIndicator` for list loading.

### String Resource Imports
```kotlin
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.*
```

### Preview Imports
```kotlin
import org.jetbrains.compose.ui.tooling.preview.Preview
import io.asterixorobelix.afrikaburn.AppTheme
```

### Fastlane Deployment
See `FASTLANE_SETUP.md` for complete setup. Quick commands via Makefile:
```bash
make deploy-internal    # Play Store Internal Testing
make deploy-beta        # Play Store Beta
make ios-beta           # TestFlight
```

## Platform Targets
- Android: Min SDK 24
- iOS: 14+

## General Conventions

For KMP/Compose conventions (MD3 theming, string resources, previews, detekt, version catalogs, architecture patterns), see `~/.claude/docs/kmp-mobile-conventions.md`.
