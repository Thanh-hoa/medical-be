package com.example.medical_be.support;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.example.medical_be.entity.Account;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.service.mailapi.IEmailService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountSentMailHelperService {
    private final IEmailService emailService;
    private final IMessageTranslator messageTranslator;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendMailActiveAccount(Account account , String activationToken) {
          try {
            Map<String, Object> templateVariables = variablesEmailActiveAccount(account, activationToken);
            String subject = messageTranslator.getMessage("mail.activation.request.subject");
            sendEmailAsync(account,subject, templateVariables, "activation-request");
        } catch (Exception e) {
            log.error("Failed to send activation email to {}", account.getEmail(), e);
        }
    }

    public void sendInfoAccountEmailSafely(Account account, String defaultPassword) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("name", account.getName() != null && !account.getName().isEmpty()
                    ? account.getName()
                    : account.getEmail());
            templateVariables.put("email", account.getEmail());
            templateVariables.put("password", defaultPassword);

            String subject = messageTranslator.getMessage("mail.account.info.subject");
            sendEmailAsync(account, subject, templateVariables, "account-info");
        } catch (Exception e) {
            log.error("Failed to send account info email to {}", account.getEmail(), e);
        }
    }

     private void sendEmailAsync(Account account,String subject, Map<String, Object> templateVariables, String templateName) {
        Locale currentLocale = LocaleContextHolder.getLocale();

        emailService.sendTemplateEmailAsync(
                account.getEmail(),
                subject,
                templateName,
                templateVariables,
                currentLocale).exceptionally(ex -> {
                    log.error("Email sending failed for {}", account.getEmail(), ex);
                    return null;
                });
    }

    public void sendPasswordResetEmail(Account account, String resetToken) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("name", account.getName() != null && !account.getName().isEmpty()
                    ? account.getName()
                    : account.getEmail());
            templateVariables.put("resetLink", frontendUrl + "/reset-password?token=" + resetToken);

            String subject = messageTranslator.getMessage("mail.reset_password.subject");
            sendEmailAsync(account, subject, templateVariables, "reset-password");
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", account.getEmail(), e);
        }
    }

    private Map<String, Object> variablesEmailActiveAccount(Account account, String activationToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", account.getName() != null && !account.getName().isEmpty()
                ? account.getName()
                : account.getEmail());
        variables.put("activationLink", frontendUrl + "/verify-email?token=" + activationToken);

        return variables;
    }
}