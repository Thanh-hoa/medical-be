package com.example.medical_be.event;

import com.example.medical_be.entity.Prescription;
import com.example.medical_be.service.AuditLogService;

public class PrescriptionCreatedEvent extends PrescriptionEvent {

    public PrescriptionCreatedEvent(Object source, Prescription prescription, Long actorId) {
        super(source, prescription, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_PRESCRIPTION_CREATE;
    }

    @Override
    public String getOldValue() {
        return null;
    }

    @Override
    public String getNewValue() {
        return String.format("{\"status\":\"DRAFT\",\"prescriptionNumber\":\"%s\"}",
                getPrescription().getPrescriptionNumber());
    }
}
