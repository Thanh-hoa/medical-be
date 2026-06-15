package com.example.medical_be.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.medical_be.event.MedicalRecordEvent;
import com.example.medical_be.service.AuditLogService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {

    private final AuditLogService auditLogService;

    @EventListener
    public void onMedicalRecordEvent(MedicalRecordEvent event) {
        try {
            auditLogService.log(
                    event.getAction(),
                    AuditLogService.RESOURCE_MEDICAL_RECORD,
                    event.getRecord().getId(),
                    event.getOldValue(),
                    event.getNewValue(),
                    event.getActorId()
            );
            log.info("Audit log created for action: {}", event.getAction());
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", event.getAction(), e);
        }
    }
}
