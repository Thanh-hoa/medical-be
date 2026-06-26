CREATE TABLE prescription_items (
    id BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_id BIGINT,
    medicine_name VARCHAR(255) NOT NULL,
    strength VARCHAR(100),
    unit VARCHAR(50),
    quantity INTEGER,
    morning_dose VARCHAR(100),
    noon_dose VARCHAR(100),
    afternoon_dose VARCHAR(100),
    evening_dose VARCHAR(100),
    instruction TEXT,
    sort_order INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_prescription_items_prescription
        FOREIGN KEY (prescription_id) REFERENCES prescriptions(id)
);

CREATE INDEX idx_prescription_items_prescription_id ON prescription_items(prescription_id);
CREATE INDEX idx_prescription_items_medicine_id ON prescription_items(medicine_id);
CREATE INDEX idx_prescription_items_sort_order ON prescription_items(sort_order);
