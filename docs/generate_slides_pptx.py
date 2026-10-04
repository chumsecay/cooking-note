# -*- coding: utf-8 -*-
"""
Cooking Note - Presentation Slides Generator (16 Slides - 5 Phases)
Generated with python-pptx according to the 5-phase structure required by the lecturer.
"""

import os
import pptx
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN
from pptx.enum.shapes import MSO_SHAPE

# --- COLOR PALETTE (Warm culinary, executive & high-contrast) ---
COLOR_PRIMARY = RGBColor(0x1B, 0x4F, 0x72)    # Navy Blue #1B4F72
COLOR_ACCENT = RGBColor(0xE6, 0x51, 0x00)     # Culinary Orange #E65100
COLOR_SECONDARY = RGBColor(0x0E, 0x66, 0x55)  # Emerald / Forest Teal #0E6655
COLOR_BG_LIGHT = RGBColor(0xF8, 0xF9, 0xFA)   # Crisp Light Gray #F8F9FA
COLOR_CARD_BG = RGBColor(0xFF, 0xFF, 0xFF)    # Pure White Card #FFFFFF
COLOR_CARD_BORDER = RGBColor(0xE2, 0xE8, 0xF0)# Slate Border #E2E8F0
COLOR_TEXT_DARK = RGBColor(0x1E, 0x29, 0x3B)  # Deep Slate Text #1E293B
COLOR_TEXT_MUTED = RGBColor(0x64, 0x74, 0x8B) # Slate Muted Text #64748B
COLOR_WHITE = RGBColor(0xFF, 0xFF, 0xFF)

def set_slide_bg(prs, slide, color=COLOR_BG_LIGHT):
    """Sets a clean flat background shape for the slide."""
    bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
    bg.fill.solid()
    bg.fill.fore_color.rgb = color
    bg.line.fill.background()
    return bg

def add_header(prs, slide, phase_text, title_text, slide_number):
    """Standardized top banner with Phase Tag, Title, and Slide Counter."""
    # Phase Tag
    tag_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.35), Inches(8.0), Inches(0.35))
    tf_tag = tag_box.text_frame
    tf_tag.word_wrap = True
    tf_tag.margin_left = tf_tag.margin_top = tf_tag.margin_right = tf_tag.margin_bottom = 0
    p_tag = tf_tag.paragraphs[0]
    p_tag.text = phase_text.upper()
    p_tag.font.size = Pt(11)
    p_tag.font.bold = True
    p_tag.font.color.rgb = COLOR_ACCENT

    # Main Title
    title_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.68), Inches(10.5), Inches(0.65))
    tf_t = title_box.text_frame
    tf_t.word_wrap = True
    tf_t.margin_left = tf_t.margin_top = tf_t.margin_right = tf_t.margin_bottom = 0
    p_t = tf_t.paragraphs[0]
    p_t.text = title_text
    p_t.font.size = Pt(21)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_PRIMARY

    # Slide Counter
    num_box = slide.shapes.add_textbox(Inches(11.333), Inches(0.5), Inches(1.2), Inches(0.4))
    tf_num = num_box.text_frame
    tf_num.word_wrap = True
    tf_num.margin_left = tf_num.margin_top = tf_num.margin_right = tf_num.margin_bottom = 0
    p_num = tf_num.paragraphs[0]
    p_num.text = f"{slide_number:02d} / 16"
    p_num.alignment = PP_ALIGN.RIGHT
    p_num.font.size = Pt(13)
    p_num.font.bold = True
    p_num.font.color.rgb = COLOR_TEXT_MUTED

    # Divider Line
    line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.38), Inches(11.733), Inches(0.02))
    line.fill.solid()
    line.fill.fore_color.rgb = COLOR_CARD_BORDER
    line.line.fill.background()

def add_footer(slide, current_page):
    """Subtle bottom branding and page info."""
    footer_box = slide.shapes.add_textbox(Inches(0.8), Inches(7.05), Inches(11.733), Inches(0.3))
    tf = footer_box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
    p = tf.paragraphs[0]
    p.text = "Cooking Note — Sổ tay nấu ăn thông minh  |  Báo cáo BTL Phát triển Ứng dụng Di động  |  Khoa CNTT - CBK"
    p.font.size = Pt(10)
    p.font.color.rgb = COLOR_TEXT_MUTED

def add_card(slide, left, top, width, height, title, items, badge=None, bg_color=COLOR_CARD_BG, border_color=COLOR_CARD_BORDER, title_color=COLOR_PRIMARY):
    """Reusable card component for structured content."""
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = bg_color
    shape.line.color.rgb = border_color
    shape.line.width = Pt(1.2)

    pad_x = Inches(0.25)
    pad_y = Inches(0.2)
    tx_box = slide.shapes.add_textbox(left + pad_x, top + pad_y, width - (pad_x * 2), height - (pad_y * 2))
    tf = tx_box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    first_para = tf.paragraphs[0]
    if badge:
        first_para.text = badge.upper()
        first_para.font.size = Pt(10)
        first_para.font.bold = True
        first_para.font.color.rgb = COLOR_ACCENT
        first_para.space_after = Pt(2)
        p_title = tf.add_paragraph()
    else:
        p_title = first_para

    if title:
        p_title.text = title
        p_title.font.bold = True
        p_title.font.size = Pt(13.5)
        p_title.font.color.rgb = title_color
        p_title.space_after = Pt(6)

    for item in items:
        p = tf.add_paragraph()
        p.text = f"•  {item}"
        p.font.size = Pt(11)
        p.font.color.rgb = COLOR_TEXT_DARK
        p.space_after = Pt(4)

def add_metric_box(slide, left, top, width, height, big_num, label_text, sub_text="", bg_color=COLOR_CARD_BG):
    """Reusable KPI / Metric stat box."""
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = bg_color
    shape.line.color.rgb = COLOR_CARD_BORDER
    shape.line.width = Pt(1.2)

    tx_box = slide.shapes.add_textbox(left + Inches(0.12), top + Inches(0.12), width - Inches(0.24), height - Inches(0.24))
    tf = tx_box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p1 = tf.paragraphs[0]
    p1.text = big_num
    p1.font.bold = True
    p1.font.size = Pt(24)
    p1.font.color.rgb = COLOR_ACCENT
    p1.alignment = PP_ALIGN.CENTER

    p2 = tf.add_paragraph()
    p2.text = label_text
    p2.font.bold = True
    p2.font.size = Pt(11)
    p2.font.color.rgb = COLOR_PRIMARY
    p2.alignment = PP_ALIGN.CENTER
    p2.space_before = Pt(2)

    if sub_text:
        p3 = tf.add_paragraph()
        p3.text = sub_text
        p3.font.size = Pt(9.5)
        p3.font.color.rgb = COLOR_TEXT_MUTED
        p3.alignment = PP_ALIGN.CENTER
        p3.space_before = Pt(1)

def add_screenshot_mockup(slide, screenshots_dir, left, top, width, height, img_filename, label_title):
    """Embeds an actual app screenshot inside a phone frame mockup."""
    frame = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    frame.fill.solid()
    frame.fill.fore_color.rgb = COLOR_WHITE
    frame.line.color.rgb = RGBColor(0xCB, 0xD5, 0xE1)
    frame.line.width = Pt(1.5)

    tag_box = slide.shapes.add_textbox(left + Inches(0.1), top + Inches(0.08), width - Inches(0.2), Inches(0.32))
    tf_t = tag_box.text_frame
    tf_t.word_wrap = True
    tf_t.margin_left = tf_t.margin_top = tf_t.margin_right = tf_t.margin_bottom = 0
    p_t = tf_t.paragraphs[0]
    p_t.text = label_title
    p_t.font.bold = True
    p_t.font.size = Pt(10.5)
    p_t.font.color.rgb = COLOR_PRIMARY
    p_t.alignment = PP_ALIGN.CENTER

    img_path = os.path.join(screenshots_dir, img_filename)
    if os.path.exists(img_path):
        img_left = left + Inches(0.12)
        img_top = top + Inches(0.42)
        img_width = width - Inches(0.24)
        img_height = height - Inches(0.52)
        slide.shapes.add_picture(img_path, img_left, img_top, width=img_width, height=img_height)


# ==========================================
# GIAI ĐOẠN 1: ĐẶT VẤN ĐỀ (SLIDES 1 - 3)
# ==========================================

def build_slide_1(prs, blank_layout):
    """SLIDE 1: Title & Project Identity"""
    s1 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s1, COLOR_PRIMARY)

    top_bar = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, Inches(0.12))
    top_bar.fill.solid()
    top_bar.fill.fore_color.rgb = COLOR_ACCENT
    top_bar.line.fill.background()

    t_box = s1.shapes.add_textbox(Inches(0.8), Inches(0.55), Inches(11.733), Inches(3.2))
    tf1 = t_box.text_frame
    tf1.word_wrap = True

    p = tf1.paragraphs[0]
    p.text = "TRƯỜNG CAO ĐẲNG KỸ THUẬT – CÔNG NGHỆ BÁCH KHOA\nKHOA CÔNG NGHỆ THÔNG TIN"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = RGBColor(0x93, 0xC5, 0xFD)
    p.alignment = PP_ALIGN.CENTER
    p.space_after = Pt(8)

    p2 = tf1.add_paragraph()
    p2.text = "BÁO CÁO ĐỒ ÁN MÔN HỌC: PHÁT TRIỂN ỨNG DỤNG DI ĐỘNG"
    p2.font.size = Pt(14)
    p2.font.bold = True
    p2.font.color.rgb = RGBColor(0xFB, 0xBF, 0x24)
    p2.alignment = PP_ALIGN.CENTER
    p2.space_after = Pt(10)

    p3 = tf1.add_paragraph()
    p3.text = "COOKING NOTE — SỔ TAY NẤU ĂN THÔNG MINH"
    p3.font.size = Pt(30)
    p3.font.bold = True
    p3.font.color.rgb = COLOR_WHITE
    p3.alignment = PP_ALIGN.CENTER
    p3.space_after = Pt(8)

    p4 = tf1.add_paragraph()
    p4.text = "Ứng dụng ghi chép công thức ẩm thực, quản lý tủ lạnh thông minh & trợ lý AI hỗ trợ đa nhà cung cấp"
    p4.font.size = Pt(14)
    p4.font.italic = True
    p4.font.color.rgb = RGBColor(0xE2, 0xE8, 0xF0)
    p4.alignment = PP_ALIGN.CENTER

    info_card = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.8), Inches(3.85), Inches(9.733), Inches(2.9))
    info_card.fill.solid()
    info_card.fill.fore_color.rgb = RGBColor(0x0F, 0x2D, 0x42)
    info_card.line.color.rgb = RGBColor(0x3B, 0x82, 0xF6)
    info_card.line.width = Pt(1.5)

    ic_box = s1.shapes.add_textbox(Inches(2.1), Inches(4.0), Inches(9.133), Inches(2.6))
    tf_ic = ic_box.text_frame
    tf_ic.word_wrap = True

    p_gv = tf_ic.paragraphs[0]
    p_gv.text = "Giảng viên hướng dẫn:  ThS. Lê Văn Quân"
    p_gv.font.size = Pt(15)
    p_gv.font.bold = True
    p_gv.font.color.rgb = RGBColor(0xFB, 0xBF, 0x24)
    p_gv.space_after = Pt(8)

    p_sv_title = tf_ic.add_paragraph()
    p_sv_title.text = "Nhóm sinh viên thực hiện — Lớp TT602-K16LT:"
    p_sv_title.font.size = Pt(13.5)
    p_sv_title.font.bold = True
    p_sv_title.font.color.rgb = COLOR_WHITE
    p_sv_title.space_after = Pt(6)

    p_sv1 = tf_ic.add_paragraph()
    p_sv1.text = "1. Vũ Đình Đạo (Nhóm trưởng)                       2. Ngô Gia Bảo"
    p_sv1.font.size = Pt(13.5)
    p_sv1.font.color.rgb = RGBColor(0xF1, 0xF5, 0xF9)
    p_sv1.space_after = Pt(4)

    p_sv2 = tf_ic.add_paragraph()
    p_sv2.text = "3. Nguyễn Tiến Dũng                                4. Lê Đức Trọng"
    p_sv2.font.size = Pt(13.5)
    p_sv2.font.color.rgb = RGBColor(0xF1, 0xF5, 0xF9)
    p_sv2.space_after = Pt(10)

    p_tech_badges = tf_ic.add_paragraph()
    p_tech_badges.text = "✦ Kotlin 2.0  ✦ Jetpack Compose M3  ✦ Room Database v3  ✦ MVVM + UDF  ✦ 100% Offline-First"
    p_tech_badges.font.size = Pt(11.5)
    p_tech_badges.font.color.rgb = RGBColor(0x93, 0xC5, 0xFD)

def build_slide_2(prs, blank_layout):
    """SLIDE 2: Ly do chon de tai & Thach thuc"""
    s2 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s2)
    add_header(prs, s2, "Giai đoạn 1: Đặt vấn đề", "1. Lý do chọn đề tài & Thách thức thực tiễn", 2)
    add_footer(s2, 2)

    add_card(s2, Inches(0.8), Inches(1.6), Inches(5.7), Inches(5.2),
             "VẤN ĐỀ CỦA NGƯỜI NỘI TRỢ HIỆN ĐẠI",
             [
                 "Áp lực thực đơn hàng ngày: Câu hỏi đắn đo hôm nay ăn gì gây mất nhiều thời gian, lặp món nhàm chán.",
                 "Lãng phí nguyên liệu trong bếp: Thực phẩm mua về tích trữ trong tủ lạnh dễ bị quên lãng, quá hạn sử dụng gây lãng phí kinh tế.",
                 "Mất mạng khi đang nấu ăn: Gian bếp hoặc tầng hầm thường có sóng Wi-Fi/4G yếu; các ứng dụng đám mây bị gián đoạn tra cứu.",
                 "Bất tiện từ ứng dụng hiện tại: Tràn ngập quảng cáo video, thao tác phức tạp, thiếu tính năng ghi chú khẩu vị riêng của gia đình."
             ],
             badge="Thực trạng & Nhu cầu",
             border_color=RGBColor(0xFD, 0xBA, 0x74),
             title_color=COLOR_ACCENT)

    add_card(s2, Inches(6.8), Inches(1.6), Inches(5.7), Inches(5.2),
             "GIẢI PHÁP ĐỘT PHÁ CỦA COOKING NOTE",
             [
                 "Kiến trúc Offline-First thực thụ: Toàn bộ công thức lưu trữ 100% cục bộ bằng SQLite Room; mở app tra cứu tức thì không cần Internet.",
                 "Quản lý Tủ lạnh thông minh (Pantry): Kiểm kê số lượng, hạn dùng và tự động gắn nhãn cảnh báo đỏ với nguyên liệu sắp hết.",
                 "Trợ lý AI đa nhà cung cấp: Gợi ý món ăn thông minh bằng cách tự động nạp danh sách thực phẩm trong tủ lạnh vào ngữ cảnh chat.",
                 "Thuật toán Fail-Safe ngoại tuyến (RuleBasedAi): Vẫn đưa ra gợi ý nấu ăn chuẩn xác theo nguyên liệu ngay cả khi mất mạng hoặc không có API Key."
             ],
             badge="Giải pháp đề xuất",
             border_color=RGBColor(0x6E, 0xE7, 0xB7),
             title_color=COLOR_SECONDARY)

def build_slide_3(prs, blank_layout):
    """SLIDE 3: Muc tieu & Pham vi san pham"""
    s3 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s3)
    add_header(prs, s3, "Giai đoạn 1: Đặt vấn đề", "2. Mục tiêu nghiên cứu & Phạm vi sản phẩm", 3)
    add_footer(s3, 3)

    add_card(s3, Inches(0.8), Inches(1.6), Inches(3.65), Inches(5.2),
             "🎯 Mục tiêu Chức năng",
             [
                 "Quản lý trọn vẹn vòng đời công thức ẩm thực: CRUD món ăn, định lượng nguyên liệu, các bước nấu đánh số kèm mẹo.",
                 "Quản lý kho tủ lạnh (Pantry): Theo dõi tồn kho thực tế, cảnh báo chạm ngưỡng đỏ lowStockThreshold.",
                 "Trợ lý AI thông minh: Tích hợp 4 cổng API Cloud LLM và bộ quy tắc luật cục bộ RuleBasedAi.",
                 "Tìm kiếm tức thì: Lọc món ăn nhanh với kỹ thuật Debounce 300ms chống giật lag."
             ],
             badge="Tính năng cốt lõi")

    add_card(s3, Inches(4.84), Inches(1.6), Inches(3.65), Inches(5.2),
             "⚡ Mục tiêu Kỹ thuật",
             [
                 "Hiện đại hóa 100% giao diện bằng Jetpack Compose Material 3, loại bỏ hoàn toàn XML.",
                 "Tuân thủ nghiêm ngặt mô hình MVVM + Unidirectional Data Flow (UDF) chuẩn Google.",
                 "Bảo mật khóa API cấp quân đội bằng EncryptedSharedPreferences (AES-256-GCM Keystore).",
                 "Đảm bảo chất lượng tuyệt đối: Đạt tỷ lệ 135/135 Unit Test tự động vượt qua 100% trên JVM."
             ],
             badge="Tiêu chuẩn kỹ thuật")

    add_card(s3, Inches(8.88), Inches(1.6), Inches(3.65), Inches(5.2),
             "📍 Phạm vi & Đối tượng",
             [
                 "Đối tượng người dùng: Người nội trợ gia đình, sinh viên sống tự lập, người đam mê nấu ăn tại nhà.",
                 "Nền tảng hỗ trợ: Hệ điều hành Android (hỗ trợ từ Android 8.0 Oreo đến Android 15, compileSdk 35).",
                 "Phạm vi lưu trữ: CSDL nội bộ SQLite Room, hỗ trợ sao lưu và phục hồi dữ liệu an toàn ra tệp.",
                 "Mô hình AI kép: Vừa hỗ trợ Cloud LLM linh hoạt, vừa hoạt động ngoại tuyến an toàn dữ liệu."
             ],
             badge="Ranh giới triển khai")


# ==========================================
# GIAI ĐOẠN 2: PHÂN TÍCH & THIẾT KẾ (SLIDES 4 - 6)
# ==========================================

def build_slide_4(prs, blank_layout):
    """SLIDE 4: Yeu cau chuc nang 5 phan he"""
    s4 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s4)
    add_header(prs, s4, "Giai đoạn 2: Phân tích & Thiết kế", "3. Phân tích yêu cầu chức năng (5 Phân hệ chính)", 4)
    add_footer(s4, 4)

    add_card(s4, Inches(0.8), Inches(1.6), Inches(5.7), Inches(2.45),
             "Phân hệ 1: Quản lý Công thức (Recipe)",
             [
                 "Duyệt, xem chi tiết món ăn với hình ảnh, thời gian nấu, khẩu phần và độ khó.",
                 "Định lượng nguyên liệu chi tiết, các bước nấu đánh số kèm mẹo thực hiện (Tips).",
                 "Tạo mới, chỉnh sửa món ăn; đánh dấu Yêu thích (Favorites) và lịch sử đã nấu."
             ],
             badge="CRUD & Tương tác")

    add_card(s4, Inches(6.8), Inches(1.6), Inches(5.7), Inches(2.45),
             "Phân hệ 2: Quản lý Tủ lạnh (Pantry)",
             [
                 "Kiểm kê danh sách thực phẩm trong bếp kèm số lượng và đơn vị đo linh hoạt.",
                 "Tự động tính toán và gắn nhãn cảnh báo đỏ đối với nguyên liệu sắp hết hàng.",
                 "Kết nối dữ liệu trực tiếp với AI để gợi ý món ăn có thể nấu ngay từ đồ sẵn có."
             ],
             badge="Kho thực phẩm thông minh")

    add_card(s4, Inches(0.8), Inches(4.3), Inches(3.65), Inches(2.55),
             "Phân hệ 3: Trợ lý AI",
             [
                 "Chatbot ẩm thực nạp ngữ cảnh tủ lạnh + món yêu thích vào System Prompt.",
                 "Hỗ trợ 4 Cloud Providers (OpenAI, Gemini, Claude, DeepSeek).",
                 "Cơ chế dự phòng RuleBasedAi hoạt động 100% khi mất mạng."
             ],
             badge="Multi-Provider & Offline")

    add_card(s4, Inches(4.84), Inches(4.3), Inches(3.65), Inches(2.55),
             "Phân hệ 4: Tìm kiếm & Lọc",
             [
                 "Thanh tìm kiếm phản ứng nhanh tích hợp Debounce 300ms chống giật lag.",
                 "Tìm kiếm đa trường: Theo tên món, mô tả và tên nguyên liệu thành phần.",
                 "Lọc nhanh công thức theo danh mục món ăn (Canh, mặn, xào, chay...)."
             ],
             badge="Truy vấn tức thì")

    add_card(s4, Inches(8.88), Inches(4.3), Inches(3.65), Inches(2.55),
             "Phân hệ 5: Cài đặt & Sao lưu",
             [
                 "Chuyển đổi Provider, Model ID, cấu hình Endpoint URL và nạp Remote Config.",
                 "Bảo mật khóa API bằng Keystore AES-256-GCM an toàn cấp quân đội.",
                 "Sao lưu / Phục hồi cơ sở dữ liệu SQLite Room bất đồng bộ qua FileProvider."
             ],
             badge="Quản trị & An toàn")

def build_slide_5(prs, blank_layout):
    """SLIDE 5: Yeu cau phi chuc nang & Bao mat"""
    s5 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s5)
    add_header(prs, s5, "Giai đoạn 2: Phân tích & Thiết kế", "4. Yêu cầu phi chức năng, Hiệu năng & Bảo mật", 5)
    add_footer(s5, 5)

    add_card(s5, Inches(0.8), Inches(1.6), Inches(5.7), Inches(5.2),
             "⚡ TỐI ƯU HÓA HIỆU NĂNG & TRẢI NGHIỆM",
             [
                 "Thời gian khởi động ứng dụng (Cold Start): Đạt dưới 1.5 giây; chuyển đổi giữa các màn hình duy trì chuẩn 60fps mượt mà.",
                 "Kỹ thuật Debounce 300ms: Khi người dùng gõ tìm kiếm, hệ thống chỉ kích hoạt truy vấn sau khi ngừng gõ 300ms, giảm hơn 85% số lượt đọc đĩa DB không cần thiết.",
                 "Xử lý đa luồng bất đồng bộ: 100% tác vụ I/O đĩa (Room Database) và gọi mạng AI chạy trên luồng nền Dispatchers.IO, triệt tiêu hoàn toàn hiện tượng nghẽn UI Thread.",
                 "Đồ họa Vector tối ưu hiệu năng: Sử dụng Material Icons và Vector Graphics dựng sẵn, giảm 100% chi phí I/O mạng và ngăn ngừa tràn bộ nhớ RAM (OOM) trên thiết bị cấu hình thấp."
             ],
             badge="Hiệu năng & Tài nguyên",
             title_color=COLOR_PRIMARY)

    add_card(s5, Inches(6.8), Inches(1.6), Inches(5.7), Inches(5.2),
             "🔒 AN TOÀN THÔNG TIN & TOÀN VẸN DỮ LIỆU",
             [
                 "Bảo mật khóa bí mật API: Sử dụng Jetpack Security EncryptedSharedPreferences với thuật toán mã hóa khóa đối xứng AES-256-GCM, quản lý bởi phần cứng Android Keystore.",
                 "Xác thực phiên làm việc an toàn: Quy trình đăng nhập/đăng ký giao tiếp qua REST backend (api/v1/auth); xác thực mật khẩu diễn ra phía máy chủ, client không lưu mật khẩu thô.",
                 "Toàn vẹn sao lưu CSDL (SQLite WAL): Khi thực hiện xuất file backup, hệ thống thực hiện checkpoint WAL nhằm đảm bảo dữ liệu ghi gần nhất được đồng bộ hoàn toàn ra đĩa.",
                 "Tính sẵn sàng cao (High Availability): 100% chức năng xem, thêm, sửa, quản lý tủ lạnh và tìm kiếm hoạt động ổn định khi offline, loại bỏ điểm nghẽn phụ thuộc mạng."
             ],
             badge="Bảo mật & Toàn vẹn",
             title_color=COLOR_SECONDARY)

def build_slide_6(prs, blank_layout):
    """SLIDE 6: Thiet ke CSDL Room 10 bang"""
    s6 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s6)
    add_header(prs, s6, "Giai đoạn 2: Phân tích & Thiết kế", "5. Thiết kế Cơ sở Dữ liệu Room (10 Bảng & 9 DAOs)", 6)
    add_footer(s6, 6)

    add_card(s6, Inches(0.8), Inches(1.6), Inches(5.7), Inches(3.9),
             "NHÓM THỰC THỂ CỐT LÕI (5 BẢNG)",
             [
                 "recipes: Bảng món ăn chính (id PK, title, description, prepTime, cookTime, servings, difficulty, categoryId FK, isFavorite).",
                 "ingredients: Nguyên liệu món ăn (id PK, recipeId FK CASCADE, name, amount, unit).",
                 "steps: Các bước thực hiện (id PK, recipeId FK CASCADE, stepNumber, instruction, tip).",
                 "categories: Danh mục ẩm thực (id PK, name, iconRes, colorHex).",
                 "pantry_items: Kho thực phẩm tủ lạnh (id PK, name, quantity, unit, lowStockThreshold, expiryDate)."
             ],
             badge="Quản lý Công thức & Tủ lạnh")

    add_card(s6, Inches(6.8), Inches(1.6), Inches(5.7), Inches(3.9),
             "NHÓM QUAN HỆ, LỊCH SỬ & AI (5 BẢNG)",
             [
                 "tags: Nhãn ẩm thực phân loại (id PK, name - ví dụ: #ĂnChay, #Nhanh, #MónCay).",
                 "recipe_tag_cross_ref: Bảng liên kết trung gian (recipeId, tagId) thiết kế quan hệ N-N.",
                 "cook_history: Nhật ký nấu nướng gia đình (id PK, recipeId FK, cookedAt, rating, notes).",
                 "chat_messages: Lưu trữ toàn bộ lịch sử hội thoại với trợ lý AI (id PK, role, content, timestamp).",
                 "ai_query_logs: Nhật ký giám sát chất lượng AI (id PK, provider, model, latencyMs, isSuccess)."
             ],
             badge="Mở rộng & Trí tuệ nhân tạo")

    hl_bar = s6.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(5.65), Inches(11.733), Inches(1.2))
    hl_bar.fill.solid()
    hl_bar.fill.fore_color.rgb = COLOR_CARD_BG
    hl_bar.line.color.rgb = COLOR_SECONDARY
    hl_bar.line.width = Pt(1.5)

    hl_box = s6.shapes.add_textbox(Inches(1.0), Inches(5.75), Inches(11.333), Inches(1.0))
    tf_hl = hl_box.text_frame
    tf_hl.word_wrap = True
    p_hl1 = tf_hl.paragraphs[0]
    p_hl1.text = "✦ ĐIỂM SÁNG THIẾT KẾ CƠ SỞ DỮ LIỆU ROOM DATABASE V3"
    p_hl1.font.bold = True
    p_hl1.font.size = Pt(12)
    p_hl1.font.color.rgb = COLOR_SECONDARY

    p_hl2 = tf_hl.add_paragraph()
    p_hl2.text = "• Ràng buộc Foreign Keys ON DELETE CASCADE tự động dọn sạch nguyên liệu và các bước khi xóa công thức, chống dữ liệu rác.\n• Đánh chỉ mục Index trên cột title và ingredients giúp câu lệnh truy vấn tìm kiếm SQL LIKE phản hồi tức thì dưới 20ms."
    p_hl2.font.size = Pt(11)
    p_hl2.font.color.rgb = COLOR_TEXT_DARK
    p_hl2.space_before = Pt(2)


# ==========================================
# GIAI ĐOẠN 3: CÀI ĐẶT & TRIỂN KHAI (SLIDES 7 - 8)
# ==========================================

def build_slide_7(prs, blank_layout):
    """SLIDE 7: Kien truc MVVM + UDF"""
    s7 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s7)
    add_header(prs, s7, "Giai đoạn 3: Cài đặt & Triển khai", "6. Kiến trúc Phần mềm MVVM & Luồng Dữ liệu Một Chiều (UDF)", 7)
    add_footer(s7, 7)

    add_card(s7, Inches(0.8), Inches(1.6), Inches(3.65), Inches(3.8),
             "🎨 Tầng Giao diện (UI)",
             [
                 "100% Jetpack Compose khai báo (Stateless Content + Stateful Screen).",
                 "Nhận duy nhất một đối tượng UiState bất biến (Immutable State).",
                 "Mọi tương tác chạm phát sự kiện onAction ngược lên ViewModel.",
                 "Triệt tiêu 100% việc gọi trực tiếp Database hay Network từ Composable."
             ],
             badge="Giao diện thuần khiết")

    add_card(s7, Inches(4.84), Inches(1.6), Inches(3.65), Inches(3.8),
             "🧠 Tầng ViewModel",
             [
                 "Quản lý 10 ViewModel độc lập tương ứng với các phân hệ màn hình.",
                 "Tiếp nhận Event từ UI, thực thi logic nghiệp vụ qua Coroutine viewModelScope.",
                 "Phát dữ liệu phản ứng qua StateFlow với cấu hình an toàn WhileSubscribed.",
                 "Thu thập dữ liệu an toàn với collectAsStateWithLifecycle() chống rò rỉ."
             ],
             badge="Xử lý nghiệp vụ")

    add_card(s7, Inches(8.88), Inches(1.6), Inches(3.65), Inches(3.8),
             "💾 Tầng Dữ liệu (Data)",
             [
                 "CookbookRepository đóng vai trò Single Source of Truth duy nhất.",
                 "Điều phối truy vấn bất đồng bộ giữa 9 Room DAOs và AI Subsystem.",
                 "Chuyển đổi dữ liệu Entity nội bộ sang Domain Model sạch cho ViewModel.",
                 "Hỗ trợ giao dịch Room Transaction đảm bảo dữ liệu nguyên tử (ACID)."
             ],
             badge="Nguồn chân lý dữ liệu")

    arch_bar = s7.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(5.6), Inches(11.733), Inches(1.2))
    arch_bar.fill.solid()
    arch_bar.fill.fore_color.rgb = COLOR_CARD_BG
    arch_bar.line.color.rgb = COLOR_ACCENT
    arch_bar.line.width = Pt(1.5)

    ab_box = s7.shapes.add_textbox(Inches(1.0), Inches(5.7), Inches(11.333), Inches(1.0))
    tf_ab = ab_box.text_frame
    tf_ab.word_wrap = True
    p_ab1 = tf_ab.paragraphs[0]
    p_ab1.text = "✦ MANUAL DEPENDENCY INJECTION: KHÔNG HILT / KOIN CỒNG KỀNH"
    p_ab1.font.bold = True
    p_ab1.font.size = Pt(12)
    p_ab1.font.color.rgb = COLOR_ACCENT

    p_ab2 = tf_ab.add_paragraph()
    p_ab2.text = "• AppContainer khởi tạo tập trung toàn bộ Database, Repository và AiService từ CookingNoteApp.\n• AppViewModelFactory cung cấp ViewModel sạch sẽ, giúp 100% các lớp Unit Test có thể chạy trực tiếp trên máy ảo JVM với tốc độ vượt trội mà không cần giả lập Android."
    p_ab2.font.size = Pt(11)
    p_ab2.font.color.rgb = COLOR_TEXT_DARK
    p_ab2.space_before = Pt(2)

def build_slide_8(prs, blank_layout):
    """SLIDE 8: Cong nghe su dung (Tech Stack)"""
    s8 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s8)
    add_header(prs, s8, "Giai đoạn 3: Cài đặt & Triển khai", "7. Công nghệ & Thư viện Sử dụng (Tech Stack)", 8)
    add_footer(s8, 8)

    add_card(s8, Inches(0.8), Inches(1.6), Inches(5.7), Inches(2.5),
             "📱 Ngôn ngữ & Nền tảng Core",
             [
                 "Kotlin 2.0.21: Sử dụng Coroutines 1.9.0, StateFlow và SharedFlow xử lý phản ứng bất đồng bộ.",
                 "Target SDK 35 (Android 15), Min SDK 26 (Android 8.0 Oreo), JVM Target 17.",
                 "Gradle Wrapper 9.3.0 với catalog gradle/libs.versions.toml quản lý thư viện tập trung."
             ],
             badge="Core Platform")

    add_card(s8, Inches(6.8), Inches(1.6), Inches(5.7), Inches(2.5),
             "🎨 Giao diện & Đồ họa (UI/UX)",
             [
                 "Jetpack Compose (BOM 2024.12.01) + Material 3 Design: Hỗ trợ Light & Dark Theme mượt mà.",
                 "Navigation Compose: Quản lý điều hướng an toàn tham số với sealed class Route.",
                 "Material Icons & Vector Graphics: Hệ thống biểu tượng vector Material 3 tối ưu hóa render, hiển thị sắc nét trên mọi độ phân giải màn hình."
             ],
             badge="Declarative UI")

    add_card(s8, Inches(0.8), Inches(4.35), Inches(5.7), Inches(2.5),
             "🗄️ Cơ sở Dữ liệu & Lưu trữ",
             [
                 "Room Database 2.6.1 + KSP: Kiểm tra tính đúng đắn của SQL ngay lúc biên dịch (Compile-time).",
                 "EncryptedSharedPreferences (Jetpack Security Tink): Mã hóa khóa API cấp quân đội AES-256-GCM.",
                 "DataStore Preferences: Lưu trữ tùy biến cấu hình giao diện và tùy chọn người dùng."
             ],
             badge="Local Storage")

    add_card(s8, Inches(6.8), Inches(4.35), Inches(5.7), Inches(2.5),
             "🌐 Mạng & Bộ công cụ Kiểm thử",
             [
                 "OkHttp 4.12.0 + Moshi 1.15.1: Xử lý kết nối REST API và đa cổng dịch vụ AI (OpenAI, Gemini, Claude).",
                 "Hạ tầng Unit Test: JUnit 4, MockK 1.13.13, Turbine 1.2.0, Coroutines-Test chạy độc lập trên JVM.",
                 "Chuyển đổi Base64 chuẩn java.util.Base64 tương thích 100% môi trường kiểm thử máy chủ."
             ],
             badge="Network & Quality")


# ==========================================
# GIAI ĐOẠN 4: DEMO SẢN PHẨM (SLIDES 9 - 12)
# ==========================================

def build_slide_9(prs, blank_layout, screenshots_dir):
    """SLIDE 9: Demo Trang chu & Thu vien"""
    s9 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s9)
    add_header(prs, s9, "Giai đoạn 4: Demo sản phẩm", "8. Trải nghiệm Thực tế: Trang chủ & Thư viện Công thức", 9)
    add_footer(s9, 9)

    add_screenshot_mockup(s9, screenshots_dir, Inches(0.8), Inches(1.6), Inches(2.4), Inches(5.2), "home.png", "Màn hình Trang chủ")
    add_screenshot_mockup(s9, screenshots_dir, Inches(3.45), Inches(1.6), Inches(2.4), Inches(5.2), "library.png", "Thư viện Công thức")

    add_card(s9, Inches(6.2), Inches(1.6), Inches(6.333), Inches(2.5),
             "ĐIỂM NỔI BẬT: TRANG CHỦ (HOME SCREEN)",
             [
                 "Hiển thị lời chào cá nhân hóa theo thời gian trong ngày (Sáng, Trưa, Tối).",
                 "Gợi ý nhanh món ăn hôm nay dựa trên khẩu vị và thực đơn thịnh hành.",
                 "Bộ sưu tập món ăn mới nhất và danh sách phân loại ẩm thực trực quan sinh động.",
                 "Lối tắt nhanh dẫn thẳng tới trợ lý AI gợi ý món và kiểm tra kho tủ lạnh."
             ],
             badge="Tổng quan & Khám phá",
             title_color=COLOR_PRIMARY)

    add_card(s9, Inches(6.2), Inches(4.3), Inches(6.333), Inches(2.5),
             "ĐIỂM NỔI BẬT: THƯ VIỆN CÔNG THỨC (LIBRARY)",
             [
                 "Hiển thị danh sách công thức dạng lưới 2 cột hiện đại, tối ưu không gian hiển thị.",
                 "Thẻ món ăn thể hiện đầy đủ: Ảnh đại diện, tên món, thời gian nấu và độ khó.",
                 "Bộ lọc nhanh theo danh mục (Món chính, Canh, Ăn vặt, Đồ uống, Món chay...).",
                 "Tương tác chạm một chạm để bật/tắt yêu thích (Favorite) cập nhật tức thì."
             ],
             badge="Quản lý bộ sưu tập",
             title_color=COLOR_SECONDARY)

def build_slide_10(prs, blank_layout, screenshots_dir):
    """SLIDE 10: Demo Chi tiet mon & Tao cong thuc"""
    s10 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s10)
    add_header(prs, s10, "Giai đoạn 4: Demo sản phẩm", "9. Trải nghiệm Thực tế: Chi tiết Món & Biên tập Công thức", 10)
    add_footer(s10, 10)

    add_screenshot_mockup(s10, screenshots_dir, Inches(0.8), Inches(1.6), Inches(2.4), Inches(5.2), "detail.png", "Chi tiết Món ăn")
    add_screenshot_mockup(s10, screenshots_dir, Inches(3.45), Inches(1.6), Inches(2.4), Inches(5.2), "create_recipe.png", "Tạo mới & Biên tập")

    add_card(s10, Inches(6.2), Inches(1.6), Inches(6.333), Inches(2.5),
             "ĐIỂM NỔI BẬT: CHI TIẾT CÔNG THỨC (RECIPE DETAIL)",
             [
                 "Bố cục hình ảnh Hero tràn viền kết hợp thông số: Thời gian chuẩn bị, nấu, khẩu phần.",
                 "Danh sách nguyên liệu định lượng chi tiết kèm đơn vị đo lường trực quan.",
                 "Các bước thực hiện được đánh số thứ tự rõ ràng kèm mẹo vặt nấu nướng thực tế.",
                 "Nút bắt đầu nấu ghi nhận thời gian và tự động lưu vào bảng lịch sử cook_history."
             ],
             badge="Hướng dẫn nấu nướng",
             title_color=COLOR_PRIMARY)

    add_card(s10, Inches(6.2), Inches(4.3), Inches(6.333), Inches(2.5),
             "ĐIỂM NỔI BẬT: TẠO MỚI & CHỈNH SỬA (RECIPE EDITOR)",
             [
                 "Trình biên tập thống nhất dùng chung cho cả tính năng Tạo mới và Chỉnh sửa.",
                 "Cơ chế validation đầu vào chặt chẽ: Ngăn chặn lưu công thức rỗng hoặc sai định dạng.",
                 "Thêm bớt nguyên liệu và các bước hướng dẫn động một cách mượt mà và trực quan.",
                 "Lưu trữ cục bộ nguyên tử (Atomic Room Transaction), đảm bảo dữ liệu không bị thất thoát."
             ],
             badge="Sáng tạo ẩm thực",
             title_color=COLOR_SECONDARY)

def build_slide_11(prs, blank_layout, screenshots_dir):
    """SLIDE 11: Demo Quan ly Tu lanh & Tim kiem"""
    s11 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s11)
    add_header(prs, s11, "Giai đoạn 4: Demo sản phẩm", "10. Trải nghiệm Thực tế: Quản lý Tủ lạnh & Tìm kiếm Debounce", 11)
    add_footer(s11, 11)

    add_screenshot_mockup(s11, screenshots_dir, Inches(0.8), Inches(1.6), Inches(2.4), Inches(5.2), "pantry.png", "Quản lý Tủ lạnh")
    add_screenshot_mockup(s11, screenshots_dir, Inches(3.45), Inches(1.6), Inches(2.4), Inches(5.2), "search.png", "Tìm kiếm Tức thì")

    add_card(s11, Inches(6.2), Inches(1.6), Inches(6.333), Inches(2.5),
             "ĐIỂM NỔI BẬT: QUẢN LÝ TỦ LẠNH (PANTRY MANAGER)",
             [
                 "Kiểm soát số lượng tồn kho nguyên liệu trong gian bếp theo thời gian thực.",
                 "Cảnh báo thông minh: Tự động đánh dấu màu đỏ khi số lượng chạm ngưỡng lowStockThreshold.",
                 "Hỗ trợ phân loại nhóm thực phẩm: Thịt cá, rau củ, gia vị, đồ hộp khô.",
                 "Nút kích hoạt gợi ý món ngay từ tủ lạnh chuyển tiếp sang trợ lý AI."
             ],
             badge="Kiểm soát kho bếp",
             title_color=COLOR_PRIMARY)

    add_card(s11, Inches(6.2), Inches(4.3), Inches(6.333), Inches(2.5),
             "ĐIỂM NỔI BẬT: TÌM KIẾM ĐA NĂNG (SMART SEARCH)",
             [
                 "Tích hợp toán tử phản ứng Debounce 300ms chống giật lag và giảm tải bộ nhớ.",
                 "Khả năng tìm kiếm linh hoạt: Tra cứu theo tên món ăn, mô tả hoặc tên nguyên liệu thành phần.",
                 "Hiển thị trạng thái rỗng (Empty State) thân thiện kèm gợi ý món nổi bật khi không có kết quả.",
                 "Lọc kết quả kết hợp đa tiêu chí: Lọc theo danh mục, thời gian nấu và độ khó."
             ],
             badge="Truy vấn thông minh",
             title_color=COLOR_SECONDARY)

def build_slide_12(prs, blank_layout, screenshots_dir):
    """SLIDE 12: Demo Tro ly AI & Cai dat bao mat"""
    s12 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s12)
    add_header(prs, s12, "Giai đoạn 4: Demo sản phẩm", "11. Trải nghiệm Thực tế: Trợ lý Ẩm thực AI & Cài đặt Bảo mật", 12)
    add_footer(s12, 12)

    add_screenshot_mockup(s12, screenshots_dir, Inches(0.8), Inches(1.6), Inches(2.4), Inches(5.2), "ai_chat.png", "Trợ lý Ẩm thực AI")
    add_screenshot_mockup(s12, screenshots_dir, Inches(3.45), Inches(1.6), Inches(2.4), Inches(5.2), "settings_ai.png", "Cài đặt & Bảo mật")

    add_card(s12, Inches(6.2), Inches(1.6), Inches(6.333), Inches(2.5),
             "ĐIỂM NỔI BẬT: TRỢ LÝ ẨM THỰC AI (AI ASSISTANT)",
             [
                 "Khởi tạo hội thoại theo thời gian thực với định dạng bong bóng chat hiện đại.",
                 "Tự động nạp danh sách đồ trong tủ lạnh và món yêu thích vào ngữ cảnh câu hỏi.",
                 "Đề xuất công thức chi tiết, hướng dẫn sơ chế và các giải pháp thay thế gia vị.",
                 "Cơ chế Fail-Safe: Tự động chuyển sang RuleBasedAi khi mất mạng, không gây lỗi giao diện."
             ],
             badge="Hội thoại thông minh",
             title_color=COLOR_PRIMARY)

    add_card(s12, Inches(6.2), Inches(4.3), Inches(6.333), Inches(2.5),
             "ĐIỂM NỔI BẬT: CÀI ĐẶT & BẢO MẬT (SETTINGS & SECURITY)",
             [
                 "Tùy chọn linh hoạt 4 nhà cung cấp AI: OpenAI, Google Gemini, Claude, DeepSeek.",
                 "Khóa API được mã hóa an toàn bằng AES-256-GCM qua Android Keystore phần cứng.",
                 "Hỗ trợ cấu hình đám mây từ xa (AiRemoteConfig) nạp endpoint mà không cần sửa code.",
                 "Trung tâm sao lưu và khôi phục CSDL an toàn kèm cơ chế đồng bộ checkpoint WAL."
             ],
             badge="An toàn & Cấu hình",
             title_color=COLOR_SECONDARY)


# ==========================================
# GIAI ĐOẠN 5: KẾT LUẬN & HƯỚNG PHÁT TRIỂN (SLIDES 13 - 16)
# ==========================================

def build_slide_13(prs, blank_layout):
    """SLIDE 13: Dam bao chat luong & 135/135 Unit tests pass"""
    s13 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s13)
    add_header(prs, s13, "Giai đoạn 5: Kết luận & Hướng phát triển", "12. Đảm bảo Chất lượng & Kiểm thử Tự động (135/135 Tests Pass)", 13)
    add_footer(s13, 13)

    add_metric_box(s13, Inches(0.8), Inches(1.6), Inches(2.7), Inches(1.4), "135 / 135", "Unit Tests Vượt Qua", "Tỷ lệ đạt 100% tuyệt đối")
    add_metric_box(s13, Inches(3.8), Inches(1.6), Inches(2.7), Inches(1.4), "23 Files", "Tệp Kiểm Thử Độc Lập", "Bao phủ toàn diện các tầng")
    add_metric_box(s13, Inches(6.8), Inches(1.6), Inches(2.7), Inches(1.4), "0 ms", "Độ trễ Giả lập Emulator", "Thực thi trực tiếp trên máy ảo JVM")
    add_metric_box(s13, Inches(9.8), Inches(1.6), Inches(2.733), Inches(1.4), "100%", "Tuân thủ Kiến trúc MVVM", "Bộ test kiến trúc tự động giám sát")

    add_card(s13, Inches(0.8), Inches(3.2), Inches(5.7), Inches(3.6),
             "CHIẾN LƯỢC KIỂM THỬ ĐƠN VỊ ĐA TẦNG",
             [
                 "Tầng ViewModel (Turbine & MockK): Kiểm thử hành vi phát StateFlow, xử lý luồng sự kiện UI và trạng thái tải dữ liệu Loading / Success / Error.",
                 "Tầng Dữ liệu (Room In-Memory DB): Kiểm thử các câu lệnh DAO, ràng buộc khóa ngoại Foreign Keys và logic giao dịch nguyên tử.",
                 "Tầng AI Subsystem: Kiểm định bộ quy tắc ngoại tuyến RuleBasedAi, xác thực ngữ cảnh tủ lạnh và kiểm thử phân tích JSON kết quả.",
                 "Tương thích môi trường JVM: Sử dụng java.util.Base64 và Test Dispatcher, giúp toàn bộ 135 test chạy hoàn tất chỉ trong vài giây."
             ],
             badge="Bảo đảm chất lượng mã nguồn",
             title_color=COLOR_PRIMARY)

    add_card(s13, Inches(6.8), Inches(3.2), Inches(5.7), Inches(3.6),
             "KIỂM TRA KIẾN TRÚC & ADVERSARIAL TESTS",
             [
                 "Milestone3ArchitectureConformanceTest: Bộ kiểm tra tự động quét mã nguồn bằng regex để đảm bảo 100% màn hình tuân thủ quy tắc Stateless Content.",
                 "Milestone3ChallengerAdversarialTest: Bộ kiểm tra đóng vai trò đối nghịch (Challenger), cố tình đưa dữ liệu dị thường để kiểm tra tính bền vững.",
                 "Giám sát chu kỳ sống ViewModel: Rà soát việc sử dụng collectAsStateWithLifecycle(), ngăn chặn tuyệt đối lỗi rò rỉ bộ nhớ (Memory Leak).",
                 "Khả năng mở rộng bền vững: Đảm bảo khi bổ sung tính năng mới, hệ thống không phá vỡ các hợp đồng kiến trúc đã cam kết."
             ],
             badge="Bảo vệ toàn vẹn kiến trúc",
             title_color=COLOR_SECONDARY)

def build_slide_14(prs, blank_layout):
    """SLIDE 14: Tong ket ket qua & Han che thuc te"""
    s14 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s14)
    add_header(prs, s14, "Giai đoạn 5: Kết luận & Hướng phát triển", "13. Tổng kết Kết quả Đạt được & Hạn chế Thực tế", 14)
    add_footer(s14, 14)

    add_card(s14, Inches(0.8), Inches(1.6), Inches(5.7), Inches(5.2),
             "🏆 KẾT QUẢ ĐẠT ĐƯỢC NỔI BẬT",
             [
                 "Xây dựng thành công ứng dụng Android Cooking Note hoàn chỉnh, mượt mà và trực quan với 100% Jetpack Compose Material 3 hiện đại.",
                 "Kiến trúc Offline-First vững chắc: 10 thực thể CSDL Room v3 hoạt động ổn định không cần Internet; kiểm soát kho tủ lạnh và cảnh báo tồn kho đỏ.",
                 "Tích hợp trợ lý AI thông minh: Hỗ trợ linh hoạt 4 Cloud LLM (OpenAI, Gemini, Claude, DeepSeek) kết hợp thuật toán RuleBasedAi dự phòng khi mất mạng.",
                 "Đạt chuẩn bảo mật cấp hệ thống: Khóa bí mật API mã hóa bằng Keystore phần cứng AES-256-GCM; luồng xác thực thực hiện an toàn phía máy chủ.",
                 "Chất lượng sản phẩm được kiểm chứng: 135/135 bài kiểm thử đơn vị tự động vượt qua 100% trên nền tảng máy ảo JVM."
             ],
             badge="Thành tựu nghiên cứu",
             border_color=RGBColor(0x6E, 0xE7, 0xB7),
             title_color=COLOR_SECONDARY)

    add_card(s14, Inches(6.8), Inches(1.6), Inches(5.7), Inches(5.2),
             "⚠️ HẠN CHẾ THỰC TẾ & BÀI HỌC KINH NGHIỆM",
             [
                 "Chưa có bộ kiểm thử tự động giao diện (Instrumented UI Tests): Hiện tại kiểm thử hiển thị và trải nghiệm người dùng vẫn phụ thuộc vào đo đạc thủ công trên máy thật.",
                 "Tính năng CameraX chưa nối trực tiếp vào UI biên tập: Người dùng hiện tải ảnh đại diện công thức qua đường dẫn URL hoặc chọn tệp sẵn có trên máy.",
                 "Chưa có tính năng đồng bộ đám mây thời gian thực (Cloud Sync): Dữ liệu giữa nhiều thiết bị hiện đồng bộ thủ công thông qua tính năng xuất/nhập tệp sao lưu CSDL.",
                 "Phụ thuộc kết nối mạng khi dùng Cloud AI: Mặc dù có RuleBasedAi dự phòng, nhưng câu trả lời phân tích chuyên sâu của AI vẫn cần đường truyền mạng ổn định."
             ],
             badge="Nhìn nhận thực tế",
             border_color=RGBColor(0xFD, 0xBA, 0x74),
             title_color=COLOR_ACCENT)

def build_slide_15(prs, blank_layout):
    """SLIDE 15: Huong phat trien trong tuong lai"""
    s15 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s15)
    add_header(prs, s15, "Giai đoạn 5: Kết luận & Hướng phát triển", "14. Kế hoạch & Hướng Phát triển Trong Tương lai", 15)
    add_footer(s15, 15)

    add_card(s15, Inches(0.8), Inches(1.6), Inches(3.65), Inches(5.2),
             "📸 AI Camera Vision (1-3 tháng)",
             [
                 "Quét hóa đơn mua hàng siêu thị: Tự động trích xuất tên nguyên liệu và hạn dùng để nhập kho tủ lạnh Pantry chỉ với 1 bức ảnh chụp.",
                 "Nhận diện thực phẩm qua Camera: Hướng camera vào rau củ quả trong tủ lạnh để AI tự động nhận diện và cập nhật danh mục tồn kho.",
                 "Chụp ảnh món ăn hoàn thiện: AI đánh giá màu sắc, độ chín và tự động chấm điểm trang trí món ăn cho người nội trợ."
             ],
             badge="Ngắn hạn: Thị giác máy tính",
             title_color=COLOR_PRIMARY)

    add_card(s15, Inches(4.84), Inches(1.6), Inches(3.65), Inches(5.2),
             "🎙️ Trợ lý Rảnh tay (3-6 tháng)",
             [
                 "Điều khiển bằng giọng nói (Voice Command): Đọc bước tiếp theo, tạm dừng hoặc hẹn giờ nấu nướng mà không cần chạm tay ướt/bẩn vào màn hình.",
                 "Đọc to công thức (Text-to-Speech): Trợ lý ảo đọc to từng bước nấu nướng với giọng đọc tiếng Việt truyền cảm, thân thiện.",
                 "Gợi ý thực đơn cá nhân hóa: Lập kế hoạch bữa ăn thông minh cả tuần theo chế độ ăn kiêng (Keto, Low-Carb, Ăn chay, Tiểu đường)."
             ],
             badge="Trung hạn: Tương tác tự nhiên",
             title_color=COLOR_SECONDARY)

    add_card(s15, Inches(8.88), Inches(1.6), Inches(3.65), Inches(5.2),
             "☁️ Đồng bộ & Dinh dưỡng (6-12 tháng)",
             [
                 "Đồng bộ đám mây thời gian thực (Real-time Cloud Sync): Chia sẻ thực đơn và tủ lạnh chung cho mọi thành viên trong gia đình cùng theo dõi.",
                 "Phân tích dinh dưỡng chuyên sâu: Tự động tính toán tổng lượng Calo, Protein, Carb, Fat dựa trên khối lượng nguyên liệu trong món ăn.",
                 "Xây dựng mạng xã hội ẩm thực: Cho phép người dùng xuất bản, chia sẻ và đánh giá công thức nấu nướng của cộng đồng nội trợ."
             ],
             badge="Dài hạn: Nền tảng toàn diện",
             title_color=COLOR_ACCENT)

def build_slide_16(prs, blank_layout):
    """SLIDE 16: Loi cam on & Q&A"""
    s16 = prs.slides.add_slide(blank_layout)
    set_slide_bg(prs, s16, COLOR_PRIMARY)

    top_bar = s16.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, Inches(0.12))
    top_bar.fill.solid()
    top_bar.fill.fore_color.rgb = COLOR_ACCENT
    top_bar.line.fill.background()

    t_box = s16.shapes.add_textbox(Inches(0.8), Inches(0.8), Inches(11.733), Inches(2.2))
    tf = t_box.text_frame
    tf.word_wrap = True

    p = tf.paragraphs[0]
    p.text = "XIN CHÂN THÀNH CẢM ƠN HỘI ĐỒNG & QUÝ THẦY CÔ!"
    p.font.size = Pt(28)
    p.font.bold = True
    p.font.color.rgb = COLOR_WHITE
    p.alignment = PP_ALIGN.CENTER
    p.space_after = Pt(8)

    p2 = tf.add_paragraph()
    p2.text = "Nhóm xin gửi lời tri ân sâu sắc tới ThS. Lê Văn Quân đã tận tình hướng dẫn và định hướng chuyên môn cho đồ án Cooking Note."
    p2.font.size = Pt(14)
    p2.font.color.rgb = RGBColor(0x93, 0xC5, 0xFD)
    p2.alignment = PP_ALIGN.CENTER

    main_card = s16.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.8), Inches(3.2), Inches(9.733), Inches(3.6))
    main_card.fill.solid()
    main_card.fill.fore_color.rgb = RGBColor(0x0F, 0x2D, 0x42)
    main_card.line.color.rgb = RGBColor(0x3B, 0x82, 0xF6)
    main_card.line.width = Pt(1.5)

    mc_box = s16.shapes.add_textbox(Inches(2.1), Inches(3.4), Inches(9.133), Inches(3.2))
    tf_mc = mc_box.text_frame
    tf_mc.word_wrap = True

    p_qa = tf_mc.paragraphs[0]
    p_qa.text = "PHIÊN HỎI ĐÁP & BẢO VỆ ĐỒ ÁN (Q&A)"
    p_qa.font.size = Pt(20)
    p_qa.font.bold = True
    p_qa.font.color.rgb = RGBColor(0xFB, 0xBF, 0x24)
    p_qa.alignment = PP_ALIGN.CENTER
    p_qa.space_after = Pt(14)

    p_qa_desc = tf_mc.add_paragraph()
    p_qa_desc.text = "Nhóm sinh viên thực hiện đã sẵn sàng lắng nghe các câu hỏi, góp ý quý báu và phản biện chuyên môn từ Hội đồng chấm thi."
    p_qa_desc.font.size = Pt(13.5)
    p_qa_desc.font.color.rgb = COLOR_WHITE
    p_qa_desc.alignment = PP_ALIGN.CENTER
    p_qa_desc.space_after = Pt(14)

    p_info = tf_mc.add_paragraph()
    p_info.text = "✦ Nhóm thực hiện: Vũ Đình Đạo (Nhóm trưởng) • Ngô Gia Bảo • Nguyễn Tiến Dũng • Lê Đức Trọng\n✦ Lớp: TT602-K16LT  |  Khoa Công nghệ Thông tin — CBK\n✦ Mã nguồn & Báo cáo kỹ thuật: Đã kiểm chứng 135/135 Unit Tests tự động vượt qua 100%"
    p_info.font.size = Pt(12)
    p_info.font.color.rgb = RGBColor(0xE2, 0xE8, 0xF0)
    p_info.alignment = PP_ALIGN.CENTER


# ==========================================
# MAIN GENERATOR FUNCTION
# ==========================================

def main():
    prs = Presentation()
    # 16:9 Widescreen standard dimensions
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    script_dir = os.path.dirname(os.path.abspath(__file__))
    screenshots_dir = os.path.join(script_dir, "screenshots")
    output_path = os.path.join(script_dir, "THUYET_TRINH_COOKING_NOTE.pptx")

    print("[*] Bắt đầu khởi tạo bộ slide 16 trang chuẩn 5 giai đoạn...")

    # Giai đoạn 1: Đặt vấn đề (Slides 1 - 3)
    print("  -> Đang dựng Slide 1: Bìa đề tài & Thông tin 4 sinh viên...")
    build_slide_1(prs, blank_layout)
    print("  -> Đang dựng Slide 2: Lý do chọn đề tài & Thách thức thực tiễn...")
    build_slide_2(prs, blank_layout)
    print("  -> Đang dựng Slide 3: Mục tiêu nghiên cứu & Phạm vi sản phẩm...")
    build_slide_3(prs, blank_layout)

    # Giai đoạn 2: Phân tích & Thiết kế (Slides 4 - 6)
    print("  -> Đang dựng Slide 4: Yêu cầu chức năng 5 phân hệ...")
    build_slide_4(prs, blank_layout)
    print("  -> Đang dựng Slide 5: Yêu cầu phi chức năng & Bảo mật...")
    build_slide_5(prs, blank_layout)
    print("  -> Đang dựng Slide 6: Thiết kế CSDL Room 10 bảng...")
    build_slide_6(prs, blank_layout)

    # Giai đoạn 3: Cài đặt & Triển khai (Slides 7 - 8)
    print("  -> Đang dựng Slide 7: Kiến trúc MVVM + UDF luồng 1 chiều...")
    build_slide_7(prs, blank_layout)
    print("  -> Đang dựng Slide 8: Công nghệ & Thư viện sử dụng...")
    build_slide_8(prs, blank_layout)

    # Giai đoạn 4: Demo sản phẩm (Slides 9 - 12)
    print("  -> Đang dựng Slide 9: Demo Trang chủ & Thư viện công thức...")
    build_slide_9(prs, blank_layout, screenshots_dir)
    print("  -> Đang dựng Slide 10: Demo Chi tiết món & Biên tập công thức...")
    build_slide_10(prs, blank_layout, screenshots_dir)
    print("  -> Đang dựng Slide 11: Demo Quản lý Tủ lạnh & Tìm kiếm Debounce...")
    build_slide_11(prs, blank_layout, screenshots_dir)
    print("  -> Đang dựng Slide 12: Demo Trợ lý AI & Cài đặt bảo mật...")
    build_slide_12(prs, blank_layout, screenshots_dir)

    # Giai đoạn 5: Kết luận & Hướng phát triển (Slides 13 - 16)
    print("  -> Đang dựng Slide 13: Đảm bảo chất lượng & 135/135 Unit Tests...")
    build_slide_13(prs, blank_layout)
    print("  -> Đang dựng Slide 14: Tổng kết kết quả & Hạn chế thực tế...")
    build_slide_14(prs, blank_layout)
    print("  -> Đang dựng Slide 15: Hướng phát triển tương lai...")
    build_slide_15(prs, blank_layout)
    print("  -> Đang dựng Slide 16: Lời cảm ơn & Phiên giải đáp thắc mắc...")
    build_slide_16(prs, blank_layout)

    print(f"[*] Đang lưu tệp PowerPoint vào: {output_path}...")
    prs.save(output_path)
    print(f"[+] THÀNH CÔNG! Đã tạo bộ slide 16 trang hoàn chỉnh: {output_path}")

if __name__ == "__main__":
    main()
