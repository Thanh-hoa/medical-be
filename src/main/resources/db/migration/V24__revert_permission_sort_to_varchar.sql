-- Đảo ngược V23: quyết định giữ permission.sort là VARCHAR (không phải INTEGER).
-- Không sửa trực tiếp V23 vì migration đó đã chạy thật trên DB dev, sửa sẽ gây
-- lỗi checksum mismatch cho Flyway trên các DB đã áp dụng V23.
ALTER TABLE permission
    ALTER COLUMN sort TYPE VARCHAR(255) USING sort::VARCHAR;
