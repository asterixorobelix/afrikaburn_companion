# App Store Screenshots

Screenshots are captured manually and uploaded via fastlane.

## Required Devices

### Android (Play Store)

| Type | Device/Emulator | Resolution |
|------|----------------|------------|
| Phone | Pixel 8 (or any modern phone) | Any (min 320px, max 3840px per side) |

Minimum 4 screenshots, maximum 8. PNG or JPEG format.

### iOS (App Store Connect)

| Type | Simulator | Resolution |
|------|-----------|------------|
| 6.9" iPhone | iPhone 16 Pro Max | 1320 x 2868 |
| 12.9" iPad | iPad Pro 13-inch (M4) | 2064 x 2752 |

iPhone screenshots are **required**. iPad screenshots are required if your app supports iPad.
Minimum 2 screenshots per device class, maximum 10.

## Screens to Capture

1. **home** - Home/countdown screen
2. **explore** - Explore projects tab
3. **map** - Map tab
4. **more** - More options tab
5. **about** - About screen (Android only, via More > About)

## How to Take Screenshots

### Android

1. Start an emulator (Pixel 8 recommended):
   ```bash
   # List available AVDs
   emulator -list-avds
   # Start one
   emulator -avd Pixel_8_API_35
   ```

2. Install the debug app:
   ```bash
   cd mobile
   ./gradlew :composeApp:installDebug
   ```

3. Clean the status bar:
   ```bash
   adb shell settings put global window_animation_scale 0
   adb shell settings put global transition_animation_scale 0
   adb shell settings put global animator_duration_scale 0
   # Set clean status bar (time, battery, signal)
   adb shell cmd statusbar set-demo-mode-enabled 1
   adb shell cmd statusbar enter-demo-mode clock -e hhmm 0941
   adb shell cmd statusbar enter-demo-mode battery -e level 100 -e plugged false
   adb shell cmd statusbar enter-demo-mode network -e mobile show -e level 4 -e datatype none -e wifi show -e wifiLevel 4
   adb shell cmd statusbar enter-demo-mode notifications -e visible false
   ```

4. Navigate to each screen and capture:
   ```bash
   adb exec-out screencap -p > fastlane/screenshots/android/en-US/phoneScreenshots/1_home.png
   # ... navigate to next screen ...
   adb exec-out screencap -p > fastlane/screenshots/android/en-US/phoneScreenshots/2_explore.png
   adb exec-out screencap -p > fastlane/screenshots/android/en-US/phoneScreenshots/3_map.png
   adb exec-out screencap -p > fastlane/screenshots/android/en-US/phoneScreenshots/4_more.png
   adb exec-out screencap -p > fastlane/screenshots/android/en-US/phoneScreenshots/5_about.png
   ```

5. Reset demo mode when done:
   ```bash
   adb shell cmd statusbar exit-demo-mode
   ```

### iOS

1. Open the simulator for each required device:
   ```bash
   # iPhone 16 Pro Max
   xcrun simctl boot "iPhone 16 Pro Max"
   open -a Simulator

   # iPad Pro 13-inch (M4)
   xcrun simctl boot "iPad Pro 13-inch (M4)"
   ```

2. Build and install:
   ```bash
   cd mobile
   ./gradlew :composeApp:assembleDebugXCFramework
   cd iosApp && pod install && cd ..
   # Then run from Xcode on the simulator
   ```

3. Override the status bar:
   ```bash
   xcrun simctl status_bar "iPhone 16 Pro Max" override \
     --time "9:41" --batteryState charged --batteryLevel 100
   ```

4. Take screenshots using Cmd+S in Simulator (saves to Desktop), then move them:
   ```bash
   # iPhone screenshots
   mv ~/Desktop/Simulator\ Screen\ Shot*.png "fastlane/screenshots/ios/en-US/iPhone 16 Pro Max/"
   # Rename to: 1_home.png, 2_explore.png, etc.

   # iPad screenshots
   mv ~/Desktop/Simulator\ Screen\ Shot*.png "fastlane/screenshots/ios/en-US/iPad Pro 13-inch (M4)/"
   ```

5. Reset status bar:
   ```bash
   xcrun simctl status_bar "iPhone 16 Pro Max" clear
   ```

## Directory Structure

```
fastlane/screenshots/
  android/
    en-US/
      phoneScreenshots/
        1_home.png
        2_explore.png
        3_map.png
        4_more.png
        5_about.png
  ios/
    en-US/
      iPhone 16 Pro Max/
        1_home.png
        2_explore.png
        3_map.png
        4_more.png
      iPad Pro 13-inch (M4)/
        1_home.png
        2_explore.png
        3_map.png
        4_more.png
```

## Uploading Screenshots

Screenshots are committed to the repo and uploaded manually via fastlane:

```bash
cd mobile

# Upload to Play Store
PLAY_STORE_JSON_KEY=/path/to/play-store.json bundle exec fastlane android upload_screenshots

# Upload to App Store Connect (requires ASC API key env vars)
ASC_KEY_ID=xxx ASC_ISSUER_ID=xxx ASC_KEY_CONTENT=xxx bundle exec fastlane ios upload_screenshots
```

## Naming Convention

Files are sorted alphabetically in the store listings. Prefix with numbers to control order:
`1_home.png`, `2_explore.png`, `3_map.png`, etc.
