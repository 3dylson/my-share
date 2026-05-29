# 100% Revenue-First Ads Rollout

My Share ships ads to 100% of eligible free users only after the first salary plan is created and shown. Premium and trial users remain fully ad-free.

## Product Rules

- Never show ads before first salary plan value, during onboarding, on the first result reveal, in paywall/purchase/restore/subscription management, salary input, allocation editing, goal/rule edit forms, reminder permission flow, active keyboard/input states, or notification reminder deep links.
- Show ads on free read-only surfaces after first value: anchored banner above bottom navigation on Home/More, native sponsored card in More, app-open from session 3, interstitial only after completed actions, and rewarded ads only after explicit opt-in.
- Do not use financial user input for ad targeting. Ad requests default to non-personalized mode.
- Blank Gradle ad unit IDs disable only that format. Use release Gradle properties for production IDs.

## Architecture

- `EvaluateAdEligibilityUseCase` owns business eligibility, caps, cooldowns, and safe Remote Config fallback behavior.
- `AdsOrchestrator` is the single presentation entry point for SDK init, ad requests, fullscreen formats, reward grants, analytics, and exposure recording.
- `AdExposureStore` records session count, rollout bucket, daily caps, cooldown timestamps, and temporary rewarded goal grants.
- Compose ad containers are format-specific: `AnchoredAdBanner` and `NativeSponsoredAdCard`.
- Legacy direct `AppOpenAdManager`, `InterstitialAdManager`, and generic premium banner paths must not be reintroduced.

## Remote Config Defaults

These defaults intentionally launch ads on for eligible free users:

- `ads_experiment_variant = revenue_standard`
- `ads_rollout_percent = 100`
- `ads_anchored_banner_enabled = true`
- `ads_native_more_enabled = true`
- `ads_app_open_enabled = true`
- `ads_interstitial_enabled = true`
- `ads_rewarded_enabled = true`
- `ads_min_sessions_app_open = 3`
- `ads_app_open_cooldown_hours = 12`
- `ads_interstitial_daily_cap = 1`
- `ads_rewarded_daily_cap = 3`
- `ads_native_daily_cap = 3`
- `ads_non_banner_daily_cap = 3`

Unknown ad experiment variants fall back to `control` so a bad backend value disables ads safely.

## Analytics

Track every ad decision and lifecycle event with `placement`, `format`, `variant`, `session_count`, `reason`, and `is_premium`:

- `ad_eligible`
- `ad_ineligible`
- `ad_request_started`
- `ad_loaded`
- `ad_load_failed`
- `ad_impression`
- `ad_clicked`
- `ad_hidden`
- `rewarded_ad_started`
- `rewarded_ad_completed`
- `reward_granted`
- `ad_free_premium_upsell_viewed`

## Kill Rules

- D1 retention drops: disable `ads_app_open_enabled`.
- D7 retention drops: reduce `ads_interstitial_daily_cap` or disable `ads_interstitial_enabled`.
- Paywall conversion drops: reduce banner pressure via `ads_anchored_banner_enabled` or rollout percent in higher-value countries.
- Reviews/support mention ads, scam, or privacy: disable interstitial first.
- Suspicious CTR: increase spacing or disable the risky placement.
- AdMob/Play policy warning: immediately disable the affected format by Remote Config or blanking the release ad unit ID before rollout.

## Release Checklist

- Play Data Safety and privacy policy source of truth: `docs/play-data-safety.md`.
- Privacy policy updated for the full AdMob rollout on 2026-05-29 and hosted at `https://my-share-finance.web.app/`.
- Production AdMob IDs are configured through `MYSHARE_ADMOB_*` Gradle properties. Current Android app ID: `ca-app-pub-9538602323287593~1471105513`; banner: `ca-app-pub-9538602323287593/2154982867`; native: `ca-app-pub-9538602323287593/9109229095`; interstitial: `ca-app-pub-9538602323287593/3284232946`; app-open: `ca-app-pub-9538602323287593/1001683294`; rewarded: `ca-app-pub-9538602323287593/2538127488`.
- `web/public/app-ads.txt` must be deployed to Firebase Hosting before release so `https://my-share-finance.web.app/app-ads.txt` serves the AdMob publisher record.
- AdMob Privacy & Messaging has a published EU/UK/Switzerland UMP message for `pt.ms.myshare` as of 2026-05-29. It targets My Share, includes English plus German, Spanish, French, and Portuguese, and exposes an explicit "Do not consent" choice.
- Keep UMP privacy options visible in More.
- Validate on Pixel 7: no ads before first plan result, ads activate after Premium dismissal/home entry, banner does not overlap nav/CTAs, keyboard hides banner, native card is clearly labeled/separated, Premium sees no ad containers, and notification opens do not trigger app-open.
- Run `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug --console=plain`.
