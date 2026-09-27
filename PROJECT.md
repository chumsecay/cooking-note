# Project: Cooking Note — Architecture Modernization & Backend v2 Skeleton

## Architecture
- **Offline-First Clean Architecture**: Room database (`AppDatabase`, `cookingnote.db`) is the Single Source of Truth (SSOT).
- **Remote / Sync Skeleton**: Package `com.cookingnote.app.data.remote` containing:
  - `dto/`: Moshi DTO models for Recipe, Pantry, CookLog, Ingredients, Steps, Tags.
  - `api/`: `RecipeApiService` Retrofit interface stub.
  - `datasource/`: `RemoteDataSource` interface & `DefaultRemoteDataSource` stub implementation.
  - `model/`: `SyncStatus` (Synced, Syncing, Offline, Error) tracking model.
- **Repository Layer**: `CookbookRepository` accepts `RemoteDataSource` with default argument for backward compatibility; exposes `observeSyncStatus(): Flow<SyncStatus>`.
- **Dependency Injection**: Manual DI via `AppContainer` (`LocalAppContainer`) and `AppViewModelFactory`. Zero external DI frameworks.
- **Presentation Layer**: Unidirectional Data Flow (UDF) in Jetpack Compose:
  - Exactly 10 screen files in `ui/screens/`.
  - Stateless `*Content` composables taking pure `uiState: *UiState` as the first argument, followed by event callbacks.
  - Lifecycle-aware flow collection via `collectAsStateWithLifecycle()`.
  - Reusable components (e.g. `SyncStatusBadge`, modern `RecipeCard`) placed in `ui/components/`.
  - ViewModels incorporate `observeSyncStatus()` via `.onStart { emit(SyncStatus.Synced) }` into `*UiState`.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Network DTO Models | Moshi-annotated data classes for Recipe, Ingredient, Step, Pantry, CookLog, Tag remote payloads | M1 | Survey (Explorer 1) |
| 2 | RecipeApiService Stub | Retrofit service interface defining API endpoints for v2 sync | M1 | Survey (Explorer 1) |
| 3 | RemoteDataSource & Impl | Abstraction and default stub implementation for fetching/pushing remote data | M1 | Survey (Explorer 1) |
| 4 | SyncStatus Modeling | Domain model `SyncStatus` (Synced, Syncing, Offline, Error) | M1 | Survey (Explorer 1) |
| 5 | Repository Sync Integration | `CookbookRepository` constructor accepts `RemoteDataSource = DefaultRemoteDataSource()`, exposes `observeSyncStatus(): Flow<SyncStatus>` | M1 | Survey (Explorer 1) |
| 6 | AppContainer Wiring | Wire `DefaultRemoteDataSource` into `AppContainer` and `CookbookRepository` | M1 | Survey (Explorer 1) |
| 7 | Data Remote Unit Tests | Unit tests for DTO serialization, `RemoteDataSource`, and `CookbookRepository` sync status flow | M1 | Survey (Explorer 1 & 3) |
| 8 | SyncStatusBadge Component | Polished, non-intrusive badge in `ui/components/SyncStatusBadge.kt` showing Synced, Syncing, Offline, Error | M2 | Survey (Explorer 2) |
| 9 | RecipeCard Modernization | Elevated card styling (RoundedCornerShape 16dp), subtle outline border, refined typography | M2 | Survey (Explorer 2) |
| 10 | Core Screen Modernization | Material 3 card styling, elevation, typography, and sync badge in TopAppBar across Home, Library, Detail, Pantry | M2 | Survey (Explorer 2) |
| 11 | ViewModel Sync Integration | Combine `observeSyncStatus()` with `.onStart { emit(SyncStatus.Synced) }` in ViewModels into `*UiState` | M2 | Survey (Explorer 2) |
| 12 | Conformance & Adversarial Integrity | Ensure 10 screen files, AST/regex conformance (`*Content(uiState, ...)`, `AlertDialog`, etc.), 100% test pass | M3 | Survey (Explorer 3) |
| 13 | Forensic Audit & Build Verification | Independent review, adversarial stress test, forensic audit verification, and assembleDebug build | M3 | Project Pattern |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Data & Remote Architecture Skeleton | `com.cookingnote.app.data.remote` (DTOs, ApiService, RemoteDataSource, SyncStatus), `CookbookRepository` & `AppContainer` wiring, remote unit tests | none | DONE |
| 2 | Presentation Layer Polish & Sync Indicator | `ui/components/SyncStatusBadge.kt`, `RecipeCard.kt`, ViewModel/UiState sync status, M3 styling on Home, Library, Detail, Pantry | M1 | DONE |
| 3 | Verification, Conformance Hardening & Audit | Run complete unit test suite (100% pass), check Milestone3 conformance & adversarial tests, run Challenger, Reviewer, and Forensic Auditor | M2 | DONE |

## Interface Contracts
### Data Layer ↔ Presentation Layer
- `SyncStatus`:
  ```kotlin
  package com.cookingnote.app.data.model // or com.cookingnote.app.data.remote.model

  enum class SyncStatus {
      Synced,
      Syncing,
      Offline,
      Error
  }
  ```
- `CookbookRepository`:
  ```kotlin
  fun observeSyncStatus(): Flow<SyncStatus>
  ```
- `RemoteDataSource`:
  ```kotlin
  interface RemoteDataSource {
      suspend fun fetchRecipes(): Result<List<RecipeDto>>
      suspend fun pushRecipe(dto: RecipeDto): Result<Unit>
      suspend fun fetchPantryItems(): Result<List<PantryItemDto>>
      suspend fun fetchCookLogs(): Result<List<CookLogDto>>
      fun observeSyncStatus(): Flow<SyncStatus>
  }
  ```
- `UiState` Models (`HomeUiState`, `LibraryUiState`, `DetailUiState`, `PantryUiState`):
  - Contain `val syncStatus: SyncStatus = SyncStatus.Synced` so tests without sync mock still construct and pass cleanly.

## Code Layout
```
app/src/main/java/com/cookingnote/app/
├── data/
│   ├── remote/
│   │   ├── api/RecipeApiService.kt
│   │   ├── datasource/RemoteDataSource.kt, DefaultRemoteDataSource.kt
│   │   ├── dto/RecipeDto.kt, PantryDto.kt, CookLogDto.kt
│   │   └── model/SyncStatus.kt
│   ├── repository/CookbookRepository.kt
│   └── AppContainer.kt
├── ui/
│   ├── components/
│   │   ├── SyncStatusBadge.kt
│   │   └── RecipeCard.kt
│   ├── screens/ [EXACTLY 10 FILES, NEVER ADD MORE]
│   │   ├── HomeScreen.kt
│   │   ├── LibraryScreen.kt
│   │   ├── DetailScreen.kt
│   │   ├── PantryScreen.kt
│   │   └── ... (6 others unchanged)
│   ├── viewmodel/
│   │   ├── HomeViewModel.kt
│   │   ├── LibraryViewModel.kt
│   │   ├── DetailViewModel.kt
│   │   ├── PantryViewModel.kt
│   │   └── ...
│   └── CookingNoteRoot.kt
app/src/test/java/com/cookingnote/app/
├── data/remote/RemoteDataSourceTest.kt
├── ui/viewmodel/...
└── Milestone3*
```
