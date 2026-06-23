CREATE TABLE medical_records (
    id BIGSERIAL PRIMARY KEY,
    record_number VARCHAR(50) UNIQUE,
    uploaded_by BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_type VARCHAR(10),
    original_image_path VARCHAR(500),
    patient_id BIGINT,
    department VARCHAR(100),
    record_type VARCHAR(50),
    status VARCHAR(30) DEFAULT 'Processing',
    notes TEXT,
    approved_by BIGINT,
    approved_at TIMESTAMP,
    verified_by BIGINT,
    verified_at TIMESTAMP,
    extracted_data JSONB,
    lab_data JSONB,
    rejected_by BIGINT,
    rejected_at TIMESTAMP,
    rejection_reason TEXT,
    is_delete BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_medical_records_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES account(id),
    CONSTRAINT fk_medical_records_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_medical_records_approved_by
        FOREIGN KEY (approved_by) REFERENCES account(id),
    CONSTRAINT fk_medical_records_verified_by
        FOREIGN KEY (verified_by) REFERENCES account(id),
    CONSTRAINT fk_medical_records_rejected_by
        FOREIGN KEY (rejected_by) REFERENCES account(id)
);

CREATE INDEX idx_medical_records_record_number ON medical_records(record_number);
CREATE INDEX idx_medical_records_uploaded_by ON medical_records(uploaded_by);
CREATE INDEX idx_medical_records_patient_id ON medical_records(patient_id);
CREATE INDEX idx_medical_records_status ON medical_records(status);
CREATE INDEX idx_medical_records_is_delete ON medical_records(is_delete);
CREATE INDEX idx_medical_records_created_at ON medical_records(created_at);
