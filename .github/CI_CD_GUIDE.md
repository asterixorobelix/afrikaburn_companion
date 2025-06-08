# CI/CD Test and Code Quality Reporting Guide

This guide explains how test results and detekt (code quality) results are automatically displayed in pull requests.

## 🎯 Overview

Our CI/CD pipeline automatically:
- Runs all tests for backend and mobile modules
- Executes detekt code quality analysis
- Reports results directly in PR comments and job summaries
- Uploads detailed reports as artifacts

## 🔍 Workflows

### 1. Backend CI (`backend-ci.yml`)
**Triggers:** Push/PR to main with backend changes

**What it does:**
- Runs backend tests with `./gradlew test --continue`
- Executes detekt analysis with `./gradlew detekt --continue`
- Generates test summary in job summary
- Comments on PR with detekt results
- Uploads test and detekt reports as artifacts

### 2. Mobile CI (`mobile-ci.yml`)
**Triggers:** Push/PR to main with mobile changes

**What it does:**
- Runs mobile tests across all subprojects
- Executes detekt analysis with mobile-specific rules
- Generates test summary in job summary
- Comments on PR with mobile-specific detekt guidance
- Builds Android APK and iOS framework
- Uploads test and detekt reports as artifacts

### 3. PR Validation (`pr-validation.yaml`)
**Triggers:** All pull requests

**What it does:**
- Comprehensive PR validation including:
  - PR title validation (conventional commits)
  - Code size analysis
  - Lint and detekt analysis with detailed reporting
  - Build validation for Debug/Release
  - Test execution with summary generation
  - Performance checks (APK size)
  - Auto-assignment of reviewers

## 📊 Test Result Reporting

### In Job Summaries
Each workflow generates a markdown summary showing:
- Total test count
- Pass/fail/skip breakdown
- Visual status indicators (✅ ❌ ⏭️)

### Example Output:
```markdown
## 🧪 Backend Test Results

✅ **All 42 tests passed!** 🎉

| Status | Count |
|-----------|-------|
| ✅ Passed | 40 |
| ❌ Failed | 0 |
| ⏭️ Skipped | 2 |
| 📊 **Total** | **42** |
```

## 🔍 Detekt Result Reporting

### Automatic PR Comments
When detekt finds issues, the bot automatically comments with:
- Issue count per module (Backend/Mobile)
- Actionable next steps
- Links to detailed reports

### Example Comment:
```markdown
## 🔍 Detekt Code Quality Report

⚠️ **Found 3 Detekt issue(s)** across modules:

- **Backend**: 1 issue(s)
- **Mobile**: 2 issue(s)

### 🔧 Next Steps:
1. Check the [full Detekt report](link) for details
2. Run `./gradlew detekt` locally to see specific issues
3. Many issues can be auto-fixed with `./gradlew detektFormat`
4. Review the detekt.yml configuration for rule explanations
```

## 📎 Artifact Reports & Test Result Display

### 📱 Where Test Results Appear:

**1. Pull Request Comments:**
- Automated comprehensive summary comment
- Test pass/fail counts with percentages
- Code quality (detekt) issue counts
- Direct links to downloadable artifacts
- Clear status indicators (✅ ❌ ⚠️)

**2. Job Summaries:**
- Markdown tables showing detailed breakdowns
- Visual status indicators
- Quick overview in Actions tab

**3. Downloadable Artifacts (7-day retention):**
- **Test Reports** (`mobile-test-results`): HTML test reports with detailed failure info
- **Detekt Reports** (`mobile-detekt-results`): XML and HTML detekt reports
- **Backend Test Results** (`backend-test-results`): Backend-specific test reports
- **Backend Detekt Results** (`backend-detekt-results`): Backend code quality reports

### 🔗 Accessing Detailed Reports:

**Method 1: From PR Comment**
1. Look for the comprehensive summary comment in your PR
2. Click the "Download All Artifacts" link
3. Scroll to "Artifacts" section
4. Download the relevant ZIP files

**Method 2: Direct Navigation**
1. Go to the GitHub Actions run page
2. Scroll to bottom to find "Artifacts" section
3. Download and extract the ZIP files
4. Open `index.html` files in browser for detailed views

## ⚙️ Configuration

### Test Configuration
**Backend** (`backend/build.gradle.kts`):
- JUnit 5 platform
- JaCoCo coverage (80% minimum)
- XML and HTML report generation
- Detailed test logging

**Mobile** (`mobile/build.gradle.kts`):
- Subproject test configuration
- Consistent reporting across modules

### Detekt Configuration
**Both modules use**:
- Custom `detekt.yml` configuration
- XML and HTML report output
- Mobile-specific rules for Compose

## 🛠️ Troubleshooting

### Common Issues:

**"No test results found"**
- Check if tests are actually running
- Verify test task names match gradle configuration
- Ensure XML reports are being generated

**"Permission denied" errors**
- Workflows have proper permissions set:
  ```yaml
  permissions:
    contents: read
    issues: write
    pull-requests: write
    checks: write
  ```

**Detekt issues not showing**
- Verify detekt.xml is generated in `build/reports/detekt/`
- Check if detekt task completed successfully
- Review detekt configuration in `detekt.yml`

### Local Testing:
```bash
# Backend
cd backend
./gradlew test detekt

# Mobile  
cd mobile
./gradlew test detekt

# View reports
open backend/build/reports/tests/test/index.html
open backend/build/reports/detekt/detekt.html
```

## 🔄 Workflow Permissions

All workflows are configured with minimal required permissions:
- `contents: read` - Access repository content
- `issues: write` - Comment on issues
- `pull-requests: write` - Comment on PRs
- `checks: write` - Create check runs

## 🚀 Best Practices

1. **Write comprehensive tests** - Higher coverage = better confidence
2. **Fix detekt issues early** - Don't let technical debt accumulate
3. **Review reports regularly** - Use HTML reports for detailed analysis
4. **Keep detekt config updated** - Adjust rules as project evolves
5. **Monitor performance** - Watch APK size and build times

## 📚 Additional Resources

- [Detekt Documentation](https://detekt.dev/)
- [JUnit 5 Guide](https://junit.org/junit5/docs/current/user-guide/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

*This CI/CD setup ensures code quality and test coverage are maintained across all contributions to the project.*