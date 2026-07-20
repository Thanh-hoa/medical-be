-- permission.sort được V3 tạo là INTEGER, nhưng entity Permission từng khai sai kiểu String,
-- khiến Hibernate (ddl-auto=update) tự đổi cột này thành VARCHAR trên các DB chạy lâu ngày.
-- Entity đã sửa lại đúng Integer, nên đưa cột về đúng INTEGER như migration gốc.
ALTER TABLE permission
    ALTER COLUMN sort TYPE INTEGER USING sort::INTEGER;
