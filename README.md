# Full-Stack Kotlin Project Template

🚀 **Production-ready starter template** for modern Kotlin development with automated CI/CD, comprehensive testing, and AI assistant integration.

## 🎯 Project Overview

This template provides a complete foundation for building:
- **📱 Mobile Apps**: Compose Multiplatform (Android + iOS)
- **⚙️ Backend APIs**: Ktor server with PostgreSQL/H2
- **🤖 AI Integration**: Multi-provider AI API support
- **🔄 CI/CD Pipeline**: Automated testing, code quality, and deployment

## 🏢 Architecture

```
project_setup/
├── backend/           # Ktor server (Kotlin)
│   ├── src/main/kotlin/ # Clean Architecture + DDD
│   ├── claude.md        # AI assistant context
│   └── detekt.yml       # Code quality rules
├── mobile/            # Compose Multiplatform
│   ├── composeApp/      # Shared code (Common/Android/iOS)
│   ├── iosApp/          # iOS app entry point
│   ├── CLAUDE.md        # Mobile development context
│   └── detekt-mobile.yml # Mobile-specific quality rules
└── .github/workflows/ # CI/CD automation
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
- **AI Integration**: Multi-provider support (Claude, OpenAI, Gemini)
- **Deployment Ready**: Railway/Docker configuration

### 🤖 **AI Assistant Integration**
- **Comprehensive Documentation**: Detailed context files for AI assistants
- **Code Generation Guidelines**: Best practices and patterns
- **Architecture Guidance**: How to maintain clean code structure
- **CI/CD Awareness**: Integration with automated feedback

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

---

**🎆 This template is production-ready and includes everything you need to build, test, and deploy modern Kotlin applications with confidence.**