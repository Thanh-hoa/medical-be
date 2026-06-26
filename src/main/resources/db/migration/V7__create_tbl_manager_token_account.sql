CREATE TABLE tbl_manager_token_account (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255),
    is_acitve BOOLEAN NOT NULL,
    expired_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    account_id BIGINT,
    CONSTRAINT fk_manager_token_account_account
        FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE INDEX idx_manager_token_account_account_id ON tbl_manager_token_account(account_id);
CREATE INDEX idx_manager_token_account_token ON tbl_manager_token_account(token);
