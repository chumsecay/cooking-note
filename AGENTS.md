# Cooking Note — Hướng dẫn Thống nhất cho AI Agents

> **Tài liệu quy chuẩn dành cho mọi AI Agent (Claude Code, OpenAI Codex, Gemini Antigravity, ARTEMIS, v.v.) khi làm việc trên dự án Cooking Note.**

---

## 1. Tổng quan Dự án & Môi trường Kỹ thuật

- **Nền tảng:** Android ứng dụng đơn module (`:app`), ngôn ngữ Kotlin 2.0.21.
- **SDK & JVM:** `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`, JVM Target 17.
- **Build tool:** Gradle Wrapper 9.3.0 (chạy từ thư mục gốc, dùng `./gradlew` hoặc `.\gradlew.bat`).
- **Giao diện:** 100% Jetpack Compose Material 3 kết hợp hệ thống Material Icons và Vector Graphics.
- **Cơ sở dữ liệu:** Room 2.6.1, database `cookingnote.db`, **Schema Version 3** (10 Entities, 9 DAOs).
- **Lưu trữ bảo mật:** `EncryptedSharedPreferences` (AES-256-GCM qua Android Keystore).

---

## 2. Lệnh Build, Kiểm thử & Kiểm tra Chất lượng

Tất cả lệnh chạy từ thư mục gốc repo. Trên Windows PowerShell dùng `.\gradlew.bat`, trên Linux/macOS/Git Bash dùng `./gradlew`:

```bash
# Kiểm tra biên dịch và kiểu dữ liệu Kotlin
./gradlew :app:compileDebugKotlin

# Chạy toàn bộ 135 unit test tự động trên JVM (~11-15s, không cần emulator)
./gradlew :app:testDebugUnitTest

# Chạy kiểm thử đơn vị cho một lớp cụ thể
./gradlew :app:testDebugUnitTest --tests "com.cookingnote.app.ui.viewmodel.HomeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.cookingnote.app.Milestone3*"

# Kiểm tra tĩnh mã nguồn (Lint)
./gradlew :app:lintDebug

# Đóng gói APK Debug (đầu ra: app/build/outputs/apk/debug/app-debug.apk)
./gradlew :app:assembleDebug
```

- Báo cáo kết quả kiểm thử: `app/build/reports/tests/testDebugUnitTest/index.html`.
- Báo cáo Lint: `app/build/reports/lint-results-debug.html`.
- Quản lý dependency tập trung tại `gradle/libs.versions.toml`.

---

## 3. Quy chuẩn Kiến trúc Bắt buộc (Wiring Rules)

Đường dẫn tương đối với `app/src/main/java/com/cookingnote/app/`:

1. **Dependency Injection (Thủ công / Manual DI):**
   - `CookingNoteApp` khởi tạo duy nhất `data/AppContainer`.
   - `MainActivity` cung cấp container qua `LocalAppContainer` CompositionLocal.
   - Tạo ViewModel qua `ui/viewmodel/AppViewModelFactory`.
   - **Tuyệt đối KHÔNG thêm Hilt, Dagger hoặc Koin** để giữ kiến trúc tối giản và tương thích kiểm thử.
2. **Luồng dữ liệu một chiều (UDF) & Quản lý Trạng thái:**
   - Màn hình (`*Screen`) lấy ViewModel, thu thập state qua `collectAsStateWithLifecycle()`.
   - Tách biệt rõ ràng: Screen Composable điều phối logic -> truyền UI State bất biến và lambda callback xuống `*Content` stateless.
   - **Không gọi Repository, DAO hoặc AI Service trực tiếp trong Composable.**
   - Khi thêm hoặc chỉnh sửa màn hình, phải đồng bộ:
     + `ui/Routes.kt`
     + `ui/CookingNoteRoot.kt`
     + `ui/viewmodel/AppViewModelFactory.kt`
     + Các bài kiểm thử kiến trúc trong `Milestone3ArchitectureConformanceTest`.
3. **Quản lý Định danh & Điều hướng:**
   - Màn hình Tạo/Sửa công thức dùng chung `CreateRecipeScreen`, phân biệt bằng `recipeId: Long` (`NavType.LongType`). Key ViewModel theo `recipeId` để tránh tái sử dụng sai state.
4. **Bảo mật & Cấu hình Phiên:**
   - API Key AI và Session Token lưu trong `data/prefs/AiSettingsStore.kt` và `UserSessionStore.kt` qua `EncryptedSharedPreferences`.
   - `AndroidManifest.xml` tắt `android:allowBackup="false"` để tránh rủi ro giải mã MasterKey khi khôi phục thiết bị.
   - Sao lưu CSDL SQLite trong `SettingsViewModel` sao chép cả file chính kèm file WAL (`.db-wal`) và SHM (`.db-shm`) để bảo toàn giao dịch.

---

## 4. Dữ liệu & Trí tuệ Nhân tạo (Room DB & AI Engine)

1. **Room Database Version 3:**
   - Quản lý 10 thực thể: `RecipeEntity`, `IngredientEntity`, `StepEntity`, `CategoryEntity`, `PantryItemEntity`, `CookHistoryEntity`, `TagEntity`, `RecipeTagCrossRef`, `ChatMessageEntity` (chứa `matchedRecipeId`), `AiQueryLogEntity`.
   - File schema JSON xuất tại `app/schemas/` được theo dõi trong Git. Không chỉnh sửa code Room sinh tự động trong `app/build/`.
2. **Dữ liệu Khởi tạo (SeedData):**
   - Nạp tự động trong `AppContainer` qua `SeedData.populate` khi tạo database.
   - Gồm: 21 công thức món ăn (đầy đủ 5 danh mục kèm `notes` mẹo nấu), 25 nguyên liệu tủ lạnh (có `expiryDate` và `lowStockThreshold`), 5 bản ghi lịch sử nấu, và tin nhắn mẫu cho AI.
3. **Phân hệ AI Đa tầng (Multi-Provider AI):**
   - Triển khai tại `ai/DefaultAiService.kt`, hỗ trợ 4 nhà cung cấp: OpenAI Chat, OpenAI Responses, Anthropic Claude, Google Gemini.
   - Cơ chế fail-safe: Khi không có kết nối mạng hoặc chưa cấu hình API key, hệ thống tự động chuyển sang `RuleBasedAi` để gợi ý món ăn cục bộ dựa trên nguyên liệu tủ lạnh, không gây crash ứng dụng.
   - Cấu hình đám mây mặc định tại `data/prefs/AiCloudDefaults.kt`; nạp remote config qua `data/prefs/AiRemoteConfig.kt` mà không chứa API key trong JSON.

---

## 5. Bẫy Kiểm thử Cần Lưu ý (Testing Gotchas)

- **Kiểm tra cú pháp chuỗi tĩnh:** `Milestone3ArchitectureConformanceTest.kt` và `Milestone3ChallengerAdversarialTest.kt` phân tích mã nguồn bằng chuỗi và Regex:
  + Không tự ý tạo thêm file helper `.kt` trong `ui/screens/` mà chưa cập nhật danh sách kiểm tra trong test.
  + Trong hàm `*Content`, tham số `uiState` bắt buộc phải đặt trước các lambda callback (vì parser dừng ở dấu ngoặc `)` đầu tiên).
  + Luôn giữ nguyên literal `collectAsStateWithLifecycle()`.
- **Coroutine & ViewModel Testing:**
  + Sử dụng `testutil/MainDispatcherRule` (mặc định `UnconfinedTestDispatcher`), MockK và Turbine.
  + Với StateFlow dùng `SharingStarted.WhileSubscribed`, phải có subscriber lắng nghe (ví dụ qua Turbine `.test { ... }`) trước khi đọc giá trị state.

---

## 6. Hướng dẫn Dành cho Tự động hóa Thiết bị (ARTEMIS / UI Automator)

- **Cài đặt & Khởi chạy:**
  1. Build APK: `./gradlew :app:assembleDebug`.
  2. Cài đặt vào thiết bị/emulator: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
  3. Khởi chạy ứng dụng: `adb shell am start -n com.cookingnote.app/.MainActivity`.
- **Làm mới dữ liệu kiểm thử (Clean Re-seed):**
  - Chạy lệnh `adb shell pm clear com.cookingnote.app` để xóa dữ liệu cũ và buộc Room kích hoạt lại `SeedData.populate`.
- **Nguyên tắc định vị phần tử UI:**
  - Ưu tiên định vị động bằng Content Description và Text tiếng Việt có dấu.
  - Sử dụng tọa độ tuyệt đối chỉ làm phương án dự phòng (fallback) khi kiểm thử trên emulator độ phân giải tiêu chuẩn (1080x2400).
