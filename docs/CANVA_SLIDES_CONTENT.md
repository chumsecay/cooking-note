# NỘI DUNG 15 SLIDE THUYẾT TRÌNH DÀNH CHO CANVA
**Dự án:** Cooking Note — Sổ tay Công thức Nấu ăn Thông minh  
**Liên kết Canva:** [Thiết kế không tên - Bài thuyết trình](https://www.canva.com/design/DAHU2QT6v74/3rhwByRRgyPxY7--8pSUBQ/edit?ui=e30)  
**File PowerPoint gốc:** `THUYET_TRINH_COOKING_NOTE.pptx` (Đã tải lên Google Drive & lưu tại thư mục `docs/`)

---

## CÁCH 1: NHẬP TRỰC TIẾP VÀO CANVA TRONG 5 GIÂY (KHUYÊN DÙNG)
1. Mở liên kết Canva của bạn: https://www.canva.com/design/DAHU2QT6v74/3rhwByRRgyPxY7--8pSUBQ/edit?ui=e30
2. Trên thanh menu trên cùng, bấm **Tệp (File)** > **Nhập tệp (Import files)** (hoặc kéo thả trực tiếp file `THUYET_TRINH_COOKING_NOTE.pptx` từ máy tính vào giao diện Canva).
3. Canva sẽ tự động phân tích và nhập toàn bộ 15 trang chiếu với màu sắc cam ấm, bố cục khối, kiểu chữ tiếng Việt không lỗi font.

---

## CÁCH 2: NỘI DUNG CHI TIẾT TỪNG SLIDE ĐỂ COPY - PASTE VÀO CANVA

### SLIDE 1: TRANG TIÊU ĐỀ
* **Tiêu đề chính:** SỔ TAY CÔNG THỨC NẤU ĂN THÔNG MINH (COOKING NOTE)
* **Tiêu đề phụ:** Báo cáo Bài tập lớn Môn Phát triển Ứng dụng Di động
* **Thông tin nhóm:**
  - Lớp: TT602-K16LT
  - Giảng viên hướng dẫn: ThS. Lê Văn Quân
  - Sinh viên thực hiện:
    1. Vũ Đình Đạo (Nhóm trưởng)
    2. Ngô Gia Bảo
    3. Nguyễn Tiến Dũng
    4. Lê Đức Trọng

### SLIDE 2: BỐI CẢNH & NỖI ĐAU NGƯỜI DÙNG (PAIN POINTS)
* **Tiêu đề:** Bối cảnh Thực tế & Vấn đề Cần giải quyết
* **Nội dung 3 cột / 3 hộp (Boxes):**
  - **Hộp 1: "Hôm nay ăn gì?"**  
    Người nội trợ và sinh viên mất 20–30 phút mỗi ngày đắn đo chọn món cho bữa ăn gia đình.
  - **Hộp 2: Lãng phí thực phẩm**  
    Đồ ăn trong tủ lạnh hay bị bỏ quên, hết hạn mà không biết cách phối hợp chế biến.
  - **Hộp 3: Bất tiện khi nấu ăn**  
    Mạng Internet trong gian bếp chập chờn, nhiều ứng dụng hiện nay dày đặc quảng cáo che khuất công thức.

### SLIDE 3: MỤC TIÊU & GIẢI PHÁP ĐỘT PHÁ
* **Tiêu đề:** Giải pháp Cooking Note
* **Nội dung chính:**
  - **Offline-First:** Hoạt động ngoại tuyến 100%, tra cứu công thức tức thì ngay cả khi mất mạng.
  - **Tủ lạnh thông minh (Pantry):** Theo dõi số lượng tồn kho, cảnh báo đỏ khi sắp hết đồ ăn.
  - **Trợ lý AI đa nền tảng:** Gợi ý món thông minh theo sở thích và đồ sẵn có trong tủ lạnh.
  - **Trải nghiệm hiện đại:** Giao diện Jetpack Compose Material 3 mượt mà, thao tác 1 chạm.

### SLIDE 4: NỀN TẢNG CÔNG NGHỆ ÁP DỤNG
* **Tiêu đề:** Công nghệ Cốt lõi (Tech Stack)
* **Nội dung liệt kê:**
  - **Ngôn ngữ & Nền tảng:** Kotlin 2.0.21, Android SDK (minSdk 26, compileSdk 35)
  - **Giao diện người dùng:** 100% Jetpack Compose BOM 2024.12.01, Material 3
  - **Cơ sở dữ liệu cục bộ:** Room Database 2.6.1 với KSP (Kotlin Symbol Processing)
  - **Quản lý bất đồng bộ:** Kotlin Coroutines 1.9.0 & StateFlow
  - **Bảo mật & Tùy biến:** EncryptedSharedPreferences (AES-256) & DataStore Preferences
  - **AI Engines:** OpenAI Chat, OpenAI Responses, Anthropic Claude, Google Gemini & Rule-based Offline

### SLIDE 5: KIẾN TRÚC TỔNG THỂ (MVVM + UDF)
* **Tiêu đề:** Kiến trúc Phần mềm: MVVM + Unidirectional Data Flow
* **Bố cục 3 tầng:**
  - **1. Presentation Layer:** Composable Screens (Stateless) + 10 ViewModels độc lập
  - **2. Domain / Repository Layer:** CookbookRepository (Single Source of Truth)
  - **3. Data Layer:** Room Database (10 Entities, 9 DAOs) & Remote AI Web Services
* **Nguyên tắc vàng:** UI chỉ nhận State để vẽ và gửi Event ngược lên ViewModel, tuyệt đối không gọi Database trực tiếp.

### SLIDE 6: TẦNG TRÌNH DIỄN (PRESENTATION LAYER)
* **Tiêu đề:** Thiết kế Tầng Trình diễn & Quản lý Trạng thái
* **Nội dung chính:**
  - **10 ViewModels chuyên biệt:** Quản lý độc lập từng màn hình (Home, Library, Detail, Pantry, AI, ...)
  - **AppViewModelFactory:** Khởi tạo tập trung theo mô hình Manual Dependency Injection
  - **Mô hình hóa State chuẩn mực:** Mọi màn hình đều có 4 trạng thái rõ ràng (Loading, Content, Empty, Error)
  - **Thành phần dùng chung:** Thẻ RecipeCard, TopAppBar, NavigationBar đồng bộ thiết kế

### SLIDE 7: TRẢI NGHIỆM NGƯỜI DÙNG & MÀN HÌNH CHÍNH
* **Tiêu đề:** Hệ thống Màn hình Chức năng Đa dạng
* **Nội dung 4 điểm nhấn:**
  - **Trang chủ (Home):** Gợi ý món ăn theo buổi trong ngày (Sáng, Trưa, Tối)
  - **Thư viện (Library):** Phân loại theo danh mục (Món mặn, Canh, Ăn chay, Tráng miệng)
  - **Chi tiết món (Detail):** Xem định lượng nguyên liệu, hướng dẫn từng bước, 1 chạm đánh dấu "Đã nấu"
  - **Tìm kiếm tức thì (Search):** Áp dụng kỹ thuật Debounce 300ms, tìm siêu tốc theo tên & nguyên liệu

### SLIDE 8: PHÂN HỆ TRỢ LÝ AI ĐA NỀN TẢNG
* **Tiêu đề:** Phân hệ Trợ lý Nấu ăn AI Thông minh
* **Nội dung chính:**
  - **Đa nhà cung cấp:** Hỗ trợ linh hoạt OpenAI GPT-4o, Anthropic Claude 3.5, Google Gemini 1.5
  - **Bơm ngữ cảnh tự động (Context Injection):** Tự động nạp danh sách đồ trong tủ lạnh và món yêu thích vào câu hỏi
  - **Thuật toán Offline Fallback (RuleBasedAi):** Khi mất mạng, thuật toán tự phân tích từ khóa ("nhanh", "cay", "chay") để gợi ý món nội bộ
  - **Bảo mật tối đa:** API Key được mã hóa AES-256 qua Android Keystore, không bao giờ lộ trên bộ nhớ

### SLIDE 9: QUẢN LÝ TỦ LẠNH & NGUYÊN LIỆU (PANTRY)
* **Tiêu đề:** Quản lý Kho Tủ lạnh & Cảnh báo Tồn kho
* **Nội dung chính:**
  - **Kiểm kê trực quan:** Theo dõi số lượng, đơn vị tính (g, quả, gói) của từng loại thực phẩm
  - **Cảnh báo thông minh:** Tự động gắn nhãn cảnh báo đỏ khi nguyên liệu sắp hết (dưới ngưỡng tối thiểu)
  - **Gợi ý nấu ăn từ tủ lạnh:** Kết hợp nguyên liệu có sẵn để đề xuất món có thể nấu ngay
  - **Tối ưu chi tiêu:** Giúp người nội trợ lên danh sách đi chợ chính xác, chống lãng phí

### SLIDE 10: TỐI ƯU HIỆU NĂNG & VÒNG ĐỜI (LIFECYCLE)
* **Tiêu đề:** Tối ưu Hiệu năng & An toàn Vòng đời
* **Nội dung so sánh / Kỹ thuật:**
  - **collectAsStateWithLifecycle():** Thay thế hoàn toàn collectAsState() thông thường, tự động dừng lắng nghe khi app vào nền (Background), tiết kiệm pin và bộ nhớ.
  - **Stable UI State:** Tránh hiện tượng Recomposition dư thừa trên Jetpack Compose.
  - **Dispatchers.IO chuyên dụng:** Mọi thao tác Database và sao lưu file chạy trên luồng ngầm riêng biệt, giao diện luôn duy trì 60–120 FPS mượt mà.

### SLIDE 11: CƠ SỞ DỮ LIỆU ROOM (DATA LAYER)
* **Tiêu đề:** Thiết kế Cơ sở Dữ liệu Quan hệ Room
* **Nội dung chính:**
  - **10 Entities:** Recipe, Ingredient, Step, Category, PantryItem, CookHistory, Tag, RecipeTagCrossRef, AiLog, ChatMessage
  - **Ràng buộc toàn vẹn:** Khóa ngoại ForeignKey liên kết chặt chẽ với hành động CASCADE khi xóa món
  - **9 DAOs độc lập:** Truy vấn bất đồng bộ qua Flow và hàm suspend
  - **Seed dữ liệu:** Tự động nạp dữ liệu mẫu phong phú ngay lần đầu khởi chạy ứng dụng

### SLIDE 12: ĐẢM BẢO CHẤT LƯỢNG & KIỂM THỬ TỰ ĐỘNG
* **Tiêu đề:** Chiến lược Kiểm thử Chất lượng Phần mềm
* **Nội dung số liệu ấn tượng:**
  - **100/100 Unit Tests Đạt 100%:** Chạy độc lập hoàn toàn trên JVM máy tính với thời gian chỉ 5,8 giây.
  - **Hạ tầng kiểm thử chuyên nghiệp:** JUnit 4, MockK giả lập Repository, Turbine kiểm tra StateFlow, MainDispatcherRule điều phối coroutines.
  - **Biên dịch APK thành công:** Lệnh assembleDebug tạo ra file app-debug.apk sạch sẽ trong 49 giây, không có cảnh báo lỗi.

### SLIDE 13: TRÌNH DIỄN SẢN PHẨM THỰC TẾ (DEMO)
* **Tiêu đề:** Trình diễn Sản phẩm Thực tế (Live Demo)
* **Các kịch bản demo trực tiếp:**
  1. Thao tác duyệt công thức và lọc theo danh mục
  2. Xem chi tiết món và nhấn đánh dấu "Đã nấu hôm nay"
  3. Thêm nguyên liệu vào Tủ lạnh và kích hoạt cảnh báo sắp hết
  4. Trò chuyện với Trợ lý AI và thử nghiệm chế độ ngoại tuyến (ngắt mạng)
  5. Xuất tệp sao lưu dữ liệu SQLite từ màn hình Cài đặt

### SLIDE 14: ĐÁNH GIÁ & HƯỚNG PHÁT TRIỂN TƯƠNG LAI
* **Tiêu đề:** Đánh giá Kết quả & Hướng Mở rộng
* **Ưu điểm đạt được:**
  - Ứng dụng mượt mà, cấu trúc chuẩn công nghiệp (Clean Architecture + MVVM + UDF).
  - Độ ổn định cao, không có nguy cơ rò rỉ bộ nhớ.
* **Kế hoạch mở rộng (Roadmap):**
  - Tích hợp CameraX & Computer Vision để quét nhận diện trực tiếp thực phẩm trong tủ lạnh.
  - Tính toán hàm lượng Calo và giá trị dinh dưỡng của từng món ăn.
  - Hỗ trợ ra lệnh và đọc hướng dẫn nấu ăn bằng giọng nói rảnh tay.

### SLIDE 15: LỜI CẢM ƠN & PHIÊN HỎI ĐÁP (Q&A)
* **Tiêu đề:** Lời Cảm ơn & Phiên Hỏi Đáp (Q&A)
* **Nội dung:**
  - "Nhóm xin chân thành cảm ơn Thầy ThS. Lê Văn Quân và các bạn đã chú ý theo dõi phần trình bày!"
  - "Rất mong nhận được những nhận xét, đóng góp quý báu từ Thầy và các bạn để sản phẩm ngày càng hoàn thiện hơn."
* **Thông tin liên hệ nhóm:** Lớp TT602-K16LT