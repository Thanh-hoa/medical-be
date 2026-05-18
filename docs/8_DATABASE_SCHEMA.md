# TÀI LIỆU LƯỢC ĐỒ CƠ SỞ DỮ LIỆU (DATABASE SCHEMA)
## DỰ ÁN: MED-OCR — HỆ THỐNG TRÍCH XUẤT THÔNG TIN BỆNH ÁN

> **Lưu ý:** Tên bảng và cột được lấy trực tiếp từ JPA entity (`@Table`, `@Column`) trong source code.

---

## 1. Tổng Quan Các Bảng

| Bảng | Entity class | Mô tả |
|------|-------------|-------|
| `account` | `Account.java` | Tài khoản người dùng |
| `role` | `Role.java` | Vai trò: admin, doctor, employee |
| `tbl_rf_account_role` | `RfAccountRole.java` | Account ↔ Role (N:M) |
| `permission` | `Permission.java` | Module quyền hạn (8 module) |
| `permission_action` | `PermissionAction.java` | Hành động: view/create/edit/delete/cancel/rollback |
| `permission_role` | `PermissionRole.java` | Role ↔ Permission + Action |
| `tbl_manager_token_account` | `ManagerTokenAccount.java` | Quản lý JWT token (hỗ trợ logout) |
| `patients` | `Patient.java` | Thông tin bệnh nhân — `bhyt` UNIQUE, key tìm kiếm |
| `medical_records` | `MedicalRecord.java` | Bệnh án — FK `patient_id`, workflow status |
| `ocr_regions` | `OcrRegion.java` | Raw output từ AI theo từng region (5 loại) |
| `extracted_fields` | `ExtractedField.java` | Key-value đã parse từ region (employee verify) |
| `lab_results` | `LabResult.java` | Kết quả xét nghiệm từ test_table region |
| `metadata` | `Metadata.java` | ICD-10, loại bệnh án, danh sách khoa phòng |

---

## 2. ERD (Entity Relationship Diagram)

```
┌──────────────────────────────────────────────────────────────────────┐
│  PHÂN QUYỀN (đã implement)                                           │
│                                                                      │
│  account ──(N:M)── tbl_rf_account_role ──(N:M)── role               │
│                                                     │                │
│                              permission_role ───────┘                │
│                                    │                                 │
│                              permission + permission_action          │
│                                                                      │
│  tbl_manager_token_account ─── account  (JWT token store)           │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│  NGHIỆP VỤ                                                           │
│                                                                      │
│  patients ──(bhyt: key search)                                       │
│      │                                                               │
│      └──(1:N via patient_id)──► medical_records                      │
│                                      │                               │
│  account ──(uploaded_by)────────────►│                               │
│  account ──(approved_by)────────────►│                               │
│                                      │                               │
│                    ┌─────────────────┘                               │
│                    │                                                 │
│                    ├──(1:N)──► ocr_regions   (raw AI output)        │
│                    │               │                                 │
│                    │               ├──(1:N)──► extracted_fields      │
│                    │               └──(1:N)──► lab_results           │
│                    │                                                 │
│  metadata  (độc lập — ICD-10, loại bệnh án, khoa phòng)             │
└──────────────────────────────────────────────────────────────────────┘

Search flow:
  BHYT ──► patients ──► list medical_records (ORDER BY created_at DESC)
                                  └──► click vào ──► chi tiết record
```

---

## 3. Chi Tiết Từng Bảng

### 3.1 `account` — Tài khoản người dùng

```sql
CREATE TABLE account (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255),
    birthday        DATE,
    phone_number    VARCHAR(12),
    email           VARCHAR(255) NOT NULL UNIQUE,
    username        VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,               -- BCrypt
    is_active       BOOLEAN,
    is_delete       BOOLEAN DEFAULT FALSE,               -- soft delete
    email_verify_at DATETIME,                            -- NULL = chưa kích hoạt
    created_by      BIGINT,                              -- ID admin tạo, NULL nếu tự đăng ký
    gender          ENUM('male','female','other') DEFAULT 'other',
    photo_url       VARCHAR(500),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME
);
```

| Cột | Kiểu | Ghi chú |
|-----|------|---------|
| `email_verify_at` | DATETIME | NULL = chưa verify email → `isActive = false` |
| `is_delete` | BOOLEAN | Soft delete — không xóa vật lý |
| `created_by` | BIGINT | FK lỏng đến `account.id` (admin tạo) |
| `gender` | ENUM | `male`, `female`, `other` |

---

### 3.2 `role` — Vai trò

```sql
CREATE TABLE role (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(255),            -- "Quản Trị Viên", "Bác Sĩ", "Nhân Viên"
    code         VARCHAR(255) UNIQUE,     -- "admin", "doctor", "employee"
    is_active    BOOLEAN,
    is_super_admin BOOLEAN DEFAULT FALSE, -- TRUE chỉ cho admin
    created_at   DATETIME,
    updated_at   DATETIME
);

-- Dữ liệu seed
INSERT INTO role (name, code, is_active, is_super_admin) VALUES
    ('Quản Trị Viên', 'admin',    TRUE, TRUE),
    ('Bác Sĩ',        'doctor',   TRUE, FALSE),
    ('Nhân Viên',     'employee', TRUE, FALSE);
```

---

### 3.3 `tbl_rf_account_role` — Liên kết Account ↔ Role

```sql
CREATE TABLE tbl_rf_account_role (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,

    FOREIGN KEY (account_id) REFERENCES account(id),
    FOREIGN KEY (role_id)    REFERENCES role(id),
    UNIQUE KEY uq_account_role (account_id, role_id)
);
```

---

### 3.4 `permission` — Module quyền hạn

```sql
CREATE TABLE permission (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(255),        -- "Quản lý Tài khoản", "Quản lý Bệnh án", ...
    slug      VARCHAR(255) UNIQUE, -- "accounts", "medical-records", ...
    sort      VARCHAR(50),         -- thứ tự hiển thị menu
    is_hidden BOOLEAN DEFAULT FALSE
);

-- 8 permissions của dự án (theo docs/4_PERMISSION_DESIGN.md)
INSERT INTO permission (name, slug, sort, is_hidden) VALUES
    ('Quản lý Tài khoản',    'accounts',                  '1',  FALSE),
    ('Quản lý Bệnh án',      'medical-records',           '2',  FALSE),
    ('Phê duyệt Bệnh án',    'medical-records-approval',  '3',  FALSE),
    ('Tra cứu Bệnh nhân',    'patient-search',            '4',  FALSE),
    ('Nhật ký Hệ thống',     'audit-logs',                '5',  FALSE),
    ('Quản lý Metadata',     'metadata-management',       '6',  FALSE),
    ('Thống kê',             'dashboard',                 '7',  FALSE),
    ('Log AI / OCR',         'ai-logs',                   '8',  FALSE);
```

---

### 3.5 `permission_action` — Hành động

```sql
CREATE TABLE permission_action (
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE  -- "view","create","edit","delete","cancel","rollback"
);

-- 6 actions chuẩn
INSERT INTO permission_action (code) VALUES
    ('view'), ('create'), ('edit'), ('delete'), ('cancel'), ('rollback');
```

---

### 3.6 `permission_role` — Gán Permission + Action cho Role

```sql
CREATE TABLE permission_role (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_id        BIGINT NOT NULL,
    permission_action_id BIGINT NOT NULL,
    role_id              BIGINT NOT NULL,

    FOREIGN KEY (permission_id)        REFERENCES permission(id),
    FOREIGN KEY (permission_action_id) REFERENCES permission_action(id),
    FOREIGN KEY (role_id)              REFERENCES role(id),
    UNIQUE KEY uq_perm_action_role (permission_id, permission_action_id, role_id)
);
```

**Dữ liệu (theo Permission Matrix — `docs/4_PERMISSION_DESIGN.md` mục 4):**

```
-- ADMIN: toàn bộ actions trên tất cả module
accounts              → view, create, edit, delete
medical-records       → view, create, edit, delete
medical-records-approval → view, create, cancel
patient-search        → view
audit-logs            → view
metadata-management   → view, create, edit, delete
dashboard             → view
ai-logs               → view

-- DOCTOR
medical-records           → view, edit
medical-records-approval  → view, create, cancel
patient-search            → view
dashboard                 → view

-- EMPLOYEE
medical-records  → view, create, edit
patient-search   → view
```

---

### 3.7 `tbl_manager_token_account` — Quản lý JWT Token

```sql
CREATE TABLE tbl_manager_token_account (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    token      TEXT NOT NULL,
    is_acitve  BOOLEAN DEFAULT TRUE,   -- lưu ý: typo trong entity, giữ nguyên để match code
    expired_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    account_id BIGINT NOT NULL,

    FOREIGN KEY (account_id) REFERENCES account(id),
    INDEX idx_token (token(255)),
    INDEX idx_account_id (account_id)
);
```

**Mục đích:** JWT thuần là stateless, không thể logout. Lưu token vào bảng này → khi logout set `is_acitve = false` → filter kiểm tra → 401.

---

### 3.8 `medical_records` — Bệnh án *(cần tạo)*

```sql
CREATE TABLE medical_records (
    id                  BIGSERIAL PRIMARY KEY,
    record_number       VARCHAR(50) NOT NULL UNIQUE,    -- VD: "REC-2026-001"
    uploaded_by         BIGINT NOT NULL,                -- FK → account.id (employee upload)
    file_name           VARCHAR(255),                   -- tên file gốc upload
    file_type           VARCHAR(10),                    -- 'jpg', 'png', 'pdf'
    original_image_path VARCHAR(500),                   -- đường dẫn lưu trữ

    -- Denormalized từ OCR để search nhanh, không cần JOIN extracted_fields
    patient_name        VARCHAR(200),                   -- parse từ patient_info region
    patient_bhyt        VARCHAR(30),                    -- Số thẻ BHYT, key để search bệnh nhân

    -- Workflow
    status              VARCHAR(30) DEFAULT 'Processing',
    notes               TEXT,
    rejection_reason    TEXT,                           -- doctor điền khi reject
    approved_by         BIGINT,                         -- FK → account.id (doctor)
    approved_at         TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,

    FOREIGN KEY (uploaded_by) REFERENCES account(id),
    FOREIGN KEY (approved_by) REFERENCES account(id)
);
CREATE INDEX idx_medical_uploaded_by  ON medical_records(uploaded_by);
CREATE INDEX idx_medical_status       ON medical_records(status);
CREATE INDEX idx_medical_patient_name ON medical_records(patient_name);
CREATE INDEX idx_medical_patient_bhyt ON medical_records(patient_bhyt);
```

**Status values** (Java enum `MedicalRecordStatus` — Hibernate lưu dạng VARCHAR):

| Value | Mô tả |
|-------|-------|
| `Processing` | AI đang xử lý ảnh |
| `Extracted` | AI xong, employee xem & verify OCR |
| `Pending Doctor Review` | Employee submit, chờ bác sĩ duyệt |
| `Approved` | Bác sĩ duyệt thành công |
| `Rejected` | Bác sĩ từ chối, employee có thể sửa lại |

**Quy tắc nghiệp vụ (Service Layer):**

| Quy tắc | Điều kiện |
|---------|-----------|
| Employee chỉ `edit` khi | `status IN ('Processing', 'Extracted', 'Rejected')` |
| Doctor chỉ `edit` khi | `status = 'Pending Doctor Review'` |
| Admin `edit` mọi lúc | Không giới hạn |
| Employee chỉ thấy record của mình | `uploaded_by = currentUserId` |
| Doctor + Admin thấy tất cả | Không lọc `uploaded_by` |

---

### 3.9 `ocr_regions` — Raw OCR Output từ AI *(cần tạo)*

Lưu nguyên vẹn kết quả thô từ AI. Mỗi record có **5 region** tương ứng 5 vùng YOLO detect trong ảnh.

```sql
CREATE TABLE ocr_regions (
    id              BIGSERIAL PRIMARY KEY,
    record_id       BIGINT NOT NULL,                    -- FK → medical_records.id
    region_type     VARCHAR(50) NOT NULL,               -- xem bảng bên dưới
    count           INT DEFAULT 1,                      -- số vùng detect của type này
    confidence_avg  DECIMAL(8,6),                       -- confidence trung bình (0.000000 – 1.000000)
    raw_text        TEXT,                               -- text thô Tesseract trả về
    bounding_boxes  JSONB,                              -- [[x1,y1,x2,y2], ...] tọa độ pixel
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (record_id) REFERENCES medical_records(id) ON DELETE CASCADE
);
CREATE INDEX idx_ocr_record      ON ocr_regions(record_id);
CREATE INDEX idx_ocr_record_type ON ocr_regions(record_id, region_type);
```

**5 region_type từ AI model:**

| region_type | raw_text chứa gì |
|-------------|-----------------|
| `hospital_header` | Tên BV, địa chỉ, số điện thoại |
| `patient_info` | Họ tên, ngày sinh, giới tính, địa chỉ, số thẻ BHYT |
| `diagnosis_block` | Chẩn đoán, khoa/phòng, BS chỉ định, thời gian lấy mẫu... |
| `test_table` | Bảng xét nghiệm dạng `NEU# \| 4.6 \| K/uL \| (2 - 7.5)` |
| `footer_signature` | Chức danh và họ tên người ký |

---

### 3.10 `extracted_fields` — Dữ liệu Parse Từ OCR *(cần tạo)*

Sau khi có `raw_text`, ứng dụng parse thành từng cặp **key: value** để employee xem và chỉnh sửa.
> **Chỉ dùng cho 4 region**: `hospital_header`, `patient_info`, `diagnosis_block`, `footer_signature`.  
> `test_table` → lưu vào `lab_results` (bảng riêng, có cột unit/range).

```sql
CREATE TABLE extracted_fields (
    id          BIGSERIAL PRIMARY KEY,
    record_id   BIGINT NOT NULL,                        -- FK → medical_records.id
    region_id   BIGINT,                                 -- FK → ocr_regions.id
    field_name  VARCHAR(100) NOT NULL,                  -- xem bảng mapping bên dưới
    field_value TEXT,                                   -- giá trị OCR (chưa verify)
    is_verified BOOLEAN DEFAULT FALSE,                  -- employee xác nhận đúng
    verified_by BIGINT,                                 -- FK → account.id
    verified_at TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,

    FOREIGN KEY (record_id)   REFERENCES medical_records(id) ON DELETE CASCADE,
    FOREIGN KEY (region_id)   REFERENCES ocr_regions(id) ON DELETE SET NULL,
    FOREIGN KEY (verified_by) REFERENCES account(id)
);
CREATE INDEX idx_ef_record     ON extracted_fields(record_id);
CREATE INDEX idx_ef_field_name ON extracted_fields(record_id, field_name);
```

**Mapping field_name theo region:**

| Region | field_name được parse |
|--------|----------------------|
| `hospital_header` | `hospital_name`, `hospital_address`, `hospital_phone` |
| `patient_info` | `patient_name`, `patient_dob`, `patient_gender`, `patient_address`, `patient_bhyt` |
| `diagnosis_block` | `diagnosis`, `department`, `requesting_unit`, `prescribing_doctor`, `sample_collector`, `sample_collected_at`, `sample_receiver`, `sample_received_at`, `sample_status`, `specimen_type` |
| `footer_signature` | `signer_title`, `signer_name` |

---

### 3.11 `lab_results` — Kết Quả Xét Nghiệm *(cần tạo)*

Dành riêng cho `test_table` region. Mỗi dòng xét nghiệm trong bảng = 1 row.

```sql
CREATE TABLE lab_results (
    id              BIGSERIAL PRIMARY KEY,
    record_id       BIGINT NOT NULL,                    -- FK → medical_records.id
    region_id       BIGINT,                             -- FK → ocr_regions.id (test_table)
    test_name       VARCHAR(100) NOT NULL,              -- 'NEU#', 'EOS#', 'MCV', ...
    test_value      VARCHAR(50),                        -- '4.6', '0.4', '88.5', ...
    unit            VARCHAR(30),                        -- 'K/uL', 'fL', 'pg', 'g/dL', '%', ...
    reference_range VARCHAR(50),                        -- '(2 - 7.5)', '(85 - 95)', ...
    is_abnormal     BOOLEAN,                            -- TRUE nếu ngoài khoảng tham chiếu
    is_verified     BOOLEAN DEFAULT FALSE,
    verified_by     BIGINT,                             -- FK → account.id (employee)
    verified_at     TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,

    FOREIGN KEY (record_id)   REFERENCES medical_records(id) ON DELETE CASCADE,
    FOREIGN KEY (region_id)   REFERENCES ocr_regions(id) ON DELETE SET NULL,
    FOREIGN KEY (verified_by) REFERENCES account(id)
);
CREATE INDEX idx_lab_record ON lab_results(record_id);
```

**Ví dụ dữ liệu** (từ AI response thực tế):

| test_name | test_value | unit | reference_range | is_abnormal |
|-----------|-----------|------|----------------|-------------|
| NEU# | 4.6 | K/uL | (2 - 7.5) | false |
| EOS# | 0.4 | K/uL | (0 - 0.5) | false |
| MCV | 88.5 | fL | (85 - 95) | false |
| MCH | 32.1 | pg | (28 - 32) | false |
| MCHC | 32.3 | g/dL | (32 - 36) | false |
| NRBC% | 0.2 | % | (0 - 0.2) | false |
| NRBC# | 0.01 | K/uL | (0 - 0.01) | false |
| RDWs | 32.2 | fL | (20 - 42) | false |
| MPV | 8.7 | fL | (6 - 9) | false |

---

### 3.12 `audit_logs` — Nhật ký hệ thống *(cần tạo)*

```sql
CREATE TABLE audit_logs (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT,                   -- FK → account.id (ai thực hiện)
    action       VARCHAR(50) NOT NULL,     -- LOGIN, LOGOUT, UPLOAD, EDIT, APPROVE, REJECT, DELETE
    entity_type  VARCHAR(50),              -- USER, MEDICAL_RECORD, EXTRACTED_FIELD, METADATA
    entity_id    BIGINT,
    old_value    JSON,                     -- dữ liệu trước khi thay đổi
    new_value    JSON,                     -- dữ liệu sau khi thay đổi
    ip_address   VARCHAR(50),
    user_agent   VARCHAR(500),
    status       VARCHAR(20),              -- SUCCESS, FAILURE
    error_message TEXT,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES account(id),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    INDEX idx_entity (entity_type, entity_id)
);
```

> **Quan trọng:** Bảng này KHÔNG cho phép UPDATE/DELETE (audit log bất biến). Chỉ INSERT và SELECT.

---

### 3.13 `metadata` — Dữ liệu tham chiếu *(cần tạo)*

```sql
CREATE TABLE metadata (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    type       VARCHAR(50) NOT NULL,       -- 'icd10', 'record_type', 'department'
    code       VARCHAR(100),               -- VD: "J00", "NGOAI_KHOA"
    name       VARCHAR(255) NOT NULL,      -- VD: "Viêm họng cấp", "Ngoại Khoa"
    description TEXT,
    is_active  BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,

    INDEX idx_type (type),
    INDEX idx_type_code (type, code)
);

-- Ví dụ dữ liệu
INSERT INTO metadata (type, code, name) VALUES
    ('department',   'NGOAI_KHOA',    'Ngoại Khoa'),
    ('department',   'NOI_KHOA',      'Nội Khoa'),
    ('department',   'TIM_MACH',      'Tim Mạch'),
    ('department',   'THAN_KINH',     'Thần Kinh'),
    ('record_type',  'BENH_AN_NOI',   'Bệnh án Nội trú'),
    ('record_type',  'BENH_AN_NGOAI', 'Bệnh án Ngoại trú'),
    ('icd10',        'J00',           'Viêm họng cấp'),
    ('icd10',        'I10',           'Tăng huyết áp');
```

---

## 4. Workflow Trạng Thái Bệnh Án

```
                    Employee upload
                         │
                         ▼
                    [Processing]   ← AI đang xử lý (YOLO + Tesseract)
                         │
                         ▼
                    [Extracted]    ← AI xong, employee xem & chỉnh sửa OCR
                         │
               employee submit (lưu)
                         │
                         ▼
               [Pending Doctor Review] ← Chờ bác sĩ duyệt
                    │         │
              approve        reject (kèm lý do)
                 │               │
                 ▼               ▼
           [Approved]       [Rejected] ← Employee có thể sửa lại → submit lại
```

**Ai thấy gì theo status:**

| Status | Employee (chủ record) | Doctor | Admin |
|--------|----------------------|--------|-------|
| Processing | ✅ | ✅ | ✅ |
| Extracted | ✅ (có thể edit) | ✅ | ✅ |
| Pending Doctor Review | ✅ (chỉ xem) | ✅ (có thể edit + approve/reject) | ✅ |
| Approved | ✅ (chỉ xem) | ✅ (chỉ xem) | ✅ |
| Rejected | ✅ (có thể edit lại) | ✅ | ✅ |

---

## 5. Script Khởi Tạo Toàn Bộ

```sql
CREATE DATABASE IF NOT EXISTS med_ocr_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE med_ocr_db;

-- 1. Bảng phân quyền (đã có entity Java)
CREATE TABLE role (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(255),
    code           VARCHAR(255) UNIQUE,
    is_active      BOOLEAN,
    is_super_admin BOOLEAN DEFAULT FALSE,
    created_at     DATETIME,
    updated_at     DATETIME
);

CREATE TABLE account (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255),
    birthday        DATE,
    phone_number    VARCHAR(12),
    email           VARCHAR(255) NOT NULL UNIQUE,
    username        VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    is_active       BOOLEAN,
    is_delete       BOOLEAN DEFAULT FALSE,
    email_verify_at DATETIME,
    created_by      BIGINT,
    gender          ENUM('male','female','other') DEFAULT 'other',
    photo_url       VARCHAR(500),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME
);

CREATE TABLE tbl_rf_account_role (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (account_id) REFERENCES account(id),
    FOREIGN KEY (role_id)    REFERENCES role(id),
    UNIQUE KEY uq_account_role (account_id, role_id)
);

CREATE TABLE permission (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(255),
    slug      VARCHAR(255) UNIQUE,
    sort      VARCHAR(50),
    is_hidden BOOLEAN DEFAULT FALSE
);

CREATE TABLE permission_action (
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE
);

CREATE TABLE permission_role (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_id        BIGINT NOT NULL,
    permission_action_id BIGINT NOT NULL,
    role_id              BIGINT NOT NULL,
    FOREIGN KEY (permission_id)        REFERENCES permission(id),
    FOREIGN KEY (permission_action_id) REFERENCES permission_action(id),
    FOREIGN KEY (role_id)              REFERENCES role(id),
    UNIQUE KEY uq_perm_action_role (permission_id, permission_action_id, role_id)
);

CREATE TABLE tbl_manager_token_account (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    token      TEXT NOT NULL,
    is_acitve  BOOLEAN DEFAULT TRUE,
    expired_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    account_id BIGINT NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(id),
    INDEX idx_token (token(255))
);

-- 2. Bảng nghiệp vụ (cần tạo entity Java)
CREATE TABLE medical_records (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_number       VARCHAR(50) NOT NULL UNIQUE,
    uploaded_by         BIGINT NOT NULL,
    patient_name        VARCHAR(200),
    patient_id_card     VARCHAR(20),
    patient_dob         DATE,
    patient_phone       VARCHAR(20),
    original_image_path VARCHAR(500),
    status              ENUM('Processing','Extracted','Pending Doctor Review','Approved','Rejected')
                        DEFAULT 'Processing',
    notes               TEXT,
    rejection_reason    TEXT,
    approved_by         BIGINT,
    approved_at         DATETIME,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME,
    FOREIGN KEY (uploaded_by) REFERENCES account(id),
    FOREIGN KEY (approved_by) REFERENCES account(id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX ft_patient_name (patient_name)
);

CREATE TABLE extracted_fields (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id        BIGINT NOT NULL,
    field_name       VARCHAR(100) NOT NULL,
    field_value      TEXT,
    confidence_score DECIMAL(3,2),
    is_verified      BOOLEAN DEFAULT FALSE,
    verified_by      BIGINT,
    verified_at      DATETIME,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME,
    FOREIGN KEY (record_id)   REFERENCES medical_records(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES account(id),
    INDEX idx_record_id (record_id)
);

CREATE TABLE audit_logs (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT,
    action        VARCHAR(50) NOT NULL,
    entity_type   VARCHAR(50),
    entity_id     BIGINT,
    old_value     JSON,
    new_value     JSON,
    ip_address    VARCHAR(50),
    user_agent    VARCHAR(500),
    status        VARCHAR(20),
    error_message TEXT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES account(id),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    INDEX idx_entity (entity_type, entity_id)
);

CREATE TABLE metadata (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    type        VARCHAR(50) NOT NULL,
    code        VARCHAR(100),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_active   BOOLEAN DEFAULT TRUE,
    sort_order  INT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME,
    INDEX idx_type (type)
);
```

---

## 6. Cấu Hình Spring Boot (application.yaml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medical
    username: postgres
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update         # Dev: auto-update schema
    properties:
      hibernate:
        format_sql: true
    show-sql: true
  hikari:
    maximum-pool-size: 10
    minimum-idle: 2
    connection-timeout: 30000
```

---

## 7. Seeder Order (Thứ Tự Chạy)

| Order | Seeder | Dữ liệu tạo ra |
|-------|--------|----------------|
| 1 | `RoleSeeder` | 3 roles: admin, doctor, employee |
| 2 | `AccountSeeder` | 1 tài khoản admin mặc định |
| 3 | `PermissionActionSeeder` | 6 actions: view/create/edit/delete/cancel/rollback |
| 4 | `PermissionSeeder` | 8 permissions theo slug |
| 5 | `PermissionRoleSeeder` | Gán permission+action cho 3 role |

---

## 8. Lưu Ý Bảo Mật

- Mật khẩu hash bằng **BCrypt** (cost = 10)
- JWT secret key ≥ 256 bit (32 bytes) cho HS256
- `audit_logs` chỉ INSERT, không UPDATE/DELETE
- `is_delete = TRUE` thay vì xóa vật lý (soft delete)
- `tbl_manager_token_account` cho phép revoke token khi logout

---

**Version:** 2.0 (cập nhật theo source code thực tế)
**Ngày cập nhật:** 2026-05-12
**Database:** PostgreSQL 15+
