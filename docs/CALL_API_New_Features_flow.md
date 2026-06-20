# CALL API — New Features Flow (Reject, Audit Log, Dashboard)

> Tài liệu mô tả **đầu vào / đầu ra** của các API mới được thêm vào:  
> - Từ chối bệnh án (Reject + Resubmit)  
> - Nhật ký thao tác (Audit Log)  
> - Thống kê tổng quan (Dashboard)  
>
> Base URL: `http://localhost:8080/api/v1`  
> Mọi request đều cần header: `Authorization: Bearer <token>`

---

## Tổng quan các endpoint mới

| # | Method | Endpoint | Quyền yêu cầu | Mô tả |
|---|--------|----------|---------------|-------|
| 1 | PUT | `/medical-record/{id}/reject` | `medical-records-approval:create` | Từ chối bệnh án, ghi lý do |
| 2 | PUT | `/medical-record/{id}/resubmit` | `medical-records:edit` | Nhân viên nộp lại bệnh án đã bị từ chối |
| 3 | GET | `/audit-logs` | `audit-logs:view` | Danh sách nhật ký thao tác (admin) |
| 4 | GET | `/audit-logs/{id}` | `audit-logs:view` | Chi tiết một log entry |
| 5 | GET | `/medical-record/{id}/audit-logs` | `medical-records:view` | Lịch sử thao tác trên một bệnh án |
| 6 | GET | `/dashboard/overview` | `dashboard:view` | Tổng quan thống kê hệ thống |
| 7 | GET | `/dashboard/records-by-status` | `dashboard:view` | Thống kê số bệnh án theo trạng thái |
| 8 | GET | `/dashboard/records-by-department` | `dashboard:view` | Thống kê số bệnh án theo khoa/phòng |
| 9 | GET | `/dashboard/user-performance` | `dashboard:view` | Hiệu suất upload/duyệt/từ chối theo user |

---

## Vòng đời trạng thái (Status Flow — cập nhật)

```
Upload file
    └─► EXTRACTED
            └─► PENDING_DOCTOR_REVIEW   (nhân viên/admin submit)
                        ├─► APPROVED    (bác sĩ/admin duyệt)
                        └─► REJECTED    (bác sĩ/admin từ chối + lý do)  ← MỚI
                                └─► PENDING_DOCTOR_REVIEW   (nhân viên resubmit)  ← MỚI
```

**Giá trị `status` hợp lệ (bổ sung):**

| Giá trị | Mô tả |
|---------|-------|
| `PROCESSING` | Đang xử lý OCR |
| `EXTRACTED` | OCR xong, chờ nhân viên xác nhận |
| `PENDING_DOCTOR_REVIEW` | Đã submit, chờ bác sĩ duyệt |
| `APPROVED` | Đã được duyệt (trạng thái cuối) |
| `REJECTED` | Bị từ chối, nhân viên có thể sửa và nộp lại ← **MỚI** |

---

## Cấu trúc response bổ sung

### MedicalRecordDetailRes (bổ sung các field rejection)

Các field sau được thêm vào `MedicalRecordDetailRes` (dùng trong `/medical-record/{id}`):

| Field | Type | Mô tả |
|-------|------|-------|
| `rejectedBy` | Long \| null | ID bác sĩ/admin đã từ chối |
| `rejectedAt` | LocalDateTime \| null | Thời điểm từ chối |
| `rejectionReason` | string \| null | Lý do từ chối |

### AuditLogRes

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID log entry |
| `actorId` | Long \| null | ID tài khoản thực hiện thao tác |
| `actorName` | string \| null | **Tên người thực hiện** (VD: `"BS. Trần Văn Khoa"`) |
| `action` | string | Mã thao tác (VD: `"APPROVE"`) |
| `actionLabel` | string \| null | **Tên thao tác hiển thị** (VD: `"Phê duyệt bệnh án"`) |
| `resourceType` | string | Loại tài nguyên (ví dụ: `MEDICAL_RECORD`) |
| `resourceId` | Long \| null | ID của tài nguyên |
| `oldValue` | string \| null | Trạng thái trước thao tác (JSON string) |
| `newValue` | string \| null | Trạng thái sau thao tác (JSON string) |
| `ipAddress` | string \| null | Địa chỉ IP của client |
| `userAgent` | string \| null | User agent trình duyệt/client |
| `createdAt` | LocalDateTime | Thời điểm ghi log |

**Mapping `action` → `actionLabel`:**

| `action` | `actionLabel` | Tự động ghi khi |
|----------|--------------|-----------------|
| `UPLOAD` | `"Upload bệnh án"` | `POST /medical-record/upload` |
| `SUBMIT` | `"Gửi để bác sĩ duyệt"` | `PUT /medical-record/{id}/submit` |
| `APPROVE` | `"Phê duyệt bệnh án"` | `PUT /medical-record/{id}/approve` |
| `REJECT` | `"Từ chối bệnh án"` | `PUT /medical-record/{id}/reject` |
| `RESUBMIT` | `"Nộp lại sau từ chối"` | `PUT /medical-record/{id}/resubmit` |
| `UPDATE` | `"Cập nhật bệnh án"` | `PUT /medical-record/update-detail` |
| `DELETE` | `"Xóa bệnh án"` | `DELETE /medical-record/{id}` |

---

## 1. Từ chối bệnh án

### `PUT /medical-record/{id}/reject`

**Quyền:** `medical-records-approval:create` (Doctor hoặc Admin)

> Bệnh án phải đang ở trạng thái `PENDING_DOCTOR_REVIEW`. Sau khi từ chối chuyển sang `REJECTED`. Nhân viên sẽ thấy lý do từ chối và có thể sửa rồi nộp lại.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |

**Request body:**
```json
{
    "rejectionReason": "Thiếu thông tin bệnh nhân, chẩn đoán không rõ ràng"
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `rejectionReason` | string | ✅ | Lý do từ chối (không được để trống) |

**Ví dụ:** `PUT /api/v1/medical-record/1/reject`

**Response (200):**
```json
{
    "isError": false,
    "message": "Từ chối bệnh án thành công",
    "data": {
        "id": 1,
        "recordNumber": "REC-2026-000001",
        "status": "Rejected",
        "department": "Nội tổng hợp",
        "recordType": "Kết quả xét nghiệm máu",
        "fileName": "xetnghiem_20260510.jpg",
        "fileType": "image/jpeg",
        "uploadedBy": 3,
        "patient": { "...": "..." },
        "createdAt": "2026-05-10T14:30:00",
        "updatedAt": "2026-05-11T10:00:00"
    }
}
```

**Response lỗi (400) — sai trạng thái:**
```json
{
    "isError": true,
    "message": "Chỉ có thể từ chối bệnh án đang ở trạng thái Pending Doctor Review"
}
```

**Response lỗi (403) — không đủ quyền:**
```json
{
    "isError": true,
    "message": "Chỉ bác sĩ hoặc quản trị viên mới có thể từ chối bệnh án"
}
```

> Thao tác này tự động ghi audit log với `action = REJECT` và `newValue` chứa lý do từ chối.

---

## 2. Nộp lại bệnh án sau khi bị từ chối

### `PUT /medical-record/{id}/resubmit`

**Quyền:** `medical-records:edit` (chỉ Employee)

> Bệnh án phải đang ở trạng thái `REJECTED`. Sau khi resubmit chuyển sang `PENDING_DOCTOR_REVIEW`. Nhân viên nên cập nhật bệnh án trước khi gọi API này.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |

**Ví dụ:** `PUT /api/v1/medical-record/1/resubmit`

**Request body:** Không có

**Response (200):**
```json
{
    "isError": false,
    "message": "Nộp lại bệnh án thành công",
    "data": {
        "id": 1,
        "recordNumber": "REC-2026-000001",
        "status": "Pending Doctor Review",
        "department": "Nội tổng hợp",
        "recordType": "Kết quả xét nghiệm máu",
        "fileName": "xetnghiem_20260510.jpg",
        "fileType": "image/jpeg",
        "uploadedBy": 3,
        "patient": { "...": "..." },
        "createdAt": "2026-05-10T14:30:00",
        "updatedAt": "2026-05-11T11:00:00"
    }
}
```

**Response lỗi (400) — sai trạng thái:**
```json
{
    "isError": true,
    "message": "Chỉ có thể nộp lại bệnh án đang ở trạng thái Rejected"
}
```

> `rejectedBy`, `rejectedAt`, `rejectionReason` sẽ được xóa sau khi resubmit.  
> Thao tác này tự động ghi audit log với `action = RESUBMIT`.

---

## 3. Danh sách nhật ký thao tác

### `GET /audit-logs`

**Quyền:** `audit-logs:view` (Admin)

> Xem toàn bộ log thao tác trong hệ thống. Hỗ trợ filter theo loại tài nguyên, tài khoản và loại thao tác.

**Query params:**

| Param | Type | Mặc định | Mô tả |
|-------|------|----------|-------|
| `page` | integer | `1` | Trang hiện tại |
| `limit` | integer | `20` | Số bản ghi mỗi trang |
| `resourceType` | string | — | Filter theo loại tài nguyên (ví dụ: `MEDICAL_RECORD`) |
| `actorId` | Long | — | Filter theo ID tài khoản thực hiện |
| `action` | string | — | Filter theo loại thao tác (ví dụ: `APPROVE`) |

**Ví dụ:**
```
GET /api/v1/audit-logs?resourceType=MEDICAL_RECORD&action=REJECT&page=1&limit=20
GET /api/v1/audit-logs?actorId=2&page=1&limit=10
```

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy danh sách nhật ký thao tác thành công",
    "data": {
        "items": [
            {
                "id": 5,
                "actorId": 2,
                "actorName": "BS. Trần Văn Khoa",
                "action": "REJECT",
                "actionLabel": "Từ chối bệnh án",
                "resourceType": "MEDICAL_RECORD",
                "resourceId": 1,
                "oldValue": "{\"status\":\"Pending Doctor Review\"}",
                "newValue": "{\"status\":\"Rejected\",\"reason\":\"Thiếu thông tin bệnh nhân\"}",
                "ipAddress": "127.0.0.1",
                "userAgent": "Mozilla/5.0 ...",
                "createdAt": "2026-05-11T10:00:00"
            }
        ],
        "currentPage": 1,
        "limit": 20,
        "totalItems": 1,
        "totalPage": 1
    }
}
```

---

## 4. Chi tiết nhật ký thao tác

### `GET /audit-logs/{id}`

**Quyền:** `audit-logs:view` (Admin)

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID log entry |

**Ví dụ:** `GET /api/v1/audit-logs/5`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy chi tiết nhật ký thao tác thành công",
    "data": {
        "id": 5,
        "actorId": 2,
        "actorName": "BS. Trần Văn Khoa",
        "action": "APPROVE",
        "actionLabel": "Phê duyệt bệnh án",
        "resourceType": "MEDICAL_RECORD",
        "resourceId": 1,
        "oldValue": "{\"status\":\"Pending Doctor Review\"}",
        "newValue": "{\"status\":\"Approved\"}",
        "ipAddress": "192.168.1.10",
        "userAgent": "Mozilla/5.0 ...",
        "createdAt": "2026-05-11T09:00:00"
    }
}
```

---

## 5. Lịch sử thao tác trên một bệnh án

### `GET /medical-record/{id}/audit-logs`

**Quyền:** `medical-records:view` (Admin, Doctor, Employee)

> Xem toàn bộ lịch sử thao tác trên một bệnh án cụ thể, sắp xếp từ mới nhất.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |

**Query params:**

| Param | Type | Mặc định | Mô tả |
|-------|------|----------|-------|
| `page` | integer | `1` | Trang hiện tại |
| `limit` | integer | `20` | Số bản ghi mỗi trang |

**Ví dụ:** `GET /api/v1/medical-record/1/audit-logs?page=1&limit=10`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy danh sách nhật ký thao tác thành công",
    "data": {
        "items": [
            {
                "id": 5,
                "actorId": 2,
                "actorName": "BS. Trần Văn Khoa",
                "action": "REJECT",
                "actionLabel": "Từ chối bệnh án",
                "resourceType": "MEDICAL_RECORD",
                "resourceId": 1,
                "oldValue": "{\"status\":\"Pending Doctor Review\"}",
                "newValue": "{\"status\":\"Rejected\",\"reason\":\"Thiếu thông tin\"}",
                "ipAddress": "127.0.0.1",
                "userAgent": "Mozilla/5.0 ...",
                "createdAt": "2026-05-11T10:00:00"
            },
            {
                "id": 3,
                "actorId": 3,
                "actorName": "Nguyễn Nhân Viên",
                "action": "SUBMIT",
                "actionLabel": "Gửi để bác sĩ duyệt",
                "resourceType": "MEDICAL_RECORD",
                "resourceId": 1,
                "oldValue": "{\"status\":\"Extracted\"}",
                "newValue": "{\"status\":\"Pending Doctor Review\"}",
                "ipAddress": "127.0.0.1",
                "userAgent": "Mozilla/5.0 ...",
                "createdAt": "2026-05-10T15:00:00"
            },
            {
                "id": 1,
                "actorId": 3,
                "actorName": "Nguyễn Nhân Viên",
                "action": "UPLOAD",
                "actionLabel": "Upload bệnh án",
                "resourceType": "MEDICAL_RECORD",
                "resourceId": 1,
                "oldValue": null,
                "newValue": "{\"status\":\"Extracted\"}",
                "ipAddress": "127.0.0.1",
                "userAgent": "Mozilla/5.0 ...",
                "createdAt": "2026-05-10T14:30:00"
            }
        ],
        "currentPage": 1,
        "limit": 20,
        "totalItems": 3,
        "totalPage": 1
    }
}
```

---

## 6. Tổng quan dashboard

### `GET /dashboard/overview`

**Quyền:** `dashboard:view` (Admin, Doctor)

> Trả về một snapshot thống kê toàn hệ thống tại thời điểm gọi.

**Ví dụ:** `GET /api/v1/dashboard/overview`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy tổng quan dashboard thành công",
    "data": {
        "totalRecords": 128,
        "totalPatients": 95,
        "totalAccounts": 12,
        "processingRecords": 0,
        "extractedRecords": 14,
        "pendingReviewRecords": 7,
        "approvedRecords": 100,
        "rejectedRecords": 7,
        "todayUploads": 5,
        "todayApprovals": 3
    }
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `totalRecords` | long | Tổng số bệnh án chưa bị xóa |
| `totalPatients` | long | Tổng số bệnh nhân |
| `totalAccounts` | long | Tổng số tài khoản |
| `processingRecords` | long | Bệnh án đang xử lý OCR |
| `extractedRecords` | long | Bệnh án đã OCR xong, chờ xác nhận |
| `pendingReviewRecords` | long | Bệnh án chờ bác sĩ duyệt |
| `approvedRecords` | long | Bệnh án đã duyệt |
| `rejectedRecords` | long | Bệnh án đã bị từ chối |
| `todayUploads` | long | Số bệnh án được upload hôm nay |
| `todayApprovals` | long | Số bệnh án được duyệt hôm nay |

---

## 7. Thống kê bệnh án theo trạng thái

### `GET /dashboard/records-by-status`

**Quyền:** `dashboard:view` (Admin, Doctor)

**Ví dụ:** `GET /api/v1/dashboard/records-by-status`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy thống kê bệnh án theo trạng thái thành công",
    "data": [
        { "label": "Extracted",             "count": 14 },
        { "label": "Pending Doctor Review", "count": 7  },
        { "label": "Approved",              "count": 100 },
        { "label": "Rejected",              "count": 7  }
    ]
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `label` | string | Tên trạng thái (giá trị DB: `Extracted`, `Pending Doctor Review`, `Approved`, `Rejected`) |
| `count` | long | Số bệnh án ở trạng thái đó |

---

## 8. Thống kê bệnh án theo khoa/phòng

### `GET /dashboard/records-by-department`

**Quyền:** `dashboard:view` (Admin, Doctor)

> Sắp xếp giảm dần theo số lượng. Chỉ đếm bệnh án có `department` không rỗng.

**Ví dụ:** `GET /api/v1/dashboard/records-by-department`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy thống kê bệnh án theo khoa thành công",
    "data": [
        { "label": "Nội tổng hợp", "count": 45 },
        { "label": "Xét nghiệm",  "count": 38 },
        { "label": "Tim mạch",    "count": 25 },
        { "label": "Nhi",         "count": 20 }
    ]
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `label` | string | Tên khoa/phòng |
| `count` | long | Số bệnh án thuộc khoa đó |

---

## 9. Hiệu suất người dùng

### `GET /dashboard/user-performance`

**Quyền:** `dashboard:view` (Admin, Doctor)

> Thống kê số bệnh án đã upload, đã duyệt và đã từ chối theo từng tài khoản. Bao gồm tất cả tài khoản từng tham gia tương tác với bệnh án.

**Ví dụ:** `GET /api/v1/dashboard/user-performance`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy hiệu suất người dùng thành công",
    "data": [
        {
            "accountId": 3,
            "accountName": "Nguyễn Nhân Viên",
            "uploaded": 55,
            "approved": 0,
            "rejected": 0
        },
        {
            "accountId": 2,
            "accountName": "BS. Trần Văn Khoa",
            "uploaded": 0,
            "approved": 80,
            "rejected": 7
        }
    ]
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `accountId` | Long | ID tài khoản |
| `accountName` | string | Tên hiển thị |
| `uploaded` | long | Số bệnh án đã upload |
| `approved` | long | Số bệnh án đã duyệt |
| `rejected` | long | Số bệnh án đã từ chối |

---

## Thứ tự gọi API điển hình — Luồng từ chối và nộp lại

```
[Nhân viên/Admin — Upload và submit]
    POST /medical-record/upload           → bệnh án EXTRACTED
    PUT  /medical-record/update-detail    (sửa nếu OCR sai)
    PUT  /medical-record/{id}/submit      → PENDING_DOCTOR_REVIEW

[Bác sĩ — Xem và từ chối]
    GET  /medical-record/pending-review   (xem danh sách chờ duyệt)
    GET  /medical-record/{id}             (xem chi tiết, đọc extractedData)
    PUT  /medical-record/{id}/reject      (body: {"rejectionReason": "..."})
                                          → REJECTED + ghi audit log

[Nhân viên — Sửa và nộp lại]
    GET  /medical-record/{id}             (xem lý do từ chối trong rejectionReason)
    PUT  /medical-record/update-detail    (cập nhật lại thông tin)
    PUT  /medical-record/{id}/resubmit    → PENDING_DOCTOR_REVIEW lần 2

[Bác sĩ — Xem lại và duyệt]
    GET  /medical-record/{id}             (xem lại sau khi nhân viên sửa)
    PUT  /medical-record/{id}/approve     → APPROVED

[Admin — Truy vết và thống kê]
    GET  /medical-record/{id}/audit-logs  (xem toàn bộ lịch sử bệnh án)
    GET  /audit-logs?action=REJECT        (tìm tất cả lần từ chối)
    GET  /dashboard/overview              (xem tổng quan hệ thống)
    GET  /dashboard/user-performance      (so sánh hiệu suất bác sĩ)
```

---

## Xử lý lỗi chung

| HTTP Status | Ý nghĩa | Xử lý |
|-------------|---------|-------|
| 400 | Sai trạng thái / thiếu field bắt buộc | Đọc `message` để hiển thị |
| 401 | Token hết hạn | Gọi refresh-token, nếu vẫn lỗi → logout |
| 403 | Không có quyền | Hiển thị "Không có quyền truy cập" |
| 404 | Không tìm thấy | Hiển thị thông báo tương ứng |
| 500 | Lỗi server | Hiển thị lỗi chung |

**Cấu trúc response lỗi:**
```json
{
    "isError": true,
    "message": "Mô tả lỗi cụ thể"
}
```
