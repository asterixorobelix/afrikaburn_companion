#!/bin/bash

# Branch Protection Setup Script
# This script applies branch protection rules to the main branch

set -e

echo "Setting up branch protection for main branch..."

# Check if GitHub CLI is installed
if ! command -v gh &> /dev/null; then
    echo "Error: GitHub CLI (gh) is not installed."
    echo "Please install it from: https://cli.github.com/"
    exit 1
fi

# Check if user is authenticated
if ! gh auth status &> /dev/null; then
    echo "Error: Not authenticated with GitHub CLI."
    echo "Please run: gh auth login"
    exit 1
fi

# Apply branch protection rules
echo "Applying branch protection rules..."
gh api repos/:owner/:repo/branches/main/protection \
    --method PUT \
    --input branch-protection.json

echo "✅ Branch protection rules applied successfully!"
echo ""
echo "Main branch is now protected with:"
echo "  - Required PR reviews (1 approval required)"
echo "  - Required status checks (CodeQL, backend-ci, mobile-ci)"
echo "  - Dismiss stale reviews enabled"
echo "  - Code owner reviews required"
echo "  - Force pushes disabled"
echo "  - Branch deletion disabled"
echo "  - Admin enforcement enabled"