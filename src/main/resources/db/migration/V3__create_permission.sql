CREATE TABLE permission (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    sort INTEGER,
    is_hidden BOOLEAN,
    slug VARCHAR(255)
);

CREATE INDEX idx_permission_slug ON permission(slug);
CREATE INDEX idx_permission_sort ON permission(sort);
