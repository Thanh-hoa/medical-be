package com.example.medical_be.event;

import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.service.AuditLogService;
import lombok.Getter;

@Getter
public class MedicalRecordDeletedEvent extends MedicalRecordEvent {

    public MedicalRecordDeletedEvent(Object source, MedicalRecord record, Long actorId) {
        super(source, record, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_DELETE;
    }

    @Override
    public String getOldValue() {
        return "{\"status\":\"" + getRecord().getStatus().getDbValue() + "\"}";
    }

    @Override
    public String getNewValue() {
        return null;
    }
}
