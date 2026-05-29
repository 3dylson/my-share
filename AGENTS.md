# Agent Instructions for My Share

This document serves as the foundational instruction set for any AI agents or developers contributing to the "My Share" Android project. 

When working on this project, adhere strictly to the following guidelines and principles to ensure high code quality, maintainability, and alignment with modern Android best practices.

## Core Architectural Principles

### 1. Clean Architecture
- **Separation of Concerns:** The project strictly follows Clean Architecture. Maintain clear boundaries between the **Presentation** (UI, ViewModels), **Domain** (Use Cases, Entities, Repository Interfaces), and **Data** (Repository Implementations, Remote/Local Data Sources).
- **Dependency Rule:** Source code dependencies must only point inward toward the Domain layer. The Domain layer should have absolutely no dependencies on the Data or Presentation layers (no Android framework classes in the domain layer).

### 2. SOLID Principles
- **S**ingle Responsibility Principle: Every class, module, and function should have one, and only one, reason to change.
- **O**pen/Closed Principle: Software entities should be open for extension but closed for modification.
- **L**iskov Substitution Principle: Objects should be replaceable with instances of their subtypes without altering the correctness of the program.
- **I**nterface Segregation Principle: Many client-specific interfaces are better than one general-purpose interface.
- **D**ependency Inversion Principle: Depend upon abstractions, not concretions. Use Hilt for all dependency injection.

## Development Methodology

### 3. Test-Driven Development (TDD)
- Approach all new feature development or bug fixes using TDD.
- Write a failing test first, write the minimal code required to pass the test, and then refactor while keeping the tests green.
- Ensure thorough unit testing for Domain Use Cases and ViewModels using MockK and Coroutine Test rules.

## Code Organization & Structure

### 4. File Responsibility & Naming
- **Single Responsibility per File:** Every file must have a single, clearly defined responsibility.
- **Meaningful Names:** Files must be named strictly after their core responsibility (e.g., `PlayBillingEntitlementRepository.kt`, not `BillingStuff.kt`).
- **Cohesion:** Place only closely related declarations together. Do not mix unrelated code within the same file. If a class or file starts handling multiple concerns, extract them into separate files to keep readability high and the project easy to navigate.

## Coding Standards

### 5. Comments & Documentation
- **Self-Documenting Code:** Code should explain *what* it does. Use clear, descriptive variable and function names.
- **Comments:** Only add comments for **non-obvious code**, explaining *why* a decision was made (e.g., a workaround for an OS bug, or complex business logic). Avoid redundant comments that merely repeat the code.

### 6. Logging & Troubleshooting
- **Critical Debug Logs:** Always include critical debug logs (`Timber.d()`, `Timber.e()`) at major state changes, repository data fetches, or error catches to support troubleshooting.
- **Timber Integration:** Use `Timber` for all logging. The app is configured with `CrashlyticsTree` to pipe errors and warnings to Firebase Crashlytics. Use `Timber.e()` for non-fatal exceptions and `Timber.d()` for general flow tracking.
- **Configuration and Analytics Alignment:** When changing user-facing flows, paywall/onboarding copy, Premium behavior, billing logic, retention flows, or feature gates, update and validate the matching Analytics events, Crashlytics keys, Remote Config defaults, Firebase/Play Console configuration, and any live Remote Config template values. Do not rely on app-side compatibility mappings to hide stale backend configuration; keep real configuration data aligned so testing reflects production behavior.

## Android Best Practices & Tech Stack
- **UI:** Exclusively use Jetpack Compose for all UI development. Maintain unidirectional data flow (UDF) using state hoisting.
- **Concurrency:** Use Kotlin Coroutines and Flows for all asynchronous operations.
- **Dependency Injection:** Use Dagger Hilt.
- **Security:** Do not bypass security systems (e.g., App Check, Firebase rules) for convenience. Validate all critical state (like subscription status) via backend Cloud Functions.

## Product Experience Bar
- During validation, fix issues as soon as they are found, then resume the test from the interrupted flow.
- The app must be understandable for users with no financial literacy. Use plain, outcome-first copy and avoid technical wording.
- Premium should feel necessary because it clearly improves the user's payday decisions, not because the free path feels punished.
- Onboarding is the primary conversion moment. Protect the aha moment: show a concrete plan from the user's own numbers before asking for Premium.
- Use tasteful upsell, retention, gamification, and small relevant animations where they make the experience clearer or more motivating.
- Preserve the premium look and feel across screen sizes. Always consider scalability, screen affordance, scrolling, keyboard behavior, and compact phones.
- Keep the app consistent, avoid repeated information on the same screen, and make every Premium surface answer what Premium will do next for this user's plan.

## UI, Copy, and Localization Standards
- Design for the task first. On task screens, the user's primary action must appear before explanatory Premium, account, or status content unless a blocking state requires otherwise.
- Treat every visible string as translated text that may grow by 30-50%. Components must adapt vertically or change layout rather than clip, overlap, or hide meaning.
- Do not put explanatory copy inside compact chips, pills, or small buttons. Use short decision labels in compact controls, then show the selected explanation in a full-width wrapping area when extra context is needed.
- Avoid `maxLines = 1` and `TextOverflow.Ellipsis` on user-facing action labels, financial instructions, paywall claims, form labels, or anything needed to make a decision. Ellipsis is acceptable only for inherently user-generated or secondary identifiers, such as long goal names, emails, or archive row titles, and only when the full value is available elsewhere.
- Scaffold insets must constrain the scroll viewport, not only list content padding. Tab content must not scroll under app bars or bottom bars because partial clipping makes headings look broken.
- Do not place two critical status values side by side unless they stay readable at Pixel 7 width, large font scale, and the longest supported locale. Prefer full-width stacked rows for Premium status, review status, next action, and reminder timing.
- Match copy weight to component size: buttons and chips use short commands or choices; cards can carry title plus supporting text; bottom sheets can explain decisions in more detail.
- Prefer concrete, outcome-first copy tied to the user's plan: what to move, what to keep, what changed, and what Premium will do next. Avoid generic feature language like "insights", "automation", or "unlock value" unless the next action is explicit.
- Write for tired users who may be checking the app quickly. Use plain verbs, low reading load, and non-judgmental recovery language.
- Validate new or changed UI copy in at least English and Portuguese on the Pixel 7 emulator. For compact controls, also inspect the UI tree or screenshots for clipping, ellipsis, overlap, keyboard coverage, and bottom-navigation obstruction.
- When changing copy-driven flows, update matching analytics event names/parameters and verify Firebase Analytics naming rules. Do not use reserved prefixes such as `google_`.
