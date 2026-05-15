# UI/UX QA Notes - 2026-05-15

Scope: emulator-driven end-to-end audit of onboarding, auth, paywall, home tabs, and premium/free feature edges.

Device under test: `emulator-5554` (`Pixel_7(AVD) - 16`).

Evidence folder for this run: `/tmp/myshare-qa-2026-05-15`.

Screen recording from the deeper crawl: `/tmp/myshare-qa-2026-05-15/myshare-deep-crawl.mp4`.

## High Priority Findings

### 1. Ad consent can appear after the first onboarding tap
- Screen: Welcome / AdMob UMP consent.
- Repro: fresh app start, tap `Get Started`; the consent WebView can appear after the tap instead of before the user begins onboarding.
- Impact: feels like the app ignored the first interaction and then interrupted the flow.
- Suggested fix: block onboarding interaction until consent status is resolved, or show a lightweight loading/consent preparation state before the welcome CTA becomes active.

### 2. Invalid fixed costs can lead to a dead-end allocation screen
- Screen: Fixed costs -> Allocation.
- Repro: enter fixed costs greater than income, continue to allocation.
- Result: allocation opens with `Over budget`, all allocation fields at `$0.00`, and the CTA disabled.
- Impact: user is stuck unless they infer they must go back and reduce fixed costs.
- Suggested fix: validate fixed costs before leaving the fixed-costs screen and show a clear inline error like `Fixed costs exceed your payday income. Lower fixed costs or increase income.`

### 3. Onboarding screens use disabled CTAs without explaining what is missing
- Screens: Goal Picker, Salary, Fixed Costs.
- Repro: choose Custom Goal with blank goal name; leave income/fixed-cost field empty.
- Result: CTA is disabled with no inline explanation.
- Impact: low confidence, especially on financial inputs where users need exact guidance.
- Suggested fix: add field-level helper/error text and keep the CTA enabled enough to reveal validation on tap, or add a compact checklist of missing inputs.

### 4. Scrolled onboarding headers can clip under the status bar
- Screen: Goal Picker after scrolling.
- Repro: scroll down from the top of Goal Picker.
- Result: the large H1 is partially clipped behind the top/status area.
- Impact: visually rough and not premium.
- Suggested fix: add top content padding/insets to the scroll container or make the header collapse/stick cleanly.

### 5. Google sign-in failure is too transient
- Screen: Signup.
- Repro: tap `Continue with Google` on an emulator without a Google account.
- Result: Credential Manager reaches Play Services, fails with `NoCredentialException: No credentials available`; user gets only a short snackbar.
- Impact: users may miss the error and have no persistent next step.
- Suggested fix: show a persistent inline error with a recovery action, e.g. `No Google account is available on this device. Add an account or continue locally.`

### 6. Legal links point to unreachable public URLs
- Screen: More -> Legal & Support.
- Repro: tap `Terms of Service`.
- Result: Chrome opens `myshare.pt/terms` and shows `DNS_PROBE_FINISHED_NXDOMAIN`.
- Impact: this is a production readiness blocker for legal/compliance trust.
- Suggested fix: either host valid legal pages before release or show in-app bundled legal content with externally reachable canonical URLs.
- 2026-05-15 follow-up: the repo has a Firebase Hosting web module at `web/public/index.html`, and the live root `https://my-share-finance.web.app/` serves the Privacy Policy. A local fix was added to point Terms to `https://my-share-finance.web.app/terms` and add `web/public/terms/index.html`; deploy the web module before treating this as resolved in production. The live `/terms` path currently returns HTTP 200 through the Firebase rewrite but still renders Privacy Policy content.

### 7. Bottom navigation overlaps scroll content and primary actions
- Screens: Home Plan, Review.
- Repro: view Plan allocation preview or submit a review.
- Result: bottom navigation fades over lower content; on Review, the `Submit Review` button is partially under the navigation area after submission/scroll positioning.
- Impact: key financial numbers and actions feel clipped and less trustworthy.
- Suggested fix: add bottom content padding equal to navigation height plus system gesture inset, and avoid placing primary CTAs under the persistent nav.

### 8. Home shell header consumes too much space on every tab
- Screens: Plan, Strategy, Review, More.
- Repro: switch across home tabs.
- Result: each tab repeats a large `My Share` header and tagline, pushing actual task content far down the screen.
- Impact: premium visual direction is present, but task efficiency suffers and the app feels oversized.
- Suggested fix: use a compact app bar after onboarding, or collapse the brand header as the user scrolls.

## Screen-by-Screen Notes

### Welcome
- Clean but sparse; large blank vertical space makes it feel more like setup utility than premium finance app.
- No first-viewport brand signal beyond icon/copy.
- Debug `Skip to Home (Dev)` is acceptable in debug builds, but visually competes with onboarding CTA during internal QA.

### Goal Picker
- H1 is oversized and wraps awkwardly on mobile.
- Back control is text-only; should be a standard arrow/icon button with accessible label.
- Primary Continue action is below the fold on the initial view.
- Custom Goal clears the name correctly, but no validation message is shown when the blank custom name disables Continue.
- Floating field labels visually collide with the outlined field border in a few states.

### Salary And Schedule
- `Net income per payday` field has no visible currency prefix until a value is entered.
- `Payday` field should read more explicitly as `Payday day of month` and show valid range.
- Bi-weekly date uses raw `YYYY-MM-DD`; this should be a date picker or masked date input.
- Disabled Continue lacks explanation.

### Fixed Costs
- Preset copy is clearer after renaming to `Allocation style`, but Lean/Balanced/Growth still do not show financial impact.
- Fixed costs greater than income are accepted, which creates the allocation dead-end.
- Disabled Continue lacks explanation when fixed costs are empty.

### Allocation Priorities
- Valid state is readable, but `Remaining` is only a label; it should show the remaining amount.
- Over-budget state gives red labels but no recovery instruction.
- Four raw amount fields provide precision but feel heavy; consider sliders/steppers plus exact edit affordance.

### Plan Preview
- Strongest onboarding screen so far: clear action-plan hierarchy and helpful payday framing.
- Top content starts close to the status bar.
- `Automate my mission` implies automation/premium but the next step is signup/paywall, so copy should set expectations.

### Signup
- Trust messaging is good, but layout has a very large empty gap before the Google button.
- Google button styling reads somewhat disabled due low-contrast gray text/border.
- Google failure is transient and non-actionable.
- `Skip Cloud Sync (Run Locally)` is clear but could be framed less like a second-class path.

### Trajectory
- Good concept and visual direction.
- Hero-sized text inside the first card wraps heavily and feels oversized for card content.
- Large blank area before CTA makes the page feel stretched.

### Paywall
- Close icon is oversized and visually dominates the header.
- CTA sits crowded against the bottom/navigation area.
- Plan cards are understandable, but subscription/trial details are not close enough to each plan.
- Restore appears visually inert in this emulator pass; no persistent success/failure feedback was visible.
- Upgrade tap did not show a clear loading/error state in this environment, so billing failures or unavailable products need user-visible feedback.

### Reminders
- Reminder setup is visually cleaner than the form-heavy onboarding steps.
- Time is adjusted only by plus/minus controls; a native time picker would be faster and more familiar.
- The notification permission request appears immediately after `Enable Reminders`; a compact pre-permission rationale would make the OS dialog feel less abrupt.
- Enabling reminders successfully advanced to Bank Sync after the Android notification permission was allowed.

### Bank Sync
- Clean but sparse.
- `Notify me when it's ready` is ambiguous for a local-only guest user; it does not explain where the notification will go or what state is being stored.
- Flow completed onboarding successfully after choosing the notify option.

### Home Plan
- Home can briefly appear almost blank/faded right after onboarding completion, then resolves after waiting.
- The first screen is visually polished, but the mission and metric cards are oversized for repeated use.
- `Your Next Payday Mission: 1 Jun` wraps awkwardly; the date should be kept together or moved to a subtitle line.
- Allocation preview starts under the bottom navigation, hiding lower values.
- App foreground relaunch returned to the Plan tab without showing an app-open ad in this pass, which is appropriate for avoiding interruption around financial review.

### Strategy
- Goal card is readable and local onboarding goal persisted correctly.
- `Add another goal` and `Add another rule` correctly open a premium gate bottom sheet.
- Premium bottom sheet copy is clear, but it uses the generic title `Unlock Premium Strategy` for both goals and rules; the message should match the specific tapped feature.
- Tapping `Upgrade Now` from the premium gate dismissed the sheet without visible billing handoff, loading, or error feedback in the debug emulator build.
- Scrolling Strategy leaves the large app header pinned/highly visible and clips the top of the underlying content.
- Rule cards repeat the rule type as both title and subtitle (`Savings` / `Savings`, `Investing` / `Investing`), which feels unfinished.
- Existing goal edit mode is reachable by tapping the goal card. Delete confirmation works and copy is clear.
- Goal edit screen shows a `Multiple Goals` info card while editing a single free/basic goal, which reads like the premium feature is already available or incorrectly scoped.
- Rule add/edit mode is reachable. Blank save shows validation, creating a valid rule returns to Strategy, tapping the created rule opens edit mode, and delete confirmation/removal works.
- Rule add previously kept the validation error visible after fields were corrected until save succeeded, creating a false-error state. Fixed locally by clearing add/edit validation errors on input changes.
- Rule add/edit primary buttons sat too close to the gesture/navigation area; on the emulator the `SAVE RULE` label was clipped to the bottom edge and required a precise low tap. Fixed locally by moving add/edit primary actions into sticky bottom bars above system navigation; emulator evidence saved at `/tmp/myshare-qa-2026-05-15/68-rule-add-sticky-action.png`.
- Stale goal/rule edit routes now have explicit missing states instead of falling back into a blank "new" form.

### Review
- Manual review submission worked and updated `Performance insights` to `Trust score 100%`, `1 reviews`, and `1 day streak`.
- The sliders are visually clear but imprecise for money entry; users need exact numeric input or tap-to-edit amounts.
- There was no obvious success toast/banner after submit; the screen updates, but a lightweight confirmation would help.
- Submit button can be partially obscured by the bottom nav after the screen repositions.
- `The habit loop` is personable but vague as an information architecture label for a finance workflow.

### More
- Account, subscription, preferences, and legal/support are all discoverable.
- Subscription cards are repeated inline in More, making the tab feel like a paywall before settings.
- `Smart notifications` toggle updates state from `PAYDAY at 09:00` to `Reminders are off`.
- `Smart automation` is labeled Premium, but tapping the row itself does not explain the premium boundary.
- `Ad Preferences` is discoverable and opens the UMP consent form. The system form is functional but visually generic and interrupts the app context.
- Account actions and the bottom ad area exist below Legal & Support, but the bottom navigation partially covers the advertisement section.
- `Sign out` immediately returns the user to onboarding with no confirmation or explanation that local session state will reset.
- Legal rows are easy to find; Terms was locally retargeted to the Firebase Hosting domain and needs deployment.

## Runtime / Logs

- Google sign-in log confirms Credential Manager flow reaches Play Services and returns `NoCredentialException` on accountless emulator.
- 2026-05-15 final environment pass: Firebase MCP confirmed Android app `pt.ms.myshare` is registered in project `my-share-finance`, and both debug/release SHA-256 hashes match the local signing report. The repo/local Google client id was stale and was updated to the current Firebase web OAuth client id. After rebuild/reinstall, Credential Manager still returned `NoCredentialException` on `emulator-5554`; logs show GoogleIdService accepted the option but returned no credential. This now points to device/account/provider eligibility rather than missing Firebase SHA registration.
- Paywall/billing attempts did not produce a clear visible app error state in the emulator pass.
- Deeper screen-recorded crawl saved at `/tmp/myshare-qa-2026-05-15/myshare-deep-crawl.mp4`; thumbnails were generated as `deep-crawl-thumb-*.png` plus `deep-crawl-contact-sheet.png`.
- During the crawl, an external Gmail onboarding screen appeared after a broad lower-screen tap sequence. Manual revisit suggests this was likely a test tap hitting an external intent area or app-switch state rather than a deterministic My Share route, but external transitions should be retested after legal links are fixed and deployed.
- Firestore logged a transient `UNAVAILABLE` write stream warning during later navigation, but no user-visible failure was observed.
- No app crash observed during the audited onboarding, home, review, premium gate, preferences, or legal-link path.
- Validation screenshots for goal/rule edit and premium-gate paths were saved as `/tmp/myshare-qa-2026-05-15/52-goal-edit-open.png` through `/tmp/myshare-qa-2026-05-15/63-premium-upgrade-tapped.png`.
- Final local environment pass artifacts were saved under `/tmp/myshare-qa-2026-05-15/final-env`. This pass covered fresh launch, consent denial, onboarding through Signup/Paywall/Reminder/Bank Sync/Home, notification permission denial, More/legal/ad preferences, Firebase config comparison, and logcat capture.
- Gradle verification passed with `:app:testDebugUnitTest`, `:app:assembleDebug`, and `:app:assembleRelease`. Release still emits deprecation/unchecked-cast warnings but builds successfully.
- Notification permission denied path is functional: Android permission dialog appears, denial returns to Reminder setup, and an inline message says notification permission is required for reminders. Evidence: `/tmp/myshare-qa-2026-05-15/final-env/24-notification-denied-result.png`.
- Ad preferences remain reachable after denying consent and open the UMP privacy form. Evidence: `/tmp/myshare-qa-2026-05-15/final-env/31-ad-preferences.png`.
- Firebase Hosting was deployed on 2026-05-15. Live `https://my-share-finance.web.app/terms` now redirects to `/terms/` and serves `My Share - Terms of Service`; root still serves `My Share - Privacy Policy`.
- Smart automation row is still inert for free users: tapping the premium-labeled row did not show a paywall or explanation. Evidence: `/tmp/myshare-qa-2026-05-15/final-env/30-smart-automation-tap.png`.
- 2026-05-15 fix pass after parallel implementation:
  - `:app:testDebugUnitTest` and `:app:assembleRelease` passed. Release still has Compose deprecation warnings for icon/progress overloads and Gradle 9 compatibility warnings.
  - Fresh-launch consent still appears immediately as a Google UMP WebView before onboarding context. This remains open as a premium UX concern. Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/02-consent-webview.png`.
  - Onboarding headers now sit below system status bars in the tested screens. Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/03-salary-blank-validation.png`.
  - Disabled/unclear onboarding CTAs were changed to clickable CTAs with inline validation. Salary blank state now shows `Enter your net income per payday.` Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/03-salary-blank-validation.png`.
  - Fixed-costs greater than payday income now block progression and show `Fixed costs exceed your payday income. Lower fixed costs or increase income.` Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/04-fixed-cost-over-income.png`.
  - Valid fixed-cost values advance to allocation and action-plan generation without the prior dead-end. Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/05-allocation-valid.png` and `/tmp/myshare-qa-2026-05-15/fix-pass/06-after-allocation.png`.
  - Google sign-in still returns `No Google account is available on this device...` through Credential Manager on `emulator-5554`, but the error is now persistent and actionable with `Continue locally`. Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/07-google-signin-result.png`.
  - Home header is now compact (`My Share` + current tab label), and bottom navigation no longer hides Plan content after scrolling. Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/12-home-plan-after-bank-sync.png`.
  - More > Smart automation now opens a premium explanation dialog with `Not now` and `View Premium`. Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/16-more-smart-automation-dialog.png`.
  - Review submission now shows a snackbar (`Review saved. Performance insights updated.`) and updates trust score/history. Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/18-review-submitted.png`.
- Strategy rule cards no longer repeat `Savings` / `Savings`; the tested rule shows `Savings` with `Recurring payday rule`. Evidence: `/tmp/myshare-qa-2026-05-15/fix-pass/19-strategy.png`.
- 2026-05-15 premium polish shipment:
  - Ad consent no longer runs from `MainComposeActivity.onCreate`; consent is gathered after free Home context is ready or when the user opens Ad Preferences. Fresh launch no longer showed the UMP WebView before onboarding. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/01-fresh-launch-no-consent.png`.
  - Review now offers exact money inputs for flexible spend and goal contribution while keeping sliders for quick adjustment. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/03-review-exact-fields.png`.
  - Bank Sync no longer feels like a blocking setup step: `Continue to Home` is the primary action, and `Notify Me When It's Ready` is secondary.
  - Onboarding and More paywalls now keep visible billing state for starting, Play purchase handoff, unavailable products, restore checking, restore success, and no-purchase restore outcomes.
  - More suppresses duplicate inline billing errors when the richer billing status card is available. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/06-more-billing-feedback.png`.
  - Final fresh launch after reinstall still opens directly to the welcome screen without an immediate UMP consent interruption. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/07-fresh-launch-final.png`.
- 2026-05-15 compact More polish:
  - More subscription rows were tightened from large paywall cards into compact selectable plan rows, leaving App Preferences visible in the first More viewport. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/13-compact-more-no-auto-consent.png`.
  - Automatic ads consent is now skipped on first free sessions and reserved for returning sessions or explicit Ad Preferences access. This prevented UMP from interrupting the first-session More upgrade path.
  - Billing unavailable feedback still appears after the compact layout and no duplicate inline error is shown. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/14-compact-more-billing-feedback.png`.
  - Sign-out confirmation was already present in the current code path and remains the expected behavior from More.
- 2026-05-15 Plan/account polish:
  - Plan was tightened into a dashboard-style first viewport: compact payday summary, two core metric cards, four allocation cells, and the Smart Adjustments prompt all fit without hiding the financial context behind the bottom nav. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/16-plan-account-build.png`.
  - More profile header now opens an account details dialog instead of acting like an inert row. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/18-account-details-dialog.png`.
  - Automatic UMP display from Home was removed after emulator testing showed it could interrupt account/upgrade interactions on returning sessions. Existing consent still initializes/preloads ads, and explicit Ad Preferences still opens UMP.
- 2026-05-15 Review polish:
  - Review entry is now a single precise payday check-in card with two exact money fields and one primary submit action; the previous duplicated slider stack was removed from the main path. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/22-compact-review-history.png`.
  - Review submission still updates performance insights and shows the snackbar confirmation. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/23-compact-review-submitted.png`.
  - Historical performance now uses one compact review card with flexible and goal actuals side by side instead of two large repeated metric cards. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/24-compact-review-history-after-snackbar.png`.
- 2026-05-15 Strategy polish:
  - Empty Strategy now uses one workspace card with goal and rule setup actions instead of two sparse sections. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/26-strategy-workspace.png`.
  - Active goal cards were tightened into compact cards with progress, saved amount, and target date in one scan. Long names now ellipsize instead of crowding the percentage. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/29-active-compact-strategy-created.png`.
- 2026-05-15 Strategy form and paywall polish:
  - Goal and rule add/edit screens now use a compact shared form header instead of oversized editorial headers, and Goal edit is scroll-safe like Rule edit. Goal creation and Rule creation were both validated on `emulator-5554`. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/40-goal-new-form.png` and `/tmp/myshare-qa-2026-05-15/ship-polish/41-goal-rule-created.png`.
  - Rule form copy no longer truncates on phone width, and creating a valid savings rule returned to Strategy with the new rule visible. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/38-rule-form-copy-fixed.png` and `/tmp/myshare-qa-2026-05-15/ship-polish/39-rule-created.png`.
  - Onboarding paywall now has smaller top controls, compact benefit cards, and a sticky purchase footer with trial context above system navigation. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/60-paywall-grid.png`.
  - Restore feedback on the paywall is now a compact persistent billing notice instead of a tall info card that pushes plan selection out of view. Evidence: `/tmp/myshare-qa-2026-05-15/ship-polish/58-paywall-compact-restore.png`.
- 2026-05-15 continued onboarding polish:
  - Goal Picker, Salary, Fixed Costs, and Allocation now share an icon-back/sticky-CTA scaffold so primary actions stay above system navigation instead of living inside long scroll content. Evidence: `/tmp/myshare-qa-2026-05-15/continued-polish/02-goal-picker.png`, `/tmp/myshare-qa-2026-05-15/continued-polish/03-salary.png`, and `/tmp/myshare-qa-2026-05-15/continued-polish/11-allocation-updated.png`.
  - Salary blank-submit validation remains persistent with the sticky CTA visible. Evidence: `/tmp/myshare-qa-2026-05-15/continued-polish/04-salary-validation.png`.
  - Fixed-cost over-budget validation still blocks progression and keeps recovery guidance visible. Evidence: `/tmp/myshare-qa-2026-05-15/continued-polish/06-fixed-overbudget.png`.
  - Valid fixed costs still advance into Allocation, and Allocation still advances through plan generation to Home in the emulator replay. Evidence: `/tmp/myshare-qa-2026-05-15/continued-polish/07-allocation-after-valid-fixed.png` and `/tmp/myshare-qa-2026-05-15/continued-polish/08-allocation-updated.png`.
  - Plan Preview actions were moved into a sticky footer to match the rest of onboarding and were recaptured in a careful UI-tree driven pass. Evidence: `/tmp/myshare-qa-2026-05-15/continued-polish/29-plan-preview-careful.png`.
  - Signup was tightened into a compact trust card with a sticky Google/local choice footer, replacing the previous large empty gap before auth actions. Evidence: `/tmp/myshare-qa-2026-05-15/continued-polish/30-signup-polished.png`.
  - Google sign-in failure still surfaces persistently in the sticky auth area after the Signup layout change. Evidence: `/tmp/myshare-qa-2026-05-15/continued-polish/33-google-result-polished-signup.png`.
  - Trajectory was condensed into one scan-friendly summary card with payday allocation, goal path, and contribution intensity, plus a sticky `See Full Plan` action. Evidence: `/tmp/myshare-qa-2026-05-15/continued-polish/31-trajectory-polished.png`.
- 2026-05-15 contextual premium gates:
  - Strategy locked goal and rule actions now open feature-specific premium sheets instead of the previous generic premium copy. Evidence: `/tmp/myshare-qa-2026-05-15/premium-gates/04-goal-gate.png` and `/tmp/myshare-qa-2026-05-15/premium-gates/10-rule-gate.png`.
  - More > Smart automation now keeps the two-step premium explanation flow and opens an automation-specific premium sheet from `View Premium`. Evidence: `/tmp/myshare-qa-2026-05-15/premium-gates/07-automation-dialog.png` and `/tmp/myshare-qa-2026-05-15/premium-gates/08-automation-gate.png`.
- 2026-05-15 production trial logic:
  - Trial copy is now driven by Play Billing `ProductDetails` offer/pricing phases instead of the local hardcoded pricing strategy. The app only shows `Start Free Trial` and trial conversion terms when the selected Play offer contains a zero-priced finite trial phase.
  - Billing product mapping now prefers an eligible trial offer when Play returns one, uses the paid recurring phase as the displayed price, and passes the matching offer token into the purchase flow.
  - Local debug emulator without live Play products no longer shows a false 7-day trial claim in More. Evidence: `/tmp/myshare-qa-2026-05-15/trial-logic/01-more-no-false-trial.png`.

## Code Cleanup Decisions

- Removed unreachable edit-profile implementation: `presentation/ui/edit_profile`, `domain/use_case/edit_profile`, `UserDataRepository`, and its data implementation. No route was registered in `AppNavigation`, no tap path reached it, and the only references were its own DI/use-case stack.
- Removed unreachable legacy dashboard snapshot code: `GetDashboardDataUseCase` and `DashboardState`. It depended on the dead `UserDataRepository` and had no active callers.
- Removed unreachable standalone paywall package: `presentation/ui/paywall`. The active onboarding paywall is the `PaywallScreen` declared in `presentation/ui/onboarding/OnboardingScreens.kt`; the standalone route/view model had no navigation entry.
- Added add/edit route tests covering missing goal/rule IDs and validation error clearing.

## Still To Audit

- `PremiumPaywallBottomSheet` still needs a production billing/test-product pass. Debug emulator now shows persistent unavailable-product feedback, but real Play purchase/restore requires Play Console products and a licensed tester.
- `AdsConsentManager.showPrivacyOptionsForm` was reached through `Ad Preferences`; ad rendering itself still needs a consented session/test-ad pass.
- Real Google sign-in still needs a device/account that Credential Manager returns as an eligible Google ID credential. Firebase SHA registration is verified, and the repo Google client id was corrected.
- Real Play Billing purchase/restore with Play Console test products and a licensed tester.
- Privacy Policy row behavior after the hosted Terms fix is deployed.
- Physical-device pass for smaller/larger displays and release build performance.
