package com.example.medical_be.dto.req.medicalRecord;

import java.util.List;

import com.example.medical_be.dto.json.ExtractedDataDto;
import com.example.medical_be.dto.json.LabResultJson;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateMedicalRecordDetailReq(
        @NotNull Long id,
        String department,
        String recordType,
        String notes,
        @Valid UpdateMedicalRecordPatientReq patient,
        ExtractedDataDto extractedData,
        List<LabResultJson> labData
) {}
