CREATE TABLE permission_action (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255)
);

CREATE INDEX idx_permission_action_code ON permission_action(code);
