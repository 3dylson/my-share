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
