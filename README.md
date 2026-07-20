# MED-OCR Backend

Backend Spring Boot cho hệ thống quản lý bệnh án điện tử có OCR. API phục vụ 4 nhóm người dùng:

- **employee**: upload bệnh án, kiểm tra/chỉnh sửa dữ liệu OCR, submit để bác sĩ duyệt
- **doctor**: xem bệnh án chờ duyệt, approve/reject, chỉnh sửa dữ liệu OCR khi cần, kê toa thuốc
- **admin**: quản lý tài khoản/phân quyền, xem toàn bộ bệnh án, quản lý bệnh nhân, xóa bệnh án, xem audit log/dashboard, quản lý webhook
- **patient**: tự tra cứu bệnh án và toa thuốc của chính mình

Ứng dụng lưu dữ liệu nghiệp vụ trong PostgreSQL, gọi OCR Model Service để xử lý ảnh, lưu ảnh upload lên AWS S3 và gửi email (quên mật khẩu, thông báo) qua SMTP.

## Chức Năng Hiện Có

- Đăng ký/đăng nhập/đăng xuất, refresh token, quên/đặt lại mật khẩu qua email
- Phân quyền theo permission dạng `resource:action` gắn vào JWT (Spring Security `@PreAuthorize`)
- Upload ảnh/PDF bệnh án, gọi OCR Model Service để trích xuất dữ liệu
- Quản lý bệnh án: xem danh sách/chi tiết, sửa field OCR, submit/approve/reject/resubmit, xóa mềm
- Quản lý bệnh nhân: tạo, cập nhật, tìm kiếm theo BHYT, tra cứu hồ sơ
- Quản lý đơn thuốc: tạo/cập nhật/phát hành/in toa thuốc theo bệnh án, danh mục thuốc
- Quản lý tài khoản và vai trò cho admin
- Audit log cho thao tác trên bệnh án
- Dashboard thống kê (tổng quan, theo trạng thái, theo khoa, hiệu suất người dùng, theo thời gian)
- Thông báo (notification) trong hệ thống
- Quản lý webhook (admin) để nhận callback từ OCR Model Service
- Đa ngôn ngữ cho message lỗi/response (i18n)

## Công Nghệ

- Java 21, Spring Boot 3.2.5 (Web, WebFlux, Validation, Mail, Thymeleaf)
- Spring Security, JWT (jjwt)
- PostgreSQL 15
- JPA/Hibernate, Flyway
- MapStruct, Lombok
- Springdoc OpenAPI/Swagger
- JUnit 5, Testcontainers, Spring Security Test
- Docker, Docker Compose
- Nginx, Certbot/Let's Encrypt
- AWS S3

## Cấu Trúc Thư Mục

```text
src/main/java/com/example/medical_be/
  auth/         UserDetails và UserDetailsService cho Spring Security
  config/       Cấu hình Security, OpenAPI, Validation, Web, Async
  controller/   REST controller theo domain
  converter/    Convert dữ liệu request/entity
  dto/          Request/response DTO
  entity/       JPA entity
  event/        Application event (vd. audit log, notification)
  exception/    Exception và GlobalExceptionHandler
  i18n/         Message translator đa ngôn ngữ
  jwt/          JwtService, JwtAuthenticationFilter
  listener/     Event listener
  mapper/       MapStruct mapper entity <-> DTO
  properties/   Cấu hình bind từ application.yaml
  repository/   Spring Data JPA repository
  routes/       Hằng số đường dẫn API (APIRoutes)
  seeder/       Seed dữ liệu khởi tạo (role, permission, ...)
  service/      Business logic
  support/      Hằng số, helper dùng chung
  swagger/      Cấu hình annotation Swagger dùng chung
  util/         Utility function
  validation/   Custom validator
```

## Kiến Trúc Chính

### Auth

- Access token (JWT) được xác thực bởi `JwtAuthenticationFilter` cho mỗi request.
- Role và permission (`resource:action`) được nhúng vào JWT như authorities, kiểm tra bằng `@PreAuthorize("hasAuthority('...')")` ở từng endpoint.
- Có endpoint `refresh-token` để cấp access token mới khi hết hạn, và `forgot-password`/`reset-password` gửi email qua SMTP.
- Các file liên quan: `jwt/JwtService.java`, `jwt/JwtAuthenticationFilter.java`, `auth/AccountUserDetailsService.java`, `config/SecurityConfig.java`.

### Dữ liệu khởi tạo

`seeder/` chạy khi ứng dụng khởi động để seed role (`admin`, `doctor`, `employee`, `patient`) và permission mặc định, đảm bảo hệ thống có sẵn dữ liệu phân quyền.

## Kiến Trúc Production

```text
Client / Frontend
        |
        v
https://api.medicalocr-nthoa.io.vn
        |
        v
Nginx trên EC2
        |
        v
Spring Boot container :8080
        |
        +--> PostgreSQL container :5432
        +--> OCR Model Service: https://model.medicalocr-nthoa.io.vn
        +--> AWS S3 bucket
```

PostgreSQL chạy bằng Docker container và lưu dữ liệu trong Docker volume `postgres_data`. Backend kết nối database qua Docker network bằng hostname `db`.

## Cấu Hình Môi Trường

Tạo file `.env` từ file mẫu:

```bash
cp .env.example .env
```

Các biến quan trọng:

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_db_password

APP_KEY=your_jwt_secret
ENCRYPTION_SECRET_KEY=your_encryption_secret

SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_gmail_app_password

APP_SERVER_URL=https://api.medicalocr-nthoa.io.vn
OCR_API_URL=https://model.medicalocr-nthoa.io.vn

AWS_REGION=ap-southeast-2
AWS_BUCKET_NAME=medicaler-image
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
```

Không commit file `.env` lên Git.

## Chạy Bằng Docker Compose

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

Các service chính:

- `app`: Spring Boot backend.
- `db`: PostgreSQL 15.
- `postgres_data`: Docker volume lưu dữ liệu database.

Trong Docker Compose, backend dùng JDBC URL:

```text
jdbc:postgresql://db:5432/medical
```

## Deploy Trên EC2

Clone source:

```bash
mkdir -p ~/apps
cd ~/apps
git clone https://github.com/Thanh-hoa/medical-be.git medical-be
cd medical-be
```

Tạo `.env`:

```bash
cp .env.example .env
nano .env
```

Tạo secret:

```bash
openssl rand -base64 32
```

Khởi chạy backend và database:

```bash
sudo docker compose up -d --build
sudo docker compose ps
```

Kiểm tra log:

```bash
sudo docker compose logs -f app
```

## Restore Database

Upload file backup, ví dụ `medical_backup.sql`, lên EC2:

```text
/home/ubuntu/medical_backup.sql
```

Restore vào PostgreSQL container:

```bash
cat ~/medical_backup.sql | sudo docker compose exec -T db psql -U postgres -d medical
```

Kiểm tra dữ liệu:

```bash
sudo docker compose exec db psql -U postgres -d medical
```

Trong `psql`:

```sql
\dt
select count(*) from account;
select count(*) from patients;
select count(*) from medical_records;
\q
```

Không dùng lệnh sau trên production nếu không muốn xóa database:

```bash
sudo docker compose down -v
```

## Nginx Và HTTPS

Nginx proxy domain vào backend:

```nginx
server {
    listen 80;
    server_name api.medicalocr-nthoa.io.vn;

    client_max_body_size 25M;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Cấp HTTPS bằng Certbot:

```bash
sudo certbot --nginx -d api.medicalocr-nthoa.io.vn
sudo certbot renew --dry-run
```

AWS Security Group nên mở:

```text
22   SSH
80   HTTP
443  HTTPS
```

Không public port `5432` và `8080`.

## Lưu Ảnh Lên AWS S3

Ảnh upload được xử lý trong `FileStorageService`.

Luồng xử lý:

1. Kiểm tra định dạng file: `jpg`, `jpeg`, `png`, `webp`.
2. Tạo tên file mới bằng UUID.
3. Upload file lên S3 bucket.
4. Trả về URL ảnh.

URL trả về có dạng:

```text
https://{bucket}.s3.{region}.amazonaws.com/{filename}
```

Ví dụ:

```text
https://medicaler-image.s3.ap-southeast-2.amazonaws.com/image-id.png
```

Credential S3:

- Local/dev: dùng `AWS_ACCESS_KEY_ID` và `AWS_SECRET_ACCESS_KEY`.
- Production trên EC2: nên dùng IAM Role cho EC2 và để trống access key trong `.env`.

Quyền IAM tối thiểu:

```json
{
  "Effect": "Allow",
  "Action": [
    "s3:PutObject"
  ],
  "Resource": "arn:aws:s3:::medicaler-image/*"
}
```

## API Chính

Tất cả endpoint có prefix `/api/v1`. Danh sách đầy đủ và schema request/response xem ở [Swagger](#swagger).

### Auth & Account

| Method | Endpoint | Mô tả |
|--------|----------|------|
| POST | `/auth/login` | Đăng nhập |
| POST | `/auth/logout` | Đăng xuất |
| POST | `/auth/refresh-token` | Làm mới token |
| POST | `/auth/forgot-password` | Quên mật khẩu (gửi email) |
| POST | `/auth/reset-password` | Đặt lại mật khẩu |
| GET | `/account/profile` | Thông tin tài khoản đang đăng nhập |
| GET | `/account/my-permissions` | Danh sách quyền của tài khoản |
| GET | `/account/list` | Danh sách tài khoản (admin) |
| POST | `/account/create` | Tạo tài khoản (admin) |
| PUT | `/account/update` | Cập nhật tài khoản (admin) |
| DELETE | `/account/delete` | Xóa tài khoản (admin) |

### Patient

| Method | Endpoint | Mô tả |
|--------|----------|------|
| GET | `/patient/search` | Tìm bệnh nhân theo BHYT |
| GET | `/patient/me` | Hồ sơ của bệnh nhân đang đăng nhập |
| GET | `/patient/me/records/{id}` | Chi tiết bệnh án của chính mình |
| GET | `/patient/me/records/{id}/prescription` | Toa thuốc của chính mình |
| GET | `/patient/list` | Danh sách bệnh nhân |
| POST | `/patient/create` | Tạo bệnh nhân |
| PUT | `/patient/update/{id}` | Cập nhật bệnh nhân |

### Medical Record

| Method | Endpoint | Mô tả |
|--------|----------|------|
| POST | `/medical-record/upload` | Upload ảnh và gọi OCR |
| GET | `/medical-record/list` | Danh sách bệnh án |
| GET | `/medical-record/pending-review` | Danh sách bệnh án chờ duyệt |
| GET | `/medical-record/{id}` | Chi tiết bệnh án |
| PUT | `/medical-record/update-detail` | Cập nhật chi tiết bệnh án |
| PUT | `/medical-record/{id}/submit` | Nộp bệnh án để duyệt |
| PUT | `/medical-record/{id}/approve` | Duyệt bệnh án |
| PUT | `/medical-record/{id}/reject` | Từ chối bệnh án |
| PUT | `/medical-record/{id}/resubmit` | Nộp lại bệnh án |
| DELETE | `/medical-record/{id}` | Xóa mềm bệnh án |

### Prescription & Medicine

| Method | Endpoint | Mô tả |
|--------|----------|------|
| POST | `/prescription/medical-record/{recordId}` | Tạo toa thuốc cho bệnh án |
| GET | `/prescription/medical-record/{recordId}` | Lấy toa thuốc theo bệnh án |
| PUT | `/prescription/{id}` | Cập nhật toa thuốc |
| PUT | `/prescription/{id}/issue` | Phát hành toa thuốc |
| GET | `/prescription/{id}/print` | Lấy dữ liệu in toa thuốc |
| GET | `/medicine/list` | Danh sách thuốc |

### Dashboard, Audit Log & Notification

| Method | Endpoint | Mô tả |
|--------|----------|------|
| GET | `/dashboard/overview` | Số liệu tổng quan |
| GET | `/dashboard/records-by-status` | Thống kê bệnh án theo trạng thái |
| GET | `/dashboard/records-by-department` | Thống kê bệnh án theo khoa |
| GET | `/dashboard/user-performance` | Hiệu suất người dùng |
| GET | `/dashboard/timeline` | Thống kê theo thời gian |
| GET | `/audit-logs` | Danh sách audit log |
| GET | `/audit-logs/{id}` | Chi tiết audit log |
| GET | `/notifications` | Danh sách thông báo |
| GET | `/notifications/unread-count` | Số thông báo chưa đọc |
| PUT | `/notifications/{id}/read` | Đánh dấu đã đọc |

### Common & Admin

| Method | Endpoint | Mô tả |
|--------|----------|------|
| POST | `/common/upload/media` | Upload file dùng chung |
| GET | `/common/roles` | Danh sách vai trò |
| GET | `/config/permission/menu` | Menu theo quyền |
| GET/POST/PUT/DELETE | `/admin/webhooks` | Quản lý webhook (admin only) |

## Swagger

Local:

```text
http://localhost:8080/documents/swagger-ui/index.html
```

Production:

```text
https://api.medicalocr-nthoa.io.vn/documents/swagger-ui/index.html
```

Swagger có thể được bật/tắt trong `application-prod.yaml`.

## Test

Unit test và integration test (JUnit 5, Testcontainers) nằm trong `src/test/java`.

```bash
./mvnw clean verify
```

## CI

GitHub Actions CI nằm tại:

```text
.github/workflows/ci.yml
```

CI dùng PostgreSQL service và chạy:

```bash
./mvnw clean verify
```

## Vận Hành

Start lại backend:

```bash
cd ~/apps/medical-be
sudo docker compose up -d
sudo docker compose ps
```

Deploy code mới:

```bash
cd ~/apps/medical-be
git pull
sudo docker compose up -d --build app
sudo docker compose logs -f app
```

Backup database:

```bash
mkdir -p ~/backups/medical-be
sudo docker compose exec -T db pg_dump -U postgres -d medical > ~/backups/medical-be/medical_$(date +%F_%H-%M).sql
```