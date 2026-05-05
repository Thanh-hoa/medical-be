package com.example.medical_be.service.mailapi;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IEmailService {
     CompletableFuture<Void> sendTemplateEmailAsync(String to, String subject, String templateName, Map<String, Object> templateVariables, Locale locale);
}
