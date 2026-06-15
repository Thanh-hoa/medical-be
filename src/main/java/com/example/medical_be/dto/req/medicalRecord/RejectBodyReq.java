package com.example.medical_be.dto.req.medicalRecord;

import jakarta.validation.constraints.NotBlank;

public record RejectBodyReq(
        @NotBlank String rejectionReason
) {}
