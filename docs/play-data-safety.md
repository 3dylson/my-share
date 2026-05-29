# Play Data Safety Reference

Last verified: 2026-05-29

This document is the source of truth for the Google Play Data Safety, Ads,
Advertising ID, and Privacy Policy declarations for `pt.ms.myshare`.

## Official Inputs

- Google Play requires the Data Safety form to include app data collection and
  sharing, including data handled by third-party SDKs.
- Google Mobile Ads SDK 25.3.0 automatically collects and shares IP address,
  user product interactions, diagnostic information, and device/account
  identifiers for advertising, analytics, and fraud prevention.
- Firebase SDKs encrypt listed end-user data in transit and require us to
  account for developer-defined data stored through Firestore, Functions, Auth,
  Analytics, Crashlytics, Performance, Remote Config, App Check, and FCM.
- Google Play privacy policy requirements require a public, non-PDF URL that
  names the app/developer, contact method, collected/shared data, security,
  retention, and deletion practices.

Sources:
- https://support.google.com/googleplay/android-developer/answer/10787469
- https://support.google.com/googleplay/android-developer/answer/10144311
- https://developers.google.com/admob/android/privacy/play-data-disclosure
- https://firebase.google.com/docs/android/play-data-disclosure

## Play Console Declarations

### Privacy Policy

- URL: `https://my-share-finance.web.app/`
- The hosted page must remain publicly accessible, non-geofenced, non-PDF, and
  consistent with this document.

### Ads

- App contains ads: `Yes`
- Rationale: eligible free users can see AdMob banner, native, app-open,
  interstitial, and rewarded ads after first plan value.

### Advertising ID

- App uses Android Advertising ID: `Yes`
- Purposes:
  - Advertising or marketing
  - Analytics
  - Fraud prevention, security, and compliance

### Data Collection And Security

- Does the app collect or share user data types covered by Data Safety? `Yes`
- Is all collected user data encrypted in transit? `Yes`
- Can users request data deletion? `Yes`
- Can users request deletion of some or all app data without deleting their
  account? `Yes`
- Data deletion URL: `https://my-share-finance.web.app/`

## Data Types To Select

| Category | Data type | Collect/share | Required? | Purposes |
| --- | --- | --- | --- | --- |
| Location | Approximate location | Collected and shared | Required | Advertising or marketing; Analytics; Fraud prevention, security, and compliance |
| Personal info | Name | Collected | Optional | App functionality; Account management |
| Personal info | Email address | Collected | Optional | App functionality; Account management |
| Personal info | User IDs | Collected | Optional | App functionality; Account management; Fraud prevention, security, and compliance |
| Financial info | Purchase history | Collected | Optional | App functionality; Account management; Fraud prevention, security, and compliance |
| Financial info | Other financial info | Collected | Optional | App functionality; Account management |
| App activity | App interactions | Collected and shared | Required | App functionality; Analytics; Advertising or marketing; Fraud prevention, security, and compliance; Personalization |
| App info and performance | Crash logs | Collected | Required | Analytics; App functionality |
| App info and performance | Diagnostics | Collected and shared | Required | Analytics; Advertising or marketing; Fraud prevention, security, and compliance |
| Device or other IDs | Device or other IDs | Collected and shared | Required | App functionality; Analytics; Advertising or marketing; Developer communications; Fraud prevention, security, and compliance; Account management |

## Data Type Notes

- Approximate location is disclosed because AdMob can use IP address to estimate
  general location and Firebase Performance collects IP address to map
  performance events to country.
- Name, email, and Firebase user ID are optional because users can use local-only
  mode without Google sign-in.
- Other financial info covers salary, fixed costs, debts, goals, allocations,
  goal/rule names, and review/check-in values that users voluntarily enter.
  Financial values must never be logged to Analytics or used for ad targeting.
- Purchase history covers subscription product IDs, purchase tokens, and
  entitlement status received from Google Play Billing. Payment card details are
  not collected by My Share.
- App interactions includes onboarding progress, feature use, ad eligibility,
  ad impressions/clicks, subscription funnel events, and session activity. Do
  not include raw financial values in analytics params.
- Diagnostics and device IDs are shared because the Google Mobile Ads SDK shares
  diagnostic information and identifiers for advertising, analytics, and fraud
  prevention.

## Production Checklist

- Update and deploy `web/public/index.html` before submitting Play changes.
- Confirm `https://my-share-finance.web.app/` serves the updated privacy policy.
- Confirm Play Console Privacy Policy URL is `https://my-share-finance.web.app/`.
- Confirm Play Console Ads declaration says the app contains ads.
- Confirm Play Console Advertising ID declaration uses the three purposes above.
- Confirm Data Safety selections match the table above.
- Confirm both account deletion and partial app data deletion URLs point to
  `https://my-share-finance.web.app/`.
- Re-check this document whenever Firebase, AdMob, Billing, analytics, account,
  notification, or ad targeting behavior changes.
