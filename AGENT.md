# Cooking Note — Hướng dẫn cho agent

## Build và kiểm chứng

- Chạy từ root repo; chỉ có module Android `:app`. Dùng Gradle wrapper (hiện 9.3.0), không dùng Gradle hệ thống. Cần JDK chạy được wrapper và Android SDK 35; JVM target 17 không đồng nghĩa wrapper chạy được trên Java 17. Đường dẫn SDK nằm trong `local.properties` (không commit).
- Lệnh Windows PowerShell dưới đây; macOS/Linux thay `.\gradlew.bat` bằng `./gradlew`:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:testDebugUnitTest --tests "com.cookingnote.app.ui.viewmodel.HomeViewModelTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.cookingnote.app.Milestone3*"
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

- `compileDebugKotlin` kiểm tra biên dịch/kiểu; không có task typecheck/formatter riêng được cấu hình. Unit test chạy JVM, không cần emulator nhưng vẫn cần Android SDK để build.
- Báo cáo: `app/build/reports/tests/testDebugUnitTest/index.html`, `app/build/reports/lint-results-debug.html`. APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Thêm/nâng dependency qua `gradle/libs.versions.toml`; repository Maven khai báo ở `settings.gradle.kts` vì bật `FAIL_ON_PROJECT_REPOS`.

## Wiring cần giữ

Các đường dẫn Kotlin dưới đây tương đối với `app/src/main/java/com/cookingnote/app/`.

- `CookingNoteApp` tạo `data/AppContainer`; `MainActivity` mở `ui/CookingNoteRoot`. Giữ manual DI qua `LocalAppContainer` và `AppViewModelFactory`, không thêm Hilt/Koin.
- Screen lấy ViewModel, thu state bằng `collectAsStateWithLifecycle()`, truyền UI state tường minh và callback xuống `*Content` stateless. Không gọi Repository/DAO/AI trực tiếp trong Composable. README còn ghi `collectAsState`: không làm theo đoạn đó.
- Thêm/đổi màn hình phải đồng bộ `ui/Routes.kt`, `ui/CookingNoteRoot.kt`, `ui/viewmodel/AppViewModelFactory.kt` và tests Milestone3. Create/Edit dùng chung editor; giữ `recipeId`, `NavType.LongType` và key ViewModel theo ID để tránh tái sử dụng state của công thức khác.
- Chuỗi UI dùng tiếng Việt. Giữ API key trong `data/prefs/AiSettingsStore.kt` (EncryptedSharedPreferences), không chuyển sang DataStore/plaintext.
- AI thực tế ở `ai/DefaultAiService.kt`: OkHttp đồng bộ trên IO và JSON viết tay, không phải Retrofit service dù dependency có sẵn. Giữ fallback `RuleBasedAi` và `java.util.Base64` để tương thích JVM tests.
- Cloud AI cấu hình sẵn duy nhất ở `data/prefs/AiCloudDefaults.kt` (provider/baseUrl/model + `REMOTE_CONFIG_URL`); không hard-code key. `data/prefs/AiRemoteConfig.kt` tải JSON từ xa, merge đè baked defaults, offline-first; JSON không chứa apiKey.

## Bẫy kiểm thử

- `app/src/test/java/com/cookingnote/app/Milestone3ArchitectureConformanceTest.kt` và `Milestone3ChallengerAdversarialTest.kt` đọc source bằng chuỗi/regex, không kiểm thử Compose runtime. Chúng cố định danh sách file screen, wiring factory, tên state và cả một số định dạng code; refactor hợp lệ vẫn có thể làm fail.
- Không thêm file helper `.kt` vào `ui/screens/` mà quên cập nhật danh sách test. Parser chữ ký `*Content` dừng ở dấu `)` đầu tiên: đặt tham số `uiState` trước callback. Kiểm tra cả literal `collectAsStateWithLifecycle()` và tên/format khai báo ViewModel khi sửa wiring.
- ViewModel tests dùng `testutil/MainDispatcherRule` (mặc định `UnconfinedTestDispatcher`), MockK và Turbine. Với `SharingStarted.WhileSubscribed`, phải có subscriber mới kích hoạt upstream; không chỉ đọc `.value` rồi chờ scheduler.
- `unitTests.isReturnDefaultValues = true`: Android framework stub có thể trả 0/null/false; test xanh không chứng minh hành vi thiết bị. Dùng dispatcher/scheduler chung khi inject IO trong test (xem `SettingsViewModelTest`).

## Dữ liệu và điểm cần rà soát trước khi đưa vào sử dụng

- Room là `data/database/RoomDatabase.kt`, DB `cookingnote.db`, version 2. KSP xuất schema vào `app/schemas/`; giữ schema trong git, không sửa code sinh trong `app/build/`.
- `data/AppContainer.kt` vẫn dùng `fallbackToDestructiveMigration()`, chưa đăng ký migration bảo toàn dữ liệu. Khi thay schema hoặc chuẩn bị nâng cấp bản đã cài, viết/test migration thay vì dựa vào fallback xóa dữ liệu.
- Seed chạy bất đồng bộ trong callback tạo DB; tạo container không có nghĩa seed đã xong. Sửa `SeedData` không cập nhật DB đã tồn tại.
- `ui/viewmodel/SettingsViewModel.kt` chỉ copy file DB đang mở, chưa checkpoint/đồng bộ ghi: không coi đây là snapshot SQLite nhất quán khi dùng WAL. Không bao gồm preferences/API settings; test copy file không chứng minh backup có thể restore.
- `app/src/main/AndroidManifest.xml` bật `allowBackup` nhưng chưa có quy tắc loại trừ encrypted preferences; cần rà soát backup/restore với MasterKey trước phát hành.
- Khi sửa AI, kiểm tra JSON request/response thực tế: mã hiện ghép/parse bằng chuỗi. Rà soát lỗi/log trong `DefaultAiService` trước phát hành; Gemini đặt key trong URL và exception có thể chứa body phản hồi provider.

## Tài liệu

- `README.md` mô tả tính năng/cấu hình AI; `ORIGINAL_REQUEST.md` chứa tiêu chí refactor gốc. Khi mâu thuẫn, ưu tiên cấu hình build và source hiện tại; không suy ra production-ready chỉ từ mô tả tính năng hoặc unit test.
- `AGENT.md` là bản sao của file này; khi chỉnh sửa phải đồng bộ nội dung hai file.
