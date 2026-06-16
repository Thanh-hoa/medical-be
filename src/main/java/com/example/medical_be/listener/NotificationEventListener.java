package com.example.medical_be.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.medical_be.entity.Account;
import com.example.medical_be.entity.Notification.NotificationType;
import com.example.medical_be.event.MedicalRecordRejectedEvent;
import com.example.medical_be.event.MedicalRecordResubmittedEvent;
import com.example.medical_be.event.MedicalRecordSubmittedEvent;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.service.NotificationService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final AccountRepository accountRepository;

    @EventListener
    @Async("taskExecutor")
    public void onMedicalRecordSubmitted(MedicalRecordSubmittedEvent event) {
        try {
            List<Account> doctors = accountRepository.findByRole("doctor");
            for (Account doctor : doctors) {
                notificationService.createNotification(
                        doctor,
                        "Bệnh án chờ duyệt",
                        "Bệnh án " + event.getRecord().getRecordNumber() + " từ nhân viên cần được duyệt",
                        NotificationType.SUBMIT,
                        "MedicalRecord",
                        event.getRecord().getId()
                );
            }
            log.info("Notifications created for submitted record: {}", event.getRecord().getId());
        } catch (Exception e) {
            log.error("Failed to create notifications for submitted record", e);
        }
    }

    @EventListener
    @Async("taskExecutor")
    public void onMedicalRecordRejected(MedicalRecordRejectedEvent event) {
        try {
            Account uploader = event.getRecord().getUploader();
            if (uploader != null) {
                notificationService.createNotification(
                        uploader,
                        "Bệnh án bị từ chối",
                        "Lý do: " + event.getRejectionReason(),
                        NotificationType.REJECT,
                        "MedicalRecord",
                        event.getRecord().getId()
                );
            }
            log.info("Notifications created for rejected record: {}", event.getRecord().getId());
        } catch (Exception e) {
            log.error("Failed to create notifications for rejected record", e);
        }
    }

    @EventListener
    @Async("taskExecutor")
    public void onMedicalRecordResubmitted(MedicalRecordResubmittedEvent event) {
        try {
            List<Account> doctors = accountRepository.findByRole("doctor");
            for (Account doctor : doctors) {
                notificationService.createNotification(
                        doctor,
                        "Bệnh án được nộp lại",
                        "Bệnh án " + event.getRecord().getRecordNumber() + " đã được nhân viên nộp lại và cần được duyệt",
                        NotificationType.RESUBMIT,
                        "MedicalRecord",
                        event.getRecord().getId()
                );
            }
            log.info("Notifications created for resubmitted record: {}", event.getRecord().getId());
        } catch (Exception e) {
            log.error("Failed to create notifications for resubmitted record", e);
        }
    }
}
