# Original User Request

## 2026-09-09T01:20:48Z

<USER_REQUEST>
Refactor the Cooking Note Android application's architecture to decouple presentation logic from the data layer using ViewModels and UI state holders, optimize Jetpack Compose performance and lifecycle safety, and establish a robust automated unit test suite.

Working directory: c:/Users/ADMIN/Documents/cooking-note
Integrity mode: development

## Requirements

### R1. Presentation Layer Decoupling
Separate presentation logic and UI state management from Jetpack Compose UI components across the application. Composable screens must receive state and dispatch user events through dedicated ViewModels/state holders rather than directly querying or mutating Room repositories and containers.

### R2. Compose Lifecycle and Performance Optimization
Ensure UI state flows are collected in a lifecycle-aware manner so that flow collection pauses when screens are not in the foreground. Avoid unnecessary recompositions and stabilize state representations to deliver smooth rendering.

### R3. Comprehensive Automated Unit Test Suite
Establish automated unit tests covering core business workflows, including repository data operations, ViewModel state transitions, and offline rule-based AI fallback logic. Tests must run deterministically in the local JVM test environment without requiring an attached Android device or emulator.

### R4. Build and Integration Integrity
The complete refactored codebase must compile cleanly with the existing Gradle build setup (`./gradlew :app:assembleDebug`) and all unit tests must execute and pass via `./gradlew :app:testDebugUnitTest`.

## Acceptance Criteria

### Architecture & Separation of Concerns
- [ ] Composable screens (Home, Library, Detail, Pantry, Favorites, History, AI Chat, Search, Settings) do not directly call Room DAO or repository mutation/query methods.
- [ ] Screen states (data loading, content, empty, error states) are explicitly modeled and exposed via observable state flows from ViewModels.

### Lifecycle & Performance
- [ ] State flows in Compose UI are collected using lifecycle-safe mechanisms (e.g. `collectAsStateWithLifecycle`) across all screen destinations.
- [ ] UI event handling (navigation triggers, user input, button clicks) follows unidirectional data flow (state flows down, events flow up).

### Automated Testing & Verification
- [ ] Unit test dependencies (e.g., JUnit, Kotlinx Coroutines Test, Turbine/mocking utilities as appropriate) are configured in `app/build.gradle.kts`.
- [ ] Automated unit test suites are implemented for ViewModels, Repository logic, and AI fallback resolution.
- [ ] Executing `./gradlew :app:testDebugUnitTest` (or `./gradlew.bat :app:testDebugUnitTest`) completes successfully with 100% passing tests.
- [ ] Executing `./gradlew :app:assembleDebug` (or `./gradlew.bat :app:assembleDebug`) produces a valid debug APK with zero compilation errors.

</USER_REQUEST>
<ADDITIONAL_METADATA>
The current local time is: 2026-09-09T08:20:48+07:00.
</ADDITIONAL_METADATA>
