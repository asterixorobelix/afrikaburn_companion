# AI Assistant Reference

## Contents

- [AI Assistant Guidelines](#ai-assistant-guidelines)
- [Environment-Specific Development Practices](#environment-specific-development-practices)
- [Environment Testing Checklist](#environment-testing-checklist)
- [Quick Reference Commands](#quick-reference-commands)
- [Important Notes for AI Assistants](#important-notes-for-ai-assistants)

---

## AI Assistant Guidelines

When working with this project structure:

1. **Always use environment-aware configuration** - Check current environment and adjust behavior accordingly
2. **Implement proper environment separation** - Use dev/staging/prod specific API keys and settings
3. **Include proper error handling** for all AI API calls with environment-specific fallback logic
4. **Monitor costs carefully** with usage tracking per environment
5. **Test in development first** before deploying to staging, then production
6. **Keep API keys secure** - never commit to git, use environment-specific variables
7. **Use environment variables** for all configuration, with environment-specific fallbacks
8. **Follow the established project structure** - maintain separation between environments
9. **Include both backend and mobile considerations** - ensure mobile apps can connect to different environment backends
10. **Document all AI integrations clearly** - specify environment requirements and configurations

## Environment-Specific Development Practices

### Development Environment
- **Use lower token limits** to save costs during development
- **Enable verbose logging** for debugging
- **Use H2 database** for simplicity
- **Allow permissive CORS** for frontend development
- **Mock AI responses** when API keys not available

### Staging Environment
- **Mirror production configuration** but with staging-specific keys
- **Enable detailed monitoring** for testing
- **Use moderate resource limits**
- **Test production-like scenarios**
- **Validate CI/CD pipeline**

### Production Environment
- **Strict security headers** and CORS policies
- **Minimal logging** for performance
- **Optimized resource usage**
- **Real monitoring and alerting**
- **High availability configuration**

## Environment Testing Checklist

Before deploying to any environment, verify:
- [ ] Environment variables are correctly set
- [ ] Database connections work
- [ ] AI API keys are valid and have appropriate limits
- [ ] Security headers are appropriate for environment
- [ ] Logging levels are correct
- [ ] CORS policies match frontend requirements
- [ ] Health checks return expected environment information

---

## Quick Reference Commands

### Project Setup
```bash
# Initial setup (creates all environment files)
./scripts/development/setup.sh

# Setup Railway environments (one-time)
./scripts/deployment/setup-railway-environments.sh
```

### Local Development
```bash
# Development environment
./scripts/development/run-dev.sh
# OR: export $(cat .env.dev | xargs) && cd backend && ./gradlew runDev

# Staging configuration (local)
./scripts/development/run-staging.sh
# OR: export $(cat .env.staging | xargs) && cd backend && ./gradlew runProd

# Run tests
./scripts/development/test-dev.sh
# OR: export $(cat .env.dev | xargs) && cd backend && ./gradlew test
```

### Build Commands
```bash
cd backend

# Development build
./gradlew buildDev

# Production build
./gradlew buildProd

# Run specific environment
./gradlew runDev    # Development
./gradlew runProd   # Production
```

### Deployment Commands
```bash
# Deploy to staging
./scripts/deployment/railway-staging.sh

# Deploy to production (with confirmation)
./scripts/deployment/railway-production.sh

# Manual Railway commands
railway environment use staging && railway up
railway environment use production && railway up
```

### Testing Commands
```bash
# Local endpoints
curl http://localhost:8080/health
curl http://localhost:8080/info
curl http://localhost:8080/api/v1/status

# Staging endpoints
curl https://my-kotlin-project-staging.up.railway.app/health
curl https://my-kotlin-project-staging.up.railway.app/info

# Production endpoints
curl https://my-kotlin-project-production.up.railway.app/health
curl https://my-kotlin-project-production.up.railway.app/info

# AI endpoint test
curl -X POST http://localhost:8080/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Test message"}'
```

### Environment Management
```bash
# Switch between environment files
ln -sf .env.dev .env      # Use development
ln -sf .env.staging .env  # Use staging
ln -sf .env.prod .env     # Use production

# Load specific environment
export $(cat .env.dev | xargs)     # Development
export $(cat .env.staging | xargs) # Staging
export $(cat .env.prod | xargs)    # Production
```

### Mobile Development (When Ready)
```bash
# Uncomment mobile modules in settings.gradle.kts
# Then run:
cd mobile && ./gradlew assembleDebug           # Android
cd mobile && ./gradlew iosSimulatorArm64Test  # iOS Simulator
```

### Database Commands
```bash
# Development (H2 - automatic)
# No setup required

# Staging/Production (PostgreSQL via Railway)
railway environment use staging
railway connect postgres  # Connect to staging DB

railway environment use production
railway connect postgres  # Connect to production DB
```

### Monitoring Commands
```bash
# View Railway logs
railway logs --environment staging
railway logs --environment production

# Check environment status
railway status --environment staging
railway status --environment production
```

---

## Important Notes for AI Assistants

### Environment-Aware Development Principles

1. **This guide is optimized for multi-environment development** - Always consider dev/staging/prod implications when making suggestions
2. **Environment separation is critical** - Use environment-specific API keys, databases, and configurations
3. **Cost optimization varies by environment** - Development should minimize costs, production should optimize performance
4. **Security increases with environment maturity** - Development is permissive, production is strict
5. **Testing strategy is environment-dependent** - Test in dev, validate in staging, monitor in production

### Environment-Specific Configurations

#### Development Environment
- **Purpose**: Local development and debugging
- **Database**: H2 in-memory (free, fast setup)
- **AI Models**: Cheaper options (GPT-4o mini, Gemini Flash)
- **Token Limits**: Low (1000 tokens) to control costs
- **Logging**: Verbose (DEBUG level)
- **Security**: Permissive CORS, optional JWT
- **Features**: Debug endpoints, usage tracking, config inspection

#### Staging Environment
- **Purpose**: Pre-production testing and validation
- **Database**: PostgreSQL (production-like)
- **AI Models**: Mid-tier options (Claude 3.5 Sonnet)
- **Token Limits**: Medium (2000 tokens)
- **Logging**: Moderate (INFO level)
- **Security**: Production-like with staging-specific keys
- **Features**: Limited debug endpoints, monitoring

#### Production Environment
- **Purpose**: Live user-facing application
- **Database**: Optimized PostgreSQL with connection pooling
- **AI Models**: Best available (Claude 3.5 Sonnet, GPT-4o)
- **Token Limits**: High (4000 tokens) for full functionality
- **Logging**: Minimal (WARN level) for performance
- **Security**: Strict CORS, mandatory JWT, security headers
- **Features**: No debug endpoints, full monitoring

### Critical Development Guidelines

#### Environment Variable Management
- **Never mix environment variables** between dev/staging/prod
- **Use environment-specific fallbacks** in configuration
- **Validate required variables** per environment
- **Document environment-specific requirements**

#### Testing Strategy
```bash
# Development Testing
export $(cat .env.dev | xargs) && cd backend && ./gradlew test

# Staging Validation
export $(cat .env.staging | xargs) && cd backend && ./gradlew test

# Production Readiness
export $(cat .env.prod | xargs) && cd backend && ./gradlew buildProd
```

#### Cost Management Rules
- **Development**: Minimize AI API calls, use mocks when possible
- **Staging**: Monitor usage closely, set conservative limits
- **Production**: Optimize for performance, implement proper caching

#### Security Progression
- **Development**: Focus on functionality over security
- **Staging**: Implement production-like security measures
- **Production**: Maximum security, regular audits

### Mobile App Environment Considerations

#### Backend Environment Connections
- **Development**: Mobile connects to localhost:8080
- **Staging**: Mobile connects to staging Railway URL
- **Production**: Mobile connects to production Railway URL

#### Environment Detection in Mobile
```kotlin
// Platform-specific environment detection
expect fun getCurrentEnvironment(): AppEnvironment

// iOS Implementation
actual fun getCurrentEnvironment(): AppEnvironment {
    return if (DEBUG) AppEnvironment.DEVELOPMENT else AppEnvironment.PRODUCTION
}

// Android Implementation
actual fun getCurrentEnvironment(): AppEnvironment {
    return if (BuildConfig.DEBUG) AppEnvironment.DEVELOPMENT else AppEnvironment.PRODUCTION
}
```

### Deployment Best Practices

#### Progressive Deployment Strategy
1. **Develop locally** with development environment
2. **Test thoroughly** before deploying to staging
3. **Validate in staging** with production-like data
4. **Deploy to production** only after staging validation
5. **Monitor production** deployment for issues

#### CI/CD Environment Handling
- **Pull Requests**: Test with development configuration
- **Develop Branch**: Auto-deploy to staging
- **Main Branch**: Manual deploy to production with confirmation
- **Feature Branches**: Local testing only

#### Environment-Specific Secrets
```bash
# Development Secrets (can be less secure)
JWT_SECRET_DEV=dev-secret-minimum-32-chars
CLAUDE_API_KEY_DEV=dev-claude-key

# Staging Secrets (production-like)
JWT_SECRET_STAGING=staging-secret-minimum-32-chars-secure
CLAUDE_API_KEY_STAGING=staging-claude-key

# Production Secrets (maximum security)
JWT_SECRET=production-secret-minimum-32-chars-maximum-security
CLAUDE_API_KEY=production-claude-key
```

### Repository Structure Benefits

#### Unified Context for AI Assistants
- **Single repository** contains both backend and mobile code
- **Shared domain models** ensure consistency
- **Environment configurations** are centralized
- **AI assistants** have full context of both components

#### Environment Consistency
- **Configuration patterns** are consistent across backend/mobile
- **API contracts** are synchronized between environments
- **Testing strategies** cover full stack in each environment
- **Deployment** maintains environment separation

### Troubleshooting by Environment

#### Development Issues
- **Check .env.dev file** for correct configuration
- **Verify H2 database** starts properly
- **Confirm API keys** are set (can be test keys)
- **Enable debug logging** for detailed information

#### Staging Issues
- **Validate staging database** connection
- **Check staging API keys** and quotas
- **Review CI/CD pipeline** logs
- **Compare with development** configuration

#### Production Issues
- **Check production environment** variables in Railway
- **Monitor production logs** (minimal due to WARN level)
- **Verify database** connection and performance
- **Review security headers** and CORS policies

This comprehensive environment separation ensures safe development practices while maintaining production reliability and cost efficiency.
