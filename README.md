# Sổ tay Công thức Nấu ăn / Cooking Note

Ứng dụng Android quản lý công thức nấu ăn — BTL môn **Phát triển Ứng dụng Di động**, Cao đẳng Kỹ thuật – Công nghệ Bách Khoa (GVHD: Lê Văn Quân).

Android recipe-cookbook app — student major assignment.

---

## ✨ Tính năng / Features

- **Thư viện công thức** — CRUD, danh mục, tag, bước nấu, nguyên liệu, ảnh minh hoạ.
- **Tủ lạnh (Pantry)** — quản lý nguyên liệu còn/hết, định lượng, hạn dùng.
- **Yêu thích + Lịch sử nấu** — đánh dấu món, theo dõi lần nấu gần nhất.
- **Tìm kiếm + Lọc** — theo tên, nguyên liệu, tag, danh mục.
- **AI Trợ lý nấu ăn (chat)** — phiên chat 1 session, lưu memory vào Room, gợi ý từ tủ lạnh / nhanh / chay / cay, đính kèm ảnh (vision), fallback rule-based khi offline.
- **Cài đặt AI** — multi-provider: OpenAI Chat Completions, OpenAI Responses, Anthropic Messages, Google Gemini. Lưu key mã hoá `EncryptedSharedPreferences`.
- **Backup SQLite** — xuất DB local ra file.

---

## 🛠 Stack

| Layer | Tech |
|-------|------|
| UI | Jetpack Compose · Material 3 |
| Kiến trúc | MVVM + Flow + `collectAsState` |
| Điều hướng | Navigation Compose · sealed `Route` |
| Persistence | Room 2.6.1 (KSP) + DataStore Preferences + EncryptedSharedPreferences |
| Network | OkHttp + Moshi + Retrofit |
| AI | 4 endpoint chuẩn (`/chat/completions`, `/responses`, `/v1/messages`, `:generateContent`) + rule-based offline |
| Image | Coil 2.x |

`minSdk = 26`, `compileSdk = 35`, Kotlin 2.0.21, JVM 17.

---

## 🚀 Build & cài đặt

```bash
git clone https://github.com/chumsecay/cooking-note
cd cooking-note
./gradlew.bat :app:assembleDebug          # Windows
./gradlew :app:assembleDebug              # macOS / Linux
```

APK nằm ở `app/build/outputs/apk/debug/app-debug.apk`. Cài qua `adb install` hoặc Android Studio.

### Chạy thật trên thiết bị

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.cookingnote.app/.MainActivity
```

---

## ⚙️ Cấu hình AI

Vào **Trang chủ → ⚙️ Cài đặt** (icon gear góc phải top-bar):

| Trường | Mô tả |
|--------|-------|
| Provider | OpenAI Chat · OpenAI Responses · Anthropic · Gemini · Rule-based |
| Base URL | Endpoint gốc, ví dụ `https://api.openai.com/v1/` |
| API Key | Key của provider (mã hoá EncryptedSharedPreferences) |
| Model | Model ID, ví dụ `gpt-4o-mini`, `claude-3-5-haiku-latest`, `gemini-1.5-flash` |
| max_tokens | 256 – 8192 |

Bấm **Lưu cấu hình AI** → mở tab **AI Gợi ý** → chat.

Nếu không cấu hình AI, app tự động dùng rule-based gợi ý cục bộ (từ DB nội bộ).

---

## 🧠 Cấu trúc AI Chat

```
ChatMessageEntity (Room)
    ↓ observe
LazyColumn of MessageBubble (user / assistant)
    ↓ Send
appendMessage() → last 20 messages → chat(prompt, history)
    ↓
buildSystemContext() = pantry ∪ favorites ∪ recent cooked
    ↓
provider.chat() → AiSuggestion → bubble + recipe card
```

- Memory: lưu mọi message, mỗi lượt gửi kèm 20 message gần nhất.
- Context: pantry items + 5 favorites + 3 lần nấu gần nhất inject vào system prompt.
- Quick chips: `Từ tủ lạnh` · `Món nhanh 15 phút` · `Món chay` · `Món cay`.

---

## 📂 Cấu trúc thư mục

```
app/src/main/java/com/cookingnote/app/
├── CookingNoteApp.kt          # Application, AppContainer
├── MainActivity.kt
├── ai/                        # AiService, providers, rules
├── data/
│   ├── AppContainer.kt        # DI tay (không dùng Hilt)
│   ├── dao/                   # Room DAOs
│   ├── database/              # RoomDatabase
│   ├── entity/                # @Entity classes
│   ├── prefs/                 # DataStore + EncryptedSharedPreferences
│   └── repository/            # CookbookRepository
└── ui/
    ├── CookingNoteRoot.kt     # NavHost + BottomBar
    ├── screens/               # Home / Library / Pantry / AI / Settings / Detail / Editor
    └── theme/                 # Material 3 theme
```

---

## 🗄 Schema (Room v2)

Bảng chính: `recipes`, `categories`, `tags`, `ingredients`, `recipe_tag_cross_ref`, `recipe_ingredient`, `steps`, `pantry_items`, `cook_history`, `ai_query_logs`, `chat_messages`.

`chat_messages(id, role, content, createdAt)` — `role ∈ {"user","assistant","system"}`, index theo `createdAt`.

Migration v1 → v2: `fallbackToDestructiveMigration` (dev mode). Khi ship release, viết `Migration(1, 2)` thêm bảng `chat_messages`.

---

## 🧪 Seed data

Lần mở đầu tiên app tự seed 9 recipes Việt (Canh chua, Mì xào bò, Bánh flan, Gỏi cuốn, …) + 4 categories + tags + pantry mẫu. Xoá dữ liệu: **Settings → App data → Clear** (tương đương `pm clear com.cookingnote.app`).

---

## 📸 Screenshots

**Chat AI interface** (sau khi tap chip "Món nhanh 15 phút" và "Món chay"):
![Chat UI](https://github.com/chumsecay/cooking-note/raw/main/chat-ui-example.png)

**Recipe detail card** in AI response:
- **Cơm chiên trứng cà chua** (35 phút)
  Gạo, trứng, cà chua đều có. Hành tím thơm, trứng đánh sẵn, com rang, nêm nước mắm + đường.

- **Canh chua trứng kho**
  Trứng bò mù, xào tỏi, đặt trứng vào khòa nhỏ. Nước mắm + đường. Ăn với com trắng.

- **Trứng chiên hành tỏi**
  Trứng đánh với hành tím + tỏi băm, chiên vàng. Đầu ăn để chấm nước mắm đường.

**Món yêu thích gần đây**: Gỏi cuốn, bún chả, phở bò… (cần thịt, rau sống, bún – tủ lạnh thiếu. Đi chợ bổ sung nếu muốn).

---

## 📖 Documentation

- Full features, stack, AI config, Room schema, build instructions in `README.md`.
- Report slides and docx to be generated in Phase 5.


---

## 👥 Nhóm thực hiện

- Sinh viên thực hiện: _(điền tên + MSSV)_
- GVHD: ThS. Lê Văn Quân
- Trường: Cao đẳng Kỹ thuật – Công nghệ Bách Khoa
- Năm: 2025 – 2026

---

## ⚠️ Security

- API key lưu trong **EncryptedSharedPreferences** (AES-GCM), không commit vào git.
- Nếu lỡ commit/push key, **rotate ngay** tại provider dashboard.
- File `*.db`, `*.png`, `ui-*.xml` đã có trong `.gitignore`.

---

## 📜 License

MIT — dùng cho mục đích học tập.
