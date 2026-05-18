# Implementation Plan — MED-OCR Backend
**Cập nhật lần cuối:** 2026-05-12  
**DB:** PostgreSQL 15+ | **Framework:** Spring Boot 3.x + Java 17

> Đây là file plan / checklist chính. Mỗi lần bắt đầu session mới, đọc file này trước để biết đã làm đến đâu.  
> Tài liệu tham chiếu: [docs/4_PERMISSION_DESIGN.md](4_PERMISSION_DESIGN.md) | [docs/8_DATABASE_SCHEMA.md](8_DATABASE_SCHEMA.md)

---

## Ký Hiệu
- ✅ Hoàn thành
- 🔧 Cần sửa (đã có nhưng sai / thiếu)
- ⬜ Chưa làm

---

## Phase 1 — Authentication & Account (✅ Hoàn thành)

| # | Task | File | Ghi chú |
|---|------|------|---------|
| ✅ | Entity `Account` | `entity/Account.java` | Table `account`, có gender ENUM, soft-delete |
| ✅ | Entity `Role` | `entity/Role.java` | Table `role`, có `is_super_admin` |
| ✅ | Entity `RfAccountRole` | `entity/RfAccountRole.java` | Table `tbl_rf_account_role` |
| ✅ | Entity `ManagerTokenAccount` | `entity/ManagerTokenAccount.java` | Table `tbl_manager_token_account`, typo `is_acitve` giữ nguyên |
| ✅ | JWT Service | `jwt/JwtService.java` | Access token 1h, refresh token 7 ngày |
| ✅ | JWT Filter | `jwt/JwtAuthenticationFilter.java` | Parse JWT từ header |
| ✅ | Auth Controller | `controller/AuthController.java` | login, logout, refresh-token |
| ✅ | Account Controller | `controller/AccountController.java` | register, validate-token, profile, list, create, get/{id}, update, delete |
| ✅ | Account Service | `service/AccountService.java` | CRUD + email verify |
| ✅ | Account Seeder | `seeder/AccountSeeder.java` | Tạo admin mặc định `hoa1312004@gmail.com` / `Aa123456` |
| ✅ | PermissionAction Seeder | `seeder/PermissionActionSeeder.java` | 6 actions: view, create, edit, delete, cancel, rollback — order=3 |
| ✅ | Swagger / OpenAPI | `config/OpenApiConfig.java` | UI tại `/documents/swagger-ui` |
| ✅ | i18n messages | `i18n/MessageTranslator.java` | `lang/messages_vi.properties` |
| ✅ | Exception Handler | `exception/GlobalExceptionHandler.java` | |
| ✅ | SecurityConfig | `config/SecurityConfig.java` | `@EnableMethodSecurity` bắt buộc |

---

## Phase 2 — Permission System (✅ Hoàn thành)

### 2A — Constants & Seeders

| # | Task | File | Ghi chú |
|---|------|------|---------|
| ✅ | Sửa `RoleConstant` | `support/RoleConstant.java` | ROLE_ADMIN, ROLE_DOCTOR, ROLE_EMPLOYEE |
| ✅ | Sửa `RoleSeeder` | `seeder/RoleSeeder.java` | 3 roles: Quản Trị Viên / Bác Sĩ / Nhân Viên |
| ✅ | Sửa `AccountSeeder` | `seeder/AccountSeeder.java` | assignRole dùng ROLE_ADMIN |
| ✅ | Tạo `AccountConstant` | `support/AccountConstant.java` | ACCOUNT_MANAGEMENT = "accounts" |
| ✅ | Tạo `MedicalRecordConstant` | `support/MedicalRecordConstant.java` | MEDICAL_RECORD, MEDICAL_RECORD_APPROVAL, PATIENT_SEARCH |
| ✅ | Tạo `AdminConstant` | `support/AdminConstant.java` | AUDIT_LOG, METADATA_MANAGEMENT, DASHBOARD, AI_LOG |
| ✅ | Tạo `PermissionSeeder` | `seeder/PermissionSeeder.java` | order=4, seed 8 permissions |
| ✅ | Tạo `PermissionRoleSeeder` | `seeder/PermissionRoleSeeder.java` | order=5, gán permission+action cho 3 role theo matrix |
| ✅ | Thêm `findBySlug` | `repository/PermissionRepository.java` | Dùng bởi PermissionSeeder & PermissionRoleSeeder |
| ✅ | Thêm `existsByPermissionIdAndPermissionActionIdAndRoleId` | `repository/PermissionRoleRepository.java` | Idempotent check trong PermissionRoleSeeder |

**8 Permissions cần seed (PermissionSeeder):**
```
accounts               | Quản lý Tài khoản     | sort=1
medical-records        | Quản lý Bệnh án       | sort=2
medical-records-approval| Phê duyệt Bệnh án    | sort=3
patient-search         | Tra cứu Bệnh nhân     | sort=4
audit-logs             | Nhật ký Hệ thống      | sort=5
metadata-management    | Quản lý Metadata      | sort=6
dashboard              | Thống kê              | sort=7
ai-logs                | Log AI / OCR          | sort=8
```

### 2B — Security Loading

| # | Task | File | Ghi chú |
|---|------|------|---------|
| ✅ | Cập nhật `AccountUserDetails` | `auth/AccountUserDetails.java` | `of(account, permissionStrings)` — load cả `ROLE_` và `slug:action` |
| ✅ | Cập nhật `AccountUserDetailsService` | `auth/AccountUserDetailsService.java` | Inject `PermissionRoleRepository`, query permissions trước khi build UserDetails |
| ✅ | Thêm query `findPermissionStringsByAccountId` | `repository/PermissionRoleRepository.java` | 1 JPQL query JOIN permission + permissionAction qua roleId subquery |

### 2C — Controller @PreAuthorize

| # | Task | File | Ghi chú |
|---|------|------|---------|
| ✅ | Cập nhật `AccountController` | `controller/AccountController.java` | list/detail→`accounts:view`, create→`accounts:create`, update→`accounts:edit`, delete→`accounts:delete`; profile endpoints giữ `isAuthenticated()` |

---

## Phase 3 — Medical Records Domain (🔧 Đang làm)

### 3A — Entities (✅ Hoàn thành)

| # | Task | File | Ghi chú |
|---|------|------|---------|
| ✅ | Entity `Patient` | `entity/Patient.java` | Table `patients` — thông tin bệnh nhân, `bhyt` UNIQUE, key để search |
| ✅ | Entity `MedicalRecord` | `entity/MedicalRecord.java` | Table `medical_records` — FK `patient_id`, thêm `department` + `record_type` VARCHAR |
| ✅ | Enum `MedicalRecordStatus` | `entity/MedicalRecordStatus.java` | PROCESSING/EXTRACTED/PENDING_DOCTOR_REVIEW/APPROVED/REJECTED — custom converter |
| ✅ | Converter `MedicalRecordStatusConverter` | `entity/MedicalRecordStatusConverter.java` | Map enum → VARCHAR "Pending Doctor Review" (có space) |
| ✅ | Entity `OcrRegion` | `entity/OcrRegion.java` | Table `ocr_regions` — raw AI output, `bounding_boxes` lưu dạng text (JSON string) |
| ✅ | Entity `ExtractedField` | `entity/ExtractedField.java` | Table `extracted_fields` — key:value parse từ 4 region (không gồm test_table) |
| ✅ | Entity `LabResult` | `entity/LabResult.java` | Table `lab_results` — từng dòng xét nghiệm từ test_table region |
| ~~Metadata~~ | ~~Đã bỏ~~ | — | Không có FK liên kết thực sự; `department` + `record_type` đưa thẳng vào `medical_records` |
| ~~AuditLog~~ | ~~Đã bỏ~~ | — | Không cần thiết |

**MedicalRecord status ENUM:**
```java
public enum MedicalRecordStatus {
    PROCESSING, EXTRACTED, PENDING_DOCTOR_REVIEW, APPROVED, REJECTED
}
// DB lưu dạng VARCHAR: 'Processing', 'Extracted', 'Pending Doctor Review', 'Approved', 'Rejected'
```

**Flow khi AI trả về response:**
```
AI response JSON
    └─► Lưu 5 OcrRegion (mỗi region_type 1 row)
    └─► Parse patient_info, diagnosis_block, hospital_header, footer_signature
            └─► Lưu N ExtractedField (field_name: field_value)
            └─► Cập nhật medical_records.patient_name, patient_bhyt
    └─► Parse test_table (pipe-separated rows)
            └─► Lưu N LabResult (mỗi dòng xét nghiệm 1 row)
    └─► Cập nhật medical_records.status = 'Extracted'
```

### 3B — Repositories & Services

| # | Task | File | Ghi chú |
|---|------|------|---------|
| ⬜ | `MedicalRecordRepository` | `repository/` | Query theo uploaded_by (employee filter) |
| ⬜ | `MedicalRecordService` | `service/` | CRUD + business rules: edit status check theo role |
| ⬜ | `AuditLogService` | `service/` | Log mọi action CRUD — chỉ INSERT |
| ⬜ | `MetadataService` | `service/` | CRUD metadata |

### 3C — Controllers

| # | Task | File | Ghi chú |
|---|------|------|---------|
| ⬜ | `MedicalRecordController` | `controller/` | CRUD + upload endpoint |
| ⬜ | `ApprovalController` | `controller/` | approve / reject với status check |
| ⬜ | `PatientController` | `controller/` | search endpoint |
| ⬜ | `AuditLogController` | `controller/` | view-only |
| ⬜ | `MetadataController` | `controller/` | CRUD |
| ⬜ | `DashboardController` | `controller/` | Statistics endpoint |
| ⬜ | `AiLogController` | `controller/` | OCR log view |

---

## Phase 4 — AI Integration (⬜ Chưa làm)

| # | Task | Ghi chú |
|---|------|---------|
| ⬜ | HTTP client gọi Python AI Service | RestTemplate hoặc WebClient |
| ⬜ | Upload file (JPG/PNG/PDF) → gửi Python | 5MB limit đã config trong application.yaml |
| ⬜ | Nhận kết quả OCR → lưu `extracted_fields` | confidence_score mỗi field |
| ⬜ | Update `medical_records.status` = `Extracted` sau OCR | |
| ⬜ | `AiLog` entity + service | Lưu log xử lý AI |

---

## Phase 5 — Housekeeping (⬜ Chưa làm)

| # | Task | Ghi chú |
|---|------|---------|
| ⬜ | Cập nhật `docs/9_AUTH_FLOW.md` | Còn references "staff", đổi thành admin/doctor/employee |
| ⬜ | Flyway migration scripts | Thay thế ddl-auto=update cho production |
| ⬜ | Integration tests | Auth flow + permission check |

---

## Seeder Order Cuối Cùng

```
order=1  RoleSeeder              → 3 roles: admin, doctor, employee
order=2  AccountSeeder           → 1 admin default
order=3  PermissionActionSeeder  → 6 actions (✅ đã có)
order=4  PermissionSeeder        → 8 permissions (⬜ cần tạo)
order=5  RfPermissionActionSeeder→ gán action available (⬜ cần tạo)
order=6  PermissionRoleSeeder    → gán permission+action per role (⬜ cần tạo)
```

---

## Quick Reference — Cấu Hình Hiện Tại

```yaml
DB: PostgreSQL @ localhost:5432/medical (user: postgres)
Port: 8080
Swagger: http://localhost:8080/documents/swagger-ui
Upload dir: uploads/photos
JWT: access=1h, refresh=7 days
ddl-auto: update (dev only)
```

**Default credentials (dev):** `hoa1312004@gmail.com` / `Aa123456`
