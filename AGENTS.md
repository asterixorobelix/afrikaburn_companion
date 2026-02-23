# AfrikaBurn Companion — Quick Reference

## Entry Points

- **Mobile Root:** `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/App.kt`
- **Backend Root:** `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/Application.kt`
- **Specs:** `specs/001-a-comprehensive-mobile/`

## Package Structure

- **Shared code:** `mobile/composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/`
  - `data/` | `domain/` | `di/` | `navigation/` | `presentation/` | `ui/` | `models/` | `platform/`
- **Android:** `mobile/composeApp/src/androidMain/kotlin/`
- **iOS:** `mobile/composeApp/src/iosMain/kotlin/`
- **Tests:** `mobile/composeApp/src/commonTest/kotlin/`
- **Backend:** `backend/src/main/kotlin/io/asterixorobelix/afrikaburn/`
- **Resources:** `mobile/composeApp/composeResources/` (images, strings, JSON data)

## Commands

```bash
# Mobile
./mobile/gradlew -p mobile test jacocoTestReport
./mobile/gradlew -p mobile jacocoTestCoverageVerification
./mobile/gradlew -p mobile :composeApp:installDebug
./mobile/gradlew -p mobile detekt

# Backend
./backend/gradlew -p backend run
./backend/gradlew -p backend test jacocoTestReport
./backend/gradlew -p backend detekt
```
