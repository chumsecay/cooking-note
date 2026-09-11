# -*- coding: utf-8 -*-
import os
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def create_report():
    doc = docx.Document()

    # Configure Margins (A4 standard: 2cm top/bottom, 2.5cm left, 2cm right)
    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(0.8)

    # Set Base Style
    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Times New Roman'
    normal_style.font.size = Pt(13)
    normal_style.font.color.rgb = RGBColor(0x22, 0x22, 0x22)
    normal_style.paragraph_format.line_spacing = 1.25
    normal_style.paragraph_format.space_after = Pt(6)

    # --- COVER PAGE ---
    p_univ = doc.add_paragraph()
    p_univ.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p_univ.add_run("TRƯỜNG CAO ĐẲNG KỸ THUẬT – CÔNG NGHỆ BÁCH KHOA\n")
    r.bold = True
    r.font.size = Pt(14)
    r2 = p_univ.add_run("KHOA CÔNG NGHỆ THÔNG TIN\n")
    r2.bold = True
    r2.font.size = Pt(14)
    r2.font.color.rgb = RGBColor(0x1B, 0x4F, 0x72)

    p_div = doc.add_paragraph()
    p_div.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_div.add_run("-------------------***-------------------\n\n\n")

    p_subject = doc.add_paragraph()
    p_subject.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_sub = p_subject.add_run("BÁO CÁO BÀI TẬP LỚN\n")
    r_sub.bold = True
    r_sub.font.size = Pt(18)
    r_sub.font.color.rgb = RGBColor(0xC0, 0x39, 0x2B)

    r_sub2 = p_subject.add_run("MÔN HỌC: PHÁT TRIỂN ỨNG DỤNG DI ĐỘNG\n\n")
    r_sub2.bold = True
    r_sub2.font.size = Pt(14)

    p_topic = doc.add_paragraph()
    p_topic.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_topic_lbl = p_topic.add_run("ĐỀ TÀI:\n")
    r_topic_lbl.bold = True
    r_topic_lbl.font.size = Pt(15)
    r_topic = p_topic.add_run("XÂY DỰNG ỨNG DỤNG SỔ TAY CÔNG THỨC NẤU ĂN THÔNG MINH\n(COOKING NOTE - OFFLINE FIRST & AI ASSISTANT)\n\n\n")
    r_topic.bold = True
    r_topic.font.size = Pt(16)
    r_topic.font.color.rgb = RGBColor(0x0E, 0x66, 0x55)

    p_info = doc.add_paragraph()
    p_info.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p_info.paragraph_format.left_indent = Inches(1.5)
    
    r_gvhd = p_info.add_run("Giảng viên hướng dẫn:\t")
    r_gvhd.bold = True
    p_info.add_run("ThS. Lê Văn Quân\n\n")
    
    r_sv = p_info.add_run("Nhóm sinh viên thực hiện:\n")
    r_sv.bold = True
    
    students = [
        ("Vũ Đình Đạo (Nhóm trưởng)", "TT602-K16LT"),
        ("Ngô Gia Bảo", "TT602-K16LT"),
        ("Nguyễn Tiến Dũng", "TT602-K16LT"),
        ("Lê Đức Trọng", "TT602-K16LT"),
    ]
    for name, msv in students:
        p_info.add_run(f"• {name:<32}\tLớp: {msv}\n")

    p_date = doc.add_paragraph()
    p_date.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_date.paragraph_format.space_before = Pt(80)
    r_date = p_date.add_run("Hà Nội, Năm 2026")
    r_date.italic = True

    doc.add_page_break()

    # --- PHÂN CHIA CÔNG VIỆC ---
    h_task = doc.add_paragraph()
    r = h_task.add_run("BẢNG PHÂN CHIA CÔNG VIỆC VÀ ĐÁNH GIÁ ĐÓNG GÓP")
    r.bold = True
    r.font.size = Pt(15)
    r.font.color.rgb = RGBColor(0x1B, 0x4F, 0x72)

    table_task = doc.add_table(rows=1, cols=4)
    table_task.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr_cells = table_task.rows[0].cells
    headers = ["Họ và tên", "Lớp", "Nhiệm vụ đảm nhiệm", "Mức độ hoàn thành"]
    for i, h in enumerate(headers):
        hdr_cells[i].text = h
        hdr_cells[i].paragraphs[0].runs[0].bold = True
        hdr_cells[i].paragraphs[0].runs[0].font.size = Pt(11)
        hdr_cells[i].paragraphs[0].runs[0].font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        set_cell_background(hdr_cells[i], "1B4F72")
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=100, right=100)

    task_data = [
        ("Vũ Đình Đạo\n(Nhóm trưởng)", "TT602-K16LT", "• Thiết kế kiến trúc tổng thể (MVVM + UDF + Repository Pattern)\n• Thiết kế và cấu hình Room Database (10 Entities, 9 DAOs)\n• Quản lý điều hướng Navigation Compose và AppContainer\n• Tổng hợp mã nguồn và điều phối dự án", "100%"),
        ("Ngô Gia Bảo", "TT602-K16LT", "• Xây dựng tầng Presentation (10 ViewModels độc lập)\n• Mô hình hóa UI State rõ ràng (Loading, Content, Empty, Error)\n• Tối ưu hóa vòng đời Compose với collectAsStateWithLifecycle()\n• Hiện thực hóa AppViewModelFactory", "100%"),
        ("Nguyễn Tiến Dũng", "TT602-K16LT", "• Lập trình giao diện Jetpack Compose Material 3\n• Xây dựng các màn hình: Home, Library, Detail, Pantry, Create Recipe\n• Tối ưu hóa trải nghiệm người dùng, bảng màu và animation", "100%"),
        ("Lê Đức Trọng", "TT602-K16LT", "• Phát triển phân hệ AI Assistant (OpenAI, Gemini, Anthropic)\n• Xây dựng thuật toán RuleBasedAi hỗ trợ ngoại tuyến\n• Xây dựng bộ tự động kiểm thử 100 Unit Tests trên JVM\n• Soạn thảo báo cáo kỹ thuật và tài liệu thuyết trình", "100%")
    ]

    for row_idx, data in enumerate(task_data):
        row = table_task.add_row()
        for c_idx, val in enumerate(data):
            cell = row.cells[c_idx]
            cell.text = val
            p = cell.paragraphs[0]
            p.runs[0].font.size = Pt(10.5)
            if c_idx in [0, 1, 3]:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            set_cell_margins(cell, top=100, bottom=100, left=100, right=100)
            if row_idx % 2 == 1:
                set_cell_background(cell, "F2F4F4")

    doc.add_paragraph().paragraph_format.space_after = Pt(12)

    # --- MỤC LỤC TỔNG QUAN ---
    h_toc = doc.add_paragraph()
    r_toc = h_toc.add_run("MỤC LỤC TỔNG QUAN")
    r_toc.bold = True
    r_toc.font.size = Pt(15)
    r_toc.font.color.rgb = RGBColor(0x1B, 0x4F, 0x72)

    toc_items = [
        ("CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI", "Trang 3"),
        ("  1.1. Lý do chọn đề tài", "3"),
        ("  1.2. Mục tiêu của bài tập lớn", "3"),
        ("  1.3. Phạm vi và đối tượng sử dụng", "4"),
        ("CHƯƠNG 2: PHÂN TÍCH BÀI TOÁN", "Trang 5"),
        ("  2.1. Mô tả bài toán", "5"),
        ("  2.2. Yêu cầu chức năng của hệ thống", "5"),
        ("  2.3. Yêu cầu phi chức năng", "7"),
        ("  2.4. Xác định các tác nhân và đối tượng trong hệ thống", "8"),
        ("CHƯƠNG 3: THIẾT KẾ HỆ THỐNG", "Trang 9"),
        ("  3.1. Thiết kế kiến trúc tổng thể (MVVM + UDF)", "9"),
        ("  3.2. Thiết kế lớp (Class Design)", "10"),
        ("  3.3. Thiết kế cơ sở dữ liệu (Database Schema & ERD)", "13"),
        ("  3.4. Thiết kế luồng xử lý và vòng đời dữ liệu (Flow & Lifecycle)", "16"),
        ("CHƯƠNG 4: CÀI ĐẶT VÀ KẾT QUẢ THỰC NGHIỆM", "Trang 17"),
        ("  4.1. Công cụ và môi trường phát triển", "17"),
        ("  4.2. Cấu trúc project mã nguồn", "17"),
        ("  4.3. Giao diện và các màn hình chức năng chính", "18"),
        ("  4.4. Đánh giá chất lượng và kết quả kiểm thử tự động (100 Tests)", "21"),
        ("KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN", "Trang 23"),
        ("TÀI LIỆU THAM KHẢO", "Trang 24")
    ]

    for title, page in toc_items:
        p = doc.add_paragraph()
        p.paragraph_format.line_spacing = 1.15
        p.paragraph_format.space_after = Pt(3)
        r1 = p.add_run(title)
        if title.startswith("CHƯƠNG") or title.startswith("KẾT") or title.startswith("TÀI"):
            r1.bold = True
            r1.font.color.rgb = RGBColor(0x1B, 0x4F, 0x72)
        r2 = p.add_run(f" {'.' * (60 - len(title))} {page}")
        r2.font.color.rgb = RGBColor(0x7F, 0x8C, 0x8D)

    doc.add_page_break()

    # Helper function for Section Heading
    def add_h1(text):
        h = doc.add_paragraph()
        h.paragraph_format.space_before = Pt(14)
        h.paragraph_format.space_after = Pt(6)
        r = h.add_run(text)
        r.bold = True
        r.font.size = Pt(15)
        r.font.color.rgb = RGBColor(0x1B, 0x4F, 0x72)
        return h

    def add_h2(text):
        h = doc.add_paragraph()
        h.paragraph_format.space_before = Pt(10)
        h.paragraph_format.space_after = Pt(4)
        r = h.add_run(text)
        r.bold = True
        r.font.size = Pt(13.5)
        r.font.color.rgb = RGBColor(0x0E, 0x66, 0x55)
        return h

    def add_h3(text):
        h = doc.add_paragraph()
        h.paragraph_format.space_before = Pt(6)
        h.paragraph_format.space_after = Pt(2)
        r = h.add_run(text)
        r.bold = True
        r.italic = True
        r.font.size = Pt(13)
        r.font.color.rgb = RGBColor(0x2C, 0x3E, 0x50)
        return h

    # ==========================================
    # CHƯƠNG 1
    # ==========================================
    add_h1("CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI")

    add_h2("1.1. Lý do chọn đề tài")
    doc.add_paragraph(
        "Trong nhịp sống hiện đại bận rộn, việc tự nấu ăn tại nhà không chỉ giúp đảm bảo vệ sinh an toàn thực phẩm, "
        "cân bằng dinh dưỡng mà còn là một nét văn hóa gia đình ấm cúng. Tuy nhiên, người nội trợ và các bạn trẻ sống tự lập "
        "thường xuyên phải đối mặt với các câu hỏi nan giải mỗi ngày: \"Hôm nay ăn gì?\", \"Tủ lạnh còn những gì và nấu được món gì?\", "
        "hoặc gặp khó khăn khi muốn lưu trữ lại những công thức nấu ăn sáng tạo của riêng mình."
    )
    doc.add_paragraph(
        "Mặc dù trên thị trường có nhiều ứng dụng công thức nấu ăn, đa số các ứng dụng hiện nay gặp phải những hạn chế lớn:\n"
        "1. Phụ thuộc hoàn toàn vào kết nối Internet: Khi vào bếp, tầng hầm hoặc vùng sóng yếu, người dùng không thể tra cứu công thức.\n"
        "2. Thiếu tính năng quản lý thực phẩm tại chỗ: Không kết nối được giữa kho nguyên liệu đang có trong tủ lạnh với các món có thể nấu.\n"
        "3. Trải nghiệm quảng cáo tràn lan và hiệu năng chưa tối ưu: Gây gián đoạn quá trình nấu ăn.\n"
        "4. Thiếu trợ lý thông minh hỗ trợ tùy biến thực đơn: Không tận dụng được sức mạnh của trí tuệ nhân tạo (AI) để gợi ý món theo nhu cầu thực tế."
    )
    doc.add_paragraph(
        "Chính vì những lý do trên, nhóm chúng em đã lựa chọn đề tài \"Xây dựng ứng dụng Sổ tay Công thức Nấu ăn Thông minh (Cooking Note)\" "
        "với định hướng Offline-first (hoạt động bền bỉ không cần mạng), kết hợp linh hoạt cùng Trợ lý AI và thuật toán gợi ý cục bộ, "
        "nhằm mang đến cho người dùng một công cụ đắc lực, tiện lợi và hiện đại nhất trong gian bếp."
    )

    add_h2("1.2. Mục tiêu của bài tập lớn")
    doc.add_paragraph(
        "Đề tài hướng tới việc hoàn thành toàn diện cả về mặt kiến thức công nghệ lẫn kỹ năng lập trình chuyên nghiệp:"
    )
    doc.add_paragraph(
        "• Về mặt kiến thức:\n"
        "  - Nắm vững và làm chủ bộ công cụ phát triển giao diện hiện đại nhất của Android: Jetpack Compose và Material 3 Design.\n"
        "  - Hiểu sâu sắc và áp dụng chuẩn mực mô hình kiến trúc MVVM (Model - View - ViewModel) kết hợp nguyên lý Unidirectional Data Flow (UDF).\n"
        "  - Thành thạo công nghệ lưu trữ dữ liệu bền vững Offline với SQLite thông qua Room Database 2.6.1 và bộ xử lý KSP (Kotlin Symbol Processing).\n"
        "  - Nắm vững kỹ thuật lập trình bất đồng bộ phản ứng với Kotlin Coroutines và Reactive Flow.\n"
        "  - Tiếp cận và tích hợp các mô hình ngôn ngữ lớn (LLM AI) thông qua chuẩn RESTful API và kỹ thuật bảo mật khóa bằng EncryptedSharedPreferences."
    )
    doc.add_paragraph(
        "• Về mặt kỹ năng thực tế:\n"
        "  - Kỹ năng phân tích, thiết kế hệ thống phần mềm di động theo quy chuẩn công nghiệp.\n"
        "  - Kỹ năng xây dựng bộ kiểm thử tự động (Unit Test, Regression Test) đạt độ tin cậy tuyệt đối (100% test pass).\n"
        "  - Kỹ năng làm việc nhóm, quản lý mã nguồn chuyên nghiệp với Git và GitHub."
    )

    add_h2("1.3. Phạm vi và đối tượng sử dụng")
    doc.add_paragraph(
        "• Phạm vi chương trình:\n"
        "  - Ứng dụng chạy độc lập trên hệ điều hành Android (hỗ trợ từ Android 8.0 Oreo - API 26 đến Android 15 - API 35).\n"
        "  - Lưu trữ cơ sở dữ liệu nội bộ SQLite Room với 10 bảng dữ liệu có quan hệ toàn vẹn.\n"
        "  - Hoạt động trơn tru ngoại tuyến (Offline-first) và có khả năng kết nối linh hoạt tới 4 nhà cung cấp AI hàng đầu thế giới (OpenAI Chat, OpenAI Responses, Anthropic Claude, Google Gemini)."
    )
    doc.add_paragraph(
        "• Đối tượng người dùng:\n"
        "  - Người nội trợ gia đình, sinh viên ở trọ, người độc thân muốn tự nấu ăn nhanh chóng, khoa học.\n"
        "  - Những người đam mê ẩm thực muốn lưu trữ công thức cá nhân và theo dõi hạn sử dụng nguyên liệu trong tủ lạnh."
    )

    # ==========================================
    # CHƯƠNG 2
    # ==========================================
    add_h1("CHƯƠNG 2: PHÂN TÍCH BÀI TOÁN")

    add_h2("2.1. Mô tả bài toán")
    doc.add_paragraph(
        "Hệ thống phần mềm cần giải quyết bài toán quản trị thông tin ẩm thực và hỗ trợ ra quyết định nấu nướng cho người dùng cá nhân. "
        "Người dùng cần một ứng dụng có tốc độ mở tức thì, giao diện trực quan, cho phép thao tác một chạm khi tay đang ướt hoặc đang bận rộn làm bếp. "
        "Dữ liệu cần được bảo đảm an toàn trên thiết bị của chính người dùng, không phụ thuộc vào máy chủ trung gian (Zero cloud lock-in)."
    )

    add_h2("2.2. Yêu cầu chức năng của hệ thống")
    doc.add_paragraph(
        "Hệ thống Cooking Note được phân rã thành 5 phân hệ chức năng cốt lõi:"
    )
    doc.add_paragraph(
        "1. Phân hệ Quản lý Công thức Nấu ăn (Recipe Management):\n"
        "   - Xem danh sách công thức theo thẻ (card) trực quan gồm ảnh, tên, thời gian nấu, độ khó.\n"
        "   - Xem chi tiết công thức: Danh mục, định lượng nguyên liệu, các bước nấu có đánh số thứ tự, ghi chú.\n"
        "   - Thêm mới công thức: Hỗ trợ thêm linh hoạt nhiều nguyên liệu và các bước làm.\n"
        "   - Chỉnh sửa, xóa công thức hiện có.\n"
        "   - Đánh dấu món ăn yêu thích (Favorites) và ghi nhận nhật ký đã nấu (Cook History).\n"
        "   - Chia sẻ nhanh công thức dưới dạng văn bản có định dạng chuẩn qua các mạng xã hội."
    )
    doc.add_paragraph(
        "2. Phân hệ Tủ lạnh & Nguyên liệu (Pantry Management):\n"
        "   - Quản lý danh sách thực phẩm hiện có trong tủ lạnh (số lượng, đơn vị đo, ngày hết hạn).\n"
        "   - Cảnh báo trực quan các nguyên liệu sắp hết (Low stock threshold alert).\n"
        "   - Thêm, sửa, xóa nhanh nguyên liệu trong tủ lạnh."
    )
    doc.add_paragraph(
        "3. Phân hệ Trợ lý Thông minh (AI Kitchen Assistant):\n"
        "   - Giao diện chat dạng hội thoại thời gian thực, lưu trữ lịch sử chat vào SQLite Room.\n"
        "   - Tự động nạp ngữ cảnh (Context Injection): Tự động nạp danh sách nguyên liệu trong tủ lạnh, 5 món yêu thích và lịch sử nấu gần nhất vào lời nhắc hệ thống.\n"
        "   - Các phím tắt thông minh (Quick Prompts): Gợi ý món từ tủ lạnh, Món nhanh 15 phút, Món chay thanh đạm, Món cay kích thích vị giác.\n"
        "   - Thuật toán ngoại tuyến RuleBasedAi: Hoạt động ngay lập tức khi người dùng không có mạng hoặc không cấu hình API Key."
    )
    doc.add_paragraph(
        "4. Phân hệ Tra cứu & Lọc Nâng cao (Search & Filtering):\n"
        "   - Tìm kiếm tức thì hỗ trợ Debounce 300ms: Tìm kiếm theo tên món, mô tả, ghi chú và tên nguyên liệu.\n"
        "   - Lọc công thức đa chiều theo danh mục món ăn (Món canh, món kho, món xào, món tráng miệng...)."
    )
    doc.add_paragraph(
        "5. Phân hệ Quản trị Cấu hình & Dữ liệu (Settings & Backup):\n"
        "   - Cấu hình Multi-provider AI (OpenAI Chat, OpenAI Responses, Anthropic, Gemini, Rule-based).\n"
        "   - Nhập và lưu trữ an toàn mã khóa API (API Key Encryption).\n"
        "   - Sao lưu toàn bộ cơ sở dữ liệu SQLite ra tệp dự phòng `cookingnote-backup.db` chạy bất đồng bộ trên luồng IO, chia sẻ qua hệ điều hành bằng Android FileProvider."
    )

    add_h2("2.3. Yêu cầu phi chức năng")
    doc.add_paragraph(
        "• Hiệu năng và phản hồi (Performance):\n"
        "  - Tốc độ khởi động nguội (Cold start) < 1.0 giây.\n"
        "  - Thao tác tìm kiếm phản hồi < 50ms nhờ cơ chế tối ưu index cơ sở dữ liệu Room.\n"
        "  - Tốc độ cuộn danh sách (LazyColumn) đạt chuẩn mượt mà 60fps - 120fps, không gây hiện tượng giật khung hình (jank).\n\n"
        "• An toàn vòng đời và bộ nhớ (Lifecycle Safety):\n"
        "  - 100% màn hình sử dụng `collectAsStateWithLifecycle()`: Tự động ngừng lắng nghe luồng dữ liệu khi ứng dụng xuống chế độ nền (background), triệt tiêu hoàn toàn nguy cơ rò rỉ bộ nhớ (memory leaks).\n\n"
        "• Tính bảo mật (Security):\n"
        "  - API Key của các dịch vụ AI được mã hóa bằng tiêu chuẩn mã hóa quân đội AES-256 GCM thông qua Android Jetpack Security (`EncryptedSharedPreferences`), không lưu trữ plaintext.\n\n"
        "• Khả năng vận hành ngoại tuyến (Offline-first Resilience):\n"
        "  - Toàn bộ tính năng CRUD, tra cứu, tủ lạnh, lịch sử và gợi ý cục bộ hoạt động 100% độc lập không cần mạng Internet."
    )

    add_h2("2.4. Xác định các đối tượng trong hệ thống")
    doc.add_paragraph(
        "Hệ thống bao gồm các đối tượng nghiệp vụ chính:\n"
        "1. Recipe (Công thức): Đại diện cho một món ăn, chứa thông tin tên, mô tả, thời gian chuẩn bị, thời gian nấu, khẩu phần, độ khó, trạng thái yêu thích.\n"
        "2. Ingredient (Nguyên liệu công thức): Thuộc về một Recipe, bao gồm tên, định lượng (amount), đơn vị (unit), thuộc tính tùy chọn (isOptional).\n"
        "3. Step (Bước nấu): Thuộc về một Recipe, có số thứ tự (stepNumber) và nội dung hướng dẫn chi tiết.\n"
        "4. Category (Danh mục món ăn): Phân loại món ăn (Ví dụ: Món canh, Món xào, Món mặn, Món chay, Đồ uống).\n"
        "5. PantryItem (Nguyên liệu trong tủ lạnh): Gồm tên, định lượng thực tế, hạn dùng và ngưỡng cảnh báo sắp hết.\n"
        "6. CookHistory (Lịch sử nấu): Lưu vết mỗi lần nấu một món ăn cụ thể, thời gian nấu và ghi chú cảm nhận.\n"
        "7. ChatMessage (Tin nhắn trợ lý): Ghi nhận các câu hỏi của người dùng và phản hồi gợi ý từ AI."
    )

    # ==========================================
    # CHƯƠNG 3
    # ==========================================
    add_h1("CHƯƠNG 3: THIẾT KẾ HỆ THỐNG")

    add_h2("3.1. Thiết kế kiến trúc tổng thể (MVVM + UDF)")
    doc.add_paragraph(
        "Ứng dụng Cooking Note được thiết kế tuân thủ nghiêm ngặt mô hình MVVM (Model - View - ViewModel) kết hợp nguyên lý Unidirectional Data Flow (UDF) được Google khuyến nghị cho phát triển Android hiện đại:"
    )

    arch_diagram = (
        "┌────────────────────────────────────────────────────────────────────────┐\n"
        "│                          PRESENTATION LAYER                            │\n"
        "│  10 Composable Screens (HomeScreen, LibraryScreen, DetailScreen,...)   │\n"
        "│        ▲                                                     │         │\n"
        "│        │ (State down: collectAsStateWithLifecycle)           │ (Events)│\n"
        "│        │                                                     ▼         │\n"
        "│  10 Dedicated ViewModels (HomeViewModel, DetailViewModel, ...)         │\n"
        "│  (StateFlow<UiState> - Loading, Content, Empty, Error)                 │\n"
        "└────────────────────────────────────────────────────────────────────────┘\n"
        "                                  ▲  │\n"
        "  (Flow / Coroutines)             │  │ (Suspend Methods Execution)\n"
        "                                  │  ▼\n"
        "┌────────────────────────────────────────────────────────────────────────┐\n"
        "│                             DATA LAYER                                 │\n"
        "│  CookbookRepository (Single Source of Truth)                           │\n"
        "│  AppContainer (Manual Dependency Injection Container)                  │\n"
        "│        │                                                     │         │\n"
        "│        ▼                                                     ▼         │\n"
        "│  Room Database (9 DAOs)                              AiService         │\n"
        "│  (SQLite local persistence)                          (Cloud & Offline) │\n"
        "└────────────────────────────────────────────────────────────────────────┘"
    )
    p_code = doc.add_paragraph()
    r = p_code.add_run(arch_diagram)
    r.font.name = 'Consolas'
    r.font.size = Pt(9.5)
    r.font.color.rgb = RGBColor(0x1A, 0x52, 0x76)
    set_cell_background(p_code, "EAEDED") if hasattr(p_code, '_tc') else None

    add_h2("3.2. Thiết kế lớp (Class Design)")
    doc.add_paragraph(
        "Các lớp chính trong hệ thống được phân định vai trò rõ ràng:"
    )
    doc.add_paragraph(
        "• Tầng ViewModel (10 ViewModels độc lập):\n"
        "  - HomeViewModel: Điều phối luồng dữ liệu trang chủ, thống kê số lượng món và gợi ý ngẫu nhiên mỗi ngày.\n"
        "  - LibraryViewModel: Quản lý danh mục và lọc công thức theo danh mục được chọn.\n"
        "  - DetailViewModel: Nạp thông tin chi tiết một món ăn (`RecipeWithDetails`), đánh dấu yêu thích, ghi nhận đã nấu, xóa món.\n"
        "  - CreateRecipeViewModel: Quản lý biểu mẫu nhập liệu công thức, tự động kiểm tra tính hợp lệ (form validation), lưu giao dịch đa bảng.\n"
        "  - PantryViewModel: Quản lý kho thực phẩm, cảnh báo nguyên liệu sắp hết, hiển thị dialog thêm đồ.\n"
        "  - FavoritesViewModel: Lọc và hiển thị danh sách các món ăn được người dùng yêu thích.\n"
        "  - HistoryViewModel: Quản lý lịch sử nấu nướng, định dạng ngày tháng hiển thị thân thiện.\n"
        "  - SearchViewModel: Xử lý tìm kiếm công thức tức thì tích hợp Debounce 300ms nhằm giảm tải CPU.\n"
        "  - SettingsViewModel: Xử lý cấu hình AI Provider, mã hóa khóa API và sao lưu DB trên luồng IO.\n"
        "  - AiViewModel: Quản lý phiên hội thoại với AI, nạp ngữ cảnh tủ lạnh và điều phối fallback ngoại tuyến.\n"
        "  - AppViewModelFactory: Lớp khởi tạo ViewModel Factory tập trung, thực hiện Manual Dependency Injection từ `AppContainer`."
    )
    doc.add_paragraph(
        "• Tầng Repository & Data Access:\n"
        "  - CookbookRepository: Cung cấp API duy nhất cho toàn bộ ViewModels để truy vấn dữ liệu Room và AI, thực thi các nghiệp vụ giao dịch (Transaction).\n"
        "  - 9 Room DAOs: RecipeDao, IngredientDao, StepDao, CategoryDao, PantryDao, HistoryDao, TagDao, AiLogDao, ChatMessageDao.\n"
        "  - DefaultAiService & RuleBasedAi: Thực thi gọi mạng tới LLM hoặc thuật toán luật ngoại tuyến."
    )

    add_h2("3.3. Thiết kế cơ sở dữ liệu (Database Schema)")
    doc.add_paragraph(
        "Cơ sở dữ liệu SQLite trong ứng dụng được quản lý bởi Room Database (phiên bản 2), bao gồm 10 bảng dữ liệu có ràng buộc khóa ngoại (Foreign Key) chặt chẽ:"
    )

    table_db = doc.add_table(rows=1, cols=4)
    table_db.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr = table_db.rows[0].cells
    for i, title in enumerate(["Tên Bảng", "Khóa chính (PK)", "Khóa ngoại (FK)", "Mục đích sử dụng"]):
        hdr[i].text = title
        hdr[i].paragraphs[0].runs[0].bold = True
        hdr[i].paragraphs[0].runs[0].font.size = Pt(11)
        hdr[i].paragraphs[0].runs[0].font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        set_cell_background(hdr[i], "1B4F72")

    db_schemas = [
        ("recipes", "id (Long, AutoGen)", "categoryId -> categories(id)", "Lưu thông tin chính của công thức nấu ăn"),
        ("ingredients", "id (Long, AutoGen)", "recipeId -> recipes(id) [CASCADE]", "Lưu các nguyên liệu và định lượng của từng công thức"),
        ("steps", "id (Long, AutoGen)", "recipeId -> recipes(id) [CASCADE]", "Lưu các bước thực hiện có đánh số thứ tự"),
        ("categories", "id (Long, AutoGen)", "None", "Danh mục phân loại món ăn (Canh, Xào, Mặn, Chay...)"),
        ("pantry_items", "id (Long, AutoGen)", "None", "Kho nguyên liệu thực tế có trong tủ lạnh"),
        ("cook_history", "id (Long, AutoGen)", "recipeId -> recipes(id) [CASCADE]", "Nhật ký lịch sử mỗi lần nấu món ăn"),
        ("tags", "id (Long, AutoGen)", "None", "Nhãn phân loại tự do (Nhanh, Cay, Món tiệc...)"),
        ("recipe_tag_cross_ref", "(recipeId, tagId)", "recipeId, tagId [CASCADE]", "Bảng quan hệ nhiều - nhiều giữa Recipe và Tag"),
        ("chat_messages", "id (Long, AutoGen)", "None", "Lịch sử tin nhắn giữa người dùng và trợ lý AI"),
        ("ai_query_logs", "id (Long, AutoGen)", "None", "Nhật ký ghi vết hiệu năng và độ trễ các lượt gọi AI")
    ]

    for idx, row_data in enumerate(db_schemas):
        r_cells = table_db.add_row().cells
        for col_idx, text in enumerate(row_data):
            r_cells[col_idx].text = text
            r_cells[col_idx].paragraphs[0].runs[0].font.size = Pt(10)
            set_cell_margins(r_cells[col_idx], top=80, bottom=80, left=80, right=80)
            if idx % 2 == 1:
                set_cell_background(r_cells[col_idx], "F8F9F9")

    add_h2("3.4. Thiết kế luồng xử lý và vòng đời dữ liệu (Flow & Lifecycle)")
    doc.add_paragraph(
        "Mọi luồng dữ liệu phát sinh trong ứng dụng đều tuân thủ chu trình khép kín:\n"
        "1. Người dùng tương tác giao diện (Event up) -> Composable gọi hàm trên ViewModel tương ứng.\n"
        "2. ViewModel điều phối tác vụ sang `CookbookRepository` thông qua `viewModelScope` và các Coroutine Dispatcher thích hợp (`Dispatchers.IO` cho tác vụ mạng/đĩa).\n"
        "3. Repository tương tác với Room DAO hoặc AI Service, sau đó phát xạ kết quả về dưới dạng `Flow`.\n"
        "4. ViewModel biến đổi dữ liệu thành các dạng `UiState` bất biến (`Loading`, `Content`, `Empty`, `Error`) và lưu giữ bằng `StateFlow`.\n"
        "5. Composable Screen lắng nghe qua `collectAsStateWithLifecycle()`, tự động cập nhật lại giao diện (Recomposition) một cách tối ưu nhất."
    )

    # ==========================================
    # CHƯƠNG 4
    # ==========================================
    add_h1("CHƯƠNG 4: CÀI ĐẶT VÀ KẾT QUẢ THỰC NGHIỆM")

    add_h2("4.1. Công cụ và môi trường phát triển")
    doc.add_paragraph(
        "• Hệ điều hành phát triển: Windows 11 64-bit / Linux / macOS.\n"
        "• Môi trường phát triển tích hợp (IDE): Android Studio Ladybug / Koala.\n"
        "• Ngôn ngữ lập trình: Kotlin 2.0.21, mục tiêu JVM 17.\n"
        "• Khung giao diện: Jetpack Compose (BOM 2024.12.01), Material 3 Design.\n"
        "• Quản lý phụ thuộc: Gradle 9.3.0, Gradle Version Catalog (`gradle/libs.versions.toml`).\n"
        "• SDK Target: `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26` (tương thích hơn 95% thiết bị Android đang hoạt động)."
    )

    add_h2("4.2. Cấu trúc project mã nguồn")
    doc.add_paragraph(
        "Dự án được cấu trúc bài bản, phân lớp rõ ràng theo tiêu chuẩn kiến trúc công nghiệp:\n"
        "• `com.cookingnote.app.data.database`: Cấu hình Room Database, TypeConverters.\n"
        "• `com.cookingnote.app.data.dao`: 9 Interface Room Data Access Objects.\n"
        "• `com.cookingnote.app.data.entity`: Các thực thể bảng dữ liệu và lớp quan hệ quan hệ (Relations).\n"
        "• `com.cookingnote.app.data.repository`: Lớp trung tâm `CookbookRepository`.\n"
        "• `com.cookingnote.app.data.prefs`: Quản lý lưu trữ cài đặt bằng DataStore Preferences và EncryptedSharedPreferences.\n"
        "• `com.cookingnote.app.ai`: Phân hệ kết nối dịch vụ AI và thuật toán ngoại tuyến `RuleBasedAi`.\n"
        "• `com.cookingnote.app.ui.viewmodel`: 10 ViewModels và `AppViewModelFactory`.\n"
        "• `com.cookingnote.app.ui.screens`: 10 màn hình Composable đại diện cho giao diện người dùng.\n"
        "• `com.cookingnote.app.ui.components`: Các thành phần giao diện dùng chung (`RecipeCard`...).\n"
        "• `com.cookingnote.app.testutil`: Hạ tầng hỗ trợ kiểm thử đơn vị (`MainDispatcherRule`)."
    )

    add_h2("4.3. Giao diện và các màn hình chức năng chính")
    doc.add_paragraph(
        "Ứng dụng Cooking Note sở hữu giao diện cam - kem ấm áp, đậm chất ẩm thực với 10 màn hình chức năng chính:\n"
        "1. Màn hình Trang chủ (HomeScreen): Hiển thị bảng điều khiển tổng thể, thống kê số món ăn trong sổ tay, số món yêu thích và 3 món ăn được gợi ý ngẫu nhiên hôm nay.\n"
        "2. Màn hình Thư viện (LibraryScreen): Duyệt toàn bộ công thức món ăn, hỗ trợ thanh trượt danh mục ngang (Chips) giúp lọc nhanh món canh, xào, kho, luộc, chay.\n"
        "3. Màn hình Chi tiết món (DetailScreen): Trình bày hình ảnh món ăn, thời gian, khẩu phần, danh sách nguyên liệu và các bước làm có đánh số. Nút bấm một chạm đánh dấu \"Yêu thích\", \"Đánh dấu đã nấu\" và \"Chia sẻ công thức\".\n"
        "4. Màn hình Thêm/Sửa công thức (CreateRecipeScreen): Biểu mẫu nhập liệu trực quan cho phép thêm không giới hạn các nguyên liệu và các bước nấu với cơ chế tự động kiểm tra lỗi (validation).\n"
        "5. Màn hình Tủ lạnh (PantryScreen): Hiển thị danh mục các thực phẩm đang có trong nhà, gắn nhãn cảnh báo đỏ với các món có số lượng dưới ngưỡng sắp hết.\n"
        "6. Màn hình Yêu thích (FavoritesScreen): Danh sách các món ăn đã được người dùng gắn sao yêu thích để truy cập nhanh.\n"
        "7. Màn hình Lịch sử nấu (HistoryScreen): Dòng thời gian ghi nhận các bữa ăn đã được nấu kèm ngày giờ và nhận xét độ ngon miệng.\n"
        "8. Màn hình Tìm kiếm (SearchScreen): Thanh tìm kiếm thông minh tự động debounce 300ms, tìm tức thì theo tên món hoặc nguyên liệu sẵn có.\n"
        "9. Màn hình Trợ lý AI (AiScreen): Giao diện chat thời gian thực với trợ lý ảo, tích hợp các phím tắt chọn nhanh câu hỏi và tự động gợi ý món dựa trên thực phẩm đang có trong tủ lạnh.\n"
        "10. Màn hình Cài đặt (SettingsScreen): Cho phép lựa chọn giữa 5 chế độ AI, thiết lập khóa API an toàn và nút bấm sao lưu toàn bộ cơ sở dữ liệu SQLite thành file chia sẻ."
    )

    add_h2("4.4. Đánh giá chất lượng và kết quả kiểm thử tự động (100 Tests Passed)")
    doc.add_paragraph(
        "Để bảo đảm ứng dụng đạt tiêu chuẩn chất lượng sản phẩm thương mại (Production-ready), nhóm đã xây dựng một hệ sinh thái kiểm thử đơn vị toàn diện, chạy 100% độc lập trên JVM với 100 kịch bản kiểm thử (100 Test Cases):"
    )
    doc.add_paragraph(
        "• Kết quả thực tế từ Gradle Test Runner:\n"
        "  Lệnh thực thi: ./gradlew.bat :app:testDebugUnitTest\n"
        "  Tổng số bài test: 100 tests completed\n"
        "  Số bài thất bại: 0 failures (0%)\n"
        "  Số bài bỏ qua: 0 skipped (0%)\n"
        "  Tỷ lệ thành công: 100% PASS\n"
        "  Thời gian thực thi trung bình: ~11.5 giây trên JVM."
    )
    doc.add_paragraph(
        "• Kết quả biên dịch APK sản phẩm (Build Verification):\n"
        "  Lệnh thực thi: ./gradlew.bat :app:assembleDebug\n"
        "  Trạng thái: BUILD SUCCESSFUL in 49s\n"
        "  Tệp APK hoàn chỉnh được sinh ra tại: app/build/outputs/apk/debug/app-debug.apk với dung lượng tối ưu, sẵn sàng cài đặt thử nghiệm."
    )

    # ==========================================
    # KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN
    # ==========================================
    add_h1("KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN")

    add_h2("1. Đánh giá kết quả đạt được")
    doc.add_paragraph(
        "Sau thời gian nghiên cứu và lập trình nghiêm túc, nhóm sinh viên đã hoàn thành xuất sắc toàn bộ các mục tiêu đề ra cho bài tập lớn:\n"
        "• Xây dựng thành công ứng dụng Android Cooking Note hoàn chỉnh, hoạt động mượt mà, giao diện Material 3 bắt mắt và hiện đại.\n"
        "• Ứng dụng đáp ứng trọn vẹn tiêu chí Offline-first, lưu trữ dữ liệu an toàn trên thiết bị thông qua Room Database 2.6.1.\n"
        "• Tái cấu trúc thành công 100% kiến trúc phần mềm sang mô hình chuẩn MVVM + Unidirectional Data Flow, tách biệt hoàn toàn giữa UI và Data Layer.\n"
        "• Tích hợp linh hoạt giữa phân hệ Trí tuệ nhân tạo điện toán đám mây và thuật toán gợi ý ngoại tuyến thông minh.\n"
        "• Xây dựng hệ thống kiểm thử tự động với 100 bài test đạt tỷ lệ vượt qua 100%."
    )

    add_h2("2. Hạn chế của đề tài")
    doc.add_paragraph(
        "Dù đã đạt được những kết quả rất khả quan, ứng dụng vẫn còn một số điểm có thể hoàn thiện hơn nữa:\n"
        "• Tính năng nhận diện hình ảnh món ăn trực tiếp qua Camera (CameraX) mới chỉ tích hợp dependency cấu hình, chưa hoàn thiện giao diện chụp ảnh trực tiếp trong phiên bản hiện tại.\n"
        "• Chưa hỗ trợ đồng bộ dữ liệu đa thiết bị qua tài khoản đám mây (Cloud Sync) do định hướng hiện tại là ưu tiên bảo mật dữ liệu cục bộ."
    )

    add_h2("3. Hướng phát triển trong tương lai")
    doc.add_paragraph(
        "• Tích hợp thị giác máy tính (AI Vision CameraX): Cho phép người dùng chụp ảnh tủ lạnh thực tế, AI tự động quét và thêm nguyên liệu vào danh sách Pantry.\n"
        "• Tính năng tính toán dinh dưỡng (Calories & Macro Tracker): Tự động phân tích hàm lượng Calo, Protein, Carb, Fat cho từng công thức món ăn.\n"
        "• Hỗ trợ hẹn giờ nấu ăn thông minh (Cooking Timer & Voice Assistant): Giúp người dùng vừa nấu nướng vừa nghe hướng dẫn các bước bằng giọng nói tiếng Việt mà không cần chạm vào màn hình."
    )

    # Save Document
    output_path = os.path.join(os.path.dirname(__file__), "BAO_CAO_COOKING_NOTE.docx")
    doc.save(output_path)
    print(f"Report generated successfully at: {output_path}")

if __name__ == "__main__":
    create_report()
