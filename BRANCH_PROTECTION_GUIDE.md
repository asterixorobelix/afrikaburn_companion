# Branch Protection Setup Guide


## Contents

- [Quick Setup](#quick-setup)
- [Configuration File: `branch-protection.json`](#configuration-file-branch-protectionjson)
- [Customization Options](#customization-options)
  - [Required Status Checks](#required-status-checks)
  - [Pull Request Reviews](#pull-request-reviews)
  - [Admin Enforcement](#admin-enforcement)
  - [Push Restrictions](#push-restrictions)
  - [User/Team Restrictions](#userteam-restrictions)
- [Example Configurations](#example-configurations)
  - [Strict Protection (Recommended for Production)](#strict-protection-recommended-for-production)
  - [Relaxed Protection (For Development)](#relaxed-protection-for-development)
- [Manual Setup (Alternative)](#manual-setup-alternative)
- [Troubleshooting](#troubleshooting)
  - [Status Check Names](#status-check-names)
  - [Permissions](#permissions)
  - [GitHub CLI Issues](#github-cli-issues)
- [Automatic Branch Cleanup](#automatic-branch-cleanup)
- [Verification](#verification)
- [Updating Protection Rules](#updating-protection-rules)

This guide explains how to set up and customize branch protection rules for your GitHub repository.

## Quick Setup

1. **Prerequisites:**
   - Install [GitHub CLI](https://cli.github.com/)
   - Authenticate: `gh auth login`
   - Have admin access to your repository

2. **Apply protection rules:**
   ```bash
   ./setup-branch-protection.sh
   ```

## Configuration File: `branch-protection.json`

The `branch-protection.json` file contains all branch protection settings:

```json
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["CodeQL", "backend-ci", "mobile-ci"]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": true
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
```

## Customization Options

### Required Status Checks
- **`strict`**: `true` requires branches to be up to date before merging
- **`contexts`**: Array of status check names that must pass
  - Modify to match your CI workflow names
  - Common examples: `"CI"`, `"build"`, `"test"`, `"lint"`

### Pull Request Reviews
- **`required_approving_review_count`**: Number of required approvals (1-6)
- **`dismiss_stale_reviews`**: `true` dismisses approvals when new commits are pushed
- **`require_code_owner_reviews`**: `true` requires approval from code owners (needs CODEOWNERS file)

### Admin Enforcement
- **`enforce_admins`**: `true` applies rules to repository admins too
- Set to `false` if admins need bypass access

### Push Restrictions
- **`allow_force_pushes`**: `false` prevents force pushes to protected branch
- **`allow_deletions`**: `false` prevents branch deletion

### User/Team Restrictions
- **`restrictions`**: Limit who can push to branch
  ```json
  "restrictions": {
    "users": ["username1", "username2"],
    "teams": ["team1", "team2"],
    "apps": ["app-name"]
  }
  ```
- Set to `null` for no restrictions

## Example Configurations

### Strict Protection (Recommended for Production)
```json
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["CodeQL", "backend-ci", "mobile-ci", "security-scan"]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 2,
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": true
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
```

### Relaxed Protection (For Development)
```json
{
  "required_status_checks": {
    "strict": false,
    "contexts": ["CI"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": false,
    "require_code_owner_reviews": false
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
```

## Manual Setup (Alternative)

If you prefer using the GitHub web interface:

1. Go to your repository on GitHub
2. Click **Settings** → **Branches**
3. Click **Add rule** next to "Branch protection rules"
4. Configure the same settings as in the JSON file

## Troubleshooting

### Status Check Names
- Check your workflow files in `.github/workflows/` for job names
- Use exact job names from your CI workflows
- Remove non-existent status checks from the `contexts` array

### Permissions
- Ensure you have admin access to the repository
- For organization repos, check if branch protection is restricted by organization settings

### GitHub CLI Issues
- Update GitHub CLI: `gh extension upgrade cli`
- Re-authenticate: `gh auth logout && gh auth login`

## Automatic Branch Cleanup

The repository includes an auto-delete workflow (`.github/workflows/auto-delete-merged-branches.yml`) that automatically deletes feature branches after PRs are merged. This helps keep the repository clean.

**How it works:**
- Triggers when a PR is closed
- Only deletes if the PR was merged (not just closed)
- Protects main/master branches from deletion
- Logs success/failure for transparency

## Verification

After applying protection rules, verify they're working:

1. Try to push directly to main branch (should be blocked)
2. Create a PR and verify required checks are enforced
3. Check the repository settings page to confirm rules are active
4. Merge a test PR to verify automatic branch deletion

## Updating Protection Rules

To modify protection rules:

1. Edit `branch-protection.json`
2. Run `./setup-branch-protection.sh` again
3. The script will update the existing rules