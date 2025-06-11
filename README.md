# 🔥 AfrikaBurn App

[![Mobile CI](https://github.com/asterixorobelix/afrikaburn_companion/actions/workflows/mobile-ci.yml/badge.svg)](https://github.com/asterixorobelix/afrikaburn_companion/actions/workflows/mobile-ci.yml)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.5.7-blue)](https://github.com/JetBrains/compose-multiplatform)
![Platform](https://img.shields.io/badge/Platform-iOS%20%7C%20Android-blue)

A comprehensive mobile app for [AfrikaBurn](https://www.afrikaburn.org/), the South African regional Burning Man event. Built with Compose Multiplatform to provide essential tools for surviving and thriving in the Tankwa Karoo desert.

[Firebase crashlytics](https://console.firebase.google.com/u/0/project/afrikaburn-companion-app/crashlytics/app/android:io.asterixorobelix.afrikaburn/issues?state=open&time=last-seven-days&types=crash&tag=all&sort=eventCount)

## 🌟 Features

### 🗺️ **Offline-First Design**
- **Complete offline functionality** - Works without internet in the desert
- **Offline maps** with pinned locations for all artworks and theme camps
- **Pre-downloaded content** including images and event schedules
- **Smart sync** - Download everything before you leave for the event

### 🎨 **Event Discovery**
- **Interactive artwork map** - Find installations with artist details and photos
- **Theme camp directory** - Discover camps, activities, and amenities
- **Mutant vehicle tracker** - Locate and ride art cars across the playa
- **Advanced search & filtering** - Find exactly what you're looking for

### 🧭 **Planning & Navigation**
- **GPS navigation** to the event location
- **Packing checklists** - Tailored for desert survival
- **Event timeline** - Personal schedule builder with conflict detection
- **Weather integration** - Real-time Tankwa Karoo weather alerts

### 🎭 **Surprise Elements**
- **Time-released content** - Some artworks revealed only during the event
- **Location-based unlocks** - Special content when you arrive at AfrikaBurn
- **Hidden gems** - Discover secret installations and experiences

### 🚨 **Safety & Emergency**
- **Emergency contacts** - Quick access to Rangers and medical services
- **Dust storm alerts** - Weather warnings for harsh conditions
- **Resource sharing** - Find water, food, and equipment when needed
- **MOOP tracking** - Help keep the desert pristine

### 🤖 **Advanced Features**
- **AR artwork discovery** - Point your camera to get installation details
- **QR code scanning** - Quick access to camp and artwork information
- **Real-time messaging** - Connect with other Burners (when online)
- **Dark mode** - Essential for nighttime desert use

## 🚀 Quick Start

This project was generated using the Kotlin project template. It includes:

- **Backend**: Ktor server with Kotlin and PostgreSQL
- **Mobile**: Compose Multiplatform app (Android + iOS)
- **CI/CD**: GitHub Actions for automated testing and deployment
- **Code Quality**: Detekt for static analysis

## 📁 Project Structure

```
Afrikaburn/
├── backend/              # Ktor backend server
│   ├── src/main/kotlin/io.asterixorobelix.afrikaburn/
│   ├── build.gradle.kts
│   └── detekt.yml
├── mobile/               # Compose Multiplatform app
│   ├── composeApp/       # Shared UI code
│   ├── iosApp/          # iOS application
│   ├── build.gradle.kts
│   └── detekt.yml
├── .github/workflows/    # CI/CD pipelines
├── setup.sh             # Project setup script
└── README.md
```

## ✨ Key Features

### 🔄 **Advanced CI/CD Pipeline**
- ✅ **Automated Testing**: Unit tests with detailed reporting
- ✅ **Code Quality**: Detekt analysis with custom rules
- ✅ **Test Coverage**: JaCoCo integration (80% minimum)
- ✅ **Multi-Platform Builds**: Android APK + iOS XCFramework
- ✅ **PR Integration**: Comprehensive comments with results
- ✅ **Artifact Management**: 7-day retention for detailed reports

### 📱 **Mobile Excellence**
- **Compose Multiplatform**: Shared UI for Android & iOS
- **Clean Architecture**: MVVM with proper separation of concerns
- **Dependency Injection**: Koin integration
- **Firebase Crashlytics**: Cross-platform crash reporting
- **Custom Detekt Rules**: Mobile-specific code quality (Compose naming, etc.)
- **Performance Monitoring**: APK size tracking

### ⚙️ **Backend Robustness**
- **Ktor Framework**: Modern Kotlin web server
- **Clean Architecture**: Domain-Driven Design patterns
- **Database Flexibility**: PostgreSQL (prod) + H2 (dev) support
- **Security**: JWT auth, CORS, rate limiting, security headers
- **Deployment Ready**: Railway/Docker configuration

## 🚀 Quick Start

### 1. Backend Setup
```bash
cd backend
./gradlew test detekt build
./gradlew run  # Starts on http://localhost:8080
```

### 2. Mobile Setup
```bash
cd mobile

# 🔥 IMPORTANT: Firebase Crashlytics Setup
# ⚠️  Project includes DEFAULT google-services.json template
# ⚠️  Crashlytics will NOT work until you replace with your Firebase config
#
# Quick Firebase setup:
# 1. Create Firebase project at https://console.firebase.google.com
# 2. Add Android app with your package name
# 3. Download and replace composeApp/google-services.json
# 4. See mobile/FIREBASE_SETUP.md for detailed instructions

# Build and test (compiles with default config)
./gradlew test detekt
./gradlew composeApp:assembleDebug  # Android APK
./gradlew :composeApp:assembleReleaseXCFramework  # iOS Framework
```

### 3. CI/CD Verification
- Create a pull request
- Watch automated testing and reporting
- Review PR comments for results
- Download artifacts for detailed analysis

## 📊 What You Get in Pull Requests

**Automated Comments Include:**
- 🧪 **Test Results**: Pass/fail counts with percentages
- 🔍 **Code Quality**: Detekt issue analysis
- 📈 **Coverage Reports**: Test coverage statistics (backend)
- 📎 **Artifacts**: Direct links to detailed HTML reports
- 🔧 **Action Items**: Clear guidance for fixing issues

## 🛠️ Technology Stack

### Backend
- **Language**: Kotlin 1.9.24
- **Framework**: Ktor 2.3.12
- **Database**: Exposed ORM + PostgreSQL/H2
- **Testing**: JUnit 5 + Kotest + JaCoCo
- **Quality**: Detekt + Custom Rules
- **Deployment**: Railway/Docker ready

### Mobile
- **Language**: Kotlin 2.1.21
- **UI**: Compose Multiplatform 1.8.1
- **Platforms**: Android (API 24+) + iOS (14+)
- **Architecture**: Clean Architecture + MVVM
- **DI**: Koin 3.5.3
- **Crash Reporting**: Firebase Crashlytics
- **Quality**: Detekt + Mobile-specific rules

### DevOps
- **CI/CD**: GitHub Actions
- **Code Quality**: Detekt static analysis
- **Testing**: Automated unit test execution
- **Reporting**: Comprehensive PR comments + artifacts
- **Security**: Dependency scanning + secure workflows

## 📚 Documentation

- **🚀 GitHub Setup**: [GITHUB_SETUP.md](GITHUB_SETUP.md) - Complete repository setup with Firebase & CI/CD
- **🔥 Firebase Setup**: [mobile/FIREBASE_SETUP.md](mobile/FIREBASE_SETUP.md) - Crashlytics configuration guide
- **Backend**: [backend/claude.md](backend/claude.md) - Complete backend development guide
- **Mobile**: [mobile/CLAUDE.md](mobile/CLAUDE.md) - Mobile development context
- **Setup**: [TEMPLATE_USAGE.md](TEMPLATE_USAGE.md) - How to use this template

## 🎆 Best Practices Included

- **⚙️ Clean Architecture**: Proper separation of concerns
- **🔒 Security First**: JWT, CORS, rate limiting, input validation
- **🧪 Test-Driven**: Comprehensive testing with coverage tracking
- **📊 Quality Gates**: Automated code quality enforcement
- **📝 Documentation**: AI assistant-ready context files
- **🚀 CI/CD**: Production-ready automation
- **📱 Cross-Platform**: True code sharing between Android/iOS

## 🛠️ Setup

### Prerequisites

- JDK 17+
- Android Studio (for mobile development)
- Xcode (for iOS development, macOS only)
- IntelliJ IDEA (for backend development)

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Run the application:
   ```bash
   ./gradlew run
   ```

3. Test the API:
   ```bash
   curl http://localhost:8080/health
   ```

### Mobile Setup

1. Navigate to the mobile directory:
   ```bash
   cd mobile
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run on Android:
   ```bash
   ./gradlew composeApp:installDebug
   ```

4. For iOS: Open `iosApp/iosApp.xcodeproj` in Xcode and run

## 🧪 Testing

### Backend Tests
```bash
cd backend
./gradlew test
./gradlew detekt
```

### Mobile Tests
```bash
cd mobile
./gradlew test
./gradlew detekt
```

## 🚀 Deployment

### Backend Deployment (Railway)

1. Connect your GitHub repository to Railway
2. Set environment variables:
   ```env
   PORT=8080
   DATABASE_URL=postgresql://...
   JWT_SECRET=your-secret-key
   APP_ENVIRONMENT=production
   ```
3. Deploy automatically on push to main branch

### Mobile Deployment

- **Android**: Build APK/AAB and deploy to Google Play
- **iOS**: Build and deploy to App Store

## 📊 Code Quality

This project uses Detekt for code quality and style checking:

```bash
# Run detekt on backend
cd backend && ./gradlew detekt

# Run detekt on mobile
cd mobile && ./gradlew detekt
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes
4. Run tests and detekt: `./gradlew test detekt`
5. Commit your changes: `git commit -am 'Add feature'`
6. Push to the branch: `git push origin feature-name`
7. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙋‍♂️ Support

For questions and support:
- Create an issue in the GitHub repository
- Check the documentation in each component's directory
- Review the CI/CD pipeline logs for deployment issues

## 🌍 About AfrikaBurn

[AfrikaBurn](https://www.afrikaburn.org/) is the official South African regional event of the global Burning Man network. It takes place annually in the Tankwa Karoo, bringing together a community of creative individuals for a week of radical self-expression, self-reliance, and gifting.

### Principles

- **Radical Self-Expression** - Freedom to be yourself
- **Gifting** - Unconditional giving without expectation
- **Self-Reliance** - Taking responsibility for your own survival
- **Leave No Trace** - Respect for the environment

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **AfrikaBurn Community** - For inspiration and feedback
- **Burning Man Project** - For the original vision
- **JetBrains** - For Compose Multiplatform
- **Contributors** - Everyone who helps make this project better

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/asterixorobelix/afrikaburn-app/issues)
- **Discussions**: [GitHub Discussions](https://github.com/asterixorobelix/afrikaburn-app/discussions)
- **Email**: support@afrikaburn-app.com

## 🔥 Built with Love for the Burn

This app is created by Burners, for Burners. It's a gift to the AfrikaBurn community to help everyone have a safer, more connected, and more magical experience in the Tankwa Karoo.

*See you on the playa!* 🐪

---

**Author**: asterixorobelix  
**Package**: io.asterixorobelix.afrikaburn  
**Generated**: 2025-06-08
