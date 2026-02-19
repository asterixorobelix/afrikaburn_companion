# Mobile CD Pipeline Setup Instructions

This guide walks you through setting up the GitHub repository to enable the mobile-cd.yml workflow for automated mobile app releases.

## 📋 Prerequisites

Before starting, ensure you have:
- [ ] A GitHub repository with the mobile project
- [ ] Android signing certificate (keystore file)
- [ ] Apple Developer account (for iOS signing)
- [ ] Repository admin access to configure secrets

## 🔧 Repository Configuration

### 1. Enable GitHub Actions

1. Go to your repository on GitHub
2. Click **Settings** tab
3. Navigate to **Actions** → **General** (left sidebar)
4. Under "Actions permissions", select:
   - ✅ **Allow all actions and reusable workflows**
5. Under "Workflow permissions", select:
   - ✅ **Read and write permissions**
   - ✅ **Allow GitHub Actions to create and approve pull requests**
6. Click **Save**

### 2. Configure Branch Protection (Recommended)

1. Go to **Settings** → **Branches**
2. Click **Add rule** for `main` branch
3. Configure:
   - ✅ **Require a pull request before merging**
   - ✅ **Require status checks to pass before merging**
   - ✅ **Require branches to be up to date before merging**
4. Click **Create** or **Save changes**

## 🔐 Android Signing Setup

### Step 1: Prepare Android Keystore

If you don't have a keystore, create one:

```bash
# Create a new keystore (replace values with your info)
keytool -genkey -v \
  -keystore release-key.keystore \
  -alias my-key-alias \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Answer the prompts with your information
```

If you have an existing keystore, locate these files:
- `your-release-key.keystore` (or `.jks`)
- Note your keystore password
- Note your key alias name
- Note your key password

### Step 2: Convert Keystore to Base64

```bash
# Convert keystore to base64 string
base64 -i release-key.keystore | tr -d '\n' | pbcopy

# This copies the base64 string to your clipboard
# Save this string - you'll need it for GitHub secrets
```

### Step 3: Add Android Secrets to GitHub

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret** for each:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `ANDROID_SIGNING_KEY` | `[base64 string from Step 2]` | Base64 encoded keystore file |
| `ANDROID_KEY_ALIAS` | `my-key-alias` | Your key alias name |
| `ANDROID_KEYSTORE_PASSWORD` | `your_keystore_password` | Keystore password |
| `ANDROID_KEY_PASSWORD` | `your_key_password` | Key password |

## 🍎 iOS Signing Setup

### Step 1: Apple Developer Account Setup

1. **Enroll in Apple Developer Program**:
   - Go to [developer.apple.com](https://developer.apple.com)
   - Enroll in the Developer Program ($99/year)
   - Complete verification process

2. **Find Your Team ID**:
   - Log into [Apple Developer Portal](https://developer.apple.com/account/)
   - Go to **Membership** section
   - Copy your **Team ID** (10-character alphanumeric)

### Step 2: App ID and Certificates

1. **Create App ID**:
   - Go to **Certificates, Identifiers & Profiles**
   - Select **Identifiers** → **App IDs**
   - Click **+** to create new App ID
   - Use your app's bundle ID (e.g., `com.yourcompany.yourapp`)

2. **Create Distribution Certificate**:
   - Go to **Certificates** → **Production**
   - Click **+** to create new certificate
   - Select **App Store and Ad Hoc**
   - Follow instructions to generate and download

3. **Create Provisioning Profile**:
   - Go to **Profiles** → **Distribution**
   - Click **+** to create new profile
   - Select **App Store** distribution
   - Choose your App ID and certificate
   - Download the provisioning profile

### Step 3: Add iOS Secrets to GitHub

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `APPLE_TEAM_ID` | `ABCD123456` | Your 10-character Apple Team ID |

## 📱 Project Configuration

### Update iOS Bundle Identifier

1. Open `mobile/iosApp/Configuration/Config.xcconfig`
2. Update the bundle ID:
   ```
   BUNDLE_ID = com.yourcompany.yourapp
   APP_NAME = YourAppName
   ```

### Update Android Application ID

1. Open `mobile/composeApp/build.gradle.kts`
2. Update the application ID:
   ```kotlin
   android {
       namespace = "com.yourcompany.yourapp"
       
       defaultConfig {
           applicationId = "com.yourcompany.yourapp"
           // ... other config
       }
   }
   ```

### Set Initial Version

Ensure your `mobile/composeApp/build.gradle.kts` has proper versioning:

```kotlin
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
        // ... other config
    }
}
```

## 🧪 Testing the Workflow

### Test Workflow Dispatch

1. Go to your repository on GitHub
2. Click **Actions** tab
3. Select **Mobile Continuous Deployment** workflow
4. Click **Run workflow** button
5. Select:
   - Branch: `main`
   - Version bump: `patch`
   - Release notes: `Test release`
6. Click **Run workflow**

### Verify Workflow Execution

The workflow should:
1. ✅ Bump version from `1.0.0` → `1.0.1`
2. ✅ Create git tag `v1.0.1`
3. ✅ Generate changelog
4. ✅ Build Android AAB
5. ✅ Build iOS IPA
6. ✅ Create GitHub release with artifacts
7. ✅ Capture and upload store screenshots (Android + iOS)

## 📸 Store Screenshots (CI)

The CD workflow captures screenshots in CI and uploads them to the stores using fastlane.

### Android (Google Play)

- Screenshot capture is driven by `fastlane android screenshots` (Screengrab).
- Images are stored under:
  - `mobile/fastlane/metadata/android/en-US/images/phoneScreenshots/`
- Ensure the instrumentation test `ScreengrabScreenshotTest` stays up to date with the screens you want on the store listing.

### iOS (App Store Connect)

- Screenshots are uploaded via `fastlane ios upload_screenshots`.
- Fastlane expects screenshots at:
  - `mobile/fastlane/screenshots/`
- If you want to capture them in CI, use `fastlane ios screenshots` on a macOS runner.

### Required Secrets (Screenshots + Uploads)

The CD workflow requires these secrets to upload builds and screenshots:

- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` (base64 JSON content)
- `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_KEY_CONTENT` (App Store Connect API key)

## 🔍 Troubleshooting

### Common Issues

#### **"Permission denied" Error**
- **Solution**: Check that workflow permissions are set to "Read and write"
- **Location**: Settings → Actions → General → Workflow permissions

#### **Android Signing Failed**
- **Check**: Verify all Android secrets are correctly set
- **Test**: Ensure keystore passwords are correct
- **Verify**: Confirm base64 encoding was done correctly

#### **iOS Build Failed**
- **Check**: Verify Apple Team ID is correct
- **Ensure**: Bundle ID matches your Apple Developer setup
- **Confirm**: You have valid Apple Developer membership

#### **Version Parsing Error**
- **Check**: Ensure `versionName` in build.gradle.kts follows semantic versioning (e.g., "1.0.0")
- **Format**: Must be MAJOR.MINOR.PATCH (three numbers separated by dots)

#### **Git Push Failed**
- **Solution**: Ensure the workflow has write permissions to the repository
- **Check**: Branch protection rules aren't blocking the workflow

### Debug Steps

1. **Check workflow logs**:
   - Go to Actions tab → Failed workflow → Click on failed job
   - Expand failed steps to see detailed error messages

2. **Validate secrets**:
   - Go to Settings → Secrets and variables → Actions
   - Ensure all required secrets are present (values are hidden)

3. **Test local builds**:
   ```bash
   # Test Android build locally
   cd mobile
   ./gradlew :composeApp:bundleRelease
   
   # Check if iOS framework builds
   ./gradlew :composeApp:assembleReleaseXCFramework
   ```

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [iOS App Distribution](https://developer.apple.com/documentation/xcode/distributing-your-app-for-beta-testing-and-releases)
- [Semantic Versioning](https://semver.org/)

## 🎉 Success Checklist

After setup, you should be able to:
- [ ] Manually trigger the Mobile CD workflow
- [ ] See version numbers automatically increment
- [ ] Have git tags created for each release
- [ ] Download signed AAB and IPA files from GitHub releases
- [ ] View auto-generated changelogs in releases

---

**Next Steps**: Once everything is working, you can integrate this with your development workflow by triggering releases when PRs are merged to main, or on a scheduled basis.
