package com.example.medical_be.entity.enums;

public enum MedicalRecordStatus {
    PROCESSING("Processing"),
    EXTRACTED("Extracted"),
    PENDING_DOCTOR_REVIEW("Pending Doctor Review"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String dbValue;

    MedicalRecordStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static MedicalRecordStatus fromDbValue(String value) {
        for (MedicalRecordStatus s : values()) {
            if (s.dbValue.equals(value)) return s;
        }
        throw new IllegalArgumentException("Unknown MedicalRecordStatus: " + value);
    }
}
