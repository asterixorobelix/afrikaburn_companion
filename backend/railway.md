# Deploy Kotlin Backend to Railway - 5 Minute Guide


## Contents

- [Prerequisites](#prerequisites)
- [Step 1: Prepare Your Project](#step-1-prepare-your-project)
- [Step 2: Create Railway Service](#step-2-create-railway-service)
- [Step 3: Configure Environment Variables](#step-3-configure-environment-variables)
- [Step 4: Add Database (Optional)](#step-4-add-database-optional)
- [Step 5: Deploy](#step-5-deploy)
- [Step 6: Set Up Custom Domain (Optional)](#step-6-set-up-custom-domain-optional)
- [Automatic Deployments](#automatic-deployments)
- [Environment-Specific Configuration](#environment-specific-configuration)
- [Monitoring](#monitoring)
- [Scaling](#scaling)
- [Cost](#cost)
- [Troubleshooting](#troubleshooting)
- [One-Click Deploy Button](#one-click-deploy-button)
- [Next Steps](#next-steps)

## Prerequisites
- GitHub repository with your Kotlin backend
- Railway account (sign up with GitHub at railway.app)

## Step 1: Prepare Your Project

Add this to your `build.gradle.kts` to ensure proper JAR building:

```kotlin
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.ApplicationKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

## Step 2: Create Railway Service

1. Go to [railway.app](https://railway.app)
2. Click **"Start a New Project"**
3. Select **"Deploy from GitHub repo"**
4. Choose your Kotlin backend repository
5. Railway automatically detects it's a Gradle project

## Step 3: Configure Environment Variables

In the Railway dashboard, go to **Variables** tab and add:

```env
PORT=8080
DATABASE_URL=${{Postgres.DATABASE_URL}}
JWT_SECRET=your-super-secret-jwt-key-here
APP_ENVIRONMENT=production
```

## Step 4: Add Database (Optional)

If you need PostgreSQL:
1. Click **"New"** → **"Database"** → **"Add PostgreSQL"**
2. Railway automatically creates `DATABASE_URL` variable
3. Your app can use `${{Postgres.DATABASE_URL}}` to reference it

## Step 5: Deploy

Railway automatically deploys when you push to your main branch!

**First deployment:**
- Click **"Deploy"** in the Railway dashboard
- Watch the build logs
- Your app will be available at: `https://your-app-name.up.railway.app`

## Step 6: Set Up Custom Domain (Optional)

1. Go to **Settings** → **Domains**
2. Click **"Custom Domain"**
3. Enter your domain (e.g., `api.yoursite.com`)
4. Update DNS records as shown

## Automatic Deployments

Railway automatically redeploys when you push to GitHub:

```bash
git add .
git commit -m "Update API"
git push origin main
# Railway deploys automatically!
```

## Environment-Specific Configuration

Update your `application.conf` for Railway:

```hocon
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
    }
}

database {
    url = ${?DATABASE_URL}
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
}
```

## Monitoring

Railway provides:
- **Real-time logs**: Click "View Logs"
- **Metrics**: CPU, Memory, Network usage
- **Health checks**: Automatic uptime monitoring

## Scaling

To handle more traffic:
1. Go to **Settings** → **Resources**
2. Increase memory allocation
3. Enable auto-scaling (paid plans)

## Cost

- **Free tier**: 500 hours/month (enough for development)
- **Pro**: $5/month per service (unlimited hours)
- **Database**: $5/month for PostgreSQL

## Troubleshooting

**Build fails?**
- Check build logs in Railway dashboard
- Ensure `./gradlew build` works locally
- Verify JDK 17+ in `build.gradle.kts`

**App won't start?**
- Check your main class in JAR manifest
- Verify PORT environment variable usage
- Check application logs for errors

**Database connection issues?**
- Verify DATABASE_URL format
- Check if PostgreSQL service is running
- Test connection string locally

## One-Click Deploy Button

Add this to your README.md for instant deployments:

```markdown
[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/new/template/your-template-id)
```

## Next Steps

1. **Custom domain**: Point your domain to Railway
2. **CI/CD**: Your GitHub Actions already work with Railway
3. **Monitoring**: Add application monitoring (Sentry, etc.)
4. **Scaling**: Monitor usage and upgrade plan as needed

Total deployment time: **Under 5 minutes!** 🚀