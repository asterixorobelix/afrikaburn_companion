# Firebase Secrets Setup for GitHub Actions


## Contents

- [Overview](#overview)
- [Setup Instructions](#setup-instructions)
  - [1. Prepare Your Firebase Configuration Files](#1-prepare-your-firebase-configuration-files)
  - [2. Encode Files to Base64](#2-encode-files-to-base64)
  - [3. Add Secrets to GitHub Repository](#3-add-secrets-to-github-repository)
  - [4. Update GitHub Actions Workflow](#4-update-github-actions-workflow)
- [Security Best Practices](#security-best-practices)
  - [✅ Do:](#do)
  - [❌ Don't:](#dont)
- [Environment-Specific Configurations](#environment-specific-configurations)
- [Troubleshooting](#troubleshooting)
  - [Common Issues:](#common-issues)
  - [Verification:](#verification)
- [Additional Resources](#additional-resources)

This document explains how to securely add Firebase configuration files (`google-services.json` and `GoogleService-Info.plist`) to GitHub Actions for CI/CD workflows.

## Overview

Firebase configuration files contain sensitive information and should never be committed directly to your repository. Instead, we use GitHub Secrets to store them securely and inject them during CI/CD builds.

## Setup Instructions

### 1. Prepare Your Firebase Configuration Files

First, ensure you have the correct Firebase configuration files:

- **Android**: `google-services.json` (downloaded from Firebase Console → Project Settings → General → Your apps → Android app)
- **iOS**: `GoogleService-Info.plist` (downloaded from Firebase Console → Project Settings → General → Your apps → iOS app)

### 2. Encode Files to Base64

GitHub Secrets work best with text content, so we need to encode the files as Base64 strings.

#### For macOS/Linux:
```bash
# Encode Android config
base64 -i path/to/google-services.json | pbcopy

# Encode iOS config  
base64 -i path/to/GoogleService-Info.plist | pbcopy
```

#### For Windows:
```powershell
# Encode Android config
[Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\google-services.json")) | Set-Clipboard

# Encode iOS config
[Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\GoogleService-Info.plist")) | Set-Clipboard
```

### 3. Add Secrets to GitHub Repository

1. Go to your GitHub repository
2. Click **Settings** tab
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click **New repository secret**
5. Add the following secrets:

| Secret Name | Description | Value |
|-------------|-------------|--------|
| `GOOGLE_SERVICES_JSON` | Android Firebase config | Base64 encoded `google-services.json` content |
| `GOOGLE_SERVICE_INFO_PLIST` | iOS Firebase config | Base64 encoded `GoogleService-Info.plist` content |

### 4. Update GitHub Actions Workflow

Add steps to your workflow files to decode and place the configuration files during builds:

#### For Android Builds (mobile-ci.yml)

```yaml
- name: Setup Firebase Config for Android
  run: |
    echo "${{ secrets.GOOGLE_SERVICES_JSON }}" | base64 --decode > mobile/composeApp/google-services.json
  
- name: Build Android
  run: |
    cd mobile
    gradle assembleDebug
```

#### For iOS Builds (mobile-ci.yml)

```yaml
- name: Setup Firebase Config for iOS  
  run: |
    echo "${{ secrets.GOOGLE_SERVICE_INFO_PLIST }}" | base64 --decode > mobile/iosApp/iosApp/GoogleService-Info.plist

- name: Build iOS
  run: |
    cd mobile/iosApp
    xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 15'
```

#### Complete Workflow Example

```yaml
name: Mobile CI

on:
  push:
    branches: [ main, develop ]
    paths: [ 'mobile/**' ]
  pull_request:
    branches: [ main ]
    paths: [ 'mobile/**' ]

jobs:
  test:
    runs-on: macos-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3
      with:
        gradle-version: 8.11.1
    
    # Setup Firebase configs
    - name: Setup Firebase Config for Android
      run: |
        mkdir -p mobile/composeApp/src/androidMain/res/values
        echo "${{ secrets.GOOGLE_SERVICES_JSON }}" | base64 --decode > mobile/composeApp/google-services.json
    
    - name: Setup Firebase Config for iOS
      run: |
        echo "${{ secrets.GOOGLE_SERVICE_INFO_PLIST }}" | base64 --decode > mobile/iosApp/iosApp/GoogleService-Info.plist
    
    # Build and test
    - name: Run tests
      run: |
        cd mobile
        gradle test
    
    - name: Build Android
      run: |
        cd mobile  
        gradle assembleDebug
    
    - name: Build iOS
      run: |
        cd mobile/iosApp
        xcodebuild -project iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 15'
```

## Security Best Practices

### ✅ Do:
- Use GitHub repository secrets for sensitive Firebase configuration
- Encode files as Base64 before storing in secrets
- Use descriptive secret names (`GOOGLE_SERVICES_JSON`, not `SECRET1`)
- Limit access to secrets by using environment-specific secrets when needed
- Regularly rotate Firebase API keys if compromised

### ❌ Don't:
- Commit `google-services.json` or `GoogleService-Info.plist` files to your repository
- Share secret values in pull requests, issues, or documentation
- Use the same Firebase project for development and production (use separate projects)
- Store secrets in workflow files or code comments

## Environment-Specific Configurations

For different environments (development, staging, production), consider using environment-specific secrets:

```yaml
# Development secrets
GOOGLE_SERVICES_JSON_DEV
GOOGLE_SERVICE_INFO_PLIST_DEV

# Staging secrets  
GOOGLE_SERVICES_JSON_STAGING
GOOGLE_SERVICE_INFO_PLIST_STAGING

# Production secrets
GOOGLE_SERVICES_JSON_PROD
GOOGLE_SERVICE_INFO_PLIST_PROD
```

Then use them conditionally in your workflow:

```yaml
- name: Setup Firebase Config
  run: |
    if [ "${{ github.ref }}" = "refs/heads/main" ]; then
      echo "${{ secrets.GOOGLE_SERVICES_JSON_PROD }}" | base64 --decode > mobile/composeApp/google-services.json
    elif [ "${{ github.ref }}" = "refs/heads/develop" ]; then
      echo "${{ secrets.GOOGLE_SERVICES_JSON_STAGING }}" | base64 --decode > mobile/composeApp/google-services.json
    else
      echo "${{ secrets.GOOGLE_SERVICES_JSON_DEV }}" | base64 --decode > mobile/composeApp/google-services.json
    fi
```

## Troubleshooting

### Common Issues:

1. **"Invalid JSON" errors**: Ensure the Base64 encoding/decoding is correct
   ```bash
   # Test locally
   echo "YOUR_BASE64_STRING" | base64 --decode > test-file.json
   cat test-file.json # Should be valid JSON
   ```

2. **"File not found" errors**: Check the file paths in your workflow match your project structure

3. **Permission errors**: Ensure the workflow has write permissions to create files

4. **Build failures**: Verify the Firebase configuration matches your app's bundle identifier/package name

### Verification:

Add a verification step to ensure files are created correctly:

```yaml
- name: Verify Firebase configs
  run: |
    ls -la mobile/composeApp/google-services.json
    ls -la mobile/iosApp/iosApp/GoogleService-Info.plist
    echo "Android config project_id:" 
    cat mobile/composeApp/google-services.json | grep project_id
```

## Additional Resources

- [GitHub Actions Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firebase iOS Setup](https://firebase.google.com/docs/ios/setup)
- [Kotlin Multiplatform Firebase Setup](https://github.com/GitLiveApp/firebase-kotlin-sdk)

---

**⚠️ Important**: Never commit this document with actual secret values. This is a template for setting up the process securely.