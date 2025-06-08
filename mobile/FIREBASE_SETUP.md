# 🔥 Firebase Crashlytics Setup Guide

⚠️ **IMPORTANT**: This project includes a default `google-services.json` template that allows compilation but **Crashlytics will NOT work** until you set up your own Firebase project.

## 🚀 Quick Setup

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Create a project" or use existing project
3. Follow the setup wizard

### 2. Add Android App
1. In your Firebase project, click "Add app" → Android
2. **Package name**: Use your actual package name (e.g., `com.yourcompany.yourapp`)
3. **App nickname**: Your app name (optional)
4. **Debug signing certificate**: Not required for Crashlytics

### 3. Download Configuration
1. Download the `google-services.json` file
2. Replace the existing file at `mobile/composeApp/google-services.json`

### 4. Enable Crashlytics
1. In Firebase Console, go to **Crashlytics** in the left menu
2. Click "Get started"
3. Follow the setup instructions (dependencies are already added to this project)

### 5. Add iOS App (Optional)
For iOS crash reporting:
1. Add iOS app to your Firebase project
2. **Bundle ID**: Same as your Android package name
3. Download `GoogleService-Info.plist`
4. Add to `mobile/iosApp/iosApp/GoogleService-Info.plist`

## 🧪 Testing

### Verify Setup
```bash
# Build the app
cd mobile
./gradlew composeApp:assembleDebug

# Install and run on device/emulator
./gradlew composeApp:installDebug
```

### Test Crash Reporting
Add this to your app for testing (DEBUG builds only):
```kotlin
// In your Composable
val crashLogger: CrashLogger = koinInject()

// Test button (remove in production)
Button(onClick = { 
    crashLogger.testCrash() 
}) {
    Text("Test Crash (Debug Only)")
}
```

## ⚠️ Current Status

- **Default Template**: ❌ Crashlytics disabled
- **Your Firebase Config**: ✅ Crashlytics enabled

## 🔧 Troubleshooting

### Build Errors
- Ensure package name in `google-services.json` matches your app's package name
- Verify the file is in the correct location: `mobile/composeApp/google-services.json`

### Crashlytics Not Working
- Check Firebase Console → Crashlytics for incoming data
- Ensure you've enabled Crashlytics in Firebase Console
- Verify internet connection on test device
- It may take a few minutes for first crashes to appear

### Missing Dependencies
If you see Firebase-related build errors:
```bash
# Clean and rebuild
./gradlew clean
./gradlew composeApp:assembleDebug
```

## 📱 Production Deployment

For production releases:
1. ✅ Replace default `google-services.json` with your project's configuration
2. ✅ Enable Crashlytics in Firebase Console
3. ✅ Test crash reporting on staging builds
4. ✅ Monitor Firebase Console for crash reports

---

**Need help?** Check the [Firebase Documentation](https://firebase.google.com/docs/android/setup) or [Crashlytics Guide](https://firebase.google.com/docs/crashlytics/get-started)