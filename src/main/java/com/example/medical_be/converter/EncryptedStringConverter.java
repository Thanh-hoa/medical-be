package com.example.medical_be.converter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.medical_be.util.AESUtil;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Override
    public String convertToDatabaseColumn(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return AESUtil.encrypt(value, secretKey);
    }

    @Override
    public String convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return dbValue;
        }
        return AESUtil.decrypt(dbValue, secretKey);
    }
}
