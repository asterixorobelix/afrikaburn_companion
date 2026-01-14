# Makefile for AfrikaBurn Companion
# Simplifies common development and deployment tasks
#
# Usage: make <target>
# Run `make help` to see all available targets

.PHONY: help setup test lint build-debug build-release \
        deploy-internal deploy-beta deploy-release \
        ios-certs ios-beta ios-release \
        clean check-env changelog

# Default target
help:
	@echo "AfrikaBurn Companion - Development Commands"
	@echo ""
	@echo "Setup:"
	@echo "  make setup          Install dependencies (Ruby gems, etc.)"
	@echo "  make check-env      Verify environment variables are configured"
	@echo ""
	@echo "Development:"
	@echo "  make test           Run unit tests"
	@echo "  make lint           Run detekt code analysis"
	@echo "  make build-debug    Build debug APK"
	@echo "  make clean          Clean build artifacts"
	@echo ""
	@echo "Android Deployment:"
	@echo "  make deploy-internal Deploy to Play Store Internal Testing"
	@echo "  make deploy-beta     Deploy to Play Store Beta"
	@echo "  make deploy-release  Deploy to Play Store Production (10% rollout)"
	@echo ""
	@echo "iOS Deployment:"
	@echo "  make ios-certs      Sync iOS code signing certificates"
	@echo "  make ios-beta       Deploy to TestFlight"
	@echo "  make ios-release    Upload to App Store"
	@echo ""
	@echo "Utilities:"
	@echo "  make changelog      Generate changelog from git commits"
	@echo ""

# ============================================================================
# SETUP
# ============================================================================

setup:
	@echo "Installing Ruby dependencies..."
	bundle install
	@echo ""
	@echo "Setup complete! Run 'make check-env' to verify configuration."

check-env:
	bundle exec fastlane check_env

# ============================================================================
# DEVELOPMENT
# ============================================================================

test:
	bundle exec fastlane android test

lint:
	bundle exec fastlane android lint

build-debug:
	bundle exec fastlane android build_debug

build-release:
	bundle exec fastlane android build_release

clean:
	bundle exec fastlane clean

# ============================================================================
# ANDROID DEPLOYMENT
# ============================================================================

deploy-internal:
	bundle exec fastlane android internal

deploy-beta:
	bundle exec fastlane android beta

deploy-release:
	bundle exec fastlane android release

# Full rollout (use with caution)
deploy-release-full:
	bundle exec fastlane android release rollout:1.0

# Promote between tracks
promote-internal-beta:
	bundle exec fastlane android promote_to_beta

promote-beta-production:
	bundle exec fastlane android promote_to_production

# ============================================================================
# iOS DEPLOYMENT
# ============================================================================

ios-certs:
	bundle exec fastlane ios certificates

ios-build-framework:
	bundle exec fastlane ios build_framework

ios-build:
	bundle exec fastlane ios build

ios-beta:
	bundle exec fastlane ios beta

ios-release:
	bundle exec fastlane ios release

ios-release-submit:
	bundle exec fastlane ios release_submit

# ============================================================================
# UTILITIES
# ============================================================================

changelog:
	bundle exec fastlane changelog

# Quick quality check before commit
pre-commit: test lint
	@echo ""
	@echo "All checks passed! Ready to commit."

# Full release preparation
prepare-release: test lint build-release
	@echo ""
	@echo "Release build ready!"
