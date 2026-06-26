package com.example.medical_be.event;

import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.service.AuditLogService;

public class MedicalRecordResubmittedEvent extends MedicalRecordEvent {

    public MedicalRecordResubmittedEvent(Object source, MedicalRecord record, Long actorId) {
        super(source, record, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_RESUBMIT;
    }

    @Override
    public String getOldValue() {
        return "{\"status\":\"Rejected\"}";
    }

    @Override
    public String getNewValue() {
        return "{\"status\":\"Pending Doctor Review\"}";
    }
}
