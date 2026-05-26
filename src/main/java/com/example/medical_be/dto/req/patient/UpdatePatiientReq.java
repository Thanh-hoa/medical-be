package com.example.medical_be.dto.req.patient;

import jakarta.validation.constraints.NotBlank;

public record UpdatePatiientReq(
        Long id,
        @NotBlank String bhyt,
        @NotBlank String name,
        String dob,     
        String gender,   
        String address,
        String phone
) {
}
