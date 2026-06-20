# CALL API — Patient Flow

> Mô tả **đầu vào / đầu ra** của toàn bộ API quản lý bệnh nhân.  
> Base URL: `http://localhost:8080/api/v1`  
> Mọi request đều cần header: `Authorization: Bearer <token>`

---

## Tổng quan các endpoint

| # | Method | Endpoint | Quyền yêu cầu | Mô tả |
|---|--------|----------|---------------|-------|
| 1 | GET | `/patient/search?bhyt=...` | `patient-search:view` | **Chi tiết bệnh nhân** — tra cứu theo BHYT, trả về thông tin bệnh nhân + danh sách bệnh án tóm tắt |
| 2 | GET | `/patient/list` | `patient-search:view` | Danh sách bệnh nhân (tìm kiếm, phân trang) |
| 3 | POST | `/patient/create` | `patient-search:create` | Tạo hồ sơ bệnh nhân mới |
| 4 | PUT | `/patient/update/{id}` | `patient-search:edit` | Cập nhật thông tin bệnh nhân |

---

## Cấu trúc response dùng chung

### PatientRes

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh nhân |
| `bhyt` | string | Mã thẻ BHYT (duy nhất) |
| `name` | string | Họ tên |
| `dob` | string \| null | Ngày sinh (`yyyy-MM-dd`) |
| `gender` | string \| null | Giới tính |
| `address` | string \| null | Địa chỉ |
| `phone` | string \| null | Số điện thoại |
| `createdAt` | LocalDateTime | Thời điểm tạo |
| `updatedAt` | LocalDateTime \| null | Thời điểm cập nhật gần nhất |

### MedicalRecordSummaryPatient (dùng trong search)

| Field | Type | Mô tả |
|-------|------|-------|
| `patient` | PatientRes | Thông tin bệnh nhân |
| `records` | `Item[]` | Danh sách bệnh án gần nhất (tối đa 20, mới nhất trước) |
| `totalRecords` | Long | Tổng số bệnh án của bệnh nhân |

#### MedicalRecordSummaryPatient.Item

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh án |
| `recordNumber` | string | Mã bệnh án |
| `status` | string | Trạng thái (`PROCESSING`, `PENDING_REVIEW`, `APPROVED`, ...) |
| `department` | string \| null | Khoa/phòng |
| `signerName` | string \| null | Tên người ký (từ dữ liệu OCR) |
| `diagnosis` | string \| null | Chẩn đoán (từ dữ liệu OCR) |

---

## 1. Chi tiết bệnh nhân (tra cứu theo BHYT)

### `GET /patient/search`

**Quyền:** `patient-search:view`

> Đây là endpoint "detail" của bệnh nhân — thay vì tra cứu theo `id`, hệ thống dùng `bhyt` làm định danh chính vì BHYT là mã duy nhất và có ý nghĩa nghiệp vụ. Trả về thông tin bệnh nhân kèm danh sách bệnh án tóm tắt (nhẹ). Để xem chi tiết từng bệnh án gọi `GET /medical-record/{id}`.

**Query param:**

| Param | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `bhyt` | string | ✅ | Số thẻ BHYT cần tra cứu (khớp chính xác) |

**Ví dụ:** `GET /api/v1/patient/search?bhyt=GD4030000123456`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy thông tin bệnh nhân thành công",
    "data": {
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
        "records": [
            {
                "id": 2,
                "recordNumber": "REC-2026-000002",
                "status": "APPROVED",
                "department": "Xét nghiệm",
                "signerName": "BS. Trần Văn B",
                "diagnosis": "Thiếu máu nhẹ"
            },
            {
                "id": 1,
                "recordNumber": "REC-2026-000001",
                "status": "APPROVED",
                "department": "Nội tổng quát",
                "signerName": "BS. Nguyễn Thị C",
                "diagnosis": "Viêm họng cấp"
            }
        ],
        "totalRecords": 2
    }
}
```

**Response lỗi (400) — không tìm thấy:**
```json
{
    "isError": true,
    "message": "Không tìm thấy bệnh nhân"
}
```

---

## 2. Danh sách bệnh nhân

### `GET /patient/list`

**Quyền:** `patient-search:view`

**Query params:**

| Param | Type | Mặc định | Mô tả |
|-------|------|----------|-------|
| `q` | string | — | Tìm kiếm theo tên hoặc mã BHYT (tìm gần đúng) |
| `page` | integer | `1` | Trang hiện tại |
| `limit` | integer | `20` | Số bản ghi mỗi trang |

> Kết quả sắp xếp theo `name ASC`.

**Ví dụ:** `GET /api/v1/patient/list?q=Nguyễn&page=1&limit=20`

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy danh sách bệnh nhân thành công",
    "data": {
        "items": [
            {
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
            {
                "id": 2,
                "bhyt": "GD4030000654321",
                "name": "Trần Thị B",
                "dob": "1985-08-20",
                "gender": "Nữ",
                "address": "456 Nguyễn Huệ, Quận 1, TP.HCM",
                "phone": "0912345678",
                "createdAt": "2026-02-01T08:00:00",
                "updatedAt": null
            }
        ],
        "currentPage": 1,
        "limit": 20,
        "totalItems": 2,
        "totalPage": 1
    }
}
```

**Cấu trúc `data` (PagedResponse):**

| Field | Type | Mô tả |
|-------|------|-------|
| `items` | `PatientRes[]` | Danh sách bệnh nhân trang hiện tại |
| `currentPage` | integer | Trang hiện tại |
| `limit` | integer | Số bản ghi mỗi trang |
| `totalItems` | Long | Tổng số bản ghi |
| `totalPage` | integer | Tổng số trang |

---

## 3. Tạo bệnh nhân mới

### `POST /patient/create`

**Quyền:** `patient-search:create`

> Nếu đã tồn tại bệnh nhân với cùng `bhyt`, trả về bệnh nhân đó thay vì tạo mới (find-or-create).

**Request body:**
```json
{
    "bhyt": "GD4030000123456",
    "name": "Nguyễn Văn A",
    "dob": "1990-05-15",
    "gender": "Nam",
    "address": "123 Lê Lợi, Quận 1, TP.HCM",
    "phone": "0901234567"
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `bhyt` | string | ✅ | Mã thẻ BHYT |
| `name` | string | ✅ | Họ tên bệnh nhân |
| `dob` | string | ❌ | Ngày sinh (`yyyy-MM-dd`) |
| `gender` | string | ❌ | Giới tính |
| `address` | string | ❌ | Địa chỉ |
| `phone` | string | ❌ | Số điện thoại |

**Response (200):** Cấu trúc `data` là `PatientRes`, `message`: `"Tạo bệnh nhân thành công"`.

```json
{
    "isError": false,
    "message": "Tạo bệnh nhân thành công",
    "data": {
        "id": 1,
        "bhyt": "GD4030000123456",
        "name": "Nguyễn Văn A",
        "dob": "1990-05-15",
        "gender": "Nam",
        "address": "123 Lê Lợi, Quận 1, TP.HCM",
        "phone": "0901234567",
        "createdAt": "2026-06-04T09:00:00",
        "updatedAt": null
    }
}
```

**Response lỗi (400) — thiếu field bắt buộc:**
```json
{
    "isError": true,
    "message": "bhyt không được để trống"
}
```

---

## 4. Cập nhật thông tin bệnh nhân

### `PUT /patient/update/{id}`

**Quyền:** `patient-search:edit`

> `{id}` trong URL là ID bệnh nhân. `id` trong body phải khớp.  
> Ghi đè toàn bộ — tất cả field đều được cập nhật, kể cả các field nullable.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID bệnh nhân cần cập nhật |

**Request body:**
```json
{
    "id": 1,
    "bhyt": "GD4030000123456",
    "name": "Nguyễn Văn A",
    "dob": "1990-05-15",
    "gender": "Nam",
    "address": "456 Lê Duẩn, Quận 1, TP.HCM",
    "phone": "0901234567"
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `id` | Long | ✅ | ID bệnh nhân |
| `bhyt` | string | ✅ | Mã thẻ BHYT (nếu đổi sang BHYT đã tồn tại → lỗi 400) |
| `name` | string | ✅ | Họ tên bệnh nhân |
| `dob` | string | ❌ | Ngày sinh (`yyyy-MM-dd`) |
| `gender` | string | ❌ | Giới tính |
| `address` | string | ❌ | Địa chỉ |
| `phone` | string | ❌ | Số điện thoại |

**Response (200):** Cấu trúc `data` là `PatientRes`, `message`: `"Cập nhật thông tin bệnh nhân thành công"`.

**Response lỗi (400) — BHYT đã tồn tại:**
```json
{
    "isError": true,
    "message": "Số thẻ BHYT đã tồn tại trong hệ thống"
}
```

**Response lỗi (400) — không tìm thấy:**
```json
{
    "isError": true,
    "message": "Không tìm thấy bệnh nhân"
}
```

---

## Xử lý lỗi chung

| HTTP Status | Ý nghĩa | FE xử lý |
|-------------|---------|----------|
| 400 | Dữ liệu đầu vào sai / BHYT trùng / ngày sinh sai format | Hiện lỗi từ `message` |
| 401 | Chưa đăng nhập / token hết hạn | Gọi refresh-token → nếu vẫn lỗi thì logout |
| 403 | Không có quyền | Hiện thông báo "Không có quyền truy cập" |
| 404 | Không tìm thấy bệnh nhân | Hiện thông báo lỗi |
| 500 | Lỗi server | Hiện thông báo lỗi chung |

---

## Thứ tự gọi API điển hình

```
[Tìm kiếm bệnh nhân]
    GET /patient/search?bhyt=...         (tra cứu theo BHYT → thông tin bệnh nhân + danh sách bệnh án tóm tắt)
    GET /patient/list?q=...              (tìm theo tên hoặc BHYT → chọn bệnh nhân)

[Xem chi tiết bệnh án]
    GET /medical-record/{id}             (xem toàn bộ dữ liệu 1 bệnh án cụ thể)

[Quản lý bệnh nhân]
    POST /patient/create                 (tạo mới nếu chưa có trong hệ thống)
    PUT  /patient/update/{id}            (cập nhật thông tin cá nhân)
```
