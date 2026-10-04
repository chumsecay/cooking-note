# -*- coding: utf-8 -*-
import docx
from docx import Document
from docx.oxml import parse_xml
import shutil
import win32com.client as win32
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

def make_title_p(prefix, title, align="center", size_pt=15, color_rgb="0D47A1", space_before=120, space_after=60):
    sz_val = str(int(size_pt * 2))
    pPr_xml = f'<w:pPr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:jc w:val="{align}"/><w:spacing w:before="{space_before}" w:after="{space_after}"/></w:pPr>'
    p = parse_xml(f'<w:p xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{pPr_xml}</w:p>')
    
    rPr1 = f'<w:rPr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:bCs/><w:sz w:val="{sz_val}"/><w:szCs w:val="{sz_val}"/><w:color w:val="{color_rgb}"/></w:rPr>'
    r1 = parse_xml(f'<w:r xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">{rPr1}<w:t xml:space="preserve">{prefix}</w:t></w:r>')
    p.append(r1)
    
    rPr2 = f'<w:rPr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:bCs/><w:sz w:val="{sz_val}"/><w:szCs w:val="{sz_val}"/><w:color w:val="{color_rgb}"/></w:rPr>'
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
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="24"/></w:rPr><w:t>Vũ Đình Đạo (Nhóm trưởng)</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>6160556</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>TT602 – K16LT</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="4619" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>Ngô Gia Bảo</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>.....................</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>TT602 – K16LT</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="4619" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>Nguyễn Tiến Dũng</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>.....................</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>TT602 – K16LT</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="4619" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>Lê Đức Trọng</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2711" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>.....................</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="2232" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr>
      <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/></w:rPr><w:t>TT602 – K16LT</w:t></w:r></w:p>
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
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="22"/></w:rPr><w:t>Vũ Đình Đạo (Nhóm trưởng)</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Phân tích yêu cầu bài toán &amp; Thiết kế kiến trúc tổng thể MVVM + Unidirectional Data Flow (UDF).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Thiết kế &amp; triển khai CSDL SQLite Room Database (Schema v3: 10 thực thể, 9 DAOs, quan hệ CASCADE).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Thùng chứa phụ thuộc thủ công AppContainer &amp; hệ thống điều hướng Navigation Compose.</w:t></w:r></w:p>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Quản lý mã nguồn Git, tích hợp kiểm thử tự động và tổng hợp báo cáo kỹ thuật đồ án.</w:t></w:r></w:p>
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
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="22"/></w:rPr><w:t>Ngô Gia Bảo</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:shd w:val="clear" w:color="auto" w:fill="EAF1FB"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Thiết kế giao diện 100% Jetpack Compose Material 3 hiện đại, hỗ trợ Dynamic Color &amp; Dark Theme.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Module Khám phá (HomeScreen) &amp; Quản lý Danh mục món ăn (CategoryGrid).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Màn hình Chi tiết công thức (RecipeDetailScreen) &amp; Tìm kiếm tức thì với Debounce 300ms.</w:t></w:r></w:p>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Hiện thực hóa cơ chế kiểm tra tính hợp lệ biểu mẫu (Form validation) và tối ưu hóa collectAsStateWithLifecycle().</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="2140" w:type="dxa"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="22"/></w:rPr><w:t>Nguyễn Tiến Dũng</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Module Quản lý Kho tủ lạnh (PantryScreen): theo dõi số lượng, hạn sử dụng &amp; cảnh báo hết hạn.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Thiết kế hộp thoại Quick Add nguyên liệu và tính toán đồng bộ định lượng thực tế với công thức nấu.</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng Module Đánh dấu Yêu thích (Favorites) &amp; Nhật ký Lịch sử nấu nướng (Cook History).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Tối ưu hóa truy vấn CSDL Room, thiết lập chỉ mục (Index) tăng tốc độ lọc và tìm kiếm dữ liệu.</w:t></w:r></w:p>
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
      <w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="22"/></w:rPr><w:t>Lê Đức Trọng</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="7266" w:type="dxa"/>
        <w:shd w:val="clear" w:color="auto" w:fill="EAF1FB"/>
        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
        <w:vAlign w:val="center"/>
      </w:tcPr>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Tích hợp Trợ lý Trí tuệ Nhân tạo đa nền tảng (Multi-Provider AI: OpenAI Chat, Responses, Claude, Gemini).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng thuật toán ngoại tuyến RuleBasedAi hỗ trợ gợi ý thực đơn khi mất kết nối mạng (Fail-safe).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Triển khai bảo mật khóa API &amp; Session Token bằng EncryptedSharedPreferences (AES-256-GCM Keystore).</w:t></w:r></w:p>
      <w:p><w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="21"/></w:rPr><w:t>• Xây dựng hạ tầng kiểm thử và viết toàn bộ 135 bài Unit Tests tự động chạy trên JVM (100% Pass).</w:t></w:r></w:p>
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

    elements = [
        # Cover page header
        make_p("TRƯỜNG CAO ĐẲNG KỸ THUẬT – CÔNG NGHỆ BÁCH KHOA", align="center", bold=True, size_pt=13, space_before=100, space_after=40),
        make_p("KHOA CÔNG NGHỆ THÔNG TIN", align="center", bold=True, size_pt=13, space_before=0, space_after=260),
        
        # Spacing
        make_p("", space_after=80),
        
        # Project Title
        make_title_p("ĐỀ TÀI: ", "XÂY DỰNG ỨNG DỤNG SỔ TAY CÔNG THỨC NẤU ĂN THÔNG MINH (COOKING NOTE)", align="center", size_pt=15.5, color_rgb="0D47A1", space_before=140, space_after=60),
        make_p("MÔN HỌC: PHÁT TRIỂN ỨNG DỤNG DI ĐỘNG", align="center", bold=True, size_pt=14, space_before=0, space_after=60),
        make_p("Ứng dụng Android Offline-First hiện đại tích hợp Trợ lý Trí tuệ Nhân tạo đa nền tảng (Multi-Provider AI)", align="center", italic=True, size_pt=11.5, space_before=0, space_after=200),
        
        # Spacing
        make_p("", space_after=120),
        
        # Instructor & Group
        make_p("GVHD: ThS. Lê Văn Quân", align="center", bold=False, size_pt=13, space_before=60, space_after=60),
        make_p("Nhóm sinh viên thực hiện:", align="left", bold=False, size_pt=12, space_before=30, space_after=50),
        
        # Table 0: Student list
        make_table_0(),
        
        # Spacing & Date
        make_p("", space_after=140),
        make_p("Hà Nội, 2026", align="center", bold=True, italic=True, size_pt=13, space_before=100, space_after=80),
        
        # Page 2: Standalone Task Assignment Page (pageBreakBefore)
        make_p("PHÂN CHIA CÔNG VIỆC:", align="center", bold=True, size_pt=14, color_rgb="0D47A1", space_before=240, space_after=120, page_break_before=True),
        make_table_1(),
        
        # Section Break Next Page
        make_section_break()
    ]

    elem_13 = body[13]
    for el in elements:
        body.insert(body.index(elem_13), el)
    
    for _ in range(13):
        body.remove(body[0])

    doc.save(target_docx_path)
    print(f"Updated document saved to: {target_docx_path}")

if __name__ == "__main__":
    update_document("CookingNote_BaoCao.docx")
