-- IF NOT EXISTS vì 1 số môi trường (vd. DB dev chạy lâu nay với ddl-auto=update) đã có
-- sẵn các cột này do Hibernate tự thêm từ trước; script vẫn phải chạy an toàn trên DB mới.
ALTER TABLE account
    ADD COLUMN IF NOT EXISTS photo_url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
