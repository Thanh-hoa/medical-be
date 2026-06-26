package com.example.medical_be.event;

import java.time.LocalDateTime;
import org.springframework.context.ApplicationEvent;
import com.example.medical_be.entity.MedicalRecord;
import lombok.Getter;

@Getter
public abstract class MedicalRecordEvent extends ApplicationEvent {
    private final MedicalRecord record;
    private final Long actorId;
    private final LocalDateTime occurredAt;

    public MedicalRecordEvent(Object source, MedicalRecord record, Long actorId) {
        super(source);
        this.record = record;
        this.actorId = actorId;
        this.occurredAt = LocalDateTime.now();
    }

    public abstract String getAction();
    public abstract String getOldValue();
    public abstract String getNewValue();
}
