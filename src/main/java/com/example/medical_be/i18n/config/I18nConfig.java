package com.example.medical_be.i18n.config;
import java.util.List;
import java.util.Locale;

import jakarta.annotation.PostConstruct;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class I18nConfig {
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");

    @PostConstruct
    public void setDefaultLocale() {
        Locale.setDefault(VIETNAMESE);
    }

    @Bean
    public MessageSource messageSource(){
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        // Thiết lập tên file properties (không cần đuôi .properties)
        messageSource.setBasename("lang/messages");

        // Thiết lập encoding để hỗ trợ ký tự đặc biệt (Tiếng Việt, v.v.)
        messageSource.setDefaultEncoding("UTF-8");

        // Thời gian cache (ms), -1 = không cache
        messageSource.setCacheSeconds(3600);

        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(VIETNAMESE);
        resolver.setSupportedLocales(List.of(VIETNAMESE, Locale.forLanguageTag("vi")));
        return resolver;
    }
}
