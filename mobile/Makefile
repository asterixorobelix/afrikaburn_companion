# Makefile for AfrikaBurn Companion
# Simplifies common development and deployment tasks
#
# Usage: make <target>
# Run `make help` to see all available targets

.PHONY: help setup test test-coverage lint build-debug build-release \
        install-android run-ios clean watch \
        deploy-internal deploy-beta deploy-release \
        ios-certs ios-beta ios-release \
        check-env changelog pre-commit prepare-release

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

# Default target
help:
	@echo "$(BLUE)╔══════════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║     AfrikaBurn Companion - Development Commands              ║$(NC)"
	@echo "$(BLUE)╚══════════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(GREEN)Quick Start:$(NC)"
	@echo "  make setup              Install all dependencies"
	@echo "  make test               Run unit tests"
	@echo "  make build              Build debug APK"
	@echo "  make install            Build and install on connected device"
	@echo ""
	@echo "$(GREEN)Development (Direct Gradle - no Fastlane required):$(NC)"
	@echo "  make test               Run unit tests"
	@echo "  make test-coverage      Run tests with Jacoco coverage report"
	@echo "  make lint               Run detekt code analysis"
	@echo "  make build              Build debug APK"
	@echo "  make build-release      Build release AAB"
	@echo "  make install            Install debug APK on device"
	@echo "  make clean              Clean build artifacts"
	@echo "  make pre-commit         Run all checks before committing"
	@echo ""
	@echo "$(GREEN)iOS Development:$(NC)"
	@echo "  make ios-framework      Build iOS XCFramework"
	@echo "  make ios-open           Open Xcode project"
	@echo ""
	@echo "$(GREEN)Fastlane Deployment (requires 'make setup' first):$(NC)"
	@echo "  make deploy-internal    Deploy to Play Store Internal Testing"
	@echo "  make deploy-beta        Deploy to Play Store Beta"
	@echo "  make deploy-release     Deploy to Play Store Production"
	@echo "  make ios-beta           Deploy to TestFlight"
	@echo "  make ios-release        Upload to App Store"
	@echo ""
	@echo "$(GREEN)Utilities:$(NC)"
	@echo "  make changelog          Generate changelog from git commits"
	@echo "  make check-env          Verify Fastlane environment"
	@echo "  make deps               Check for dependency updates"
	@echo ""
	@echo "$(YELLOW)Tip: Run 'make pre-commit' before every commit!$(NC)"
	@echo ""

# ============================================================================
# SETUP
# ============================================================================

setup: setup-gradle setup-fastlane
	@echo ""
	@echo "$(GREEN)✓ Setup complete!$(NC)"
	@echo "  Run 'make test' to verify everything works."
	@echo "  Run 'make check-env' to verify Fastlane configuration."

setup-gradle:
	@echo "$(BLUE)Checking Gradle wrapper...$(NC)"
	@if [ ! -f "./gradlew" ]; then \
		echo "$(YELLOW)Gradle wrapper not found. Please run 'gradle wrapper' first.$(NC)"; \
		exit 1; \
	fi
	@chmod +x ./gradlew
	@echo "$(GREEN)✓ Gradle wrapper ready$(NC)"

setup-fastlane:
	@echo "$(BLUE)Installing Ruby dependencies (Fastlane)...$(NC)"
	@if command -v bundle >/dev/null 2>&1; then \
		bundle install; \
		echo "$(GREEN)✓ Fastlane installed$(NC)"; \
	else \
		echo "$(YELLOW)⚠ Bundler not found. Fastlane features unavailable.$(NC)"; \
		echo "  Install with: gem install bundler"; \
	fi

# ============================================================================
# DEVELOPMENT (Direct Gradle - No Fastlane Required)
# ============================================================================

test:
	@echo "$(BLUE)Running unit tests...$(NC)"
	./gradlew :composeApp:testDebugUnitTest
	@echo "$(GREEN)✓ Tests passed$(NC)"

test-coverage:
	@echo "$(BLUE)Running tests with coverage...$(NC)"
	./gradlew :composeApp:testDebugUnitTest jacocoTestReport
	@echo "$(GREEN)✓ Coverage report generated$(NC)"
	@echo "  Open: composeApp/build/reports/jacoco/test/html/index.html"
	@if command -v open >/dev/null 2>&1; then \
		open composeApp/build/reports/jacoco/test/html/index.html 2>/dev/null || true; \
	fi

test-coverage-verify:
	@echo "$(BLUE)Verifying test coverage meets 80% threshold...$(NC)"
	./gradlew :composeApp:jacocoTestCoverageVerification
	@echo "$(GREEN)✓ Coverage verification passed$(NC)"

lint:
	@echo "$(BLUE)Running detekt code analysis...$(NC)"
	./gradlew detekt
	@echo "$(GREEN)✓ Code analysis passed$(NC)"

# Aliases for common commands
build: build-debug

build-debug:
	@echo "$(BLUE)Building debug APK...$(NC)"
	./gradlew :composeApp:assembleDebug
	@echo "$(GREEN)✓ Debug APK built$(NC)"
	@echo "  Location: composeApp/build/outputs/apk/debug/"

build-release:
	@echo "$(BLUE)Building release AAB...$(NC)"
	./gradlew :composeApp:bundleRelease
	@echo "$(GREEN)✓ Release AAB built$(NC)"
	@echo "  Location: composeApp/build/outputs/bundle/release/"

install: install-android

install-android:
	@echo "$(BLUE)Installing debug APK on connected device...$(NC)"
	./gradlew :composeApp:installDebug
	@echo "$(GREEN)✓ App installed$(NC)"

clean:
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	./gradlew clean
	@rm -rf fastlane/build 2>/dev/null || true
	@echo "$(GREEN)✓ Clean complete$(NC)"

# ============================================================================
# iOS DEVELOPMENT
# ============================================================================

ios-framework:
	@echo "$(BLUE)Building iOS XCFramework...$(NC)"
	./gradlew :composeApp:assembleReleaseXCFramework
	@echo "$(GREEN)✓ XCFramework built$(NC)"

ios-open:
	@echo "$(BLUE)Opening Xcode project...$(NC)"
	@open iosApp/iosApp.xcodeproj

# ============================================================================
# PRE-COMMIT & QUALITY CHECKS
# ============================================================================

pre-commit: lint test
	@echo ""
	@echo "$(GREEN)╔══════════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)║  ✓ All checks passed! Ready to commit.                       ║$(NC)"
	@echo "$(GREEN)╚══════════════════════════════════════════════════════════════╝$(NC)"

# Full quality check including coverage
quality: lint test-coverage-verify
	@echo ""
	@echo "$(GREEN)✓ All quality checks passed$(NC)"

# ============================================================================
# FASTLANE DEPLOYMENT (Requires Ruby/Bundler)
# ============================================================================

check-env:
	@bundle exec fastlane check_env

deploy-internal:
	@echo "$(BLUE)Deploying to Play Store Internal Testing...$(NC)"
	bundle exec fastlane android internal

deploy-beta:
	@echo "$(BLUE)Deploying to Play Store Beta...$(NC)"
	bundle exec fastlane android beta

deploy-release:
	@echo "$(BLUE)Deploying to Play Store Production (10% rollout)...$(NC)"
	bundle exec fastlane android release

deploy-release-full:
	@echo "$(YELLOW)⚠ Deploying to Play Store Production (100% rollout)...$(NC)"
	bundle exec fastlane android release rollout:1.0

promote-internal-beta:
	bundle exec fastlane android promote_to_beta

promote-beta-production:
	bundle exec fastlane android promote_to_production

# iOS Fastlane
ios-certs:
	bundle exec fastlane ios certificates

ios-build:
	bundle exec fastlane ios build

ios-beta:
	@echo "$(BLUE)Deploying to TestFlight...$(NC)"
	bundle exec fastlane ios beta

ios-release:
	@echo "$(BLUE)Uploading to App Store...$(NC)"
	bundle exec fastlane ios release

ios-release-submit:
	bundle exec fastlane ios release_submit

# ============================================================================
# UTILITIES
# ============================================================================

changelog:
	@bundle exec fastlane changelog

deps:
	@echo "$(BLUE)Checking for dependency updates...$(NC)"
	./gradlew dependencyUpdates 2>/dev/null || echo "$(YELLOW)Add gradle-versions-plugin for dependency updates$(NC)"

# Prepare a release (run all checks + build)
prepare-release: pre-commit build-release
	@echo ""
	@echo "$(GREEN)✓ Release build ready!$(NC)"
	@echo "  Run 'make deploy-internal' to deploy to internal testing."

# Watch mode - re-run tests on file changes (requires entr)
watch:
	@if command -v entr >/dev/null 2>&1; then \
		find composeApp/src -name "*.kt" | entr -c make test; \
	else \
		echo "$(YELLOW)Install 'entr' for watch mode: brew install entr$(NC)"; \
	fi
