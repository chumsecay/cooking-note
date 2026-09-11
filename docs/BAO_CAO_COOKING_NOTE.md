# BÁO CÁO BÀI TẬP LỚN MÔN: PHÁT TRIỂN ỨNG DỤNG DI ĐỘNG

**TRƯỜNG CAO ĐẲNG KỸ THUẬT – CÔNG NGHỆ BÁCH KHOA**  
**KHOA CÔNG NGHỆ THÔNG TIN**  

---

### **ĐỀ TÀI:**  
# XÂY DỰNG ỨNG DỤNG SỔ TAY CÔNG THỨC NẤU ĂN THÔNG MINH  
### *(COOKING NOTE — OFFLINE-FIRST & AI ASSISTANT)*

* **Giảng viên hướng dẫn:** ThS. Lê Văn Quân  
* **Lớp:** TT602-K16LT  
* **Nhóm sinh viên thực hiện:**
  1. **Vũ Đình Đạo (Nhóm trưởng)** — TT602-K16LT
  2. **Ngô Gia Bảo** — TT602-K16LT
  3. **Nguyễn Tiến Dũng** — TT602-K16LT
  4. **Lê Đức Trọng** — TT602-K16LT

*Hà Nội, Năm 2026*

---

## BẢNG PHÂN CHIA CÔNG VIỆC VÀ ĐÁNH GIÁ ĐÓNG GÓP

| Họ và tên | Lớp | Nhiệm vụ đảm nhiệm | Mức độ hoàn thành |
|---|:---:|---|:---:|
| **Vũ Đình Đạo**<br>*(Nhóm trưởng)* | TT602-K16LT | • Thiết kế kiến trúc tổng thể (MVVM + UDF + Repository Pattern)<br>• Thiết kế và cấu hình Room Database (10 Entities, 9 DAOs)<br>• Quản lý điều hướng Navigation Compose và AppContainer<br>• Tổng hợp mã nguồn, quản lý Git và điều phối dự án | **100%** |
| **Ngô Gia Bảo** | TT602-K16LT | • Xây dựng tầng Presentation (10 ViewModels độc lập)<br>• Mô hình hóa UI State rõ ràng (Loading, Content, Empty, Error)<br>• Tối ưu hóa vòng đời Compose với `collectAsStateWithLifecycle()`<br>• Hiện thực hóa `AppViewModelFactory` | **100%** |
| **Nguyễn Tiến Dũng** | TT602-K16LT | • Lập trình giao diện Jetpack Compose Material 3<br>• Xây dựng các màn hình: Home, Library, Detail, Pantry, Create Recipe<br>• Tối ưu hóa trải nghiệm người dùng, bảng màu và animation | **100%** |
| **Lê Đức Trọng** | TT602-K16LT | • Phát triển phân hệ AI Assistant (OpenAI, Gemini, Anthropic)<br>• Xây dựng thuật toán `RuleBasedAi` hỗ trợ ngoại tuyến<br>• Xây dựng bộ tự động kiểm thử 100 Unit Tests trên JVM<br>• Soạn thảo báo cáo kỹ thuật và tài liệu thuyết trình | **100%** |

---

## MỤC LỤC

- [CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI](#chương-1-giới-thiệu-đề-tài)
  - [1.1. Lý do chọn đề tài](#11-lý-do-chọn-đề-tài)
  - [1.2. Mục tiêu của bài tập lớn](#12-mục-tiêu-của-bài-tập-lớn)
  - [1.3. Phạm vi và đối tượng sử dụng](#13-phạm-vi-và-đối-tượng-sử-dụng)
- [CHƯƠNG 2: PHÂN TÍCH BÀI TOÁN](#chương-2-phân-tích-bài-toán)
  - [2.1. Mô tả bài toán](#21-mô-tả-bài-toán)
  - [2.2. Yêu cầu chức năng của hệ thống](#22-yêu-cầu-chức-năng-của-hệ-thống)
  - [2.3. Yêu cầu phi chức năng](#23-yêu-cầu-phi-chức-năng)
  - [2.4. Xác định các đối tượng trong hệ thống](#24-xác-định-các-đối-tượng-trong-hệ-thống)
- [CHƯƠNG 3: THIẾT KẾ HỆ THỐNG](#chương-3-thiết-kế-hệ-thống)
  - [3.1. Thiết kế kiến trúc tổng thể (MVVM + UDF)](#31-thiết-kế-kiến-trúc-tổng-thể-mvvm--udf)
  - [3.2. Thiết kế lớp (Class Design)](#32-thiết-kế-lớp-class-design)
  - [3.3. Thiết kế cơ sở dữ liệu (Database Schema)](#33-thiết-kế-cơ-sở-dữ-liệu-database-schema)
  - [3.4. Thiết kế luồng xử lý và vòng đời dữ liệu](#34-thiết-kế-luồng-xử-lý-và-vòng-đời-dữ-liệu)
- [CHƯƠNG 4: CÀI ĐẶT VÀ KẾT QUẢ THỰC NGHIỆM](#chương-4-cài-đặt-và-kết-quả-thực-nghiệm)
  - [4.1. Công cụ và môi trường phát triển](#41-công-cụ-và-môi-trường-phát-triển)
  - [4.2. Cấu trúc project mã nguồn](#42-cấu-trúc-project-mã-nguồn)
  - [4.3. Giao diện và các màn hình chức năng chính](#43-giao-diện-và-các-màn-hình-chức-năng-chính)
  - [4.4. Đánh giá chất lượng và kết quả kiểm thử tự động (100 Tests)](#44-đánh-giá-chất-lượng-và-kết-quả-kiểm-thử-tự-động-100-tests)
- [KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN](#kết-luận-và-hướng-phát-triển)
- [TÀI LIỆU THAM KHẢO](#tài-liệu-tham-khảo)

---

## CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI

### 1.1. Lý do chọn đề tài
Trong nhịp sống hiện đại bận rộn, việc tự nấu ăn tại nhà không chỉ giúp đảm bảo vệ sinh an toàn thực phẩm, cân bằng dinh dưỡng mà còn là một nét văn hóa gia đình ấm cúng. Tuy nhiên, người nội trợ và các bạn trẻ sống tự lập thường xuyên phải đối mặt với các câu hỏi nan giải mỗi ngày: *"Hôm nay ăn gì?"*, *"Tủ lạnh còn những gì và nấu được món gì?"*, hoặc gặp khó khăn khi muốn lưu trữ lại những công thức nấu ăn sáng tạo của riêng mình.

Mặc dù trên thị trường có nhiều ứng dụng công thức nấu ăn, đa số các ứng dụng hiện nay gặp phải những hạn chế lớn:
1. **Phụ thuộc hoàn toàn vào kết nối Internet**: Khi vào bếp, tầng hầm hoặc vùng sóng yếu, người dùng không thể tra cứu công thức.
2. **Thiếu tính năng quản lý thực phẩm tại chỗ**: Không kết nối được giữa kho nguyên liệu đang có trong tủ lạnh với các món có thể nấu.
3. **Trải nghiệm quảng cáo tràn lan và hiệu năng chưa tối ưu**: Gây gián đoạn quá trình nấu ăn.
4. **Thiếu trợ lý thông minh hỗ trợ tùy biến thực đơn**: Không tận dụng được sức mạnh của trí tuệ nhân tạo (AI) để gợi ý món theo nhu cầu thực tế.

Chính vì những lý do trên, nhóm chúng em đã lựa chọn đề tài **"Xây dựng ứng dụng Sổ tay Công thức Nấu ăn Thông minh (Cooking Note)"** với định hướng **Offline-first** (hoạt động bền bỉ không cần mạng), kết hợp linh hoạt cùng **Trợ lý AI đa nền tảng** và thuật toán gợi ý cục bộ, nhằm mang đến cho người dùng một công cụ đắc lực, tiện lợi và hiện đại nhất trong gian bếp.

### 1.2. Mục tiêu của bài tập lớn
Đề tài hướng tới việc hoàn thành toàn diện cả về mặt kiến thức công nghệ lẫn kỹ năng lập trình chuyên nghiệp:
* **Về mặt kiến thức:**
  - Nắm vững và làm chủ bộ công cụ phát triển giao diện hiện đại nhất của Android: **Jetpack Compose và Material 3 Design**.
  - Hiểu sâu sắc và áp dụng chuẩn mực mô hình kiến trúc **MVVM (Model - View - ViewModel)** kết hợp nguyên lý **Unidirectional Data Flow (UDF)**.
  - Thành thạo công nghệ lưu trữ dữ liệu bền vững Offline với SQLite thông qua **Room Database 2.6.1** và bộ xử lý **KSP** (Kotlin Symbol Processing).
  - Nắm vững kỹ thuật lập trình bất đồng bộ phản ứng với **Kotlin Coroutines và Reactive Flow**.
  - Tiếp cận và tích hợp các mô hình ngôn ngữ lớn (LLM AI) thông qua chuẩn RESTful API và kỹ thuật bảo mật khóa bằng **EncryptedSharedPreferences**.
* **Về mặt kỹ năng thực tế:**
  - Kỹ năng phân tích, thiết kế hệ thống phần mềm di động theo quy chuẩn công nghiệp.
  - Kỹ năng xây dựng bộ kiểm thử tự động (Unit Test, Regression Test) đạt độ tin cậy tuyệt đối (100% test pass).
  - Kỹ năng làm việc nhóm, quản lý mã nguồn chuyên nghiệp với Git và GitHub.

### 1.3. Phạm vi và đối tượng sử dụng
* **Phạm vi chương trình:**
  - Ứng dụng chạy độc lập trên hệ điều hành Android (hỗ trợ từ Android 8.0 Oreo - API 26 đến Android 15 - API 35).
  - Lưu trữ cơ sở dữ liệu nội bộ SQLite Room với 10 bảng dữ liệu có quan hệ toàn vẹn.
  - Hoạt động trơn tru ngoại tuyến (Offline-first) và có khả năng kết nối linh hoạt tới 4 nhà cung cấp AI hàng đầu thế giới (OpenAI Chat, OpenAI Responses, Anthropic Claude, Google Gemini).
* **Đối tượng người dùng:**
  - Người nội trợ gia đình, sinh viên ở trọ, người độc thân muốn tự nấu ăn nhanh chóng, khoa học.
  - Những người đam mê ẩm thực muốn lưu trữ công thức cá nhân và theo dõi hạn sử dụng nguyên liệu trong tủ lạnh.

---

## CHƯƠNG 2: PHÂN TÍCH BÀI TOÁN

### 2.1. Mô tả bài toán
Hệ thống phần mềm cần giải quyết bài toán quản trị thông tin ẩm thực và hỗ trợ ra quyết định nấu nướng cho người dùng cá nhân. Người dùng cần một ứng dụng có tốc độ mở tức thì, giao diện trực quan, cho phép thao tác một chạm khi tay đang ướt hoặc đang bận rộn làm bếp. Dữ liệu cần được bảo đảm an toàn trên thiết bị của chính người dùng, không phụ thuộc vào máy chủ trung gian (Zero cloud lock-in).

### 2.2. Yêu cầu chức năng của hệ thống
Hệ thống Cooking Note được phân rã thành 5 phân hệ chức năng cốt lõi:

1. **Phân hệ Quản lý Công thức Nấu ăn (Recipe Management):**
   - Xem danh sách công thức theo thẻ (card) trực quan gồm ảnh, tên, thời gian nấu, độ khó.
   - Xem chi tiết công thức: Danh mục, định lượng nguyên liệu, các bước nấu có đánh số thứ tự, ghi chú.
   - Thêm mới công thức: Hỗ trợ thêm linh hoạt nhiều nguyên liệu và các bước làm.
   - Chỉnh sửa, xóa công thức hiện có.
   - Đánh dấu món ăn yêu thích (Favorites) và ghi nhận nhật ký đã nấu (Cook History).
   - Chia sẻ nhanh công thức dưới dạng văn bản có định dạng chuẩn qua các mạng xã hội.

2. **Phân hệ Tủ lạnh & Nguyên liệu (Pantry Management):**
   - Quản lý danh sách thực phẩm hiện có trong tủ lạnh (số lượng, đơn vị đo, ngày hết hạn).
   - Cảnh báo trực quan các nguyên liệu sắp hết (Low stock threshold alert).
   - Thêm, sửa, xóa nhanh nguyên liệu trong tủ lạnh.

3. **Phân hệ Trợ lý Thông minh (AI Kitchen Assistant):**
   - Giao diện chat dạng hội thoại thời gian thực, lưu trữ lịch sử chat vào SQLite Room.
   - Tự động nạp ngữ cảnh (Context Injection): Tự động nạp danh sách nguyên liệu trong tủ lạnh, 5 món yêu thích và lịch sử nấu gần nhất vào lời nhắc hệ thống.
   - Các phím tắt thông minh (Quick Prompts): Gợi ý món từ tủ lạnh, Món nhanh 15 phút, Món chay thanh đạm, Món cay kích thích vị giác.
   - Thuật toán ngoại tuyến `RuleBasedAi`: Hoạt động ngay lập tức khi người dùng không có mạng hoặc không cấu hình API Key.

4. **Phân hệ Tra cứu & Lọc Nâng cao (Search & Filtering):**
   - Tìm kiếm tức thì hỗ trợ Debounce 300ms: Tìm kiếm theo tên món, mô tả, ghi chú và tên nguyên liệu.
   - Lọc công thức đa chiều theo danh mục món ăn (Món canh, món kho, món xào, món tráng miệng...).

5. **Phân hệ Quản trị Cấu hình & Dữ liệu (Settings & Backup):**
   - Cấu hình Multi-provider AI (OpenAI Chat, OpenAI Responses, Anthropic, Gemini, Rule-based).
   - Nhập và lưu trữ an toàn mã khóa API (API Key Encryption).
   - Sao lưu toàn bộ cơ sở dữ liệu SQLite ra tệp dự phòng `cookingnote-backup.db` chạy bất đồng bộ trên luồng IO, chia sẻ qua hệ điều hành bằng Android FileProvider.

### 2.3. Yêu cầu phi chức năng
* **Hiệu năng và phản hồi (Performance):**
  - Tốc độ khởi động nguội (Cold start) < 1.0 giây.
  - Thao tác tìm kiếm phản hồi < 50ms nhờ cơ chế tối ưu index cơ sở dữ liệu Room.
  - Tốc độ cuộn danh sách (LazyColumn) đạt chuẩn mượt mà 60fps - 120fps, không gây hiện tượng giật khung hình (jank).
* **An toàn vòng đời và bộ nhớ (Lifecycle Safety):**
  - 100% màn hình sử dụng `collectAsStateWithLifecycle()`: Tự động ngừng lắng nghe luồng dữ liệu khi ứng dụng xuống chế độ nền (background), triệt tiêu hoàn toàn nguy cơ rò rỉ bộ nhớ (memory leaks).
* **Tính bảo mật (Security):**
  - API Key của các dịch vụ AI được mã hóa bằng tiêu chuẩn mã hóa quân đội AES-256 GCM thông qua Android Jetpack Security (`EncryptedSharedPreferences`), không lưu trữ plaintext.
* **Khả năng vận hành ngoại tuyến (Offline-first Resilience):**
  - Toàn bộ tính năng CRUD, tra cứu, tủ lạnh, lịch sử và gợi ý cục bộ hoạt động 100% độc lập không cần mạng Internet.

### 2.4. Xác định các đối tượng trong hệ thống
1. **Recipe (Công thức):** Đại diện cho một món ăn, chứa thông tin tên, mô tả, thời gian chuẩn bị, thời gian nấu, khẩu phần, độ khó, trạng thái yêu thích.
2. **Ingredient (Nguyên liệu công thức):** Thuộc về một Recipe, bao gồm tên, định lượng (amount), đơn vị (unit), thuộc tính tùy chọn (isOptional).
3. **Step (Bước nấu):** Thuộc về một Recipe, có số thứ tự (stepNumber) và nội dung hướng dẫn chi tiết.
4. **Category (Danh mục món ăn):** Phân loại món ăn (Ví dụ: Món canh, Món xào, Món mặn, Món chay, Đồ uống).
5. **PantryItem (Nguyên liệu trong tủ lạnh):** Gồm tên, định lượng thực tế, hạn dùng và ngưỡng cảnh báo sắp hết.
6. **CookHistory (Lịch sử nấu):** Lưu vết mỗi lần nấu một món ăn cụ thể, thời gian nấu và ghi chú cảm nhận.
7. **ChatMessage (Tin nhắn trợ lý):** Ghi nhận các câu hỏi của người dùng và phản hồi gợi ý từ AI.

---

## CHƯƠNG 3: THIẾT KẾ HỆ THỐNG

### 3.1. Thiết kế kiến trúc tổng thể (MVVM + UDF)

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│  10 Composable Screens (HomeScreen, LibraryScreen, DetailScreen,...)   │
│        ▲                                                     │         │
│        │ (State down: collectAsStateWithLifecycle)           │ (Events)│
│        │                                                     ▼         │
│  10 Dedicated ViewModels (HomeViewModel, DetailViewModel, ...)         │
│  (StateFlow<UiState> - Loading, Content, Empty, Error)                 │
└────────────────────────────────────────────────────────────────────────┘
                                  ▲  │
  (Flow / Coroutines)             │  │ (Suspend Methods Execution)
                                  │  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             DATA LAYER                                 │
│  CookbookRepository (Single Source of Truth)                           │
│  AppContainer (Manual Dependency Injection Container)                  │
│        │                                                     │         │
│        ▼                                                     ▼         │
│  Room Database (9 DAOs)                              AiService         │
│  (SQLite local persistence)                          (Cloud & Offline) │
└────────────────────────────────────────────────────────────────────────┘
```

### 3.2. Thiết kế lớp (Class Design)
* **Tầng ViewModel (10 ViewModels độc lập):**
  - `HomeViewModel`: Điều phối luồng dữ liệu trang chủ, thống kê số lượng món và gợi ý ngẫu nhiên mỗi ngày.
  - `LibraryViewModel`: Quản lý danh mục và lọc công thức theo danh mục được chọn.
  - `DetailViewModel`: Nạp thông tin chi tiết một món ăn (`RecipeWithDetails`), đánh dấu yêu thích, ghi nhận đã nấu, xóa món.
  - `CreateRecipeViewModel`: Quản lý biểu mẫu nhập liệu công thức, tự động kiểm tra tính hợp lệ (form validation), lưu giao dịch đa bảng.
  - `PantryViewModel`: Quản lý kho thực phẩm, cảnh báo nguyên liệu sắp hết, hiển thị dialog thêm đồ.
  - `FavoritesViewModel`: Lọc và hiển thị danh sách các món ăn được người dùng yêu thích.
  - `HistoryViewModel`: Quản lý lịch sử nấu nướng, định dạng ngày tháng hiển thị thân thiện.
  - `SearchViewModel`: Xử lý tìm kiếm công thức tức thì tích hợp Debounce 300ms nhằm giảm tải CPU.
  - `SettingsViewModel`: Xử lý cấu hình AI Provider, mã hóa khóa API và sao lưu DB trên luồng IO.
  - `AiViewModel`: Quản lý phiên hội thoại với AI, nạp ngữ cảnh tủ lạnh và điều phối fallback ngoại tuyến.
  - `AppViewModelFactory`: Lớp khởi tạo ViewModel Factory tập trung, thực hiện Manual Dependency Injection từ `AppContainer`.

* **Tầng Repository & Data Access:**
  - `CookbookRepository`: Cung cấp API duy nhất cho toàn bộ ViewModels để truy vấn dữ liệu Room và AI, thực thi các nghiệp vụ giao dịch (Transaction).
  - 9 Room DAOs: `RecipeDao`, `IngredientDao`, `StepDao`, `CategoryDao`, `PantryDao`, `HistoryDao`, `TagDao`, `AiLogDao`, `ChatMessageDao`.
  - `DefaultAiService` & `RuleBasedAi`: Thực thi gọi mạng tới LLM hoặc thuật toán luật ngoại tuyến.

### 3.3. Thiết kế cơ sở dữ liệu (Database Schema)

| Tên Bảng | Khóa chính (PK) | Khóa ngoại (FK) | Mục đích sử dụng |
|---|---|---|---|
| `recipes` | `id` (Long, AutoGen) | `categoryId -> categories(id)` | Lưu thông tin chính của công thức nấu ăn |
| `ingredients` | `id` (Long, AutoGen) | `recipeId -> recipes(id) [CASCADE]` | Lưu các nguyên liệu và định lượng của từng công thức |
| `steps` | `id` (Long, AutoGen) | `recipeId -> recipes(id) [CASCADE]` | Lưu các bước thực hiện có đánh số thứ tự |
| `categories` | `id` (Long, AutoGen) | Không | Danh mục phân loại món ăn (Canh, Xào, Mặn, Chay...) |
| `pantry_items` | `id` (Long, AutoGen) | Không | Kho nguyên liệu thực tế có trong tủ lạnh |
| `cook_history` | `id` (Long, AutoGen) | `recipeId -> recipes(id) [CASCADE]` | Nhật ký lịch sử mỗi lần nấu món ăn |
| `tags` | `id` (Long, AutoGen) | Không | Nhãn phân loại tự do (Nhanh, Cay, Món tiệc...) |
| `recipe_tag_cross_ref` | `(recipeId, tagId)` | `recipeId, tagId [CASCADE]` | Bảng quan hệ nhiều - nhiều giữa Recipe và Tag |
| `chat_messages` | `id` (Long, AutoGen) | Không | Lịch sử tin nhắn giữa người dùng và trợ lý AI |
| `ai_query_logs` | `id` (Long, AutoGen) | Không | Nhật ký ghi vết hiệu năng và độ trễ các lượt gọi AI |

### 3.4. Thiết kế luồng xử lý và vòng đời dữ liệu
1. Người dùng tương tác giao diện (Event up) -> Composable gọi hàm trên ViewModel tương ứng.
2. ViewModel điều phối tác vụ sang `CookbookRepository` thông qua `viewModelScope` và các Coroutine Dispatcher thích hợp (`Dispatchers.IO` cho tác vụ mạng/đĩa).
3. Repository tương tác với Room DAO hoặc AI Service, sau đó phát xạ kết quả về dưới dạng `Flow`.
4. ViewModel biến đổi dữ liệu thành các dạng `UiState` bất biến (`Loading`, `Content`, `Empty`, `Error`) và lưu giữ bằng `StateFlow`.
5. Composable Screen lắng nghe qua `collectAsStateWithLifecycle()`, tự động cập nhật lại giao diện (Recomposition) một cách tối ưu nhất.

---

## CHƯƠNG 4: CÀI ĐẶT VÀ KẾT QUẢ THỰC NGHIỆM

### 4.1. Công cụ và môi trường phát triển
* **Hệ điều hành phát triển:** Windows 11 64-bit / Linux / macOS.
* **Môi trường phát triển tích hợp (IDE):** Android Studio Ladybug / Koala.
* **Ngôn ngữ lập trình:** Kotlin 2.0.21, mục tiêu JVM 17.
* **Khung giao diện:** Jetpack Compose (BOM 2024.12.01), Material 3 Design.
* **Quản lý phụ thuộc:** Gradle 9.3.0, Gradle Version Catalog (`gradle/libs.versions.toml`).
* **SDK Target:** `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26` (tương thích hơn 95% thiết bị Android đang hoạt động).

### 4.2. Cấu trúc project mã nguồn
```text
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
│   │   └── RoomDatabase.kt        # AppDatabase Room definition & TypeConverters
│   ├── entity/
│   │   ├── Entities.kt            # Room entities (RecipeEntity, IngredientEntity, ...)
│   │   ├── ChatMessageEntity.kt   # Entity lưu lịch sử chat AI
│   │   └── Relations.kt           # Quan hệ 1-N, N-N (RecipeWithDetails, ...)
│   ├── prefs/
│   │   ├── AiSettingsStore.kt     # EncryptedSharedPreferences lưu API Keys an toàn
│   │   └── UserPrefsStore.kt      # DataStore Preferences cho tùy chọn người dùng
│   └── repository/
│       └── CookbookRepository.kt  # Repository trung tâm điều phối toàn bộ dữ liệu
└── ui/
    ├── CookingNoteRoot.kt         # NavHost điều hướng 11 màn hình
    ├── Routes.kt                  # Sealed class Route định nghĩa đường dẫn điều hướng
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

### 4.3. Giao diện và các màn hình chức năng chính
1. **Màn hình Trang chủ (HomeScreen):** Hiển thị bảng điều khiển tổng thể, thống kê số món ăn trong sổ tay, số món yêu thích và 3 món ăn được gợi ý ngẫu nhiên hôm nay.
2. **Màn hình Thư viện (LibraryScreen):** Duyệt toàn bộ công thức món ăn, hỗ trợ thanh trượt danh mục ngang (Chips) giúp lọc nhanh món canh, xào, kho, luộc, chay.
3. **Màn hình Chi tiết món (DetailScreen):** Trình bày hình ảnh món ăn, thời gian, khẩu phần, danh sách nguyên liệu và các bước làm có đánh số. Nút bấm một chạm đánh dấu "Yêu thích", "Đánh dấu đã nấu" và "Chia sẻ công thức".
4. **Màn hình Thêm/Sửa công thức (CreateRecipeScreen):** Biểu mẫu nhập liệu trực quan cho phép thêm không giới hạn các nguyên liệu và các bước nấu với cơ chế tự động kiểm tra lỗi (validation).
5. **Màn hình Tủ lạnh (PantryScreen):** Hiển thị danh mục các thực phẩm đang có trong nhà, gắn nhãn cảnh báo đỏ với các món có số lượng dưới ngưỡng sắp hết.
6. **Màn hình Yêu thích (FavoritesScreen):** Danh sách các món ăn đã được người dùng gắn sao yêu thích để truy cập nhanh.
7. **Màn hình Lịch sử nấu (HistoryScreen):** Dòng thời gian ghi nhận các bữa ăn đã được nấu kèm ngày giờ và nhận xét độ ngon miệng.
8. **Màn hình Tìm kiếm (SearchScreen):** Thanh tìm kiếm thông minh tự động debounce 300ms, tìm tức thì theo tên món hoặc nguyên liệu sẵn có.
9. **Màn hình Trợ lý AI (AiScreen):** Giao diện chat thời gian thực với trợ lý ảo, tích hợp các phím tắt chọn nhanh câu hỏi và tự động gợi ý món dựa trên thực phẩm đang có trong tủ lạnh.
10. **Màn hình Cài đặt (SettingsScreen):** Cho phép lựa chọn giữa 5 chế độ AI, thiết lập khóa API an toàn và nút bấm sao lưu toàn bộ cơ sở dữ liệu SQLite thành file chia sẻ.

### 4.4. Đánh giá chất lượng và kết quả kiểm thử tự động (100 Tests)
* **Kết quả thực tế từ Gradle Test Runner:**
  ```text
  > Task :app:testDebugUnitTest
  BUILD SUCCESSFUL in 1m 24s
  100 tests completed, 0 failed, 0 skipped
  ```
  - `CookbookRepositoryTest`: Xác thực các giao dịch lưu công thức, cập nhật yêu thích, xóa công thức, gợi ý theo tủ lạnh, lưu vết lịch sử nấu và quản lý chat.
  - `RuleBasedAiTest`: Kiểm thử thuật toán phân loại và gợi ý món ăn theo từ khóa ("nhanh", "chay", v.v.) và cơ chế fallback.
  - `Base64EmpiricalTest`: Kiểm thử mã hóa/giải mã tương thích JVM.
  - 10 ViewModel Tests (`Home`, `Library`, `Detail`, `Pantry`, `Favorites`, `History`, `Search`, `Settings`, `Ai`, `CreateRecipe`): Kiểm tra 100% các trạng thái Loading, Content, Empty, Error.
* **Kết quả biên dịch APK sản phẩm (Build Verification):**
  ```text
  > Task :app:assembleDebug
  BUILD SUCCESSFUL in 49s
  Tệp APK: app/build/outputs/apk/debug/app-debug.apk
  ```

---

## KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

### 1. Đánh giá kết quả đạt được
* Xây dựng thành công ứng dụng Android Cooking Note hoàn chỉnh, hoạt động mượt mà, giao diện Material 3 bắt mắt và hiện đại.
* Ứng dụng đáp ứng trọn vẹn tiêu chí Offline-first, lưu trữ dữ liệu an toàn trên thiết bị thông qua Room Database 2.6.1.
* Tái cấu trúc thành công 100% kiến trúc phần mềm sang mô hình chuẩn MVVM + Unidirectional Data Flow, tách biệt hoàn toàn giữa UI và Data Layer.
* Tích hợp linh hoạt giữa phân hệ Trí tuệ nhân tạo điện toán đám mây và thuật toán gợi ý ngoại tuyến thông minh.
* Xây dựng hệ thống kiểm thử tự động với 100 bài test đạt tỷ lệ vượt qua 100%.

### 2. Hạn chế của đề tài
* Tính năng nhận diện hình ảnh món ăn trực tiếp qua Camera (CameraX) mới chỉ tích hợp dependency cấu hình, chưa hoàn thiện giao diện chụp ảnh trực tiếp trong phiên bản hiện tại.
* Chưa hỗ trợ đồng bộ dữ liệu đa thiết bị qua tài khoản đám mây (Cloud Sync) do định hướng hiện tại là ưu tiên bảo mật dữ liệu cục bộ.

### 3. Hướng phát triển trong tương lai
* **Tích hợp thị giác máy tính (AI Vision CameraX):** Cho phép người dùng chụp ảnh tủ lạnh thực tế, AI tự động quét và thêm nguyên liệu vào danh sách Pantry.
* **Tính năng tính toán dinh dưỡng (Calories & Macro Tracker):** Tự động phân tích hàm lượng Calo, Protein, Carb, Fat cho từng công thức món ăn.
* **Hỗ trợ hẹn giờ nấu ăn thông minh (Cooking Timer & Voice Assistant):** Giúp người dùng vừa nấu nướng vừa nghe hướng dẫn các bước bằng giọng nói tiếng Việt mà không cần chạm vào màn hình.

---

## TÀI LIỆU THAM KHẢO

1. **Android Developers Official Documentation**:
   * *Jetpack Compose basics & Material 3*: https://developer.android.com/develop/ui/compose
   * *Guide to app architecture (MVVM & UDF)*: https://developer.android.com/topic/architecture
   * *Save data in a local database using Room*: https://developer.android.com/training/data-storage/room
   * *Kotlin Coroutines and Flow on Android*: https://developer.android.com/kotlin/coroutines
2. **Kotlin Programming Language Documentation**: https://kotlinlang.org/docs/home.html
3. **OpenAI API & Anthropic API Documentation**:
   * *OpenAI Chat API reference*: https://platform.openai.com/docs/api-reference
   * *Anthropic Messages API reference*: https://docs.anthropic.com/en/api/messages
4. **Google Gemini API Developer Guide**: https://ai.google.dev/docs
