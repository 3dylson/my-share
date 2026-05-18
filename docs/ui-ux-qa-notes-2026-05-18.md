# UI/UX QA Notes - 2026-05-18

## Scope

Final onboarding-to-paywall evidence pass for the premium conversion flow:

- Welcome
- Goal Picker
- Salary
- Fixed Costs
- Allocation
- Plan Preview aha reveal
- Signup
- Trajectory adaptive bridge
- Paywall

Addendum: Phase 5 Plan, Strategy, and Review tab premium value-surface validation.

## Environment

- App: debug build installed from the current workspace
- Test command: `./gradlew testDebugUnitTest :app:installDebug --console=plain`
- Main-tab addendum test/install commands: `./gradlew testDebugUnitTest --console=plain` and `ANDROID_SERIAL=emulator-5554 ./gradlew :app:installDebug --console=plain`
- Review addendum test/install commands: `./gradlew testDebugUnitTest --console=plain` and `ANDROID_SERIAL=emulator-5554 ./gradlew :app:installDebug --console=plain`
- Device: `emulator-5554`
- Normal profile: `1080x2400 / 420dpi`
- Compact profile: `720x1280 / 320dpi`
- Main-tab addendum compact profile: `720x1280 / 360dpi`
- Inputs:
  - Goal: Emergency fund
  - Net income per payday: `$1,500`
  - Monthly essentials: `$600`
  - Default allocation split

## Evidence

Artifacts were saved under:

`/tmp/myshare-onboarding-qa-2026-05-18`

Main-tab addendum artifacts were saved under:

`/tmp/myshare-main-tab-qa-2026-05-18`

Review addendum artifacts were saved under:

`/tmp/myshare-review-qa-2026-05-18`

More addendum artifacts were saved under:

`/tmp/myshare-more-qa-2026-05-18`

Captured:

- 23 screenshots
- 23 UI-tree summaries
- 23 raw UI XML dumps
- Main-tab addendum captured normal and compact screenshots with matching UI-tree summaries and raw XML dumps.
- Review addendum captured pre-implementation Review submissions, post-implementation normal and compact screenshots, UI-tree summaries, and raw XML dumps.
- More addendum captured normal and compact screenshots with matching UI-tree summaries and raw XML dumps.

Key files:

- `normal-06-plan-preview.png`
- `normal-09-trajectory-scrolled.png`
- `normal-10-paywall-top.png`
- `normal-11-paywall-scrolled.png`
- `compact-06-plan-preview.png`
- `compact-10-trajectory-bottom.png`
- `compact-12-paywall-scrolled.png`
- `normal-plan-initial.png`
- `normal-plan-smart-preview.png`
- `normal-plan-smart-gate.png`
- `compact-plan-top.png`
- `compact-plan-smart-preview-bottom.png`
- `compact-plan-smart-gate.png`
- `strategy-normal-top.png`
- `strategy-normal-goal-gate.png`
- `strategy-normal-rule-preview-clean.png`
- `strategy-normal-rule-gate.png`
- `strategy-compact-top-clean.png`
- `strategy-compact-rule-preview-bottom.png`
- `strategy-compact-rule-gate.png`
- `review-initial-empty.png`
- `review-after-first-submit.png`
- `review-after-second-submit.png`
- `review-history-after-two-clean.png`
- `review-history-gate.png`
- `review-implemented-normal-top.png`
- `review-implemented-normal-history-lock.png`
- `review-implemented-normal-gate.png`
- `review-implemented-compact-recommendation.png`
- `review-implemented-compact-history-lock-full.png`
- `more-normal-top-final.png`
- `more-normal-premium-final.png`
- `more-normal-automation-lock.png`
- `more-normal-automation-gate.png`
- `more-compact-top.png`
- `more-compact-premium.png`
- `more-compact-pricing-cta-full.png`
- `more-compact-bottom.png`
- `more-compact-account.png`

## Result

Pass for the onboarding-to-paywall conversion path.

The flow now communicates a coherent value ladder:

- Plan Preview delivers the aha moment with `Safe weekly spend: $103.85`, bills protected, priority move, and ordered payday actions.
- Signup preserves trust by keeping Google sync optional and local mode visible.
- Trajectory makes the Premium bridge concrete with a locked adjustment example: unused money can move toward the selected goal while protecting the weekly guide.
- Paywall continues the same story with a personalized Premium adjustment example and clear Free vs Premium contrast.
- Trial terms and planning-vs-store currency copy remain visible near the sticky CTA.

Main-tab addendum pass:

- The Plan tab now shows a locked Premium preview explaining what Premium would adjust next using `$450.00` priority move and `$103.85` weekly guide values from the plan.
- Tapping the preview opens the contextual `Unlock adaptive adjustments` Premium gate.
- The allocation preview stays in two columns on the normal profile so money values are not cut off by the bottom nav on first view.
- The locked preview stacks correctly on the compact profile, and the CTA remains reachable above the bottom nav after scrolling.
- The Strategy tab now shows a locked multi-goal preview tied to the current priority move and `Emergency fund`, then opens the `Unlock multi-goal payday splits` Premium gate.
- A free Savings rule was created during QA to validate the rule state. The rule card is labeled `STATIC MANUAL RULE`, and the locked preview explains how Premium would coordinate multiple rules after reviews.
- Strategy locked preview cards stack correctly on the compact profile, with CTAs and contextual Premium gates reachable above the bottom nav after scrolling.

Review addendum pass:

- The Review feature was tested before implementation by submitting an on-plan review and then an off-plan review.
- Pre-implementation behavior: review submission updated the trust score and history, but the latest off-plan same-day review was hidden behind the free history lock while the visible free row still showed the older on-plan review.
- Pre-implementation behavior: the history gate copy was too generic, using `Trajectory insights` without showing the concrete value Premium would unlock.
- Post-implementation behavior: the Review tab shows the latest off-plan review first with `Needs attention`, `$850.00` actual flexible spend, `$720.00 (+$130.00)` planned delta, `$50.00` actual goal contribution, and `$180.00 (-$130.00)` planned delta.
- Post-implementation behavior: free users now see a locked `What Premium would adjust next` card after performance stats, using the saved planned values to explain the next adjustment Premium would preview.
- Post-implementation behavior: the hidden-history lock now says `1 more review pattern locked` and explains that Premium compares hidden history with the latest review to produce a next-payday adjustment.
- Tapping both the recommendation preview and hidden-history lock opens the contextual `See full review history` paywall.

More addendum pass:

- The More tab now starts with account status and a payday control-center summary instead of pricing.
- The control-center summary uses the current `$166.16` weekly guide, `$180.00` priority move, `1` saved rule, and `2` reviews so the settings screen still reflects the user's plan.
- Plan settings now appear before the Premium checkout section, keeping reminders, language, currency, and automation controls first.
- The Premium upgrade section now explains review-based automation with the user's weekly guide, priority move, and saved rules before showing annual/monthly pricing.
- The Auto rules lock now says Premium turns reviews into the next rule adjustment, and `See Premium` opens the contextual `Unlock adaptive adjustments` gate.
- Legal links, ad preferences, Google account connection, subscription management, and sign-out remained reachable after the subscription section.

## Compact Check

The compact profile remained usable through the full path:

- Sticky CTAs stayed visible.
- Plan Preview first viewport showed the safe weekly spend and protected amounts without overlap.
- Trajectory required scrolling, but the Premium example, Free vs Adaptive Premium comparison, and `See adaptive plan` CTA were reachable.
- Paywall hero, personalized adjustment preview, pricing, currency notice, and `Start free trial` CTA were reachable.
- Review recommendation, review form, latest review row, and locked history preview remained reachable on the compact profile without overlapping the bottom nav.
- More control-center metrics stacked cleanly on the compact profile, and the Premium preview, pricing CTA, legal links, and account rows remained reachable.

## Residual Risk

- Play Billing checkout handoff was not triggered during this pass to avoid starting an actual purchase flow.
- Main-tab QA from the Phase 6 list remains separate from this onboarding conversion pass.
