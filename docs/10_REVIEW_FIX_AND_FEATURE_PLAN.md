# Review khắt khe và kế hoạch bổ sung chức năng

Cập nhật gần nhất: 2026-06-11.

Ký hiệu:

- `[x]` Đã làm.
- `[~]` Đã làm một phần, vẫn cần sửa tiếp.
- `[ ]` Chưa làm.

## 1. Kết quả rà soát hiện tại

### 1.1. Các mục đã làm hoặc đã cải thiện

- `[~]` Tách cấu hình theo môi trường.
  - Đã có `src/main/resources/application-prod.yaml`.
  - Đã đưa nhiều cấu hình sang biến môi trường trong `application.yaml`.
  - Đã có `.env.example`.
  - Còn thiếu `application-dev.yaml` và `application-test.yaml`.
  - `application.yaml` vẫn có fallback DB password `123456` và fallback JWT key dev, chỉ chấp nhận được cho local demo.

- `[~]` Production config an toàn hơn.
  - `application-prod.yaml` đã đặt `ddl-auto: validate`.
  - `show-sql: false`.
  - `app.debug: false`.
  - Swagger đã tắt ở profile `prod`.
  - Còn thiếu migration Flyway/Liquibase nên `ddl-auto: validate` có thể gây khó deploy nếu DB chưa có schema đúng.

- `[~]` README đã cập nhật một phần.
  - README đã ghi Java 21 và Spring Boot 3.2.5.
  - Swagger path đã sửa thành `/documents/swagger-ui`.
  - Đã thêm Docker Compose và profile `prod`.
  - Tuy nhiên README vẫn bị lỗi encoding tiếng Việt khi đọc trong terminal.
  - README vẫn liệt kê Dashboard/AI logs nhưng code chưa có module thật.

- `[x]` Thêm Docker/Docker Compose.
  - Đã có `Dockerfile`.
  - Đã có `docker-compose.yml` với PostgreSQL service và app service.
  - Điểm này giúp demo/deploy tốt hơn.

- `[~]` CI đã được thêm.
  - Đã có `.github/workflows/ci.yml`.
  - CI có PostgreSQL service và chạy `./mvnw clean verify`.
  - Nhưng test hiện tại vẫn chỉ có `contextLoads`, nên CI mới chứng minh project start được, chưa chứng minh nghiệp vụ đúng.

- `[~]` `mvn test` hiện đã chạy pass trên máy local.
  - Kết quả local: `Tests run: 1, Failures: 0, Errors: 0`.
  - Vẫn phụ thuộc PostgreSQL local đang chạy.
  - Chưa có `application-test.yaml`, H2 hoặc Testcontainers.

- `[x]` Đổi tên typo DTO patient.
  - Đã đổi `UpdatePatiientReq` thành `UpdatePatientReq`.

- `[~]` Xóa bệnh án đã chuyển sang soft delete.
  - `MedicalRecord` đã có `isDelete` và `deletedAt`.
  - `MedicalRecordService.delete()` đã set soft delete thay vì xóa cứng.
  - Cần kiểm tra repository list/detail đã lọc `isDelete = false` chưa. Nếu chưa lọc thì record đã xóa mềm vẫn có thể hiện lại.

- `[~]` Upload path đã dùng `app.server.url`.
  - `FileStorageService` không còn hardcode `http://localhost:8080` trực tiếp.
  - Nhưng file vẫn public qua `/uploads/photos/**`.

### 1.2. Các mục chưa làm hoặc vẫn yếu

- `[ ]` Chưa có `application-dev.yaml`.
- `[ ]` Chưa có `application-test.yaml`.
- `[ ]` Chưa có Flyway/Liquibase migration.
- `[ ]` Chưa sửa seeder admin mặc định.
- `[ ]` Chưa phân biệt access token và refresh token bằng claim token type.
- `[ ]` Logout vẫn public và nhận token bằng request body.
- `[ ]` Upload file vẫn chỉ check extension, chưa check MIME/magic bytes.
- `[ ]` File bệnh án vẫn public qua `/uploads/**`.
- `[ ]` Chưa có secure file access API.
- `[x]` Audit log module (refactored to Event-Driven).
- `[x]` Reject medical record hoàn chỉnh.
- `[x]` Dashboard API thật.
- `[x]` Notification module (Event-Driven with Webhook support).
- `[x]` Webhook integration (HTTP callbacks for external services).
- `[ ]` Chưa có OCR confidence/manual verification.
- `[ ]` Chưa có version history.
- `[ ]` Chưa nâng cấp search nâng cao đúng nghiệp vụ.
- `[ ]` Chưa chuẩn hóa ngày tháng toàn bộ API.
- `[ ]` Chưa sửa logic sinh `recordNumber` bằng sequence/UUID/retry an toàn.

## 2. Các lỗi cần sửa trước khi thêm chức năng mới

### 2.1. Bảo mật cấu hình

Trạng thái: `[~]` Đã làm một phần.

Đã làm:

- Đã đưa DB/mail/JWT/app config sang biến môi trường trong `application.yaml`.
- Đã có `.env.example`.
- Đã có `application-prod.yaml`.
- Production đã tắt debug, SQL log và Swagger.

Cần làm tiếp:

- Tạo `application-dev.yaml`.
- Tạo `application-test.yaml`.
- Bỏ fallback password thật như `123456` khỏi config mặc định nếu muốn nghiêm túc hơn.
- Đổi Gmail app password và JWT key đã từng lộ trong source.
- Đảm bảo `.env` nằm trong `.gitignore`.

Gợi ý:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

app:
  key: ${APP_KEY}
  debug: false
```

### 2.2. Seeder admin mặc định

Trạng thái: `[ ]` Chưa làm.

Hiện tại `AccountSeeder` vẫn hardcode:

- email admin: `hoa1312004@gmail.com`
- password: `Aa123456`
- khi app restart vẫn update lại password account admin

Cần sửa:

- Chỉ seed admin trong profile `dev`.
- Lấy email/password từ biến môi trường.
- Nếu account đã tồn tại thì không reset password.
- Trong README ghi rõ đây chỉ là tài khoản demo.

### 2.3. Token access/refresh

Trạng thái: `[ ]` Chưa làm.

Hiện tại:

- Access token và refresh token chưa có claim `typ`.
- Refresh endpoint chỉ check token active, sau đó parse refresh token nhưng không xác minh token type.
- Logout vẫn nhận request body string.

Cần sửa:

- Access token có claim `typ: access`.
- Refresh token có claim `typ: refresh`.
- `refreshToken()` chỉ chấp nhận token có `typ = refresh`.
- Logout lấy token từ `Authorization: Bearer ...`.
- Khi disable account hoặc đổi password nên revoke token của user.

### 2.4. Upload file và bảo vệ file bệnh án

Trạng thái: `[ ]` Chưa làm phần bảo mật chính.

Đã cải thiện:

- Tên file lưu bằng UUID.
- URL server lấy từ config `app.server.url`.

Vẫn còn lỗi:

- Vẫn expose `/uploads/**` public trong `SecurityConfig`.
- `WebConfig` vẫn map `/uploads/photos/**`.
- `FileStorageService` chỉ check extension `jpg/jpeg/png/webp`.
- Chưa check MIME thật/magic bytes.
- Chưa có API tải file có phân quyền.

Cần sửa:

- Không public trực tiếp thư mục upload bệnh án.
- Tạo API:

```text
GET /api/v1/medical-record/{id}/file
```

- Quyền:
  - Admin xem tất cả.
  - Doctor xem record cần duyệt/đã duyệt.
  - Employee chỉ xem record do mình upload.

### 2.5. Test

Trạng thái: `[~]` Đã làm một phần.

Đã làm:

- `mvn test` hiện pass trên máy local.
- Đã có GitHub Actions CI với PostgreSQL service.

Vẫn yếu:

- Chỉ có 1 test `contextLoads`.
- Chưa có `application-test.yaml`.
- Test vẫn phụ thuộc database PostgreSQL local hoặc PostgreSQL service trong CI.
- Chưa có test nghiệp vụ.

Cần thêm test:

- Login thành công/thất bại.
- Refresh token đúng/sai loại token.
- Employee chỉ xem record của mình.
- Doctor approve record đang pending.
- Doctor không approve record chưa pending.
- Reject medical record.
- Upload file sai type.
- Search patient theo BHYT.

### 2.6. README và tài liệu

Trạng thái: `[~]` Đã làm một phần.

Đã làm:

- Đã sửa Java 21.
- Đã sửa Swagger path.
- Đã thêm Docker Compose.
- Đã thêm thông tin profile.

Cần làm tiếp:

- Sửa lỗi encoding tiếng Việt.
- Không liệt kê Dashboard/AI logs như chức năng đã xong nếu code chưa có.
- Tách mục "Đã hoàn thành" và "Roadmap".

### 2.7. Database migration

Trạng thái: `[ ]` Chưa làm.

Cần làm:

- Thêm Flyway hoặc Liquibase.
- Tạo migration cho schema hiện tại.
- Dev có thể dùng `ddl-auto=update`.
- Prod nên dùng `ddl-auto=validate`.

### 2.8. Logic sinh mã bệnh án

Trạng thái: `[ ]` Chưa làm.

Hiện tại vẫn dùng `Math.random()` và check tồn tại.

Cần sửa:

- Dùng DB sequence.
- Hoặc UUID/ULID.
- Hoặc bắt lỗi unique constraint và retry có giới hạn.

### 2.9. Format ngày tháng

Trạng thái: `[ ]` Chưa làm đầy đủ.

Hiện tại:

- Patient dùng `yyyy-MM-dd`.
- Account vẫn dùng `dd/MM/yyyy`.

Cần sửa:

- Chuẩn hóa toàn bộ request date về ISO `yyyy-MM-dd`.
- Ghi rõ trong Swagger/README.

### 2.10. Audit log

Trạng thái: `[ ]` Chưa làm.

Cần thêm:

- Entity `AuditLog`.
- Repository.
- Service ghi log.
- API cho admin xem log.
- Ghi log khi upload/update/submit/approve/reject/view patient.

## 3. Chức năng nên làm thêm

### 3.1. Audit Log

Trạng thái: `[x]` Đã làm.

Ưu tiên: Rất cao.

Bảng gợi ý:

```text
audit_logs
- id
- actor_id
- action
- resource_type
- resource_id
- old_value jsonb
- new_value jsonb
- ip_address
- user_agent
- created_at
```

API gợi ý:

```text
GET /api/v1/audit-logs
GET /api/v1/audit-logs/{id}
GET /api/v1/medical-record/{id}/audit-logs
```

### 3.2. Reject Medical Record

Trạng thái: `[x]` Đã làm.

Đã có:

- `RejectMedicalRecordReq`.
- Message i18n liên quan reject.
- Swagger example có nhắc reject.

Chưa có:

- Enum `REJECTED` trong `MedicalRecordStatus`.
- Field `rejectedBy`, `rejectedAt`, `rejectionReason`.
- Method service `reject()`.
- Endpoint controller reject.
- Resubmit flow.

Cần thêm:

```text
PUT /api/v1/medical-record/{id}/reject
PUT /api/v1/medical-record/{id}/resubmit
```

### 3.3. OCR Confidence và Manual Verification

Trạng thái: `[ ]` Chưa làm.

Cần thêm:

- Confidence score cho từng field.
- Trạng thái verified cho từng field.
- Người verify và thời điểm verify.
- Không cho submit nếu field bắt buộc chưa verified.

### 3.4. Dashboard API

Trạng thái: `[x]` Đã làm.

Hiện tại:

- Có permission/menu `dashboard`.
- README có nhắc dashboard.

Chưa có:

- Dashboard controller/service/repository query.
- API thống kê thật.

API nên thêm:

```text
GET /api/v1/dashboard/overview
GET /api/v1/dashboard/records-by-status
GET /api/v1/dashboard/records-by-department
GET /api/v1/dashboard/ocr-quality
GET /api/v1/dashboard/user-performance
```

### 3.5. Notification

Trạng thái: `[x]` Đã làm hoàn chỉnh (Event-Driven).

Đã thêm:

- Entity `Notification` + `Webhook`.
- API: list/unread-count/read/read-all/delete notifications.
- API: admin webhooks CRUD.
- Event-Driven Architecture: MedicalRecordService → publish events
- Listeners: AuditLogEventListener, NotificationEventListener, WebhookEventListener (@Async).
- Notification tạo tự động khi submit/approve/reject.
- Webhook callbacks async POST tới external services.
- HMAC-SHA256 signing cho webhook security.
- Database migration V5 (notifications + webhooks tables).

### 3.6. Search nâng cao

Trạng thái: `[ ]` Chưa làm đầy đủ.

Hiện có search cơ bản.

Cần bổ sung filter:

- Tên bệnh nhân.
- BHYT.
- Khoa/phòng.
- Trạng thái.
- Người upload.
- Người duyệt.
- Khoảng ngày upload.
- Khoảng ngày duyệt.
- Chẩn đoán.

### 3.7. Version history

Trạng thái: `[ ]` Chưa làm.

Cần thêm nếu còn thời gian:

```text
medical_record_versions
- id
- medical_record_id
- version_no
- data_snapshot jsonb
- created_by
- created_at
- note
```

### 3.8. Secure File Access

Trạng thái: `[ ]` Chưa làm.

Cần thay public URL bằng API có phân quyền:

```text
GET /api/v1/medical-record/{id}/file
```

## 4. Gói ưu tiên tiếp theo

### Làm ngay để tránh bị bắt bẻ

1. `[ ]` Sửa `AccountSeeder`: không hardcode/reset admin password.
2. `[ ]` Thêm `application-test.yaml` để test không phụ thuộc DB local.
3. `[ ]` Sửa token type cho access/refresh.
4. `[ ]` Chặn public file bệnh án.
5. `[ ]` Thêm reject medical record thật.
6. `[ ]` Sửa README encoding và bỏ các mục chưa làm ra khỏi "đã có".

### Làm để tăng điểm đồ án

1. `[x]` Audit log (refactored to Event-Driven).
2. `[x]` Dashboard overview.
3. `[x]` Notification module (Event-Driven + Webhook).
4. `[ ]` OCR confidence/manual verification.
5. `[ ]` Search nâng cao.

### Làm nếu còn thời gian

1. `[ ]` Flyway migration.
2. `[ ]` Version history.
3. `[ ]` Testcontainers PostgreSQL.
4. `[ ]` Secure file streaming với permission chi tiết.

## 4.5. Implementation Details — Event-Driven Architecture

### Refactoring AuditLog từ Direct Call sang Event-Driven

**Before (Cách 2: Direct):**
```java
MedicalRecordService.submitForReview() {
    record.setStatus(PENDING_DOCTOR_REVIEW);
    medicalRecordRepository.save(record);
    auditLogService.log(ACTION_SUBMIT, ...);  // ❌ Direct call
}
```

**After (Cách 1: Event-Driven):**
```java
MedicalRecordService.submitForReview() {
    record.setStatus(PENDING_DOCTOR_REVIEW);
    record = medicalRecordRepository.save(record);
    eventPublisher.publishEvent(
        new MedicalRecordSubmittedEvent(this, record, actorId)
    );  // ✅ Event publish
}

@Component
@EventListener
public class AuditLogEventListener {
    public void onMedicalRecordEvent(MedicalRecordEvent event) {
        auditLogService.log(
            event.getAction(),
            event.getRecord().getId(),
            event.getOldValue(),
            event.getNewValue()
        );  // ✅ Listener ghi log
    }
}
```

### Event Classes Tạo

| Class | Trigger | oldValue | newValue |
|-------|---------|----------|----------|
| `MedicalRecordUploadedEvent` | POST /upload | null | `{"status":"Extracted"}` |
| `MedicalRecordSubmittedEvent` | PUT /submit | `{"status":"Extracted"}` | `{"status":"Pending..."}` |
| `MedicalRecordApprovedEvent` | PUT /approve | `{"status":"Pending..."}` | `{"status":"Approved"}` |
| `MedicalRecordRejectedEvent` | PUT /reject | `{"status":"Pending..."}` | `{"status":"Rejected","reason":"..."}` |
| `MedicalRecordResubmittedEvent` | PUT /resubmit | `{"status":"Rejected"}` | `{"status":"Pending..."}` |
| `MedicalRecordDeletedEvent` | DELETE | `{"status":"..."}` | null |

### 3 Listeners Chạy Parallel

1. **AuditLogEventListener** → Ghi audit log vào DB
2. **NotificationEventListener** → Tạo notification cho users
3. **WebhookEventListener (@Async)** → POST HTTP async không block

### Database Tables

- `notifications` - Lưu thông báo cho users
- `webhooks` - Config webhooks (admin)

### File Structure

```
src/main/java/com/example/medical_be/
├── event/
│   ├── MedicalRecordEvent.java
│   ├── MedicalRecordUploadedEvent.java
│   ├── MedicalRecordSubmittedEvent.java
│   ├── MedicalRecordApprovedEvent.java
│   ├── MedicalRecordRejectedEvent.java
│   ├── MedicalRecordResubmittedEvent.java
│   └── MedicalRecordDeletedEvent.java
├── listener/
│   ├── AuditLogEventListener.java
│   ├── NotificationEventListener.java
│   └── WebhookEventListener.java
├── entity/
│   ├── Notification.java
│   └── Webhook.java
├── service/
│   ├── NotificationService.java
│   ├── WebhookService.java
│   └── MedicalRecordService.java (refactored)
├── controller/
│   ├── NotificationController.java
│   └── WebhookController.java
├── config/
│   └── AsyncConfig.java (ThreadPoolTaskExecutor)
└── dto/
    ├── res/
    │   ├── NotificationRes.java
    │   └── WebhookRes.java
    └── req/webhook/
        ├── CreateWebhookReq.java
        └── UpdateWebhookReq.java
```

---

## 6. Câu chuyện bảo vệ nên trình bày

Không nên nói "em thêm nhiều chức năng". Nên nói:

> Ban đầu hệ thống dừng ở upload OCR và quản lý bệnh án. Sau khi phân tích nghiệp vụ bệnh viện, em bổ sung:
> 
> 1. **Quy trình kiểm duyệt** (submit → approve/reject)
> 2. **Audit log** để truy vết dữ liệu y tế
> 3. **Dashboard** để quản trị tiến độ
> 4. **Notification** để user biết status change
> 5. **Webhook integration** để tích hợp external systems (ERP, email services, etc.)
> 
> Ngoài ra, em refactor **AuditLog và Notification thành Event-Driven Architecture** để:
> - ✅ Decoupling: MedicalRecordService không trực tiếp phụ thuộc Notification/AuditLog
> - ✅ Scalability: Có thể thêm listeners mới mà không sửa MedicalRecordService
> - ✅ Async webhooks: HTTP callbacks không block main thread
> - ✅ Enterprise pattern: Sử dụng Spring Events publish-subscribe model
>
> Các thay đổi này giúp hệ thống không chỉ là CRUD mà mô phỏng được quy trình xử lý bệnh án số có kiểm soát, đồng thời có kiến trúc sạch, mở rộng được.

## 6. Ghi chú về trạng thái test

Lần rà soát này đã chạy:

```text
mvn test
```

Kết quả:

```text
BUILD SUCCESS
Tests run: 1, Failures: 0, Errors: 0
```

Đánh giá: project compile và Spring context start được trên môi trường hiện tại, nhưng test coverage vẫn rất thấp.
