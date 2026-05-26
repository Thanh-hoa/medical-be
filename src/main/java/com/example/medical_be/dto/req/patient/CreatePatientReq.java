package com.example.medical_be.dto.req.patient;

import jakarta.validation.constraints.NotBlank;

public record CreatePatientReq(
        @NotBlank String bhyt,
        @NotBlank String name,
        String dob,     
        String gender,   
        String address,
        String phone
) {}
