# Phase 2: Backend Development & Deployment (Time: 60 minutes)

## Contents

- [Step 1: Test Backend Locally](#step-1-test-backend-locally)
- [Step 2: Deploy to Railway](#step-2-deploy-to-railway)
- [Step 3: Set Up CI/CD Pipeline](#step-3-set-up-cicd-pipeline)

---

### Step 1: Test Backend Locally

**Test with Development Environment:**
```bash
# Run development server (uses .env.dev)
./scripts/development/run-dev.sh

# OR manually:
export $(cat .env.dev | xargs)
cd backend && ./gradlew runDev

# Test endpoints
curl http://localhost:8080/health
curl http://localhost:8080/info  # Shows environment info
curl http://localhost:8080/api/v1/status
```

**Test with Staging Configuration Locally:**
```bash
# Run with staging config (uses .env.staging)
./scripts/development/run-staging.sh

# OR manually:
export $(cat .env.staging | xargs)
cd backend && ./gradlew runProd
```

**Run Tests:**
```bash
# Run tests in development mode
./scripts/development/test-dev.sh

# OR manually:
export $(cat .env.dev | xargs)
cd backend && ./gradlew test
```

**Environment-specific Testing:**
```bash
# Test development environment response
curl http://localhost:8080/info
# Should return: {"environment":"development","ai_provider":"claude","debug_mode":true,...}

# Test health check
curl http://localhost:8080/health
# Should return: {"status":"healthy","environment":"development","build_variant":"dev",...}
```

### Step 2: Deploy to Railway

**First, set up Railway environments:**

1. **Install Railway CLI and setup environments**:
   ```bash
   # Install Railway CLI
   npm install -g @railway/cli

   # Setup environments
   chmod +x scripts/deployment/setup-railway-environments.sh
   ./scripts/deployment/setup-railway-environments.sh
   ```

2. **Configure Environment Variables**:

   **For Staging:**
   ```bash
   railway environment use staging
   railway variables set JWT_SECRET_STAGING=your-super-secret-jwt-key-minimum-32-characters-staging
   railway variables set CLAUDE_API_KEY_STAGING=your_claude_api_key_for_staging
   railway variables set OPENAI_API_KEY_STAGING=your_openai_api_key_for_staging  # optional
   railway variables set GEMINI_API_KEY_STAGING=your_gemini_api_key_for_staging  # optional
   ```

   **For Production:**
   ```bash
   railway environment use production
   railway variables set JWT_SECRET=your-super-secret-jwt-key-minimum-32-characters-production
   railway variables set CLAUDE_API_KEY=your_claude_api_key_for_production
   railway variables set OPENAI_API_KEY=your_openai_api_key_for_production      # optional
   railway variables set GEMINI_API_KEY=your_gemini_api_key_for_production      # optional
   railway variables set ALLOWED_ORIGINS=yourapp.com,www.yourapp.com
   ```

3. **Deploy to Different Environments**:

   **Deploy to Staging:**
   ```bash
   # Deploy to staging (for testing)
   ./scripts/deployment/railway-staging.sh
   ```

   **Deploy to Production:**
   ```bash
   # Deploy to production (requires confirmation)
   ./scripts/deployment/railway-production.sh
   ```

4. **Environment URLs**:
   - **Staging**: `https://my-kotlin-project-staging.up.railway.app`
   - **Production**: `https://my-kotlin-project-production.up.railway.app`

### Step 3: Set Up CI/CD Pipeline

**Create `.github/workflows/ci.yml`:**
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

env:
  JAVA_VERSION: '17'
  JAVA_DISTRIBUTION: 'temurin'

jobs:
  test:
    name: Test & Quality Checks
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
        ports: [5432:5432]
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK ${{ env.JAVA_VERSION }}
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: ${{ env.JAVA_DISTRIBUTION }}

    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3
      with:
        cache-read-only: ${{ github.event_name == 'pull_request' }}

    - name: Grant execute permission for gradlew
      run: chmod +x backend/gradlew

    - name: Run backend tests (Development)
      run: |
        cd backend
        ./gradlew test
      env:
        BUILD_VARIANT: dev
        APP_ENVIRONMENT: development
        KTOR_ENV: development
        DATABASE_URL_DEV: jdbc:postgresql://localhost:5432/testdb?user=testuser&password=testpass
        JWT_SECRET_DEV: test-secret-key-for-ci-pipeline-development-testing
        CLAUDE_API_KEY_DEV: test-key  # Mock for testing

    - name: Build backend (Development)
      run: |
        cd backend
        ./gradlew buildDev

    - name: Run backend tests (Production Build)
      run: |
        cd backend
        ./gradlew test
      env:
        BUILD_VARIANT: prod
        APP_ENVIRONMENT: production
        KTOR_ENV: production
        DATABASE_URL: jdbc:postgresql://localhost:5432/testdb?user=testuser&password=testpass
        JWT_SECRET: test-secret-key-for-ci-pipeline-production-testing
        CLAUDE_API_KEY: test-key  # Mock for testing

    - name: Build backend (Production)
      run: |
        cd backend
        ./gradlew buildProd

    - name: Upload build artifacts
      uses: actions/upload-artifact@v4
      with:
        name: backend-jar-${{ github.sha }}
        path: backend/build/libs/*.jar
        retention-days: 5

  security-scan:
    name: Security Scan
    runs-on: ubuntu-latest
    needs: test
    if: github.event_name == 'push'

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK ${{ env.JAVA_VERSION }}
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: ${{ env.JAVA_DISTRIBUTION }}

    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3

    - name: Run dependency check
      run: |
        cd backend
        chmod +x gradlew
        ./gradlew dependencyCheckAnalyze --info || true

    - name: Upload security scan results
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: security-scan-${{ github.sha }}
        path: backend/build/reports/dependency-check-report.html

  deploy-staging:
    name: Deploy to Staging
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    if: github.ref == 'refs/heads/develop' && github.event_name == 'push'
    environment: staging

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Download build artifacts
      uses: actions/download-artifact@v4
      with:
        name: backend-jar-${{ github.sha }}
        path: backend/build/libs/

    - name: Install Railway CLI
      run: npm install -g @railway/cli

    - name: Deploy to Railway Staging
      run: |
        railway login --token ${{ secrets.RAILWAY_TOKEN }}
        railway environment use staging
        railway up --detach
      env:
        RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}

    - name: Verify staging deployment
      run: |
        echo "Staging deployment completed"
        echo "Staging URL: https://my-kotlin-project-staging.up.railway.app"
        echo "Health check in 30 seconds..."
        sleep 30
        curl -f https://my-kotlin-project-staging.up.railway.app/health || echo "Health check failed - deployment may still be starting"

  deploy-production:
    name: Deploy to Production
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    environment: production

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Download build artifacts
      uses: actions/download-artifact@v4
      with:
        name: backend-jar-${{ github.sha }}
        path: backend/build/libs/

    - name: Install Railway CLI
      run: npm install -g @railway/cli

    - name: Deploy to Railway Production
      run: |
        railway login --token ${{ secrets.RAILWAY_TOKEN }}
        railway environment use production
        railway up --detach
      env:
        RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}

    - name: Verify production deployment
      run: |
        echo "Production deployment completed"
        echo "Production URL: https://my-kotlin-project-production.up.railway.app"
        echo "Health check in 60 seconds..."
        sleep 60
        curl -f https://my-kotlin-project-production.up.railway.app/health || echo "Health check failed - deployment may still be starting"

  # Future: mobile tests and builds
  # mobile-test:
  #   name: Mobile Tests
  #   runs-on: macos-latest
  #   steps:
  #   - uses: actions/checkout@v4
  #   - name: Run mobile tests
  #     run: echo "Mobile tests coming soon"
```
