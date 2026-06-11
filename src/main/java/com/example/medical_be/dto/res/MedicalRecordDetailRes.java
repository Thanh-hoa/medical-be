package com.example.medical_be.dto.res;

import java.time.LocalDateTime;
import java.util.List;

import com.example.medical_be.dto.json.ExtractedDataDto;
import com.example.medical_be.dto.json.LabResultJson;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalRecordDetailRes {
    Long id;
    String recordNumber;
    String status;
    String department;
    String recordType;
    String fileName;
    String fileType;
    String originalImagePath;
    String notes;
    Long uploadedBy;
    Long verifiedBy;
    LocalDateTime verifiedAt;
    Long approvedBy;
    LocalDateTime approvedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    PatientRes patient;
    ExtractedDataDto extractedData;
    List<LabResultJson> labData;
}
