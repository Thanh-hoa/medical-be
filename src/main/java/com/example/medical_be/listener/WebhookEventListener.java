package com.example.medical_be.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.medical_be.entity.Webhook;
import com.example.medical_be.event.MedicalRecordEvent;
import com.example.medical_be.repository.WebhookRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventListener {

    private final WebhookRepository webhookRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @EventListener
    @Async("taskExecutor")
    public void onMedicalRecordEvent(MedicalRecordEvent event) {
        try {
            List<Webhook> webhooks = webhookRepository.findAllByIsActiveTrue();
            String eventType = event.getAction().toLowerCase();

            for (Webhook webhook : webhooks) {
                if (webhook.getEventTypes() != null
                    && webhook.getEventTypes().contains(event.getAction())) {
                    sendWebhook(webhook, event, eventType);
                }
            }
        } catch (Exception e) {
            log.error("Failed to send webhooks for event: {}", event.getAction(), e);
        }
    }

    private void sendWebhook(Webhook webhook, MedicalRecordEvent event, String eventType) {
        try {
            Map<String, Object> payload = buildPayload(event, eventType);
            String jsonPayload = objectMapper.writeValueAsString(payload);
            String signature = generateHmac(jsonPayload, webhook.getSecret());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Signature", "sha256=" + signature);
            headers.set("X-Webhook-Event", eventType);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            restTemplate.postForEntity(webhook.getUrl(), request, Void.class);
            log.info("Webhook sent successfully to: {}", webhook.getUrl());
        } catch (Exception e) {
            log.error("Failed to send webhook to: {}", webhook.getUrl(), e);
        }
    }

    private Map<String, Object> buildPayload(MedicalRecordEvent event, String eventType) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", "medical-record." + eventType);
        payload.put("medicalRecordId", event.getRecord().getId());
        payload.put("recordNumber", event.getRecord().getRecordNumber());
        payload.put("status", event.getRecord().getStatus().toString());
        payload.put("actorId", event.getActorId());
        payload.put("timestamp", LocalDateTime.now());

        Map<String, Object> data = new LinkedHashMap<>();
        if (event.getRecord().getUploader() != null) {
            data.put("uploaderName", event.getRecord().getUploader().getName());
            data.put("uploaderEmail", event.getRecord().getUploader().getEmail());
        }
        if (event.getRecord().getPatient() != null) {
            data.put("patientName", event.getRecord().getPatient().getName());
            data.put("patientBhyt", event.getRecord().getPatient().getBhyt());
        }
        payload.put("data", data);

        return payload;
    }

    private String generateHmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            byte[] bytes = mac.doFinal(payload.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }
}
