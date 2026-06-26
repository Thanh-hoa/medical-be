package com.example.medical_be.i18n;
public interface IMessageTranslator {
    String getMessage(String key );
    String getMessage(String key , Object[] args);
}
