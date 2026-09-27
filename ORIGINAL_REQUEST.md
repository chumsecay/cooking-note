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

## 2026-09-23T03:18:36Z

<USER_REQUEST>
Refactor the Cooking Note Android application frontend to establish a unified architectural skeleton (Material 3 UI modernization, network/sync state indicators, network DTO and RemoteDataSource stubs) to prepare seamlessly for backend v2 integration, while strictly maintaining 100% test pass rate, architecture conformance constraints, and presentation readiness.

Working directory: c:/Users/ADMIN/Documents/cooking-note
Integrity mode: development
Requested team: Nhóm đầy đủ (Full multi-agent team): Phân chia song song giữa tầng kiến trúc khung sườn (Network/Sync skeleton), tầng UI polish (Compose screens) và tầng tài liệu/kiểm thử.

## Requirements

### R1. Unified Architecture Skeleton & Network Stub Layer
Establish clean data source abstractions for backend v2 integration in the data layer:
- Define `RemoteDataSource` interface and Retrofit API service stub (`RecipeApiService`) with Moshi DTO models representing recipes, pantry items, and cook logs.
- Add sync tracking models and repository abstractions (`SyncStatus`: Synced, Syncing, Offline, Error) that keep Room database as the offline-first Single Source of Truth.
- Wire dependencies into `AppContainer` via manual dependency injection without introducing external DI frameworks (no Hilt/Koin).

### R2. UI Modernization & Sync Status Indicator
Refine the presentation layer across core screens (Home, Library, Detail, Pantry):
- Modernize Compose UI components with polished Material 3 card styling, subtle elevation, and clear typography hierarchy.
- Add a persistent or non-intrusive network/sync status badge/bar (Offline / Online / Đã đồng bộ) to give immediate visual feedback of connectivity and data freshness.
- Retain clear visual structure that is intuitive for both daily use and project presentation/documentation.

### R3. Preservation of Architectural Conformance & 100% Test Passing
Strictly respect all established architectural rules and constraints specified in `AGENTS.md`:
- Exactly 10 screen files in `ui/screens/` matching `Milestone3ArchitectureConformanceTest`.
- All composable screens must maintain stateless `*Content` composables taking pure `uiState` as first parameter followed by callbacks.
- All state collection in screens must use `collectAsStateWithLifecycle()`.
- No direct Room DAO or Repository access from Compose screens.
- Keep `CookingNoteRoot` manual DI wiring via `AppViewModelFactory`.
- Zero compilation errors and 100% passing tests for both unit tests and adversarial architectural conformance tests.

## Verification Resources
- Test command: `.\gradlew.bat :app:testDebugUnitTest` (or `./gradlew :app:testDebugUnitTest`)
- Conformance test: `app/src/test/java/com/cookingnote/app/Milestone3ArchitectureConformanceTest.kt`
- Adversarial test: `app/src/test/java/com/cookingnote/app/Milestone3ChallengerAdversarialTest.kt`
- Build command: `.\gradlew.bat :app:assembleDebug`

## Acceptance Criteria

### Architecture & Network Skeleton
- [ ] `com.cookingnote.app.data.remote` package contains DTOs, API service interface, and `RemoteDataSource` abstraction.
- [ ] Sync state is exposed through `CookbookRepository` and observable by ViewModels.
- [ ] Room remains the offline-first local cache / Single Source of Truth; app functions completely with or without remote network connectivity.

### UI & Presentation Polish
- [ ] Core screens (Home, Library, Detail, Pantry) render updated Material 3 styling and sync status indicators.
- [ ] No regression in navigation, recipe creation/editing, or AI assistant interaction.

### Quality, Conformance & Build
- [ ] `./gradlew.bat :app:testDebugUnitTest` runs with 100% pass rate (0 failures, 0 errors).
- [ ] `Milestone3ArchitectureConformanceTest` and `Milestone3ChallengerAdversarialTest` pass without modification to test rules.
- [ ] `./gradlew.bat :app:assembleDebug` produces a valid debug APK with 0 errors.

</USER_REQUEST>
<ADDITIONAL_METADATA>
The current local time is: 2026-09-23T10:18:36+07:00.
</ADDITIONAL_METADATA>
