CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(255),
    is_active BOOLEAN,
    is_super_admin BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_role_code ON role(code);
CREATE INDEX idx_role_is_active ON role(is_active);
