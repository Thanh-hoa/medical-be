package com.example.medical_be.dto.req.medicalRecord;

public record MedicalRecordListReq(
        Integer page,
        Integer limit,
        String sortBy,
        String orderBy,
        String status,   
        Long patientId,  
        String q         
) {}
