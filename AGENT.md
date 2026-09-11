# Cooking Note — Hướng dẫn Kỹ thuật cho Agent & Nhà phát triển (AGENTS.md)

Tài liệu này cung cấp hướng dẫn toàn diện về kiến trúc, quy ước viết mã, hạ tầng kiểm thử và quy trình làm việc cho các AI Agent và lập trình viên khi tiếp quản, bảo trì hoặc mở rộng dự án **Cooking Note**.

---

## 1. Tổng quan Dự án (Project Overview)

- **Tên dự án**: Sổ tay Công thức Nấu ăn (Cooking Note)
- **Nền tảng**: Android (Offline-first)
- **Công nghệ chính**:
  - **UI**: Jetpack Compose · Material 3
  - **Kiến trúc**: MVVM + Unidirectional Data Flow (UDF) + Flow
  - **Điều hướng**: Navigation Compose với sealed class `Route`
  - **Persistence**: Room 2.6.1 (KSP) + DataStore Preferences + EncryptedSharedPreferences
  - **Network**: Retrofit + Moshi + OkHttp Logging
  - **AI Subsystem**: OpenAI Chat, OpenAI Responses, Anthropic Messages, Google Gemini, và Rule-based offline fallback
  - **Image Loading**: Coil Compose 2.x
- **Cấu hình SDK**:
  - `minSdk = 26`
  - `compileSdk = 35`
  - `targetSdk = 35`
  - Kotlin 2.0.21 / JVM target 17 / Gradle 9.3.0
- **Phiên bản thư viện chính** (khai báo tập trung tại `gradle/libs.versions.toml` — luôn thêm/nâng cấp dependency qua Version Catalog, không hard-code phiên bản trong `app/build.gradle.kts`):
  - AGP 8.7.3 · KSP 2.0.21-1.0.27 · Compose BOM 2024.12.01 · Material 3 (icons-extended)
  - Room 2.6.1 · Lifecycle 2.8.7 · Navigation Compose 2.8.4 · Coroutines 1.9.0
  - Retrofit 2.11.0 · Moshi 1.15.1 · OkHttp Logging 4.12.0 · Coil 2.7.0
  - CameraX 1.4.0 (**đã khai báo nhưng chưa được sử dụng trong mã nguồn**)
  - DataStore Preferences 1.1.1 · security-crypto 1.1.0-alpha06
  - Kiểm thử: JUnit 4.13.2 · kotlinx-coroutines-test · Turbine 1.2.0 · MockK 1.13.13

---

## 2. Kiến trúc Ứng dụng (Architecture Blueprint)

Ứng dụng tuân thủ nghiêm ngặt nguyên lý **Unidirectional Data Flow (UDF)** và phân tách rõ ràng giữa Presentation Layer và Data Layer:

```
┌─────────────────────────────────────────────────────────────┐
│                     Composable Screens                      │
│     (Stateless UI + Stateful Wrappers, nhận State & Event)   │
└───────────────────────────▲─────────────────────────────────┘
                            │ (Events up: callbacks)
                            │ (State down: collectAsStateWithLifecycle)
┌───────────────────────────▼─────────────────────────────────┐
│                ViewModels & UI State Holders                │
│ (Home, Library, Detail, Pantry, Favorites, History, ...)    │
└───────────────────────────▲─────────────────────────────────┘
                            │ (StateFlow / Kotlin Coroutines)
┌───────────────────────────▼─────────────────────────────────┐
│              AppContainer & CookbookRepository              │
│       (Single Source of Truth, Manual Dependency Injection) │
└───────────────────────────▲─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│             Room Database (9 DAOs) & RuleBasedAi            │
└─────────────────────────────────────────────────────────────┘
```

### Nguyên tắc Bắt buộc:
1. **Không gọi DAO/Repository trực tiếp trong Composable**: Mọi tương tác dữ liệu, truy vấn Room hoặc gọi AI phải đi qua `ViewModel`.
2. **Thu thập State an toàn theo vòng đời**: Luôn sử dụng `collectAsStateWithLifecycle()` từ `androidx.lifecycle.compose` thay vì `collectAsState()` thông thường để tránh rò rỉ tài nguyên khi màn hình vào nền (background).
3. **Mô hình hóa State tường minh**: Mọi màn hình phải có sealed interface hoặc data class đại diện cho UI state (ví dụ: `Loading`, `Content`, `Empty`, `Error`).
4. **Dependency Injection**: Sử dụng `AppViewModelFactory` kết hợp với `AppContainer` cung cấp qua `LocalAppContainer`.

> ⚠️ Các nguyên tắc 1–4 ở trên được **kiểm tra tự động bằng source-parsing tests** (`Milestone3ArchitectureConformanceTest`, `Milestone3ChallengerAdversarialTest`). Đổi tên screen/route/ViewModel hoặc vi phạm UDF sẽ làm các test này fail — xem Mục 4.

### Chi tiết Room Database:
- **Tên file DB**: `cookingnote.db` (hằng số `AppDatabase.DB_NAME`).
- **Version hiện tại**: `2`, `exportSchema = true` → schema JSON được KSP xuất ra `app/schemas/com.cookingnote.app.data.database.AppDatabase/{1,2}.json` (cấu hình qua `ksp { arg("room.schemaLocation", "$projectDir/schemas") }` trong `app/build.gradle.kts`). **Thư mục `app/schemas/` phải được commit vào git.**
- **Migration**: `AppContainer` đang dùng `fallbackToDestructiveMigration()` — khi tăng version DB, dữ liệu cũ sẽ bị xóa và DB được tạo lại. Nếu sau này cần giữ dữ liệu người dùng, phải viết `Migration` tường minh thay thế.
- **Seed dữ liệu mẫu**: Khi DB được tạo lần đầu, callback `RoomDatabase.Callback.onCreate` gọi `SeedData.populate(...)` (`data/seed/SeedData.kt`) để chèn danh mục, công thức, nguyên liệu, pantry mẫu.
- **Quy mô**: 10 entities (`RecipeEntity`, `IngredientEntity`, `StepEntity`, `CategoryEntity`, `PantryItemEntity`, `CookHistoryEntity`, `TagEntity`, `RecipeTagCrossRef`, `AiQueryLogEntity`, `ChatMessageEntity`) và 9 DAOs trong `data/dao/Daos.kt` (`RecipeDao`, `IngredientDao`, `StepDao`, `CategoryDao`, `PantryDao`, `HistoryDao`, `TagDao`, `AiLogDao`, `ChatMessageDao`).
- **TypeConverters**: `data/util/Converters.kt`, đăng ký qua `@TypeConverters(Converters::class)` trên `AppDatabase`.
- **Điều hướng**: `Route` có 11 đích — 9 đích tĩnh (`Home`, `Library`, `Pantry`, `Ai`, `Settings`, `Favorites`, `History`, `Create`, `Search`) và 2 đích tham số hóa `Detail`/`Edit` dạng `detail/{id}`, `edit/{id}` (dùng `Route.Detail.of(id)` / `Route.Edit.of(id)` để điều hướng).

---

## 3. Cấu trúc Thư mục Mã nguồn (Project Layout)

```text
app/schemas/                           # Room schema JSON xuất bởi KSP (1.json, 2.json) — commit vào git
app/src/main/java/com/cookingnote/app/
├── CookingNoteApp.kt              # Application class, khởi tạo AppContainer
├── MainActivity.kt                # ComponentActivity, thiết lập Theme và CookingNoteRoot
├── ai/
│   ├── AiProvider.kt              # Interface AiService & data class AiSuggestion
│   ├── DefaultAiService.kt        # Triển khai gọi OpenAI/Anthropic/Gemini (dùng java.util.Base64)
│   └── RuleBasedAi.kt             # Thuật toán gợi ý ngoại tuyến theo từ khóa & tủ lạnh
├── data/
│   ├── AppContainer.kt            # DI Container cấp phát Repository, Database, Prefs
│   ├── dao/
│   │   └── Daos.kt                # 9 Room DAOs (Recipe, Ingredient, Step, Pantry, ...)
│   ├── database/
│   │   └── RoomDatabase.kt        # AppDatabase Room definition (version 2, exportSchema = true)
│   ├── entity/
│   │   ├── Entities.kt            # Room entities (RecipeEntity, IngredientEntity, ...)
│   │   ├── ChatMessageEntity.kt   # Entity lưu lịch sử chat AI
│   │   └── Relations.kt           # Quan hệ 1-N, N-N (RecipeWithDetails, ...)
│   ├── prefs/
│   │   ├── AiSettingsStore.kt     # EncryptedSharedPreferences lưu API Keys an toàn
│   │   └── UserPrefsStore.kt      # DataStore Preferences cho tùy chọn người dùng
│   ├── repository/
│   │   └── CookbookRepository.kt  # Repository trung tâm điều phối toàn bộ dữ liệu
│   ├── seed/
│   │   └── SeedData.kt            # Dữ liệu mẫu (danh mục, công thức, pantry) nạp khi tạo DB lần đầu
│   └── util/
│       └── Converters.kt          # Room TypeConverters
└── ui/
    ├── CookingNoteRoot.kt         # NavHost điều hướng 11 đích (9 tĩnh + Detail/Edit theo {id})
    ├── Routes.kt                  # Sealed class Route định nghĩa đường dẫn điều hướng
    ├── components/
    │   └── RecipeCard.kt          # Card công thức dùng chung giữa các màn hình
    ├── local/
    │   └── LocalAppContainer.kt   # CompositionLocal cung cấp AppContainer cho cây Compose
    ├── theme/
    │   ├── Color.kt               # Bảng màu ứng dụng
    │   └── Theme.kt               # Material 3 Theme (light/dark tùy chỉnh, bảng màu cam-kem, không dùng dynamic color)
    ├── screens/                   # 10 Composable Screens chính
    │   ├── HomeScreen.kt          # Trang chủ & gợi ý hôm nay
    │   ├── LibraryScreen.kt       # Thư viện công thức & lọc danh mục
    │   ├── DetailScreen.kt        # Chi tiết món, đánh dấu đã nấu, yêu thích
    │   ├── CreateRecipeScreen.kt  # Thêm/sửa công thức
    │   ├── PantryScreen.kt        # Tủ lạnh & cảnh báo sắp hết nguyên liệu
    │   ├── FavoritesScreen.kt     # Danh sách món yêu thích
    │   ├── HistoryScreen.kt       # Lịch sử nấu nướng
    │   ├── SearchScreen.kt        # Tìm kiếm tức thì với 300ms debounce
    │   ├── SettingsScreen.kt      # Cài đặt AI & sao lưu SQLite nền
    │   └── AiScreen.kt            # Trợ lý AI chat
    └── viewmodel/                 # 10 ViewModels & AppViewModelFactory
        ├── AppViewModelFactory.kt # Factory khởi tạo ViewModels với DI
        ├── HomeViewModel.kt
        ├── LibraryViewModel.kt
        ├── DetailViewModel.kt
        ├── PantryViewModel.kt
        ├── FavoritesViewModel.kt
        ├── HistoryViewModel.kt
        ├── SearchViewModel.kt
        ├── SettingsViewModel.kt
        ├── AiViewModel.kt
        └── CreateRecipeViewModel.kt
```

Tài nguyên Android đáng chú ý trong `app/src/main/res/`:
- `xml/file_paths.xml` — cấu hình đường dẫn cho `FileProvider` (cache + files), dùng khi chia sẻ file backup DB qua `ACTION_SEND`.
- `values/strings.xml`, `values/themes.xml` — chuỗi và theme gốc (`Theme.CookingNote`) khai báo trong Manifest.

---

## 4. Hạ tầng Kiểm thử (Testing Infrastructure)

Dự án sở hữu bộ kiểm thử đơn vị **100% độc lập trên JVM** (không yêu cầu thiết bị vật lý hoặc máy ảo Android khi chạy `./gradlew test`), tổng cộng khoảng **100 test methods**:

```text
app/src/test/java/com/cookingnote/app/
├── testutil/
│   └── MainDispatcherRule.kt          # JUnit4 TestWatcher Rule thiết lập Dispatchers.Main
├── Milestone1AdversarialVerificationTest.kt   # Kiểm thử đối kháng tầng dữ liệu (Repository/DAO/prefs)
├── Milestone2AdversarialVerificationTest.kt   # Kiểm thử đối kháng tầng ViewModel & AI
├── Milestone2EdgeCasesChallengerTest.kt       # Thách thức các ca biên (empty, race, lỗi)
├── Milestone3ArchitectureConformanceTest.kt   # ⚠️ Parse mã nguồn: bắt buộc screens tuân thủ UDF
├── Milestone3ChallengerAdversarialTest.kt     # ⚠️ Parse mã nguồn: verify wiring Route/Factory/UI state
├── data/
│   └── CookbookRepositoryTest.kt      # Kiểm thử nghiệp vụ CRUD, transaction, tủ lạnh, chat
├── ai/
│   ├── Base64EmpiricalTest.kt         # Kiểm thử tương thích JVM Base64
│   └── RuleBasedAiTest.kt             # Kiểm thử logic gợi ý offline đa tầng
└── ui/viewmodel/
    ├── AppViewModelFactoryTest.kt     # Kiểm thử khởi tạo toàn bộ 10 ViewModels
    ├── HomeViewModelTest.kt
    ├── LibraryViewModelTest.kt
    ├── DetailViewModelTest.kt
    ├── PantryViewModelTest.kt
    ├── FavoritesViewModelTest.kt
    ├── HistoryViewModelTest.kt
    ├── SearchViewModelTest.kt
    ├── SettingsViewModelTest.kt
    ├── AiViewModelTest.kt
    └── CreateRecipeViewModelTest.kt
```

### Quy tắc Viết Kiểm thử:
1. **Bắt buộc dùng `MainDispatcherRule`**: Khi kiểm thử bất kỳ `ViewModel` hoặc Coroutine sử dụng `viewModelScope`, luôn thêm `@get:Rule val mainDispatcherRule = MainDispatcherRule()`.
2. **Kiểm tra luồng `StateFlow`**: Sử dụng thư viện `Turbine` (`viewModel.uiState.test { ... }`) để kiểm tra chính xác các phát xạ trạng thái của StateFlow có `SharingStarted.WhileSubscribed`.
3. **Mock đối tượng phụ thuộc**: Dùng `MockK` (`mockk<T>()`, `coEvery`, `coVerify`) cho Repository/DAO/Store giả lập trong ViewModel tests.
4. **Mã hóa chuỗi / Base64**: Luôn sử dụng `java.util.Base64` trong mã nguồn chung để tránh lỗi `Stub!` khi chạy trên JVM.
5. **`unitTests.isReturnDefaultValues = true`** đã được bật trong `app/build.gradle.kts` — các lời gọi Android framework trong unit test trả về giá trị mặc định (0/null/false) thay vì ném exception `Stub!`.
6. **⚠️ Cẩn trọng với các test Milestone3**: hai file này **đọc và parse trực tiếp mã nguồn production** (tên file screen, tên hàm composable, danh sách route, số lượng ViewModel...). Nếu bạn đổi tên screen/route/ViewModel, thêm/bớt route, hoặc thay đổi cấu trúc hàm `*Screen`/`*Content`, **phải cập nhật đồng bộ các test này**, nếu không chúng sẽ fail dù code đúng.

---

## 5. Các Lệnh Thường Dùng (Essential Commands)

### Chạy Unit Test
```bash
# Windows
.\gradlew.bat :app:testDebugUnitTest

# macOS / Linux
./gradlew :app:testDebugUnitTest
```

### Biên dịch APK Debug
```bash
# Windows
.\gradlew.bat :app:assembleDebug

# macOS / Linux
./gradlew :app:assembleDebug
# File APK đầu ra: app/build/outputs/apk/debug/app-debug.apk
```

### Chạy Android Lint
```bash
# Windows
.\gradlew.bat :app:lintDebug

# macOS / Linux
./gradlew :app:lintDebug
# Báo cáo: app/build/reports/lint-results-debug.html
```

### Cài đặt và Chạy trên Thiết bị Android (qua ADB)
```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.cookingnote.app/.MainActivity
```

> Kết quả unit test dạng HTML nằm tại `app/build/reports/tests/testDebugUnitTest/index.html`. Build chạy với `org.gradle.jvmargs=-Xmx3072m -Dfile.encoding=UTF-8` (khai báo trong `gradle.properties`).

---

## 6. Hướng dẫn Mở rộng Tính năng Mới (How to Extend)

Khi thêm một màn hình hoặc tính năng mới vào Cooking Note, hãy thực hiện theo các bước chuẩn sau:

1. **Định nghĩa Route**: Thêm object kế thừa `Route` trong [`ui/Routes.kt`](file:///c:/Users/ADMIN/Documents/cooking-note/app/src/main/java/com/cookingnote/app/ui/Routes.kt).
2. **Xây dựng UI State & ViewModel**:
   - Tạo file `*UiState` và `*ViewModel` trong `ui/viewmodel/`.
   - Kế thừa `ViewModel()`, nhận dependencies qua constructor (Repository, Stores).
3. **Đăng ký vào Factory**: Thêm nhánh `modelClass.isAssignableFrom(...::class.java)` trong [`ui/viewmodel/AppViewModelFactory.kt`](file:///c:/Users/ADMIN/Documents/cooking-note/app/src/main/java/com/cookingnote/app/ui/viewmodel/AppViewModelFactory.kt).
4. **Tạo Composable Screen**:
   - Khởi tạo ViewModel qua `viewModel(factory = AppViewModelFactory(container, ...))`.
   - Thu thập state: `val state by viewModel.uiState.collectAsStateWithLifecycle()`.
   - Tạo hàm Composable Stateless riêng nhận `state` và các event callback (`onAction: () -> Unit`).
5. **Đăng ký Navigation**: Khai báo composable route trong [`ui/CookingNoteRoot.kt`](file:///c:/Users/ADMIN/Documents/cooking-note/app/src/main/java/com/cookingnote/app/ui/CookingNoteRoot.kt).
6. **Viết Unit Test**: Thêm file kiểm thử tương ứng trong `app/src/test/java/com/cookingnote/app/ui/viewmodel/` và đảm bảo `./gradlew testDebugUnitTest` vượt qua 100%. Nếu thêm/đổi route hoặc screen, cập nhật đồng bộ `Milestone3ArchitectureConformanceTest` và `Milestone3ChallengerAdversarialTest` (các test này hard-code số lượng route/screen/hàm composable).

---

## 7. AndroidManifest, Quyền & Bảo mật

### Quyền đã khai báo (`app/src/main/AndroidManifest.xml`):
| Quyền | Mục đích |
|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Gọi API AI (OpenAI/Anthropic/Gemini), kiểm tra kết nối |
| `CAMERA` + `uses-feature` (không bắt buộc) | Chụp ảnh món ăn — **CameraX đã thêm dependency nhưng chưa có mã nguồn sử dụng** |
| `RECORD_AUDIO` | Dự phòng cho tính năng nhập liệu giọng nói (chưa triển khai) |
| `READ_MEDIA_IMAGES` (API 33+) / `READ_EXTERNAL_STORAGE` (API ≤ 32) | Chọn ảnh từ thư viện |

### FileProvider & Sao lưu DB:
- `FileProvider` với authority `${applicationId}.fileprovider`, cấu hình đường dẫn tại `res/xml/file_paths.xml` (cache-path + files-path).
- Luồng backup: `SettingsScreen` gọi `viewModel.backupDatabase(context.cacheDir)` → `SettingsViewModel.createDatabaseBackup()` copy file `cookingnote.db` thành `cookingnote-backup.db` trên `Dispatchers.IO` → trả `File` về main thread → UI chia sẻ qua `FileProvider.getUriForFile` + `Intent.ACTION_SEND`.

### Bảo mật:
- **API keys AI chỉ được lưu qua `AiSettingsStore`** (EncryptedSharedPreferences, thư viện `androidx.security:security-crypto`). Không bao giờ log, hard-code, hoặc lưu plaintext key.
- Release build hiện **chưa bật minify** (`isMinifyEnabled = false`) — nếu bật R8, cần thêm rule cho Moshi/Retrofit/Room.

---

## 8. Tài liệu Liên quan & Quy ước Chung

- `README.md` — giới thiệu tính năng dành cho người dùng cuối, hướng dẫn cấu hình AI provider, sơ đồ schema.
- `ORIGINAL_REQUEST.md` — yêu cầu refactor gốc (R1–R4) và acceptance criteria của dự án.
- `AGENT.md` — **bản sao y hệt** của chính file `AGENTS.md` này (phục vụ tool đọc tên số ít). Khi sửa `AGENTS.md`, phải cập nhật `AGENT.md` đồng bộ.
- **Quy ước mã**: `kotlin.code.style=official`; encoding UTF-8; ngôn ngữ UI và comment trong app là tiếng Việt.
- **Không dùng thư viện DI bên ngoài** (Hilt/Koin): dự án chủ đích dùng manual DI qua `AppContainer` — giữ nguyên pattern này khi mở rộng.
