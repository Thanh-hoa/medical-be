package com.example.medical_be.entity.enums;

public enum PrescriptionDurationOption {
    ONE_WEEK(7),
    TWO_WEEKS(14),
    THREE_WEEKS(21),
    ONE_MONTH(30),
    CUSTOM(null);

    private final Integer defaultDays;

    PrescriptionDurationOption(Integer defaultDays) {
        this.defaultDays = defaultDays;
    }

    public Integer getDefaultDays() {
        return defaultDays;
    }
}
