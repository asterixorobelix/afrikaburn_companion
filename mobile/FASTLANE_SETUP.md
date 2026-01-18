# Fastlane Setup Guide for AfrikaBurn Companion

This guide walks you through setting up Fastlane for automated deployments of the AfrikaBurn Companion app to both Google Play Store and Apple App Store.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Android Setup](#android-setup)
- [iOS Setup](#ios-setup)
- [Daily Usage](#daily-usage)
- [CI/CD Integration](#cicd-integration)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
```bash
# macOS (required for iOS builds)
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Ruby (Fastlane is built with Ruby)
brew install ruby

# Install Fastlane
brew install fastlane

# Or install via gem (alternative)
gem install fastlane
```

### Required Accounts
- **Google Play Console** account with your app registered
- **Apple Developer Program** membership ($99/year)
- **App Store Connect** access for your app

---

## Quick Start

### 1. Install Dependencies
```bash
cd mobile

# Install Ruby dependencies
bundle install

# Verify installation
bundle exec fastlane --version
```

### 2. Configure Environment Variables
```bash
# Copy the template
cp fastlane/.env.default fastlane/.env

# Edit with your values
nano fastlane/.env  # or use your preferred editor
```

### 3. Verify Setup
```bash
# Check all environment variables are configured
bundle exec fastlane check_env
```

### 4. Run Your First Build
```bash
# Android debug build (no signing required)
bundle exec fastlane android build_debug

# Run tests
bundle exec fastlane android test
```

---

## Android Setup

### Step 1: Create a Keystore (First Time Only)

If you don't have a keystore yet:

```bash
# Create a new keystore for signing release builds
keytool -genkey -v \
  -keystore afrikaburn-release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias afrikaburn

# Store this file securely! You'll need it for all future releases.
# Recommended: Store in a secure location outside the repo
```

**Important:** Keep your keystore file and passwords secure. If you lose them, you cannot update your app on Play Store.

### Step 2: Configure Keystore in Environment

Add to `fastlane/.env`:
```bash
KEYSTORE_PATH=/path/to/afrikaburn-release.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=afrikaburn
KEY_PASSWORD=your_key_password
```

### Step 3: Set Up Google Play Console API Access

1. Go to [Google Cloud Console - Create Service Account](https://console.cloud.google.com/iam-admin/serviceaccounts/create?previousPage=%2Fapis%2Fapi%2Fandroidpublisher.googleapis.com%2Fcredentials&project=api-8884311451644643561-193695)
2. Create a service account:
   - Name: `fastlane-deploy`
   - Description: `Fastlane deployment for AfrikaBurn`
   - Click **"CREATE AND CONTINUE"**
   - Skip the role selection (click **"CONTINUE"**)
   - Click **"DONE"**
3. Create JSON key:
   - Click on the service account you just created
   - Go to **"Keys"** tab
   - Click **"ADD KEY"** → **"Create new key"**
   - Select **JSON** format → Click **"CREATE"**
   - This downloads the JSON file
4. Grant permissions in [Google Play Console](https://play.google.com/console):
   - Go to **Users & Permissions** in the left sidebar
   - Click **"Invite new users"**
   - Enter your service account email (find it in the JSON file under `"client_email"`)
   - Under **App permissions**, select the AfrikaBurn app
   - Enable: **Release to production** and **Release apps to testing tracks**
   - Click **"Invite user"** → **"Send invite"**
5. Save the JSON file as `fastlane/play-store-credentials.json`

```bash
# Update your .env
PLAY_STORE_JSON_KEY=fastlane/play-store-credentials.json
```

### Step 4: Test Android Deployment

```bash
# First, ensure your app is already uploaded manually to Play Console
# (Fastlane can't create new apps, only update existing ones)

# Test internal deployment
bundle exec fastlane android internal
```

---

## iOS Setup

### Step 1: Configure Apple Developer Account

Add to `fastlane/.env`:
```bash
APPLE_ID=your-apple-id@email.com
APPLE_TEAM_ID=ABC123XYZ  # Find at developer.apple.com -> Membership
ITC_TEAM_ID=123456789    # Find at appstoreconnect.apple.com -> Users and Access
```

### Step 2: Set Up Match (Code Signing)

Match stores your certificates in a private git repository, eliminating code signing headaches.

#### 2a. Create a Private Repository

Create a **private** git repository for certificates:
- GitHub: `github.com/yourusername/afrikaburn-certificates`
- This repo will store encrypted certificates and provisioning profiles

#### 2b. Configure Match

Update `fastlane/.env`:
```bash
MATCH_GIT_URL=git@github.com:yourusername/afrikaburn-certificates.git
MATCH_PASSWORD=a_secure_password_for_encrypting_certificates
```

#### 2c. Initialize Match (First Time Only)

```bash
# This will create certificates and store them in your private repo
bundle exec fastlane match appstore

# Also create development certificates
bundle exec fastlane match development
```

**Note:** You'll be prompted for your Apple ID password. Consider using an [app-specific password](https://support.apple.com/en-us/HT204397).

### Step 3: Test iOS Build

```bash
# Build the Kotlin framework first
bundle exec fastlane ios build_framework

# Build the iOS app
bundle exec fastlane ios build
```

### Step 4: Test TestFlight Deployment

```bash
# Deploy to TestFlight
bundle exec fastlane ios beta
```

---

## Daily Usage

### Common Commands

```bash
# ============ ANDROID ============

# Run tests
bundle exec fastlane android test

# Run code linting
bundle exec fastlane android lint

# Build debug APK
bundle exec fastlane android build_debug

# Deploy to Internal Testing (recommended for development)
bundle exec fastlane android internal

# Deploy to Beta (Open Testing)
bundle exec fastlane android beta

# Deploy to Production (10% rollout)
bundle exec fastlane android release

# Deploy to Production (full rollout)
bundle exec fastlane android release rollout:1.0

# Promote Internal → Beta
bundle exec fastlane android promote_to_beta

# Promote Beta → Production
bundle exec fastlane android promote_to_production rollout:0.2


# ============ iOS ============

# Sync certificates (run if you get code signing errors)
bundle exec fastlane ios certificates

# Build iOS app
bundle exec fastlane ios build

# Deploy to TestFlight (internal only)
bundle exec fastlane ios beta

# Deploy to TestFlight (external testers)
bundle exec fastlane ios beta_external

# Upload to App Store (without submitting for review)
bundle exec fastlane ios release

# Upload and submit for review
bundle exec fastlane ios release_submit


# ============ UTILITY ============

# Generate changelog from git commits
bundle exec fastlane changelog

# Clean build artifacts
bundle exec fastlane clean

# Check environment configuration
bundle exec fastlane check_env
```

### Recommended Workflow

#### During Development
```bash
# Test your changes
bundle exec fastlane android test

# Build debug for local testing
bundle exec fastlane android build_debug
```

#### Weekly Beta Release
```bash
# Push to internal testing
bundle exec fastlane android internal
bundle exec fastlane ios beta
```

#### Production Release
```bash
# Staged rollout (recommended)
bundle exec fastlane android release rollout:0.1   # 10% first
bundle exec fastlane android release rollout:0.5   # Then 50%
bundle exec fastlane android release rollout:1.0   # Then 100%

# iOS App Store
bundle exec fastlane ios release_submit
```

---

## CI/CD Integration

### GitHub Actions

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy

on:
  push:
    tags:
      - 'v*'

env:
  RUBY_VERSION: '3.2'

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

      - name: Setup Ruby
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: ${{ env.RUBY_VERSION }}
          bundler-cache: true
          working-directory: mobile

      - name: Decode Keystore
        run: |
          echo "${{ secrets.ANDROID_KEYSTORE_BASE64 }}" | base64 -d > mobile/keystore.jks

      - name: Create Play Store credentials
        run: |
          echo '${{ secrets.PLAY_STORE_JSON }}' > mobile/fastlane/play-store-credentials.json

      - name: Deploy to Play Store
        working-directory: mobile
        env:
          KEYSTORE_PATH: ${{ github.workspace }}/mobile/keystore.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: bundle exec fastlane android internal

  deploy-ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Ruby
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: ${{ env.RUBY_VERSION }}
          bundler-cache: true
          working-directory: mobile

      - name: Setup Xcode
        uses: maxim-lobanov/setup-xcode@v1
        with:
          xcode-version: latest-stable

      - name: Deploy to TestFlight
        working-directory: mobile
        env:
          MATCH_PASSWORD: ${{ secrets.MATCH_PASSWORD }}
          MATCH_GIT_BASIC_AUTHORIZATION: ${{ secrets.MATCH_GIT_AUTH }}
          APPLE_ID: ${{ secrets.APPLE_ID }}
          APPLE_TEAM_ID: ${{ secrets.APPLE_TEAM_ID }}
          ITC_TEAM_ID: ${{ secrets.ITC_TEAM_ID }}
        run: bundle exec fastlane ios beta
```

### Required GitHub Secrets

Add these secrets to your repository (Settings → Secrets and variables → Actions):

| Secret | Description |
|--------|-------------|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded keystore file (`base64 keystore.jks`) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `PLAY_STORE_JSON` | Contents of play-store-credentials.json |
| `MATCH_PASSWORD` | Password for match certificate encryption |
| `MATCH_GIT_AUTH` | Base64-encoded `username:token` for git access |
| `APPLE_ID` | Apple Developer email |
| `APPLE_TEAM_ID` | Apple Developer Team ID |
| `ITC_TEAM_ID` | App Store Connect Team ID |

---

## Troubleshooting

### Common Issues

#### "No signing certificate found"
```bash
# Regenerate certificates with match
bundle exec fastlane match nuke appstore  # Remove old certs
bundle exec fastlane match appstore       # Create new ones
```

#### "App not found in Play Console"
You must manually upload your first APK/AAB to Play Console before Fastlane can update it.

#### "Invalid credentials" for Play Store
1. Ensure your service account has "Release manager" permission in Play Console
2. Check that the JSON key file is valid and in the correct location

#### "Could not find gradle wrapper"
```bash
cd mobile
./gradlew wrapper  # Regenerate wrapper
```

#### iOS build fails with "framework not found"
```bash
# Rebuild the Kotlin framework
bundle exec fastlane ios build_framework
```

#### "Match: Could not clone repo"
1. Ensure SSH keys are set up for your certificates repo
2. Try using HTTPS with a personal access token instead:
   ```
   MATCH_GIT_URL=https://github.com/username/certificates.git
   MATCH_GIT_BASIC_AUTHORIZATION=base64_encoded_username:token
   ```

### Getting Help

```bash
# List all available lanes
bundle exec fastlane lanes

# Get help on a specific lane
bundle exec fastlane action upload_to_play_store

# Run with verbose output
bundle exec fastlane android internal --verbose
```

---

## Security Best Practices

1. **Never commit secrets to git**
   - `.env` is in `.gitignore`
   - Use environment variables or secret managers

2. **Protect your keystore**
   - Store in a secure location outside the repo
   - Back up to a secure cloud storage
   - Document the passwords in a password manager

3. **Use app-specific passwords for Apple ID**
   - Go to appleid.apple.com → Security → App-Specific Passwords

4. **Limit service account permissions**
   - Only grant "Release manager" for Play Store, not "Admin"

5. **Rotate credentials periodically**
   - Update Match passwords annually
   - Regenerate Play Store service account keys

---

## File Structure Reference

```
mobile/
├── fastlane/
│   ├── Fastfile           # Lane definitions
│   ├── Appfile            # App identifiers
│   ├── Matchfile          # iOS code signing config
│   ├── Pluginfile         # Fastlane plugins
│   ├── Gemfile            # Ruby dependencies
│   ├── .env.default       # Environment template (safe to commit)
│   ├── .env               # Your environment (DO NOT COMMIT)
│   ├── play-store-credentials.json  # Play Store API key (DO NOT COMMIT)
│   ├── metadata/          # Store listing metadata (auto-generated)
│   └── build/             # Build outputs
└── FASTLANE_SETUP.md      # This file
```

---

## Quick Reference Card

| Task | Command |
|------|---------|
| Run tests | `bundle exec fastlane android test` |
| Debug build | `bundle exec fastlane android build_debug` |
| Deploy to Internal | `bundle exec fastlane android internal` |
| Deploy to Beta | `bundle exec fastlane android beta` |
| Deploy to Production | `bundle exec fastlane android release` |
| iOS TestFlight | `bundle exec fastlane ios beta` |
| iOS App Store | `bundle exec fastlane ios release` |
| Sync iOS certs | `bundle exec fastlane ios certificates` |
| View changelog | `bundle exec fastlane changelog` |
| Clean builds | `bundle exec fastlane clean` |
