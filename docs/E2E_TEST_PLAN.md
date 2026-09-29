# Bộ Kịch Bản Kiểm Thử E2E — Cooking Note v2.0
*(Tối ưu cho kiểm thử tự động với ARTEMIS Agent)*

Bộ test case này bao phủ toàn bộ 11 màn hình và luồng người dùng chính của ứng dụng **Sổ tay Nấu ăn**. Do backend `10.0.2.2:8080` chưa chạy, mọi kịch bản đều dùng luồng bypass **"Dùng ngoại tuyến (Bỏ qua)"** trên màn hình Auth.

---

## Danh Sách Kịch Bản E2E

### S0 — Khởi động & Kiểm tra dữ liệu Seed (Smoke Test)
- **Mục tiêu**: Xác nhận app mở sạch, bypass auth vào được Home, dữ liệu seed tự nạp đủ 15 món và 4 món yêu thích.
- **Tiền điều kiện**: App vừa được cài đặt hoặc vừa chạy `adb shell pm clear com.cookingnote.app`.
- **Thao tác**:
  1. Mở app `com.cookingnote.app`.
  2. Tại màn hình "Tài khoản SmartChef", bấm nút **"Dùng ngoại tuyến (Bỏ qua)"**.
  3. Tại màn hình Trang chủ: quan sát lời chào "Xin chào 👋", thẻ "Công thức" và thẻ "Yêu thích".
- **Kỳ vọng**:
  - Số công thức hiển thị: **15**.
  - Số yêu thích hiển thị: **4**.
  - Mục "Gợi ý hôm nay" có ít nhất 1 món ăn hiển thị.
  - Thanh điều hướng đáy có đủ 4 tab: "Trang chủ", "Thư viện", "AI Gợi ý", "Tủ lạnh".
- **ARTEMIS Task Prompt**:
  ```
  Open the Cooking Note app (com.cookingnote.app). On the welcome/auth screen, tap 'Dùng ngoại tuyến (Bỏ qua)'. On the Home screen, verify that the recipe count displays '15', favorite count displays '4', and the 4 bottom navigation tabs ('Trang chủ', 'Thư viện', 'AI Gợi ý', 'Tủ lạnh') are visible.
  ```

---

### S1 — Điều hướng toàn bộ các Tab & Cài đặt (Navigation Sweep)
- **Mục tiêu**: Đảm bảo tất cả 4 tab đáy và màn hình Cài đặt chuyển đổi mượt mà, không crash.
- **Tiền điều kiện**: Đang ở màn hình Trang chủ (sau S0).
- **Thao tác**:
  1. Bấm tab **"Thư viện"** → kiểm tra tiêu đề và thanh chip phân loại ("Tất cả", "Món chính", ...).
  2. Bấm tab **"AI Gợi ý"** → kiểm tra ô nhập "Hỏi trợ lý…" và các chip gợi ý nhanh.
  3. Bấm tab **"Tủ lạnh"** → kiểm tra danh sách nguyên liệu và nút FAB "+".
  4. Bấm tab **"Trang chủ"** → quay lại Trang chủ.
  5. Bấm icon bánh răng **Cài đặt** ở góc trên bên phải → kiểm tra màn hình "Cài đặt".
  6. Bấm nút mũi tên quay lại Trang chủ.
- **Kỳ vọng**: Mọi màn hình tải đúng nội dung, không có màn hình đen hay crash.
- **ARTEMIS Task Prompt**:
  ```
  From the Home screen, tap the bottom tab 'Thư viện' and verify the recipe list appears. Next, tap 'AI Gợi ý' and verify the AI assistant chat screen opens. Next, tap 'Tủ lạnh' and verify the pantry screen displays ingredients. Tap 'Trang chủ' to return, then tap the Settings gear icon in the top right to verify the Settings screen opens, and finally navigate back to Home.
  ```

---

### S2 — Thêm, Sửa, Xóa Công Thức (Recipe CRUD)
- **Mục tiêu**: Kiểm tra trọn vẹn vòng đời một món ăn mới do người dùng tạo.
- **Tiền điều kiện**: Đang ở tab "Thư viện".
- **Thao tác**:
  1. Bấm nút thêm mới **"+"** (hoặc nút tạo món).
  2. Nhập:
     - Tên món: `Thịt kho tiêu E2E`
     - Chọn danh mục: `Món chính`
     - Nguyên liệu: bấm "Thêm nguyên liệu" → Tên `Thịt heo`, SL `300`, ĐV `g`
     - Các bước: Bước 1 `Ướp thịt với tiêu và nước mắm`, Bước 2 `Kho nhỏ lửa 20 phút`
  3. Bấm **"Lưu công thức"**.
  4. Trong danh sách Thư viện, tìm và bấm vào món `Thịt kho tiêu E2E`.
  5. Bấm icon **"Sửa"** (bút chì) → sửa Khẩu phần thành `4` → bấm **"Cập nhật công thức"**.
  6. Bấm icon **"Xóa"** (thùng rác) → hộp thoại "Bạn có chắc chắn muốn xóa công thức này?" xuất hiện → bấm **"Xóa"**.
- **Kỳ vọng**: Món mới tạo hiển thị đúng, sửa lưu thành công, sau khi xóa không còn trong danh sách Thư viện.
- **ARTEMIS Task Prompt**:
  ```
  Go to 'Thư viện', tap the add button to create a new recipe named 'Thịt kho tiêu E2E', add 1 ingredient ('Thịt heo', 300g) and 1 step ('Kho nhỏ lửa'), then tap 'Lưu công thức'. Open the newly created recipe, tap edit to change servings to 4 and save. Finally, tap delete, confirm deletion in the dialog, and verify it no longer appears in the library.
  ```

---

### S3 — Tìm Kiếm Theo Tên & Nguyên Liệu (Search)
- **Mục tiêu**: Kiểm tra ô tìm kiếm lọc chính xác theo cả tên món và nguyên liệu thành phần.
- **Tiền điều kiện**: Đang ở Trang chủ hoặc Thư viện.
- **Thao tác**:
  1. Bấm icon **Kính lúp** ở thanh trên cùng để vào màn hình Tìm kiếm.
  2. Gõ `Cháo` → kiểm tra kết quả hiện `Cháo gà`.
  3. Xóa ô tìm kiếm, gõ `Gừng` (nguyên liệu của Cháo gà) → kiểm tra kết quả vẫn tìm thấy `Cháo gà`.
  4. Xóa ô tìm kiếm, gõ chuỗi không tồn tại `zzzzzz` → kiểm tra màn hình báo không tìm thấy kết quả.
- **Kỳ vọng**: Tìm đúng theo tên, tìm đúng theo nguyên liệu, xử lý kết quả rỗng thân thiện.
- **ARTEMIS Task Prompt**:
  ```
  Tap the search icon in the top bar. In the search field, type 'Cháo' and verify 'Cháo gà' appears in the results. Clear and type 'Gừng' and verify 'Cháo gà' is still found by ingredient. Clear and type 'zzzzzz' to verify an empty state message is shown.
  ```

---

### S4 — Bật/Tắt Yêu Thích (Favorites Flow)
- **Mục tiêu**: Thao tác yêu thích cập nhật tức thì trên Trang chủ và màn hình Yêu thích.
- **Tiền điều kiện**: Ghi nhận số lượng yêu thích ban đầu (4 món).
- **Thao tác**:
  1. Vào Thư viện, mở món `Cháo gà` (món này ban đầu chưa yêu thích).
  2. Bấm icon **Trái tim** ở thanh trên cùng để thích món này.
  3. Bấm quay lại Trang chủ → kiểm tra số thẻ "Yêu thích" tăng từ 4 lên **5**.
  4. Mở lại `Cháo gà`, bấm icon Trái tim lần nữa để bỏ thích.
  5. Quay lại Trang chủ → kiểm tra số thẻ "Yêu thích" giảm về **4**.
- **Kỳ vọng**: Trạng thái yêu thích đồng bộ ngay lập tức giữa các màn hình.
- **ARTEMIS Task Prompt**:
  ```
  Open recipe 'Cháo gà' from Library. Tap the heart icon to add it to favorites. Return to Home and verify the favorites counter increased to 5. Open 'Cháo gà' again, unfavorite it, return to Home and verify the counter returns to 4.
  ```

---

### S5 — Ghi Lịch Sử Nấu Ăn ("Đã nấu hôm nay")
- **Mục tiêu**: Ghi nhận một lần nấu và kiểm tra xuất hiện trong Lịch sử nấu.
- **Tiền điều kiện**: Màn hình Lịch sử nấu đã có sẵn các bản ghi seed.
- **Thao tác**:
  1. Vào Thư viện, mở món `Bún chả`.
  2. Cuộn xuống và bấm nút **"Đã nấu hôm nay"**.
  3. Quan sát thông báo / trạng thái nút đổi sang đã ghi nhận.
  4. Mở màn hình **Lịch sử nấu** (từ icon Lịch sử ở Trang chủ hoặc Thư viện).
- **Kỳ vọng**: Món `Bún chả` xuất hiện ở đầu danh sách Lịch sử với nhãn ngày hôm nay.
- **ARTEMIS Task Prompt**:
  ```
  Open 'Bún chả' from the Library. Tap 'Đã nấu hôm nay'. Navigate to the cooking history screen and verify that 'Bún chả' appears at the top of the history list for today.
  ```

---

### S6 — Quản Lý Tủ Lạnh & Cảnh Báo "Sắp Hết" (Pantry Management)
- **Mục tiêu**: Kiểm tra cảnh báo nguyên liệu sắp hết và thêm/xóa nguyên liệu tủ lạnh.
- **Tiền điều kiện**: App đã nạp seed mới (có sẵn "Tiêu đen hạt" và "Bơ lạt" dưới ngưỡng tối thiểu).
- **Thao tác**:
  1. Chuyển sang tab **"Tủ lạnh"**.
  2. Kiểm tra phần cảnh báo **"Sắp hết nguyên liệu"** có hiển thị các món thiếu.
  3. Bấm nút FAB **"+"** ("Thêm nguyên liệu"):
     - Tên: `Nấm rơm E2E`
     - Số lượng: `200`
     - Đơn vị: `g`
     - Bấm **"Lưu"**.
  4. Tìm món vừa thêm trong danh sách, bấm icon **"Xóa"** của món đó.
- **Kỳ vọng**: Cảnh báo sắp hết hoạt động đúng; thêm nguyên liệu mới hiển thị ngay; xóa thành công.
- **ARTEMIS Task Prompt**:
  ```
  Go to the 'Tủ lạnh' tab. Verify that the 'Sắp hết nguyên liệu' low-stock section is displayed. Tap the '+' button to add an ingredient: name 'Nấm rơm E2E', amount '200', unit 'g', then save. Verify it appears in the list, then delete it.
  ```

---

### S7 — Trợ Lý AI Nấu Ăn (Offline Rule-Based)
- **Mục tiêu**: Kiểm tra trợ lý AI hoạt động ổn định ở chế độ ngoại tuyến (rule-based).
- **Tiền điều kiện**: Đang ở tab "AI Gợi ý".
- **Thao tác**:
  1. Chuyển sang tab **"AI Gợi ý"**.
  2. Bấm một chip gợi ý nhanh (VD: **"Món nhanh"** hoặc **"Từ tủ lạnh"**) → kiểm tra tin nhắn trả lời và thẻ món ăn gợi ý xuất hiện.
  3. Bấm vào thẻ món ăn trong bong bóng chat → kiểm tra mở ra màn hình Chi tiết của món đó.
  4. Quay lại màn hình AI, gõ vào ô nhập: `thịt bò` và bấm Gửi.
- **Kỳ vọng**: AI phản hồi mượt, thẻ công thức điều hướng đúng sang Chi tiết, lịch sử hội thoại giữ nguyên.
- **ARTEMIS Task Prompt**:
  ```
  Go to the 'AI Gợi ý' tab. Tap the quick-prompt chip 'Món nhanh' and verify the assistant responds with recipe suggestions. Tap one of the recipe cards in the reply to ensure it opens the Detail screen, then navigate back. Type 'thịt bò' and send to verify a response is generated.
  ```

---

### S8 — Cài Đặt Giao Diện & Sao Lưu Cơ Sở Dữ Liệu
- **Mục tiêu**: Kiểm tra toggle chế độ tối/sáng và tính năng sao lưu SQLite.
- **Tiền điều kiện**: Đang ở màn hình Cài đặt.
- **Thao tác**:
  1. Vào màn hình **Cài đặt** (từ icon bánh răng Trang chủ).
  2. Tìm mục **"Chế độ tối"** và bật công tắc (Switch).
  3. Kiểm tra toàn bộ theme của app đổi sang màu tối ngay lập tức.
  4. Tắt lại công tắc để về theme sáng.
  5. Cuộn xuống phần "Dữ liệu & Bộ nhớ", bấm **"Sao lưu cơ sở dữ liệu"**.
- **Kỳ vọng**: Theme đổi tức thì mà không cần restart app; chức năng sao lưu chạy không báo lỗi.
- **ARTEMIS Task Prompt**:
  ```
  Open the Settings screen. Find the 'Chế độ tối' (Dark Mode) toggle, switch it ON, verify the app theme turns dark, then switch it back OFF. Scroll down to 'Dữ liệu & Bộ nhớ' and tap 'Sao lưu cơ sở dữ liệu' to verify the backup operation initiates without crashing.
  ```

---

### S9 — Màn Hình Auth & Validation Ngoại Tuyến
- **Mục tiêu**: Kiểm tra giao diện đăng nhập/đăng ký và validation khi không có mạng.
- **Tiền điều kiện**: Thoát đăng nhập hoặc mở app lần đầu.
- **Thao tác**:
  1. Vào Cài đặt → nếu có nút **"Đăng xuất"** thì bấm Đăng xuất để về màn hình Auth (hoặc `adb shell pm clear`).
  2. Tại màn hình Auth: chuyển qua lại giữa 2 tab **"Đăng ký mới"** và **"Đăng nhập"**.
  3. Để trống email/mật khẩu và bấm nút "Đăng nhập" → kiểm tra có thông báo lỗi / không cho phép gửi.
  4. Bấm **"Dùng ngoại tuyến (Bỏ qua)"** → vào thẳng Trang chủ.
- **Kỳ vọng**: Validation form chuẩn, nút bypass luôn đưa người dùng vào app an toàn.
- **ARTEMIS Task Prompt**:
  ```
  On the Auth screen, switch between 'Đăng nhập' and 'Đăng ký mới' tabs. Try tapping the submit button with empty fields to verify validation prevents submission. Finally, tap 'Dùng ngoại tuyến (Bỏ qua)' and verify it takes you to the Home screen.
  ```
