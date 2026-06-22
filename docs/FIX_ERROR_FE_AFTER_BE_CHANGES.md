# FIX ERROR FE AFTER BE CHANGES

> File này dành cho FE đọc nhanh để biết cần sửa gì sau các thay đổi mới ở BE.
>
> Base URL: `http://localhost:8080/api/v1`
>
> Header chung: `Authorization: Bearer <token>`

---

## 1. Tóm tắt việc FE cần sửa

| Mục | FE có cần sửa? | Việc cần làm |
|---|---:|---|
| Mã hóa thông tin bệnh nhân và ghi chú y tế | Không bắt buộc | Giữ nguyên payload/response như cũ. BE tự encrypt/decrypt. |
| Search patient theo tên/BHYT | Không bắt buộc | Giữ nguyên endpoint và params. |
| Tìm patient theo BHYT | Không bắt buộc | Giữ nguyên `GET /patient/search?bhyt=...`. |
| Audit log filter theo ngày/tuần/tháng/năm | Có | Thêm UI filter và truyền query params mới. |
| Audit log theo từng bệnh án | Nên sửa | Nếu có màn hình history trong detail, thêm cùng bộ filter thời gian. |

---

## 2. Mã hóa dữ liệu: FE không đổi contract API

BE đã mã hóa các field sau khi lưu DB:

- `patients.bhyt`
- `patients.name`
- `patients.dob`
- `patients.phone`
- `patients.address`
- `medical_records.notes`
- `medical_records.rejectionReason`

FE vẫn gửi và nhận plaintext như cũ.

Ví dụ request tạo/cập nhật patient vẫn giữ:

```json
{
  "bhyt": "GD4030000123456",
  "name": "Nguyễn Văn A",
  "dob": "1990-05-15",
  "gender": "Nam",
  "address": "Hà Nội",
  "phone": "0909123456"
}
```

Response FE nhận về vẫn là:

```json
{
  "id": 1,
  "bhyt": "GD4030000123456",
  "name": "Nguyễn Văn A",
  "dob": "1990-05-15",
  "gender": "Nam",
  "address": "Hà Nội",
  "phone": "0909123456"
}
```

FE không được tự encrypt/decrypt. Việc đó đã nằm ở BE.

---

## 3. Audit log: thêm filter theo ngày/tuần/tháng/năm

### Endpoint danh sách audit log

Endpoint cũ vẫn dùng:

```http
GET /audit-logs?page=1&limit=20
```

BE đã thêm các query params mới:

| Param | Type | Bắt buộc | Giá trị hợp lệ | Ghi chú |
|---|---|---:|---|---|
| `period` | string | Không | `day`, `week`, `month`, `year` | Kiểu lọc thời gian |
| `date` | string | Không | `yyyy-MM-dd` | Ngày mốc để tính period. Không truyền thì BE lấy ngày hiện tại |
| `fromDate` | string | Không | `yyyy-MM-dd` | Ngày bắt đầu custom range |
| `toDate` | string | Không | `yyyy-MM-dd` | Ngày kết thúc custom range, BE tính hết ngày này |

Vẫn có thể kết hợp với filter cũ:

| Param cũ | Ghi chú |
|---|---|
| `resourceType` | Ví dụ `MEDICAL_RECORD`, `PRESCRIPTION` |
| `actorId` | ID người thao tác |
| `action` | Ví dụ `UPLOAD`, `APPROVE`, `REJECT`, `RESUBMIT` |
| `page` | Trang hiện tại |
| `limit` | Số item mỗi trang |

### Ví dụ FE call

Lọc log trong ngày:

```http
GET /api/v1/audit-logs?period=day&date=2026-06-20&page=1&limit=20
```

Lọc log trong tuần của ngày `2026-06-20`:

```http
GET /api/v1/audit-logs?period=week&date=2026-06-20&page=1&limit=20
```

Lọc log trong tháng:

```http
GET /api/v1/audit-logs?period=month&date=2026-06-20&page=1&limit=20
```

Lọc log trong năm:

```http
GET /api/v1/audit-logs?period=year&date=2026-06-20&page=1&limit=20
```

Lọc khoảng ngày tùy chọn:

```http
GET /api/v1/audit-logs?fromDate=2026-06-01&toDate=2026-06-20&page=1&limit=20
```

Lọc log reject trong tháng:

```http
GET /api/v1/audit-logs?resourceType=MEDICAL_RECORD&action=REJECT&period=month&date=2026-06-20&page=1&limit=20
```

---

## 4. Audit log của một bệnh án: thêm cùng filter thời gian

Endpoint:

```http
GET /medical-record/{id}/audit-logs
```

Query params mới giống `/audit-logs`:

```http
GET /api/v1/medical-record/1/audit-logs?period=month&date=2026-06-20&page=1&limit=10
```

Custom range:

```http
GET /api/v1/medical-record/1/audit-logs?fromDate=2026-06-01&toDate=2026-06-20&page=1&limit=10
```

---

## 5. Quy tắc tính period ở BE

| `period` | BE lọc từ | BE lọc đến |
|---|---|---|
| `day` | 00:00 của `date` | Trước 00:00 ngày kế tiếp |
| `week` | Thứ Hai của tuần chứa `date` | Trước Thứ Hai tuần kế tiếp |
| `month` | Ngày 01 của tháng chứa `date` | Trước ngày 01 tháng kế tiếp |
| `year` | Ngày 01/01 của năm chứa `date` | Trước ngày 01/01 năm kế tiếp |

Nếu FE không truyền `date`, BE dùng ngày hiện tại.

Khuyến nghị FE nên luôn truyền `date` để UI hiển thị đúng với lựa chọn của user.

---

## 6. Gợi ý sửa FE API client

Thêm type params:

```ts
export type AuditLogPeriod = 'day' | 'week' | 'month' | 'year'

export type AuditLogListParams = {
  page?: number
  limit?: number
  resourceType?: string
  actorId?: number
  action?: string
  period?: AuditLogPeriod
  date?: string
  fromDate?: string
  toDate?: string
}
```

API client:

```ts
export const auditLogApi = {
  list: (params: AuditLogListParams) =>
    api.get('/audit-logs', { params }),

  detail: (id: number) =>
    api.get(`/audit-logs/${id}`),

  listByRecord: (recordId: number, params: AuditLogListParams) =>
    api.get(`/medical-record/${recordId}/audit-logs`, { params }),
}
```

UI nên có các option:

- `Tất cả`
- `Hôm nay`
- `Theo ngay`
- `Theo tuần`
- `Theo tháng`
- `Theo năm`
- `Khoảng ngày`

Map UI sang query params:

| UI | Params gửi lên BE |
|---|---|
| Tất cả | Không gửi `period`, `date`, `fromDate`, `toDate` |
| Hôm nay | `period=day&date=<today>` |
| Theo ngày | `period=day&date=<selectedDate>` |
| Theo tuần | `period=week&date=<selectedDateInWeek>` |
| Theo tháng | `period=month&date=<anyDateInMonth>` |
| Theo năm | `period=year&date=<anyDateInYear>` |
| Khoảng ngày | `fromDate=<start>&toDate=<end>` |

---

## 7. Lỗi FE cần handle

Nếu FE truyền sai format ngày hoặc period, BE trả error message:

| Trường hợp | Message |
|---|---|
| `period` sai | `Khoảng thời gian audit log không hợp lệ. Dùng day, week, month hoặc year` |
| `date` sai format | `Ngày không hợp lệ. Dùng định dạng yyyy-MM-dd` |
| `fromDate` sai format | `fromDate không hợp lệ. Dùng định dạng yyyy-MM-dd` |
| `toDate` sai format | `toDate không hợp lệ. Dùng định dạng yyyy-MM-dd` |
| `fromDate > toDate` | `fromDate phải nhỏ hơn hoặc bằng toDate` |

FE chỉ cần hiển thị `response.data.message`.

---

## 8. Checklist nhanh cho FE

- [ ] Thêm filter thời gian vào màn hình Audit Log.
- [ ] Thêm params `period`, `date`, `fromDate`, `toDate` vào API `/audit-logs`.
- [ ] Nếu có tab lịch sử trong detail bệnh án, thêm params tương tự cho `/medical-record/{id}/audit-logs`.
- [ ] Đảm bảo format date gửi BE là `yyyy-MM-dd`.
- [ ] Không encrypt/decrypt ở FE.
- [ ] Không sửa payload patient/medical record vì BE vẫn trả plaintext.

---

## 9. Dashboard: BE đã nâng cấp filter thời gian và dữ liệu so sánh

Dashboard cũ chỉ trả số tổng, chưa đủ để FE biết tăng/giảm hoặc vẽ biểu đồ theo thời gian. BE đã mở rộng các endpoint dashboard hiện có và thêm endpoint mới cho timeline.

Các endpoint dashboard đều hỗ trợ query params thời gian:

| Param | Type | Bắt buộc | Giá trị hợp lệ | Ghi chú |
|---|---|---:|---|---|
| `period` | string | Không | `day`, `week`, `month`, `year` | Kỳ thống kê |
| `date` | string | Không | `yyyy-MM-dd` | Ngày mốc để tính kỳ |
| `fromDate` | string | Không | `yyyy-MM-dd` | Ngày bắt đầu khoảng tùy chọn |
| `toDate` | string | Không | `yyyy-MM-dd` | Ngày kết thúc khoảng tùy chọn |

Nếu không truyền gì, BE mặc định dùng `period=day` và `date` là ngày hiện tại.

Ví dụ:

```http
GET /api/v1/dashboard/overview?period=month&date=2026-06-20
GET /api/v1/dashboard/records-by-department?fromDate=2026-06-01&toDate=2026-06-20
GET /api/v1/dashboard/user-performance?period=week&date=2026-06-20
```

---

## 10. Dashboard overview: thêm `range` và `cards`

Endpoint:

```http
GET /dashboard/overview
```

Ví dụ:

```http
GET /api/v1/dashboard/overview?period=month&date=2026-06-20
```

Response vẫn giữ các field cũ như `totalRecords`, `totalPatients`, `todayUploads`, `todayApprovals` để không làm hỏng FE cũ. BE thêm 2 field mới:

- `range`: kỳ hiện tại và kỳ trước đó.
- `cards`: danh sách card có số hiện tại, số kỳ trước, mức tăng/giảm, phần trăm tăng/giảm.

Ví dụ response rút gọn:

```json
{
  "isError": false,
  "message": "Lấy tổng quan dashboard thành công",
  "data": {
    "range": {
      "period": "month",
      "fromDate": "2026-06-01",
      "toDate": "2026-06-30",
      "previousFromDate": "2026-05-01",
      "previousToDate": "2026-05-31"
    },
    "cards": [
      {
        "key": "newRecords",
        "label": "Bệnh án mới",
        "value": 42,
        "previousValue": 30,
        "change": 12,
        "changePercent": 40.0,
        "trend": "up"
      },
      {
        "key": "approvedRecords",
        "label": "Bệnh án đã duyệt",
        "value": 18,
        "previousValue": 20,
        "change": -2,
        "changePercent": -10.0,
        "trend": "down"
      }
    ],
    "totalRecords": 128,
    "totalPatients": 95,
    "totalAccounts": 12,
    "todayUploads": 5,
    "todayApprovals": 3
  }
}
```

FE nên dùng `cards` để vẽ các ô tổng quan:

| Field | Ý nghĩa |
|---|---|
| `value` | Số của kỳ hiện tại |
| `previousValue` | Số của kỳ trước |
| `change` | Chênh lệch tuyệt đối |
| `changePercent` | Phần trăm tăng/giảm |
| `trend` | `up`, `down`, `flat` |

---

## 11. Dashboard timeline: endpoint mới để vẽ biểu đồ

Endpoint mới:

```http
GET /dashboard/timeline
```

Query params:

| Param | Type | Giá trị hợp lệ | Ghi chú |
|---|---|---|---|
| `metric` | string | `uploads`, `records`, `approvals`, `rejections` | Loại dữ liệu cần vẽ |
| `period` | string | `day`, `week`, `month`, `year` | Kỳ thống kê |
| `date` | string | `yyyy-MM-dd` | Ngày mốc |
| `fromDate` | string | `yyyy-MM-dd` | Khoảng tùy chọn |
| `toDate` | string | `yyyy-MM-dd` | Khoảng tùy chọn |

Ghi chú: `records` được map giống `uploads`, tức là bệnh án được tạo mới.

Ví dụ:

```http
GET /api/v1/dashboard/timeline?metric=uploads&period=month&date=2026-06-20
GET /api/v1/dashboard/timeline?metric=approvals&period=year&date=2026-06-20
GET /api/v1/dashboard/timeline?metric=rejections&fromDate=2026-06-01&toDate=2026-06-20
```

Response:

```json
{
  "isError": false,
  "message": "Lấy dữ liệu biểu đồ dashboard thành công",
  "data": {
    "metric": "uploads",
    "period": "month",
    "fromDate": "2026-06-01",
    "toDate": "2026-06-30",
    "items": [
      {
        "key": "2026-06-01",
        "label": "2026-06-01",
        "count": 5,
        "percent": 0.0
      },
      {
        "key": "2026-06-02",
        "label": "2026-06-02",
        "count": 3,
        "percent": 0.0
      }
    ]
  }
}
```

FE dùng `items` để vẽ line chart hoặc bar chart. Với `period=year` hoặc khoảng ngày quá dài, BE tự gom theo tháng, label sẽ có dạng `yyyy-MM`.

---

## 12. Dashboard breakdown: status, department, user performance

### Records by status

Endpoint:

```http
GET /dashboard/records-by-status
```

Params mới:

| Param | Giá trị | Ghi chú |
|---|---|---|
| `scope` | `created` | Thống kê các bệnh án được tạo trong kỳ. Đây là mặc định. |
| `scope` | `current` | Thống kê trạng thái hiện tại của toàn bộ bệnh án. |

Ví dụ:

```http
GET /api/v1/dashboard/records-by-status?scope=created&period=month&date=2026-06-20
GET /api/v1/dashboard/records-by-status?scope=current
```

Response item có thêm `key` và `percent`:

```json
{
  "key": "APPROVED",
  "label": "Approved",
  "count": 100,
  "percent": 78.13
}
```

### Records by department

Endpoint:

```http
GET /dashboard/records-by-department?period=month&date=2026-06-20
```

Response item có thêm `key` và `percent`, dùng để vẽ bar chart hoặc bảng xếp hạng khoa/phòng:

```json
{
  "key": "Nội tổng hợp",
  "label": "Nội tổng hợp",
  "count": 45,
  "percent": 35.16
}
```

### User performance

Endpoint:

```http
GET /dashboard/user-performance?period=month&date=2026-06-20
```

Response thêm `totalActions` và đã được sort giảm dần theo tổng thao tác:

```json
{
  "accountId": 2,
  "accountName": "BS. Trần Văn Khoa",
  "uploaded": 0,
  "approved": 18,
  "rejected": 3,
  "totalActions": 21
}
```

---

## 13. Checklist FE cần sửa thêm cho Dashboard

- [ ] Thêm bộ lọc thời gian chung cho dashboard: ngày, tuần, tháng, năm, khoảng ngày.
- [ ] Gọi `/dashboard/overview` kèm `period/date` hoặc `fromDate/toDate`.
- [ ] Đổi card overview sang đọc từ `data.cards`.
- [ ] Dùng `trend`, `change`, `changePercent` để hiển thị tăng/giảm.
- [ ] Gọi `/dashboard/timeline` để vẽ line chart/bar chart.
- [ ] Gọi `/dashboard/records-by-status?scope=created` cho biểu đồ status theo kỳ.
- [ ] Gọi `/dashboard/records-by-status?scope=current` nếu muốn xem trạng thái hiện tại toàn hệ thống.
- [ ] Gọi `/dashboard/records-by-department` kèm filter thời gian để vẽ ranking khoa/phòng.
- [ ] Gọi `/dashboard/user-performance` kèm filter thời gian để vẽ bảng hiệu suất người dùng.
