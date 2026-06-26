package com.example.medical_be.i18n;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageTranslatorImpl implements IMessageTranslator{
    private final MessageSource messageSource;
    @Override
    public String getMessage(String key) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key,null ,currentLocale);
    }

    @Override
    public String getMessage(String key, Object[] args) {
       Locale locale = LocaleContextHolder.getLocale();
       return  messageSource.getMessage(key , args , locale);
    }
}
