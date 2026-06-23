CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    bhyt VARCHAR(500) NOT NULL UNIQUE,
    name VARCHAR(500) NOT NULL,
    dob VARCHAR(500),
    gender VARCHAR(10),
    address TEXT,
    phone VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_patients_name ON patients(name);
CREATE INDEX idx_patients_phone ON patients(phone);
