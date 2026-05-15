# MED-OCR Backend

Hệ thống trích xuất thông tin bệnh án tiếng Việt sử dụng YOLO và Tesseract OCR.

## Tech Stack

- **Java 17** + **Spring Boot 3.x**
- **Spring Security** + JWT (access token 1h, refresh token 7 ngày)
- **PostgreSQL 15+** + JPA/Hibernate
- **Swagger/OpenAPI** — tài liệu API tại `/swagger-ui.html`
- **Python AI Service** — YOLO + Tesseract OCR (service riêng, giao tiếp qua HTTP)

## Yêu Cầu Môi Trường

- JDK 17+
- PostgreSQL 15+
- Maven 3.8+

## Cài Đặt & Chạy

```bash
# 1. Clone và cấu hình
cp src/main/resources/application.yaml.example src/main/resources/application.yaml
# Sửa datasource, app.key trong application.yaml

# 2. Tạo database
psql -U postgres -c "CREATE DATABASE medical;"

# 3. Chạy ứng dụng (seeder tự chạy khi khởi động)
mvn spring-boot:run
```

Sau khi khởi động, seeder tự động tạo:
- 3 roles: `admin`, `doctor`, `employee`
- 1 tài khoản admin mặc định
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
├── mapper/         # AccountMapper (Entity ↔ DTO)
├── repository/     # Spring Data JPA repositories
├── routes/         # APIRoutes constants
├── seeder/         # Database seeders
├── service/        # Business logic
├── support/        # Constants, utils, helpers
├── swagger/        # Swagger examples & grouping
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
| Nhật ký hệ thống (`audit-logs`) | view | — | — |
| Quản lý metadata (`metadata-management`) | CRUD | — | — |
| Thống kê (`dashboard`) | view | view | — |
| Log AI/OCR (`ai-logs`) | view | — | — |

> Chi tiết phân quyền: [docs/4_PERMISSION_DESIGN.md](docs/4_PERMISSION_DESIGN.md)

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

> Tài liệu đầy đủ: [docs/](docs/) | Swagger UI: `http://localhost:8080/swagger-ui.html`

## Tài Khoản Mặc Định (Dev)

| Email | Password | Role |
|-------|----------|------|
| `hoa1312004@gmail.com` | `Aa123456` | admin |

## Tài Liệu Dự Án

| File | Nội dung |
|------|---------|
| [docs/1_BRD.md](docs/1_BRD.md) | Business Requirements |
| [docs/2_PRD.md](docs/2_PRD.md) | Product Requirements |
| [docs/3_MVP.md](docs/3_MVP.md) | MVP scope |
| [docs/4_PERMISSION_DESIGN.md](docs/4_PERMISSION_DESIGN.md) | Thiết kế phân quyền chi tiết |
| [docs/8_DATABASE_SCHEMA.md](docs/8_DATABASE_SCHEMA.md) | Schema database đầy đủ |
| [docs/9_AUTH_FLOW.md](docs/9_AUTH_FLOW.md) | Flow xác thực & JWT |
| [docs/PERMISSION_SYSTEM_GUIDE.md](docs/PERMISSION_SYSTEM_GUIDE.md) | Hướng dẫn hệ thống RBAC |
