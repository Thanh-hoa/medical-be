# CALL API — Medical Record Flow

> Mô tả **đầu vào / đầu ra** của toàn bộ API quản lý bệnh án.  
> Base URL: `http://localhost:8080/api/v1`  
> Mọi request đều cần header: `Authorization: Bearer <token>`

---

## Tổng quan các endpoint

| # | Method | Endpoint | Quyền yêu cầu | Mô tả |
|---|--------|----------|---------------|-------|
| 1 | POST | `/medical-record/upload` | `medical-records:create` | Upload ảnh/PDF, gọi OCR đồng bộ, tạo bệnh án |
| 2 | GET | `/medical-record/list` | `medical-records:view` | Danh sách bệnh án (phân trang, filter) |
| 3 | GET | `/medical-record/pending-review` | `medical-records-approval:create` | Danh sách bệnh án chờ bác sĩ duyệt |
| 4 | GET | `/medical-record/{id}` | `medical-records:view` | Chi tiết bệnh án |
| 5 | PUT | `/medical-record/update-detail` | `medical-records:edit` | Cập nhật toàn bộ thông tin bệnh án |
| 6 | PUT | `/medical-record/field/update` | `medical-records:edit` | Cập nhật một trường OCR |
| 7 | PUT | `/medical-record/{id}/submit` | `medical-records:edit` | Gửi bệnh án để bác sĩ duyệt |
| 8 | PUT | `/medical-record/{id}/approve` | `medical-records-approval:create` | Duyệt bệnh án |
| 9 | DELETE | `/medical-record/{id}` | `medical-records:delete` | Xóa vĩnh viễn bệnh án |

---

## Vòng đời trạng thái (Status Flow)

```
Upload file (transaction đồng bộ)
    └─► [PROCESSING → EXTRACTED]   (nội bộ trong 1 transaction, client không thấy PROCESSING)
                └─► PENDING_DOCTOR_REVIEW   (nhân viên/admin submit)
                            └─► APPROVED    (bác sĩ/admin duyệt, bắt buộc)
```

---

## Cấu trúc response dùng chung

### MedicalRecordSummaryRes (dùng trong list, pending-review, submit, approve)

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |
| `recordNumber` | string | Mã bệnh án (VD: `REC-2026-000001`) |
| `status` | string | Trạng thái hiện tại |
| `department` | string \| null | Khoa/phòng |
| `recordType` | string \| null | Loại bệnh án |
| `fileName` | string | Tên file gốc |
| `fileType` | string | MIME type (VD: `image/jpeg`) |
| `uploadedBy` | Long | ID tài khoản upload |
| `patient` | PatientRes \| null | Thông tin bệnh nhân (nếu đã xác định) |
| `createdAt` | LocalDateTime | Thời điểm tạo |
| `updatedAt` | LocalDateTime \| null | Thời điểm cập nhật gần nhất |

### MedicalRecordDetailRes (dùng trong upload, detail, update-detail)

Bao gồm tất cả field của `MedicalRecordSummaryRes`, cộng thêm:

| Field | Type | Mô tả |
|-------|------|-------|
| `originalImagePath` | string | URL đầy đủ tới file ảnh gốc |
| `notes` | string \| null | Ghi chú |
| `verifiedBy` | Long \| null | ID nhân viên/admin đã submit |
| `verifiedAt` | LocalDateTime \| null | Thời điểm submit |
| `approvedBy` | Long \| null | ID bác sĩ/admin đã duyệt |
| `approvedAt` | LocalDateTime \| null | Thời điểm duyệt |
| `extractedData` | `ExtractedDataDto` | Dữ liệu OCR đã phân loại theo nhóm |
| `labData` | `LabResultJson[]` | Danh sách kết quả xét nghiệm |

### ExtractedDataDto

Dữ liệu OCR được tổ chức theo nhóm. Các field `null` sẽ bị bỏ qua trong JSON response (`@JsonInclude(NON_NULL)`).

> Thông tin bệnh nhân (name, bhyt, dob, gender, address) **không lưu vào đây** — xem tại field `patient` của response.

**Nhóm: Thông tin cơ sở / phiếu**

| Field | Type | Mô tả |
|-------|------|-------|
| `facility` | string \| null | Tên bệnh viện / cơ sở |
| `department` | string \| null | Khoa/Phòng (đã strip tiền tố "Phòng:" / "Khoa:") |
| `signerName` | string \| null | Người ký phiếu |

**Nhóm: Chẩn đoán**

| Field | Type | Mô tả |
|-------|------|-------|
| `diagnosis` | string \| null | Chẩn đoán |

**Trường chưa phân loại**

| Field | Type | Mô tả |
|-------|------|-------|
| `extra` | `Map<string, string>` \| null | Các key OCR lạ không thuộc nhóm nào ở trên |

### PatientRes

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh nhân |
| `bhyt` | string | Mã BHYT |
| `name` | string | Họ tên |
| `dob` | string \| null | Ngày sinh (`yyyy-MM-dd`) |
| `gender` | string \| null | Giới tính |
| `address` | string \| null | Địa chỉ |
| `phone` | string \| null | Số điện thoại |
| `createdAt` | LocalDateTime | Thời điểm tạo |
| `updatedAt` | LocalDateTime \| null | Thời điểm cập nhật |

### LabResultJson

| Field | Type | Mô tả |
|-------|------|-------|
| `testName` | string | Tên chỉ số xét nghiệm |
| `testValue` | string | Giá trị đo được |
| `unit` | string | Đơn vị (VD: `mmol/L`, `%`) |
| `referenceRange` | string | Khoảng tham chiếu bình thường |
| `isAbnormal` | boolean | `true` nếu nằm ngoài khoảng bình thường |

---

## 1. Upload ảnh/PDF và xử lý OCR

### `POST /medical-record/upload`

**Quyền:** `medical-records:create`

**Content-Type:** `multipart/form-data`

**Form field:**

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `file` | File | ✅ | File ảnh cần upload (jpg, jpeg, png, webp) |

**Flow xử lý (đồng bộ, trong 1 transaction):**
```
Client upload file
    └─► Lưu file + tạo bệnh án PROCESSING
            └─► Gọi OCR service xử lý ảnh
                    └─► Nhận extractedData + labData
                            └─► Tự động tạo/tìm bệnh nhân theo BHYT + tên
                                    └─► Trả về bệnh án EXTRACTED
                                        (nếu lỗi ở bất kỳ bước nào → rollback toàn bộ)
```

**Response (200):**
```json
{
    "isError": false,
    "message": "Upload và xử lý OCR thành công",
    "data": {
        "id": 1,
        "recordNumber": "REC-2026-000001",
        "status": "EXTRACTED",
        "department": "Nội tổng hợp",
        "recordType": null,
        "fileName": "xetnghiem_20260510.jpg",
        "fileType": "image/jpeg",
        "originalImagePath": "http://localhost:8080/uploads/photos/uuid-xxx.jpg",
        "notes": null,
        "uploadedBy": 3,
        "verifiedBy": null,
        "verifiedAt": null,
        "approvedBy": null,
        "approvedAt": null,
        "createdAt": "2026-05-10T14:30:00",
        "updatedAt": "2026-05-10T14:30:05",
        "patient": {
            "id": 1,
            "bhyt": "GD4030000123456",
            "name": "Nguyễn Văn A",
            "dob": "1990-05-15",
            "gender": "Nam",
            "address": "123 Lê Lợi, Quận 1, TP.HCM",
            "phone": "0901234567",
            "createdAt": "2026-01-10T09:00:00",
            "updatedAt": null
        },
        "extractedData": {
            "facility": "BV Đa Khoa Trung Ương",
            "department": "Nội tổng hợp",
            "signerName": "BS. Nguyễn Văn Khoa",
            "diagnosis": "Theo dõi nhiễm trùng, chỉ định xét nghiệm bổ sung"
        },
        "labData": [
            {
                "testName": "Glucose",
                "testValue": "5.2",
                "unit": "mmol/L",
                "referenceRange": "3.9 - 6.1",
                "isAbnormal": false
            },
            {
                "testName": "HbA1c",
                "testValue": "7.8",
                "unit": "%",
                "referenceRange": "< 6.5",
                "isAbnormal": true
            }
        ]
    }
}
```

**Response lỗi (400) — sai định dạng file:**
```json
{
    "isError": true,
    "message": "Chỉ chấp nhận file jpg, jpeg, png, webp"
}
```

---

## 2. Danh sách bệnh án

### `GET /medical-record/list`

**Quyền:** `medical-records:view`

> **Lưu ý phân quyền:** Employee chỉ thấy bệnh án do mình upload. Admin/Doctor thấy tất cả.

**Query params:**

| Param | Type | Mặc định | Mô tả |
|-------|------|----------|-------|
| `page` | integer | `1` | Trang hiện tại |
| `limit` | integer | `20` | Số bản ghi mỗi trang |
| `q` | string | — | Tìm kiếm theo mã bệnh án / tên file |
| `status` | string | — | Filter theo trạng thái (xem bảng status bên dưới) |
| `patientId` | Long | — | Filter theo ID bệnh nhân (chỉ Admin/Doctor) |
| `sort_by` | string | `created_at` | Trường sắp xếp: `created_at` \| `status` \| `record_number` |
| `order_by` | string | `desc` | `asc` \| `desc` |

**Giá trị hợp lệ cho `status`:**

| Giá trị | Mô tả |
|---------|-------|
| `PROCESSING` | Đang xử lý OCR |
| `EXTRACTED` | OCR xong, chờ nhân viên xác nhận |
| `PENDING_DOCTOR_REVIEW` | Đã submit, chờ bác sĩ duyệt |
| `APPROVED` | Đã được duyệt |

**Ví dụ:** `GET /api/v1/medical-record/list?page=1&limit=20&status=EXTRACTED`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy danh sách bệnh án thành công",
    "data": {
        "items": [
            {
                "id": 1,
                "recordNumber": "REC-2026-000001",
                "status": "APPROVED",
                "department": "Xét nghiệm",
                "recordType": "Kết quả xét nghiệm máu",
                "fileName": "xetnghiem_20260510.jpg",
                "fileType": "image/jpeg",
                "uploadedBy": 3,
                "patient": {
                    "id": 1,
                    "bhyt": "GD4030000123456",
                    "name": "Nguyễn Văn A",
                    "dob": "1990-05-15",
                    "gender": "Nam",
                    "address": "123 Lê Lợi, Quận 1, TP.HCM",
                    "phone": "0901234567",
                    "createdAt": "2026-01-10T09:00:00",
                    "updatedAt": null
                },
                "createdAt": "2026-05-10T14:30:00",
                "updatedAt": "2026-05-11T09:00:00"
            }
        ],
        "currentPage": 1,
        "limit": 20,
        "totalItems": 1,
        "totalPage": 1
    }
}
```

**Cấu trúc `data` (PagedResponse):**

| Field | Type | Mô tả |
|-------|------|-------|
| `items` | `MedicalRecordSummaryRes[]` | Danh sách bệnh án trang hiện tại |
| `currentPage` | integer | Trang hiện tại |
| `limit` | integer | Số bản ghi mỗi trang |
| `totalItems` | integer | Tổng số bản ghi |
| `totalPage` | integer | Tổng số trang |

---

## 3. Danh sách bệnh án chờ bác sĩ duyệt

### `GET /medical-record/pending-review`

**Quyền:** `medical-records-approval:create` (Doctor hoặc Admin)

> Luôn trả về các bệnh án đang ở trạng thái `PENDING_DOCTOR_REVIEW`. Sắp xếp theo `createdAt ASC` — bệnh án chờ lâu nhất hiển thị đầu tiên. Bác sĩ bấm vào từng bệnh án để xem chi tiết và duyệt.

**Query params:**

| Param | Type | Mặc định | Mô tả |
|-------|------|----------|-------|
| `page` | integer | `1` | Trang hiện tại |
| `limit` | integer | `20` | Số bản ghi mỗi trang |
| `q` | string | — | Tìm kiếm theo mã bệnh án / tên file |
| `patientId` | Long | — | Filter theo ID bệnh nhân |

> `status`, `sort_by`, `order_by` không có hiệu lực — endpoint này luôn trả về `PENDING_DOCTOR_REVIEW` sắp xếp vào trước ra trước.

**Ví dụ:** `GET /api/v1/medical-record/pending-review?page=1&limit=20`

**Response (200):** Cấu trúc giống [Danh sách bệnh án](#3-danh-sách-bệnh-án), `message`: `"Lấy danh sách bệnh án thành công"`.

---

## 4. Chi tiết bệnh án

### `GET /medical-record/{id}`

**Quyền:** `medical-records:view`

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |

**Ví dụ:** `GET /api/v1/medical-record/1`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy chi tiết bệnh án thành công",
    "data": {
        "id": 1,
        "recordNumber": "REC-2026-000001",
        "status": "APPROVED",
        "department": "Xét nghiệm",
        "recordType": "Kết quả xét nghiệm máu",
        "fileName": "xetnghiem_20260510.jpg",
        "fileType": "image/jpeg",
        "originalImagePath": "http://localhost:8080/uploads/photos/uuid-xxx.jpg",
        "notes": "Bệnh nhân cần tái khám sau 1 tháng",
        "uploadedBy": 3,
        "verifiedBy": 3,
        "verifiedAt": "2026-05-10T15:00:00",
        "approvedBy": 2,
        "approvedAt": "2026-05-11T09:00:00",
        "createdAt": "2026-05-10T14:30:00",
        "updatedAt": "2026-05-11T09:00:00",
        "patient": {
            "id": 1,
            "bhyt": "GD4030000123456",
            "name": "Nguyễn Văn A",
            "dob": "1990-05-15",
            "gender": "Nam",
            "address": "123 Lê Lợi, Quận 1, TP.HCM",
            "phone": "0901234567",
            "createdAt": "2026-01-10T09:00:00",
            "updatedAt": null
        },
        "extractedData": {
            "facility": "BV Đa Khoa Trung Ương",
            "department": "Xét nghiệm",
            "signerName": "BS. Nguyễn Văn Khoa",
            "diagnosis": "Bệnh nhân cần tái khám sau 1 tháng"
        },
        "labData": [
            {
                "testName": "Glucose",
                "testValue": "5.2",
                "unit": "mmol/L",
                "referenceRange": "3.9 - 6.1",
                "isAbnormal": false
            },
            {
                "testName": "HbA1c",
                "testValue": "7.8",
                "unit": "%",
                "referenceRange": "< 6.5",
                "isAbnormal": true
            }
        ]
    }
}
```

**Response lỗi (404):**
```json
{
    "isError": true,
    "message": "Bệnh án không tồn tại"
}
```

---

## 5. Cập nhật chi tiết bệnh án

### `PUT /medical-record/update-detail`

**Quyền:** `medical-records:edit`

> Employee chỉ cập nhật được bệnh án do mình upload.

**Request body:**
```json
{
    "id": 1,
    "department": "Xét nghiệm",
    "recordType": "Kết quả xét nghiệm máu",
    "notes": "Đã chỉnh sửa thủ công",
    "patient": {
        "bhyt": "GD4030000123456",
        "name": "Nguyễn Văn A",
        "dob": "1990-05-15",
        "gender": "Nam",
        "address": "123 Lê Lợi, Quận 1, TP.HCM",
        "phone": "0901234567"
    },
    "extractedData": {
        "facility": "BV Đa Khoa Trung Ương",
        "department": "Xét nghiệm",
        "diagnosis": "Theo dõi sau điều trị"
    },
    "labData": [
        {
            "testName": "Glucose",
            "testValue": "5.5",
            "unit": "mmol/L",
            "referenceRange": "3.9 - 6.1",
            "isAbnormal": false
        }
    ]
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `id` | Long | ✅ | ID bệnh án cần cập nhật |
| `department` | string | ❌ | Khoa/phòng |
| `recordType` | string | ❌ | Loại bệnh án |
| `notes` | string | ❌ | Ghi chú |
| `patient` | object | ❌ | Thông tin bệnh nhân (xem bảng bên dưới) |
| `extractedData` | `ExtractedDataDto` | ❌ | Ghi đè toàn bộ extracted data (xem cấu trúc `ExtractedDataDto` ở trên) |
| `labData` | `LabResultJson[]` | ❌ | Ghi đè toàn bộ lab data |

**Cấu trúc object `patient`:**

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `bhyt` | string | ✅ | Mã BHYT (bắt buộc nếu truyền `patient`) |
| `name` | string | ✅ | Họ tên (bắt buộc nếu truyền `patient`) |
| `dob` | string | ❌ | Ngày sinh (`yyyy-MM-dd`) |
| `gender` | string | ❌ | Giới tính |
| `address` | string | ❌ | Địa chỉ |
| `phone` | string | ❌ | Số điện thoại |

> **Lưu ý:** Chỉ các field được truyền mới được cập nhật (partial update). Trừ `extractedData` và `labData` — nếu truyền thì **ghi đè toàn bộ**.

**Response (200):** Cấu trúc `data` là `MedicalRecordDetailRes`, `message`: `"Cập nhật chi tiết bệnh án thành công"`.

---

## 6. Cập nhật một trường OCR

### `PUT /medical-record/field/update`

**Quyền:** `medical-records:edit`

> Dùng khi nhân viên chỉnh sửa nhanh một trường đơn lẻ trong `extractedData` mà không cần gửi toàn bộ data.

**Request body:**
```json
{
    "recordId": 1,
    "fieldName": "diagnosis",
    "fieldValue": "Theo dõi sau điều trị"
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `recordId` | Long | ✅ | ID bệnh án |
| `fieldName` | string | ✅ | Tên field cần cập nhật (xem bảng bên dưới) |
| `fieldValue` | string | ❌ | Giá trị mới |

**Giá trị hợp lệ cho `fieldName`:**

| `fieldName` (snake_case hoặc camelCase) | Cập nhật field |
|-----------------------------------------|---------------|
| `facility` | `facility` |
| `department` | `department` |
| `signer_name` / `signerName` | `signerName` |
| `diagnosis` | `diagnosis` |
| *(key khác)* | Thêm vào `extra` |

> Để cập nhật thông tin bệnh nhân, dùng endpoint **5. Cập nhật chi tiết bệnh án** với field `patient`.

**Response (200):**
```json
{
    "isError": false,
    "message": "Cập nhật trường OCR thành công"
}
```

> Không có `data`.

---

## 7. Gửi bệnh án để bác sĩ duyệt

### `PUT /medical-record/{id}/submit`

**Quyền:** `medical-records:edit` (Employee hoặc Admin)

> Bệnh án phải đang ở trạng thái `EXTRACTED`. Sau khi submit sẽ chuyển sang `PENDING_DOCTOR_REVIEW`.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |

**Ví dụ:** `PUT /api/v1/medical-record/1/submit`

**Response (200):** Cấu trúc `data` là `MedicalRecordSummaryRes` với `status: "PENDING_DOCTOR_REVIEW"`, `message`: `"Gửi bệnh án để duyệt thành công"`.

**Response lỗi (400) — sai trạng thái:**
```json
{
    "isError": true,
    "message": "Bệnh án không ở trạng thái phù hợp để submit"
}
```

---

## 8. Duyệt bệnh án

### `PUT /medical-record/{id}/approve`

**Quyền:** `medical-records-approval:create` (Doctor hoặc Admin)

> Bệnh án phải đang ở trạng thái `PENDING_DOCTOR_REVIEW`. Sau khi duyệt chuyển sang `APPROVED` — đây là trạng thái cuối, không thể hoàn tác.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |

**Ví dụ:** `PUT /api/v1/medical-record/1/approve`

**Response (200):** Cấu trúc `data` là `MedicalRecordSummaryRes` với `status: "APPROVED"`, `message`: `"Duyệt bệnh án thành công"`.

---

## 9. Xóa bệnh án

### `DELETE /medical-record/{id}`

**Quyền:** `medical-records:delete` (chỉ Admin)

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |

**Ví dụ:** `DELETE /api/v1/medical-record/1`

**Response (200):**
```json
{
    "isError": false,
    "message": "Xóa bệnh án thành công"
}
```

> Không có `data`. Xóa vĩnh viễn (không phải xóa mềm).

---

## Xử lý lỗi chung

| HTTP Status | Ý nghĩa | FE xử lý |
|-------------|---------|----------|
| 400 | Dữ liệu đầu vào sai / sai trạng thái / file không hợp lệ | Hiện lỗi từ `message` |
| 401 | Chưa đăng nhập / token hết hạn | Gọi refresh-token → nếu vẫn lỗi thì logout |
| 403 | Không có quyền | Hiện thông báo "Không có quyền truy cập" |
| 404 | Không tìm thấy bệnh án | Hiện thông báo "Bệnh án không tồn tại" |
| 500 | Lỗi server | Hiện thông báo lỗi chung |

**Cấu trúc response lỗi:**
```json
{
    "isError": true,
    "message": "Mô tả lỗi cụ thể"
}
```

---

## Thứ tự gọi API điển hình

```
[Nhân viên — Tạo và xử lý bệnh án]
    POST /medical-record/upload           (upload ảnh → nhận bệnh án EXTRACTED)
    GET  /medical-record/{id}             (xem kết quả OCR)
    PUT  /medical-record/update-detail    (chỉnh sửa thông tin nếu OCR sai)
    PUT  /medical-record/field/update     (sửa nhanh từng trường đơn lẻ)
    PUT  /medical-record/{id}/submit      (gửi để bác sĩ duyệt)

[Bác sĩ / Admin — Duyệt bệnh án]
    GET  /medical-record/pending-review   (xem danh sách chờ duyệt, cũ nhất trước)
    GET  /medical-record/{id}             (xem chi tiết từng bệnh án)
    PUT  /medical-record/{id}/approve     (duyệt — bắt buộc)

[Admin — Quản lý tổng quát]
    GET  /medical-record/list             (xem tất cả, filter theo status/patientId)
    DELETE /medical-record/{id}           (xóa vĩnh viễn nếu cần)
```
