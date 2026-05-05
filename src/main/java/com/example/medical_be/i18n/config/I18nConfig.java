package com.example.medical_be.i18n.config;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class I18nConfig {
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
}
