# TÀI LIỆU MVP (MINIMUM VIABLE PRODUCT) - MED-OCR

Trong giới hạn đồ án, MVP tập trung vào luồng cơ bản: Upload -> Kiểm tra -> Phê duyệt. Ưu tiên P0 là các chức năng bắt buộc để demo hoạt động end-to-end.

## 1. Dành cho Nhân viên (Staff)
Xác thực:
- Đăng nhập bằng username/password và JWT.

Upload hồ sơ:
- Upload 1 tệp bệnh án (JPG/PNG/PDF, tối đa 10MB).
- Lưu tệp gốc và metadata vào DB.

Kiểm tra dữ liệu OCR:
- Hiển thị ảnh gốc và dữ liệu OCR.
- Chỉnh sửa inline từng trường và lưu lại.

Lịch sử:
- Danh sách hồ sơ đã upload, lọc theo trạng thái/ngày.

## 2. Dành cho Bác sĩ (Doctor)
Danh sách chờ phê duyệt:
- Xem hồ sơ đang chờ, sắp xếp theo ngày.

Phê duyệt tối giản:
- Phê duyệt hoặc từ chối hồ sơ, kèm lý do.

Tra cứu cơ bản:
- Tìm kiếm theo tên/BHYT/CMND để xem lịch sử khám.

## 3. Dành cho Hệ thống / Backend
Kiến trúc:
- Java Spring Boot monolith, kết nối Python AI service.

AI và xử lý bất đồng bộ:
- Gọi AI async để OCR, lưu extracted data và confidence scores.

Cơ sở dữ liệu:
- Bảng Users, Medical Records, Extracted Data, Audit Logs.

Bảo mật:
- JWT + Spring Security, phân quyền Staff/Doctor/Admin.

## 4. Các tính năng Đưa vào Giai đoạn sau (Post-MVP)
- Dashboard thống kê cho Doctor/Admin.
- Tìm kiếm nâng cao và bộ lọc mở rộng.
- Giao diện Admin đầy đủ.
- Tiền xử lý ảnh (tự xoay, làm nét).
- Ứng dụng Mobile.
- Gửi email thông báo tự động.

