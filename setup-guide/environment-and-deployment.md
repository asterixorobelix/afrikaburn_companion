# Environment & Deployment Reference

## Contents

- [Essential Environment Variables](#essential-environment-variables)
  - [Development Environment (.env.dev)](#development-environment-envdev)
  - [Staging Environment (.env.staging)](#staging-environment-envstaging)
  - [Production Environment (.env.prod)](#production-environment-envprod)
- [Quick Deploy Commands](#quick-deploy-commands)
  - [Local Development](#local-development)
  - [Test API Endpoints](#test-api-endpoints)
  - [Deploy to Railway](#deploy-to-railway)
  - [Test Deployed Environments](#test-deployed-environments)
  - [Environment Comparison](#environment-comparison)
- [Cost Management](#cost-management)
  - [Monthly Cost Estimates by Environment](#monthly-cost-estimates-by-environment)
  - [Cost Optimization by Environment](#cost-optimization-by-environment)
  - [Environment-Specific Monitoring](#environment-specific-monitoring)
  - [Cost Alerts Setup](#cost-alerts-setup)

---

## Essential Environment Variables

### Development Environment (.env.dev)

```env
# Server Configuration
BUILD_VARIANT=dev
APP_ENVIRONMENT=development
KTOR_ENV=development
PORT=8080

# Database Configuration (Development - H2 for simplicity)
DATABASE_URL_DEV=jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
DATABASE_USER_DEV=sa
DATABASE_PASSWORD_DEV=
DB_POOL_SIZE_DEV=5

# JWT Security (Development - Less secure for testing)
JWT_SECRET_DEV=development-jwt-secret-minimum-32-characters-for-dev-only
JWT_ISSUER_DEV=your-app-dev
JWT_AUDIENCE_DEV=your-app-users-dev

# AI API Configuration (Development - Lower token limits)
AI_PRIMARY_PROVIDER_DEV=claude
CLAUDE_API_KEY_DEV=your_claude_api_key_for_dev_here
OPENAI_API_KEY_DEV=your_openai_api_key_for_dev_here
GEMINI_API_KEY_DEV=your_gemini_api_key_for_dev_here
```

### Staging Environment (.env.staging)

```env
# Server Configuration
BUILD_VARIANT=staging
APP_ENVIRONMENT=staging
KTOR_ENV=staging
PORT=8080

# Database Configuration (Staging - PostgreSQL)
DATABASE_URL_STAGING=postgresql://user:pass@staging-host:5432/staging_db
DATABASE_USER_STAGING=staging_user
DATABASE_PASSWORD_STAGING=staging_password
DB_POOL_SIZE_STAGING=10

# JWT Security (Staging - Production-like security)
JWT_SECRET_STAGING=staging-jwt-secret-minimum-32-characters-long-for-staging
JWT_ISSUER_STAGING=your-app-staging
JWT_AUDIENCE_STAGING=your-app-users-staging

# AI API Configuration (Staging - Medium token limits)
AI_PRIMARY_PROVIDER_STAGING=claude
CLAUDE_API_KEY_STAGING=your_claude_api_key_for_staging_here
OPENAI_API_KEY_STAGING=your_openai_api_key_for_staging_here
GEMINI_API_KEY_STAGING=your_gemini_api_key_for_staging_here
```

### Production Environment (.env.prod)

```env
# Server Configuration
BUILD_VARIANT=prod
APP_ENVIRONMENT=production
KTOR_ENV=production
PORT=8080

# Database Configuration (Production - PostgreSQL with connection pooling)
DATABASE_URL=postgresql://user:pass@prod-host:5432/prod_db
DATABASE_USER=prod_user
DATABASE_PASSWORD=prod_password
DB_POOL_SIZE=20

# JWT Security (Production - Maximum security)
JWT_SECRET=production-jwt-secret-minimum-32-characters-long-CHANGE-THIS
JWT_ISSUER=your-app
JWT_AUDIENCE=your-app-users

# AI API Configuration (Production - High token limits)
AI_PRIMARY_PROVIDER=claude
CLAUDE_API_KEY=your_claude_api_key_for_production_here
OPENAI_API_KEY=your_openai_api_key_for_production_here
GEMINI_API_KEY=your_gemini_api_key_for_production_here

# Security & Monitoring (Production)
ALLOWED_ORIGINS=yourapp.com,www.yourapp.com,app.yourapp.com
SENTRY_DSN=your_sentry_dsn_for_production_environment
```

---

## Quick Deploy Commands

### Local Development
```bash
# Start development server
./scripts/development/run-dev.sh

# Start with staging configuration (locally)
./scripts/development/run-staging.sh

# Run tests
./scripts/development/test-dev.sh
```

### Test API Endpoints
```bash
# Test development environment
curl http://localhost:8080/health
curl http://localhost:8080/info
curl http://localhost:8080/api/v1/status

# Test AI endpoint (when implemented)
curl -X POST http://localhost:8080/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello AI!"}'
```

### Deploy to Railway
```bash
# Setup Railway environments (one-time)
./scripts/deployment/setup-railway-environments.sh

# Deploy to staging
./scripts/deployment/railway-staging.sh

# Deploy to production (with confirmation)
./scripts/deployment/railway-production.sh
```

### Test Deployed Environments
```bash
# Test staging deployment
curl https://my-kotlin-project-staging.up.railway.app/health
curl https://my-kotlin-project-staging.up.railway.app/info

# Test production deployment
curl https://my-kotlin-project-production.up.railway.app/health
curl https://my-kotlin-project-production.up.railway.app/info
```

### Environment Comparison
```bash
# Development
curl http://localhost:8080/info
# Returns: {"environment":"development","debug_mode":true,...}

# Staging
curl https://my-kotlin-project-staging.up.railway.app/info
# Returns: {"environment":"staging","ai_provider":"claude",...}

# Production
curl https://my-kotlin-project-production.up.railway.app/info
# Returns: {"environment":"production","version":"1.0.0",...}
```

---

## Cost Management

### Monthly Cost Estimates by Environment

| Component | Development | Staging | Production |
|-----------|-------------|---------|------------|
| **Railway Hosting** | Free (local) | $5/month | $5-20/month |
| **PostgreSQL** | Free (H2) | $5/month | $5-15/month |
| **Claude API** | $1-5/month | $5-15/month | $20-100/month |
| **Monitoring** | Free tools | Free tools | $0-26/month |

**Environment-Specific Totals:**
- **Development**: $1-5/month (mostly local, minimal API usage)
- **Staging**: $15-25/month (testing and validation)
- **Production**: $30-160/month (full features, real usage)

### Cost Optimization by Environment

#### Development Environment
- **Use H2 database** (free, in-memory)
- **Lower AI token limits** (1000 tokens max)
- **Cheaper AI models** (GPT-4o mini, Gemini Flash)
- **Minimal logging** (reduce storage costs)
- **Local development** (no hosting costs)

#### Staging Environment
- **Shared PostgreSQL** (small instance)
- **Medium AI token limits** (2000 tokens max)
- **Mid-tier AI models** (Claude 3.5 Sonnet)
- **Moderate logging** (info level)
- **Single Railway instance** ($5/month)

#### Production Environment
- **Optimized PostgreSQL** (connection pooling)
- **High AI token limits** (4000 tokens max)
- **Best AI models** (Claude 3.5 Sonnet, GPT-4o)
- **Minimal logging** (warn level, performance)
- **Scalable Railway deployment** ($5-20/month)

### Environment-Specific Monitoring

#### Track API Usage by Environment
```kotlin
// Add to AIService.kt
class AIUsageTracker(private val environment: Environment) {
    private val dailyUsage = mutableMapOf<String, AtomicInteger>()

    fun trackUsage(provider: AIProvider, tokens: Int) {
        val limits = when (environment) {
            Environment.DEVELOPMENT -> 10_000   // 10K tokens/day
            Environment.STAGING -> 50_000      // 50K tokens/day
            Environment.PRODUCTION -> 200_000  // 200K tokens/day
        }

        val key = "${provider.name}-${LocalDate.now()}"
        val usage = dailyUsage.getOrPut(key) { AtomicInteger(0) }

        if (usage.addAndGet(tokens) > limits) {
            // Log warning or switch to fallback provider
        }
    }
}
```

### Cost Alerts Setup

#### Environment-Specific Monitoring
```env
# Development - Conservative limits
MAX_DAILY_API_CALLS_DEV=100
MAX_AI_TOKENS_DAILY_DEV=10000

# Staging - Moderate limits
MAX_DAILY_API_CALLS_STAGING=500
MAX_AI_TOKENS_DAILY_STAGING=50000

# Production - High limits with monitoring
MAX_DAILY_API_CALLS_PROD=5000
MAX_AI_TOKENS_DAILY_PROD=200000
COST_ALERT_EMAIL=admin@yourapp.com
```
