# CALL API — Reset Password & Change Password Flow

> Mô tả **đầu vào / đầu ra** của các API liên quan đến đặt lại và đổi mật khẩu.  
> Base URL: `http://localhost:8080/api/v1`

---

## Tổng quan các endpoint

| # | Method | Endpoint | Xác thực | Mô tả |
|---|--------|----------|----------|-------|
| 1 | POST | `/auth/forgot-password` | Không cần | Gửi email chứa link đặt lại mật khẩu (hết hạn sau 15 phút) |
| 2 | POST | `/auth/reset-password` | Không cần | Đặt lại mật khẩu bằng token nhận từ email |
| 3 | PUT | `/account/change-password` | `Bearer <token>` | Đổi mật khẩu khi đã đăng nhập (vào trang Profile) |

---

## 1. Quên mật khẩu

### `POST /auth/forgot-password`

**Xác thực:** Không cần token.

> Gửi email chứa link đặt lại mật khẩu đến địa chỉ email của người dùng.  
> Nếu email không tồn tại trong hệ thống, API vẫn trả về thành công — **không tiết lộ email có tồn tại hay không** (tránh email enumeration attack).  
> Link trong email có dạng: `http://localhost:3000/reset-password?token=<token>`  
> Token hết hạn sau **15 phút**.

**Request body:**

```json
{
    "email": "user@example.com"
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `email` | string | ✅ | Email đã đăng ký tài khoản |

**Response (200):**

```json
{
    "isError": false,
    "message": "Nếu email tồn tại, link đặt lại mật khẩu đã được gửi"
}
```

> Response luôn trả về `200` dù email có tồn tại hay không.

---

## 2. Đặt lại mật khẩu

### `POST /auth/reset-password`

**Xác thực:** Không cần token.

> FE lấy `token` từ query param trong link email (`?token=...`) rồi gửi cùng mật khẩu mới.  
> Token chỉ hợp lệ trong **15 phút** kể từ khi được tạo.

**Request body:**

```json
{
    "token": "dXNlckBleGFtcGxlLmNvbToxNzUwMDAwMDAwMDAwOmEzZjliMmMx...",
    "password": "matkhaumoi123",
    "repeat_password": "matkhaumoi123"
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `token` | string | ✅ | Token nhận từ link trong email |
| `password` | string | ✅ | Mật khẩu mới |
| `repeat_password` | string | ✅ | Xác nhận mật khẩu mới (phải khớp với `password`) |

**Response (200):**

```json
{
    "isError": false,
    "message": "Đặt lại mật khẩu thành công"
}
```

**Response lỗi (400) — token không hợp lệ hoặc hết hạn:**

```json
{
    "isError": true,
    "message": "Token không hợp lệ"
}
```

**Response lỗi (400) — mật khẩu không khớp:**

```json
{
    "isError": true,
    "message": "Mật khẩu không khớp"
}
```

---

## 3. Đổi mật khẩu (khi đã đăng nhập)

### `PUT /account/change-password`

**Xác thực:** Cần header `Authorization: Bearer <token>`.

> Dùng khi user đã đăng nhập và muốn đổi mật khẩu từ trang Profile.  
> Yêu cầu nhập đúng mật khẩu hiện tại trước khi đặt mật khẩu mới.

**Request body:**

```json
{
    "current_password": "matkhaucu123",
    "new_password": "matkhaumoi456",
    "repeat_new_password": "matkhaumoi456"
}
```

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `current_password` | string | ✅ | Mật khẩu hiện tại |
| `new_password` | string | ✅ | Mật khẩu mới |
| `repeat_new_password` | string | ✅ | Xác nhận mật khẩu mới (phải khớp với `new_password`) |

**Response (200):**

```json
{
    "isError": false,
    "message": "Đổi mật khẩu thành công"
}
```

**Response lỗi (400) — mật khẩu hiện tại sai:**

```json
{
    "isError": true,
    "message": "Mật khẩu không chính xác"
}
```

**Response lỗi (400) — mật khẩu mới không khớp:**

```json
{
    "isError": true,
    "message": "Mật khẩu không khớp"
}
```

**Response lỗi (401) — chưa đăng nhập:**

```json
{
    "isError": true,
    "message": "Bạn không có quyền truy cập tài nguyên này"
}
```

---

## Xử lý lỗi chung

| HTTP Status | Ý nghĩa | FE xử lý |
|-------------|---------|----------|
| 400 | Dữ liệu sai / token hết hạn / mật khẩu không khớp | Hiện lỗi từ `message` |
| 401 | Chưa đăng nhập / token hết hạn | Chuyển về trang đăng nhập |
| 500 | Lỗi server | Hiện thông báo lỗi chung |

---

## Thứ tự gọi API điển hình

```
[Quên mật khẩu]
    POST /auth/forgot-password          { "email": "user@example.com" }
        → Hệ thống gửi email chứa link reset
    (User mở email, click link → FE lấy ?token=... từ URL)
    POST /auth/reset-password           { "token": "...", "password": "...", "repeat_password": "..." }
        → Đặt lại mật khẩu thành công → chuyển sang trang đăng nhập

[Đổi mật khẩu khi đã đăng nhập]
    PUT  /account/change-password       { "current_password": "...", "new_password": "...", "repeat_new_password": "..." }
        → Đổi mật khẩu thành công
```

---

## Cấu trúc Token Reset Password

Token được mã hoá theo cấu trúc:

```
Base64Url( email : timestamp_ms : HmacSHA256(email:timestamp_ms) )
```

- `timestamp_ms`: thời điểm tạo token (milliseconds)
- Token hết hạn sau **15 phút** kể từ `timestamp_ms`
- Token không thể tái sử dụng sau khi hết hạn
