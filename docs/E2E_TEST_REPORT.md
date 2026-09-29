# BÁO CÁO KẾT QUẢ KIỂM THỬ E2E TỰ ĐỘNG (ARTEMIS)
**Dự án:** Cooking Note v2.0 (Sổ tay Nấu ăn) · **Package:** `com.cookingnote.app`  
**Môi trường:** Android Emulator (API 28 / Android 9, `emulator-5554`)  
**AI Agent Executor:** ARTEMIS Flash Engine (Model: `cac` qua endpoint `192.168.1.100:20128/v1`)  
**Thời gian thực hiện:** 28/09/2026

---

## 1. Tổng Quan Kết Quả

| Mã | Tên kịch bản | Kết quả | Thời gian | Trạng thái / Ghi chú |
|---|---|:---:|:---:|---|
| **S0** | Smoke & Baseline Seed | **PASS** | 32s | Khởi động sạch, bypass offline → hiển thị đúng 15 công thức, 4 yêu thích, đủ 4 tab đáy. |
| **S1** | Navigation Sweep | **PASS** | 75s | Duyệt thành công cả 4 tab đáy và màn hình Cài đặt, không crash, không lag. |
| **S2** | Recipe CRUD | **PASS** | 120s | Thêm món `Thịt kho tiêu E2E` → sửa khẩu phần 2 thành 4 → xóa qua dialog xác nhận, list cập nhật đúng. |
| **S3** | Search by Name & Ingredient | **PASS** | 92s | Tìm `Cháo` ra Cháo gà; tìm `Gừng` (nguyên liệu) vẫn tìm ra Cháo gà; `zzzzzz` hiện empty state chuẩn. |
| **S4** | Favorite Toggle Flow | **PASS** | 58s | Thích Cháo gà → số đếm Home tăng 4 lên 5; bỏ thích → giảm về 4. Đồng bộ thời gian thực. |
| **S5** | Cook History Logging | **PASS** | 80s | Bấm "Đã nấu hôm nay" ở món Bún chả → Lịch sử nấu xuất hiện bản ghi hôm nay kèm giờ chính xác. |
| **S6** | Pantry & Low-stock Alerts | **PASS** | 65s | Cảnh báo "Sắp hết nguyên liệu" đỏ hiện đúng 2 món (`Bơ lạt`, `Tiêu đen hạt`); thêm & xóa `Nấm rơm E2E` mượt. |
| **S7** | AI Assistant (Rule-based) | **DEFECT** | 60s | Phản hồi tin nhắn nhanh; tuy nhiên bong bóng chat là text tĩnh không có thẻ công thức bấm được và câu trả lời offline chưa bám sát từ khóa `thịt bò`. |
| **S8** | Settings & Backup DB | **DEFECT / PASS** | 45s | Sao lưu cơ sở dữ liệu hoạt động tốt (mở share sheet hệ thống). Phát hiện mục "Chế độ tối" không có switch toggle bấm được. |
| **S9** | Auth Screen Validation | **PASS** | 60s | Để trống email/pass khi bấm Đăng nhập bị chặn bằng banner đỏ; form Đăng ký đầy đủ 3 trường; bấm Bỏ qua vào thẳng Home. |

---

## 2. Chi Tiết Các Lỗi Phát Hiện Được (Defects)

### Defect 1 (UI/UX) — AI Assistant: Bong bóng chat không có thẻ món ăn có thể bấm vào (S7)
- **Mô tả**: Khi trợ lý AI gợi ý món ăn, nội dung chỉ hiển thị dưới dạng văn bản tĩnh (TextView) và kết thúc bằng câu: *"Bạn có thể bấm vào công thức tương ứng trong thư viện để xem chi tiết các bước nấu!"*. Người dùng không thể bấm trực tiếp vào tên món trong bong bóng chat để điều hướng sang màn hình Chi tiết.
- **Vị trí**: `app/src/main/java/com/cookingnote/app/ui/screens/AiScreen.kt`.

### Defect 2 (AI Logic) — AI Assistant: Phản hồi RuleBasedAi khi offline chưa khớp từ khóa (S7)
- **Mô tả**: Khi người dùng hỏi *"thịt bò"*, hệ thống fallback rule-based trả về danh sách ngẫu nhiên gồm Cà phê muối, Bánh flan, Súp nấm hạt sen (không có món nào chứa thịt bò).
- **Vị trí**: `app/src/main/java/com/cookingnote/app/ai/RuleBasedAi.kt`.

### Defect 3 (UI) — Settings: Mục "Chế độ tối" là text tĩnh, thiếu nút Toggle (S8)
- **Mô tả**: Dưới mục "Giao diện & Hiển thị", hàng "Chế độ tối" chỉ có nhãn và subtitle *"Tự động theo hệ thống thiết bị"*, không có Switch hay nút bật/tắt nào để người dùng chủ động chọn chế độ tối.
- **Vị trí**: `app/src/main/java/com/cookingnote/app/ui/screens/SettingsScreen.kt`.

### Defect 4 (UX) — Chi Tiết Món Ăn: Nút "Đã nấu hôm nay" thiếu phản hồi trực quan (S5)
- **Mô tả**: Sau khi bấm nút "Đã nấu hôm nay", dữ liệu đã được ghi vào Room database thành công, nhưng trên màn hình không hiện Toast/Snackbar hay đổi màu nút ngay lập tức để người dùng biết thao tác đã thành công.
- **Vị trí**: `app/src/main/java/com/cookingnote/app/ui/screens/DetailScreen.kt`.

---

## 3. Video & Telemetry Minh Chứng
Toàn bộ video ghi hình màn hình (`recording.mp4`) và telemetry chi tiết của từng bước đã được ARTEMIS tự động lưu trữ tại:
`C:\Users\ADMIN\Documents\e67\artemis\traces\`
Có thể xem lại trực tiếp trên Web Dashboard tại **http://localhost:8000**.
