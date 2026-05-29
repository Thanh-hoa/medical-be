package com.example.medical_be.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LabResultJson(
        String testName,
        String testValue,
        String unit,
        String referenceRange,
        Boolean isAbnormal) {}
