# Premium Conversion Improvement Plan

Date: 2026-05-17
Status: Working plan

## Goal

Make My Share good enough that a real user would not only start the Premium trial, but keep paying after the trial because Premium clearly improves each payday decision.

The current product is useful as a free manual payday planner. Premium is close, but the value still reads too much like extra capacity: more goals, more rules, more history. The stronger premium promise should be:

> Adaptive Payday Autopilot: My Share learns from each check-in and recommends exact adjustments for the next payday.

The onboarding funnel should convert as many qualified users as possible before they reach Home. That does not mean forcing an early paywall. It means onboarding must build desire in the right order:

1. Show the user a useful payday plan from their own numbers.
2. Make the manual path feel valuable and trustworthy.
3. Make Premium feel like the obvious way to keep that plan improving automatically.
4. Ask for the trial only after the user has seen concrete value.

## Baseline From Latest Validation

Validated on `emulator-5554` from a clean debug install on 2026-05-17.

Covered:
- Fresh onboarding from welcome through home.
- Salary, fixed-cost, and allocation validation.
- Plan preview, local signup, trajectory, paywall, Play Billing handoff.
- Reminder setup and Android notification permission.
- Bank sync placeholder.
- Plan, Strategy, Review, and More tabs with scrolling.
- Goal edit, rule edit, contextual premium gates, review submission/history lock, account dialog, sign-out dialog, and ad consent.
- `./gradlew testDebugUnitTest` passed.

Key decision:
- I would use the free app.
- I would start the Premium trial only out of curiosity.
- I would not keep paying yet until Premium proves it makes the next payday plan smarter.

## Plan Analysis

The work should not start with broad visual polish. The two biggest blockers are trust and value clarity:

1. Billing trust is a hard blocker. A user who sees an incorrect billing state will not pay.
2. Premium value must become concrete before UI polish can convert. The product needs to show what Premium would do differently for the user's actual plan.
3. Onboarding is the highest-leverage conversion surface. It must feel premium before the paywall, and it must progressively prime the Premium promise without blocking the user's first value moment.
4. Main-tab polish matters after the trial starts. A user should repeatedly see Premium doing useful work, not just see locked limits.

Recommended order:
1. Fix billing state and trust copy.
2. Redesign onboarding around a conversion-focused value ladder.
3. Add domain-level adaptive recommendation logic.
4. Put locked recommendation previews at premium gates.
5. Strengthen paywall and tab-level premium surfaces.
6. Run a full emulator verification pass.

## Conversion And Copy Principles

Marketing copy should follow a premium, trust-first approach:

- Lead with the user's outcome, not the app's features.
- Use concrete numbers from the user's plan whenever possible.
- Show the pain without fearmongering: reactive spending, forgotten payday moves, drift after a few weeks.
- Position Premium as the shortcut to consistency, not as a punishment for staying free.
- Prefer specific verbs: `adjust`, `apply`, `protect`, `move`, `repeat`, `recover`.
- Avoid vague wealth language unless tied to an actual calculation.
- Keep risk reversal visible: free trial, Google Play checkout, cancel anytime, manual-first, no bank sync required.
- Keep the free path respectful. Users who do not upgrade should still feel the app is useful.

Recommended copy arc:

1. Welcome: promise a clear next payday plan.
2. Inputs: explain what each input changes.
3. Plan preview: show immediate personal value.
4. Trajectory: show the first emotional payoff.
5. Paywall: offer Premium as the way to keep the plan adapting automatically.
6. Reminder and Home: reinforce the habit loop for users who continue free.

## Phase 1: Billing Trust

Status after first implementation pass on 2026-05-17:
- Done: Play Billing launch results and purchase updates now map to truthful in-app messages.
- Done: Onboarding paywall and More replace stale handoff copy with failed/canceled/pending/completed states.
- Done: Unit tests cover billing message mapping and ViewModel failure/cancel paths.
- Validated: clean emulator onboarding paywall and More purchase attempts return to `Google Play could not open checkout...` after Play Billing response `5`.
- Done: paywall and More explain planning currency vs Google Play billing currency when they differ.

### Problems

- Google Play can show an external error: this build is not configured for billing through Google Play.
- After dismissing that error, My Share still says: `Play purchase screen opened. Finish there to activate Premium.`
- That stale message appears across paywall, More, and premium bottom sheets.
- Planning currency can be USD while Play billing is EUR, with no explanation.

### Improvements

- Replace one-way handoff messaging with explicit purchase states:
  - `Idle`
  - `Preparing`
  - `PlayOpened`
  - `Canceled`
  - `Unavailable`
  - `Failed`
  - `Restoring`
  - `Restored`
  - `NoPurchaseFound`
- Make `BillingClientWrapper.launchBillingFlow` return or emit the `BillingResult` from `launchBillingFlow`.
- Clear handoff messages when Play returns a non-OK response.
- Show user-facing copy for canceled, unavailable, and failed states.
- Add planning-vs-store currency copy near the paywall terms when currencies differ:
  - `Your plan uses USD. Google Play charges in EUR based on your Play Store region.`

### Acceptance Criteria

- Tapping `Start free trial` and getting a Play error returns to a truthful in-app state.
- No premium gate says the Play purchase screen is open after Play has already returned an error.
- The retry path is clear.
- Currency mismatch is explained in plain language.
- Unit tests cover billing state mapping.

## Phase 2: Adaptive Payday Autopilot

### Problems

- Premium currently sells mostly quantity: more goals, rules, and history.
- Review history lock is not compelling enough.
- `Smart adjustments` sends users toward More instead of showing what would change.

### Product Direction

Free:
- One plan.
- One goal.
- Static payday rules.
- Manual check-ins.
- Limited review history.

Premium:
- Adaptive next-payday recommendations after reviews.
- Multiple goals.
- Multiple recurring rules.
- Full history and trend insights.
- One-tap apply for recommended plan/rule changes.

### Domain Work

Create a use case such as `CreatePaydayAdjustmentRecommendationUseCase`.

Inputs:
- Current salary plan.
- Active goals.
- Current rules.
- Recent manual reviews.
- User preferences.

Outputs:
- Recommendation headline.
- Suggested flexible spend adjustment.
- Suggested goal contribution adjustment.
- Suggested rule percentage changes.
- Estimated impact on goal date when possible.
- Confidence/explanation copy key.
- Whether the recommendation is applyable or preview-only.

Example outputs:
- `Move $100 more to Emergency fund next payday.`
- `You underspent flexible by $75 twice. Apply $50 of that buffer to your goal.`
- `Raise Savings from 35% to 38% and keep weekly spend above $160.`

### Acceptance Criteria

- Review submission produces a recommendation preview.
- Free users can see the recommendation but cannot apply it.
- Premium users can apply the recommendation to plan/rules.
- Recommendation logic has focused unit tests.
- Copy is specific to the user's numbers, not generic premium marketing.

## Phase 3: Premium Gates That Prove Value

### Problems

- Current gates are contextual, but still descriptive.
- The most convincing value should appear exactly when the user hits a limitation.

### Improvements

- Plan `Smart adjustments` gate:
  - Show locked before/after payday plan preview.
  - Make the CTA `Apply with Premium` or `Start trial to apply`.
- Review `Trajectory insights` gate:
  - Show the next recommended adjustment from the new use case.
  - Lock full trend history and applying the adjustment.
- Strategy `Add another goal` gate:
  - Show how the next payday could split across two named goals.
- Strategy `Add another rule` gate:
  - Show a concrete rule preview such as savings + investing + debt.
- More `Auto rules` gate:
  - Keep the two-step explanation, but include the user's current rule count and next automatic action.

### Acceptance Criteria

- Every premium gate answers: `What will Premium do for my actual plan?`
- Gates avoid generic phrases such as `scale your wealth` unless tied to a specific result.
- Upgrade CTA context is preserved in analytics source values.

## Phase 4: Onboarding UX And Premium Feel

Status after onboarding value-ladder pass on 2026-05-18:
- Done: Plan Preview no longer presents two different choices that both lead to account setup; the CTA now honestly says `Continue to account setup`.
- Done: The onboarding paywall now shows a personalized adaptive-plan preview using the user's weekly spend guide and priority move.
- Done: The paywall value copy now frames Premium around review-based adjustment guidance, while keeping the free manual path visible.
- Done: The paywall now shows a concrete example Premium adjustment based on the user's plan, such as moving an unused buffer toward the selected goal while protecting the weekly guide.
- Done: Trajectory now bridges into the paywall with review-based guidance copy tied to the user's weekly guide and priority transfer.
- Done: Welcome now leads with a concrete payday outcome, a compact outcome card, a manual-first trust cue, quieter language/currency controls, and a sticky CTA.
- Done: Welcome preference controls now feel less crowded: the currency picker uses a curated high-confidence currency list, currency rows separate code/name/symbol clearly, and language/currency selections update the visible app state immediately.
- Done: Welcome now includes a native Compose payday-flow visual, and Trajectory now includes a plan-backed progress path using the user's weekly guide, priority transfer, and goal date.
- Done: Goal Picker now uses outcome-first copy, a first-goal-free Premium cue, compact icon tiles, and a selected-goal details panel validated on normal and compact emulator viewports.
- Done: Salary now frames income as the source for weekly guide, goal path, and reminders; income preview updates immediately, and monthly/biweekly scheduling was validated on normal and compact emulator viewports.
- Done: Fixed Costs now frames essentials as protected first, shows live protected/remaining feedback, uses compact guardrail and strategy selectors, and adds a Premium recurring-rules cue validated on normal and compact emulator viewports.
- Done: Allocation now frames available money after essentials, shows a clearer allocated/remaining summary, supports percent/fixed planning with live equivalents, and adds a Premium refinement cue validated on normal and compact emulator viewports.
- Done: Plan Preview is now the onboarding aha reveal: it leads with safe weekly spend, bills protected, priority move, ordered payday actions, goal path, and a static-vs-adaptive Premium teaser validated on normal and compact emulator viewports.
- Done: Trajectory now makes the manual-to-adaptive bridge concrete with a locked Premium adjustment example based on the user's weekly guide and goal transfer, plus clearer adaptive-plan CTA copy validated on normal and compact emulator viewports.
- Done: Final onboarding-to-paywall QA evidence pass captured normal and compact emulator screenshots/UI-tree summaries in `/tmp/myshare-onboarding-qa-2026-05-18` and summarized results in `docs/ui-ux-qa-notes-2026-05-18.md`.
- Pending: Move into main-tab Premium value surfaces so the product keeps proving Premium after onboarding.

### Problems

- Some onboarding screens still feel like plain forms.
- Visual quality varies across the setup.
- A few screens have large empty gaps or weak hierarchy.
- The conversion intent is not yet strong enough before the paywall.
- Premium is introduced mostly as automation after the plan, rather than being gradually framed as the best version of the plan.

### UX Principles

- Each screen should feel like a guided financial setup step, not a data-entry chore.
- Keep sticky CTAs, clear validation, and scroll-safe content.
- Use compact premium-looking cards with meaningful content.
- Avoid decorative filler.
- Every screen should answer: `Why are we asking this, and what will it affect?`
- Every screen should also quietly build toward the Premium promise: `This plan can run manually for free, or adapt automatically with Premium.`
- Conversion copy should be confident and specific, not loud. The app should feel like a financial assistant, not an aggressive subscription funnel.

### Onboarding Conversion Ladder

Welcome:
- Hook: `Know exactly what to do when money lands.`
- Support: `Build a payday plan in under two minutes.`
- Trust cue: `Manual-first. No bank connection required.`
- Conversion role: create curiosity without asking for payment.

Goal Picker:
- Hook: `Pick the outcome this payday should protect.`
- Support: `Your first goal is free. Premium can split each payday across every goal that matters.`
- Conversion role: introduce the free limit as a natural product boundary.

Salary:
- Hook: `Tell My Share what arrives each payday.`
- Support: `This becomes your weekly spend guide and goal path.`
- Conversion role: make the user feel every input is creating future value.

Fixed Costs:
- Hook: `Protect essentials before anything else.`
- Support: `Premium can keep this protection running automatically every payday.`
- Conversion role: connect automation to trust and consistency.

Allocation:
- Hook: `Shape what the rest of your money should do.`
- Support: `Start with a static split. Premium can adjust it after each review.`
- Conversion role: make the future adaptive benefit visible before the paywall.

Plan Preview:
- Hook: `Your payday action plan is ready.`
- Support: show exact moves from the user's numbers.
- Conversion role: deliver the first value moment before asking for trial.
- CTA copy should avoid overpromising. Prefer `Continue` or `Secure this plan` over vague language like `Automate my mission` unless the next step clearly explains the paywall.

Signup:
- Hook: `Save this plan safely.`
- Support: `Google sync is optional. Local mode works now.`
- Conversion role: preserve trust before monetization.

Trajectory:
- Hook: `This is where consistency takes you.`
- Support: `Premium keeps this path adapting after each check-in.`
- Conversion role: bridge from personal outcome to Premium.

Paywall:
- Hook: `Let My Share adjust every payday for you.`
- Support: `Start with your plan. After each review, Premium recommends what to change next.`
- Conversion role: ask for trial after value has been proven.
- Risk reversal: trial terms, Play checkout, cancellation, manual-first plan.

### Screens To Improve

Welcome:
- Make the first screen feel more premium and more concrete.
- Add a concise signal of the outcome: next payday plan, weekly spend guide, goal path.
- Keep language/currency controls discoverable but visually quieter.
- Replace generic aspiration with a direct payday outcome.

Goal Picker:
- Improve card hierarchy and reduce form heaviness.
- Show a small `This becomes your first free goal` cue.
- Keep custom goal fields visually tied to the selected goal type.
- Use copy that frames Premium positively: `Premium can track all your goals together`, not only `Upgrade to add more`.

Salary:
- Make it feel like a payday setup, not a generic form.
- Add a compact preview: `This becomes your income per payday.`
- Keep validation visible and specific.
- Add a small outcome preview after valid input where practical: weekly guide or payday path.

Fixed Costs:
- Show why fixed costs matter: `Protected first, then the rest is allocated.`
- When fixed costs exceed income, keep the recovery instruction strong.
- Make presets communicate practical impact, not just names.
- Make Premium automation feel tied to avoiding missed payday moves.

Allocation:
- Make remaining/allocated state more visual and easier to trust.
- Consider a summary card before fields:
  - `Available after fixed costs: $1,500`
  - `Goal path: September 2026`
- Keep all allocation categories scroll-safe with the sticky CTA.
- Make static-vs-adaptive clear: free users are choosing today's split; Premium can refine it over time.

Plan Preview:
- Completed: make this the first true value reveal.
- Lead with `Your payday plan is ready` and the user's safe weekly spend.
- Show bills protected, priority move, and ordered payday actions before any Premium tease.
- Keep `Continue to account setup` as the honest CTA.
- Add Premium only after value is visible: free saves a static split, Premium reviews what happened and suggests the next adjustment.

Signup:
- Current compact layout is acceptable.
- Keep Google optional and local mode clearly first-class.

Trajectory:
- Completed: make the Premium bridge concrete.
- Show a locked adjustment example under the trajectory summary.
- Tie the example to the user's weekly guide and goal transfer.
- Keep the CTA honest by pointing to the adaptive plan offer.

Paywall:
- Lead with Adaptive Payday Autopilot.
- Keep pricing and trial terms close to CTA.
- Add a concise Free vs Premium comparison.
- Use the user's plan values in the hero or benefit list when possible.
- The primary CTA should match trial eligibility:
  - `Start free trial`
  - `Start Premium`
  - `Try Adaptive Autopilot`

### Acceptance Criteria

- Onboarding feels visually consistent from welcome to paywall.
- Inputs do not feel like raw forms.
- Sticky CTAs remain above system navigation.
- No screen relies on large empty space to look premium.
- The user sees at least three Premium value cues before the paywall, each tied to their plan rather than generic benefits.
- The paywall explains Premium in one sentence without requiring the feature grid.
- The free path remains clear and respectful.
- Emulator screenshots are captured for each onboarding step.

## Phase 5: Main Tab Polish

Status after More tab premium value-surface pass on 2026-05-18:
- Done: Plan `Smart adjustments` now renders a locked adaptive-adjustment preview tied to the user's weekly guide and priority move instead of routing generically to More.
- Done: The preview opens the contextual `SmartAutomation` Premium gate with concrete adaptive-adjustment copy.
- Done: The allocation grid keeps two columns on normal-width phones and stacks only on genuinely narrow or large-font layouts, preventing the first viewport from cutting off money values.
- Done: Strategy `Add another goal` now renders a locked multi-goal payday split preview tied to the user's current priority move and first goal, then opens the contextual `MultipleGoals` gate.
- Done: Strategy rules now label free rules as `Static manual rule` and show a locked adaptive-rules preview tied to the user's current rule before opening the contextual `MultipleRules` gate.
- Done: Review was exercised before implementation by submitting an on-plan review and then an off-plan review. This exposed that the latest same-day review could be hidden behind the free history lock.
- Done: Review history now shows the latest saved review first, review deltas use the saved planned-value snapshots, and free users see a locked `What Premium would adjust next` preview after a review.
- Done: Review history lock copy now shows a concrete locked pattern preview instead of generic trajectory language.
- Done: Review performance analytics are now Premium-only: free users see a locked trend preview from the latest check-in, while Premium users get score, streak, sparkline, savings, and coaching state.
- Done: More now behaves like a control center first: profile, current weekly guide, priority move, rule count, review count, reminders, language, currency, and automation before checkout.
- Done: More subscription copy now explains the Premium upgrade with the user's current weekly guide, priority move, saved rules, and review-based automation instead of opening with a generic pricing dump.
- Done: Auto rules lock copy now points to review-based rule adjustments and opens the contextual adaptive-adjustment gate.
- Validated: normal and compact emulator passes captured Plan and Strategy screenshots/UI-tree summaries under `/tmp/myshare-main-tab-qa-2026-05-18`, Review under `/tmp/myshare-review-qa-2026-05-18`, and More under `/tmp/myshare-more-qa-2026-05-18`.
- Done: Phase 6 full end-to-end verification pass completed on 2026-05-18.

### Plan

- Make `Smart adjustments` open a locked recommendation preview, not just route to More.
- Ensure initial viewport does not look clipped, even if more content is scrollable.
- Add bottom padding where financial rows or cards can sit behind nav.

### Strategy

- Label generated rules as static/manual for free users.
- Show what adaptive Premium would do differently.
- Keep goal/rule edit screens compact and scroll-safe.

### Review

- Completed: turn review submission into a stronger Premium conversion moment.
- After saving, show:
  - performance update
  - locked next-adjustment preview for free users
  - latest review with saved planned-vs-actual deltas
- Completed: make history lock more valuable by showing a locked insight preview.

### More

- Completed: keep account/settings first, subscription second.
- Completed: reduce the pricing-dump feeling by leading with a control-center summary and a concrete Premium automation preview.
- Completed: keep ad preferences, legal links, Google connect, subscription management, and sign-out reachable.
- Completed: make subscription management clear for free users without making the whole More tab feel like a paywall.

### Acceptance Criteria

- User sees premium value in Plan, Strategy, Review, and More without repetitive copy.
- Bottom nav never hides core actions or money values.
- More remains useful as settings even for users who do not upgrade.

## Phase 6: Verification

Status after full verification pass on 2026-05-18:
- Done: Clean install, launch, and all manual QA interactions were run only on `emulator-5554`; the connected physical tablet stayed untouched.
- Done: `./gradlew testDebugUnitTest --console=plain` passed.
- Done: Full onboarding validated from Welcome through goal, income, fixed costs, allocation, plan preview, signup, trajectory, paywall, reminder setup, bank-sync skip, and Home.
- Done: Blank and invalid validation states were checked for salary and fixed costs.
- Done: Paywall checkout and restore paths were exercised. The Play Billing unavailable/error state now returns truthful in-app copy instead of implying Premium was activated or that purchase completion is still pending.
- Done: Reminder permission deny and allow paths were exercised from onboarding and More.
- Done: Plan, Strategy, Review, and More were exercised end to end, including smart-adjustment gates, goal/rule edit entry points, review submission/history/recommendation states, language/currency dialogs, legal/account rows, and sign-out confirmation.
- Done: Compact-screen checks were run at `720x1280 / 360dpi`; key onboarding, Plan, Strategy, Review, and More surfaces stayed scrollable and reachable.
- Evidence: screenshots, UI-tree dumps, summaries, and log captures are saved under `/tmp/myshare-phase6-qa-2026-05-18`.
- Residual: More paywall checkout feedback on compact screens can push the `Start free trial` button slightly farther down, but it remains reachable after one extra scroll.
- Residual: Logcat contains emulator/system noise, including a Bluetooth process abort outside `pt.ms.myshare`, Google Play/Firebase first-run warnings, and StrictMode stack traces around Firebase/Auth initialization. No `pt.ms.myshare` fatal crash or ANR was found during the pass.

### Automated Tests

- Billing state mapper tests.
- Adaptive recommendation use-case tests.
- ViewModel tests for review submission producing recommendation state.
- Premium gate state tests where practical.
- Existing unit tests stay green.

### Emulator Pass

Run a clean-install pass covering:
- Full onboarding.
- Blank and invalid validation states.
- Paywall scroll and CTA.
- Play Billing error/cancel path.
- Reminder permission allow and deny paths.
- Bank sync skip.
- Plan scroll and Smart adjustment gate.
- Strategy goal/rule edit and locked extra goal/rule gates.
- Review submit, history lock, recommendation preview.
- More account, settings, automation gate, ad preferences, legal links, sign out.

### Evidence

Save screenshots and UI-tree summaries under a dated temp folder, then summarize the outcome in `docs/ui-ux-qa-notes-YYYY-MM-DD.md` or a follow-up markdown file.

## Definition Of Premium-Ready

Premium is ready when all of these are true:

- A user understands exactly what Premium will do for their current payday plan.
- The trial CTA can fail, cancel, or restore without misleading state.
- Review creates a clear `the app got smarter` moment.
- Premium gates show concrete locked value, not just feature names.
- Onboarding looks polished enough that the paywall feels earned.
- The main tabs do not hide or clip important financial information.
- A full emulator pass validates the end-to-end experience.

## Phase 7: Premium Lifecycle And Downgrade Hardening

Status after implementation pass on 2026-05-18:
- Done: Entitlement is now modeled as `UNKNOWN`, `FREE`, `PRO`, and `GRACE_PERIOD` instead of only a raw boolean.
- Done: Billing refresh runs on app resume and defers safely until the Google Play Billing client is connected.
- Done: Server entitlement snapshots from Firestore can override local billing state, including expired, revoked, account-hold, and grace-period states.
- Done: Losing Premium disables saved automation and hides automation as inactive in More.
- Done: Extra goals and payday rules created during Premium remain visible after downgrade, but locked rows open the relevant Premium gate instead of edit screens.
- Done: Direct add routes now block second goals/rules when Premium is inactive, so users cannot bypass the Strategy tab gates.
- Done: Firebase `verifySubscription` now writes an explicit entitlement snapshot to the user document and `/users/{uid}/entitlements/current`.
- Done: Backend Premium lifecycle hardening added a Google Play RTDN handler, scheduled entitlement revalidation, and a server-only purchase-token index.
- Done: User-readable entitlement snapshots now store purchase-token hashes instead of raw Play purchase tokens; raw tokens are kept in server-only Firestore documents.
- Done: Function cost guardrails keep warm instances at zero, cap billing lifecycle scale-out, and run scheduled revalidation daily because RTDN is the primary lifecycle path.
- Done: Firebase `verifySubscription` now attempts Android Publisher server-side acknowledgement for callable purchase verification before the app falls back to BillingClient acknowledgement.
- Done: Firebase functions and Firestore rules were deployed to `my-share-finance`.
- Done: Play Console Real-time Developer Notifications are enabled for `projects/my-share-finance/topics/play-billing-rtdn`, with subscriptions and voided purchases selected.
- Validated: focused premium lifecycle unit tests, full `testDebugUnitTest`, androidTest Kotlin compile, Firebase function lint/syntax/unit checks, Firebase rules tests, emulator launch on `emulator-5554`, and Play Console RTDN test notification received by `handlePlayBillingNotification`.
- Validated: release build `3.0.1` / `versionCode 9` was installed on `emulator-5554`, completed a Google Play test subscription, wrote `PRO` entitlement state in Firestore, showed the post-purchase account-protection prompt, preserved Premium on cold start, and showed the Premium control center in More.

Residual:
- The server-side acknowledgement path was deployed after the current test subscription was already completed; the next fresh Play test purchase should confirm `serverAcknowledgementStatus=acknowledged` on the entitlement/token documents.
- Restore, grace-period, account-hold, renewal, cancel, and refund sequences still need Play Console/internal-test validation with subscription test cards.
- A real internal-test purchase should confirm renewal, cancel, refund, grace-period, and account-hold events reach Firebase with production Play payloads.

## Phase 8: Real Premium Product Work

Status after Premium recommendation apply-flow pass on 2026-05-18:
- Done: Premium review recommendations now open a confirmation sheet before mutating payday rules.
- Done: The confirmation shows the current flexible guide and priority move beside the recommended next guide and next move.
- Done: After applying, Premium users get a post-apply sheet with `Review rules` and `Undo adjustment`.
- Done: Undo restores only the rules affected by the recommendation, preserving unrelated strategy data.
- Done: The post-undo state uses inline card feedback instead of a duplicate snackbar.
- Validated: focused `HomeViewModelTest` coverage, localization string parity, release build install on `emulator-5554`, recommendation confirmation, apply, Strategy handoff, and undo flow.

Status after Smart Adjustments control-center pass on 2026-05-18:
- Done: Premium users now see a Smart Adjustments control card in More with `Watching` or `Paused` status.
- Done: The control card surfaces pending recommendation values, signal direction, confidence, and review count.
- Done: `Review adjustment` routes directly to the Review recommendation so rule changes still require explicit user approval.
- Done: `Enable watch` and `Pause watch` control the existing Premium automation state from the card.
- Validated: focused `HomeViewModelTest` coverage, localization string parity, release build install on `emulator-5554`, normal-screen More card, Review handoff, automation toggle, and compact `720x1280 / 360dpi` layout. The temporary Firestore Premium snapshot used for this UI validation was restored to `FREE`.

Status after Premium scheduled check-in pass on 2026-05-19:
- Done: Added a domain-level Premium check-in planner that calculates ready, overdue, scheduled, and reviewed states from the user's payday cadence, latest review, reminders, and automation state.
- Done: Premium users now see a check-in card in Review so the manual form is framed as the next guided payday review instead of a blank input surface.
- Done: More Smart Adjustments now shows the next check-in state and changes the primary action to `Start check-in` when a payday review is due.
- Done: Reminder setup is reachable from the Premium check-in card when reminders are off, while recommendations still require explicit review and approval before rules change.
- Validated: focused Premium check-in and HomeViewModel tests, full `testDebugUnitTest`, release build, normal emulator Review/More check-in surfaces, compact `720x1280 / 360dpi` Review/More layouts, and More-to-Review handoff. The temporary Firestore Premium snapshot used for validation was restored to `FREE`.

Status after Premium advanced-account Strategy pass on 2026-05-19:
- Done: Premium Strategy now keeps long goal and rule collections scan-friendly by showing the first three items inline and moving the full editable list into dedicated bottom sheets.
- Done: The compact top bar now keeps Premium as a small icon badge instead of pushing the badge onto a second line on small phones.
- Done: Compact Plan metrics now render as one aligned metrics panel, so income and weekly guide no longer look like misplaced cards on small screens.
- Done: New goal/rule copy was softened from technical `stack/archive` language to user-facing labels like `Your goals`, `See every rule`, and `All your goals`.
- Validated: `testDebugUnitTest`, release build, release install on `emulator-5554`, normal Strategy goal/rule list and bottom sheets, compact `720x1280 / 360dpi` Plan header/metrics, Strategy summaries, and goal/rule bottom sheets. Temporary Firestore Premium multi-goal/rule QA data was restored to its previous state, and the emulator was restored to `1080x2400 / 420dpi`.

Status after Premium multi-goal payday split implementation pass on 2026-05-19:
- Done: Added a domain-level Premium payday split that turns the user's next priority move into a deterministic multi-goal split.
- Done: Premium Strategy now replaces the generic multi-goal summary with a concrete `Next payday split` card when the user has a plan, a positive priority move, and multiple active goals.
- Done: The split card keeps long goal lists scan-friendly by showing the first three split rows inline and routing the full list to `See every goal`.
- Tested: focused split use-case coverage, HomeViewModel state coverage, and full `testDebugUnitTest`.
- Validated: release build, release install on `emulator-5554`, normal Strategy split card, normal all-goals sheet, compact `720x1280 / 360dpi` split card top and scrolled states, and compact all-goals sheet. Temporary Firestore Premium goal data was restored to the previous `FREE` state, and the emulator was restored to `1080x2400 / 420dpi`.

Status after Premium multi-rule payday mix implementation pass on 2026-05-19:
- Done: Added a domain-level Premium rule payday mix that calculates the user's next priority move per saved priority rule, including percentage and fixed rules.
- Done: Premium Strategy now replaces the generic multi-rule summary with a concrete `Next payday rules` card when the user has a plan, a positive priority move, and multiple saved priority rules.
- Done: The mix card keeps long rule lists scan-friendly by showing the first three rule rows inline and routing the full list to `See every rule`.
- Tested: focused rule-mix use-case coverage, HomeViewModel state coverage, and full `testDebugUnitTest`.
- Validated: release build, release install on `emulator-5554`, normal Strategy rule mix card, normal all-rules sheet, compact `720x1280 / 360dpi` rule mix top and scrolled states, and compact all-rules sheet. Temporary Firestore Premium rule data was restored to the previous `FREE` state, and the emulator was restored to `1080x2400 / 420dpi`.

Status after Premium history/coaching depth implementation pass on 2026-05-19:
- Done: Added a deterministic Premium coaching summary that reads the last six review snapshots and turns them into a strong, steady, or needs-attention pattern.
- Done: Premium Review now shows a concrete pattern card with average flexible spend left over, average extra moved to goals, and the on-track payday rate before the simpler coaching tips.
- Done: The coaching summary layout stacks its metric rows on compact screens and keeps the copy outcome-focused instead of technical.
- Tested: focused coaching-summary use-case coverage, HomeViewModel Premium/free gating coverage, and full `testDebugUnitTest`.
- Validated: release build, release install on `emulator-5554`, normal Review coaching card, compact `720x1280 / 360dpi` coaching card top and metric stack, no app fatal crash in logcat, temporary Firestore Premium review data restored to the previous `FREE` state, and the emulator restored to `1080x2400 / 420dpi`.

Status after Premium post-review loop implementation pass on 2026-05-19:
- Done: Premium users now get a post-review result sheet instead of a generic saved snackbar after submitting a check-in.
- Done: The sheet shows the saved review date, total review count, what Premium learned from the pattern, and the next payday action.
- Done: Applyable recommendations can move directly from the result sheet into the existing confirmation sheet, keeping rule changes explicit and reversible.
- Tested: focused HomeViewModel coverage for Premium post-review result state and full `testDebugUnitTest`.
- Validated: release build, release install on `emulator-5554`, normal Premium post-review result sheet, result-to-adjustment handoff, compact `720x1280 / 360dpi` sheet top and scrolled actions, no app fatal crash in logcat, temporary Firestore Premium review data restored to the previous state, and the emulator restored to `1080x2400 / 420dpi`.

Status after Premium adjustment memory implementation pass on 2026-05-19:
- Done: Applying a Premium payday recommendation now writes a Premium adjustment record with the before/after guide, amount, confidence, review count, affected rules, and applied/undone status.
- Done: More now shows a Premium memory card explaining the latest adjustment in user-friendly language, including the flexible and priority guide change.
- Done: Strategy now marks rules touched by the latest applied Premium adjustment with `Adjusted after check-in`, so users can see what Premium changed.
- Done: Undo marks the adjustment memory as undone and removes the active rule badge instead of leaving stale trust signals.
- Tested: focused HomeViewModel coverage for adjustment memory, full `testDebugUnitTest`, and Firebase rules tests for the new `adjustments` subcollection.
- Validated: release build, Firestore rules deployment, normal Premium apply-to-memory flow, normal and compact `720x1280 / 360dpi` More memory card, compact Strategy adjusted badge, no app fatal crash in logcat, temporary Firestore Premium adjustment data restored to the previous state, and emulator restored to `1080x2400 / 420dpi`.

Status after Premium adjustment history implementation pass on 2026-05-19:
- Done: More now exposes `See adjustment history` from the Premium memory card when more than one Premium adjustment exists.
- Done: Premium users get a dedicated adjustment history sheet with newest-first applied and undone records, each showing the plain-language change, before/after guide values, and affected rule count.
- Done: Free users still do not receive adjustment memory or history state, keeping the value Premium-only.
- Tested: focused HomeViewModel coverage for newest-first history and free-user gating, plus full `testDebugUnitTest`.
- Validated: release build, release install on `emulator-5554`, compact `720x1280 / 360dpi` More memory card history action, compact adjustment history sheet top and scrolled undone record, no app fatal crash in logcat, temporary Firestore Premium history data restored to the previous state, and emulator restored to `1080x2400 / 420dpi`.

Status after Premium missed-check-in recovery implementation pass on 2026-05-19:
- Done: Overdue Premium check-ins now use stronger recovery copy that explains Premium is waiting on the missed review before it can adjust the next payday.
- Done: More now changes the overdue primary action to `Catch up now`, shows `Resume watch` when automation is paused, and offers `Turn reminders on` when reminders are off.
- Done: The compact More recovery summary now stacks the title and late badge so small screens do not truncate the check-in state.
- Tested: focused HomeViewModel coverage for missed-review overdue state with paused automation and reminders off, plus full `testDebugUnitTest`.
- Validated: release build, release install on `emulator-5554`, compact `720x1280 / 360dpi` Review overdue card, compact More recovery card/actions, no app fatal crash in logcat, temporary Firestore Premium missed-check-in data restored to the previous state, and emulator restored to `1080x2400 / 420dpi`.

Status after first-review Premium proof implementation pass on 2026-05-19:
- Done: Review now shows a first-review Premium proof card when the user has no review history and no active Premium check-in card, so the empty state explains the next value moment before the user submits actuals.
- Done: The proof card uses compact-friendly stacked steps for `Review`, `Learn`, and `Adjust`, with a dedicated first-review Premium gate instead of reusing the full-history upsell.
- Done: First-review gate copy now focuses on comparing the next payday with the user's guide, catching drift early, and showing the next adjustment.
- Tested: full `testDebugUnitTest`.
- Validated: debug build, debug install on `emulator-5554`, compact `720x1280 / 360dpi` Review proof card, CTA-to-first-review Premium sheet, no app fatal crash in logcat, and emulator restored to `1080x2400 / 420dpi`.

Status after More inline Premium autopilot proof implementation pass on 2026-05-19:
- Done: The More inline upgrade card now sells `Premium payday autopilot` instead of generic automation, with copy that adapts for users with no reviews, saved reviews, plan values, or missing plan values.
- Done: Free users now see concrete Premium signals in More: review readiness, current weekly guide, and watched rule count.
- Done: The free-vs-paid comparison copy was rewritten to focus on manual adjustment versus each review pointing to the next move.
- Tested: full `testDebugUnitTest`, including localized string parity.
- Validated: debug build, debug install on `emulator-5554`, compact `720x1280 / 360dpi` More upgrade card top and scrolled states, no app fatal crash in logcat, and emulator restored to `1080x2400 / 420dpi`.

Status after onboarding/paywall conversion polish pass on 2026-05-19:
- Done: Plan Preview now reveals the first payday plan card with a subtle Compose fade/slide animation so the aha moment feels more intentional without adding decorative filler.
- Done: The onboarding paywall now shows the personalized Premium next-adjustment proof before plan selection, so users see why Premium matters before evaluating price.
- Done: High-impact onboarding, paywall, Premium-gate, coaching, and Home copy was simplified away from technical wording like `blueprint`, `safe-to-spend`, `adaptive`, `rule mix`, and generic `unlock` language.
- Done: English, Portuguese, Spanish, French, German, and Arabic high-impact conversion strings were kept aligned so language switching does not return users to older technical copy.
- Tested: full `testDebugUnitTest` and debug build.
- Validated: debug install on `emulator-5554`, fresh compact onboarding through Welcome, Goal, Salary, Fixed Costs, Plan Preview, Signup, Trajectory, Paywall, Reminder skip, and Home Plan at `720x1280 / 360dpi`. Paywall plan cards remain reachable, the annual badge does not overlap the selected state, Home core metrics stay aligned, no app fatal crash or ANR appeared in logcat, and the emulator was restored to `1080x2400 / 420dpi`.

Status after Premium review momentum implementation pass on 2026-05-19:
- Done: Added a domain-level Premium review momentum use case that turns total reviews and current streak into the next review milestone.
- Done: Premium Review now shows a compact momentum card with a progress bar, review count, streak, and next milestone so the check-in habit feels like visible progress instead of raw data entry.
- Done: The momentum card stacks its chips on compact screens and uses simple retention-focused copy rather than technical analytics language.
- Tested: focused review-momentum use-case coverage, HomeViewModel Premium/free gating coverage, full `testDebugUnitTest`, and debug build.
- Validated: debug install on `emulator-5554`, compact `720x1280 / 360dpi` Review route stability, no app fatal crash or ANR in logcat, and emulator restored to `1080x2400 / 420dpi`.

Status after Premium Plan proof implementation pass on 2026-05-19:
- Done: The project experience bar was written into `AGENTS.md` so future passes keep fixing issues during validation, avoid technical copy, protect onboarding conversion, and preserve compact-screen polish.
- Done: Premium users now see a compact Plan-tab proof card showing that the plan is being watched, when the next review is due, and whether the Premium watch is on or paused.
- Done: The card uses plain, outcome-first copy and stays separate from the More control center so Premium value is visible without repeating settings content.
- Done: Follow-up validation found the paused state could imply the plan was already being watched. The paused card now says Premium is ready, shows a visible `Turn watch on` action, and routes to More controls.
- Tested: full `testDebugUnitTest` and debug build.
- Validated: debug install on `emulator-5554`, Google Play test subscription completion, server entitlement snapshot `PRO`, server acknowledgement, compact `720x1280 / 360dpi` Premium Plan proof card, paused-state card-to-More handoff, no app fatal crash or ANR in logcat, and emulator restored to `1080x2400 / 420dpi`.

Status after Premium watch activation pass on 2026-05-19:
- Done: A completed Premium purchase now enables the Premium watch automatically so the paid product starts delivering value immediately instead of landing in a paused state.
- Done: The post-purchase account-protection prompt is preserved, and Premium still only recommends changes until the user explicitly approves an adjustment.
- Tested: HomeViewModel purchase-completion coverage now verifies the watch setting is saved and visible after Premium activation, plus full `testDebugUnitTest` and debug build.
- Validated: debug install on `emulator-5554`, compact `720x1280 / 360dpi` paused Plan-to-More path, manual watch enable in More, compact active Plan proof card, no app fatal crash or ANR in logcat, and emulator restored to `1080x2400 / 420dpi`.

Status after Premium watch waiting-state copy pass on 2026-05-19:
- Done: More's Premium watch card now shows `Waiting for first review` instead of `Review adjustment` when there is no recommendation yet, so the disabled action no longer feels broken.
- Done: The new waiting action copy is localized across supported languages.
- Tested: full `testDebugUnitTest` and debug build.
- Validated: debug install on `emulator-5554`, compact `720x1280 / 360dpi` More waiting state, no app fatal crash or ANR in logcat, and emulator restored to `1080x2400 / 420dpi`.

Status after Premium card shadow cleanup pass on 2026-05-19:
- Done: Removed Compose elevation from the Plan and More Premium cards so dark-mode rounded cards no longer render a square shadow block behind the content.
- Done: Kept the Premium border and tonal container treatment, preserving the premium feel without the visual artifact.
- Tested: full `testDebugUnitTest` and debug build.
- Validated: debug install on `emulator-5554`, compact `720x1280 / 360dpi` active Premium More watch card, active Premium Plan proof card, Google Play test purchase state still active, and no app fatal crash or ANR in logcat.

Status after Home top-bar duplication cleanup pass on 2026-05-19:
- Done: Removed the selected tab label from the Home top bar because the bottom navigation already communicates location and the duplicate label used valuable compact-screen space.
- Done: Kept the top bar brand-only with the Premium status badge, preserving app identity while making the shell cleaner.
- Tested: full `testDebugUnitTest` and debug build.
- Validated: debug install on `emulator-5554`, compact `720x1280 / 360dpi` Plan and More tabs, no repeated selected-tab label in the top bar, no app fatal crash or ANR in logcat, and emulator restored to `1080x2400 / 420dpi`.

Status after Review Premium elevation cleanup pass on 2026-05-19:
- Done: Removed Compose elevation from the Review Premium check-in card shown in validation, eliminating the square shadow block behind the rounded card.
- Done: Removed the same tinted Premium-container elevation pattern from related Premium recommendation, coaching, goal split, and rule mix cards so the artifact does not reappear on nearby Premium value surfaces.
- Tested: full `testDebugUnitTest` and debug build.
- Validated: debug install on `emulator-5554`, compact `720x1280 / 360dpi` Review Premium check-in card, no app fatal crash or ANR in logcat, and emulator restored to `1080x2400 / 420dpi`.

Next:
- Continue the next Premium product slice while keeping compact-screen validation in every pass.
- Continue validating compact-screen layouts when new Premium controls are added.
