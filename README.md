# MED-OCR Backend

Hệ thống trích xuất thông tin bệnh án tiếng Việt sử dụng YOLO và Tesseract OCR.

## Tech Stack

- **Java 21** + **Spring Boot 3.2.5**
- **Spring Security** + JWT (access token 1h, refresh token 7 ngày)
- **PostgreSQL 15+** + JPA/Hibernate
- **Swagger/OpenAPI** — tài liệu API tại `/documents/swagger-ui`
- **Python AI Service** — YOLO + Tesseract OCR (service riêng, giao tiếp qua HTTP)

## Yêu Cầu Môi Trường

- JDK 21+
- PostgreSQL 15+
- Maven 3.8+
- (Tùy chọn) Docker & Docker Compose

## Cài Đặt & Chạy

### Cách 1: Chạy trực tiếp

```bash
# 1. Cấu hình biến môi trường
cp .env.example .env
# Sửa các giá trị trong .env (DB password, APP_KEY, mail credentials)

# 2. Tạo database
psql -U postgres -c "CREATE DATABASE medical;"

# 3. Chạy ứng dụng (seeder tự chạy khi khởi động)
mvn spring-boot:run
```

### Cách 2: Docker Compose

```bash
cp .env.example .env
# Sửa .env, sau đó:
docker-compose up --build
```

Sau khi khởi động, seeder tự động tạo:
- 3 roles: `admin`, `doctor`, `employee`
- 1 tài khoản admin mặc định (xem `.env.example`)
- 6 permission actions và 8 permissions

## Cấu Trúc Thư Mục

```
src/main/java/com/example/medical_be/
├── auth/           # AccountUserDetails, AccountUserDetailsService
├── config/         # SecurityConfig, OpenApiConfig, WebConfig
├── controller/     # REST controllers
├── dto/            # Request/Response DTOs
├── entity/         # JPA entities
├── exception/      # GlobalExceptionHandler, ApplicationException
├── i18n/           # MessageTranslator (đa ngôn ngữ)
├── jwt/            # JwtService, JwtAuthenticationFilter
├── mapper/         # MapStruct mappers (Entity ↔ DTO)
├── repository/     # Spring Data JPA repositories
├── routes/         # APIRoutes constants
├── seeder/         # Database seeders
├── service/        # Business logic
├── support/        # Constants, utils, helpers
└── validation/     # Custom validators
```

## Phân Quyền

3 role: **admin**, **doctor**, **employee**

| Module | Admin | Doctor | Employee |
|--------|-------|--------|----------|
| Quản lý tài khoản (`accounts`) | CRUD | — | — |
| Quản lý bệnh án (`medical-records`) | CRUD | view, edit | view, create, edit |
| Phê duyệt bệnh án (`medical-records-approval`) | view, approve, reject | view, approve, reject | — |
| Tra cứu bệnh nhân (`patient-search`) | view | view | view |
| Dashboard (`dashboard`) | view | view | — |
| Log AI/OCR (`ai-logs`) | view | — | — |

## API Endpoints

### Auth
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/v1/auth/login` | Đăng nhập |
| POST | `/api/v1/auth/logout` | Đăng xuất |
| POST | `/api/v1/auth/refresh-token` | Làm mới token |

### Account
| Method | Endpoint | Quyền | Mô tả |
|--------|----------|-------|-------|
| POST | `/api/v1/account/register` | Public | Tự đăng ký |
| GET | `/api/v1/account/validate-token` | Public | Kích hoạt email |
| GET | `/api/v1/account/profile` | Authenticated | Xem profile |
| PUT | `/api/v1/account/profile` | Authenticated | Cập nhật profile |
| GET | `/api/v1/account/list` | `accounts:view` | Danh sách tài khoản |
| POST | `/api/v1/account/create` | `accounts:create` | Admin tạo tài khoản |
| GET | `/api/v1/account/{id}` | `accounts:view` | Chi tiết tài khoản |
| PUT | `/api/v1/account/update` | `accounts:edit` | Cập nhật tài khoản |
| DELETE | `/api/v1/account/delete` | `accounts:delete` | Xóa tài khoản |

### Medical Record
| Method | Endpoint | Quyền | Mô tả |
|--------|----------|-------|-------|
| POST | `/api/v1/medical-record/upload` | `medical-records:create` | Upload & OCR |
| GET | `/api/v1/medical-record/list` | `medical-records:view` | Danh sách bệnh án |
| GET | `/api/v1/medical-record/pending-review` | `medical-records-approval:create` | Chờ duyệt |
| GET | `/api/v1/medical-record/{id}` | `medical-records:view` | Chi tiết |
| PUT | `/api/v1/medical-record/update` | `medical-records:edit` | Cập nhật |
| PUT | `/api/v1/medical-record/{id}/submit` | `medical-records:create` | Gửi duyệt |
| PUT | `/api/v1/medical-record/{id}/approve` | `medical-records-approval:approve` | Phê duyệt |
| DELETE | `/api/v1/medical-record/{id}` | `medical-records:delete` | Xóa mềm |

> Tài liệu API đầy đủ: Swagger UI tại `http://localhost:8080/documents/swagger-ui`  
> ⚠️ Swagger bị tắt ở profile `prod`

## Tài Khoản Mặc Định (Dev)

Tài khoản admin mặc định được tạo bởi `AccountSeeder` khi khởi động.  
Xem thông tin trong `AccountSeeder.java` hoặc kiểm tra trong DB sau khi seed.

> ⚠️ Đổi mật khẩu ngay sau khi triển khai production.

## Profiles

| Profile | Cách kích hoạt | DDL Auto | Swagger | SQL Log |
|---------|---------------|----------|---------|---------|
| dev (mặc định) | Không cần | `update` | Bật | Bật |
| prod | `SPRING_PROFILES_ACTIVE=prod` | `validate` | Tắt | Tắt |

## Tài Liệu Dự Án

| File | Nội dung |
|------|---------|
| [docs/1_BRD.md](docs/1_BRD.md) | Business Requirements |
| [docs/2_PRD.md](docs/2_PRD.md) | Product Requirements |
| [docs/3_MVP.md](docs/3_MVP.md) | MVP scope |
| [docs/8_DATABASE_SCHEMA.md](docs/8_DATABASE_SCHEMA.md) | Schema database đầy đủ |
