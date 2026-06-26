CREATE TABLE prescriptions (
    id BIGSERIAL PRIMARY KEY,
    prescription_number VARCHAR(50) UNIQUE,
    medical_record_id BIGINT NOT NULL,
    patient_id BIGINT,
    doctor_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    hospital_name VARCHAR(255),
    receiver_name VARCHAR(255),
    insurance_code VARCHAR(50),
    receiver_address TEXT,
    diagnosis TEXT,
    duration_option VARCHAR(20),
    duration_days INTEGER,
    advice TEXT,
    issued_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_prescriptions_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_records(id),
    CONSTRAINT fk_prescriptions_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_prescriptions_doctor
        FOREIGN KEY (doctor_id) REFERENCES account(id)
);

CREATE INDEX idx_prescriptions_prescription_number ON prescriptions(prescription_number);
CREATE INDEX idx_prescriptions_medical_record_id ON prescriptions(medical_record_id);
CREATE INDEX idx_prescriptions_patient_id ON prescriptions(patient_id);
CREATE INDEX idx_prescriptions_doctor_id ON prescriptions(doctor_id);
CREATE INDEX idx_prescriptions_status ON prescriptions(status);
CREATE INDEX idx_prescriptions_created_at ON prescriptions(created_at);
