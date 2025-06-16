# Mobile CD Workflow Optimization Guide

This guide addresses performance issues and timeouts in the mobile-cd.yml workflow, specifically the iOS framework build step that can hang indefinitely.

## 🐛 Common Issues

### Issue 1: iOS Framework Build Timeout
**Symptoms**: 
- Workflow hangs at `linkReleaseFrameworkIosArm64` task
- Build takes over 1 hour and eventually times out
- No progress after linking tasks start

**Root Causes**:
1. **Memory Issues**: Kotlin/Native linking can consume excessive memory on CI runners
2. **Parallelization**: Multiple iOS targets building simultaneously can overwhelm the runner
3. **CI Environment**: GitHub Actions macOS runners have resource constraints
4. **Gradle Daemon**: Can cause memory leaks in long-running builds

## 🔧 Optimized Workflow Configuration

### Solution 1: Update mobile-cd.yml with Optimizations

Replace the iOS build section in your `mobile-cd.yml` with this optimized version:

```yaml
  build-ios:
    runs-on: macos-14  # Use specific runner version
    timeout-minutes: 45  # Add overall timeout
    needs: version-and-build
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      with:
        ref: main
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
          ~/.konan
        key: ${{ runner.os }}-gradle-konan-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-konan-
          ${{ runner.os }}-gradle-
    
    - name: Set up Firebase Configuration for iOS Release
      run: |
        echo "🔥 Setting up Firebase for iOS production release..."
        
        # Set up Android Firebase config if available
        if [ ! -z "${{ secrets.GOOGLE_SERVICES_JSON }}" ]; then
          echo "${{ secrets.GOOGLE_SERVICES_JSON }}" | base64 --decode > mobile/composeApp/google-services.json
          echo "✅ Android Firebase configuration set up."
        else
          echo "⚠️ Using default Android Firebase configuration."
        fi
        
        # Set up iOS Firebase config if available
        if [ ! -z "${{ secrets.IOS_FIREBASE_CONFIG_PLIST }}" ]; then
          echo '${{ secrets.IOS_FIREBASE_CONFIG_PLIST }}' > mobile/iosApp/iosApp/GoogleService-Info.plist
          echo "✅ iOS Firebase configuration set up - Crashlytics enabled."
        else
          echo "⚠️ iOS Firebase configuration not found. Add IOS_FIREBASE_CONFIG_PLIST secret for iOS Crashlytics."
        fi

    - name: Make gradlew executable
      run: chmod +x mobile/gradlew
    
    - name: Initialize Gradle wrapper
      run: |
        cd mobile
        gradle wrapper --gradle-version 8.11.1
    
    - name: Configure Gradle for CI
      run: |
        cd mobile
        # Create gradle.properties for CI optimization
        cat >> gradle.properties << EOF
        # CI Optimizations
        org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC
        org.gradle.parallel=false
        org.gradle.daemon=false
        org.gradle.configureondemand=false
        
        # Kotlin/Native optimizations
        kotlin.native.ignoreDisabledTargets=true
        kotlin.native.useXcodeMessageStyle=true
        kotlin.incremental.native=false
        
        # Disable unnecessary features for CI
        android.useAndroidX=true
        android.enableJetifier=false
        EOF
    
    - name: Build iOS Framework (Sequential)
      timeout-minutes: 30
      run: |
        cd mobile
        echo "🔨 Building iOS framework targets sequentially to avoid memory issues..."
        
        # Build each target separately to reduce memory pressure
        echo "📱 Building iOS Arm64 target..."
        ./gradlew :composeApp:linkReleaseFrameworkIosArm64 --no-daemon --max-workers=1
        
        echo "📱 Building iOS X64 target..."
        ./gradlew :composeApp:linkReleaseFrameworkIosX64 --no-daemon --max-workers=1
        
        echo "📱 Building iOS Simulator Arm64 target..."
        ./gradlew :composeApp:linkReleaseFrameworkIosSimulatorArm64 --no-daemon --max-workers=1
        
        echo "🔗 Creating XCFramework..."
        ./gradlew :composeApp:assembleReleaseXCFramework --no-daemon --max-workers=1
    
    - name: Set up Xcode
      uses: maxim-lobanov/setup-xcode@v1
      with:
        xcode-version: '15.4'  # Use specific version
    
    - name: Create exportOptions.plist
      run: |
        cd mobile
        cat > exportOptions.plist << EOF
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
        <plist version="1.0">
        <dict>
            <key>method</key>
            <string>development</string>
            <key>teamID</key>
            <string>\${{ secrets.APPLE_TEAM_ID }}</string>
            <key>compileBitcode</key>
            <false/>
            <key>stripSwiftSymbols</key>
            <true/>
            <key>uploadBitcode</key>
            <false/>
            <key>uploadSymbols</key>
            <true/>
        </dict>
        </plist>
        EOF
    
    - name: Build iOS IPA
      timeout-minutes: 15
      run: |
        cd mobile/iosApp
        
        echo "🍎 Building iOS app archive..."
        # Configure build settings
        xcodebuild \
          -project iosApp.xcodeproj \
          -scheme iosApp \
          -configuration Release \
          -destination generic/platform=iOS \
          -archivePath build/iosApp.xcarchive \
          archive \
          CODE_SIGN_IDENTITY="" \
          CODE_SIGNING_REQUIRED=NO \
          CODE_SIGNING_ALLOWED=NO \
          -quiet
        
        echo "📦 Exporting IPA..."
        # Export IPA
        xcodebuild \
          -archivePath build/iosApp.xcarchive \
          -exportArchive \
          -exportPath build/ \
          -exportOptionsPlist ../exportOptions.plist \
          -quiet
    
    - name: Upload IPA artifact
      uses: actions/upload-artifact@v4
      with:
        name: ios-ipa
        path: mobile/iosApp/build/*.ipa
```

### Solution 2: Alternative Approach - Skip iOS IPA Creation

If the full iOS build continues to be problematic, you can modify the workflow to only build the iOS framework (which is usually sufficient for distribution):

```yaml
    - name: Build iOS Framework Only
      timeout-minutes: 20
      run: |
        cd mobile
        echo "🔨 Building iOS framework for distribution..."
        ./gradlew :composeApp:assembleReleaseXCFramework --no-daemon --max-workers=1 --no-parallel
    
    - name: Package iOS Framework
      run: |
        cd mobile
        # Create a distributable archive of the framework
        tar -czf composeApp-ios-framework.tar.gz -C composeApp/build/XCFrameworks/release/ .
    
    - name: Upload iOS Framework artifact
      uses: actions/upload-artifact@v4
      with:
        name: ios-framework
        path: mobile/composeApp-ios-framework.tar.gz
```

## 🔧 Additional Optimizations

### 1. Update Gradle Configuration

Create or update `mobile/gradle.properties`:

```properties
# Performance optimizations
org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC
org.gradle.parallel=false
org.gradle.daemon=false
org.gradle.configureondemand=false

# Kotlin/Native optimizations
kotlin.native.ignoreDisabledTargets=true
kotlin.native.useXcodeMessageStyle=true
kotlin.incremental.native=false
kotlin.native.cacheKind=none

# Disable unnecessary features
android.useAndroidX=true
android.enableJetifier=false
org.jetbrains.compose.experimental.jscanvas.enabled=false
org.jetbrains.compose.experimental.macos.enabled=false
org.jetbrains.compose.experimental.uikit.enabled=true
```

### 2. Optimize build.gradle.kts

Update your `mobile/composeApp/build.gradle.kts` to reduce memory usage:

```kotlin
kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    // Build iOS targets with optimizations
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            
            // Optimize for CI builds
            if (System.getenv("CI") == "true") {
                freeCompilerArgs += listOf(
                    "-Xruntime-logs=gc=info",
                    "-Xallocator=custom"
                )
            }
        }
    }
    
    // Remove XCFramework tasks that cause issues
    if (System.getenv("CI") == "true") {
        tasks.named("assembleDebugXCFramework") {
            enabled = false
        }
    }
}
```

### 3. GitHub Actions Workflow Optimizations

Add these environment variables to your workflow:

```yaml
env:
  CI: true
  GRADLE_OPTS: -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false -Dorg.gradle.jvmargs="-Xmx6g -XX:MaxMetaspaceSize=1g"
  KONAN_DATA_DIR: /Users/runner/.konan
```

## 🚨 Emergency Workarounds

### Option 1: Disable iOS Build Temporarily

If you need releases urgently, temporarily disable the iOS build:

```yaml
  # build-ios:
  #   runs-on: macos-latest
  #   needs: version-and-build
  #   # ... iOS build steps commented out

  create-release:
    runs-on: ubuntu-latest
    needs: [version-and-build]  # Remove build-ios dependency
    
    steps:
    # ... existing steps, but only download Android artifacts
```

### Option 2: Use Matrix Strategy

Split iOS builds across multiple jobs:

```yaml
  build-ios:
    runs-on: macos-14
    timeout-minutes: 30
    needs: version-and-build
    strategy:
      fail-fast: false
      matrix:
        target: [iosArm64, iosX64, iosSimulatorArm64]
    
    steps:
    # ... setup steps
    
    - name: Build iOS Target ${{ matrix.target }}
      timeout-minutes: 20
      run: |
        cd mobile
        ./gradlew :composeApp:linkReleaseFramework${{ matrix.target }} --no-daemon
```

## 📊 Monitoring and Debugging

### Add Resource Monitoring

```yaml
    - name: Monitor System Resources
      run: |
        echo "=== System Resources Before Build ==="
        top -l 1 | head -20
        vm_stat
        df -h
```

### Add Detailed Logging

```yaml
    - name: Build with Verbose Logging
      run: |
        cd mobile
        ./gradlew :composeApp:assembleReleaseXCFramework \
          --info \
          --no-daemon \
          --max-workers=1 \
          2>&1 | tee build.log
```

## 🎯 Recommended Implementation

1. **Start with Solution 1**: Implement the optimized workflow with sequential builds
2. **Monitor Results**: Check if builds complete within 30-45 minutes
3. **Fallback to Solution 2**: If still having issues, build framework only
4. **Emergency Option**: Temporarily disable iOS builds if needed

## 📈 Expected Results

After implementing these optimizations:
- ✅ iOS builds should complete in 15-30 minutes instead of timing out
- ✅ Reduced memory pressure and resource contention
- ✅ More reliable builds with proper timeout handling
- ✅ Better error reporting and debugging information

## 🔍 Troubleshooting

If builds still fail:

1. **Check Runner Logs**: Look for memory or resource errors
2. **Reduce Parallelism**: Set `--max-workers=1` for all Gradle tasks
3. **Use Smaller Runner**: Consider using `macos-13` if `macos-14` has issues
4. **Split Builds**: Use matrix strategy to build targets separately
5. **Contact Support**: GitHub Actions support can help with runner issues

Remember: The goal is to have working releases. It's better to have Android-only releases than no releases at all while debugging iOS build issues.