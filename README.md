# Afrikaburn

A Kotlin project template with backend (Ktor) and mobile (Compose Multiplatform) components.

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

---

**Author**: asterixorobelix  
**Package**: io.asterixorobelix.afrikaburn  
**Generated**: 2025-06-08
