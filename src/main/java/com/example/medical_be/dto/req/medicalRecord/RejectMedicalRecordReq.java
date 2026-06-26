package com.example.medical_be.dto.req.medicalRecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RejectMedicalRecordReq(
        @NotNull Long id,
        @NotBlank String rejectionReason
) {}
