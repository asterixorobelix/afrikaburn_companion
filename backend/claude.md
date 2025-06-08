# claude.md - Kotlin Backend Reference Guide
*Essential context for AI assistants working on this Ktor backend project*

---

## 🏗️ Project Overview

**Tech Stack:**
- **Framework**: Ktor 2.3.5 (Kotlin web framework)
- **Language**: Kotlin 1.9.10 with JVM target
- **Database**: Exposed ORM with PostgreSQL (prod) / H2 (dev)
- **Auth**: JWT with Auth0 algorithm
- **Deployment**: Railway/Render with Docker support
- **AI Integration**: Multi-provider support (Claude 3.5 Sonnet primary)

**Architecture**: Clean Architecture with Domain-Driven Design patterns

---

## 📁 Directory Structure

```
backend/
├── src/main/kotlin/com/yourapp/
│   ├── Application.kt                    # Entry point
│   ├── domain/                          # Business logic (DDD)
│   │   ├── customer/
│   │   │   ├── Customer.kt             # Entity
│   │   │   ├── CustomerService.kt      # Domain service  
│   │   │   ├── CustomerRepository.kt   # Repository interface
│   │   │   └── CustomerRoutes.kt       # HTTP routes
│   │   └── shared/                     # Shared domain concepts
│   ├── infrastructure/                  # External concerns
│   │   ├── database/
│   │   │   ├── DatabaseConfig.kt
│   │   │   └── repositories/           # Repository implementations
│   │   └── web/
│   ├── plugins/                        # Ktor plugin configurations
│   │   ├── Routing.kt                  # Route definitions
│   │   ├── Serialization.kt           # JSON handling
│   │   ├── Security.kt                # JWT + auth
│   │   ├── Databases.kt               # DB connection
│   │   ├── HTTP.kt                    # CORS + headers
│   │   ├── Monitoring.kt              # Logging + metrics
│   │   └── StatusPages.kt             # Error handling
│   ├── configuration/                  # App configuration
│   │   ├── AppConfig.kt               # Type-safe config
│   │   ├── AIConfig.kt                # AI provider setup
│   │   └── Environment.kt             # Environment detection
│   ├── services/                       # Application services
│   │   ├── AIService.kt               # AI API integration
│   │   └── CustomerService.kt         # Business logic
│   └── util/                          # Utilities and extensions
├── src/main/resources/
│   └── application.conf                # HOCON configuration
├── src/test/kotlin/com/yourapp/        # Tests
├── build.gradle.kts                    # Dependencies + build config
└── gradle.properties                   # Gradle settings
```

---

## ⚙️ Core Configuration Patterns

### 1. Application Entry Point (Application.kt)
```kotlin
fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()      // JWT + rate limiting
    configureSerialization() // JSON handling
    configureDatabases()     // DB connection + pooling
    configureHTTP()         // CORS + security headers
    configureMonitoring()   // Logging + metrics
    configureStatusPages()  // Error handling
    configureRouting()      // API routes
}
```

### 2. Database Configuration (plugins/Databases.kt)
```kotlin
// Supports both H2 (dev) and PostgreSQL (prod) automatically
fun Application.configureDatabases() {
    val databaseUrl = System.getenv("DATABASE_URL") 
        ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    
    val (url, driver, user, password) = when {
        databaseUrl.startsWith("jdbc:postgresql") -> {
            listOf(databaseUrl, "org.postgresql.Driver", 
                   System.getenv("DATABASE_USER") ?: "", 
                   System.getenv("DATABASE_PASSWORD") ?: "")
        }
        else -> listOf(databaseUrl, "org.h2.Driver", "sa", "")
    }
    
    // HikariCP connection pooling
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        driverClassName = driver
        username = user
        password = password
        maximumPoolSize = System.getenv("DB_POOL_SIZE")?.toInt() ?: 10
    }
    
    Database.connect(HikariDataSource(hikariConfig))
}
```

### 3. AI Service Integration (services/AIService.kt)
```kotlin
class AIService(private val config: AIConfig) {
    suspend fun chat(message: String): String {
        return try {
            when (config.primaryProvider) {
                AIProvider.CLAUDE -> chatWithClaude(message)
                AIProvider.OPENAI -> chatWithOpenAI(message)
                AIProvider.GEMINI -> chatWithGemini(message)
            }
        } catch (e: Exception) {
            "Sorry, AI service is temporarily unavailable."
        }
    }
}
```

---

## 🔐 Security Configuration

### JWT Setup (plugins/Security.kt)
```kotlin
install(Authentication) {
    jwt("auth-jwt") {
        realm = "Your App"
        verifier(
            JWT.require(Algorithm.HMAC256(jwtSecret))
                .withAudience(jwtAudience)
                .withIssuer(jwtIssuer)
                .build()
        )
        validate { credential ->
            if (credential.payload.getClaim("username").asString() != "") {
                JWTPrincipal(credential.payload)
            } else null
        }
    }
}
```

### Security Headers (plugins/HTTP.kt)
```kotlin
install(DefaultHeaders) {
    header("X-Content-Type-Options", "nosniff")
    header("X-Frame-Options", "DENY")
    header("X-XSS-Protection", "1; mode=block") 
    header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
}
```

### Rate Limiting
```kotlin
install(RateLimit) {
    register(RateLimitName("api")) {
        rateLimiter(limit = 100, refillPeriod = 1.minutes)
        requestKey { call -> call.request.origin.remoteHost }
    }
}
```

---

## 🌍 Environment Variables

### Required for Production
```env
# Server
PORT=8080
APP_ENVIRONMENT=production

# Database  
DATABASE_URL=postgresql://user:pass@host:5432/dbname
DATABASE_USER=username
DATABASE_PASSWORD=password
DB_POOL_SIZE=10

# Security
JWT_SECRET=your-super-secret-key-minimum-32-chars
JWT_ISSUER=your-app-name
JWT_AUDIENCE=your-app-users

# AI APIs
AI_PRIMARY_PROVIDER=claude
CLAUDE_API_KEY=your_claude_api_key_here
OPENAI_API_KEY=your_openai_api_key_here
GEMINI_API_KEY=your_gemini_api_key_here

# Monitoring (Optional)
SENTRY_DSN=your_sentry_dsn
```

### Development Defaults (application.conf)
```hocon
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
    }
    application {
        modules = [ com.yourapp.ApplicationKt.module ]
    }
}

database {
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    url = ${?DATABASE_URL}
}

jwt {
    secret = ${?JWT_SECRET}
    issuer = ${?JWT_ISSUER}  
    audience = ${?JWT_AUDIENCE}
}
```

---

## 🚀 Essential Dependencies (build.gradle.kts)

```kotlin
val ktor_version = "2.3.5"
val exposed_version = "0.44.1"

dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    
    // Content & Serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    
    // Security
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-rate-limit-jvm:$ktor_version")
    
    // Database (Exposed ORM)
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.zaxxer:HikariCP:5.0.1")
    
    // Database Drivers
    implementation("com.h2database:h2:2.1.214")         // Development
    implementation("org.postgresql:postgresql:42.6.0")  // Production
    
    // AI API Clients
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.mockk:mockk:1.13.8")
}

// Essential for deployment
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.yourapp.ApplicationKt"
    }
    from(configurations.runtimeClasspath.get().map { 
        if (it.isDirectory) it else zipTree(it) 
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

---

## 🛠️ Common Development Tasks

### Local Development
```bash
# Run locally
./gradlew run

# Run tests
./gradlew test

# Run code quality analysis
./gradlew detekt

# Run tests with coverage
./gradlew test jacocoTestReport

# Combined quality check
./gradlew test detekt jacocoTestReport

# Build JAR
./gradlew build

# Clean build
./gradlew clean build
```

### Testing Endpoints
```bash
# Health check
curl http://localhost:8080/health

# API status  
curl http://localhost:8080/api/v1/status

# AI chat (requires JWT token)
curl -X POST http://localhost:8080/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"message": "Hello AI!"}'
```

### Database Operations
```kotlin
// Transaction wrapper for Exposed
suspend fun <T> dbQuery(block: suspend () -> T): T = 
    newSuspendedTransaction(Dispatchers.IO) { block() }

// Usage example
suspend fun createCustomer(name: String, email: String): Customer = dbQuery {
    CustomerTable.insert {
        it[CustomerTable.name] = name
        it[CustomerTable.email] = email
    }
    // Return created customer...
}
```

---

## 🔄 CI/CD Pipeline

### Current Automation
- ✅ **Automated testing** on every PR with JUnit 5 + Kotest
- ✅ **Code quality analysis** using Detekt with backend-specific rules
- ✅ **Test coverage reporting** with JaCoCo (80% minimum threshold)
- ✅ **Comprehensive PR comments** with test results and artifact links
- ✅ **JAR build verification** for deployment readiness
- ✅ **Artifact uploads** with 7-day retention for detailed analysis
- 🔄 Railway deployment on main branch (configured)
- 🔄 Production environment management (planned)

### Quality Gates
- ✅ **Code quality** (Detekt static analysis)
- ✅ **Test execution** with detailed pass/fail reporting
- ✅ **Coverage verification** (80% minimum with JaCoCo)
- ✅ **Build verification** (Fat JAR generation)
- ✅ **Dependency security** (implicit via Gradle)

### 📊 What You Get in PRs
- **Automated test result comments** with coverage statistics
- **Code quality analysis** with issue counts and recommendations
- **Direct links to detailed reports** via artifacts
- **Coverage reports** showing tested vs untested code
- **Clear action items** when quality gates fail

---

## 🤖 AI Integration Details

### Supported Providers
1. **Claude 3.5 Sonnet** (Primary) - $3/$15 per million tokens
2. **OpenAI GPT-4o mini** (Alternative) - $0.15/$0.60 per million tokens  
3. **Google Gemini 1.5 Flash** (Budget) - $0.07/$0.30 per million tokens

### AI Service Pattern
```kotlin
// Multi-provider setup with fallback
class AIService(private val config: AIConfig) {
    suspend fun chat(message: String): String {
        return try {
            callPrimaryProvider(message)
        } catch (e: Exception) {
            log.warn("Primary AI provider failed, using fallback", e)
            callFallbackProvider(message)
        }
    }
}
```

### Cost Management
```kotlin
// Usage tracking example
object AIUsageTracker {
    private val dailyRequests = AtomicInteger(0)
    private val dailyLimit = 1000
    
    fun canMakeRequest(): Boolean {
        return dailyRequests.get() < dailyLimit
    }
    
    fun trackRequest() {
        dailyRequests.incrementAndGet()
    }
}
```

---

## 🔍 Domain-Driven Design Patterns

### Entity Pattern
```kotlin
@JvmInline
value class CustomerId(val value: Long)

@Serializable
data class Customer(
    val id: CustomerId,
    val name: String,
    val email: Email,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // Domain methods
    fun canPlaceOrder(): Boolean = email.value.isNotEmpty()
}
```

### Repository Pattern  
```kotlin
// Domain layer interface
interface CustomerRepository {
    suspend fun save(customer: Customer): Customer
    suspend fun findById(id: CustomerId): Customer?
    suspend fun findByEmail(email: Email): Customer?
}

// Infrastructure implementation
class CustomerRepositoryImpl : CustomerRepository {
    override suspend fun save(customer: Customer): Customer = dbQuery {
        // Exposed ORM implementation
    }
}
```

### Service Layer
```kotlin
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val eventPublisher: EventPublisher
) {
    suspend fun createCustomer(name: String, email: String): Customer {
        val customer = Customer(
            id = CustomerId(0),
            name = name,
            email = Email(email)
        )
        
        val savedCustomer = customerRepository.save(customer)
        eventPublisher.publish(CustomerCreatedEvent(savedCustomer.id))
        
        return savedCustomer
    }
}
```

---

## 🚀 Deployment Configuration

### Railway Deployment
```yaml
# railway.json (optional)
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS"
  },
  "deploy": {
    "startCommand": "java -jar build/libs/*-all.jar",
    "healthcheckPath": "/health"
  }
}
```

### Docker Support
```dockerfile
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . /app
RUN gradle build --no-daemon

FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### Environment Detection
```kotlin
enum class Environment {
    DEVELOPMENT, STAGING, PRODUCTION;
    
    companion object {
        fun current(): Environment {
            return when (System.getenv("APP_ENVIRONMENT")?.uppercase()) {
                "PRODUCTION", "PROD" -> PRODUCTION
                "STAGING", "STAGE" -> STAGING
                else -> DEVELOPMENT
            }
        }
    }
}
```

---

## 🧪 Testing Patterns

### API Testing
```kotlin
@Test
fun testHealthEndpoint() = testApplication {
    client.get("/health").apply {
        assertEquals(HttpStatusCode.OK, status)
        val response = Json.decodeFromString<Map<String, Any>>(bodyAsText())
        assertEquals("healthy", response["status"])
    }
}

@Test  
fun testAIChatEndpoint() = testApplication {
    client.post("/api/v1/ai/chat") {
        header("Authorization", "Bearer $testJwtToken")
        header("Content-Type", "application/json")
        setBody("""{"message": "Test"}""")
    }.apply {
        assertEquals(HttpStatusCode.OK, status)
    }
}
```

### Service Testing
```kotlin
@Test
suspend fun testCustomerService() {
    val mockRepository = mockk<CustomerRepository>()
    val service = CustomerService(mockRepository, mockk())
    
    every { mockRepository.save(any()) } returns testCustomer
    
    val result = service.createCustomer("John", "john@example.com")
    assertEquals("John", result.name)
}
```

---

## 🚨 Common Issues & Solutions

### Database Connection Issues
```kotlin
// Always check DATABASE_URL format
// PostgreSQL: jdbc:postgresql://host:port/database?user=username&password=password
// H2: jdbc:h2:mem:test;DB_CLOSE_DELAY=-1

// Connection pool exhaustion
maximumPoolSize = 5  // Start small for development
connectionTimeout = 30000
idleTimeout = 600000
```

### CORS Issues
```kotlin
// Development CORS setup
install(CORS) {
    allowHost("localhost:3000")      // React dev server
    allowHost("localhost:3001")      // Alternative dev port
    allowHost("127.0.0.1:3000")     // Alternative localhost
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)
}
```

### JWT Token Issues
```kotlin
// Ensure JWT secret is properly configured
val jwtSecret = System.getenv("JWT_SECRET") 
    ?: throw IllegalStateException("JWT_SECRET environment variable is required")

// Token validation debugging
validate { credential ->
    val username = credential.payload.getClaim("username").asString()
    log.debug("Validating JWT for user: $username")
    if (username.isNotEmpty()) JWTPrincipal(credential.payload) else null
}
```

---

## 📊 Monitoring & Observability

### Health Checks
```kotlin
get("/health") {
    val dbHealthy = try {
        dbQuery { CustomerTable.selectAll().limit(1).count() >= 0 }
    } catch (e: Exception) { false }
    
    call.respond(mapOf(
        "status" to if (dbHealthy) "healthy" else "unhealthy",
        "timestamp" to System.currentTimeMillis(),
        "version" to "1.0.0",
        "database" to dbHealthy,
        "environment" to Environment.current().name
    ))
}
```

### Error Tracking
```kotlin
// Sentry integration (free: 5K errors/month)
install(StatusPages) {
    exception<Throwable> { call, cause ->
        log.error("Unhandled exception", cause)
        // Sentry.captureException(cause) // Enable in production
        
        when (cause) {
            is IllegalArgumentException -> {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
            }
            else -> {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }
    }
}
```

---

## 💡 AI Assistant Guidelines

When working with this backend:

1. **Always use the established project structure** - follow DDD patterns
2. **Include environment variable support** for all configurations
3. **Provide both H2 and PostgreSQL support** in database code
4. **Include proper error handling** and status pages
5. **Follow security best practices** - JWT, CORS, rate limiting
6. **Consider cost implications** when adding AI features
7. **Use Exposed ORM patterns** for database operations
8. **Include comprehensive testing** for new features (CI validates automatically)
9. **Document environment variables** needed
10. **Consider deployment requirements** (JAR configuration, Railway compatibility)
11. **NEW**: **Monitor CI/CD feedback** - check PR comments for test results and code quality
12. **NEW**: **Maintain test coverage** - aim for 80%+ (enforced by JaCoCo)
13. **NEW**: **Address detekt issues** - follow code quality recommendations

### Code Generation Preferences
- Use `suspend` functions for database operations
- Prefer `@Serializable` data classes for API models
- Include input validation for all endpoints
- Use dependency injection patterns (constructor injection)
- Follow Kotlin coding conventions
- Include proper logging statements

### Security Checklist
- ✅ JWT token validation on protected routes
- ✅ Input sanitization and validation
- ✅ Rate limiting on public endpoints  
- ✅ CORS configuration for allowed origins
- ✅ Security headers on all responses
- ✅ Environment variables for secrets
- ✅ Database query parameterization (Exposed handles this)
- ✅ **NEW**: Automated security scanning in CI/CD (via dependency analysis)
- ✅ **NEW**: Code quality enforcement (detekt security rules)

This backend is production-ready with AI integration, following industry best practices for scalability, security, and maintainability.