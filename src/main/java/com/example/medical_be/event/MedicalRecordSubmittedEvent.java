package com.example.medical_be.event;

import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.service.AuditLogService;

public class MedicalRecordSubmittedEvent extends MedicalRecordEvent {

    public MedicalRecordSubmittedEvent(Object source, MedicalRecord record, Long actorId) {
        super(source, record, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_SUBMIT;
    }

    @Override
    public String getOldValue() {
        return "{\"status\":\"Extracted\"}";
    }

    @Override
    public String getNewValue() {
        return "{\"status\":\"Pending Doctor Review\"}";
    }
}
