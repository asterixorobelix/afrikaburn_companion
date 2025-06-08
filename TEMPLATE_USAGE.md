# Template Usage Guide

This document explains how to use this repository template to quickly set up a new Kotlin project with backend and mobile components.

## 🚀 Quick Start

### 1. Use This Template

1. Click "Use this template" button on GitHub
2. Create a new repository with your project name
3. Clone your new repository locally

### 2. Run Setup Script

```bash
# Make sure you're in the repository root
cd your-project-name

# Run the setup script
./setup.sh
```

The script will ask you for:
- **Project Name**: Used for documentation and display names (e.g., "MyAwesomeApp")
- **Package Name**: Used for Kotlin packages (e.g., "com.company.myawesomeapp")
- **Author Name**: Used in documentation (e.g., "Your Name")

### 3. Verify Setup

After running the setup script, verify everything works:

```bash
# Test backend
cd backend
./gradlew test
./gradlew run

# Test mobile
cd ../mobile
./gradlew test
./gradlew build
```

## 📁 What Gets Created

### Backend Structure
```
backend/
├── src/main/kotlin/your/package/
│   ├── Application.kt           # Main entry point
│   ├── plugins/                 # Ktor plugins
│   │   ├── Routing.kt          # API routes
│   │   ├── Security.kt         # JWT authentication
│   │   ├── Databases.kt        # Database configuration
│   │   └── ...
│   └── ...
├── src/test/kotlin/your/package/
├── build.gradle.kts             # Build configuration
└── detekt.yml                   # Code quality rules
```

### Mobile Structure
```
mobile/
├── composeApp/
│   ├── src/commonMain/kotlin/your/package/
│   │   └── App.kt              # Shared UI
│   ├── src/androidMain/kotlin/your/package/
│   │   └── MainActivity.kt     # Android entry point
│   ├── src/iosMain/kotlin/your/package/
│   │   └── MainViewController.kt # iOS entry point
│   └── build.gradle.kts
├── iosApp/                      # iOS Xcode project
└── build.gradle.kts
```

### CI/CD Pipelines
```
.github/workflows/
├── backend-ci.yml              # Backend testing & deployment
└── mobile-ci.yml               # Mobile testing & building
```

## 🛠️ Customization Options

### Project Configuration

The setup script replaces these template placeholders:

| Placeholder | Description | Example |
|------------|-------------|---------|
| `myproject` | Default project name | `MyAwesomeApp` |
| `com.example.myproject` | Default package name | `com.company.myawesomeapp` |
| `Developer` | Default author name | `John Doe` |

### Package Structure

Your package name will be used throughout:
- Kotlin source files: `src/main/kotlin/com/company/myawesomeapp/`
- Android manifest: `android:name="com.company.myawesomeapp.MainActivity"`
- iOS bundle ID: `com.company.myawesomeapp`

### Environment Variables

The template is configured to use these environment variables:

#### Backend
```env
# Required for production
PORT=8080
DATABASE_URL=postgresql://...
JWT_SECRET=your-secret-key
APP_ENVIRONMENT=production

# Optional
DATABASE_USER=username
DATABASE_PASSWORD=password
DB_POOL_SIZE=10
```

#### Mobile
```env
# Optional - for API endpoints
API_BASE_URL=https://your-backend.com
```

## 🚀 Deployment

### Backend Deployment (Railway)

1. Connect your GitHub repository to Railway
2. Set the required environment variables
3. Railway will automatically deploy on pushes to main

### Mobile Deployment

#### Android
```bash
cd mobile
./gradlew composeApp:assembleRelease
```

#### iOS
1. Open `mobile/iosApp/iosApp.xcodeproj` in Xcode
2. Select your team and bundle ID
3. Build and deploy to App Store

## 🧪 Testing

### Running Tests

```bash
# Backend tests
cd backend
./gradlew test
./gradlew detekt

# Mobile tests
cd mobile
./gradlew test
./gradlew detekt
```

### Code Quality

Both projects use Detekt for code quality:
- Configuration: `detekt.yml`
- Run manually: `./gradlew detekt`
- Runs automatically in CI/CD

## 📚 Development Workflow

### 1. Feature Development

```bash
# Create feature branch
git checkout -b feature/awesome-feature

# Make changes to backend/mobile
# Run tests
./backend/gradlew test
./mobile/gradlew test

# Commit and push
git add .
git commit -m "Add awesome feature"
git push origin feature/awesome-feature
```

### 2. Pull Request

- CI/CD runs automatically
- Tests must pass
- Detekt checks must pass
- Code review required

### 3. Deployment

- Merge to main triggers deployment
- Backend deploys to Railway automatically
- Mobile builds are available for manual deployment

## 🔧 Advanced Configuration

### Custom CI/CD

Edit `.github/workflows/` files to:
- Add deployment targets
- Configure test reporting
- Add security scanning
- Set up artifact publishing

### Database Migration

For production databases:
1. Add migration scripts to `backend/src/main/resources/db/migration/`
2. Configure Flyway or Liquibase
3. Update deployment pipeline

### API Documentation

Add OpenAPI/Swagger:
1. Add Ktor OpenAPI plugin
2. Annotate your routes
3. Generate documentation automatically

## 🆘 Troubleshooting

### Common Issues

**Setup script fails**
- Ensure you're in a git repository
- Check file permissions: `chmod +x setup.sh`
- Verify you have bash available

**Build fails**
- Check Java version (requires JDK 17+)
- Update Gradle wrapper: `./gradlew wrapper --gradle-version=8.5`
- Clear Gradle cache: `rm -rf ~/.gradle/caches`

**iOS build fails**
- Ensure Xcode is installed (macOS only)
- Check iOS deployment target in build files
- Update Kotlin Multiplatform plugin

**CI/CD fails**
- Check environment variables are set
- Verify GitHub Actions permissions
- Review build logs in Actions tab

### Getting Help

1. Check the documentation in each component's directory
2. Review CI/CD logs for specific errors
3. Create an issue in the repository
4. Check Kotlin Multiplatform documentation
5. Review Ktor documentation for backend issues

## 📄 License

This template is provided under the MIT License. See LICENSE file for details.

---

**Happy coding!** 🎉

This template provides a solid foundation for Kotlin projects. Customize it further based on your specific needs and requirements.