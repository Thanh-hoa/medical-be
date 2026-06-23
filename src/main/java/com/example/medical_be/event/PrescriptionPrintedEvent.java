package com.example.medical_be.event;

import com.example.medical_be.entity.Prescription;
import com.example.medical_be.service.AuditLogService;

public class PrescriptionPrintedEvent extends PrescriptionEvent {

    public PrescriptionPrintedEvent(Object source, Prescription prescription, Long actorId) {
        super(source, prescription, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_PRESCRIPTION_PRINT;
    }

    @Override
    public String getOldValue() {
        return null;
    }

    @Override
    public String getNewValue() {
        return String.format("{\"prescriptionNumber\":\"%s\"}",
                getPrescription().getPrescriptionNumber());
    }
}
