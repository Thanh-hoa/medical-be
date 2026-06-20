# Kế hoạch triển khai luồng toa thuốc sau khi bác sĩ duyệt bệnh án

## 1. Mục tiêu

Hiện tại hệ thống đang dừng ở luồng:

```text
Upload bệnh án -> OCR -> Nhân viên kiểm tra -> Submit -> Bác sĩ approve -> Kết thúc
```

Điểm yếu của luồng này là sau khi bác sĩ duyệt bệnh án thì không còn nghiệp vụ y tế tiếp theo. Mục tiêu của task là bổ sung luồng kê toa:

```text
Bác sĩ approve bệnh án
-> Bác sĩ bấm "Tạo toa thuốc"
-> Chọn thời gian dùng thuốc
-> Search thuốc trong danh mục hoặc nhập tay nếu không có
-> Lưu nháp / Phát hành toa
-> In toa cho bệnh nhân đem đi lãnh thuốc
```

Yêu cầu mới:

- Bác sĩ có thể search thuốc trong danh mục.
- Nếu search không có thuốc phù hợp thì bác sĩ được nhập tay tên thuốc.
- Toa thuốc khi in phải có thông tin:
  - Tên bệnh viện
  - Tên người nhận / bệnh nhân
  - Mã bảo hiểm
  - Địa chỉ
  - Chẩn đoán
  - Tên bác sĩ
  - Danh mục thuốc chỉ định
  - Lời dặn
  - Ngày lập toa hiện tại

Phiên bản này vẫn phù hợp với 5 tiếng nếu chỉ làm danh mục thuốc cơ bản và search, chưa làm tồn kho/cấp phát/PDF backend.

## 2. Phạm vi MVP trong 5 tiếng

### 2.1 Làm trong task này

- Thêm module `Prescription` để quản lý toa thuốc.
- Thêm module `Medicine` để có danh mục thuốc cơ bản.
- Seed sẵn một số thuốc mẫu để bác sĩ search khi demo.
- Bác sĩ tạo toa thuốc sau khi bệnh án đã `APPROVED`.
- Bác sĩ chọn thời gian dùng thuốc:
  - 1 tuần
  - 2 tuần
  - 3 tuần
  - 1 tháng
  - tùy chỉnh số ngày
- Bác sĩ search thuốc theo tên.
- Nếu thuốc có trong danh mục: chọn thuốc, hệ thống tự fill tên, hàm lượng, đơn vị.
- Nếu không có trong danh mục: bác sĩ nhập tay tên thuốc.
- Lưu nháp toa thuốc.
- Phát hành toa thuốc.
- Lấy dữ liệu in toa thuốc.

### 2.2 Chưa làm trong task này

- Quản lý tồn kho thuốc.
- Nhập kho/xuất kho.
- Xác nhận đã cấp phát thuốc.
- Cảnh báo tương tác thuốc.
- Cảnh báo dị ứng.
- PDF backend thật.
- Chữ ký số.
- Lịch sử sửa toa.
- Hủy toa và tạo phiên bản mới.

Lý do: các phần trên tốt nhưng sẽ làm task lớn hơn nhiều. Trong 5 tiếng nên ưu tiên luồng end-to-end có thể demo.

## 3. Luồng nghiệp vụ chính

```text
1. Nhân viên upload bệnh án.
2. Hệ thống OCR và trích xuất thông tin.
3. Nhân viên kiểm tra, chỉnh sửa thông tin bệnh án.
4. Nhân viên submit bệnh án cho bác sĩ.
5. Bác sĩ xem danh sách bệnh án chờ duyệt.
6. Bác sĩ xem chi tiết bệnh án.
7. Bác sĩ approve bệnh án.
8. Bệnh án chuyển sang APPROVED.
9. Bác sĩ bấm "Tạo toa thuốc".
10. Hệ thống tạo toa DRAFT gắn với bệnh án.
11. Hệ thống mở form toa thuốc.
12. Form tự động lấy thông tin:
    - tên bệnh viện từ extractedData.facility nếu có
    - tên bệnh nhân từ patient.name
    - mã bảo hiểm từ patient.bhyt
    - địa chỉ từ patient.address
    - chẩn đoán từ extractedData.diagnosis hoặc bác sĩ sửa lại
    - tên bác sĩ từ account hiện tại
    - ngày lập toa là ngày hiện tại
13. Bác sĩ chọn thời gian dùng thuốc.
14. Bác sĩ thêm danh sách thuốc:
    - Search thuốc trong danh mục.
    - Nếu có, chọn thuốc.
    - Nếu không có, nhập tay.
15. Bác sĩ nhập số lượng, liều theo buổi sáng/trưa/chiều/tối, cách dùng.
16. Bác sĩ nhập lời dặn.
17. Bác sĩ bấm "Lưu nháp" hoặc "Phát hành toa".
18. Nếu phát hành, toa chuyển sang ISSUED.
19. Bác sĩ/nhân viên in toa cho bệnh nhân.
```

## 4. Trạng thái

### 4.1 Trạng thái bệnh án giữ nguyên

```text
EXTRACTED -> PENDING_DOCTOR_REVIEW -> APPROVED / REJECTED
```

Không thêm status mới vào `MedicalRecordStatus` trong MVP. Bệnh án đã duyệt là `APPROVED`, toa thuốc có vòng đời riêng.

### 4.2 Trạng thái toa thuốc

```text
DRAFT -> ISSUED
```

| Status | Ý nghĩa |
|---|---|
| `DRAFT` | Toa mới tạo, bác sĩ còn sửa được |
| `ISSUED` | Toa đã phát hành, dùng để in |

Quy tắc:

- Chỉ toa `DRAFT` mới được sửa.
- Toa `ISSUED` không được sửa trong bản MVP.
- Mỗi bệnh án chỉ có 1 toa thuốc trong bản MVP.

## 5. Thiết kế database

### 5.1 Bảng `medicines`

Bảng này dùng để search thuốc khi kê toa. Không quản lý tồn kho.

```sql
CREATE TABLE medicines (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    strength VARCHAR(100),
    unit VARCHAR(50),
    dosage_form VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

Giải thích:

| Field | Mô tả |
|---|---|
| `code` | Mã thuốc, ví dụ `MED-0001` |
| `name` | Tên thuốc, ví dụ `Paracetamol` |
| `strength` | Hàm lượng, ví dụ `500mg` |
| `unit` | Đơn vị cấp, ví dụ `viên`, `gói`, `chai` |
| `dosage_form` | Dạng bào chế, ví dụ `viên nén`, `siro`, `bột pha` |
| `description` | Mô tả ngắn |
| `is_active` | Thuốc còn được search hay không |

Thuốc seed mẫu nên có 15-30 dòng:

```text
Paracetamol 500mg
Amoxicillin 500mg
Vitamin C 500mg
Loratadine 10mg
Cetirizine 10mg
Omeprazole 20mg
ORS
Ibuprofen 400mg
Metformin 500mg
Salbutamol
```

### 5.2 Bảng `prescriptions`

```sql
CREATE TABLE prescriptions (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    prescription_number VARCHAR(50) UNIQUE,
    medical_record_id BIGINT NOT NULL REFERENCES medical_records(id),
    patient_id BIGINT REFERENCES patients(id),
    doctor_id BIGINT NOT NULL REFERENCES account(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',

    hospital_name VARCHAR(255),
    receiver_name VARCHAR(255),
    insurance_code VARCHAR(50),
    receiver_address TEXT,
    diagnosis TEXT,

    duration_option VARCHAR(20),
    duration_days INT,
    advice TEXT,

    issued_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

Giải thích:

| Field | Mô tả |
|---|---|
| `hospital_name` | Tên bệnh viện/phòng khám in trên toa |
| `receiver_name` | Tên người nhận/bệnh nhân |
| `insurance_code` | Mã bảo hiểm/BHYT |
| `receiver_address` | Địa chỉ bệnh nhân |
| `diagnosis` | Chẩn đoán |
| `doctor_id` | Bác sĩ kê toa |
| `duration_option` | Lựa chọn nhanh: 1 tuần, 2 tuần, 3 tuần, 1 tháng, tùy chỉnh |
| `duration_days` | Số ngày dùng thuốc thực tế |
| `advice` | Lời dặn của bác sĩ |
| `issued_at` | Ngày giờ phát hành toa |

Lý do lưu snapshot các thông tin in toa:

- Thông tin bệnh nhân có thể bị sửa sau này.
- Toa đã phát hành cần giữ nguyên thông tin tại thời điểm kê toa.
- Khi in lại toa cũ, dữ liệu không bị lệch.

### 5.3 Bảng `prescription_items`

```sql
CREATE TABLE prescription_items (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicine_id BIGINT REFERENCES medicines(id),
    medicine_name VARCHAR(255) NOT NULL,
    strength VARCHAR(100),
    unit VARCHAR(50),
    quantity INT,
    morning_dose VARCHAR(100),
    noon_dose VARCHAR(100),
    afternoon_dose VARCHAR(100),
    evening_dose VARCHAR(100),
    instruction TEXT,
    sort_order INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

Giải thích:

| Field | Mô tả |
|---|---|
| `medicine_id` | Có nếu chọn từ danh mục, null nếu bác sĩ nhập tay |
| `medicine_name` | Luôn lưu tên thuốc để in toa |
| `strength` | Hàm lượng |
| `unit` | Đơn vị |
| `quantity` | Số lượng |
| `morning_dose` | Liều buổi sáng, ví dụ `1 viên` |
| `noon_dose` | Liều buổi trưa, ví dụ `1 viên` |
| `afternoon_dose` | Liều buổi chiều, ví dụ `1 viên` |
| `evening_dose` | Liều buổi tối, ví dụ `1 viên` |
| `instruction` | Cách dùng, ví dụ `Uống sau ăn` |
| `sort_order` | Thứ tự hiển thị |

Quy tắc quan trọng:

- Nếu chọn thuốc từ danh mục: `medicineId` có giá trị.
- Nếu nhập tay: `medicineId = null`, `medicineName` là text bác sĩ nhập.
- Luôn lưu `medicineName`, `strength`, `unit` vào item để toa đã in không phụ thuộc vào danh mục thuốc sau này.

## 6. Enum cần thêm

### 6.1 `PrescriptionStatus`

```java
public enum PrescriptionStatus {
    DRAFT,
    ISSUED
}
```

### 6.2 `PrescriptionDurationOption`

```java
public enum PrescriptionDurationOption {
    ONE_WEEK,
    TWO_WEEKS,
    THREE_WEEKS,
    ONE_MONTH,
    CUSTOM
}
```

Quy đổi:

| Option | Số ngày |
|---|---:|
| `ONE_WEEK` | 7 |
| `TWO_WEEKS` | 14 |
| `THREE_WEEKS` | 21 |
| `ONE_MONTH` | 30 |
| `CUSTOM` | Lấy từ `durationDays` request |

## 7. API đề xuất

Base URL:

```text
/api/v1
```

### 7.1 Search danh mục thuốc

```text
GET /medicine/list?q=para&page=1&limit=20
```

Quyền:

```text
medical-records:view
```

Điều kiện:

- Chỉ trả thuốc `isActive = true`.
- Search theo `name`, `code`, `strength`.

Response mẫu:

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
        "dosageForm": "viên nén"
      }
    ],
    "currentPage": 1,
    "limit": 20,
    "totalItems": 1,
    "totalPage": 1
  }
}
```

### 7.2 Tạo toa thuốc từ bệnh án

```text
POST /prescription/medical-record/{recordId}
```

Quyền:

```text
medical-records-approval:create
```

Điều kiện:

- Người gọi phải là doctor hoặc admin.
- Bệnh án phải tồn tại.
- Bệnh án phải có status `APPROVED`.
- Bệnh án phải có `patientId`.
- Mỗi bệnh án chỉ có 1 toa thuốc trong MVP.
- Nếu toa đã tồn tại thì trả về toa hiện có, không tạo trùng.

Khi tạo toa, backend nên tự fill:

| Prescription field | Lấy từ đâu |
|---|---|
| `hospitalName` | `medicalRecord.extractedData.facility` nếu có |
| `receiverName` | `patient.name` |
| `insuranceCode` | `patient.bhyt` |
| `receiverAddress` | `patient.address` |
| `diagnosis` | `medicalRecord.extractedData.diagnosis` nếu có |
| `doctorId` | account hiện tại |

Response mẫu:

```json
{
  "isError": false,
  "message": "Tạo toa thuốc thành công",
  "data": {
    "id": 1,
    "prescriptionNumber": "PRE-2026-000001",
    "medicalRecordId": 10,
    "status": "DRAFT",
    "hospitalName": "Bệnh viện Đa khoa ABC",
    "receiverName": "Nguyễn Văn A",
    "insuranceCode": "GD4030000123456",
    "receiverAddress": "123 Lê Lợi, Quận 1",
    "diagnosis": "Viêm họng cấp",
    "doctorId": 2,
    "doctorName": "Trần Văn B",
    "durationOption": null,
    "durationDays": null,
    "advice": null,
    "items": []
  }
}
```

### 7.3 Lấy toa thuốc theo bệnh án

```text
GET /prescription/medical-record/{recordId}
```

Mục đích:

- FE mở chi tiết bệnh án `APPROVED` thì gọi API này.
- Nếu chưa có toa: trả HTTP 200 với `data = null`, FE hiện nút `Tạo toa thuốc`.
- Nếu có toa `DRAFT`: hiện nút `Tiếp tục kê toa`.
- Nếu có toa `ISSUED`: hiện nút `Xem/In toa`.

Lưu ý: không dùng HTTP 404 khi chưa có toa, để FE phân biệt được "chưa tạo toa" với "lỗi server".

### 7.4 Cập nhật toa thuốc nháp

```text
PUT /prescription/{id}
```

Request mẫu:

```json
{
  "hospitalName": "Bệnh viện Đa khoa ABC",
  "receiverName": "Nguyễn Văn A",
  "insuranceCode": "GD4030000123456",
  "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
  "diagnosis": "Viêm họng cấp",
  "durationOption": "TWO_WEEKS",
  "durationDays": null,
  "advice": "Uống nhiều nước, tái khám nếu sốt trên 3 ngày",
  "items": [
    {
      "medicineId": 1,
      "medicineName": "Paracetamol",
      "strength": "500mg",
      "unit": "viên",
      "quantity": 20,
      "morningDose": "1 viên",
      "noonDose": null,
      "afternoonDose": null,
      "eveningDose": "1 viên",
      "instruction": "Uống sau ăn"
    },
    {
      "medicineId": null,
      "medicineName": "Thuốc ngậm họng ABC",
      "strength": null,
      "unit": "hộp",
      "quantity": 1,
      "morningDose": "1 viên",
      "noonDose": "1 viên",
      "afternoonDose": null,
      "eveningDose": "1 viên",
      "instruction": "Ngậm trong miệng, không nuốt ngay"
    }
  ]
}
```

Ghi chú:

- Item đầu tiên là thuốc chọn từ danh mục.
- Item thứ hai là thuốc nhập tay vì `medicineId = null`.

Điều kiện:

- Chỉ doctor/admin được cập nhật.
- Chỉ cập nhật được khi toa là `DRAFT`.
- `durationOption` bắt buộc khi phát hành.
- Nếu `durationOption = CUSTOM` thì `durationDays` bắt buộc.
- Nếu `durationOption != CUSTOM` thì backend tự tính `durationDays`.
- Khi lưu nháp, có thể cho phép items rỗng.

### 7.5 Phát hành toa thuốc

```text
PUT /prescription/{id}/issue
```

Điều kiện:

- Toa phải ở status `DRAFT`.
- Toa phải có `hospitalName`.
- Toa phải có `receiverName`.
- Toa phải có `diagnosis`.
- Toa phải có `durationDays`.
- Toa phải có ít nhất 1 thuốc.
- Mỗi thuốc khi phát hành phải có:
  - `medicineName`
  - `quantity > 0`
  - ít nhất một thời điểm dùng thuốc: `morningDose`, `noonDose`, `afternoonDose`, hoặc `eveningDose`

Kết quả:

- `status = ISSUED`
- `issuedAt = now`
- Toa không được sửa nữa trong MVP.

### 7.6 Lấy dữ liệu in toa

```text
GET /prescription/{id}/print
```

Điều kiện:

- Toa phải tồn tại.
- Nên chỉ cho in khi status là `ISSUED`.

Response mẫu:

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
        "medicineName": "Paracetamol",
        "strength": "500mg",
        "unit": "viên",
        "quantity": 20,
        "morningDose": "1 viên",
        "noonDose": null,
        "afternoonDose": null,
        "eveningDose": "1 viên",
        "instruction": "Uống sau ăn"
      }
    ]
  }
}
```

Ghi chú: backend chỉ cần trả JSON. FE có thể tạo màn hình in và dùng `window.print()`. Không cần làm PDF backend trong 5 tiếng.

## 8. Ràng buộc nghiệp vụ

### 8.1 Tạo toa

- Chỉ tạo toa khi bệnh án đã `APPROVED`.
- Không tạo toa cho bệnh án `PENDING_DOCTOR_REVIEW`.
- Không tạo toa cho bệnh án `REJECTED`.
- Bệnh án phải có bệnh nhân.
- Mỗi bệnh án chỉ có 1 toa trong MVP.

### 8.2 Search và nhập thuốc

- Bác sĩ search thuốc trong danh mục bằng API `/medicine/list`.
- Nếu có thuốc: chọn thuốc và FE fill thông tin vào dòng toa.
- Nếu không có thuốc: bác sĩ nhập tay.
- Khi lưu item, `medicineId` được phép null.
- `medicineName` luôn bắt buộc khi phát hành.
- Không cần bắt buộc mọi thuốc phải nằm trong danh mục.

### 8.3 Thời gian dùng thuốc

- `ONE_WEEK` = 7 ngày.
- `TWO_WEEKS` = 14 ngày.
- `THREE_WEEKS` = 21 ngày.
- `ONE_MONTH` = 30 ngày.
- `CUSTOM` phải nhập `durationDays`.
- `durationDays` từ 1 đến 365.

### 8.4 Phát hành toa

- Toa phải có thông tin người nhận.
- Toa phải có chẩn đoán.
- Toa phải có thời gian dùng thuốc.
- Toa phải có ít nhất 1 thuốc.
- Mỗi thuốc phải có tên và số lượng.
- Mỗi thuốc phải có ít nhất một thời điểm dùng thuốc: sáng, trưa, chiều hoặc tối.
- Sau khi `ISSUED`, không sửa nữa.

### 8.5 Phân quyền

Để kịp 5 tiếng, dùng quyền hiện có:

| Hành động | Quyền |
|---|---|
| Search thuốc | `medical-records:view` |
| Tạo toa | `medical-records-approval:create` |
| Sửa toa nháp | `medical-records-approval:create` |
| Phát hành toa | `medical-records-approval:create` |
| Xem toa | `medical-records:view` |
| In toa | `medical-records:view` |

Không tạo permission module mới trong MVP để tránh sửa seeder quyền quá nhiều.

## 9. Gợi ý màn hình FE

### 9.1 Chi tiết bệnh án

Nếu `status = APPROVED`:

- Chưa có toa: hiện `Tạo toa thuốc`.
- Có toa `DRAFT`: hiện `Tiếp tục kê toa`.
- Có toa `ISSUED`: hiện `Xem/In toa`.

### 9.2 Form toa thuốc

Form nên gồm:

```text
Tên bệnh viện
Tên người nhận
Mã bảo hiểm
Địa chỉ
Chẩn đoán
Tên bác sĩ
Ngày lập toa
Thời gian dùng thuốc
Danh mục thuốc chỉ định
Lời dặn
Nút Lưu nháp
Nút Phát hành toa
Nút In toa
```

`Tên bác sĩ` và `Ngày lập toa` nên chỉ hiển thị, không cho sửa trong MVP.

### 9.3 Form dòng thuốc

Mỗi dòng thuốc:

```text
Ô search thuốc
Tên thuốc
Hàm lượng
Đơn vị
Số lượng
Sáng
Trưa
Chiều
Tối
Cách dùng
```

Quy tắc FE:

- Bác sĩ gõ tên thuốc vào ô search.
- FE gọi `/medicine/list?q=...`.
- Nếu chọn kết quả search, FE fill `medicineId`, `medicineName`, `strength`, `unit`.
- Nếu không chọn kết quả nào, FE cho bác sĩ nhập tay `medicineName`.

## 10. Mẫu toa khi in

Toa in có thể hiển thị theo bố cục:

```text
BỆNH VIỆN ĐA KHOA ABC

TOA THUỐC
Mã toa: PRE-2026-000001
Ngày lập: 17/06/2026

Người nhận: Nguyễn Văn A
Mã bảo hiểm: GD4030000123456
Địa chỉ: 123 Lê Lợi, Quận 1, TP.HCM
Chẩn đoán: Viêm họng cấp
Bác sĩ: Trần Văn B

Danh mục thuốc chỉ định:
1. Paracetamol 500mg
   Số lượng: 20 viên
   Thời điểm: Sáng 1 viên, Tối 1 viên
   Cách dùng: Uống sau ăn

2. Vitamin C 500mg
   Số lượng: 14 viên
   Thời điểm: Sáng 1 viên
   Cách dùng: Uống buổi sáng

Lời dặn:
Uống nhiều nước, tái khám nếu sốt trên 3 ngày.

Bác sĩ kê toa
Trần Văn B
```

## 11. Thứ tự triển khai trong 5 tiếng

### Giờ 1: Entity và repository

- Tạo `Medicine`.
- Tạo `Prescription`.
- Tạo `PrescriptionItem`.
- Tạo `PrescriptionStatus`.
- Tạo `PrescriptionDurationOption`.
- Tạo repositories.

### Giờ 2: Seed thuốc và API medicine

- Tạo seeder 15-30 thuốc mẫu.
- Tạo DTO `MedicineRes`.
- Tạo API `GET /medicine/list?q=`.
- Test search thuốc.

### Giờ 3: Prescription service

- Tạo DTO:
  - `PrescriptionRes`
  - `PrescriptionItemRes`
  - `UpdatePrescriptionReq`
  - `PrescriptionItemReq`
  - `PrescriptionPrintRes`
- Implement:
  - tạo toa theo bệnh án
  - lấy toa theo bệnh án
  - cập nhật toa DRAFT
  - phát hành toa
  - lấy dữ liệu in

### Giờ 4: Controller và ràng buộc

- Tạo `PrescriptionController`.
- Thêm route vào `APIRoutes`.
- Gắn `@PreAuthorize`.
- Validate:
  - chỉ tạo toa khi record `APPROVED`
  - không tạo trùng toa
  - `CUSTOM` phải có duration
  - issue phải có ít nhất 1 thuốc
  - issue phải có thông tin in toa bắt buộc

### Giờ 5: Build, docs, demo

- Chạy build.
- Test bằng Swagger/Postman:
  - search thuốc
  - tạo toa
  - update toa với thuốc có `medicineId`
  - update toa với thuốc nhập tay `medicineId = null`
  - issue toa
  - get print
- Cập nhật docs API nếu còn thời gian.

## 12. Test case cần có

### Thành công

```text
1. Search "para" -> ra Paracetamol.
2. Bệnh án APPROVED -> tạo toa DRAFT thành công.
3. Tạo toa tự fill tên bệnh nhân, BHYT, địa chỉ, chẩn đoán.
4. Cập nhật toa với duration TWO_WEEKS -> durationDays = 14.
5. Thêm 1 thuốc từ danh mục -> medicineId có giá trị.
6. Thêm 1 thuốc nhập tay -> medicineId null.
7. Issue toa -> status ISSUED.
8. GET print -> có tên bệnh viện, người nhận, BHYT, địa chỉ, chẩn đoán, bác sĩ, thuốc, lời dặn, ngày lập.
```

### Lỗi

```text
1. Bệnh án chưa APPROVED -> tạo toa lỗi.
2. Bệnh án không có patientId -> tạo toa lỗi.
3. Tạo toa lần 2 -> trả toa cũ hoặc báo đã tồn tại, không tạo trùng.
4. CUSTOM nhưng durationDays null -> lỗi.
5. durationDays = 0 -> lỗi.
6. Issue toa không có thuốc -> lỗi.
7. Issue toa có thuốc nhưng medicineName rỗng -> lỗi.
8. Issue toa có quantity <= 0 -> lỗi.
9. Issue toa có thuốc nhưng không chọn sáng/trưa/chiều/tối -> lỗi.
10. Sửa toa ISSUED -> lỗi.
```

## 13. Thông báo lỗi gợi ý

```text
Bệnh án chưa được duyệt nên không thể tạo toa thuốc
Bệnh án chưa có thông tin bệnh nhân
Toa thuốc đã tồn tại cho bệnh án này
Toa thuốc đã được phát hành, không thể chỉnh sửa
Vui lòng chọn thời gian dùng thuốc
Số ngày dùng thuốc không hợp lệ
Vui lòng nhập tên bệnh viện
Vui lòng nhập tên người nhận
Vui lòng nhập chẩn đoán
Toa thuốc phải có ít nhất một thuốc trước khi phát hành
Tên thuốc không được để trống
Số lượng thuốc phải lớn hơn 0
Vui lòng chọn ít nhất một thời điểm dùng thuốc
```

## 14. Cách trình bày với thầy

```text
Ban đầu hệ thống chỉ dừng ở việc OCR và duyệt bệnh án.
Sau khi cải tiến, bệnh án được duyệt sẽ trở thành đầu vào cho quy trình kê toa.
Bác sĩ có thể tạo toa thuốc, chọn thời gian điều trị, search thuốc trong danh mục hoặc nhập tay nếu thuốc chưa có.
Toa thuốc lưu thông tin bệnh viện, bệnh nhân, BHYT, địa chỉ, chẩn đoán, bác sĩ, danh sách thuốc, lời dặn và ngày lập.
Sau khi phát hành, toa có thể in ra để bệnh nhân đem đi lãnh thuốc.
Hệ thống vì vậy không chỉ dừng ở OCR, mà có thêm bước xử lý lâm sàng sau khi bác sĩ khám và duyệt bệnh án.
```
