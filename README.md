# MED-OCR Backend

Backend Spring Boot cho hệ thống quản lý bệnh án và trích xuất thông tin từ ảnh hồ sơ y tế. Ứng dụng lưu dữ liệu nghiệp vụ trong PostgreSQL, gọi OCR Model Service để xử lý ảnh và lưu ảnh upload lên AWS S3.

## Công Nghệ

- Java 21, Spring Boot 3.2.5
- Spring Security, JWT
- PostgreSQL 15
- JPA/Hibernate, Flyway
- Springdoc OpenAPI/Swagger
- Docker, Docker Compose
- Nginx, Certbot/Let's Encrypt
- AWS S3

## Kiến Trúc Production

```text
Client / Frontend
        |
        v
https://api.medicalocr-nthoa.io.vn
        |
        v
Nginx trên EC2
        |
        v
Spring Boot container :8080
        |
        +--> PostgreSQL container :5432
        +--> OCR Model Service: https://model.medicalocr-nthoa.io.vn
        +--> AWS S3 bucket
```

PostgreSQL chạy bằng Docker container và lưu dữ liệu trong Docker volume `postgres_data`. Backend kết nối database qua Docker network bằng hostname `db`.

## Cấu Hình Môi Trường

Tạo file `.env` từ file mẫu:

```bash
cp .env.example .env
```

Các biến quan trọng:

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_db_password

APP_KEY=your_jwt_secret
ENCRYPTION_SECRET_KEY=your_encryption_secret

APP_SERVER_URL=https://api.medicalocr-nthoa.io.vn
OCR_API_URL=https://model.medicalocr-nthoa.io.vn

AWS_REGION=ap-southeast-2
AWS_BUCKET_NAME=medicaler-image
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
```

Không commit file `.env` lên Git.

## Chạy Bằng Docker Compose

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

Các service chính:

- `app`: Spring Boot backend.
- `db`: PostgreSQL 15.
- `postgres_data`: Docker volume lưu dữ liệu database.

Trong Docker Compose, backend dùng JDBC URL:

```text
jdbc:postgresql://db:5432/medical
```

## Deploy Trên EC2

Clone source:

```bash
mkdir -p ~/apps
cd ~/apps
git clone https://github.com/Thanh-hoa/medical-be.git medical-be
cd medical-be
```

Tạo `.env`:

```bash
cp .env.example .env
nano .env
```

Tạo secret:

```bash
openssl rand -base64 32
```

Khởi chạy backend và database:

```bash
sudo docker compose up -d --build
sudo docker compose ps
```

Kiểm tra log:

```bash
sudo docker compose logs -f app
```

## Restore Database

Upload file backup, ví dụ `medical_backup.sql`, lên EC2:

```text
/home/ubuntu/medical_backup.sql
```

Restore vào PostgreSQL container:

```bash
cat ~/medical_backup.sql | sudo docker compose exec -T db psql -U postgres -d medical
```

Kiểm tra dữ liệu:

```bash
sudo docker compose exec db psql -U postgres -d medical
```

Trong `psql`:

```sql
\dt
select count(*) from account;
select count(*) from patients;
select count(*) from medical_records;
\q
```

Không dùng lệnh sau trên production nếu không muốn xóa database:

```bash
sudo docker compose down -v
```

## Nginx Và HTTPS

Nginx proxy domain vào backend:

```nginx
server {
    listen 80;
    server_name api.medicalocr-nthoa.io.vn;

    client_max_body_size 25M;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Cấp HTTPS bằng Certbot:

```bash
sudo certbot --nginx -d api.medicalocr-nthoa.io.vn
sudo certbot renew --dry-run
```

AWS Security Group nên mở:

```text
22   SSH
80   HTTP
443  HTTPS
```

Không public port `5432` và `8080`.

## Lưu Ảnh Lên AWS S3

Ảnh upload được xử lý trong `FileStorageService`.

Luồng xử lý:

1. Kiểm tra định dạng file: `jpg`, `jpeg`, `png`, `webp`.
2. Tạo tên file mới bằng UUID.
3. Upload file lên S3 bucket.
4. Trả về URL ảnh.

URL trả về có dạng:

```text
https://{bucket}.s3.{region}.amazonaws.com/{filename}
```

Ví dụ:

```text
https://medicaler-image.s3.ap-southeast-2.amazonaws.com/image-id.png
```

Credential S3:

- Local/dev: dùng `AWS_ACCESS_KEY_ID` và `AWS_SECRET_ACCESS_KEY`.
- Production trên EC2: nên dùng IAM Role cho EC2 và để trống access key trong `.env`.

Quyền IAM tối thiểu:

```json
{
  "Effect": "Allow",
  "Action": [
    "s3:PutObject"
  ],
  "Resource": "arn:aws:s3:::medicaler-image/*"
}
```

## API Chính

### Auth

| Method | Endpoint | Mô tả |
|--------|----------|------|
| POST | `/api/v1/auth/login` | Đăng nhập |
| POST | `/api/v1/auth/logout` | Đăng xuất |
| POST | `/api/v1/auth/refresh-token` | Làm mới token |

### Medical Record

| Method | Endpoint | Mô tả |
|--------|----------|------|
| POST | `/api/v1/medical-record/upload` | Upload ảnh và gọi OCR |
| GET | `/api/v1/medical-record/list` | Danh sách bệnh án |
| GET | `/api/v1/medical-record/{id}` | Chi tiết bệnh án |
| PUT | `/api/v1/medical-record/update` | Cập nhật bệnh án |
| DELETE | `/api/v1/medical-record/{id}` | Xóa mềm bệnh án |

## Swagger

Local:

```text
http://localhost:8080/documents/swagger-ui/index.html
```

Production:

```text
https://api.medicalocr-nthoa.io.vn/documents/swagger-ui/index.html
```

Swagger có thể được bật/tắt trong `application-prod.yaml`.

## CI

GitHub Actions CI nằm tại:

```text
.github/workflows/ci.yml
```

CI dùng PostgreSQL service và chạy:

```bash
./mvnw clean verify
```

## Vận Hành

Start lại backend:

```bash
cd ~/apps/medical-be
sudo docker compose up -d
sudo docker compose ps
```

Deploy code mới:

```bash
cd ~/apps/medical-be
git pull
sudo docker compose up -d --build app
sudo docker compose logs -f app
```

Backup database:

```bash
mkdir -p ~/backups/medical-be
sudo docker compose exec -T db pg_dump -U postgres -d medical > ~/backups/medical-be/medical_$(date +%F_%H-%M).sql
```