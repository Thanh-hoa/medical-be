package com.example.medical_be.dto.req.medicalRecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateExtractedFieldReq(
        @NotNull Long recordId,
        @NotBlank String fieldName,
        String fieldValue) {}
