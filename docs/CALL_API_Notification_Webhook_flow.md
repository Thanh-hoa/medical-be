# CALL API — Notification & Webhook Flow (Event-Driven)

> Tài liệu mô tả **đầu vào / đầu ra** của các API Notification và Webhook mới được thêm vào:  
> - Nhận thông báo theo sự kiện (Submit/Approve/Reject)
> - Quản lý Webhook cho tích hợp external services  
> - Event-Driven Architecture (publish → multi-listener model)
>
> Base URL: `http://localhost:8080/api/v1`  
> Mọi request đều cần header: `Authorization: Bearer <token>`

---

## Tổng quan các endpoint mới

| # | Method | Endpoint | Quyền yêu cầu | Mô tả |
|---|--------|----------|---------------|-------|
| 1 | GET | `/notifications` | Authenticated | Danh sách notification của user |
| 2 | GET | `/notifications/unread-count` | Authenticated | Đếm notification chưa đọc |
| 3 | PUT | `/notifications/{id}/read` | Authenticated | Đánh dấu notification đã đọc |
| 4 | PUT | `/notifications/read-all` | Authenticated | Đánh dấu tất cả notification đã đọc |
| 5 | DELETE | `/notifications/{id}` | Authenticated | Xóa notification |
| 6 | POST | `/admin/webhooks` | `admin` | Tạo webhook configuration |
| 7 | GET | `/admin/webhooks` | `admin` | Danh sách tất cả webhooks |
| 8 | GET | `/admin/webhooks/{id}` | `admin` | Chi tiết webhook |
| 9 | PUT | `/admin/webhooks/{id}` | `admin` | Cập nhật webhook |
| 10 | DELETE | `/admin/webhooks/{id}` | `admin` | Xóa webhook |

---

## Event Flow — Cách hoạt động

```
┌─────────────────────────────────────────────────────────────┐
│  USER ACTION (Employee submit / Doctor approve/reject)      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
         ┌─────────────────────────────┐
         │  MedicalRecordService       │
         │  - submitForReview()        │
         │  - approve()                │
         │  - reject()                 │
         └──────────┬──────────────────┘
                    │
                    ├─► save(record)
                    │
                    └─► publishEvent(MedicalRecordSubmittedEvent)
                            │
         ┌──────────────────┼──────────────────┐
         │                  │                  │
         ▼                  ▼                  ▼
    ┌─────────────┐  ┌──────────────┐  ┌────────────────┐
    │  AuditLog   │  │ Notification │  │  Webhook       │
    │  Listener   │  │  Listener    │  │  Listener      │
    │  (Sync)     │  │  (@Async)    │  │  (@Async)      │
    │ log()       │  │ create()     │  │  POST HTTP     │
    │             │  │              │  │                │
    │  → DB       │  │  → DB        │  │  → External    │
    └─────────────┘  └──────────────┘  └────────────────┘
         │                  │                  │
         └──────────────────┼──────────────────┘
                            │
                    ▼ (Parallel, Async)
              Response to Client
```

**Key Points:**
- ✅ Events trigger 3 listeners **independently**
- ✅ **AuditLog** chạy **sync** → giữ được SecurityContext & RequestContext (actorId, IP, UserAgent)
- ✅ **Notification & Webhook** chạy **@Async** → không block response
- ✅ Nếu webhook/notification fail → log error, không crash system
- ✅ Notifications lưu vào DB → user có thể xem sau

---

## Refactoring: AuditLog từ Direct → Event-Driven

### Before (Cách 2: Direct Call) ❌

**Trước đây MedicalRecordService gọi AuditLogService trực tiếp:**

```java
@Service
public class MedicalRecordService {
    
    @Autowired
    private AuditLogService auditLogService;  // ← Direct dependency
    
    public void submitForReview(Long id) {
        MedicalRecord record = findById(id);
        record.setStatus(PENDING_DOCTOR_REVIEW);
        
        // 1️⃣ Save DB
        medicalRecordRepository.save(record);
        
        // 2️⃣ Gọi trực tiếp (Tight coupling)
        auditLogService.log(
            ACTION_SUBMIT,
            RESOURCE_MEDICAL_RECORD,
            id,
            "{\"status\":\"Extracted\"}",
            "{\"status\":\"Pending Doctor Review\"}"
        );
        
        return medicalRecordMapper.toSummary(record);
    }
    
    public void approve(Long id) {
        MedicalRecord record = findById(id);
        record.setStatus(APPROVED);
        medicalRecordRepository.save(record);
        
        auditLogService.log(ACTION_APPROVE, ...);  // ← Lặp code
        return ...;
    }
    
    public void reject(Long id, String reason) {
        MedicalRecord record = findById(id);
        record.setStatus(REJECTED);
        medicalRecordRepository.save(record);
        
        auditLogService.log(ACTION_REJECT, ...);  // ← Lặp code
        return ...;
    }
}
```

**Problem:**
- ❌ MedicalRecordService phụ thuộc AuditLogService (Tight coupling)
- ❌ Lặp code `auditLogService.log()` ở nhiều nơi
- ❌ Khó thêm Notification / Webhook (phải thêm vào mỗi method)
- ❌ Nếu AuditLog fail → record cũng fail
- ❌ Không async

---

### After (Cách 1: Event-Driven) ✅

**Bây giờ MedicalRecordService chỉ publish events:**

```java
@Service
public class MedicalRecordService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;  // ← Loose coupling
    
    public void submitForReview(Long id) {
        MedicalRecord record = findById(id);
        record.setStatus(PENDING_DOCTOR_REVIEW);
        
        // 1️⃣ Save DB
        record = medicalRecordRepository.save(record);
        
        // 2️⃣ Publish event (Loose coupling)
        eventPublisher.publishEvent(
            new MedicalRecordSubmittedEvent(this, record, actorId)
        );
        
        return medicalRecordMapper.toSummary(record);
    }
    
    public void approve(Long id) {
        MedicalRecord record = findById(id);
        record.setStatus(APPROVED);
        record = medicalRecordRepository.save(record);
        
        eventPublisher.publishEvent(
            new MedicalRecordApprovedEvent(this, record, actorId)
        );
        return ...;
    }
    
    public void reject(Long id, String reason) {
        MedicalRecord record = findById(id);
        record.setStatus(REJECTED);
        record.setRejectionReason(reason);
        record = medicalRecordRepository.save(record);
        
        eventPublisher.publishEvent(
            new MedicalRecordRejectedEvent(this, record, actorId, reason)
        );
        return ...;
    }
}
```

**Listeners xử lý independent:**

```java
// 1️⃣ AuditLogEventListener — Sync (không @Async)
// Chạy trên request thread → SecurityContext & RequestContext còn tồn tại
// → actorId, ipAddress, userAgent được ghi đúng
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {
    @EventListener
    public void onMedicalRecordEvent(MedicalRecordEvent event) {
        auditLogService.log(
            event.getAction(),
            RESOURCE_MEDICAL_RECORD,
            event.getRecord().getId(),
            event.getOldValue(),
            event.getNewValue(),
            event.getActorId()  // ← từ event, không đọc SecurityContext
        );
    }
}

// 2️⃣ NotificationEventListener — @Async
// Chỉ xử lý SUBMIT, APPROVE, REJECT (không có RESUBMIT)
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    @EventListener
    @Async("taskExecutor")
    public void onMedicalRecordSubmitted(MedicalRecordSubmittedEvent event) {
        List<Account> doctors = accountRepo.findByRole("doctor");
        for (Account doctor : doctors) {
            notificationService.createNotification(
                doctor, "Bệnh án chờ duyệt",
                "Bệnh án " + event.getRecord().getRecordNumber() + " cần được duyệt",
                SUBMIT, "MedicalRecord", event.getRecord().getId()
            );
        }
    }
    @EventListener
    @Async("taskExecutor")
    public void onMedicalRecordRejected(MedicalRecordRejectedEvent event) {
        // Gửi notification cho uploader (employee)
        Account uploader = event.getRecord().getUploader();
        if (uploader != null) {
            notificationService.createNotification(
                uploader, "Bệnh án bị từ chối",
                "Lý do: " + event.getRejectionReason(),
                REJECT, "MedicalRecord", event.getRecord().getId()
            );
        }
    }
}

// 3️⃣ WebhookEventListener — @Async
@Component
@RequiredArgsConstructor
public class WebhookEventListener {
    @EventListener
    @Async("taskExecutor")
    public void onMedicalRecordEvent(MedicalRecordEvent event) {
        List<Webhook> webhooks = webhookRepo.findAllByIsActiveTrue();
        for (Webhook webhook : webhooks) {
            if (webhook.getEventTypes().contains(event.getAction())) {
                sendWebhook(webhook, event);  // ← Async, không block
            }
        }
    }
}
```

**Benefit:**
- ✅ MedicalRecordService không biết về AuditLog / Notification / Webhook
- ✅ Decoupling: Listeners độc lập
- ✅ Extensibility: Thêm listener mới không sửa MedicalRecordService
- ✅ Async: Webhook chạy trên thread pool riêng
- ✅ Resilience: Nếu webhook fail → log error, không crash record save
- ✅ Clean code: Không lặp code

---

### So Sánh

| Aspect | Before (Direct) | After (Event-Driven) |
|--------|-----------------|----------------------|
| **Coupling** | Tight ❌ | Loose ✅ |
| **Dependencies** | MedicalRecordService → AuditLogService → NotificationService → WebhookService | MedicalRecordService → EventPublisher |
| **Code duplication** | Lặp `auditLogService.log()` ở 6 methods ❌ | DRY: 1 listener ✅ |
| **Adding new feature** | Sửa 6 methods ❌ | Tạo 1 listener ✅ |
| **Failure isolation** | Nếu log fail → record fail ❌ | Nếu webhook fail → record OK ✅ |
| **Async** | ❌ Blocking | ✅ @Async webhooks |
| **Testing** | Mock 3 services ❌ | Mock event publisher ✅ |
| **Enterprise pattern** | ❌ No | ✅ Yes (pub-sub) |

---

### Event Classes

Mỗi action có 1 event class kế thừa `MedicalRecordEvent`:

```java
public abstract class MedicalRecordEvent extends ApplicationEvent {
    private final MedicalRecord record;
    private final Long actorId;
    
    public abstract String getAction();      // VD: "SUBMIT"
    public abstract String getOldValue();    // VD: "{\"status\":\"Extracted\"}"
    public abstract String getNewValue();    // VD: "{\"status\":\"Pending...\"}"
}
```

| Event Class | Action | oldValue | newValue |
|-------------|--------|----------|----------|
| `MedicalRecordUploadedEvent` | `UPLOAD` | `null` | `{"status":"Extracted"}` |
| `MedicalRecordSubmittedEvent` | `SUBMIT` | `{"status":"Extracted"}` | `{"status":"Pending Doctor Review"}` |
| `MedicalRecordApprovedEvent` | `APPROVE` | `{"status":"Pending..."}` | `{"status":"Approved"}` |
| `MedicalRecordRejectedEvent` | `REJECT` | `{"status":"Pending..."}` | `{"status":"Rejected","reason":"..."}` |
| `MedicalRecordResubmittedEvent` | `RESUBMIT` | `{"status":"Rejected"}` | `{"status":"Pending Doctor Review"}` |
| `MedicalRecordDeletedEvent` | `DELETE` | `{"status":"..."}` | `null` |

---

### Parallel Execution

Khi `publishEvent()` gọi:

```
Event published at T=0
    ├─ AuditLogListener    (Sync)   → Save DB [T=1ms]   ← actorId, IP, UA được ghi đúng
    ├─ NotificationListener(@Async) → Save DB [T=2ms on thread pool]
    └─ WebhookListener     (@Async) → POST HTTP [T=100ms on thread pool]

Response returns to client at T=2ms (không chờ notification/webhook)
```

**Kết quả:**
- ✅ Response nhanh (~2ms)
- ✅ Notification & Webhook chạy background
- ✅ Không block client
- ✅ AuditLog sync → actorId, ipAddress, userAgent luôn có giá trị

---

## Vòng đời Notification & Events

```
[Employee] Upload
    └─► MedicalRecordUploadedEvent
        └─► AuditLog: UPLOAD
        
[Employee] Submit
    └─► MedicalRecordSubmittedEvent
        ├─► AuditLog: SUBMIT
        ├─► Notification → All DOCTORS: "Bệnh án chờ duyệt"
        └─► Webhook: event_type = "SUBMIT"

[Doctor] Approve
    └─► MedicalRecordApprovedEvent
        ├─► AuditLog: APPROVE
        ├─► No notification
        └─► Webhook: event_type = "APPROVE"

[Doctor] Reject
    └─► MedicalRecordRejectedEvent
        ├─► AuditLog: REJECT
        ├─► Notification → EMPLOYEE: "Bệnh án bị từ chối + lý do"
        └─► Webhook: event_type = "REJECT"

[Employee] Resubmit
    └─► MedicalRecordResubmittedEvent
        ├─► AuditLog: RESUBMIT
        └─► Webhook: event_type = "RESUBMIT"
```

---

## 1. Danh sách Notification của User hiện tại

### `GET /notifications`

**Quyền:** Authenticated (mọi user)

> Lấy danh sách notification của user đang login, sắp xếp mới nhất trước. Có thể xem trạng thái đã đọc chưa.

**Query params:**

| Param | Type | Mặc định | Mô tả |
|-------|------|----------|-------|
| `page` | integer | `1` | Trang hiện tại (1-indexed) |
| `limit` | integer | `10` | Số bản ghi mỗi trang |

**Ví dụ:**
```
GET /api/v1/notifications?page=1&limit=10
```

**Response (200):**

> Response trả về **trực tiếp** dạng `PagedResponse`, không có wrapper `isError/message/data`.

```json
{
    "items": [
        {
            "id": 1,
            "title": "Bệnh án chờ duyệt",
            "message": "Bệnh án REC-2026-001234 từ nhân viên cần được duyệt",
            "type": "SUBMIT",
            "resourceType": "MedicalRecord",
            "resourceId": 123,
            "isRead": false,
            "createdAt": "2026-06-15T10:30:00",
            "readAt": null
        },
        {
            "id": 2,
            "title": "Bệnh án bị từ chối",
            "message": "Lý do: Thiếu thông tin bệnh nhân",
            "type": "REJECT",
            "resourceType": "MedicalRecord",
            "resourceId": 123,
            "isRead": true,
            "createdAt": "2026-06-15T11:00:00",
            "readAt": "2026-06-15T11:05:00"
        }
    ],
    "currentPage": 1,
    "limit": 10,
    "totalItems": 2,
    "totalPage": 1
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID notification |
| `title` | string | Tiêu đề (VD: "Bệnh án chờ duyệt") |
| `message` | string | Chi tiết |
| `type` | string | Event type: UPLOAD, SUBMIT, APPROVE, REJECT, RESUBMIT, DELETE, UPDATE |
| `resourceType` | string | Loại resource (VD: "MedicalRecord") |
| `resourceId` | Long | ID resource liên quan |
| `isRead` | boolean | Đã đọc chưa |
| `createdAt` | LocalDateTime | Thời điểm tạo |
| `readAt` | LocalDateTime \| null | Thời điểm đọc (nếu đã đọc) |

---

## 2. Đếm Notification chưa đọc

### `GET /notifications/unread-count`

**Quyền:** Authenticated

**Ví dụ:**
```
GET /api/v1/notifications/unread-count
```

**Response (200):**
```json
{
    "unreadCount": 5
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `unreadCount` | long | Số notification chưa đọc |

---

## 3. Đánh dấu một Notification đã đọc

### `PUT /notifications/{id}/read`

**Quyền:** Authenticated

> Đánh dấu notification là đã đọc. Chỉ user nào nhận notification đó mới có thể mark.

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID notification |

**Ví dụ:**
```
PUT /api/v1/notifications/1/read
```

**Request body:** Không có

**Response (204 No Content):** Không có body

**Response lỗi (404):**
```json
{
    "isError": true,
    "message": "Notification không tồn tại"
}
```

---

## 4. Đánh dấu tất cả Notification đã đọc

### `PUT /notifications/read-all`

**Quyền:** Authenticated

> Mark tất cả unread notifications của user hiện tại thành read.

**Ví dụ:**
```
PUT /api/v1/notifications/read-all
```

**Request body:** Không có

**Response (204 No Content):** Không có body

---

## 5. Xóa một Notification

### `DELETE /notifications/{id}`

**Quyền:** Authenticated

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID notification |

**Ví dụ:**
```
DELETE /api/v1/notifications/1
```

**Response (204 No Content):** Không có body

**Response lỗi (404):**
```json
{
    "isError": true,
    "message": "Notification không tồn tại"
}
```

---

## 6. Tạo Webhook (Admin)

### `POST /admin/webhooks`

**Quyền:** `admin`

> Admin tạo webhook configuration để nhận HTTP POST callbacks khi có events.

**Request body:**
```json
{
    "url": "https://external-system.com/medical-events",
    "eventTypes": "SUBMIT,APPROVE,REJECT,UPLOAD",
    "secret": "your-secret-key-123",
    "isActive": true
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `url` | string | ✅ | URL endpoint của external service |
| `eventTypes` | string | ✅ | Event types cách nhau bởi dấu phẩy (SUBMIT, APPROVE, REJECT, UPLOAD, RESUBMIT, DELETE) |
| `secret` | string | ✅ | Secret key để ký payload (HMAC-SHA256) |
| `isActive` | boolean | ❌ | Kích hoạt webhook (default: true) |

**Ví dụ:**
```
POST /api/v1/admin/webhooks
```

**Response (201):**
```json
{
    "id": 1,
    "url": "https://external-system.com/medical-events",
    "eventTypes": "SUBMIT,APPROVE,REJECT,UPLOAD",
    "isActive": true,
    "createdAt": "2026-06-15T10:30:00",
    "updatedAt": null
}
```

**Response lỗi (400):**
```json
{
    "isError": true,
    "message": "webhook.url.required"
}
```

---

## 7. Danh sách Webhooks (Admin)

### `GET /admin/webhooks`

**Quyền:** `admin`

**Ví dụ:**
```
GET /api/v1/admin/webhooks
```

**Response (200):**
```json
[
    {
        "id": 1,
        "url": "https://external-system.com/medical-events",
        "eventTypes": "SUBMIT,APPROVE,REJECT,UPLOAD",
        "isActive": true,
        "createdAt": "2026-06-15T10:30:00",
        "updatedAt": null
    },
    {
        "id": 2,
        "url": "https://another-system.com/api/webhooks",
        "eventTypes": "APPROVE",
        "isActive": false,
        "createdAt": "2026-06-14T09:00:00",
        "updatedAt": "2026-06-15T08:00:00"
    }
]
```

---

## 8. Chi tiết Webhook (Admin)

### `GET /admin/webhooks/{id}`

**Quyền:** `admin`

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID webhook |

**Ví dụ:**
```
GET /api/v1/admin/webhooks/1
```

**Response (200):**
```json
{
    "id": 1,
    "url": "https://external-system.com/medical-events",
    "eventTypes": "SUBMIT,APPROVE,REJECT,UPLOAD",
    "isActive": true,
    "createdAt": "2026-06-15T10:30:00",
    "updatedAt": null
}
```

---

## 9. Cập nhật Webhook (Admin)

### `PUT /admin/webhooks/{id}`

**Quyền:** `admin`

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID webhook |

**Request body:**
```json
{
    "url": "https://new-url.com/events",
    "eventTypes": "APPROVE,REJECT",
    "secret": "new-secret-key",
    "isActive": false
}
```

| Field | Type | Mô tả |
|-------|------|-------|
| `url` | string | URL mới (optional) |
| `eventTypes` | string | Event types mới (optional) |
| `secret` | string | Secret key mới (optional) |
| `isActive` | boolean | Enable/disable webhook (optional) |

**Ví dụ:**
```
PUT /api/v1/admin/webhooks/1
```

**Response (200):**
```json
{
    "id": 1,
    "url": "https://new-url.com/events",
    "eventTypes": "APPROVE,REJECT",
    "isActive": false,
    "createdAt": "2026-06-15T10:30:00",
    "updatedAt": "2026-06-15T11:00:00"
}
```

---

## 10. Xóa Webhook (Admin)

### `DELETE /admin/webhooks/{id}`

**Quyền:** `admin`

**Path param:**

| Param | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID webhook |

**Ví dụ:**
```
DELETE /api/v1/admin/webhooks/1
```

**Response (204 No Content):** Không có body

---

## Webhook Payload Format

Khi event trigger, system sẽ POST JSON payload tới webhook URL:

```json
{
    "eventType": "medical-record.submit",
    "medicalRecordId": 123,
    "recordNumber": "REC-2026-001234",
    "status": "PENDING_DOCTOR_REVIEW",
    "actorId": 5,
    "timestamp": "2026-06-15T10:30:00Z",
    "data": {
        "uploaderName": "Nguyễn Văn A",
        "uploaderEmail": "a@hospital.com",
        "patientName": "Tế Phạm Bảo",
        "patientBhyt": "BA12345678"
    }
}
```

**HTTP Headers:**
```
Content-Type: application/json
X-Webhook-Signature: sha256=abcd1234def5678...
X-Webhook-Event: submit
```

**Verify Signature (Node.js example):**
```javascript
const crypto = require('crypto');

const secret = 'your-secret-key-123';
const payload = JSON.stringify(req.body);
const signature = crypto
    .createHmac('sha256', secret)
    .update(payload)
    .digest('base64');

const headerSig = req.headers['x-webhook-signature'].split('=')[1];
if (signature !== headerSig) {
    return res.status(401).json({ error: 'Invalid signature' });
}
// ✅ Signature valid
```

---

## Event Types

| Event Type | Trigger | Doctor Notification | Employee Notification | Webhook | AuditLog |
|------------|---------|--------------------|-----------------------|---------|----------|
| UPLOAD | POST /medical-record/upload | ❌ | ❌ | ✅ | ✅ |
| SUBMIT | PUT /medical-record/{id}/submit | ✅ "Bệnh án chờ duyệt" | ❌ | ✅ | ✅ |
| APPROVE | PUT /medical-record/{id}/approve | No | No | Yes | Yes |
| REJECT | PUT /medical-record/{id}/reject | ❌ | ✅ "Bệnh án bị từ chối" | ✅ | ✅ |
| RESUBMIT | PUT /medical-record/{id}/resubmit | ❌ | ❌ | ✅ | ✅ |
| DELETE | DELETE /medical-record/{id} | ❌ | ❌ | ✅ | ✅ |

---

## Thứ tự gọi API điển hình — Notification & Webhook Flow

### Scenario 1: Employee Submit → Doctor Notification → Approve

```
[Employee] 
  PUT /medical-record/{id}/submit
    ✓ Status → PENDING_DOCTOR_REVIEW
    ✓ AuditLog: SUBMIT
    ✓ Webhook: POST https://external.com/webhooks (async)
    ✓ Notification → All DOCTORS: "Bệnh án chờ duyệt"

[Doctor]
  GET /notifications
    ✓ Xem notification "Bệnh án chờ duyệt"
  
  PUT /notifications/{id}/read
    ✓ Mark notification đã đọc
  
  GET /medical-record/{id}
    ✓ Xem chi tiết bệnh án
  
  PUT /medical-record/{id}/approve
    ✓ Status → APPROVED
    ✓ AuditLog: APPROVE
    ✓ Webhook: POST https://external.com/webhooks (async)
    ✓ No notification to employee

[Employee]
  GET /notifications
    ✓ Khong co approve notification
```

### Scenario 2: Doctor Reject → Employee Notification → Resubmit

```
[Doctor]
  PUT /medical-record/{id}/reject
    Body: { "rejectionReason": "Thiếu thông tin bệnh nhân" }
    ✓ Status → REJECTED
    ✓ AuditLog: REJECT
    ✓ Webhook: POST (async)
    ✓ Notification → EMPLOYEE: "Bệnh án bị từ chối"

[Employee]
  GET /notifications
    ✓ Xem notification "Bệnh án bị từ chối" + lý do
  
  GET /medical-record/{id}
    ✓ Xem rejectionReason
  
  PUT /medical-record/update-detail
    ✓ Sửa thông tin bệnh án
  
  PUT /medical-record/{id}/resubmit
    ✓ Status → PENDING_DOCTOR_REVIEW
    ✓ AuditLog: RESUBMIT
    ✓ Webhook: POST (async)
```

### Scenario 3: Admin Manage Webhooks

```
[Admin]
  POST /admin/webhooks
    Body: { 
      "url": "https://erp.company.com/medical-events",
      "eventTypes": "SUBMIT,APPROVE,REJECT",
      "secret": "super-secret-key"
    }
    ✓ Webhook created
  
  GET /admin/webhooks
    ✓ Xem tất cả webhooks
  
  PUT /admin/webhooks/{id}
    Body: { "isActive": false }
    ✓ Disable webhook
  
  Khi có event SUBMIT/APPROVE/REJECT:
    ✓ System tự động POST tới URL
    ✓ Header: X-Webhook-Signature: sha256=...
    ✓ Async (không block response)
```

---

## Notification User Experience

```
┌─────────────────────────────────────┐
│  Doctor Dashboard                   │
│  ┌───────────────────────────────┐  │
│  │ 🔔 Notifications (5)          │  │
│  ├───────────────────────────────┤  │
│  │ ✗ Bệnh án chờ duyệt          │  │ ← Unread (isRead = false)
│  │   REC-2026-001234             │  │
│  │   10:30 AM                    │  │
│  │                               │  │
│  │ ✓ Bệnh án được đánh giá       │  │ ← Read (isRead = true)
│  │   REC-2026-001233             │  │
│  │   09:15 AM                    │  │
│  └───────────────────────────────┘  │
│                                     │
│  [Mark all as read]                 │
│  [Unread count: 1]                  │
└─────────────────────────────────────┘
```

**Actions:**
- Click notification → Xem chi tiết bệnh án
- PUT /notifications/{id}/read → Mark đã đọc
- PUT /notifications/read-all → Mark tất cả
- DELETE /notifications/{id} → Xóa

---

## Xử lý lỗi chung

| HTTP Status | Ý nghĩa | Xử lý |
|-------------|---------|-------|
| 200 | OK | ✓ Thành công |
| 201 | Created | ✓ Webhook tạo thành công |
| 204 | No Content | ✓ Thành công (không có body) |
| 400 | Bad Request | Sai input, xem `message` |
| 401 | Unauthorized | Token hết hạn → refresh-token |
| 403 | Forbidden | Không có quyền |
| 404 | Not Found | Notification/Webhook không tồn tại |
| 500 | Server Error | Lỗi server |

**Cấu trúc lỗi:**
```json
{
    "isError": true,
    "message": "Mô tả lỗi cụ thể"
}
```

---

## Notification read sync

Backend dong bo trang thai da doc theo resource `MedicalRecord`:

- `GET /medical-record/{id}`: sau khi user co quyen xem chi tiet, system mark tat ca unread notifications cua user do voi `resourceType = "MedicalRecord"` va `resourceId = id` thanh read.
- `PUT /medical-record/{id}/approve`: doctor/admin approve xong, system mark notification unread lien quan cua chinh doctor/admin thanh read.
- `PUT /medical-record/{id}/reject`: doctor/admin reject xong, system mark notification unread lien quan cua chinh doctor/admin thanh read.
- `PUT /medical-record/{id}/resubmit`: employee/admin resubmit xong, system mark notification unread lien quan cua chinh employee/admin thanh read.
- Sau `resubmit`, backend publish `MedicalRecordResubmittedEvent` va tao notification type `RESUBMIT` cho tat ca doctor voi `resourceType = "MedicalRecord"` va `resourceId = id`.

Notification khong bi xoa khoi DB; chi doi `isRead = true` va set `readAt`. Neu FE muon thong bao "mat khoi chuong", dropdown nen filter `isRead = false`. Sau khi mo detail hoac action approve/reject/resubmit, FE nen goi lai `GET /notifications/unread-count`.

Update for Notes: Unread count da duoc backend auto sync theo `MedicalRecord`. FE khong bat buoc goi `PUT /notifications/read-all` trong flow mo detail / approve / reject / resubmit.

---

## Notes

1. **Notification không realtime** - Cần polling GET /notifications hoặc WebSocket integration
2. **Pagination 1-indexed** - `page` bắt đầu từ `1` (không phải `0`). Gọi `page=0` → 400 Bad Request
3. **Unread count auto sync theo MedicalRecord** - Khi user mo `GET /medical-record/{id}` hoac thuc hien `approve/reject/resubmit`, backend mark unread notifications lien quan den `MedicalRecord/{id}` cua current user thanh read. FE nen goi lai `GET /notifications/unread-count` de refresh badge.
4. **Response format** - Notification list trả về `PagedResponse` trực tiếp (không có wrapper `isError/message/data`). Lỗi trả về `{"isError": true, "message": "..."}`
5. **Webhook retry** - Hiện tại fail → log error. Future: implement retry queue
6. **Webhook timeout** - Set timeout 10s, fail silently
7. **Event order** - Không guarantee ordering nếu dùng message queue sau này
8. **Secret rotation** - Admin có thể update secret anytime
9. **Event tracing** - Dùng audit logs + webhooks để truy vết tất cả changes



