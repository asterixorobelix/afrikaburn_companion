# 🚀 GitHub Repository Setup Guide


## Contents

- [📋 Prerequisites](#prerequisites)
- [🔧 Repository Configuration](#repository-configuration)
  - [1. Basic Repository Setup](#1-basic-repository-setup)
  - [2. Firebase Integration](#2-firebase-integration)
  - [3. Android Release Signing (Production)](#3-android-release-signing-production)
  - [4. iOS Release Setup (Production)](#4-ios-release-setup-production)
- [🔄 CI/CD Pipeline Overview](#cicd-pipeline-overview)
  - [Continuous Integration (CI)](#continuous-integration-ci)
  - [Continuous Deployment (CD)](#continuous-deployment-cd)
- [⚙️ Workflow Configuration](#workflow-configuration)
  - [Mobile CI Workflow Features](#mobile-ci-workflow-features)
  - [Mobile CD Workflow Features](#mobile-cd-workflow-features)
- [🔐 Security Configuration](#security-configuration)
  - [Required Secrets](#required-secrets)
  - [Optional Secrets](#optional-secrets)
- [🚀 Using the Workflows](#using-the-workflows)
  - [Running CI (Automatic)](#running-ci-automatic)
  - [Running CD (Manual)](#running-cd-manual)
- [📊 Understanding Results](#understanding-results)
  - [CI Results](#ci-results)
  - [CD Results](#cd-results)
- [🔧 Troubleshooting](#troubleshooting)
  - [Common Issues](#common-issues)
  - [Getting Help](#getting-help)
- [🎯 Quick Start Checklist](#quick-start-checklist)
- [🤖 Automation Benefits](#automation-benefits)

Complete guide for setting up your GitHub repository with Firebase Crashlytics, CI/CD, and automated releases.

## 📋 Prerequisites

- [x] GitHub account with repository access
- [x] Firebase project created ([Firebase Console](https://console.firebase.google.com))
- [x] Android app added to Firebase project
- [x] (Optional) iOS app added to Firebase project

## 🔧 Repository Configuration

### 1. Basic Repository Setup

1. **Create Repository** (if using as template):
   - Click "Use this template" → "Create a new repository"
   - Choose repository name and visibility
   - Clone to your local machine

2. **Run Setup Script**:
   ```bash
   chmod +x setup.sh
   ./setup.sh
   ```
   - Follow prompts to customize package name and project details

### 2. Firebase Integration

#### 🔥 Android Firebase Setup

1. **Download Configuration**:
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Select your project → Project Settings
   - In "Your apps" section, click Android app
   - Download `google-services.json`

2. **Add as Repository Secret**:
   - GitHub Repository → Settings → Secrets and variables → Actions
   - Click "New repository secret"
   - **Name**: `FIREBASE_CONFIG_JSON`
   - **Value**: Copy entire contents of `google-services.json` file
   - Click "Add secret"

#### 🍎 iOS Firebase Setup (Optional)

1. **Download iOS Configuration**:
   - In Firebase Console, click iOS app (if added)
   - Download `GoogleService-Info.plist`

2. **Add as Repository Secret**:
   - **Name**: `IOS_FIREBASE_CONFIG_PLIST`
   - **Value**: Copy entire contents of `GoogleService-Info.plist` file

### 3. Android Release Signing (Production)

For production releases, add Android signing secrets:

1. **Generate Keystore** (if you don't have one):
   ```bash
   keytool -genkey -v -keystore release-key.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Add Repository Secrets**:
   - `ANDROID_SIGNING_KEY`: Base64 encoded keystore file
     ```bash
     base64 release-key.keystore | tr -d '\n'
     ```
   - `ANDROID_KEY_ALIAS`: Your key alias (e.g., "release")
   - `ANDROID_KEYSTORE_PASSWORD`: Keystore password
   - `ANDROID_KEY_PASSWORD`: Key password

### 4. iOS Release Setup (Production)

For iOS App Store releases:

1. **Add Repository Secrets**:
   - `APPLE_TEAM_ID`: Your Apple Developer Team ID
   - Additional iOS signing certificates as needed

## 🔄 CI/CD Pipeline Overview

### Continuous Integration (CI)

**Triggers**: Push to main/develop, Pull Requests
**Features**:
- ✅ Automated testing with detailed reporting
- ✅ Code quality analysis (Detekt)
- ✅ Multi-platform builds (Android APK + iOS XCFramework)
- ✅ Firebase configuration validation
- ✅ Comprehensive PR comments with results
- ✅ Downloadable test and quality reports

### Continuous Deployment (CD)

**Trigger**: Manual workflow dispatch
**Features**:
- 🚀 Automated version bumping (patch/minor/major)
- 📱 Production Android AAB generation
- 🍎 iOS IPA building
- 🔥 Firebase Crashlytics integration for releases
- 📋 Automated changelog generation
- 🏷️ Git tagging and GitHub releases

## ⚙️ Workflow Configuration

### Mobile CI Workflow Features

1. **Firebase Configuration Check**:
   - Validates Firebase setup for both Android and iOS
   - Reports configuration status in build logs
   - Works with both default template and production configs

2. **Test Execution**:
   - Runs all unit tests with detailed reporting
   - Generates test coverage reports
   - Comments PR with test results and links to artifacts

3. **Code Quality**:
   - Detekt static analysis with mobile-specific rules
   - Reports code quality issues in PR comments
   - Provides actionable feedback for improvements

### Mobile CD Workflow Features

1. **Automated Version Management**:
   - Semantic versioning (patch/minor/major)
   - Automatic version code incrementing
   - Git tagging with release notes

2. **Production Builds**:
   - Uses Firebase secrets for production Crashlytics
   - Generates signed Android AAB for Play Store
   - Builds iOS IPA for App Store distribution

3. **Release Management**:
   - Creates GitHub releases with changelogs
   - Uploads build artifacts
   - Notifies completion with release URLs

## 🔐 Security Configuration

### Required Secrets

| Secret Name | Description | Required For |
|-------------|-------------|--------------|
| `FIREBASE_CONFIG_JSON` | Android Firebase configuration | Production Crashlytics |
| `IOS_FIREBASE_CONFIG_PLIST` | iOS Firebase configuration | iOS Crashlytics |
| `ANDROID_SIGNING_KEY` | Base64 encoded Android keystore | Play Store releases |
| `ANDROID_KEY_ALIAS` | Android key alias | Play Store releases |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password | Play Store releases |
| `ANDROID_KEY_PASSWORD` | Key password | Play Store releases |
| `APPLE_TEAM_ID` | Apple Developer Team ID | iOS releases |

### Optional Secrets

- Additional iOS signing certificates
- Custom build configurations
- Third-party service API keys

## 🚀 Using the Workflows

### Running CI (Automatic)

CI runs automatically on:
- Push to `main` or `develop` branches
- Pull requests to `main` branch

**What you get**:
- Automated test execution and reporting
- Code quality analysis
- Build verification for both platforms
- PR comments with detailed results
- Downloadable artifacts with HTML reports

### Running CD (Manual)

1. **Navigate to Actions**:
   - GitHub Repository → Actions tab
   - Select "Mobile Continuous Deployment (CD)"

2. **Trigger Release**:
   - Click "Run workflow"
   - Choose version bump type: patch, minor, or major
   - Add optional release notes
   - Click "Run workflow"

3. **Monitor Progress**:
   - Watch workflow execution in real-time
   - Download built artifacts from completed workflow
   - Check GitHub Releases for published release

## 📊 Understanding Results

### CI Results

**In Pull Request Comments**:
- 🧪 Test results with pass/fail counts
- 🔍 Code quality analysis with issue counts
- 📎 Links to downloadable detailed reports
- 🔧 Action items for fixing issues

**In GitHub Actions Summary**:
- Detailed build logs
- Firebase configuration status
- Platform-specific build results

### CD Results

**After Successful Release**:
- 🏷️ New Git tag created
- 📋 GitHub release with changelog
- 📱 Android AAB ready for Play Store
- 🍎 iOS IPA ready for App Store
- 🔥 Crashlytics enabled with production config

## 🔧 Troubleshooting

### Common Issues

1. **Firebase Configuration Errors**:
   - Verify secret names match exactly
   - Check JSON/PLIST format is valid
   - Ensure Firebase project has correct package names

2. **Build Failures**:
   - Check build logs in Actions tab
   - Verify all required secrets are set
   - Ensure code passes local tests before pushing

3. **Signing Issues**:
   - Verify Android keystore is properly base64 encoded
   - Check iOS certificates and provisioning profiles
   - Ensure team IDs and aliases are correct

### Getting Help

- Check the [Actions tab](../../actions) for detailed logs
- Review [mobile CI/CD documentation](mobile/CLAUDE.md)
- Verify [Firebase setup](mobile/FIREBASE_SETUP.md)

## 🎯 Quick Start Checklist

- [ ] Repository created from template
- [ ] Setup script executed with custom package name
- [ ] Firebase project created and configured
- [ ] `FIREBASE_CONFIG_JSON` secret added
- [ ] First push triggers CI successfully
- [ ] (Optional) iOS Firebase config added
- [ ] (Production) Android signing secrets configured
- [ ] CD workflow tested successfully

---

## 🤖 Automation Benefits

With this setup, you get:

- **Zero-configuration CI/CD** that works out of the box
- **Professional PR reviews** with automated feedback
- **Production-ready releases** with one click
- **Firebase Crashlytics** for real-time crash monitoring
- **Multi-platform builds** for both Android and iOS
- **Comprehensive reporting** with downloadable artifacts

**Ready to scale from development to production!** 🚀