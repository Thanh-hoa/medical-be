CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    birthday DATE,
    phone_number VARCHAR(12),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN,
    is_delete BOOLEAN DEFAULT FALSE,
    email_verify_at TIMESTAMP,
    created_by BIGINT,
    gender SMALLINT DEFAULT 2
);

CREATE INDEX idx_account_email ON account(email);
CREATE INDEX idx_account_username ON account(username);
CREATE INDEX idx_account_is_delete ON account(is_delete);
