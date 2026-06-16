package com.example.medical_be.event;

import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.service.AuditLogService;
import lombok.Getter;

@Getter
public class MedicalRecordRejectedEvent extends MedicalRecordEvent {
    private final String rejectionReason;

    public MedicalRecordRejectedEvent(Object source, MedicalRecord record, Long actorId, String rejectionReason) {
        super(source, record, actorId);
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_REJECT;
    }

    @Override
    public String getOldValue() {
        return "{\"status\":\"Pending Doctor Review\"}";
    }

    @Override
    public String getNewValue() {
        return "{\"status\":\"Rejected\",\"reason\":\"" + rejectionReason.replace("\"", "'") + "\"}";
    }
}
