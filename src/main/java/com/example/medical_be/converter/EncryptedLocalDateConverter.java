package com.example.medical_be.converter;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.medical_be.util.AESUtil;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
@Component
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Override
    public String convertToDatabaseColumn(LocalDate value) {
        if (value == null) {
            return null;
        }
        return AESUtil.encrypt(value.toString(), secretKey);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }
        return LocalDate.parse(AESUtil.decrypt(dbValue, secretKey));
    }
}
