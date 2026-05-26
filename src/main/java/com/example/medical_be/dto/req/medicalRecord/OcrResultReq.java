package com.example.medical_be.dto.req.medicalRecord;

import java.util.List;
import java.util.Map;

import com.example.medical_be.dto.json.LabResultJson;

import jakarta.validation.constraints.NotNull;

public record OcrResultReq(
        @NotNull Long recordId,
        Map<String, String> extractedData,
        List<LabResultJson> labData) {}
