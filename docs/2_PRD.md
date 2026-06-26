# TÀI LIỆU YÊU CẦU SẢN PHẨM (PRD) - DỰ ÁN MED-OCR

## 1. Mở đầu & Phạm vi sản phẩm
Med-OCR là hệ thống trích xuất thông tin bệnh án tiếng Việt tự động theo mô hình monolith (Java Spring Boot + Python AI + React). Mục tiêu là giảm thời gian nhập liệu thủ công, cho phép nhân viên upload ảnh bệnh án, hệ thống OCR tự động trích xuất dữ liệu, sau đó bác sĩ phê duyệt để đảm bảo độ chính xác 100%.

## 2. Tính năng ứng dụng (Feature Specifications)

### 2.1. Phân hệ Nhân viên (Staff)
Xác thực người dùng:
- Đăng nhập bằng username/password, sử dụng JWT.
- Đăng xuất và refresh token.

Upload và xử lý bệnh án:
- Upload ảnh bệnh án (JPG/PNG/PDF), lưu metadata và tệp gốc.
- Gọi AI async (YOLO + Tesseract OCR) để trích xuất dữ liệu.

Kiểm tra và chỉnh sửa dữ liệu:
- Giao diện chia đôi (ảnh gốc + dữ liệu trích xuất).
- Chỉnh sửa inline từng trường, hiện confidence score.
- Lưu kết quả để chuyển sang trạng thái chờ phê duyệt.

Lịch sử bệnh án:
- Danh sách hồ sơ đã upload, lọc theo trạng thái/ngày/tên.

### 2.2. Phân hệ Bác sĩ (Doctor)
Danh sách chờ phê duyệt:
- Xem hồ sơ đang chờ, sắp xếp theo ngày.

Phê duyệt hồ sơ:
- Phê duyệt hoặc từ chối với lý do, cập nhật trạng thái.

Tra cứu bệnh nhân:
- Tìm kiếm theo tên/BHYT/CMND, xem tất cả lần khám.

Thống kê:
- Dashboard thống kê số hồ sơ phê duyệt theo ngày/tuần/tháng.

### 2.3. Phân hệ Quản trị viên (Admin)
Quản lý tài khoản:
- Tạo/Sửa/Xóa/Khóa user, gán vai trò, soft delete.

Audit logs:
- Xem toàn bộ log CRUD, lọc theo user/action/date.

Quản lý metadata:
- Cập nhật ICD-10, loại bệnh án, danh sách khoa phòng.

## 3. Yêu cầu phi chức năng (Non-Functional Requirements)
Hiệu năng (Performance):
- OCR có thể mất 5-10s, xử lý bất đồng bộ (async) để không block UI.

Khả năng mở rộng (Scalability):
- Có thể tách queue và AI worker (RabbitMQ/Kafka) để xử lý batch.

Bảo mật hệ thống (Security):
- Mật khẩu mã hóa bcrypt, JWT có cơ chế refresh.
- Phân quyền rõ ràng (Staff/Doctor/Admin).
- Mã hóa dữ liệu bệnh nhân, audit log toàn bộ CRUD.
- Bắt buộc HTTPS/TLS cho giao tiếp.

Toàn vẹn dữ liệu (Data Integrity):
- Dữ liệu bệnh án chỉ được lưu chính thức sau khi bác sĩ phê duyệt.
- Log thao tác và lịch sử phiên bản để đối chiếu khi cần.
