# -*- coding: utf-8 -*-
import docx
from docx import Document
from docx.oxml import parse_xml
import shutil
import os

def make_p(text="", align="center", bold=False, italic=False, size_pt=13, color_rgb="000000", space_before=0, space_after=0, page_break_before=False):
    jc_val = align
    sz_val = str(int(size_pt * 2))
    pPr_parts = []
    if page_break_before:
        pPr_parts.append("<w:pageBreakBefore/>")
    if jc_val:
        pPr_parts.append(f'<w:jc w:val="{jc_val}"/>')
    if space_before or space_after:
        pPr_parts.append(f'<w:spacing w:before="{space_before}" w:after="{space_after}"/>')
    
    pPr_xml = f'<w:pPr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{"".join(pPr_parts)}</w:pPr>'
    p = parse_xml(f'<w:p xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{pPr_xml}</w:p>')
    
    if text:
        rPr_parts = ['<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>']
        if bold:
            rPr_parts.append("<w:b/><w:bCs/>")
        if italic:
            rPr_parts.append("<w:i/><w:iCs/>")
        if size_pt:
            rPr_parts.append(f'<w:sz w:val="{sz_val}"/><w:szCs w:val="{sz_val}"/>')
        if color_rgb and color_rgb != "000000":
            rPr_parts.append(f'<w:color w:val="{color_rgb}"/>')
        
        rPr_xml = f'<w:rPr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{"".join(rPr_parts)}</w:rPr>'
        r = parse_xml(f'<w:r xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{rPr_xml}<w:t xml:space="preserve">{text}</w:t></w:r>')
        p.append(r)
    return p

def make_title_p(prefix, title, align="center", size_pt=15, color_rgb="000000", space_before=140, space_after=60):
    sz_val = str(int(size_pt * 2))
    pPr_xml = f'<w:pPr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:jc w:val="{align}"/><w:spacing w:before="{space_before}" w:after="{space_after}"/></w:pPr>'
    p = parse_xml(f'<w:p xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{pPr_xml}</w:p>')
    
    rPr1 = f'<w:rPr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:bCs/><w:sz w:val="{sz_val}"/><w:szCs w:val="{sz_val}"/></w:rPr>'
    r1 = parse_xml(f'<w:r xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{rPr1}<w:t xml:space="preserve">{prefix}</w:t></w:r>')
    p.append(r1)
    
    rPr2 = f'<w:rPr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:bCs/><w:sz w:val="{sz_val}"/><w:szCs w:val="{sz_val}"/></w:rPr>'
    r2 = parse_xml(f'<w:r xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{rPr2}<w:t xml:space="preserve">{title}</w:t></w:r>')
    p.append(r2)
    return p

def make_table_0():
    xml = """<w:tbl xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:tblPr>
    <w:tblW w:w="9562" w:type="dxa"/>
    <w:jc w:val="center"/>
    <w:tblBorders>
      <w:top w:val="single" w:color="000000" w:sz="4" w:space="0"/>
      <w:left w:val="single" w:color="000000" w:sz="4" w:space="0"/>
      <w:bottom w:val="single" w:color="000000" w:sz="4" w:space="0"/>
      <w:right w:val="single" w:color="000000" w:sz="4" w:space="0"/>
      <w:insideH w:val="single" w:color="000000" w:sz="4" w:space="0"/>
      <w:insideV w:val="single" w:color="000000" w:sz="4" w:space="0"/>
    </w:tblBorders>
    <w:tblCellMar>
      <w:top w:w="60" w:type="dxa"/>
      <w:left w:w="120" w:type="dxa"/>
      <w:bottom w:w="60" w:type="dxa"/>
      <w:right w:w="120" w:type="dxa"/>
    </w:tblCellMar>
  </w:tblPr>
  <w:tblGrid>
    <w:gridCol w:w="4619"/>
    <w:gridCol w:w="2711"/>
    <w:gridCol w:w="2232"/>
  </w:tblGrid>
  <w:tr>
    <w:trPr><w:cantSplit/><w:tblHeader/></w:trPr>
    <w:tc>
      <w:tcPr><w:tcW w:w="4619" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="24"/></w:rPr><w:t>Họ tên</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="24"/></w:rPr><w:t>MSV</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="24"/></w:rPr><w:t>Lớp</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="4619" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>Trần Bình An ( Nhóm Trưởng )</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>6150813</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>TT601 – K15</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="4619" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>Đinh Việt Dũng</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>6150268</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>TT601 – K15</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="4619" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>Trịnh Văn Dân</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>6150322</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>TT601 – K15</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="4619" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>Bùi Quốc An</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>6150544</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>TT601 – K15</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
</w:tbl>"""
    return parse_xml(xml)

def make_table_1():
    xml = """<w:tbl xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:tblPr>
    <w:tblW w:w="9406" w:type="dxa"/>
    <w:jc w:val="center"/>
    <w:tblBorders>
      <w:top w:val="single" w:color="B0BEC5" w:sz="4" w:space="0"/>
      <w:left w:val="single" w:color="B0BEC5" w:sz="4" w:space="0"/>
      <w:bottom w:val="single" w:color="B0BEC5" w:sz="4" w:space="0"/>
      <w:right w:val="single" w:color="B0BEC5" w:sz="4" w:space="0"/>
      <w:insideH w:val="single" w:color="B0BEC5" w:sz="4" w:space="0"/>
      <w:insideV w:val="single" w:color="B0BEC5" w:sz="4" w:space="0"/>
    </w:tblBorders>
  </w:tblPr>
  <w:tblGrid>
    <w:gridCol w:w="2140"/>
    <w:gridCol w:w="7266"/>
  </w:tblGrid>
  <w:tr>
    <w:trPr><w:cantSplit/><w:tblHeader/></w:trPr>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="2140" w:type="dxa"/>
        <w:shd w:val="clear" w:color="auto" w:fill="0D47A1"/>
        <w:tcMar><w:top w:w="100" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="100" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/></w:rPr><w:t>Họ và tên</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:shd w:val="clear" w:color="auto" w:fill="0D47A1"/>
        <w:tcMar><w:top w:w="100" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="100" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/></w:rPr><w:t>Nhiệm vụ</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="2140" w:type="dxa"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="22"/></w:rPr><w:t>Bùi Quốc An</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Phân tích yêu cầu bài toán &amp; Thiết kế kiến trúc UI/UX hệ thống.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Module Khám phá (HomeScreen) &amp; Quản lý Danh mục ẩm thực (CategoryGrid).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng hệ thống điều hướng Navigation Compose và thùng chứa phụ thuộc thủ công AppContainer.</w:t></w:r></w:p>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Thiết kế giao diện 100% Jetpack Compose Material 3 hiện đại, hỗ trợ Dynamic Color &amp; Dark Theme.</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="2140" w:type="dxa"/>
        <w:shd w:val="clear" w:color="auto" w:fill="EAF1FB"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="22"/></w:rPr><w:t>Trịnh Văn Dân</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:shd w:val="clear" w:color="auto" w:fill="EAF1FB"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Module Quản lý Kho tủ lạnh (PantryScreen): theo dõi số lượng, hạn sử dụng &amp; cảnh báo hết hạn.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Thiết kế hộp thoại Quick Add nguyên liệu và tính toán đồng bộ định lượng thực tế khi nấu nướng.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Module Đánh dấu Yêu thích (Favorites) &amp; Nhật ký Lịch sử nấu nướng (Cook History).</w:t></w:r></w:p>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Triển khai giải pháp sao lưu và phục hồi an toàn CSDL SQLite (sao chép cả file chính kèm .db-wal và .db-shm).</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="2140" w:type="dxa"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="22"/></w:rPr><w:t>Đinh Việt Dũng</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Module Tạo và Chỉnh sửa công thức nấu ăn (CreateRecipeScreen) với Form Validation chặt chẽ.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Triển khai thuật toán tìm kiếm tức thì đa trường dữ liệu kết hợp cơ chế Debounce 300ms chống giật lag.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Màn hình Chi tiết công thức (RecipeDetailScreen) với định lượng nguyên liệu và các bước nấu đánh số kèm mẹo.</w:t></w:r></w:p>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng hạ tầng kiểm thử và viết toàn bộ 135 bài Unit Tests tự động chạy trên JVM (100% Pass).</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="2140" w:type="dxa"/>
        <w:shd w:val="clear" w:color="auto" w:fill="EAF1FB"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="22"/></w:rPr><w:t>Trần Bình An (Nhóm Trưởng)</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:shd w:val="clear" w:color="auto" w:fill="EAF1FB"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Tích hợp Trợ lý Trí tuệ Nhân tạo đa nền tảng (Multi-Provider AI: OpenAI Chat, Responses, Claude, Gemini &amp; RuleBasedAi).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Thiết kế kiến trúc tổng thể ứng dụng theo mô hình MVVM + Unidirectional Data Flow (UDF) chuẩn Google.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Thiết kế cấu trúc CSDL SQLite Room Database Version 3 (10 thực thể, 9 DAOs, quan hệ khóa ngoại CASCADE).</w:t></w:r></w:p>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Triển khai cơ chế bảo mật khóa API bằng EncryptedSharedPreferences (AES-256-GCM Keystore) &amp; Quản lý mã nguồn Git.</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
</w:tbl>"""
    return parse_xml(xml)

def make_section_break():
    xml = """<w:p xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:pPr>
    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838"/>
      <w:pgMar w:top="1134" w:right="1247" w:bottom="1134" w:left="1247" w:header="708" w:footer="708" w:gutter="0"/>
      <w:pgBorders w:offsetFrom="page">
        <w:top w:val="double" w:color="0D47A1" w:sz="6" w:space="24"/>
        <w:left w:val="double" w:color="0D47A1" w:sz="6" w:space="24"/>
        <w:bottom w:val="double" w:color="0D47A1" w:sz="6" w:space="24"/>
        <w:right w:val="double" w:color="0D47A1" w:sz="6" w:space="24"/>
      </w:pgBorders>
      <w:cols w:space="720" w:num="1"/>
      <w:titlePg/>
      <w:docGrid w:linePitch="360" w:charSpace="0"/>
    </w:sectPr>
  </w:pPr>
</w:p>"""
    return parse_xml(xml)

def update_document(target_docx_path):
    doc = Document(target_docx_path)
    body = doc._body._body

    # Elements to replace the old cover and assignment elements (indices 0 to 14)
    elements = [
        # Cover page header
        make_p("TRƯỜNG CAO ĐẲNG KỸ THUẬT – CÔNG NGHỆ BÁCH KHOA", align="center", bold=True, size_pt=13, space_before=60, space_after=40),
        make_p("KHOA CÔNG NGHỆ THÔNG TIN", align="center", bold=True, size_pt=13, space_before=0, space_after=280),
        
        # Spacing
        make_p("", space_after=60),
        
        # Project Title
        make_title_p("ĐỀ TÀI: ", "XÂY DỰNG ỨNG DỤNG SỔ TAY CÔNG THỨC NẤU ĂN THÔNG MINH TRÊN HỆ ĐIỀU HÀNH ANDROID (COOKING NOTE)", align="center", size_pt=15, space_before=140, space_after=60),
        make_p("MÔN HỌC: PHÁT TRIỂN ỨNG DỤNG DI ĐỘNG", align="center", bold=True, size_pt=14, space_before=0, space_after=260),
        
        # Spacing
        make_p("", space_after=120),
        
        # Instructor & Group
        make_p("GVHD: Lê Văn Quân", align="center", bold=False, size_pt=13, space_before=60, space_after=60),
        make_p("Nhóm sinh viên thực hiện:", align="left", bold=False, size_pt=12, space_before=30, space_after=50),
        
        # Table 0: Student list matching reference doc
        make_table_0(),
        
        # Spacing & Date
        make_p("", space_after=180),
        make_p("Hà Nội, 2026", align="center", bold=True, italic=True, size_pt=13, space_before=120, space_after=80),
        
        # Page 2: Standalone Task Assignment Page (pageBreakBefore)
        make_p("PHÂN CHIA CÔNG VIỆC:", align="center", bold=True, size_pt=14, color_rgb="0D47A1", space_before=240, space_after=120, page_break_before=True),
        make_table_1(),
        
        # Section Break Next Page
        make_section_break()
    ]

    elem_15 = body[15]
    for el in elements:
        body.insert(body.index(elem_15), el)
    
    # Remove the 15 elements from previous insertion
    for _ in range(15):
        body.remove(body[0])

    # Also update Table 3 (Detailed assignment table in Chapter 1) if present
    for t in doc.tables:
        header_text = " ".join([c.text.strip() for c in t.rows[0].cells])
        if "Thành viên" in header_text and "Kết quả bàn giao" in header_text:
            # Map the rows
            members = [
                ("Trần Bình An (Nhóm trưởng)", 
                 "• Thiết kế kiến trúc tổng thể ứng dụng theo mô hình MVVM + Unidirectional Data Flow (UDF).\n• Phân tích và thiết kế cơ sở dữ liệu Room Database (10 bảng dữ liệu, 9 DAOs quan hệ khóa ngoại CASCADE).\n• Tích hợp Trợ lý Trí tuệ Nhân tạo đa nền tảng (OpenAI, Claude, Gemini & RuleBasedAi).\n• Quản lý mã nguồn Git, tích hợp kiểm thử tự động và điều phối tiến độ chung.",
                 "Kiến trúc phân tầng rõ ràng, cơ sở dữ liệu toàn vẹn 100%, hệ thống AI tích hợp mượt mà không xung đột."),
                ("Bùi Quốc An",
                 "• Thiết kế hệ thống giao diện người dùng bằng Jetpack Compose Material 3 theo phong cách ẩm thực hiện đại.\n• Xây dựng các màn hình: Trang chủ (Home), Thư viện công thức (Library), Chi tiết món (Detail).\n• Hiện thực hóa hệ thống điều hướng Navigation Compose và thùng chứa phụ thuộc thủ công AppContainer.\n• Tối ưu hóa vòng đời Composable với collectAsStateWithLifecycle().",
                 "11 màn hình Composable hoàn chỉnh, giao diện tương thích 2 chế độ Sáng/Tối, điều hướng ổn định."),
                ("Trịnh Văn Dân",
                 "• Phát triển phân hệ Quản lý Tủ lạnh (Pantry): theo dõi tồn kho, hạn sử dụng, cảnh báo nguyên liệu sắp hết.\n• Hiện thực hóa hộp thoại thêm nhanh nguyên liệu và đồng bộ định lượng thực tế khi nấu nướng.\n• Xây dựng phân hệ Đánh dấu Yêu thích (Favorites) và Nhật ký Lịch sử nấu ăn (Cook History).\n• Xây dựng tính năng sao lưu và phục hồi an toàn CSDL SQLite (kèm .db-wal và .db-shm).",
                 "Kho tủ lạnh vận hành ổn định, cảnh báo sắp hết trực quan, sao lưu giao dịch SQLite an toàn 100%."),
                ("Đinh Việt Dũng",
                 "• Xây dựng Module Tạo và Chỉnh sửa công thức nấu ăn (CreateRecipeScreen) với Form Validation chặt chẽ.\n• Triển khai thuật toán tìm kiếm tức thì đa trường dữ liệu kết hợp cơ chế Debounce 300ms giảm tải CPU.\n• Triển khai cơ chế bảo mật khóa API và phiên đăng nhập bằng EncryptedSharedPreferences (AES-256-GCM Keystore).\n• Xây dựng hạ tầng và bộ tự động kiểm thử 135 bài Unit Tests chạy độc lập 100% trên JVM.",
                 "Thanh tìm kiếm phản hồi < 50ms không lag, khóa API mã hóa an toàn, 135/135 Unit Tests pass 100%."),
                ("Cả nhóm",
                 "• Soạn thảo báo cáo kỹ thuật hoàn chỉnh (Word), thiết kế slide bảo vệ chuyên nghiệp (PowerPoint).\n• Thực hiện kiểm thử chéo chức năng, kiểm thử kịch bản người dùng thực tế trên thiết bị Android thật.\n• Đóng gói và biên dịch tệp APK cài đặt hoàn chỉnh (app-debug.apk).",
                 "Báo cáo bài tập lớn đạt chuẩn quy cách, bài thuyết trình sẵn sàng bảo vệ, sản phẩm hoàn thiện cài đặt thành công.")
            ]
            for r_idx, (m_name, m_duty, m_out) in enumerate(members):
                if r_idx + 1 < len(t.rows):
                    row = t.rows[r_idx + 1]
                    row.cells[0].text = m_name
                    row.cells[1].text = m_duty
                    row.cells[2].text = m_out

    # Also update LỜI CAM ĐOAN
    for p in doc.paragraphs:
        if p.text.strip() == "Nhóm thực hiện":
            p.text = "Đại diện nhóm thực hiện"
            p.alignment = docx.enum.text.WD_ALIGN_PARAGRAPH.RIGHT
        elif p.text.strip() == "(Ký và ghi rõ họ tên)":
            p.text = "(Ký và ghi rõ họ tên)\n\nAn\nTrần Bình An"
            p.alignment = docx.enum.text.WD_ALIGN_PARAGRAPH.RIGHT

    doc.save(target_docx_path)
    print(f"Updated document saved to: {target_docx_path}")

if __name__ == "__main__":
    update_document("CookingNote_BaoCao.docx")
    shutil.copyfile("CookingNote_BaoCao.docx", "docs/CookingNote_BaoCao.docx")
    print("Synchronized to docs/CookingNote_BaoCao.docx successfully.")
