package com.example.medical_be.event;

import com.example.medical_be.entity.Prescription;
import com.example.medical_be.service.AuditLogService;

public class PrescriptionSavedDraftEvent extends PrescriptionEvent {

    public PrescriptionSavedDraftEvent(Object source, Prescription prescription, Long actorId) {
        super(source, prescription, actorId);
    }

    @Override
    public String getAction() {
        return AuditLogService.ACTION_PRESCRIPTION_SAVE_DRAFT;
    }

    @Override
    public String getOldValue() {
        return null;
    }

    @Override
    public String getNewValue() {
        int itemCount = getPrescription().getItems() != null ? getPrescription().getItems().size() : 0;
        return String.format("{\"status\":\"DRAFT\",\"itemCount\":%d}", itemCount);
    }
}
