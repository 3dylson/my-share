# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-rc.1] - 2026-03-22

The official **Release Candidate 1** for the V1 launch of My Share.

### Added
- **Google Play Subscriptions**: Implemented live Android billing hooks (`BillingClient 8.3.0`) for active entitlement mapping, plan management, and error recovery.
- **Paywall Experience**: A full-screen Compose Paywall detailing limits on the free tier (1 plan, 1 goal) vs. the premium suite structure.
- **Privacy-Compliant Free Tier**: Added `SafeAdBanner` injected strictly on the `MoreTab`. Enforced UMP (User Messaging Platform) consent verification before triggering ad loads.
- **Security Check**: Patched plain-text version control credentials out of `build.gradle`, delegating signing tasks to encrypted environment variables.
- **Background Integrity**: Integrated `ReminderWorker` for scheduled local notifications.
- **V1 Documentation**: Initialized `README.md` and finalized V1 UI contracts.

### Removed
- Removed mocked SharedPreferences entitlement gates in favor of the production Google Play Billing module.
- Cleaned up unsafe/raw Ad integration code paths.
