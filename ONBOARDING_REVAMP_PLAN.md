# Onboarding Revamp Plan

Date: 2026-05-29  
Status: Implemented in the onboarding first-run path

## Activation Definition

A user is activated when they see a personalized payday plan built from their own income, essentials, and goal.

Everything before that moment must shorten the path to the plan. Anything that explains features, account setup, permissions, or Premium before the plan must be removed or moved later unless it is required to calculate the plan.

## Onboarding Promise

Create a clear payday plan before spending, then choose whether the next payday stays fixed or adapts after a quick review.

## Flow Contract

1. Welcome: state the promise and let users set language/currency.
2. Goal: ask for one protected outcome.
3. Income: ask what arrives and when.
4. Essentials: ask what must be protected first.
5. Plan preview: show the first win from the user's numbers.
6. Premium bridge: one short decision, fixed free plan or adaptive Premium.
7. Paywall: earn the ask with the user's plan and next-payday adaptation promise.
8. Reminder: ask only after value and paywall choice are clear.

## Screen Rules

- One job per screen.
- One sentence of body copy per primary section.
- No feature lists before the first plan preview.
- No signup wall before the first plan preview or Premium bridge.
- No permissions before the user sees value.
- Do not repeat the same Premium promise across multiple cards on one screen.
- Prefer one visual proof over several explanatory cards.
- CTAs must be specific: `Build my plan`, `Save my plan`, `Make next payday adaptive`.
- Long translations must wrap naturally. Compact controls may use labels only.
- Pixel 7 and large font scale must remain readable before shipping onboarding copy/layout changes.

## Product Decisions

- The first win is the plan preview, not account creation.
- The free path remains useful and respectful: a fixed manual payday plan.
- Premium is positioned as adaptive guidance: each quick review can update the next payday.
- The Premium bridge must be short enough to understand in five seconds.
- Detailed coachmarks, account protection, legal links, real purchase handling, and deeper education happen outside this first-run path.

## Visual Polish Rules

- Use polish to increase trust, clarity, and momentum, not to entertain.
- Make the plan preview the most polished moment because it is the first personalized result.
- Use short micro-animation for useful state changes only: goal selection, plan reveal, Premium bridge reveal, reminder confirmation.
- Respect Android reduced-motion settings. When animations are disabled, show static states without delayed reveals.
- Do not add sound in onboarding.
- Do not add confetti for salary, bills, debt, or budget amounts. If a milestone needs confirmation, prefer a checkmark, soft pulse, or static success state.
- Do not add Lottie unless a static card cannot explain the idea clearly. One meaningful explanatory or success animation is the upper bound for this flow.
- Use haptics only for selection and confirmation. Never use haptics for paywall appearance, errors, ordinary navigation, or typing numbers.
- Avoid playful motion around sensitive money inputs. The target emotion is relief, not excitement.

## Double Check

User perspective:
- The user should feel they are making progress, not reading instructions.
- The flow should feel like answering three useful questions to get a plan.
- The Premium choice should feel understandable without financial knowledge.

Conversion perspective:
- Premium should appear after the first win.
- The upgrade reason should be concrete: fixed plan versus adaptive next payday.
- The paywall should inherit the same promise rather than introducing a new story.

Trust perspective:
- Manual-first remains visible.
- No bank connection is required.
- Notifications are requested only after the plan and Premium choice.

Localization and accessibility perspective:
- Long translations must wrap vertically.
- Compact cards may use short labels only.
- Avoid truncation and ellipsis in critical onboarding copy.

Engineering perspective:
- Keep the route order simple.
- Use existing analytics events where possible.
- Avoid introducing new architecture for a copy/layout correction.

## Implementation Applied

- Removed the signup interruption before the Premium bridge.
- Shortened the plan preview to the first win instead of a long explanation stack.
- Replaced the long trajectory explanation with one adaptive-plan card and one fixed-vs-adaptive choice.
- Kept the paywall entry aligned with adaptive next-payday value.
- Updated localized onboarding copy for supported locales.
- Validated with resource/unit tests, debug build, and Pixel 7 emulator checks at normal and large font scale.
- Verified the Portuguese first-win preview after copy tone adjustments.
- Kept onboarding analytics and A/B attribution aligned: start, setup step views/completions, activation, trajectory bridge, paywall view, plan selection, purchase outcomes, reminder setup/skip, onboarding completion, and free-plan selection carry the experiment assignment.
- Added `time_to_first_value_ms` to `onboarding_activation_reached` so activation can be optimized by speed to first personalized plan, not just completion.
- Checked and updated live Firebase Remote Config for project `my-share-finance`: template version `4` matches local defaults for `onboarding_paywall_variant`, `onboarding_conversion_experiment`, `paywall_trial_framing`, and `onboarding_intro_variant`.
- Checked live Firebase A/B Testing: Remote Config experiment `Onboarding trial framing` is still `RUNNING` with primary objective `app_store_subscription_convert` and secondary objectives including `purchase_started` and `onboarding_activation_reached`.
- Added app-side support for an intro-promise A/B test through `onboarding_intro_variant` with supported values `plan_first` and `spend_clarity`. Firebase CLI/MCP can publish the parameter, but experiment creation remains a Firebase Console/API operation because the installed CLI exposes list/get/delete for Remote Config experiments, not create.
- Added a shared onboarding motion policy with short result reveals, Android reduced-motion fallback, and focused haptics on goal selection, plan saving, reminder cadence selection, and reminder confirmation.
