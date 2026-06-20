# CALL API — Prescription Flow (Kê toa thuốc)

> Tài liệu mô tả **đầu vào / đầu ra** của các API Medicine (danh mục thuốc) và Prescription (toa thuốc) được bổ sung sau luồng duyệt bệnh án.
>
> Base URL: `http://localhost:8080/api/v1`  
> Mọi request đều cần header: `Authorization: Bearer <token>`

---

## Tổng quan các endpoint

| # | Method | Endpoint | Quyền yêu cầu | Mô tả |
|---|--------|----------|---------------|-------|
| 1 | GET | `/medicine/list` | `medical-records:view` | Tìm kiếm thuốc trong danh mục |
| 2 | POST | `/prescription/medical-record/{recordId}` | `medical-records-approval:create` | Tạo toa thuốc từ bệnh án đã duyệt |
| 3 | GET | `/prescription/medical-record/{recordId}` | `medical-records:view` | Lấy toa thuốc theo bệnh án |
| 4 | PUT | `/prescription/{id}` | `medical-records-approval:create` | Cập nhật toa thuốc nháp |
| 5 | PUT | `/prescription/{id}/issue` | `medical-records-approval:create` | Phát hành toa thuốc |
| 6 | GET | `/prescription/{id}/print` | `medical-records:view` | Lấy dữ liệu in toa |

---

## Luồng nghiệp vụ tổng quát

```
┌─────────────────────────────────────────────────────────────┐
│  BÁC SĨ mở chi tiết bệnh án đã APPROVED                     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
         GET /prescription/medical-record/{recordId}
                       │
          ┌────────────┴────────────┐
          │                         │
     data = null               data có toa
          │                         │
          ▼                         ▼
   Nút "Tạo toa thuốc"      status = DRAFT?
          │                    ├─ DRAFT → "Tiếp tục kê toa"
          │                    └─ ISSUED → "Xem/In toa"
          │
          ▼
POST /prescription/medical-record/{recordId}
   → Tạo DRAFT, tự fill thông tin bệnh nhân
          │
          ▼
PUT /prescription/{id}
   → Bác sĩ chọn thời gian, thêm danh sách thuốc
   → Lưu nháp nhiều lần cho đến khi xong
          │
          ▼
PUT /prescription/{id}/issue
   → Phát hành toa (DRAFT → ISSUED)
          │
          ▼
GET /prescription/{id}/print
   → Lấy JSON đầy đủ để FE render màn hình in
```

---

## Trạng thái toa thuốc

```
DRAFT ──────────────────► ISSUED
  │                          │
  │  (Bác sĩ còn sửa được)   │  (Đã phát hành, không sửa được)
  └─► PUT /prescription/{id} └─► GET /prescription/{id}/print
```

| Status | Ý nghĩa |
|--------|---------|
| `DRAFT` | Toa mới tạo, bác sĩ còn chỉnh sửa được |
| `ISSUED` | Toa đã phát hành, dùng để in |

---

## 1. Tìm kiếm thuốc trong danh mục

### `GET /medicine/list`

**Quyền:** `medical-records:view`

> Tìm kiếm thuốc theo tên, mã hoặc hàm lượng. Chỉ trả thuốc đang hoạt động (`isActive = true`). Bác sĩ gõ từ khóa vào ô search, FE gọi API này để hiển thị gợi ý.

**Query params:**

| Param | Type | Mặc định | Mô tả |
|-------|------|----------|-------|
| `q` | string | `null` | Từ khóa tìm kiếm (tên, mã, hàm lượng) |
| `page` | integer | `1` | Trang hiện tại (1-indexed) |
| `limit` | integer | `10` | Số bản ghi mỗi trang |

**Ví dụ:**
```
GET /api/v1/medicine/list?q=para&page=1&limit=20
```

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy danh sách thuốc thành công",
    "data": {
        "items": [
            {
                "id": 1,
                "code": "MED-0001",
                "name": "Paracetamol",
                "strength": "500mg",
                "unit": "viên",
                "dosageForm": "viên nén",
                "description": "Hạ sốt, giảm đau"
            },
            {
                "id": 2,
                "code": "MED-0002",
                "name": "Paracetamol",
                "strength": "650mg",
                "unit": "viên",
                "dosageForm": "viên sủi",
                "description": "Hạ sốt, giảm đau (dạng sủi)"
            }
        ],
        "currentPage": 1,
        "limit": 20,
        "totalItems": 2,
        "totalPage": 1
    }
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID thuốc trong danh mục |
| `code` | string | Mã thuốc (VD: `MED-0001`) |
| `name` | string | Tên thuốc |
| `strength` | string | Hàm lượng (VD: `500mg`) |
| `unit` | string | Đơn vị cấp (VD: `viên`, `gói`, `chai`) |
| `dosageForm` | string | Dạng bào chế (VD: `viên nén`, `siro`) |
| `description` | string | Mô tả ngắn |

**Quy tắc FE khi search thuốc:**
- Bác sĩ gõ tên vào ô search → FE gọi `/medicine/list?q=...`
- Nếu chọn kết quả: FE fill `medicineId`, `medicineName`, `strength`, `unit` vào dòng toa
- Nếu không chọn kết quả nào: bác sĩ nhập tay `medicineName` (không cần `medicineId`)

---

## 2. Tạo toa thuốc từ bệnh án

### `POST /prescription/medical-record/{recordId}`

**Quyền:** `medical-records-approval:create`

> Tạo toa thuốc DRAFT cho bệnh án đã `APPROVED`. Hệ thống tự fill thông tin bệnh nhân, bệnh viện, chẩn đoán từ bệnh án. Nếu toa đã tồn tại, trả về toa hiện có (không tạo trùng).

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `recordId` | Long | ID bệnh án |

**Request body:** Không có

**Ví dụ:**
```
POST /api/v1/prescription/medical-record/10
```

**Tự động fill khi tạo toa:**

| Field toa | Lấy từ đâu |
|-----------|-----------|
| `hospitalName` | `medicalRecord.extractedData.facility` (nếu có) |
| `receiverName` | `patient.name` |
| `insuranceCode` | `patient.bhyt` |
| `receiverAddress` | `patient.address` |
| `diagnosis` | `medicalRecord.extractedData.diagnosis` (nếu có) |
| `doctorId` / `doctorName` | Tài khoản đang đăng nhập |

**Response (200):**
```json
{
    "isError": false,
    "message": "Tạo toa thuốc thành công",
    "data": {
        "id": 1,
        "prescriptionNumber": "PRE-2026-000001",
        "medicalRecordId": 10,
        "patientId": 5,
        "doctorId": 2,
        "doctorName": "Trần Văn B",
        "status": "DRAFT",
        "hospitalName": "Bệnh viện Đa khoa ABC",
        "receiverName": "Nguyễn Văn A",
        "insuranceCode": "GD4030000123456",
        "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
        "diagnosis": "Viêm họng cấp",
        "durationOption": null,
        "durationDays": null,
        "advice": null,
        "issuedAt": null,
        "createdAt": "2026-06-17T14:00:00",
        "items": []
    }
}
```

**Response lỗi — bệnh án chưa duyệt (400):**
```json
{
    "isError": true,
    "message": "Bệnh án chưa được duyệt nên không thể tạo toa thuốc"
}
```

**Response lỗi — bệnh án chưa có bệnh nhân (400):**
```json
{
    "isError": true,
    "message": "Bệnh án chưa có thông tin bệnh nhân"
}
```

---

## 3. Lấy toa thuốc theo bệnh án

### `GET /prescription/medical-record/{recordId}`

**Quyền:** `medical-records:view`

> Lấy toa thuốc của bệnh án. Nếu chưa có toa, trả HTTP 200 với `data = null` (không dùng 404) để FE phân biệt "chưa tạo toa" với "lỗi server".

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `recordId` | Long | ID bệnh án |

**Ví dụ:**
```
GET /api/v1/prescription/medical-record/10
```

**Response (200) — Đã có toa:**
```json
{
    "isError": false,
    "message": "Lấy toa thuốc thành công",
    "data": {
        "id": 1,
        "prescriptionNumber": "PRE-2026-000001",
        "medicalRecordId": 10,
        "patientId": 5,
        "doctorId": 2,
        "doctorName": "Trần Văn B",
        "status": "DRAFT",
        "hospitalName": "Bệnh viện Đa khoa ABC",
        "receiverName": "Nguyễn Văn A",
        "insuranceCode": "GD4030000123456",
        "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
        "diagnosis": "Viêm họng cấp",
        "durationOption": "TWO_WEEKS",
        "durationDays": 14,
        "advice": null,
        "issuedAt": null,
        "createdAt": "2026-06-17T14:00:00",
        "items": []
    }
}
```

**Response (200) — Chưa có toa:**
```json
{
    "isError": false,
    "message": "Lấy toa thuốc thành công",
    "data": null
}
```

**Quy tắc FE xử lý response:**

| `data` | `status` | Nút hiển thị |
|--------|----------|--------------|
| `null` | — | `Tạo toa thuốc` |
| có | `DRAFT` | `Tiếp tục kê toa` |
| có | `ISSUED` | `Xem/In toa` |

---

## 4. Cập nhật toa thuốc nháp

### `PUT /prescription/{id}`

**Quyền:** `medical-records-approval:create`

> Cập nhật toa thuốc đang ở trạng thái `DRAFT`. Gửi toàn bộ danh sách `items`, hệ thống sẽ xóa items cũ và thay bằng items mới. Có thể lưu nháp nhiều lần.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID toa thuốc |

**Request body — snake_case:**
```json
{
    "hospital_name": "Bệnh viện Đa khoa ABC",
    "receiver_name": "Nguyễn Văn A",
    "insurance_code": "GD4030000123456",
    "receiver_address": "123 Lê Lợi, Quận 1, TP.HCM",
    "diagnosis": "Viêm họng cấp",
    "duration_option": "TWO_WEEKS",
    "duration_days": null,
    "advice": "Uống nhiều nước, tái khám nếu sốt trên 3 ngày",
    "items": [
        {
            "medicine_id": 1,
            "medicine_name": "Paracetamol",
            "strength": "500mg",
            "unit": "viên",
            "quantity": 20,
            "morning_dose": "1 viên",
            "noon_dose": null,
            "afternoon_dose": null,
            "evening_dose": "1 viên",
            "instruction": "Uống sau ăn",
            "sort_order": 0
        },
        {
            "medicine_id": null,
            "medicine_name": "Thuốc ngậm họng ABC",
            "strength": null,
            "unit": "hộp",
            "quantity": 1,
            "morning_dose": "1 viên",
            "noon_dose": "1 viên",
            "afternoon_dose": null,
            "evening_dose": "1 viên",
            "instruction": "Ngậm trong miệng, không nuốt ngay",
            "sort_order": 1
        }
    ]
}
```

**Giải thích request body:**

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `hospital_name` | string | ❌ | Tên bệnh viện/phòng khám |
| `receiver_name` | string | ❌ | Tên bệnh nhân/người nhận |
| `insurance_code` | string | ❌ | Mã BHYT |
| `receiver_address` | string | ❌ | Địa chỉ bệnh nhân |
| `diagnosis` | string | ❌ | Chẩn đoán |
| `duration_option` | string | ❌ | `ONE_WEEK` / `TWO_WEEKS` / `THREE_WEEKS` / `ONE_MONTH` / `CUSTOM` |
| `duration_days` | integer | ❌* | Số ngày, bắt buộc khi `duration_option = CUSTOM` |
| `advice` | string | ❌ | Lời dặn bác sĩ |
| `items` | array | ❌ | Danh sách thuốc (null = giữ nguyên items cũ) |

**Giải thích item:**

| Field | Type | Mô tả |
|-------|------|-------|
| `medicine_id` | Long \| null | Có nếu chọn từ danh mục, null nếu nhập tay |
| `medicine_name` | string | Tên thuốc (luôn lưu để in toa) |
| `strength` | string | Hàm lượng |
| `unit` | string | Đơn vị |
| `quantity` | integer | Số lượng |
| `morning_dose` | string \| null | Liều sáng (VD: `1 viên`) |
| `noon_dose` | string \| null | Liều trưa |
| `afternoon_dose` | string \| null | Liều chiều |
| `evening_dose` | string \| null | Liều tối |
| `instruction` | string | Cách dùng (VD: `Uống sau ăn`) |
| `sort_order` | integer | Thứ tự hiển thị |

**`duration_option` → `duration_days` tự động:**

| `duration_option` | `duration_days` |
|-------------------|----------------|
| `ONE_WEEK` | 7 |
| `TWO_WEEKS` | 14 |
| `THREE_WEEKS` | 21 |
| `ONE_MONTH` | 30 |
| `CUSTOM` | Lấy từ `duration_days` trong request |

**Ví dụ:**
```
PUT /api/v1/prescription/1
```

**Response (200):**
```json
{
    "isError": false,
    "message": "Cập nhật toa thuốc thành công",
    "data": {
        "id": 1,
        "prescriptionNumber": "PRE-2026-000001",
        "medicalRecordId": 10,
        "patientId": 5,
        "doctorId": 2,
        "doctorName": "Trần Văn B",
        "status": "DRAFT",
        "hospitalName": "Bệnh viện Đa khoa ABC",
        "receiverName": "Nguyễn Văn A",
        "insuranceCode": "GD4030000123456",
        "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
        "diagnosis": "Viêm họng cấp",
        "durationOption": "TWO_WEEKS",
        "durationDays": 14,
        "advice": "Uống nhiều nước, tái khám nếu sốt trên 3 ngày",
        "issuedAt": null,
        "createdAt": "2026-06-17T14:00:00",
        "items": [
            {
                "id": 1,
                "medicineId": 1,
                "medicineName": "Paracetamol",
                "strength": "500mg",
                "unit": "viên",
                "quantity": 20,
                "morningDose": "1 viên",
                "noonDose": null,
                "afternoonDose": null,
                "eveningDose": "1 viên",
                "instruction": "Uống sau ăn",
                "sortOrder": 0
            },
            {
                "id": 2,
                "medicineId": null,
                "medicineName": "Thuốc ngậm họng ABC",
                "strength": null,
                "unit": "hộp",
                "quantity": 1,
                "morningDose": "1 viên",
                "noonDose": "1 viên",
                "afternoonDose": null,
                "eveningDose": "1 viên",
                "instruction": "Ngậm trong miệng, không nuốt ngay",
                "sortOrder": 1
            }
        ]
    }
}
```

> **Lưu ý response:** JSON trả về dùng `camelCase` (`medicineName`, `morningDose`, `durationOption`, ...) dù request gửi vào là `snake_case`.

**Response lỗi — toa đã phát hành (400):**
```json
{
    "isError": true,
    "message": "Toa thuốc đã được phát hành, không thể chỉnh sửa"
}
```

**Response lỗi — CUSTOM không có số ngày (400):**
```json
{
    "isError": true,
    "message": "Số ngày dùng thuốc không hợp lệ"
}
```

---

## 5. Phát hành toa thuốc

### `PUT /prescription/{id}/issue`

**Quyền:** `medical-records-approval:create`

> Chuyển toa từ `DRAFT` sang `ISSUED`. Sau khi phát hành, toa không được sửa. Hệ thống validate đầy đủ trước khi phát hành.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID toa thuốc |

**Request body:** Không có

**Ví dụ:**
```
PUT /api/v1/prescription/1/issue
```

**Điều kiện để phát hành:**

| Điều kiện | Lỗi nếu vi phạm |
|-----------|----------------|
| Toa phải ở `DRAFT` | `Toa thuốc đã được phát hành, không thể chỉnh sửa` |
| Có `hospitalName` | `Vui lòng nhập tên bệnh viện` |
| Có `receiverName` | `Vui lòng nhập tên người nhận` |
| Có `diagnosis` | `Vui lòng nhập chẩn đoán` |
| Có `durationDays` | `Vui lòng chọn thời gian dùng thuốc` |
| Có ít nhất 1 thuốc | `Toa thuốc phải có ít nhất một thuốc trước khi phát hành` |
| Mỗi thuốc: `medicineName` không rỗng | `Tên thuốc không được để trống` |
| Mỗi thuốc: `quantity > 0` | `Số lượng thuốc phải lớn hơn 0` |
| Mỗi thuốc: có ít nhất 1 trong sáng/trưa/chiều/tối | `Vui lòng chọn ít nhất một thời điểm dùng thuốc` |

**Response (200):**
```json
{
    "isError": false,
    "message": "Phát hành toa thuốc thành công",
    "data": {
        "id": 1,
        "prescriptionNumber": "PRE-2026-000001",
        "medicalRecordId": 10,
        "patientId": 5,
        "doctorId": 2,
        "doctorName": "Trần Văn B",
        "status": "ISSUED",
        "hospitalName": "Bệnh viện Đa khoa ABC",
        "receiverName": "Nguyễn Văn A",
        "insuranceCode": "GD4030000123456",
        "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
        "diagnosis": "Viêm họng cấp",
        "durationOption": "TWO_WEEKS",
        "durationDays": 14,
        "advice": "Uống nhiều nước, tái khám nếu sốt trên 3 ngày",
        "issuedAt": "2026-06-17T14:30:00",
        "createdAt": "2026-06-17T14:00:00",
        "items": [
            {
                "id": 1,
                "medicineId": 1,
                "medicineName": "Paracetamol",
                "strength": "500mg",
                "unit": "viên",
                "quantity": 20,
                "morningDose": "1 viên",
                "noonDose": null,
                "afternoonDose": null,
                "eveningDose": "1 viên",
                "instruction": "Uống sau ăn",
                "sortOrder": 0
            }
        ]
    }
}
```

---

## 6. Lấy dữ liệu in toa

### `GET /prescription/{id}/print`

**Quyền:** `medical-records:view`

> Trả về đầy đủ thông tin để FE render màn hình in. Chỉ hoạt động với toa đã `ISSUED`. Backend chỉ trả JSON; FE dùng `window.print()` để in.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID toa thuốc |

**Ví dụ:**
```
GET /api/v1/prescription/1/print
```

**Response (200):**
```json
{
    "isError": false,
    "message": "Lấy dữ liệu in toa thành công",
    "data": {
        "prescriptionNumber": "PRE-2026-000001",
        "hospitalName": "Bệnh viện Đa khoa ABC",
        "receiverName": "Nguyễn Văn A",
        "insuranceCode": "GD4030000123456",
        "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
        "diagnosis": "Viêm họng cấp",
        "doctorName": "Trần Văn B",
        "durationDays": 14,
        "advice": "Uống nhiều nước, tái khám nếu sốt trên 3 ngày",
        "issuedAt": "2026-06-17T14:30:00",
        "items": [
            {
                "id": 1,
                "medicineId": 1,
                "medicineName": "Paracetamol",
                "strength": "500mg",
                "unit": "viên",
                "quantity": 20,
                "morningDose": "1 viên",
                "noonDose": null,
                "afternoonDose": null,
                "eveningDose": "1 viên",
                "instruction": "Uống sau ăn",
                "sortOrder": 0
            },
            {
                "id": 2,
                "medicineId": null,
                "medicineName": "Vitamin C",
                "strength": "500mg",
                "unit": "viên",
                "quantity": 14,
                "morningDose": "1 viên",
                "noonDose": null,
                "afternoonDose": null,
                "eveningDose": null,
                "instruction": "Uống buổi sáng",
                "sortOrder": 1
            }
        ]
    }
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `prescriptionNumber` | string | Mã toa (VD: `PRE-2026-000001`) |
| `hospitalName` | string | Tên bệnh viện in trên toa |
| `receiverName` | string | Tên bệnh nhân |
| `insuranceCode` | string | Mã BHYT |
| `receiverAddress` | string | Địa chỉ bệnh nhân |
| `diagnosis` | string | Chẩn đoán |
| `doctorName` | string | Tên bác sĩ kê toa |
| `durationDays` | integer | Số ngày dùng thuốc |
| `advice` | string | Lời dặn bác sĩ |
| `issuedAt` | LocalDateTime | Ngày giờ phát hành toa |
| `items` | array | Danh sách thuốc |

**Response lỗi — toa chưa phát hành (400):**
```json
{
    "isError": true,
    "message": "Toa thuốc chưa được phát hành"
}
```

---

## Mẫu toa khi FE render để in

```
BỆNH VIỆN ĐA KHOA ABC
─────────────────────────────────────────────────

                    TOA THUỐC
        Mã toa: PRE-2026-000001
        Ngày lập: 17/06/2026

Người nhận : Nguyễn Văn A
Mã bảo hiểm: GD4030000123456
Địa chỉ    : 123 Lê Lợi, Quận 1, TP.HCM
Chẩn đoán  : Viêm họng cấp
Bác sĩ     : Trần Văn B

Danh mục thuốc chỉ định:
1. Paracetamol 500mg
   Số lượng  : 20 viên
   Thời điểm : Sáng 1 viên, Tối 1 viên
   Cách dùng : Uống sau ăn

2. Vitamin C 500mg
   Số lượng  : 14 viên
   Thời điểm : Sáng 1 viên
   Cách dùng : Uống buổi sáng

Lời dặn:
Uống nhiều nước, tái khám nếu sốt trên 3 ngày.

                              Bác sĩ kê toa
                              Trần Văn B
```

---

## Thứ tự gọi API điển hình

### Scenario 1: Bác sĩ tạo và phát hành toa thuốc

```
[Bác sĩ]
  GET /prescription/medical-record/10
    ✓ data = null → hiện nút "Tạo toa thuốc"

  POST /prescription/medical-record/10
    ✓ Tạo toa DRAFT
    ✓ Tự fill: tên bệnh nhân, BHYT, địa chỉ, chẩn đoán

  GET /medicine/list?q=para
    ✓ Tìm Paracetamol → nhận id=1, strength="500mg", unit="viên"

  GET /medicine/list?q=vitamin
    ✓ Tìm Vitamin C → nhận id=3

  PUT /prescription/1
    Body: duration_option="TWO_WEEKS", items=[Paracetamol, Vitamin C]
    ✓ Lưu nháp, durationDays tự tính = 14

  [Bác sĩ xem lại, thêm lời dặn]

  PUT /prescription/1
    Body: advice="Uống nhiều nước...", items=[...]
    ✓ Lưu nháp lần 2

  PUT /prescription/1/issue
    ✓ status → ISSUED, issuedAt = now

  GET /prescription/1/print
    ✓ Lấy JSON đầy đủ → FE render màn hình in → window.print()
```

### Scenario 2: Bác sĩ nhập thuốc không có trong danh mục

```
[Bác sĩ]
  GET /medicine/list?q=thuoc-ngam-hong
    ✓ items = [] → không tìm thấy

  [Bác sĩ nhập tay "Thuốc ngậm họng ABC"]

  PUT /prescription/1
    Body: items=[
      { medicine_id: null, medicine_name: "Thuốc ngậm họng ABC", ... }
    ]
    ✓ medicineId = null → thuốc nhập tay, lưu tên trực tiếp
```

### Scenario 3: FE mở chi tiết bệnh án APPROVED

```
[FE]
  GET /prescription/medical-record/{recordId}

  Nếu data = null:
    → Hiện nút "Tạo toa thuốc"

  Nếu data.status = "DRAFT":
    → Hiện nút "Tiếp tục kê toa"
    → Gọi PUT /prescription/{id} để mở lại form

  Nếu data.status = "ISSUED":
    → Hiện nút "Xem/In toa"
    → Gọi GET /prescription/{id}/print để render
```

---

## Xử lý lỗi chung

| HTTP Status | Ý nghĩa | Xử lý |
|-------------|---------|-------|
| 200 | OK | ✓ Thành công |
| 400 | Bad Request | Sai nghiệp vụ — xem `message` |
| 401 | Unauthorized | Token hết hạn → refresh-token |
| 403 | Forbidden | Không có quyền |
| 500 | Server Error | Lỗi server |

**Cấu trúc lỗi:**
```json
{
    "isError": true,
    "message": "Mô tả lỗi cụ thể"
}
```

**Danh sách lỗi nghiệp vụ:**

| Thông báo lỗi | Nguyên nhân |
|---------------|------------|
| `Bệnh án chưa được duyệt nên không thể tạo toa thuốc` | Bệnh án chưa ở trạng thái `APPROVED` |
| `Bệnh án chưa có thông tin bệnh nhân` | Bệnh án chưa được gắn `patientId` |
| `Toa thuốc đã được phát hành, không thể chỉnh sửa` | Gọi PUT khi toa đã `ISSUED` |
| `Số ngày dùng thuốc không hợp lệ` | `duration_option = CUSTOM` nhưng `duration_days` null hoặc < 1 hoặc > 365 |
| `Vui lòng nhập tên bệnh viện` | Issue mà `hospitalName` trống |
| `Vui lòng nhập tên người nhận` | Issue mà `receiverName` trống |
| `Vui lòng nhập chẩn đoán` | Issue mà `diagnosis` trống |
| `Vui lòng chọn thời gian dùng thuốc` | Issue mà `durationDays` null |
| `Toa thuốc phải có ít nhất một thuốc trước khi phát hành` | Issue mà `items` rỗng |
| `Tên thuốc không được để trống` | Item có `medicineName` rỗng |
| `Số lượng thuốc phải lớn hơn 0` | Item có `quantity <= 0` |
| `Vui lòng chọn ít nhất một thời điểm dùng thuốc` | Item không có sáng/trưa/chiều/tối nào |
| `Toa thuốc chưa được phát hành` | GET print khi toa vẫn `DRAFT` |

---

## Notes

1. **Request snake_case, Response camelCase** — Request body gửi lên dùng `snake_case` (`medicine_name`, `morning_dose`, `duration_option`). Response trả về dùng `camelCase` (`medicineName`, `morningDose`, `durationOption`).
2. **Items replace toàn bộ** — Mỗi lần gọi `PUT /prescription/{id}`, nếu `items` không null, toàn bộ items cũ bị xóa và thay bằng items mới. FE phải gửi lại toàn bộ danh sách.
3. **Thuốc nhập tay** — `medicine_id = null` hoàn toàn hợp lệ. Backend không yêu cầu thuốc phải có trong danh mục.
4. **Một bệnh án chỉ có một toa** — Gọi `POST` lần 2 cho cùng `recordId` sẽ trả về toa đã tạo, không tạo trùng.
5. **Lưu snapshot** — Khi toa được phát hành, thông tin bệnh nhân đã được lưu trực tiếp vào toa (`receiverName`, `insuranceCode`, `receiverAddress`). Dù bệnh nhân cập nhật thông tin sau này, toa vẫn giữ nguyên nội dung tại thời điểm kê.
6. **Pagination 1-indexed** — `page` bắt đầu từ `1`. Gọi `page=0` → 400 Bad Request.
7. **Print là JSON** — Backend không tạo PDF. FE nhận JSON từ `GET /prescription/{id}/print` và tự render màn hình in, dùng `window.print()`.
