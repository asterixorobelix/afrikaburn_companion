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
```bash
# Build the entire project
./gradlew build

# Run tests
./gradlew test

# Run code quality analysis
./gradlew detekt

# Combined quality check
./gradlew test detekt

# Build Android APK
./gradlew composeApp:assembleDebug

# Build iOS XCFramework
./gradlew :composeApp:assembleReleaseXCFramework
```

### Platform-Specific
```bash
# Android installation
./gradlew composeApp:installDebug

# iOS (after XCFramework build)
# Open iosApp/iosApp.xcodeproj in Xcode and run
```

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

## 📁 Project Structure

```
mobile/
├── composeApp/          # Shared code
│   ├── commonMain/      # Platform-agnostic code
│   ├── androidMain/     # Android-specific code
│   └── iosMain/         # iOS-specific code
└── iosApp/              # iOS app entry point
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