-- Encrypted values include an IV, PKCS5 padding, and Base64 encoding.
-- Keep encrypted patient fields large enough for their ciphertext.
ALTER TABLE patients
    ALTER COLUMN bhyt TYPE VARCHAR(500),
    ALTER COLUMN name TYPE VARCHAR(500),
    ALTER COLUMN phone TYPE VARCHAR(500);
