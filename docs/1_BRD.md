# BRD - HỆ THỐNG TRÍCH XUẤT THÔNG TIN BỆNH ÁN (MED-OCR)

## 1. Tổng quan dự án

**Tên dự án:** Med-OCR - Hệ thống trích xuất thông tin bệnh án tiếng Việt tự động

**Kiến trúc:** Monolith (Java Spring Boot 3.x Backend + Python AI Core + React Frontend)

**Mục tiêu:** Giảm thời gian nhập liệu bệnh án bằng YOLO + Tesseract OCR

**Bối cảnh:** Bệnh án hiện vẫn lưu trữ giấy, nhập liệu thủ công → Cần tự động hóa bằng AI Vision

---

## 2. Vai Trò & Quyền Hạn

### Nhân Viên (Staff)
- Upload bệnh án (JPG/PNG/PDF)
- Xem & chỉnh sửa kết quả OCR (giao diện chia đôi: ảnh + dữ liệu)
- Lưu với status "Chờ xác nhận" → chuyển Bác sĩ
- Xem lịch sử bệnh án (lọc theo status, ngày)

### Bác Sĩ
- Xem danh sách chờ phê duyệt
- Phê duyệt/Từ chối bệnh án (review y khoa)
- Tra cứu lịch sử bệnh nhân
- Xem dashboard thống kê

### Quản Trị Viên
- Quản lý tài khoản (tạo/sửa/xóa/khóa)
- Cập nhật metadata (ICD-10, loại bệnh án, khoa phòng)
- Xem audit logs (các CRUD operations)

---

## 4. Yêu cầu nghiệp vụ chi tiết (Detailed Business Requirements)

### 4.1 Yêu cầu Chức năng (Functional Requirements - FR)

#### 4.1.1 Nhân viên (Staff)

| ID | Yêu cầu | Chi tiết |
|----|---------|---------|
| FR-S-001 | Đăng nhập an toàn | Xác thực bằng username/password + JWT token |
| FR-S-002 | Tải lên bệnh án | Hỗ trợ upload ảnh (JPG/PNG/PDF), tải nhiều ảnh cùng lúc |
| FR-S-003 | Kích hoạt AI | Bấm nút "Process" → gọi AI Vision (YOLO + OCR) |
| FR-S-004 | Xem kết quả trích xuất | Hiển thị ảnh gốc song song với dữ liệu đã trích xuất |
| FR-S-005 | Chỉnh sửa (Verification) | Sửa lỗi OCR, thêm/xóa trường dữ liệu |
| FR-S-006 | Lưu nháp | Lưu với status "Chờ xác nhận" để chuyển Bác sĩ |
| FR-S-007 | Xem lịch sử | Danh sách bệnh án đã upload, lọc theo trạng thái |

#### 4.1.2 Bác sĩ (Doctor)

| ID | Yêu cầu | Chi tiết |
|----|---------|---------|
| FR-D-001 | Danh sách chờ | Hiển thị bệnh án có status "Chờ xác nhận", phân trang |
| FR-D-002 | Phê duyệt hồ sơ | Kiểm tra, approve (status → "Đã duyệt") hoặc reject với ghi chú |
| FR-D-003 | Tra cứu bệnh nhân | Tìm theo tên/SĐT, xem lịch sử khám (danh sách) |
| FR-D-004 | Xem biểu đồ | Dashboard thống kê số bệnh nhân/ngày/tuần/tháng (Chart.js/Highcharts) |
| FR-D-005 | So sánh phiên bản | Xem ảnh gốc bất cứ lúc nào để đối chiếu |

#### 4.1.3 Quản trị viên (Admin)

| ID | Yêu cầu | Chi tiết |
|----|---------|---------|
| FR-A-001 | Quản lý tài khoản | Create/Edit/Delete/Block user, gán vai trò |
| FR-A-002 | Quản lý Metadata | Cập nhật ICD-10, loại bệnh án, danh sách khoa phòng |
| FR-A-003 | Xem log AI | Thời gian xử lý, confidence score, tỉ lệ lỗi |
| FR-A-004 | Dashboard admin | Tổng bệnh án, tỉ lệ thành công, độ trễ trung bình |

#### 4.1.4 Chức năng AI & Xử lý (AI/Processing Requirements)

| ID | Yêu cầu | Chi tiết |
|----|---------|---------|
| FR-AI-001 | Preprocessing | Xoay ảnh tự động, làm nét, điều chỉnh độ sáng |
| FR-AI-002 | YOLO Detection | Phát hiện vùng tin, confidence > 95% |
| FR-AI-003 | OCR (Tesseract) | Trích xuất chữ viết, support tiếng Việt |
| FR-AI-004 | Post-processing | Chuẩn hóa dữ liệu, xử lý ký tự lạ, điều chỉnh khoảng cách từ |
| FR-AI-005 | Lưu trữ dữ liệu | Lưu ảnh gốc + dữ liệu trích xuất + metadata |

### 4.2 Yêu cầu Phi Chức năng (Non-Functional Requirements - NFR)

#### 4.2.1 Tính Chính xác (Accuracy)

**Yêu cầu:** Đây là hệ thống y tế, nên tính chính xác là yêu cầu ĐẶNG CẤP:
- Dữ liệu lưu vào database **phải 100% chính xác** (nhân viên và bác sĩ kiểm tra, không tự động save)
- Tính năng **"Chỉnh sửa sau OCR"** là bắt buộc
- Tính năng **"So sánh phiên bản"** để xử lý tranh chấp dữ liệu

#### 4.2.2 Bảo mật (Security)

- ✅ Xác thực JWT token
- ✅ Mã hóa mật khẩu (bcrypt)
- ✅ Phân quyền rõ ràng: Nhân viên không được sửa hồ sơ đã Bác sĩ duyệt
- ✅ Mã hóa dữ liệu bệnh nhân (encryption at rest)

#### 4.2.3 Kiến trúc (Architecture)

- **Cấu trúc:** Monolith (Java Backend + Python AI Core)
- **Tách biệt:** Code logic (Java) riêng biệt từ code AI (Python)
- **Scalability:** Hỗ trợ queue system (RabbitMQ/Kafka) cho batch processing


#### 4.2.4 Compatibility

- Desktop: Chrome, 
- Mobile: Responsive design (hỗ trợ chụp ảnh từ camera)
