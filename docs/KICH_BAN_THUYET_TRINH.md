# KỊCH BẢN THUYẾT TRÌNH BÀI TẬP LỚN
## ĐỀ TÀI: XÂY DỰNG ỨNG DỤNG SỔ TAY NẤU ĂN THÔNG MINH (COOKING NOTE)
**Môn học:** Phát triển Ứng dụng Di động  
**GVHD:** ThS. Lê Văn Quân  
**Nhóm sinh viên thực hiện (Lớp TT602-K16LT):**
1. **Vũ Đình Đạo (Nhóm trưởng)**: Mở đầu, Kiến trúc MVVM + UDF, Database Room
2. **Ngô Gia Bảo**: Trình bày tầng Presentation, ViewModels và tối ưu Lifecycle
3. **Nguyễn Tiến Dũng**: Trình bày giao diện Jetpack Compose và demo các màn hình chính
4. **Lê Đức Trọng**: Phân hệ Trợ lý AI, Chiến lược Kiểm thử (100 Tests) và Kết luận

---

## PHẦN 1: MỞ ĐẦU & TỔNG QUAN (Vũ Đình Đạo)
**Thời lượng dự kiến:** 3 phút | **Slide:** 1 - 4

* **Lời thoại:**
> "Kính thưa Thầy và các bạn, em là Vũ Đình Đạo, đại diện nhóm sinh viên lớp TT602-K16LT. Hôm nay, nhóm em rất vinh dự được trình bày báo cáo bài tập lớn môn Phát triển Ứng dụng Di động với đề tài: **'Xây dựng ứng dụng Sổ tay Công thức Nấu ăn Thông minh (Cooking Note)'** dưới sự hướng dẫn tận tình của thầy Lê Văn Quân.
>
> *(Chuyển sang Slide 2)*
> Thưa Thầy và các bạn, xuất phát từ thực tế cuộc sống, câu hỏi 'Hôm nay ăn gì?' luôn là nỗi đau đầu của người nội trợ và các bạn trẻ sống tự lập. Thêm vào đó, thực phẩm trong tủ lạnh thường hay bị lãng quên dẫn tới quá hạn gây lãng phí. Khi vào bếp tra cứu công thức, mạng Internet hay chập chờn, trong khi các ứng dụng hiện có trên thị trường thì dày đặc quảng cáo và thiếu tính kết nối với thực phẩm sẵn có.
>
> *(Chuyển sang Slide 3 & 4)*
> Chính vì vậy, Cooking Note ra đời với triết lý: **Offline-first (luôn sẵn sàng ngoại tuyến), Tối ưu hoá thao tác (100% Jetpack Compose Material 3), và Tích hợp linh hoạt Trợ lý Trí tuệ nhân tạo (Multi-Provider AI)**. Hệ thống được xây dựng trên nền tảng Kotlin 2.0, Room Database 2.6.1 KSP và hỗ trợ mọi thiết bị Android từ API 26 đến API 35."

---

## PHẦN 2: KIẾN TRÚC HỆ THỐNG & CƠ SỞ DỮ LIỆU (Vũ Đình Đạo & Ngô Gia Bảo)
**Thời lượng dự kiến:** 3 phút | **Slide:** 5, 10, 11

* **Lời thoại (Vũ Đình Đạo):**
> *(Chuyển sang Slide 5 & 11)*
> "Về mặt kiến trúc, dự án Cooking Note tuân thủ nghiêm ngặt mô hình **MVVM kết hợp Unidirectional Data Flow (UDF)**. 
> Toàn bộ dữ liệu được quản lý tập trung qua `CookbookRepository` đóng vai trò Single Source of Truth kết nối với Room Database gồm 10 bảng dữ liệu có ràng buộc khóa ngoại CASCADE chặt chẽ: từ công thức, nguyên liệu, các bước nấu, danh mục, kho tủ lạnh, lịch sử đến tin nhắn AI.
> Tiếp theo, bạn Ngô Gia Bảo sẽ trình bày chi tiết về tầng Presentation và cách tối ưu hóa vòng đời ứng dụng."

* **Lời thoại (Ngô Gia Bảo):**
> *(Chuyển sang Slide 5 & 10)*
> "Xin chào Thầy và các bạn, em là Ngô Gia Bảo. Trong dự án này, em phụ trách xây dựng tầng Presentation.
> Điểm đặc biệt của Cooking Note là **triệt tiêu 100% việc gọi Database/Repository trực tiếp trong giao diện Composable**. Thay vào đó, nhóm em xây dựng 10 `ViewModel` độc lập tương ứng với 10 màn hình, được khởi tạo thông qua `AppViewModelFactory`.
> Mọi màn hình đều mô hình hóa UI State rõ ràng thành 4 trạng thái: Loading, Content, Empty và Error. 
> Đặc biệt, để tối ưu pin và hiệu năng, 100% luồng dữ liệu đều được thu thập bằng `collectAsStateWithLifecycle()`, giúp tự động dừng lắng nghe khi app xuống nền (background), loại bỏ hoàn toàn nguy cơ rò rỉ bộ nhớ."

---

## PHẦN 3: GIAO DIỆN & TRẢI NGHIỆM NGƯỜI DÙNG (Nguyễn Tiến Dũng)
**Thời lượng dự kiến:** 4 phút | **Slide:** 6, 7, 9, 13 (Demo)

* **Lời thoại:**
> "Kính thưa Thầy, em là Nguyễn Tiến Dũng, người phụ trách lập trình giao diện người dùng bằng Jetpack Compose Material 3.
> *(Chuyển sang Slide 6, 7, 9)*
> Giao diện Cooking Note mang tông màu cam - kem ấm cúng, đậm chất ẩm thực. Ứng dụng mang đến các trải nghiệm đột phá:
> 1. **Thư viện món ăn:** Duyệt theo thẻ trực quan, lọc nhanh qua thanh cuộn danh mục nằm ngang (Chips).
> 2. **Chi tiết món & Tương tác 1 chạm:** Xem nguyên liệu, các bước làm có đánh số, một chạm để đánh dấu yêu thích hoặc ghi nhận 'Đã nấu hôm nay'.
> 3. **Tủ lạnh thông minh (Pantry):** Hiển thị rõ số lượng thực phẩm trong nhà, tự động gắn nhãn cảnh báo đỏ với các món sắp hết để người dùng kịp thời đi chợ.
> 4. **Tìm kiếm tức thì với Debounce 300ms:** Người dùng gõ đến đâu hệ thống lọc đến đó sau 300ms dừng gõ, vừa mượt mà vừa không làm nóng máy.
> 5. **Sao lưu dữ liệu:** Màn hình Cài đặt cho phép xuất toàn bộ database thành tệp backup chạy ngầm trên luồng IO, chia sẻ dễ dàng qua Zalo hoặc Google Drive."

---

## PHẦN 4: TRỢ LÝ AI, KIỂM THỬ CHẤT LƯỢNG & KẾT LUẬN (Lê Đức Trọng)
**Thời lượng dự kiến:** 4 phút | **Slide:** 8, 12, 14, 15

* **Lời thoại:**
> "Kính thưa Thầy, em là Lê Đức Trọng, phụ trách phân hệ AI và hệ thống kiểm thử tự động.
> *(Chuyển sang Slide 8)*
> Điểm nhấn công nghệ của Cooking Note là phân hệ **Trợ lý AI đa nền tảng**:
> - Ứng dụng có thể kết nối linh hoạt tới 4 chuẩn API: OpenAI Chat, OpenAI Responses, Anthropic Claude và Google Gemini. Khóa API được mã hóa an toàn chuẩn AES-256 qua `EncryptedSharedPreferences`.
> - **Cơ chế nạp ngữ cảnh tự động (Context Injection):** Khi người dùng hỏi 'Tối nay nấu gì?', hệ thống tự động nạp danh sách đồ trong tủ lạnh, 5 món yêu thích và lịch sử nấu gần nhất vào System Prompt để AI tư vấn sát thực tế nhất.
> - **Đặc biệt, cơ chế ngoại tuyến RuleBasedAi:** Khi không có mạng hoặc chưa nhập API Key, thuật toán cục bộ sẽ tự động kích hoạt, phân tích từ khóa 'nhanh', 'chay', 'cay' và gợi ý món ăn ngay lập tức từ thư viện nội bộ.
>
> *(Chuyển sang Slide 12)*
> Về mặt đảm bảo chất lượng phần mềm, nhóm em đã xây dựng một bộ kiểm thử tự động toàn diện:
> - **100/100 Unit Tests Đạt 100% (0 lỗi, 0 bỏ qua)** chạy thuần túy trên JVM thông qua `MainDispatcherRule`, `MockK` và `Turbine`.
> - Dự án biên dịch sạch sẽ ra file APK chuẩn `app-debug.apk` trong 49 giây.
>
> *(Chuyển sang Slide 14 & 15)*
> Trong tương lai, nhóm định hướng sẽ phát triển thêm tính năng nhận diện tủ lạnh qua Camera (CameraX Vision), tính toán hàm lượng Calo dinh dưỡng và hỗ trợ trợ lý giọng nói khi nấu ăn.
> Nhóm chúng em xin chân thành cảm ơn Thầy Lê Văn Quân và các bạn đã chú ý lắng nghe. Rất mong nhận được những góp ý quý báu từ Thầy!"

---

## GỢI Ý CÂU HỎI VẢ PHẢN BIỆN THƯỜNG GẶP (Q&A CHEAT SHEET)

1. **Câu hỏi:** Tại sao lại dùng `collectAsStateWithLifecycle()` thay vì `collectAsState()` thông thường?
   * **Trả lời:** `collectAsState()` thông thường vẫn tiếp tục lắng nghe Flow ngay cả khi ứng dụng bị ẩn xuống nền (Background), gây lãng phí CPU, pin và có thể dẫn đến crash hoặc rò rỉ bộ nhớ. `collectAsStateWithLifecycle()` tự động hủy đăng ký khi Activity/Fragment xuống dưới trạng thái `Lifecycle.State.STARTED` và tự động lắng nghe lại khi người dùng quay lại màn hình.

2. **Câu hỏi:** Tại sao ứng dụng chọn Manual Dependency Injection (`AppContainer`) mà không dùng Hilt hoặc Koin?
   * **Trả lời:** Với quy mô của ứng dụng và yêu cầu kiểm thử độc lập 100% trên JVM (không phụ thuộc Android framework), việc dùng `AppContainer` kết hợp `AppViewModelFactory` giúp dự án có thời gian build nhanh gấp 3 lần (không cần annotation processor KAPT nặng nề của Hilt), kiểm soát hoàn toàn vòng đời đối tượng và dễ dàng truyền mock dependencies trong Unit Tests.

3. **Câu hỏi:** Khóa API của AI được lưu trữ như thế nào để đảm bảo không bị lộ?
   * **Trả lời:** Nhóm sử dụng thư viện `androidx.security:security-crypto` với `EncryptedSharedPreferences`. Khóa được mã hóa bằng thuật toán `AES256_SIV` cho Key và `AES256_GCM` cho Value, thông qua Android Keystore của hệ điều hành, đảm bảo an toàn ngay cả khi thiết bị bị can thiệp file hệ thống.
