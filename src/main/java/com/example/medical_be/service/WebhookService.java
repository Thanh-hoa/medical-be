package com.example.medical_be.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.dto.req.webhook.CreateWebhookReq;
import com.example.medical_be.dto.req.webhook.UpdateWebhookReq;
import com.example.medical_be.dto.res.WebhookRes;
import com.example.medical_be.entity.Webhook;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.repository.WebhookRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class WebhookService {

    final WebhookRepository webhookRepository;
    final IMessageTranslator messageTranslator;

    @Transactional
    public WebhookRes create(CreateWebhookReq req) {
        Webhook webhook = Webhook.builder()
                .url(req.getUrl())
                .eventTypes(req.getEventTypes())
                .secret(req.getSecret())
                .isActive(req.getIsActive())
                .build();

        webhook = webhookRepository.save(webhook);
        log.info("Webhook created: {}", webhook.getId());
        return toRes(webhook);
    }

    @Transactional(readOnly = true)
    public List<WebhookRes> list() {
        return webhookRepository.findAll().stream()
                .map(this::toRes)
                .toList();
    }

    @Transactional(readOnly = true)
    public WebhookRes detail(Long id) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("webhook.not_found")));
        return toRes(webhook);
    }

    @Transactional
    public WebhookRes update(Long id, UpdateWebhookReq req) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("webhook.not_found")));

        if (req.getUrl() != null) webhook.setUrl(req.getUrl());
        if (req.getEventTypes() != null) webhook.setEventTypes(req.getEventTypes());
        if (req.getSecret() != null) webhook.setSecret(req.getSecret());
        if (req.getIsActive() != null) webhook.setIsActive(req.getIsActive());
        webhook.setUpdatedAt(LocalDateTime.now());

        webhook = webhookRepository.save(webhook);
        log.info("Webhook updated: {}", id);
        return toRes(webhook);
    }

    @Transactional
    public void delete(Long id) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("webhook.not_found")));
        webhookRepository.delete(webhook);
        log.info("Webhook deleted: {}", id);
    }

    private WebhookRes toRes(Webhook webhook) {
        return WebhookRes.builder()
                .id(webhook.getId())
                .url(webhook.getUrl())
                .eventTypes(webhook.getEventTypes())
                .isActive(webhook.getIsActive())
                .createdAt(webhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .build();
    }
}
