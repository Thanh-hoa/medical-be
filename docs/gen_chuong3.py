from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL

doc = Document()
section = doc.sections[0]
section.top_margin    = Cm(2.5)
section.bottom_margin = Cm(2.5)
section.left_margin   = Cm(3.0)
section.right_margin  = Cm(2.0)

FONT    = "Times New Roman"
SZ_BODY = Pt(13)
SZ_H1   = Pt(14)
SZ_H2   = Pt(13)
LINE_SP = Pt(20)

# ── helpers ──────────────────────────────────────────────────────────────────
def _rf(run, bold=False, italic=False, size=None):
    run.font.name   = FONT
    run.font.size   = size or SZ_BODY
    run.font.bold   = bold
    run.font.italic = italic
    rPr = run._r.get_or_add_rPr()
    rf  = OxmlElement('w:rFonts')
    for a in ('w:ascii','w:hAnsi','w:eastAsia','w:cs'):
        rf.set(qn(a), FONT)
    rPr.insert(0, rf)

def _pf(p, align=WD_ALIGN_PARAGRAPH.JUSTIFY,
        fi=None, li=None, sb=Pt(0), sa=Pt(4)):
    pf = p.paragraph_format
    pf.alignment    = align
    pf.space_before = sb
    pf.space_after  = sa
    pf.line_spacing = LINE_SP
    if fi is not None: pf.first_line_indent = fi
    if li is not None: pf.left_indent = li

def chap(text):
    p = doc.add_paragraph()
    _pf(p, align=WD_ALIGN_PARAGRAPH.CENTER, sb=Pt(12), sa=Pt(8))
    r = p.add_run(text.upper())
    _rf(r, bold=True, size=SZ_H1)

def h2(text):
    p = doc.add_paragraph()
    _pf(p, align=WD_ALIGN_PARAGRAPH.LEFT, sb=Pt(10), sa=Pt(4))
    r = p.add_run(text)
    _rf(r, bold=True, size=SZ_H2)

def h3(text):
    p = doc.add_paragraph()
    _pf(p, align=WD_ALIGN_PARAGRAPH.LEFT, fi=Cm(0.5), sb=Pt(6), sa=Pt(3))
    r = p.add_run(text)
    _rf(r, bold=True, size=SZ_H2)

def body(text):
    p = doc.add_paragraph()
    _pf(p, fi=Cm(1.27))
    r = p.add_run(text)
    _rf(r)

def bullet(text, level=0):
    p  = doc.add_paragraph()
    li = 1.0 + level * 0.5
    pf = p.paragraph_format
    pf.left_indent       = Cm(li)
    pf.first_line_indent = Cm(-0.5)
    pf.alignment         = WD_ALIGN_PARAGRAPH.JUSTIFY
    pf.space_before      = Pt(0)
    pf.space_after       = Pt(3)
    pf.line_spacing      = LINE_SP
    sym = "•" if level == 0 else "◦"
    r = p.add_run(f"{sym}  {text}")
    _rf(r)

def label_body(label, text):
    p  = doc.add_paragraph()
    pf = p.paragraph_format
    pf.left_indent       = Cm(1.0)
    pf.first_line_indent = Cm(-0.5)
    pf.alignment         = WD_ALIGN_PARAGRAPH.JUSTIFY
    pf.space_before      = Pt(2); pf.space_after = Pt(4)
    pf.line_spacing      = LINE_SP
    r1 = p.add_run(f"- {label}: ")
    _rf(r1, bold=True)
    r2 = p.add_run(text)
    _rf(r2)

def note_fig(text):
    """chú thích hình/bảng — căn giữa, nghiêng"""
    p = doc.add_paragraph()
    _pf(p, align=WD_ALIGN_PARAGRAPH.CENTER, sb=Pt(2), sa=Pt(6))
    r = p.add_run(text)
    _rf(r, italic=True)

def add_table(headers, rows, caption):
    """Thêm bảng có header in đậm"""
    tbl = doc.add_table(rows=1+len(rows), cols=len(headers))
    tbl.style = 'Table Grid'
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    # header
    hdr = tbl.rows[0]
    for i, h in enumerate(headers):
        cell = hdr.cells[i]
        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        _rf(r, bold=True, size=Pt(12))
        cell._tc.get_or_add_tcPr()
        shd = OxmlElement('w:shd')
        shd.set(qn('w:val'), 'clear')
        shd.set(qn('w:color'), 'auto')
        shd.set(qn('w:fill'), 'D9E1F2')
        cell._tc.tcPr.append(shd)
    # data rows
    for ri, row in enumerate(rows):
        tr = tbl.rows[ri+1]
        for ci, val in enumerate(row):
            cell = tr.cells[ci]
            p    = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            r = p.add_run(val)
            _rf(r, size=Pt(12))
    doc.add_paragraph()
    note_fig(caption)

def page_footer():
    footer = doc.sections[0].footer
    para   = footer.paragraphs[0]
    para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = para.add_run()
    _rf(run, size=Pt(12))
    for tag, val in [('w:fldChar','begin'),('w:instrText','PAGE'),('w:fldChar','end')]:
        el = OxmlElement(tag)
        if tag == 'w:fldChar': el.set(qn('w:fldCharType'), val)
        else: el.text = val
        run._r.append(el)

page_footer()

# ════════════════════════════════════════════════════════════════════════════
chap("Chương 3: Phân tích và Thiết kế Hệ Thống")
# ════════════════════════════════════════════════════════════════════════════

# ────────────────────────────────────────────────────────────────────────────
h2("3.1.  Khảo sát và phân tích yêu cầu hệ thống")
# ────────────────────────────────────────────────────────────────────────────

body(
    "Trước khi tiến hành thiết kế và lập trình, việc khảo sát và phân tích yêu cầu "
    "hệ thống là bước không thể thiếu nhằm xác định rõ những gì hệ thống phải làm "
    "được (yêu cầu chức năng) và hệ thống phải đạt được những tiêu chuẩn chất lượng "
    "nào (yêu cầu phi chức năng). Giai đoạn này giúp định hướng toàn bộ quá trình "
    "thiết kế kiến trúc, lựa chọn công nghệ và phát triển về sau."
)

h3("3.1.1.  Yêu cầu chức năng")

body(
    "Dựa trên việc phân tích quy trình làm việc thực tế tại các cơ sở y tế và "
    "đặc thù bài toán số hóa bệnh án, hệ thống cần đáp ứng các yêu cầu chức năng "
    "sau:"
)

label_body(
    "Quản lý tài khoản người dùng",
    "Hệ thống cho phép tạo mới tài khoản (Admin tạo hoặc tự đăng ký), đăng nhập "
    "bằng tên đăng nhập/mật khẩu, xác thực qua email, xem và cập nhật thông tin cá "
    "nhân (họ tên, ngày sinh, số điện thoại, ảnh đại diện), đổi mật khẩu và vô hiệu "
    "hóa tài khoản khi cần thiết."
)

label_body(
    "Phân quyền theo vai trò (RBAC)",
    "Hệ thống hỗ trợ ba vai trò chính: Admin (quản trị hệ thống toàn quyền), Bác "
    "sĩ (xem xét và phê duyệt bệnh án), Nhân viên (tải ảnh, xử lý và chỉnh sửa "
    "kết quả OCR). Mỗi vai trò được gán các quyền hạn cụ thể trên từng module "
    "(xem, tạo, sửa, xóa, duyệt, hủy) thông qua bảng permission_role. Menu giao "
    "diện hiển thị động theo quyền của từng người dùng."
)

label_body(
    "Quản lý thông tin bệnh nhân",
    "Hệ thống cho phép tạo mới, xem chi tiết, cập nhật thông tin bệnh nhân bao "
    "gồm số thẻ BHYT (duy nhất), họ tên, ngày sinh, giới tính, địa chỉ, số điện "
    "thoại. Hỗ trợ tìm kiếm bệnh nhân nhanh theo số thẻ BHYT và tìm kiếm danh "
    "sách có phân trang theo từ khóa."
)

label_body(
    "Tải lên hình ảnh bệnh án",
    "Người dùng có thể tải lên hình ảnh bệnh án (chụp từ thiết bị di động hoặc "
    "scan) dưới các định dạng phổ biến (JPG, PNG, PDF). Hệ thống lưu trữ ảnh gốc "
    "và gắn liên kết với hồ sơ bệnh nhân tương ứng. Thông tin bổ sung như khoa "
    "điều trị, loại hồ sơ và ghi chú có thể được nhập kèm theo."
)

label_body(
    "Trích xuất thông tin tự động bằng AI",
    "Sau khi ảnh được tải lên, hệ thống tự động gọi dịch vụ AI (Python) để thực "
    "hiện: (1) YOLO11n-OBB phát hiện và khoanh vùng các trường thông tin trên biểu "
    "mẫu; (2) Tesseract OCR đọc văn bản tiếng Việt trong từng vùng đã khoanh; "
    "(3) Trả về kết quả dưới dạng JSON gồm dữ liệu trích xuất (extracted_data) và "
    "dữ liệu xét nghiệm (lab_data), lưu trực tiếp vào cơ sở dữ liệu."
)

label_body(
    "Xem xét và chỉnh sửa kết quả OCR",
    "Sau khi AI trích xuất, hệ thống hiển thị ảnh gốc song song với kết quả nhận "
    "dạng. Người dùng có thể chỉnh sửa từng trường thông tin chưa chính xác trực "
    "tiếp trên giao diện. Hỗ trợ cập nhật từng trường riêng lẻ (field/update) hoặc "
    "cập nhật toàn bộ thông tin chi tiết bệnh án (update-detail)."
)

label_body(
    "Quản lý vòng đời và phê duyệt bệnh án",
    "Bệnh án trải qua bốn trạng thái: PROCESSING (đang xử lý AI) → EXTRACTED "
    "(đã trích xuất, chờ nhân viên kiểm tra) → PENDING_DOCTOR_REVIEW (đã nộp, "
    "chờ bác sĩ phê duyệt) → APPROVED (đã phê duyệt và lưu chính thức). Nhân "
    "viên thực hiện nộp hồ sơ (submit); Bác sĩ thực hiện duyệt (approve) hoặc "
    "trả lại để chỉnh sửa. Admin có thể xóa bệnh án khi cần."
)

label_body(
    "Xem danh sách và tìm kiếm bệnh án",
    "Hệ thống cung cấp danh sách bệnh án có phân trang, hỗ trợ lọc theo trạng "
    "thái, bệnh nhân và khoảng thời gian. Nhân viên xem danh sách chờ xử lý; "
    "Bác sĩ xem danh sách chờ phê duyệt (pending-review). Xem chi tiết bệnh án "
    "bao gồm thông tin bệnh nhân, ảnh gốc và kết quả trích xuất đầy đủ."
)

h3("3.1.2.  Yêu cầu phi chức năng")

body(
    "Bên cạnh các yêu cầu chức năng, hệ thống cần đáp ứng các tiêu chuẩn chất "
    "lượng về bảo mật, hiệu năng, tính khả dụng và trải nghiệm người dùng như sau:"
)

label_body(
    "Bảo mật",
    "Toàn bộ API được bảo vệ bằng JWT (Access Token thời hạn 1 giờ, Refresh "
    "Token thời hạn 7 ngày). Mật khẩu được mã hóa một chiều bằng BCrypt trước "
    "khi lưu vào cơ sở dữ liệu. Mỗi endpoint được kiểm soát quyền truy cập theo "
    "vai trò thông qua Spring Security @PreAuthorize. Dữ liệu truyền tải được "
    "bảo vệ qua HTTPS khi triển khai thực tế. Tài khoản hỗ trợ xác thực email "
    "trước khi kích hoạt."
)

label_body(
    "Hiệu năng xử lý",
    "Thời gian phản hồi của các API nghiệp vụ thông thường (CRUD bệnh nhân, "
    "danh sách bệnh án) không vượt quá 2 giây trong điều kiện tải bình thường. "
    "Pipeline xử lý ảnh (YOLO + OCR) hoàn thành trong vòng 10–15 giây đối với "
    "ảnh bệnh án tiêu chuẩn. Kết quả xử lý AI được lưu vào cột JSONB của "
    "PostgreSQL để tối ưu truy xuất không cần parse lại."
)

label_body(
    "Tính sẵn sàng và ổn định",
    "Hệ thống được đóng gói bằng Docker Compose để dễ dàng triển khai lại khi "
    "có sự cố. Backend và AI Service chạy độc lập, đảm bảo khi dịch vụ AI "
    "tạm thời không phản hồi, các chức năng quản lý dữ liệu khác vẫn hoạt động "
    "bình thường. Cơ sở dữ liệu PostgreSQL đảm bảo tính toàn vẹn dữ liệu thông "
    "qua ràng buộc khóa ngoại và transaction."
)

label_body(
    "Tính dễ sử dụng",
    "Giao diện người dùng được thiết kế trực quan, phù hợp với nhân viên y tế "
    "và hành chính không có nhiều kinh nghiệm với phần mềm. Hệ thống hiển thị "
    "menu động theo quyền hạn, tránh gây nhầm lẫn khi người dùng không có quyền "
    "truy cập một số chức năng. Tất cả thông báo lỗi được trả về bằng tiếng Việt "
    "rõ ràng thông qua hệ thống i18n của ứng dụng."
)

label_body(
    "Khả năng mở rộng và bảo trì",
    "Kiến trúc phân tầng rõ ràng (Controller → Service → Repository) giúp dễ "
    "dàng mở rộng thêm chức năng mới mà không ảnh hưởng đến các module hiện có. "
    "API được tài liệu hóa đầy đủ qua Swagger UI tại /swagger-ui.html, hỗ trợ "
    "kiểm thử và tích hợp nhanh chóng. Mã nguồn được quản lý bằng Git với CI/CD "
    "tự động qua GitHub Actions."
)

# ────────────────────────────────────────────────────────────────────────────
h2("3.2.  Lựa chọn mô hình và kiến trúc phát triển")
# ────────────────────────────────────────────────────────────────────────────

body(
    "Dựa trên các yêu cầu đã phân tích ở mục 3.1 và cơ sở lý thuyết được trình "
    "bày tại Chương 2, nhóm tiến hành lựa chọn mô hình phát triển phần mềm và "
    "chốt các công nghệ sẽ sử dụng trong toàn bộ dự án."
)

h3("3.2.1.  Kiến trúc phần mềm")

body(
    "Hệ thống được xây dựng theo mô hình Client–Server kết hợp kiến trúc "
    "hướng dịch vụ (Service-Oriented), trong đó tầng xử lý AI được tách biệt "
    "thành một microservice độc lập. Cụ thể, hệ thống gồm bốn thành phần chính "
    "giao tiếp với nhau qua giao thức HTTP/REST:"
)

bullet("Frontend (ReactJS) — giao diện người dùng chạy trên trình duyệt, giao tiếp "
       "với Backend qua RESTful API sử dụng JSON.")
bullet("Backend (Java Spring Boot) — trung tâm điều phối nghiệp vụ, xác thực "
       "JWT, phân quyền RBAC, quản lý dữ liệu và điều phối gọi AI Service.")
bullet("AI Python Service (FastAPI) — nhận ảnh từ Backend, chạy pipeline YOLO → "
       "OCR, trả kết quả JSON. Chạy độc lập, dễ dàng nâng cấp mô hình AI mà "
       "không ảnh hưởng đến Backend.")
bullet("Cơ sở dữ liệu (PostgreSQL) — lưu trữ toàn bộ dữ liệu bệnh nhân, bệnh "
       "án, tài khoản, kết quả OCR (dạng JSONB) và cấu hình phân quyền.")

body(
    "Việc tách AI Service thành một thành phần riêng biệt mang lại nhiều lợi "
    "thế: cho phép thay thế hoặc nâng cấp mô hình YOLO/OCR độc lập với Backend; "
    "dễ dàng scale riêng AI Service khi nhu cầu xử lý ảnh tăng cao; đồng thời "
    "giúp Backend không bị block trong khi chờ kết quả xử lý ảnh nặng."
)

h3("3.2.2.  Lựa chọn công nghệ AI")

body(
    "Qua quá trình khảo sát và đánh giá các giải pháp phát hiện đối tượng và "
    "nhận dạng văn bản hiện có, nhóm quyết định sử dụng kết hợp hai công nghệ "
    "sau cho pipeline trích xuất thông tin:"
)

label_body(
    "YOLO11n-OBB cho bước phát hiện vùng thông tin",
    "YOLO (You Only Look Once) thế hệ 11 với biến thể OBB (Oriented Bounding "
    "Box) được lựa chọn vì khả năng phát hiện đối tượng có hướng trong thời gian "
    "thực, phù hợp với trường hợp ảnh bệnh án có thể bị nghiêng nhẹ khi chụp. "
    "Biến thể nano (11n) được ưu tiên nhằm tối ưu tốc độ inference trên phần "
    "cứng phổ thông mà vẫn đảm bảo độ chính xác chấp nhận được. So với các "
    "phương án thay thế như EfficientDet hay Faster R-CNN, YOLO11n-OBB cho tốc "
    "độ xử lý nhanh hơn đáng kể và dễ triển khai hơn trên môi trường production."
)

label_body(
    "Tesseract OCR cho bước nhận dạng văn bản",
    "Tesseract OCR phiên bản 5 (kiến trúc LSTM) được lựa chọn để đọc văn bản "
    "tiếng Việt trong các vùng đã được YOLO khoanh vùng. Tesseract hỗ trợ ngôn "
    "ngữ tiếng Việt thông qua bộ ngữ liệu huấn luyện chuyên biệt (vie.traineddata) "
    "và có thể được tinh chỉnh thêm với dữ liệu văn bản y tế nếu cần. So với "
    "PaddleOCR, Tesseract nhẹ hơn và không đòi hỏi GPU, phù hợp với môi trường "
    "triển khai không có tăng tốc phần cứng. Tiền xử lý ảnh bằng OpenCV (grayscale, "
    "thresholding, denoising) được thực hiện trước khi đưa vào Tesseract nhằm "
    "nâng cao độ chính xác nhận dạng."
)

# ────────────────────────────────────────────────────────────────────────────
h2("3.3.  Thiết kế kiến trúc tổng thể hệ thống")
# ────────────────────────────────────────────────────────────────────────────

body(
    "Phần này trình bày bức tranh tổng thể về kiến trúc hệ thống và luồng dữ "
    "liệu, giúp hình dung rõ cách các thành phần phối hợp với nhau trong quá "
    "trình vận hành thực tế."
)

h3("3.3.1.  Sơ đồ kiến trúc tổng thể")

body(
    "Hệ thống được tổ chức theo mô hình bốn tầng như mô tả trong Hình 3.1. "
    "Người dùng (nhân viên, bác sĩ, admin) tương tác thông qua giao diện "
    "Frontend ReactJS chạy trên trình duyệt web. Mọi yêu cầu từ Frontend đều "
    "được gửi qua RESTful API đến Backend Spring Boot — đây là trung tâm xử lý "
    "nghiệp vụ của hệ thống. Khi cần xử lý ảnh, Backend gọi sang AI Python "
    "Service qua HTTP nội bộ. Cả Backend và AI Service đều đọc/ghi dữ liệu vào "
    "PostgreSQL, trong đó kết quả OCR được lưu dưới dạng JSONB để tối ưu "
    "hiệu suất truy xuất."
)

note_fig("(Hình 3.1: Sơ đồ kiến trúc tổng thể hệ thống — sinh viên chèn hình vào đây)")

body(
    "Mỗi thành phần trong kiến trúc được đóng gói độc lập bằng Docker container, "
    "cho phép triển khai và khởi động lại từng thành phần mà không ảnh hưởng đến "
    "toàn bộ hệ thống. Docker Compose được sử dụng để định nghĩa và khởi chạy "
    "toàn bộ stack trong một lệnh duy nhất, thuận tiện cho cả môi trường phát "
    "triển lẫn triển khai thực tế."
)

h3("3.3.2.  Luồng giao tiếp giữa các thành phần")

body(
    "Luồng xử lý chính khi người dùng tải ảnh bệnh án và yêu cầu trích xuất "
    "thông tin diễn ra theo các bước sau:"
)

p_steps = [
    ("Bước 1 — Tải ảnh",
     "Người dùng chọn file ảnh bệnh án trên giao diện Frontend. Frontend gửi "
     "multipart/form-data request đến API POST /api/v1/medical-record/upload kèm "
     "theo ảnh, thông tin bệnh nhân và metadata (khoa, loại hồ sơ, ghi chú)."),
    ("Bước 2 — Lưu trữ ban đầu",
     "Backend nhận request, xác thực JWT và quyền hạn người dùng, lưu file ảnh "
     "vào thư mục lưu trữ trên server và tạo bản ghi medical_record trong "
     "PostgreSQL với trạng thái PROCESSING."),
    ("Bước 3 — Gọi AI Service",
     "Backend gửi HTTP POST request đến AI Python Service kèm đường dẫn ảnh. "
     "AI Service thực hiện pipeline: đọc ảnh → tiền xử lý OpenCV → YOLO11n-OBB "
     "phát hiện vùng → cắt từng vùng → Tesseract OCR đọc chữ → tổng hợp "
     "kết quả thành JSON."),
    ("Bước 4 — Cập nhật kết quả",
     "AI Service trả về JSON chứa extracted_data (thông tin bệnh nhân, chẩn đoán, "
     "toa thuốc,...) và lab_data (kết quả xét nghiệm). Backend cập nhật bản ghi "
     "medical_record với dữ liệu này, chuyển trạng thái sang EXTRACTED."),
    ("Bước 5 — Kiểm tra và chỉnh sửa",
     "Frontend hiển thị ảnh gốc và kết quả trích xuất song song. Nhân viên kiểm "
     "tra, chỉnh sửa các trường chưa chính xác qua API PATCH /api/v1/medical-record/"
     "field/update. Sau khi hài lòng, nộp hồ sơ qua POST /medical-record/{id}/submit, "
     "chuyển trạng thái sang PENDING_DOCTOR_REVIEW."),
    ("Bước 6 — Phê duyệt",
     "Bác sĩ xem lại hồ sơ và thực hiện phê duyệt qua POST /medical-record/{id}/"
     "approve. Trạng thái chuyển sang APPROVED, hồ sơ được lưu chính thức vào "
     "hệ thống kèm thông tin người phê duyệt và thời gian phê duyệt."),
]
for label, text in p_steps:
    p = doc.add_paragraph()
    pf = p.paragraph_format
    pf.left_indent = Cm(1.0); pf.first_line_indent = Cm(-0.5)
    pf.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    pf.space_before = Pt(2); pf.space_after = Pt(4)
    pf.line_spacing = LINE_SP
    r1 = p.add_run(f"{label}: ")
    _rf(r1, bold=True)
    r2 = p.add_run(text)
    _rf(r2)

# ────────────────────────────────────────────────────────────────────────────
h2("3.4.  Thiết kế chức năng hệ thống (Mô hình UML)")
# ────────────────────────────────────────────────────────────────────────────

body(
    "Phần này sử dụng các ký hiệu UML để mô hình hóa các chức năng và luồng "
    "hoạt động của hệ thống, phục vụ việc thống nhất hiểu biết giữa các thành "
    "viên nhóm và làm tài liệu đặc tả cho quá trình cài đặt."
)

h3("3.4.1.  Sơ đồ Use Case")

body(
    "Hệ thống có ba tác nhân chính tương tác với các use case khác nhau tùy "
    "theo vai trò và quyền hạn được cấp phát."
)

note_fig("(Hình 3.2: Sơ đồ Use Case tổng quát — sinh viên chèn hình vào đây)")

body("Bảng 3.1 mô tả chi tiết các use case chính của từng vai trò trong hệ thống:")

add_table(
    ["Vai trò", "Use Case", "Mô tả ngắn"],
    [
        ("Admin",    "Quản lý tài khoản",       "Tạo, xem, sửa, vô hiệu hóa tài khoản nhân viên và bác sĩ"),
        ("Admin",    "Phân quyền vai trò",       "Gán/thu hồi quyền hạn trên từng module cho từng vai trò"),
        ("Admin",    "Xem toàn bộ bệnh án",      "Xem và quản lý tất cả hồ sơ bệnh án trong hệ thống"),
        ("Admin",    "Xóa bệnh án",              "Xóa hồ sơ bệnh án không hợp lệ hoặc thừa"),
        ("Nhân viên","Quản lý bệnh nhân",        "Tìm kiếm, tạo mới, cập nhật thông tin bệnh nhân"),
        ("Nhân viên","Tải ảnh bệnh án",          "Upload ảnh bệnh án và gắn với hồ sơ bệnh nhân"),
        ("Nhân viên","Xem kết quả OCR",          "Xem ảnh gốc và kết quả trích xuất song song"),
        ("Nhân viên","Chỉnh sửa kết quả OCR",    "Sửa các trường thông tin nhận dạng chưa chính xác"),
        ("Nhân viên","Nộp hồ sơ để duyệt",       "Chuyển hồ sơ sang trạng thái chờ bác sĩ phê duyệt"),
        ("Bác sĩ",  "Xem hồ sơ chờ duyệt",      "Xem danh sách bệnh án đang ở trạng thái PENDING_DOCTOR_REVIEW"),
        ("Bác sĩ",  "Phê duyệt bệnh án",         "Xác nhận thông tin và phê duyệt hồ sơ chính thức"),
        ("Bác sĩ",  "Trả hồ sơ để chỉnh sửa",   "Từ chối phê duyệt, yêu cầu nhân viên bổ sung/sửa thông tin"),
        ("Tất cả",  "Đăng nhập / Đăng xuất",     "Xác thực bằng JWT, làm mới token khi hết hạn"),
        ("Tất cả",  "Xem thông tin cá nhân",     "Xem và cập nhật hồ sơ tài khoản của chính mình"),
    ],
    "Bảng 3.1: Danh sách use case theo vai trò"
)

h3("3.4.2.  Sơ đồ hoạt động (Activity Diagram)")

body(
    "Nhóm xây dựng sơ đồ hoạt động cho ba luồng nghiệp vụ phức tạp và quan "
    "trọng nhất của hệ thống."
)

body(
    "Luồng 1 — Đăng nhập và xác thực: Người dùng nhập thông tin đăng nhập → "
    "Backend kiểm tra username/password → Nếu đúng, kiểm tra trạng thái tài "
    "khoản (isActive, emailVerifyAt) → Tạo Access Token (1h) và Refresh Token "
    "(7 ngày) → Trả về Frontend lưu vào bộ nhớ cục bộ. Khi Access Token hết "
    "hạn, Frontend tự động gọi refresh-token API để lấy token mới mà không "
    "cần đăng nhập lại."
)

note_fig("(Hình 3.3: Activity Diagram — Luồng đăng nhập — sinh viên chèn hình vào đây)")

body(
    "Luồng 2 — Tải ảnh và xử lý OCR: Nhân viên chọn bệnh nhân và tải ảnh → "
    "Backend lưu ảnh gốc, khởi tạo bản ghi trạng thái PROCESSING → Gọi AI "
    "Service bất đồng bộ → AI Service xử lý YOLO + Tesseract → Trả kết quả "
    "JSON → Backend cập nhật extracted_data, lab_data, chuyển trạng thái "
    "EXTRACTED → Frontend nhận thông báo và hiển thị kết quả để nhân viên "
    "kiểm tra."
)

note_fig("(Hình 3.4: Activity Diagram — Luồng xử lý OCR — sinh viên chèn hình vào đây)")

body(
    "Luồng 3 — Phê duyệt hồ sơ: Nhân viên hoàn thiện chỉnh sửa và nhấn Nộp → "
    "Trạng thái chuyển sang PENDING_DOCTOR_REVIEW → Bác sĩ nhận thông báo có "
    "hồ sơ chờ duyệt → Bác sĩ xem xét thông tin → Nếu đồng ý: Approve, trạng "
    "thái chuyển APPROVED, ghi lại approved_by và approved_at → Nếu từ chối: "
    "trả lại trạng thái EXTRACTED kèm ghi chú yêu cầu chỉnh sửa → Nhân viên "
    "chỉnh sửa và nộp lại."
)

note_fig("(Hình 3.5: Activity Diagram — Luồng phê duyệt bệnh án — sinh viên chèn hình vào đây)")

# ────────────────────────────────────────────────────────────────────────────
h2("3.5.  Thiết kế cơ sở dữ liệu")
# ────────────────────────────────────────────────────────────────────────────

body(
    "Cơ sở dữ liệu được thiết kế trên PostgreSQL 15, gồm bảy bảng chính với "
    "các mối quan hệ rõ ràng đảm bảo tính toàn vẹn dữ liệu và hỗ trợ hiệu "
    "quả các truy vấn nghiệp vụ."
)

h3("3.5.1.  Sơ đồ thực thể liên kết (ERD)")

body(
    "Sơ đồ ERD trong Hình 3.6 thể hiện các thực thể chính và mối quan hệ giữa "
    "chúng. Một bệnh nhân (patients) có thể có nhiều hồ sơ bệnh án "
    "(medical_records). Mỗi bệnh án được tạo bởi một tài khoản (account) và có "
    "thể được phê duyệt bởi một tài khoản khác. Một tài khoản có thể thuộc "
    "nhiều vai trò (role) thông qua bảng trung gian rf_account_role. Mỗi vai "
    "trò được gán các quyền hạn (permission) với các hành động cụ thể thông "
    "qua bảng permission_role."
)

note_fig("(Hình 3.6: Sơ đồ ERD — sinh viên chèn hình vào đây)")

h3("3.5.2.  Thiết kế Schema chi tiết")

body("Dưới đây là mô tả chi tiết schema của các bảng chính trong hệ thống.")

body("a) Bảng account — Lưu thông tin tài khoản người dùng")
add_table(
    ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
    [
        ("id",              "BIGINT",       "PK, AUTO_INCREMENT",   "Khóa chính, tự tăng"),
        ("name",            "VARCHAR(255)", "",                      "Họ và tên đầy đủ"),
        ("birthday",        "DATE",         "",                      "Ngày sinh"),
        ("phone_number",    "VARCHAR(12)",  "",                      "Số điện thoại"),
        ("email",           "VARCHAR(255)", "UNIQUE, NOT NULL",      "Địa chỉ email đăng nhập"),
        ("username",        "VARCHAR(255)", "UNIQUE, NOT NULL",      "Tên đăng nhập"),
        ("password",        "VARCHAR(255)", "NOT NULL",              "Mật khẩu đã mã hóa BCrypt"),
        ("is_active",       "BOOLEAN",      "",                      "Trạng thái kích hoạt tài khoản"),
        ("is_delete",       "BOOLEAN",      "DEFAULT false",         "Cờ xóa mềm"),
        ("email_verify_at", "TIMESTAMP",    "",                      "Thời điểm xác thực email"),
        ("gender",          "VARCHAR(10)",  "DEFAULT 'other'",       "Giới tính (male/female/other)"),
        ("photo_url",       "VARCHAR(255)", "",                      "URL ảnh đại diện"),
        ("created_by",      "BIGINT",       "",                      "ID tài khoản tạo (Admin)"),
        ("created_at",      "TIMESTAMP",    "DEFAULT NOW()",         "Thời điểm tạo"),
        ("updated_at",      "TIMESTAMP",    "",                      "Thời điểm cập nhật cuối"),
    ],
    "Bảng 3.2: Schema bảng account"
)

body("b) Bảng patients — Lưu thông tin hồ sơ bệnh nhân")
add_table(
    ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
    [
        ("id",         "BIGINT",       "PK, AUTO_INCREMENT",   "Khóa chính"),
        ("bhyt",       "VARCHAR(30)",  "UNIQUE, NOT NULL",     "Số thẻ bảo hiểm y tế (duy nhất)"),
        ("name",       "VARCHAR(200)", "NOT NULL",             "Họ và tên bệnh nhân"),
        ("dob",        "DATE",         "",                     "Ngày sinh"),
        ("gender",     "VARCHAR(10)",  "",                     "Giới tính"),
        ("address",    "TEXT",         "",                     "Địa chỉ thường trú"),
        ("phone",      "VARCHAR(20)",  "",                     "Số điện thoại liên hệ"),
        ("created_at", "TIMESTAMP",    "DEFAULT NOW()",        "Thời điểm tạo hồ sơ"),
        ("updated_at", "TIMESTAMP",    "",                     "Thời điểm cập nhật cuối"),
    ],
    "Bảng 3.3: Schema bảng patients"
)

body("c) Bảng medical_records — Lưu hồ sơ bệnh án và kết quả OCR")
add_table(
    ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
    [
        ("id",                  "BIGINT",       "PK, AUTO_INCREMENT",   "Khóa chính"),
        ("record_number",       "VARCHAR(50)",  "UNIQUE",               "Mã số hồ sơ bệnh án"),
        ("patient_id",          "BIGINT",       "FK → patients.id",     "Liên kết bệnh nhân"),
        ("uploaded_by",         "BIGINT",       "NOT NULL",             "ID nhân viên tải lên"),
        ("file_name",           "VARCHAR(255)", "",                     "Tên file ảnh gốc"),
        ("file_type",           "VARCHAR(10)",  "",                     "Định dạng file (jpg/png/pdf)"),
        ("original_image_path", "VARCHAR(500)", "",                     "Đường dẫn ảnh gốc trên server"),
        ("department",          "VARCHAR(100)", "",                     "Khoa điều trị"),
        ("record_type",         "VARCHAR(50)",  "",                     "Loại hồ sơ"),
        ("status",              "VARCHAR(30)",  "DEFAULT 'Processing'", "Trạng thái vòng đời hồ sơ"),
        ("extracted_data",      "JSONB",        "",                     "Dữ liệu trích xuất từ OCR (JSON)"),
        ("lab_data",            "JSONB",        "DEFAULT '[]'",         "Kết quả xét nghiệm (JSON array)"),
        ("notes",               "TEXT",         "",                     "Ghi chú thêm"),
        ("verified_by",         "BIGINT",       "",                     "ID nhân viên xác nhận"),
        ("verified_at",         "TIMESTAMP",    "",                     "Thời điểm xác nhận"),
        ("approved_by",         "BIGINT",       "",                     "ID bác sĩ phê duyệt"),
        ("approved_at",         "TIMESTAMP",    "",                     "Thời điểm phê duyệt"),
        ("created_at",          "TIMESTAMP",    "DEFAULT NOW()",        "Thời điểm tạo"),
        ("updated_at",          "TIMESTAMP",    "",                     "Thời điểm cập nhật cuối"),
    ],
    "Bảng 3.4: Schema bảng medical_records"
)

body("d) Bảng role — Định nghĩa vai trò trong hệ thống")
add_table(
    ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
    [
        ("id",            "BIGINT",       "PK, AUTO_INCREMENT", "Khóa chính"),
        ("name",          "VARCHAR(255)", "",                   "Tên hiển thị vai trò (Admin, Bác sĩ, Nhân viên)"),
        ("code",          "VARCHAR(255)", "",                   "Mã vai trò (admin, doctor, employee)"),
        ("is_active",     "BOOLEAN",      "",                   "Trạng thái kích hoạt vai trò"),
        ("is_super_admin","BOOLEAN",      "",                   "Cờ xác định đây là vai trò quản trị tối cao"),
        ("created_at",    "TIMESTAMP",    "",                   "Thời điểm tạo"),
        ("updated_at",    "TIMESTAMP",    "",                   "Thời điểm cập nhật cuối"),
    ],
    "Bảng 3.5: Schema bảng role"
)

body("e) Bảng permission — Định nghĩa các module quyền hạn")
add_table(
    ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
    [
        ("id",        "BIGINT",       "PK, AUTO_INCREMENT", "Khóa chính"),
        ("name",      "VARCHAR(255)", "",                   "Tên module quyền hạn"),
        ("slug",      "VARCHAR(255)", "",                   "Định danh dạng slug (accounts, medical-records,...)"),
        ("sort",      "VARCHAR(255)", "",                   "Thứ tự hiển thị trong menu"),
        ("is_hidden", "BOOLEAN",      "",                   "Ẩn khỏi menu giao diện nếu true"),
    ],
    "Bảng 3.6: Schema bảng permission"
)

body("f) Bảng permission_role — Gán quyền hành động cho từng vai trò")
add_table(
    ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
    [
        ("id",            "BIGINT", "PK, AUTO_INCREMENT",       "Khóa chính"),
        ("role_id",       "BIGINT", "FK → role.id",             "Liên kết vai trò"),
        ("permission_id", "BIGINT", "FK → permission.id",       "Liên kết module quyền hạn"),
        ("actions",       "JSON",   "",                          "Danh sách hành động được phép\n(view, create, edit, delete, approve, cancel)"),
    ],
    "Bảng 3.7: Schema bảng permission_role"
)

body("g) Bảng rf_account_role — Liên kết tài khoản với vai trò")
add_table(
    ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mô tả"],
    [
        ("id",         "BIGINT", "PK, AUTO_INCREMENT", "Khóa chính"),
        ("account_id", "BIGINT", "FK → account.id",   "Liên kết tài khoản người dùng"),
        ("role_id",    "BIGINT", "FK → role.id",       "Liên kết vai trò được gán"),
    ],
    "Bảng 3.8: Schema bảng rf_account_role"
)

body(
    "Trạng thái vòng đời của bệnh án trong cột status tuân theo enum "
    "MedicalRecordStatus với bốn giá trị: Processing (đang xử lý AI), "
    "Extracted (đã trích xuất, chờ nhân viên kiểm tra), Pending Doctor Review "
    "(đã nộp, chờ bác sĩ phê duyệt) và Approved (đã phê duyệt chính thức). "
    "Luồng chuyển trạng thái một chiều này đảm bảo tính nhất quán của dữ liệu "
    "và hỗ trợ kiểm tra lịch sử xử lý hồ sơ một cách rõ ràng."
)

# ─── Save ─────────────────────────────────────────────────────────────────────
out = r"d:\HK2_4\DoAn\medical-be\docs\Chuong3_Phan_Tich_Thiet_Ke.docx"
doc.save(out)
print(f"Saved: {out}")
