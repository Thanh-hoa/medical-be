package com.example.medical_be.entity.enums;

public enum PrescriptionStatus {
    DRAFT("Draft"),
    ISSUED("Issued");

    private final String dbValue;

    PrescriptionStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static PrescriptionStatus fromDbValue(String value) {
        for (PrescriptionStatus s : values()) {
            if (s.dbValue.equals(value)) return s;
        }
        throw new IllegalArgumentException("Unknown PrescriptionStatus: " + value);
    }
}
