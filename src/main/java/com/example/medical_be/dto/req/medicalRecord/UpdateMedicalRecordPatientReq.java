package com.example.medical_be.dto.req.medicalRecord;

public record UpdateMedicalRecordPatientReq(
        String bhyt,
        String citizenId,
        String name,
        String dob,
        String gender,
        String address,
        String phone
) {}
