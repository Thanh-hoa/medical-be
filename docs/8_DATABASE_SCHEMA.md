# TÀI LIỆU LƯỢC ĐỒ CƠ SỞ DỮ LIỆU (DATABASE SCHEMA)
## DỰ ÁN: HỆ THỐNG TRÍCH XUẤT THÔNG TIN BỆNH ÁN (MED-OCR)

---

## 1. Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  ┌────────────────┐        ┌──────────────────────┐             │
│  │ roles          │        │ users                │             │
│  ├────────────────┤        ├──────────────────────┤             │
│  │ id (PK)        │◄───────│ id (PK)              │             │
│  │ role_name      │ 1    * │ username             │             │
│  │ created_at     │        │ email                │             │
│  └────────────────┘        │ password (hashed)    │             │
│                            │ full_name            │             │
│                            │ role_id (FK)         │             │
│                            │ is_active            │             │
│                            │ created_at           │             │
│                            │ updated_at           │             │
│                            └──────────────────────┘             │
│                                   │                             │
│                                   │ 1 * (has many records)      │
│                                   │                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ medical_records                                         │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │ id (PK)                                                 │    │
│  │ record_number (unique)                                  │    │
│  │ uploaded_by (FK → users.id)                             │    │
│  │ patient_name                                            │    │
│  │ patient_id_card                                         │    │
│  │ patient_dob                                             │    │
│  │ original_image_path                                     │    │
│  │ status (Processing, Extracted, Pending, Approved, ...) │    │
│  │ created_at                                              │    │
│  │ updated_at                                              │    │
│  │ approved_by (FK → users.id, nullable)                   │    │
│  │ approved_at (nullable)                                  │    │
│  │ rejection_reason (nullable)                             │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                   │                             │
│                        1 * (has many fields)                    │
│                                   │                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ extracted_fields                                        │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │ id (PK)                                                 │    │
│  │ record_id (FK → medical_records.id)                     │    │
│  │ field_name (patient_name, disease_name, drug, etc.)    │    │
│  │ field_value (text)                                      │    │
│  │ confidence_score (0.0 - 1.0)                            │    │
│  │ is_verified (boolean)                                   │    │
│  │ created_at                                              │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ audit_logs                                              │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │ id (PK)                                                 │    │
│  │ user_id (FK → users.id)                                 │    │
│  │ action (LOGIN, UPLOAD, EDIT, APPROVE, REJECT)          │    │
│  │ entity_type (USER, MEDICAL_RECORD, etc.)                │    │
│  │ entity_id                                               │    │
│  │ old_value (JSON)                                        │    │
│  │ new_value (JSON)                                        │    │
│  │ ip_address                                              │    │
│  │ created_at                                              │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Table Definitions

### 2.1 `roles` - Vai trò người dùng

```sql
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO roles (role_name, description) VALUES
    ('ROLE_STAFF', 'Nhân viên y tế - Upload và xác minh bệnh án'),
    ('ROLE_DOCTOR', 'Bác sĩ - Phê duyệt bệnh án'),
    ('ROLE_ADMIN', 'Quản trị viên - Quản lý hệ thống');
```

---

### 2.2 `users` - Người dùng

```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    role_id INT NOT NULL,
    department VARCHAR(100),
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (role_id) REFERENCES roles(id),
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role_id (role_id),
    INDEX idx_is_active (is_active)
);
```

**Columns:**
- `id`: Primary key
- `username`: Unique username for login
- `email`: Email address (unique)
- `password`: Hashed password (bcrypt)
- `full_name`: Display name
- `role_id`: Reference to roles table
- `department`: Department/Khoa phòng (e.g., "Ngoại khoa", "Tim mạch")
- `phone_number`: Contact phone
- `is_active`: Status (active/inactive)
- `failed_login_attempts`: For security (lock after 3 failed attempts)
- `locked_until`: Timestamp when user is locked

---

### 2.3 `medical_records` - Bệnh án

```sql
CREATE TABLE medical_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    record_number VARCHAR(50) NOT NULL UNIQUE,
    uploaded_by INT NOT NULL,
    patient_name VARCHAR(200),
    patient_id_card VARCHAR(20),
    patient_dob DATE,
    patient_phone VARCHAR(20),
    original_image_path VARCHAR(500),
    status ENUM('Processing', 'Extracted', 'Pending Doctor Review', 'Approved', 'Rejected') DEFAULT 'Processing',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_by INT,
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT,
    
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_record_number (record_number),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_patient_name (patient_name),
    FULLTEXT INDEX ft_patient_name (patient_name)
);
```

**Columns:**
- `id`: Primary key
- `record_number`: Unique identifier (e.g., "REC-2026-001")
- `uploaded_by`: FK to users table (who uploaded)
- `patient_name`: Patient name
- `patient_id_card`: ID card / CMND number
- `patient_dob`: Date of birth
- `original_image_path`: Path to uploaded image file
- `status`: Workflow status
  - `Processing`: AI is processing
  - `Extracted`: AI extraction done
  - `Pending Doctor Review`: Waiting for doctor approval
  - `Approved`: Doctor approved
  - `Rejected`: Doctor rejected
- `approved_by`: FK to users (doctor who approved)
- `approved_at`: Timestamp of approval
- `rejection_reason`: If rejected, why?

**Indexes:**
- PK: id
- Unique: record_number
- Regular: uploaded_by, status, created_at
- Fulltext: patient_name (for search)

---

### 2.4 `extracted_fields` - Dữ liệu trích xuất

```sql
CREATE TABLE extracted_fields (
    id INT PRIMARY KEY AUTO_INCREMENT,
    record_id INT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_value TEXT,
    confidence_score DECIMAL(3, 2),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by INT,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (record_id) REFERENCES medical_records(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(id),
    INDEX idx_record_id (record_id),
    INDEX idx_field_name (field_name)
);
```

**Columns:**
- `id`: Primary key
- `record_id`: FK to medical_records
- `field_name`: Name of extracted field (e.g., "patient_name", "disease_icd10", "drug_name")
- `field_value`: The extracted/verified value
- `confidence_score`: AI confidence (0.0 - 1.0, where 1.0 = 100%)
- `is_verified`: Is this value verified by user?
- `verified_by`: FK to users (who verified)
- `verified_at`: When verified

**Example records:**
```
record_id | field_name      | field_value            | confidence_score
1         | patient_name    | Nguyễn Văn A           | 0.95
1         | patient_dob     | 1990-05-15             | 0.87
1         | disease_main    | Viêm họng cấp (J00)    | 0.92
1         | drug_1          | Amoxicillin 500mg      | 0.89
```

---

### 2.5 `audit_logs` - Nhật ký hệ thống

```sql
CREATE TABLE audit_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    old_value JSON,
    new_value JSON,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    status VARCHAR(20),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    INDEX idx_entity (entity_type, entity_id)
);
```

**Columns:**
- `id`: Primary key
- `user_id`: FK to users (who performed the action)
- `action`: What action? (LOGIN, LOGOUT, UPLOAD, EDIT, APPROVE, REJECT, DELETE)
- `entity_type`: What entity? (USER, MEDICAL_RECORD, EXTRACTED_FIELD)
- `entity_id`: ID of the entity
- `old_value`: JSON of old data (for UPDATE)
- `new_value`: JSON of new data (for UPDATE)
- `ip_address`: IP of user
- `user_agent`: Browser info
- `status`: SUCCESS or FAILURE
- `error_message`: If failed, why?

**Example:**
```json
{
  "user_id": 5,
  "action": "UPLOAD",
  "entity_type": "MEDICAL_RECORD",
  "entity_id": 42,
  "old_value": null,
  "new_value": {
    "record_number": "REC-2026-042",
    "patient_name": "Nguyễn Văn A",
    "original_image_path": "/uploads/record-42-1.jpg"
  },
  "status": "SUCCESS",
  "created_at": "2026-05-01 10:30:00"
}
```

---

## 3. Database Initialization Script

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS med_ocr_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE med_ocr_db;

-- Create roles table
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    role_id INT NOT NULL,
    department VARCHAR(100),
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role_id (role_id),
    INDEX idx_is_active (is_active)
);

-- Create medical_records table
CREATE TABLE medical_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    record_number VARCHAR(50) NOT NULL UNIQUE,
    uploaded_by INT NOT NULL,
    patient_name VARCHAR(200),
    patient_id_card VARCHAR(20),
    patient_dob DATE,
    patient_phone VARCHAR(20),
    original_image_path VARCHAR(500),
    status ENUM('Processing', 'Extracted', 'Pending Doctor Review', 'Approved', 'Rejected') DEFAULT 'Processing',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_by INT,
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT,
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_record_number (record_number),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX ft_patient_name (patient_name)
);

-- Create extracted_fields table
CREATE TABLE extracted_fields (
    id INT PRIMARY KEY AUTO_INCREMENT,
    record_id INT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_value TEXT,
    confidence_score DECIMAL(3, 2),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by INT,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_records(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(id),
    INDEX idx_record_id (record_id),
    INDEX idx_field_name (field_name)
);

-- Create audit_logs table
CREATE TABLE audit_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    old_value JSON,
    new_value JSON,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    status VARCHAR(20),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    INDEX idx_entity (entity_type, entity_id)
);

-- Insert sample roles
INSERT INTO roles (role_name, description) VALUES
    ('ROLE_STAFF', 'Nhân viên y tế'),
    ('ROLE_DOCTOR', 'Bác sĩ'),
    ('ROLE_ADMIN', 'Quản trị viên');

-- Insert sample users
INSERT INTO users (username, email, password, full_name, role_id, department, phone_number) VALUES
    ('staff1', 'staff1@hospital.com', '$2a$10$...', 'Nguyễn Văn Staff', 1, 'Ngoại khoa', '0123456789'),
    ('doctor1', 'doctor1@hospital.com', '$2a$10$...', 'Trần Thị Doctor', 2, 'Ngoại khoa', '0987654321'),
    ('admin1', 'admin1@hospital.com', '$2a$10$...', 'Lê Văn Admin', 3, 'IT', '0888888888');
```

---

## 4. Indexes Optimization

**Query Performance Tips:**

```sql
-- Search by patient name (with fulltext index)
SELECT * FROM medical_records WHERE MATCH(patient_name) AGAINST('Nguyễn +Văn' IN BOOLEAN MODE);

-- Find pending records for doctor
SELECT * FROM medical_records WHERE status = 'Pending Doctor Review' AND approved_by IS NULL ORDER BY created_at DESC;

-- Find records by staff
SELECT * FROM medical_records WHERE uploaded_by = 5 ORDER BY created_at DESC;

-- Get extracted fields for a record
SELECT * FROM extracted_fields WHERE record_id = 42 AND confidence_score > 0.7;
```

---

## 5. Data Retention & Archival

**Data Retention Policy:**
- ✅ Active records: Keep in main database
- ✅ Archived records (> 1 year): Move to archive table
- ✅ Audit logs: Keep for 3 years (compliance)
- ✅ Soft delete: Never physically delete (logical delete with is_deleted flag)

---

## 6. Backup & Recovery

**Backup Strategy:**
- Daily full backup at 2 AM
- Weekly incremental backup
- Store backups on external drive + cloud

**Restore Procedure:**
```bash
# Full restore
mysql -u root -p med_ocr_db < backup_full_20260501.sql

# Point-in-time restore (using binary logs)
mysqlbinlog binlog.000001 --stop-datetime="2026-05-01 10:30:00" | mysql -u root -p
```

---

## 7. Security Considerations

- ✅ Passwords hashed with bcrypt (cost = 10)
- ✅ Sensitive data encrypted at rest
- ✅ Audit log immutable (no UPDATE/DELETE allowed)
- ✅ User IP & user-agent logged for security audit
- ✅ GDPR compliance: Soft delete + data retention policy
- ✅ Regular backups (encrypted)

---

## 8. Connection Configuration (Spring Boot)

```yaml
# application.yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/med_ocr_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-create/update (manage manually)
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
    show-sql: false
    
  hikari:
    maximum-pool-size: 10
    minimum-idle: 2
    connection-timeout: 30000
```

---

## 9. ER Diagram (Text Format)

```
Users (1) ─────── (Many) MedicalRecords
  │
  └─ Role

MedicalRecords (1) ─────── (Many) ExtractedFields

MedicalRecords ──────── AuditLogs
Users ──────── AuditLogs
```

---

## 10. Notes

- All timestamps in UTC
- UTF-8 encoding for Vietnamese characters
- Indexes optimized for read-heavy workload (search, filter)
- Foreign keys enforced (RESTRICT on delete)
- Audit trail complete (all CRUD operations logged)

---

**Version:** 1.0  
**Date:** 2026-04-30  
**Database:** MySQL 8.0+  
**Status:** Ready for Implementation
