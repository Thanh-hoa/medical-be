CREATE TABLE medicines (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    strength VARCHAR(100),
    unit VARCHAR(50),
    dosage_form VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_medicines_code ON medicines(code);
CREATE INDEX idx_medicines_name ON medicines(name);
CREATE INDEX idx_medicines_is_active ON medicines(is_active);
