CREATE TABLE webhooks (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    event_types TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    secret VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_webhooks_is_active ON webhooks(is_active);
