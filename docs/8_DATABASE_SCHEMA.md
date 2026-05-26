# TÀI LIỆU LƯỢC ĐỒ CƠ SỞ DỮ LIỆU (DATABASE SCHEMA)
## DỰ ÁN: MED-OCR — HỆ THỐNG TRÍCH XUẤT THÔNG TIN BỆNH ÁN

> **Lưu ý:** Tên bảng và cột lấy trực tiếp từ JPA entity (`@Table`, `@Column`) trong source code.  
> **Database:** PostgreSQL 15+ | **ddl-auto:** update (dev)

---

## 1. Tổng Quan Các Bảng

| Bảng | Entity | Mô tả |
|------|--------|-------|
| `account` | `Account.java` | Tài khoản người dùng |
| `role` | `Role.java` | Vai trò: admin, doctor, employee |
| `tbl_rf_account_role` | `RfAccountRole.java` | Liên kết Account ↔ Role (N:M) |
| `permission` | `Permission.java` | Module quyền hạn (8 module) |
| `permission_action` | `PermissionAction.java` | Hành động: view/create/edit/delete/cancel/rollback |
| `permission_role` | `PermissionRole.java` | Gán Permission + Action cho Role |
| `tbl_manager_token_account` | `ManagerTokenAccount.java` | Lưu JWT token (hỗ trợ logout) |
| `patients` | `Patient.java` | Thông tin bệnh nhân — `bhyt` UNIQUE, key tìm kiếm |
| `medical_records` | `MedicalRecord.java` | Bệnh án — FK `patient_id`, workflow status |
| `ocr_regions` | `OcrRegion.java` | Raw AI output theo từng vùng (5 loại) |
| `extracted_fields` | `ExtractedField.java` | Key-value parse từ OCR, employee verify |
| `lab_results` | `LabResult.java` | Từng dòng kết quả xét nghiệm |

---

## 2. ERD

```
┌─────────────────────────────────────────────────────────┐
│  PHÂN QUYỀN (đã implement)                              │
│                                                         │
│  account ──(N:M)── tbl_rf_account_role ──── role        │
│                                              │          │
│                         permission_role ─────┘          │
│                               │                         │
│                    permission + permission_action        │
│                                                         │
│  tbl_manager_token_account ── account  (JWT revoke)     │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  NGHIỆP VỤ                                              │
│                                                         │
│  patients (bhyt UNIQUE ← key search)                    │
│      └──(1:N)──► medical_records                        │
│                       │                                 │
│  account (uploaded_by)─┤                                │
│  account (approved_by)─┘                                │
│                       │                                 │
│              ┌────────┘                                 │
│              ├──(1:N)──► ocr_regions  (raw AI output)   │
│              │                │                         │
│              │                ├──► extracted_fields      │
│              │                └──► lab_results           │
└─────────────────────────────────────────────────────────┘

Search flow:
  BHYT ──► patients ──► list medical_records (mới nhất trước)
                                 └──► click ──► chi tiết record
```

---

## 3. Chi Tiết Từng Bảng

### 3.1 `account` — Tài khoản người dùng

```sql
CREATE TABLE account (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name            VARCHAR(255),
    birthday        DATE,
    phone_number    VARCHAR(12),
    email           VARCHAR(255) NOT NULL UNIQUE,
    username        VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,       -- BCrypt
    is_active       BOOLEAN,
    is_delete       BOOLEAN DEFAULT FALSE,        -- soft delete
    email_verify_at TIMESTAMP,                   -- NULL = chưa kích hoạt
    created_by      BIGINT,                       -- admin tạo (FK lỏng)
    gender          VARCHAR(10) DEFAULT 'other',  -- male/female/other
    photo_url       VARCHAR(500),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP
);
```

---

### 3.2 `role` — Vai trò

```sql
CREATE TABLE role (
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name           VARCHAR(255),       -- "Quản Trị Viên", "Bác Sĩ", "Nhân Viên"
    code           VARCHAR(255) UNIQUE, -- "admin", "doctor", "employee"
    is_active      BOOLEAN,
    is_super_admin BOOLEAN DEFAULT FALSE,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);
```

---

### 3.3 `tbl_rf_account_role` — Account ↔ Role

```sql
CREATE TABLE tbl_rf_account_role (
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id BIGINT NOT NULL REFERENCES account(id),
    role_id    BIGINT NOT NULL REFERENCES role(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (account_id, role_id)
);
```

---

### 3.4 `permission` — Module quyền hạn

```sql
CREATE TABLE permission (
    id        BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name      VARCHAR(255),
    slug      VARCHAR(255) UNIQUE,  -- "accounts", "medical-records", ...
    sort      VARCHAR(50),
    is_hidden BOOLEAN DEFAULT FALSE
);
```

**8 permissions:**

| slug | Tên | sort |
|------|-----|------|
| `accounts` | Quản lý Tài khoản | 1 |
| `medical-records` | Quản lý Bệnh án | 2 |
| `medical-records-approval` | Phê duyệt Bệnh án | 3 |
| `patient-search` | Tra cứu Bệnh nhân | 4 |
| `audit-logs` | Nhật ký Hệ thống | 5 |
| `metadata-management` | Quản lý Metadata | 6 |
| `dashboard` | Thống kê | 7 |
| `ai-logs` | Log AI / OCR | 8 |

---

### 3.5 `permission_action` — Hành động

```sql
CREATE TABLE permission_action (
    id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code VARCHAR(50) UNIQUE  -- view/create/edit/delete/cancel/rollback
);
```

---

### 3.6 `permission_role` — Gán Permission + Action cho Role

```sql
CREATE TABLE permission_role (
    id                   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    permission_id        BIGINT NOT NULL REFERENCES permission(id),
    permission_action_id BIGINT NOT NULL REFERENCES permission_action(id),
    role_id              BIGINT NOT NULL REFERENCES role(id),
    UNIQUE (permission_id, permission_action_id, role_id)
);
```

**Permission Matrix:**

| Module | Admin | Doctor | Employee |
|--------|-------|--------|----------|
| `accounts` | view/create/edit/delete | — | — |
| `medical-records` | view/create/edit/delete | view/edit | view/create/edit |
| `medical-records-approval` | view/create/cancel | view/create/cancel | — |
| `patient-search` | view | view | view |
| `audit-logs` | view | — | — |
| `metadata-management` | view/create/edit/delete | — | — |
| `dashboard` | view | view | — |
| `ai-logs` | view | — | — |

---

### 3.7 `tbl_manager_token_account` — JWT Token Store

```sql
CREATE TABLE tbl_manager_token_account (
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    token      TEXT NOT NULL,
    is_acitve  BOOLEAN DEFAULT TRUE,  -- typo giữ nguyên để match entity
    expired_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    account_id BIGINT NOT NULL REFERENCES account(id)
);
```

> Mục đích: JWT stateless không logout được. Lưu token → logout set `is_acitve = false` → JwtFilter từ chối.

---

### 3.8 `patients` — Bệnh nhân

```sql
CREATE TABLE patients (
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    bhyt       VARCHAR(30) NOT NULL UNIQUE,  -- Số thẻ BHYT, key tìm kiếm
    name       VARCHAR(200) NOT NULL,
    dob        DATE,
    gender     VARCHAR(10),                  -- male/female/other
    address    TEXT,
    phone      VARCHAR(20),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);
```

**Quy tắc:**
- `bhyt` là key duy nhất để identify bệnh nhân — tìm kiếm và link với bệnh án
- Khi AI extract xong `patient_info` region → `findOrCreate` patient theo bhyt → gán `patient_id` vào `medical_records`

---

### 3.9 `medical_records` — Bệnh án

```sql
CREATE TABLE medical_records (
    id                  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    record_number       VARCHAR(50) NOT NULL UNIQUE,  -- VD: "REC-2026-001234"
    uploaded_by         BIGINT NOT NULL REFERENCES account(id),
    file_name           VARCHAR(255),
    file_type           VARCHAR(10),       -- jpg / png / pdf
    original_image_path VARCHAR(500),

    patient_id          BIGINT REFERENCES patients(id),
    department          VARCHAR(100),      -- "Nội Khoa", "Ngoại Khoa" (từ OCR)
    record_type         VARCHAR(50),       -- "Nội trú", "Ngoại trú"

    status              VARCHAR(30) DEFAULT 'Processing',
    notes               TEXT,
    rejection_reason    TEXT,
    approved_by         BIGINT REFERENCES account(id),
    approved_at         TIMESTAMP,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP
);
```

**Trạng thái (MedicalRecordStatus enum → lưu VARCHAR):**

| Enum | DB value | Mô tả |
|------|----------|-------|
| `PROCESSING` | `Processing` | AI đang xử lý |
| `EXTRACTED` | `Extracted` | AI xong, employee verify OCR |
| `PENDING_DOCTOR_REVIEW` | `Pending Doctor Review` | Chờ bác sĩ duyệt |
| `APPROVED` | `Approved` | Bác sĩ duyệt |
| `REJECTED` | `Rejected` | Bác sĩ từ chối |

**Business rules:**

| Role | Xem | Edit | Điều kiện edit |
|------|-----|------|----------------|
| Employee | Chỉ record của mình | ✅ | status IN (Processing, Extracted, Rejected) |
| Doctor | Tất cả | ✅ | status = Pending Doctor Review |
| Admin | Tất cả | ✅ | Không giới hạn |

---

### 3.10 `ocr_regions` — Raw AI Output

Mỗi bệnh án có **5 region**, mỗi region = 1 row.

```sql
CREATE TABLE ocr_regions (
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    record_id      BIGINT NOT NULL REFERENCES medical_records(id) ON DELETE CASCADE,
    region_type    VARCHAR(50) NOT NULL,
    count          INT DEFAULT 1,          -- số vùng detect được
    confidence_avg DECIMAL(8,6),           -- 0.000000 – 1.000000
    raw_text       TEXT,                   -- text thô từ Tesseract
    bounding_boxes TEXT,                   -- JSON string: [[x1,y1,x2,y2], ...]
    created_at     TIMESTAMP DEFAULT NOW()
);
```

**5 region_type:**

| region_type | raw_text chứa |
|-------------|---------------|
| `hospital_header` | Tên BV, địa chỉ, SĐT |
| `patient_info` | Họ tên, ngày sinh, giới tính, địa chỉ, BHYT |
| `diagnosis_block` | Chẩn đoán, khoa, BS chỉ định, thời gian lấy mẫu... |
| `test_table` | Bảng xét nghiệm: `NEU# \| 4.6 \| K/uL \| (2 - 7.5)` |
| `footer_signature` | Chức danh, họ tên người ký |

---

### 3.11 `extracted_fields` — Dữ Liệu Parse Từ OCR

Parse `raw_text` của 4 region (không gồm `test_table`) thành từng cặp `field_name: field_value`.

```sql
CREATE TABLE extracted_fields (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    record_id   BIGINT NOT NULL REFERENCES medical_records(id) ON DELETE CASCADE,
    region_id   BIGINT REFERENCES ocr_regions(id) ON DELETE SET NULL,
    field_name  VARCHAR(100) NOT NULL,
    field_value TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by BIGINT REFERENCES account(id),
    verified_at TIMESTAMP,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP
);
```

**field_name theo region:**

| Region | field_name |
|--------|-----------|
| `hospital_header` | `hospital_name`, `hospital_address`, `hospital_phone` |
| `patient_info` | `patient_name`, `patient_dob`, `patient_gender`, `patient_address`, `patient_bhyt` |
| `diagnosis_block` | `diagnosis`, `department`, `prescribing_doctor`, `sample_collected_at`, `specimen_type`, ... |
| `footer_signature` | `signer_title`, `signer_name` |

---

### 3.12 `lab_results` — Kết Quả Xét Nghiệm

Parse `test_table` region — mỗi dòng xét nghiệm = 1 row.

```sql
CREATE TABLE lab_results (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    record_id       BIGINT NOT NULL REFERENCES medical_records(id) ON DELETE CASCADE,
    region_id       BIGINT REFERENCES ocr_regions(id) ON DELETE SET NULL,
    test_name       VARCHAR(100) NOT NULL,  -- NEU#, EOS#, MCV, ...
    test_value      VARCHAR(50),            -- "4.6", "0.4"
    unit            VARCHAR(30),            -- K/uL, fL, pg, g/dL, %
    reference_range VARCHAR(50),            -- "(2 - 7.5)", "(85 - 95)"
    is_abnormal     BOOLEAN,
    is_verified     BOOLEAN DEFAULT FALSE,
    verified_by     BIGINT REFERENCES account(id),
    verified_at     TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP
);
```

---

## 4. Workflow Bệnh Án

```
Employee upload file
        │
        ▼
  [Processing]  ← tạo medical_record, gửi lên AI
        │
        ▼  (AI trả response)
  [Extracted]   ← lưu ocr_regions + extracted_fields + lab_results
        │          findOrCreate patient theo bhyt → gán patient_id
        │
  Employee xem, chỉnh sửa extracted_fields / lab_results
        │
        ▼  (employee submit)
  [Pending Doctor Review]
        │
    ┌───┴───┐
    ▼       ▼
[Approved] [Rejected] ← employee sửa lại → submit lại
```

---

## 5. API Endpoints

| Method | URL | Permission | Mô tả |
|--------|-----|-----------|-------|
| GET | `/api/v1/patient/search?bhyt=` | `patient-search:view` | Tìm bệnh nhân + list records |
| GET | `/api/v1/patient/list` | `patient-search:view` | Danh sách bệnh nhân (phân trang) |
| GET | `/api/v1/patient/{id}` | `patient-search:view` | Chi tiết bệnh nhân |
| POST | `/api/v1/patient/create` | `medical-records:create` | Tạo bệnh nhân |
| PUT | `/api/v1/patient/update/{id}` | `medical-records:edit` | Cập nhật bệnh nhân |
| POST | `/api/v1/medical-record/create` | `medical-records:create` | Tạo bệnh án |
| GET | `/api/v1/medical-record/list` | `medical-records:view` | Danh sách bệnh án |
| GET | `/api/v1/medical-record/{id}` | `medical-records:view` | Chi tiết bệnh án |
| PUT | `/api/v1/medical-record/update` | `medical-records:edit` | Cập nhật bệnh án |
| PUT | `/api/v1/medical-record/field/update` | `medical-records:edit` | Sửa extracted field |
| PUT | `/api/v1/medical-record/{id}/submit` | `medical-records:edit` | Submit để bác sĩ duyệt |
| PUT | `/api/v1/medical-record/{id}/approve` | `medical-records-approval:create` | Duyệt bệnh án |
| PUT | `/api/v1/medical-record/reject` | `medical-records-approval:cancel` | Từ chối bệnh án |
| DELETE | `/api/v1/medical-record/{id}` | `medical-records:delete` | Xóa bệnh án (admin) |

---

## 6. Cấu Hình

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medical
    username: postgres
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

app:
  upload-dir: uploads/photos
  server:
    url: http://localhost:8080

springdoc:
  swagger-ui:
    path: /documents/swagger-ui
```

**Default credentials (dev):** `hoa1312004@gmail.com` / `Aa123456`

---

## 7. Seeder Order

| Order | Seeder | Tạo ra |
|-------|--------|--------|
| 1 | `RoleSeeder` | 3 roles: admin, doctor, employee |
| 2 | `AccountSeeder` | 1 admin mặc định |
| 3 | `PermissionActionSeeder` | 6 actions: view/create/edit/delete/cancel/rollback |
| 4 | `PermissionSeeder` | 8 permissions |
| 5 | `PermissionRoleSeeder` | Gán permission+action cho 3 role |

---

## 8. Lưu Ý Bảo Mật

- Mật khẩu hash **BCrypt** (cost = 10)
- JWT secret ≥ 256 bit cho HS256; access token 1h, refresh 7 ngày
- `is_delete = TRUE` thay vì xóa vật lý (soft delete cho `account`)
- `tbl_manager_token_account` cho phép revoke token khi logout
- `ON DELETE CASCADE` trên `ocr_regions`, `extracted_fields`, `lab_results` → xóa bệnh án thì xóa luôn data con

---

**Version:** 3.0  
**Ngày cập nhật:** 2026-05-19  
**Database:** PostgreSQL 15+
