# My Share: Android V1 🚀

**When money comes in, My Share tells you exactly what to do next.** 

My Share is a manual-first, salary and payday planning companion for Android. It is designed around the philosophy of providing immediate financial clarity without requiring invasive bank syncs or complex setup.

## 🎯 V1 Release Candidate Features

- **The Hub**: A focused, Jetpack Compose-driven interface to allocate and preview upcoming paydays.
- **Goals & Drift**: Keep track of financial goals and identify budget drift before it happens.
- **Paywall & Premium Options**: Google Play Billing (8.3.0) integration offering deeper review histories and multiple recurring rules.
- **Privacy-First Ads**: AdMob integration fortified by the Google User Messaging Platform (UMP) SDK to respect GDPR/CCPA. Free-user ads are limited to low-sensitivity surfaces such as More, with capped interstitials only when leaving More and app-open ads only for returning free users after Home has loaded. Ads are never placed inside onboarding, paywall, plan, goal, review, or other financial decision flows, and are disabled entirely for Premium users.
- **Background Reminders**: WorkManager-powered local notifications for payday and weekly check-ins.

## 🛠️ Architecture

* **UI/UX**: 100% Jetpack Compose using Material 3, optimized for compact devices and low-tier hardware.
* **Domain & Data**: Clean architecture utilizing Kotlin Coroutines, Flow, and Hilt for dependency injection. 
* **Billing**: `PlayBillingEntitlementRepository` acts as the single source of truth for free vs. premium states.
* **Security**: Keystore secrets and localized AdMob configurations are cleanly abstracted into Gradle properties outside the repository.

## 🚀 Getting Started

1. Clone the repository.
2. Add release-only secrets to `~/.gradle/gradle.properties` or your CI secret store:
   ```properties
   MYSHARE_RELEASE_STORE_FILE=/path/to/keystore
   MYSHARE_RELEASE_STORE_PASSWORD=***
   MYSHARE_RELEASE_KEY_ALIAS=***
   MYSHARE_RELEASE_KEY_PASSWORD=***
   MYSHARE_ADMOB_APP_OPEN_AD_UNIT_ID=***
   ```
3. Sync Gradle and run on an Android Emulator or Device (Min SDK optimized for modern Android target).

## 📄 Documentation

* For the extensive UI/UX component guidance, explore [DESIGN.md](./DESIGN.md).
* For Play Console Data Safety constraints, refer to our internal Data Safety Artifacts.
