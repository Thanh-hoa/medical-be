CREATE TABLE permission_role (
    id BIGSERIAL PRIMARY KEY,
    permission_id BIGINT,
    permission_action_id BIGINT,
    role_id BIGINT,
    CONSTRAINT fk_permission_role_permission
        FOREIGN KEY (permission_id) REFERENCES permission(id),
    CONSTRAINT fk_permission_role_permission_action
        FOREIGN KEY (permission_action_id) REFERENCES permission_action(id),
    CONSTRAINT fk_permission_role_role
        FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE INDEX idx_permission_role_permission_id ON permission_role(permission_id);
CREATE INDEX idx_permission_role_permission_action_id ON permission_role(permission_action_id);
CREATE INDEX idx_permission_role_role_id ON permission_role(role_id);
