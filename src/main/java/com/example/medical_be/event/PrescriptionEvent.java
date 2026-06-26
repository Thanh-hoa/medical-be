package com.example.medical_be.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEvent;

import com.example.medical_be.entity.Prescription;

import lombok.Getter;

@Getter
public abstract class PrescriptionEvent extends ApplicationEvent {

    private final Prescription prescription;
    private final Long actorId;
    private final LocalDateTime occurredAt;

    public PrescriptionEvent(Object source, Prescription prescription, Long actorId) {
        super(source);
        this.prescription = prescription;
        this.actorId = actorId;
        this.occurredAt = LocalDateTime.now();
    }

    public abstract String getAction();
    public abstract String getOldValue();
    public abstract String getNewValue();
}
