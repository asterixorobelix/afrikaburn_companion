# Mobile Kotlin Multiplatform Project

Compose Multiplatform mobile app targeting Android and iOS platforms with Firebase Crashlytics integration.

![Platform](https://img.shields.io/badge/Platform-iOS%20%7C%20Android-blue)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)
![Compose](https://img.shields.io/badge/UI-Compose%20Multiplatform-green)
![Firebase](https://img.shields.io/badge/Crash%20Reporting-Firebase%20Crashlytics-orange)

## 🚀 Quick Start

### Prerequisites Setup

**🔥 Firebase Crashlytics Configuration:**

⚠️ **IMPORTANT**: This project includes a default `google-services.json` template that allows compilation but **Crashlytics will NOT work** until you set up your Firebase project.

**Quick Setup:**
1. **Create Firebase Project**: Go to [Firebase Console](https://console.firebase.google.com)
2. **Add Android App**: Use your package name (e.g., `com.example.myproject`)
3. **Download Config**: Replace `composeApp/google-services.json` with your downloaded file
4. **Enable Crashlytics**: In Firebase Console → Crashlytics → Get started

**Detailed Instructions**: See [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for complete setup guide.

**Current Status**: 
- ✅ **Compiles**: Uses default template for development
- ❌ **Crashlytics**: Disabled until you add your Firebase config

### Development Commands

This project includes a **Makefile** for simplified commands. Run `make help` to see all options.

```bash
# Quick start
make setup              # Install all dependencies
make test               # Run unit tests
make build              # Build debug APK
make install            # Install on connected device

# Quality checks
make lint               # Run detekt analysis
make pre-commit         # Run all checks (lint + test)
make test-coverage      # Run tests with coverage report

# iOS
make ios-framework      # Build XCFramework
make ios-open           # Open Xcode project
```

**VS Code / Cursor Users**: Press `Cmd+Shift+B` (build) or `Cmd+Shift+P` → "Tasks: Run Task" to access all commands.

<details>
<summary>Direct Gradle commands (without Make)</summary>

```bash
# Build
./gradlew build
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# Test
./gradlew :composeApp:testDebugUnitTest
./gradlew :composeApp:jacocoTestReport

# Quality
./gradlew detekt

# iOS
./gradlew :composeApp:assembleReleaseXCFramework
```
</details>

## 🔄 CI/CD Integration

This project includes comprehensive automated testing and code quality analysis:

- ✅ **Automated testing** on every pull request
- ✅ **Code quality analysis** with Detekt (mobile-specific rules)
- ✅ **Multi-platform builds** (Android APK + iOS XCFramework)
- ✅ **Comprehensive PR reporting** with test results and artifact links
- ✅ **7-day artifact retention** for detailed analysis

### What You Get in PRs
- Test result summaries with pass/fail breakdowns
- Code quality feedback with mobile-specific guidance
- Direct links to detailed HTML reports
- Performance monitoring (APK size tracking)
- Clear action items for any issues found

## 🚀 Fastlane Deployment

This project uses **Fastlane** for automated deployments to Google Play Store and Apple App Store.

### Quick Start
```bash
# Install dependencies
make setup

# Configure credentials
cp fastlane/.env.default fastlane/.env
# Edit fastlane/.env with your credentials

# Verify setup
make check-env
```

### Common Commands

| Task | Command |
|------|---------|
| Run tests | `make test` |
| Build debug APK | `make build-debug` |
| Deploy to Internal Testing | `make deploy-internal` |
| Deploy to Beta | `make deploy-beta` |
| Deploy to Production | `make deploy-release` |
| iOS TestFlight | `make ios-beta` |
| iOS App Store | `make ios-release` |

### What Fastlane Automates
- **Version bumping** - Automatic version code increment
- **Code signing** - Match handles iOS certificates automatically
- **Build & upload** - One command for complete deployment
- **Release notes** - Generated from git commits

### Setup Requirements
- **Android**: Keystore file + Play Store service account
- **iOS**: Apple Developer account + private certificate repo

📖 **Full setup guide**: [FASTLANE_SETUP.md](FASTLANE_SETUP.md)

## 📁 Project Structure

```
mobile/
├── composeApp/          # Shared code
│   ├── commonMain/      # Platform-agnostic code
│   ├── androidMain/     # Android-specific code
│   └── iosMain/         # iOS-specific code
├── iosApp/              # iOS app entry point
├── fastlane/            # Deployment automation
│   ├── Fastfile         # Lane definitions
│   ├── Appfile          # App identifiers
│   ├── Matchfile        # iOS code signing
│   └── .env.default     # Environment template
├── .vscode/             # VS Code / Cursor configuration
│   └── tasks.json       # Build and test tasks
└── Makefile             # Simplified commands
```

## 🛠️ Development

### Code Quality
- **Detekt**: Static analysis with mobile-specific rules
- **Custom rules**: Compose function naming, magic number detection
- **Formatting**: Automated via IDE or `detekt` with formatting rules

### Testing
- **Unit tests**: `./gradlew test`
- **Coverage**: Tracked and reported in CI
- **Multi-platform**: Tests run on all target platforms

### Architecture
- **Clean Architecture**: Separation of concerns
- **Compose Multiplatform**: Shared UI components
- **Koin**: Dependency injection
- **MVVM**: ViewModel pattern with Compose State
- **Firebase Crashlytics**: Cross-platform crash reporting

## 🔥 Firebase Crashlytics Features

### Crash Reporting
- **Real-time crash alerts** for both Android and iOS
- **Automatic symbolication** for readable stack traces
- **Custom key-value pairs** for debugging context
- **Non-fatal exception tracking** for handled errors

### Usage Example
```kotlin
@Composable
fun MyScreen() {
    val crashLogger: CrashLogger = koinInject()
    
    LaunchedEffect(Unit) {
        crashLogger.setCustomKey("screen", "MyScreen")
    }
    
    try {
        // Your UI code
    } catch (e: Exception) {
        crashLogger.logException(e, "Error in MyScreen")
        // Show user-friendly error message
    }
}
```

### Development vs Production
- **Development**: Uses template configuration for basic logging
- **Production**: Full Firebase integration with real-time dashboard
- **Testing**: Includes test crash functionality for validation

---

*For detailed development guidelines, see [CLAUDE.md](CLAUDE.md)*