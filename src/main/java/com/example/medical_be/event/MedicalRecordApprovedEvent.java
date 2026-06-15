package com.example.medical_be.event;

import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.service.AuditLogService;

public class MedicalRecordApprovedEvent extends MedicalRecordEvent {

    public MedicalRecordApprovedEvent(Object source, MedicalRecord record, Long actorId) {
        super(source, record, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_APPROVE;
    }

    @Override
    public String getOldValue() {
        return "{\"status\":\"Pending Doctor Review\"}";
    }

    @Override
    public String getNewValue() {
        return "{\"status\":\"Approved\"}";
    }
}
