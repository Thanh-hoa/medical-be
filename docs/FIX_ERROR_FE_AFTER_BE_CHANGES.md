# FIX ERROR FE AFTER BE CHANGES

> File nay danh cho FE doc nhanh de biet can sua gi sau cac thay doi moi o BE.
>
> Base URL: `http://localhost:8080/api/v1`
>
> Header chung: `Authorization: Bearer <token>`

---

## 1. Tom tat viec FE can sua

| Muc | FE co can sua? | Viec can lam |
|---|---:|---|
| Ma hoa thong tin benh nhan va ghi chu y te | Khong bat buoc | Giu nguyen payload/response nhu cu. BE tu encrypt/decrypt. |
| Search patient theo ten/BHYT | Khong bat buoc | Giu nguyen endpoint va params. |
| Tim patient theo BHYT | Khong bat buoc | Giu nguyen `GET /patient/search?bhyt=...`. |
| Audit log filter theo ngay/tuan/thang/nam | Co | Them UI filter va truyen query params moi. |
| Audit log theo tung benh an | Nen sua | Neu co man hinh history trong detail, them cung bo filter thoi gian. |

---

## 2. Ma hoa du lieu: FE khong doi contract API

BE da ma hoa cac field sau khi luu DB:

- `patients.bhyt`
- `patients.name`
- `patients.dob`
- `patients.phone`
- `patients.address`
- `medical_records.notes`
- `medical_records.rejectionReason`

FE van gui va nhan plaintext nhu cu.

Vi du request tao/cap nhat patient van giu:

```json
{
  "bhyt": "GD4030000123456",
  "name": "Nguyen Van A",
  "dob": "1990-05-15",
  "gender": "Nam",
  "address": "Ha Noi",
  "phone": "0909123456"
}
```

Response FE nhan ve van la:

```json
{
  "id": 1,
  "bhyt": "GD4030000123456",
  "name": "Nguyen Van A",
  "dob": "1990-05-15",
  "gender": "Nam",
  "address": "Ha Noi",
  "phone": "0909123456"
}
```

FE khong duoc tu encrypt/decrypt. Viec do da nam o BE.

---

## 3. Audit log: them filter theo ngay/tuan/thang/nam

### Endpoint danh sach audit log

Endpoint cu van dung:

```http
GET /audit-logs?page=1&limit=20
```

BE da them cac query params moi:

| Param | Type | Bat buoc | Gia tri hop le | Ghi chu |
|---|---|---:|---|---|
| `period` | string | Khong | `day`, `week`, `month`, `year` | Kieu loc thoi gian |
| `date` | string | Khong | `yyyy-MM-dd` | Ngay moc de tinh period. Khong truyen thi BE lay ngay hien tai |
| `fromDate` | string | Khong | `yyyy-MM-dd` | Ngay bat dau custom range |
| `toDate` | string | Khong | `yyyy-MM-dd` | Ngay ket thuc custom range, BE tinh het ngay nay |

Van co the ket hop voi filter cu:

| Param cu | Ghi chu |
|---|---|
| `resourceType` | Vi du `MEDICAL_RECORD`, `PRESCRIPTION` |
| `actorId` | ID nguoi thao tac |
| `action` | Vi du `UPLOAD`, `APPROVE`, `REJECT`, `RESUBMIT` |
| `page` | Trang hien tai |
| `limit` | So item moi trang |

### Vi du FE call

Loc log trong ngay:

```http
GET /api/v1/audit-logs?period=day&date=2026-06-20&page=1&limit=20
```

Loc log trong tuan cua ngay `2026-06-20`:

```http
GET /api/v1/audit-logs?period=week&date=2026-06-20&page=1&limit=20
```

Loc log trong thang:

```http
GET /api/v1/audit-logs?period=month&date=2026-06-20&page=1&limit=20
```

Loc log trong nam:

```http
GET /api/v1/audit-logs?period=year&date=2026-06-20&page=1&limit=20
```

Loc khoang ngay tuy chon:

```http
GET /api/v1/audit-logs?fromDate=2026-06-01&toDate=2026-06-20&page=1&limit=20
```

Loc log reject trong thang:

```http
GET /api/v1/audit-logs?resourceType=MEDICAL_RECORD&action=REJECT&period=month&date=2026-06-20&page=1&limit=20
```

---

## 4. Audit log cua mot benh an: them cung filter thoi gian

Endpoint:

```http
GET /medical-record/{id}/audit-logs
```

Query params moi giong `/audit-logs`:

```http
GET /api/v1/medical-record/1/audit-logs?period=month&date=2026-06-20&page=1&limit=10
```

Custom range:

```http
GET /api/v1/medical-record/1/audit-logs?fromDate=2026-06-01&toDate=2026-06-20&page=1&limit=10
```

---

## 5. Quy tac tinh period o BE

| `period` | BE loc tu | BE loc den |
|---|---|---|
| `day` | 00:00 cua `date` | Truoc 00:00 ngay ke tiep |
| `week` | Thu Hai cua tuan chua `date` | Truoc Thu Hai tuan ke tiep |
| `month` | Ngay 01 cua thang chua `date` | Truoc ngay 01 thang ke tiep |
| `year` | Ngay 01/01 cua nam chua `date` | Truoc ngay 01/01 nam ke tiep |

Neu FE khong truyen `date`, BE dung ngay hien tai.

Khuyen nghi FE nen luon truyen `date` de UI hien thi dung voi lua chon cua user.

---

## 6. Goi y sua FE API client

Them type params:

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

UI nen co cac option:

- `Tat ca`
- `Hom nay`
- `Theo ngay`
- `Theo tuan`
- `Theo thang`
- `Theo nam`
- `Khoang ngay`

Map UI sang query params:

| UI | Params gui len BE |
|---|---|
| Tat ca | Khong gui `period`, `date`, `fromDate`, `toDate` |
| Hom nay | `period=day&date=<today>` |
| Theo ngay | `period=day&date=<selectedDate>` |
| Theo tuan | `period=week&date=<selectedDateInWeek>` |
| Theo thang | `period=month&date=<anyDateInMonth>` |
| Theo nam | `period=year&date=<anyDateInYear>` |
| Khoang ngay | `fromDate=<start>&toDate=<end>` |

---

## 7. Loi FE can handle

Neu FE truyen sai format ngay hoac period, BE tra error message:

| Truong hop | Message |
|---|---|
| `period` sai | `Khoang thoi gian audit log khong hop le. Dung day, week, month hoac year` |
| `date` sai format | `Ngay khong hop le. Dung dinh dang yyyy-MM-dd` |
| `fromDate` sai format | `fromDate khong hop le. Dung dinh dang yyyy-MM-dd` |
| `toDate` sai format | `toDate khong hop le. Dung dinh dang yyyy-MM-dd` |
| `fromDate > toDate` | `fromDate phai nho hon hoac bang toDate` |

FE chi can hien thi `response.data.message`.

---

## 8. Checklist nhanh cho FE

- [ ] Them filter thoi gian vao man hinh Audit Log.
- [ ] Them params `period`, `date`, `fromDate`, `toDate` vao API `/audit-logs`.
- [ ] Neu co tab lich su trong detail benh an, them params tuong tu cho `/medical-record/{id}/audit-logs`.
- [ ] Dam bao format date gui BE la `yyyy-MM-dd`.
- [ ] Khong encrypt/decrypt o FE.
- [ ] Khong sua payload patient/medical record vi BE van tra plaintext.
