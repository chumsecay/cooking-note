# KỊCH BẢN THUYẾT TRÌNH BẢO VỆ ĐỒ ÁN — COOKING NOTE
## (Thời lượng chuẩn: 10 phút | Khớp chính xác 16 Slide theo 5 Giai đoạn)

> **Tài liệu trình chiếu tham chiếu:** `docs/THUYET_TRINH_COOKING_NOTE.pptx` (16 slides, tỷ lệ 16:9).  
> **Quy ước ký hiệu:**  
> • `[SLIDE n]`: Điểm chuyển trang slide tương ứng trên màn hình máy chiếu.  
> • `[CHUYỂN LƯỢT]`: Bàn giao quyền phát biểu giữa các thành viên.  
> • **Phương án phân vai:** Hỗ trợ linh hoạt cả **Nhóm 4 sinh viên** hoặc **2 đại diện báo cáo chính** (Vũ Đình Đạo & Lê Đức Trọng).

---

### BẢNG PHÂN BỔ THỜI GIAN & PHÂN VAI (TỔNG THỜI GIAN: 10 PHÚT)

| Giai đoạn | Slide | Nội dung chính | Thời lượng | Người trình bày (Nhóm 4) | Đại diện (Nhóm 2) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **GĐ 1: Đặt vấn đề** | [SLIDE 1] – [SLIDE 3] | Bìa đề tài, Lý do chọn đề tài, Mục tiêu & Phạm vi | 2 phút (0:00 – 2:00) | **SV1: Vũ Đình Đạo** | **SV1: Vũ Đình Đạo** |
| **GĐ 2: Phân tích & Thiết kế** | [SLIDE 4] – [SLIDE 6] | 5 Phân hệ, Phi chức năng & Bảo mật, CSDL Room 10 bảng | 2 phút (2:00 – 4:00) | **SV2: Ngô Gia Bảo** | **SV1: Vũ Đình Đạo** |
| **GĐ 3: Cài đặt & Triển khai** | [SLIDE 7] – [SLIDE 8] | Kiến trúc MVVM + UDF, Tech Stack chuẩn | 1 phút 20s (4:00 – 5:20) | **SV3: Nguyễn Tiến Dũng** | **SV2: Lê Đức Trọng** |
| **GĐ 4: Demo sản phẩm** | [SLIDE 9] – [SLIDE 12] | Demo Home, Detail, Pantry & Search, AI & Settings | 2 phút 40s (5:20 – 8:00) | **SV4: Lê Đức Trọng** | **SV2: Lê Đức Trọng** |
| **GĐ 5: Kết luận & Phát triển** | [SLIDE 13] – [SLIDE 16] | 135/135 Tests, Đánh giá, Hướng tương lai, Q&A | 2 phút (8:00 – 10:00) | **SV1 & SV4** | **SV2: Lê Đức Trọng** |

---

## NỘI DUNG CHI TIẾT TỪNG SLIDE

### GIAI ĐOẠN 1: ĐẶT VẤN ĐỀ (0:00 – 2:00)
*(Người trình bày: **Vũ Đình Đạo** — Nhóm trưởng)*

#### [SLIDE 1] — Bìa đề tài & Giới thiệu thành viên (0:00 – 0:40)
> *"Kính thưa quý thầy cô trong Hội đồng chấm đồ án tốt nghiệp, kính thưa ThS. Lê Văn Quân cùng toàn thể các bạn sinh viên.  
> Em tên là **Vũ Đình Đạo**, đại diện cho nhóm sinh viên lớp **TT602-K16LT** gồm 4 thành viên: em Vũ Đình Đạo (Nhóm trưởng), bạn Ngô Gia Bảo, bạn Nguyễn Tiến Dũng và bạn Lê Đức Trọng.  
> Hôm nay, nhóm chúng em xin được báo cáo đề tài Đồ án Phát triển Ứng dụng Di động: **Cooking Note — Sổ tay nấu ăn thông minh**. Đây là ứng dụng Android hiện đại hỗ trợ người nội trợ ghi chép công thức, quản lý tủ lạnh thực phẩm và tích hợp trợ lý AI gợi ý món ăn theo thời gian thực."*

#### [SLIDE 2] — Lý do chọn đề tài & Thách thức thực tiễn (0:40 – 1:20)
> *"Thưa thầy cô, xuất phát điểm của đề tài đến từ 4 thách thức rất lớn trong cuộc sống hàng ngày:  
> 1. **Áp lực thực đơn:** Câu hỏi «Hôm nay ăn gì?» luôn làm mất nhiều thời gian của người nấu nướng gia đình.  
> 2. **Lãng phí thực phẩm:** Đồ mua về tích trữ trong tủ lạnh thường xuyên bị lãng quên dẫn tới quá hạn sử dụng.  
> 3. **Môi trường bếp mất kết nối:** Không gian bếp gia đình hoặc tầng hầm thường có sóng Wi-Fi/4G chập chờn, khiến các ứng dụng phụ thuộc đám mây bị treo gián đoạn.  
> 4. **Trải nghiệm ứng dụng hiện hành:** Các app ẩm thực trên thị trường chứa quá nhiều quảng cáo video phiền toái và thiếu tính năng ghi chú khẩu vị gia đình.  
> **Giải pháp của Cooking Note:** Một ứng dụng theo đuổi triết lý **100% Offline-First**, lưu trữ cục bộ bằng SQLite Room, tích hợp quản lý Tủ lạnh cảnh báo tồn kho và cơ chế AI dự phòng `RuleBasedAi` vẫn hoạt động chuẩn xác ngay cả khi không có mạng."*

#### [SLIDE 3] — Mục tiêu nghiên cứu & Phạm vi sản phẩm (1:20 – 2:00)
> *"Nhóm xác định rõ 3 mục tiêu trọng tâm:  
> • **Về chức năng:** Hoàn thiện trọn vẹn CRUD công thức, phân loại danh mục, quản lý kho tủ lạnh (Pantry) gắn nhãn cảnh báo đỏ, tìm kiếm tức thì và chatbot AI ẩm thực nạp ngữ cảnh thực tế.  
> • **Về kỹ thuật:** Xây dựng trên 100% Jetpack Compose Material 3 hiện đại, tuân thủ mô hình MVVM và luồng dữ liệu một chiều (UDF), bảo mật khóa API bằng Keystore phần cứng AES-256-GCM, và kiểm thử tự động đạt tỷ lệ 135/135 Unit Tests vượt qua 100% trên JVM.  
> • **Về phạm vi:** Ứng dụng hỗ trợ các thiết bị Android từ phiên bản 8.0 đến Android 15 (API 35), dữ liệu lưu trữ bền vững tại thiết bị kèm tính năng sao lưu phục hồi an toàn."*  
> **[CHUYỂN LƯỢT: SV1 bàn giao cho SV2 Ngô Gia Bảo (hoặc SV1 tiếp tục nói nếu báo cáo 2 người)]**

---

### GIAI ĐOẠN 2: PHÂN TÍCH & THIẾT KẾ (2:00 – 4:00)
*(Người trình bày: **Ngô Gia Bảo** / hoặc **Vũ Đình Đạo**)*

#### [SLIDE 4] — Phân tích yêu cầu chức năng (5 Phân hệ chính) (2:00 – 2:40)
> *"Tiếp theo, em xin trình bày về phân tích yêu cầu chức năng. Hệ thống Cooking Note được chia thành 5 phân hệ cốt lõi:  
> 1. **Phân hệ Công thức (Recipe):** Cho phép xem chi tiết món ăn kèm thời gian, khẩu phần, định lượng nguyên liệu và các bước nấu đánh số kèm mẹo thực hiện (Tips); hỗ trợ đánh dấu Yêu thích và ghi lại lịch sử đã nấu.  
> 2. **Phân hệ Tủ lạnh (Pantry):** Kiểm kê số lượng, đơn vị thực phẩm trong bếp; tự động tính toán gắn cờ cảnh báo đỏ khi nguyên liệu chạm ngưỡng tối thiểu `lowStockThreshold`.  
> 3. **Phân hệ Trợ lý AI:** Chatbot tư vấn ẩm thực hỗ trợ 4 nhà cung cấp đám mây (OpenAI, Gemini, Claude, DeepSeek) cùng bộ quy tắc luật cục bộ `RuleBasedAi`.  
> 4. **Phân hệ Tìm kiếm & Lọc:** Thanh tìm kiếm đa trường theo tên món, mô tả và thành phần nguyên liệu, tích hợp bộ lọc nhanh theo danh mục ẩm thực.  
> 5. **Phân hệ Cài đặt & Sao lưu:** Cung cấp giao diện cấu hình API key an toàn, nạp cấu hình đám mây từ xa và sao lưu/phục hồi CSDL."*

#### [SLIDE 5] — Yêu cầu phi chức năng, Hiệu năng & Bảo mật (2:40 – 3:20)
> *"Đối với một ứng dụng di động, hiệu năng và bảo mật là yếu tố sống còn:  
> • **Về hiệu năng:** Cooking Note khởi động dưới 1.5 giây và duy trì khung hình ổn định 60fps. Đặc biệt, nhóm cài đặt toán tử Flow **Debounce 300ms** cho thanh tìm kiếm — loại bỏ tình trạng người dùng gõ mỗi ký tự lại quét cơ sở dữ liệu, giúp **giảm hơn 85% số lượt truy vấn đĩa**. Toàn bộ tác vụ Room và Network đều được điều phối bất đồng bộ trên `Dispatchers.IO`, triệt tiêu hoàn toàn hiện tượng nghẽn UI Thread.  
> • **Về bảo mật:** Khóa API của người dùng được mã hóa bằng **EncryptedSharedPreferences (AES-256-GCM)** với MasterKey quản lý bởi phần cứng Android Keystore.  
> • **Về xác thực:** Xác thực đăng nhập/đăng ký được thực hiện an toàn qua REST backend (`api/v1/auth`), client tuyệt đối không lưu trữ hay băm mật khẩu thô.  
> • **Về sao lưu:** Khi xuất file backup cơ sở dữ liệu, hệ thống tự động kích hoạt **checkpoint WAL** để đồng bộ toàn bộ giao dịch ghi gần nhất ra đĩa, chống thất thoát dữ liệu."*

#### [SLIDE 6] — Thiết kế Cơ sở Dữ liệu Room (10 Bảng & 9 DAOs) (3:20 – 4:00)
> *"Trên Slide 6 là sơ đồ CSDL Room Database phiên bản 3 được thiết kế chuẩn mực với 10 bảng thực thể:  
> • Nhóm thực thể cốt lõi gồm: `recipes` (chứa thông tin món ăn), `ingredients` (nguyên liệu), `steps` (các bước thực hiện), `categories` (danh mục) và `pantry_items` (kho thực phẩm tủ lạnh).  
> • Nhóm quan hệ và AI mở rộng gồm: `tags` (nhãn món ăn), `recipe_tag_cross_ref` (bảng liên kết trung gian quan hệ N-N giữa món ăn và nhãn), `cook_history` (nhật ký nấu nướng), `chat_messages` (lịch sử hội thoại AI) và `ai_query_logs` (nhật ký giám sát chất lượng phản hồi AI).  
> • **Điểm sáng kỹ thuật:** Toàn bộ bảng nguyên liệu và các bước đều ràng buộc `ForeignKey` với thuộc tính `ON DELETE CASCADE` — khi xóa công thức, các dữ liệu liên quan sẽ tự động dọn sạch. Đồng thời, các cột tìm kiếm thường xuyên đều được đánh chỉ mục `Index`, đảm bảo truy vấn SQL phản hồi tức thì dưới 20ms."*  
> **[CHUYỂN LƯỢT: Bàn giao cho SV3 Nguyễn Tiến Dũng / hoặc SV2 Lê Đức Trọng]**

---

### GIAI ĐOẠN 3: CÀI ĐẶT & TRIỂN KHAI (4:00 – 5:20)
*(Người trình bày: **Nguyễn Tiến Dũng** / hoặc **Lê Đức Trọng**)*

#### [SLIDE 7] — Kiến trúc Phần mềm MVVM & Luồng Dữ liệu Một Chiều UDF (4:00 – 4:45)
> *"Kính thưa thầy cô, để đảm bảo mã nguồn dễ bảo trì và mở rộng, nhóm áp dụng kiến trúc chuẩn MVVM kết hợp Unidirectional Data Flow (UDF):  
> • **Tầng UI:** Xây dựng bằng 100% Jetpack Compose theo mô hình Stateless Content. Màn hình chỉ nhận một trạng thái bất biến `UiState` và phát sự kiện `onAction` ngược lên ViewModel.  
> • **Tầng ViewModel:** Tiếp nhận sự kiện, thực thi nghiệp vụ trong `viewModelScope` và phát dữ liệu ra ngoài bằng `StateFlow` với cấu hình an toàn `SharingStarted.WhileSubscribed(5000)`. UI thu thập state qua `collectAsStateWithLifecycle()` giúp ngăn chặn triệt để hiện tượng rò rỉ bộ nhớ khi ứng dụng chuyển xuống nền.  
> • **Tầng Data:** `CookbookRepository` đóng vai trò nguồn chân lý duy nhất (Single Source of Truth), điều phối giữa 9 Room DAOs và AI Subsystem.  
> • **Manual Dependency Injection:** Thay vì sử dụng Hilt hay Koin làm tăng dung lượng và thời gian biên dịch, nhóm sử dụng `AppContainer` và `AppViewModelFactory` khởi tạo tập trung. Cách tiếp cận gọn nhẹ này giúp 100% lớp Unit Test có thể chạy trực tiếp trên máy ảo JVM với tốc độ cao mà không cần giả lập Android."*

#### [SLIDE 8] — Công nghệ & Thư viện Sử dụng (Tech Stack) (4:45 – 5:20)
> *"Về hệ sinh thái công nghệ:  
> • **Ngôn ngữ:** Kotlin 2.0.21 với Coroutines 1.9.0, Target SDK 35 (Android 15 mới nhất).  
> • **Giao diện:** Jetpack Compose BOM 2024.12.01, Material 3 Design hỗ trợ Light & Dark theme mượt mà; Navigation Compose điều hướng bằng sealed class Route an toàn tham số.  
> • **Cơ sở dữ liệu:** Room 2.6.1 kết hợp bộ xử lý KSP kiểm tra cú pháp SQL ngay trong thời gian biên dịch (compile-time).  
> • **Mạng & AI:** OkHttp 4.12.0 kết hợp Moshi 1.15.1 gọi REST API và kết nối 4 dịch vụ AI đám mây; Material Icons Extended tối ưu hiển thị đồ họa vector không tốn I/O mạng.  
> • **Kiểm thử:** Bộ công cụ JUnit 4, MockK, Turbine và thư viện chuẩn `java.util.Base64` giúp kiểm thử độc lập hoàn toàn trên máy ảo JVM."*  
> **[CHUYỂN LƯỢT: Bàn giao cho SV4 Lê Đức Trọng trình bày phần Demo thực tế]**

---

### GIAI ĐOẠN 4: DEMO SẢN PHẨM THỰC TẾ (5:20 – 8:00)
*(Người trình bày: **Lê Đức Trọng** — Phụ trách Demo & Tính năng đặc sắc)*

#### [SLIDE 9] — Demo Trang chủ & Thư viện công thức (5:20 – 6:00)
> *"Sau đây, em xin phép kính mời thầy cô cùng theo dõi trải nghiệm thực tế của ứng dụng Cooking Note được trích xuất từ thiết bị thật:  
> • **Tại màn hình Trang chủ (bên trái):** Giao diện chào người dùng theo buổi trong ngày (Sáng, Trưa, Tối). Phần banner hiển thị gợi ý món ăn tiêu biểu hôm nay, danh mục phân loại ẩm thực sinh động và các lối tắt nhanh dẫn thẳng tới trợ lý AI và kho Tủ lạnh.  
> • **Tại Thư viện công thức (bên phải):** Toàn bộ món ăn được hiển thị dạng lưới 2 cột hiện đại. Mỗi thẻ món ăn cung cấp trực quan: ảnh minh họa, tên món, thời gian chế biến và độ khó. Người dùng có thể lọc nhanh theo danh mục như Món chính, Món canh, Đồ ăn vặt, hoặc chạm vào biểu tượng trái tim để đánh dấu yêu thích tức thì."*

#### [SLIDE 10] — Demo Chi tiết Món & Biên tập công thức (6:00 – 6:40)
> *"Tiếp theo là trải nghiệm nấu nướng và sáng tạo công thức:  
> • **Chi tiết món ăn (bên trái):** Hình ảnh Hero tràn viền sống động, thông số khẩu phần và thời gian rõ ràng. Danh sách nguyên liệu được định lượng chuẩn xác; các bước nấu nướng được đánh số thứ tự rõ ràng kèm các mẹo vặt hữu ích (Tips). Nút «Bắt đầu nấu» sẽ tự động ghi nhận thời gian vào bảng lịch sử nấu nướng `cook_history`.  
> • **Trình tạo mới & Biên tập (bên phải):** Nhóm thiết kế một trình editor dùng chung cho cả tạo mới và sửa đổi. Hệ thống có cơ chế validation đầu vào chặt chẽ — ngăn chặn người dùng lưu dữ liệu rỗng; cho phép thêm bớt nguyên liệu và các bước hướng dẫn động một cách linh hoạt và lưu trữ nguyên tử vào CSDL."*

#### [SLIDE 11] — Demo Quản lý Tủ lạnh & Tìm kiếm Debounce (6:40 – 7:20)
> *"Đây là một trong những tính năng thực tế được người dùng đánh giá cao nhất:  
> • **Quản lý Tủ lạnh (bên trái):** Giúp người nội trợ kiểm soát chính xác từng nguyên liệu trong bếp. Khi số lượng nguyên liệu giảm xuống dưới ngưỡng `lowStockThreshold`, hệ thống sẽ tự động gắn huy hiệu màu đỏ cảnh báo «Sắp hết». Nút «Hôm nay nấu gì?» tại đây sẽ lập tức chuyển danh sách thực phẩm đang có sang cho trợ lý AI.  
> • **Tìm kiếm phản ứng tức thì (bên phải):** Nhờ cơ chế **Debounce 300ms**, người dùng có thể tìm kiếm theo tên món, mô tả hoặc nguyên liệu thành phần một cách siêu mượt mà, không hề có độ trễ giật lag. Khi không có kết quả, màn hình hiển thị trạng thái Empty State thân thiện kèm các món ăn gợi ý."*

#### [SLIDE 12] — Demo Trợ lý Ẩm thực AI & Cài đặt Bảo mật (7:20 – 8:00)
> *"Điểm nhấn công nghệ đặc biệt của Cooking Note là Trợ lý AI và Bảo mật:  
> • **Trợ lý AI nạp ngữ cảnh (bên trái):** Khi người dùng mở chat hỏi «Tối nay ăn gì?», hệ thống không gửi một câu hỏi trống rỗng, mà **tự động nạp danh sách đồ trong tủ lạnh, món yêu thích và khẩu vị gia đình vào System Prompt**. AI sẽ đưa ra câu trả lời thực tế: món nào nấu được ngay, món nào cần mua thêm. Nếu mất mạng hoặc API hết hạn, thuật toán `RuleBasedAi` ngay lập tức kích hoạt, đưa ra gợi ý nấu ăn từ rule cục bộ mà không báo lỗi crash app.  
> • **Cài đặt & Bảo mật (bên phải):** Người dùng có thể linh hoạt chuyển đổi giữa 4 nhà cung cấp AI hàng đầu: OpenAI, Gemini, Claude và DeepSeek; nhập API key an toàn vào phần cứng Android Keystore, tải cấu hình đám mây từ xa qua Remote Config và sao lưu CSDL một chạm an toàn."*  
> **[CHUYỂN LƯỢT: Bàn giao lại cho SV1 hoặc SV4 trình bày phần Tổng kết & Kiểm thử]**

---

### GIAI ĐOẠN 5: KẾT LUẬN & HƯỚNG PHÁT TRIỂN (8:00 – 10:00)
*(Người trình bày: **Vũ Đình Đạo** / **Lê Đức Trọng**)*

#### [SLIDE 13] — Đảm bảo Chất lượng & 135/135 Unit Tests tự động (8:00 – 8:45)
> *"Để minh chứng cho chất lượng phần mềm, nhóm không chỉ dừng lại ở kiểm thử thủ công mà đã xây dựng hệ thống kiểm thử tự động toàn diện:  
> • **Con số biết nói:** Đạt tỷ lệ **135/135 bài kiểm thử đơn vị vượt qua 100% tuyệt đối** trên **23 tệp test độc lập**.  
> • **Chiến lược kiểm thử đa tầng:** Sử dụng Turbine và MockK để kiểm thử phản ứng StateFlow ở tầng ViewModel; dùng Room In-Memory Database kiểm thử toàn vẹn giao dịch ở tầng Dữ liệu; và kiểm thử chặt chẽ bộ luật ngoại tuyến của `RuleBasedAi`.  
> • **Kiểm tra kiến trúc & Adversarial Tests:** Hai bộ test đặc biệt là `Milestone3ArchitectureConformanceTest` và `Milestone3ChallengerAdversarialTest` tự động quét mã nguồn bằng regex để đảm bảo 100% màn hình tuân thủ cấu trúc Stateless Content và thử thách hệ thống với các trường hợp dữ liệu dị thường.  
> • **Tối ưu JVM:** Toàn bộ 135 bài test chạy trực tiếp trên JVM trong vài giây mà không cần khởi động máy ảo Android giả lập nặng nề."*

#### [SLIDE 14] — Tổng kết Kết quả Đạt được & Hạn chế Thực tế (8:45 – 9:15)
> *"Nhìn lại quá trình nghiên cứu và phát triển:  
> • **Về thành tựu:** Nhóm đã hoàn thành 100% các mục tiêu đề ra ban đầu: Xây dựng ứng dụng Cooking Note hiện đại với Jetpack Compose M3; CSDL Room 10 bảng ngoại tuyến ổn định; trợ lý AI đa nhà cung cấp có fallback; bảo mật Keystore AES-256-GCM và bộ kiểm thử tự động 135 tests.  
> • **Về hạn chế thực tế:** Nhóm thẳng thắn nhìn nhận 3 điểm còn hạn chế:  
> 1. Chưa xây dựng được bộ kiểm thử tự động trên giao diện thật (Instrumented UI Test) mà vẫn phụ thuộc vào đo đạc thủ công.  
> 2. Module CameraX chưa được đấu nối trực tiếp vào nút chụp ảnh của UI biên tập món ăn (hiện vẫn chọn ảnh qua thư viện máy hoặc URL).  
> 3. Chưa có cơ chế đồng bộ đám mây thời gian thực (Real-time Cloud Sync) giữa nhiều thiết bị mà đang thông qua file sao lưu CSDL cục bộ."*

#### [SLIDE 15] — Kế hoạch & Hướng Phát triển Trong Tương lai (9:15 – 9:45)
> *"Dựa trên những hạn chế đã xác định, nhóm đề xuất lộ trình nâng cấp Cooking Note:  
> • **Ngắn hạn (1–3 tháng):** Tích hợp thị giác máy tính **AI Camera Vision** — cho phép chụp hóa đơn đi chợ để tự động nhập kho thực phẩm Tủ lạnh và quét trực tiếp rau củ quả để nhận diện món ăn.  
> • **Trung hạn (3–6 tháng):** Xây dựng **Trợ lý giọng nói rảnh tay (Hands-Free Voice)** — giúp người nội trợ ra lệnh chuyển bước nấu tiếp theo bằng giọng nói mà không cần chạm tay dính dầu mỡ vào màn hình điện thoại.  
> • **Dài hạn (6–12 tháng):** Hoàn thiện hệ thống **Real-time Cloud Sync** đồng bộ tủ lạnh dùng chung cho cả gia đình và module **Phân tích dinh dưỡng** tự động tính toán hàm lượng Calo, Protein, Carb cho mỗi bữa ăn."*

#### [SLIDE 16] — Lời cảm ơn & Phiên giải đáp thắc mắc Q&A (9:45 – 10:00)
> *"Kính thưa quý thầy cô, trên đây là toàn bộ nội dung báo cáo đồ án môn học Phát triển Ứng dụng Di động của nhóm sinh viên chúng em.  
> Chúng em xin bày tỏ lòng biết ơn chân thành và sâu sắc nhất tới **ThS. Lê Văn Quân** — người thầy đã luôn nhiệt tình hướng dẫn, chỉ bảo và định hướng chuyên môn để đồ án Cooking Note đạt được chất lượng hoàn thiện như ngày hôm nay.  
> Chúng em xin trân trọng cảm ơn quý thầy cô trong Hội đồng đã dành thời gian theo dõi. Nhóm sinh viên đã sẵn sàng đón nhận các câu hỏi, ý kiến đóng góp và phản biện chuyên môn từ Hội đồng.  
> **Xin trân trọng cảm ơn quý thầy cô!**"*

---

## BỘ CÂU HỎI PHẢN BIỆN DỰ PHÒNG CHO HỘI ĐỒNG (Q&A)

### Câu 1: "Cơ chế bảo mật khóa API và thông tin người dùng trong ứng dụng được hiện thực như thế nào?"
> **Trả lời:**  
> *"Dạ thưa thầy cô, ứng dụng Cooking Note bảo vệ an toàn thông tin theo 2 cấp độ:  
> 1. **Khóa bí mật API:** Được lưu trữ trong `EncryptedSharedPreferences` sử dụng thuật toán mã hóa đối xứng **AES-256-GCM**, với khóa mã hóa chính (MasterKey) được bảo vệ bằng phần cứng **Android Keystore**. Khóa không bao giờ bị lưu ở dạng plaintext hay commit vào mã nguồn.  
> 2. **Xác thực phiên làm việc:** Đăng ký và đăng nhập được chuyển tiếp qua REST backend chuẩn (`api/v1/auth`). Toàn bộ việc xử lý xác thực và kiểm tra mật khẩu diễn ra hoàn toàn **phía máy chủ**. Ứng dụng client tuyệt đối không lưu mật khẩu thô và không tự băm mật khẩu tại client."*

### Câu 2: "Tại sao nhóm chọn Room Database thay vì SQLite thuần hay Realm/ObjectBox?"
> **Trả lời:**  
> *"Dạ thưa thầy cô, nhóm chọn Room Database 2.6.1 vì 3 lý do cốt lõi:  
> 1. **An toàn lúc biên dịch (Compile-time Verification):** Nhờ bộ tiền xử lý KSP, mọi câu lệnh truy vấn SQL đều được kiểm tra tính đúng đắn ngay lúc build. Nếu gõ sai tên cột hay sai kiểu dữ liệu, trình biên dịch sẽ báo lỗi ngay lập tức thay vì bị crash lúc runtime như SQLite thuần.  
> 2. **Hỗ trợ Coroutines Flow nguyên bản:** Room trả về kiểu `Flow<List<T>>`, giúp tầng UI của Jetpack Compose tự động cập nhật ngay lập tức khi cơ sở dữ liệu có thay đổi mà không cần viết các hàm callback thủ công.  
> 3. **Quản lý Migration và Entity chuẩn Google:** Room hỗ trợ định nghĩa Foreign Keys với `CASCADE` và đánh chỉ mục `Index` rất rõ ràng, đồng thời tương thích hoàn hảo với kiến trúc Android Jetpack chính thống."*

### Câu 3: "Khi thiết bị mất kết nối Internet, trợ lý AI của ứng dụng sẽ xử lý ra sao?"
> **Trả lời:**  
> *"Dạ thưa thầy cô, Cooking Note được thiết kế theo triết lý **Offline-First**. Khi người dùng mất kết nối Internet hoặc chưa cấu hình API Key:  
> Hệ thống sẽ kích hoạt bộ quy tắc ngoại tuyến **`RuleBasedAi`** chạy cục bộ 100% trên thiết bị. Thuật toán này phân tích các nguyên liệu hiện có trong bảng `pantry_items`, đối sánh với cơ sở dữ liệu các món ăn đã lưu trong Room và đưa ra gợi ý các món có thể nấu được ngay.  
> Nhờ đó, người dùng chỉ bị giới hạn việc chat ngôn ngữ tự nhiên thông minh, nhưng **tính năng gợi ý thực đơn cốt lõi không bao giờ bị gián đoạn hay crash ứng dụng**."*

### Câu 4: "Kiến trúc UDF (Unidirectional Data Flow) đem lại lợi ích gì so với cách code truyền thống?"
> **Trả lời:**  
> *"Dạ thưa thầy cô, UDF giúp luồng dữ liệu chỉ đi theo một chiều duy nhất: **Sự kiện từ UI đi lên ViewModel, và Trạng thái UiState từ ViewModel phát xuống UI**.  
> Lợi ích lớn nhất là:  
> • **Dễ debug:** Giao diện chỉ là một hàm vẽ lại trạng thái `UiState` tại thời điểm đó (State in, UI out). Khi có lỗi hiển thị, nhóm chỉ cần kiểm tra xem `UiState` đang mang giá trị gì mà không cần mò tìm trạng thái phân tán ở nhiều biến nội bộ.  
> • **Phân tách trách nhiệm tuyệt đối:** Tầng Composable không được phép gọi trực tiếp Repository hay DAO; ViewModel không giữ bất kỳ tham chiếu nào tới Android Context, giúp việc viết Unit Test cho ViewModel trên JVM trở nên cực kỳ đơn giản và độc lập."*

### Câu 5: "Kỹ thuật Debounce 300ms trong ô tìm kiếm hoạt động như thế nào?"
> **Trả lời:**  
> *"Dạ thưa thầy cô, trong ô tìm kiếm, mỗi khi người dùng gõ phím, một sự kiện đổi text sẽ được phát ra. Nếu truy vấn DB ngay lập tức, khi người dùng gõ từ «Thịt bò xào», hệ thống sẽ phải thực hiện hàng chục truy vấn SQL liên tiếp gây giật lag (jank).  
> Nhóm đã sử dụng toán tử Flow `debounce(300L)`. Toán tử này có cơ chế: Sau khi người dùng gõ ký tự cuối cùng và dừng lại quá **300 mili-giây**, luồng dữ liệu mới kích hoạt câu truy vấn vào Room Database. Kỹ thuật này đã giúp **cắt giảm hơn 85% số lượt đọc đĩa không cần thiết**, giúp trải nghiệm tìm kiếm luôn đạt chuẩn 60fps mượt mà."*

---

### GHI NHỚ ĐẶC BIỆT KHI BẢO VỆ (KHÔNG NÓI SAI SỰ THẬT KỸ THUẬT)
- ✅ **135/135 Unit Tests / 23 files** — đã kiểm chứng thật, pass 100% trên JVM.
- ✅ **Room Database v3 gồm 10 bảng thực thể & 9 DAOs** — có `ON DELETE CASCADE` và `Index`.
- ✅ **Debounce tìm kiếm:** chính xác là **300ms**.
- ✅ **Bảo mật:** `EncryptedSharedPreferences` AES-256-GCM với MasterKey từ Android Keystore.
- ✅ **Xác thực:** Backend REST `api/v1/auth`, xác thực và xử lý mật khẩu phía server.
- ❌ **TUYỆT ĐỐI KHÔNG NÓI:** «Client tự băm mật khẩu SHA-256» (xác thực nằm ở server).
- ❌ **TUYỆT ĐỐI KHÔNG NÓI:** «Đã có benchmark tự động hay test UI tự động» (đo hiệu năng đo thủ công trên máy thật; hạn chế là chưa có instrumented test).
