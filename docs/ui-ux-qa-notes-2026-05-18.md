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

Addendum: Phase 5 Plan-tab premium value-surface validation.

## Environment

- App: debug build installed from the current workspace
- Test command: `./gradlew testDebugUnitTest :app:installDebug --console=plain`
- Plan-tab addendum test/install commands: `./gradlew testDebugUnitTest --console=plain` and `ANDROID_SERIAL=emulator-5554 ./gradlew :app:installDebug --console=plain`
- Device: `emulator-5554`
- Normal profile: `1080x2400 / 420dpi`
- Compact profile: `720x1280 / 320dpi`
- Plan-tab addendum compact profile: `720x1280 / 360dpi`
- Inputs:
  - Goal: Emergency fund
  - Net income per payday: `$1,500`
  - Monthly essentials: `$600`
  - Default allocation split

## Evidence

Artifacts were saved under:

`/tmp/myshare-onboarding-qa-2026-05-18`

Plan-tab addendum artifacts were saved under:

`/tmp/myshare-main-tab-qa-2026-05-18`

Captured:

- 23 screenshots
- 23 UI-tree summaries
- 23 raw UI XML dumps
- Plan-tab addendum captured normal and compact screenshots with matching UI-tree summaries and raw XML dumps.

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

## Result

Pass for the onboarding-to-paywall conversion path.

The flow now communicates a coherent value ladder:

- Plan Preview delivers the aha moment with `Safe weekly spend: $103.85`, bills protected, priority move, and ordered payday actions.
- Signup preserves trust by keeping Google sync optional and local mode visible.
- Trajectory makes the Premium bridge concrete with a locked adjustment example: unused money can move toward the selected goal while protecting the weekly guide.
- Paywall continues the same story with a personalized Premium adjustment example and clear Free vs Premium contrast.
- Trial terms and planning-vs-store currency copy remain visible near the sticky CTA.

Plan-tab addendum pass:

- The Plan tab now shows a locked Premium preview explaining what Premium would adjust next using `$450.00` priority move and `$103.85` weekly guide values from the plan.
- Tapping the preview opens the contextual `Unlock adaptive adjustments` Premium gate.
- The allocation preview stays in two columns on the normal profile so money values are not cut off by the bottom nav on first view.
- The locked preview stacks correctly on the compact profile, and the CTA remains reachable above the bottom nav after scrolling.

## Compact Check

The compact profile remained usable through the full path:

- Sticky CTAs stayed visible.
- Plan Preview first viewport showed the safe weekly spend and protected amounts without overlap.
- Trajectory required scrolling, but the Premium example, Free vs Adaptive Premium comparison, and `See adaptive plan` CTA were reachable.
- Paywall hero, personalized adjustment preview, pricing, currency notice, and `Start free trial` CTA were reachable.

## Residual Risk

- Play Billing checkout handoff was not triggered during this pass to avoid starting an actual purchase flow.
- Strategy, Review, and More still need their planned Premium value-surface pass.
- Main-tab QA from the Phase 6 list remains separate from this onboarding conversion pass.
