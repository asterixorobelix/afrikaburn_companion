# Backend Kotlin Project

![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)
![Ktor](https://img.shields.io/badge/Framework-Ktor-orange)
![Koin](https://img.shields.io/badge/DI-Koin-green)

A modern Kotlin backend built with Ktor framework and Koin dependency injection.

## 🏗️ Tech Stack

- **Framework**: Ktor 2.3.12
- **Language**: Kotlin 1.9.24
- **Dependency Injection**: Koin 3.5.6
- **Database**: Exposed ORM with PostgreSQL/H2
- **Security**: JWT Authentication
- **Testing**: JUnit 5, Kotest, MockK
- **Code Quality**: Detekt, JaCoCo

## 🚀 Quick Start

### Prerequisites
- JDK 17 or higher
- Gradle 7.6 or higher

### Development
```bash
# Run the application
./gradlew run

# Run tests
./gradlew test

# Run with test coverage
./gradlew test jacocoTestReport

# Code quality check
./gradlew detekt

# Build JAR
./gradlew build
```

### Environment Variables
```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/mydb
DATABASE_USER=username
DATABASE_PASSWORD=password

# Security
JWT_SECRET=your-secret-key-minimum-32-characters
JWT_ISSUER=your-app
JWT_AUDIENCE=your-users

# Server
PORT=8080
```

## 📁 Project Structure

```
src/main/kotlin/com/example/myproject/
├── Application.kt              # Entry point
├── di/
│   └── AppModule.kt           # Koin dependency injection
└── plugins/
    ├── Databases.kt           # Database configuration
    ├── HTTP.kt                # CORS and headers
    ├── Monitoring.kt          # Logging
    ├── Routing.kt             # API routes
    ├── Security.kt            # JWT authentication
    ├── Serialization.kt       # JSON handling
    └── StatusPages.kt         # Error handling
```

## 🔧 Dependency Injection

This project uses Koin for dependency injection. Define your dependencies in `di/AppModule.kt`:

```kotlin
val appModule = module {
    // Services
    single { MyService(get()) }
    
    // Repositories
    single<MyRepository> { MyRepositoryImpl() }
}
```

## 🗄️ Database

- **Development**: H2 in-memory database
- **Production**: PostgreSQL with connection pooling
- **ORM**: Jetbrains Exposed

## 🔐 Security Features

- JWT token authentication
- CORS configuration
- Rate limiting
- Security headers
- Input validation

## 🧪 Testing

- Unit tests with JUnit 5 and Kotest
- Integration tests for API endpoints
- Mocking with MockK
- Test coverage reporting with JaCoCo (80% minimum)

## 📊 Quality Assurance

- **Static Analysis**: Detekt with custom rules
- **Test Coverage**: JaCoCo with 80% minimum threshold
- **CI/CD**: Automated testing and quality checks
- **Code Style**: Kotlin coding conventions

## 🚀 Deployment

### Railway
The application is configured for Railway deployment:
```bash
# Railway will automatically detect and build the JAR
# Set environment variables in Railway dashboard
```

### Docker
```bash
# Build and run with Docker
docker build -t myproject-backend .
docker run -p 8080:8080 myproject-backend
```

## 📝 API Documentation

### Health Check
```http
GET /health
```

### Status
```http
GET /api/v1/status
```

## 🔗 Related Projects

- [Mobile App](../mobile/) - Kotlin Multiplatform mobile application
- [Project Setup](../) - Unified project configuration

## 📚 Documentation

For detailed development guidelines, see [claude.md](claude.md).
