# Mobile CD Workflow Setup Guide


## Contents

- [📋 Prerequisites](#prerequisites)
- [🎯 Workflow Overview](#workflow-overview)
- [🔧 Repository Setup](#repository-setup)
  - [1. Enable GitHub Actions](#1-enable-github-actions)
  - [2. Configure Branch Protection (Recommended)](#2-configure-branch-protection-recommended)
- [📱 Project Structure Requirements](#project-structure-requirements)
- [🔐 Android Signing Configuration](#android-signing-configuration)
  - [Step 1: Create or Locate Android Keystore](#step-1-create-or-locate-android-keystore)
  - [Step 2: Convert Keystore to Base64](#step-2-convert-keystore-to-base64)
  - [Step 3: Add Android Secrets to GitHub](#step-3-add-android-secrets-to-github)
- [🍎 iOS Signing Configuration](#ios-signing-configuration)
  - [Step 1: Apple Developer Account Setup](#step-1-apple-developer-account-setup)
  - [Step 2: Configure App Registration](#step-2-configure-app-registration)
  - [Step 3: Add iOS Secrets to GitHub](#step-3-add-ios-secrets-to-github)
- [🔥 Firebase Configuration (Optional)](#firebase-configuration-optional)
  - [Step 1: Set up Firebase Project](#step-1-set-up-firebase-project)
  - [Step 2: Add Firebase Secrets to GitHub](#step-2-add-firebase-secrets-to-github)
- [⚙️ Project Configuration](#project-configuration)
  - [1. Update Android Configuration](#1-update-android-configuration)
  - [2. Update iOS Configuration](#2-update-ios-configuration)
  - [3. Verify Project Structure](#3-verify-project-structure)
- [🧪 Testing the Workflow](#testing-the-workflow)
  - [Step 1: Manual Workflow Trigger](#step-1-manual-workflow-trigger)
  - [Step 2: Monitor Workflow Execution](#step-2-monitor-workflow-execution)
  - [Step 3: Verify Success](#step-3-verify-success)
- [🔍 Troubleshooting](#troubleshooting)
  - [Common Issues](#common-issues)
  - [Debug Steps](#debug-steps)
- [📚 Advanced Configuration](#advanced-configuration)
  - [Custom Release Notes](#custom-release-notes)
  - [Automated Triggers](#automated-triggers)
  - [Version Strategy](#version-strategy)
- [🎉 Success Checklist](#success-checklist)
- [📖 Related Documentation](#related-documentation)

This guide provides step-by-step instructions to set up the GitHub Actions workflow (`mobile-cd.yml`) for automated mobile app continuous deployment (CD). This workflow automatically builds, signs, and releases your Kotlin Multiplatform mobile app for both Android and iOS platforms.

## 📋 Prerequisites

Before starting, ensure you have:
- [ ] GitHub repository with admin access
- [ ] Kotlin Multiplatform Mobile project in `/mobile` directory
- [ ] Android Studio (for keystore generation)
- [ ] Apple Developer account ($99/year) for iOS releases
- [ ] macOS machine (for iOS signing certificates - if doing locally)

## 🎯 Workflow Overview

The `mobile-cd.yml` workflow performs these actions:
1. **Version Management**: Automatically bumps version numbers (patch/minor/major)
2. **Android Build**: Creates signed AAB (Android App Bundle) files
3. **iOS Build**: Creates IPA files for iOS distribution
4. **Release Creation**: Publishes GitHub releases with downloadable artifacts
5. **Changelog Generation**: Auto-generates release notes from git commits

## 🔧 Repository Setup

### 1. Enable GitHub Actions

1. Navigate to your repository on GitHub
2. Go to **Settings** → **Actions** → **General**
3. Under "Actions permissions":
   - ✅ Select **"Allow all actions and reusable workflows"**
4. Under "Workflow permissions":
   - ✅ Select **"Read and write permissions"**
   - ✅ Check **"Allow GitHub Actions to create and approve pull requests"**
5. Click **Save**

### 2. Configure Branch Protection (Recommended)

1. Go to **Settings** → **Branches**
2. Click **Add rule** for your main branch (`main` or `master`)
3. Configure:
   - ✅ **Require a pull request before merging**
   - ✅ **Require status checks to pass before merging**
   - ✅ **Require branches to be up to date before merging**
   - ✅ **Require conversation resolution before merging**
4. Click **Create** or **Save changes**

## 📱 Project Structure Requirements

Your project must follow this structure for the workflow to work:
```
repository-root/
├── .github/
│   └── workflows/
│       └── mobile-cd.yml
└── mobile/
    ├── composeApp/
    │   ├── build.gradle.kts          # Contains version info
    │   └── google-services.json      # Firebase config (generated)
    ├── iosApp/
    │   ├── iosApp.xcodeproj/
    │   └── Configuration/
    │       └── Config.xcconfig        # iOS bundle ID config
    ├── gradlew
    └── settings.gradle.kts
```

## 🔐 Android Signing Configuration

### Step 1: Create or Locate Android Keystore

**Option A: Create New Keystore**
```bash
# Create a new keystore (replace with your details)
keytool -genkey -v \
  -keystore release-key.keystore \
  -alias my-key-alias \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Fill in the prompts:
# - First and last name: Your Name or Company Name
# - Organizational unit: Your Department (optional)
# - Organization: Your Company Name
# - City/Locality: Your City
# - State/Province: Your State
# - Country code: US (or your country)
# - Password: Choose a strong password
```

**Option B: Use Existing Keystore**
If you already have a keystore file (`.keystore` or `.jks`), gather this information:
- Keystore file location
- Keystore password
- Key alias name
- Key password

### Step 2: Convert Keystore to Base64

```bash
# Convert your keystore file to base64
base64 -i release-key.keystore | tr -d '\n' | pbcopy

# On Linux/Windows, use this instead:
base64 -w 0 release-key.keystore | xclip -selection clipboard
# or
certutil -encode release-key.keystore keystore.base64 && type keystore.base64 | clip
```

### Step 3: Add Android Secrets to GitHub

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret** for each of these:

| Secret Name | Example Value | Description |
|-------------|---------------|-------------|
| `ANDROID_SIGNING_KEY` | `MIIEvgIBADANBgkqhkiG9w0B...` | Base64 encoded keystore file |
| `ANDROID_KEY_ALIAS` | `my-key-alias` | The alias name from keystore |
| `ANDROID_KEYSTORE_PASSWORD` | `your_keystore_password` | Password for the keystore |
| `ANDROID_KEY_PASSWORD` | `your_key_password` | Password for the specific key |

## 🍎 iOS Signing Configuration

### Step 1: Apple Developer Account Setup

1. **Enroll in Apple Developer Program**:
   - Visit [developer.apple.com](https://developer.apple.com)
   - Enroll in the Developer Program ($99/year)
   - Complete identity verification (can take 24-48 hours)

2. **Find Your Team ID**:
   - Log into [Apple Developer Console](https://developer.apple.com/account/)
   - Go to **Membership** section
   - Copy your **Team ID** (10-character alphanumeric, e.g., `ABCD123456`)

### Step 2: Configure App Registration

1. **Create App Identifier**:
   - Go to **Certificates, Identifiers & Profiles**
   - Select **Identifiers** → **App IDs**
   - Click **+** to register new App ID
   - Enter your Bundle ID (e.g., `io.asterixorobelix.afrikaburn`)
   - Enable required capabilities (Push Notifications, etc.)

2. **Create Distribution Certificate**:
   - Go to **Certificates** → **Production**
   - Click **+** to create new certificate
   - Select **App Store and Ad Hoc**
   - Generate CSR (Certificate Signing Request):
     - Open **Keychain Access** on Mac
     - Go to **Keychain Access** → **Certificate Assistant** → **Request a Certificate From a Certificate Authority**
     - Enter your email and name, save to disk
   - Upload CSR file and download the distribution certificate
   - Double-click to install in Keychain Access

3. **Create Provisioning Profile**:
   - Go to **Profiles** → **Distribution**
   - Click **+** to create new profile
   - Select **Ad Hoc** (for testing) or **App Store** (for release)
   - Choose your App ID: `io.asterixorobelix.afrikaburn`
   - Select your distribution certificate
   - For Ad Hoc: Choose test devices (optional)
   - Download the `.mobileprovision` file

4. **Export Distribution Certificate as P12**:
   - Open **Keychain Access**
   - Find your distribution certificate
   - Right-click → **Export**
   - Save as `.p12` file with a secure password

### Step 3: Add iOS Secrets to GitHub

1. **Convert Provisioning Profile to Base64**:
   ```bash
   # Convert your downloaded .mobileprovision file
   base64 -i YourApp.mobileprovision | tr -d '\n' | pbcopy
   # This copies the base64 string to your clipboard
   ```

2. **Convert Distribution Certificate to Base64**:
   ```bash
   # Convert your exported .p12 certificate file
   base64 -i distribution_certificate.p12 | tr -d '\n' | pbcopy
   # This copies the base64 string to your clipboard
   ```

3. **Add Secrets to GitHub Repository**:
   - Go to **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret** for each:

| Secret Name | Example Value | Description |
|-------------|---------------|-------------|
| `APPLE_TEAM_ID` | `ABCD123456` | Your 10-character Apple Team ID |
| `IOS_PROVISIONING_PROFILE` | `[base64 string from step 1]` | Base64 encoded .mobileprovision file |
| `IOS_DISTRIBUTION_CERTIFICATE` | `[base64 string from step 2]` | Base64 encoded .p12 certificate |
| `IOS_CERTIFICATE_PASSWORD` | `your_p12_password` | Password for the .p12 certificate |

## 🔥 Firebase Configuration (Optional)

Firebase provides crash reporting and analytics. This is optional but recommended for production apps.

### Step 1: Set up Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new project or select existing one
3. Add Android app:
   - Enter your Android package name (from `build.gradle.kts`)
   - Download `google-services.json`
4. Add iOS app:
   - Enter your iOS Bundle ID (from `Config.xcconfig`)
   - Download `GoogleService-Info.plist`

### Step 2: Add Firebase Secrets to GitHub

```bash
# Convert Android Firebase config to base64
base64 -i google-services.json | tr -d '\n' | pbcopy

# Convert iOS Firebase config to base64
base64 -i GoogleService-Info.plist | tr -d '\n' | pbcopy
```

Add these secrets to GitHub:

| Secret Name | Description |
|-------------|-------------|
| `GOOGLE_SERVICES_JSON` | Base64 encoded `google-services.json` |
| `IOS_FIREBASE_CONFIG_PLIST` | Base64 encoded `GoogleService-Info.plist` |

## ⚙️ Project Configuration

### 1. Update Android Configuration

Edit `mobile/composeApp/build.gradle.kts`:

```kotlin
android {
    namespace = "com.yourcompany.yourapp"  // Your package name
    
    defaultConfig {
        applicationId = "com.yourcompany.yourapp"  // Same as namespace
        versionCode = 1         // Integer version (auto-incremented)
        versionName = "1.0.0"   // Semantic version (auto-bumped)
        
        // Other configuration...
    }
}
```

### 2. Update iOS Configuration

Edit `mobile/iosApp/Configuration/Config.xcconfig`:

```
BUNDLE_ID = com.yourcompany.yourapp
APP_NAME = YourAppName
MARKETING_VERSION = 1.0.0
CURRENT_PROJECT_VERSION = 1
```

### 3. Verify Project Structure

Ensure your mobile project has the correct Gradle setup:

```kotlin
// mobile/settings.gradle.kts
rootProject.name = "AfrikaBurnCompanion"
include(":composeApp")

// mobile/composeApp/build.gradle.kts should have:
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    // ... other plugins
}
```

## 🧪 Testing the Workflow

### Step 1: Manual Workflow Trigger

1. Go to your GitHub repository
2. Click **Actions** tab
3. Select **"Mobile Continuous Deployment (CD)"** workflow
4. Click **"Run workflow"** button
5. Configure the run:
   - **Branch**: `main` (or your main branch)
   - **Version bump**: `patch` (for 1.0.0 → 1.0.1)
   - **Release notes**: `"First automated release test"`
6. Click **"Run workflow"**

### Step 2: Monitor Workflow Execution

The workflow should complete these steps:
1. ✅ **Version and Build** (Ubuntu runner, ~5-10 minutes)
   - Calculate new version number
   - Update `build.gradle.kts`
   - Commit version bump
   - Create git tag
   - Build and sign Android AAB

2. ✅ **Build iOS** (macOS runner, ~10-15 minutes)
   - Build iOS framework
   - Create iOS IPA file

3. ✅ **Create Release** (Ubuntu runner, ~2-3 minutes)
   - Download all artifacts
   - Create GitHub release
   - Upload AAB and IPA files

### Step 3: Verify Success

After successful completion, you should see:
- [ ] New git tag created (e.g., `v1.0.1`)
- [ ] Version in `build.gradle.kts` updated
- [ ] GitHub release created with changelog
- [ ] Android AAB file available for download
- [ ] iOS IPA file available for download

## 🔍 Troubleshooting

### Common Issues

#### **"Permission denied" or "403 Forbidden"**
**Symptoms**: Workflow fails when trying to commit or push
**Solution**: 
- Go to **Settings** → **Actions** → **General**
- Ensure **"Read and write permissions"** is selected
- Check branch protection rules aren't blocking the workflow

#### **Android Signing Failed**
**Symptoms**: `r0adkll/sign-android-release` step fails
**Solutions**:
- Verify all Android secrets are correctly set in GitHub
- Test keystore locally: `keytool -list -v -keystore your-keystore.jks`
- Ensure passwords match exactly (no extra spaces)
- Verify base64 encoding was done correctly

#### **iOS Build Failed**
**Symptoms**: iOS build step fails with code signing errors
**Solutions**:
- Verify `APPLE_TEAM_ID` secret is correct (10 characters)
- Ensure bundle ID in `Config.xcconfig` matches Apple Developer setup
- Check that iOS app is registered in Apple Developer Console
- Verify Apple Developer account is active and paid

#### **Version Parsing Error**
**Symptoms**: Version calculation step fails
**Solutions**:
- Ensure `versionName` follows semantic versioning: `"1.0.0"`
- Format must be exactly: `MAJOR.MINOR.PATCH` (three numbers with dots)
- Check for extra quotes or spaces in `build.gradle.kts`

#### **Gradle Build Failed**
**Symptoms**: Android or iOS build steps fail
**Solutions**:
- Test builds locally:
  ```bash
  cd mobile
  ./gradlew :composeApp:bundleRelease    # Android
  ./gradlew :composeApp:assembleReleaseXCFramework  # iOS framework
  ```
- Check for compilation errors in your code
- Verify all dependencies are correctly configured

#### **Firebase Configuration Issues**
**Symptoms**: Build succeeds but Firebase features don't work
**Solutions**:
- Verify base64 encoding of Firebase config files
- Ensure package names match between project and Firebase
- Check Firebase project has both Android and iOS apps configured

### Debug Steps

1. **Check Workflow Logs**:
   - Go to **Actions** tab → Select failed workflow run
   - Click on the failed job name
   - Expand failed steps to see detailed error messages

2. **Validate Secrets**:
   - Go to **Settings** → **Secrets and variables** → **Actions**
   - Ensure all required secrets are present (values are hidden but names should show)

3. **Test Local Builds**:
   ```bash
   # Test Android release build
   cd mobile
   ./gradlew clean
   ./gradlew :composeApp:bundleRelease
   
   # Test iOS framework build
   ./gradlew :composeApp:assembleReleaseXCFramework
   
   # Check if Firebase is properly configured
   ls -la composeApp/google-services.json
   ```

4. **Verify Project Configuration**:
   ```bash
   # Check current version in build.gradle.kts
   grep -n "versionName\|versionCode" mobile/composeApp/build.gradle.kts
   
   # Check iOS bundle configuration
   cat mobile/iosApp/Configuration/Config.xcconfig
   ```

## 📚 Advanced Configuration

### Custom Release Notes

You can provide custom release notes when triggering the workflow:
```
## Features
- Added new user authentication
- Improved app performance

## Bug Fixes
- Fixed crash on startup
- Resolved memory leak issues
```

### Automated Triggers

To trigger the workflow automatically, modify the `on:` section in `mobile-cd.yml`:

```yaml
on:
  # Keep manual trigger
  workflow_dispatch:
    # ... existing inputs

  # Add automatic triggers
  push:
    branches: [ main ]
    paths: [ 'mobile/**' ]  # Only trigger when mobile code changes
    
  # Or trigger on schedule (e.g., weekly releases)
  schedule:
    - cron: '0 10 * * 1'  # Every Monday at 10:00 AM UTC
```

### Version Strategy

The workflow supports three types of version bumps:
- **Patch** (1.0.0 → 1.0.1): Bug fixes and small improvements
- **Minor** (1.0.0 → 1.1.0): New features, backward compatible
- **Major** (1.0.0 → 2.0.0): Breaking changes, major features

## 🎉 Success Checklist

After successful setup, you should be able to:
- [ ] Manually trigger the Mobile CD workflow from GitHub Actions
- [ ] See version numbers automatically increment in `build.gradle.kts`
- [ ] Have git tags created for each release (e.g., `v1.0.1`)
- [ ] Download signed Android AAB files from GitHub releases
- [ ] Download iOS IPA files from GitHub releases
- [ ] View auto-generated changelogs in GitHub releases
- [ ] See Firebase crash reports (if configured)

## 📖 Related Documentation

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android App Signing Guide](https://developer.android.com/studio/publish/app-signing)
- [iOS App Distribution Guide](https://developer.apple.com/documentation/xcode/distributing-your-app-for-beta-testing-and-releases)
- [Kotlin Multiplatform Mobile](https://kotlinlang.org/docs/multiplatform.html)
- [Firebase Setup Guide](https://firebase.google.com/docs/android/setup)
- [Semantic Versioning](https://semver.org/)

---

**Next Steps**: Once your CD pipeline is working, consider setting up:
- Automated testing before releases
- Beta distribution channels
- App Store/Google Play Store integration
- Slack/Discord notifications for releases