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
- Pending: Run a final onboarding-to-paywall QA pass, capture dated evidence, then move into main-tab Premium value surfaces.

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

### Plan

- Make `Smart adjustments` open a locked recommendation preview, not just route to More.
- Ensure initial viewport does not look clipped, even if more content is scrollable.
- Add bottom padding where financial rows or cards can sit behind nav.

### Strategy

- Label generated rules as static/manual for free users.
- Show what adaptive Premium would do differently.
- Keep goal/rule edit screens compact and scroll-safe.

### Review

- Turn review submission into the main Premium conversion moment.
- After saving, show:
  - performance update
  - next recommendation
  - locked apply action for free users
- Make history lock more valuable by showing a locked insight preview.

### More

- Keep account/settings first, subscription second.
- Reduce repeated billing error surfaces.
- Keep ad preferences and legal links reachable.
- Make subscription management clear for free users without feeling like a paywall dump.

### Acceptance Criteria

- User sees premium value in Plan, Strategy, Review, and More without repetitive copy.
- Bottom nav never hides core actions or money values.
- More remains useful as settings even for users who do not upgrade.

## Phase 6: Verification

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
