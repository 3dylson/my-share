# DESIGN.md: My Share Design System

Version: v1  
Product: My Share  
Platform priority: Android first, phone first  
Implementation target: Jetpack Compose + Material 3  
Status: Source of truth for design, product UI, and AI-assisted implementation

---

## 1. Product intent

### Core promise
**When money comes in, My Share tells you exactly what to do next.**

### What My Share is
My Share is a **salary and payday planning companion**, not a generic investment dashboard and not a bank-sync-first finance app.

It helps people:
- plan what to do with their next salary or paycheck
- split money across fixed costs, spending, savings, goals, investing, and debt
- create recurring rules for each payday
- get reminders and weekly check-ins
- compare plan versus actual over time

### What My Share is not
Do not design My Share as:
- a trading app
- a portfolio tracker first
- a bank-sync-required budgeting product
- a dense finance dashboard with many charts on day 1
- an app that asks for too much setup before showing value

### Product constraints that shape the system
- The first session must feel useful in **60–90 seconds**.
- The app must work well on **mainstream and lower-tier Android devices**.
- The audience is weighted toward **Nigeria, Pakistan, India, and Brazil**, so the system must favor clarity, localization, and low-friction setup.
- Manual-first is the default. Bank sync is optional later and never the core first-run path.
- Trust, calmness, clarity, and ethical monetization matter more than visual novelty.

---

## 2. Experience principles

### 2.1 Trust over novelty
Use familiar Android and Material 3 patterns. Prefer stable layouts, plain language, and obvious actions over expressive or flashy visuals.

### 2.2 Show value before asking
Do not place paywalls, signup walls, or permissions prompts before the user has seen a concrete salary plan preview.

### 2.3 One clear next step per screen
Every major screen must answer: **what should I do next?**

### 2.4 Manual-first, low-friction, low-anxiety
Ask only for the information needed to create a first useful plan. Avoid early bank sync, long forms, or technical terms.

### 2.5 Financial clarity beats feature density
Numbers, dates, and next actions are the main hierarchy. Charts are secondary and must never replace labeled values.

### 2.6 Calm, not sterile
The app should feel reassuring and competent, not cold. Use restrained warmth in copy, spacing, and motion.

### 2.7 Progressive disclosure
Advanced rules, settings, and detailed analysis should appear only after the core plan is understood.

### 2.8 Ethical monetization
Premium must feel like helpful automation and deeper guidance, not removal of artificially created pain.

### 2.9 Build for real Android use
Optimize for small screens, variable hardware performance, and inconsistent connectivity.

---

## 3. Product model and information architecture

### 3.1 Core domain model
The core objects are:
- **Income event / payday**
- **Plan**: how the next salary should be split
- **Allocation**: category + amount or percentage
- **Goal**: target amount and target date
- **Recurring rule**: reusable payday behavior
- **Reminder**: payday, weekly review, bill, goal, drift alert
- **Review**: plan versus actual check-in

### 3.2 Primary top-level destinations
Use **4 top-level destinations max** on compact phones.

Recommended:
1. **Plan** – current salary plan and dashboard
2. **Strategy** – goals, payday rules, and priority setup
3. **Review** – plan versus actual, weekly or monthly check-ins
4. **More** – reminders, recurring rules, settings, account, help

Do not make Reminders or Settings top-level destinations unless product scope expands materially.

### 3.3 Core user loop
1. User enters salary and payday cadence
2. User sets essential obligations and priorities
3. User sees an immediate plan preview
4. User saves the plan
5. User returns on payday or weekly check-in
6. User tracks drift and adjusts
7. User upgrades for recurring automation, deeper tracking, and more goals

---

## 4. Design system contract

### 4.1 Base system
My Share uses a **Material 3-compatible design system** with My Share-specific semantic tokens and component rules.

### 4.2 Customization strategy
- Start from Material 3 foundations
- Customize through **color roles, typography roles, spacing, shapes, and semantic tokens**
- Prefer extending Material 3 rather than replacing it
- Do not create decorative one-off UI when a standard M3 component fits

### 4.3 Theme architecture
Use:
- `MaterialTheme.colorScheme`
- `MaterialTheme.typography`
- `MaterialTheme.shapes`
- My Share semantic extensions for finance-specific states such as success, warning, drift, locked, and helper guidance

### 4.4 Dynamic color
Dynamic color is optional, not mandatory.

Rules:
- It may be enabled only if contrast, trust, and financial legibility remain strong
- Brand identity and semantic clarity matter more than dynamic personalization
- Do not let dynamic color make warnings, money values, or premium states ambiguous

---

## 5. Visual foundations

### 5.1 Color system
The palette must feel calm, credible, and accessible.

#### Suggested brand direction
- Primary tone: deep teal / blue-green
- Secondary tone: muted slate / gray-blue
- Accent usage: restrained
- Avoid neon finance colors, glossy gradients, and high-saturation crypto/trading energy

#### Material 3 color roles
Define and implement:
- `primary`
- `onPrimary`
- `primaryContainer`
- `onPrimaryContainer`
- `secondary`
- `onSecondary`
- `secondaryContainer`
- `onSecondaryContainer`
- `tertiary` only if truly needed
- `onTertiary`
- `background`
- `onBackground`
- `surface`
- `onSurface`
- `surfaceVariant`
- `onSurfaceVariant`
- `surfaceContainer`
- `surfaceContainerHigh`
- `surfaceContainerHighest`
- `outline`
- `outlineVariant`
- `error`
- `onError`
- `errorContainer`
- `onErrorContainer`

#### My Share semantic extension colors
Add semantic aliases for:
- `moneyPositive`
- `moneyNegative`
- `moneyNeutral`
- `driftWarning`
- `goalOnTrack`
- `goalAtRisk`
- `lockedPremium`
- `helperInfo`
- `sensitiveSurface`
- `safeEmptyState`

#### Suggested seed values
These are a starting point, not fixed branding law:
- Primary seed: `#005A70`
- Secondary seed: `#5E6B75`
- Success seed: `#2E7D32`
- Warning seed: `#A66300`
- Error seed: `#B3261E`
- Neutral background family: warm white to cool light gray

#### Color behavior rules
- Light theme is the primary optimization target
- Dark theme must preserve contrast and reduce glare, but not turn the app into a “trading terminal”
- Use color sparingly for emphasis
- Never rely on color alone to communicate meaning
- Financial danger states should be clear but not panic-inducing

### 5.2 Typography
The typography must optimize for legibility, localization, and dense numeric comprehension.

#### Font guidance
Default to Android-friendly system sans behavior:
- Primary: Roboto or a highly readable neutral sans
- Fallbacks and localization: Noto Sans family support where needed
- Numeric emphasis: tabular figures for major amounts if supported

Do **not** hard-require a trendy display font if it reduces readability or localization quality.

#### Typography roles
Define semantic roles:
- `appTitle`
- `screenTitle`
- `sectionTitle`
- `cardTitle`
- `body`
- `bodyStrong`
- `supporting`
- `helper`
- `errorText`
- `cta`
- `moneyPrimary`
- `moneySecondary`
- `moneyTertiary`
- `percentage`
- `dateLabel`
- `caption`

#### Recommended scale
- Screen title: 24–28sp
- Section title: 18–22sp
- Body: 14–16sp
- Helper / caption: 12–13sp
- CTA labels: 14–16sp
- Primary money amount: 28–40sp depending on context, not every screen
- Secondary money amount: 18–24sp

#### Typography rules
- Use sentence case, not all caps, except where platform conventions require it
- Keep line lengths short on phones
- Avoid ultra-light weights
- Keep money labels visually tied to their amounts
- Use consistent decimal alignment where money is shown in lists

### 5.3 Spacing
Use a **4dp base grid** with common steps on 4, 8, 12, 16, 24, and 32dp.

#### Core spacing tokens
- `space-1 = 4dp`
- `space-2 = 8dp`
- `space-3 = 12dp`
- `space-4 = 16dp`
- `space-5 = 24dp`
- `space-6 = 32dp`

#### Layout rules
- Minimum screen horizontal padding: 16dp
- Dense list item vertical padding: 12dp
- Standard card padding: 16dp
- Large summary card padding: 20–24dp
- Section spacing: 24dp between major groups
- Sticky CTA safe spacing from screen edges: minimum 16dp

### 5.4 Shape
Use calm, moderate rounded corners.

#### Shape tokens
- Small: 8dp
- Medium: 12dp
- Large: 16dp
- Extra large: 24dp for bottom sheets and large hero surfaces only

#### Shape rules
- Cards: 12dp default
- Inputs: 12dp
- Buttons: 12dp or platform-appropriate M3 default
- Bottom sheets: 24dp top corners
- Avoid exaggerated softness that makes finance UI feel toy-like

### 5.5 Elevation
Use low elevation and rely more on spacing and surface tone than shadows.

#### Elevation levels
- Flat: 0dp
- Resting card: 1dp
- Featured card: 2dp
- Overlays / modal surfaces: 3–4dp as needed

#### Elevation rules
- Do not stack many elevated cards in one screen
- Do not use heavy shadows
- Prefer tonal separation on lower-tier Android for visual cleanliness and performance

### 5.6 Iconography
- Use Material-symbol style or similarly simple line icons
- Maintain a consistent family
- Standard size: 24dp
- Use 20dp only for tight inline contexts
- All interactive icons must have accessible labels
- Avoid mixed filled/outlined/icon packs unless intentional and documented

### 5.7 Illustration
Use illustrations sparingly.

Illustrations are allowed for:
- onboarding
- empty states
- success moments
- educational blocks

Illustration rules:
- simple, flat, low-detail
- inclusive and culturally neutral
- never more important than the CTA or money data
- no mascot-heavy personality layer

### 5.8 Motion
Motion should clarify change, not entertain.

Rules:
- Typical duration: 150–300ms
- Respect reduced motion settings
- Use motion for:
  - state transitions
  - bottom-sheet entry/exit
  - loading skeleton fade
  - subtle confirmation feedback
- Avoid celebratory money animations, spinning counters, or casino-like reveals

---

## 6. Copy and tone

### 6.1 Tone
- plain
- competent
- calm
- direct
- non-judgmental
- never hype-driven

### 6.2 Copy rules
Use:
- “Create your salary plan”
- “Next payday”
- “You can change this later”
- “You’re on track”
- “Review this week”

Avoid:
- “Crush your finances”
- “Get rich faster”
- “Don’t miss out”
- “You failed your plan”

### 6.3 Localization and readable-copy rules
All product copy must survive translation, font scaling, and compact Android screens.

Rules:
- Assume translated strings can grow by 30-50%.
- Do not rely on one-line English length when designing components.
- Do not truncate copy that explains a decision, amount, Premium promise, form field, error, or primary action.
- Use ellipsis only for secondary identifiers where truncation is expected, such as long goal names, emails, merchant-like labels, or archive row titles.
- If text is required to understand the action, the component must wrap, stack, resize, or move the text to a larger container.
- Validate new copy in English and Portuguese at minimum, and inspect emulator screenshots or UI trees for clipped labels, hidden CTAs, overlapping text, and bottom-nav coverage.
- Prefer short, translatable labels with plain verbs. Avoid idioms, jokes, and metaphor-heavy phrasing that will not localize cleanly.

### 6.4 Copy-to-component fit
Match the amount of copy to the component's job.

Use:
- **Chip / pill / segmented choice:** short label only, such as “Fine”, “Tight”, “Skipped goal”.
- **Button:** direct command, such as “Save review”, “Apply adjustment”, “Set reminder”.
- **Card:** one title plus one short explanation.
- **Bottom sheet:** decision support, tradeoffs, and confirmation details.
- **Inline selected note:** the full explanation for a selected compact choice.

Avoid:
- putting title + explanatory body inside a compact chip
- using `maxLines = 1` for action labels or financial instructions
- shrinking text below readable Material typography just to make English copy fit
- repeating Premium explanations above the user's primary task

Recommended scalable pattern for translated choices:
1. Show a compact grid of short labels.
2. Let labels wrap if needed.
3. Show the selected option's full explanation below the grid in a full-width wrapping area.
4. Keep the primary task fields and CTA reachable in the first viewport on Pixel 7 when possible.

### 6.5 Error messaging
Errors must explain what happened and what to do next.

Good pattern:
- What happened
- Why, if useful
- Clear recovery action

Example:
- “We couldn’t save this reminder. Check the date and try again.”

---

## 7. Financial data display rules

### 7.1 Hierarchy
The primary hierarchy on financial screens is:
1. **What money event is this screen about?**
2. **What amount matters most right now?**
3. **What action should the user take?**
4. **What status or risk should they know?**

### 7.2 Amount priorities
Use consistent semantics:
- **Primary amount**: salary, available to allocate, goal amount due, or drift total
- **Secondary amount**: category amount, goal progress, weekly spend, etc.
- **Tertiary amount**: helper math, percentages, small comparisons

### 7.3 Formatting rules
- Always use locale-aware currency formatting
- Always show currency symbol or code where ambiguity is possible
- Use localized date formats
- Use localized thousands and decimal separators
- For percentages, show the percent sign explicitly and avoid excessive decimals

### 7.4 Plan vs actual
Show both values clearly:
- Planned amount
- Actual amount
- Difference

Difference must be communicated with:
- signed value if useful
- supporting text
- status color + icon, not color alone

### 7.5 Negative and risk states
Use calm but visible treatment.

Examples:
- “Over plan by ₦4,500”
- “You may run short before payday”
- “Goal is slipping by 2 weeks”

Do not use alarmist language unless the risk is truly severe.

### 7.6 Charts
Charts are optional support, never the only explanation.

Rules:
- Always pair charts with labeled values
- Prefer bars, progress indicators, and simple comparisons over complex finance charts
- Avoid donut-only summaries when exact values matter
- Provide text alternatives for screen readers and small screens

---

## 8. Navigation and layout rules

### 8.1 Compact phones first
Design for compact width first.

### 8.2 Top-level navigation
Use bottom navigation for compact phone layouts with up to 4 items.

Use adaptive navigation patterns for larger windows:
- compact: `NavigationBar`
- medium / expanded: `NavigationRail` or `NavigationSuiteScaffold`

### 8.3 App bars
Use standard Material 3 top app bars.

Patterns:
- top-level screens: small top app bar
- detail screens: top app bar with back navigation
- scrolled state: subtle, not dramatic

### 8.4 Primary CTA placement
- Primary CTA should be visible near the bottom of the layout or in a sticky footer when the task is form-like
- Avoid hiding essential actions in overflow menus
- A user should not need to hunt for “Continue”, “Save”, or “Review”

### 8.5 Bottom sheets vs dialogs vs full screens
Use:
- **Bottom sheet** for focused selections and editable options
- **Dialog** for confirmation or destructive decisions
- **Full screen** for multi-step forms and important review screens

Bottom sheets are preferred for:
- payday selection
- reminder options
- simple category pickers
- premium feature explanation snippets

Dialogs are preferred for:
- delete goal
- reset plan
- discard changes

### 8.6 Lists and forms
- Keep each form step small
- Group related fields clearly
- Use helper text only when it reduces uncertainty
- Do not overload a single screen with all advanced settings

---

## 9. Component inventory

All components must define at least these states where relevant:
- default
- pressed
- focused
- disabled
- loading
- error
- success
- selected
- empty
- locked
- offline

### 9.1 App bar
Use Material 3 top app bar variants.

Rules:
- show contextual title
- use at most one high-priority text action in the app bar
- do not overload with small icon actions

### 9.2 Bottom navigation
Rules:
- max 4 destinations preferred
- always show icon + label
- current destination must be obvious
- labels must remain visible; do not rely on icon memorization

### 9.3 Buttons
Supported variants:
- filled
- filled tonal
- outlined
- text
- destructive
- icon button

Rules:
- one primary filled CTA per screen section
- use outlined or text for secondary actions
- loading buttons must preserve width to avoid layout jump
- disabled buttons must also explain why when practical
- labels must wrap or the layout must stack before truncating decision-critical text
- critical status pairs should stack full-width on phone layouts unless the longest localized values are validated without truncation
- icon-only buttons require accessible labels and should be used only when the icon is familiar or repeatedly established

### 9.3.1 Chips, pills, and quick choices
Use chips and pills for compact status or selection, not explanation.

Rules:
- Short labels only inside the control.
- Do not place explanatory subtext inside compact choice controls.
- Do not use ellipsis for labels that are required to choose correctly.
- If explanation is useful, show it outside the chip in a full-width selected-note or helper area.
- Choices should support two-line labels when translated text requires it.
- Keep hit targets at least 48dp high.
- When a choice writes values into a form, keep the edited fields visible and editable so users can correct the preset.

### 9.4 Text fields
Supported field types:
- salary amount
- fixed cost amount
- allocation amount
- allocation percentage
- payday date
- goal amount
- goal target date
- note / label

Rules:
- use persistent labels, not placeholder-only labeling
- amount fields must open numeric keyboard
- helper text should clarify the expected unit or timing
- error text must say what to fix
- for money input, allow either raw amount or simplified input per locale

### 9.5 Payday selector
This is a first-class component, not an afterthought.

Support:
- monthly date
- weekly day
- biweekly / fortnightly cadence if supported
- salary rhythm selection

The output must be human-readable:
- “You’re usually paid on the 28th”
- “Every Friday”

### 9.6 Allocation row
Core component.

Must support:
- category name
- amount
- optional percentage
- edit affordance
- optional icon
- optional status label

Rules:
- remaining amount must always stay visible somewhere in the screen
- percentage sliders must never be the only control for exact values

### 9.7 Summary cards
Required summary cards:
- Available to allocate
- Next payday plan
- Fixed obligations summary
- Goal progress
- Weekly review summary
- Premium value summary

Rules:
- one main message per card
- avoid more than two actions inside a card
- cards must not become mini dashboards

### 9.8 Goal card
Must show:
- goal name
- target amount
- current progress
- target date
- on-track or at-risk status

### 9.9 Reminder row
Must show:
- reminder type
- date / cadence
- on/off state
- context label if needed

### 9.10 Recurring rule row
Must show:
- trigger cadence
- action summary in plain language
- edit affordance
- premium lock state if unavailable on free tier

### 9.11 Review row
Must show:
- category or plan item
- planned amount
- actual amount
- difference
- status tone

### 9.12 Inline explanation block
Used for trust-sensitive clarification.

Examples:
- “You can change this later.”
- “We use this to build your first plan.”
- “Premium unlocks recurring automation.”

### 9.13 Banners and snackbars
Use banners for important persistent warnings. Use snackbars for brief confirmations.

Banner examples:
- missed payday reminder setup
- offline mode
- plan drift alert

Snackbar examples:
- plan saved
- reminder created
- rule removed

### 9.14 Progress indicators
Use:
- linear progress for goals and onboarding
- circular progress for compact loading only
- skeleton loading for cards and lists

### 9.15 Bottom sheet
Required use cases:
- choose payday cadence
- add reminder
- edit allocation category
- feature info / premium explanation

Rules:
- visible drag handle
- clear dismiss option
- not for destructive confirmation when a dialog is clearer

### 9.16 Dialog
Reserved for:
- destructive action confirmation
- leaving a partially completed flow
- irreversible reset

### 9.17 Empty state
Required empty states:
- no plan yet
- no goals yet
- no reminders yet
- no review data yet
- offline with no cached data

Each empty state must include:
- simple title
- one-sentence explanation
- one primary CTA
- optional light illustration

### 9.18 Offline state
Offline is a first-class state.

Rules:
- explain whether data is still available locally
- avoid scary language
- provide retry only if meaningful

### 9.19 Premium locked state
Premium lock must teach, not tease.

Show:
- feature purpose
- practical benefit
- why it is premium
- one unlock CTA

Do not make free UI look broken or fake-disabled.

---

## 10. Canonical screen patterns

### 10.1 Welcome / pre-signup promise
Goal: explain value before signup.

Must include:
- short promise
- reassurance that bank sync is not required
- clear CTA to begin

### 10.2 Intent selection
Goal: tailor copy and future nudges.

Use single-choice cards for intentions such as:
- save more easily
- invest with discipline
- stop wondering where money went
- plan with a partner

### 10.3 Salary and cadence setup
Goal: collect minimum viable planning data.

Fields:
- net salary / income amount
- frequency
- typical payday

Rules:
- numeric keyboard
- short helper text
- clear reassurance that this can be changed later

### 10.4 Fixed obligations setup
Goal: estimate committed outflow without overwhelming detail.

Rules:
- keep to the essentials first
- allow “I’m not sure” or approximate mode
- do not force a full expense database on day 1

### 10.5 Allocation priorities
Goal: create the “My Share moment”.

Must show live feedback such as:
- “This leaves ₹X per week to spend.”
- “This puts R$Y per payday toward your goal.”

### 10.6 First plan preview
Goal: deliver immediate value while still on free flow.

Must show:
- next salary amount
- split across key buckets
- one concrete next action
- optional projected goal date

### 10.7 Save / signup gate
Signup may appear only after useful preview.

Framing:
- save plan
- turn on reminders
- access deeper timeline

Do not frame signup as mandatory before value.

### 10.8 Deep value view
This is the screen after plan preview and before paywall.

May include:
- goal timeline
- payday rule summary
- one suggested reminder

### 10.9 Paywall
Paywall appears only after value is visible.

Structure:
- page 1: outcome and practical benefit
- page 2: trust and reassurance
- page 3: offer and pricing

Paywall must clearly explain:
- what free includes
- what premium unlocks
- monthly and annual options
- trial details if offered
- cancellation clarity

### 10.10 Dashboard / plan home
The default home after setup.

Must answer:
- what payday or plan is active
- what to do next
- whether the user is on track

Recommended order:
1. next payday plan summary
2. available-to-allocate or key amount
3. current goals progress
4. weekly check-in / review prompt
5. subtle premium or reminder suggestion if relevant

### 10.11 Goals overview
In the current app navigation this content lives under **Strategy**, not a standalone Goals tab.

Must support:
- multiple goals
- sorting by urgency or progress
- clear CTA to add goal

### 10.12 Goal detail
Must show:
- target
- progress
- projected date
- next contribution suggestion
- reminder affordance

### 10.13 Review screen
Must show:
- plan versus actual clearly
- one correction suggestion or next step
- trend or timeline only if useful

### 10.14 Reminders screen
Should live under More, not top-level.

Must support:
- payday reminders
- weekly check-ins
- bill reminders
- goal milestone reminders

### 10.15 Recurring rules screen
Should explain rules in plain language.

Example:
- “Every payday, move ₦15,000 to Emergency Fund.”

### 10.16 Settings / account
Sections:
- profile and subscription
- notification preferences
- language / locale
- currency preferences if needed
- privacy and help

### 10.17 Empty dashboard
When no plan exists, the dashboard becomes a restart surface:
- explain what plan creation does
- one CTA to create plan
- optional example outcome

### 10.18 Error state
Error states must preserve trust.

Use:
- direct explanation
- no blame
- one clear recovery action

---

## 11. Onboarding and paywall flow contract

This flow is the default source of truth for first-session UX.

### Step order
1. Promise screen
2. Intent selection
3. Salary + cadence
4. Fixed obligations
5. Immediate plan preview from the user's own numbers
6. Optional allocation tuning from the plan preview
7. Fixed free plan versus adaptive Premium bridge
8. Paywall earned by the user's plan and next-payday adaptation promise
9. Reminder setup after the plan and Premium choice are clear

Account protection is contextual, not a blocking onboarding step. Ask for it only when it protects a real entitlement or saved state.

### Hard rules
- No bank sync before first plan preview
- No paywall before concrete plan value
- No notification permission before reminder context
- No aggressive tutorial carousel before action
- No deceptive trial or pricing UI

---

## 12. Free, premium, and monetization rules

### 12.1 Business model stance
My Share is **subscription-led** with a useful free tier.

### 12.2 Free tier baseline
Free should provide enough value to build trust.

Recommended free baseline:
- 1 active plan
- 1 active goal
- manual check-in
- first plan creation and preview

### 12.3 Premium meaning
Premium should mean:
- multiple payday rules
- recurring automation
- salary-day reminders
- weekly check-ins and drift alerts
- extra goals
- deeper plan-versus-actual tracking
- richer timelines and planning views

### 12.4 Paywall copy rules
Sell outcomes, not abstract features.

Good examples:
- “Put your salary plan on autopilot”
- “Stay on track with payday reminders”
- “Reach goals with clearer timelines”

### 12.5 Pricing presentation rules
- monthly and annual options should both be clear
- annual may be highlighted as best value
- monthly should remain visible and understandable
- trial language must be explicit and low-anxiety

### 12.6 Ads policy for design
Default assumption: **no ads in core money decision flows**.

Hard rule:
- no third-party ads in onboarding
- no ads on paywall
- no ads on plan, goals, review, salary setup, allocation, or reminder permission screens
- no ads immediately before or after saving a plan, saving a review, buying premium, restoring purchases, or enabling reminders

Allowed free-user placements:
- clearly labeled banner ads on low-sensitivity utility surfaces such as More
- capped interstitials only when leaving a low-sensitivity utility surface such as More, never as a reward gate and never during a financial decision
- app-open ads only after consent, only for returning free users after Home has loaded, and only when they do not interrupt onboarding, checkout, restore, or reminder flows

Even then:
- never near critical CTAs
- never disguised as product UI
- never in premium trial or subscriber experiences
- always respect UMP consent and regional privacy requirements

---

## 13. Accessibility requirements

### 13.1 Touch targets
Minimum 48x48dp for interactive targets.

### 13.2 Contrast
- body text: target 4.5:1 or better
- non-text UI and large text must remain clearly legible

### 13.3 Font scaling
Support large font scaling up to 200 percent without losing core actions.

### 13.4 TalkBack and semantics
- all interactive icons need descriptions
- group related money labels and values meaningfully
- chart alternatives must exist in text
- traversal order must follow reading order

### 13.5 Error prevention
- validate inline where possible
- keep correction suggestions close to the problem
- avoid destructive surprise actions

### 13.6 Motion accessibility
Respect reduced motion settings and avoid essential information that depends only on animation.

---

## 14. Localization and internationalization

### 14.1 Localized formatting
Must support locale-aware:
- currency
- decimal separators
- thousands separators
- date formats
- 12h / 24h times

### 14.2 Copy length tolerance
Design for longer strings in Portuguese, Hindi, Urdu, and other localized contexts.

Minimum acceptance:
- No decision-critical copy is hidden behind ellipsis.
- Compact controls do not carry explanatory body text.
- Long translated labels can wrap, stack, or move into a selected-note pattern.
- Scrollable tab content uses the scaffold inset as the viewport boundary so content does not pass underneath fixed app bars.
- Keyboard-open states keep active money fields reachable on Pixel 7.

### 14.3 Category naming
Prefer simple, culturally portable categories.

Examples:
- Fixed bills
- Food
- Transport
- Savings
- Goals
- Debt

Do not rely on US-centric budgeting terminology.

### 14.4 Currency ambiguity
Where multiple currencies may be possible, make the selected currency obvious during setup and in summaries.

---

## 15. Performance and implementation constraints

### 15.1 Performance stance
Design for modest Android hardware.

Rules:
- avoid heavy raster imagery
- avoid excessive nested surfaces
- keep motion subtle
- avoid overly complex dashboards on first load
- prefer lazy lists and stable item structures

### 15.2 Offline and low-connectivity tolerance
- cached data should still be readable when offline
- actions that need network should explain it clearly
- signup and sync errors must fail gracefully

---

## 16. Analytics-aware UX hooks

The design system must leave room for instrumentation of key product events.

Required Firebase events to support visibly and structurally:
- `onboarding_started`
- `onboarding_step_viewed`
- `onboarding_step_completed`
- `create_plan_completed`
- `onboarding_activation_reached`
- `trajectory_viewed`
- `paywall_viewed`
- `paywall_plan_selected`
- `purchase_started`
- `purchase_completed`
- `purchase_canceled`
- `purchase_failed`
- `onboarding_free_plan_selected`
- `reminder_enabled`
- `reminder_skipped`
- `onboarding_completed`
- `review_completed`
- second-session and Day 7 retention cohorts

The UI should make these moments legible and intentional.

Onboarding conversion events must carry low-cardinality attribution when available:
- `onboarding_paywall_variant`
- `onboarding_experiment`
- `paywall_trial_framing`
- `onboarding_intro_variant`
- `premium_value_frame`
- `price_cluster`
- `billing_plan` for paywall and purchase events
- `time_to_first_value_ms` on `onboarding_activation_reached`

---

## 17. Jetpack Compose mapping notes

### 17.1 Theme
Implement with:
- `MaterialTheme`
- custom `colorScheme`
- custom `Typography`
- custom `Shapes`
- optional semantic extensions through CompositionLocal or extension properties

### 17.2 Scaffolds and navigation
Use:
- `Scaffold` for top-level screens
- `NavigationBar` for compact screens
- `NavigationRail` or `NavigationSuiteScaffold` for larger windows
- `NavHost` for navigation structure

### 17.3 Components
Prefer standard Material 3 components before custom work:
- `TopAppBar`
- `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton`
- `Card`, `ElevatedCard`
- `TextField`, `OutlinedTextField`
- `Switch`
- `FilterChip` / `InputChip` where appropriate
- `LinearProgressIndicator`, `CircularProgressIndicator`
- `Snackbar`
- `AlertDialog`
- `ModalBottomSheet`
- date and time picker patterns where appropriate

### 17.4 Adaptive behavior
Use `WindowSizeClass` for navigation changes, not for changing the core design language.

Rules:
- compact screens remain the reference layout
- larger layouts may add rail or secondary pane behavior
- do not redesign the entire visual hierarchy per size class

### 17.5 Custom components that are acceptable
Custom components are allowed for:
- allocation row
- plan summary card
- goal timeline card
- plan-vs-actual review row
- premium lock card

Even custom components must still inherit My Share tokens and M3 interaction patterns.

---

## 18. Do / don’t

### Do
- Use Material 3 as the base
- Show value before monetization asks
- Keep the plan and next action visible
- Use plain, trustworthy copy
- Support manual-first setup without pressure
- Keep premium helpful and clearly bounded
- Preserve clarity on low-end Android devices

### Don’t
- Build a generic investment dashboard aesthetic
- Lead with bank sync or invasive permissions
- Ask for subscription before the user has seen a useful plan
- Use trading-style visuals, hype copy, or alarmist alerts
- Hide costs, trial conditions, or cancellation logic
- Put ads on money-sensitive screens or around financial decision CTAs
- Create custom UI that breaks Android familiarity without a strong reason

---

## 19. Canonical source-of-truth statement

When there is a conflict between a visually interesting option and a clear, trustworthy, Android-friendly option, choose the clear and trustworthy option.

When there is a conflict between feature breadth and first-session usefulness, choose first-session usefulness.

When there is a conflict between aggressive monetization and user trust, choose user trust.

That is the My Share design system.
