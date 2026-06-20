package com.example.medical_be.event;

import com.example.medical_be.entity.Prescription;
import com.example.medical_be.service.AuditLogService;

public class PrescriptionIssuedEvent extends PrescriptionEvent {

    public PrescriptionIssuedEvent(Object source, Prescription prescription, Long actorId) {
        super(source, prescription, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_PRESCRIPTION_ISSUE;
    }

    @Override
    public String getOldValue() {
        return "{\"status\":\"DRAFT\"}";
    }

    @Override
    public String getNewValue() {
        return String.format("{\"status\":\"ISSUED\",\"issuedAt\":\"%s\"}",
                getPrescription().getIssuedAt());
    }
}
