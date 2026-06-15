package com.example.medical_be.event;

import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.service.AuditLogService;

public class MedicalRecordUploadedEvent extends MedicalRecordEvent {

    public MedicalRecordUploadedEvent(Object source, MedicalRecord record, Long actorId) {
        super(source, record, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_UPLOAD;
    }

    @Override
    public String getOldValue() {
        return null;
    }

    @Override
    public String getNewValue() {
        return "{\"status\":\"Extracted\"}";
    }
}
