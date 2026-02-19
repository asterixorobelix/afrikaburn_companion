# AfrikaBurn Companion

A Kotlin Multiplatform mobile app for AfrikaBurn, focused on offline access to camps and artworks, a MapLibre-based map, and on-site navigation tools.

## Status

- Mobile app: active development (Android + iOS)
- Backend: scaffolded Ktor service with only `/` and `/health` endpoints

## Implemented Features

- Offline project catalog from bundled JSON (theme camps + artworks)
- Search and filtering (family-friendly, time filter) per project tab
- Project detail screens with image loading and external link confirmation
- MapLibre map with bundled GeoJSON overlays
- User location tracking and camp pin save/move/delete
- Directions, About, and More screens
- Surprise mode unlock (date or geofence-based visibility for tabs)

## Tech Stack

### Mobile

- Kotlin 2.2.20
- Compose Multiplatform 1.9.0
- Koin 4.1.1
- SQLDelight 2.0.2
- Ktor client + kotlinx.serialization
- Coil 3
- MapLibre Compose

### Backend

- Ktor 3.1.3 (configured)
- Exposed 0.61.0 + HikariCP
- PostgreSQL (prod) + H2 (dev)
- JWT auth configured (not applied to routes)

## Project Structure

```
Afrikaburn/
├── backend/              # Ktor backend server
│   ├── src/main/kotlin/io/asterixorobelix/afrikaburn/
│   ├── build.gradle.kts
│   └── detekt.yml
├── mobile/               # Compose Multiplatform app
│   ├── composeApp/       # Shared UI code
│   ├── iosApp/           # iOS application
│   ├── build.gradle.kts
│   └── detekt.yml
├── .github/workflows/    # CI/CD pipelines
└── README.md
```

## Quick Start

### Backend

```bash
./backend/gradlew -p backend run
```

### Mobile

```bash
./mobile/gradlew -p mobile test
./mobile/gradlew -p mobile :composeApp:installDebug
```

For iOS (macOS only):

```bash
cd mobile/iosApp && xcodebuild
```

## Documentation

- `GITHUB_SETUP.md`
- `mobile/FIREBASE_SETUP.md`
- `mobile/CLAUDE.md`
- `backend/claude.md`
- `TEMPLATE_USAGE.md`

## License

MIT. See `LICENSE`.
