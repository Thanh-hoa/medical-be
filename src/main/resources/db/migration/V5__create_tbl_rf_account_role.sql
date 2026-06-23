CREATE TABLE tbl_rf_account_role (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT,
    role_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_rf_account_role_account
        FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT fk_rf_account_role_role
        FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE INDEX idx_rf_account_role_account_id ON tbl_rf_account_role(account_id);
CREATE INDEX idx_rf_account_role_role_id ON tbl_rf_account_role(role_id);
