package com.example.medical_be.dto.req.medicalRecord;

public record UpdateMedicalRecordPatientReq(
        String bhyt,
        String name,
        String dob,
        String gender,
        String address,
        String phone
) {}
