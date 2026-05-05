package com.example.medical_be.service.mailapi;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {
     // → Công cụ để GỬI email, Spring tự inject từ config trong application.yml
    private final JavaMailSender mailSender;

    // → Công cụ để ĐỌC file HTML template (activation-request.html)
    //   và điền dữ liệu vào (tên, link...)
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;


     public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            // → Tạo một "phong bì email" trống
            MimeMessage message = mailSender.createMimeMessage();
            
            // → Công cụ giúp điền thông tin vào phong bì dễ hơn
            MimeMessageHelper helper = new MimeMessageHelper(
                message, 
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Async
    @Override
    public CompletableFuture<Void> sendTemplateEmailAsync(String to, String subject, String templateName, Map<String, Object> templateVariables, Locale locale) {
        try {
            // String htmlContent = templateEngine.processTemplate(templateName, templateVariables, locale);
            
            // → Tạo "hộp chứa dữ liệu" cho Thymeleaf
            Context context = new Context(locale);
             
            // → Đổ dữ liệu vào hộp
            //   ví dụ: name="Nguyễn Văn A", activationLink="http://..."
            context.setVariables(templateVariables);

            // → Thymeleaf đọc file activation-request.html
            //   + điền dữ liệu vào → ra chuỗi HTML hoàn chỉnh
            String htmlContent = templateEngine.process(templateName, context);

            sendHtmlEmail(to, subject, htmlContent);

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
}
