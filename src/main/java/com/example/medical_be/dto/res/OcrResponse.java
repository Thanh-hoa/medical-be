package com.example.medical_be.dto.res;

import java.util.List;
import java.util.Map;

import com.example.medical_be.dto.json.LabResultJson;

public record OcrResponse(
        String filename,
        ParsedData parsedData) {

    public record ParsedData(
            Map<String, String> extractedData,
            List<LabResultJson> labData) {}
}
