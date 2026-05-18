package com.example.medical_be.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MedicalRecordStatusConverter implements AttributeConverter<MedicalRecordStatus, String> {

    @Override
    public String convertToDatabaseColumn(MedicalRecordStatus status) {
        return status == null ? null : status.getDbValue();
    }

    @Override
    public MedicalRecordStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MedicalRecordStatus.fromDbValue(dbData);
    }
}
